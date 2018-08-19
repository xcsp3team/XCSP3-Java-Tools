package org.xcsp.parser.loaders;

import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZING_BEFORE_CONVERTING;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionRel;
import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.Utilities.ModifiableBoolean;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.common.predicates.EvaluationManager;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.callbacks.XCallbacks;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * This class allows us to load integer constraints, at parsing time.
 * 
 * @author Christophe Lecoutre -- {@literal lecoutre@cril.fr}
 *
 */
public class CtrLoaderInteger {

	// ************************************************************************
	// ***** Static fields and methods
	// ************************************************************************

	/**
	 * Constant used to control the maximum allowed number of values in the domain of a variable.
	 */
	public static final int N_MAX_VALUES = 10000000;

	/**
	 * Transforms the specified {@code long} into an {@code int} (while controlling that no information is lost).
	 * 
	 * @param l
	 *            a specified long integer
	 * @return an {@code int} corresponding to the specified {@code long}
	 */
	static int trInteger(Long l) {
		return Utilities.safeLong2Int(l, true);
	}

	/**
	 * Transforms the specified object into a 1-dimensional array of integers
	 * 
	 * @param value
	 *            an object denoting a sequence of integers
	 * @return a 1-dimensional array of integers
	 */
	static int[] trIntegers(Object value) {
		if (value instanceof int[])
			return (int[]) value;
		if (value instanceof IntegerEntity[]) {
			int[] values = IntegerEntity.toIntArray((IntegerEntity[]) value, N_MAX_VALUES);
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
	static int[][] build(int size1, int size2, BiFunction<Integer, Integer, Integer> f) {
		return IntStream.range(0, size1).mapToObj(i -> IntStream.range(0, size2).map(j -> f.apply(i, j)).toArray()).toArray(int[][]::new);
	}

	/**
	 * Transforms the specified object into a 2-dimensional array of integers
	 * 
	 * @param value
	 *            an object denoting a 2-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	static int[][] trIntegers2D(Object value) {
		if (value instanceof int[][])
			return (int[][]) value;
		if (value instanceof byte[][]) {
			byte[][] m = (byte[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_BYTE ? Constants.STAR : m[i][j]);
		}
		if (value instanceof short[][]) {
			short[][] m = (short[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_SHORT ? Constants.STAR : m[i][j]);
		}
		if (value instanceof long[][]) {
			long[][] m = (long[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_LONG ? Constants.STAR : trInteger(m[i][j]));
		}
		if (value instanceof Long[][]) {
			Long[][] m = (Long[][]) value;
			return build(m.length, m[0].length, (i, j) -> m[i][j] == Constants.STAR_LONG ? Constants.STAR : trInteger(m[i][j]));
		}
		return (int[][]) Utilities.exit(value + " was expected to be an object denoting a 2-dimensional array of integers");
	}

	// ************************************************************************
	// ***** Methods for loading integer constraints
	// ************************************************************************

	/**
	 * A reference to the main parsing object, responsible for dealing with callback functions.
	 */
	private XCallbacks xc;

	/**
	 * The object used to recognize special forms (primitive, sum, count, ...) forms of intension constraints.
	 */
	private ConstraintRecognizer recognizer;

	/**
	 * Builds an object that can be used to load integer constraints.
	 * 
	 * @param xc
	 *            the main parsing object, responsible for dealing with callback functions
	 */
	public CtrLoaderInteger(XCallbacks xc) {
		this.xc = xc;
		this.recognizer = new ConstraintRecognizer(xc);
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
		case stretch: // no more in XCSP3-core
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
		case circuit: // now in XCSP3-core
			circuit(c);
			break;
		case binPacking: // not in XCSP3-core (but could enter)
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
		int[][] domValues = Stream.of(scp).map(x -> IntegerEntity.toIntArray((IntegerEntity[]) ((Dom) x.dom).values, Integer.MAX_VALUE)).toArray(int[][]::new);
		ModifiableBoolean b = new ModifiableBoolean(null); // later, maybe a control parameter
		int[][] tuples = new EvaluationManager(root).generateTuples(domValues, b);
		assert b.value != null;
		if (tuples.length == 0) { // special case because 0 tuple
			if (b.value)
				xc.buildCtrFalse(id, scp);
			else
				xc.buildCtrTrue(id, scp);
		} else if (scp.length == 1) // unary constraint
			xc.buildCtrExtension(id, scp[0], Stream.of(tuples).mapToInt(t -> t[0]).toArray(), b.value, new HashSet<>());
		else
			xc.buildCtrExtension(id, scp, tuples, b.value, new HashSet<>());
		return true;
	}

	private void intension(XCtr c) {
		// System.out.println("\nROOT1= " + c.childs[0].value + "\nROOT2= " + ((XNodeParent<?>) c.childs[0].value).canonization());
		XNode<XVarInteger> r = ((XNode<XVarInteger>) c.childs[0].value).canonization(); // we first canonize the predicate
		if (r.type == TypeExpr.LONG) {
			Utilities.control(r.val(0) == 0 || r.val(0) == 1, "Bad form of the predicate obtained after canonization");
			if (r.val(0) == 0)
				xc.buildCtrFalse(c.id, c.vars());
			else
				xc.buildCtrTrue(c.id, c.vars());
			return;
		}
		XNodeParent<XVarInteger> root = (XNodeParent<XVarInteger>) r;
		XVarInteger[] scope = root.vars();
		if (xc.implem().currParameters.get(RECOGNIZING_BEFORE_CONVERTING) == Boolean.FALSE) // we try first converting into extension
			if (intensionToExtension(c.id, scope, root))
				return;
		if (recognizer.specificIntensionCases(c.id, root, scope.length)) // we try to recognize special forms of intension constraints
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
				int[][] tuples = xc.implem().cache4Tuples.computeIfAbsent(c1.value, k -> trIntegers2D(c1.value));
				// if (tuples == null)
				// xc.implem().cache4Tuples.put(c1.value, tuples = trIntegers2D(c1.value));
				// control to insert later below ?
				// for (int i = 0; i < tuples.length - 1; i++) if (Utilities.lexComparatorInt.compare(tuples[i], tuples[i + 1]) >= 0) {
				// System.out.println("\nSAME " + c + " " + Utilities.join(tuples[i]) + " " + Utilities.join(tuples[i + 1]) + " (" + i + ")");
				// System.exit(1); }
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
		if (c.childs[0].value instanceof XNodeParent[]) {
			Utilities.control(childs.length == 1 && childs[0].type == TypeChild.list, "Other forms not implemented");
			XNodeParent<XVarInteger>[] trees = ((XNodeParent<XVarInteger>[]) c.childs[0].value);
			xc.buildCtrAllDifferent(c.id, trees);
		} else if (childs[0].type == TypeChild.matrix) {
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
			else if (c.childs.length == 3 && c.childs[1].type == TypeChild.lengths) {
				if (c.childs[1].value instanceof XVarInteger[])
					xc.buildCtrOrdered(c.id, (XVarInteger[]) c.childs[0].value, (XVarInteger[]) c.childs[1].value, ((TypeOperator) c.childs[2].value).toRel());
				else
					xc.buildCtrOrdered(c.id, (XVarInteger[]) c.childs[0].value, trIntegers(c.childs[1].value), ((TypeOperator) c.childs[2].value).toRel());
			} else
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
		Condition condition = (Condition) c.childs[c.childs.length - 1].value;
		if (c.childs[0].value instanceof XNodeParent[]) {
			XNodeParent<XVarInteger>[] trees = ((XNodeParent<XVarInteger>[]) c.childs[0].value);
			xc.buildCtrSum(c.id, trees, trIntegers(c.childs[1].value), condition);
		} else if (c.childs[0].value instanceof XVarInteger[]) {
			XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
			if (c.childs.length == 2)
				xc.buildCtrSum(c.id, list, condition);
			else if (c.childs[1].value instanceof XVarInteger[])
				xc.buildCtrSum(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
			else
				xc.buildCtrSum(c.id, list, trIntegers(c.childs[1].value), condition);
		} else
			xc.unimplementedCase();
	}

	private void count(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[2].value;
		if (c.childs[1].value instanceof Long[] && condition instanceof ConditionRel) {
			TypeConditionOperatorRel op = ((ConditionRel) condition).operator;
			Long[] values = (Long[]) c.childs[1].value;
			if (recognizer.specificCountCases(c.id, list, values, op, condition))
				return;
			xc.buildCtrCount(c.id, list, trIntegers(c.childs[1].value), condition);
		} else
			xc.buildCtrCount(c.id, list, (XVarInteger[]) c.childs[1].value, condition);
	}

	private void nValues(XCtr c) {
		XVarInteger[] list = (XVarInteger[]) c.childs[0].value;
		Condition condition = (Condition) c.childs[c.childs.length - 1].value;
		if (c.childs.length == 2 && recognizer.specificNvaluesCases(c.id, list, condition))
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