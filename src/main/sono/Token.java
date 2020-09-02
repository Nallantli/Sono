package main.sono;

public class Token {
	private final String line;
	private final String filename;
	private final StringBuilder key;
	private final int index;
	private final int lineNumber;

	public Token(final String key, String line, String filename, int index, final int lineNumber) {
		if (line != null && line.length() > 0) {
			while (line.charAt(0) == '\t' || line.charAt(0) == ' ') {
				line = line.substring(1);
				index--;
			}
		}
		this.line = line;
		this.filename = filename;
		this.index = index;
		this.key = new StringBuilder(key);
		this.lineNumber = lineNumber;
	}

	public void append(final String s) {
		this.key.append(s);
	}

	public void append(final int i) {
		this.key.append(i);
	}

	public void append(final char c) {
		this.key.append(c);
	}

	public int length() {
		return this.key.length();
	}

	public String substring(final int i, final int j) {
		return this.key.substring(i, j);
	}

	public void deleteCharAt(final int i) {
		this.key.deleteCharAt(i);
	}

	public String getKey() {
		return this.key.toString();
	}

	public String getLine() {
		return this.line;
	}

	public char charAt(final int i) {
		return this.key.charAt(i);
	}

	public int getCursor() {
		return this.index;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public String getFilename() {
		if (this.filename == null)
			return "*";
		return this.filename;
	}

	@Override
	public String toString() {
		return this.getKey();
	}
}