package org.xcsp.common.structures;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionSet;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Constants;
import org.xcsp.common.Utilities;

public interface AbstractTuple {

	boolean match(int[] tuple);

	public static class OrdinaryTuple implements AbstractTuple {
		public int[] values;

		public OrdinaryTuple(int[] values) {
			this.values = values;
		}

		@Override
		public boolean match(int[] t) {
			assert t.length == values.length;
			return IntStream.range(0, t.length).allMatch(i -> values[i] == Constants.STAR || t[i] == Constants.STAR || values[i] == t[i]);
		}

	}

	public static class SmartTuple implements AbstractTuple {
		public Object[] values; // either Integer or Condition objects inside the array

		public SmartTuple(Object[] values) {
			this.values = values;
			Utilities.control(Stream.of(values).allMatch(v -> v instanceof Integer || v instanceof ConditionVal || v instanceof ConditionSet),
					"Bad form for smart tuple");
		}

		@Override
		public boolean match(int[] t) {
			assert t.length == values.length;
			for (int i = 0; i < t.length; i++) {
				if (values[i] instanceof Integer) {
					int v = ((Integer) values[i]).intValue();
					if (v != Constants.STAR && t[i] != Constants.STAR && v != t[i])
						return false;
				} else {
					Condition condition = (Condition) values[i];
					if (condition instanceof ConditionVal) {
						if (((ConditionVal) condition).operator.isValidFor(t[i], ((ConditionVal) condition).k) == false)
							return false;
					} else if (condition instanceof ConditionIntvl) {
						if (((ConditionIntvl) condition).operator.isValidFor(t[i], ((ConditionIntvl) condition).min, ((ConditionIntvl) condition).max) == false)
							return false;
					} else if (condition instanceof ConditionIntset) {
						if (((ConditionIntset) condition).operator.isValidFor(t[i], ((ConditionIntset) condition).t) == false)
							return false;
					}
				}
			}
			return true;
		}
	}
}