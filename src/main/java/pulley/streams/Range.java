package pulley.streams;

import pulley.Cons;
import pulley.Stream;

public class Range implements Stream<Cons<Integer>> {

	@Override
	public void complete(Cons<Cons<Integer>> value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isFulfilled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void failure(Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public Cons<Cons<Integer>> get() {
		// TODO Auto-generated method stub
		return null;
	}

}
