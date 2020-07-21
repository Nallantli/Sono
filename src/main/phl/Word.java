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
		},
		NULL {
			@Override
			public String toString() {
				return "";
			}
		}
	}

	private final List<Phone> phones;
	private final List<SyllableDelim> delimits;

	public Word() {
		this.phones = new ArrayList<>();
		this.delimits = new ArrayList<>();
	}

	public Word(final List<Phone> phones, final List<SyllableDelim> delimits) {
		this.phones = phones;
		this.delimits = delimits;
	}

	public void add(final Phone p) {
		this.phones.add(p);
	}

	public void remove(final int i) {
		this.phones.remove(i);
	}

	public void addAll(final Word w) {
		this.phones.addAll(w.phones);
		this.delimits.addAll(w.delimits);
	}

	public int size() {
		return this.phones.size();
	}

	public Phone get(final int i) {
		if (i < this.phones.size() && i >= 0)
			return this.phones.get(i);
		return null;
	}

	public SyllableDelim getDelim(final int i) {
		if (i < this.delimits.size() && i >= 0)
			return this.delimits.get(i);
		return SyllableDelim.NULL;
	}

	@Override
	public String toString() {
		final StringBuilder s = new StringBuilder();
		for (int i = 0; i < phones.size(); i++) {
			s.append(delimits.get(i).toString());
			s.append(phones.get(i).toString());
		}
		return s.toString();
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Word w = (Word) o;
		if (w.size() != this.size())
			return false;
		for (int i = 0; i < this.size(); i++) {
			if (!w.get(i).equals(this.get(i)))
				return false;
			if (!w.getDelim(i).equals(this.getDelim(i)))
				return false;
		}
		return true;
	}
}