package pulley;

import java.util.concurrent.TimeUnit;

import pulley.actions.A0;
import pulley.actions.Actions;

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
