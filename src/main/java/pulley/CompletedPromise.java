package pulley;


public class CompletedPromise<T> implements Promise<T> {

	private final T value;

	public CompletedPromise(T t) {
		this.value = t;
	}

	@Override
	public T get() {
		return value;
	}

}