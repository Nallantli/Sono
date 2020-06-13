package src.sono.err;

public class SonoException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SonoException(String message) {
		super(message);
	}
}