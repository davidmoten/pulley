package pulley.streams;

import static pulley.Stream.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import pulley.A0;
import pulley.CachingPromise;
import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.util.Optional;

public class Merge {

    public static <T> Stream<T> create(final List<Stream<T>> list) {
        return stream(factory(list));
    }

    private static <T> Factory<Promise<Optional<Cons<T>>>> factory(final List<Stream<T>> streams) {
        return new Factory<Promise<Optional<Cons<T>>>>() {
            @Override
            public Promise<Optional<Cons<T>>> create() {
                List<Promise<Optional<Cons<T>>>> promises = new ArrayList<Promise<Optional<Cons<T>>>>(
                        streams.size());
                for (Stream<T> stream : streams) {
                    promises.add(stream.factory().create());
                }
                return new MergePromise<T>(promises);
            }
        };
    }

    private static class MergePromise<T> implements Promise<Optional<Cons<T>>> {

        private final List<Promise<Optional<Cons<T>>>> promises;

        MergePromise(List<Promise<Optional<Cons<T>>>> promises) {
            this.promises = promises;
        }

        @Override
        public Optional<Cons<T>> get() {
            // blocking !!!!
            final List<Promise<Optional<Cons<T>>>> promises2 = new ArrayList<Promise<Optional<Cons<T>>>>(
                    promises);
            final AtomicBoolean found = new AtomicBoolean(false);
            final AtomicReference<Optional<Cons<T>>> value = new AtomicReference<Optional<Cons<T>>>();
            final CountDownLatch latch = new CountDownLatch(1);
            final AtomicInteger countTerminated = new AtomicInteger(0);

            for (int i = 0; i < promises.size(); i++) {
                final int index = i;
                promises.get(index).scheduler().schedule(new A0() {
                    @Override
                    public void call() {
                        if (!found.get()) {
                            if (!(promises2.get(index) instanceof CachingPromise))
                                promises2.set(index, new CachingPromise<Optional<Cons<T>>>(
                                        promises2.get(index)));
                            Optional<Cons<T>> t = promises2.get(index).get();
                            if (t.isPresent() && found.compareAndSet(false, true)) {
                                value.set(t);
                                promises2.set(index, t.get().tail());
                                latch.countDown();
                            } else if (!t.isPresent()) {
                                if (countTerminated.incrementAndGet() == promises.size()) {
                                    latch.countDown();
                                }
                            }
                        }
                    }
                });
            }
            try {
                latch.await();
                if (countTerminated.get() == promises.size())
                    return Optional.absent();
                else
                    return Optional.of(Cons.cons(value.get().get().head(), new MergePromise<T>(
                            promises2)));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public A0 closeAction() {
            return new A0() {
                @Override
                public void call() {
                    for (Promise<Optional<Cons<T>>> promise : promises)
                        promise.closeAction().call();
                }
            };
        }

        @Override
        public Scheduler scheduler() {
            return Schedulers.immediate();
        }

    }
}