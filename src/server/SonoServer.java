package server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import main.SonoWrapper;
import main.phl.PhoneLoader;
import server.io.ErrorOutput;
import server.io.StandardInput;
import server.io.StandardOutput;

public class SonoServer extends WebSocketServer {

	private static int TCP_PORT = 7777;
	private static PhoneLoader pl;

	private final Map<WebSocket, SonoWrapper> conns;
	private final Map<WebSocket, StandardOutput> stdout;
	private final Map<WebSocket, ErrorOutput> stderr;
	private final Map<WebSocket, StandardInput> stdin;

	private final Map<WebSocket, Boolean> WAIT;

	public static void main(final String[] args) {
		SonoWrapper.setGlobalOption("WEB", "TRUE");
		String path = new File(SonoServer.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getParentFile().getParent() + "/bin/";
		try {
			path = URLDecoder.decode(path, "UTF-8");
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		SonoWrapper.setGlobalOption("PATH", path);

		final File directory = new File(System.getProperty("user.home"), ".sono");
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
		final SSLContext context = getContext();
		if (context != null)
			server.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(getContext()));
		server.setConnectionLostTimeout(30);
		server.start();
	}

	private static SSLContext getContext() {
		SSLContext context;
		final String password = "";
		try {
			context = SSLContext.getInstance("TLS");

			final byte[] certBytes = parseDERFromPEM(
					getBytes(Path.of("/", "etc", "letsencrypt", "live", "sonolang.com", "fullchain.pem").toFile()),
					"-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
			final byte[] keyBytes = parseDERFromPEM(
					getBytes(Path.of("/", "etc", "letsencrypt", "live", "sonolang.com", "privkey.pem").toFile()),
					"-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

			final X509Certificate cert = generateCertificateFromDER(certBytes);
			final RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

			final KeyStore keystore = KeyStore.getInstance("JKS");
			keystore.load(null);
			keystore.setCertificateEntry("cert-alias", cert);
			keystore.setKeyEntry("key-alias", key, password.toCharArray(), new Certificate[] { cert });

			final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(keystore, password.toCharArray());

			final KeyManager[] km = kmf.getKeyManagers();

			context.init(km, null, null);
		} catch (final Exception e) {
			e.printStackTrace();
			context = null;
		}
		return context;
	}

	private static byte[] parseDERFromPEM(final byte[] pem, final String beginDelimiter, final String endDelimiter) {
		final String data = new String(pem);
		System.out.println("PARSE DATA\t" + data);
		String[] tokens = data.split(beginDelimiter);
		tokens = tokens[1].split(endDelimiter);
		System.out.println("PARSE TOKENS\t" + tokens[0]);
		return Base64.getMimeDecoder().decode(tokens[0]);
	}

	private static RSAPrivateKey generatePrivateKeyFromDER(final byte[] keyBytes)
			throws InvalidKeySpecException, NoSuchAlgorithmException {
		final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

		final KeyFactory factory = KeyFactory.getInstance("RSA");

		return (RSAPrivateKey) factory.generatePrivate(spec);
	}

	private static X509Certificate generateCertificateFromDER(final byte[] certBytes) throws CertificateException {
		final CertificateFactory factory = CertificateFactory.getInstance("X.509");

		return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
	}

	private static byte[] getBytes(final File file) {
		final byte[] bytesArray = new byte[(int) file.length()];

		try (FileInputStream fis = new FileInputStream(file);) {
			fis.read(bytesArray);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return bytesArray;
	}

	public SonoServer() {
		super(new InetSocketAddress(TCP_PORT));
		conns = new HashMap<>();
		stdout = new HashMap<>();
		stderr = new HashMap<>();
		stdin = new HashMap<>();
		WAIT = new HashMap<>();
	}

	@Override
	public void onStart() {
		System.out.println("Server started on " + this.getAddress().toString());
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}

	@Override
	public void onOpen(final WebSocket conn, final ClientHandshake handshake) {
		System.out.println(LocalDateTime.now(ZoneId.of("America/New_York")) + "\tNew connection from "
				+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
		final StandardOutput out = new StandardOutput(conn);
		final ErrorOutput err = new ErrorOutput(conn);
		final StandardInput in = new StandardInput();
		stdout.put(conn, out);
		stderr.put(conn, err);
		stdin.put(conn, in);
		WAIT.put(conn, false);
		SonoWrapper wrapper = null;
		try {
			wrapper = new SonoWrapper(pl, null, out, err, in, false, null);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			e.printStackTrace();
		}
		conns.put(conn, wrapper);
		out.printHeader("OUT", validate("Sono " + SonoWrapper.VERSION + " - Online Interface\n"));
		out.printHeader("OUT",
				validate("Phonological Data Loaded From <" + SonoWrapper.getGlobalOption("DATA") + ">\n"));
		out.printHeader("OUT", validate("> "));
	}

	@Override
	public void onClose(final WebSocket conn, final int code, final String reason, final boolean remote) {
		conns.remove(conn);
		try {
			System.out.println(LocalDateTime.now(ZoneId.of("America/New_York")) + "\tClosed connection to "
					+ conn.getRemoteSocketAddress().getAddress().toString());
		} catch (final Exception e) {
			e.printStackTrace(System.err);
		}
	}

	@Override
	public void onMessage(final WebSocket conn, final String raw) {
		final String[] sections = raw.split("\n", 2);
		final String header = sections[0];
		final String message = sections[1];

		if (header.equals("CODE")) {
			runCode(conn, message);
		} else if (header.equals("FILE")) {
			final StringBuilder sb = new StringBuilder();
			String line;
			try (BufferedReader br = new BufferedReader(new FileReader(new File("examples", message)));) {
				while ((line = br.readLine()) != null)
					sb.append(line + "\n");
			} catch (final IOException e) {
				e.printStackTrace();
			}
			stdout.get(conn).printHeader("FILE", sb.toString());
		}
	}

	public void runCode(final WebSocket conn, final String message) {
		if (WAIT.get(conn).equals(Boolean.FALSE)) {
			if (message.contains("\n")) {
				String shortened = message.split("\n")[0];
				final int surplus = message.length() - shortened.length();
				shortened += "... (+" + surplus + ")";
				stdout.get(conn).printHeader("OUT", "<span class=\"green\">" + validate(shortened) + "</span>\n");
			} else {
				stdout.get(conn).printHeader("OUT", validate(message + "\n"));
			}

			final ThreadWrapper thread = new ThreadWrapper(this, conns.get(conn), conn, message, stdout.get(conn));
			thread.start();
		} else {
			stdout.get(conn).printHeader("OUT", validate(message) + "\n");
			stdin.get(conn).setInput(message);
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		ex.printStackTrace();
		if (conn != null)
			conns.remove(conn);
		System.out.println(LocalDateTime.now(ZoneId.of("America/New_York")) + "\tERROR from "
				+ conn.getRemoteSocketAddress().getAddress().getHostAddress());
	}

	public void pause(final WebSocket conn) {
		this.WAIT.put(conn, true);
	}

	public void unPause(final WebSocket conn) {
		this.WAIT.put(conn, false);
	}

	public static String validate(String s) {
		s = s.replace("&", "&amp;");
		s = s.replace(">", "&gt;");
		s = s.replace("<", "&lt;");
		s = s.replace("\"", "&quot;");
		return s;
	}
}