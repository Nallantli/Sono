package src.sono.err;

import java.util.ArrayList;

public class SonoCompilationException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoCompilationException(String message) {
		super("COMPILATION ERROR: " + message, new ArrayList<>());
	}
}