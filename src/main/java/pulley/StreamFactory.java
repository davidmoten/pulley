package pulley;

import pulley.promises.Promise;
import pulley.util.Optional;

public interface StreamFactory<T> extends Factory<Promise<Optional<Cons<T>>>> {

}
