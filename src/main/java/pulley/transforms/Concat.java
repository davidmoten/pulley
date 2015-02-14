package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Actions;
import pulley.Actions.Latest;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.Transformer;
import pulley.util.Optional;

public class Concat {
    public static <T> Stream<T> concat(Stream<T> source, final Stream<T> stream) {
        return source.transform(new ConcatTransformer<T>(stream));
    }

    private static class ConcatTransformer<T> implements Transformer<T, T> {

        private final Stream<T> stream;

        public ConcatTransformer(Stream<T> stream) {
            this.stream = stream;
        }

        @Override
        public StreamPromise<T> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, T>(promise) {

                @Override
                public Optional<Cons<T>> get() {
                    Latest<T> recorder = Actions.latest();
                    Optional<Promise<Optional<Cons<T>>>> p = Promises
                            .performActionAndAwaitCompletion(promise, recorder);
                    if (recorder.get().isPresent() && p.isPresent())
                        return Optional.of(Cons.cons(recorder.get().get(),
                                ConcatTransformer.this.transform(p.get())));
                    else {
                        Promise<Optional<Cons<T>>> promise2 = stream.factory().create();
                        Latest<T> recorder2 = Actions.latest();
                        Optional<Promise<Optional<Cons<T>>>> p2 = Promises
                                .performActionAndAwaitCompletion(promise2, recorder2);
                        if (recorder2.get().isPresent() && p2.isPresent())
                            return Optional.of(Cons.cons(recorder2.get().get(), p2.get()));
                        else if (recorder2.get().isPresent())
                            return Optional.of(Cons.cons(recorder2.get().get()));
                        else
                            return Optional.absent();
                    }
                }
            };
        }
    }
}
