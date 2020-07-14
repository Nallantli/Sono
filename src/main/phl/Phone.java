package main.phl;

import java.util.HashMap;
import java.util.Map;

public class Phone implements Comparable<Phone> {
	private final PhoneManager pm;

	private final String segment;
	private final Matrix features;

	private final Map<Matrix, Phone> transformationCache;

	public Phone(final PhoneManager pm, final String segment, final Matrix features, final boolean validate) {
		this.pm = pm;
		this.segment = segment;
		this.features = features;
		this.transformationCache = new HashMap<>();

		pm.add(this, validate);
	}

	public boolean hasFeatures(final Matrix map) {
		for (final Pair e : map) {
			if (getFeatureQuality(e.getFeature()) != e.getQuality() && e.getQuality() != Hasher.ANY) {
				return false;
			}
		}
		return true;
	}

	public int getFeatureQuality(final int feature) {
		return features.getQuality(feature);
	}

	public Matrix getMatrix() {
		final Matrix map = new Matrix();
		for (final Pair p : features)
			if (p.getQuality() != Hasher.ZERO)
				map.put(pm, p);
		return map;
	}

	private int[] getQualityArray(final PhoneManager pm) {
		final int[] values = new int[pm.getFeatureNames().size()];

		int i = 0;
		for (final int v : pm.getFeatureNames())
			values[i++] = getFeatureQuality(v);

		return values;
	}

	public String getDataString(final String split) {
		final StringBuilder s = new StringBuilder(segment);
		final int[] values = getQualityArray(pm);
		for (int i = 0; i < values.length; i++) {
			s.append(split);
			s.append(Hasher.deHash(values[i]));
		}
		return s.toString();
	}

	public Phone transform(final Matrix matrix, final boolean search) {
		if (transformationCache.containsKey(matrix))
			return transformationCache.get(matrix);

		final Matrix newFeatures = getMatrix();

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

		if (!pm.contains(newFeatures)) {
			if (search) {
				final Phone fuzzy = pm.validate(pm.fuzzySearch(newFeatures));
				if (fuzzy != null)
					return fuzzy;
				else
					return pm.validate(newFeatures);
			} else {
				return pm.validate(newFeatures);
			}
		}
		final Phone ret = pm.validate(newFeatures);
		transformationCache.put(matrix, ret);
		return ret;
	}

	@Override
	public String toString() {
		return segment;
	}

	@Override
	public int hashCode() {
		return segment.hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Phone p = (Phone) o;

		return p.getMatrix().equals(getMatrix());
	}

	public String getSegment() {
		return segment;
	}

	@Override
	public int compareTo(final Phone o) {
		return segment.compareTo(o.segment);
	}
}