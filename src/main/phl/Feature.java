package main.phl;

public class Feature {
	private final int key;
	private final int quality;

	public Feature(final int key, final int quality) {
		this.key = key;
		this.quality = quality;
	}

	public int getKey() {
		return this.key;
	}

	public int getQuality() {
		return this.quality;
	}

	@Override
	public String toString() {
		return Hasher.deHash(quality) + "|" + Hasher.deHash(key);
	}

	@Override
	public int hashCode() {
		return 1;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (o == null)
			return false;
		if (o.getClass() != this.getClass())
			return false;
		final Feature p = (Feature) o;
		return key == p.getKey() && quality == p.getQuality();
	}
}