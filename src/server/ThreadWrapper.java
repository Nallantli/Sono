package server;

import java.util.ArrayList;

import main.SonoWrapper;
import main.sono.Datum;
import server.io.StandardOutput;

public class ThreadWrapper extends Thread {
	private final SonoWrapper wrapper;
	private final String code;
	private final StandardOutput stdout;
	private final SonoServer server;

	public ThreadWrapper(final SonoServer server, final SonoWrapper wrapper, final String code,
			final StandardOutput stdout) {
		this.server = server;
		this.wrapper = wrapper;
		this.code = code;
		this.stdout = stdout;
	}

	@Override
	public void run() {
		server.pause();
		System.out.println("RUNNING SERVER CODE THREAD\t" + Thread.currentThread());
		final Datum output = wrapper.run(code);

		final StringBuilder sb = new StringBuilder();

		if (output.getType() == Datum.Type.VECTOR) {
			sb.append("\n<details class=\"fold\">");
			sb.append("<summary>Raw Output Vector (" + output.getVector(new ArrayList<>()).size() + " <i class=\"fab fa-buffer\"></i>)</summary>");
			int i = 0;
			for (final Datum e : output.getVector(new ArrayList<>())) {
				sb.append("\t" + i++ + ":\t" + SonoServer.validate(e.toStringTrace(new ArrayList<>())) + "\n");
			}
			sb.append("</details>");
		} else {
			sb.append("\n<span class=\"blue\">");
			sb.append("\t" + SonoServer.validate(output.toStringTrace(new ArrayList<>())) + "\n");
			sb.append("</span>");
		}
		stdout.printHeader("OUT", sb.toString() + "\n");

		stdout.printHeader("OUT", SonoServer.validate("> "));

		server.unpause();
	}
}