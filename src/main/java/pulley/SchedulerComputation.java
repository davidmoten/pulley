package pulley;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class SchedulerComputation implements Scheduler {

	private final ExecutorService executor = Executors
			.newCachedThreadPool(new LimitedThreadFactory(Runtime.getRuntime()
					.availableProcessors()));

	@Override
	public void schedule(A0 action) {
		executor.execute(Actions.toRunnable(action));
	}

	private static class LimitedThreadFactory implements ThreadFactory {

		private final AtomicInteger count = new AtomicInteger();
		private final int maxThreads;

		LimitedThreadFactory(int maxThreads) {
			this.maxThreads = maxThreads;
		}

		@Override
		public Thread newThread(Runnable r) {
			if (count.incrementAndGet() <= maxThreads) {
				Thread t = new Thread(r, "Computation-" + count.get());
				t.setDaemon(true);
				return t;
			} else
				// no more
				return null;
		}
	}

}
