package pulley.streams;

import static pulley.Cons.cons;
import static pulley.Stream.stream;

import java.util.concurrent.atomic.AtomicInteger;

import pulley.Cons;
import pulley.Factory;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.promises.Promise;
import pulley.util.Optional;

public class Range {

    public static Stream<Integer> create(final int start, final int count) {
        Factory<Promise<Optional<Cons<Integer>>>> factory = new Factory<Promise<Optional<Cons<Integer>>>>() {

            @Override
            public Promise<Optional<Cons<Integer>>> create() {
                return new RangePromise(new AtomicInteger(start), start + count);
            }
        };
        return stream(factory);
    }

    private static class RangePromise implements StreamPromise<Integer> {

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

        @Override
        public A0 closeAction() {
            return Actions.doNothing0();
        }

        @Override
        public Scheduler scheduler() {
            return Schedulers.immediate();
        }
    }
}
