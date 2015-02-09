package pulley;

import static pulley.Cons.cons;
import static pulley.util.Optional.absent;
import static pulley.util.Optional.of;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import pulley.util.Optional;

public class IterablePromiseFactory<T> implements
		Factory<Promise<Optional<Cons<T>>>> {

	private final Iterable<T> iterable;

	public IterablePromiseFactory(Iterable<T> iterable) {
		this.iterable = iterable;
	}

	@Override
	public Promise<Optional<Cons<T>>> create() {
		return createPromiseFactory(iterable.iterator()).create();
	}

	private Factory<Promise<Optional<Cons<T>>>> createPromiseFactory(
			final Iterator<T> it) {
		return new IteratorPromiseFactory<T>(it);
	}

	private static class IteratorPromiseFactory<T> implements
			Factory<Promise<Optional<Cons<T>>>> {
		private final Iterator<T> iterator;

		IteratorPromiseFactory(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public Promise<Optional<Cons<T>>> create() {
			final AtomicReference<FunctionPromise<Optional<Cons<T>>>> ref = new AtomicReference<FunctionPromise<Optional<Cons<T>>>>();
			ref.set(new FunctionPromise<Optional<Cons<T>>>(
					new F0<Optional<Cons<T>>>() {
						@Override
						public Optional<Cons<T>> call() {
							if (iterator.hasNext()) {
								return of(cons(iterator.next(), ref.get()));
							} else
								return absent();
						}
					}));
			return ref.get();
		}
	}

}
