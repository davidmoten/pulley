package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Actions;
import pulley.Actions.Latest;
import pulley.Cons;
import pulley.F1;
import pulley.Promise;
import pulley.Promises;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.Transformer;
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
                    Optional<Promise<Optional<Cons<T>>>> p = Optional.of(promise);
                    Latest<T> recorder = Actions.latest();
                    do {
                        p = Promises.performActionAndAwaitCompletion(p.get(), recorder);
                    } while (p.isPresent() && recorder.get().isPresent()
                            && !predicate.call(recorder.get().get()));
                    if (p.isPresent() && recorder.get().isPresent())
                        return Optional.of(Cons.cons(recorder.get().get(),
                                FilterTransformer.this.transform(p.get())));
                    else
                        return Optional.absent();
                }

            };
        }
    }

}
