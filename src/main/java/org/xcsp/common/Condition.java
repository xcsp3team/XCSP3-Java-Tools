package org.xcsp.common;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.xcsp.common.Types.TypeConditionOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.domains.Values.IntegerInterval;

/**
 * The root interface for denoting a condition, i.e., a pair (operator,operand) used in many XCSP3 constraints.
 */
public interface Condition {

	/**
	 * Returns an object instance of a class implementing {@code Condition}, built from the specified arguments.
	 * 
	 * @param operator
	 *            a relational operator {@code TypeConditionOperatorRel}, a set operator {@code TypeConditionOperatorSet} or a more general object
	 *            {@code TypeConditionOperator}
	 * @param limit
	 *            an integer (object {@code Number}), a variable (object {@code IVar}), a range (object {@code Range}) or a 1-dimensional array of
	 *            {@code int}
	 * @return an object instance of a class implementing {@code Condition}, built from the specified arguments
	 */
	public static Condition buildFrom(Object operator, Object limit) {
		if (operator instanceof TypeConditionOperatorRel) {
			TypeConditionOperatorRel op = (TypeConditionOperatorRel) operator;
			return limit instanceof Number ? new ConditionVal(op, ((Number) limit).longValue()) : new ConditionVar(op, (IVar) limit);
		} else if (operator instanceof TypeConditionOperatorSet) {
			TypeConditionOperatorSet op = (TypeConditionOperatorSet) operator;
			return limit instanceof Range ? new ConditionIntvl(op, ((Range) limit)) : new ConditionIntset(op, ((int[]) limit));
		} else {
			Utilities.control(operator instanceof TypeConditionOperator, " Bad Argument");
			TypeConditionOperator op = (TypeConditionOperator) operator;
			if (limit instanceof Long)
				return new ConditionVal(op.toRel(), (Long) limit);
			if (limit instanceof IVar)
				return new ConditionVar(op.toRel(), (IVar) limit);
			if (limit instanceof IntegerInterval)
				return new ConditionIntvl(op.toSet(), ((IntegerInterval) limit).inf, ((IntegerInterval) limit).sup);
			return new ConditionIntset(op.toSet(), LongStream.of((long[]) limit).mapToInt(l -> Utilities.safeLong2Int(l, true)).toArray());
		}
	}

	/**
	 * Returns the variable involved in the condition, if one is present, {@code null} otherwise.
	 * 
	 * @return the variable involved in the condition, if one is present, {@code null} otherwise
	 */
	default IVar involvedVar() {
		return null;
	}

	/**
	 * Represents a condition based on a relational operator.
	 */
	public abstract class ConditionRel implements Condition {
		/**
		 * The relational operator on which the condition is based.
		 */
		public TypeConditionOperatorRel operator;

		/**
		 * Constructs a condition based on a relational operator.
		 * 
		 * @param operator
		 *            a relational operator
		 */
		public ConditionRel(TypeConditionOperatorRel operator) {
			this.operator = operator;
		}
	}

	/**
	 * Represents a condition composed of a relational operator and a variable as operand.
	 */
	public static class ConditionVar extends ConditionRel {
		/**
		 * The variable that represents the operand of the condition.
		 */
		public IVar x;

		/**
		 * Constructs a condition composed of the specified relational operator and the specified variable as (right) operand
		 * 
		 * @param operator
		 *            a relational operator
		 * @param x
		 *            a variable
		 */
		public ConditionVar(TypeConditionOperatorRel operator, IVar x) {
			super(operator);
			this.x = x;
		}

		@Override
		public IVar involvedVar() {
			return x;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + x + ")";
		}
	}

	/**
	 * Represents a condition composed of a relational operator and a value (long integer) as (right) operand.
	 */
	public static class ConditionVal extends ConditionRel {
		/**
		 * The value that represents the operand of the condition.
		 */
		public long k;

		/**
		 * Constructs a condition composed of the specified relational operator and the specified value as (right) operand
		 * 
		 * @param operator
		 *            a relational operator
		 * @param k
		 *            an integer
		 */
		public ConditionVal(TypeConditionOperatorRel operator, long k) {
			super(operator);
			this.k = k;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + k + ")";
		}
	}

	/**
	 * Represents a condition based on a set operator.
	 */
	public abstract class ConditionSet implements Condition {
		/**
		 * The set operator on which the condition is based.
		 */
		public TypeConditionOperatorSet operator;

		/**
		 * Constructs a condition based on a set operator.
		 * 
		 * @param operator
		 *            a set operator
		 */
		public ConditionSet(TypeConditionOperatorSet operator) {
			this.operator = operator;
		}
	}

	/** The class denoting a condition where the operand is an interval. */

	/**
	 * Represents a condition composed of a set operator and an interval (defined by its two inclusive bounds) as (right) operand.
	 */
	public static class ConditionIntvl extends ConditionSet {
		/**
		 * The lower bound (inclusive) of the interval.
		 */
		public long min;

		/**
		 * The upper bound (inclusive) of the interval.
		 */
		public long max;

		/**
		 * Constructs a condition composed of a set operator and an interval (defined by its two inclusive bounds) as (right) operand
		 * 
		 * @param operator
		 *            a set operator
		 * @param min
		 *            the lower bound (inclusive) of the interval
		 * @param max
		 *            the upper bound (inclusive) of the interval
		 */
		public ConditionIntvl(TypeConditionOperatorSet operator, long min, long max) {
			super(operator);
			Utilities.control(min <= max, "The sepcified bouds are not valid.");
			this.min = min;
			this.max = max;
		}

		/**
		 * Constructs a condition composed of a set operator and an interval defined by a range.
		 * 
		 * @param operator
		 *            a set operator
		 * @param range
		 *            a range denoting an interval
		 */
		public ConditionIntvl(TypeConditionOperatorSet operator, Range range) {
			this(operator, range.startInclusive, range.endExclusive - 1);
			Utilities.control(range.step == 1, "Specified ranges must have a step equal to 1");
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + "," + min + ".." + max + ")";
		}

	}

	/**
	 * Represents a condition composed of a set operator and an array of values (int) as (right) operand.
	 */
	public static class ConditionIntset extends ConditionSet {
		/**
		 * The array of values, used as (right) operand.
		 */
		public int[] t;

		/**
		 * Constructs a condition composed of a set operator and an array of values (int) as (right) operand.
		 * 
		 * @param operator
		 *            a set operator
		 * @param t
		 *            the array of values, used as (right) operand
		 */
		public ConditionIntset(TypeConditionOperatorSet operator, int[] t) {
			super(operator);
			t = IntStream.of(t).sorted().distinct().toArray();
			Utilities.control(t.length > 0, "The sepcified array is not valid.");
			this.t = t;
		}

		@Override
		public String toString() {
			return "(" + operator.name().toLowerCase() + ",{" + Utilities.join(t, ",") + "})";
		}
	}
}