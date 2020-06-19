package main.phl;

import java.util.ArrayList;
import java.util.List;

public class Word {
	public enum SyllableDelim {
		DELIM {
			@Override
			public String toString() {
				return ".";
			}
		},
		MORPHEME {
			@Override
			public String toString() {
				return "+";
			}
		}
	}

	private List<Object> internal;

	public Word() {
		this.internal = new ArrayList<>();
	}

	public Word(List<Object> internal) {
		this.internal = internal;
	}

	public void add(Object p) {
		this.internal.add(p);
	}

	public void remove(int i) {
		this.internal.remove(i);
	}

	public void addAll(Word w) {
		this.internal.addAll(w.internal);
	}

	public int size() {
		return this.internal.size();
	}

	public Object get(int i) {
		if (i < this.size() && i >= 0)
			return this.internal.get(i);
		return null;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Object p : internal)
			s.append(p.toString());
		return s.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		Word w = (Word) o;
		if (w.size() != this.size())
			return false;
		for (int i = 0; i < this.size(); i++) {
			if (!w.get(i).equals(this.get(i)))
				return false;
		}
		return true;
	}
}