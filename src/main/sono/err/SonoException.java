package main.sono.err;

import main.sono.Token;

public class SonoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final transient Token line;

	public SonoException(final String message, final Token line) {
		super(message);
		this.line = line;
	}

	public String getLine() {
		if (this.line != null) {
			final StringBuilder s = new StringBuilder(
					"\tLine " + (this.line.getLineNumber() + 1) + ":\n\t" + this.line.getLine() + "\n\t");
			for (int i = 0; i < line.getCursor() - 1; i++)
				s.append(" ");
			s.append("^");
			for (int i = 1; i < line.getKey().length(); i++) {
				s.append("~");
			}
			s.append("\n");
			return s.toString();
		}
		return "";
	}
}