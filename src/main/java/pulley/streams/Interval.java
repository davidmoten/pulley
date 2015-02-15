package pulley.streams;

import static pulley.Stream.stream;

import java.util.concurrent.TimeUnit;

import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Scheduler;
import pulley.SchedulerDelayed;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.util.Optional;

public class Interval {

    public static Stream<Long> create(final long delay, final TimeUnit unit,
            final Scheduler scheduler) {
        Factory<Promise<Optional<Cons<Long>>>> factory = new Factory<Promise<Optional<Cons<Long>>>>() {

            @Override
            public Promise<Optional<Cons<Long>>> create() {
                SchedulerDelayed delayedScheduler = new SchedulerDelayed(scheduler, delay, unit);
                return new IntervalPromise(0, delayedScheduler);
            }
        };
        return stream(factory);
    }

    private static class IntervalPromise implements StreamPromise<Long> {

        private final long count;
        private final Scheduler scheduler;

        IntervalPromise(long count, Scheduler scheduler) {
            this.count = count;
            this.scheduler = scheduler;
        }

        @Override
        public Optional<Cons<Long>> get() {
            return Optional.of(Cons.cons(count, new IntervalPromise(count + 1, scheduler)));
        }

        @Override
        public A0 closeAction() {
            return Actions.doNothing0();
        }

        @Override
        public Scheduler scheduler() {
            return scheduler;
        }
    }
}
