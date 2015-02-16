package pulley;

public class Results {

    public static <T> ResultValue<T> result(T t) {
        return new ResultValue<T>(t);
    }

    public static <T> ResultError<T> error(Throwable t) {
        return new ResultError<T>(t);
    }

    public static <T> T value(Result<T> result) {
        if (result instanceof ResultValue)
            return ((ResultValue<T>) result).get();
        else
            return Exceptions.throwException(((ResultError<T>) result).get());
    }
}
