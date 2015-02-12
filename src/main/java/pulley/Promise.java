package pulley;

public interface Promise<T> {

    T get();

    A0 closeAction();

    Scheduler scheduler();

}
