package main.phl;

import java.util.ArrayList;
import java.util.List;

public class Hasher {
	private List<String> hashCode;

	public Hasher() {
		hashCode = new ArrayList<>();
	}

	public int hash(String s) {
		if (!hashCode.contains(s))
			hashCode.add(s);
		return hashCode.indexOf(s);
	}

	public String deHash(int i) {
		if (i >= hashCode.size())
			return "NULL";
		return hashCode.get(i);
	}
}