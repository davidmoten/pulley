package pulley.streams;

import static pulley.Stream.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import pulley.A0;
import pulley.A1;
import pulley.CachingPromise;
import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Promises;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.Streams;
import pulley.util.Optional;

public class Merge {

    public static <T> Stream<T> create(final List<Stream<T>> list) {
        // return stream(factory(list));
        return create(Streams.from(list));
    }

    public static <T> Stream<T> create(final Stream<Stream<T>> streams) {
        return Stream.stream(new Factory<Promise<Optional<Cons<T>>>>() {
            @Override
            public Promise<Optional<Cons<T>>> create() {
                return new MergePromise2<T>(Collections.<Promise<Optional<Cons<T>>>> emptyList(),
                        streams.factory().create());
            }
        });
    }

    // private static <T> Factory<Promise<Optional<Cons<T>>>> factory(final
    // List<Stream<T>> streams) {
    // return new Factory<Promise<Optional<Cons<T>>>>() {
    // @Override
    // public Promise<Optional<Cons<T>>> create() {
    // List<Promise<Optional<Cons<T>>>> promises = new
    // ArrayList<Promise<Optional<Cons<T>>>>(
    // streams.size());
    // for (Stream<T> stream : streams) {
    // promises.add(stream.factory().create());
    // }
    // return new MergePromise<T>(promises);
    // }
    // };
    // }

    // private static class MergePromise<T> implements
    // Promise<Optional<Cons<T>>> {
    //
    // private final List<Promise<Optional<Cons<T>>>> promises;
    //
    // MergePromise(List<Promise<Optional<Cons<T>>>> promises) {
    // this.promises = promises;
    // }
    //
    // @Override
    // public Optional<Cons<T>> get() {
    // // blocking !!!!
    // final List<Promise<Optional<Cons<T>>>> promises2 = new
    // ArrayList<Promise<Optional<Cons<T>>>>(
    // promises);
    // final AtomicBoolean found = new AtomicBoolean(false);
    // final AtomicReference<Optional<Cons<T>>> value = new
    // AtomicReference<Optional<Cons<T>>>();
    // final CountDownLatch latch = new CountDownLatch(1);
    // final ConcurrentSkipListSet<Integer> completed = new
    // ConcurrentSkipListSet<Integer>();
    //
    // for (int i = 0; i < promises.size(); i++) {
    // final int index = i;
    // Promise<Optional<Cons<T>>> p = promises.get(index);
    // if (!found.get())
    // p.scheduler().schedule(new A0() {
    // @Override
    // public void call() {
    // if (!found.get()) {
    // promises2.set(index, Promises.cache(promises2.get(index)));
    // Optional<Cons<T>> t = promises2.get(index).get();
    // if (t.isPresent() && found.compareAndSet(false, true)) {
    // value.set(t);
    // promises2.set(index, t.get().tail());
    // latch.countDown();
    // } else if (!t.isPresent()) {
    // completed.add(index);
    // if (completed.size() == promises.size()) {
    // latch.countDown();
    // }
    // }
    // }
    // }
    // });
    // }
    // try {
    // latch.await();
    // if (completed.size() == promises.size())
    // return Optional.absent();
    // else {
    // return Optional.of(Cons.cons(value.get().get().head(), new
    // MergePromise<T>(
    // copyExcluding(promises2, completed))));
    // }
    // } catch (InterruptedException e) {
    // throw new RuntimeException(e);
    // }
    // }
    //
    // private static <T> List<T> copyExcluding(List<T> list, Set<Integer>
    // excludeIndexes) {
    // List<T> list2 = new ArrayList<T>();
    // for (int i = 0; i < list.size(); i++) {
    // if (!excludeIndexes.contains(i))
    // list2.add(list.get(i));
    // }
    // return list2;
    // }
    //
    // @Override
    // public A0 closeAction() {
    // return new A0() {
    // @Override
    // public void call() {
    // for (Promise<Optional<Cons<T>>> promise : promises)
    // promise.closeAction().call();
    // }
    // };
    // }
    //
    // @Override
    // public Scheduler scheduler() {
    // return Schedulers.immediate();
    // }
    //
    // }

    private static class MergePromise2<T> implements Promise<Optional<Cons<T>>> {

        private final Promise<Optional<Cons<Stream<T>>>> streamPromise;
        private final List<Promise<Optional<Cons<T>>>> promises;
        private final Object lock = new Object();

        MergePromise2(List<Promise<Optional<Cons<T>>>> promises,
                Promise<Optional<Cons<Stream<T>>>> streamPromise) {
            this.streamPromise = streamPromise;
            this.promises = promises;
        }

        @Override
        public Optional<Cons<T>> get() {
            // blocking !!!!
            final List<Promise<Optional<Cons<T>>>> promises2 = cached(new ArrayList<Promise<Optional<Cons<T>>>>(
                    promises));
            final AtomicBoolean found = new AtomicBoolean(false);
            final AtomicReference<Optional<Cons<T>>> value = new AtomicReference<Optional<Cons<T>>>();
            final CountDownLatch latch = new CountDownLatch(1);
            final ConcurrentSkipListSet<Integer> completed = new ConcurrentSkipListSet<Integer>();

            for (int i = 0; i < promises2.size(); i++)
                getFromPromise(promises2, found, value, latch, completed, i, lock);

            Optional<Promise<Optional<Cons<Stream<T>>>>> p = Optional.of(streamPromise);
            if (!found.get()) {
                do {
                    p = Stream.performActionAndAwaitCompletion(p.get(), new A1<Stream<T>>() {
                        @Override
                        public void call(Stream<T> stream) {
                            Promise<Optional<Cons<T>>> q = stream.factory().create();
                            int newIndex = addToPromises(promises2, q, lock);
                            getFromPromise(promises2, found, value, latch, completed, newIndex,
                                    lock);
                        }

                    });
                } while (!found.get() && p.isPresent());
            }
            try {
                latch.await();
                if (completed.size() == promises2.size())
                    return Optional.absent();
                else {
                    return Optional.of(Cons.cons(
                            value.get().get().head(),
                            new MergePromise2<T>(copyExcluding(promises2, completed), p.or(Promises
                                    .<Cons<Stream<T>>> empty()))));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private static <T> List<Promise<T>> cached(List<Promise<T>> list) {
            // for performance could use mutable update
            List<Promise<T>> list2 = new ArrayList<Promise<T>>();
            for (Promise<T> t : list)
                list2.add(Promises.cache(t));
            return list2;
        }

        private static <T> int addToPromises(List<Promise<Optional<Cons<T>>>> promises,
                Promise<Optional<Cons<T>>> q, Object lock) {
            int newIndex;
            synchronized (lock) {
                promises.add(Promises.cache(q));
                newIndex = promises.size() - 1;
            }
            return newIndex;
        }

        private static <T> void getFromPromise(final List<Promise<Optional<Cons<T>>>> promises,
                final AtomicBoolean found, final AtomicReference<Optional<Cons<T>>> value,
                final CountDownLatch latch, final ConcurrentSkipListSet<Integer> completed,
                final int index, final Object lock) {
            Promise<Optional<Cons<T>>> p = promises.get(index);
            if (!found.get())
                p.scheduler().schedule(new A0() {
                    @Override
                    public void call() {
                        if (!found.get()) {
                            Optional<Cons<T>> t = promises.get(index).get();
                            if (t.isPresent() && found.compareAndSet(false, true)) {
                                value.set(t);
                                promises.set(index, t.get().tail());
                                latch.countDown();
                            } else if (!t.isPresent()) {
                                completed.add(index);
                                synchronized (lock) {
                                    if (completed.size() == promises.size()) {
                                        latch.countDown();
                                    }
                                }
                            }
                        }
                    }
                });
        }

        private static <T> List<T> copyExcluding(List<T> list, Set<Integer> excludeIndexes) {
            List<T> list2 = new ArrayList<T>();
            for (int i = 0; i < list.size(); i++) {
                if (!excludeIndexes.contains(i))
                    list2.add(list.get(i));
            }
            return list2;
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
