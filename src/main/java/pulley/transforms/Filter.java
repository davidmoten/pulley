package pulley.transforms;

import pulley.A0;
import pulley.Actions;
import pulley.Cons;
import pulley.F1;
import pulley.Promise;
import pulley.Scheduler;
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
            return new StreamPromise<T>() {

                @Override
                public Optional<Cons<T>> get() {
                    Optional<Promise<Optional<Cons<T>>>> p = Optional.of(promise);
                    Actions.ActionLatest<T> recorder = Actions.latest();
                    do {
                        p = Stream.performActionAndAwaitCompletion(p.get(), recorder);
                    } while (p.isPresent() && !predicate.call(recorder.get()));
                    if (p.isPresent())
                        return Optional.of(Cons.cons(recorder.get(),
                                FilterTransformer.this.transform(p.get())));
                    else
                        return Optional.absent();
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
