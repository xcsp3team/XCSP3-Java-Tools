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
		Utilities.control(type.arityMax > 0, "Pb with this node that should be a parent");
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
		XNode<V>[] newSons = Stream.of(sons).map(s -> s.canonization()).toArray(XNode[]::new); // sons are made canonical
		if (type.isSymmetricOperator())
			Arrays.sort(newSons); // sons are sorted if the type of the node is symmetric
		TypeExpr newType = type;
		// sons are potentially sorted if the type corresponds to a non-symmetric binary relational operator (in that case, we swap sons and arithmetically
		// inverse the operator)
		if (newSons.length == 2 && type.isNonSymmetricRelationalOperator()) {
			if (newSons[0].type != newSons[1].type) {
				if (newSons[0].compareTo(newSons[1]) > 0) {
					newType = type.arithmeticInversion();
					Utilities.swap(newSons, 0, 1);
				}
			} else if (type.arithmeticInversion().ordinal() < type.ordinal()
					|| (type.arithmeticInversion().ordinal() == type.ordinal() && newSons[0].compareTo(newSons[1]) > 0)) {
				newType = type.arithmeticInversion();
				Utilities.swap(newSons, 0, 1);
			}
		}

		// Now, some specific reformulation rules are applied
		if (newType == TypeExpr.ABS && newSons[0].type == TypeExpr.SUB) // abs(sub(...)) becomes dist(...)
			return new XNodeParent<V>(TypeExpr.DIST, ((XNodeParent<V>) newSons[0]).sons);
		if (newType == TypeExpr.NOT && newSons[0].type == TypeExpr.NOT) // not(not(...)) becomes ...
			return ((XNodeParent<V>) newSons[0]).sons[0];
		if (newType == TypeExpr.NOT) { // not(lt(...)) becomes ge(...), not(eq(...)) becomes ne(...), and so on.
			TypeExpr invertedType = newSons[0].type.logicalInversion(); // null if the type does not allow that
			if (invertedType != null)
				return new XNodeParent<V>(invertedType, ((XNodeParent<V>) newSons[0]).sons);
		}
		if (newType == TypeExpr.ADD) {
			if (newSons.length == 2 && newSons[0].type == TypeExpr.LONG && newSons[1].type == TypeExpr.LONG)
				return new XNodeLeaf<V>(TypeExpr.LONG,
						(long) ((Long) ((XNodeLeaf<?>) newSons[0]).value).intValue() + ((Long) ((XNodeLeaf<?>) newSons[1]).value).intValue());
		}

		if (newSons.length == 2 && newType.isRelationalOperator()) {
			// First, we replace sub by add when possible
			if (newSons[0].type == TypeExpr.SUB && newSons[1].type == TypeExpr.SUB) {
				XNode<V> a = new XNodeParent<V>(TypeExpr.ADD, new XNode[] { ((XNodeParent<V>) newSons[0]).sons[0], ((XNodeParent<V>) newSons[1]).sons[1] });
				XNode<V> b = new XNodeParent<V>(TypeExpr.ADD, new XNode[] { ((XNodeParent<V>) newSons[1]).sons[0], ((XNodeParent<V>) newSons[0]).sons[1] });
				return new XNodeParent<V>(newType, new XNode[] { a, b }).canonization();
			} else if (newSons[1].type == TypeExpr.SUB) {
				XNode<V> a = new XNodeParent<V>(TypeExpr.ADD, new XNode[] { newSons[0], ((XNodeParent<V>) newSons[1]).sons[1] });
				XNode<V> b = ((XNodeParent<V>) newSons[1]).sons[0];
				return new XNodeParent<V>(newType, new XNode[] { a, b }).canonization();
			} else if (newSons[0].type == TypeExpr.SUB && (((XNodeParent<V>) newSons[0]).sons[1].type != TypeExpr.VAR || newSons[1].type != TypeExpr.LONG)) {
				// we avoid swapping a var at left with a val at right
				XNode<V> a = ((XNodeParent<V>) newSons[0]).sons[0];
				XNode<V> b = new XNodeParent<V>(TypeExpr.ADD, new XNode[] { newSons[1], ((XNodeParent<V>) newSons[0]).sons[1] });
				return new XNodeParent<V>(newType, new XNode[] { a, b }).canonization();
			}
			// next, we remove some add when possible
			if (newSons[0].type == TypeExpr.ADD && newSons[1].type == TypeExpr.ADD) {
				XNode<?>[] ns1 = ((XNodeParent<V>) newSons[0]).sons, ns2 = ((XNodeParent<V>) newSons[1]).sons;
				if (ns1.length == 2 && ns2.length == 2 && ns1[1].type == TypeExpr.LONG && ns2[1].type == TypeExpr.LONG) {
					((XNodeLeaf<?>) ns1[1]).value = (long) ((Long) ((XNodeLeaf<?>) ns1[1]).value).intValue()
							- ((Long) ((XNodeLeaf<?>) ns2[1]).value).intValue();
					newSons[1] = (XNode<V>) ns2[0];
					return new XNodeParent<V>(newType, newSons).canonization();
				}
			}
			// we move variables to the left
			if (newSons[0].type == TypeExpr.ADD && ((XNodeParent<V>) newSons[0]).sons[1].type == TypeExpr.LONG && newSons[1].type == TypeExpr.VAR) {
				XNode<V> a = new XNodeParent<V>(TypeExpr.SUB, new XNode[] { ((XNodeParent<V>) newSons[0]).sons[0], newSons[1] });
				XNode<V> b = new XNodeLeaf<V>(TypeExpr.LONG, -(long) ((Long) ((XNodeLeaf<?>) ((XNodeParent<V>) newSons[0]).sons[1]).value).intValue());
				return new XNodeParent<V>(newType, new XNode[] { a, b }).canonization();
			}
		}
		return new XNodeParent<V>(newType, newSons);
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
	public <T> T valueOfFirstLeafOfType(TypeExpr type) {
		return (T) Stream.of(sons).map(son -> (T) son.valueOfFirstLeafOfType(type)).filter(o -> o != null).findFirst().orElse(null);
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
