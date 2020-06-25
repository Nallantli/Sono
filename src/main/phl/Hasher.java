package main.phl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Hasher implements Serializable {
	private static final long serialVersionUID = 1L;

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