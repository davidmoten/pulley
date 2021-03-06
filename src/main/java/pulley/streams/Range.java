package pulley.streams;

import static pulley.Cons.cons;
import static pulley.Stream.stream;
import pulley.Cons;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.Stream;
import pulley.StreamPromise;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.util.Optional;

public class Range {

	public static Stream<Integer> create(final int start, final int count) {
		return stream(() -> new RangePromise(start, start + count - 1));
	}

	private static class RangePromise implements StreamPromise<Integer> {

		private final int maxValue;
		private final int n;

		RangePromise(int n, int maxValue) {
			this.n = n;
			this.maxValue = maxValue;
		}

		@Override
		public Optional<Cons<Integer>> get() {
			if (n > maxValue)
				return Optional.absent();
			else
				return Optional.of(cons(n, new RangePromise(n + 1, maxValue)));
		}

		@Override
		public A0 closeAction() {
			return Actions.doNothing0();
		}

		@Override
		public Scheduler scheduler() {
			return Schedulers.immediate();
		}
	}
}
