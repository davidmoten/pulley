package pulley;

import java.util.concurrent.atomic.AtomicBoolean;

import pulley.actions.A0;

public class CachingPromise<T> implements Promise<T> {

    private final Promise<T> promise;
    private final AtomicBoolean calculated = new AtomicBoolean(false);
    private volatile T value;

    public CachingPromise(Promise<T> promise) {
        this.promise = promise;
    }

    @Override
    public T get() {
        if (calculated.compareAndSet(false, true))
            value = promise.get();
        return value;
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
