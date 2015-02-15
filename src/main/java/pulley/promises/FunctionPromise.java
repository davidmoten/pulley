package pulley.promises;

import pulley.Scheduler;
import pulley.Schedulers;
import pulley.actions.A0;
import pulley.actions.Actions;
import pulley.functions.F0;

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
