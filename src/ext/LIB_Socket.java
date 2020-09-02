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
	public LIB_Socket(final Interpreter interpreter) {
		super(interpreter);
	}

	public Datum OPEN(final Datum[] data, final Token line) {
		if (SonoWrapper.getGlobalOption("WEB").equals("TRUE"))
			throw error("Socket permissions are disabled for this interpreter.", line);
		final String address = data[0].getString(line);
		final int port = (int) data[1].getNumber(line);
		try {
			final Socket socket = new Socket(address, port);
			return new Datum((Object) socket);
		} catch (final Exception e) {
			throw error("Could not open socket <" + address + ":" + port + ">", line);
		}
	}

	public Datum OUT_OPEN(final Datum[] data, final Token line) {
		final Socket socket = (Socket) data[0].getPointer(line);
		try {
			final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
			return new Datum((Object) out);
		} catch (final Exception e) {
			throw error("Could not open output of socket <" + socket.toString() + ">", line);
		}
	}

	public Datum OUT_SEND(final Datum[] data, final Token line) {
		final DataOutputStream out = (DataOutputStream) data[0].getPointer(line);
		final String value = data[1].getString(line);
		try {
			out.writeUTF(value);
			return new Datum((Object) out);
		} catch (final Exception e) {
			throw error("Could not send output of socket <" + out.toString() + ">", line);
		}
	}

	public Datum OUT_CLOSE(final Datum[] data, final Token line) {
		final DataOutputStream out = (DataOutputStream) data[0].getPointer(line);
		try {
			out.close();
			return new Datum();
		} catch (final Exception e) {
			throw error("Could not close output of socket <" + out.toString() + ">", line);
		}
	}

	public Datum IN_OPEN(final Datum[] data, final Token line) {
		final Socket socket = (Socket) data[0].getPointer(line);
		try {
			final DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			return new Datum((Object) in);
		} catch (final Exception e) {
			throw error("Could not open input of socket <" + socket.toString() + ">", line);
		}
	}

	public Datum IN_READ(final Datum[] data, final Token line) {
		final DataInputStream in = (DataInputStream) data[0].getPointer(line);
		try {
			final String s = in.readUTF();
			return new Datum(s);
		} catch (final Exception e) {
			throw error("Could not read input of socket <" + in.toString() + ">", line);
		}
	}

	public Datum IN_CLOSE(final Datum[] data, final Token line) {
		final DataInputStream in = (DataInputStream) data[0].getPointer(line);
		try {
			in.close();
			return new Datum();
		} catch (final Exception e) {
			throw error("Could not close input of socket <" + in.toString() + ">", line);
		}
	}

	public Datum CLOSE(final Datum[] data, final Token line) {
		final Socket socket = (Socket) data[0].getPointer(line);
		try {
			socket.close();
			return new Datum();
		} catch (final Exception e) {
			throw error("Could not close socket <" + socket.toString() + ">", line);
		}
	}

	public Datum SERVER_OPEN(final Datum[] data, final Token line) {
		if (SonoWrapper.getGlobalOption("WEB").equals("TRUE"))
			throw error("Socket permissions are disabled for this interpreter.", line);
		final int port = (int) data[0].getNumber(line);
		try {
			final ServerSocket server = new ServerSocket(port);
			return new Datum((Object) server);
		} catch (final Exception e) {
			throw error("Could not open server <" + port + ">", line);
		}
	}

	public Datum SERVER_ACCEPT(final Datum[] data, final Token line) {
		final ServerSocket server = (ServerSocket) data[0].getPointer(line);
		try {
			final Socket socket = server.accept();
			return new Datum((Object) socket);
		} catch (final Exception e) {
			throw error("Could not connect client for <" + server.toString() + ">", line);
		}
	}

	public Datum SERVER_CLOSE(final Datum[] data, final Token line) {
		final ServerSocket server = (ServerSocket) data[0].getPointer(line);
		try {
			server.close();
			return new Datum();
		} catch (final Exception e) {
			throw error("Could not close server <" + server.toString() + ">", line);
		}
	}
}