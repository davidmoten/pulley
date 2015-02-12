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
                        A1<T> addToList = Actions.addToList(list);
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

    public Stream<T> filter(final F1<? super T, Boolean> predicate) {
        return transform(new FilterTransformer<T>(predicate));
    }

    private static class FilterTransformer<T> implements Transformer<T, T> {

        private final F1<? super T, Boolean> predicate;

        FilterTransformer(F1<? super T, Boolean> predicate) {
            this.predicate = predicate;
        }

        @Override
        public StreamPromise<T> transform(final Promise<Optional<Cons<T>>> promise) {
            return new StreamPromise<T>() {

                @Override
                public Optional<Cons<T>> get() {
                    Optional<Promise<Optional<Cons<T>>>> p = Optional.of(promise);
                    Actions.ActionLatest<T> recorder = Actions.latest();
                    do {
                        p = performActionAndAwaitCompletion(p.get(), recorder);
                    } while (p.isPresent() && !predicate.call(recorder.get()));
                    if (p.isPresent())
                        return Optional.of(Cons.cons(recorder.get(),
                                FilterTransformer.this.transform(p.get())));
                    else
                        return Optional.absent();
                }

                @Override
                public A0 closeAction() {
                    return promise.closeAction();
                }

                @Override
                public Scheduler scheduler() {
                    return promise.scheduler();
                }
            };
        }
    }

    public Stream<T> concatWith(final Stream<T> stream) {
        return transform(new ConcatTransformer<T>(stream));
    }

    private static class ConcatTransformer<T> implements Transformer<T, T> {

        private final Stream<T> stream;

        public ConcatTransformer(Stream<T> stream) {
            this.stream = stream;
        }

        @Override
        public StreamPromise<T> transform(final Promise<Optional<Cons<T>>> promise) {
            return new StreamPromise<T>() {

                @Override
                public Optional<Cons<T>> get() {
                    Actions.ActionLatest<T> recorder = Actions.latest();
                    Optional<Promise<Optional<Cons<T>>>> p = performActionAndAwaitCompletion(
                            promise, recorder);
                    if (p.isPresent())
                        return Optional.of(Cons.cons(recorder.get(),
                                ConcatTransformer.this.transform(p.get())));
                    else {
                        Promise<Optional<Cons<T>>> promise2 = stream.factory.create();
                        Actions.ActionLatest<T> recorder2 = Actions.latest();
                        Optional<Promise<Optional<Cons<T>>>> p2 = performActionAndAwaitCompletion(
                                promise2, recorder2);
                        if (p2.isPresent())
                            return Optional.of(Cons.cons(recorder2.get(), p2.get()));
                        else
                            return Optional.absent();
                    }
                }

                @Override
                public A0 closeAction() {
                    return promise.closeAction();
                }

                @Override
                public Scheduler scheduler() {
                    return promise.scheduler();
                }
            };
        }

    }

    public <R> Stream<R> flatMap(F1<T, Stream<R>> f) {
        return Streams.merge(map(f));
    }

    public void forEach(A1<? super T> action) {
        forEach(factory.create(), action);
    }

    private static <T> void forEach(Promise<Optional<Cons<T>>> promise, final A1<? super T> action) {
        Optional<Promise<Optional<Cons<T>>>> p = Optional.of(promise);
        do {
            p = performActionAndAwaitCompletion(p.get(), action);
        } while (p.isPresent());
    }

    private static <T> Optional<Promise<Optional<Cons<T>>>> performActionAndAwaitCompletion(
            final Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Optional<Promise<Optional<Cons<T>>>>> ref = new AtomicReference<Optional<Promise<Optional<Cons<T>>>>>(
                null);
        final A0 a = new A0() {
            @Override
            public void call() {
                final Optional<Cons<T>> value = p.get();
                if (value.isPresent()) {
                    action.call(value.get().head());
                    ref.set(Optional.of(value.get().tail()));
                } else {
                    p.closeAction().call();
                    ref.set(Optional.<Promise<Optional<Cons<T>>>> absent());
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
        A1<T> addToList = Actions.addToList(list);
        final Optional<Promise<Optional<Cons<T>>>> p2 = performActionAndAwaitCompletion(p,
                addToList);
        if (list.size() == 0) {
            throw new RuntimeException("expected one item but no items emitted");
        } else {
            performActionAndAwaitCompletion(p2.get(), addToList);
            if (list.size() > 1)
                throw new RuntimeException("expected one item but more than one emitted");
            else
                return list.get(0);
        }
    }
}