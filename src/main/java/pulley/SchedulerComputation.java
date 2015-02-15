package pulley;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pulley.actions.A0;
import pulley.actions.Actions;

public class SchedulerComputation implements Scheduler {

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime()
            .availableProcessors());

    @Override
    public void schedule(A0 action) {
        executor.execute(Actions.toRunnable(action));
    }

    @Override
    public void schedule(A0 action, long duration, TimeUnit unit) {
        Util.notImplemented();
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
