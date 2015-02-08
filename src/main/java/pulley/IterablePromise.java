package pulley;

import java.util.Iterator;
import java.util.concurrent.Future;

public class IterablePromise<T> implements Promise<T> {

	private final Iterator<T> iterator;

	public IterablePromise(Iterator<T> iterator) {
		this.iterator = iterator;
	}

	@Override
	public Future<T> start() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void complete(T value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isCompleted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void failure(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public T get() {
		return iterator.next();
	}

	@Override
	public <R> Promise<R> map(F1<? super T, R> f) {
		// TODO Auto-generated method stub
		return null;
	}

}
