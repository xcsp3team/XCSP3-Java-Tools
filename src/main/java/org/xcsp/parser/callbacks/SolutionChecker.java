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
package org.xcsp.parser.callbacks;

import static org.xcsp.common.Types.TypeObjective.LEX;
import static org.xcsp.common.Types.TypeObjective.MAXIMUM;
import static org.xcsp.common.Types.TypeObjective.MINIMUM;
import static org.xcsp.common.Types.TypeObjective.NVALUES;
import static org.xcsp.common.Types.TypeObjective.PRODUCT;
import static org.xcsp.common.Types.TypeObjective.SUM;
import static org.xcsp.common.Utilities.control;
import static org.xcsp.common.Utilities.lexComparatorInt;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
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
import org.xcsp.common.IVar.Var;
import org.xcsp.common.Range;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeConditionOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.domains.Values.Occurrences;
import org.xcsp.common.predicates.TreeEvaluator;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.AbstractTuple;
import org.xcsp.common.structures.Transition;
import org.xcsp.parser.XParser;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

/**
 * This class allows us to check solutions and bounds obtained for XCSP3 instances.
 * 
 * @author Gilles Audemard and Christophe Lecoutre
 */
public final class SolutionChecker implements XCallbacks2 {

	// ************************************************************************
	// ***** Main (and other static stuff)
	// ************************************************************************

	private static final int MAX_DISPLAY_STRING_SIZE = 2000;

	public static void main(String[] args) throws Exception {
		boolean competitionMode = args.length > 0 && args[0].equals("-cm");
		args = competitionMode ? Arrays.copyOfRange(args, 1, args.length) : args;
		if (args.length != 1 && args.length != 2) {
			System.out.println("Usage: " + SolutionChecker.class.getName() + " [-cm] <instanceFilename> [ <solutionFileName> ]");
		} else
			new SolutionChecker(competitionMode, args[0],
					args.length == 1 ? System.in : args[1].charAt(0) == '<' ? new ByteArrayInputStream(args[1].getBytes()) : new FileInputStream(args[1]));
	}

	// ************************************************************************
	// ***** Implementation object (bridge pattern)
	// ************************************************************************

	private Implem implem = new Implem(this);

	@Override
	public Implem implem() {
		return implem;
	}

	// ************************************************************************
	// ***** Intern class
	// ************************************************************************

	/** The class that manages all information about the (current) solution to test. */
	private class Solution {
		/** The root of the XML tree representing a solution (element instantiation). */
		private Element root;

		/** The sequence of variables of the solution. */
		private Object[] variables;

		/** The sequence of values of the solution. */
		private Object[] values;

		/**
		 * The sequence of costs of the solution. We have 0 cost for a satisfaction problem, and several costs for a multi-optimization problem.
		 */
		private BigInteger[] costs;

		/** The map that stores the value assigned to each variable. */
		private Map<XVar, Object> map = new HashMap<>();

		private int intValueOf(XVarInteger x) {
			control(map.containsKey(x), "The variable " + x + " is not assigned a value");
			Object value = map.get(x);
			if (value instanceof String && ((String) value).equals("*")) {
				control(((Dom) x.dom).nValues() == 1, "* is accepted when there is only one value");
				value = x.firstValue();
			}
			return Utilities.safeInt((Long) value);
		}

		private int[] intValuesOf(XVarInteger[] list) {
			return Stream.of(list).mapToInt(x -> intValueOf(x)).toArray();
		}

		private int[][] intValuesOf(XVarInteger[][] lists) {
			return Stream.of(lists).map(t -> intValuesOf(t)).toArray(int[][]::new);
		}

		private String symbolicValueOf(XVarSymbolic x) {
			control(map.containsKey(x), "The variable " + x + " is not assigned a value");
			return (String) map.get(x);
		}

		private String[] symbolicValuesOf(XVarSymbolic[] list) {
			return Stream.of(list).map(x -> symbolicValueOf(x)).toArray(String[]::new);
		}

		private Solution(Element root) {
			this.root = root;
			Element[] childs = Utilities.childElementsOf(this.root);
			control(Utilities.isTag(childs[0], TypeChild.list) && Utilities.isTag(childs[1], TypeChild.values), "Badly formed solution/instantiation");
		}

		private void parseVariablesAndValues(XParser parser) {
			Element[] childs = Utilities.childElementsOf(this.root);
			variables = parser.parseSequence(childs[0].getTextContent().trim(), "\\s+");
			for (Object x : variables) {
				control(x == null || x instanceof XVarInteger || x instanceof XVarSymbolic,
						x + " " + " is not an integer or symbolic variable. Currently, only these types of variables are supported.");
				// null is also accepted (although it corresponds to an undefined variable in an array) but the
				// associated value must be *
			}
			values = parser.parseSequence(childs[1].getTextContent().trim(), "\\s+");
			if (Stream.of(values).anyMatch(v -> v instanceof Occurrences)) { // managing compact forms of values in
																				// solutions
				List<Object> list = new ArrayList<>();
				for (Object obj : values)
					if (obj instanceof Occurrences)
						for (int i = 0; i < ((Occurrences) obj).nOccurrences; i++)
							list.add(((Occurrences) obj).value);
					else
						list.add(obj);
				values = list.toArray(new Object[list.size()]);
			}
			control(variables.length == values.length, "list and values must be of the same size " + variables.length + " vs " + values.length);
			for (int i = 0; i < variables.length; i++) {
				if (variables[i] == null)
					control(values[i] instanceof String && ((String) values[i]).equals("*"),
							"* must be necessarily associated with a null variable (corresponding to a hole in an array)");
				else {
					XVar x = (XVar) variables[i];
					map.put(x, values[i]);
					if (!(values[i] instanceof String && ((String) values[i]).equals("*"))) {
						if (x instanceof XVarInteger)
							control(((Dom) x.dom).contains(intValueOf((XVarInteger) x)), "Wrong value for variable " + x);
						else if (x instanceof XVarSymbolic)
							control(((DomSymbolic) x.dom).contains(symbolicValueOf((XVarSymbolic) x)), "Wrong value for variable " + x);
						else
							unimplementedCase();
					}
				}
			}
			// map.entrySet().stream().forEach(e -> System.out.println(e.getKey() + "->" + e.getValue() + " "));
			costs = root.getAttribute(TypeAtt.cost.name()).length() == 0 ? null
					: Stream.of(root.getAttribute(TypeAtt.cost.name()).split("\\s+")).map(s -> new BigInteger(s)).toArray(BigInteger[]::new);
			// costs = root.getAttribute(TypeAtt.cost.name()).length() == 0 ? null :
			// parser.parseSequence(root.getAttribute(TypeAtt.cost.name()), "\\s+");
			control(costs == null || costs.length == parser.oEntries.size(),
					"Either you indicate no cost at all or you indicate a long cost for each objective.");
		}
	}

	// ************************************************************************
	// ***** Fields and Constructors
	// ************************************************************************

	private boolean competitionMode;

	private BigInteger competitionComputedCost;

	/** The current solution to test */
	private Solution solution;

	/** The current constraint of the (current) solution to test. */
	private XCtr currCtr;

	/** The current objective of the (current) solution to test. */
	private XObj currObj;

	/** The numbers used for the current constraint and objective. */
	private int numCtr, numObj;

	/** The list of ids of violated constraints (for the current solution). */
	public List<String> violatedCtrs;

	/** The list of ids of invalid objectives (for the current solution). */
	public List<String> invalidObjs;

	public SolutionChecker(boolean competitionMode, String fileName, InputStream solutionStream) throws Exception {
		this.competitionMode = competitionMode;
		implem().rawParameters(); // to avoid being obliged to override special functions
		Scanner scanner = new Scanner(solutionStream);
		if (competitionMode) {
			String sline = null;
			List<String> vlines = new ArrayList<>();
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("s ")) {
					sline = line; // we store the last s line
				} else if (line.startsWith("v ")) {
					String l = line.substring(2).trim();
					if (l.startsWith("<instantiation"))
						vlines.clear(); // we store the last solution
					vlines.add(line);
				}
			}
			scanner.close();
			String vline = vlines.size() == 0 ? null : vlines.stream().map(s -> s.substring(2)).collect(Collectors.joining(" ")).trim();
			if (sline == null)
				System.out.println("One s line expected");
			else if (sline.startsWith("s SATISFIABLE") || sline.startsWith("s OPTIMUM")) {
				if (vline == null || !vline.endsWith("</instantiation>"))
					System.out.println("ERROR: no instantiation found");
				else {
					try {
						Element elt = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(vline.getBytes()))
								.getDocumentElement();
						this.solution = new Solution(elt);
						loadInstance(fileName);
						if (violatedCtrs.size() == 0 && invalidObjs.size() == 0) {
							System.out.println("OK\t" + (competitionComputedCost != null ? competitionComputedCost : ""));
						} else {
							System.out.println("INVALID Solution! (" + (violatedCtrs.size() + invalidObjs.size()) + " errors)");
							if (violatedCtrs.size() > 0)
								System.out.println("  Violated Constraint " + violatedCtrs.get(0));
							if (invalidObjs.size() > 0)
								System.out.println("  Invalid Objective " + invalidObjs.get(0));
						}
					} catch (Exception e) {
						System.out.println("ERROR: the instantiation cannot be checked " + e);
						e.printStackTrace();
					}
				}
			}
		} else {
			// code below to be improved
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
	}

	protected void controlConstraint(boolean condition) {
		if (!condition) {
			String s = currCtr.toString();
			violatedCtrs.add(currCtr.id + " : " + (s.length() > MAX_DISPLAY_STRING_SIZE ? s.substring(0, MAX_DISPLAY_STRING_SIZE) : s));
		}
	}

	protected void controlObjective(BigInteger computedCost) {
		competitionComputedCost = computedCost;
		if (!competitionMode && solution.costs != null && solution.costs[numObj] != null && !computedCost.equals(solution.costs[numObj])) {
			System.out.println(computedCost + " vs " + solution.costs[numObj]);
			String s = currObj.toString();
			invalidObjs.add(currObj.id + " : " + (s.length() > MAX_DISPLAY_STRING_SIZE ? s.substring(0, MAX_DISPLAY_STRING_SIZE) : s));
		}
	}

	// ************************************************************************
	// ***** Redefining Callback Functions on Main Components for Checking Solution(s)
	// ************************************************************************

	@Override
	public void loadVariables(XParser parser) {
		if (!competitionMode)
			System.out.println("LOG: Check variables");
		XCallbacks2.super.loadVariables(parser);
		solution.parseVariablesAndValues(parser);
	}

	@Override
	public void loadConstraints(XParser parser) {
		if (!competitionMode)
			System.out.println("LOG: Check constraints");
		violatedCtrs = new ArrayList<>();
		numCtr = -1;
		XCallbacks2.super.loadConstraints(parser);
	}

	@Override
	public void loadCtr(XCtr c) {
		for (XVar x : c.vars()) {
			Object obj = solution.map.get(x);
			control(obj != null, x + "is not given a value although it is involved in one constraint ");
			control(!(obj instanceof String && ((String) obj).equals("*")), x + " cannot be assigned the value * because it has not degree 0");
		}
		currCtr = c;
		numCtr++;
		XCallbacks2.super.loadCtr(c);
	}

	@Override
	public void loadObjectives(XParser parser) {
		invalidObjs = new ArrayList<>();
		if (parser.oEntries.size() > 0) {
			if (!competitionMode)
				System.out.println("LOG: Check objectives");
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
		if (!competitionMode)
			if (violatedCtrs.size() == 0 && invalidObjs.size() == 0) {
				System.out.println("OK\t" + (competitionComputedCost != null ? competitionComputedCost : ""));
			} else {
				System.out.println("INVALID Solution! (" + (violatedCtrs.size() + invalidObjs.size()) + " errors)");
				violatedCtrs.stream().forEach(c -> System.out.println("  Violated Constraint " + c));
				invalidObjs.stream().forEach(o -> System.out.println("  Invalid Objective " + o));
			}
	}

	// ************************************************************************
	// ***** Methods on integer variables/constraints
	// ************************************************************************

	@Override
	public void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
	} // nothing to do

	@Override
	public void buildVarInteger(XVarInteger x, int[] values) {
	} // nothing to do

	private LongStream valuesOfTrees(XNode<XVarInteger>[] trees, int[] coeffs) {
		XVarInteger[][] scopes = Stream.of(trees).map(t -> t.vars()).toArray(XVarInteger[][]::new);
		return IntStream.range(0, trees.length)
				.mapToLong(i -> new TreeEvaluator(trees[i]).evaluate(solution.intValuesOf(scopes[i])) * (coeffs == null ? 1 : coeffs[i]));
	}

	private IntStream intValuesOfTrees(XNode<XVarInteger>[] trees) {
		return valuesOfTrees(trees, null).peek(l -> control(Utilities.isSafeInt(l), "Pb with a long")).mapToInt(l -> (int) l);
	}

	@Override
	public void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree) {
		Utilities.control(tree.exactlyVars(scope), "Pb with scope");
		controlConstraint(new TreeEvaluator(tree).evaluate(solution.intValuesOf(scope)) == 1);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		controlConstraint(Utilities.contains(values, solution.intValueOf(x)) == positive);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		int[] tuple = solution.intValuesOf(list);
		boolean found = Stream.of(tuples).parallel().anyMatch(t -> IntStream.range(0, t.length).allMatch(i -> t[i] == Constants.STAR || t[i] == tuple[i]));
		// TODO dichotomic search instead of linear search ? compatible with * ?
		controlConstraint(found == positive);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, AbstractTuple[] tuples, boolean positive, Set<TypeFlag> flags) {
		int[] tuple = solution.intValuesOf(list);
		boolean found = Stream.of(tuples).parallel().anyMatch(t -> t.match(tuple));
		controlConstraint(found == positive);
	}

	private void collectStates(String current, int i, XVarInteger[] list, Map<String, List<String>> map, Set<String> reached, Set<String> seen) {
		String node = current + " " + i; // it is important to record the level
		if (seen.contains(node))
			return; // because already explored
		seen.add(node);
		List<String> nexts = map.get(current + ":" + solution.intValueOf(list[i]));
		if (nexts != null)
			for (String next : nexts)
				if (i == list.length - 1)
					reached.add(next);
				else
					collectStates(next, i + 1, list, map, reached, seen);
	}

	private Set<String> reachedStates(String startState, XVarInteger[] list, Transition[] transitions) {
		Set<String> seen = new HashSet<>(), reached = new HashSet<>();
		Map<String, List<String>> map = new HashMap<>();
		for (Transition tr : transitions)
			map.computeIfAbsent(tr.start + ":" + tr.value, r -> new ArrayList<String>()).add(tr.end);
		collectStates(startState, 0, list, map, reached, seen);
		return reached;
	}

	// private List<String> reachedStates(String startState, XVarInteger[] list, Transition[] transitions) {
	// List<String> reached = new ArrayList<>();
	// Map<String, String> map = new HashMap<>();
	// Stream.of(transitions).forEach(tr -> map.put(tr.start + ":" + tr.value, tr.end + ""));
	// String current = startState;
	// for (XVarInteger x : list) {
	// String next = map.get(current + ":" + solution.intValueOf(x));
	// System.out.println("tra " + x + " " + solution.intValueOf(x) + " " + next);
	// if (next == null)
	// return null;
	// else
	// current = next;
	// }
	// reached.add(current);
	// return reached;
	// }

	@Override
	public void buildCtrRegular(String id, XVarInteger[] list, Transition[] transitions, String startState, String[] finalStates) {
		Set<String> reached = reachedStates(startState, list, transitions);
		controlConstraint(reached.size() > 0 && Arrays.stream(finalStates).anyMatch(v -> reached.stream().anyMatch(state -> v.equals(state))));
	}

	@Override
	public void buildCtrMDD(String id, XVarInteger[] list, Transition[] transitions) {
		Set<String> reached = reachedStates(transitions[0].start, list, transitions);
		// The first state of the first transition MUST be the starting state
		controlConstraint(reached.size() > 0);
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

	private boolean distinctVectors(int[] v1, int[] v2, int[][] except) {
		assert v1.length == v2.length;
		if (except != null)
			for (int[] t : except)
				if (lexComparatorInt.compare(v1, t) == 0 || lexComparatorInt.compare(v2, t) == 0)
					return true;
		return IntStream.range(0, v1.length).anyMatch(i -> v1[i] != v2[i]);
	}

	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		int[][] tuples = solution.intValuesOf(lists);
		controlConstraint(IntStream.range(0, tuples.length)
				.allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> distinctVectors(tuples[i], tuples[j], null))));
	}

	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists, int[][] except) {
		int[][] tuples = solution.intValuesOf(lists);
		controlConstraint(IntStream.range(0, tuples.length)
				.allMatch(i -> IntStream.range(i + 1, tuples.length).allMatch(j -> distinctVectors(tuples[i], tuples[j], except))));
	}

	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		int[][] tuples = solution.intValuesOf(matrix);
		controlConstraint(IntStream.range(0, tuples.length).allMatch(i -> IntStream.of(tuples[i]).distinct().count() == tuples[i].length)); // rows
		int[][] transposedTuples = IntStream.range(0, tuples.length).mapToObj(i -> IntStream.range(0, tuples[0].length).map(j -> tuples[j][i]).toArray())
				.toArray(int[][]::new);
		controlConstraint(
				IntStream.range(0, transposedTuples.length).allMatch(i -> IntStream.of(transposedTuples[i]).distinct().count() == transposedTuples[i].length)); // cols
	}

	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix, int[] except) {
		for (XVarInteger[] row : matrix)
			buildCtrAllDifferentExcept(id, row, except);
		XVarInteger[][] transposed = IntStream.range(0, matrix.length)
				.mapToObj(i -> IntStream.range(0, matrix[0].length).mapToObj(j -> matrix[j][i]).toArray(XVarInteger[]::new)).toArray(XVarInteger[][]::new);
		for (XVarInteger[] col : transposed)
			buildCtrAllDifferentExcept(id, col, except);
	}

	@Override
	public void buildCtrAllDifferent(String id, XNode<XVarInteger>[] trees) {
		controlConstraint(valuesOfTrees(trees, null).distinct().count() == trees.length);
	}

	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		controlConstraint(IntStream.of(solution.intValuesOf(list)).distinct().count() == 1);
	}

	@Override
	public void buildCtrAllEqual(String id, XNode<XVarInteger>[] trees) {
		controlConstraint(valuesOfTrees(trees, null).distinct().count() == 1);
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, tuple.length - 1).allMatch(i -> operator.isValidFor(tuple[i], tuple[i + 1])));
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, int[] lengths, TypeOperatorRel operator) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(IntStream.range(0, tuple.length - 1).allMatch(i -> operator.isValidFor(tuple[i] + lengths[i], tuple[i + 1])));
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, XVarInteger[] lengths, TypeOperatorRel operator) {
		int[] ls = solution.intValuesOf(list);
		int[] lg = solution.intValuesOf(lengths);
		controlConstraint(IntStream.range(0, ls.length - 1).allMatch(i -> operator.isValidFor(ls[i] + lg[i], ls[i + 1])));
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
	public void buildCtrLex(String id, XVarInteger[] list, int[] limit, TypeOperatorRel operator) {
		int[] tuple = solution.intValuesOf(list);
		controlConstraint(orderedVectors(tuple, limit, operator));
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

	@Override
	public void buildCtrPrecedence(String id, XVarInteger[] list) {
		Set<Integer> allValues = new HashSet<>();
		for (XVarInteger x : list) {
			Object d = ((Dom) x.dom).allValues();
			if (d instanceof Range)
				d = ((Range) d).toArray();
			for (int v : (int[]) d)
				allValues.add(v);
		}
		buildCtrPrecedence(id, list, allValues.stream().mapToInt(v -> v).sorted().toArray(), false);
	}

	@Override
	public void buildCtrPrecedence(String id, XVarInteger[] list, int[] values, boolean covered) {
		int[] tuple = solution.intValuesOf(list);
		if (covered)
			controlConstraint(Utilities.indexOf(values[values.length - 1], tuple) != -1);
		IntStream.range(0, values.length - 1).forEach(i -> {
			int i1 = Utilities.indexOf(values[i], tuple);
			int i2 = Utilities.indexOf(values[i + 1], tuple);
			controlConstraint(i2 == -1 || (i1 != -1 && i1 < i2));
		});
	}

	protected boolean evaluateCondition(int value, Condition condition) {
		if (condition instanceof ConditionVar)
			return ((ConditionVar) condition).operator.isValidFor(value, solution.intValueOf((XVarInteger) ((ConditionVar) condition).x));
		if (condition instanceof ConditionVal)
			return ((ConditionVal) condition).operator.isValidFor(value, ((ConditionVal) condition).k);
		if (condition instanceof ConditionIntvl)
			return ((ConditionIntvl) condition).operator.isValidFor(value, ((ConditionIntvl) condition).min, ((ConditionIntvl) condition).max);
		assert condition instanceof ConditionIntset;
		return ((ConditionIntset) condition).operator.isValidFor(value, ((ConditionIntset) condition).t);
	}

	protected void checkCondition(int value, Condition condition) {
		controlConstraint(evaluateCondition(value, condition));
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
	public void buildCtrSum(String id, XNode<XVarInteger>[] trees, Condition condition) {
		BigInteger b = BigInteger.ZERO;
		for (long v : valuesOfTrees(trees, null).toArray())
			b = b.add(BigInteger.valueOf(v));
		checkCondition(b.intValueExact(), condition);
	}

	@Override
	public void buildCtrSum(String id, XNode<XVarInteger>[] trees, int[] coeffs, Condition condition) {
		BigInteger b = BigInteger.ZERO;
		for (long v : valuesOfTrees(trees, coeffs).toArray())
			b = b.add(BigInteger.valueOf(v));
		checkCondition(b.intValueExact(), condition);
	}

	@Override
	public void buildCtrSum(String id, XNode<XVarInteger>[] trees, XVarInteger[] coeffs, Condition condition) {
		XVarInteger[][] scopes = Stream.of(trees).map(t -> t.vars()).toArray(XVarInteger[][]::new);
		long[] t = IntStream.range(0, trees.length)
				.mapToLong(i -> new TreeEvaluator(trees[i]).evaluate(solution.intValuesOf(scopes[i])) * solution.intValueOf(coeffs[i])).toArray();
		BigInteger b = BigInteger.ZERO;
		for (long v : t)
			b = b.add(BigInteger.valueOf(v));
		checkCondition(b.intValueExact(), condition);
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		checkCondition((int) IntStream.of(solution.intValuesOf(list)).filter(v -> Utilities.contains(values, v)).count(), condition);
	}

	@Override
	public void buildCtrCount(String id, XNode<XVarInteger>[] trees, int[] values, Condition condition) {
		checkCondition((int) intValuesOfTrees(trees).filter(v -> Utilities.contains(values, v)).count(), condition);
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
	public void buildCtrNValues(String id, XNode<XVarInteger>[] trees, Condition condition) {
		checkCondition((int) valuesOfTrees(trees, null).distinct().count(), condition);
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
	public void buildCtrMaximum(String id, XNode<XVarInteger>[] trees, Condition condition) {
		checkCondition(Utilities.safeInt(valuesOfTrees(trees, null).max().getAsLong()), condition);
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		checkCondition(IntStream.of(solution.intValuesOf(list)).min().getAsInt(), condition);
	}

	@Override
	public void buildCtrMinimum(String id, XNode<XVarInteger>[] trees, Condition condition) {
		checkCondition(Utilities.safeInt(valuesOfTrees(trees, null).min().getAsLong()), condition);
	}

	private void checkArgKnownIndex(String id, int[] tuple, int startIndex, XVarInteger index, TypeRank rank, Condition condition, int value) {
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
		checkArgKnownIndex(id, tuple, startIndex, index, rank, condition, IntStream.of(tuple).max().getAsInt());
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		int[] tuple = solution.intValuesOf(list);
		checkArgKnownIndex(id, tuple, startIndex, index, rank, condition, IntStream.of(tuple).min().getAsInt());
	}

	private void checkArg(String id, int[] tuple, TypeRank rank, Condition condition, int value) {
		controlConstraint(condition != null);
		if (rank == TypeRank.FIRST)
			checkCondition(Utilities.indexOf(value, tuple), condition);
		else if (rank == TypeRank.LAST)
			checkCondition(Utilities.rindexOf(value, tuple), condition);
		else {
			for (int i = 0; i < tuple.length; i++)
				if (tuple[i] == value && evaluateCondition(i, condition))
					return; // ok
			controlConstraint(false);
		}
	}

	@Override
	public void buildCtrMaximumArg(String id, XVarInteger[] list, TypeRank rank, Condition condition) {
		int[] tuple = solution.intValuesOf(list);
		checkArg(id, tuple, rank, condition, IntStream.of(tuple).max().getAsInt());
	}

	@Override
	public void buildCtrMaximumArg(String id, XNode<XVarInteger>[] trees, TypeRank rank, Condition condition) {
		int[] tuple = intValuesOfTrees(trees).toArray();
		checkArg(id, tuple, rank, condition, IntStream.of(tuple).max().getAsInt());
	}

	@Override
	public void buildCtrMinimumArg(String id, XVarInteger[] list, TypeRank rank, Condition condition) {
		int[] tuple = solution.intValuesOf(list);
		checkArg(id, tuple, rank, condition, IntStream.of(tuple).min().getAsInt());
	}

	@Override
	public void buildCtrMinimumArg(String id, XNode<XVarInteger>[] trees, TypeRank rank, Condition condition) {
		int[] tuple = intValuesOfTrees(trees).toArray();
		checkArg(id, tuple, rank, condition, IntStream.of(tuple).min().getAsInt());
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
	public void buildCtrElement(String id, XVarInteger[] list, Condition condition) {
		controlConstraint(IntStream.of(solution.intValuesOf(list)).anyMatch(v -> evaluateCondition(v, condition)));
	}

	private void controlElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		int i = solution.intValueOf(index) - startIndex;
		checkCondition(list[i], condition);
		controlConstraint(rank != TypeRank.FIRST || !IntStream.range(0, i - 1).anyMatch(j -> evaluateCondition(list[j], condition)));
		controlConstraint(rank != TypeRank.LAST || !IntStream.range(i + 1, list.length).anyMatch(j -> evaluateCondition(list[j], condition)));
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		controlElement(id, solution.intValuesOf(list), startIndex, index, rank, condition);
	}

	@Override
	public void buildCtrElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		controlElement(id, list, startIndex, index, rank, condition);
	}

	@Override
	public void buildCtrElement(String id, int[][] matrix, int startRowIndex, XVarInteger rowIndex, int startColIndex, XVarInteger colIndex,
			Condition condition) {
		int i = solution.intValueOf(rowIndex) - startRowIndex;
		int j = solution.intValueOf(colIndex) - startColIndex;
		checkCondition(matrix[i][j], condition);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[][] matrix, int startRowIndex, XVarInteger rowIndex, int startColIndex, XVarInteger colIndex,
			Condition condition) {
		buildCtrElement(id, solution.intValuesOf(matrix), startRowIndex, rowIndex, startColIndex, colIndex, condition);
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

	private Map<Integer, Integer> filling(int[] tuple, int[] sizes) {
		Map<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < tuple.length; i++) {
			int b = tuple[i];
			map.put(b, sizes[i] + map.getOrDefault(b, 0));
		}
		return map;
	}

	@Override
	public void buildCtrBinPacking(String id, XVarInteger[] list, int[] sizes, Condition condition) {
		Map<Integer, Integer> map = filling(solution.intValuesOf(list), sizes);
		for (int w : map.values())
			checkCondition(w, condition);
	}

	@Override
	public void buildCtrBinPacking(String id, XVarInteger[] list, int[] sizes, int[] capacities, boolean loads) {
		TypeConditionOperatorRel op = loads ? TypeConditionOperatorRel.EQ : TypeConditionOperatorRel.LE;
		Condition[] conditions = IntStream.of(capacities).mapToObj(v -> Condition.buildFrom(op, v)).toArray(Condition[]::new);
		buildCtrBinPacking(id, list, sizes, conditions, 0);
	}

	@Override
	public void buildCtrBinPacking(String id, XVarInteger[] list, int[] sizes, XVarInteger[] capacities, boolean loads) {
		TypeConditionOperatorRel op = loads ? TypeConditionOperatorRel.EQ : TypeConditionOperatorRel.LE;
		Condition[] conditions = Stream.of(capacities).map(v -> Condition.buildFrom(op, v)).toArray(Condition[]::new);
		buildCtrBinPacking(id, list, sizes, conditions, 0);
	}

	@Override
	public void buildCtrBinPacking(String id, XVarInteger[] list, int[] sizes, Condition[] conditions, int startIndex) {
		Map<Integer, Integer> map = filling(solution.intValuesOf(list), sizes);
		for (int i = startIndex; i < startIndex + conditions.length; i++)
			checkCondition(map.getOrDefault(i, 0), conditions[i - startIndex]);
	}

	@Override
	public void buildCtrKnapsack(String id, XVarInteger[] list, int[] weights, Condition wcondition, int[] profits, Condition pcondition) {
		int[] tuple = solution.intValuesOf(list);
		checkCondition(IntStream.range(0, list.length).map(i -> tuple[i] * weights[i]).sum(), wcondition);
		checkCondition(IntStream.range(0, list.length).map(i -> tuple[i] * profits[i]).sum(), pcondition);
	}

	@Override
	public void buildCtrFlow(String id, XVarInteger[] list, int[] balance, int[][] arcs) {
		int[] tuple = solution.intValuesOf(list);
		int[] nodes = IntStream.range(0, arcs.length).flatMap(t -> IntStream.of(arcs[t])).distinct().sorted().toArray();
		assert nodes.length == balance.length;
		int sm = nodes[0];
		List<Var>[] preds = (List<Var>[]) IntStream.range(0, nodes.length).mapToObj(i -> new ArrayList<>()).toArray(List<?>[]::new);
		List<Var>[] succs = (List<Var>[]) IntStream.range(0, nodes.length).mapToObj(i -> new ArrayList<>()).toArray(List<?>[]::new);
		for (int i = 0; i < arcs.length; i++) {
			preds[arcs[i][1] - sm].add(list[i]);
			succs[arcs[i][0] - sm].add(list[i]);
		}
		for (int i = 0; i < nodes.length; i++) {
			List<Var> s = succs[i], p = preds[i];
			int[] t = solution.intValuesOf(Stream.concat(s.stream(), p.stream()).toArray(XVarInteger[]::new));
			int[] coeffs = IntStream.range(0, s.size() + p.size()).map(j -> j < s.size() ? 1 : -1).toArray();
			controlConstraint(IntStream.range(0, t.length).map(j -> t[j] * coeffs[j]).sum() == balance[i]);
		}
	}

	@Override
	public void buildCtrFlow(String id, XVarInteger[] list, int[] balance, int[][] arcs, int[] weights, Condition condition) {
		buildCtrFlow(id, list, balance, arcs);
		buildCtrSum(id, list, weights, condition);
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

	// ************************************************************************
	// ***** Methods for managing objectives
	// ************************************************************************

	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {
		controlObjective(BigInteger.valueOf(solution.intValueOf(x)));
	}

	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
		buildObjToMinimize(id, x); // possible to refer to 'minimize' because the code for checking is independent of
									// minimization/maximization
	}

	@Override
	public void buildObjToMinimize(String id, XNodeParent<XVarInteger> tree) {
		controlObjective(BigInteger.valueOf(new TreeEvaluator(tree).evaluate(solution.intValuesOf(tree.vars()))));
	}

	@Override
	public void buildObjToMaximize(String id, XNodeParent<XVarInteger> tree) {
		buildObjToMinimize(id, tree); // possible to refer to 'minimize' because the code for checking is independent of
										// minimization/maximization
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		buildObjToMinimize(id, type, list, null);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		buildObjToMinimize(id, type, list, null);
	}

	private void computeObjective(String id, TypeObjective type, BigInteger[] list, int[] coeffs) {
		BigInteger[] bis = coeffs == null ? list
				: IntStream.range(0, list.length).mapToObj(i -> list[i].multiply(BigInteger.valueOf(coeffs[i]))).toArray(BigInteger[]::new);
		if (type == NVALUES) {
			controlObjective(BigInteger.valueOf(Stream.of(bis).distinct().count()));
		} else {
			assert type != LEX;
			BigInteger computedCost = bis[0];
			for (int i = 1; i < list.length; i++) {
				if (type == SUM)
					computedCost = computedCost.add(bis[i]);
				if (type == PRODUCT)
					computedCost = computedCost.multiply(bis[i]);
				if (type == MINIMUM)
					computedCost = computedCost.min(bis[i]);
				if (type == MAXIMUM)
					computedCost = computedCost.max(bis[i]);
			}
			controlObjective(computedCost);
		}
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		computeObjective(id, type, Stream.of(list).map(x -> BigInteger.valueOf(solution.intValueOf(x))).toArray(BigInteger[]::new), coeffs);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		buildObjToMinimize(id, type, list, coeffs); // possible to refer to 'minimize' because the code for checking is
													// independent of
													// minimization/maximization
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XNode<XVarInteger>[] trees) {
		buildObjToMinimize(id, type, trees, null);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XNode<XVarInteger>[] trees) {
		buildObjToMinimize(id, type, trees); // possible to refer to 'minimize' because the code for checking is
												// independent of
												// minimization/maximization
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XNode<XVarInteger>[] trees, int[] coeffs) {
		computeObjective(id, type, valuesOfTrees(trees, null).mapToObj(l -> BigInteger.valueOf(l)).toArray(BigInteger[]::new), coeffs);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XNode<XVarInteger>[] trees, int[] coeffs) {
		buildObjToMinimize(id, type, trees, coeffs); // possible to refer to 'minimize' because the code for checking is
														// independent of
														// minimization/maximization
	}

	// ************************************************************************
	// ***** Methods on symbolic variables/constraints
	// ************************************************************************

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
		controlConstraint(
				new TreeEvaluator(tree, mapOfSymbols).evaluate(Stream.of(solution.symbolicValuesOf(scope)).mapToInt(s -> mapOfSymbols.get(s)).toArray()) == 1);
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
