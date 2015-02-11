package pulley;

import static pulley.util.Optional.of;

import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import pulley.util.Optional;

public class Stream<T> {
	private final Factory<Promise<Optional<Cons<T>>>> factory;

	public Stream(Factory<Promise<Optional<Cons<T>>>> factory) {
		this.factory = factory;
	}

	public Factory<Promise<Optional<Cons<T>>>> factory() {
		return factory;
	}

	public static <T> Stream<T> stream(
			Factory<Promise<Optional<Cons<T>>>> factory) {
		return new Stream<T>(factory);
	}

	public <R> Stream<R> map(final F1<? super T, ? extends R> f) {
		final F1<Optional<Cons<T>>, Optional<Cons<R>>> g = F
				.optional(F.cons(f));
		Factory<Promise<Optional<Cons<R>>>> factory2 = new Factory<Promise<Optional<Cons<R>>>>() {
			@Override
			public Promise<Optional<Cons<R>>> create() {
				final Promise<Optional<Cons<T>>> p = factory.create();
				return new Promise<Optional<Cons<R>>>() {
					@Override
					public Optional<Cons<R>> get() {
						return g.call(p.get());
					}

					@Override
					public A0 closeAction() {
						return p.closeAction();
					}

					@Override
					public Scheduler scheduler() {
						return Schedulers.immediate();
					}
				};
			}

		};
		return stream(factory2);
	}

	public Stream<List<T>> toList() {
		Factory<Promise<Optional<Cons<List<T>>>>> factory2 = new Factory<Promise<Optional<Cons<List<T>>>>>() {
			@Override
			public Promise<Optional<Cons<List<T>>>> create() {
				final Promise<Optional<Cons<T>>> p = factory.create();
				Promise<Optional<Cons<List<T>>>> p2 = new Promise<Optional<Cons<List<T>>>>() {

					@Override
					public Optional<Cons<List<T>>> get() {
						final Deque<Optional<T>> queue = new ConcurrentLinkedDeque<Optional<T>>();
						A1 addToQueue = new A1<T>() {
							@Override
							public void call(T t) {
								queue.push(of(t));
							}
						};

					}

					@Override
					public A0 closeAction() {
						return p.closeAction();
					}

					@Override
					public Scheduler scheduler() {
						return p.scheduler();
					}
				};
			}
		};
		return stream(factory2);
	}

	public void forEach(A1<? super T> action) {
		Promise<Optional<Cons<T>>> p = factory.create();
		while (true) {
			Optional<Cons<T>> value = p.get();
			if (value.isPresent()) {
				action.call(value.get().head());
				p = value.get().tail();
			} else {
				p.closeAction().call();
				return;
			}
		}
	}

	public void forEach() {
		forEach(Actions.doNothing1());
	}

	public T single() {
		Optional<Cons<T>> c = factory.create().get();
		final T value;
		if (c.isPresent())
			value = c.get().head();
		else
			throw new RuntimeException("expected one item but no items emitted");
		Optional<Cons<T>> c2 = c.get().tail().get();
		if (c2.isPresent())
			throw new RuntimeException(
					"expected one item but more than one emitted");
		else
			return value;
	}

}