package pulley;

public interface Promise<T> extends Job<T> {

	T get();

	A0 closeAction();

	Scheduler scheduler();

}
