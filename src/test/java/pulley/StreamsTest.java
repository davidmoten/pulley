package pulley;

import static org.junit.Assert.assertEquals;
import static pulley.Actions.println;

import org.junit.Test;

public class StreamsTest {

	@Test
	public void testHelloWorld() {
		Stream.just("hello world").forEach(println());
	}

	@Test
	public void testMap() {
		assertEquals(2, (int) Stream.just(1).map(plusOne()).single());
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
		Stream.just(1, 2).single();
	}

	@Test
	public void testSingle() {
		assertEquals(1, (int) Stream.just(1).single());
	}

}
