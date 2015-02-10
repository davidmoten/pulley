package pulley;

public final class Schedulers {

	private static final Scheduler TRAMPOLINE = new SchedulerTrampoline();
	private static final Scheduler IMMEDIATE = new SchedulerImmediate();
	private static final Scheduler COMPUTATION = new SchedulerComputation();

	public static final Scheduler trampoline() {
		return TRAMPOLINE;
	}

	public static final Scheduler immediate() {
		return IMMEDIATE;
	}

	public static Scheduler computation() {
		return COMPUTATION;
	}
}
