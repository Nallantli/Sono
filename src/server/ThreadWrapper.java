package server;

import java.util.ArrayList;

import org.java_websocket.WebSocket;

import main.SonoWrapper;
import main.sono.Datum;
import server.io.StandardOutput;

public class ThreadWrapper extends Thread {
	private final SonoWrapper wrapper;
	private final String code;
	private final StandardOutput stdout;
	private final SonoServer server;
	private final WebSocket conn;

	public ThreadWrapper(final SonoServer server, final SonoWrapper wrapper, final WebSocket conn, final String code,
			final StandardOutput stdout) {
		this.server = server;
		this.wrapper = wrapper;
		this.code = code;
		this.stdout = stdout;
		this.conn = conn;
	}

	@Override
	public void run() {
		stdout.printHeader("STATUS", "TRUE");
		server.pause(conn);
		final Datum output = wrapper.run("examples", code);

		final StringBuilder sb = new StringBuilder();

		if (output.getType() == Datum.Type.VECTOR) {
			sb.append("\n<details class=\"fold\">");
			sb.append("<summary>Raw Output Vector (" + output.getVectorLength(null)
					+ " <i class=\"fab fa-buffer\"></i>)</summary>");
			for (int i = 0; i < output.getVectorLength(null); i++)
				sb.append("\t" + i + ":\t" + SonoServer.validate(output.indexVector(i).toStringTrace(new ArrayList<>())) + "\n");
			sb.append("</details>");
		} else {
			sb.append("\n<span class=\"blue\">");
			sb.append("\t" + SonoServer.validate(output.toStringTrace(new ArrayList<>())) + "\n");
			sb.append("</span>");
		}
		stdout.printHeader("OUT", sb.toString() + "\n");

		stdout.printHeader("OUT", SonoServer.validate("> "));

		stdout.printHeader("STATUS", "FALSE");
		server.unPause(conn);
	}
}