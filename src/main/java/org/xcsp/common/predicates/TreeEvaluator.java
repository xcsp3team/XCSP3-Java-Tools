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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeCircuitableOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.Utilities.ModifiableBoolean;
import org.xcsp.common.enumerations.EnumerationCartesian;

/**
 * @author Christophe Lecoutre
 */
public class TreeEvaluator {

	/**********************************************************************************************
	 * Static
	 *********************************************************************************************/

	private static final Map<String, Class<?>> classMap = new HashMap<>();

	private static final Map<String, Integer> arityMap = new HashMap<>();

	private static final Set<String> symmetricEvaluators = new HashSet<>(), associativeEvaluators = new HashSet<>();

	static {
		for (Class<?> cl : Stream.of(TreeEvaluator.class.getDeclaredClasses())
				.filter(c -> Evaluator.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers())).toArray(Class<?>[]::new)) {
			String evaluatorToken = cl.getSimpleName().substring(0, 1).toLowerCase()
					+ cl.getSimpleName().substring(1, cl.getSimpleName().lastIndexOf(Evaluator.class.getSimpleName()));
			classMap.put(evaluatorToken, cl);
			// System.out.println(evaluatorToken + " " + clazz);
			int arity = -1;
			try {
				if (TagArity0.class.isAssignableFrom(cl))
					arity = 0;
				if (TagArity1.class.isAssignableFrom(cl))
					arity = 1;
				if (TagArity2.class.isAssignableFrom(cl))
					arity = 2;
				if (TagArity3.class.isAssignableFrom(cl))
					arity = 3;
				if (TagArityX.class.isAssignableFrom(cl))
					arity = Integer.MAX_VALUE;
				if (TagSymmetric.class.isAssignableFrom(cl))
					symmetricEvaluators.add(evaluatorToken);
				if (TagAssociative.class.isAssignableFrom(cl))
					associativeEvaluators.add(evaluatorToken);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
			Utilities.control(arity != -1, "Pb with arity");
			arityMap.put(evaluatorToken, arity);
		}
	}

	public static Class<?> classOf(String tok) {
		return classMap.get(tok);
	}

	public static int arityOf(String tok) {
		Integer a = arityMap.get(tok);
		if (a != null)
			return a; // arity of a basic operator
		int pos = IntStream.range(0, tok.length()).filter(i -> !Character.isDigit(tok.charAt(i))).findFirst().orElse(tok.length()) - 1;
		// either a token that is not an operator (return -1) or an eXtended operator (necessarily starts with an
		// integer)
		return pos == -1 || pos == tok.length() - 1 ? -1 : Integer.parseInt(tok.substring(0, pos + 1));
	}

	public static boolean isSymmetric(String tok) {
		return symmetricEvaluators.contains(tok);
	}

	public static boolean isAssociative(String tok) {
		return associativeEvaluators.contains(tok);
	}

	public static final int SAMPLING_LIMIT = 1000;

	/**********************************************************************************************
	 * Tags
	 *********************************************************************************************/

	public interface TagBoolean {
	}

	public interface TagInteger {
	}

	public interface TagArithmetic extends TagInteger {
	}

	public interface TagLogical extends TagBoolean {
	}

	public interface TagRelational extends TagBoolean {
	}

	public interface TagSet {
	}

	public interface TagTerminal {
	}

	public interface TagSymmetric {
	}

	public interface TagAssociative {
	}

	public interface TagArity0 {
	}

	public interface TagArity1 {
	}

	public interface TagArity2 {
	}

	public interface TagArity3 {
	}

	public interface TagArityX {
	}

	/**********************************************************************************************
	 * Root class for evaluators
	 *********************************************************************************************/

	public abstract class Evaluator {

		public int arity = -1;

		public void fixArity() {
			Utilities.control(arity == -1 || this instanceof TagArityX, "Pb with arity");
			if (arity == -1)
				arity = this instanceof TagArity0 ? 0 : this instanceof TagArity1 ? 1 : this instanceof TagArity2 ? 2 : this instanceof TagArity3 ? 3 : -1;
		}

		public abstract void evaluate();

		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}

	/**********************************************************************************************
	 * Arithmetic Evaluators
	 *********************************************************************************************/

	public class NegEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		@Override
		public void evaluate() {
			stack[top] = -stack[top];
		}
	}

	public class AbsEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		@Override
		public void evaluate() {
			stack[top] = Math.abs(stack[top]);
		}
	}

	public class AddEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = stack[top] + stack[top + 1];
		}
	}

	public class AddxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long sum = stack[top];
			for (int i = 1; i < arity; i++)
				sum += stack[top + i];
			stack[top] = sum;
		}
	}

	public class SubEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		@Override
		public void evaluate() {
			top--;
			stack[top] = stack[top] - stack[top + 1];
		}
	}

	public class MulEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = stack[top] * stack[top + 1];
		}
	}

	public class MulxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long product = stack[top];
			for (int i = 1; i < arity; i++)
				product *= stack[top + i];
			stack[top] = product;
		}
	}

	public class DivEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		@Override
		public void evaluate() {
			top--;
			stack[top] = stack[top] / stack[top + 1];
		}
	}

	public class ModEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		@Override
		public void evaluate() {
			top--;
			stack[top] = stack[top] % stack[top + 1];
		}
	}

	public class SqrEvaluator extends Evaluator implements TagArity1, TagArithmetic {
		@Override
		public void evaluate() {
			stack[top] = stack[top] * stack[top];
		}
	}

	public class PowEvaluator extends Evaluator implements TagArity2, TagArithmetic {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (long) Math.pow(stack[top], stack[top + 1]);
		}
	}

	public class MinEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = Math.min(stack[top], stack[top + 1]);
		}
	}

	public class MinxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long min = stack[top];
			for (int i = 1; i < arity; i++)
				min = Math.min(min, stack[top + i]);
			stack[top] = min;
		}
	}

	public class MaxEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = Math.max(stack[top], stack[top + 1]);
		}
	}

	public class MaxxEvaluator extends Evaluator implements TagArityX, TagArithmetic, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long max = stack[top];
			for (int i = 1; i < arity; i++)
				max = Math.max(max, stack[top + i]);
			stack[top] = max;
		}
	}

	public class DistEvaluator extends Evaluator implements TagArity2, TagArithmetic, TagSymmetric {
		@Override
		public void evaluate() {
			top--;
			stack[top] = Math.abs(stack[top] - stack[top + 1]);
		}
	}

	public interface ExternFunctionArity1 {
		public long evaluate(long l);
	}

	public interface ExternFunctionArity2 {
		public long evaluate(long l1, long l2);
	}

	public class F1Evaluator extends Evaluator implements TagArity1, TagArithmetic {
		public ExternFunctionArity1 function;

		@Override
		public void evaluate() {
			stack[top] = function.evaluate(stack[top]);
		}
	}

	public class F2Evaluator extends Evaluator implements TagArity2, TagArithmetic {
		public ExternFunctionArity2 function;

		@Override
		public void evaluate() {
			top--;
			stack[top] = function.evaluate(stack[top], stack[top + 1]);
		}
	}

	/**********************************************************************************************
	 * Relational Evaluators
	 *********************************************************************************************/

	public class LtEvaluator extends Evaluator implements TagArity2, TagRelational {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] < stack[top + 1] ? 1 : 0);
		}
	}

	public class LtxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] >= stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class LeEvaluator extends Evaluator implements TagArity2, TagRelational {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] <= stack[top + 1] ? 1 : 0);
		}
	}

	public class LexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] > stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class GeEvaluator extends Evaluator implements TagArity2, TagRelational {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] >= stack[top + 1] ? 1 : 0);
		}
	}

	public class GexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] < stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class GtEvaluator extends Evaluator implements TagArity2, TagRelational {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] > stack[top + 1] ? 1 : 0);
		}
	}

	public class GtxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 1; i < arity; i++)
				if (stack[top + i - 1] <= stack[top + i]) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class NeEvaluator extends Evaluator implements TagArity2, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] != stack[top + 1] ? 1 : 0);
		}
	}

	public class NexEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = arity - 1; i > 0; i--)
				for (int j = i - 1; j >= 0; j--)
					if (stack[top + i] == stack[top + j]) {
						stack[top] = 0;
						return;
					}
			stack[top] = 1;
		}
	}

	public class EqEvaluator extends Evaluator implements TagArity2, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == stack[top + 1] ? 1 : 0);
		}
	}

	public class EqxEvaluator extends Evaluator implements TagArityX, TagRelational, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long value = stack[top];
			for (int i = 1; i < arity; i++)
				if (stack[top + i] != value) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	/**********************************************************************************************
	 * Set Evaluators
	 *********************************************************************************************/

	public class SetxEvaluator extends Evaluator implements TagArityX, TagSet {
		@Override
		public void evaluate() {
			// System.out.println("arity=" + arity + " " + this);
			stack[++top] = arity; // to be used by next operator which is necessarily in or notin
		}
	}

	public class InEvaluator extends Evaluator implements TagArity2, TagSet, TagBoolean {
		@Override
		public void evaluate() {
			int arity = (int) stack[top--]; // comes from operator set
			top -= arity;
			long value = stack[top];
			for (int i = 1; i < arity + 1; i++)
				if (stack[top + i] == value) {
					stack[top] = 1;
					return;
				}

			stack[top] = 0;
		}
	}

	public class NotinEvaluator extends Evaluator implements TagArity2, TagSet, TagBoolean {
		@Override
		public void evaluate() {
			int arity = (int) stack[top--]; // comes from operator set
			top -= arity;
			long value = stack[top];
			for (int i = 1; i < arity + 1; i++)
				if (stack[top + i] == value) {
					stack[top] = 0;
					return;
				}

			stack[top] = 1;
		}
	}

	/**********************************************************************************************
	 * Logical Evaluators
	 *********************************************************************************************/

	public class NotEvaluator extends Evaluator implements TagArity1 {
		@Override
		public void evaluate() {
			stack[top] = 1 - stack[top]; // (stack[nbStackElements - 1] == 1 ? 0 : 1);
		}
	}

	public class AndEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = Math.min(stack[top], stack[top + 1]);
		}
	}

	public class AndxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 0) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class OrEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = Math.max(stack[top], stack[top + 1]);
		}
	}

	public class OrxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 1) {
					stack[top] = 1;
					return;
				}
			stack[top] = 0;
		}
	}

	public class XorEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] + stack[top + 1] == 1 ? 1 : 0);
		}
	}

	public class XorxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			int cnt = 0;
			for (int i = 0; i < arity; i++)
				if (stack[top + i] == 1)
					cnt++;
			stack[top] = cnt % 2;
		}
	}

	public class IffEvaluator extends Evaluator implements TagArity2, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == stack[top + 1] ? 1 : 0);
		}
	}

	public class IffxEvaluator extends Evaluator implements TagArityX, TagLogical, TagSymmetric, TagAssociative {
		@Override
		public void evaluate() {
			top -= arity - 1;
			long value = stack[top];
			for (int i = 1; i < arity; i++)
				if (stack[top + i] != value) {
					stack[top] = 0;
					return;
				}
			stack[top] = 1;
		}
	}

	public class ImpEvaluator extends Evaluator implements TagArity2, TagLogical {
		@Override
		public void evaluate() {
			top--;
			stack[top] = (stack[top] == 0 || stack[top + 1] == 1 ? 1 : 0);
		}
	}

	public class IfEvaluator extends Evaluator implements TagArity3, TagArithmetic {
		@Override
		public void evaluate() {
			top -= 2;
			stack[top] = stack[top] == 1 ? stack[top + 1] : stack[top + 2];
			// if (stack[top+2] == 1)
			// stack[top]=stack[top+1];
		}
	}

	/**********************************************************************************************
	 * Terminal Evaluators
	 *********************************************************************************************/

	public class FalseEvaluator extends Evaluator implements TagArity0, TagTerminal, TagBoolean {
		@Override
		public void evaluate() {
			stack[++top] = 0;
		}
	}

	public class TrueEvaluator extends Evaluator implements TagArity0, TagTerminal, TagBoolean {
		@Override
		public void evaluate() {
			stack[++top] = 1;
		}
	}

	public class LongEvaluator extends Evaluator implements TagArity0, TagTerminal, TagInteger {

		public final long value;

		public LongEvaluator(long value) {
			this.value = value;
		}

		@Override
		public void evaluate() {
			stack[++top] = value;
		}

		@Override
		public String toString() {
			return super.toString() + "(" + value + ")";
		}
	}

	public class VariableEvaluator extends Evaluator implements TagArity0, TagTerminal, TagInteger {

		public final int position;

		public VariableEvaluator(int position) {
			this.position = position;
		}

		@Override
		public void evaluate() {
			stack[++top] = values[position];
		}
	}

	public class ShortCircuit {

		public final TypeCircuitableOperator operator;
		public final int nextPosition;

		public ShortCircuit(TypeCircuitableOperator operator, int nextPosition) {
			this.operator = operator;
			this.nextPosition = nextPosition;
		}

		@Override
		public String toString() {
			return operator + " " + nextPosition;
		}
	}

	/**********************************************************************************************
	 * The body of the class
	 *********************************************************************************************/

	/**
	 * The syntactic tree representing the predicate.
	 */
	private XNode<? extends IVar> tree;

	/**
	 * The sequence of evaluators (built from a post-fixed expression) that can be called for evaluating a tuple of values (instantiation).
	 */
	public Evaluator[] evaluators;

	/**
	 * The current top value for the stack. Initially, at -1
	 */
	private int top = -1;

	/**
	 * The stack used for evaluating a tuple of values (instantiation).
	 */
	private long[] stack;

	/**
	 * If not null, shortCorcuits[i] indicates for the ith evaluator the position of the next evaluator to consider and its type (allows short-circuiting).
	 */
	private ShortCircuit[] shortCircuits;

	/**
	 * This field is inserted in order to avoid having systematically a tuple of values as parameter of methods evaluate() in Evaluator classes.
	 */
	private int[] values;

	private int[] tmp = new int[1];

	Integer arity;

	public boolean isBoolean() {
		return evaluators[evaluators.length - 1] instanceof TagBoolean;
	}

	public boolean isInteger() {
		return evaluators[evaluators.length - 1] instanceof TagInteger;
	}

	private Evaluator buildEvaluator(String tok, List<String> varNames) {
		try {
			if (tok.matches("^(-?)\\d+$"))
				return new LongEvaluator(Long.parseLong(tok));
			if (tok.startsWith("%"))
				return new VariableEvaluator(Integer.parseInt(tok.substring(1)));
			if (classOf(tok) != null)
				return (Evaluator) classOf(tok).getDeclaredConstructor(TreeEvaluator.class).newInstance(TreeEvaluator.this);
			int pos = IntStream.range(0, tok.length()).filter(i -> !Character.isDigit(tok.charAt(i))).findFirst().orElse(tok.length()) - 1;
			if (pos == -1) {
				int varPos = varNames.indexOf(tok);
				if (varPos == -1) {
					varPos = varNames.size();
					varNames.add(tok);
				}
				return new VariableEvaluator(varPos);
			}
			Evaluator evaluator = (Evaluator) classOf(tok.substring(pos + 1) + "x").getDeclaredConstructor(TreeEvaluator.class).newInstance(TreeEvaluator.this);
			evaluator.arity = Integer.parseInt(tok.substring(0, pos + 1));
			return evaluator;
		} catch (Exception e) {
			(e.getCause() == null ? e : e.getCause()).printStackTrace();
			return null;
		}
	}

	private void dealWithShortCircuits() {
		for (int i = 0; i < evaluators.length - 1; i++) {
			if (evaluators[i] instanceof TagInteger)
				continue;
			// from a Boolean evaluator, we may find a short circuit
			int j = i + 1;
			int nbStackedElements = 1;
			while (j < evaluators.length) {
				nbStackedElements += 1 - evaluators[j].arity;
				if (nbStackedElements <= 1)
					break;
				j++;
			}
			if (j == i + 1)
				continue;
			if (!(evaluators[j] instanceof OrEvaluator || evaluators[j] instanceof AndEvaluator || evaluators[j] instanceof ImpEvaluator))
				continue;
			if (shortCircuits == null)
				shortCircuits = new ShortCircuit[evaluators.length];
			if (evaluators[j] instanceof OrEvaluator)
				shortCircuits[i] = new ShortCircuit(TypeCircuitableOperator.OR, j + 1);
			else if (evaluators[j] instanceof AndEvaluator)
				shortCircuits[i] = new ShortCircuit(TypeCircuitableOperator.AND, j + 1);
			else
				shortCircuits[i] = new ShortCircuit(TypeCircuitableOperator.IMP, j + 1);
		}
	}

	private void buildEvaluators() {
		List<String> varNames = new ArrayList<>(); // necessary to collect variable names when building the evaluators
		evaluators = Stream.of(tree.toPostfixExpression(tree.vars()).split(Constants.REG_WS)).map(s -> buildEvaluator(s, varNames)).peek(e -> e.fixArity())
				.toArray(Evaluator[]::new);
		dealWithShortCircuits();
		stack = new long[evaluators.length];
		assert evaluators.length > 0;
		int[] allPositions = Stream.of(evaluators).filter(e -> e instanceof VariableEvaluator).mapToInt(e -> ((VariableEvaluator) e).position).distinct()
				.sorted().toArray();
		Utilities.control(IntStream.range(0, allPositions.length).allMatch(i -> i == allPositions[i]), "");
		arity = allPositions.length;
	}

	public TreeEvaluator(XNode<? extends IVar> tree) {
		this.tree = tree;
		buildEvaluators();
	}

	public TreeEvaluator(XNode<? extends IVar> tree, Map<String, Integer> mapOfSymbols) {
		this(mapOfSymbols == null ? tree : (XNode<? extends IVar>) tree.replaceSymbols(mapOfSymbols));
	}

	/** Evaluates the specified tuple of values, by using the recorded so-called evaluators. */
	public final long evaluate(int[] values) {
		this.values = values;
		top = -1;
		if (shortCircuits == null)
			for (Evaluator evaluator : evaluators)
				evaluator.evaluate();
		else
			for (int i = 0; i < evaluators.length;) {
				evaluators[i].evaluate();
				if (shortCircuits[i] == null)
					i++;
				else {
					TypeCircuitableOperator op = shortCircuits[i].operator;
					if (op == TypeCircuitableOperator.OR)
						i = stack[top] == 1 ? shortCircuits[i].nextPosition : i + 1;
					else if (op == TypeCircuitableOperator.AND)
						i = stack[top] == 0 ? shortCircuits[i].nextPosition : i + 1;
					else {
						if (stack[top] == 0) {
							stack[top] = 1;
							i = shortCircuits[i].nextPosition;
						} else
							i++;
					}
				}
			}
		assert top == 0 : "" + top;
		return stack[top]; // 1 means true while 0 means false
	}

	// public final int[][] generateTuples(int[] sizes, Function<int[], int[]> f, ModifiableBoolean positive, int limit)
	// {
	// List<int[]> supports = new ArrayList<>(), conflicts = new ArrayList<>();
	// int[] tupleIdx = new int[sizes.length];
	// int cnt = 0;
	// for (boolean hasNext = true; hasNext;) {
	// int[] tupleVal = f.apply(tupleIdx);
	// boolean consistent = evaluate(tupleVal) == 1;
	// if (consistent && positive.value != Boolean.FALSE)
	// supports.add(tupleVal.clone());
	// if (!consistent && positive.value != Boolean.TRUE)
	// conflicts.add(tupleVal.clone());
	// if (positive.value == null && ++cnt > limit)
	// positive.value = supports.size() <= conflicts.size() ? Boolean.TRUE : Boolean.FALSE;
	// hasNext = false;
	// for (int i = 0; !hasNext && i < tupleIdx.length; i++)
	// if (tupleIdx[i] + 1 < sizes[i].length) {
	// tupleIdx[i]++;
	// hasNext = true;
	// } else
	// tupleIdx[i] = 0;
	// }
	// if (positive.value == null)
	// positive.value = supports.size() <= conflicts.size() ? Boolean.TRUE : Boolean.FALSE;
	// return positive.value ? supports.toArray(new int[0][]) : conflicts.toArray(new int[0][]);
	// }

	public final int[] generatePossibleValues(int[][] domValues) {
		if (isBoolean())
			return new int[] { 0, 1 };
		Set<Long> set = new HashSet<>();
		new EnumerationCartesian(domValues).execute(tuple -> set.add(evaluate(tuple)));
		// for (int[] tuple : new EnumerationCartesian(domValues).toArray())
		// set.add(evaluate(tuple));
		return set.stream().peek(i -> Utilities.isSafeInt(i)).mapToInt(i -> i.intValue()).sorted().toArray();
	}

	public final int[][] generateTuples(int[][] domValues, ModifiableBoolean positive, int limit) {
		// control Boolean evaluator
		List<int[]> supports = new ArrayList<>(), conflicts = new ArrayList<>();
		int[] tupleIdx = new int[domValues.length], tupleVal = new int[domValues.length];
		int cnt = 0;
		for (boolean hasNext = true; hasNext;) {
			for (int i = 0; i < tupleVal.length; i++)
				tupleVal[i] = domValues[i][tupleIdx[i]];
			boolean consistent = evaluate(tupleVal) == 1;
			if (consistent && positive.value != Boolean.FALSE)
				supports.add(tupleVal.clone());
			if (!consistent && positive.value != Boolean.TRUE)
				conflicts.add(tupleVal.clone());
			if (positive.value == null && ++cnt > limit)
				positive.value = supports.size() <= conflicts.size() ? Boolean.TRUE : Boolean.FALSE;
			hasNext = false;
			for (int i = 0; !hasNext && i < tupleIdx.length; i++)
				if (tupleIdx[i] + 1 < domValues[i].length) {
					tupleIdx[i]++;
					hasNext = true;
				} else
					tupleIdx[i] = 0;
		}
		if (positive.value == null)
			positive.value = supports.size() <= conflicts.size() ? Boolean.TRUE : Boolean.FALSE;
		return positive.value ? supports.toArray(new int[0][]) : conflicts.toArray(new int[0][]);
	}

	public final int[][] generateTuples(int[][] domValues, ModifiableBoolean positive) {
		return generateTuples(domValues, positive, SAMPLING_LIMIT);
	}

	public final int[][] generateSupports(int[][] domValues) {
		return generateTuples(domValues, new ModifiableBoolean(true));
	}

	public final int[][] generateConflicts(int[][] domValues) {
		return generateTuples(domValues, new ModifiableBoolean(false));
	}

	public final int[] getUniqueConflict(int[][] domValues) {
		int[] tupleIdx = new int[domValues.length], tupleVal = new int[domValues.length];
		int[] conflict = null;
		for (boolean hasNext = true; hasNext;) {
			for (int i = 0; i < tupleVal.length; i++)
				tupleVal[i] = domValues[i][tupleIdx[i]];
			boolean consistent = evaluate(tupleVal) == 1;
			if (!consistent) {
				if (conflict != null)
					return null; // because at least two conflicts
				conflict = tupleVal.clone();  // otherwise, we record the conflict
			}
			hasNext = false;
			for (int i = 0; !hasNext && i < tupleIdx.length; i++)
				if (tupleIdx[i] + 1 < domValues[i].length) {
					tupleIdx[i]++;
					hasNext = true;
				} else
					tupleIdx[i] = 0;
		}
		return conflict;
	}

	public final int[][] computeTuples(int[][] domValues, int[] targetDom) {
		assert targetDom == null || IntStream.range(0, targetDom.length - 1).allMatch(i -> targetDom[i] <= targetDom[i + 1]);
		int arity = domValues.length;
		List<int[]> tuples = new ArrayList<>();
		int[] tupleIdx = new int[arity], tupleVal = new int[arity + 1];
		for (boolean hasNext = true; hasNext;) {
			for (int i = 0; i < arity; i++)
				tupleVal[i] = domValues[i][tupleIdx[i]];
			int v = (int) evaluate(tupleVal); // TODO control long to int ?
			if (targetDom == null || Arrays.binarySearch(targetDom, v) >= 0) {
				tupleVal[arity] = v;
				tuples.add(tupleVal.clone());
			}
			hasNext = false;
			for (int i = 0; !hasNext && i < tupleIdx.length; i++)
				if (tupleIdx[i] + 1 < domValues[i].length) {
					tupleIdx[i]++;
					hasNext = true;
				} else
					tupleIdx[i] = 0;
		}
		return tuples.toArray(new int[0][]);
	}

	/** Evaluates the value, by using the recorded so-called evaluators. */
	public final long evaluate(int value) {
		tmp[0] = value;
		return evaluate(tmp);
	}

	public boolean controlArityOfEvaluators() {
		return Stream.of(evaluators).mapToInt(e -> 1 - e.arity).sum() == 1;
	}

	public boolean controlTypeOfEvaluators(boolean booleanType) {
		if (booleanType && !isBoolean())
			return false;
		if (!booleanType && !isInteger())
			return false;
		boolean[] booleanTypes = new boolean[evaluators.length];
		int top = -1;
		for (Evaluator evaluator : evaluators) {
			if (evaluator instanceof TagArithmetic) {
				if (evaluator instanceof IfEvaluator) {
					if (!booleanTypes[top] || booleanTypes[top - 1] || booleanTypes[top - 2])
						return false;
					top -= 3;
				} else {
					for (int j = 0; j < evaluator.arity; j++) {
						if (booleanTypes[top])
							return false;
						top--;
					}
				}
			} else if (evaluator instanceof TagLogical) {
				for (int j = 0; j < evaluator.arity; j++) {
					if (!booleanTypes[top])
						return false;
					top--;
				}
			} else if (evaluator instanceof TagRelational) {
				for (int j = 0; j < evaluator.arity; j++) {
					if (booleanTypes[top])
						return false;
					top--;
				}
			}
			booleanTypes[++top] = (evaluator instanceof TagBoolean);
		}
		return true;
	}
}