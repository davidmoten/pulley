package pulley;

import pulley.util.Optional;

public class Cons<T> {
    private final T head;
    private final Promise<Optional<Cons<T>>> tail;

    public Cons(T head, Promise<Optional<Cons<T>>> tail) {
        this.head = head;
        this.tail = tail;
    }

    public static <T> Cons<T> cons(T head, Promise<Optional<Cons<T>>> tail) {
        return new Cons<T>(head, tail);
    }

    public static <T> Cons<T> cons(T head) {
        return cons(head, Promises.<Cons<T>> empty());
    }

    public T head() {
        return head;
    }

    public Promise<Optional<Cons<T>>> tail() {
        return tail;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Cons [head=");
        builder.append(head);
        builder.append("]");
        return builder.toString();
    }

}
