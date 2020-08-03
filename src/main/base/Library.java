package main.base;

import main.sono.Interpreter;
import main.sono.Token;
import main.sono.err.SonoRuntimeException;

public class Library {
	protected final Interpreter interpreter;

	public Library(final Interpreter interpreter) {
		this.interpreter = interpreter;
	}

	protected SonoRuntimeException error(final String message, final Token line) {
		return new SonoRuntimeException(message, line);
	}
}