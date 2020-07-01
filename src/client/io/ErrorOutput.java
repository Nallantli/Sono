package client.io;

import main.sono.io.Output;

public class ErrorOutput extends Output {
	@Override
	public void print(final String s) {
		System.err.print(s);
	}
}