package src.sono.err;

public class SonoCompilationException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoCompilationException(String message) {
		super("COMPILATION ERROR: " + message);
	}
}