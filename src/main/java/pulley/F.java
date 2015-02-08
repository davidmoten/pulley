package pulley;

import pulley.util.Optional;

public final class F {

	public static <T, R> F1<Optional<T>, Optional<R>> optional(
			final F1<? super T, ? extends R> f) {
		return new F1<Optional<T>, Optional<R>>() {
			@Override
			public Optional<R> call(Optional<T> t) {
				if (t.isPresent())
					return Optional.of((R) f.call(t.get()));
				else
					return Optional.absent();
			}
		};
	}

	public static <T, R> F1<Cons<T>, Cons<R>> cons(
			final F1<? super T, ? extends R> f) {
		return new F1<Cons<T>, Cons<R>>() {
			@Override
			public Cons<R> call(Cons<T> c) {
				return new Cons<R>(f.call(c.head()), c.tail().map(f));
			}
		};
	}
}
