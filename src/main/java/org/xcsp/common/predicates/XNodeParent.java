package org.xcsp.common.predicates;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xcsp.common.XEnums;
import org.xcsp.common.XEnums.TypeArithmeticOperator;
import org.xcsp.common.XEnums.TypeExpr;
import org.xcsp.common.XUtility;

/** The class used for representing a parent node in a syntactic tree. */
public final class XNodeParent<V> extends XNodeExpr<V> {
	/** The sons of the node. */
	public final XNodeExpr<V>[] sons;

	/** Builds a parent node for a syntactic tree, with the specified type and the specified sons. */
	public XNodeParent(TypeExpr type, XNodeExpr<V>[] sons) {
		super(type);
		this.sons = sons;
	}

	@Override
	public Set<V> collectVars(Set<V> set) {
		Stream.of(sons).forEach(s -> s.collectVars(set));
		return set;
	}

	public XNodeExpr<V> concretizeWith(Object[] args) {
		return new XNodeParent<V>(type, Stream.of(sons).map(s -> s.concretizeWith(args)).toArray(XNodeExpr[]::new));
	}

	@Override
	public boolean hasBasicForm() {
		return (type == TypeExpr.ADD && hasVarAndCstSons()) || (type == TypeExpr.SUB && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG);
	}

	@Override
	public TypeArithmeticOperator arithmeticOperatorOnTwoVariables() {
		TypeArithmeticOperator op = XEnums.valueOf(TypeArithmeticOperator.class, type.name());
		return op != null && hasVarAndVarSons() ? op : null;
	}

	@Override
	public boolean hasVarAndCstSons() {
		return sons.length == 2
				&& ((sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG) || (sons[0].type == TypeExpr.LONG && sons[1].type == TypeExpr.VAR));
	}

	@Override
	public boolean hasVarAndVarSons() {
		return sons.length == 2 && sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR;
	}

	@Override
	public Object getValueOfFirstLeafOfType(TypeExpr type) {
		for (XNodeExpr<V> son : sons) {
			Object o = son.getValueOfFirstLeafOfType(type);
			if (o != null)
				return o;
		}
		return null;
	}

	@Override
	public List<String> canonicalForm(List<String> tokens, V[] scope) {
		Stream.of(sons).forEach(s -> s.canonicalForm(tokens, scope));
		tokens.add((type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + super.toString());
		return tokens;
	}

	@Override
	public String toString(Object[] args, boolean postfixed) {
		String s = Stream.of(sons).map(c -> c.toString(args, postfixed)).collect(Collectors.joining(postfixed ? " " : ","));
		return postfixed ? s + " " + (type == TypeExpr.SET || sons.length > 2 && type != TypeExpr.IF ? sons.length : "") + super.toString() : super.toString()
				+ "(" + s + ")";
	}

	@Override
	public String toString() {
		String s = XUtility.join(sons, postfixed ? " " : ",");
		return postfixed ? s + " " + super.toString() : super.toString() + "(" + s + ")";
	}
}
