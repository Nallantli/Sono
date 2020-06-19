package main.phl;

import java.io.Serializable;

public class Pair implements Serializable {
	private static final long serialVersionUID = 1L;

	private final Phone.Feature feature;
	private final String quality;

	public Pair(final Phone.Feature feature, final String quality) {
		this.feature = feature;
		this.quality = quality;
	}

	public Phone.Feature getFeature() {
		return this.feature;
	}

	public String getQuality() {
		return this.quality;
	}

	@Override
	public String toString() {
		return quality + "|" + feature.toString();
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