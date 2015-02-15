package pulley.promises;

import pulley.Factory;
import pulley.Scheduler;
import pulley.Schedulers;
import pulley.actions.A0;
import pulley.actions.Actions;

public class CompletedPromise<T> implements Promise<T> {

    private final T value;

    public CompletedPromise(T t) {
        this.value = t;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public A0 closeAction() {
        return Actions.doNothing0();
    }

    @Override
    public Scheduler scheduler() {
        return Schedulers.immediate();
    }

    public static class CompletedPromiseFactory<T> implements Factory<Promise<T>> {

        private final T t;

        public CompletedPromiseFactory(T t) {
            this.t = t;
        }

        @Override
        public Promise<T> create() {
            return new CompletedPromise<T>(t);
        }

    }

}
