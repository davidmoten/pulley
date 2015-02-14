package pulley.transforms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pulley.A1;
import pulley.AbstractStreamPromise;
import pulley.Actions;
import pulley.Cons;
import pulley.Promise;
import pulley.Promises;
import pulley.Stream;
import pulley.Transformer;
import pulley.util.Optional;

public class ToList {

    public static <T> Stream<List<T>> toList(Stream<T> stream) {
        Transformer<T, List<T>> transformer = new Transformer<T, List<T>>() {
            @Override
            public Promise<Optional<Cons<List<T>>>> transform(final Promise<Optional<Cons<T>>> p) {
                return new AbstractStreamPromise<T, List<T>>(p) {

                    @Override
                    public Optional<Cons<List<T>>> get() {
                        final List<T> list = Collections.synchronizedList(new ArrayList<T>());
                        A1<T> addToList = Actions.addToList(list);
                        Stream.forEach(p, addToList);
                        return Optional.of(Cons.cons(list, Promises.<Cons<List<T>>> empty()));
                    }

                };
            }
        };
        return stream.transform(transformer);
    }
}