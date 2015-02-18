package pulley;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pulley.actions.A0;
import pulley.actions.Actions;

public class SchedulerTrampoline implements Scheduler {

    private final Deque<A0> queue = new ConcurrentLinkedDeque<A0>();
    private final AtomicInteger wip = new AtomicInteger(0);

    private void schedule(A0 action) {
        queue.add(action);
        if (wip.getAndIncrement() == 0) {
            do {
                drainQueue();
            } while (wip.decrementAndGet() > 0);
        }
    }

    private void drainQueue() {
        while (true) {
            A0 a = queue.peek();
            if (a != null) {
                queue.remove();
                a.call();
            } else
                return;
        }
    }

    @Override
    public void schedule(A0 action, long delay, TimeUnit unit) {
        schedule(new Actions.ActionSleeping(this, action, delay, unit));
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }

    @Override
    public Scheduler worker() {
        return this;
    }
}
