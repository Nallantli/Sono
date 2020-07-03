package server.io;

import java.math.BigDecimal;

import org.java_websocket.WebSocket;

import main.sono.io.Input;

public class StandardInput extends Input {
	private final WebSocket conn;

	public StandardInput(final WebSocket conn) {
		this.conn = conn;
	}

	@Override
	public String getLine() {
		try {
			System.out.println("PAUSING THREAD\t" + Thread.currentThread());
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BigDecimal getNumber() {
		try {
			System.out.println("PAUSING THREAD\t" + Thread.currentThread());
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}