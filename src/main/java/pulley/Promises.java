package pulley;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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

    public static <T> Promise<T> cache(final Promise<T> promise) {
        if (!(promise instanceof CachingPromise))
            return new CachingPromise<T>(promise);
        else
            return promise;
    }

    public static <T> Optional<Promise<Optional<Cons<T>>>> performActionAndAwaitCompletion(
            final Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Optional<Promise<Optional<Cons<T>>>>> ref = new AtomicReference<Optional<Promise<Optional<Cons<T>>>>>(
                null);
        final A0 a = new A0() {
            @Override
            public void call() {
                final Optional<Cons<T>> value = p.get();
                if (value.isPresent()) {
                    action.call(value.get().head());
                    ref.set(Optional.of(value.get().tail()));
                } else {
                    p.closeAction().call();
                    ref.set(Optional.<Promise<Optional<Cons<T>>>> absent());
                }
                latch.countDown();
            }
        };
        p.scheduler().schedule(a);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return ref.get();
    }
}
