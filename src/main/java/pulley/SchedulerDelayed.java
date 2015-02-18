package pulley;

import java.util.concurrent.TimeUnit;

import pulley.actions.A0;

public class SchedulerDelayed implements Scheduler {

    private final Scheduler scheduler;
    private final long delay;
    private final TimeUnit unit;

    public SchedulerDelayed(Scheduler scheduler, long delay, TimeUnit unit) {
        this.scheduler = scheduler;
        this.delay = delay;
        this.unit = unit;
    }

    @Override
    public long now() {
        return scheduler.now();
    }

    @Override
    public void schedule(A0 action, long delay, TimeUnit unit) {
        schedule(action, delay, unit);
    }

    @Override
    public Scheduler worker() {
        return new SchedulerDelayed(scheduler.worker(), delay, unit);
    }
}
