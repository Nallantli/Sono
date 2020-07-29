package main.sono.err;

import main.sono.Token;

public class SonoRuntimeException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoRuntimeException(final String message, final Token line) {
		super("RUNTIME ERROR: " + message, line);
	}
}