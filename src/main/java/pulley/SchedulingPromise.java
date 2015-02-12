package pulley;

public class SchedulingPromise<T> implements Promise<T> {

    private final Promise<T> promise;
    private final Scheduler scheduler;

    public SchedulingPromise(Promise<T> promise, Scheduler scheduler) {
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
