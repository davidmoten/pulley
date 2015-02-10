package pulley.streams;

import static pulley.Stream.stream;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import pulley.A0;
import pulley.Actions;
import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.util.Optional;

public class Merge {
	public static <T> Stream<T> create(Stream<T> s1, Stream<T> s2) {
		return stream(factory(s1, s2));
	}

	private static <T> Factory<Promise<Optional<Cons<T>>>> factory(
			final Stream<T> s1, final Stream<T> s2) {
		return new Factory<Promise<Optional<Cons<T>>>>() {
			@Override
			public Promise<Optional<Cons<T>>> create() {
				Promise<Optional<Cons<T>>> p1 = s1.factory().create();
				Promise<Optional<Cons<T>>> p2 = s2.factory().create();
				return new MergePromise<T>(p1, p2);
			}
		};
	}

	private static class MergePromise<T> implements Promise<Optional<Cons<T>>> {

		private Promise<Optional<Cons<T>>> p1;
		private Promise<Optional<Cons<T>>> p2;

		public MergePromise(Promise<Optional<Cons<T>>> p1,
				Promise<Optional<Cons<T>>> p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		@Override
		public Optional<Cons<T>> get() {
			// blocking !!!!
			final AtomicReference<Byte> which = new AtomicReference<Byte>(
					(byte) 0);
			final AtomicReference<Optional<Cons<T>>> ref = new AtomicReference<Optional<Cons<T>>>();
			final CountDownLatch latch = new CountDownLatch(1);
			p1.scheduler().schedule(new A0() {
				@Override
				public void call() {
					Optional<Cons<T>> value = p1.get();
					if (which.compareAndSet((byte) 0, (byte) 1)) {
						ref.set(value);
						latch.countDown();
					}
				}
			});
			p2.scheduler().schedule(new A0() {
				@Override
				public void call() {
					Optional<Cons<T>> value = p2.get();
					if (which.compareAndSet((byte) 0, (byte) 2)) {
						ref.set(value);
						latch.countDown();
					}
				}
			});
			try {
				latch.await();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public A0 closeAction() {
			return Actions.sequence(p1.closeAction(), p2.closeAction());
		}

		@Override
		public Scheduler scheduler() {
			return Schedulers.immediate();
		}

	}
}
