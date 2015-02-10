package pulley.streams;

import static pulley.Cons.cons;
import static pulley.Stream.stream;

import java.util.concurrent.atomic.AtomicInteger;

import pulley.A0;
import pulley.Actions;
import pulley.Cons;
import pulley.Factory;
import pulley.Promise;
import pulley.Stream;
import pulley.util.Optional;

public class Range {

	public static Stream<Integer> create(final int start, final int count) {
		Factory<Promise<Optional<Cons<Integer>>>> factory = new Factory<Promise<Optional<Cons<Integer>>>>() {

			@Override
			public Promise<Optional<Cons<Integer>>> create() {
				return new RangePromise(new AtomicInteger(start), start + count);
			}
		};
		return stream(factory);
	}

	private static class RangePromise implements
			Promise<Optional<Cons<Integer>>> {

		private final AtomicInteger n;
		private final int maxValue;

		RangePromise(AtomicInteger n, int maxValue) {
			this.n = n;
			this.maxValue = maxValue;
		}

		@Override
		public Optional<Cons<Integer>> get() {
			int m = n.getAndIncrement();
			if (m >= maxValue)
				return Optional.absent();
			else
				return Optional.of(cons(m, new RangePromise(n, maxValue)));
		}

		@Override
		public A0 closeAction() {
			return Actions.doNothing0();
		}
	}
}
