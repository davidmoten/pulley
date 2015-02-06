package pulley;

final class Util {

	private static final UnexpectedException UNEXPECTED = new UnexpectedException();

	public static <T> T unexpected() {
		throw UNEXPECTED;
	}
}
