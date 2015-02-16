package pulley;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import pulley.actions.A0;
import pulley.functions.F;

public final class Schedulers {

    private static final Scheduler TRAMPOLINE = new SchedulerTrampoline();
    private static final Scheduler IMMEDIATE = new SchedulerImmediate();
    private static final Scheduler COMPUTATION = new SchedulerComputation();

    public static Scheduler trampoline() {
        return TRAMPOLINE;
    }

    public static Scheduler immediate() {
        return IMMEDIATE;
    }

    public static Scheduler computation() {
        return COMPUTATION;
    }

    public static <T> Result<T> getAndJoin(final Promise<T> promise) {
        final AtomicReference<Result<T>> ref = new AtomicReference<Result<T>>();
        final CountDownLatch latch = new CountDownLatch(1);
        promise.scheduler().schedule(new A0() {

            @Override
            public void call() {
                ref.set(Promises.result(promise).get());
                latch.countDown();
            }
        });
        try {
            latch.await();
            return ref.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
