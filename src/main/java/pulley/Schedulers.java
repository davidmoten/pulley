package pulley;

import java.util.concurrent.CountDownLatch;

import pulley.actions.A0;

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

    public static <T> Result<T> get(final Promise<T> promise) {
        PromiseGettingAction<T> action = new PromiseGettingAction<T>(promise);
        promise.scheduler().schedule(action);
        return action.get();
    }

    private static class PromiseGettingAction<T> implements A0 {

        volatile Result<T> result = Result.absent();
        private final Promise<T> promise;
        private final CountDownLatch latch;

        PromiseGettingAction(Promise<T> promise) {
            this.promise = promise;
            this.latch = new CountDownLatch(1);
        }

        @Override
        public void call() {
            result = Promises.result(promise).get();
            latch.countDown();
        }

        public Result<T> get() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return result;
        }

    }
}
