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
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.NOTIN;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SQR;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.common.predicates.MatcherInterface.add_mul_vals;
import static org.xcsp.common.predicates.MatcherInterface.add_mul_vars;
import static org.xcsp.common.predicates.MatcherInterface.add_vars;
import static org.xcsp.common.predicates.MatcherInterface.logic_vars;
import static org.xcsp.common.predicates.MatcherInterface.max_vars;
import static org.xcsp.common.predicates.MatcherInterface.min_vars;
import static org.xcsp.common.predicates.MatcherInterface.set_vals;
import static org.xcsp.common.predicates.MatcherInterface.val;
import static org.xcsp.common.predicates.MatcherInterface.var;
import static org.xcsp.common.predicates.MatcherInterface.varOrVal;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.ariop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.relop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.setop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.unaop;
import static org.xcsp.common.predicates.XNode.node;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_LOGIC_CASES;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_SUM_CASES;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES;
import static org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES;
import static org.xcsp.parser.loaders.CtrLoaderInteger.trInteger;
import static org.xcsp.parser.loaders.CtrLoaderInteger.trIntegers;

import java.util.LinkedHashMap;
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
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.MatcherInterface.Matcher;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.callbacks.XCallbacks;
import org.xcsp.parser.callbacks.XCallbacks.XCallbacksParameters;
import org.xcsp.parser.entries.XVariables.XVarInteger;

class ConstraintRecognizer {

	private XCallbacks xc;

	private Matcher x_relop_k = new Matcher(node(relop, var, val));
	private Matcher k_relop_x = new Matcher(node(relop, val, var));
	private Matcher x_ariop_k__relop_l = new Matcher(node(relop, node(ariop, var, val), val));
	private Matcher l_relop__x_ariop_k = new Matcher(node(relop, val, node(ariop, var, val)));
	private Matcher x_setop_S = new Matcher(node(setop, var, set_vals), (node, level) -> level == 0 && node.type.oneOf(IN, NOTIN));
	private Matcher x_in_intvl = new Matcher(node(AND, node(TypeExpr.LE, var, val), node(TypeExpr.LE, val, var)));
	private Matcher x_notin_intvl = new Matcher(node(OR, node(TypeExpr.LE, var, val), node(TypeExpr.LE, val, var)));

	private Matcher x_relop_y = new Matcher(node(relop, var, var));
	private Matcher x_ariop_k__relop_y = new Matcher(node(relop, node(ariop, var, val), var));
	private Matcher x_ariop_y__relop_k = new Matcher(node(relop, node(ariop, var, var), val));
	private Matcher x_relop__y_ariop_k = new Matcher(node(relop, var, node(ariop, var, val)));
	private Matcher k_relop__x_ariop_y = new Matcher(node(relop, val, node(ariop, var, var)));
	private Matcher unaop_x__eq_y = new Matcher(node(TypeExpr.EQ, node(unaop, var), var), (node, level) -> level == 1 && node.type.oneOf(ABS, NEG, SQR, NOT));
	// unaop(x) = y with unaop in {abs,neg,sqr,not}

	private Matcher x_ariop_y__relop_z = new Matcher(node(relop, node(ariop, var, var), var));
	private Matcher x_relop__y_ariop_z = new Matcher(node(relop, var, node(ariop, var, var)));

	private Matcher logic_X = new Matcher(logic_vars);
	private Matcher logic_X__eq_x = new Matcher(node(TypeExpr.EQ, logic_vars, var));
	private Matcher logic_X__ne_x = new Matcher(node(TypeExpr.NE, logic_vars, var));

	private Matcher add_vars__relop = new Matcher(node(relop, add_vars, varOrVal));
	private Matcher add_mul_vals__relop = new Matcher(node(relop, add_mul_vals, varOrVal));
	private Matcher add_mul_vars__relop = new Matcher(node(relop, add_mul_vars, varOrVal));

	private Matcher min_relop = new Matcher(node(relop, min_vars, varOrVal));
	private Matcher max_relop = new Matcher(node(relop, max_vars, varOrVal));

	// The following maps are useful for dealing with intension constraints. We use LinkdHashMap because insertion order may be important
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> unaryRules = new LinkedHashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> binaryRules = new LinkedHashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> ternaryRules = new LinkedHashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> logicRules = new LinkedHashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> sumRules = new LinkedHashMap<>();
	private Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> extremumRules = new LinkedHashMap<>();

	private Condition basicCondition(XNodeParent<XVarInteger> r) {
		if (r.type.isRelationalOperator() && r.sons.length == 2 && r.sons[1].type.oneOf(VAR, LONG))
			return r.sons[1].type == VAR ? new ConditionVar(r.relop(0), r.sons[1].var(0)) : new ConditionVal(r.relop(0), r.sons[1].val(0));
		return null;
	}

	ConstraintRecognizer(XCallbacks xc) {
		this.xc = xc;
		unaryRules.put(x_relop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.relop(0), r.val(0)));
		unaryRules.put(k_relop_x, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.relop(0).arithmeticInversion(), r.val(0)));
		unaryRules.put(x_ariop_k__relop_l, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(0), r.relop(0), r.val(1)));
		unaryRules.put(l_relop__x_ariop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(1), r.relop(0).arithmeticInversion(), r.val(0)));
		unaryRules.put(x_setop_S, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.type.toSetop(), r.arrayOfVals()));
		unaryRules.put(x_in_intvl, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeConditionOperatorSet.IN, r.val(1), r.val(0)));
		unaryRules.put(x_notin_intvl, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeConditionOperatorSet.NOTIN, r.val(0) + 1, r.val(1) - 1));
		binaryRules.put(x_relop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), TypeArithmeticOperator.SUB, r.var(1), r.relop(0), 0));
		binaryRules.put(x_ariop_k__relop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.val(0), r.relop(0), r.var(1)));
		binaryRules.put(x_ariop_y__relop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0), r.val(0)));
		binaryRules.put(x_relop__y_ariop_k, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.ariop(0), r.val(0), r.relop(0).arithmeticInversion(), r.var(0)));
		binaryRules.put(k_relop__x_ariop_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0).arithmeticInversion(), r.val(0)));
		binaryRules.put(unaop_x__eq_y, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.sons[0].type.toUnaryAriop(), r.var(0)));
		ternaryRules.put(x_ariop_y__relop_z, (id, r) -> xc.buildCtrPrimitive(id, r.var(0), r.ariop(0), r.var(1), r.relop(0), r.var(2)));
		ternaryRules.put(x_relop__y_ariop_z, (id, r) -> xc.buildCtrPrimitive(id, r.var(1), r.ariop(0), r.var(2), r.relop(0).arithmeticInversion(), r.var(0)));
		logicRules.put(logic_X, (id, r) -> xc.buildCtrLogic(id, r.type.toLogop(), r.arrayOfVars()));
		logicRules.put(logic_X__eq_x,
				(id, r) -> xc.buildCtrLogic(id, r.sons[1].var(0), TypeEqNeOperator.EQ, r.sons[0].type.toLogop(), r.sons[0].arrayOfVars()));
		logicRules.put(logic_X__ne_x,
				(id, r) -> xc.buildCtrLogic(id, r.sons[1].var(0), TypeEqNeOperator.NE, r.sons[0].type.toLogop(), r.sons[0].arrayOfVars()));
		sumRules.put(add_vars__relop, (id, r) -> xc.buildCtrSum(id, r.sons[0].arrayOfVars(), basicCondition(r)));
		sumRules.put(add_mul_vals__relop, (id, r) -> {
			int[] coeffs = Stream.of(r.sons[0].sons).mapToInt(s -> s.type == VAR ? 1 : s.val(0)).toArray();
			if (IntStream.of(coeffs).allMatch(v -> v == 1))
				xc.buildCtrSum(id, r.sons[0].arrayOfVars(), basicCondition(r));
			else
				xc.buildCtrSum(id, r.sons[0].arrayOfVars(), coeffs, basicCondition(r));
		});
		sumRules.put(add_mul_vars__relop, (id, r) -> {
			XVarInteger[] list = Stream.of(r.sons[0].sons).map(s -> s.var(0)).toArray(XVarInteger[]::new);
			XVarInteger[] coeffs = Stream.of(r.sons[0].sons).map(s -> s.var(1)).toArray(XVarInteger[]::new);
			xc.buildCtrSum(id, list, coeffs, basicCondition(r));
		});
		extremumRules.put(min_relop, (id, r) -> xc.buildCtrMinimum(id, r.sons[0].vars(), basicCondition(r)));
		extremumRules.put(max_relop, (id, r) -> xc.buildCtrMaximum(id, r.sons[0].vars(), basicCondition(r)));
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
	private boolean recognizeIntensionIn(String id, XNodeParent<XVarInteger> tree, Map<Matcher, BiConsumer<String, XNodeParent<XVarInteger>>> rules,
			boolean condition) {
		return condition && rules.entrySet().stream().anyMatch(rule -> {
			if (!rule.getKey().matches(tree))
				return false;
			// System.out.println("Rec " + rule.getKey().target());
			posted(id); // keep it before calling the rule (because reposting is possible)
			rule.getValue().accept(id, tree);
			return true;
		});
	}

	private boolean recognizeIntension(String id, XNodeParent<XVarInteger> tree, int arity) {
		Map<XCallbacksParameters, Object> map = xc.implem().currParameters;
		if (recognizeIntensionIn(id, tree, unaryRules, arity == 1 && map.containsKey(RECOGNIZE_UNARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, binaryRules, arity == 2 && map.containsKey(RECOGNIZE_BINARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, ternaryRules, arity == 3 && map.containsKey(RECOGNIZE_TERNARY_PRIMITIVES)))
			return true;
		if (recognizeIntensionIn(id, tree, logicRules, map.containsKey(RECOGNIZE_LOGIC_CASES)))
			return true;
		if (recognizeIntensionIn(id, tree, sumRules, map.containsKey(RECOGNIZE_SUM_CASES)))
			return true;
		if (recognizeIntensionIn(id, tree, extremumRules, map.containsKey(RECOGNIZE_EXTREMUM_CASES)))
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
