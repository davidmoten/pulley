package pulley;

import com.google.common.base.Optional;

public class Stream<T> {
	private final Promise<Optional<Cons<T>>> promise;

	public Stream(Promise<Optional<Cons<T>>> promise) {
		this.promise = promise;
	}

	public <R> Stream<R> map(F1<? super T, ? extends R> f) {
		return new Stream<R>(promise.map(F.optional(F.cons(f))));
	}

	public void forEach(A1<T> action) {
		// TODO
		promise.start();
	}
}