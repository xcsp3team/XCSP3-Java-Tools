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
package org.xcsp.parser.entries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.IDom;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.parser.XParser.TypePrimitive;
import org.xcsp.parser.entries.ParsingEntry.VEntry;

/**
 * In this class, we find intern classes for managing variables and arrays of variables.
 * 
 * @author Christophe Lecoutre
 */
public class XVariables {

	public static final String OTHERS = "others";

	/** The class used to represent variables. */
	public static abstract class XVar extends VEntry implements IVar {

		/** Builds a variable with the specified id, type and domain. */
		public static final XVar build(String id, TypeVar type, IDom dom) {
			switch (type) {
			case integer:
				return new XVarInteger(id, type, dom);
			case symbolic:
				return new XVarSymbolic(id, type, dom);
			case stochastic:
				return new XVarStochastic(id, type, dom);
			case real:
				return new XVarReal(id, type, dom);
			case set:
				return new XVarSet(id, type, dom);
			default:
				throw new RuntimeException("Unimplemented case ");
			}
		}

		/** Builds a variable from an array with the specified id (combined with the specified indexes), type and domain. */
		public static final XVar build(String idArray, TypeVar type, IDom dom, int[] indexes) {
			return build(idArray + "[" + Utilities.join(indexes, "][") + "]", type, dom);
		}

		/** The domain of the variable. It is null if the variable is qualitative. */
		public final IDom dom;

		/** The degree of the variable. This is automatically computed after all constraints have been parsed. */
		public int degree;

		/** Builds a variable with the specified id, type and domain. */
		protected XVar(String id, TypeVar type, IDom dom) {
			super(id, type);
			this.dom = dom;
		}

		// /** Builds a variable from an array with the specified id (combined with the specified indexes), type and domain. */
		// protected VVar(String idArray, TypeVar type, DDom dom, int[] indexes) {
		// this(idArray + "[" + XUtility.join(indexes, "][") + "]", type, dom);
		// }

		@Override
		public String id() {
			return id;
		}

		@Override
		public String toString() {
			return id; // + " :" + type + " of " + dom;
		}
	}

	/** The following classes are introduced, only for being able to have types for variables in the parser interface */
	public static final class XVarInteger extends XVar implements Var {

		/**
		 * Returns the size of the Cartesian product for the domains of the specified variables. Importantly, if this value does not fit within a
		 * {@code long}, -1 is returned.
		 */
		public static long domainCartesianProductSize(XVarInteger[] scp) {
			long[] domSizes = Stream.of(scp).mapToLong(x -> IntegerEntity.nValues((IntegerEntity[]) ((Dom) x.dom).values)).toArray();
			if (LongStream.of(domSizes).anyMatch(l -> l == -1L))
				return -1L;
			long cnt = 1;
			try {
				for (long size : domSizes)
					cnt = Math.multiplyExact(cnt, size);
			} catch (ArithmeticException e) {
				return -1L;
			}
			return cnt;
		}

		public TypePrimitive whichPrimitive() {
			return TypePrimitive.whichPrimitiveFor(firstValue(), lastValue());
		}

		/** Builds an integer variable with the specified id, type and domain. */
		protected XVarInteger(String id, TypeVar type, IDom dom) {
			super(id, type, dom);
		}

		public long firstValue() {
			return ((Dom) dom).firstValue();
		}

		public long lastValue() {
			return ((Dom) dom).lastValue();
		}

		public boolean isZeroOne() {
			return firstValue() == 0 && lastValue() == 1;
		}
	}

	public static final class XVarSymbolic extends XVar implements VarSymbolic {
		/** Builds a symbolic variable with the specified id, type and domain. */
		protected XVarSymbolic(String id, TypeVar type, IDom dom) {
			super(id, type, dom);
		}
	}

	public static final class XVarStochastic extends XVar {
		/** Builds a stochastic variable with the specified id, type and domain. */
		protected XVarStochastic(String id, TypeVar type, IDom dom) {
			super(id, type, dom);
		}
	}

	public static final class XVarReal extends XVar {
		/** Builds a real variable with the specified id, type and domain. */
		protected XVarReal(String id, TypeVar type, IDom dom) {
			super(id, type, dom);
		}
	}

	public static final class XVarSet extends XVar {
		/** Builds a set variable with the specified id, type and domain. */
		protected XVarSet(String id, TypeVar type, IDom dom) {
			super(id, type, dom);
		}
	}

	/** The class used to represent arrays of variables. */
	public static final class XArray extends VEntry {
		/** The size of the array, as defined in XCSP3. */
		public final int[] size;

		/**
		 * The flat (one-dimensional) array composed of all variables contained in the (multi-dimensional) array. This way, we can easily deal with
		 * arrays of any dimensions.
		 */
		public final XVar[] vars;

		/** Builds an array of variables with the specified id, type and size. */
		public XArray(String id, TypeVar type, int[] size) {
			super(id, type);
			this.size = size;
			this.vars = new XVar[Arrays.stream(size).reduce(1, (s, t) -> s * t)];
		}

		/** Builds a variable with the specified domain for each unoccupied cell of the flat array. */
		private void buildVarsWith(IDom dom) {
			int[] indexes = new int[size.length];
			for (int i = 0; i < vars.length; i++) {
				if (vars[i] == null)
					vars[i] = XVar.build(id, type, dom, indexes);
				for (int j = indexes.length - 1; j >= 0; j--)
					if (++indexes[j] == size[j])
						indexes[j] = 0;
					else
						break;
			}
		}

		/**
		 * Builds an array of variables with the specified id, type and size. All variables are directly defined with the specified domain.
		 */
		public XArray(String id, TypeVar type, int[] sizes, IDom dom) {
			this(id, type, sizes);
			buildVarsWith(dom);
		}

		/** Transforms a flat index into a multi-dimensional index. */
		protected int[] indexesFor(int flatIndex) {
			int[] t = new int[size.length];
			for (int i = t.length - 1; i > 0; i--) {
				t[i] = flatIndex % size[i];
				flatIndex = flatIndex / size[i];
			}
			t[0] = flatIndex;
			return t;
		}

		/** Transforms a multi-dimensional index into a flat index. */
		private int flatIndexFor(int... indexes) {
			int sum = 0;
			for (int i = indexes.length - 1, nb = 1; i >= 0; i--) {
				sum += indexes[i] * nb;
				nb *= size[i];
			}
			return sum;
		}

		/** Returns the variable at the position given by the multi-dimensional index. */
		public XVar varAt(int... indexes) {
			return vars[flatIndexFor(indexes)];
		}

		/**
		 * Returns the domain of the variable at the position given by the multi-dimensional index. However, if this variable does not exist or if its
		 * degree is 0, <code>null</code> is returned.
		 */
		public Dom domAt(int... indexes) {
			XVar x = vars[flatIndexFor(indexes)];
			return x == null || x.degree == 0 ? null : (Dom) x.dom;
		}

		/**
		 * Builds an array of IntegerEnity objects for representing the ranges of indexes that are computed with respect to the specified compact
		 * form.
		 */
		public IntegerEntity[] buildIndexRanges(String compactForm) {
			IntegerEntity[] t = new IntegerEntity[size.length];
			String suffix = compactForm.substring(compactForm.indexOf("["));
			for (int i = 0; i < t.length; i++) {
				int pos = suffix.indexOf("]");
				String tok = suffix.substring(1, pos);
				t[i] = tok.length() == 0 ? new IntegerInterval(0, size[i] - 1) : IntegerEntity.parse(tok);
				suffix = suffix.substring(pos + 1);
			}
			return t;
		}

		/** Computes the next multi-dimensional index with respect to specified ranges. Returns false if non exists. */
		private boolean incrementIndexes(int[] indexes, IntegerEntity[] indexRanges) {
			int j = indexes.length - 1;
			for (; j >= 0; j--)
				if (indexRanges[j].isSingleton())
					continue;
				else if (++indexes[j] > ((IntegerInterval) indexRanges[j]).sup)
					indexes[j] = (int) ((IntegerInterval) indexRanges[j]).inf;
				else
					break;
			return j >= 0;
		}

		/** Any variable that matches one compact form present in the specified string is built with the specified domain. */
		public void setDom(String s, IDom dom) {
			if (s.trim().equals(OTHERS))
				buildVarsWith(dom);
			else
				for (String tok : s.split("\\s+")) {
					Utilities.control(tok.substring(0, tok.indexOf("[")).equals(id), "One value of attribute 'for' incorrect in array " + id);
					IntegerEntity[] indexRanges = buildIndexRanges(tok);
					int[] indexes = Stream.of(indexRanges).mapToInt(it -> (int) it.smallest()).toArray(); // first index
					do {
						int flatIndex = flatIndexFor(indexes);
						Utilities.control(vars[flatIndex] == null, "Problem with two domain definitions for the same variable");
						vars[flatIndex] = XVar.build(id, type, dom, indexes);
					} while (incrementIndexes(indexes, indexRanges));
				}
		}

		/**
		 * Returns the list of variables that match the specified compact form. For example, for x[1..3], the list will contain x[1] x[2] and x[3].
		 */
		public List<XVar> getVarsFor(String compactForm) {
			List<XVar> list = new ArrayList<>();
			IntegerEntity[] indexRanges = buildIndexRanges(compactForm);
			int[] indexes = Stream.of(indexRanges).mapToInt(it -> (int) it.smallest()).toArray(); // first index
			do {
				list.add(vars[flatIndexFor(indexes)]);
			} while (incrementIndexes(indexes, indexRanges));
			return list;
		}

		@Override
		public String toString() {
			return super.toString() + " [" + Utilities.join(size, "][") + "] " + Utilities.join(vars, " ");
		}
	}
}
