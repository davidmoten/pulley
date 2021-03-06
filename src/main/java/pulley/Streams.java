package pulley;

import static pulley.Promises.completedPromiseFactory;
import static pulley.Stream.stream;
import static pulley.util.Optional.absent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pulley.actions.A0;
import pulley.actions.A1;
import pulley.actions.Actions;
import pulley.functions.F0;
import pulley.functions.F1;
import pulley.streams.FromIterable;
import pulley.streams.Interval;
import pulley.streams.Range;
import pulley.transforms.Merge;
import pulley.util.Optional;

public class Streams {

	private static Stream<?> EMPTY = stream(completedPromiseFactory(Optional
			.<Cons<Object>> absent()));

	private static StreamPromise<?> EMPTY_PROMISE = new StreamPromise<Object>() {

		@Override
		public Optional<Cons<Object>> get() {
			return absent();
		}

		@Override
		public A0 closeAction() {
			return Actions.doNothing0();
		}

		@Override
		public Scheduler scheduler() {
			return Schedulers.immediate();
		}
	};

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> just(T... t) {
		return from(Arrays.asList(t));
	}

	@SuppressWarnings("unchecked")
	public static <T> StreamPromise<T> emptyPromise() {
		return (StreamPromise<T>) EMPTY_PROMISE;
	}

	public static <T> Stream<T> just(T t1) {
		return from(Arrays.asList(t1));
	}

	public static <T> Stream<T> just(T t1, T t2) {
		return from(Arrays.asList(t1, t2));
	}

	public static <T> Stream<T> just(T t1, T t2, T t3) {
		return from(Arrays.asList(t1, t2, t3));
	}

	public static <T> Stream<T> just(T t1, T t2, T t3, T t4) {
		return from(Arrays.asList(t1, t2, t3, t4));
	}

	public static <T> Stream<T> from(Iterable<T> iterable) {
		return FromIterable.create(iterable);
	}

	public static <T> Stream<T> merge(List<Stream<T>> streams) {
		return Merge.merge(streams);
	}

	public static <T> Stream<T> merge(Stream<Stream<T>> streams) {
		return Merge.merge(streams);
	}

	@SuppressWarnings("unchecked")
	public static <T> Stream<T> empty() {
		return (Stream<T>) EMPTY;
	}

	public static Stream<Integer> range(final int start, final int count) {
		return Range.create(start, count);
	}

	public static <Resource, T> Stream<T> using(F0<Resource> resourceFactory,
			F1<Resource, Stream<T>> streamFactory, A1<Resource> disposeAction) {
		return Util.notImplemented();
	}

	public static Stream<Long> interval(long delay, TimeUnit unit) {
		return interval(delay, unit, Schedulers.computation());
	}

	public static Stream<Long> interval(long delay, TimeUnit unit,
			Scheduler scheduler) {
		return Interval.create(delay, unit, scheduler);
	}
}
