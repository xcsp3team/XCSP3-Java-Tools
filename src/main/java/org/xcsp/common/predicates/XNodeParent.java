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
			Arrays.sort(newSons); // sons are sorted
		TypeExpr newType = type;
		// Fr non-symmetric binary relational operators, we swap sons and arithmetically inverse the operator if the sons are not ordered
		if (newSons.length == 2 && (type.isNonSymmetricRelationalOperator()) && newSons[0].compareTo(newSons[1]) > 0) {
			newType = type.arithmeticInversion();
			Utilities.swap(newSons, 0, 1);
		}

		// Now, some specific reformulation rules are applied
		if (newSons.length == 1 && newType == TypeExpr.ABS && newSons[0].type == TypeExpr.SUB) // abs(sub(...)) becomes dist(...)
			return new XNodeParent<V>(TypeExpr.DIST, ((XNodeParent<V>) newSons[0]).sons);
		if (newSons.length == 1 && newType == TypeExpr.NOT && newSons[0].type == TypeExpr.NOT) // not(not(...)) becomes ...
			return ((XNodeParent<V>) newSons[0]).sons[0];
		if (newType == TypeExpr.NOT) { // not(lt(...)) becomes ge(...), not(eq(...)) becomes ne(...), and so on.
			TypeExpr invertedType = newSons[0].type.logicalInversion();
			if (invertedType != null)
				return new XNodeParent<V>(invertedType, ((XNodeParent<V>) newSons[0]).sons);
		}
		if (newSons.length == 2 && newType.isRelationalOperator()) {
			// First, we replace sub by add when possible
			if (newSons[0].type == TypeExpr.SUB && newSons[1].type == TypeExpr.SUB) {
				XNode<V> subterm = ((XNodeParent<V>) newSons[0]).sons[1];
				((XNodeParent<V>) newSons[0]).sons[1] = ((XNodeParent<V>) newSons[1]).sons[1];
				((XNodeParent<V>) newSons[1]).sons[1] = subterm;
				newSons[0].type = TypeExpr.ADD;
				newSons[1].type = TypeExpr.ADD;
				if (newSons[0].compareTo(newSons[1]) > 0) {
					newType = newType.arithmeticInversion();
					Utilities.swap(newSons, 0, 1);
				}
			} else if (newSons[0].type == TypeExpr.SUB || newSons[1].type == TypeExpr.SUB) {
				int ind1 = newSons[0].type == TypeExpr.SUB ? 0 : 1, ind2 = ind1 == 0 ? 1 : 0;
				XNode<V> subterm = ((XNodeParent<V>) newSons[ind1]).sons[1];
				XNode<V> a = new XNodeParent<V>(TypeExpr.ADD,
						newSons[ind2].compareTo(subterm) > 0 ? new XNode[] { subterm, newSons[ind2] } : new XNode[] { newSons[ind2], subterm });
				XNode<V> b = ((XNodeParent<V>) newSons[ind1]).sons[0];
				if (a.compareTo(b) > 0) {
					newType = newType.arithmeticInversion();
					newSons[0] = b;
					newSons[1] = a;
				} else {
					newSons[0] = a;
					newSons[1] = b;
				}
			}
			// next, we remove some add when possible
			if (newSons[0].type == TypeExpr.ADD) {
				XNode<?>[] ns = ((XNodeParent<V>) newSons[0]).sons;
				if (ns[0].type == TypeExpr.LONG && ns[1].type == TypeExpr.LONG) {
					newSons[0] = new XNodeLeaf<V>(TypeExpr.LONG,
							((Long) ((XNodeLeaf<?>) ns[0]).value).intValue() + ((Long) ((XNodeLeaf<?>) ns[1]).value).intValue());
				}
			}
			if (newSons[1].type == TypeExpr.ADD) {
				XNode<?>[] ns = ((XNodeParent<V>) newSons[1]).sons;
				if (ns[0].type == TypeExpr.LONG && ns[1].type == TypeExpr.LONG) {
					newSons[1] = new XNodeLeaf<V>(TypeExpr.LONG,
							(long) ((Long) ((XNodeLeaf<?>) ns[0]).value).intValue() + ((Long) ((XNodeLeaf<?>) ns[1]).value).intValue());
				}
			}
			if (newSons[0].type == TypeExpr.ADD && newSons[1].type == TypeExpr.ADD) {
				XNode<?>[] ns1 = ((XNodeParent<V>) newSons[0]).sons;
				XNode<?>[] ns2 = ((XNodeParent<V>) newSons[1]).sons;
				if (ns1[1].type == TypeExpr.LONG && ns2[1].type == TypeExpr.LONG) {
					((XNodeLeaf<?>) ns1[1]).value = (long) ((Long) ((XNodeLeaf<?>) ns1[1]).value).intValue()
							- ((Long) ((XNodeLeaf<?>) ns2[1]).value).intValue();
					newSons[1] = (XNode<V>) ns2[0];
				}
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
