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
}
