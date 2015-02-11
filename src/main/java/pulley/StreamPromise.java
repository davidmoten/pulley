package pulley;

import pulley.util.Optional;

public interface StreamPromise<T> extends Promise<Optional<Cons<T>>> {

}
