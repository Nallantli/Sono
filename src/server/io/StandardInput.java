package server.io;

import java.math.BigDecimal;

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
		}
		return input;
	}

	@Override
	public synchronized BigDecimal getNumber() {
		try {
			wait();
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return new BigDecimal(input);
	}

	public synchronized void setInput(final String input) {
		this.input = input;
		notify();
	}

}