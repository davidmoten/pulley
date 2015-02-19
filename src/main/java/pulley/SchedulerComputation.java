package pulley;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pulley.actions.A0;
import pulley.actions.Actions;

public class SchedulerComputation implements Scheduler {

	private final List<Scheduler> workers;
	private final AtomicInteger n = new AtomicInteger(0);

	SchedulerComputation() {
		List<Scheduler> workers = new ArrayList<Scheduler>();
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			workers.add(new Worker(Executors.newSingleThreadScheduledExecutor()));
		}
		this.workers = workers;
	}

	private static class Worker implements Scheduler {

		private final ScheduledExecutorService executor;
		private volatile Thread thread = null;

		Worker(ScheduledExecutorService executor) {
			this.executor = executor;
		}

		@Override
		public void schedule(final A0 action, long delay, TimeUnit unit) {
			A0 all = new A0() {
				@Override
				public void call() {
					thread = Thread.currentThread();
					action.call();
				}
			};
			// detect recursion
			if (Thread.currentThread() == thread)
				Schedulers.immediate().schedule(action, delay, unit);
			else
				executor.schedule(Actions.toRunnable(all), delay, unit);
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
		return nextWorker();
	}

	@Override
	public void schedule(A0 action, long delay, TimeUnit unit) {
		nextWorker().schedule(action, delay, unit);
	}

	private Scheduler nextWorker() {
		return workers.get(n.getAndIncrement() % workers.size());
	}
}
