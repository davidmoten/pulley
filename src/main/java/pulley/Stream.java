package pulley;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pulley.util.Optional;

public class Stream<T> {
    private final Factory<? extends Promise<Optional<Cons<T>>>> factory;

    public Stream(Factory<? extends Promise<Optional<Cons<T>>>> factory) {
        this.factory = factory;
    }

    public Factory<? extends Promise<Optional<Cons<T>>>> factory() {
        return factory;
    }

    public static <T> Stream<T> stream(Factory<? extends Promise<Optional<Cons<T>>>> factory) {
        return new Stream<T>(factory);
    }

    public <R> Stream<R> map(final F1<? super T, ? extends R> f) {
        final F1<Optional<Cons<T>>, Optional<Cons<R>>> g = F.optional(F.cons(f));
        Factory<StreamPromise<R>> factory2 = new Factory<StreamPromise<R>>() {
            @Override
            public StreamPromise<R> create() {
                final Promise<Optional<Cons<T>>> p = factory.create();
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
                        return Schedulers.immediate();
                    }
                };
            }

        };
        return stream(factory2);
    }

    public Stream<T> doOnTerminate(A0 action) {
        return null;
    }

    public Stream<List<T>> toList() {
        Factory<StreamPromise<List<T>>> factory2 = new Factory<StreamPromise<List<T>>>() {
            @Override
            public StreamPromise<List<T>> create() {
                final Promise<Optional<Cons<T>>> p = factory.create();
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
        return stream(factory2);
    }

    public void forEach(A1<? super T> action) {
        Promise<Optional<Cons<T>>> p = factory.create();
        forEach(p, action);
    }

    private static <T> void forEach(Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        while (true) {
            final Optional<Cons<T>> value = p.get();
            if (value.isPresent()) {
                A0 a = new A0() {
                    @Override
                    public void call() {
                        action.call(value.get().head());
                    }
                };
                p.scheduler().schedule(a);
                p = value.get().tail();
            } else {
                p.closeAction().call();
                return;
            }
        }
    }

    public void forEach() {
        forEach(Actions.doNothing1());
    }

    public T single() {
        Optional<Cons<T>> c = factory.create().get();
        final T value;
        if (c.isPresent())
            value = c.get().head();
        else
            throw new RuntimeException("expected one item but no items emitted");
        Optional<Cons<T>> c2 = c.get().tail().get();
        if (c2.isPresent())
            throw new RuntimeException("expected one item but more than one emitted");
        else
            return value;
    }

}