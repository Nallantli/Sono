package client.io;

import main.sono.io.Output;

public class StandardOutput extends Output {
	@Override
	public void print(final String s) {
		System.out.print(s);
	}
}