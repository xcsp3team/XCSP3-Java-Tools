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
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.DIST;
import static org.xcsp.common.Types.TypeExpr.DJOINT;
import static org.xcsp.common.Types.TypeExpr.EQ;
import static org.xcsp.common.Types.TypeExpr.IFF;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.Types.TypeExpr.XOR;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
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
	}

	/** The sons of the node. */
	public final XNode<V>[] sons;

	/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
	public XNodeParent(TypeExpr type, XNode<V>[] sons) {
		super(type);
		this.sons = sons;
		Utilities.control(type.arityMax > 0, "Pb with this node that should be a parent");
	}

	public XNodeParent(TypeExpr type, List<XNode<V>> sons) {
		this(type, sons.toArray(new XNode[sons.size()]));
	}

	/** Builds a parent node for a syntactic tree, with the specified type and the two specified sons. */
	public XNodeParent(TypeExpr type, XNode<V> son1, XNode<V> son2) {
		this(type, Arrays.asList(son1, son2)); // new XNode[] { son1, son2 });
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
		if (newSons.length == 2 && type.isNonSymmetricRelationalOperator() && (type.arithmeticInversion().ordinal() < type.ordinal()
				|| (type.arithmeticInversion().ordinal() == type.ordinal() && newSons[0].compareTo(newSons[1]) > 0))) {
			newType = type.arithmeticInversion();
			Utilities.swap(newSons, 0, 1);
		}
		// Now, some specific reformulation rules are applied
		if (newType == ABS && newSons[0].type == SUB) // abs(sub(...)) becomes dist(...)
			return new XNodeParent<V>(DIST, ((XNodeParent<V>) newSons[0]).sons);
		if (newType == NOT && newSons[0].type == NOT) // not(not(...)) becomes ...
			return ((XNodeParent<V>) newSons[0]).sons[0];
		if (newType == NEG && newSons[0].type == NEG) // neg(neg(...)) becomes ...
			return ((XNodeParent<V>) newSons[0]).sons[0];
		if (newType == NOT && newSons[0].type.logicalInversion() != null) // not(lt(...)) becomes ge(...), not(eq(...)) becomes ne(...), and so on.
			return new XNodeParent<V>(newSons[0].type.logicalInversion(), ((XNodeParent<V>) newSons[0]).sons);
		if (newSons.length == 1 && (newType == ADD || newType == MUL || newType == MIN || newType == MAX || newType == EQ || newType == AND || newType == OR
				|| newType == XOR || newType == IFF)) // certainly can happen during the canonization process
			return newSons[0];

		if (newType == ADD) { // we merge long (similar operations possible for MUL, MIN, ...)
			if (newSons.length >= 2 && newSons[newSons.length - 1].type == LONG && newSons[newSons.length - 2].type == LONG) {
				List<XNode<V>> list = IntStream.range(0, newSons.length - 2).mapToObj(j -> newSons[j]).collect(Collectors.toList());
				list.add(new XNodeLeaf<V>(LONG, (long) newSons[newSons.length - 1].firstVal() + newSons[newSons.length - 2].firstVal()));
				return new XNodeParent<V>(ADD, list).canonization();
			}
		}
		// Then, we merge operators when possible; for example add(add(x,y),z) becomes add(x,y,z)
		if (newType.isSymmetricOperator() && newType != DIST && newType != DJOINT) {
			for (int i = 0; i < newSons.length; i++) {
				if (newSons[i].type == newType) {
					List<XNode<V>> list = IntStream.range(0, i - 1).mapToObj(j -> newSons[j]).collect(Collectors.toList());
					Stream.of(((XNodeParent<V>) newSons[i]).sons).forEach(s -> list.add(s));
					IntStream.range(i + 1, newSons.length).mapToObj(j -> newSons[j]).forEach(s -> list.add(s));
					return new XNodeParent<V>(newType, list).canonization();
				}
			}
		}
		if (newSons.length == 2 && newType.isRelationalOperator()) {
			// First, we replace sub by add when possible
			if (newSons[0].type == SUB && newSons[1].type == SUB) {
				XNode<V> a = new XNodeParent<V>(ADD, ((XNodeParent<V>) newSons[0]).sons[0], ((XNodeParent<V>) newSons[1]).sons[1]);
				XNode<V> b = new XNodeParent<V>(ADD, ((XNodeParent<V>) newSons[1]).sons[0], ((XNodeParent<V>) newSons[0]).sons[1]);
				return new XNodeParent<V>(newType, a, b).canonization();
			} else if (newSons[1].type == SUB) {
				XNode<V> a = new XNodeParent<V>(ADD, newSons[0], ((XNodeParent<V>) newSons[1]).sons[1]);
				XNode<V> b = ((XNodeParent<V>) newSons[1]).sons[0];
				return new XNodeParent<V>(newType, a, b).canonization();
			} else if (newSons[0].type == SUB) {
				XNode<V> a = ((XNodeParent<V>) newSons[0]).sons[0];
				XNode<V> b = new XNodeParent<V>(ADD, newSons[1], ((XNodeParent<V>) newSons[0]).sons[1]);
				return new XNodeParent<V>(newType, a, b).canonization();
			}
			// next, we remove some add when possible
			if (newSons[0].type == ADD && newSons[1].type == ADD) {
				XNode<V>[] ns1 = ((XNodeParent<V>) newSons[0]).sons, ns2 = ((XNodeParent<V>) newSons[1]).sons;
				if (ns1.length == 2 && ns2.length == 2 && ns1[1].type == LONG && ns2[1].type == LONG) {
					((XNodeLeaf<?>) ns1[1]).value = (long) ns1[1].firstVal() - ns2[1].firstVal();
					newSons[1] = (XNode<V>) ns2[0];
					return new XNodeParent<V>(newType, newSons).canonization();
				}
			}
		}
		return new XNodeParent<V>(newType, newSons);
	}

	private XNode<V> buildNewTreeUsing(Function<XNode<V>, XNode<V>> f) {
		return new XNodeParent<V>(type, Stream.of(sons).map(f).collect(Collectors.toList()));
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
	public <T> T firstOfType(TypeExpr type) {
		return Stream.of(sons).map(son -> (T) son.firstOfType(type)).filter(o -> o != null).findFirst().orElse(null);
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
