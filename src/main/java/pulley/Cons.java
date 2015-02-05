package pulley;

public class Cons<T> {
	private final T head;
	private final Stream<T> tail;

	public Cons(T head, Stream<T> tail) {
		this.head = head;
		this.tail = tail;
	}

	public T head() {
		return head;
	}

	public Stream<T> tail() {
		return tail;
	}

}
