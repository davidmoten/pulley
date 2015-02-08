package pulley;

import static pulley.Actions.println;

import org.junit.Test;

public class StreamsTest {

	@Test
	public void testHelloWorld() {
		Stream.just("hello world").forEach(println());
	}

}
