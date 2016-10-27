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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Interfaces.IVar;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;

/**
 * The class used for representing a parent node in a syntactic tree.
 * 
 * @author Christophe Lecoutre
 */
public final class XNodeParent<V extends IVar> extends XNode<V> {

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof XNodeParent))
			return false;
		XNode<?>[] objSons = ((XNodeParent<?>) obj).sons;
		return type == ((XNodeParent<?>) obj).type && sons.length == objSons.length
				&& IntStream.range(0, sons.length).allMatch(i -> sons[i].equals(objSons[i]));
	}

	@Override
	public int compareTo(XNode<V> o) {
		if (type.ordinal() != o.type.ordinal())
			return Integer.compare(type.ordinal(), o.type.ordinal());
		XNodeParent<V> node = (XNodeParent<V>) o;
		if (sons.length < node.sons.length)
			return -1;
		if (sons.length > node.sons.length)
			return 1;
		return IntStream.range(0, sons.length).map(i -> sons[i].compareTo(node.sons[i])).filter(v -> v != 0).findFirst().orElse(0);
		// for (int i = 0; i < sons.length; i++) {
		// int res = sons[i].compareTo(node.sons[i]);
		// if (res != 0)
		// return res;
		// }
		// return 0;
	}

	/** The sons of the node. */
	public final XNode<V>[] sons;

	/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
	public XNodeParent(TypeExpr type, XNode<V>[] sons) {
		super(type);
		this.sons = sons;
		Utilities.control(type.arityMax > 0, "Pb with this node that shoulb be a parent");
	}

	@Override
	public int size() {
		return 1 + Stream.of(sons).mapToInt(c -> c.size()).sum();
	}

	@Override
	public int maxParameterNumber() {
		return Stream.of(sons).mapToInt(c -> c.maxParameterNumber()).max().orElse(-1);
	}

	@Override
	public LinkedHashSet<V> collectVars(LinkedHashSet<V> set) {
		Stream.of(sons).forEach(s -> s.collectVars(set));
		return set;
	}

	@Override
	public XNode<V> canonization() {
		XNode<V>[] t = Stream.of(sons).map(s -> s.canonization()).toArray(XNode[]::new);
		if (type.isSymmetric())
			Arrays.sort(t);
		// Now, some specific reformulation rules are applied
		// First, for non-symmetric binary relational operators, we swap sons and reverse the operator if the sons are not ordered
		if (t.length == 2 && (type == TypeExpr.LT || type == TypeExpr.LE || type == TypeExpr.GE || type == TypeExpr.GT) && t[0].compareTo(t[1]) > 0) {
			TypeExpr newType = type == TypeExpr.LT ? TypeExpr.GT : type == TypeExpr.LE ? TypeExpr.GE : type == TypeExpr.GE ? TypeExpr.LE : TypeExpr.LT;
			return new XNodeParent<V>(newType, Utilities.swap(t, 0, 1));
		}
		if (t.length == 1 && type == TypeExpr.ABS && t[0].type == TypeExpr.SUB) // abs(sub(...)) becomes dist(...)
			return new XNodeParent<V>(TypeExpr.DIST, ((XNodeParent<V>) t[0]).sons);
		if (type == TypeExpr.NOT) { // not(eq(...)) becomes ne(...), and so on.
			TypeExpr tp = t[0].type;
			TypeExpr newType = tp == TypeExpr.LT ? TypeExpr.GT
					: tp == TypeExpr.LE ? TypeExpr.GE
							: tp == TypeExpr.GE ? TypeExpr.LE
									: tp == TypeExpr.GT ? TypeExpr.LT
											: tp == TypeExpr.EQ ? TypeExpr.NE
													: tp == TypeExpr.NE ? TypeExpr.EQ
															: tp == TypeExpr.IN ? TypeExpr.NOTIN : tp == TypeExpr.NOTIN ? TypeExpr.IN : null;
			// code for set operators to add later
			if (newType != null)
				return new XNodeParent<V>(newType, ((XNodeParent<V>) t[0]).sons);
		}

		return new XNodeParent<V>(type, t);
	}

	private XNode<V> buildNewTreeUsing(Function<XNode<V>, XNode<V>> f) {
		return new XNodeParent<V>(type, Stream.of(sons).map(f).toArray(XNode[]::new));
	}

	@Override
	public XNode<V> abstraction(List<Object> args, boolean abstractIntegers, boolean multiOccurrences) {
		return buildNewTreeUsing(s -> s.abstraction(args, abstractIntegers, multiOccurrences));
	}

	@Override
	public XNode<V> concretization(Object[] args) {
		return buildNewTreeUsing(s -> s.concretization(args));
	}

	@Override
	public XNode<V> replaceSymbols(Map<String, Integer> mapOfSymbols) {
		return buildNewTreeUsing(s -> s.replaceSymbols(mapOfSymbols));
	}

	@Override
	public Object valueOfFirstLeafOfType(TypeExpr type) {
		return Stream.of(sons).map(son -> son.valueOfFirstLeafOfType(type)).filter(o -> o != null).findFirst().orElse(null);
	}

	@Override
	public String toPostfixExpression(IVar[] scopeForAbstraction) {
		String s = Stream.of(sons).map(c -> c.toPostfixExpression(scopeForAbstraction)).collect(Collectors.joining(" "));
		return s + " " + ((type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + type.toString().toLowerCase());
	}

	@Override
	public String toFunctionalExpression(Object[] argsForConcretization) {
		return type.toString().toLowerCase() + "(" + Stream.of(sons).map(c -> c.toFunctionalExpression(argsForConcretization)).collect(Collectors.joining(","))
				+ ")";
	}
}
