package main.sono.err;

import java.util.List;

public class SonoRuntimeException extends SonoException {
	private static final long serialVersionUID = 1L;

	public SonoRuntimeException(String message, List<String> trace) {
		super("RUNTIME ERROR: " + message, trace);
	}
}