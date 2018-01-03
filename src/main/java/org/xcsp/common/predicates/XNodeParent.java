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

import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.DIST;
import static org.xcsp.common.Types.TypeExpr.EQ;
import static org.xcsp.common.Types.TypeExpr.LE;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.Types.TypeExpr.VAR;
import static org.xcsp.common.predicates.MatcherInterface.abs_sub;
import static org.xcsp.common.predicates.MatcherInterface.any_lt_k;
import static org.xcsp.common.predicates.MatcherInterface.k_lt_any;
import static org.xcsp.common.predicates.MatcherInterface.ne_any_not;
import static org.xcsp.common.predicates.MatcherInterface.ne_not_any;
import static org.xcsp.common.predicates.MatcherInterface.neg_neg;
import static org.xcsp.common.predicates.MatcherInterface.not_logop;
import static org.xcsp.common.predicates.MatcherInterface.not_not;
import static org.xcsp.common.predicates.MatcherInterface.x_mul_k__eq_l;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.MatcherInterface.Matcher;

/**
 * The class used for representing a parent node in a syntactic tree.
 * 
 * @author Christophe Lecoutre
 */
public class XNodeParent<V extends IVar> extends XNode<V> {

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof XNodeParent))
			return false;
		XNodeParent<?> node = (XNodeParent<?>) obj;
		return type == node.type && sons.length == node.sons.length && IntStream.range(0, sons.length).allMatch(i -> sons[i].equals(node.sons[i]));
	}

	@Override
	public int compareTo(XNode<V> obj) {
		if (type != obj.type)
			return Integer.compare(type.ordinal(), obj.type.ordinal());
		XNodeParent<V> node = (XNodeParent<V>) obj;
		if (sons.length < node.sons.length)
			return -1;
		if (sons.length > node.sons.length)
			return 1;
		return IntStream.range(0, sons.length).map(i -> sons[i].compareTo(node.sons[i])).filter(v -> v != 0).findFirst().orElse(0);
	}

	/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
	public XNodeParent(TypeExpr type, XNode<V>[] sons) {
		super(type, sons);
		Utilities.control(sons.length > 0, "Pb with this node that should be a parent");
	}

	public XNodeParent(TypeExpr type, List<XNode<V>> sons) {
		this(type, sons.toArray(new XNode[sons.size()]));
	}

	public XNodeParent(TypeExpr type, XNode<V> son) {
		this(type, Arrays.asList(son));
	}

	/** Builds a parent node for a syntactic tree, with the specified type and the two specified sons. */
	public XNodeParent(TypeExpr type, XNode<V> son1, XNode<V> son2) {
		this(type, Arrays.asList(son1, son2));
	}

	@Override
	public int size() {
		return 1 + Stream.of(sons).mapToInt(c -> c.size()).sum();
	}

	@Override
	public int maxParameterNumber() {
		return Stream.of(sons).mapToInt(c -> c.maxParameterNumber()).max().orElse(-1);
	}

	public boolean allSonsOfType(TypeExpr type) {
		return Stream.of(sons).allMatch(s -> s.type == type);
	}

	public static class Canonizer<W extends IVar> {

		public class MatcherR {
			protected final Matcher matcher;

			protected final Function<XNodeParent<W>, XNode<W>> f;

			private MatcherR(Matcher matcher, Function<XNodeParent<W>, XNode<W>> f) {
				this.matcher = matcher;
				this.f = f;
			}

			boolean recognize(XNode<W> root) {
				// System.out.println("Testing " + root + " with " + matcher.target() + " " + matcher.recognize(root));
				return matcher.matches(root);
			}

			public XNode<W> apply(XNodeParent<W> root) {
				return f.apply(root);
			}
		}

		private XNode<W> augment(XNode<W> n, int offset) {
			((XNodeLeaf<?>) n).value = (long) n.val(0) + offset;
			return n;
		}

		@SuppressWarnings("unchecked")
		MatcherR[] ms = new Canonizer.MatcherR[] { new MatcherR(abs_sub, r -> new XNodeParent<>(DIST, r.sons[0].sons)),

			new MatcherR(not_not, r -> r.sons[0].sons[0]), new MatcherR(neg_neg, r -> r.sons[0].sons[0]),

			new MatcherR(any_lt_k, r -> new XNodeParent<>(LE, r.sons[0], augment(r.sons[1], -1)).canonization()),

			new MatcherR(k_lt_any, r -> new XNodeParent<>(LE, augment(r.sons[0], 1), r.sons[1]).canonization()),

			new MatcherR(not_logop, r -> new XNodeParent<>(r.sons[0].type.logicalInversion(), r.sons[0].sons)),

			new MatcherR(ne_not_any, r -> new XNodeParent<>(EQ, r.sons[0].sons[0], r.sons[1])),

			new MatcherR(ne_any_not, r -> new XNodeParent<>(EQ, r.sons[0], r.sons[1].sons[0])),

			new MatcherR(x_mul_k__eq_l, r -> {
				int k = r.val(0), l = r.val(1); // be careful, we need to use sons because they are different from this.sons
				if (l % k == 0)
					return new XNodeParent<>(TypeExpr.EQ, r.sons[0].sons[0], new XNodeLeaf<>(LONG, (long) l / k));
				else
					return new XNodeLeaf<>(LONG, (long) 0);
			}) };
		// mul(x,1) = > x add(x,0) => x // TODO
	}

	Canonizer<V> can = new Canonizer<>();

	/**
	 * TO BE FINISHED: this method will be rewritten/finalized within a few weeks
	 */
	@Override
	public XNode<V> canonization() {
		// we will build the canonized form of the node, with the local variables type and sons
		TypeExpr type = this.type; // possibly, this initial value of type will be modified during canonization
		XNode<V>[] sons = Stream.of(this.sons).map(s -> s.canonization()).toArray(XNode[]::new); // sons are made canonical
		if (type.isSymmetricOperator())
			Arrays.sort(sons); // sons are sorted if the type of the node is symmetric
		// sons are potentially sorted if the type corresponds to a non-symmetric binary relational operator (in that case, we swap sons and
		// arithmetically inverse the operator provided that the ordinal value of the reverse operator is smaller)
		if (sons.length == 2 && type.isUnsymmetricRelationalOperator() && (type.arithmeticInversion().ordinal() < type.ordinal() || (type.arithmeticInversion()
				.ordinal() == type.ordinal() && sons[0].compareTo(sons[1]) > 0))) {
			type = type.arithmeticInversion();
			Utilities.swap(sons, 0, 1);
		}

		XNodeParent<V> test = new XNodeParent<V>(type, sons);
		Canonizer<V> can = new Canonizer<>();
		for (Canonizer<V>.MatcherR m : can.ms)
			if (m.recognize(test))
				return m.apply(test);

		// if (type == TypeExpr.EQ && sons[0].type == TypeExpr.MUL && sons[0].sons[0].type == TypeExpr.VAR && sons[0].sons[1].type == TypeExpr.LONG
		// && sons[1].type == TypeExpr.LONG) {
		// int k = sons[0].val(0), l = sons[1].val(0); // be careful, we need to use sons because they are different from this.sons
		// System.out.println("K=" + k + " l=" + l + " v0=" + val(0) + " v1=" + val(1) + " " + this);
		// if (l % k == 0)
		// return new XNodeParent<V>(TypeExpr.EQ, sons[0].sons[0], new XNodeLeaf<V>(LONG, (long) l / k));
		// else
		// return new XNodeLeaf<V>(LONG, (long) 0);
		// }

		// Now, some specific reformulation rules are applied
		// if (type == TypeExpr.LT && sons[1].type == LONG) { // lt(x,k) becomes le(x,k-1)
		// ((XNodeLeaf<?>) sons[1]).value = (long) sons[1].val(0) - 1;
		// return new XNodeParent<V>(TypeExpr.LE, sons[0], sons[1]).canonization();
		// }
		// if (type == TypeExpr.LT && sons[0].type == LONG) { // lt(k,x) becomes le(k+1,x)
		// ((XNodeLeaf<?>) sons[0]).value = (long) sons[0].val(0) + 1;
		// return new XNodeParent<V>(TypeExpr.LE, sons[0], sons[1]).canonization();
		// }

		// if (type == NOT && sons[0].type.isLogicallyInvertible()) // not(lt(x)) becomes ge(x), not(eq(x)) becomes ne(x), ...
		// return new XNodeParent<V>(sons[0].type.logicalInversion(), sons[0].sons);

		// if (type == NE && sons[0].type == NOT) // ne(not(x),y) becomes eq(x,y)
		// return new XNodeParent<V>(EQ, sons[0].sons[0], sons[1]);
		// if (type == NE && sons[1].type == NOT) // ne(x,not(y)) becomes eq(x,y)
		// return new XNodeParent<V>(EQ, sons[0], sons[1].sons[0]);

		if (sons.length == 1 && type.isIdentityWhenOneOperand()) // add(x) becomes x, min(x) becomes x, ...
			return sons[0]; // certainly can happen during the canonization process

		if (type == ADD) { // we merge long by summing them (similar operations possible for MUL, MIN, MAX, AND, OR, ...)
			if (sons.length >= 2 && sons[sons.length - 1].type == LONG && sons[sons.length - 2].type == LONG) {
				XNode<V>[] t = Arrays.copyOf(sons, sons.length - 1);
				t[sons.length - 2] = new XNodeLeaf<V>(LONG, (long) sons[sons.length - 1].val(0) + sons[sons.length - 2].val(0));
				return new XNodeParent<V>(ADD, t).canonization();
			}
		}
		// Then, we merge operators when possible; for example add(add(x,y),z) becomes add(x,y,z)
		if (type.oneOf(ADD, MUL, MIN, MAX, AND, OR)) { // isSymmetricOperator() && type.notOneOf(EQ, DIST, DJOINT)) {
			for (int i = 0; i < sons.length; i++) {
				if (sons[i].type == type) {
					List<XNode<V>> list = IntStream.rangeClosed(0, i - 1).mapToObj(j -> sons[j]).collect(Collectors.toList());
					Stream.of(((XNodeParent<V>) sons[i]).sons).forEach(s -> list.add(s));
					IntStream.range(i + 1, sons.length).mapToObj(j -> sons[j]).forEach(s -> list.add(s));
					return new XNodeParent<V>(type, list).canonization();
				}
			}
		}
		if (sons.length == 2 && type.isRelationalOperator()) {
			// First, we replace sub by add when possible
			if (sons[0].type == SUB && sons[1].type == SUB)
				return new XNodeParent<V>(type, new XNodeParent<V>(ADD, sons[0].sons[0], sons[1].sons[1]), new XNodeParent<V>(ADD, sons[1].sons[0],
						sons[0].sons[1])).canonization();
			else if (sons[1].type == SUB)
				return new XNodeParent<V>(type, new XNodeParent<V>(ADD, sons[0], sons[1].sons[1]), sons[1].sons[0]).canonization();
			else if (sons[0].type == SUB)
				return new XNodeParent<V>(type, sons[0].sons[0], new XNodeParent<V>(ADD, sons[1], sons[0].sons[1])).canonization();
			// next, we remove some add when possible
			if (sons[0].type == ADD && sons[1].type == ADD) {
				XNode<V>[] ns1 = sons[0].sons, ns2 = sons[1].sons;
				if (ns1.length == 2 && ns2.length == 2 && ns1[1].type == LONG && ns2[1].type == LONG) {
					((XNodeLeaf<?>) ns1[1]).value = (long) ns1[1].val(0) - ns2[1].val(0);
					sons[1] = ns2[0];
					return new XNodeParent<V>(type, sons).canonization();
				}
			}
			if (sons[0].type == ADD && sons[1].type == LONG) {
				if (sons[0].sons.length == 2 && sons[0].sons[0].type == VAR && sons[0].sons[1].type == LONG) {
					sons[1] = new XNodeLeaf<V>(LONG, (long) sons[1].val(0) - sons[0].sons[1].val(0)); // keep it first
					sons[0] = sons[0].sons[0];
					return new XNodeParent<V>(type, sons).canonization();
				}
			}
			if (sons[0].type == LONG && sons[1].type == ADD) {
				if (sons[1].sons.length == 2 && sons[1].sons[0].type == VAR && sons[1].sons[1].type == LONG) {
					sons[0] = new XNodeLeaf<V>(LONG, (long) sons[0].val(0) - sons[1].sons[1].val(0));
					sons[1] = sons[1].sons[0];
					return new XNodeParent<V>(type, sons).canonization();
				}
			}
		}
		return new XNodeParent<V>(type, sons);
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
	public XNode<V> replaceLeafValues(Function<Object, Object> f) {
		return new XNodeParent<V>(type, Stream.of(sons).map(s -> s.replaceLeafValues(f)).collect(Collectors.toList()));
	}

	@Override
	public XNode<V> firstNodeSuchThat(Predicate<XNode<V>> p) {
		return p.test(this) ? this : Stream.of(sons).map(son -> son.firstNodeSuchThat(p)).filter(o -> o != null).findFirst().orElse(null);
	}

	@Override
	public LinkedList<XNode<V>> allNodesSuchThat(Predicate<XNode<V>> p, LinkedList<XNode<V>> list) {
		if (p.test(this))
			list.add(this);
		Stream.of(sons).forEach(s -> s.allNodesSuchThat(p, list));
		return list;
	}

	@Override
	public String toPostfixExpression(IVar[] scopeForAbstraction) {
		String s = Stream.of(sons).map(c -> c.toPostfixExpression(scopeForAbstraction)).collect(Collectors.joining(" "));
		return s + " " + ((type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + type.toString().toLowerCase());
	}

	@Override
	public String toFunctionalExpression(Object[] argsForConcretization) {
		String s = this instanceof XNodeParentSpecial ? ((XNodeParentSpecial<?>) this).specialName : type.toString().toLowerCase();
		return s + "(" + Stream.of(sons).map(c -> c.toFunctionalExpression(argsForConcretization)).collect(Collectors.joining(",")) + ")";
	}

}
