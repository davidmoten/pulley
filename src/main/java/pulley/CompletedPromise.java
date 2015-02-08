package pulley;

import static pulley.Util.unexpected;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	public void failure(Throwable t) {
		unexpected();
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public Future<T> start() {
		return new Future<T>() {

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public T get() throws InterruptedException, ExecutionException {
				return value;
			}

			@Override
			public T get(long timeout, TimeUnit unit)
					throws InterruptedException, ExecutionException,
					TimeoutException {
				return value;
			}
		};
	}

	@Override
	public <R> Promise<R> map(F1<? super T, R> f) {
		return unexpected();
	}

}
