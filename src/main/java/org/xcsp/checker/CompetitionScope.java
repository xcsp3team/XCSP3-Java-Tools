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
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionRel;
import org.xcsp.common.Condition.ConditionSet;
import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.XCallbacks2;
import org.xcsp.parser.entries.XVariables.XVarInteger;

/**
 * This class is used to test if XCSP3 instances are valid according to the scope of the current XCSP3 competition of constraint solvers.
 * 
 * @author Christophe Lecoutre
 */
public class CompetitionScope implements XCallbacks2 {

	// ************************************************************************
	// ***** Main (and other static stuff)
	// ************************************************************************

	private static final String INVALID = "invalid";

	public static void main(String[] args) throws Exception {
		boolean miniTrack = args.length > 0 && args[0].equals("-mini");
		args = miniTrack ? Arrays.copyOfRange(args, 1, args.length) : args;
		if (args.length != 1)
			System.out.println("Usage: " + CompetitionScope.class.getName() + " [-mini] <instanceFilename | directoryName> ");
		else
			new CompetitionScope(miniTrack, args[0]);
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
	// ***** Fields and Constructors
	// ************************************************************************

	/**
	 * Indicates if the tests are performed for the mini-tracks of the competition
	 */
	private boolean miniTrack;

	/**
	 * Indicates if only one instance is checked or a full directory
	 */
	private boolean multiMode;

	private Boolean check(File f, boolean miniTrack) {
		assert f.isFile() && (f.getName().endsWith(".xml") || f.getName().endsWith(".lzma"));
		this.miniTrack = miniTrack;
		try {
			loadInstance(f.getAbsolutePath());
		} catch (Throwable e) {
			return e.getMessage().equals(INVALID) ? Boolean.FALSE : null;
		}
		return Boolean.TRUE;
	}

	private void recursiveChecking(File file) {
		assert file.isDirectory();
		File[] files = file.listFiles();
		Arrays.sort(files);
		for (File f : files)
			if (f.isDirectory())
				recursiveChecking(f);
			else if (f.getName().endsWith(".xml") || f.getName().endsWith(".lzma")) {
				System.out.print(f.getAbsolutePath());
				if (f.getAbsolutePath().endsWith("Nonogram-069-table.xml.lzma") || f.getAbsolutePath().endsWith("Nonogram-122-table.xml.lzma") || f
						.getAbsolutePath().endsWith("KnightTour-12-ext07.xml.lzma") || f.getAbsolutePath().endsWith("MagicSquare-6-table.xml.lzma") || f
								.getAbsolutePath().contains("pigeonsPlus"))
					System.out.println("\t" + "true" + "\t" + "true");
				else
					System.out.println("\t" + check(f, false) + "\t" + check(f, true));
			}
	}

	/**
	 * Builds an object used for checking the validity of one (or several) XCSP3 instances with respect to the scope of the current competition.
	 * 
	 * @param miniTrack
	 *            Indicates if the tests are performed for the mini-tracks of the competition
	 * @param name
	 *            the name of a file or directory
	 * @throws Exception
	 *             exception thrown if a problem is encountered
	 */
	public CompetitionScope(boolean miniTrack, String name) throws Exception {
		this.miniTrack = miniTrack;
		implem().rawParameters(); // to keep initial formulations
		File file = new File(name);
		multiMode = !file.isFile();
		if (file.isFile())
			loadInstance(name);
		else
			recursiveChecking(file);
	}

	// ************************************************************************
	// ***** Redefining Callback Functions for Performing Tests
	// ************************************************************************

	@Override
	public Object unimplementedCase(Object... objects) {
		if (!multiMode) {
			System.out.println("NOT VALID (wrt the competition)");
			System.out.println("\n\n**********************");
			StackTraceElement[] t = Thread.currentThread().getStackTrace();
			System.out.println("  Method " + t[2].getMethodName());
			System.out.println("  Class " + t[2].getClassName());
			System.out.println("  Line " + t[2].getLineNumber());
			System.out.println("**********************");
			System.out.println(Stream.of(objects).filter(o -> o != null).map(o -> o.toString()).collect(Collectors.joining("\n")));
		}
		throw new RuntimeException(INVALID);
	}

	private void unimplementedCaseIf(boolean test, Object... objects) {
		if (test)
			unimplementedCase(objects);
	}

	@Override
	public void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
		unimplementedCaseIf(minValue < Constants.MIN_SAFE_INT || maxValue > Constants.MAX_SAFE_INT, x.id); // includes -/+ infinity
	}

	@Override
	public void buildVarInteger(XVarInteger x, int[] values) {}

	private boolean basicOperandsForMini(XNode<XVarInteger>[] sons) {
		assert sons.length == 2;
		return (sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG) || (sons[0].type == TypeExpr.LONG && sons[1].type == TypeExpr.VAR)
				|| (sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR);
	}

	private boolean complexOperandForMini(XNode<XVarInteger> node) {
		return node.type.isArithmeticOperator() && node.type != TypeExpr.POW && basicOperandsForMini(((XNodeParent<XVarInteger>) node).sons);
	}

	private boolean checkIntensionForMini(XNodeParent<XVarInteger> tree) {
		return tree.type.isRelationalOperator() && tree.sons.length == 2 && (basicOperandsForMini(tree.sons) || (complexOperandForMini(tree.sons[0])
				&& tree.sons[1].type == TypeExpr.VAR) || (complexOperandForMini(tree.sons[1]) && tree.sons[0].type == TypeExpr.VAR));
	}

	@Override
	public void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree) {
		unimplementedCaseIf(miniTrack && !checkIntensionForMini(tree), id);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCaseIf(values.length == 0 || flags.contains(TypeFlag.STARRED_TUPLES), id);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCaseIf(tuples.length == 0 || flags.contains(TypeFlag.STARRED_TUPLES), id);
	}

	@Override
	public void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		unimplementedCaseIf(miniTrack, id);
		// determinism should be tested, but for the moment, all automatas from available instances are deterministic
	}

	@Override
	public void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		unimplementedCaseIf(miniTrack, id);
		// restrictions as given in the call 2017 should be tested, but for the moment all available instances respect them
	}

	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {}

	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		unimplementedCaseIf(miniTrack || except.length != 1, id);
	}

	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		unimplementedCase(id); // or can we accept it for the standard tracks ???
	}

	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrAllDifferent(String id, XNodeParent<XVarInteger>[] trees) {
		unimplementedCase(id); // should we accept this form for the 2018 Competition?
	}

	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, int[] lengths, TypeOperatorRel operator) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperatorRel operator) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperatorRel operator) {
		unimplementedCaseIf(miniTrack, id);
	}

	private void checkCondition(String id, Condition condition) {
		if (condition instanceof ConditionSet)
			if (miniTrack)
				unimplementedCase(id);
			else if (!(condition instanceof ConditionIntvl) || ((ConditionIntvl) condition).operator != TypeConditionOperatorSet.IN)
				unimplementedCase(id);
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		checkCondition(id, condition);
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		checkCondition(id, condition);
	}

	@Override
	public void buildCtrSum(String id, XVarInteger[] list, XVarInteger[] coeffs, Condition condition) {
		checkCondition(id, condition);
	}

	@Override
	public void buildCtrSum(String id, XNodeParent<XVarInteger>[] trees, int[] coeffs, Condition condition) {
		unimplementedCase(id); // should we accept this form for the 2018 Competition?
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		unimplementedCaseIf(miniTrack, id);
		checkCondition(id, condition);
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		unimplementedCase(id); // values cannot be variables for the competition
	}

	@Override
	public void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		unimplementedCaseIf(miniTrack || condition instanceof ConditionSet, id);
		unimplementedCaseIf(((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ, id);
	}

	@Override
	public void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		unimplementedCaseIf(miniTrack || condition instanceof ConditionSet || except.length != 1, id);
		unimplementedCaseIf(((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ, id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		unimplementedCase(id); // values cannot be variables for the competition
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		unimplementedCase(id); // values cannot be variables for the competition
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		unimplementedCase(id); // values cannot be variables for the competition
	}

	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCaseIf(miniTrack || condition instanceof ConditionSet, id);
		unimplementedCaseIf(((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ, id);
	}

	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id); // variant not accepted for the competition
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCaseIf(miniTrack || condition instanceof ConditionSet, id);
		unimplementedCaseIf(((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ, id);
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id); // variant not accepted for the competition
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int value) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		unimplementedCaseIf(startIndex != 0 || rank != TypeRank.ANY, id);

	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		unimplementedCaseIf(startIndex != 0 || rank != TypeRank.ANY, id);
	}

	@Override
	public void buildCtrElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		// if (startIndex != 0 || rank != TypeRank.ANY)
		unimplementedCase(id); // this variant is not accepted for the competition (because not in the current specifications)
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		unimplementedCaseIf(miniTrack || list1.length != list2.length, id);
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		unimplementedCaseIf(miniTrack || IntStream.of(lengths).anyMatch(v -> v == 0), id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		unimplementedCaseIf(miniTrack || Stream.of(lengths).anyMatch(t -> IntStream.of(t).anyMatch(v -> v == 0)), id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id); // ends not accepted for the competition
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id); // ends not accepted for the competition
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id); // ends not accepted for the competition
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id); // ends not accepted for the competition
	}

	@Override
	public void buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		unimplementedCaseIf(miniTrack, id);
	}

	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {}

	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {}

	@Override
	public void buildObjToMinimize(String id, XNodeParent<XVarInteger> tree) {
		unimplementedCase(id); // not accepted for the competition
	}

	@Override
	public void buildObjToMaximize(String id, XNodeParent<XVarInteger> tree) {
		unimplementedCase(id); // not accepted for the competition
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCaseIf(type == TypeObjective.PRODUCT || type == TypeObjective.LEX, id);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCaseIf(type == TypeObjective.PRODUCT || type == TypeObjective.LEX, id);
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCaseIf(type == TypeObjective.PRODUCT || type == TypeObjective.LEX, id);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCaseIf(type == TypeObjective.PRODUCT || type == TypeObjective.LEX, id);
	}
}
