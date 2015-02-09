package pulley;

import static pulley.Cons.cons;
import static pulley.Promises.completedPromiseFactory;
import static pulley.util.Optional.of;
import pulley.util.Optional;

public class Stream<T> {
	private final PromiseFactory<Optional<Cons<T>>> factory;

	private static Stream<?> EMPTY = stream(completedPromiseFactory(Optional
			.<Cons<Object>> absent()));

	public Stream(PromiseFactory<Optional<Cons<T>>> promise) {
		this.factory = promise;
	}

	public PromiseFactory<Optional<Cons<T>>> factory() {
		return factory;
	}

	public static <T> Stream<T> stream(PromiseFactory<Optional<Cons<T>>> factory) {
		return new Stream<T>(factory);
	}

	public <R> Stream<R> map(F1<? super T, ? extends R> f) {
		return stream(PromiseFactories.map(factory, f));
	}

	public static <T> Stream<T> just(T t) {
		return stream(completedPromiseFactory(of(cons(t, Stream.<T> empty()))));
	}

	public static <T> Stream<T> just(T t1, T t2) {
		return stream(completedPromiseFactory(of(cons(t1, just(t2)))));
	}

	public static <T> Stream<T> from(Iterable<T> iterable) {
		// TODO
		return null;
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> empty() {
		return (Stream<T>) EMPTY;
	}

	public void forEach(A1<? super T> action) {
		Promise<Optional<Cons<T>>> p = factory.create();
		while (true) {
			Optional<Cons<T>> value = p.get();
			if (value.isPresent()) {
				action.call(value.get().head());
				p = p.get().get().tail().factory().create();
			} else
				return;
		}
	}

	public T single() {
		Optional<Cons<T>> c = factory.create().get();
		final T value;
		if (c.isPresent())
			value = c.get().head();
		else
			throw new RuntimeException("expected one item but no items emitted");
		Optional<Cons<T>> c2 = c.get().tail().factory().create().get();
		if (c2.isPresent())
			throw new RuntimeException(
					"expected one item but more than one emitted");
		else
			return value;
	}

}