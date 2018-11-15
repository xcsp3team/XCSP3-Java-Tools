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

import static java.util.stream.Collectors.toList;
import static org.xcsp.common.Types.TypeExpr.ABS;
import static org.xcsp.common.Types.TypeExpr.ADD;
import static org.xcsp.common.Types.TypeExpr.AND;
import static org.xcsp.common.Types.TypeExpr.DIST;
import static org.xcsp.common.Types.TypeExpr.EQ;
import static org.xcsp.common.Types.TypeExpr.LE;
import static org.xcsp.common.Types.TypeExpr.LONG;
import static org.xcsp.common.Types.TypeExpr.LT;
import static org.xcsp.common.Types.TypeExpr.MAX;
import static org.xcsp.common.Types.TypeExpr.MIN;
import static org.xcsp.common.Types.TypeExpr.MUL;
import static org.xcsp.common.Types.TypeExpr.NE;
import static org.xcsp.common.Types.TypeExpr.NEG;
import static org.xcsp.common.Types.TypeExpr.NOT;
import static org.xcsp.common.Types.TypeExpr.OR;
import static org.xcsp.common.Types.TypeExpr.SUB;
import static org.xcsp.common.predicates.MatcherInterface.any;
import static org.xcsp.common.predicates.MatcherInterface.any_add_val;
import static org.xcsp.common.predicates.MatcherInterface.anyc;
import static org.xcsp.common.predicates.MatcherInterface.not;
import static org.xcsp.common.predicates.MatcherInterface.sub;
import static org.xcsp.common.predicates.MatcherInterface.val;
import static org.xcsp.common.predicates.MatcherInterface.var;
import static org.xcsp.common.predicates.MatcherInterface.var_add_val;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.relop;
import static org.xcsp.common.predicates.MatcherInterface.AbstractOperation.symop;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

	public static XNodeParent<IVar> build(TypeExpr type, Object... os) {
		os = Stream.of(os).flatMap(o -> o instanceof Stream ? (Stream<?>) o : Stream.of(o)).toArray();
		Utilities.control(type.arityMin <= os.length && os.length <= type.arityMax, "The arity (number of sons) is not valid");
		List<XNode<IVar>> sons = Stream.of(os).map(o -> {
			if (o instanceof XNode)
				return (XNode<IVar>) o;
			if (o instanceof IVar)
				return new XNodeLeaf<IVar>(TypeExpr.VAR, o);
			if (o instanceof Byte || o instanceof Short || o instanceof Integer || o instanceof Long)
				return new XNodeLeaf<IVar>(TypeExpr.LONG, ((Number) o).longValue());
			if (o instanceof String)
				return new XNodeLeaf<IVar>(TypeExpr.SYMBOL, o);
			return (XNode<IVar>) Utilities.control(false,
					"Bad form: have you used equal, different , lessThan,... instead of eq, ne, lt,... ?\n" + "The class causing problem is " + o.getClass());
		}).collect(Collectors.toList()); // toArray(XNode[]::new);
		return new XNodeParent<IVar>(type, sons);
	}

	public static XNodeParent<IVar> abs(Object operand) {
		return build(TypeExpr.ABS, operand);
	}

	public static XNodeParent<IVar> neg(Object operand) {
		return build(TypeExpr.NEG, operand);
	}

	public static XNodeParent<IVar> sqr(Object operand) {
		return build(TypeExpr.SQR, operand);
	}

	public static XNodeParent<IVar> add(Object... operands) {
		return build(TypeExpr.ADD, operands);
	}

	public static XNodeParent<IVar> sub(Object operand1, Object operand2) {
		return build(TypeExpr.SUB, operand1, operand2);
	}

	public static XNodeParent<IVar> mul(Object... operands) {
		return build(TypeExpr.MUL, operands);
	}

	public static XNodeParent<IVar> div(Object operand1, Object operand2) {
		return build(TypeExpr.DIV, operand1, operand2);
	}

	public static XNodeParent<IVar> mod(Object operand1, Object operand2) {
		return build(TypeExpr.MOD, operand1, operand2);
	}

	public static XNodeParent<IVar> pow(Object operand1, Object operand2) {
		return build(TypeExpr.POW, operand1, operand2);
	}

	public static XNodeParent<IVar> min(Object... operands) {
		return build(TypeExpr.MIN, operands);
	}

	public static XNodeParent<IVar> max(Object... operands) {
		return build(TypeExpr.MAX, operands);
	}

	public static XNodeParent<IVar> dist(Object operand1, Object operand2) {
		return build(TypeExpr.DIST, operand1, operand2);
	}

	public static XNodeParent<IVar> lt(Object operand1, Object operand2) {
		return build(TypeExpr.LT, operand1, operand2);
	}

	public static XNodeParent<IVar> le(Object operand1, Object operand2) {
		return build(TypeExpr.LE, operand1, operand2);
	}

	public static XNodeParent<IVar> ge(Object operand1, Object operand2) {
		return build(TypeExpr.GE, operand1, operand2);
	}

	public static XNodeParent<IVar> gt(Object operand1, Object operand2) {
		return build(TypeExpr.GT, operand1, operand2);
	}

	public static XNodeParent<IVar> ne(Object... operands) {
		return build(TypeExpr.NE, operands);
	}

	public static XNodeParent<IVar> eq(Object... operands) {
		return build(TypeExpr.EQ, operands);
	}

	public static XNode<IVar> set(Object... operands) {
		if (operands.length == 0)
			return new XNodeLeaf<IVar>(TypeExpr.SET, null);
		if (operands.length == 1 && operands[0] instanceof Collection) {
			Collection<?> coll = (Collection<?>) operands[0];
			if (coll.size() == 0)
				return new XNodeLeaf<IVar>(TypeExpr.SET, null);
			Object first = coll.iterator().next();
			if (first instanceof Byte || first instanceof Short || first instanceof Integer || first instanceof Long)
				return new XNodeParent<IVar>(TypeExpr.SET,
						coll.stream().map(s -> new XNodeLeaf<IVar>(TypeExpr.LONG, ((Number) s).longValue())).collect(toList()));
			if (first instanceof String)
				return new XNodeParent<IVar>(TypeExpr.SET, coll.stream().map(s -> new XNodeLeaf<IVar>(TypeExpr.SYMBOL, s)).collect(toList()));
			throw new RuntimeException();
		}
		return build(TypeExpr.SET, operands);
	}

	public static XNode<IVar> set(int[] operands) {
		if (operands.length == 0)
			return new XNodeLeaf<IVar>(TypeExpr.SET, null);
		return new XNodeParent<IVar>(TypeExpr.SET, IntStream.of(operands).mapToObj(v -> new XNodeLeaf<IVar>(TypeExpr.LONG, (long) v)).collect(toList()));
	}

	public static XNodeParent<IVar> in(Object var, Object set) {
		return build(TypeExpr.IN, var, set);
	}

	public static XNodeParent<IVar> notin(Object var, Object set) {
		return build(TypeExpr.NOTIN, var, set);
	}

	public static XNodeParent<IVar> not(Object operand) {
		return build(TypeExpr.NOT, operand);
	}

	public static XNodeParent<IVar> and(Object... operands) {
		return operands.length == 1 ? (XNodeParent<IVar>) operands[0] : build(TypeExpr.AND, operands); // modeling facility
	}

	public static XNodeParent<IVar> or(Object... operands) {
		return operands.length == 1 ? (XNodeParent<IVar>) operands[0] : build(TypeExpr.OR, operands); // modeling facility
	}

	public static XNodeParent<IVar> xor(Object... operands) {
		return build(TypeExpr.XOR, operands);
	}

	public static XNodeParent<IVar> iff(Object... operands) {
		return build(TypeExpr.IFF, operands);
	}

	public static XNodeParent<IVar> imp(Object operand1, Object operand2) {
		return build(TypeExpr.IMP, operand1, operand2);
	}

	public static XNodeParent<IVar> ifThenElse(Object operand1, Object operand2, Object operand3) {
		return build(TypeExpr.IF, operand1, operand2, operand3);
	}

	public static XNodeParent<IVar> scalar(int[] t1, Object[] t2) {
		Utilities.control(t1.length == t2.length, "Not the same number of elements in the two arrays");
		return new XNodeParent<IVar>(TypeExpr.ADD, IntStream.range(0, t1.length).mapToObj(i -> mul(t1[i], t2[i])).collect(toList()));
	}

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

	/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
	public XNodeParent(TypeExpr type, List<XNode<V>> sons) {
		this(type, sons.toArray(new XNode[sons.size()]));
	}

	/** Builds a parent node for a syntactic tree, with the specified type and the specified son. */
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

	// private Matcher matcher() {
	// return new Matcher((XNodeParent<IVar>) this);
	// }

	private static class Canonizer<W extends IVar> {
		private Matcher abs_sub = new Matcher(node(ABS, node(SUB, any, any)));
		private Matcher not_not = new Matcher(node(NOT, node(NOT, any)));
		private Matcher neg_neg = new Matcher(node(NEG, node(NEG, any)));
		private Matcher any_lt_k = new Matcher(node(LT, any, val));
		private Matcher k_lt_any = new Matcher(node(LT, val, any));
		private Matcher not_logop = new Matcher(node(NOT, anyc), (node, level) -> level == 1 && node.type.isLogicallyInvertible());
		private Matcher not_symrel_any = new Matcher(node(symop, not, any), (node, level) -> level == 0 && node.type.oneOf(EQ, NE));
		private Matcher any_symrel_not = new Matcher(node(symop, any, not), (node, level) -> level == 0 && node.type.oneOf(EQ, NE));
		private Matcher x_mul_k__eq_l = new Matcher(node(EQ, node(MUL, var, val), val));
		private Matcher flattenable = new Matcher(anyc,
				(node, level) -> level == 0 && node.type.oneOf(ADD, MUL, MIN, MAX, AND, OR) && Stream.of(node.sons).anyMatch(s -> s.type == node.type));
		private Matcher mergeable = new Matcher(anyc, (node, level) -> level == 0 && node.type.oneOf(ADD, MUL, MIN, MAX, AND, OR) && node.sons.length >= 2
				&& node.sons[node.sons.length - 1].type == LONG && node.sons[node.sons.length - 2].type == LONG);
		private Matcher sub_relop_sub = new Matcher(node(relop, sub, sub));
		private Matcher any_relop_sub = new Matcher(node(relop, any, sub));
		private Matcher sub_relop_any = new Matcher(node(relop, sub, any));
		private Matcher any_add_val__relop__any_add_val = new Matcher(node(relop, any_add_val, any_add_val));
		private Matcher var_add_val__relop__val = new Matcher(node(relop, var_add_val, val));
		private Matcher val__relop__var_add_val = new Matcher(node(relop, val, var_add_val));

		private Map<Matcher, Function<XNodeParent<W>, XNode<W>>> rules = new LinkedHashMap<>();

		private Canonizer() {
			rules.put(abs_sub, r -> node(DIST, r.sons[0].sons)); // abs(sub(a,b)) => dist(a,b)
			rules.put(not_not, r -> r.sons[0].sons[0]); // not(not(a)) => a
			rules.put(neg_neg, r -> r.sons[0].sons[0]); // neg(neg(a)) => a
			rules.put(any_lt_k, r -> node(LE, r.sons[0], augment(r.sons[1], -1))); // e.g., lt(x,5) => le(x,4)
			rules.put(k_lt_any, r -> node(LE, augment(r.sons[0], 1), r.sons[1])); // e.g., lt(5,x) => le(6,x)
			rules.put(not_logop, r -> node(r.sons[0].type.logicalInversion(), r.sons[0].sons)); // e.g., not(lt(x)) => ge(x)
			rules.put(not_symrel_any, r -> node(r.type.logicalInversion(), r.sons[0].sons[0], r.sons[1])); // e.g., ne(not(x),y) => eq(x,y)
			rules.put(any_symrel_not, r -> node(r.type.logicalInversion(), r.sons[0], r.sons[1].sons[0])); // e.g., ne(x,not(y)) => eq(x,y)
			rules.put(x_mul_k__eq_l, r -> r.val(1) % r.val(0) == 0 ? node(EQ, r.sons[0].sons[0], longLeaf(r.val(1) / r.val(0))) : longLeaf(0));
			// below, e.g., eq(mul(x,4),8) => eq(x,2) and eq(mul(x,4),6) => 0 (false)
			rules.put(flattenable, r -> { // we flatten operators when possible; for example add(add(x,y),z) becomes add(x,y,z)
				int l1 = r.sons.length, pos = IntStream.range(0, l1).filter(i -> r.sons[i].type == r.type).findFirst().getAsInt(), l2 = r.sons[pos].sons.length;
				Stream<XNode<W>> list = IntStream.range(0, l1 - 1 + l2)
						.mapToObj(j -> j < pos ? r.sons[j] : j < pos + l2 ? r.sons[pos].sons[j - pos] : r.sons[j - l2 + 1]);
				return node(r.type, list);
			});
			rules.put(mergeable, r -> { // we merge long when possible. e.g., add(a,3,2) => add(a,5) and max(a,2,1) => max(a,2)
				XNode<W>[] t = Arrays.copyOf(r.sons, r.arity() - 1);
				long v1 = r.sons[r.arity() - 1].val(0), v2 = r.sons[r.arity() - 2].val(0);
				t[r.arity() - 2] = longLeaf(r.type == ADD ? v1 + v2 : r.type == MUL ? v1 * v2 : r.type.oneOf(MIN, AND) ? Math.min(v1, v2) : Math.max(v1, v2));
				return node(r.type, t);
			});
			// We replace sub by add when possible
			rules.put(sub_relop_sub, r -> node(r.type, node(ADD, r.sons[0].sons[0], r.sons[1].sons[1]), node(ADD, r.sons[1].sons[0], r.sons[0].sons[1])));
			rules.put(any_relop_sub, r -> node(r.type, node(ADD, r.sons[0], r.sons[1].sons[1]), r.sons[1].sons[0]));
			rules.put(sub_relop_any, r -> node(r.type, r.sons[0].sons[0], node(ADD, r.sons[1], r.sons[0].sons[1])));
			// We remove add when possible
			rules.put(any_add_val__relop__any_add_val,
					r -> node(r.type, node(ADD, r.sons[0].sons[0], longLeaf(r.sons[0].sons[1].val(0) - r.sons[1].sons[1].val(0))), r.sons[1].sons[0]));
			rules.put(var_add_val__relop__val, r -> node(r.type, r.sons[0].sons[0], longLeaf(r.sons[1].val(0) - r.sons[0].sons[1].val(0))));
			rules.put(val__relop__var_add_val, r -> node(r.type, longLeaf(r.sons[0].val(0) - r.sons[1].sons[1].val(0)), r.sons[1].sons[0]));
			// mul(x,1) = > x, and add(x,0) => x // TODO
		}

		private XNode<W> augment(XNode<W> n, int offset) {
			((XNodeLeaf<?>) n).value = (long) n.val(0) + offset;
			return n;
		}
	}

	private static Canonizer<?> canonizer;

	private Canonizer<V> canonizer() {
		if (XNodeParent.canonizer != null)
			return (Canonizer<V>) XNodeParent.canonizer;
		Canonizer<V> can = new Canonizer<>();
		canonizer = can;
		return can;
	}

	@Override
	public XNode<V> canonization() {
		// We will build the canonized form of the node, with the local variables type and sons
		TypeExpr type = this.type; // possibly, this initial value of type will be modified during canonization
		XNode<V>[] sons = this.sons.clone();
		IntStream.range(0, sons.length).forEach(i -> sons[i] = sons[i].canonization()); // sons are made canonical
		if (type.isSymmetricOperator())
			Arrays.sort(sons); // Sons are sorted if the type of the node is symmetric
		// Now, sons are potentially sorted if the type corresponds to a non-symmetric binary relational operator (in that case, we swap sons and
		// arithmetically inverse the operator provided that the ordinal value of the reverse operator is smaller)
		if (sons.length == 2 && type.isUnsymmetricRelationalOperator() && (type.arithmeticInversion().ordinal() < type.ordinal()
				|| (type.arithmeticInversion().ordinal() == type.ordinal() && sons[0].compareTo(sons[1]) > 0))) {
			type = type.arithmeticInversion();
			Utilities.swap(sons, 0, 1);
		}
		if (sons.length == 1 && type.isIdentityWhenOneOperand()) // add(x) becomes x, min(x) becomes x, ...
			return sons[0]; // certainly can happen during the canonization process
		XNodeParent<V> node = node(type, sons);
		Entry<Matcher, Function<XNodeParent<V>, XNode<V>>> rule = canonizer().rules.entrySet().stream().filter(e -> e.getKey().matches(node)).findFirst()
				.orElse(null);
		return rule != null ? rule.getValue().apply(node).canonization() : node;
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
	public XNode<V> replacePartiallyParameters(Object[] valueParameters) {
		return new XNodeParent<V>(type, Stream.of(sons).map(s -> s.replacePartiallyParameters(valueParameters)).collect(Collectors.toList()));
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
