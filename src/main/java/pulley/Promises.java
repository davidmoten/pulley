package pulley;

public class Promises {

	public static <T> CompletedPromiseFactory<T> completedPromiseFactory(T t) {
		return new CompletedPromiseFactory<T>(t);
	}

	public static <T> FunctionPromise<T> functionPromise(F0<T> f) {
		return new FunctionPromise<T>(f);
	}

	public static <T, R> Promise<R> map(final Promise<T> promise,
			final F1<? super T, ? extends R> f) {
		return new FunctionPromise<R>(new F0<R>() {
			@Override
			public R call() {
				return f.call(promise.get());
			}
		});

	}

}
