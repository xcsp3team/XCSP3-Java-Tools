package org.xcsp.common.predicates;

import static org.xcsp.common.Types.TypeExpr.ABS;
import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.EQ;
import static org.xcsp.common.Types.TypeExpr.IN;
import static org.xcsp.common.Types.TypeExpr.LE;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.LT;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.NE;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.NOTIN;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SET;
import static org.xcsp.common.Types.TypeExpr.SPECIAL;
import static org.xcsp.common.Types.TypeExpr.SQR;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.ariop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.relop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.setop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.unaop;

import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;

public interface MatcherInterface {

	enum AbstractOperation {
		ariop, relop, setop, unaop, logop;
	}

	static XNodeLeaf<IVar> any = new XNodeLeaf<>(SPECIAL, "any");
	static XNodeLeaf<IVar> anyc = new XNodeLeaf<>(SPECIAL, "anyc"); // any under condition
	static XNodeLeaf<IVar> var = new XNodeLeaf<>(SPECIAL, "var");
	static XNodeLeaf<IVar> val = new XNodeLeaf<>(SPECIAL, "val");
	static XNodeLeaf<IVar> var_or_val = new XNodeLeaf<>(SPECIAL, "var-or-val");
	static XNodeLeaf<IVar> set_vals = new XNodeLeaf<>(SPECIAL, "set-vals");
	static XNodeLeaf<IVar> min_vars = new XNodeLeaf<>(SPECIAL, "min-vars");
	static XNodeLeaf<IVar> max_vars = new XNodeLeaf<>(SPECIAL, "max-vars");
	static XNodeLeaf<IVar> logic_vars = new XNodeLeaf<>(SPECIAL, "logic-vars");
	static XNodeLeaf<IVar> add_vars = new XNodeLeaf<>(SPECIAL, "add-vars");
	static XNodeLeaf<IVar> add_mul_vals = new XNodeLeaf<>(SPECIAL, "add-mul-vals");
	static XNodeLeaf<IVar> add_mul_vars = new XNodeLeaf<>(SPECIAL, "add-mul-vars");

	static XNodeParent<IVar> node(XNode<IVar> leftSon, TypeExpr type, XNode<IVar> rightSon) {
		return new XNodeParent<>(type, leftSon, rightSon);
	}

	static XNodeParent<IVar> node(XNode<IVar> leftSon, AbstractOperation type, XNode<IVar> rightSon) {
		return new XNodeParentSpecial<>(type.name(), leftSon, rightSon);
	}

	static XNodeParent<IVar> node(TypeExpr type, XNode<IVar> son) {
		return new XNodeParent<>(type, son);
	}

	static XNodeParent<IVar> node(AbstractOperation type, XNode<IVar> son) {
		return new XNodeParentSpecial<>(type.name(), son);
	}

	abstract XNode<IVar> target();

	abstract boolean validSpecialNodeAtLevel(XNode<? extends IVar> n, int level);

	default boolean similarNodes(XNode<IVar> n1, XNode<? extends IVar> n2, int level) {
		if (n1 == any) // any node (i.e. full abstract node) => everything matches
			return true;
		if (n1 == anyc) // any node under condition (the difference with SPECIAL only, is that sons are not considered recursively)
			return validSpecialNodeAtLevel(n2, level);
		if (n1 == var)
			return n2.type == VAR;
		if (n1 == val)
			return n2.type == LONG;
		if (n1 == var_or_val)
			return n2.type == VAR || n2.type == LONG;
		if (n1 == set_vals) // if abstract set, we control that n2 is either an empty set or a set with only longs as sons
			return n2.type == SET && (n2 instanceof XNodeLeaf || Stream.of(n2.sons).allMatch(s -> s.type == LONG));
		if (n1 == min_vars) // if abstract min, we control that n2 is a min with only vars as sons
			return n2.type == MIN && n2.sons.length >= 2 && Stream.of(n2.sons).allMatch(s -> s.type == VAR);
		if (n1 == max_vars) // if abstract min, we control that n2 is a min with only vars as sons
			return n2.type == MAX && n2.sons.length >= 2 && Stream.of(n2.sons).allMatch(s -> s.type == VAR);
		if (n1 == logic_vars)
			return n2.type.isLogicalOperator() && n2.sons.length >= 2 && Stream.of(n2.sons).allMatch(s -> s.type == VAR);
		if (n1 == add_vars)
			return n2.type == ADD && n2.sons.length >= 2 && Stream.of(n2.sons).allMatch(s -> s.type == VAR);
		if (n1 == add_mul_vals)
			return n2.type == ADD && Stream.of(n2.sons).anyMatch(s -> x_mul_k.recognize(s)) && n2.sons.length >= 2
					&& Stream.of(n2.sons).allMatch(s -> s.type == VAR || x_mul_k.recognize(s));
		if (n1 == add_mul_vars)
			return n2.type == ADD && n2.sons.length >= 2 && Stream.of(n2.sons).allMatch(s -> x_mul_y.recognize(s));
		if (n1 instanceof XNodeLeaf != n2 instanceof XNodeLeaf)
			return false;
		if ((n1.type == SPECIAL && !validSpecialNodeAtLevel(n2, level)) || (n1.type != SPECIAL && n1.type != n2.type))
			return false;
		return n1 instanceof XNodeLeaf
				|| (n1.sons.length == n2.sons.length && IntStream.range(0, n1.sons.length).allMatch(i -> similarNodes(n1.sons[i], n2.sons[i], level + 1)));
	}

	default boolean positiveExtraControl(XNode<? extends IVar> root) {
		return true;
	}

	default boolean recognize(XNode<? extends IVar> root) {
		return similarNodes(target(), root, 0) && positiveExtraControl(root);
	}

	class Matcher implements MatcherInterface {
		private final XNode<IVar> target;

		private final BiFunction<XNode<? extends IVar>, Integer, Boolean> f;

		@Override
		public XNode<IVar> target() {
			return target;
		}

		public Matcher(XNode<IVar> target, BiFunction<XNode<? extends IVar>, Integer, Boolean> f) {
			this.target = target;
			this.f = f;
		}

		public Matcher(XNode<IVar> target) {
			this(target, (node, level) -> (level == 0 && node.type.isRelationalOperator()) || (level == 1 && node.type.isArithmeticOperator()));
		}

		@Override
		public boolean validSpecialNodeAtLevel(XNode<? extends IVar> n, int level) {
			return f.apply(n, level);
		}
	}

	Matcher x_mul_k = new Matcher(node(var, MUL, val));
	Matcher x_mul_k__eq_l = new Matcher(node(node(var, MUL, val), EQ, val));

	Matcher x_mul_y = new Matcher(node(var, MUL, var));

	// Matcher any_relop_k = new Matcher(node(any, relop, val));
	// Matcher any_relop_x = new Matcher(node(any, relop, var));

	Matcher x_relop_k = new Matcher(node(var, relop, val));
	Matcher k_relop_x = new Matcher(node(val, relop, var));
	Matcher x_ariop_k__relop_l = new Matcher(node(node(var, ariop, val), relop, val));
	Matcher l_relop__x_ariop_k = new Matcher(node(val, relop, node(var, ariop, val)));
	Matcher x_setop_S = new Matcher(node(var, setop, set_vals), (node, level) -> level == 0 && node.type.oneOf(IN, NOTIN));
	Matcher x_in_intvl = new Matcher(node(node(var, LE, val), AND, node(val, LE, var)));
	Matcher x_notin_intvl = new Matcher(node(node(var, LE, val), OR, node(val, LE, var)));

	Matcher x_relop_y = new Matcher(node(var, relop, var));
	Matcher x_ariop_k__relop_y = new Matcher(node(node(var, ariop, val), relop, var));
	Matcher x_ariop_y__relop_k = new Matcher(node(node(var, ariop, var), relop, val));
	Matcher x_relop__y_ariop_k = new Matcher(node(var, relop, node(var, ariop, val)));
	Matcher k_relop__x_ariop_y = new Matcher(node(val, relop, node(var, ariop, var)));
	Matcher unaop_x__eq_y = new Matcher(node(node(unaop, var), EQ, var), (node, level) -> level == 1 && node.type.oneOf(ABS, NEG, SQR, NOT));
	// uop(x) = y with abs,neg,sqr,not}

	Matcher x_ariop_y__relop_z = new Matcher(node(node(var, ariop, var), relop, var));
	Matcher x_relop__y_ariop_z = new Matcher(node(var, relop, node(var, ariop, var)));

	Matcher logic_X = new Matcher(logic_vars);
	Matcher logic_X__eq_x = new Matcher(node(logic_vars, EQ, var));
	Matcher logic_X__ne_x = new Matcher(node(logic_vars, NE, var));

	Matcher add_vars__relop = new Matcher(node(add_vars, relop, var_or_val));
	Matcher add_mul_vals__relop = new Matcher(node(add_mul_vals, relop, var_or_val));
	Matcher add_mul_vars__relop = new Matcher(node(add_mul_vars, relop, var_or_val));

	Matcher min_relop = new Matcher(node(min_vars, relop, var_or_val));
	Matcher max_relop = new Matcher(node(max_vars, relop, var_or_val));

	Matcher abs_sub = new Matcher(node(ABS, node(any, SUB, any)));
	Matcher not_not = new Matcher(node(NOT, node(NOT, any)));
	Matcher neg_neg = new Matcher(node(NEG, node(NEG, any)));
	Matcher any_lt_k = new Matcher(node(any, LT, val));
	Matcher k_lt_any = new Matcher(node(val, LT, any));

	Matcher not_logop = new Matcher(node(NOT, anyc), (node, level) -> level == 1 && node.type.isLogicallyInvertible());

	Matcher ne_not_any = new Matcher(node(node(NOT, any), NE, any));
	Matcher ne_any_not = new Matcher(node(any, NE, node(NOT, any)));
}