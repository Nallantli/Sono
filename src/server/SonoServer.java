package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import main.SonoWrapper;
import main.phl.PhoneLoader;
import main.sono.Datum;
import server.io.ErrorOutput;
import server.io.StandardOutput;

public class SonoServer extends WebSocketServer {

	private static int TCP_PORT = 80;
	private static String path;
	private static PhoneLoader pl;

	private final Map<WebSocket, SonoWrapper> conns;
	private final Map<WebSocket, StandardOutput> stdout;
	private final Map<WebSocket, ErrorOutput> stderr;

	public static void main(final String[] args) throws Exception {
		SonoWrapper.setGlobalOption("LING", "TRUE");
		SonoWrapper.setGlobalOption("WRITE", "FALSE");
		SonoWrapper.setGlobalOption("SOCKET", "FALSE");
		SonoWrapper.setGlobalOption("GRAPHICS", "FALSE");
		path = SonoServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		path = path.replace("/res/SonoServer.jar", "");

		SonoWrapper.setGlobalOption("PATH", path);

		final File directory = new File(path, ".config");
		if (!directory.exists()) {
			System.err.println("Please initialize /config file with -d");
			System.exit(1);
		}

		final File config = new File(directory, "config");
		if (config.exists()) {
			try (FileReader fr = new FileReader(config); BufferedReader br = new BufferedReader(fr);) {
				String line;
				while ((line = br.readLine()) != null) {
					final String[] s = line.split("=");
					SonoWrapper.setGlobalOption(s[0], s[1]);
				}
			} catch (final IOException e) {
				System.err.println("Error reading from /config file.");
				System.exit(1);
			}
		} else {
			System.err.println("Please initialize /config file with -d");
			System.exit(1);
		}

		try {
			pl = new PhoneLoader(SonoWrapper.getGlobalOption("DATA"), false);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		final SonoServer server = new SonoServer();
		server.start();
	}

	public SonoServer() {
		super(new InetSocketAddress(TCP_PORT));
		conns = new HashMap<>();
		stdout = new HashMap<>();
		stderr = new HashMap<>();
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		System.out.println("New connection from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
		final StandardOutput out = new StandardOutput(conn);
		final ErrorOutput err = new ErrorOutput(conn);
		stdout.put(conn, out);
		stderr.put(conn, err);
		conns.put(conn, new SonoWrapper(pl, path, null, out, err));
		out.println(validate("Sono " + SonoWrapper.VERSION + " - Online Interface"));
		out.println(validate("Phonological Data Loaded From <" + SonoWrapper.getGlobalOption("DATA") + ">"));
		conns.get(conn).run("load \"system\";");
		out.println(validate("Loaded System Library"));
		out.print(validate("> "));
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		conns.remove(conn);
		try {
			System.out.println("Closed connection to " + code);
		} catch (final Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void onMessage(final WebSocket conn, final String message) {
		final StringBuilder sb = new StringBuilder();
		stdout.get(conn).println(validate(message));
		final Datum output = conns.get(conn).run(message);
		sb.append("<span class=\"blue\">");
		if (output.getType() == Datum.Type.VECTOR) {
			int i = 0;
			for (final Datum e : output.getVector(new ArrayList<>())) {
				sb.append("\t" + i++ + ":\t" + validate(e.toStringTrace(new ArrayList<>())) + "\n");
			}
		} else {
			sb.append("\t" + validate(output.toStringTrace(new ArrayList<>())) + "\n");
		}
		sb.append("</span>");
		stdout.get(conn).println(sb.toString());

		stdout.get(conn).print(validate("> "));
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null) {
			conns.remove(conn);
		}
		System.out.println("ERROR from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	public static String validate(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace(">", "&gt;");
		s = s.replace("<", "&lt;");
		s = s.replace("\"", "&quot;");
		return s;
	}
}