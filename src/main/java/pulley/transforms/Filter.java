package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Result;
import pulley.ResultValue;
import pulley.Results;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.Transformer;
import pulley.actions.Actions;
import pulley.actions.Actions.Latest;
import pulley.functions.F1;
import pulley.util.Optional;

public class Filter {

    public static <T> Stream<T> filter(Stream<T> stream, final F1<? super T, Boolean> predicate) {
        return stream.transform(new FilterTransformer<T>(predicate));
    }

    private static class FilterTransformer<T> implements Transformer<T, T> {

        private final F1<? super T, Boolean> predicate;

        FilterTransformer(F1<? super T, Boolean> predicate) {
            this.predicate = predicate;
        }

        @Override
        public StreamPromise<T> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, T>(promise) {

                @Override
                public Optional<Cons<T>> get() {
                    Result<Promise<Optional<Cons<T>>>> p = Results.result(promise);
                    Latest<T> recorder = Actions.latest();
                    do {
                        p = Promises.performActionAndAwaitCompletion(Results.value(p), recorder);
                    } while (p instanceof ResultValue && recorder.get().isPresent()
                            && !predicate.call(recorder.get().get()));
                    if (p instanceof ResultValue && recorder.get().isPresent())
                        return Optional.of(Cons.cons(recorder.get().get(),
                                FilterTransformer.this.transform(Results.value(p))));
                    else
                        return Optional.absent();
                }

            };
        }
    }

}
