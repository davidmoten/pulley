package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Stream;
import pulley.Transformer;
import pulley.actions.A1;
import pulley.actions.Actions;
import pulley.actions.Actions.Latest;
import pulley.util.Optional;

public class OnNext {

    public static <T> Stream<T> onNext(Stream<T> stream, A1<? super T> action) {
        return stream.transform(new OnNextTransformer<T>(action));
    }

    private static class OnNextTransformer<T> implements Transformer<T, T> {

        private final A1<? super T> action;

        OnNextTransformer(A1<? super T> action) {
            this.action = action;
        }

        @Override
        public Promise<Optional<Cons<T>>> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, T>(promise) {

                @Override
                public Optional<Cons<T>> get() {
                    Latest<T> latest = Actions.latest();
                    A1<T> both = Actions.sequence(latest, action);
                    Optional<Promise<Optional<Cons<T>>>> p = Promises
                            .performActionAndAwaitCompletion(promise, both);
                    if (p.isPresent() && latest.get().isPresent())
                        return Optional.of(Cons.cons(latest.get().get(),
                                Promises.deferTransformation(p.get(), OnNextTransformer.this)));
                    else if (latest.get().isPresent())
                        return Optional.of(Cons.cons(latest.get().get()));
                    else
                        return Optional.absent();
                }
            };
        }
    }

}
