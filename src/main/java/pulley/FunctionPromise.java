package pulley;

import java.util.concurrent.atomic.AtomicBoolean;

public class FunctionPromise<T> implements Promise<T> {

	private final F0<T> f;
	private final AtomicBoolean completed = new AtomicBoolean();

	public FunctionPromise(F0<T> f) {
		this.f = f;
	}

	@Override
	public void complete(Object value) {
		Util.unexpected();
	}

	@Override
	public boolean isCompleted() {
		return completed.get();
	}

	@Override
	public T get() {
		T value = f.call();
		completed.set(true);
		return value;
	}

}
