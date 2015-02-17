package pulley.streams;

import static pulley.Cons.cons;
import static pulley.Stream.stream;

import java.util.concurrent.atomic.AtomicInteger;

import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.util.Optional;

public class Range {

    public static Stream<Integer> create(final int start, final int count) {
        Factory<Promise<Optional<Cons<Integer>>>> factory = new Factory<Promise<Optional<Cons<Integer>>>>() {

            @Override
            public Promise<Optional<Cons<Integer>>> create() {
                return new RangePromise(start, start + count - 1);
            }
        };
        return stream(factory);
    }

    private static class RangePromise implements StreamPromise<Integer> {

        private final int maxValue;
        private final int n;

        RangePromise(int n, int maxValue) {
            this.n = n;
            this.maxValue = maxValue;
        }

        @Override
        public Optional<Cons<Integer>> get() {
            if (n > maxValue)
                return Optional.absent();
            else
                return Optional.of(cons(n, new RangePromise(n + 1, maxValue)));
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
