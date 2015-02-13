package pulley;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pulley.Actions.println;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class StreamsTest {

    @Test
    public void testHelloWorld() {
        Streams.just("hello world").forEach(println());
    }

    @Test
    public void testHelloWorldFromADifferentScheduler() {
        Streams.just("hello world").scheduleOn(Schedulers.computation()).forEach(Actions.log());
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
        s.schedule(Actions.log("hello"));
        s.schedule(Actions.log("world"));
        s.schedule(Actions.log("from trampoline"));
    }

    @Test
    public void testImmediate() {
        Scheduler s = Schedulers.immediate();
        s.schedule(Actions.log("hello"));
        s.schedule(Actions.log("world"));
        s.schedule(Actions.log("from immediate"));
    }

    @Test
    public void testComputation() {
        Scheduler s = Schedulers.computation();
        s.schedule(Actions.log("hello"));
        s.schedule(Actions.log("world"));
        s.schedule(Actions.log("from computation"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMergeSychronous() {
        Streams.merge(Arrays.asList(Streams.from(Arrays.asList(1, 2, 3)),
                Streams.from(Arrays.asList(4, 5, 6))));
    }

    @Test
    public void testFilterMixOfMatchAndNoMatch() {
        List<Integer> list = Streams.from(Arrays.asList(1, 2, 3, 4, 5))
                .filter(new Predicate<Integer>() {
                    @Override
                    public Boolean call(Integer t) {
                        return t % 2 == 0;
                    }
                }).toList().single();
        assertEquals(Arrays.asList(2, 4), list);
    }

    @Test
    public void testFilterAll() {
        List<Integer> list = Streams.from(Arrays.asList(1, 2, 3, 4, 5))
                .filter(new Predicate<Integer>() {
                    @Override
                    public Boolean call(Integer t) {
                        return false;
                    }
                }).toList().single();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testFilterNone() {
        List<Integer> list = Streams.from(Arrays.asList(1, 2, 3, 4, 5))
                .filter(new Predicate<Integer>() {
                    @Override
                    public Boolean call(Integer t) {
                        return true;
                    }
                }).toList().single();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
    }

    @Test
    public void testFilterOnEmpty() {
        List<Integer> list = Streams.<Integer> empty().filter(new Predicate<Integer>() {
            @Override
            public Boolean call(Integer t) {
                return true;
            }
        }).toList().single();
        assertTrue(list.isEmpty());
    }

    @Test
    public void testList() {
        assertEquals(Arrays.asList(1, 2), Streams.just(1, 2).toList().single());
    }

    @Test
    public void testLots() {
        // for (int i = 1; i <= 10; i++)
        Streams.range(1, 100000).forEach();
    }

    @Test
    public void testConcatWith() {
        assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9),
                Streams.range(1, 5).concatWith(Streams.range(6, 4)).toList().single());
    }

    @Test
    public void testFlatMapSynchronous() {
        List<Integer> source = Streams.range(1, 10).toList().single();
        List<Integer> list = Streams.from(source).flatMap(new F1<Integer, Stream<Integer>>() {
            @Override
            public Stream<Integer> call(Integer t) {
                return Streams.just(t);
            }
        }).toList().single();
        assertEquals(source, list);
    }

    @Test
    public void testFlatMapAsynchronous() {
        List<Integer> source = Streams.range(1, 10).toList().single();
        List<Integer> list = Streams.from(source).flatMap(new F1<Integer, Stream<Integer>>() {
            @Override
            public Stream<Integer> call(Integer t) {
                return Streams.just(t).scheduleOn(Schedulers.computation());
            }
        }).toList().single();
        assertEquals(new HashSet<Integer>(source), new HashSet<Integer>(list));
    }

}
