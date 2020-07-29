package ext;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import main.SonoWrapper;
import main.base.Library;
import main.sono.Datum;
import main.sono.Interpreter;
import main.sono.Token;

public class LIB_Socket extends Library {
	public LIB_Socket() {
		super();
		commands.put("LIB_Socket.OPEN", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			if (SonoWrapper.getGlobalOption("SOCKET").equals("FALSE"))
				throw error("Socket permissions are disabled for this interpreter.", line);
			final String address = datum.getVector(line)[0].getString(line);
			final int port = (int) datum.getVector(line)[1].getNumber(line);
			try {
				final Socket socket = new Socket(address, port);
				return new Datum((Object) socket);
			} catch (final Exception e) {
				throw error("Could not open socket <" + address + ":" + port + ">", line);
			}
		});
		commands.put("LIB_Socket.OUT.OPEN", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final Socket socket = (Socket) datum.getPointer(line);
			try {
				final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
				return new Datum((Object) out);
			} catch (final Exception e) {
				throw error("Could not open output of socket <" + socket.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.OUT.SEND", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final DataOutputStream out = (DataOutputStream) datum.getVector(line)[0].getPointer(line);
			final String value = datum.getVector(line)[1].getString(line);
			try {
				out.writeUTF(value);
				return new Datum((Object) out);
			} catch (final Exception e) {
				throw error("Could not send output of socket <" + out.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.OUT.CLOSE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final DataOutputStream out = (DataOutputStream) datum.getPointer(line);
			try {
				out.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close output of socket <" + out.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.IN.OPEN", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final Socket socket = (Socket) datum.getPointer(line);
			try {
				final DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				return new Datum((Object) in);
			} catch (final Exception e) {
				throw error("Could not open input of socket <" + socket.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.IN.READ", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final DataInputStream in = (DataInputStream) datum.getPointer(line);
			try {
				final String s = in.readUTF();
				return new Datum(s);
			} catch (final Exception e) {
				throw error("Could not read input of socket <" + in.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.IN.CLOSE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final DataInputStream in = (DataInputStream) datum.getPointer(line);
			try {
				in.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close input of socket <" + in.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.CLOSE", (final Datum datum, final Token line, final Interpreter interpreter) -> {
			final Socket socket = (Socket) datum.getPointer(line);
			try {
				socket.close();
				return new Datum();
			} catch (final Exception e) {
				throw error("Could not close socket <" + socket.toString() + ">", line);
			}
		});
		commands.put("LIB_Socket.SERVER.OPEN",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					if (SonoWrapper.getGlobalOption("SOCKET").equals("FALSE"))
						throw error("Socket permissions are disabled for this interpreter.", line);
					final int port = (int) datum.getNumber(line);
					try {
						final ServerSocket server = new ServerSocket(port);
						return new Datum((Object) server);
					} catch (final Exception e) {
						throw error("Could not open server <" + port + ">", line);
					}
				});
		commands.put("LIB_Socket.SERVER.ACCEPT",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final ServerSocket server = (ServerSocket) datum.getPointer(line);
					try {
						final Socket socket = server.accept();
						return new Datum((Object) socket);
					} catch (final Exception e) {
						throw error("Could not connect client for <" + server.toString() + ">", line);
					}
				});
		commands.put("LIB_Socket.SERVER.CLOSE",
				(final Datum datum, final Token line, final Interpreter interpreter) -> {
					final ServerSocket server = (ServerSocket) datum.getPointer(line);
					try {
						server.close();
						return new Datum();
					} catch (final Exception e) {
						throw error("Could not close server <" + server.toString() + ">", line);
					}
				});
	}
}