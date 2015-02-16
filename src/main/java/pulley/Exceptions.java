package pulley;

public class Exceptions {

    public static <T> T throwException(Throwable e) {
        if (e instanceof RuntimeException)
            throw ((RuntimeException) e);
        else
            throw ((Error) e);
    }

}
