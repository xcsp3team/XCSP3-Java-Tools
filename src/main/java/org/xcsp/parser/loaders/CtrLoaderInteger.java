package org.xcsp.parser.loaders;

import static org.xcsp.common.Types.TypeConditionOperatorRel.EQ;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GT;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LT;
import static org.xcsp.common.Types.TypeExpr.ABS;
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.IN;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.NE;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.NOTIN;
import static org.xcsp.common.Types.TypeExpr.SET;
import static org.xcsp.common.Types.TypeExpr.SQR;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.INTENSION_TO_EXTENSION_ARITY_LIMIT;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.INTENSION_TO_EXTENSION_SPACE_LIMIT;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_LOGIC_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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

public class CtrLoaderInteger {
	private XCallbacks xc;

	public CtrLoaderInteger(XCallbacks xc) {
		this.xc = xc;
	}

	/** Constant to control the maximum number of values in a domain */
	public static final int NB_MAX_VALUES = 10000000;

	int[] trIntegers(Object value) {
		if (value instanceof int[])
			return (int[]) value;
		if (value instanceof IntegerEntity[]) {
			int[] values = IntegerEntity.toIntArray((IntegerEntity[]) value, NB_MAX_VALUES);
			Utilities.control(values != null, "Too many values. You have to extend the parser.");
			return values;
		}
		// Note that STAR is not allowed in simple lists (because this is irrelevant), which allows us to write:
		return IntStream.range(0, Array.getLength(value)).map(i -> trInteger((long) Array.get(value, i))).toArray();
	}

	int trInteger(Long l) {
		return Utilities.safeLong2Int(l, true);
	}

	private int[][] build(int size1, int size2, BiFunction<Integer, Integer, Integer> f) {
		return IntStream.range(0, size1).mapToObj(i -> IntStream.range(0, size2).map(j -> f.apply(i, j)).toArray()).toArray(int[][]::new);
	}

	int[][] trIntegers2D(Object value) {
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
		case binPacking: // not in XCSP3-core (and should never enter)
			binPacking(c);
			break;
		default:
			xc.unimplementedCase(c);
		}
	}

	private boolean intensionToExtension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> root, boolean firstCall) {
		if (firstCall && xc.implem().currParameters.get(INTENSION_TO_EXTENSION_PRIORITY) == Boolean.FALSE)
			return false;
		if (!firstCall && xc.implem().currParameters.get(INTENSION_TO_EXTENSION_PRIORITY) == Boolean.TRUE)
			return false;
		if (scope.length > ((Number) xc.implem().currParameters.get(INTENSION_TO_EXTENSION_ARITY_LIMIT)).intValue())
			return false;
		long[] domSizes = Stream.of(scope).mapToLong(x -> IntegerEntity.getNbValues((IntegerEntity[]) ((XDomInteger) x.dom).values)).toArray();
		if (LongStream.of(domSizes).anyMatch(l -> l == -1L || l > 1000000))
			return false;
		int spaceLimit = ((Number) xc.implem().currParameters.get(INTENSION_TO_EXTENSION_SPACE_LIMIT)).intValue();
		long product = 1;
		for (long l : domSizes)
			if ((product *= l) > spaceLimit)
				return false;
		int[][] domValues = Stream.of(scope).map(x -> IntegerEntity.toIntArray((IntegerEntity[]) ((XDomInteger) x.dom).values, 1000000)).toArray(int[][]::new);

		EvaluationManager man = new EvaluationManager(root);
		List<int[]> list = new ArrayList<>();
		int[] tupleIdx = new int[scope.length], tupleVal = new int[scope.length];
		boolean hasNext = true;
		while (hasNext) {
			for (int i = 0; i < scope.length; i++)
				tupleVal[i] = domValues[i][tupleIdx[i]];
			if (man.evaluate(tupleVal) == 1)
				list.add(tupleVal.clone());
			hasNext = false;
			for (int i = 0; !hasNext && i < tupleIdx.length; i++)
				if (tupleIdx[i] + 1 < domValues[i].length) {
					tupleIdx[i]++;
					hasNext = true;
				} else
					tupleIdx[i] = 0;
		}

		if (list.size() == 0) { // special case because 0 tuple
			xc.buildCtrFalse(id, scope);
		} else if (scope.length == 1) // unary constraint
			xc.buildCtrExtension(id, scope[0], list.stream().mapToInt(t -> t[0]).toArray(), true, new HashSet<>());
		else
			xc.buildCtrExtension(id, scope, list.toArray(new int[0][]), true, new HashSet<>());
		return true;
	}

	private void post(String id, Runnable r) {
		Utilities.control(!xc.implem().postedRecognizedCtrs.contains(id), "Pb with the same constraint posted twice");
		xc.implem().postedRecognizedCtrs.add(id);
		r.run();
	}

	// Returns an arithmetic operator iff the tree has the form s0 <op> s1 with s0 of type t0, s1 of type t1 and <op> an arithmetic operator
	// in {+,-,*,/,%,||}.
	private TypeArithmeticOperator aropOn(XNode<?> node, TypeExpr t0, TypeExpr t1) {
		if (!node.type.isArithmeticOperator())
			return null;
		XNode<?>[] sons = ((XNodeParent<?>) node).sons;
		return sons.length == 2 && sons[0].type == t0 && sons[1].type == t1 ? TypeArithmeticOperator.valueOf(node.type.name()) : null;
	}

	private TypeConditionOperatorRel relopOn(XNode<?> node, TypeExpr t0, TypeExpr t1) {
		if (!node.type.isRelationalOperator())
			return null;
		XNode<?>[] sons = ((XNodeParent<?>) node).sons;
		return sons.length == 2 && sons[0].type == t0 && sons[1].type == t1 ? TypeConditionOperatorRel.valueOf(node.type.name()) : null;
	}

	private boolean recognizePrimitive(String id, int arity, XNodeParent<XVarInteger> root) {
		if (arity > 3 || root.sons.length != 2)
			return false;
		XNode<XVarInteger> son0 = root.sons[0], son1 = root.sons[1];

		if (arity == 1 && xc.implem().currParameters.containsKey(RECOGNIZE_UNARY_PRIMITIVES)) {
			if (root.type.isRelationalOperator()) {
				TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(root.type.name());
				if (son0.type == LONG) {
					if (son1.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son1.firstVar(), op.arithmeticInversion(), son0.firstVal()));
					else if (aropOn(son1, VAR, LONG) != null)
						post(id, () -> xc.buildCtrPrimitive(id, son1.firstVar(), aropOn(son1, VAR, LONG), son1.firstVal(), op.arithmeticInversion(),
								son0.firstVal()));
				} else if (son1.type == LONG) {
					if (son0.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), op, son1.firstVal()));
					else if (aropOn(son0, VAR, LONG) != null)
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), aropOn(son0, VAR, LONG), son0.firstVal(), op, son1.firstVal()));
				}
			} else if (root.type == IN || root.type == NOTIN) {
				if (son1.type == SET && Stream.of(((XNodeParent<?>) son1).sons).allMatch(s -> s.type == LONG)) {
					TypeConditionOperatorSet op = TypeConditionOperatorSet.valueOf(root.type.name());
					int[] t = Stream.of(((XNodeParent<?>) son1).sons).mapToInt(s -> s.firstVal()).toArray();
					if (son0.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), op, t));
					// else if (aropOnVarAnd.apply(son0,LONG) != null)
					// post(id, () ->xc.buildCtrPrimitive(c.id, son0.firstVar(), aropOnVarAnd.apply(son0,LONG), son0.firstVal(), op, t));
				}
			} else if (root.type == AND) {
				if ((son0.type == TypeExpr.LT || son0.type == TypeExpr.LE) && (son1.type == TypeExpr.LT || son1.type == TypeExpr.LE)) {
					if (relopOn(son0, VAR, LONG) != null && relopOn(son1, LONG, VAR) != null && son0.firstVar() == son1.firstVar()) {
						int min = son1.firstVal() + (son1.type == TypeExpr.LT ? 1 : 0), max = son0.firstVal() - (son0.type == TypeExpr.LT ? 1 : 0);
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), TypeConditionOperatorSet.IN, min, max));
					} else if (relopOn(son0, LONG, VAR) != null && relopOn(son1, VAR, LONG) != null && son0.firstVar() == son1.firstVar()) {
						int min = son0.firstVal() + (son0.type == TypeExpr.LT ? 1 : 0), max = son1.firstVal() - (son1.type == TypeExpr.LT ? 1 : 0);
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), TypeConditionOperatorSet.IN, min, max));
					}
				}
			}
		} else if (arity == 2 && xc.implem().currParameters.containsKey(RECOGNIZE_BINARY_PRIMITIVES)) {
			if (root.type.isRelationalOperator()) {
				TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(root.type.name());
				if (son0.type == VAR && son1.type == VAR)
					post(id, () -> xc.buildCtrPrimitive(id, son0.var(0), TypeArithmeticOperator.SUB, son1.var(0), op, 0));
				else if (son0.type == VAR || son0.type == LONG) {
					// TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(root.type.name()).arithmeticInversion();
					if (aropOn(son1, VAR, LONG) != null && son0.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son1.firstVar(), aropOn(son1, VAR, LONG), son1.firstVal(), op.arithmeticInversion(),
								son0.firstVar()));
					else if (aropOn(son1, VAR, VAR) != null && son0.type == LONG)
						post(id, () -> xc.buildCtrPrimitive(id, son1.var(0), aropOn(son1, VAR, VAR), son1.var(1), op.arithmeticInversion(), son0.firstVal()));
				} else if (son1.type == VAR || son1.type == LONG) {
					if (aropOn(son0, VAR, LONG) != null && son1.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son0.firstVar(), aropOn(son0, VAR, LONG), son0.firstVal(), op, son1.firstVar()));
					else if (aropOn(son0, VAR, VAR) != null && son1.type == LONG)
						post(id, () -> xc.buildCtrPrimitive(id, son0.var(0), aropOn(son0, VAR, VAR), son0.var(1), op, son1.firstVal()));
					else if (root.type == TypeExpr.EQ && (son0.type == ABS || son0.type == NEG || son0.type == SQR || son0.type == NOT)
							&& ((XNodeParent<?>) son0).sons[0].type == VAR && son1.type == VAR)
						post(id, () -> xc.buildCtrPrimitive(id, son1.firstVar(), TypeUnaryArithmeticOperator.valueOf(son0.type.name()), son0.firstVar()));
				}
			}
		} else if (arity == 3 && xc.implem().currParameters.containsKey(RECOGNIZE_TERNARY_PRIMITIVES)) {
			if (root.type.isRelationalOperator()) {
				TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(root.type.name());
				if (aropOn(son0, VAR, VAR) != null && son1.type == VAR)
					post(id, () -> xc.buildCtrPrimitive(id, son0.var(0), aropOn(son0, VAR, VAR), son0.var(1), op, son1.var(0)));
				else if (aropOn(son1, VAR, VAR) != null && son0.type == VAR)
					post(id, () -> xc.buildCtrPrimitive(id, son1.var(0), aropOn(son1, VAR, VAR), son1.var(1), op.arithmeticInversion(), son0.var(0)));
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private boolean recognizeLogic(String id, XNodeParent<XVarInteger> root) {
		if (xc.implem().currParameters.containsKey(RECOGNIZE_LOGIC_CASES)) {
			if (root.type.isLogicalOperator() && Stream.of(root.sons).allMatch(s -> s.type == VAR && s.firstVar().isZeroOne())) {
				XVarInteger[] vars = Stream.of(root.sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
				Utilities.control(vars.length >= 2, "Bad construction for " + root);
				post(id, () -> xc.buildCtrLogic(id, TypeLogicalOperator.valueOf(root.type.name()), vars));
			} else if ((root.type == TypeExpr.EQ || root.type == NE) && root.sons.length == 2 && root.sons[0].type.isLogicalOperator()
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

	private boolean recognizeExtremum(String id, XNodeParent<XVarInteger> root) {
		if (xc.implem().currParameters.containsKey(RECOGNIZE_EXTREMUM_CASES)) {
			if (root.type.isRelationalOperator() && root.sons.length == 2 && (root.sons[1].type == VAR || root.sons[1].type == LONG)) {
				if ((root.sons[0].type == MIN || root.sons[0].type == MAX) && Stream.of(((XNodeParent<?>) root.sons[0]).sons).allMatch(s -> s.type == VAR)) {
					XVarInteger[] vars = Stream.of(((XNodeParent<?>) root.sons[0]).sons).map(s -> s.firstVar()).toArray(XVarInteger[]::new);
					TypeConditionOperatorRel op = TypeConditionOperatorRel.valueOf(root.type.name());
					Condition cond = root.sons[1].type == VAR ? new ConditionVar(op, root.sons[1].firstVar()) : new ConditionVal(op, root.sons[1].firstVal());
					if (root.sons[0].type == MIN)
						post(id, () -> xc.buildCtrMinimum(id, vars, cond));
					else
						post(id, () -> xc.buildCtrMaximum(id, vars, cond));

				}
			}
		}
		return xc.implem().postedRecognizedCtrs.contains(id);
	}

	private void intension(XCtr c) {
		// System.out.println("\nROOT1= " + (XNodeParent<?>) c.childs[0].value + "\nROOT2= " + ((XNodeParent<?>)
		// c.childs[0].value).canonization());
		XNodeParent<XVarInteger> root = (XNodeParent<XVarInteger>) ((XNode<XVarInteger>) c.childs[0].value).canonization();
		XVarInteger[] scope = Stream.of(root.vars()).map(x -> (XVarInteger) x).toArray(XVarInteger[]::new); // important: scope to be built
																											// from canonized root

		if (intensionToExtension(c.id, scope, root, true))
			return;
		if (recognizePrimitive(c.id, scope.length, root))
			return;
		if (recognizeLogic(c.id, root))
			return;
		if (recognizeExtremum(c.id, root))
			return;
		if (intensionToExtension(c.id, scope, root, false))
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