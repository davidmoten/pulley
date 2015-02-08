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
		int n = Stream.just(1).map(new F1<Integer, Integer>() {
			@Override
			public Integer call(Integer t) {
				return t + 1;
			}
		}).single();
		assertEquals(2, n);
	}

}
