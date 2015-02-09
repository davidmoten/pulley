package pulley;

import static pulley.Cons.cons;
import static pulley.Promises.completedPromiseFactory;
import static pulley.Stream.stream;
import static pulley.util.Optional.absent;
import static pulley.util.Optional.of;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import pulley.util.Optional;

public class Streams {

    private static Stream<?> EMPTY = stream(completedPromiseFactory(Optional
            .<Cons<Object>> absent()));

    private static Promise<Optional<Cons<?>>> EMPTY_PROMISE = new Promise<Optional<Cons<?>>>() {

        @Override
        public Optional<Cons<?>> get() {
            return absent();
        }
    };

    public static <T> Stream<T> just(T t) {
        return stream(completedPromiseFactory(of(cons(t, Streams.<T> emptyPromise()))));
    }

    @SuppressWarnings("unchecked")
    public static <T> Promise<Optional<Cons<T>>> emptyPromise() {
        return (Promise<Optional<Cons<T>>>) ((Promise<?>) EMPTY_PROMISE);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> just(T t1, T t2) {
        return from(Arrays.asList(t1, t2));
    }

    public static <T> Stream<T> from(Iterable<T> iterable) {
        return stream(new IterablePromiseFactory<T>(iterable));
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> empty() {
        return (Stream<T>) EMPTY;
    }

    public static Stream<Integer> range(final int start, final int count) {
        Factory<Promise<Optional<Cons<Integer>>>> factory = new Factory<Promise<Optional<Cons<Integer>>>>() {

            @Override
            public Promise<Optional<Cons<Integer>>> create() {
                return new RangePromise(new AtomicInteger(start), start + count);
            }
        };
        return stream(factory);
    }

    private static class RangePromise implements Promise<Optional<Cons<Integer>>> {

        private final AtomicInteger n;
        private final int maxValue;

        RangePromise(AtomicInteger n, int maxValue) {
            this.n = n;
            this.maxValue = maxValue;
        }

        @Override
        public Optional<Cons<Integer>> get() {
            int m = n.getAndIncrement();
            if (m >= maxValue)
                return Optional.absent();
            else
                return Optional.of(cons(m, new RangePromise(n, maxValue)));
        }

    }

}
