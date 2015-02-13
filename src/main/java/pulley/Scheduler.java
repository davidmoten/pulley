package pulley;

import java.util.concurrent.TimeUnit;

public interface Scheduler extends TimeProvider {

    void schedule(A0 action);

    void schedule(A0 action, long duration, TimeUnit unit);

}
