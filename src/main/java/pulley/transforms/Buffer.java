package pulley.transforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Stream;
import pulley.Transformer;
import pulley.actions.A1;
import pulley.actions.Actions;
import pulley.util.Optional;

public class Buffer {

    public static <T> Stream<List<T>> buffer(Stream<T> stream, final int size) {
        return stream.transform(new BufferTransformer<T>(size));
    }

    private static class BufferTransformer<T> implements Transformer<T, List<T>> {

        private final int size;

        BufferTransformer(int size) {
            this.size = size;
        }

        @Override
        public AbstractStreamPromise<T, List<T>> transform(final Promise<Optional<Cons<T>>> promise) {
            return new AbstractStreamPromise<T, List<T>>(promise) {

                @Override
                public Optional<Cons<List<T>>> get() {
                    final List<T> list = Collections.synchronizedList(new ArrayList<T>());
                    A1<T> addToList = Actions.addToList(list);
                    Optional<Promise<Optional<Cons<T>>>> p = Optional.of(promise);
                    do {
                        p = Promises.performActionAndAwaitCompletion(p.get(), addToList);
                    } while (p.isPresent() && list.size() < size);
                    if (list.size() == 0)
                        return Optional.absent();
                    else if (!p.isPresent())
                        return Optional.of(Cons.cons(list));
                    else
                        return Optional.of(Cons.cons(list,
                                BufferTransformer.this.transform(p.get())));
                }

            };
        }
    }
}
