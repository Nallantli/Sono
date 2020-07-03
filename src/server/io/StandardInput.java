package server.io;

import java.math.BigDecimal;

import org.java_websocket.WebSocket;

import main.sono.io.Input;

public class StandardInput extends Input {
	private final WebSocket conn;
	private String input;

	public StandardInput(final WebSocket conn) {
		this.conn = conn;
	}

	@Override
	public synchronized String getLine() {
		try {
			System.out.println("PAUSING THREAD\t" + Thread.currentThread());
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return input;
	}

	@Override
	public synchronized BigDecimal getNumber() {
		try {
			System.out.println("PAUSING THREAD\t" + Thread.currentThread());
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new BigDecimal(input);
	}

	public synchronized void setInput(String input) {
		this.input = input;
		notify();
	}

}