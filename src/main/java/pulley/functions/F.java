package pulley.functions;

import pulley.Cons;
import pulley.Promise;
import pulley.Result;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.Streams;
import pulley.actions.A0;
import pulley.actions.A1;
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

    public static <T> Predicate<T> alwaysTrue() {
        return new Predicate<T>() {

            @Override
            public Boolean call(T t) {
                return true;
            }
        };
    }

    public static <T> Predicate<T> alwaysFalse() {
        return new Predicate<T>() {

            @Override
            public Boolean call(T t) {
                return false;
            }
        };
    }

    public static <T, R> F1<T, Result<R>> result(final F1<T, R> f) {
        return new F1<T, Result<R>>() {

            @Override
            public Result<R> call(T t) {
                try {
                    return Result.of(f.call(t));
                } catch (Throwable e) {
                    return Result.error(e);
                }
            }
        };
    }

    public static <T, R, S> F1<T, ? extends S> compose(final F1<T, R> f1,
            final F1<? super R, ? extends S> f2) {
        return new F1<T, S>() {

            @Override
            public S call(T t) {
                return f2.call(f1.call(t));
            }
        };
    }

    public static <T, R> F0<R> compose(final F0<T> f1, final F1<? super T, ? extends R> f2) {
        return new F0<R>() {

            @Override
            public R call() {
                return f2.call(f1.call());
            }
        };
    }

    public static <T> F1<T, T> map(final A1<? super T> action) {
        return new F1<T, T>() {
            @Override
            public T call(T t) {
                action.call(t);
                return t;
            }
        };
    }

    public static <T> F0<Result<T>> result(final F0<? extends T> f) {
        return new F0<Result<T>>() {
            @Override
            public Result<T> call() {
                try {
                    return Result.<T> of(f.call());
                } catch (Throwable e) {
                    return Result.error(e);
                }
            }
        };
    }
}
