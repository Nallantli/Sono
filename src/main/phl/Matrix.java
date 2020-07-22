package main.phl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Matrix implements Iterable<Pair> {
	private final PhoneManager pm;
	private final int[] rawData;

	public Matrix(final PhoneManager pm) {
		this.rawData = new int[pm.getFeatureNames().size()];
		this.pm = pm;
	}

	public Matrix(final PhoneManager pm, final Matrix m) {
		this.pm = pm;
		this.rawData = new int[pm.getFeatureNames().size()];
		for (final Pair p : m)
			put(p.getFeature(), p.getQuality());
	}

	public Matrix(final PhoneManager pm, final Pair... entries) {
		this.pm = pm;
		this.rawData = new int[pm.getFeatureNames().size()];
		for (final Pair p : entries)
			put(p.getFeature(), p.getQuality());
	}

	public int getQuality(final int f) {
		return rawData[pm.getFeatureNames().indexOf(f)];
	}

	public void put(final int f, final int q) {
		rawData[pm.getFeatureNames().indexOf(f)] = q;

		if (pm.getMajorClasses().containsKey(f) && (q == Hasher.FALSE || q == Hasher.ANY)) {
			for (final int fm : pm.getMajorClasses().get(f))
				put(fm, Hasher.ZERO);
		} else {
			final int im = pm.inMajorClass(f);
			if (im != -1 && getQuality(im) == Hasher.ANY) {
				rawData[pm.getFeatureNames().indexOf(f)] = Hasher.ZERO;
			}
		}
	}

	public void putAll(final Matrix m) {
		for (final Pair p : m) {
			if ((getQuality(p.getFeature()) == Hasher.TRUE && p.getQuality() == Hasher.FALSE)
					|| (getQuality(p.getFeature()) == Hasher.FALSE && p.getQuality() == Hasher.TRUE))
				put(p.getFeature(), Hasher.ANY);
			else if (getQuality(p.getFeature()) == Hasher.ANY || p.getQuality() == Hasher.ANY)
				put(p.getFeature(), Hasher.ANY);
			else
				put(p.getFeature(), p.getQuality());
		}
	}

	public Matrix transform(final Matrix matrix) {
		final Matrix newFeatures = new Matrix(pm, this);

		for (final Pair e : matrix) {
			newFeatures.put(e.getFeature(), e.getQuality());
			final int im = pm.inMajorClass(e.getFeature());
			if (im != -1 && e.getQuality() != Hasher.ZERO) {
				newFeatures.put(im, Hasher.TRUE);
				for (final int f : pm.getMajorClasses().get(im))
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(f, Hasher.FALSE);
			} else if (pm.getMajorClasses().containsKey(e.getFeature()) && e.getQuality() == Hasher.TRUE) {
				for (final int f : pm.getMajorClasses().get(e.getFeature()))
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(f, Hasher.FALSE);
			} else if (pm.getMajorClasses().containsKey(e.getFeature()) && e.getQuality() == Hasher.FALSE) {
				for (final int f : pm.getMajorClasses().get(e.getFeature()))
					newFeatures.put(f, Hasher.ZERO);
			}
		}

		return newFeatures;
	}

	private List<Pair> getList() {
		final List<Pair> list = new ArrayList<>();
		for (final int f : pm.getFeatureNames()) {
			if (getQuality(f) != Hasher.ZERO)
				list.add(new Pair(f, getQuality(f)));
		}
		return list;
	}

	public int size() {
		return getList().size();
	}

	@Override
	public String toString() {
		return getList().toString();
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public Iterator<Pair> iterator() {
		return new MatrixIterator(getList());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(rawData);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Matrix m = (Matrix) o;
		for (final int f : pm.getFeatureNames())
			if (m.getQuality(f) != getQuality(f))
				return false;

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