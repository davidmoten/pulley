package pulley;

import pulley.util.Optional;

public interface Transformer<T, R> {
    Promise<Optional<Cons<R>>> transform(Promise<Optional<Cons<T>>> promise);
}
