package pulley;

public class CompletedPromiseFactory<T> implements PromiseFactory<T> {

	private final T t;

	public CompletedPromiseFactory(T t) {
		this.t = t;
	}

	public static <T> CompletedPromiseFactory<T> completedPromiseFactory(T t) {
		return new CompletedPromiseFactory<T>(t);
	}

	@Override
	public Promise<T> create() {
		return CompletedPromise.completedPromise(t);
	}

}
