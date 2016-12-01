package org.xcsp.common;

import org.xcsp.common.Interfaces.IVar;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;

/** The root interface for denoting a condition, i.e., a pair (operator,operand) used in many XCSP3 constraints. */
public interface Condition {

	public static Condition buildFrom(Object operator, Object limit) {
		if (operator instanceof TypeConditionOperatorRel) {
			if (limit instanceof Number)
				return new ConditionVal((TypeConditionOperatorRel) operator, ((Number) limit).intValue());
			else
				return new ConditionVar((TypeConditionOperatorRel) operator, (IVar) limit);
		} else {
			if (limit instanceof Range) {
				Utilities.control(((Range) limit).step == 1, "Pb with range");
				return new ConditionIntvl((TypeConditionOperatorSet) operator, ((Range) limit).minIncluded, ((Range) limit).maxIncluded);
			} else
				return new ConditionIntset((TypeConditionOperatorSet) operator, ((int[]) limit));
		}
	}

	public abstract class ConditionRel implements Condition {
		/** The operator of the condition */
		public TypeConditionOperatorRel operator;

		public ConditionRel(TypeConditionOperatorRel operator) {
			this.operator = operator;
		}
	}

	/** The class denoting a condition where the operand is a variable. */
	public static class ConditionVar extends ConditionRel {
		public IVar x;

		public ConditionVar(TypeConditionOperatorRel operator, IVar x) {
			super(operator);
			this.x = x;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + x + ")";
		}
	}

	/** The class denoting a condition where the operand is a value. */
	public static class ConditionVal extends ConditionRel {
		public int k;

		public ConditionVal(TypeConditionOperatorRel operator, int k) {
			super(operator);
			this.k = k;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + k + ")";
		}
	}

	public abstract class ConditionSet implements Condition {
		/** The operator of the condition */
		public TypeConditionOperatorSet operator;

		public ConditionSet(TypeConditionOperatorSet operator) {
			this.operator = operator;
		}
	}

	/** The class denoting a condition where the operand is an interval. */
	public static class ConditionIntvl extends ConditionSet {
		public int min, max;

		public ConditionIntvl(TypeConditionOperatorSet operator, int min, int max) {
			super(operator);
			this.min = min;
			this.max = max;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + min + ".." + max + ")";
		}

	}

	/** The class denoting a condition where the operand is a set of integers. */
	public static class ConditionIntset extends ConditionSet {
		public int[] t;

		public ConditionIntset(TypeConditionOperatorSet operator, int[] t) {
			super(operator);
			this.t = t;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + ",{" + Utilities.join(t, ",") + "})";
		}
	}
}