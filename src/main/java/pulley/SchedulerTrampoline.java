package pulley;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerTrampoline implements Scheduler {

	private final Deque<A0> queue = new ConcurrentLinkedDeque<A0>();
	private final AtomicInteger wip = new AtomicInteger(0);

	@Override
	public void schedule(A0 action) {
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

}
