package main.phl;

import java.util.List;

public class SecondaryArticulation {
	private final String segment;
	private final Matrix matrix;
	private final List<Matrix> requirements;
	private final List<PhoneLoader.Secondary> restrictions;

	public SecondaryArticulation(final String segment, final Matrix matrix, final List<PhoneLoader.Secondary> restrictions,
			final List<Matrix> requirements) {
		this.matrix = matrix;
		this.restrictions = restrictions;
		this.segment = segment;
		this.requirements = requirements;
	}

	public SecondaryArticulation(final String segment, final int feature, final int value,
			final List<PhoneLoader.Secondary> restrictions, final List<Matrix> requirements) {
		this(segment, new Matrix(new Pair(feature, value)), restrictions, requirements);
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public boolean canApply(final Matrix matrix, final List<PhoneLoader.Secondary> applied) {
		for (int i = 0; i < restrictions.size(); i++)
			if (applied.contains(restrictions.get(i)))
				return false;

		if (requirements.isEmpty())
			return true;

		for (int i = 0; i < requirements.size(); i++) {
			boolean flag = true;
			final Matrix r = requirements.get(i);
			for (final Pair feature : r) {
				if (matrix.getQuality(feature.getFeature()) != feature.getQuality()) {
					flag = false;
					break;
				}
			}
			if (flag)
				return true;
		}

		return false;
	}

	public String getSegment() {
		return segment;
	}
}
