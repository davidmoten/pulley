package pulley.transforms;

import pulley.AbstractStreamPromise;
import pulley.Cons;
import pulley.F;
import pulley.F1;
import pulley.Promise;
import pulley.Stream;
import pulley.Transformer;
import pulley.util.Optional;

public class Map {

    public static <T, R> Stream<R> map(Stream<T> stream, final F1<? super T, ? extends R> f) {
        final F1<Optional<Cons<T>>, Optional<Cons<R>>> g = F.optional(F.cons(f));
        Transformer<T, R> transformer = new Transformer<T, R>() {
            @Override
            public AbstractStreamPromise<T, R> transform(final Promise<Optional<Cons<T>>> p) {
                return new AbstractStreamPromise<T, R>(p) {
                    @Override
                    public Optional<Cons<R>> get() {
                        return g.call(p.get());
                    }
                };
            }
        };
        return stream.transform(transformer);
    }
}
