package org.xcsp.parser.loaders;

import static org.xcsp.common.Types.TypeConditionOperatorRel.EQ;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.GT;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LE;
import static org.xcsp.common.Types.TypeConditionOperatorRel.LT;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.common.predicates.MatcherInterface.add_mul_vals__relop;
import static org.xcsp.common.predicates.MatcherInterface.add_mul_vars__relop;
import static org.xcsp.common.predicates.MatcherInterface.add_vars__relop;
import static org.xcsp.common.predicates.MatcherInterface.k_relop__x_ariop_y;
import static org.xcsp.common.predicates.MatcherInterface.k_relop_x;
import static org.xcsp.common.predicates.MatcherInterface.l_relop__x_ariop_k;
import static org.xcsp.common.predicates.MatcherInterface.logic_X;
import static org.xcsp.common.predicates.MatcherInterface.logic_X__eq_x;
import static org.xcsp.common.predicates.MatcherInterface.logic_X__ne_x;
import static org.xcsp.common.predicates.MatcherInterface.max_relop;
import static org.xcsp.common.predicates.MatcherInterface.min_relop;
import static org.xcsp.common.predicates.MatcherInterface.unaop_x__eq_y;
import static org.xcsp.common.predicates.MatcherInterface.x_ariop_k__relop_l;
import static org.xcsp.common.predicates.MatcherInterface.x_ariop_k__relop_y;
import static org.xcsp.common.predicates.MatcherInterface.x_ariop_y__relop_k;
import static org.xcsp.common.predicates.MatcherInterface.x_ariop_y__relop_z;
import static org.xcsp.common.predicates.MatcherInterface.x_in_intvl;
import static org.xcsp.common.predicates.MatcherInterface.x_notin_intvl;
import static org.xcsp.common.predicates.MatcherInterface.x_relop__y_ariop_k;
import static org.xcsp.common.predicates.MatcherInterface.x_relop__y_ariop_z;
import static org.xcsp.common.predicates.MatcherInterface.x_relop_k;
import static org.xcsp.common.predicates.MatcherInterface.x_relop_y;
import static org.xcsp.common.predicates.MatcherInterface.x_setop_S;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_LOGIC_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_SUM_CASES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES;
import static org.xcsp.parser.XCallbacks.XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES;
import static org.xcsp.parser.loaders.CtrLoaderInteger.trInteger;
import static org.xcsp.parser.loaders.CtrLoaderInteger.trIntegers;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeEqNeOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.MatcherInterface.Matcher;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks;
import org.xcsp.parser.XCallbacks.XCallbacksParameters;
import org.xcsp.parser.entries.XVariables.XVarInteger;

class ConstraintRecognizer {

	private XCallbacks xc;

	// The following maps are useful for dealing with intension constraints
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> unaryMap = new HashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> binaryMap = new HashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> ternaryMap = new HashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> logicMap = new HashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> sumMap = new HashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> extremumMap = new HashMap<>();

	private Condition basicCondition(XNodeParent<XVarInteger> r) {
		if (r.type.isRelationalOperator() && r.sons.length == 2 && r.sons[1].type.oneOf(VAR, LONG))
			return r.sons[1].type == VAR ? new ConditionVar(r.relop(0), r.sons[1].var(0)) : new ConditionVal(r.relop(0), r.sons[1].val(0));
		return null;
	}

	ConstraintRecognizer(XCallbacks xc) {
		this.xc = xc;
		unaryMap.put(x_relop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.relop(0), r.val(0)));
		unaryMap.put(k_relop_x, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.relop(0).arithmeticInversion(), r.val(0)));
		unaryMap.put(x_ariop_k__relop_l, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(0), r.relop(0), r.val(1)));
		unaryMap.put(l_relop__x_ariop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(1), r.relop(0).arithmeticInversion(), r.val(0)));
		unaryMap.put(x_setop_S, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.type.toSetop(), r.arrayOfVals()));
		unaryMap.put(x_in_intvl, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeConditionOperatorSet.IN, r.val(1), r.val(0)));
		unaryMap.put(x_notin_intvl, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeConditionOperatorSet.NOTIN, r.val(0) + 1, r.val(1) - 1));
		binaryMap.put(x_relop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeArithmeticOperator.SUB, r.var(1), r.relop(0), 0));
		binaryMap.put(x_ariop_k__relop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(0), r.relop(0), r.var(1)));
		binaryMap.put(x_ariop_y__relop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0), r.val(0)));
		binaryMap.put(x_relop__y_ariop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.ariop(0), r.val(0), r.relop(0).arithmeticInversion(), r.var(0)));
		binaryMap.put(k_relop__x_ariop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0).arithmeticInversion(), r.val(0)));
		binaryMap.put(unaop_x__eq_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.sons[0].type.toUnaryAriop(), r.var(0)));
		ternaryMap.put(x_ariop_y__relop_z, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0), r.var(2)));
		ternaryMap.put(x_relop__y_ariop_z, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.ariop(0), r.var(2), r.relop(0).arithmeticInversion(), r.var(0)));
		logicMap.put(logic_X, (id, r) -> xc.buildCtrLogic(id, r.type.toLogop(), r.arrayOfVars()));
		logicMap.put(logic_X__eq_x, (id, r) -> xc.buildCtrLogic(id, r.sons[1].var(0), TypeEqNeOperator.EQ, r.sons[0].type.toLogop(), r.sons[0].arrayOfVars()));
		logicMap.put(logic_X__ne_x, (id, r) -> xc.buildCtrLogic(id, r.sons[1].var(0), TypeEqNeOperator.NE, r.sons[0].type.toLogop(), r.sons[0].arrayOfVars()));
		sumMap.put(add_vars__relop, (id, r) -> xc.buildCtrSum(id, r.sons[0].arrayOfVars(), basicCondition(r)));
		sumMap.put(add_mul_vals__relop, (id, r) -> {
			int[] coeffs = Stream.of(r.sons[0].sons).mapToInt(s -> s.type == VAR ? 1 : s.val(0)).toArray();
			if (IntStream.of(coeffs).allMatch(v -> v == 1))
				xc.buildCtrSum(id, r.sons[0].arrayOfVars(), basicCondition(r));
			else
				xc.buildCtrSum(id, r.sons[0].arrayOfVars(), coeffs, basicCondition(r));
		});
		sumMap.put(add_mul_vars__relop, (id, r) -> {
			XVarInteger[] list = Stream.of(r.sons[0].sons).map(s -> s.var(0)).toArray(XVarInteger[]::new);
			XVarInteger[] coeffs = Stream.of(r.sons[0].sons).map(s -> s.var(1)).toArray(XVarInteger[]::new);
			xc.buildCtrSum(id, list, coeffs, basicCondition(r));
		});
		extremumMap.put(min_relop, (id, r) -> xc.buildCtrMinimum(id, r.sons[0].vars(), basicCondition(r)));
		extremumMap.put(max_relop, (id, r) -> xc.buildCtrMaximum(id, r.sons[0].vars(), basicCondition(r)));
	}

	private void posted(String id) {
		Utilities.control(!xc.implem().postedRecognizedCtrs.contains(id), "Pb with the same constraint posted twice");
		xc.implem().postedRecognizedCtrs.add(id);
	}

	/**
	 * Returns {@code true} if a target matcher from the specified array matches the specified tree. Matching is considered only if the specified
	 * condition evaluates to {@code true}.
	 * 
	 * @param id
	 *            the constraint id
	 * @param tree
	 *            the constraint predicate
	 * @param matchers
	 *            the targets for matching
	 * @param condition
	 *            when {@code true}, matching is considered
	 * @return {@code true} if a target matcher from the specified array matches the specified tree
	 */
	private boolean recognizeIntensionIn(String id, XNodeParent<XVarInteger> tree, Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> map,
			boolean condition) {
		return condition && map.entrySet().stream().anyMatch(e -> {
			if (!e.getKey().matches(tree))
				return false;
			// System.out.println("Rec " + e.getKey().target());
			e.getValue().accept(id, tree);
			posted(id);
			return true;
		});
	}

	private boolean recognizeIntension(String id, XNodeParent<XVarInteger> tree, int arity) {
		Map<XCallbacksParameters, Object> map = xc.implem().currParameters;
		if (recognizeIntensionIn(id, tree, unaryMap, arity == 1 && map.containsKey(RECOGNIZE_UNARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, binaryMap, arity == 2 && map.containsKey(RECOGNIZE_BINARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, ternaryMap, arity == 3 && map.containsKey(RECOGNIZE_TERNARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, logicMap, map.containsKey(RECOGNIZE_LOGIC_CASES)))
			return true;
		if (recognizeIntensionIn(id, tree, sumMap, map.containsKey(RECOGNIZE_SUM_CASES)))
			return true;
		if (recognizeIntensionIn(id, tree, extremumMap, map.containsKey(RECOGNIZE_EXTREMUM_CASES)))
			return true;
		return false;
	}

	/**
	 * Returns {@code true} if a specific constraint, such as a primitive, logic, sum or extremum (minimum, maximum) constraint matches the specified
	 * predicate. In that case, this specific constraint is posted. Note that a successful matching can be discarded when overriding callback
	 * functions by simply reposting the original constraint.
	 * 
	 * @param id
	 *            the constraint id
	 * @param tree
	 *            the constraint predicate
	 * @param arity
	 *            the constraint arity
	 * @return {@code true} if a specific constraint corresponds to the specified predicate
	 */
	public boolean specificIntensionCases(String id, XNodeParent<XVarInteger> tree, int arity) {
		recognizeIntension(id, tree, arity);
		return xc.implem().postedRecognizedCtrs.contains(id); // let as it is, because constraints may be reposted
	}

	private Runnable recognizeCount(String id, XVarInteger[] list, Long[] values, TypeConditionOperatorRel op, Condition condition) {
		if (xc.implem().currParameters.containsKey(XCallbacksParameters.RECOGNIZE_COUNT_CASES)) {
			if (values.length == 1) {
				int value = trInteger(values[0]);
				if (condition instanceof ConditionVal) {
					int k = trInteger(((ConditionVal) condition).k); // other controls on k ?
					if (op == LT)
						return () -> xc.buildCtrAtMost(id, list, value, k - 1);
					if (op == LE)
						return () -> xc.buildCtrAtMost(id, list, value, k);
					if (op == GE)
						return () -> xc.buildCtrAtLeast(id, list, value, k);
					if (op == GT)
						return () -> xc.buildCtrAtLeast(id, list, value, k + 1);
					if (op == EQ)
						return () -> xc.buildCtrExactly(id, list, value, k);
				} else if (condition instanceof ConditionVar) {
					if (op == EQ)
						return () -> xc.buildCtrExactly(id, list, value, (XVarInteger) ((ConditionVar) condition).x);
				}
			} else if (op == EQ) {
				if (condition instanceof ConditionVal)
					return () -> xc.buildCtrAmong(id, list, trIntegers(values), trInteger(((ConditionVal) condition).k));
				else if (condition instanceof ConditionVar)
					return () -> xc.buildCtrAmong(id, list, trIntegers(values), (XVarInteger) ((ConditionVar) condition).x);
			}
		}
		return null;
	}

	public boolean specificCountCases(String id, XVarInteger[] list, Long[] values, TypeConditionOperatorRel op, Condition condition) {
		Runnable recognized = recognizeCount(id, list, values, op, condition);
		if (recognized == null)
			return false;
		recognized.run();
		posted(id);
		return true;
	}

	private Runnable recognizeNvalues(String id, XVarInteger[] list, Condition condition) {
		if (xc.implem().currParameters.containsKey(XCallbacksParameters.RECOGNIZE_NVALUES_CASES) && condition instanceof ConditionVal) {
			TypeConditionOperatorRel op = ((ConditionVal) condition).operator;
			int k = trInteger(((ConditionVal) condition).k);
			if (op == EQ && k == list.length)
				return () -> xc.buildCtrAllDifferent(id, list);
			if (op == EQ && k == 1)
				return () -> xc.buildCtrAllEqual(id, list);
			if ((op == GE && k == 2) || (op == GT && k == 1))
				return () -> xc.buildCtrNotAllEqual(id, list);
		}
		return null;
	}

	public boolean specificNvaluesCases(String id, XVarInteger[] list, Condition condition) {
		Runnable recognized = recognizeNvalues(id, list, condition);
		if (recognized == null)
			return false;
		recognized.run();
		posted(id);
		return true;
	}
}
