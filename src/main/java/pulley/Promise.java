package pulley;

import pulley.actions.A0;

public interface Promise<T> {

    T get();

    A0 closeAction();

    Scheduler scheduler();

}
