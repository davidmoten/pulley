package pulley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pulley.actions.A0;
import pulley.actions.A1;
import pulley.actions.Actions;
import pulley.functions.F1;
import pulley.functions.F2;
import pulley.promises.ScheduledPromise;
import pulley.transforms.Buffer;
import pulley.transforms.Concat;
import pulley.transforms.Filter;
import pulley.transforms.Map;
import pulley.transforms.OnNext;
import pulley.transforms.Reduce;
import pulley.transforms.Take;
import pulley.transforms.ToList;
import pulley.util.Optional;

public class Stream<T> {

	private final Factory<Promise<Optional<Cons<T>>>> factory;

	public Stream(Factory<Promise<Optional<Cons<T>>>> factory) {
		this.factory = factory;
	}

	public Factory<? extends Promise<Optional<Cons<T>>>> factory() {
		return factory;
	}

	public static <T> Stream<T> stream(
			Factory<Promise<Optional<Cons<T>>>> factory) {
		return new Stream<T>(factory);
	}

	public <R> Stream<R> transform(final Transformer<T, R> transformer) {
		final StreamFactory<R> f = () -> {
			final Promise<Optional<Cons<T>>> p = factory.create();
			return transformer.transform(p);
		};
		return stream(f);
	}

	public Stream<T> scheduleOn(final Scheduler scheduler) {
		return transform(promise -> new ScheduledPromise<Optional<Cons<T>>>(
				promise, scheduler.worker()));
	}

	public <R> Stream<R> map(final F1<? super T, ? extends R> f) {
		return Map.map(this, f);
	}

	public Stream<T> doOnTerminate(A0 action) {
		// TODO
		return Util.notImplemented();
	}

	public Stream<T> doOnNext(final A1<? super T> action) {
		return OnNext.onNext(this, action);
	}

	public Stream<List<T>> toList() {
		return ToList.toList(this);
	}

	public Stream<List<T>> buffer(final int size) {
		return Buffer.buffer(this, size);
	}

	public <R> Stream<R> reduce(R initial,
			F2<? super R, ? super T, ? extends R> reducer) {
		return Reduce.reduce(this, initial, reducer);
	}

	public Stream<Integer> count() {
		return reduce(0, (count, t) -> count + 1);
	}

	public Stream<T> filter(final F1<? super T, Boolean> predicate) {
		return Filter.filter(this, predicate);
	}

	public Stream<T> concatWith(final Stream<T> stream) {
		return Concat.concat(this, stream);
	}

	public <R> Stream<R> flatMap(F1<T, Stream<R>> f) {
		return Streams.merge(map(f));
	}

	public Stream<T> take(long n) {
		return Take.take(this, n);
	}

	public void forEach(A1<? super T> action) {// //
		forEach(factory.create(), action);
	}

	public static <T> void forEach(Promise<Optional<Cons<T>>> promise,
			final A1<? super T> action) {
		Result<Promise<Optional<Cons<T>>>> p = Result.of(promise);
		do {
			p = Promises.performActionAndAwaitCompletion(p.value().get(),
					action);
		} while (p.value().isPresent());
	}

	public void forEach() {
		forEach(Actions.doNothing1());
	}

	public T single() {
		final Promise<Optional<Cons<T>>> p = factory.create();
		final List<T> list = Collections.synchronizedList(new ArrayList<T>());
		A1<T> addToList = Actions.addToList(list);
		final Result<Promise<Optional<Cons<T>>>> p2 = Promises
				.performActionAndAwaitCompletion(p, addToList);
		if (list.size() == 0) {
			throw new RuntimeException("expected one item but no items emitted");
		} else {
			Promises.performActionAndAwaitCompletion(p2.value().get(),
					addToList);
			if (list.size() > 1)
				throw new RuntimeException(
						"expected one item but more than one emitted");
			else
				return list.get(0);
		}
	}
}