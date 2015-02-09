package pulley;

import java.util.Iterator;

public class IterablePromise<T> implements Promise<T> {

	private final Iterator<T> iterator;

	public IterablePromise(Iterator<T> iterator) {
		this.iterator = iterator;
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
	public T get() {
		return iterator.next();
	}

}
