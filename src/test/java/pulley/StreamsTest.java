package pulley;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pulley.actions.Actions.println;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Ignore;
import org.junit.Test;

import pulley.actions.Actions;
import pulley.actions.Actions.Latest;
import pulley.functions.F;
import pulley.functions.F1;
import pulley.functions.Predicate;

public class StreamsTest {

	@Test
	public void testHelloWorld() {
		Latest<String> latest = Actions.latest();
		Streams.just("hello world").forEach(latest);
		assertEquals("hello world", latest.get().get());
	}

	@Test
	public void testHelloWorldFromADifferentScheduler() {
		Latest<String> latest = Actions.latest();
		Streams.just("hello world").scheduleOn(Schedulers.computation())
				.forEach(latest);
		assertEquals("hello world", latest.get().get());
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

	@Test
	public void testMapOnMoreThanOne() {
		assertEquals(asList(2, 3, 4), Streams.just(1, 2, 3).map(plusOne())
				.toList().single());
	}

	@Test
	public void testMapOnEmpty() {
		assertTrue(Streams.<Integer> empty().map(plusOne()).toList().single()
				.isEmpty());
	}

	@Test(expected = RuntimeException.class)
	public void testSingleFailsWithTwoItems() {
		Streams.just(1, 2).single();
	}

	@Test
	public void testSingle() {
		assertEquals(1, (int) Streams.just(1).single());
	}

	@Test(expected = RuntimeException.class)
	public void testSingleFailsWithNoItems() {
		Streams.empty().single();
	}

	@Test
	public void testTwoPrinted() {
		Streams.just(3, 4).forEach(println());
	}

	@Test
	public void testIterable() {
		Streams.just(1, 2).forEach(println());
	}

	@Test
	public void testRange() {
		Streams.range(1, 1000).forEach();
	}

	@Test
	public void testTrampoline() {
		Scheduler s = Schedulers.trampoline();
		s.schedule(Actions.log("hello"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("world"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("from trampoline"), 0, TimeUnit.SECONDS);
	}

	@Test
	public void testImmediate() {
		Scheduler s = Schedulers.immediate();
		s.schedule(Actions.log("hello"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("world"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("from immediate"), 0, TimeUnit.SECONDS);
	}

	@Test
	public void testComputation() {
		Scheduler s = Schedulers.computation();
		s.schedule(Actions.log("hello"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("world"), 0, TimeUnit.SECONDS);
		s.schedule(Actions.log("from computation"), 0, TimeUnit.SECONDS);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testMergeSychronous() {
		assertEquals(
				asList(1, 2, 3, 4, 5, 6),
				Streams.merge(
						asList(Streams.just(1, 2, 3), Streams.just(4, 5, 6)))
						.toList().single());
	}

	@Test
	public void testFlatMapSynchronous() {
		List<Integer> source = Streams.range(1, 10).toList().single();
		List<Integer> list = Streams.from(source).flatMap(F.<Integer> wrap())
				.toList().single();
		assertEquals(source, list);
	}

	@Test
	public void testFlatMapAsynchronous() {
		List<Integer> source = Streams.range(1, 10).toList().single();
		List<Integer> list = Streams.from(source)
				.flatMap(new F1<Integer, Stream<Integer>>() {
					@Override
					public Stream<Integer> call(Integer t) {
						return Streams.just(t).scheduleOn(
								Schedulers.computation());
					}
				}).toList().single();
		assertEquals(new HashSet<Integer>(source), new HashSet<Integer>(list));
	}

	@Test(timeout = 10000)
	@Ignore
	public void testFlatMapAsynManyTimes() {
		for (int i = 0; i < 100; i++)
			testFlatMapAsynchronous();
	}

	// @Test
	public void testFlatMapLots() {
		Streams.range(1, 10000).flatMap(new F1<Integer, Stream<Integer>>() {
			@Override
			public Stream<Integer> call(Integer t) {
				return Streams.just(t).scheduleOn(Schedulers.computation());
			}
		}).forEach();
	}

	@Test
	public void testFilterMixOfMatchAndNoMatch() {
		List<Integer> list = Streams.just(1, 2, 3, 4, 5)
		//
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
		List<Integer> list = Streams.just(1, 2, 3, 4, 5)
				.filter(F.alwaysFalse()).toList().single();
		assertTrue(list.isEmpty());
	}

	@Test
	public void testFilterNone() {
		List<Integer> list = Streams.just(1, 2, 3, 4, 5).filter(F.alwaysTrue())
				.toList().single();
		assertEquals(Arrays.asList(1, 2, 3, 4, 5), list);
	}

	@Test
	public void testFilterOnEmpty() {
		List<Integer> list = Streams.<Integer> empty().filter(F.alwaysTrue())
				.toList().single();
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
				Streams.range(1, 5).concatWith(Streams.range(6, 4)).toList()
						.single());
	}

	@Test
	public void testConcatWithEmptyBefore() {
		assertEquals(asList(1),
				Streams.<Integer> empty().concatWith(Streams.just(1)).toList()
						.single());
	}

	@Test
	public void testConcatWithEmptyAfter() {
		assertEquals(asList(1),
				Streams.just(1).concatWith(Streams.<Integer> empty()).toList()
						.single());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBufferTwoByOne() {
		List<List<Integer>> list = Streams.range(1, 2).buffer(1).toList()
				.single();
		assertEquals(asList(asList(1), asList(2)), list);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBufferTwoByTwo() {
		List<List<Integer>> list = Streams.range(1, 2).buffer(2).toList()
				.single();
		assertEquals(asList(asList(1, 2)), list);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBufferTwoByThree() {
		List<List<Integer>> list = Streams.range(1, 2).buffer(3).toList()
				.single();
		assertEquals(asList(asList(1, 2)), list);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testBufferThreeByTwo() {
		List<List<Integer>> list = Streams.range(1, 3).buffer(2).toList()
				.single();
		assertEquals(asList(asList(1, 2), asList(3)), list);
	}

	@Test
	public void testBufferEmptyByTwo() {
		List<List<Integer>> list = Streams.<Integer> empty().buffer(2).toList()
				.single();
		assertEquals(Collections.emptyList(), list);
	}

	@Test
	public void testReduce() {
		assertEquals(4, (int) Streams.just(2, 4, 6, 8).count().single());
	}

	@Test
	public void testDoOnNext() {
		final AtomicInteger i = new AtomicInteger();
		final AtomicInteger j = new AtomicInteger();
		Streams.from(asList(1, 2, 3, 4)).doOnNext(Actions.increment(i))
				.doOnNext(Actions.increment(j)).forEach();
		assertEquals(4, i.get());
		assertEquals(4, j.get());
	}

	@Test
	public void testTake3of4() {
		assertEquals(asList(1, 2, 3), Streams.just(1, 2, 3, 4).take(3).toList()
				.single());
	}

	@Test
	public void testTake3of2() {
		assertEquals(asList(1, 2), Streams.just(1, 2).take(3).toList().single());
	}

	@Test
	public void testTake3of3() {
		assertEquals(asList(1, 2, 3), Streams.just(1, 2, 3).take(3).toList()
				.single());
	}

	@Test
	public void testTake0of3() {
		assertTrue(Streams.just(1, 2, 3).take(0).toList().single().isEmpty());
	}

	@Test
	public void testTake3of0() {
		assertTrue(Streams.empty().take(3).toList().single().isEmpty());
	}

	@Test
	public void testTake3ofMax() {
		assertEquals(asList(1, 2, 3), Streams.range(1, 1000000).take(3)
				.toList().single());
	}

	@Test
	public void testIntervalSynchronous() {
		long t = System.currentTimeMillis();
		List<Long> list = Streams
				.interval(100, TimeUnit.MILLISECONDS, Schedulers.immediate())
				.take(3).toList().single();
		assertEquals(asList(0L, 1L, 2L), list);
		assertTrue(System.currentTimeMillis() - t >= 300);
	}

	// @Test
	public void testIntervalPrintln() {
		Streams.interval(1, TimeUnit.SECONDS).forEach(println());
	}

	@Test
	@Ignore
	public void testIntervalAsynchronous() {
		long t = System.currentTimeMillis();
		List<Long> list = Streams.interval(100, TimeUnit.MILLISECONDS).take(3)
				.toList().single();
		assertEquals(asList(0L, 1L, 2L), list);
		assertTrue(System.currentTimeMillis() - t > 300);
	}
}
