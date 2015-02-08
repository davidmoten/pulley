package pulley;

import pulley.util.Optional;

public class Stream<T> {
	private final Promise<Optional<Cons<T>>> promise;

	private static Stream<?> EMPTY = create(new CompletedPromise<Optional<Cons<Object>>>(
			Optional.<Cons<Object>> absent()));

	public Stream(Promise<Optional<Cons<T>>> promise) {
		this.promise = promise;
	}

	public static <T> Stream<T> create(Promise<Optional<Cons<T>>> promise) {
		return new Stream<T>(promise);
	}

	public <R> Stream<R> map(F1<? super T, ? extends R> f) {
		return new Stream<R>(promise.map(F.optional(F.cons(f))));
	}

	public static <T> Stream<T> just(T t) {
		return create(new CompletedPromise<Optional<Cons<T>>>(
				Optional.of(new Cons<T>(t, Stream.<T> empty()))));
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> empty() {
		return (Stream<T>) EMPTY;
	}

	public void forEasch(A1<T> action) {
		Promise<Optional<Cons<T>>> p = promise;
		boolean keepGoing = true;
		while (keepGoing) {
			p.start();
			Optional<Cons<T>> value = p.get();
			if (value.isPresent()) {
				action.call(value.get().head());
			}
		}
	}
}