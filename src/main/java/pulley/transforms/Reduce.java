package pulley.transforms;

import java.util.concurrent.atomic.AtomicReference;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Stream;
import pulley.Transformer;
import pulley.actions.A1;
import pulley.functions.F2;
import pulley.promises.Promise;
import pulley.util.Optional;

public class Reduce {

    public static <T, R> Stream<R> reduce(Stream<T> stream, R initial,
            F2<? super R, ? super T, ? extends R> reducer) {
        return stream.transform(new ReduceTransformer<T, R>(initial, reducer));
    }

    private static class ReduceTransformer<T, R> implements Transformer<T, R> {

        private final R initial;
        private final F2<? super R, ? super T, ? extends R> reducer;

        public ReduceTransformer(R initial, F2<? super R, ? super T, ? extends R> reducer) {
            this.initial = initial;
            this.reducer = reducer;
        }

        @Override
        public Promise<Optional<Cons<R>>> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, R>(promise) {

                @Override
                public Optional<Cons<R>> get() {
                    final AtomicReference<R> ref = new AtomicReference<R>(initial);
                    A1<T> action = new A1<T>() {

                        @Override
                        public void call(T t) {
                            ref.set(reducer.call(ref.get(), t));
                        }
                    };
                    Stream.forEach(promise, action);
                    return Optional.of(Cons.cons(ref.get()));
                }

            };
        }
    }
}
