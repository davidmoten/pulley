package pulley;

import pulley.actions.A0;
import pulley.actions.Actions;

public class FunctionPromise<T> implements Promise<T> {

	private final F0<T> f;

	public FunctionPromise(F0<T> f) {
		this.f = f;
	}

	@Override
	public T get() {
		T value = f.call();
		return value;
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
