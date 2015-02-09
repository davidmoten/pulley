package pulley;

public class CompletedPromiseFactory<T> implements PromiseFactory<T> {

	private final T t;

	public CompletedPromiseFactory(T t) {
		this.t = t;
	}

	@Override
	public Promise<T> create() {
		return Promises.completedPromise(t);
	}

}
