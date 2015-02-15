package pulley.functions;

public final class Functions {

    public static <T> Predicate<T> alwaysTrue() {
        return new Predicate<T>() {

            @Override
            public Boolean call(T t) {
                return true;
            }
        };
    }

    public static <T> Predicate<T> alwaysFalse() {
        return new Predicate<T>() {

            @Override
            public Boolean call(T t) {
                return false;
            }
        };
    }

}
