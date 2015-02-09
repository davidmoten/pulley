package pulley;

import static pulley.Util.unexpected;

public class CompletedPromise<T> implements Promise<T> {

	private final T value;

	public CompletedPromise(T t) {
		this.value = t;
	}

	@Override
	public void complete(T value) {
		unexpected();
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public T get() {
		return value;
	}

}