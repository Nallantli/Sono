package main.sono.err;

import java.util.List;

public class SonoRuntimeException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoRuntimeException(final String message, final List<String> trace) {
		super("RUNTIME ERROR: " + message, trace);
	}
}