package pulley;

import java.util.concurrent.Future;

public interface Promise<T> extends Job<T> {

	Future<T> start();

	void complete(T value);

	boolean isCompleted();

	void failure(Throwable t);

	T get();

	<R> Promise<R> map(F1<? super T, R> f);

}
