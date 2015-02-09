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

	private PromiseFactory<Optional<Cons<T>>> createPromiseFactory(
			final Iterator<T> it) {
		return new PromiseFactory<Optional<Cons<T>>>() {

			@Override
			public Promise<Optional<Cons<T>>> create() {
				return new FunctionPromise<Optional<Cons<T>>>(
						new F0<Optional<Cons<T>>>() {
							@Override
							public Optional<Cons<T>> call() {
								if (it.hasNext()) {
									return of(cons(it.next(),
											stream(createPromiseFactory(it))));
								} else
									return absent();
							}
						});
			}
		};
	}

	@Override
	public Promise<Optional<Cons<T>>> create() {
		return createPromiseFactory(iterable.iterator()).create();
	}

}
