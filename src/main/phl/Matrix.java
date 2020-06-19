package main.phl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Matrix implements Iterable<Pair>, Serializable {
	private static final long serialVersionUID = 1L;

	private List<Pair> holder;

	public Matrix() {
		holder = new ArrayList<>();
	}

	public Matrix(Pair... entries) {
		holder = new ArrayList<>(List.of(entries));
	}

	private int getIndexOf(Phone.Feature key) {
		for (int i = 0; i < holder.size(); i++)
			if (holder.get(i).getFeature() == key)
				return i;
		return -1;
	}

	public String get(Phone.Feature key) {
		int i = getIndexOf(key);
		if (i >= 0)
			return holder.get(i).getQuality();
		return "0";
	}

	public Pair get(int i) {
		return holder.get(i);
	}

	public void put(Pair p) {
		if (Phone.majorClasses.containsKey(p.getFeature()) && (p.getQuality().equals("-") || p.getQuality().equals("~"))) {
			for (Phone.Feature f : Phone.majorClasses.get(p.getFeature()))
				put(f, "0");
		} else {
			Phone.Feature im = Phone.inMajorClass(p.getFeature());
			if (im != null && get(im).equals("~")) {
				p = new Pair(p.getFeature(), "0");
			}
		}

		int i = getIndexOf(p.getFeature());

		if (p.getQuality().equals("0") && i >= 0) {
			holder.remove(i);
			return;
		} else if (p.getQuality().equals("0")) {
			return;
		}

		if (i >= 0)
			holder.set(i, p);
		else
			holder.add(p);
	}

	public void put(Phone.Feature f, String q) {
		put(new Pair(f, q));
	}

	public void putAll(Matrix m) {
		for (Pair p : m.holder) {
			if ((get(p.getFeature()).equals("+") && p.getQuality().equals("-")) || (get(p.getFeature()).equals("-") && p.getQuality().equals("+")))
				put(new Pair(p.getFeature(), "~"));
			else if (get(p.getFeature()).equals("~") || p.getQuality().equals("~"))
				put(new Pair(p.getFeature(), "~"));
			else
				put(p);
		}
	}

	public int size() {
		return holder.size();
	}

	@Override
	public String toString() {
		return holder.toString();
	}

	public boolean isEmpty() {
		return holder.isEmpty();
	}

	@Override
	public Iterator<Pair> iterator() {
		return new MatrixIterator(this.holder);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		Matrix m = (Matrix) o;
		if (size() != m.size())
			return false;
		for (Pair p : holder) {
			if (!m.get(p.getFeature()).equals(p.getQuality()))
				return false;
		}
		return true;
	}
}

class MatrixIterator implements Iterator<Pair> {
	List<Pair> holder;
	int i;

	MatrixIterator(List<Pair> holder) {
		this.holder = holder;
		i = 0;
	}

	@Override
	public boolean hasNext() {
		return i < holder.size();
	}

	@Override
	public Pair next() {
		if (i >= holder.size())
			throw new NoSuchElementException("Matrix iterator out of bounds: " + i);
		return holder.get(i++);
	}
}