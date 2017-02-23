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
package org.xcsp.checker;

import static org.xcsp.common.Types.TypeObjective.LEX;
import static org.xcsp.common.Types.TypeObjective.MAXIMUM;
import static org.xcsp.common.Types.TypeObjective.MINIMUM;
import static org.xcsp.common.Types.TypeObjective.NVALUES;
import static org.xcsp.common.Types.TypeObjective.PRODUCT;
import static org.xcsp.common.Types.TypeObjective.SUM;
import static org.xcsp.common.Utilities.control;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.EvaluationManager;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.XParser;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

/**
 * @author Gilles Audemard and Christophe Lecoutre
 */
public class SolutionChecker implements XCallbacks2 {

	public static void main(String[] args) throws Exception {
		if (args.length != 1 && args.length != 2) {
			System.out.println("Usage: java Checker <instanceFilename> [ <solutionFileName> ]");
		} else {
			InputStream is = args.length == 1 ? System.in
					: args[1].charAt(0) == '<' ? new ByteArrayInputStream(args[1].getBytes()) : new FileInputStream(args[1]);
			new SolutionChecker(args[0], is);
		}
	}

	private Implem implem = new Implem(this);

	@Override
	public Implem implem() {
		return implem;
	}

	/** The current solution to test */
	public Solution solution;

	/** The current constraint of the (current) solution to test. */
	protected XCtr currCtr;

	/** The current objective of the (current) solution to test. */
	protected XObj currObj;

	/** The numbers used for the current constraint and objective. */
	protected int numCtr, numObj;

	/** The list of violated constraints (for the current solution). */
	public List<XCtr> violatedCtrs;

	/** The list of invalid objectives (for the current solution). */
	public List<XObj> invalidObjs;

	/** The class that manages all information about the (current) solution to test. */
	protected class Solution {
		/** The root of the XML tree representing a solution (element instantiation). */
		Element root;

		/** The sequence of variables of the solution. */
		Object[] variables;

		/** The sequence of values of the solution. */
		Object[] values;

		/**
		 * The sequence of costs of the solution. We have 0 cost for a satisfaction problem, and several costs for a multi-optimization
		 * problem.
		 */
		Object[] costs;

		/** The map that stores the value assigned to each variable. */
		Map<XVar, Object> map = new HashMap<>();

		int intValueOf(XVarInteger x) {
			control(map.containsKey(x), "The variable " + x + " is not assigned a value");
			return Utilities.safeLong2Int((Number) map.get(x), true);
		}

		int[] intValuesOf(XVarInteger[] list) {
			return Stream.of(list).mapToInt(x -> intValueOf(x)).toArray();
		}

		int[][] intValuesOf(XVarInteger[][] lists) {
			return Stream.of(lists).map(t -> intValuesOf(t)).toArray(int[][]::new);
		}

		String symbolicValueOf(XVarSymbolic x) {
			control(map.containsKey(x), "The variable " + x + " is not assigned a value");
			return (String) map.get(x);
		}

		String[] symbolicValuesOf(XVarSymbolic[] list) {
			return Stream.of(list).map(x -> symbolicValueOf(x)).toArray(String[]::new);
		}

		Solution(Element root) {
			this.root = root;
			Element[] childs = Utilities.childElementsOf(this.root);
			control(Utilities.isTag(childs[0], TypeChild.list) && Utilities.isTag(childs[1], TypeChild.values), "Badly formed solution/instantiation");
		}

		void parseVariablesAndValues(XParser parser) {
			Element[] childs = Utilities.childElementsOf(this.root);
			variables = parser.parseSequence(childs[0].getTextContent().trim(), "\\s+");
			for (Object x : variables) {
				control(x instanceof XVarInteger || x instanceof XVarSymbolic,
						x + " " + " is not an integer or symbolic variable. Currently, only these types of variables are supported.");
			}
			values = parser.parseSequence(childs[1].getTextContent().trim(), "\\s+");
			control(variables.length == values.length, "list and values must be of the same size");
			for (int i = 0; i < variables.length; i++) {
				XVar x = (XVar) variables[i];
				map.put(x, values[i]);
				if (!(values[i] instanceof String && ((String) values[i]).equals("*"))) {
					if (x instanceof XVarInteger)
						control(((XDomInteger) x.dom).contains(intValueOf((XVarInteger) x)), "Wrong value for variable " + x);
					else if (x instanceof XVarSymbolic)
						control(((XDomSymbolic) x.dom).contains(symbolicValueOf((XVarSymbolic) x)), "Wrong value for variable " + x);
					else
						unimplementedCase();
				}
			}
			costs = root.getAttribute(TypeAtt.cost.name()).length() == 0 ? null : parser.parseSequence(root.getAttribute(TypeAtt.cost.name()), "\\s+");
			control(costs == null || costs.length == parser.oEntries.size(),
					"Either you indicate no cost at all or you indicate a long cost for each objective.");
		}
	}

	public SolutionChecker(String fileName, InputStream solutionStream) throws Exception {
		// statements below to avoid being obliged to override special functions
		Map<XCallbacksParameters, Object> map = implem().currParameters;
		map.remove(XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_COUNT_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_NVALUES_CASES);

		Scanner scanner = new Scanner(solutionStream);
		String s = scanner.useDelimiter("\\A").next();
		scanner.close();
		while (true) {
			implem().allIds.clear();
			int start = s.indexOf("<instantiation"), end = s.indexOf("</instantiation>", start);
			if (start == -1 || end == -1)
				break;
			String sol = s.substring(start, end + "</instantiation>".length());
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(sol.getBytes()));
			this.solution = new Solution(doc.getDocumentElement());
			loadInstance(fileName);
			s = s.substring(end + "</instantiation>".length());

		}
	}

	protected void controlConstraint(boolean condition) {
		if (!condition)
			violatedCtrs.add(currCtr);
	}

	protected void controlObjective(long computedCost) {
		if (solution.costs == null)
			System.out.println("Objective " + numObj + " has cost " + computedCost);
		else if (computedCost != (Long) solution.costs[numObj])
			invalidObjs.add(currObj);
	}

	/**********************************************************************************************
	 * Overriding of Main Methods for loading variables, constraints and objectives
	 *********************************************************************************************/

	@Override
	public void loadVariables(XParser parser) {
		System.out.println("LOG: Check variables");
		XCallbacks2.super.loadVariables(parser);
		solution.parseVariablesAndValues(parser);
	}

	@Override
	public void loadConstraints(XParser parser) {
		System.out.println("LOG: Check constraints");
		violatedCtrs = new ArrayList<>();
		numCtr = -1;
		XCallbacks2.super.loadConstraints(parser);
	}

	@Override
	public void loadCtr(XCtr c) {
		for (XVar x : c.vars()) {
			control(solution.map.containsKey(x), x + "is not given a value although it is involved in constraint " + c);
			control(!(solution.map.get(x) instanceof String && ((String) solution.map.get(x)).equals("*")),
					x + " cannot be assigned the value * because of constraint " + c);
		}
		currCtr = c;
		numCtr++;
		XCallbacks2.super.loadCtr(c);
	}

	@Override
	public void loadObjectives(XParser parser) {
		invalidObjs = new ArrayList<>();
		if (parser.oEntries.size() > 0) {
			System.out.println("LOG: Check objectives");
			// invalidObjs = new ArrayList<>();
			numObj = -1;
			XCallbacks2.super.loadObjectives(parser);
		}
	}

	@Override
	public void loadObj(XObj o) {
		control(o.type != TypeObjective.LEX, "Currently, objectives of type lex are not managed by this checker.");
		currObj = o;
		numObj++;
		XCallbacks2.super.loadObj(o);
	}

	@Override
	public void endInstance() {
		if (violatedCtrs.size() == 0 && invalidObjs.size() == 0)
			System.out.println("VALID Solution!");
		else {
			System.out.println("INVALID Solution! (" + (violatedCtrs.size() + invalidObjs.size()) + " errors)");
			violatedCtrs.stream().forEach(c -> System.out.println("  Violated Constraint " + c));
			invalidObjs.stream().forEach(o -> System.out.println("  Invalid Objective " + o));
		}
	}

	/**********************************************************************************************
	 * Methods on integer variables/constraints
	 *********************************************************************************************/

	@Override
	public void buildVarInteger(XVarInteger x, int minValue, int maxValue) { // nothing to do
	}

	@Override
	public void buildVarInteger(XVarInteger x, int[] values) { // nothing to do
	}

	@Override
	public void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree) {
		Utilities.control(tree.exactlyVars(scope), "Pb with scope");
		controlConstraint(new EvaluationManager(tree).evaluate(solution.intValuesOf(scope)) == 1);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		controlConstraint(Utilities.contains(values, solution.intValueOf(x)) == positive);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		int[] tuple = solution.intValuesOf(list);
		boolean found = Stream.of(tuples).anyMatch(t -> IntStream.range(0, t.length).allMatch(i -> t[i] == Constants.STAR_INT || t[i] == tuple[i]));
		// TODO dichotomic search instead of linear search ? compatible with * ?
		controlConstraint(found == positive);
	}

	private String reachedState(String startState, XVarInteger[] list, Object[][] transitions) {
		Map<String, String> map = new HashMap<>();
		Stream.of(transitions).forEach(tr -> map.put(tr[0] + ":" + tr[1], tr[2] + ""));
		String current = startState;
		for (XVarInteger x : list) {
			String next = map.get(current + ":" + solution.intValueOf(x));
			if (next == null)
				return null;
			else
				current = next;
		}
		return current;
	}

	@Override
	public void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		String state = reachedState(startState, list, transitions);
		controlConstraint(state != null && Arrays.stream(finalStates).anyMatch(v -> v.equals(state)));
	}

	@Override
	public void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		String state = reachedState((String) transitions[0][0], list, transitions); // The first state of the first transition MUST be the
																					// starting state
		controlConstraint(state != null);
	}

	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {
		controlConstraint(IntStream.of(solution.intValuesOf(list)).distinct().count() == list.length);
	}

	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		XVarInteger[] sublist = Stream.of(list).filter(x -> !Utilities.contains(except, solution.intValueOf(x))).toArray(XVarInteger[]::new);
		controlConstraint(IntStream.of(solution.intValuesOf(sublist)).distinct().count() == sublist.length);
	}

	private boolean distinctVectors(int[] v1, int[] v2) {
		assert v1.length == v2.length;
		return IntStream.range(0, v1.length).anyMatch(i -> v1[i] != v2[i]);
	}

	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		int[][] tuples = solution.intValuesOf(lists);
		controlConstraint(
				IntStream.range(0, tuples.length).allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> distinctVectors(tuples[i], tuples[j]))));
	}

	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		int[][] tuples = solution.intValuesOf(matrix);
		controlConstraint(
				IntStream.range(0, tuples.length).allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> distinctVectors(tuples[i], tuples[j])))); // rows
		int[][] transposedTuples = IntStream.range(0, tuples.length).mapToObj(i -> IntStream.range(0, tuples[0].length).map(j -> tuples[j][i]).toArray())
				.toArray(int[][]::new);
		controlConstraint(IntStream.range(0, transposedTuples.length)
				.allMatch(i -> IntStream.range(i + 1, transposedTuples.length).allMatch(j -> distinctVectors(transposedTuples[i], transposedTuples[j])))); // cols
	}

	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		controlConstraint(IntStream.of(solution.intValuesOf(list)).distinct().count() == 1);
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, tuple.length - 1).allMatch(i -> operator.isValidFor(tuple[i], tuple[i + 1])));
	}

	private boolean orderedVectors(int[] v1, int[] v2, TypeOperatorRel operator) {
		assert v1.length == v2.length;
		for (int i = 0; i < v1.length; i++) {
			if (operator == TypeOperatorRel.LE || operator == TypeOperatorRel.LT) {
				if (v1[i] < v2[i])
					return true;
				if (v1[i] > v2[i])
					return false;
			} else if (operator == TypeOperatorRel.GE || operator == TypeOperatorRel.GT) {
				if (v1[i] > v2[i])
					return true;
				if (v1[i] < v2[i])
					return false;
			}
		}
		return operator == TypeOperatorRel.LE || operator == TypeOperatorRel.GE;
	}

	@Override
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperatorRel operator) {
		int[][] tuples = solution.intValuesOf(lists);
		controlConstraint(IntStream.range(0, tuples.length)
				.allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> orderedVectors(tuples[i], tuples[j], operator))));
	}

	@Override
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperatorRel operator) {
		int[][] tuples = solution.intValuesOf(matrix);
		controlConstraint(IntStream.range(0, tuples.length)
				.allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> orderedVectors(tuples[i], tuples[j], operator))));
		int[][] transposedTuples = IntStream.range(0, tuples[0].length).mapToObj(i -> IntStream.range(0, tuples.length).map(j -> tuples[j][i]).toArray())
				.toArray(int[][]::new);
		controlConstraint(IntStream.range(0, transposedTuples.length).allMatch(
				i -> IntStream.range(i + 1, transposedTuples.length).allMatch(j -> orderedVectors(transposedTuples[i], transposedTuples[j], operator))));
	}

	protected void checkCondition(int value, Condition condition) {
		if (condition instanceof ConditionVar)
			controlConstraint(((ConditionVar) condition).operator.isValidFor(value, solution.intValueOf((XVarInteger) ((ConditionVar) condition).x)));
		else if (condition instanceof ConditionVal)
			controlConstraint(((ConditionVal) condition).operator.isValidFor(value, ((ConditionVal) condition).k));
		else if (condition instanceof ConditionIntvl)
			controlConstraint(((ConditionIntvl) condition).operator.isValidFor(value, ((ConditionIntvl) condition).min, ((ConditionIntvl) condition).max));
		else if (condition instanceof ConditionIntset)
			controlConstraint(((ConditionIntset) condition).operator.isValidFor(value, ((ConditionIntset) condition).t));
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		checkCondition(IntStream.of(solution.intValuesOf(list)).sum(), condition);
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		checkCondition(IntStream.range(0, list.length).map(i -> solution.intValueOf(list[i]) * coeffs[i]).sum(), condition);
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, XVarInteger[] coeffs, Condition condition) {
		checkCondition(IntStream.range(0, list.length).map(i -> solution.intValueOf(list[i]) * solution.intValueOf(coeffs[i])).sum(), condition);
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		checkCondition((int) IntStream.of(solution.intValuesOf(list)).filter(v -> Utilities.contains(values, v)).count(), condition);
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		buildCtrCount(id, list, solution.intValuesOf(values), condition);
	}

	@Override
	public void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		checkCondition((int) IntStream.of(solution.intValuesOf(list)).filter(v -> !Utilities.contains(except, v)).distinct().count(), condition);
	}

	@Override
	public void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		buildCtrNValuesExcept(id, list, new int[] {}, condition);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		int tuple[] = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, values.length).allMatch(i -> IntStream.of(tuple).filter(v -> v == values[i]).count() == occurs[i]));
		controlConstraint(!closed || IntStream.of(tuple).allMatch(v -> Utilities.contains(values, v)));
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		buildCtrCardinality(id, list, closed, values, solution.intValuesOf(occurs));
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		buildCtrCardinality(id, list, closed, solution.intValuesOf(values), solution.intValuesOf(occurs));
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		buildCtrCardinality(id, list, closed, solution.intValuesOf(values), occurs);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, values.length).allMatch(i -> {
			int nb = (int) IntStream.of(tuple).filter(v -> v == values[i]).count();
			return occursMin[i] <= nb && nb <= occursMax[i];
		}));
		controlConstraint(!closed || IntStream.of(tuple).allMatch(v -> Utilities.contains(values, v)));
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		buildCtrCardinality(id, list, closed, solution.intValuesOf(values), occursMin, occursMax);
	}

	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		checkCondition(IntStream.of(solution.intValuesOf(list)).max().getAsInt(), condition);
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		checkCondition(IntStream.of(solution.intValuesOf(list)).min().getAsInt(), condition);
	}

	private void checkArgMin(String id, int[] tuple, int startIndex, XVarInteger index, TypeRank rank, Condition condition, int value) {
		int i = solution.intValueOf(index) - startIndex;
		controlConstraint(tuple[i] == value);
		controlConstraint(rank != TypeRank.FIRST || !Utilities.contains(tuple, value, 0, i - 1));
		controlConstraint(rank != TypeRank.LAST || !Utilities.contains(tuple, value, i + 1, tuple.length - 1));
		if (condition != null)
			checkCondition(value, condition);
	}

	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		int[] tuple = solution.intValuesOf(list);
		checkArgMin(id, tuple, startIndex, index, rank, condition, IntStream.of(tuple).max().getAsInt());
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		int[] tuple = solution.intValuesOf(list);
		checkArgMin(id, tuple, startIndex, index, rank, condition, IntStream.of(tuple).min().getAsInt());
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) { // x[i]=j iff x[j]=i
		controlConstraint(IntStream.range(0, list.length).allMatch(i -> {
			int j = solution.intValueOf(list[i]) - startIndex;
			return 0 <= j && j < list.length && solution.intValueOf(list[j]) == i + startIndex; // + startIndex ???
		}));
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		int[] t1 = solution.intValuesOf(list1), t2 = solution.intValuesOf(list2);
		// x[i]=j iff x[j]=i
		controlConstraint(IntStream.range(0, t1.length).allMatch(i -> {
			int j = t1[i] - startIndex2;
			return 0 <= j && j < t2.length && (t2[j] - startIndex1) == i;
		}));
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.of(tuple).filter(v -> v == 1).count() == 1);
		int pos = solution.intValueOf(value) - startIndex;
		controlConstraint(0 <= pos && pos < list.length && tuple[pos] == 1);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int value) {
		controlConstraint(Utilities.contains(solution.intValuesOf(list), value));
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		buildCtrElement(id, list, solution.intValueOf(value));
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		int[] tuple = solution.intValuesOf(list);
		int i = solution.intValueOf(index) - startIndex;
		controlConstraint(tuple[i] == value);
		controlConstraint(rank != TypeRank.FIRST || !Utilities.contains(tuple, value, 0, i - 1));
		controlConstraint(rank != TypeRank.LAST || !Utilities.contains(tuple, value, i + 1, tuple.length - 1));
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		buildCtrElement(id, list, startIndex, index, rank, solution.intValueOf(value));
	}

	@Override
	public void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		int[] tuple = solution.intValuesOf(list);
		for (int i = 0, j; i < tuple.length; i = j) {
			int v = tuple[i]; // value of the current stretch
			for (j = i + 1; j < tuple.length && tuple[j] == v; j++)
				;
			int width = j - i, pos = IntStream.range(0, values.length).filter(p -> values[p] == v).findFirst().getAsInt();
			controlConstraint(widthsMin[pos] <= width && width <= widthsMax[pos]);
		}
	}

	@Override
	public void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		buildCtrStretch(id, list, values, widthsMin, widthsMax);
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, tuple.length - 1)
				.noneMatch(i -> tuple[i] != tuple[i + 1] && Stream.of(patterns).anyMatch(t -> t[0] == tuple[i] && t[1] == tuple[i + 1])));
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		int[] tuple = solution.intValuesOf(origins);
		int[] sublist = IntStream.range(0, origins.length).filter(i -> !zeroIgnored || lengths[i] != 0).toArray();
		controlConstraint(IntStream.range(0, sublist.length).allMatch(i -> IntStream.range(0, sublist.length).filter(j -> j > i)
				.allMatch(j -> tuple[sublist[i]] + lengths[sublist[i]] <= tuple[sublist[j]] || tuple[sublist[j]] + lengths[sublist[j]] <= tuple[sublist[i]])));
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		buildCtrNoOverlap(id, origins, solution.intValuesOf(lengths), zeroIgnored);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		int[][] tuples = solution.intValuesOf(origins);
		int[] sublist = IntStream.range(0, origins.length).filter(i -> !zeroIgnored || IntStream.of(lengths[i]).allMatch(l -> l != 0)).toArray();
		controlConstraint(IntStream.range(0, sublist.length)
				.allMatch(i -> IntStream.range(0, sublist.length).filter(j -> j > i).allMatch(
						j -> IntStream.range(0, origins[0].length).anyMatch(k -> tuples[sublist[i]][k] + lengths[sublist[i]][k] <= tuples[sublist[j]][k]
								|| tuples[sublist[j]][k] + lengths[sublist[j]][k] <= tuples[sublist[i]][k]))));
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		buildCtrNoOverlap(id, origins, solution.intValuesOf(lengths), zeroIgnored);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		int[] tuple = solution.intValuesOf(origins);
		int min = IntStream.of(tuple).min().getAsInt(), max = IntStream.range(0, tuple.length).map(i -> tuple[i] + lengths[i]).max().getAsInt();
		IntStream.rangeClosed(min, max).forEach(t -> {
			int h = IntStream.range(0, tuple.length).filter(i -> tuple[i] <= t && t < tuple[i] + lengths[i]).map(i -> heights[i]).sum();
			checkCondition(h, condition);
		});
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		buildCtrCumulative(id, origins, lengths, solution.intValuesOf(heights), condition);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		buildCtrCumulative(id, origins, solution.intValuesOf(lengths), heights, condition);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		buildCtrCumulative(id, origins, solution.intValuesOf(lengths), solution.intValuesOf(heights), condition);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		buildCtrCumulative(id, origins, lengths, heights, condition);
		controlConstraint(IntStream.range(0, origins.length).allMatch(i -> solution.intValueOf(origins[i]) + lengths[i] == solution.intValueOf(ends[i])));
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		buildCtrCumulative(id, origins, lengths, ends, solution.intValuesOf(heights), condition);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		buildCtrCumulative(id, origins, solution.intValuesOf(lengths), ends, heights, condition);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		buildCtrCumulative(id, origins, solution.intValuesOf(lengths), ends, solution.intValuesOf(heights), condition);
	}

	@Override
	public void buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		controlConstraint(IntStream.range(0, list.length).allMatch(i -> solution.intValueOf(list[i]) == values[i]));
	}

	@Override
	public void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		controlConstraint(IntStream.of(solution.intValuesOf(pos)).anyMatch(p -> p == 1) || IntStream.of(solution.intValuesOf(neg)).anyMatch(p -> p == 0));
	}

	@Override
	public void buildCtrCircuit(String id, XVarInteger[] list, int startIndex) {
		control(startIndex == 0, "Other cases currently not implemented");
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.of(tuple).distinct().count() == list.length);
		int nbLoops = (int) IntStream.range(0, list.length).filter(i -> tuple[i] == i).count();
		controlConstraint(nbLoops != list.length);
		int i = 0;
		while (i < list.length && tuple[i] == i)
			i++;
		Set<Integer> s = new TreeSet<>();
		while (tuple[i] != i && !s.contains(tuple[i])) {
			s.add(tuple[i]);
			i = tuple[i];
		}
		controlConstraint(s.size() == (tuple.length - nbLoops));
	}

	@Override
	public void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, int size) {
		buildCtrCircuit(id, list, startIndex);
		int nbLoops = (int) IntStream.range(0, list.length).filter(i -> solution.intValueOf(list[i]) == i).count();
		controlConstraint(size == (list.length - nbLoops));
	}

	@Override
	public void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, XVarInteger size) {
		buildCtrCircuit(id, list, startIndex, solution.intValueOf(size));
	}

	/**********************************************************************************************
	 * Methods for managing objectives
	 *********************************************************************************************/

	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {
		controlObjective(solution.intValueOf(x));
	}

	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
		buildObjToMinimize(id, x); // possible because the code for checking is independent of minimization/maximization mode
	}

	@Override
	public void buildObjToMinimize(String id, XNodeParent<XVarInteger> tree) {
		controlObjective(new EvaluationManager(tree).evaluate(solution.intValuesOf(tree.vars())));
	}

	@Override
	public void buildObjToMaximize(String id, XNodeParent<XVarInteger> tree) {
		buildObjToMinimize(id, tree); // possible because the code for checking is independent of minimization/maximization mode
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		buildObjToMinimize(id, type, list, IntStream.range(0, list.length).map(i -> 1).toArray());
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		buildObjToMinimize(id, type, list, IntStream.range(0, list.length).map(i -> 1).toArray());
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		long computedCost = type == SUM ? 0 : type == PRODUCT ? 1 : type == MINIMUM ? Long.MAX_VALUE : type == MAXIMUM ? Long.MIN_VALUE : 0;
		Set<Long> distinctValues = new HashSet<>();
		for (int i = 0; i < list.length; i++) {
			long v = solution.intValueOf(list[i]) * coeffs[i];
			if (type == SUM)
				computedCost += v;
			if (type == PRODUCT)
				computedCost *= v;
			if (type == MINIMUM)
				computedCost = Math.min(computedCost, v);
			if (type == MAXIMUM)
				computedCost = Math.max(computedCost, v);
			if (type == NVALUES)
				distinctValues.add(v);
			assert type != LEX;
		}
		controlObjective(type == NVALUES ? distinctValues.size() : computedCost);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		buildObjToMinimize(id, type, list, coeffs); // possible because the code for checking is independent of minimization/maximization
													// mode
	}

	/**********************************************************************************************
	 * Methods on symbolic variables/constraints
	 *********************************************************************************************/

	/** The map that associates an arbitrary integer value with each symbol. */
	private Map<String, Integer> mapOfSymbols = new HashMap<>();

	@Override
	public void buildVarSymbolic(XVarSymbolic x, String[] values) {
		for (String v : values)
			if (mapOfSymbols.get(v) == null)
				mapOfSymbols.put(v, mapOfSymbols.size());
	}

	@Override
	public void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent<XVarSymbolic> tree) {
		Utilities.control(tree.exactlyVars(scope), "Pb with scope");
		controlConstraint(new EvaluationManager(tree, mapOfSymbols)
				.evaluate(Stream.of(solution.symbolicValuesOf(scope)).mapToInt(s -> mapOfSymbols.get(s)).toArray()) == 1);
	}

	@Override
	public void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags) {
		controlConstraint(Stream.of(values).anyMatch(v -> v.equals(solution.symbolicValueOf(x))) == positive);
	}

	@Override
	public void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags) {
		String[] tuple = solution.symbolicValuesOf(list);
		boolean found = Stream.of(tuples).anyMatch(t -> IntStream.range(0, t.length).allMatch(i -> t[i].equals("*") || t[i].equals(tuple[i])));
		// TODO dichotomic search instead of linear search ? compatible with * ?
		controlConstraint(found == positive);
	}

	@Override
	public void buildCtrAllDifferent(String id, XVarSymbolic[] list) {
		controlConstraint(Stream.of(solution.symbolicValuesOf(list)).distinct().count() == list.length);
	}
}
