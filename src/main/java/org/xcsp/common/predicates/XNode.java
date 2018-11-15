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

import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.VAR;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.MatcherInterface.AbstractOperation;

/**
 * This class is used for representing a node of a syntactic tree, which is built for functional expressions, and used especially with element
 * {@code <intension>}. Subclasses of this class allow us to manage parent and leaf nodes.
 * 
 * @author Christophe Lecoutre
 */
public abstract class XNode<V extends IVar> implements Comparable<XNode<V>> {

	// ************************************************************************
	// ***** Static Methods
	// ************************************************************************

	public static <V extends IVar> XNodeParent<V> node(TypeExpr type, XNode<V> left, XNode<V> right) {
		return new XNodeParent<>(type, left, right);
	}

	public static <V extends IVar> XNodeParent<V> node(AbstractOperation type, XNode<V> left, XNode<V> right) {
		return new XNodeParentSpecial<>(type.name(), left, right);
	}

	public static <V extends IVar> XNodeParent<V> node(TypeExpr type, XNode<V> son) {
		return new XNodeParent<>(type, son);
	}

	public static <V extends IVar> XNodeParent<V> node(AbstractOperation type, XNode<V> son) {
		return new XNodeParentSpecial<>(type.name(), son);
	}

	public static <V extends IVar> XNodeParent<V> node(TypeExpr type, XNode<V>[] sons) {
		return new XNodeParent<>(type, sons);
	}

	public static <V extends IVar> XNodeParent<V> node(TypeExpr type, List<XNode<V>> sons) {
		return new XNodeParent<>(type, sons);
	}

	public static <V extends IVar> XNodeParent<V> node(TypeExpr type, Stream<XNode<V>> sons) {
		return new XNodeParent<>(type, sons.toArray(XNode[]::new)); // sons.size()])); //collect(Collectors.toList()));
	}

	public static <V extends IVar> XNodeLeaf<V> longLeaf(long value) {
		return new XNodeLeaf<>(TypeExpr.LONG, value);
	}

	public static <V extends IVar> XNodeLeaf<V> specialLeaf(String value) {
		return new XNodeLeaf<>(TypeExpr.SPECIAL, value);
	}

	// ************************************************************************
	// ***** Fields, Constructor and Methods
	// ************************************************************************

	/**
	 * The type of the node. For example, it can be {@code ADD}, {@code NOT}, or {@code LONG}.
	 */
	public final TypeExpr type;

	/**
	 * The sons (children) of the node. It is {@code null} if the node is a leaf.
	 */
	public final XNode<V>[] sons;

	/**
	 * Builds a node for a syntactic tree, with the specified type and the specified sons (children).
	 * 
	 * @param type
	 *            the type of the node
	 * @param sons
	 *            the sons (children) of the node
	 */
	protected XNode(TypeExpr type, XNode<V>[] sons) {
		this.type = type;
		this.sons = sons;
	}

	/**
	 * Returns the type of the node. For example {@code ADD}, {@code NOT}, or {@code LONG}. Note that we need this method for language Scala.
	 * 
	 * @return the type of the node
	 */
	public final TypeExpr getType() {
		return type;
	}

	/**
	 * Returns the arity of this node, i.e., the number of sons.
	 * 
	 * @return the arity of this node
	 */
	public final int arity() {
		return sons.length;
	}

	/**
	 * Returns the size of the tree rooted by this node, i.e., the number of nodes it contains.
	 * 
	 * @return the size of the tree rooted by this node
	 */
	public abstract int size();

	/**
	 * Returns the maximum value of a parameter number in the tree rooted by this node, or -1 if there is none.
	 * 
	 * @return the maximum value of a parameter number in the tree rooted by this node, or -1
	 */
	public abstract int maxParameterNumber();

	/**
	 * Returns the first node accepted by the specified predicate in the tree rooted by this node, or {@code null} otherwise.
	 * 
	 * @param p
	 *            a predicate to be applied on nodes
	 * @return the first node accepted by the specified predicate
	 */
	public abstract XNode<V> firstNodeSuchThat(Predicate<XNode<V>> p);

	/**
	 * Adds to the specified list all nodes accepted by the specified predicate in the tree rooted by this node. The specifies list is returned.
	 * 
	 * @param p
	 *            a predicate to be applied on nodes
	 * @param list
	 *            a list in which nodes are added
	 * @return a list with all nodes accepted by the specified predicate in the tree rooted by this node
	 */
	public abstract LinkedList<XNode<V>> allNodesSuchThat(Predicate<XNode<V>> p, LinkedList<XNode<V>> list);

	/**
	 * Returns a list containing all nodes accepted by the specified predicate in the tree rooted by this node. Nodes are added in infix manner.
	 * 
	 * @param p
	 *            a predicate to be applied on nodes
	 * @return a list with all nodes accepted by the specified predicate in the tree rooted by this node
	 */
	public LinkedList<XNode<V>> allNodesSuchThat(Predicate<XNode<V>> p) {
		return allNodesSuchThat(p, new LinkedList<XNode<V>>());
	}

	/**
	 * Builds a list with the sequence of variables encountered during a depth-first exploration of the tree rooted by this node. Multiple occurrences
	 * of the same variables are possible.
	 * 
	 * @return the list of encountered variables during a depth-first exploration
	 */
	public final LinkedList<V> listOfVars() {
		return allNodesSuchThat(s -> s.type == VAR).stream().map(n -> (V) ((XNodeLeaf<V>) n).value).collect(Collectors.toCollection(LinkedList<V>::new));
	}

	/**
	 * Builds a list with the sequence of values (long integers) encountered during a depth-first exploration of the tree rooted by this node.
	 * Multiple occurrences of the same values are possible.
	 * 
	 * @return the list of encountered values (integers) during a depth-first exploration
	 */
	public final LinkedList<Long> listOfVals() {
		return allNodesSuchThat(s -> s.type == LONG).stream().map(n -> (Long) ((XNodeLeaf<V>) n).value).collect(Collectors.toCollection(LinkedList<Long>::new));
	}

	public final V[] arrayOfVars() {
		LinkedList<V> list = listOfVars();
		return list.size() == 0 ? null : list.stream().toArray(s -> Utilities.buildArray(list.iterator().next().getClass(), s));
	}

	public final int[] arrayOfVals() {
		LinkedList<Long> list = listOfVals();
		return list.size() == 0 ? new int[0] : list.stream().mapToInt(l -> Utilities.safeLong2Int(l, true)).toArray();
	}

	/**
	 * Returns the (i+1)th variable encountered while traversing (in a depth-first manner) the tree rooted by this node, or {@code null} if such
	 * variable does not exist.
	 * 
	 * @param i
	 *            the index, starting at 0, of a variable
	 * @return the (i+1)th variable encountered in the tree rooted by this node
	 */
	public final V var(int i) {
		if (i == 0) {
			XNodeLeaf<V> f = (XNodeLeaf<V>) firstNodeSuchThat(n -> n.type == TypeExpr.VAR);
			return f == null ? null : (V) f.value;
		}
		LinkedList<V> list = listOfVars();
		return i >= list.size() ? null : list.get(i);
	}

	/**
	 * Returns the (i+1)th value encountered while traversing (in a depth-first manner) the tree rooted by this node, or {@code null} if such value
	 * does not exist.
	 * 
	 * @param i
	 *            the index, starting at 0, of a value
	 * @return the (i+1)th value encountered in the tree rooted by this node
	 */
	public final Integer val(int i) {
		if (i == 0) {
			XNodeLeaf<V> f = (XNodeLeaf<V>) firstNodeSuchThat(n -> n.type == TypeExpr.LONG);
			return f == null ? null : Utilities.safeLong2Int((Long) f.value, true);
		}
		LinkedList<Long> list = listOfVals();
		return i >= list.size() ? null : Utilities.safeLong2Int(list.get(i), true);
	}

	public final TypeConditionOperatorRel relop(int i) {
		if (i == 0) {
			XNode<V> f = firstNodeSuchThat(n -> n.type.isRelationalOperator());
			return f == null ? null : f.type.toRelop();
		}
		LinkedList<TypeConditionOperatorRel> list = allNodesSuchThat(s -> s.type.isRelationalOperator()).stream().map(n -> n.type.toRelop())
				.collect(Collectors.toCollection(LinkedList<TypeConditionOperatorRel>::new));
		return i >= list.size() ? null : list.get(i);
	}

	public final TypeArithmeticOperator ariop(int i) {
		if (i == 0) {
			XNode<V> f = firstNodeSuchThat(n -> n.type.isArithmeticOperator());
			return f == null ? null : f.type.toAriop();
		}
		LinkedList<TypeArithmeticOperator> list = allNodesSuchThat(s -> s.type.isArithmeticOperator()).stream().map(n -> n.type.toAriop())
				.collect(Collectors.toCollection(LinkedList<TypeArithmeticOperator>::new));
		return i >= list.size() ? null : list.get(i);
	}

	/**
	 * Returns the set of variables in the tree rooted by this node, in the order they are collected, or {@code null} if there is none.
	 * 
	 * @return the set of variables in the tree rooted by this node, or {@code null}
	 */
	public final V[] vars() {
		LinkedHashSet<V> set = new LinkedHashSet<>();
		listOfVars().stream().forEach(x -> set.add(x));
		return set.size() == 0 ? null : set.stream().toArray(s -> Utilities.buildArray(set.iterator().next().getClass(), s));
	}

	/**
	 * Return {@code true} iff the sequence of variables encountered in the tree rooted by this node is exactly the specified array.
	 * 
	 * @param t
	 *            an array of variables
	 * @return {@code true} iff the sequence of variables encountered in the tree rooted by this node is exactly the specified array
	 */
	public final boolean exactlyVars(V[] t) {
		V[] vars = vars();
		return t.length == vars.length && IntStream.range(0, t.length).allMatch(i -> t[i] == vars[i]);
	}

	public final LinkedHashSet<V> collectVarsToSet(LinkedHashSet<V> set) {
		listOfVars().stream().forEach(x -> set.add(x));
		return set;
	}

	/**
	 * Returns a new tree, obtained from the tree rooted by this node by replacing symbols with integers, as defined by the specified map.
	 * 
	 * @param mapOfSymbols
	 *            a map associating integers with strings (symbols)
	 * @return a new tree, obtained by replacing symbols with integers, as defined by the specified map
	 */
	public abstract XNode<V> replaceSymbols(Map<String, Integer> mapOfSymbols);

	/**
	 * a new tree, obtained from the tree rooted by this node by replacing values of leaves, as defined by the specified function
	 * 
	 * @param f
	 *            a function mapping objects to objects
	 * @return a new tree, obtained by replacing values of leaves, as defined by the specified function
	 */
	public abstract XNode<V> replaceLeafValues(Function<Object, Object> f);

	public abstract XNode<V> replacePartiallyParameters(Object[] valueParameters);

	/**
	 * Returns a new tree, equivalent to the tree rooted by this node, and in canonical form. For example, commutative operators will take variables
	 * before integers as operands; actually, the total ordinal order over constants in {@code TypeExpr} is used. Some simplifications are also
	 * performed; for example, {@code not(eq(x,y))} becomes {@code ne(x,y)}.
	 * 
	 * @return a new tree, equivalent to the tree rooted by this node, and in canonical form
	 */
	public abstract XNode<V> canonization();

	/**
	 * Returns a new tree representing an abstraction of the tree rooted by this node. Variables are replaced by parameters, and integers are also
	 * replaced by parameters (if the first specified Boolean is true). Occurrences of the same variables are replaced by the same parameter (if the
	 * second specified Boolean is true). Values replaced by parameters are added to the specified list.
	 * 
	 * @param args
	 *            a list that is updated by adding the objects (variables, and possibly integers) that are abstracted (replaced by parameters)
	 * @param abstractIntegers
	 *            if {@code true}, encountered integers are also abstracted
	 * @param multiOccurrences
	 *            if {@code true}, occurrences of the same variables are replaced by the same parameter
	 * @return a new tree representing an abstraction of the tree rooted by this node
	 */
	public abstract XNode<V> abstraction(List<Object> args, boolean abstractIntegers, boolean multiOccurrences);

	/**
	 * Returns a new tree representing a concretization of the tree rooted by this node. Any parameter of value i is replaced by the ith object in the
	 * specified list of arguments.
	 * 
	 * @param args
	 *            the list of arguments to be used as values for the parameters that are present in the tree rooted by this node
	 * @return a new tree representing a concretization of the tree rooted by this node
	 */
	public abstract XNode<V> concretization(Object[] args);

	/**
	 * Returns a string denoting the post-fixed expression of the tree rooted by this node. If the specified array is not {@code null}, variables that
	 * are present in the tree are replaced by their parameterized forms {@code %i}.
	 * 
	 * @param scopeForAbstraction
	 *            if not {@code null}, the scope on which an abstract post-fixed expression is built
	 * @return a string denoting the post-fixed expression of the tree rooted by this node.
	 */
	public abstract String toPostfixExpression(IVar[] scopeForAbstraction);

	/**
	 * Returns a string denoting the functional expression of the tree rooted by this node. If the specified array is not {@code null}, parameters
	 * that are present in the tree are replaced by their corresponding arguments.
	 * 
	 * @param argsForConcretization
	 *            if not {@code null}, the list of arguments to be used as values for the parameters that are present in the tree rooted by this node
	 * @return a string denoting the functional expression of the tree rooted by this node
	 */
	public abstract String toFunctionalExpression(Object[] argsForConcretization);

	@Override
	public String toString() {
		return toFunctionalExpression(null);
	}
}
