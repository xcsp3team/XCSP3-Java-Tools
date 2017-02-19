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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Utilities;
import org.xcsp.parser.entries.XValues.Decimal;

/**
 * The class used for representing a leaf node in a syntactic tree.
 * 
 * @author Christophe Lecoutre
 */
public final class XNodeLeaf<V extends IVar> extends XNode<V> {

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof XNodeLeaf))
			return false;
		return type == ((XNodeLeaf<?>) obj).type && value.equals(((XNodeLeaf<?>) obj).value);
	}

	@Override
	public int compareTo(XNode<V> o) {
		if (type.ordinal() != o.type.ordinal())
			return Integer.compare(type.ordinal(), o.type.ordinal());
		XNodeLeaf<V> leaf = (XNodeLeaf<V>) o;
		if (type == TypeExpr.VAR)
			return ((IVar) value).id().compareTo(((IVar) leaf.value).id());
		if (type == TypeExpr.PAR || type == TypeExpr.LONG)
			return Long.compare((long) value, (long) leaf.value);
		if (type == TypeExpr.SYMBOL)
			return ((String) value).compareTo((String) leaf.value);
		if (type == TypeExpr.SET)
			return 0;
		throw new RuntimeException("Currently, this statement should not be reached.");
	}

	/** The (parsed) value of the node. it may be a variable, a decimal, a long, a parameter, a symbol or null for an empty set. */
	public Object value;

	/** Builds a leaf node for a syntactic tree, with the specified type and the specified value. */
	public XNodeLeaf(TypeExpr type, Object value) {
		super(type);
		this.value = value;
		Utilities.control(type.arityMin == 0 && type.arityMax == 0 || type == TypeExpr.SET, "Pb with this node " + type);
	}

	@Override
	public int size() {
		return 1;
	}

	@Override
	public int maxParameterNumber() {
		return type == TypeExpr.PAR ? ((Long) value).intValue() : -1; // recall that %... is not possible in predicates
	}

	@Override
	public LinkedHashSet<V> collectVars(LinkedHashSet<V> set) {
		if (type == TypeExpr.VAR)
			set.add((V) value);
		return set;
	}

	@Override
	public XNode<V> canonization() {
		return new XNodeLeaf<V>(type, value); // we return a similar object
	}

	@Override
	public XNode<V> abstraction(List<Object> args, boolean abstractIntegers, boolean multiOccurrences) {
		if (type == TypeExpr.VAR) {
			int pos = multiOccurrences ? IntStream.range(0, args.size()).filter(i -> args.get(i) == value).findFirst().orElse(-1) : -1;
			if (pos != -1)
				return new XNodeLeaf<V>(TypeExpr.PAR, (long) pos);
			else {
				args.add(value);
				return new XNodeLeaf<V>(TypeExpr.PAR, (long) (args.size() - 1)); // -1 because already added
			}
		}
		if (type == TypeExpr.LONG && abstractIntegers) {
			args.add(value);
			return new XNodeLeaf<V>(TypeExpr.PAR, (long) (args.size() - 1)); // -1 because already added
		}
		return new XNodeLeaf<V>(type, value); // we return a similar object
	}

	public XNode<V> concretization(Object[] args) {
		if (type != TypeExpr.PAR)
			return new XNodeLeaf<V>(type, value); // we return a similar object
		Object arg = args[((Long) value).intValue()]; // we know that the value is a Long, which is a safe int
		if (arg instanceof Long)
			return new XNodeLeaf<V>(TypeExpr.LONG, arg);
		if (arg instanceof Decimal)
			return new XNodeLeaf<V>(TypeExpr.DECIMAL, arg);
		if (arg instanceof String)
			return new XNodeLeaf<V>(TypeExpr.SYMBOL, arg);
		if (arg instanceof XNode)
			return (XNode<V>) arg;
		return new XNodeLeaf<V>(TypeExpr.VAR, arg); // must be kept at last position because it is complicated to check if arg is instance of V
	}

	@Override
	public XNode<V> replaceSymbols(Map<String, Integer> mapOfSymbols) {
		return type != TypeExpr.SYMBOL ? new XNodeLeaf<V>(type, value) : new XNodeLeaf<V>(TypeExpr.LONG, mapOfSymbols.get(value));
	}

	@Override
	public <T> T firstOfType(TypeExpr type) {
		return (T) (this.type == type ? this : null);
	}

	@Override
	public String toPostfixExpression(IVar[] scopeForAbstraction) {
		if (type == TypeExpr.VAR && scopeForAbstraction != null)
			return "%" + IntStream.range(0, scopeForAbstraction.length).filter(i -> scopeForAbstraction[i] == value).findFirst().getAsInt();
		return type == TypeExpr.SET ? "0set" : value.toString();
	}

	@Override
	public String toFunctionalExpression(Object[] argsForConcretization) {
		if (type == TypeExpr.PAR)
			return argsForConcretization == null ? "%" + value.toString() : argsForConcretization[((Long) value).intValue()].toString();
		return type == TypeExpr.SET ? "set()" : value.toString();
	}

}
