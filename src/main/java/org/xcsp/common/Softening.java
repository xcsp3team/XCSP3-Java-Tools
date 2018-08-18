package org.xcsp.common;

import org.xcsp.common.Types.TypeMeasure;

/** The root class used for representing softening. */
public abstract class Softening {

	/** A pair (operator,operand) for a cost-integrated soft constraint, or null for a cost function. */
	public final Condition cost;

	public boolean isCostFunction() {
		return cost == null;
	}

	public Softening(Condition cost) {
		this.cost = cost;
	}

	public Softening() {
		this(null);
	}

	@Override
	public String toString() {
		return "Softening (" + this.getClass().getSimpleName() + ")" + " " + (cost == null ? "" : "cost:" + cost);
	}

	/** The class used for representing softening of simple soft constraints. */
	public static final class SofteningSimple extends Softening {

		/** The cost to be considered when the underlying constraint is violated. */
		public final int violationCost;

		public SofteningSimple(Condition cost, int violationCost) {
			super(cost);
			this.violationCost = violationCost;
			Utilities.control(violationCost > 0, "Pb with violation cost " + violationCost);
		}

		public SofteningSimple(int violationCost) {
			this(null, violationCost);
		}

		@Override
		public String toString() {
			return super.toString() + " violationCost=" + violationCost;
		}
	}

	/** The class used for representing softening of intensional constraints (that are not simple soft constraints). */
	public static final class SofteningIntension extends Softening {

		public SofteningIntension(Condition cost) {
			super(cost);
		}

		public SofteningIntension() {
			this(null);
		}
	}

	/** The class used for representing softening of extensional constraints (that are not simple soft constraints). */
	public static final class SofteningExtension extends Softening {
		/** The default cost for all tuples not explicitly listed. -1 if not useful (because all tuples are explicitly listed). */
		public final int defaultCost;

		public SofteningExtension(Condition cost, int defaultCost) {
			super(cost);
			this.defaultCost = defaultCost;
			Utilities.control(defaultCost >= -1, "Pb with default cost " + defaultCost);
		}

		public SofteningExtension(int defaultCost) {
			this(null, defaultCost);
		}

		@Override
		public String toString() {
			return super.toString() + " defaultCost=" + defaultCost;
		}
	}

	/** The class used for representing softening of other constraints (global constraints and some meta-constraints). */
	public static final class SofteningGlobal extends Softening {
		public final TypeMeasure type;

		public final String parameters;

		public SofteningGlobal(Condition cost, TypeMeasure type, String parameters) {
			super(cost);
			this.type = type;
			this.parameters = parameters;
		}

		public SofteningGlobal(Condition cost, TypeMeasure type) {
			this(cost, type, null);
		}

		public SofteningGlobal(TypeMeasure type, String parameters) {
			this(null, type, parameters);
		}

		public SofteningGlobal(TypeMeasure type) {
			this(type, null);
		}

		@Override
		public String toString() {
			return super.toString() + " type=" + type + (parameters != null ? " parameters=" + parameters : "");
		}
	}
}
