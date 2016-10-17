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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Interfaces.IVar;

/**
 * The class used for representing a node of a syntactic tree (built for functional expressions, and used especially with <intension>). Subclasses of this class
 * allow us to manage parent and leaf nodes.
 * 
 * @author Christophe Lecoutre
 */
public abstract class XNode<V extends IVar> {

	/** The type of the node. For example, it can be add, not, or long. */
	public final TypeExpr type;

	/** Returns the type of the node. For example add, not, or long. We need this method for Scala. */
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

	/** Returns the set of variables in the syntactic tree rooted by this node, or null if there is none. */
	public V[] vars() {
		LinkedHashSet<V> set = collectVars(new LinkedHashSet<>());
		if (set.size() == 0)
			return null;
		return set.stream().toArray(s -> (V[]) Array.newInstance(set.iterator().next().getClass(), s));
	}

	/** Return true iff the sequence of variables encountered in the syntactic tree rooted by this node is exactly the same as the specified array. */
	public boolean exactlyVars(V[] t) {
		V[] vars = vars();
		return t.length == vars.length && IntStream.range(0, t.length).allMatch(i -> t[i] == vars[i]);
	}

	/** Returns true iff a leaf in the subtree rooted by this object satisfies the specified predicate. */
	public boolean canFindLeafSuchThat(Predicate<XNodeLeaf<V>> p) {
		return this instanceof XNodeParent ? Stream.of(((XNodeParent<V>) this).sons).anyMatch(c -> c.canFindLeafSuchThat(p)) : p.test((XNodeLeaf<V>) this);
	}

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

	/** Returns a new syntactic tree, obtained by replacing symbols with integers, as defined by the specified map. */
	public abstract XNode<V> replaceSymbols(Map<String, Integer> mapOfSymbols);

	public abstract Object getValueOfFirstLeafOfType(TypeExpr type);

	/**
	 * Returns the canonical post-fixed form of the syntactic tree rooted by this node, as an array of tokens. Variables are replaced by parameters (of the form
	 * %i).
	 */
	public String[] postfixCanonicalForm() {
		return postfixCanonicalForm(new ArrayList<>(), vars()).toArray(new String[0]);
	}

	/**
	 * Returns the canonical post-fixed form of the syntactic tree rooted by this node, as a list of tokens added to the specified list. Variables from the
	 * specified array are replaced by their parameterized forms (%i).
	 */
	public abstract List<String> postfixCanonicalForm(List<String> tokens, IVar[] scope);

	/**
	 * Returns a textual functional description of the subtree rooted by this node. If not null, the specified arguments will be used in case there are some
	 * parameters in the subtree.
	 */
	public abstract String functionalForm(Object[] args);

	@Override
	public String toString() {
		return functionalForm(null);
	}
}
