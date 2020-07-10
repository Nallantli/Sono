package main.sono.err;

import java.util.Collections;
import java.util.List;

public class SonoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final List<String> trace;
	public SonoException(final String message, final List<String> trace) {
		super(message);
		this.trace = trace;
	}

	public String getStackString() {
		Collections.reverse(trace);
		final StringBuilder sb = new StringBuilder();
		for (final String o : trace) {
			String s = o.substring(0, Math.min(100,o.length()));
			if (!s.equals(o))
				s += " ... (+" + (o.length() - 100) + ")";
			sb.append("\t^ " + s + "\n");
		}
		return sb.toString();
	}

	@Override
	public void printStackTrace() {
		System.err.print(getStackString());
	}
}