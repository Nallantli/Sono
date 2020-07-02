package server.io;

import org.java_websocket.WebSocket;

import main.sono.io.Output;

public class ErrorOutput extends Output {
	private final WebSocket conn;

	public ErrorOutput(final WebSocket conn) {
		this.conn = conn;
	}

	@Override
	public void print(final String s) {
		conn.send("OUT\n<span class=\"red\">" + s + "</span>");
	}
}