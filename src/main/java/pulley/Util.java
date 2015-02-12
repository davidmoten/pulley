package pulley;

public final class Util {

    private static final UnexpectedException UNEXPECTED = new UnexpectedException();
    private static final UnexpectedException NOT_IMPLEMENTED = new UnexpectedException();

    public static <T> T unexpected() {
        throw UNEXPECTED;
    }

    public static <T> T notImplemented() {
        throw NOT_IMPLEMENTED;
    }
}
