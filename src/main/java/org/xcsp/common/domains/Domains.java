/*
 * Copyright (c) 2016 XCSP3 Team (contact@xcsp.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xcsp.common.domains;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.common.domains.Values.IntegerValue;
import org.xcsp.common.domains.Values.RealInterval;
import org.xcsp.common.domains.Values.SimpleValue;

/**
 * In this class, we find intern classes for managing all types of domains.
 * 
 * @author Christophe Lecoutre
 */
public class Domains {

	/** The root interface to tag domain objects. */
	public static interface IDom {
	}

	/** A class for representing basic domains, i.e. domains for integer, symbolic, real and stochastic variables. */
	public static class DomBasic implements IDom {

		/** Returns the basic domain obtained by parsing the specified string, according to the value of the specified type. */
		public static DomBasic parse(String s, TypeVar type) {
			return type == TypeVar.integer ? new Dom(s)
					: type == TypeVar.symbolic ? new DomSymbolic(s) : type == TypeVar.real ? new DomReal(s) : DomStochastic.parse(s, type);
		}

		/**
		 * The values of the domain: for an integer domain, values are IntegerEntity, for a symbolic domain, values are String, and for a float
		 * domain, values are RealInterval.
		 */
		public final Object[] values;

		/** Builds a basic domain, with the specified values. */
		protected DomBasic(Object[] values) {
			this.values = values;
		}

		@Override
		public String toString() {
			return Utilities.join(values);
		}
	}

	/** The class for representing the domain of an integer variable. */
	public static final class Dom extends DomBasic {

		public static String compactFormOf(int[] values) {
			StringBuilder sb = new StringBuilder();
			int prevVal = values[0], startInterval = prevVal;
			for (int i = 1; i < values.length; i++) {
				int currVal = values[i];
				if (currVal != prevVal + 1) {
					sb.append(prevVal == startInterval ? prevVal : startInterval + (prevVal == startInterval + 1 ? " " : "..") + prevVal).append(" ");
					// when only two values, no need for an interval
					startInterval = currVal;
				}
				prevVal = currVal;
			}
			return sb.append(prevVal == startInterval ? prevVal : startInterval + (prevVal == startInterval + 1 ? " " : "..") + prevVal).toString();
		}

		/**
		 * Builds an integer domain, with the integer values (entities that are either integers or integer intervals) obtained by parsing the
		 * specified string.
		 */
		protected Dom(String seq) {
			super(IntegerEntity.parseSeq(seq)); // must be already sorted.
		}

		/** Builds an integer domain, with the specified integer values. */
		public Dom(int[] values) {
			super(IntStream.of(values).mapToObj(v -> new IntegerValue(v)).toArray(IntegerEntity[]::new));
		}

		/** Builds an integer domain, with the specified integer interval. */
		public Dom(int min, int max) {
			super(new IntegerEntity[] { new IntegerInterval(min, max) });
		}

		/** Returns the first (smallest) value of the domain. It may be VAL_M_INFINITY for -infinity. */
		public long firstValue() {
			return ((IntegerEntity) values[0]).smallest();
		}

		/** Returns the last (greatest) value of the domain. It may be VAL_P_INFINITY for +infinity. */
		public long lastValue() {
			return ((IntegerEntity) values[values.length - 1]).greatest();
		}

		/** Returns true iff the domain contains the specified value. */
		public boolean contains(long v) {
			for (int left = 0, right = values.length - 1; left <= right;) {
				int center = (left + right) / 2;
				int res = ((IntegerEntity) values[center]).compareContains(v);
				if (res == 0)
					return true;
				if (res == -1)
					left = center + 1;
				else
					right = center - 1;
			}
			return false;
		}

		private Long nValues; // cache for lazy initialization

		/** Returns the number of values in the domain, if the domain is finite. Return -1 otherwise. */
		public long nValues() {
			return nValues != null ? nValues : (nValues = IntegerEntity.nValues((IntegerEntity[]) values));
		}

		/**
		 * Returns this object if the condition is evaluated to {@code true}, {@code null} otherwise.
		 * 
		 * @param condition
		 *            a Boolean expression
		 * @return this object if the condition is evaluated to {@code true}, {@code null} otherwise
		 */
		public Dom when(boolean condition) {
			return condition ? this : null;
		}
	}

	/** The class for representing the domain of a symbolic variable. */
	public static final class DomSymbolic extends DomBasic {

		/** Builds a symbolic domain, with the symbols obtained by parsing the specified string. */
		protected DomSymbolic(String seq) {
			super(Stream.of(seq.split("\\s+")).sorted().toArray(String[]::new));
		}

		/** Builds a symbolic domain, with the specified symbols. */
		public DomSymbolic(String[] values) {
			super(values);
		}

		/** Returns true iff the domain contains the specified value. */
		public boolean contains(String s) {
			return Arrays.binarySearch(values, s) >= 0;
		}
	}

	/** The class for representing the domain of a real variable. */
	public static class DomReal extends DomBasic {

		/** Builds a real domain, with the intervals obtained by parsing the specified string. */
		protected DomReal(String seq) {
			super(RealInterval.parseSeq(seq));
		}
	}

	/** The class for representing the domain of a stochastic variable. */
	public static final class DomStochastic extends DomBasic {
		/** Returns the stochastic domain obtained by parsing the specified string, according to the specified type. */
		public static DomStochastic parse(String s, TypeVar type) {
			String[] toks = s.split("\\s+");
			Object[] values = new Object[toks.length];
			SimpleValue[] probas = new SimpleValue[toks.length];
			for (int i = 0; i < toks.length; i++) {
				String[] t = toks[i].split(":");
				values[i] = type == TypeVar.symbolic_stochastic ? t[0] : IntegerEntity.parse(t[0]);
				probas[i] = SimpleValue.parse(t[1]);
			}
			return new DomStochastic(values, probas);
		}

		/**
		 * The probabilities associated with the values of the domain: probas[i] is the probability of values[i]. Probabilities can be given as
		 * rational, decimal, or integer values (only, 0 and 1 for integer).
		 */
		public final SimpleValue[] probas;

		/** Builds a stochastic domain, with the specified values and the specified probabilities. */
		protected DomStochastic(Object[] values, SimpleValue[] probas) {
			super(values);
			this.probas = probas;
			assert values.length == probas.length;
		}

		@Override
		public String toString() {
			return super.toString() + " Probas: " + Utilities.join(probas);
		}
	}

	/** The interface to tag complex domains, i.e. domains for set or graph variables. */
	public static interface DomComplex extends IDom {
	}

	/** The class for representing the domain of a set variable. */
	public static final class DomSet implements DomComplex {
		/** Returns the set domain obtained by parsing the specified strings, according to the specified type. */
		public static DomSet parse(String req, String pos, TypeVar type) {
			return type == TypeVar.set ? new DomSet(IntegerEntity.parseSeq(req), IntegerEntity.parseSeq(pos))
					: new DomSet(req.split("\\s+"), pos.split("\\s+"));
		}

		/**
		 * The required and possible values. For an integer set domain, values are IntegerEntity. For a symbolic set domain, values are String.
		 */
		public final Object[] required, possible;

		/** Builds a set domain, with the specified required and possible values. */
		protected DomSet(Object[] required, Object[] possible) {
			this.required = required;
			this.possible = possible;
		}

		@Override
		public String toString() {
			return "[{" + Utilities.join(required) + "},{" + Utilities.join(possible) + "}]";
		}
	}

	/** The class for representing the domain of a graph variable. */
	public static final class DomGraph implements DomComplex {
		/** Returns the graph domain obtained by parsing the specified strings, according to the specified type. */
		public static DomGraph parse(String reqV, String reqE, String posV, String posE, TypeVar type) {
			String[] rV = reqV.split("\\s+"), pV = posV.split("\\s+");
			String[][] rE = Stream.of(reqE.split(Constants.DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*")).toArray(String[][]::new);
			String[][] pE = Stream.of(posE.split(Constants.DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*")).toArray(String[][]::new);
			return new DomGraph(rV, pV, rE, pE);
		}

		/** The required and possible nodes (vertices). */
		public final String[] requiredV, possibleV;

		/** The required and possible edges or arcs. */
		public final String[][] requiredE, possibleE;

		/** Builds a graph domain, with the specified required and possible values (nodes and edges/arcs). */
		protected DomGraph(String[] requiredV, String[] possibleV, String[][] requiredE, String[][] possibleE) {
			this.requiredV = requiredV;
			this.possibleV = possibleV;
			this.requiredE = requiredE;
			this.possibleE = possibleE;
		}

		@Override
		public String toString() {
			return "[{" + Utilities.join(requiredV) + "-" + Utilities.join(requiredE) + "},{" + Utilities.join(possibleV) + "-" + Utilities.join(possibleE)
					+ "}]";
		}
	}
}
