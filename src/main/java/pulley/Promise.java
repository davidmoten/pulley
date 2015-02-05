package pulley;

public interface Promise<T> extends Job<T> {
	void complete(T value);

	boolean isFulfilled();

	void failure(Throwable t);

	T get();
}
