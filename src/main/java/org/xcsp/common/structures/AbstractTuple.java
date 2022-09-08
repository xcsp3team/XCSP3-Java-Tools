package org.xcsp.common.structures;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionSet;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
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

		public String toString() {
			return Utilities.join(values);
		}

	}

	public static class SmartTuple implements AbstractTuple {
		public Object[] values; // either Integer or Condition objects inside the array

		public SmartTuple(Object[] values) {
			this.values = values;
			for (int i = 0; i < values.length; i++) {
				// System.out.println("ddd " + values[i].getClass());
				if (values[i] instanceof Long)
					values[i] = ((Long) values[i]).intValue();
				else if (values[i] instanceof String) {
					String s = (String) (values[i]);
					Utilities.control(s.charAt(0) == '%', "Pb with s");

				}
			}
			Utilities.control(
					Stream.of(values)
							.allMatch(v -> v instanceof Integer || v instanceof ConditionVal || v instanceof ConditionSet || v instanceof ConditionVar),
					"Bad form for smart tuple " + Utilities.join(values));
		}

		@Override
		public boolean match(int[] t) {
			assert t.length == values.length;
			for (int i = 0; i < t.length; i++) {
				if (values[i] instanceof Integer) {
					int v = ((Integer) values[i]);
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

		public String toString() {
			return "(" + Stream.of(values).map(v -> v instanceof Integer && (Integer) v == Constants.STAR ? Constants.STAR_SYMBOL : v.toString())
					.collect(Collectors.joining(",")) + ")";
		}
	}
}