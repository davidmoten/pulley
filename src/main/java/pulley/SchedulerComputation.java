package pulley;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pulley.actions.A0;
import pulley.actions.Actions;

public class SchedulerComputation implements Scheduler {

	private final Scheduler[] workers;
	private final AtomicInteger n = new AtomicInteger(0);

	SchedulerComputation() {
		Scheduler[] workers = new Scheduler[Runtime.getRuntime()
				.availableProcessors()];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new Worker(Executors.newScheduledThreadPool(1));
		}
		this.workers = workers;
	}

	private static class Worker implements Scheduler {

		private final ScheduledExecutorService executor;

		Worker(ScheduledExecutorService executor) {
			this.executor = executor;
		}

		@Override
		public void schedule(A0 action, long delay, TimeUnit unit) {
			executor.schedule(Actions.toRunnable(action), delay, unit);
		}

		@Override
		public Worker worker() {
			return this;
		}

		@Override
		public long now() {
			return System.currentTimeMillis();
		}
	}

	@Override
	public long now() {
		return System.currentTimeMillis();
	}

	@Override
	public Scheduler worker() {
		return workers[n.getAndIncrement() % workers.length];
	}

	@Override
	public void schedule(A0 action, long delay, TimeUnit unit) {
		workers[n.getAndIncrement() % workers.length].schedule(action, delay,
				unit);
	}
}
