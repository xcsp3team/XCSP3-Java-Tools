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
package org.xcsp.common;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Christophe Lecoutre
 */
public class Types {

	public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
		try {
			return Enum.valueOf(enumType, name.toUpperCase()); // just for upper-case
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/** The enum type specifying the different types of frameworks. */
	public static enum TypeFramework {
		CSP, MAXCSP, COP, WCSP, FCSP, QCSP, QCSP_PLUS, QCOP, QCOP_PLUS, SCSP, SCOP, QSTR, TCSP, NCSP, NCOP, DisCSP, DisWCSP;
	}

	/**
	 * The enum type specifying the different types of constraints and meta-constraints. We use lower-case letters, so as to directly get
	 * the names of the elements (no need to define constants or make any transformations).
	 */
	public static enum TypeCtr {
		extension,
		intension,
		regular,
		grammar,
		mdd,
		allDifferent,
		allEqual,
		allDistant,
		ordered,
		lex,
		allIncomparable,
		sum,
		count,
		nValues,
		cardinality,
		balance,
		spread,
		deviation,
		sumCosts,
		stretch,
		noOverlap,
		cumulative,
		binPacking,
		knapsack,
		networkFlow,
		circuit,
		nCircuits,
		path,
		nPaths,
		tree,
		nTrees,
		arbo,
		nArbos,
		nCliques,
		clause,
		instantiation,
		allIntersecting,
		range,
		roots,
		partition,
		minimum,
		maximum,
		element,
		channel,
		permutation,
		precedence,
		and,
		or,
		not,
		iff,
		ifThen, // future meta-constraint to be taken into account
		ifThenElse, // future meta-constraint to be taken into account
		slide,
		seqbin,
		smart; // future constraint to be taken into account

		/** Returns true if the element has a sliding nature. */
		public boolean isSliding() {
			return this == slide || this == seqbin;
		}

		/** Returns true if the element has a if-based control structure. */
		public boolean isControl() {
			return this == ifThen || this == ifThenElse;
		}

		/** Returns true if the element has a logical nature. */
		public boolean isLogical() {
			return this == and || this == or || this == not || this == iff;
		}

		/** Returns true if the element corresponds to a meta-constraint. */
		public boolean isMeta() {
			return isSliding() || isLogical() || isControl();
		}
	}

	/**
	 * The enum type specifying the different types of child elements of constraints. We use lower-case letters, so as to directly get the
	 * names of the elements (except for FINAL that needs to be managed apart, because this is a keyword).
	 */
	public static enum TypeChild {
		list,
		set,
		mset,
		matrix,
		function,
		supports,
		conflicts,
		except,
		value,
		values,
		total,
		coeffs,
		condition,
		cost,
		operator,
		number,
		transitions,
		start,
		FINAL, // upper-cased because a keyword
		terminal,
		rules,
		index,
		mapping,
		occurs,
		rowOccurs,
		colOccurs,
		widths,
		patterns,
		origins,
		lengths,
		ends,
		heights,
		machines,
		conditions,
		sizes,
		weights,
		profits,
		limit,
		size,
		root,
		image,
		graph,
		row;
	}

	/**
	 * The enum type specifying the different types of attributes that may be encountered. We use lower-case letters, so as to directly get
	 * the names of the elements (except for CLASS, FOR and CASE that need to be managed apart, because they correspond to keywords).
	 */
	public static enum TypeAtt {
		format,
		type,
		id,
		CLASS, // upper-cased because a keyword
		note,
		as,
		size,
		violationMeasure,
		violationParameters,
		defaultCost,
		violationCost,
		cost,
		reifiedBy,
		hreifiedFrom,
		hreifiedTo,
		closed,
		FOR, // upper-cased because a keyword
		restriction,
		rank,
		startIndex,
		zeroIgnored,
		CASE, // upper-cased because a keyword
		order,
		circular,
		offset,
		collect,
		violable,
		lb,
		ub,
		combination;
		// unclean, // used for tuples of table constraints
		// starred; // used for tuples of table constraints

		/**
		 * Returns true iff the element has a (full or half) reification nature.
		 */
		public boolean isReifying() {
			return this == reifiedBy || this == hreifiedFrom || this == hreifiedTo;
		}

		/**
		 * Returns the constant that corresponds to the specified string (we need this method to manage the special constants FOR and CASE).
		 */
		public static TypeAtt valOf(String s) {
			return s.equals("class") ? CLASS : s.equals("for") ? FOR : s.equals("case") ? TypeAtt.CASE : valueOf(s);
		}
	}

	/**
	 * The enum type specifying the different flags that may be associated with some elements (e.g., constraints).
	 */
	public static enum TypeFlag {
		STARRED_TUPLES, UNCLEAN_TUPLES;
	}

	/** The enum type specifying the different types of reification. */
	public static enum TypeReification {
		FULL, HALF_FROM, HALF_TO;
	}

	/** The enum type specifying the different types of lifting operations (except matrix). */
	public static enum TypeLifting {
		LIST, SET, MSET;
	}

	/**
	 * The enum type specifying the different types of operators that can be used in conditions.
	 */
	public static enum TypeConditionOperator {
		LT, LE, GE, GT, NE, EQ, IN, NOTIN;

		/**
		 * Returns the corresponding specialized TypeConditionOperatorRel for this constant, or null if this constant is a set operator.
		 */
		public TypeConditionOperatorRel toRel() {
			return isSet() ? null
					: this == LT ? TypeConditionOperatorRel.LT
							: this == LE ? TypeConditionOperatorRel.LE
									: this == GE ? TypeConditionOperatorRel.GE
											: this == GT ? TypeConditionOperatorRel.GT : this == NE ? TypeConditionOperatorRel.NE : TypeConditionOperatorRel.EQ;
		}

		/**
		 * Returns the corresponding specialized TypeConditionOperatorSet for this constant, or null if this constant is a relational
		 * operator.
		 */
		public TypeConditionOperatorSet toSet() {
			return !isSet() ? null : this == IN ? TypeConditionOperatorSet.IN : TypeConditionOperatorSet.NOTIN;
		}

		/** Returns true iff this constant corresponds to a set operator. */
		public boolean isSet() {
			return this == IN || this == NOTIN;
		}
	}

	/**
	 * The enum type specifying the different types of relational operators that can be used in conditions.
	 */
	public static enum TypeConditionOperatorRel {
		LT, LE, GE, GT, NE, EQ;

		/**
		 * Returns the operator that is the reverse operator of this operator (no change for NE and EQ).
		 */
		public TypeConditionOperatorRel arithmeticInversion() {
			return this == LT ? GT : this == LE ? GE : this == GE ? LE : this == GT ? LT : this; // no change for NE and EQ
		}

		/**
		 * Returns true iff this operator evaluates to true when given the two specified operands.
		 */
		public boolean isValidFor(long v1, long v2) {
			return this == LT ? v1 < v2 : this == LE ? v1 <= v2 : this == GE ? v1 >= v2 : this == GT ? v1 > v2 : this == NE ? v1 != v2 : v1 == v2;
		}

	}

	/**
	 * The enum type specifying the different types of set operators that can be used in conditions.
	 */
	public static enum TypeConditionOperatorSet {
		IN, NOTIN;

		/**
		 * Returns true iff this operator evaluates to true when given the specified value and the two specified bounds of an interval.
		 */
		public boolean isValidFor(int v, long min, long max) {
			return this == IN ? min <= v && v <= max : v < min || v > max;
		}

		/**
		 * Returns true iff this operator evaluates to true when given the sepcified value and the specified set of values.
		 */
		public boolean isValidFor(int v, int[] t) {
			return (this == IN) == (IntStream.of(t).anyMatch(w -> v == w));
		}
	}

	/**
	 * The enum type specifying the different types of operators that can be used in elements <operator>.
	 */
	public static enum TypeOperator {
		LT, LE, GE, GT, SUBSET, SUBSEQ, SUPSEQ, SUPSET;

		public static TypeOperator valOf(String s) {
			return TypeOperator.valueOf(s.trim().toUpperCase());
		}

		/**
		 * Returns the corresponding specialized TypeOperatorRel for this constant, or null if this constant is a set operator,.
		 */
		public TypeOperatorRel toRel() {
			return isSet() ? null : this == LT ? TypeOperatorRel.LT : this == LE ? TypeOperatorRel.LE : this == GE ? TypeOperatorRel.GE : TypeOperatorRel.GT;
		}

		/** Returns true iff the constant corresponds to a set operator. */
		public boolean isSet() {
			return this == SUBSET || this == SUBSEQ || this == SUPSEQ || this == SUPSET;
		}

	}

	/**
	 * The different operators that can be used in elements <operator>, when a relational operator is expected.
	 */
	public static enum TypeOperatorRel {
		LT, LE, GE, GT;

		/**
		 * Returns true iff this operator evaluates to true when given the two specified operands.
		 */
		public boolean isValidFor(long v1, long v2) {
			return this == LT ? v1 < v2 : this == LE ? v1 <= v2 : this == GE ? v1 >= v2 : v1 > v2;
		}
	}

	/**
	 * The different operators that can be used in elements <operator>, when a set operator is expected.
	 */
	public static enum TypeOperatorSet {
		SUBSET, SUBSEQ, SUPSEQ, SUPSET;
	}

	/**
	 * The enum type specifying the different types of operators that can be used in elements <operator>.
	 */
	public static enum TypeArithmeticOperator {
		ADD, SUB, MUL, DIV, MOD, POW, DIST;
	}

	/**
	 * The enum type specifying the different types of unary arithmetic operators.
	 */
	public static enum TypeUnaryArithmeticOperator {
		ABS, NEG, SQR, NOT;
	}

	/**
	 * The enum type specifying the different types of binary logic operators.
	 */
	public static enum TypeLogicalOperator {
		AND, OR, XOR, IFF, IMP;
	}

	/**
	 * The enum type specifying the two relational operators EQ and NE.
	 */
	public static enum TypeEqNeOperator {
		EQ, NE;
	}

	/**
	 * The enum type specifying the different types of nodes that can be found in syntactic trees (built for intensional expressions).
	 */
	public static enum TypeExpr {
		NEG(1),
		ABS(1),
		SQR(1),
		ADD(2, Integer.MAX_VALUE),
		SUB(2),
		MUL(2, Integer.MAX_VALUE),
		DIV(2),
		MOD(2),
		POW(2),
		DIST(2),
		MIN(2, Integer.MAX_VALUE),
		MAX(2, Integer.MAX_VALUE),
		LT(2),
		LE(2),
		GE(2),
		GT(2),
		NE(2, Integer.MAX_VALUE),
		EQ(2, Integer.MAX_VALUE),
		SET(0, Integer.MAX_VALUE),
		IN(2),
		NOTIN(2),
		NOT(1),
		AND(2, Integer.MAX_VALUE),
		OR(2, Integer.MAX_VALUE),
		XOR(2, Integer.MAX_VALUE),
		IFF(2, Integer.MAX_VALUE),
		IMP(2),
		IF(3),
		CARD(1),
		UNION(2, Integer.MAX_VALUE),
		INTER(2, Integer.MAX_VALUE),
		DIFF(2),
		SDIFF(2, Integer.MAX_VALUE),
		HULL(1),
		DJOINT(2),
		SUBSET(2),
		SUBSEQ(2),
		SUPSEQ(2),
		SUPSET(2),
		CONVEX(1),
		FDIV(2),
		FMOD(2),
		SQRT(1),
		NROOT(2),
		EXP(1),
		LN(1),
		LOG(2),
		SIN(1),
		COS(1),
		TAN(1),
		ASIN(1),
		ACOS(1),
		ATAN(1),
		SINH(1),
		COSH(1),
		TANH(1),
		VAR(0),
		PAR(0),
		LONG(0),
		RATIONAL(0),
		DECIMAL(0),
		SYMBOL(0);

		/** The name of the constant in lower-case. */
		public final String lcname;

		/** The minimal and maximal arity (number of sons) of the node. */
		public final int arityMin, arityMax;

		/**
		 * Builds a constant, while specifying its minimal and maximal arity (number of sons).
		 */
		TypeExpr(int arityMin, int arityMax) {
			this.arityMin = arityMin;
			this.arityMax = arityMax;
			this.lcname = name().toLowerCase();
		}

		/** Builds a constant, while specifying its arity (number of sons). */
		TypeExpr(int arity) {
			this(arity, arity);
		}

		/**
		 * returns true iff this constant denotes an operator that is commutative (and also associative when it is a non-binary operator).
		 */
		public boolean isSymmetricOperator() {
			return this == ADD || this == MUL || this == MIN || this == MAX || this == DIST || this == NE || this == EQ || this == SET || this == AND
					|| this == OR || this == XOR || this == IFF || this == UNION || this == INTER || this == DJOINT;
		}

		/** returns true iff this constant denotes a binary non-symmetric relational operator (i.e., LT, LE, GE and GT). */
		public boolean isNonSymmetricRelationalOperator() {
			return this == LT || this == LE || this == GE || this == GT;
		}

		/** returns true iff this constant denotes a relational operator (i.e., LT, LE, GE, GT, EQ and NE). */
		public boolean isRelationalOperator() {
			return isNonSymmetricRelationalOperator() || this == NE || this == EQ;
		}

		/** returns true iff this constant denotes a (non-unary) arithmetic operator (i.e., ADD, SUB, MUL, DIV, MOD, POW and DIST ). */
		public boolean isArithmeticOperator() {
			return this == ADD || this == SUB || this == MUL || this == DIV || this == MOD || this == POW || this == DIST;
		}

		/** returns true iff this constant denotes a logical operator (i.e., AND, OR, XOR, IFF, and IMP). */
		public boolean isLogicalOperator() {
			return this == AND || this == OR || this == XOR || this == IFF || this == IMP;
		}

		/**
		 * Returns the constant that denotes the arithmetic inversion of this constant, if this constant denotes a relational operator, null
		 * otherwise. The arithmetic inversion is not obtained by applying a logical negation but a multiplication by -1. For example, the
		 * arithmetic inversion of LT is GT (and not GE). Also, the arithmetic inversion of EQ is EQ.
		 */
		public TypeExpr arithmeticInversion() {
			return this == LT ? GT : this == LE ? GE : this == GE ? LE : this == GT ? LT : this == NE ? NE : this == EQ ? EQ : null;
		}

		/**
		 * Returns the constant that denotes the logical inversion of this constant, if this constant denotes a Boolean operator (that can
		 * be inversed when considering the current pool of constants), null otherwise. The logical inversion is different from the
		 * arithmetic inversion. For example, the logical inversion of LT is GE (and not GT). Also, the logical inversion of EQ is NE.
		 */
		public TypeExpr logicalInversion() {
			return this == LT ? GE
					: this == LE ? GT
							: this == GE ? LT
									: this == GT ? LE
											: this == NE ? EQ
													: this == EQ ? NE
															: this == IN ? NOTIN
																	: this == NOTIN ? IN
																			: this == SUBSET ? SUPSEQ
																					: this == SUBSEQ ? SUPSET
																							: this == SUPSEQ ? SUBSET : this == SUPSET ? SUBSEQ : null;
		}

	}

	/**
	 * The enum type specifying the different types of measures used by elements <cost>.
	 */
	public static enum TypeMeasure {
		VAR, DEC, VAL, EDIT;
	}

	/** The enum type specifying the different types of objectives. */
	public static enum TypeObjective {
		EXPRESSION, SUM, PRODUCT, MINIMUM, MAXIMUM, NVALUES, LEX;
	}

	/**
	 * The enum type specifying the different types of combination of objectives.
	 */
	public static enum TypeCombination {
		LEXICO, PARETO;
	}

	/**
	 * The enum type specifying the different types of ranking used by constraints <maximum>, <minimum>, <element>.
	 */
	public static enum TypeRank {
		FIRST, LAST, ANY;
	}

	/**
	 * The enum type specifying the different types of optimization (used for annotations).
	 */
	public static enum TypeOptimization {
		MIN, MAX;
	}

	/**
	 * The interface that denotes a class (XML/HTML meaning) that can be associated with any XCSP3 element
	 */
	public interface TypeClass {

		/**
		 * Returns the camel case name of this constant (for example, clues, or symmetryBreaking)
		 */
		public String ccname();

		/** Transforms String objects into TypeClass objects. */
		public static TypeClass[] classesFor(String... classes) {
			return Stream.of(classes).map(
					s -> Stream.of(StandardClass.values()).map(c -> (TypeClass) c).filter(c -> c.ccname().equals(s)).findFirst().orElse(new SpecialClass(s)))
					.toArray(TypeClass[]::new);
		}

		/** Determines if the two specified arrays of TypeClass objects intersect or not. */
		public static boolean intersect(TypeClass[] t1, TypeClass[] t2) {
			return t1 != null && t2 != null && Stream.of(t1).anyMatch(c1 -> Stream.of(t2).anyMatch(c2 -> c1.ccname().equals(c2.ccname())));
		}

		/**
		 * Determines if the two specified arrays of TypeClass objects are equivalent or not.
		 */
		public static boolean equivalent(Set<TypeClass> s1, Set<TypeClass> s2) {
			return (s1 == null && s2 == null) || (s1 != null && s2 != null && s1.size() == s2.size()
					&& s1.stream().allMatch(c1 -> s2.stream().anyMatch(c2 -> c1.ccname().equals(c2.ccname()))));
		}
	}

	/**
	 * The enum type describing the different standard classes that can be associated with XCSP3 elements.
	 */
	public static enum StandardClass implements TypeClass {
		CHANNELING, CLUES, ROWS, COLUMNS, BLOCKS, DIAGONALS, SYMMETRY_BREAKING, REDUNDANT_CONSTRAINTS, NOGOODS;

		private final String ccname;

		@Override
		public String ccname() {
			return ccname;
		}

		private StandardClass() {
			ccname = Utilities.toCamelCase(super.name());
		}

	}

	/** The class that allows the user to define his own classes */
	public static class SpecialClass implements TypeClass {
		private final String ccname;

		public SpecialClass(String name) {
			this.ccname = name;
		}

		@Override
		public String ccname() {
			return ccname;
		}

		public boolean equals(Object o) {
			return o instanceof SpecialClass && ((SpecialClass) o).ccname.equals(this.ccname);
		}

		public int hashCode() {
			return ccname.hashCode();
		}
	}
}
