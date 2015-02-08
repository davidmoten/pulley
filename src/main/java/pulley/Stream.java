package pulley;

import static pulley.util.Optional.of;
import pulley.util.Optional;

public class Stream<T> {
	private final Promise<Optional<Cons<T>>> promise;

	private static Stream<?> EMPTY = create(new CompletedPromise<Optional<Cons<Object>>>(
			Optional.<Cons<Object>> absent()));

	public Stream(Promise<Optional<Cons<T>>> promise) {
		this.promise = promise;
	}

	public Promise<Optional<Cons<T>>> promise() {
		return promise;
	}

	public static <T> Stream<T> create(Promise<Optional<Cons<T>>> promise) {
		return new Stream<T>(promise);
	}

	public <R> Stream<R> map(F1<? super T, ? extends R> f) {
		return new Stream<R>(promise.map(F.optional(F.cons(f))));
	}

	public static <T> Stream<T> just(T t) {
		return create(CompletedPromise.create(of(Cons.create(t,
				Stream.<T> empty()))));
	}

	public static <T> Stream<T> just(T t1, T t2) {
		return create(CompletedPromise.create(of(Cons.create(t1, just(t2)))));
	}

	public static <T> Stream<T> from(Iterable<T> iterable) {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> empty() {
		return (Stream<T>) EMPTY;
	}

	public void forEach(A1<? super T> action) {
		Promise<Optional<Cons<T>>> p = promise;
		while (true) {
			p.start();
			Optional<Cons<T>> value = p.get();
			if (value.isPresent()) {
				action.call(value.get().head());
				p = p.get().get().tail().promise();
			} else
				return;
		}
	}

	public T single() {
		Optional<Cons<T>> c = promise.get();
		final T value;
		if (c.isPresent())
			value = c.get().head();
		else
			throw new RuntimeException("expected one item but no items emitted");
		Optional<Cons<T>> c2 = c.get().tail().promise().get();
		if (c2.isPresent())
			throw new RuntimeException(
					"expected one item but more than one emitted");
		else
			return value;
	}

}