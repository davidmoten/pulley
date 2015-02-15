package pulley;

import pulley.actions.A0;
import pulley.promises.Promise;
import pulley.util.Optional;

public abstract class AbstractStreamPromise<T, R> implements StreamPromise<R> {
    private final Promise<Optional<Cons<T>>> promise;

    public AbstractStreamPromise(Promise<Optional<Cons<T>>> promise) {
        this.promise = promise;
    }

    @Override
    public A0 closeAction() {
        return promise.closeAction();
    }

    @Override
    public Scheduler scheduler() {
        return promise.scheduler();
    }

}
