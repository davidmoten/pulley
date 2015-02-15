package pulley;

import pulley.actions.A0;

public class ScheduledPromise<T> implements Promise<T> {

    private final Promise<T> promise;
    private final Scheduler scheduler;

    public ScheduledPromise(Promise<T> promise, Scheduler scheduler) {
        this.promise = promise;
        this.scheduler = scheduler;
    }

    @Override
    public T get() {
        return promise.get();
    }

    @Override
    public A0 closeAction() {
        return promise.closeAction();
    }

    @Override
    public Scheduler scheduler() {
        return scheduler;
    }
}
