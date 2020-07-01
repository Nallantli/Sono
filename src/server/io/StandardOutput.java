package server.io;

import org.java_websocket.WebSocket;

import main.sono.io.Output;

public class StandardOutput extends Output {
	private final WebSocket conn;

	public StandardOutput(final WebSocket conn) {
		this.conn = conn;
	}

	@Override
	public void print(final String s) {
		conn.send(s);
	}
}