package pulley;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class FunctionPromise<T> implements Promise<T> {

	private final F0<T> f;
	private final AtomicBoolean completed = new AtomicBoolean();
	private volatile T value;

	public FunctionPromise(F0<T> f) {
		this.f = f;
	}

	@Override
	public Future start() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void complete(Object value) {
		// TODO
	}

	@Override
	public boolean isCompleted() {
		return completed.get();
	}

	@Override
	public void failure(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public T get() {
		if (completed.compareAndSet(false, true))
			value = f.call();
		return value;
	}

	@Override
	public Promise<T> map(F1 f) {
		// TODO Auto-generated method stub
		return null;
	}

}
