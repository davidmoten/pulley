package pulley;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import pulley.actions.A0;
import pulley.actions.A1;
import pulley.actions.Actions;
import pulley.functions.F0;
import pulley.promises.CachingPromise;
import pulley.promises.CompletedPromise.CompletedPromiseFactory;
import pulley.promises.FunctionPromise;
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

    public static <T> Result<Promise<Optional<Cons<T>>>> performActionAndAwaitCompletion(
            final Promise<Optional<Cons<T>>> p, final A1<? super T> action) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<Result<Promise<Optional<Cons<T>>>>> ref = new AtomicReference<Result<Promise<Optional<Cons<T>>>>>(
                null);
        final A0 a = new A0() {
            @Override
            public void call() {
                try {
                    final Optional<Cons<T>> value = p.get();

                    if (value.isPresent()) {
                        action.call(value.get().head());
                        ref.set(Result.of(value.get().tail()));
                    } else {
                        // TODO only terminating operator should call this
                        p.closeAction().call();
                        ref.set(Result.<Promise<Optional<Cons<T>>>> absent());
                    }
                    latch.countDown();
                } catch (RuntimeException e) {
                    ref.set(Result.<Promise<Optional<Cons<T>>>> error(e));
                }
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

    public static <T> Promise<Result<T>> result(final Promise<T> promise) {
        return new Promise<Result<T>>() {
            @Override
            public Result<T> get() {
                try {
                    return Result.of(promise.get());
                } catch (Throwable e) {
                    return Result.error(e);
                }
            }

            @Override
            public A0 closeAction() {
                return promise.closeAction();
            }

            @Override
            public Scheduler scheduler() {
                return promise.scheduler();
            }
        };
    }
}
