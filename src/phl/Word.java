package src.phl;

import java.util.ArrayList;
import java.util.List;

public class Word {
	private List<Phone> internal;

	public Word() {
		this.internal = new ArrayList<>();
	}

	public Word(List<Phone> internal) {
		this.internal = internal;
	}

	public void add(Phone p) {
		this.internal.add(p);
	}

	public void addAll(Word w) {
		this.internal.addAll(w.internal);
	}

	public int size() {
		return this.internal.size();
	}

	public Phone get(int i) {
		return this.internal.get(i);
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for (Phone p : internal)
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