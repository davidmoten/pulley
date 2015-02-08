package pulley;

import com.google.common.base.Optional;

public class Stream<T> {
	private final Promise<Optional<Cons<T>>> promise;

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

	public static <T> Stream<T> empty() {
		return create(new CompletedPromise<Optional<Cons<T>>>(
				Optional.<Cons<T>> absent()));
	}

	public void forEach(A1<T> action) {
		// TODO
		promise.start();
	}
}