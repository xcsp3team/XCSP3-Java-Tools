package org.xcsp.parser.loaders;

import static org.xcsp.common.Types.TypeConditionOperatorRel.EQ;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GT;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LT;
import static org.xcsp.common.Types.TypeExpr.ABS;
import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.IN;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.NOTIN;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SET;
import static org.xcsp.common.Types.TypeExpr.SQR;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_LOGIC_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_SUM_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZING_BEFORE_CONVERTING;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionRel;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeEqNeOperator;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeLogicalOperator;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Types.TypeUnaryArithmeticOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.Utilities.ModifiableBoolean;
import org.xcsp.common.predicates.EvaluationManager;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks;
import org.xcsp.parser.XCallbacks.XCallbacksParameters;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XValues.IntegerEntity;
import org.xcsp.parser.entries.XValues.IntegerInterval;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * This class allows us to load integer constraints, at parsing time.
 * 
 * @author Christophe Lecoutre -- {@literal lecoutre@cril.fr}
 *
 */
public class CtrLoaderInteger {
	/**
	 * A reference to the main parsing object, responsible for dealing with callback functions.
	 */
	private XCallbacks xc;

	/**
	 * Builds an object that can be used to load integer constraints.
	 * 
	 * @param xc
	 *            the main parsing object, responsible for dealing with callback functions
	 */
	public CtrLoaderInteger(XCallbacks xc) {
		this.xc = xc;
	}

	/**
	 * Constant used to control the maximum allowed number of values in the domain of a variable.
	 */
	public static final int NB_MAX_VALUES = 10000000;

	/**
	 * Transforms the specified {@code long} into an {@code int} (while controlling that no information is lost).
	 * 
	 * @param l
	 *            a specified long integer
	 * @return an {@code int} corresponding to the specified {@code long}
	 */
	private int trInteger(Long l) {
		return Utilities.safeLong2Int(l, true);
	}

	/**
	 * Transforms the specified object into a 1-dimensional array of integers
	 * 
	 * @param value
	 *            an object denoting a sequence of integers
	 * @return a 1-dimensional array of integers
	 */
	private int[] trIntegers(Object value) {
		if (value instanceof int[])
			return (int[]) value;
		if (value instanceof IntegerEntity[]) {
			int[] values = IntegerEntity.toIntArray((IntegerEntity[]) value, NB_MAX_VALUES);
			Utilities.control(values != null, "Too many values. The parser needs an extension.");
			return values;
		}
		// Note that STAR is not allowed in simple lists (because this is irrelevant), which allows us to write:
		return IntStream.range(0, Array.getLength(value)).map(i -> trInteger((long) Array.get(value, i))).toArray();
	}

	/**
	 * Builds a 2-dimensional array of integers, whose size is specified and whose values are computed from the specified function.
	 * 
	 * @param size1
	 *            the size of the first dimension of the array
	 * @param size2
	 *            the size of the second dimension of the array
	 * @param f
	 *            a function mapping a pair of integers into an integer
	 * @return a 2-dimensional array of integers
	 */
	private int[][] build(int size1, int size2, BiFunction<Integer, Integer, Integer> f) {
		return IntStream.range(0, size1).mapToObj(i -> IntStream.range(0, size2).map(j -> f.apply(i, j)).toArray()).toArray(int[][]::new);
	}

	/**
	 * Transforms the specified object into a 2-dimensional array of integers
	 * 
	 * @param value
	 *            an object denoting a 2-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	private int[][] trIntegers2D(Object value) {
		if (value instanceof int[][])
			return (int[][]) value;
		if (value instanceof byte[][]) {
			byte[][] m = (byte[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_BYTE ? Constants.STAR_INT : m[i][j]);
		}
		if (value instanceof short[][]) {
			short[][] m = (short[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_SHORT ? Constants.STAR_INT : m[i][j]);
		}
		if (value instanceof long[][]) {
			long[][] m = (long[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_LONG ? Constants.STAR_INT : trInteger(m[i][j]));
		}
		if (value instanceof Long[][]) {
			Long[][] m = (Long[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_LONG ? Constants.STAR_INT : trInteger(m[i][j]));
		}
		return (int[][]) xc.unimplementedCase(value);
	}

	/**
	 * Loads the specified object denoting a parsed constraint. A callback function (or possibly several) will be called.
	 * 
	 * @param c
	 *            an object denoting a parsed constraint
	 */
	public void load(XCtr c) {
		switch (c.getType()) {
		case intension:
			intension(c);
			break;
		case extension:
			extension(c);
			break;
		case regular:
			regular(c);
			break;
		case mdd:
			mdd(c);
			break;
		case allDifferent:
			allDifferent(c);
			break;
		case allEqual:
			allEqual(c);
			break;
		case ordered:
			ordered(c);
			break;
		case lex:
			lex(c);
			break;
		case sum:
			sum(c);
			break;
		case count:
			count(c);
			break;
		case nValues:
			nValues(c);
			break;
		case cardinality:
			cardinality(c);
			break;
		case maximum:
			maximum(c);
			break;
		case minimum:
			minimum(c);
			break;
		case element:
			element(c);
			break;
		case channel:
			channel(c);
			break;
		case stretch: // in XCSP3-core (but should leave)
			stretch(c);
			break;
		case noOverlap:
			noOverlap(c);
			break;
		case cumulative:
			cumulative(c);
			break;
		case instantiation:
			instantiation(c);
			break;
		case clause: // not in XCSP3-core (but could enter)
			clause(c);
			break;
		case circuit: // not in XCSP3-core (but should enter)
			circuit(c);
			break;
		case binPacking: // not in XCSP3-core (and could enter)
			binPacking(c);
			break;
		default:
			xc.unimplementedCase(c);
		}
	}

	private boolean intensionToExtension(String id, XVarInteger[] scp, XNodeParent<XVarInteger> root) {
		int arityLimit = ((Integer) xc.implem().currParameters.get(CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT));
		if (scp.length > arityLimit)
			return false;
		long spaceLimit = ((Long) xc.implem().currParameters.get(CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT));
		long size = XVarInteger.domainCartesianProductSize(scp);
		if (size == -1 || size > spaceLimit)
			return false;
		int[][] domValues = Stream.of(scp).map(x -> IntegerEntity.toIntArray((IntegerEntity[]) ((XDomInteger) x.dom).values, Integer.MAX_VALUE))
				.toArray(int[][]::new);
		ModifiableBoolean b = new ModifiableBoolean(null); // later, maybe a control parameter
		int[][] tuples = new EvaluationManager(root).generateTuples(domValues, b);
		assert b.value != null;
		if (tuples.length == 0) { // special case because 0 tuple
			if (b.value)
				xc.buildCtrTrue(id, scp);
			else
				xc.buildCtrFalse(id, scp);
		} else if (scp.length == 1) // unary constraint
			xc.buildCtrExtension(id, scp[0], Stream.of(tuples).mapToInt(t -> t[0]).toArray(), true, new HashSet<>());
		else
			xc.buildCtrExtension(id, scp, tuples, b.value, new HashSet<>());
		return true;
	}

	/**
	 * Posts a constraint by runnning the specified object
	 * 
	 * @param id
	 *            the id of a constraint
	 * @param r
	 */
	private void post(String id, Runnable r) {
		Utilities.control(!xc.implem().postedRecognizedCtrs.contains(id), "Pb with the same constraint posted twice");
		xc.implem().postedRecognizedCtrs.add(id);
		r.run();
	}

	// Returns an arithmetic operator iff the tree has the form s0 <op> s1 with s0 of type t0, s1 of type t1 and <op> an arithmetic operator
	// in {+,-,*,/,%,||}.
	private TypeArithmeticOperator aropOn(XNode<?> node, TypeExpr t0, TypeExpr t1) {
		if (!node.type.isNonUnaryArithmeticOperator())
			return null;
		return node.sons.length == 2 && node.sons[0].type == t0 && node.sons[1].type == t1 ? TypeArithmeticOperator.valueOf(node.type.name()) : null;
	}

	protected XNodeParent<XVarInteger> node(TypeExpr type, XNode<XVarInteger> leftSon, XNode<XVarInteger> rightSon) {
		return new XNodeParent<>(type, leftSon, rightSon);
	}

	protected XNodeParent<XVarInteger> node(TypeExpr type, XNode<XVarInteger> son) {
		return new XNodeParent<>(type, son);
	}

	private XNodeLeaf<XVarInteger> var = new XNodeLeaf<>(VAR, null);
	private XNodeLeaf<XVarInteger> val = new XNodeLeaf<>(LONG, null);
	private XNodeLeaf<XVarInteger> set = new XNodeLeaf<>(SET, null);

	public abstract class Matcher {
		protected final XNodeParent<XVarInteger> target;

		Matcher(XNodeParent<XVarInteger> target) {
			this.target = target;
		}

		abstract boolean validTypeAtLevel(TypeExpr type, int level);

		abstract void primitiveFor(String id, XNodeParent<XVarInteger> root);

		boolean similarNodes(XNode<XVarInteger> n1, XNode<XVarInteger> n2, int level) {
			if (n1.type == TypeExpr.SET && n2.type == TypeExpr.SET) {
				assert n1 instanceof XNodeLeaf; // by construction of abstract pattern
				return n2 instanceof XNodeLeaf || Stream.of(n2.sons).allMatch(s -> s.type == LONG);
			}
			if (n1 instanceof XNodeLeaf != n2 instanceof XNodeLeaf)
				return false;
			if (n1 instanceof XNodeLeaf)
				return n1.type == n2.type;
			boolean validType = n1.type == n2.type || (n1.type == null && validTypeAtLevel(n2.type, level));
			return validType && n1.sons.length == n2.sons.length
					&& IntStream.range(0, n1.sons.length).allMatch(i -> similarNodes(n1.sons[i], n2.sons[i], level + 1));
		}

		boolean positiveExtraControl(XNodeParent<XVarInteger> root) {
			return true;
		}

		boolean isPrimitiveRecognized(String id, XNodeParent<XVarInteger> root) {
			if (!similarNodes(target, root, 0) || !positiveExtraControl(root))
				return false;
			primitiveFor(id, root);
			return true;
		}

		TypeConditionOperatorRel rel(TypeExpr t) {
			return TypeConditionOperatorRel.valueOf(t);
		}

		TypeConditionOperatorRel reli(TypeExpr t) {
			return TypeConditionOperatorRel.valueOf(t).arithmeticInversion();
		}

		TypeArithmeticOperator art(TypeExpr t) {
			return TypeArithmeticOperator.valueOf(t.name());
		}
	}

	public abstract class MatcherRel extends Matcher {
		MatcherRel(XNodeParent<XVarInteger> target) {
			super(target);
		}

		@Override
		boolean validTypeAtLevel(TypeExpr type, int level) {
			return (level == 0 && type.isRelationalOperator()) || (level == 1 && type.isNonUnaryArithmeticOperator());
		}
	}

	public abstract class MatcherSet extends Matcher {
		MatcherSet(XNodeParent<XVarInteger> target) {
			super(target);
		}

		@Override
		boolean validTypeAtLevel(TypeExpr type, int level) {
			return level == 0 && type.oneOf(IN, NOTIN);
		}
	}

	Matcher[] m1s = new Matcher[] { new MatcherRel(node(null, var, val)) { // x <op> k
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.firstVar() + " " + rel(r.type) + " " + r.firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), rel(r.type), r.firstVal()));
		}
	}, new MatcherRel(node(null, val, var)) { // k <op> x
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.firstVar() + " " + reli(r.type) + " " + r.firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), reli(r.type), r.firstVal()));
		}
	}, new MatcherRel(node(null, node(null, var, val), val)) { // (x <+> p) op k
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.firstVar() + " " + art(r.sons[0].type) + " " + r.sons[0].firstVal() + " " + rel(r.type) + " " + r.sons[1].firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), art(r.sons[0].type), r.sons[0].firstVal(), rel(r.type), r.sons[1].firstVal()));
		}
	}, new MatcherRel(node(null, val, node(null, var, val))) { // k op (x <+> p)
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out
					.println("Rec " + r.firstVar() + " " + art(r.sons[1].type) + " " + r.sons[1].firstVal() + " " + reli(r.type) + " " + r.sons[0].firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), art(r.sons[1].type), r.sons[1].firstVal(), reli(r.type), r.sons[0].firstVal()));
		}
	}, new MatcherSet(node(null, var, set)) { // k <in|notin> t
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			int[] t = r.sons[1] instanceof XNodeLeaf ? new int[0] : Stream.of(r.sons[1].sons).mapToInt(s -> s.firstVal()).toArray();
			System.out.println("Rec " + r.firstVar() + " " + TypeConditionOperatorSet.valueOf(r.type) + " " + Utilities.join(t));
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), TypeConditionOperatorSet.valueOf(r.type), t));
		}
	}, new MatcherSet(node(AND, node(TypeExpr.LE, var, val), node(TypeExpr.LE, val, var))) { // x in [min..max]
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.firstVar() + " IN " + r.sons[1].firstVal() + ".." + r.sons[0].firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), TypeConditionOperatorSet.IN, r.sons[1].firstVal(), r.sons[0].firstVal()));
		}
	}, new MatcherSet(node(OR, node(TypeExpr.LE, var, val), node(TypeExpr.LE, val, var))) { // x notin [min..max]
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.firstVar() + " NOTIN " + (r.sons[0].firstVal() + 1) + ".." + (r.sons[1].firstVal() - 1));
			post(id, () -> xc.buildCtrPrimitive(id, r.firstVar(), TypeConditionOperatorSet.NOTIN, r.sons[0].firstVal() + 1, r.sons[1].firstVal() - 1));
		}
	} };

	Matcher[] m2s = new Matcher[] { new MatcherRel(node(null, var, var)) { // x <op> y
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[0].firstVar() + " SUB " + r.sons[1].firstVar() + " " + TypeConditionOperatorRel.valueOf(r.type) + " 0");
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[0].firstVar(), TypeArithmeticOperator.SUB, r.sons[1].firstVar(),
					TypeConditionOperatorRel.valueOf(r.type), 0));
		}
	}, new MatcherRel(node(null, node(null, var, val), var)) { // (x aop k) op y
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[0].firstVar() + " " + TypeArithmeticOperator.valueOf(r.sons[0].type.name()) + " " + r.sons[0].firstVal() + " "
					+ TypeConditionOperatorRel.valueOf(r.type) + " " + r.sons[1].firstVar());
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[0].firstVar(), TypeArithmeticOperator.valueOf(r.sons[0].type.name()), r.sons[0].firstVal(),
					TypeConditionOperatorRel.valueOf(r.type), r.sons[1].firstVar()));
		}
	}, new MatcherRel(node(null, node(null, var, var), val)) { // (x aop y) op k
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[0].firstVar() + " " + TypeArithmeticOperator.valueOf(r.sons[0].type.name()) + " " + r.sons[0].var(1) + " "
					+ TypeConditionOperatorRel.valueOf(r.type) + " " + r.sons[1].firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[0].firstVar(), TypeArithmeticOperator.valueOf(r.sons[0].type.name()), r.sons[0].var(1),
					TypeConditionOperatorRel.valueOf(r.type), r.sons[1].firstVal()));
		}
	}, new MatcherRel(node(null, var, node(null, var, val))) { // x op (y aop k)
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[1].firstVar() + " " + TypeArithmeticOperator.valueOf(r.sons[1].type.name()) + " " + r.sons[1].firstVal() + " "
					+ TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion() + " " + r.sons[0].firstVar());
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[1].firstVar(), TypeArithmeticOperator.valueOf(r.sons[1].type.name()), r.sons[1].firstVal(),
					TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion(), r.sons[0].firstVar()));
		}
	}, new MatcherRel(node(null, val, node(null, var, var))) { // k op (x aop y)
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[1].firstVar() + " " + TypeArithmeticOperator.valueOf(r.sons[1].type.name()) + " " + r.sons[1].var(1) + " "
					+ TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion() + " " + r.sons[0].firstVal());
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[1].firstVar(), TypeArithmeticOperator.valueOf(r.sons[1].type.name()), r.sons[1].var(1),
					TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion(), r.sons[0].firstVal()));
		}
	}, new MatcherRel(node(TypeExpr.EQ, node(null, var), var)) { // uop(x) = y with uop in {abs,neg,sqr,not}
		@Override
		boolean validTypeAtLevel(TypeExpr type, int level) {
			return level == 1 && type.oneOf(ABS, NEG, SQR, NOT);
		}

		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[1].firstVar() + " " + TypeUnaryArithmeticOperator.valueOf(r.sons[0].type.name()) + " " + r.sons[0].firstVar());
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[1].firstVar(), TypeUnaryArithmeticOperator.valueOf(r.sons[0].type.name()), r.sons[0].firstVar()));
		}
	} };

	Matcher[] m3s = new Matcher[] { new MatcherRel(node(null, node(null, var, var), var)) { // (x aop y) <op> z
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[0].var(0) + " " + TypeArithmeticOperator.valueOf(r.sons[0].type.name()) + " " + r.sons[0].var(1) + " "
					+ TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion() + " " + r.sons[1].var(0));
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[0].var(0), TypeArithmeticOperator.valueOf(r.sons[0].type.name()), r.sons[0].var(1),
					TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion(), r.sons[1].var(0)));

		}
	}, new MatcherRel(node(null, var, node(null, var, var))) { // x <op> (y aop z)
		@Override
		public void primitiveFor(String id, XNodeParent<XVarInteger> r) {
			System.out.println("Rec " + r.sons[1].var(0) + " " + TypeArithmeticOperator.valueOf(r.sons[1].type.name()) + " " + r.sons[1].var(1) + " "
					+ TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion() + " " + r.sons[0].var(0));
			post(id, () -> xc.buildCtrPrimitive(id, r.sons[1].var(0), TypeArithmeticOperator.valueOf(r.sons[1].type.name()), r.sons[1].var(1),
					TypeConditionOperatorRel.valueOf(r.type).arithmeticInversion(), r.sons[0].var(0)));

		}
	} };

	private boolean recognizePrimitive(String id, int arity, XNodeParent<XVarInteger> root) {
		if (arity > 3 || root.sons.length != 2)
			return false;
		if (arity == 1 && xc.implem().currParameters.containsKey(RECOGNIZE_UNARY_PRIMITIVES))
			for (Matcher m : m1s)
				if (m.isPrimitiveRecognized(id, root))
					break;
		if (arity == 2 && xc.implem().currParameters.containsKey(RECOGNIZE_BINARY_PRIMITIVES))
			for (Matcher m : m2s)
				if (m.isPrimitiveRecognized(id, root))
					break;
		if (arity == 3 && xc.implem().currParameters.containsKey(RECOGNIZE_TERNARY_PRIMITIVES))
			for (Matcher m : m3s)
				if (m.isPrimitiveRecognized(id, root))
					break;
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private boolean recognizeLogic(String id, XNodeParent<XVarInteger> root) {
		if (xc.implem().currParameters.containsKey(RECOGNIZE_LOGIC_CASES)) {
			if (root.type.isNonUnaryLogicalOperator() && Stream.of(root.sons).allMatch(s -> s.type == VAR && s.firstVar().isZeroOne())) {
				XVarInteger[] vars = Stream.of(root.sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
				Utilities.control(vars.length >= 2, "Bad construction for " + root);
				post(id, () -> xc.buildCtrLogic(id, TypeLogicalOperator.valueOf(root.type.name()), vars));
			} else if (root.type.oneOf(TypeExpr.EQ, TypeExpr.NE) && root.sons.length == 2 && root.sons[0].type.isNonUnaryLogicalOperator()
					&& root.sons[1].type == VAR) {
				if (Stream.of(((XNodeParent<XVarInteger>) root.sons[0]).sons).allMatch(s -> s.type == VAR && s.firstVar().isZeroOne())) {
					XVarInteger[] vars = Stream.of(((XNodeParent<?>) root.sons[0]).sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
					Utilities.control(vars.length >= 2, "Bad construction for " + root);
					post(id, () -> xc.buildCtrLogic(id, root.sons[1].firstVar(), TypeEqNeOperator.valueOf(root.type.name()),
							TypeLogicalOperator.valueOf(root.sons[0].type.name()), vars));
				}
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private Condition basicCondition(XNodeParent<XVarInteger> node) {
		if (node.type.isRelationalOperator() && node.sons.length == 2 && node.sons[1].type.oneOf(VAR, LONG)) {
			TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(node.type.name());
			return node.sons[1].type == VAR ? new ConditionVar(op, node.sons[1].firstVar()) : new ConditionVal(op, node.sons[1].firstVal());
		}
		return null;
	}

	private boolean recognizeExtremum(String id, XNodeParent<XVarInteger> root) {
		if (xc.implem().currParameters.containsKey(RECOGNIZE_EXTREMUM_CASES)) {
			Condition cond = basicCondition(root);
			if (cond != null && root.sons[0].type.oneOf(MIN, MAX) && Stream.of(((XNodeParent<?>) root.sons[0]).sons).allMatch(s -> s.type == VAR)) {
				XVarInteger[] vars = Stream.of(((XNodeParent<?>) root.sons[0]).sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
				if (root.sons[0].type == MIN)
					post(id, () -> xc.buildCtrMinimum(id, vars, cond));
				else
					post(id, () -> xc.buildCtrMaximum(id, vars, cond));
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private boolean recognizeSum(String id, XNodeParent<XVarInteger> root) {
		if (xc.implem().currParameters.containsKey(RECOGNIZE_SUM_CASES)) {
			Condition cond = basicCondition(root);
			if (cond != null && root.sons[0].type == ADD && root.vars().length > 2) {
				XNodeParent<XVarInteger> add = (XNodeParent<XVarInteger>) root.sons[0];
				if (Stream.of(add.sons).allMatch(s -> s.type == VAR || aropOn(s, VAR, LONG) == TypeArithmeticOperator.MUL)) {
					XVarInteger[] vars = Stream.of(add.sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
					int[] coeffs = Stream.of(add.sons).mapToInt(s -> s.type == VAR ? 1 : (int) s.firstVal()).toArray();
					if (IntStream.of(coeffs).allMatch(v -> v == 1))
						post(id, () -> xc.buildCtrSum(id, vars, cond));
					else
						post(id, () -> xc.buildCtrSum(id, vars, coeffs, cond));
				} else if (Stream.of(add.sons).allMatch(s -> aropOn(s, VAR, VAR) == TypeArithmeticOperator.MUL)) {
					XVarInteger[] vars = Stream.of(add.sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
					XVarInteger[] coeffs = Stream.of(add.sons).map(s -> s.var(1)).toArray(XVarInteger[]::new);
					post(id, () -> xc.buildCtrSum(id, vars, coeffs, cond));
				}
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private void intension(XCtr c) {
		System.out.println("\nROOT1= " + c.childs[0].value + "\nROOT2= " + ((XNodeParent<?>) c.childs[0].value).canonization());
		XNodeParent<XVarInteger> root = (XNodeParent<XVarInteger>) ((XNode<XVarInteger>) c.childs[0].value).canonization();
		XVarInteger[] scope = Stream.of(root.vars()).map(x -> x).toArray(XVarInteger[]::new); // important: scope to be built from canonized root

		if (xc.implem().currParameters.get(RECOGNIZING_BEFORE_CONVERTING) == Boolean.FALSE) // we try first converting into extension
			if (intensionToExtension(c.id, scope, root))
				return;
		if (recognizePrimitive(c.id, scope.length, root) || recognizeLogic(c.id, root) || recognizeExtremum(c.id, root))
			return;
		if (xc.implem().currParameters.get(RECOGNIZING_BEFORE_CONVERTING) == Boolean.TRUE) // we now try converting into extension
			if (intensionToExtension(c.id, scope, root))
				return;
		xc.buildCtrIntension(c.id, scope, root);
	}

	private void extension(XCtr c) {
		CChild c1 = c.childs[1];
		boolean positive = c1.type == TypeChild.supports;
		if (c1.value == null || Array.getLength(c1.value) == 0) { // special case because 0 tuple
			if (positive)
				xc.buildCtrFalse(c.id, c.vars());
			else
				xc.buildCtrTrue(c.id, c.vars());
		} else {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			if (list.length == 1) // unary constraint
				xc.buildCtrExtension(c.id, list[0], trIntegers(c1.value), positive, c1.flags);
			else {
				int[][] tuples = xc.implem().cache4Tuples.get(c1.value);
				if (tuples == null)
					xc.implem().cache4Tuples.put(c1.value, tuples = trIntegers2D(c1.value));
				xc.buildCtrExtension(c.id, list, tuples, positive, c1.flags);
			}
		}
	}

	private void regular(XCtr c) {
		xc.buildCtrRegular(c.id, (XVarInteger[]) c.childs[0].value, (Object[][]) c.childs[1].value, (String) c.childs[2].value, (String[]) c.childs[3].value);
	}

	private void mdd(XCtr c) {
		xc.buildCtrMDD(c.id, (XVarInteger[]) c.childs[0].value, (Object[][]) c.childs[1].value);
	}

	private void allDifferent(XCtr c) {
		CChild[] childs = c.childs;
		if (childs[0].type == TypeChild.matrix) {
			Utilities.control(childs.length == 1, "Other forms of allDifferent-matrix not implemented");
			xc.buildCtrAllDifferentMatrix(c.id, (XVarInteger[][]) (childs[0].value));
		} else if (childs[0].type == TypeChild.list) {
			if (childs.length == 1)
				xc.buildCtrAllDifferent(c.id, (XVarInteger[]) childs[0].value);
			else if (childs[1].type == TypeChild.except)
				xc.buildCtrAllDifferentExcept(c.id, (XVarInteger[]) childs[0].value, trIntegers(childs[1].value));
			else if (childs[childs.length - 1].type == TypeChild.list)
				xc.buildCtrAllDifferentList(c.id, Stream.of(childs).map(p -> p.value).toArray(XVarInteger[][]::new));
			else
				xc.unimplementedCase(c);
		} else
			xc.unimplementedCase(c);
	}

	private void allEqual(XCtr c) {
		if (c.childs[0].type == TypeChild.list)
			if (c.childs.length == 1)
				xc.buildCtrAllEqual(c.id, (XVarInteger[]) c.childs[0].value);
			else
				xc.unimplementedCase(c);
		else
			xc.unimplementedCase(c);
	}

	private void ordered(XCtr c) {
		if (c.childs[0].type == TypeChild.list)
			if (c.childs.length == 2)
				xc.buildCtrOrdered(c.id, (XVarInteger[]) c.childs[0].value, ((TypeOperator) c.childs[1].value).toRel());
			else
				xc.unimplementedCase(c);
		else
			xc.unimplementedCase(c);
	}

	private void lex(XCtr c) {
		TypeOperatorRel op = ((TypeOperator) c.childs[c.childs.length - 1].value).toRel();
		if (c.childs[0].type == TypeChild.matrix)
			xc.buildCtrLexMatrix(c.id, (XVarInteger[][]) c.childs[0].value, op);
		else {
			xc.buildCtrLex(c.id, IntStream.range(0, c.childs.length - 1).mapToObj(i -> c.childs[i].value).toArray(XVarInteger[][]::new), op);
		}
	}

	private void sum(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[c.childs.length - 1].value;
		if (c.childs.length == 2)
			xc.buildCtrSum(c.id, list, condition);
		else if (c.childs[1].value instanceof XVarInteger[])
			xc.buildCtrSum(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
		else
			xc.buildCtrSum(c.id, list, trIntegers(c.childs[1].value), condition);
	}

	private boolean recognizeCountCases(String id, XVarInteger[] list, Long[] values, TypeConditionOperatorRel op, Condition condition) {
		if (xc.implem().currParameters.containsKey(XCallbacksParameters.RECOGNIZE_COUNT_CASES)) {
			if (values.length == 1) {
				if (condition instanceof ConditionVal) {
					int k = Utilities.safeLong2Int(((ConditionVal) condition).k, true);
					// other controls on k ?
					if (op == LT)
						post(id, () -> xc.buildCtrAtMost(id, list, trInteger(values[0]), k - 1));
					else if (op == LE)
						post(id, () -> xc.buildCtrAtMost(id, list, trInteger(values[0]), k));
					else if (op == GE)
						post(id, () -> xc.buildCtrAtLeast(id, list, trInteger(values[0]), k));
					else if (op == GT)
						post(id, () -> xc.buildCtrAtLeast(id, list, trInteger(values[0]), k + 1));
					else if (op == EQ)
						post(id, () -> xc.buildCtrExactly(id, list, trInteger(values[0]), k));
				} else if (condition instanceof ConditionVar) {
					if (op == EQ)
						post(id, () -> xc.buildCtrExactly(id, list, trInteger(values[0]), (XVarInteger) ((ConditionVar) condition).x));
				}
			} else if (op == EQ) {
				if (condition instanceof ConditionVal)
					post(id, () -> xc.buildCtrAmong(id, list, trIntegers(values), Utilities.safeLong2Int(((ConditionVal) condition).k, true)));
				else if (condition instanceof ConditionVar)
					post(id, () -> xc.buildCtrAmong(id, list, trIntegers(values), (XVarInteger) ((ConditionVar) condition).x));
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private void count(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[2].value;
		if (c.childs[1].value instanceof Long[] && condition instanceof ConditionRel) {
			TypeConditionOperatorRel op = ((ConditionRel) condition).operator;
			Long[] values = (Long[]) c.childs[1].value;
			if (recognizeCountCases(c.id, list, values, op, condition))
				return;
			xc.buildCtrCount(c.id, list, trIntegers(c.childs[1].value), condition);
		} else
			xc.buildCtrCount(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
	}

	private boolean recognizeNvaluesCases(String id, XVarInteger[] list, Condition condition) {
		if (xc.implem().currParameters.containsKey(XCallbacksParameters.RECOGNIZE_NVALUES_CASES) && condition instanceof ConditionVal) {
			TypeConditionOperatorRel op = ((ConditionRel) condition).operator;
			int k = Utilities.safeLong2Int(((ConditionVal) condition).k, true);
			if (op == EQ && k == list.length)
				post(id, () -> xc.buildCtrAllDifferent(id, list));
			else if (op == EQ && k == 1)
				post(id, () -> xc.buildCtrAllEqual(id, list));
			else if ((op == GE && k == 2) || (op == GT && k == 1))
				post(id, () -> xc.buildCtrNotAllEqual(id, list));
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private void nValues(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[c.childs.length - 1].value;
		if (c.childs.length == 2 && recognizeNvaluesCases(c.id, list, condition))
			return;
		if (c.childs.length == 2)
			xc.buildCtrNValues(c.id, list, condition);
		else
			xc.buildCtrNValuesExcept(c.id, list, trIntegers(c.childs[1].value), condition);
	}

	private void cardinality(XCtr c) {
		CChild[] childs = c.childs;
		Utilities.control(childs[1].value instanceof Long[], "unimplemented case");
		boolean closed = childs[1].getAttributeValue(TypeAtt.closed, false);
		if (childs[1].value instanceof Long[]) {
			if (childs[2].value instanceof Long[])
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), trIntegers(childs[2].value));
			else if (childs[2].value instanceof XVar[])
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), (XVarInteger[]) childs[2].value);
			else {
				Utilities.control(childs[2].value instanceof IntegerInterval[], "Pb");
				int[] occursMin = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.inf, true)).toArray();
				int[] occursMax = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.sup, true)).toArray();
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, trIntegers(childs[1].value), occursMin, occursMax);
			}
		} else {
			if (childs[2].value instanceof Long[])
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, trIntegers(childs[2].value));
			else if (childs[2].value instanceof XVar[])
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, (XVarInteger[]) childs[2].value);
			else {
				Utilities.control(childs[2].value instanceof IntegerInterval[], "Pb");
				int[] occursMin = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.inf, true)).toArray();
				int[] occursMax = Stream.of((IntegerInterval[]) childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.sup, true)).toArray();
				xc.buildCtrCardinality(c.id, (XVarInteger[]) childs[0].value, closed, (XVarInteger[]) childs[1].value, occursMin, occursMax);
			}
		}
	}

	private void minimumMaximum(XCtr c) {
		CChild[] childs = c.childs;
		XVarInteger[] list = (XVarInteger[]) childs[0].value;
		Condition condition = childs[childs.length - 1].type == TypeChild.condition ? (Condition) childs[childs.length - 1].value : null;
		if (childs[1].type == TypeChild.condition)
			if (c.getType() == TypeCtr.maximum)
				xc.buildCtrMaximum(c.id, list, condition);
			else
				xc.buildCtrMinimum(c.id, list, condition);
		else {
			int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
			TypeRank rank = childs[1].getAttributeValue(TypeAtt.rank, TypeRank.class, TypeRank.ANY);
			if (c.getType() == TypeCtr.maximum)
				xc.buildCtrMaximum(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, condition);
			else
				xc.buildCtrMinimum(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, condition);
		}
	}

	private void maximum(XCtr c) {
		minimumMaximum(c);
	}

	private void minimum(XCtr c) {
		minimumMaximum(c);
	}

	private void element(XCtr c) {
		CChild[] childs = c.childs;
		if (childs[0].value instanceof XVarInteger[]) {
			XVarInteger[] list = (XVarInteger[]) childs[0].value;
			if (childs[1].type == TypeChild.value) {
				if (childs[1].value instanceof XVar)
					xc.buildCtrElement(c.id, list, (XVarInteger) childs[1].value);
				else
					xc.buildCtrElement(c.id, list, Utilities.safeLong2Int((Long) childs[1].value, true));
			} else {
				int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
				TypeRank rank = childs[1].getAttributeValue(TypeAtt.rank, TypeRank.class, TypeRank.ANY);
				if (childs[2].value instanceof XVar)
					xc.buildCtrElement(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, (XVarInteger) childs[2].value);
				else
					xc.buildCtrElement(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, Utilities.safeLong2Int((Long) childs[2].value, true));
			}
		} else {
			int[] list = trIntegers(c.childs[0].value);
			int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
			TypeRank rank = childs[1].getAttributeValue(TypeAtt.rank, TypeRank.class, TypeRank.ANY);
			xc.buildCtrElement(c.id, list, startIndex, (XVarInteger) childs[1].value, rank, (XVarInteger) childs[2].value);
		}
	}

	private void channel(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		int startIndex0 = c.childs[0].getAttributeValue(TypeAtt.startIndex, 0);
		if (c.childs.length == 1)
			xc.buildCtrChannel(c.id, list, startIndex0);
		else if (c.childs[1].type == TypeChild.list) {
			int startIndex1 = c.childs[1].getAttributeValue(TypeAtt.startIndex, 0);
			xc.buildCtrChannel(c.id, list, startIndex0, (XVarInteger[]) c.childs[1].value, startIndex1);
		} else
			xc.buildCtrChannel(c.id, list, startIndex0, (XVarInteger) c.childs[1].value);
	}

	private void stretch(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		int[] values = trIntegers(c.childs[1].value);
		int[] widthsMin = Stream.of((IntegerInterval[]) c.childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.inf, true)).toArray();
		int[] widthsMax = Stream.of((IntegerInterval[]) c.childs[2].value).mapToInt(ii -> Utilities.safeLong2Int(ii.sup, true)).toArray();
		if (c.childs.length == 3)
			xc.buildCtrStretch(c.id, list, values, widthsMin, widthsMax);
		else
			xc.buildCtrStretch(c.id, list, values, widthsMin, widthsMax, trIntegers2D(c.childs[3].value));
	}

	private void noOverlap(XCtr c) {
		boolean zeroIgnored = c.getAttributeValue(TypeAtt.zeroIgnored, true);
		if (c.childs[0].value instanceof XVarInteger[][]) {
			if (c.childs[1].value instanceof XVarInteger[][])
				xc.buildCtrNoOverlap(c.id, (XVarInteger[][]) c.childs[0].value, (XVarInteger[][]) c.childs[1].value, zeroIgnored);
			else
				xc.buildCtrNoOverlap(c.id, (XVarInteger[][]) c.childs[0].value, trIntegers2D(c.childs[1].value), zeroIgnored);
		} else {
			if (c.childs[1].value instanceof XVarInteger[])
				xc.buildCtrNoOverlap(c.id, (XVarInteger[]) c.childs[0].value, (XVarInteger[]) c.childs[1].value, zeroIgnored);
			else
				xc.buildCtrNoOverlap(c.id, (XVarInteger[]) c.childs[0].value, trIntegers(c.childs[1].value), zeroIgnored);
		}
	}

	private void cumulative(XCtr c) {
		CChild[] childs = c.childs;
		XVarInteger[] origins = (XVarInteger[]) childs[0].value;
		Condition condition = (Condition) childs[childs.length - 1].value;
		if (childs.length == 4) {
			if (childs[1].value instanceof Long[] && childs[2].value instanceof Long[])
				xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), trIntegers(childs[2].value), condition);
			else if (childs[1].value instanceof Long[] && !(childs[2].value instanceof Long[]))
				xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), (XVarInteger[]) childs[2].value, condition);
			else if (!(childs[1].value instanceof Long[]) && childs[2].value instanceof Long[])
				xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, trIntegers(childs[2].value), condition);
			else
				xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, (XVarInteger[]) childs[2].value, condition);
		} else {
			XVarInteger[] ends = (XVarInteger[]) childs[2].value;
			if (childs[1].value instanceof Long[] && childs[3].value instanceof Long[])
				xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), ends, trIntegers(childs[3].value), condition);
			else if (childs[1].value instanceof Long[] && !(childs[3].value instanceof Long[]))
				xc.buildCtrCumulative(c.id, origins, trIntegers(childs[1].value), ends, (XVarInteger[]) childs[3].value, condition);
			else if (!(childs[1].value instanceof Long[]) && childs[3].value instanceof Long[])
				xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, ends, trIntegers(childs[3].value), condition);
			else
				xc.buildCtrCumulative(c.id, origins, (XVarInteger[]) childs[1].value, ends, (XVarInteger[]) childs[3].value, condition);
		}
	}

	private void instantiation(XCtr c) {
		xc.buildCtrInstantiation(c.id, (XVarInteger[]) c.childs[0].value, trIntegers(c.childs[1].value));
	}

	private void clause(XCtr c) {
		Object[] t = (Object[]) c.childs[0].value;
		XVarInteger[] pos = Stream.of(t).filter(o -> o instanceof XVar).map(o -> (XVar) o).toArray(XVarInteger[]::new);
		XVarInteger[] neg = Stream.of(t).filter(o -> !(o instanceof XVar)).map(o -> (XVar) ((XNodeLeaf<?>) ((XNodeParent<?>) o).sons[0]).value)
				.toArray(XVarInteger[]::new);
		xc.buildCtrClause(c.id, pos, neg);
	}

	private void circuit(XCtr c) {
		CChild[] childs = c.childs;
		XVarInteger[] list = (XVarInteger[]) childs[0].value;
		int startIndex = childs[0].getAttributeValue(TypeAtt.startIndex, 0);
		if (childs.length == 1)
			xc.buildCtrCircuit(c.id, list, startIndex);
		else if (childs[1].value instanceof XVar)
			xc.buildCtrCircuit(c.id, list, startIndex, (XVarInteger) childs[1].value);
		else
			xc.buildCtrCircuit(c.id, list, startIndex, Utilities.safeLong2Int((Long) childs[1].value, true));
	}

	private void binPacking(XCtr c) {
		CChild[] childs = c.childs;
		XVarInteger[] list = (XVarInteger[]) childs[0].value;
		int[] sizes = trIntegers(c.childs[1].value);
		if (childs[2].type == TypeChild.condition)
			xc.buildBinPacking(c.id, list, sizes, (Condition) childs[2].value);
		else
			xc.buildBinPacking(c.id, list, sizes, (Condition[]) childs[2].value, childs[2].getAttributeValue(TypeAtt.startIndex, 0));
	}
}