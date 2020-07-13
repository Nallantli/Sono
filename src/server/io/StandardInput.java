package server.io;

import main.sono.io.Input;

public class StandardInput extends Input {
	private String input;

	public StandardInput() {
		this.input = null;
	}

	@Override
	public synchronized String getLine() {
		try {
			wait();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return input;
	}

	@Override
	public synchronized double getNumber() {
		try {
			wait();
		} catch (final InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
		return Double.valueOf(input);
	}

	public synchronized void setInput(final String input) {
		this.input = input;
		notify();
	}
}