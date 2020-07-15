package main.phl;

public class Pair {
	private final int feature;
	private final int quality;

	public Pair(final int feature, final int quality) {
		this.feature = feature;
		this.quality = quality;
	}

	public int getFeature() {
		return this.feature;
	}

	public int getQuality() {
		return this.quality;
	}

	@Override
	public String toString() {
		return Hasher.deHash(quality) + "|" + Hasher.deHash(feature);
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
		final Pair p = (Pair) o;
		return feature == p.getFeature() && quality == p.getQuality();
	}
}