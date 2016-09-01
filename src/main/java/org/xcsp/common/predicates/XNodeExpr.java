package org.xcsp.common.predicates;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.xcsp.common.XEnums.TypeArithmeticOperator;
import org.xcsp.common.XEnums.TypeExpr;

/**
 * The class used for representing a node of a syntactic tree (built for functional expressions, and used especially with <intension>). Also includes, as intern
 * classes, subclasses for managing parent and leaf nodes.
 */
public abstract class XNodeExpr<V> {

	/** private static field just used in toString(). */
	public static boolean postfixed = true; // hard coding

	/** The type of the node. For example add, not, or long. */
	public final TypeExpr type;

	/** Returns the type of the node. For example add, not, or long. We need this method for Scala. */
	public final TypeExpr getType() {
		return type;
	}

	/** Builds a node for a syntactic tree, with the specified type. */
	protected XNodeExpr(TypeExpr type) {
		this.type = type;
	}

	/** Collects the set of variables involved in the subtree whose root is this object, and add them to the specified set. */
	public abstract Set<V> collectVars(Set<V> set);

	/** Returns true iff a leaf in the subtree whose root is this object satisfies the specified predicate. */
	public boolean canFindLeafSuchThat(Predicate<XNodeLeaf<V>> p) {
		return this instanceof XNodeParent ? Stream.of(((XNodeParent<V>) this).sons).anyMatch(c -> c.canFindLeafSuchThat(p)) : p.test((XNodeLeaf<V>) this);
	}

	/** Returns true iff a leaf in the subtree whose root is this object has the specified type. */
	public boolean canFindleafWith(TypeExpr type) {
		return canFindLeafSuchThat(n -> n.getType() == type);
	}

	/** Returns the maximum value of a parameter number in the subtree whose root is this object, or -1 if there is none. */
	public int maxParameterNumber() {
		if (this instanceof XNodeParent)
			return Stream.of(((XNodeParent<V>) this).sons).mapToInt(c -> c.maxParameterNumber()).max().orElse(-1);
		else
			return type == TypeExpr.PAR ? ((Long) ((XNodeLeaf<V>) this).value).intValue() : -1; // recall that %... is not possible in predicates
	}

	/** Replaces parameters with values of the specified array. */
	public abstract XNodeExpr<V> concretizeWith(Object[] args);

	/** Returns true iff the tree has the form x, or the form x + k, or the form k + x or the form x - k, with x a variable and k a (long) integer. */
	public abstract boolean hasBasicForm();

	/** Returns true iff the tree has the form x <opa> y with <opa> an arithmetic operator in {+,-,*,/,%,dist}. */
	public abstract TypeArithmeticOperator arithmeticOperatorOnTwoVariables();

	/** Returns true iff the tree has two sons, one with a variable and the other with a (long) integer, in any order. */
	public abstract boolean hasVarAndCstSons();

	/** Returns true iff the tree has two sons, both with a variable. */
	public abstract boolean hasVarAndVarSons();

	public abstract Object getValueOfFirstLeafOfType(TypeExpr type);

	public abstract List<String> canonicalForm(List<String> tokens, V[] scope);

	/**
	 * Returns a textual description of the subtree whose root is this node. The specified effective arguments will be used if there are some parameters in the
	 * subtree. The specified boolean value indicates if a post-fixed or a functional form is wanted.
	 */
	public abstract String toString(Object[] args, boolean postfixed);

	@Override
	public String toString() {
		return type.toString().toLowerCase();
	}

}
