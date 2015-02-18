package pulley;

import java.util.concurrent.TimeUnit;

import pulley.actions.A0;

public class SchedulerDelayed implements Scheduler {

	private final Scheduler scheduler;
	private final long delay;
	private final TimeUnit unit;

	public SchedulerDelayed(Scheduler scheduler, long delay, TimeUnit unit) {
		this.scheduler = scheduler;
		this.delay = unit.toMillis(delay);
		this.unit = unit;
	}

	@Override
	public long now() {
		return scheduler.now();
	}

	@Override
	public void schedule(A0 action, long delay, TimeUnit unit) {
		long totalMs = unit.toMillis(delay) + this.delay;
		scheduler.schedule(action, totalMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public Scheduler worker() {
		return new SchedulerDelayed(scheduler.worker(), delay, unit);
	}
}
