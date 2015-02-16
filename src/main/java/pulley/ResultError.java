package pulley;

public class ResultError<T> implements Result<T> {

    private final Throwable throwable;

    public ResultError(Throwable t) {
        this.throwable = t;
    }

    public Throwable get() {
        return throwable;
    }
}
