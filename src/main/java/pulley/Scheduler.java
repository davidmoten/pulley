package pulley;

import java.util.concurrent.TimeUnit;

import pulley.actions.A0;

public interface Scheduler extends TimeProvider {

    void schedule(A0 action);

    void schedule(A0 action, long delay, TimeUnit unit);

}
