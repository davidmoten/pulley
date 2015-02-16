package pulley;

import pulley.util.Optional;
import pulley.util.Preconditions;

public class Result<T> {

    private final Optional<T> value;
    private final Optional<Throwable> throwable;

    public Result(Optional<T> value, Optional<Throwable> throwable) {
        Preconditions.checkArgument(!value.isPresent() || !throwable.isPresent(),
                "both can't be present");
        this.value = value;
        this.throwable = throwable;
    }

    public static <T> Result<T> of(T t) {
        return new Result<T>(Optional.of(t), Optional.<Throwable> absent());
    }

    public static <T> Result<T> error(Throwable e) {
        return new Result<T>(Optional.<T> absent(), Optional.of(e));
    }

    public Optional<T> value() {
        if (throwable.isPresent())
            return Exceptions.throwException(throwable.get());
        else
            return value;
    }

    public T or(T t) {
        if (throwable.isPresent())
            return Exceptions.throwException(throwable.get());
        else
            return value.or(t);
    }

    public Optional<Throwable> error() {
        return throwable;
    }

}
