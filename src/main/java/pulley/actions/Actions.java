package pulley.actions;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pulley.TimeProvider;
import pulley.util.Optional;

public class Actions {

	private static final Logger log = LoggerFactory.getLogger(Actions.class);

	public static A1<Object> println() {
		return new A1<Object>() {
			@Override
			public void call(Object t) {
				System.out.println(t);
			}
		};
	}

	public static A0 println(final String s) {
		return new A0() {
			@Override
			public void call() {
				System.out.println(s);
			}
		};
	}

	public static A0 log(final String s) {
		return new A0() {
			@Override
			public void call() {
				log.info(s);
			}
		};
	}

	public static <T> A1<T> log() {
		return new A1<T>() {
			@Override
			public void call(T t) {
				log.info(String.valueOf(t));
			}
		};
	}

	public static <T> A1<T> addToList(final List<T> list) {
		return new A1<T>() {
			@Override
			public void call(T t) {
				list.add(t);
			}
		};
	}

	public static A1<Object> doNothing1() {
		return new A1<Object>() {
			@Override
			public void call(Object t) {
				// do nothing
			}
		};
	}

	public static A0 sequence(final A0 action1, final A0 action2) {
		return new A0() {

			@Override
			public void call() {
				action1.call();
				action2.call();
			}
		};
	}

	public static <T> A1<T> sequence(final A1<? super T> action1,
			final A1<? super T> action2) {
		return new A1<T>() {

			@Override
			public void call(T t) {
				action1.call(t);
				action2.call(t);
			}
		};
	}

	private static final A0 DO_NOTHING = new A0() {
		@Override
		public void call() {
			// do nothing
		}
	};

	public static A0 doNothing0() {
		return DO_NOTHING;
	}

	public static Runnable toRunnable(final A0 action) {
		return new Runnable() {

			@Override
			public void run() {
				action.call();
			}
		};
	}

	public static <T> Latest<T> latest() {
		return new Latest<T>();
	}

	public static class Latest<T> implements A1<T> {

		private volatile Optional<T> latest = Optional.absent();

		@Override
		public void call(T t) {
			latest = Optional.of(t);
		}

		public Optional<T> get() {
			return latest;
		}

	}

	public static class ActionLatest<T> implements A1<T> {

		private volatile T latest;

		@Override
		public void call(T t) {
			latest = t;
		}

		public T get() {
			return latest;
		}
	}

	public static class ActionSleeping implements A0 {

		private final A0 action;
		private final long time;
		private final TimeProvider timeProvider;

		public ActionSleeping(TimeProvider timeProvider, A0 action,
				long duration, TimeUnit unit) {
			this.action = action;
			this.timeProvider = timeProvider;
			this.time = timeProvider.now() + unit.toMillis(duration);
		}

		@Override
		public void call() {
			long t = time - timeProvider.now();
			if (t > 0)
				try {
					Thread.sleep(t);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			action.call();
		}

	}

	public static A1<Integer> increment(final AtomicInteger i) {
		return new A1<Integer>() {

			@Override
			public void call(Integer t) {
				i.incrementAndGet();
			}
		};
	}

	public static <T> A0 toA0(final A1<? super T> action, final T t) {
		return new A0() {

			@Override
			public void call() {
				action.call(t);
			}
		};
	}

}
