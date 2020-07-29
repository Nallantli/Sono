package main.phl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Matrix implements Iterable<Feature> {
	private final PhoneManager pm;
	private final int[] rawData;

	public Matrix(final PhoneManager pm) {
		this.rawData = new int[pm.getFeatureNames().size()];
		this.pm = pm;
	}

	public Matrix(final PhoneManager pm, final Matrix m) {
		this.pm = pm;
		this.rawData = new int[pm.getFeatureNames().size()];
		for (final Feature p : m)
			put(p.getKey(), p.getQuality());
	}

	public Matrix(final PhoneManager pm, final Feature... entries) {
		this.pm = pm;
		this.rawData = new int[pm.getFeatureNames().size()];
		for (final Feature p : entries)
			put(p.getKey(), p.getQuality());
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
		for (final Feature p : m) {
			if ((getQuality(p.getKey()) == Hasher.TRUE && p.getQuality() == Hasher.FALSE)
					|| (getQuality(p.getKey()) == Hasher.FALSE && p.getQuality() == Hasher.TRUE))
				put(p.getKey(), Hasher.ANY);
			else if (getQuality(p.getKey()) == Hasher.ANY || p.getQuality() == Hasher.ANY)
				put(p.getKey(), Hasher.ANY);
			else
				put(p.getKey(), p.getQuality());
		}
	}

	public Matrix transform(final Matrix matrix) {
		final Matrix newFeatures = new Matrix(pm, this);

		for (final Feature e : matrix) {
			newFeatures.put(e.getKey(), e.getQuality());
			final int im = pm.inMajorClass(e.getKey());
			if (im != -1 && e.getQuality() != Hasher.ZERO) {
				newFeatures.put(im, Hasher.TRUE);
				for (final int f : pm.getMajorClasses().get(im))
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(f, Hasher.FALSE);
			} else if (pm.getMajorClasses().containsKey(e.getKey()) && e.getQuality() == Hasher.TRUE) {
				for (final int f : pm.getMajorClasses().get(e.getKey()))
					if (newFeatures.getQuality(f) == Hasher.ZERO)
						newFeatures.put(f, Hasher.FALSE);
			} else if (pm.getMajorClasses().containsKey(e.getKey()) && e.getQuality() == Hasher.FALSE) {
				for (final int f : pm.getMajorClasses().get(e.getKey()))
					newFeatures.put(f, Hasher.ZERO);
			}
		}

		return newFeatures;
	}

	private List<Feature> getList() {
		final List<Feature> list = new ArrayList<>();
		for (final int f : pm.getFeatureNames()) {
			if (getQuality(f) != Hasher.ZERO)
				list.add(new Feature(f, getQuality(f)));
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
	public Iterator<Feature> iterator() {
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

class MatrixIterator implements Iterator<Feature> {
	private final List<Feature> holder;
	private int i;

	MatrixIterator(final List<Feature> holder) {
		this.holder = holder;
		i = 0;
	}

	@Override
	public boolean hasNext() {
		return i < holder.size();
	}

	@Override
	public Feature next() {
		if (i >= holder.size())
			throw new NoSuchElementException("Matrix iterator out of bounds: " + i);
		return holder.get(i++);
	}
}