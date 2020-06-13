package intr;

public class SonoRuntimeException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoRuntimeException(String message) {
		super("RUNTIME ERROR: " + message);
	}
}