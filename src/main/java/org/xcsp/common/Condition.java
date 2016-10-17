package org.xcsp.common;

import org.xcsp.common.Types.TypeConditionOperator;
import org.xcsp.common.Interfaces.IVar;

/** The root class for denoting a condition, i.e., a pair (operator,operand) used in many XCSP3 constraints. */
public abstract class Condition {
	/** The operator of the condition */
	public TypeConditionOperator operator;

	public Condition(TypeConditionOperator operator) {
		this.operator = operator;
		Utilities.control(operator.isSet() == (this instanceof Condition.ConditionIntvl || this instanceof Condition.ConditionIntset), "Bad form");
	}

	/** The class denoting a condition where the operand is a variable. */
	public static class ConditionVar extends Condition {
		public IVar x;

		public ConditionVar(TypeConditionOperator operator, IVar x) {
			super(operator);
			this.x = x;
		}
	}

	/** The class denoting a condition where the operand is a value. */
	public static class ConditionVal extends Condition {
		public int k;

		public ConditionVal(TypeConditionOperator operator, int k) {
			super(operator);
			this.k = k;
		}
	}

	/** The class denoting a condition where the operand is an interval. */
	public static class ConditionIntvl extends Condition {
		public int min, max;

		public ConditionIntvl(TypeConditionOperator operator, int min, int max) {
			super(operator);
			this.min = min;
			this.max = max;
		}
	}

	/** The class denoting a condition where the operand is a set of integers. */
	public static class ConditionIntset extends Condition {
		public int[] t;

		public ConditionIntset(TypeConditionOperator operator, int[] t) {
			super(operator);
			this.t = t;
		}
	}
}