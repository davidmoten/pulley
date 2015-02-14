package pulley.transforms;

import java.util.concurrent.atomic.AtomicReference;

import pulley.A0;
import pulley.A1;
import pulley.Cons;
import pulley.F2;
import pulley.Promise;
import pulley.Promises;
import pulley.Scheduler;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.Transformer;
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
            return new StreamPromise<R>() {

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
                    return Optional.of(Cons.cons(ref.get(), Promises.<Cons<R>> empty()));
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
}
