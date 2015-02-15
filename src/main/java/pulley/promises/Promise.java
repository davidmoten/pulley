package pulley.promises;

import pulley.Scheduler;
import pulley.actions.A0;

public interface Promise<T> {

    T get();

    A0 closeAction();

    Scheduler scheduler();

}
