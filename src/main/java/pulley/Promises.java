package pulley;

public class Promises {

	static <T> Promise<T> create(T t) {
		return new CompletedPromise<T>(t);
	}

}
