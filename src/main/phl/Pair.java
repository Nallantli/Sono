package main.phl;

import java.io.Serializable;

public class Pair implements Serializable {
	private static final long serialVersionUID = 1L;

	private final int feature;
	private final String quality;
	private final PhoneManager pm;

	public Pair(final PhoneManager pm, final int feature, final String quality) {
		this.pm = pm;
		this.feature = feature;
		this.quality = quality;
	}

	public int getFeature() {
		return this.feature;
	}

	public String getQuality() {
		return this.quality;
	}

	@Override
	public String toString() {
		return quality + "|" + pm.hasher.deHash(feature);
	}

	@Override
	public int hashCode() {
		return (quality + feature).hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Pair p = (Pair) o;
		return feature == p.getFeature() && quality.equals(p.getQuality());
	}
}