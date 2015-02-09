package pulley;

import static pulley.Cons.cons;
import static pulley.Promises.completedPromiseFactory;
import static pulley.util.Optional.absent;
import static pulley.util.Optional.of;

import java.util.Arrays;

import pulley.util.Optional;

public class Stream<T> {
    private final Factory<Promise<Optional<Cons<T>>>> factory;

    private static Stream<?> EMPTY = stream(completedPromiseFactory(Optional
            .<Cons<Object>> absent()));

    public Stream(Factory<Promise<Optional<Cons<T>>>> factory) {
        this.factory = factory;
    }

    public Factory<Promise<Optional<Cons<T>>>> factory() {
        return factory;
    }

    public static <T> Stream<T> stream(Factory<Promise<Optional<Cons<T>>>> factory) {
        return new Stream<T>(factory);
    }

    public <R> Stream<R> map(final F1<? super T, ? extends R> f) {
        final F1<Optional<Cons<T>>, Optional<Cons<R>>> g = F.optional(F.cons(f));
        Factory<Promise<Optional<Cons<R>>>> factory2 = new Factory<Promise<Optional<Cons<R>>>>() {
            @Override
            public Promise<Optional<Cons<R>>> create() {
                final Promise<Optional<Cons<T>>> p = factory.create();
                return new Promise<Optional<Cons<R>>>() {
                    @Override
                    public Optional<Cons<R>> get() {
                        return g.call(p.get());
                    }
                };
            }
        };
        return stream(factory2);
    }

    public static <T> Stream<T> just(T t) {
        return stream(completedPromiseFactory(of(cons(t, Stream.<T> emptyPromise()))));
    }

    private static Promise<Optional<Cons<?>>> EMPTY_PROMISE = new Promise<Optional<Cons<?>>>() {

        @Override
        public Optional<Cons<?>> get() {
            return absent();
        }
    };

    @SuppressWarnings("unchecked")
    private static <T> Promise<Optional<Cons<T>>> emptyPromise() {
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

    public void forEach(A1<? super T> action) {
        Promise<Optional<Cons<T>>> p = factory.create();
        while (true) {
            Optional<Cons<T>> value = p.get();
            if (value.isPresent()) {
                action.call(value.get().head());
                p = value.get().tail();
            } else
                return;
        }
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