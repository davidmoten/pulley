package pulley;

import static pulley.Cons.cons;
import static pulley.Promises.completedPromiseFactory;
import static pulley.Stream.stream;
import static pulley.util.Optional.absent;
import static pulley.util.Optional.of;

import java.util.Arrays;
import java.util.List;

import pulley.streams.FromIterable;
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

    public static <T> Stream<T> just(T... t) {
        return from(Arrays.asList(t));
    }

    @SuppressWarnings("unchecked")
    public static <T> StreamPromise<T> emptyPromise() {
        return (StreamPromise<T>) EMPTY_PROMISE;
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> just(T t1, T t2) {
        return from(Arrays.asList(t1, t2));
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

}
