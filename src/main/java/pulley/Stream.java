package pulley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import pulley.util.Optional;

public class Stream<T> {

    private final Factory<Promise<Optional<Cons<T>>>> factory;

    public Stream(Factory<Promise<Optional<Cons<T>>>> factory) {
        this.factory = factory;
    }

    public Factory<? extends Promise<Optional<Cons<T>>>> factory() {
        return factory;
    }

    public static <T> Stream<T> stream(Factory<Promise<Optional<Cons<T>>>> factory) {
        return new Stream<T>(factory);
    }

    public <R> Stream<R> transform(final Transformer<T, R> transformer) {
        final StreamFactory<R> f = new StreamFactory<R>() {
            @Override
            public Promise<Optional<Cons<R>>> create() {
                final Promise<Optional<Cons<T>>> p = factory.create();
                return transformer.transform(p);
            }
        };
        return stream(f);
    }

    public Stream<T> scheduleOn(final Scheduler scheduler) {
        return transform(new Transformer<T, T>() {

            @Override
            public Promise<Optional<Cons<T>>> transform(Promise<Optional<Cons<T>>> promise) {
                return new ScheduledPromise<Optional<Cons<T>>>(promise, scheduler);
            }
        });
    }

    public <R> Stream<R> map(final F1<? super T, ? extends R> f) {
        final F1<Optional<Cons<T>>, Optional<Cons<R>>> g = F.optional(F.cons(f));
        Transformer<T, R> transformer = new Transformer<T, R>() {
            @Override
            public StreamPromise<R> transform(final Promise<Optional<Cons<T>>> p) {
                return new StreamPromise<R>() {
                    @Override
                    public Optional<Cons<R>> get() {
                        return g.call(p.get());
                    }

                    @Override
                    public A0 closeAction() {
                        return p.closeAction();
                    }

                    @Override
                    public Scheduler scheduler() {
                        return p.scheduler();
                    }
                };
            }
        };
        return transform(transformer);
    }

    public Stream<T> doOnTerminate(A0 action) {
        // TODO
        return null;
    }

    public Stream<List<T>> toList() {
        Transformer<T, List<T>> transformer = new Transformer<T, List<T>>() {
            @Override
            public Promise<Optional<Cons<List<T>>>> transform(final Promise<Optional<Cons<T>>> p) {
                return new StreamPromise<List<T>>() {

                    @Override
                    public Optional<Cons<List<T>>> get() {
                        final List<T> list = Collections.synchronizedList(new ArrayList<T>());
                        A1<T> addToList = new A1<T>() {
                            @Override
                            public void call(T t) {
                                list.add(t);
                            }
                        };
                        forEach(p, addToList);
                        return Optional.of(Cons.cons(list, Promises.<Cons<List<T>>> empty()));
                    }

                    @Override
                    public A0 closeAction() {
                        return p.closeAction();
                    }

                    @Override
                    public Scheduler scheduler() {
                        return p.scheduler();
                    }
                };
            }
        };
        return transform(transformer);
    }

    public <R> Stream<R> flatMap(F1<T, Stream<R>> f) {
        return Streams.merge(map(f));
    }

    public void forEach(A1<? super T> action) {
        Promise<Optional<Cons<T>>> p = factory.create();
        forEach(p, action);
    }

    private static <T> void forEach(Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        while (true) {
            p = performActionAndAwaitCompletion(p, action);
            if (p == null)
                return;
        }
    }

    private static <T> Promise<Optional<Cons<T>>> performActionAndAwaitCompletion(
            final Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Promise<Optional<Cons<T>>>> ref = new AtomicReference<Promise<Optional<Cons<T>>>>(
                null);
        A0 a = new A0() {
            @Override
            public void call() {
                final Optional<Cons<T>> value = p.get();
                if (value.isPresent()) {
                    action.call(value.get().head());
                    ref.set(value.get().tail());
                } else {
                    p.closeAction().call();
                    ref.set(null);
                }
                latch.countDown();
            }
        };
        p.scheduler().schedule(a);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ref.get();
    }

    public void forEach() {
        forEach(Actions.doNothing1());
    }

    public T single() {
        final Promise<Optional<Cons<T>>> p = factory.create();
        final List<T> list = Collections.synchronizedList(new ArrayList<T>());
        A1<T> addToList = new A1<T>() {
            @Override
            public void call(T t) {
                list.add(t);
            }
        };
        final Promise<Optional<Cons<T>>> p2 = performActionAndAwaitCompletion(p, addToList);
        if (list.size() == 0) {
            throw new RuntimeException("expected one item but no items emitted");
        } else {
            performActionAndAwaitCompletion(p2, addToList);
            if (list.size() > 1)
                throw new RuntimeException("expected one item but more than one emitted");
            else
                return list.get(0);
        }
    }
}