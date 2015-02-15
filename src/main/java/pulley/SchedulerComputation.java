package pulley;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import pulley.actions.A0;
import pulley.actions.Actions;

public class SchedulerComputation implements Scheduler {

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime
            .getRuntime().availableProcessors());

    @Override
    public void schedule(A0 action) {
        executor.execute(Actions.toRunnable(action));
    }

    @Override
    public void schedule(A0 action, long delay, TimeUnit unit) {
        executor.schedule(Actions.toRunnable(action), delay, unit);
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
