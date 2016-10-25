package org.xcsp.parser.loaders;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionRel;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Constants;
import org.xcsp.common.Types;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeBinaryLogicOperator;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeRank;
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

	int[][] trIntegers2D(Object value) {
		if (value instanceof int[][])
			return (int[][]) value;
		if (value instanceof byte[][]) {
			byte[][] m = (byte[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == Constants.STAR_BYTE ? Constants.STAR_INT : m[i][j];
			return tuples;
		}
		if (value instanceof short[][]) {
			short[][] m = (short[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == Constants.STAR_SHORT ? Constants.STAR_INT : m[i][j];
			return tuples;
		}
		if (value instanceof long[][]) {
			long[][] m = (long[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == Constants.STAR ? Constants.STAR_INT : trInteger(m[i][j]);
			return tuples;
		}
		if (value instanceof Long[][]) {
			Long[][] m = (Long[][]) value;
			int[][] tuples = new int[m.length][m[0].length];
			for (int i = 0; i < tuples.length; i++)
				for (int j = 0; j < tuples[i].length; j++)
					tuples[i][j] = m[i][j] == Constants.STAR ? Constants.STAR_INT : trInteger(m[i][j]);
			return tuples;
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
		case stretch:
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
		case clause: // not in XCSP3-core
			clause(c);
			break;
		case circuit: // not in XCSP3-core for the moment
			circuit(c);
			break;
		default:
			xc.unimplementedCase(c);
		}
	}

	private void unaryPrimitive(String id, XNode<? extends XVar> sonLeft, XNode<? extends XVar> sonRight, TypeConditionOperatorRel op) {
		XVarInteger x = (XVarInteger) ((XNodeLeaf<? extends XVar>) sonLeft).value;
		int k = Utilities.safeLong2Int((Long) ((XNodeLeaf<? extends XVar>) sonRight).value, true);
		xc.buildCtrPrimitive(id, x, op, k);
	}

	private void binaryPrimitive(String id, XNode<? extends XVar> sonLeft, XNode<? extends XVar> sonRight, TypeArithmeticOperator opa,
			TypeConditionOperatorRel op) {
		XVarInteger x = (XVarInteger) ((XNodeLeaf<? extends XVar>) ((XNodeParent<? extends XVar>) sonLeft).sons[0]).value;
		XVarInteger y = (XVarInteger) ((XNodeLeaf<? extends XVar>) ((XNodeParent<? extends XVar>) sonLeft).sons[1]).value;
		int k = Utilities.safeLong2Int((Long) ((XNodeLeaf<? extends XVar>) sonRight).value, true);
		xc.buildCtrPrimitive(id, x, opa, y, op, k);
	}

	private void ternaryPrimitive(String id, XNode<? extends XVar> sonLeft, XNode<? extends XVar> sonRight, TypeArithmeticOperator opa,
			TypeConditionOperatorRel op) {
		XVarInteger x = (XVarInteger) ((XNodeLeaf<? extends XVar>) ((XNodeParent<? extends XVar>) sonLeft).sons[0]).value;
		XVarInteger y = (XVarInteger) ((XNodeLeaf<? extends XVar>) ((XNodeParent<? extends XVar>) sonLeft).sons[1]).value;
		XVarInteger z = (XVarInteger) ((XNodeLeaf<? extends XVar>) sonRight).value;
		xc.buildCtrPrimitive(id, x, opa, y, op, z);
	}

	private boolean intensionToExtension(XCtr c, XVarInteger[] scope, boolean firstCall) {
		if (firstCall && xc.implem().currentParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY) == Boolean.FALSE)
			return false;
		if (!firstCall && xc.implem().currentParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY) == Boolean.TRUE)
			return false;
		if (scope.length > ((Number) xc.implem().currentParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_ARITY_LIMIT)).intValue())
			return false;
		long[] domSizes = Stream.of(scope).mapToLong(x -> IntegerEntity.getNbValues((IntegerEntity[]) ((XDomInteger) x.dom).values)).toArray();
		if (LongStream.of(domSizes).anyMatch(l -> l == -1L || l > 1000000))
			return false;
		int spaceLimit = ((Number) xc.implem().currentParameters.get(XCallbacksParameters.INTENSION_TO_EXTENSION_SPACE_LIMIT)).intValue();
		long product = 1;
		for (long l : domSizes)
			if ((product *= l) > spaceLimit)
				return false;
		int[][] domValues = Stream.of(scope).map(x -> IntegerEntity.toIntArray((IntegerEntity[]) ((XDomInteger) x.dom).values, 1000000)).toArray(int[][]::new);

		XNodeParent<XVarInteger> root = (XNodeParent<XVarInteger>) c.childs[0].value;
		EvaluationManager man = new EvaluationManager(root); // root.canonicalForm(new ArrayList<>(), scope).toArray(new String[0]));
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
			xc.buildCtrFalse(c.id, c.vars());
		} else {
			if (scope.length == 1) // unary constraint
				xc.buildCtrExtension(c.id, scope[0], list.stream().mapToInt(t -> t[0]).toArray(), true, new HashSet<>());
			else
				xc.buildCtrExtension(c.id, scope, list.toArray(new int[0][]), true, new HashSet<>());
		}
		return true;
	}

	private void intension(XCtr c) {
		// Returns true iff the tree has the form x, x + k, k + x or x - k, with x a variable and k a (long) integer.
		Predicate<XNode<XVarInteger>> basicForm = (XNode<XVarInteger> node) -> {
			if (node.type == TypeExpr.VAR)
				return true;
			if (node.type == TypeExpr.ADD || node.type == TypeExpr.SUB) {
				XNode<XVarInteger>[] sons = (XNode<XVarInteger>[]) ((XNodeParent<XVarInteger>) node).sons;
				return sons.length == 2 && ((sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG)
						|| (node.type == TypeExpr.ADD && sons[0].type == TypeExpr.LONG && sons[1].type == TypeExpr.VAR));
			}
			return false;
		};
		// Returns an arithmetic operator iff the tree has the form x <aop> y with <aop> an arithmetic operator in {+,-,*,/,%,dist}.
		Function<XNode<XVarInteger>, TypeArithmeticOperator> aopOnTwoVars = (XNode<XVarInteger> node) -> {
			if (node instanceof XNodeLeaf)
				return null;
			XNode<XVarInteger>[] sons = (XNode<XVarInteger>[]) ((XNodeParent<XVarInteger>) node).sons;
			TypeArithmeticOperator op = Types.valueOf(TypeArithmeticOperator.class, node.type.name());
			return op != null && sons.length == 2 && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR ? op : null;
		};
		// Returns a binary logic operator iff the tree has the form x <lop> y with <lop> a logic operator in {and,or,xor,iff,imp}.
		Function<XNode<XVarInteger>, TypeBinaryLogicOperator> lopOnTwoVars = (XNode<XVarInteger> node) -> {
			if (node instanceof XNodeLeaf)
				return null;
			XNode<XVarInteger>[] sons = (XNode<XVarInteger>[]) ((XNodeParent<XVarInteger>) node).sons;
			TypeBinaryLogicOperator op = Types.valueOf(TypeBinaryLogicOperator.class, node.type.name());
			return op != null && sons.length == 2 && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR ? op : null;
		};

		XVarInteger[] scope = Stream.of(c.vars()).map(x -> (XVarInteger) x).toArray(XVarInteger[]::new);
		if (intensionToExtension(c, scope, true))
			return;
		XNodeParent<XVarInteger> root = (XNodeParent<XVarInteger>) c.childs[0].value;
		if (root.sons.length == 2) {
			XNode<XVarInteger> son0 = root.sons[0], son1 = root.sons[1];
			if (scope.length == 1 && xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES)) {
				TypeConditionOperatorRel op = Types.valueOf(TypeConditionOperatorRel.class, root.type.name());
				if (op != null) {
					if (son0.type == TypeExpr.VAR && son1.type == TypeExpr.LONG) {
						unaryPrimitive(c.id, son0, son1, op);
						return;
					} else if (son1.type == TypeExpr.VAR && son0.type == TypeExpr.LONG) {
						unaryPrimitive(c.id, son1, son0, op.reverseForSwap());
						return;
					}
				}
			}
			if (scope.length == 2 && xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES)) {
				TypeConditionOperatorRel op = Types.valueOf(TypeConditionOperatorRel.class, root.type.name());
				if (op != null) {
					if (basicForm.test(son0) && basicForm.test(son1)) {
						XVarInteger x = (XVarInteger) son0.getValueOfFirstLeafOfType(TypeExpr.VAR);
						XVarInteger y = (XVarInteger) son1.getValueOfFirstLeafOfType(TypeExpr.VAR);
						Long l1 = (Long) son0.getValueOfFirstLeafOfType(TypeExpr.LONG);
						Long l2 = (Long) son1.getValueOfFirstLeafOfType(TypeExpr.LONG);
						int k = (l2 == null ? 0 : Utilities.safeLong2Int(l2, true) * (son1.type == TypeExpr.SUB ? -1 : 1))
								- (l1 == null ? 0 : Utilities.safeLong2Int(l1, true) * (son0.type == TypeExpr.SUB ? -1 : 1));
						xc.buildCtrPrimitive(c.id, x, TypeArithmeticOperator.SUB, y, op, k);
						return;
					} else {
						if (aopOnTwoVars.apply(son0) != null && son1.type == TypeExpr.LONG) {
							binaryPrimitive(c.id, son0, son1, aopOnTwoVars.apply(son0), op);
							return;
						} else if (aopOnTwoVars.apply(son1) != null && son0.type == TypeExpr.LONG) {
							binaryPrimitive(c.id, son1, son0, aopOnTwoVars.apply(son1), op.reverseForSwap());
							return;
						}
					}
				}
			}
			if (scope.length == 2 && xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_BINARY_LOGIC_INTENSION_CASES)) {
				TypeBinaryLogicOperator op = lopOnTwoVars.apply(root);
				if (op != null) {
					xc.buildCtrPrimitive(c.id, (XVarInteger) son0.getValueOfFirstLeafOfType(TypeExpr.VAR), op,
							(XVarInteger) son1.getValueOfFirstLeafOfType(TypeExpr.VAR));
				}
			}
			if (scope.length == 3 && xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES)) {
				TypeConditionOperatorRel op = Types.valueOf(TypeConditionOperatorRel.class, root.type.name());
				if (op != null) {
					if (aopOnTwoVars.apply(son0) != null && son1.type == TypeExpr.VAR) {
						ternaryPrimitive(c.id, son0, son1, aopOnTwoVars.apply(son0), op);
						return;
					} else if (aopOnTwoVars.apply(son1) != null && son0.type == TypeExpr.VAR) {
						ternaryPrimitive(c.id, son1, son0, aopOnTwoVars.apply(son1), op.reverseForSwap());
						return;
					}
				}
			}
		}
		if (intensionToExtension(c, scope, false))
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
				xc.buildCtrOrdered(c.id, (XVarInteger[]) c.childs[0].value, (TypeOperator) c.childs[1].value);
			else
				xc.unimplementedCase(c);
		else
			xc.unimplementedCase(c);
	}

	private void lex(XCtr c) {
		TypeOperator op = (TypeOperator) c.childs[c.childs.length - 1].value;
		if (c.childs[0].type == TypeChild.matrix)
			xc.buildCtrLexMatrix(c.id, (XVarInteger[][]) c.childs[0].value, op);
		else {
			Utilities.control(!op.isSet(), "Lex on sets and msets currently not implemented");
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

	private void count(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[2].value;
		if (c.childs[1].value instanceof Long[] && condition instanceof ConditionRel) {
			TypeConditionOperatorRel op = ((ConditionRel) condition).operator;
			Long[] values = (Long[]) c.childs[1].value;
			if (xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_COUNT_CASES)) {
				if (values.length == 1) {
					if (condition instanceof ConditionVal) {
						if (op == TypeConditionOperatorRel.LT) {
							xc.buildCtrAtMost(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k - 1);
							return;
						}
						if (op == TypeConditionOperatorRel.LE) {
							xc.buildCtrAtMost(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
							return;
						}
						if (op == TypeConditionOperatorRel.GE) {
							xc.buildCtrAtLeast(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
							return;
						}
						if (op == TypeConditionOperatorRel.GT) {
							xc.buildCtrAtLeast(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k + 1);
							return;
						}
						if (op == TypeConditionOperatorRel.EQ) {
							xc.buildCtrExactly(c.id, list, trInteger(values[0]), ((ConditionVal) condition).k);
							return;
						}
					} else if (condition instanceof ConditionVar) {
						if (op == TypeConditionOperatorRel.EQ) {
							xc.buildCtrExactly(c.id, list, trInteger(values[0]), (XVarInteger) ((ConditionVar) condition).x);
							return;
						}
					}
				} else {
					if (op == TypeConditionOperatorRel.EQ) {
						if (condition instanceof ConditionVal) {
							xc.buildCtrAmong(c.id, list, trIntegers(values), ((ConditionVal) condition).k);
							return;
						} else if (condition instanceof ConditionVar) {
							xc.buildCtrAmong(c.id, list, trIntegers(values), (XVarInteger) ((ConditionVar) condition).x);
							return;
						}
					}
				}
			}
			xc.buildCtrCount(c.id, list, trIntegers(c.childs[1].value), condition);
		} else
			xc.buildCtrCount(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
	}

	private void nValues(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[c.childs.length - 1].value;
		if (xc.implem().currentParameters.containsKey(XCallbacksParameters.RECOGNIZE_SPECIAL_NVALUES_CASES) && c.childs.length == 2
				&& condition instanceof ConditionVal) {
			TypeConditionOperatorRel op = ((ConditionRel) condition).operator;
			if (op == TypeConditionOperatorRel.EQ && ((ConditionVal) condition).k == list.length) {
				xc.buildCtrAllDifferent(c.id, list);
				return;
			} else if (op == TypeConditionOperatorRel.EQ && ((ConditionVal) condition).k == 1) {
				xc.buildCtrAllEqual(c.id, list);
				return;
			} else if ((op == TypeConditionOperatorRel.GE && ((ConditionVal) condition).k == 2)
					|| (op == TypeConditionOperatorRel.GT && ((ConditionVal) condition).k == 1)) {
				xc.buildCtrNotAllEqual(c.id, list);
				return;
			}
		}
		if (c.childs.length == 2)
			xc.buildCtrNValues(c.id, list, condition);
		else
			xc.buildCtrNValuesExcept(c.id, list, trIntegers(c.childs[1].value), condition);
	}

	private void cardinality(XCtr c) {
		CChild[] childs = c.childs;
		Utilities.control(childs[1].value instanceof Long[], "unimplemented case");
		boolean closed = childs[1].getAttributeValue(TypeAtt.closed, true);
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
}