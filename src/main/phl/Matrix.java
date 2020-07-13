package main.phl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Matrix implements Iterable<Pair> {
	private final List<Pair> holder;

	public Matrix() {
		holder = new ArrayList<>();
	}

	public Matrix(final Matrix m) {
		holder = new ArrayList<>();
		for (final Pair p : m.holder) {
			holder.add(new Pair(p.getFeature(), p.getQuality()));
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

	public int getQuality(final int key) {
		final int i = getIndexOf(key);
		if (i >= 0)
			return holder.get(i).getQuality();
		return Hasher.ZERO;
	}

	public void put(final PhoneManager pm, Pair p) {
		if (pm.getMajorClasses().containsKey(p.getFeature())
				&& (p.getQuality() == Hasher.FALSE || p.getQuality() == Hasher.ANY)) {
			for (final int f : pm.getMajorClasses().get(p.getFeature()))
				put(pm, f, Hasher.ZERO);
		} else {
			final int im = pm.inMajorClass(p.getFeature());
			if (im != -1 && getQuality(im) == Hasher.ANY) {
				p = new Pair(p.getFeature(), Hasher.ZERO);
			}
		}

		final int i = getIndexOf(p.getFeature());

		if (p.getQuality() == Hasher.ZERO && i >= 0) {
			holder.remove(i);
			return;
		} else if (p.getQuality() == Hasher.ZERO) {
			return;
		}

		if (i >= 0)
			holder.set(i, p);
		else
			holder.add(p);
	}

	public void put(final PhoneManager pm, final int f, final int q) {
		put(pm, new Pair(f, q));
	}

	public void putAll(final PhoneManager pm, final Matrix m) {
		for (final Pair p : m.holder) {
			if ((getQuality(p.getFeature()) == Hasher.TRUE && p.getQuality() == Hasher.FALSE)
					|| (getQuality(p.getFeature()) == Hasher.FALSE && p.getQuality() == Hasher.TRUE))
				put(pm, new Pair(p.getFeature(), Hasher.ANY));
			else if (getQuality(p.getFeature()) == Hasher.ANY || p.getQuality() == Hasher.ANY)
				put(pm, new Pair(p.getFeature(), Hasher.ANY));
			else
				put(pm, p);
		}
	}

	public Matrix transform(final PhoneManager pm, final Matrix matrix) {
		final Matrix newFeatures = new Matrix(this);

		for (final Pair e : matrix) {
			newFeatures.put(pm, e.getFeature(), e.getQuality());
			final int im = pm.inMajorClass(e.getFeature());
			if (im != -1 && e.getQuality() != Hasher.ZERO) {
				newFeatures.put(pm, im, Hasher.TRUE);
				for (final int f : pm.getMajorClasses().get(im)) {
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(pm, f, Hasher.FALSE);
				}
			} else if (pm.getMajorClasses().containsKey(e.getFeature()) && e.getQuality() == Hasher.TRUE) {
				for (final int f : pm.getMajorClasses().get(e.getFeature())) {
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(pm, f, Hasher.FALSE);
				}
			} else if (pm.getMajorClasses().containsKey(e.getFeature()) && e.getQuality() == Hasher.FALSE) {
				for (final int f : pm.getMajorClasses().get(e.getFeature())) {
					newFeatures.put(pm, f, Hasher.ZERO);
				}
			}
		}

		return newFeatures;
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
			if (m.getQuality(p.getFeature()) != p.getQuality())
				return false;
		}
		for (final Pair p : m.holder) {
			if (getQuality(p.getFeature()) != p.getQuality())
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