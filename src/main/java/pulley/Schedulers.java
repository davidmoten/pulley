package pulley;

public final class Schedulers {

	public static final Scheduler trampoline() {
		return new SchedulerTrampoline();
	}
}
