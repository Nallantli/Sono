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
		Map<String, Command> commands = new HashMap<>();
		commands.put("Sock.OPEN", (Datum datum, List<String> trace) -> {
			String address = datum.getVector(trace).get(0).getString(trace);
			int port = datum.getVector(trace).get(1).getNumber(trace).intValue();
			try {
				Socket socket = new Socket(address, port);
				return new Datum((Object)socket);
			} catch (Exception e) {
				throw error("Could not open socket <" + address + ":" + port + ">", trace);
			}
		});
		commands.put("Sock.OUT.OPEN", (Datum datum, List<String> trace) -> {
			Socket socket = (Socket) datum.getPointer(trace);
			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				return new Datum((Object)out);
			} catch (Exception e) {
				throw error("Could not open output of socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.OUT.SEND", (Datum datum, List<String> trace) -> {
			DataOutputStream out = (DataOutputStream) datum.getVector(trace).get(0).getPointer(trace);
			String value = datum.getVector(trace).get(1).getString(trace);
			try {
				out.writeUTF(value);
				return new Datum((Object)out);
			} catch (Exception e) {
				throw error("Could not send output of socket <" + out.toString() + ">", trace);
			}
		});
		commands.put("Sock.OUT.CLOSE", (Datum datum, List<String> trace) -> {
			DataOutputStream out = (DataOutputStream) datum.getPointer(trace);
			try {
				out.close();
				return new Datum();
			} catch (Exception e) {
				throw error("Could not close output of socket <" + out.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.OPEN", (Datum datum, List<String> trace) -> {
			Socket socket = (Socket) datum.getPointer(trace);
			try {
				DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				return new Datum((Object)in);
			} catch (Exception e) {
				throw error("Could not open input of socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.READ", (Datum datum, List<String> trace) -> {
			DataInputStream in = (DataInputStream) datum.getPointer(trace);
			try {
				String s = in.readUTF();
				return new Datum(s);
			} catch (Exception e) {
				throw error("Could not read input of socket <" + in.toString() + ">", trace);
			}
		});
		commands.put("Sock.IN.CLOSE", (Datum datum, List<String> trace) -> {
			DataInputStream in = (DataInputStream) datum.getPointer(trace);
			try {
				in.close();
				return new Datum();
			} catch (Exception e) {
				throw error("Could not close input of socket <" + in.toString() + ">", trace);
			}
		});
		commands.put("Sock.CLOSE", (Datum datum, List<String> trace) -> {
			Socket socket = (Socket) datum.getPointer(trace);
			try {
				socket.close();
				return new Datum();
			} catch (Exception e) {
				throw error("Could not close socket <" + socket.toString() + ">", trace);
			}
		});
		commands.put("Sock.SERVER.OPEN", (Datum datum, List<String> trace) -> {
			int port = datum.getNumber(trace).intValue();
			try {
				ServerSocket server = new ServerSocket(port);
				return new Datum((Object)server);
			} catch (Exception e) {
				throw error("Could not open server <" + port + ">", trace);
			}
		});
		commands.put("Sock.SERVER.ACCEPT", (Datum datum, List<String> trace) -> {
			ServerSocket server = (ServerSocket) datum.getPointer(trace);
			try {
				Socket socket = server.accept();
				return new Datum((Object)socket);
			} catch (Exception e) {
				throw error("Could not connect client for <" + server.toString() + ">", trace);
			}
		});
		commands.put("Sock.SERVER.CLOSE", (Datum datum, List<String> trace) -> {
			ServerSocket server = (ServerSocket) datum.getPointer(trace);
			try {
				server.close();
				return new Datum();
			} catch (Exception e) {
				throw error("Could not close server <" + server.toString() + ">", trace);
			}
		});
		setCommands(commands);
	}
}