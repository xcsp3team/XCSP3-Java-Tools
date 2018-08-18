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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Utilities;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XObjectives.OObjectiveExpr;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

/**
 * This class allows us to display some general information about XCSP3 instances, such the number of variables, the number of constraints, the
 * distribution of constraints, etc.
 * 
 * @author Christophe Lecoutre
 */
public class FeatureDisplayer implements XCallbacks2 {

	// ************************************************************************
	// ***** Main (and other static stuff)
	// ************************************************************************

	private static final String INVALID = "invalid";

	public static void main(String[] args) throws Exception {
		boolean competitionMode = args.length > 0 && args[0].equals("-cm");
		args = competitionMode ? Arrays.copyOfRange(args, 1, args.length) : args;
		if (args.length != 1) {
			System.out.println("Usage: " + FeatureDisplayer.class.getName() + " [-cm] <instanceFilename | directoryName> ");
			System.out.println("\tcm stands for competition mode");
		} else
			new FeatureDisplayer(competitionMode, args[0]);
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

	/**
	 * This class allows us to count the number of occurrences of various keys.
	 * 
	 * @param <T>
	 *            The type of keys
	 */
	private static class Repartitioner<T extends Comparable<? super T>> {

		private static final int FULL_DISPLAY_LIMIT = 50;

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
			return "{\"" + name + "\":" + (k instanceof Integer ? k.toString() : "\"" + k + "\"") + ",\"count\":" + repartition.get(k) + "}";
		}

		@Override
		public String toString() {
			if (sortedKeys == null)
				freeze();
			if (sortedKeys.size() <= FULL_DISPLAY_LIMIT)
				return "[" + sortedKeys.stream().map(k -> pair(k)).collect(joining(",")) + "]";
			String s1 = IntStream.range(0, FULL_DISPLAY_LIMIT / 2).mapToObj(i -> pair(sortedKeys.get(i))).collect(joining(","));
			String s2 = IntStream.range(sortedKeys.size() - FULL_DISPLAY_LIMIT / 2, sortedKeys.size()).mapToObj(i -> pair(sortedKeys.get(i)))
					.collect(joining(", "));
			return "[" + s1 + ",\"...\"," + s2 + "]";
		}
	}

	// ************************************************************************
	// ***** Fields and Constructors
	// ************************************************************************

	private boolean competitionMode;

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
	public void loadInstance(String fileName, String... discardedClasses) throws Exception {
		try {
			reset();
			XCallbacks2.super.loadInstance(fileName, discardedClasses);
			if (competitionMode) {
				System.out.print("nbVar=" + n + ",nbConstr=" + e + ",nbDomains=" + implem().cache4DomObject.size());
				System.out.print(",domainsSize='" + sizes + "',minDomSize=" + sizes.first() + ",maxDomSize=" + sizes.last());
				System.out.print(",variablesDegree='" + degrees + "',minDegree=" + degrees.first() + ",maxDegree=" + degrees.last());
				System.out.print(",constraintArities='" + arities + "',minConstrArity=" + arities.first() + ",maxConstrArity=" + arities.last());
				int nIntension = constraints.repartition.getOrDefault(TypeCtr.intension, 0);
				int nExtension = constraints.repartition.getOrDefault(TypeCtr.extension, 0);
				System.out.print(",globalConstraints='" + constraints + "',nbPredicateConstr=" + nIntension + ",nbRelationConstr=" + nExtension);
				boolean objVar = obj == null ? false : (obj.type == TypeObjective.EXPRESSION && ((OObjectiveExpr) obj).rootNode.getType() == TypeExpr.VAR);
				System.out.print(",hasObjective=" + (obj != null)
						+ (obj != null ? ",objectiveType='" + (obj.minimize ? "min" : "max") + ' ' + (objVar ? "VAR" : obj.type) + "'" : ""));
			}
		} catch (Throwable e) {
			if (e.getMessage().equals(INVALID))
				System.out.print("Instance with some unimplemented method(s)");
			else
				System.out.print("Unable to be (totally) parsed");
			// e.printStackTrace();
		}
		System.out.println();
	}

	private void recursiveHandling(File file) throws Exception {
		if (!file.exists())
			Utilities.exit("The file " + file.getName() + " does not exist (or has not been found)");
		if (file.isFile()) {
			if (file.getName().endsWith(".xml") || file.getName().endsWith(".lzma"))
				loadInstance(file.getAbsolutePath());
			else
				Utilities.exit("The file " + file.getName() + " has not a proper suffix (.xml or .lzma)");
		} else
			for (File f : Stream.of(file.listFiles(f -> f.getName().endsWith(".xml") || f.getName().endsWith(".lzma"))).sorted().collect(toList()))
				recursiveHandling(f);
	}

	/**
	 * Builds an object {@code InstanceInformation} that directly parses the XCSP3 file(s) from the specified name that denotes a file or a directory.
	 * 
	 * @param competitionMode
	 *            {@code true} if information is displayed to be used by tools of XCSP3 competitions
	 * @param name
	 *            the name of a file or directory
	 * @throws Exception
	 */
	public FeatureDisplayer(boolean competitionMode, String name) throws Exception {
		this.competitionMode = competitionMode;
		Utilities.control(competitionMode, "For the moment, the competition mode is the only implemented mode");
		implem().rawParameters(); // to keep initial formulations (no reformation being processed)
		recursiveHandling(new File(name));
	}

	// ************************************************************************
	// ***** Overridden Callback Functions
	// ************************************************************************

	@Override
	public Object unimplementedCase(Object... objects) {
		throw new RuntimeException(INVALID);
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
	public void buildVarSymbolic(XVarSymbolic x, String[] values) {
		// TODO : manage some specific information about symbolic variables
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
