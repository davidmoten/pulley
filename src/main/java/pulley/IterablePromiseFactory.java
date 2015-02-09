package pulley;

import static pulley.Cons.cons;
import static pulley.Stream.stream;
import static pulley.util.Optional.absent;
import static pulley.util.Optional.of;

import java.util.Iterator;

import pulley.util.Optional;

public class IterablePromiseFactory<T> implements
		PromiseFactory<Optional<Cons<T>>> {

	private final Iterable<T> iterable;

	public IterablePromiseFactory(Iterable<T> iterable) {
		this.iterable = iterable;
	}

	@Override
	public Promise<Optional<Cons<T>>> create() {
		return createPromiseFactory(iterable.iterator()).create();
	}

	private PromiseFactory<Optional<Cons<T>>> createPromiseFactory(
			final Iterator<T> it) {
		return new IteratorPromiseFactory<T>(it);
	}

	private static class IteratorPromiseFactory<T> implements
			PromiseFactory<Optional<Cons<T>>> {
		private final Iterator<T> iterator;

		IteratorPromiseFactory(Iterator<T> iterator) {
			this.iterator = iterator;
		}

		@Override
		public Promise<Optional<Cons<T>>> create() {
			return new FunctionPromise<Optional<Cons<T>>>(
					new F0<Optional<Cons<T>>>() {
						@Override
						public Optional<Cons<T>> call() {
							if (iterator.hasNext()) {
								return of(cons(iterator.next(),
										stream(IteratorPromiseFactory.this)));
							} else
								return absent();
						}
					});
		}
	}
}
