package pulley;

public interface PromiseFactory<T> {
	Promise<T> create();
}
