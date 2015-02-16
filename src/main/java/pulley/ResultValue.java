package pulley;

public class ResultValue<T> implements Result<T> {
    private final T value;

    public ResultValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}
