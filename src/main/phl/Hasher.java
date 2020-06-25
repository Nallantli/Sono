package main.phl;

import java.util.ArrayList;
import java.util.List;

public class Hasher {
	private static List<String> hashCode = new ArrayList<>();
	public static final int ZERO = Hasher.hash("0");
	public static final int TRUE = Hasher.hash("+");
	public static final int FALSE = Hasher.hash("-");
	public static final int ANY = Hasher.hash("~");

	public static int hash(final String s) {
		if (!hashCode.contains(s))
			hashCode.add(s);
		return hashCode.indexOf(s);
	}

	public static String deHash(final int i) {
		if (i >= hashCode.size())
			return "NULL";
		return hashCode.get(i);
	}
}