package pulley;

public interface Promise<T> extends Job<T> {

	void complete(T value);

	boolean isCompleted();

	T get();

}
