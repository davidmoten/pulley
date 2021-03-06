package pulley.promises;

import java.util.concurrent.atomic.AtomicBoolean;

import pulley.Promise;
import pulley.Scheduler;
import pulley.actions.A0;

public class CachingPromise<T> implements Promise<T> {

    private final Promise<T> promise;
    private final AtomicBoolean calculated = new AtomicBoolean(false);
    private volatile T value;
    private volatile Throwable throwable;

    public CachingPromise(Promise<T> promise) {
        this.promise = promise;
    }

    @Override
    public T get() {
        if (calculated.compareAndSet(false, true)) {
            try {
                value = promise.get();
            } catch (Throwable e) {
                throwable = e;
            }
        }
        if (throwable != null) {
            if (throwable instanceof RuntimeException)
                throw (RuntimeException) throwable;
            else
                throw (Error) throwable;
        }
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
