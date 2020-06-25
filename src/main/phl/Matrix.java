package main.phl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Matrix implements Iterable<Pair>, Serializable {
	private static final long serialVersionUID = 1L;

	private final List<Pair> holder;

	public Matrix() {
		holder = new ArrayList<>();
	}

	public Matrix(final PhoneManager pm, final Matrix m) {
		holder = new ArrayList<>();
		for (final Pair p : m.holder) {
			holder.add(new Pair(pm, p.getFeature(), p.getQuality()));
		}
	}

	public Matrix(final Pair... entries) {
		holder = new ArrayList<>(List.of(entries));
	}

	private int getIndexOf(final int key) {
		for (int i = 0; i < holder.size(); i++)
			if (holder.get(i).getFeature() == key)
				return i;
		return -1;
	}

	public String getQuality(final int key) {
		final int i = getIndexOf(key);
		if (i >= 0)
			return holder.get(i).getQuality();
		return "0";
	}

	/*
	 * public Pair get(final int i) { return holder.get(i); }
	 */

	public void put(final PhoneManager pm, Pair p) {
		if (pm.majorClasses.containsKey(p.getFeature()) && (p.getQuality().equals("-") || p.getQuality().equals("~"))) {
			for (final int f : pm.majorClasses.get(p.getFeature()))
				put(pm, f, "0");
		} else {
			final int im = pm.inMajorClass(p.getFeature());
			if (im != -1 && getQuality(im).equals("~")) {
				p = new Pair(pm, p.getFeature(), "0");
			}
		}

		final int i = getIndexOf(p.getFeature());

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

	public void put(final PhoneManager pm, final int f, final String q) {
		put(pm, new Pair(pm, f, q));
	}

	public void putAll(final PhoneManager pm, final Matrix m) {
		for (final Pair p : m.holder) {
			if ((getQuality(p.getFeature()).equals("+") && p.getQuality().equals("-"))
					|| (getQuality(p.getFeature()).equals("-") && p.getQuality().equals("+")))
				put(pm, new Pair(pm, p.getFeature(), "~"));
			else if (getQuality(p.getFeature()).equals("~") || p.getQuality().equals("~"))
				put(pm, new Pair(pm, p.getFeature(), "~"));
			else
				put(pm, p);
		}
	}

	public Matrix transform(final PhoneManager pm, final Matrix matrix) {
		final Matrix new_features = new Matrix(pm, this);

		for (final Pair e : matrix) {
			new_features.put(pm, e.getFeature(), e.getQuality());
			final int im = pm.inMajorClass(e.getFeature());
			if (im != -1 && !e.getQuality().equals("0")) {
				new_features.put(pm, im, "+");
				for (final int f : pm.majorClasses.get(im)) {
					if (new_features.getQuality(f).equals("0"))
						new_features.put(pm, f, "-");
				}
			} else if (pm.majorClasses.containsKey(e.getFeature()) && e.getQuality().equals("+")) {
				for (final int f : pm.majorClasses.get(e.getFeature())) {
					if (new_features.getQuality(f).equals("0"))
						new_features.put(pm, f, "-");
				}
			} else if (pm.majorClasses.containsKey(e.getFeature()) && e.getQuality().equals("-")) {
				for (final int f : pm.majorClasses.get(e.getFeature())) {
					new_features.put(pm, f, "0");
				}
			}
		}

		return new_features;
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
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Matrix m = (Matrix) o;
		for (final Pair p : holder) {
			if (!m.getQuality(p.getFeature()).equals(p.getQuality()))
				return false;
		}
		for (final Pair p : m.holder) {
			if (!getQuality(p.getFeature()).equals(p.getQuality()))
				return false;
		}
		return true;
	}
}

class MatrixIterator implements Iterator<Pair> {
	private final List<Pair> holder;
	private int i;

	MatrixIterator(final List<Pair> holder) {
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