package pulley;

public class SchedulerImmediate implements Scheduler {

	@Override
	public void schedule(A0 action) {
		action.call();
	}

}
