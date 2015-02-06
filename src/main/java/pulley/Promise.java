package pulley;

public interface Promise<T> extends Job<T> {

	void complete(T value);

	boolean isCompleted();

	void failure(Throwable t);

	T get();

	<R> Promise<T> map(Promise<R> parent, F1<? super R, T> f);

	// static Promise<T> create(T t);
}
