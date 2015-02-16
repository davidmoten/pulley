package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Result;
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
                    Result<Promise<Optional<Cons<T>>>> p = Result.of(promise);
                    Latest<T> recorder = Actions.latest();
                    do {
                        p = Promises.performActionAndAwaitCompletion(p.value().get(), recorder);
                    } while (p.isPresent() && recorder.get().isPresent()
                            && !predicate.call(recorder.get().get()));
                    if (p.isPresent() && recorder.get().isPresent())
                        return Optional.of(Cons.cons(recorder.get().get(),
                                FilterTransformer.this.transform(p.value().get())));
                    else
                        return Optional.absent();
                }

            };
        }
    }

}
