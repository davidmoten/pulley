package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Result;
import pulley.ResultValue;
import pulley.Results;
import pulley.Stream;
import pulley.Transformer;
import pulley.actions.Actions;
import pulley.actions.Actions.Latest;
import pulley.util.Optional;

public class Take {

    public static <T> Stream<T> take(Stream<T> stream, long n) {
        return stream.transform(new TakeTransformer<T>(n));
    }

    private static class TakeTransformer<T> implements Transformer<T, T> {

        private final long n;

        public TakeTransformer(long n) {
            this.n = n;
        }

        @Override
        public Promise<Optional<Cons<T>>> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, T>(promise) {

                @Override
                public Optional<Cons<T>> get() {
                    if (n == 0)
                        return Optional.absent();
                    else {
                        Result<Promise<Optional<Cons<T>>>> p = Results.result(promise);
                        Latest<T> recorder = Actions.latest();
                        p = Promises.performActionAndAwaitCompletion(Results.value(p), recorder);
                        if (recorder.get().isPresent()) {
                            if (p instanceof ResultValue) {
                                return Optional.of(Cons.cons(recorder.get().get(),
                                        new TakeTransformer<T>(n - 1).transform(Results.value(p))));
                            } else
                                return Optional.of(Cons.cons(recorder.get().get()));
                        } else
                            return Optional.absent();
                    }
                }
            };
        }
    }

}
