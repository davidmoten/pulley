package pulley;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class FunctionPromise<T> implements Promise<T> {

	private final F0<T> f;
	private final AtomicBoolean completed = new AtomicBoolean();

	public FunctionPromise(F0<T> f) {
		this.f = f;
	}

	@Override
	public Future<T> start() {
		completed.set(false);
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
		T value = f.call();
		completed.set(true);
		return value;
	}

	@Override
	public <R> Promise<R> map(final F1<? super T, R> g) {
		return new FunctionPromise<R>(new F0<R>() {
			@Override
			public R call() {
				return g.call(get());
			}
		});

	}

}
