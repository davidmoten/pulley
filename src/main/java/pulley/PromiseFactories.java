package pulley;

import static pulley.CompletedPromise.completedPromise;
import pulley.util.Optional;

public class PromiseFactories {

	public static final <T, R> PromiseFactory<Optional<Cons<R>>> map(
			final PromiseFactory<Optional<Cons<T>>> factory,
			final F1<? super T, ? extends R> f) {
		return new PromiseFactory<Optional<Cons<R>>>() {

			@Override
			public Promise<Optional<Cons<R>>> create() {
				return completedPromise(F.optional(F.cons(f)).call(
						factory.create().get()));
			}
		};
	}

}
