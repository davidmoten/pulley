package pulley;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerTrampoline implements Scheduler {

	private final Deque<A0> queue = new ConcurrentLinkedDeque<A0>();
	private final AtomicBoolean wip = new AtomicBoolean(false);

	@Override
	public void schedule(A0 action) {
		queue.add(action);
		if (wip.compareAndSet(false, true)) {
			drainQueue();
			wip.set(false);
			// to cover race condition where just prior to the wip being set to
			// false an action is added to the queue in another thread
			drainQueue();
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

}
