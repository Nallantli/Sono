package ext;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import main.base.Command;
import main.base.Library;
import main.sono.Datum;

public class Sock extends Library {
	public Sock() {
		final Map<String, Command> commands = new HashMap<>();
		commands.put("Sock.OPEN", (final Datum datum, final List<String> trace) -> {
			final String address = datum.getVector(trace).get(0).getString(trace);
			final int port = datum.getVector(trace).get(1).getNumber(trace).intValue();
			try {
				final Socket socket = new Socket(address, port);
				return new Datum((Object) socket);
			} catch (final Exception e) {
				throw error("Could not open socket <" + address + ":" + port + ">", trace);
			}
		});
		commands.put("Sock.OUT.OPEN", (final Datum datum, final List<String> trace) -> {
			final Socket socket = (Socket) datum.getPointer(trace);
			try {
				final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				return new Datum((Object) out);
			} catch (final Exception e) {
				throw error("Could not open output of socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.OUT.SEND", (final Datum datum, final List<String> trace) -> {
			final DataOutputStream out = (DataOutputStream) datum.getVector(trace).get(0).getPointer(trace);
			final String value = datum.getVector(trace).get(1).getString(trace);
			try {
				out.writeUTF(value);
				return new Datum((Object) out);
			} catch (final Exception e) {
				throw error("Could not send output of socket <" + out.toString() + ">", trace);
			}
		});
		commands.put("Sock.OUT.CLOSE", (final Datum datum, final List<String> trace) -> {
			final DataOutputStream out = (DataOutputStream) datum.getPointer(trace);
			try {
				out.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close output of socket <" + out.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.OPEN", (final Datum datum, final List<String> trace) -> {
			final Socket socket = (Socket) datum.getPointer(trace);
			try {
				final DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				return new Datum((Object) in);
			} catch (final Exception e) {
				throw error("Could not open input of socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.READ", (final Datum datum, final List<String> trace) -> {
			final DataInputStream in = (DataInputStream) datum.getPointer(trace);
			try {
				final String s = in.readUTF();
				return new Datum(s);
			} catch (final Exception e) {
				throw error("Could not read input of socket <" + in.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.CLOSE", (final Datum datum, final List<String> trace) -> {
			final DataInputStream in = (DataInputStream) datum.getPointer(trace);
			try {
				in.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close input of socket <" + in.toString() + ">", trace);
			}
		});
		commands.put("Sock.CLOSE", (final Datum datum, final List<String> trace) -> {
			final Socket socket = (Socket) datum.getPointer(trace);
			try {
				socket.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.SERVER.OPEN", (final Datum datum, final List<String> trace) -> {
			final int port = datum.getNumber(trace).intValue();
			try {
				final ServerSocket server = new ServerSocket(port);
				return new Datum((Object) server);
			} catch (final Exception e) {
				throw error("Could not open server <" + port + ">", trace);
			}
		});
		commands.put("Sock.SERVER.ACCEPT", (final Datum datum, final List<String> trace) -> {
			final ServerSocket server = (ServerSocket) datum.getPointer(trace);
			try {
				final Socket socket = server.accept();
				return new Datum((Object) socket);
			} catch (final Exception e) {
				throw error("Could not connect client for <" + server.toString() + ">", trace);
			}
		});
		commands.put("Sock.SERVER.CLOSE", (final Datum datum, final List<String> trace) -> {
			final ServerSocket server = (ServerSocket) datum.getPointer(trace);
			try {
				server.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close server <" + server.toString() + ">", trace);
			}
		});
		setCommands(commands);
	}
}