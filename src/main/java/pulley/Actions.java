package pulley;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Actions {

	private static final Logger log = LoggerFactory.getLogger(Actions.class);

	public static A1<Object> println() {
		return new A1<Object>() {
			@Override
			public void call(Object t) {
				System.out.println(t);
			}
		};
	}

	public static A0 println(final String s) {
		return new A0() {
			@Override
			public void call() {
				System.out.println(s);
			}
		};
	}

	public static A0 info(final String s) {
		return new A0() {
			@Override
			public void call() {
				log.info(s);
			}
		};
	}

	public static A1<Object> doNothing1() {
		return new A1<Object>() {
			@Override
			public void call(Object t) {
				// do nothing
			}
		};
	}

	public static A0 doNothing0() {
		return new A0() {
			@Override
			public void call() {
				// do nothing
			}
		};
	}

	public static Runnable toRunnable(final A0 action) {
		return new Runnable() {

			@Override
			public void run() {
				action.call();
			}
		};
	}
}
