package pulley;

public class Stream<T> {
	private final Promise<Cons<T>> promise;

	public Stream(Promise<Cons<T>> promise) {
		this.promise = promise;
	}

	public <R> Stream<R> map(F1<? super T, ? extends R> f) {

	}
}