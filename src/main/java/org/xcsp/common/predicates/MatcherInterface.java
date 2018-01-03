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

import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;

/**
 * This interface is used to test if a specified (source) tree matches a predefined target tree. Some kind of abstraction can be used by means of
 * special nodes.
 * 
 * @author Christophe Lecoutre
 *
 */
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

	static <V extends IVar>XNodeParent<V > node(XNode<V> left, TypeExpr type, XNode<V> right) {
		return new XNodeParent<>(type, left, right);
	}

	static <V extends IVar>XNodeParent<V> node(XNode<V> left, AbstractOperation type, XNode<V> right) {
		return new XNodeParentSpecial<>(type.name(), left, right);
	}

	static <V extends IVar>XNodeParent<V> node(TypeExpr type, XNode<V> son) {
		return new XNodeParent<>(type, son);
	}

	static <V extends IVar>XNodeParent<V> node(AbstractOperation type, XNode<V> son) {
		return new XNodeParentSpecial<>(type.name(), son);
	}

	/**
	 * Returns the target tree, which may possibly involve some form of abstraction by means of special nodes.
	 * 
	 * @return the target tree
	 */
	abstract XNode<IVar> target();

	/**
	 * Returns {@code true} if the specified node (considered at the specified level/depth) is valid with respect to the target tree when assuming
	 * that the corresponding node in the target tree is a special node.
	 * 
	 * @param node
	 *            a (source) node
	 * @param level
	 *            the level/depth associated with the node
	 * @return {@code true} if the specified source node is valid with respect to a corresponding special node in the target tree
	 */
	abstract boolean validForSpecialTargetNode(XNode<? extends IVar> node, int level);

	/**
	 * Returns {@code true} if the specified source tree matches the specified target tree (at the specified level).
	 * 
	 * @param source
	 *            the source (sub-)tree
	 * @param target
	 *            the target (sub-)tree
	 * @param level
	 *            the level/depth for the comparison
	 * @return {@code true} if the specified source tree matches the specified target tree
	 */
	default boolean matching(XNode<? extends IVar> source, XNode<IVar> target, int level) {
		if (target == any) // any node (i.e., full abstract node) => everything matches
			return true;
		if (target == anyc) // any node under condition (the difference with SPECIAL only, is that sons are not considered recursively)
			return validForSpecialTargetNode(source, level);
		if (target == var)
			return source.type == VAR;
		if (target == val)
			return source.type == LONG;
		if (target == var_or_val)
			return source.type == VAR || source.type == LONG;
		if (target == set_vals) // abstract set => we control that source is either an empty set or a set built on only longs
			return source.type == SET && (source instanceof XNodeLeaf || Stream.of(source.sons).allMatch(s -> s.type == LONG));
		if (target == min_vars) // abstract min => we control that source is a min built on only variables
			return source.type == MIN && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> s.type == VAR);
		if (target == max_vars) // abstract max => we control that source is a max built on only variables
			return source.type == MAX && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> s.type == VAR);
		if (target == logic_vars)
			return source.type.isLogicalOperator() && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> s.type == VAR);
		if (target == add_vars)
			return source.type == ADD && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> s.type == VAR);
		if (target == add_mul_vals)
			return source.type == ADD && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> s.type == VAR || x_mul_k.matches(s));
		if (target == add_mul_vars)
			return source.type == ADD && source.sons.length >= 2 && Stream.of(source.sons).allMatch(s -> x_mul_y.matches(s));
		if (target instanceof XNodeLeaf != source instanceof XNodeLeaf)
			return false;
		if ((target.type == SPECIAL && !validForSpecialTargetNode(source, level)) || (target.type != SPECIAL && target.type != source.type))
			return false;
		return target instanceof XNodeLeaf || (target.sons.length == source.sons.length && IntStream.range(0, target.sons.length).allMatch(i -> matching(
				source.sons[i], target.sons[i], level + 1)));
	}

	/**
	 * Returns {@code true} if the predefined target tree matches the specified (source) tree.
	 * 
	 * @param tree
	 *            a tree
	 * @return {@code true} if the predefined target tree matches the specified (source) tree
	 */
	default boolean matches(XNode<? extends IVar> tree) {
		return matching(tree, target(), 0);
	}

	/**
	 * This class allows us to perform matching tests between trees.
	 */
	class Matcher implements MatcherInterface {
		private final XNode<IVar> target;

		private final BiPredicate<XNode<? extends IVar>, Integer> p;

		@Override
		public XNode<IVar> target() {
			return target;
		}

		/**
		 * Builds a {@code Matcher} object with the specified target tree.
		 * 
		 * @param target
		 *            the target tree
		 * @param p
		 *            a predicate used for special nodes in some occasions
		 */
		public Matcher(XNode<IVar> target, BiPredicate<XNode<? extends IVar>, Integer> p) {
			this.target = target;
			this.p = p;
		}

		/**
		 * Builds a {@code Matcher} object with the specified target tree.
		 * 
		 * @param target
		 *            the target tree
		 */
		public Matcher(XNode<IVar> target) {
			this(target, (node, level) -> (level == 0 && node.type.isRelationalOperator()) || (level == 1 && node.type.isArithmeticOperator()));
		}

		@Override
		public boolean validForSpecialTargetNode(XNode<? extends IVar> node, int level) {
			return p.test(node, level);
		}
	}

	Matcher x_mul_k = new Matcher(node(var, MUL, val));
	Matcher x_mul_k__eq_l = new Matcher(node(node(var, MUL, val), EQ, val));
	Matcher x_mul_y = new Matcher(node(var, MUL, var));

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

	Matcher not_any__ne_any = new Matcher(node(node(NOT, any), NE, any));
	Matcher any_ne__not_any = new Matcher(node(any, NE, node(NOT, any)));
}