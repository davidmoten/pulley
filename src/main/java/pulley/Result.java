package pulley;

public class Result<T> {

    private final T value;
    private final Throwable throwable;

    public Result(T value, Throwable throwable) {
        this.value = value;
        this.throwable = throwable;
    }

    public static <T> Result<T> value(T t) {
        return new Result<T>(t, null);
    }

    public static <T> Result<T> error(Throwable throwable) {
        return new Result<T>(null, throwable);
    }

    public T value() {
        return value;
    }

    public Throwable error() {
        return throwable;
    }

    public boolean isError() {
        return throwable != null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Result [value=");
        builder.append(value);
        builder.append(", throwable=");
        builder.append(throwable);
        builder.append("]");
        return builder.toString();
    }

}
