package pulley;

import pulley.util.Optional;

public class Promises {

    public static <T> CompletedPromiseFactory<T> completedPromiseFactory(T t) {
        return new CompletedPromiseFactory<T>(t);
    }

    public static <T> FunctionPromise<T> functionPromise(F0<T> f) {
        return new FunctionPromise<T>(f);
    }

    public static <T> Promise<Optional<T>> empty() {
        return new Promise<Optional<T>>() {

            @Override
            public Optional<T> get() {
                return Optional.absent();
            }

            @Override
            public A0 closeAction() {
                return Actions.doNothing0();
            }

            @Override
            public Scheduler scheduler() {
                return Schedulers.immediate();
            }
        };
    }
}
