package main.phl;

public class Phone implements Comparable<Phone> {
	private final PhoneManager pm;

	private final String segment;
	private final Matrix features;

	public Phone(final PhoneManager pm, final String segment, final Matrix features, final boolean validate) {
		this.pm = pm;
		this.segment = segment;
		this.features = features;

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
		final int[] values = new int[pm.featureNames.size()];

		int i = 0;
		for (final int v : pm.featureNames)
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
		final Matrix new_features = getMatrix();

		for (final Pair e : matrix) {
			new_features.put(pm, e.getFeature(), e.getQuality());
			final int im = pm.inMajorClass(e.getFeature());
			if (im != -1 && e.getQuality() != Hasher.ZERO) {
				new_features.put(pm, im, Hasher.TRUE);
				for (final int f : pm.majorClasses.get(im)) {
					if (new_features.getQuality(f) == Hasher.ZERO)
						new_features.put(pm, f, Hasher.FALSE);
				}
			} else if (pm.majorClasses.containsKey(e.getFeature()) && e.getQuality() == Hasher.TRUE) {
				for (final int f : pm.majorClasses.get(e.getFeature())) {
					if (new_features.getQuality(f) == Hasher.ZERO)
						new_features.put(pm, f, Hasher.FALSE);
				}
			} else if (pm.majorClasses.containsKey(e.getFeature()) && e.getQuality() == Hasher.FALSE) {
				for (final int f : pm.majorClasses.get(e.getFeature())) {
					new_features.put(pm, f, Hasher.ZERO);
				}
			}
		}

		if (!pm.contains(new_features)) {
			if (search) {
				final Phone fuzzy = pm.validate(pm.fuzzySearch(new_features));
				if (fuzzy != null)
					return fuzzy;
				else
					return pm.validate(new_features);
			} else {
				return pm.validate(new_features);
			}
		}
		return pm.validate(new_features);
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