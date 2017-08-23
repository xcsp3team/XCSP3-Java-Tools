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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Utilities;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XObjectives.OObjectiveExpr;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * @author Christophe Lecoutre
 */
public class InstanceInformation implements XCallbacks2 {

	public static void main(String[] args) throws Exception {
		boolean competitionMode = args.length > 0 && args[0].equals("-cm");
		args = competitionMode ? Arrays.copyOfRange(args, 1, args.length) : args;
		if (args.length != 1) {
			System.out.println("Usage: " + InstanceInformation.class.getName() + " [-cm] <instanceFilename | directoryName> ");
			System.out.println("\tcm stands for competition mode");
		} else
			new InstanceInformation(competitionMode, args[0]);
	}

	private static final String INVALID = "invalid";

	private Implem implem = new Implem(this);

	@Override
	public Implem implem() {
		return implem;
	}

	private static class Repartitioner<T extends Comparable<? super T>> {

		private static final int MAX_DATA = 50;

		/** For each key, the number of occurrences is recorded (as value). */
		private final Map<T, Integer> repartition = new HashMap<>();

		/** Sorted keys, when the repartition has been frozen. */
		private List<T> sortedKeys;

		private String name;

		private Repartitioner(String name) {
			this.name = name;
		}

		private void clear() {
			repartition.clear();
			sortedKeys = null;
		}

		private void add(T value) {
			Integer nb = repartition.get(value);
			repartition.put(value, nb == null ? 1 : nb + 1);
		}

		private void freeze() {
			Collections.sort(sortedKeys = new ArrayList<T>(repartition.keySet()));
		}

		private T first() {
			if (sortedKeys == null)
				freeze();
			return sortedKeys.size() == 0 ? null : sortedKeys.get(0);
		}

		private T last() {
			if (sortedKeys == null)
				freeze();
			return sortedKeys.size() == 0 ? null : sortedKeys.get(sortedKeys.size() - 1);
		}

		private String pair(T k) {
			return "{\"" + name + "\":" + (k instanceof Integer ? k : "\"" + k + "\"") + ",\"count\":" + repartition.get(k) + "}";
		}

		@Override
		public String toString() {
			if (sortedKeys == null)
				freeze();
			if (sortedKeys.size() <= MAX_DATA)
				return "[" + sortedKeys.stream().map(k -> pair(k)).collect(Collectors.joining(",")) + "]";
			else {
				String s1 = IntStream.range(0, MAX_DATA / 2).mapToObj(i -> pair(sortedKeys.get(i))).collect(Collectors.joining(","));
				String s2 = IntStream.range(sortedKeys.size() - MAX_DATA / 2, sortedKeys.size()).mapToObj(i -> pair(sortedKeys.get(i)))
						.collect(Collectors.joining(", "));
				return "[" + s1 + ",\"...\"," + s2 + "]";
			}
		}
	}

	boolean competitionMode;

	private int n, e;
	private Repartitioner<Integer> sizes = new Repartitioner<>("size");
	private Repartitioner<Integer> degrees = new Repartitioner<>("degree");
	private Repartitioner<Integer> arities = new Repartitioner<>("arity");
	private Repartitioner<TypeCtr> constraints = new Repartitioner<>("type");
	private XObj obj;

	private void reset() {
		n = e = 0;
		sizes.clear();
		degrees.clear();
		arities.clear();
		constraints.clear();
		obj = null;
	}

	@Override
	public Object unimplementedCase(Object... objects) {
		throw new RuntimeException(INVALID);
	}

	@Override
	public void loadInstance(String fileName, String... discardedClasses) throws Exception {
		try {
			reset();
			System.out.print(fileName + "\t");
			XCallbacks2.super.loadInstance(fileName, discardedClasses);
			System.out.print("nbVar=" + n + ",nbConstr=" + e + ",nbDomains=" + implem().cache4DomObject.size());
			System.out.print(",domainsSize=" + sizes + ",minDomSize=" + sizes.first() + ",maxDomSize=" + sizes.last());
			System.out.print(",variablesDegree=" + degrees + ",minDegree=" + degrees.first() + ",maxDegree=" + degrees.last());
			System.out.print(",constraintArities=" + arities + ",minConstrArity=" + arities.first() + ",maxConstrArity=" + arities.last());
			int nIntension = constraints.repartition.containsKey(TypeCtr.intension) ? constraints.repartition.get(TypeCtr.intension) : 0;
			int nExtension = constraints.repartition.containsKey(TypeCtr.extension) ? constraints.repartition.get(TypeCtr.extension) : 0;
			System.out.print(",globalConstraints=" + constraints + ",nbPredicateConstr=" + nIntension + ",nbRelationConstr=" + nExtension);
			boolean objVar = obj == null ? false : (obj.type == TypeObjective.EXPRESSION && ((OObjectiveExpr) obj).rootNode.getType() == TypeExpr.VAR);
			System.out.print(",hasObjective=" + (obj != null)
					+ (obj != null ? ",objectiveType=" + (obj.minimize ? "min" : "max") + obj.type + (objVar ? "(VAR)" : "") : ""));
		} catch (Throwable e) {
			if (e.getMessage().equals(INVALID))
				System.out.print("Instance with some unimplemented method(s)");
			else
				System.out.print("Unable to be (totally) parsed");
		}
		System.out.println();
	}

	private void recursiveHandling(File file) throws Exception {
		if (file.isFile()) {
			if (file.getName().endsWith(".xml") || file.getName().endsWith(".lzma"))
				loadInstance(file.getAbsolutePath());
		} else {
			File[] files = file.listFiles();
			Arrays.sort(files);
			for (File f : files)
				recursiveHandling(f);
		}
	}

	public InstanceInformation(boolean competitionMode, String name) throws Exception {
		this.competitionMode = competitionMode;
		Utilities.control(competitionMode, "For the moment, the competition mode is the only implemented mode");
		// statements below to keep initial formulations
		Map<XCallbacksParameters, Object> map = implem().currParameters;
		map.remove(XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_LOGIC_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_COUNT_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_NVALUES_CASES);
		recursiveHandling(new File(name));
	}

	@Override
	public void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
		sizes.add(maxValue - minValue + 1);
	}

	@Override
	public void buildVarInteger(XVarInteger x, int[] values) {
		sizes.add(values.length);
	}

	@Override
	public void loadVar(XVar v) {
		n++;
		degrees.add(v.degree);
		XCallbacks2.super.loadVar(v);
	}

	@Override
	public void loadCtr(XCtr c) {
		e++;
		arities.add(c.vars().length);
		constraints.add(c.getType());
	}

	@Override
	public void loadObj(XObj o) {
		obj = o;
	}

}
