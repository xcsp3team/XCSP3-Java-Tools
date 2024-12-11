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

import static org.xcsp.common.Types.TypeExpr.ABS;
import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.DIST;
import static org.xcsp.common.Types.TypeExpr.DIV;
import static org.xcsp.common.Types.TypeExpr.EQ;
import static org.xcsp.common.Types.TypeExpr.GE;
import static org.xcsp.common.Types.TypeExpr.GT;
import static org.xcsp.common.Types.TypeExpr.IF;
import static org.xcsp.common.Types.TypeExpr.LE;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.LT;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MOD;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.NE;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.POW;
import static org.xcsp.common.Types.TypeExpr.SET;
import static org.xcsp.common.Types.TypeExpr.SPECIAL;
import static org.xcsp.common.Types.TypeExpr.SQR;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.Types.TypeExpr.VAR;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.Range;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeLogicalOperator;
import org.xcsp.common.Types.TypeUnaryArithmeticOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.enumerations.EnumerationCartesian;
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
		return new XNodeLeaf<>(LONG, value);
	}

	public static <V extends IVar> XNodeLeaf<V> varLeaf(V value) {
		return new XNodeLeaf<>(VAR, value);
	}

	public static <V extends IVar> XNodeLeaf<V> specialLeaf(String value) {
		return new XNodeLeaf<>(SPECIAL, value);
	}

	public static <V extends IVar> Object logicallyInverse(XNode<V> node1, XNode<V> node2) {
		TypeExpr tp1 = node1.type, tp2 = node2.type;
		if (tp1 == VAR)
			return tp2 == NOT && node1.compareTo(node2.sons[0]) == 0;
		if (tp2 == VAR)
			return tp1 == NOT && node2.compareTo(node1.sons[0]) == 0;
		if (tp1.isLogicallyInvertible() && tp2.isLogicallyInvertible()) {
			if (tp1.logicalInversion() == tp2)
				return node1.sons.length == node2.sons.length && IntStream.range(0, node1.sons.length).map(i -> node1.sons[i].compareTo(node2.sons[i]))
						.filter(v -> v != 0).findFirst().orElse(0) == 0;
			if ((tp1 == LT && tp2 == LE) || (tp1 == LE && tp2 == LT) || (tp1 == GT && tp2 == GE) || (tp1 == GE && tp2 == GT))
				return node1.sons[0].compareTo(node2.sons[1]) == 0 && node1.sons[1].compareTo(node2.sons[0]) == 0;
			if ((tp1 == EQ && tp2 == EQ) || (tp1 == NE && tp2 == NE)) {
				if (node1.sons.length == 2 && node2.sons.length == 2 && node1.sons[0].compareTo(node2.sons[0]) == 0 && node1.sons[1].type == LONG
						&& node2.sons[1].type == LONG) {
					int v1 = node1.sons[1].val(0), v2 = node2.sons[1].val(0);
					if ((v1 == 0 && v2 == 1) || (v1 == 1 && v2 == 0))
						return node1.sons[0];  // maybe useful to determine if the tree/variable must be limited to {0,1}
				}
			}
		}
		return false;
	}

	// ************************************************************************
	// ***** Fields, Constructor and Methods
	// ************************************************************************

	/**
	 * The type of the node. For example, it can be {@code ADD}, {@code NOT}, or {@code LONG}.
	 */
	public TypeExpr type;

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

	public boolean checkTypeOfSons(TypeExpr... types) {
		assert types.length > 0;
		if (sons == null || sons.length != types.length)
			return false;
		for (int i = 0; i < sons.length; i++)
			if (sons[i].type != types[i])
				return false;
		return true;
	}

	public final XNode<V> logicalInversion() {
		assert type.isLogicallyInvertible();
		type = type.logicalInversion();
		return this;
	}

	public final XNode<V> logicalInversionShallowCopy() {
		assert type.isLogicallyInvertible();
		return node(type.logicalInversion(), sons);
	}

	/**
	 * Returns the arity of this node, i.e., the number of sons.
	 * 
	 * @return the arity of this node
	 */
	public final int arity() {
		return sons == null ? 0 : sons.length;
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
	 * Builds a list with the sequence of variables encountered during a depth-first exploration of the tree rooted by this node. Multiple occurrences of the
	 * same variables are possible.
	 * 
	 * @return the list of encountered variables during a depth-first exploration
	 */
	public final LinkedList<V> listOfVars() {
		return allNodesSuchThat(s -> s.type == VAR).stream().map(n -> (V) ((XNodeLeaf<V>) n).value).collect(Collectors.toCollection(LinkedList<V>::new));
	}

	/**
	 * Builds a list with the sequence of values (long integers) encountered during a depth-first exploration of the tree rooted by this node. Multiple
	 * occurrences of the same values are possible.
	 * 
	 * @return the list of encountered values (integers) during a depth-first exploration
	 */
	public final LinkedList<Long> listOfVals() {
		return allNodesSuchThat(s -> s.type == LONG).stream().map(n -> (Long) ((XNodeLeaf<V>) n).value).collect(Collectors.toCollection(LinkedList<Long>::new));
	}

	/**
	 * Returns the (i+1)th variable encountered while traversing (in a depth-first manner) the tree rooted by this node, or {@code null} if such variable does
	 * not exist.
	 * 
	 * @param i
	 *            the index, starting at 0, of a variable
	 * @return the (i+1)th variable encountered in the tree rooted by this node
	 */
	public final V var(int i) {
		if (i == 0) {
			XNodeLeaf<V> f = (XNodeLeaf<V>) firstNodeSuchThat(n -> n.type == VAR);
			return f == null ? null : (V) f.value;
		}
		LinkedList<V> list = listOfVars();
		return i >= list.size() ? null : list.get(i);
	}

	/**
	 * Returns the (i+1)th value encountered while traversing (in a depth-first manner) the tree rooted by this node, or {@code null} if such value does not
	 * exist.
	 * 
	 * @param i
	 *            the index, starting at 0, of a value
	 * @return the (i+1)th value encountered in the tree rooted by this node
	 */
	public final Integer val(int i) {
		if (i == 0) {
			XNodeLeaf<V> f = (XNodeLeaf<V>) firstNodeSuchThat(n -> n.type == LONG);
			return f == null ? null : Utilities.safeInt((Long) f.value);
		}
		LinkedList<Long> list = listOfVals();
		return i >= list.size() ? null : Utilities.safeInt(list.get(i));
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

	public final TypeConditionOperatorSet setop(int i) {
		if (i == 0) {
			XNode<V> f = firstNodeSuchThat(n -> n.type.isSetOperator());
			return f == null ? null : f.type.toSetop();
		}
		LinkedList<TypeConditionOperatorSet> list = allNodesSuchThat(s -> s.type.isSetOperator()).stream().map(n -> n.type.toSetop())
				.collect(Collectors.toCollection(LinkedList<TypeConditionOperatorSet>::new));
		return i >= list.size() ? null : list.get(i);
	}

	public final TypeUnaryArithmeticOperator unalop(int i) {
		if (i == 0) {
			XNode<V> f = firstNodeSuchThat(n -> n.type.isUnaryArithmeticOrLogicOperator());
			return f == null ? null : f.type.toUnalop();
		}
		LinkedList<TypeUnaryArithmeticOperator> list = allNodesSuchThat(s -> s.type.isUnaryArithmeticOrLogicOperator()).stream().map(n -> n.type.toUnalop())
				.collect(Collectors.toCollection(LinkedList<TypeUnaryArithmeticOperator>::new));
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

	public final TypeLogicalOperator logop(int i) {
		if (i == 0) {
			XNode<V> f = firstNodeSuchThat(n -> n.type.isLogicalOperator());
			return f == null ? null : f.type.toLogop();
		}
		LinkedList<TypeLogicalOperator> list = allNodesSuchThat(s -> s.type.isLogicalOperator()).stream().map(n -> n.type.toLogop())
				.collect(Collectors.toCollection(LinkedList<TypeLogicalOperator>::new));
		return i >= list.size() ? null : list.get(i);
	}

	/**
	 * Returns the list of variables in the tree rooted by this node, in the order they are encountered, or {@code null} if there is none. Contrary to vars(),
	 * the same variables may occur several times.
	 * 
	 * @return the list of variables in the order they are encountered
	 */
	public final V[] arrayOfVars() {
		LinkedList<V> list = listOfVars();
		return list.size() == 0 ? null : list.stream().toArray(s -> Utilities.buildArray(list.iterator().next().getClass(), s));
	}

	/**
	 * Returns the list of values (integers) in the tree rooted by this node, in the order they are encountered, or {@code null} if there is none. Of course,
	 * the same values may occur several times.
	 * 
	 * @return the list of values (integers) in the order they are encountered
	 */
	public final int[] arrayOfVals() {
		LinkedList<Long> list = listOfVals();
		return list.size() == 0 ? new int[0] : list.stream().mapToInt(l -> Utilities.safeInt(l)).toArray();
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
	 * Return {@code true} iff the sequence of variables (without duplicates) encountered in the tree rooted by this node is exactly the specified array.
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
	 * Returns a new tree, equivalent to the tree rooted by this node, and in canonical form. For example, commutative operators will take variables before
	 * integers as operands; actually, the total ordinal order over constants in {@code TypeExpr} is used. Some simplifications are also performed; for example,
	 * {@code not(eq(x,y))} becomes {@code ne(x,y)}.
	 * 
	 * @return a new tree, equivalent to the tree rooted by this node, and in canonical form
	 */
	public abstract XNode<V> canonization();

	/**
	 * Returns a new tree representing an abstraction of the tree rooted by this node. Variables are replaced by parameters, and integers are also replaced by
	 * parameters (if the first specified Boolean is true). Occurrences of the same variables are replaced by the same parameter (if the second specified
	 * Boolean is true). Values replaced by parameters are added to the specified list.
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
	 * Returns a new tree representing a concretization of the tree rooted by this node. Any parameter of value i is replaced by the ith object in the specified
	 * list of arguments.
	 * 
	 * @param args
	 *            the list of arguments to be used as values for the parameters that are present in the tree rooted by this node
	 * @return a new tree representing a concretization of the tree rooted by this node
	 */
	public abstract XNode<V> concretization(Object[] args);

	/**
	 * Returns a string denoting the post-fixed expression of the tree rooted by this node. If the specified array is not {@code null}, variables that are
	 * present in the tree are replaced by their parameterized forms {@code %i}.
	 * 
	 * @param scopeForAbstraction
	 *            if not {@code null}, the scope on which an abstract post-fixed expression is built
	 * @return a string denoting the post-fixed expression of the tree rooted by this node.
	 */
	public abstract String toPostfixExpression(IVar[] scopeForAbstraction);

	/**
	 * Returns a string denoting the functional expression of the tree rooted by this node. If the specified array is not {@code null}, parameters that are
	 * present in the tree are replaced by their corresponding arguments.
	 * 
	 * @param argsForConcretization
	 *            if not {@code null}, the list of arguments to be used as values for the parameters that are present in the tree rooted by this node
	 * @return a string denoting the functional expression of the tree rooted by this node
	 */
	public abstract String toFunctionalExpression(Object[] argsForConcretization);

	private static Range negRange(Range r) {
		assert r.step == 1;
		return new Range(-r.stop + 1, -r.start + 1);
	}

	private static Range absRange(Range r) {
		assert r.step == 1;
		return new Range(r.contains(0) ? 0 : Math.min(Math.abs(r.start), Math.abs(r.stop - 1)), Math.max(Math.abs(r.start), Math.abs(r.stop - 1)) + 1);
	}

	private static Range addRange(Range r1, Range r2) {
		assert r1.step == 1 && r2.step == 1;
		return new Range(r1.start + r2.start, r1.stop + r2.stop - 1);
	}

	private static Object possibleRange(int[] s) {
		int[] l = IntStream.of(s).sorted().distinct().toArray();
		int d = l[l.length - 1] - l[0] + 1;
		return d == l.length ? new Range(l[0], l[l.length - 1] + 1) : l;
	}

	public Object possibleValues() {
		if (type.isPredicateOperator())
			return new Range(0, 2); // we use a range instead of [0,1] because it simplifies computation (see code
									// below, where we try to reason the most possible with ranges)
		if (type.arityMin == 0 && type.arityMax == 0) {
			if (type == VAR) {
				Var x = (Var) (((XNodeLeaf<?>) this).oldValue != null ? ((XNodeLeaf<?>) this).oldValue : ((XNodeLeaf<?>) this).value);
				Object av = x.allValues(); // either a range or a sorted array of distinct integers is returned
				if (av instanceof Range)
					return av;
				int[] values = (int[]) av;
				return values.length == 1 ? new Range(values[0], values[0] + 1)
						: values.length == 2 && values[0] + 1 == values[1] ? new Range(values[0], values[1] + 1) : values;
			}
			if (type == LONG) {
				int value = Utilities.safeInt(((Long) ((XNodeLeaf<?>) this).value).longValue());
				return new Range(value, value + 1); // we use a range instead of a singleton list because it simplifies
													// computation (see code below)
			}
			Utilities.control(false, "no such 0-ary type " + type + " is expected");
		}
		if (type.arityMin == 1 && type.arityMax == 1) {
			Object pv = sons[0].possibleValues();
			if (type == NEG) {
				if (pv instanceof Range)
					return negRange((Range) pv);
				int[] t = (int[]) pv;
				return IntStream.range(0, t.length).map(i -> -t[t.length - i - 1]).toArray();
			}
			if (type == ABS) {
				if (pv instanceof Range)
					return absRange((Range) pv);
				int[] t = (int[]) pv;
				return possibleRange(IntStream.of(t).map(v -> Math.abs(v)).toArray());
			}
			if (type == SQR) {
				int[] t = pv instanceof Range ? ((Range) pv).toArray() : (int[]) pv;
				return possibleRange(IntStream.of(t).map(v -> v * v).toArray());
			}
			Utilities.control(false, "no such 1-ary type " + type + " is expected");
		}
		if (type.arityMin == 2 && type.arityMax == 2) {
			Object pv1 = sons[0].possibleValues(), pv2 = sons[1].possibleValues();
			if (pv1 instanceof Range && pv2 instanceof Range) {
				if (type == SUB)
					return addRange((Range) pv1, negRange((Range) pv2));
				if (type == DIST)
					return absRange(addRange((Range) pv1, negRange((Range) pv2)));
			}
			Utilities.control(type == SUB || type == DIV || type == MOD || type == POW || type == DIST, "no such 2-ary type " + type + " is expected");
			Set<Integer> set = new HashSet<>();
			int[] t1 = pv1 instanceof Range ? ((Range) pv1).toArray() : (int[]) pv1;
			int[] t2 = pv2 instanceof Range ? ((Range) pv2).toArray() : (int[]) pv2;
			for (int v1 : t1)
				for (int v2 : t2) {
					if (type == SUB)
						set.add(v1 - v2);
					if (type == DIV)
						set.add(v1 / v2);
					if (type == MOD)
						set.add(v1 % v2);
					if (type == POW)
						set.add((int) Math.pow(v1, v2)); // TODO control here
					if (type == DIST)
						set.add(Math.abs(v1 - v2));
				}
			return possibleRange(set.stream().mapToInt(i -> i).toArray());
		}
		if (type == IF) {
			Object pv1 = sons[1].possibleValues(), pv2 = sons[2].possibleValues(); // sons[0] is for the condition
			if (pv1 instanceof Range && pv2 instanceof Range) {
				int s1 = ((Range) pv1).start, e1 = ((Range) pv1).stop;
				int s2 = ((Range) pv2).start, e2 = ((Range) pv2).stop;
				if (Math.max(s1, s2) <= Math.min(e1, e2))
					return new Range(Math.min(s1, s2), Math.max(e1, e2));
			}
			int[] t1 = pv1 instanceof Range ? ((Range) pv1).toArray() : (int[]) pv1;
			int[] t2 = pv2 instanceof Range ? ((Range) pv2).toArray() : (int[]) pv2;
			return possibleRange(IntStream.range(0, t1.length + t2.length).map(i -> i < t1.length ? t1[i] : t2[i - t1.length]).toArray());
		}
		if (type.arityMin == 2 && type.arityMax == Integer.MAX_VALUE) {
			if (type == MUL && sons.length == 2 && sons[0].type == VAR && sons[1].type == VAR
					&& ((XNodeLeaf<?>) sons[0]).value == ((XNodeLeaf<?>) sons[1]).value)
				return node(SQR, sons[0]).possibleValues();
			Object[] pvs = Stream.of(sons).map(t -> t.possibleValues()).toArray();
			if (Stream.of(pvs).allMatch(pv -> pv instanceof Range)) {
				if (type == ADD)
					return Stream.of(pvs).reduce((r1, r2) -> ((Range) r1).add((Range) r2)).get();
				if (type == MIN)
					return new Range(Stream.of(pvs).mapToInt(pv -> ((Range) pv).start).min().getAsInt(),
							Stream.of(pvs).mapToInt(pv -> ((Range) pv).stop).min().getAsInt());
				if (type == MAX)
					return new Range(Stream.of(pvs).mapToInt(pv -> ((Range) pv).start).max().getAsInt(),
							Stream.of(pvs).mapToInt(pv -> ((Range) pv).stop).max().getAsInt());
			}
			Utilities.control(type == ADD || type == MUL || type == MIN || type == MAX, "the type " + type + " is currently not implemented");
			Set<Integer> set = new HashSet<>();
			int[][] values = Stream.of(pvs).map(pv -> pv instanceof Range ? ((Range) pv).toArray() : (int[]) pv).toArray(int[][]::new);
			EnumerationCartesian ec = new EnumerationCartesian(values, false);
			while (ec.hasNext()) {
				int[] t = ec.next();
				if (type == ADD)
					set.add(IntStream.of(t).sum());
				if (type == MUL)
					set.add(IntStream.of(t).reduce((a, b) -> a * b).getAsInt());
				if (type == MIN)
					set.add(IntStream.of(t).min().getAsInt());
				if (type == MAX)
					set.add(IntStream.of(t).max().getAsInt());
			}
			return possibleRange(set.stream().mapToInt(i -> i).toArray());
		}
		if (type == SET) {
			int[][] values = Stream.of(sons).map(t -> t.possibleValues()).map(pv -> pv instanceof Range ? ((Range) pv).toArray() : (int[]) pv)
					.toArray(int[][]::new);
			Set<Integer> set = new HashSet<>();
			for (int[] t : values)
				for (int v : t)
					set.add(v);
			return possibleRange(set.stream().mapToInt(i -> i).toArray());
		}

		Utilities.control(false, "The operator " + type + " currently not implemented");
		return null;
	}

	@Override
	public String toString() {
		return toFunctionalExpression(null);
	}
}
