package src.sono.err;

import java.util.Collections;
import java.util.List;

public class SonoException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private final List<String> trace;
	public SonoException(String message, List<String> trace) {
		super(message);
		this.trace = trace;
	}

	@Override
	public void printStackTrace() {
		Collections.reverse(trace);
		for (String o : trace) {
			String s = o.substring(0, Math.min(100,o.length()));
			if (!s.equals(o))
				s += " ... (+" + (o.length() - 100) + ")";
			System.err.println("\t^ " + s);
		}
	}
}