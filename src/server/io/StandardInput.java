package server.io;

import java.math.BigDecimal;

import org.java_websocket.WebSocket;

import main.SonoWrapper;
import main.sono.io.Input;

public class StandardInput extends Input {
	private final WebSocket conn;
	private SonoWrapper wrapper;

	public StandardInput(final WebSocket conn) {
		this.conn = conn;
	}

	public void setWrapper(SonoWrapper wrapper) {
		this.wrapper = wrapper;
	}

	@Override
	public String getLine() {
		try {
			wrapper.pauseExecution();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public BigDecimal getNumber() {
		try {
			wrapper.pauseExecution();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

}