package pulley;

public class Actions {

	public static A1<Object> println() {
		return new A1<Object>() {
			@Override
			public void call(Object t) {
				System.out.println(t);
			}
		};
	}

}
