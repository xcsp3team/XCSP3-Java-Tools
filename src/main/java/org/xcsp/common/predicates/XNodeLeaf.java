package org.xcsp.common.predicates;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.xcsp.common.XEnums.TypeArithmeticOperator;
import org.xcsp.common.XEnums.TypeExpr;
import org.xcsp.parser.XValues.Decimal;

/** The class used for representing a leaf node in a syntactic tree. */
public final class XNodeLeaf<V> extends XNodeExpr<V> {
	/** The (parsed) value of the node. it may be a variable, a decimal, a long, a parameter, a symbol or null for an empty set. */
	public final Object value;

	/** Builds a leaf node for a syntactic tree, with the specified type and the specified value. */
	public XNodeLeaf(TypeExpr type, Object value) {
		super(type);
		this.value = value;
	}

	@Override
	public Set<V> collectVars(Set<V> set) {
		if (type == TypeExpr.VAR)
			set.add((V) value);
		return set;
	}

	public XNodeExpr<V> concretizeWith(Object[] args) {
		if (type != TypeExpr.PAR)
			return new XNodeLeaf<V>(type, value); // we return a similar object
		Object arg = args[((Long) value).intValue()]; // we know that the value is a Long, which is a safe int
		if (arg instanceof Long)
			return new XNodeLeaf<V>(TypeExpr.LONG, arg);
		if (arg instanceof Decimal)
			return new XNodeLeaf<V>(TypeExpr.DECIMAL, arg);
		if (arg instanceof String)
			return new XNodeLeaf<V>(TypeExpr.SYMBOL, arg);
		if (arg instanceof XNodeExpr)
			return (XNodeExpr<V>) arg;
		// if (arg instanceof V)
		return new XNodeLeaf<V>(TypeExpr.VAR, arg); // must be kept at last position because it is very complicated to check if arg is instance of V
		// XUtility.control(false, "Another case need to be implemented for " + arg);
	}

	@Override
	public boolean hasBasicForm() {
		return type == TypeExpr.VAR;
	}

	@Override
	public TypeArithmeticOperator arithmeticOperatorOnTwoVariables() {
		return null;
	}

	@Override
	public boolean hasVarAndCstSons() {
		return false;
	}

	@Override
	public boolean hasVarAndVarSons() {
		return false;
	}

	@Override
	public Object getValueOfFirstLeafOfType(TypeExpr type) {
		return this.type == type ? value : null;
	}

	@Override
	public List<String> canonicalForm(List<String> tokens, V[] scope) {
		if (type == TypeExpr.VAR)
			tokens.add("%" + IntStream.range(0, scope.length).filter(i -> scope[i] == (V) value).findFirst().orElse(-1));
		else if (type == TypeExpr.SET)
			tokens.add("0set");
		else
			tokens.add(value.toString());
		return tokens;
	}

	@Override
	public String toString(Object[] args, boolean postfixed) {
		if (type == TypeExpr.PAR)
			return args[((Long) value).intValue()].toString();
		if (type == TypeExpr.SET)
			return postfixed ? "0set" : "set()";
		return value.toString();
		// return type == TypeExpr.PAR ? args[((Long) value).intValue()].toString() : type == TypeExpr.SET ? "0set" : value.toString();
	}

	@Override
	public String toString() {
		if (type == TypeExpr.PAR)
			return "%" + value.toString();
		if (type == TypeExpr.SET)
			return postfixed ? "0set" : "set()";
		return value.toString();
	}
}
