package pulley;

import java.util.concurrent.TimeUnit;

public class SchedulerImmediate implements Scheduler {

    @Override
    public void schedule(A0 action) {
        action.call();
    }

    @Override
    public void schedule(A0 action, long duration, TimeUnit unit) {
        schedule(new Actions.ActionSleeping(this, action, duration, unit));
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
