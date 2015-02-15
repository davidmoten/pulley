package pulley;

import pulley.promises.Promise;
import pulley.util.Optional;

public interface StreamPromise<T> extends Promise<Optional<Cons<T>>> {

}
