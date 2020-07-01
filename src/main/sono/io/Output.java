package main.sono.io;

public abstract class Output {
	public abstract void print(String s);

	public void println(final String s) {
		print(s + "\n");
	}
}