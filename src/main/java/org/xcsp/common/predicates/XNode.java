/*
 * Copyright (c) 2016 XCSP3 Team (contact@xcsp.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xcsp.common.predicates;

import java.lang.reflect.Array;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;

/**
 * The class used for representing a node of a syntactic tree (built for functional expressions, and used especially with <intension>). Subclasses of this class
 * allow us to manage parent and leaf nodes.
 * 
 * @author Christophe Lecoutre
 */
public abstract class XNode<V extends IVar> implements Comparable<XNode<V>> {

	/** The type of the node. For example, it can be add, not, or long. */
	public TypeExpr type;

	/** Returns the type of the node. For example add, not, or long. We need this method for language Scala. */
	public final TypeExpr getType() {
		return type;
	}

	/** Builds a node for a syntactic tree, with the specified type. */
	protected XNode(TypeExpr type) {
		this.type = type;
	}

	/** Returns the size of the syntactic tree, i.e. the number of nodes it contains. */
	public abstract int size();

	/** Returns the maximum value of a parameter number in the subtree rooted by this node, or -1 if there is none. */
	public abstract int maxParameterNumber();

	/** Collects the set of variables involved in the subtree rooted by this object, and add them to the specified set. */
	public abstract LinkedHashSet<V> collectVars(LinkedHashSet<V> set);

	/** Returns the set of variables in the syntactic tree rooted by this node, in the order they are collected, or null if there is none. */
	public final V[] vars() {
		LinkedHashSet<V> set = collectVars(new LinkedHashSet<>());
		return set.size() == 0 ? null : set.stream().toArray(s -> (V[]) Array.newInstance(set.iterator().next().getClass(), s));
	}

	/** Returns the (i+1)th variable encountered while traversing (depth-first) the syntactic tree rooted by this node, or null if it does not exist. */
	public final V var(int i) {
		if (i == 0)
			return firstVar();
		LinkedHashSet<V> set = collectVars(new LinkedHashSet<>());
		if (i >= set.size())
			return null;
		int j = 0;
		for (V x : set)
			if (j++ == i)
				return x;
		throw new RuntimeException();
	}

	public final V firstVar() {
		XNodeLeaf<V> f = firstOfType(TypeExpr.VAR);
		return f == null ? null : (V) f.value;
	}

	public final Integer firstVal() {
		XNodeLeaf<V> f = firstOfType(TypeExpr.LONG);
		return f == null ? null : Utilities.safeLong2Int((Long) f.value, true);
	}

	/**
	 * Return true iff the sequence of variables encountered in the syntactic tree rooted by this node is exactly the same as the specified array.
	 */
	public final boolean exactlyVars(V[] t) {
		V[] vars = vars();
		return t.length == vars.length && IntStream.range(0, t.length).allMatch(i -> t[i] == vars[i]);
	}

	/** Returns true iff a leaf in the subtree rooted by this object satisfies the specified predicate. */
	public final boolean containsLeafSuchThat(Predicate<XNodeLeaf<V>> p) {
		return this instanceof XNodeParent ? Stream.of(((XNodeParent<V>) this).sons).anyMatch(c -> c.containsLeafSuchThat(p)) : p.test((XNodeLeaf<V>) this);
	}

	public abstract <T> T firstOfType(TypeExpr type);

	/** Returns a new syntactic tree, obtained by replacing symbols with integers, as defined by the specified map. */
	public abstract XNode<V> replaceSymbols(Map<String, Integer> mapOfSymbols);

	/**
	 * Returns a new syntactic tree, equivalent to this tree, but in a canonical form. For example, commutative operators will take variables before integers as
	 * operands; actually, the total ordinal order over constants in TypeExpr is used. Some simplifications are also perrformed; for example, not(eq(x,y))
	 * becomes ne(x,y).
	 */
	public abstract XNode<V> canonization();

	/**
	 * Returns a new syntactic tree that represents an abstraction of this tree. Variables are replaced by parameters, and integers are also replaced by
	 * parameters (if the first specified Boolean is true). Occurrences of the same variables are replaced by the same parameter (if the second specified
	 * Boolean is true). Values replaced by parameters are added to the specified list.
	 */
	public abstract XNode<V> abstraction(List<Object> args, boolean abstractIntegers, boolean multiOccurrences);

	/**
	 * Returns a new syntactic tree that represents a concretization of this tree. Any parameter of value i is replaced by the ith value in the specified list
	 * of arguments.
	 */
	public abstract XNode<V> concretization(Object[] args);

	/**
	 * Returns the post-fixed expression (under the form of a String) of the syntactic tree rooted by this node. If the specified array is not null, variables
	 * that are present in the tree are replaced by their parameterized forms (%i).
	 */
	public abstract String toPostfixExpression(IVar[] scopeForAbstraction);

	/**
	 * Returns the functional expression (under the form of a String) of the syntactic tree rooted by this node. If the specified array is not null, parameters
	 * that are present in the tree are replaced by their corresponding arguments.
	 */
	public abstract String toFunctionalExpression(Object[] argsForConcretization);

	@Override
	public String toString() {
		return toFunctionalExpression(null);
	}
}
