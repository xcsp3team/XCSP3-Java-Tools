package org.xcsp.common.predicates;

import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.SET;
import static org.xcsp.common.Types.TypeExpr.SPECIAL;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.common.predicates.XNode.node;
import static org.xcsp.common.predicates.XNode.specialLeaf;

import java.util.function.BiPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;

/**
 * This interface is used to test if a specified (source) tree matches a predefined target tree. Some kind of abstraction can be used by means of
 * special nodes.
 * 
 * @author Christophe Lecoutre
 *
 */
public interface MatcherInterface {

	enum AbstractOperation {
		ariop, relop, setop, unaop, logop, symop;
	}

	XNodeLeaf<IVar> any = specialLeaf("any");
	XNodeLeaf<IVar> anyc = specialLeaf("anyc"); // any under condition
	XNodeLeaf<IVar> var = specialLeaf("var");
	XNodeLeaf<IVar> val = specialLeaf("val");
	XNodeLeaf<IVar> varOrVal = specialLeaf("var-or-val");
	XNodeLeaf<IVar> any_add_val = specialLeaf("any-add-val");
	XNodeLeaf<IVar> var_add_val = specialLeaf("var-add-val");
	XNodeLeaf<IVar> sub = specialLeaf("sub");
	XNodeLeaf<IVar> not = specialLeaf("not");
	XNodeLeaf<IVar> set_vals = specialLeaf("set-vals");
	XNodeLeaf<IVar> min_vars = specialLeaf("min-vars");
	XNodeLeaf<IVar> max_vars = specialLeaf("max-vars");
	XNodeLeaf<IVar> logic_vars = specialLeaf("logic-vars");
	XNodeLeaf<IVar> add_vars = specialLeaf("add-vars");
	XNodeLeaf<IVar> add_mul_vals = specialLeaf("add-mul-vals");
	XNodeLeaf<IVar> add_mul_vars = specialLeaf("add-mul-vars");

	Matcher x_mul_k = new Matcher(node(MUL, var, val));
	Matcher x_mul_y = new Matcher(node(MUL, var, var));

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
		if (target == varOrVal)
			return source.type == VAR || source.type == LONG;
		if (target == any_add_val)
			return source.type == ADD && source.sons.length == 2 && source.sons[1].type == LONG;
		if (target == var_add_val)
			return source.type == ADD && source.sons.length == 2 && source.sons[0].type == VAR && source.sons[1].type == LONG;
		if (target == sub)
			return source.type == SUB;
		if (target == not)
			return source.type == NOT;
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
		return target instanceof XNodeLeaf || (target.sons.length == source.sons.length
				&& IntStream.range(0, target.sons.length).allMatch(i -> matching(source.sons[i], target.sons[i], level + 1)));
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

}