package pulley.functions;

import pulley.Cons;
import pulley.Promise;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.Streams;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.util.Optional;

public final class F {

    public static <T, R> F1<Optional<T>, Optional<R>> optional(final F1<? super T, ? extends R> f) {
        return new F1<Optional<T>, Optional<R>>() {
            @Override
            public Optional<R> call(Optional<T> t) {
                if (t.isPresent())
                    return Optional.of((R) f.call(t.get()));
                else
                    return Optional.absent();
            }
        };
    }

    public static <T, R> F1<Cons<T>, Cons<R>> cons(final F1<? super T, ? extends R> f) {
        return new Transformer<T, R>(f);
    }

    private static class Transformer<T, R> implements F1<Cons<T>, Cons<R>> {

        private final F1<? super T, ? extends R> f;

        Transformer(F1<? super T, ? extends R> f) {
            this.f = f;
        }

        @Override
        public Cons<R> call(final Cons<T> c) {
            Promise<Optional<Cons<R>>> p = new Promise<Optional<Cons<R>>>() {

                @Override
                public Optional<Cons<R>> get() {
                    Optional<Cons<T>> value = c.tail().get();
                    if (!value.isPresent())
                        return Optional.absent();
                    else
                        return Optional.of(Transformer.this.call(value.get()));
                }

                @Override
                public A0 closeAction() {
                    return Actions.doNothing0();
                }

                @Override
                public Scheduler scheduler() {
                    return Schedulers.immediate();
                }
            };
            return Cons.cons(f.call(c.head()), p);
        }
    }

    public static <T> F1<T, Stream<T>> wrap() {
        return new F1<T, Stream<T>>() {
            @Override
            public Stream<T> call(T t) {
                return Streams.just(t);
            }
        };
    }

}