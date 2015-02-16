package pulley;

public class ResultAbsent<T> implements Result<T> {

    private static ResultAbsent<?> INSTANCE = new ResultAbsent<Object>();

    public static ResultAbsent<?> instance() {
        return INSTANCE;
    }
}
