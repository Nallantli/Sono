package server.io;

import org.java_websocket.WebSocket;

import main.sono.io.Output;

public class StandardOutput extends Output {
	private final WebSocket conn;

	public StandardOutput(final WebSocket conn) {
		this.conn = conn;
	}

	public void printHeader(final String header, final String s) {
		conn.send(header + "\n" + s);
	}

	@Override
	public void print(final String s) {
		conn.send("OUT\n" + s);
	}
}