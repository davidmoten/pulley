package pulley;

import static org.junit.Assert.assertEquals;
import static pulley.Actions.println;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class StreamsTest {

    @Test
    public void testHelloWorld() {
        Streams.just("hello world").forEach(println());
    }

    @Test
    public void testMap() {
        assertEquals(2, (int) Streams.just(1).map(plusOne()).single());
    }

    private static F1<Integer, Integer> plusOne() {
        return new F1<Integer, Integer>() {
            @Override
            public Integer call(Integer t) {
                return t + 1;
            }
        };
    }

    @Test(expected = RuntimeException.class)
    public void testSingleFailsWithTwoItems() {
        Streams.just(1, 2).single();
    }

    @Test
    public void testSingle() {
        assertEquals(1, (int) Streams.just(1).single());
    }

    @Test
    public void testTwoPrinted() {
        Streams.just(3, 4).forEach(println());
    }

    @Test
    public void testIterable() {
        Streams.from(Arrays.asList(1, 2)).forEach(println());
    }

    @Test
    public void testRange() {
        Streams.range(1, 1000000).forEach();
    }

    @Test
    public void testTrampoline() {
        Scheduler s = Schedulers.trampoline();
        s.schedule(Actions.info("hello"));
        s.schedule(Actions.info("world"));
        s.schedule(Actions.info("from trampoline"));
    }

    @Test
    public void testImmediate() {
        Scheduler s = Schedulers.immediate();
        s.schedule(Actions.info("hello"));
        s.schedule(Actions.info("world"));
        s.schedule(Actions.info("from immediate"));
    }

    @Test
    public void testComputation() {
        Scheduler s = Schedulers.computation();
        s.schedule(Actions.info("hello"));
        s.schedule(Actions.info("world"));
        s.schedule(Actions.info("from computation"));
    }

    @Test
    public void testMergeSychronous() {
        Streams.merge(Arrays.asList(Streams.from(Arrays.asList(1, 2, 3)),
                Streams.from(Arrays.asList(4, 5, 6))));
    }

    @Test
    public void testList() {
        assertEquals(Arrays.asList(1, 2), Streams.just(1, 2).toList().single());
    }

}
