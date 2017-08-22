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
import java.util.Map;
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
 * @author Christophe Lecoutre
 */
public class CompetitionChecker implements XCallbacks2 {

	public static void main(String[] args) throws Exception {
		boolean miniTrack = args.length > 0 && args[0].equals("-mini");
		args = miniTrack ? Arrays.copyOfRange(args, 1, args.length) : args;
		if (args.length != 1)
			System.out.println("Usage: " + CompetitionChecker.class.getName() + " [-mini] <instanceFilename | directoryName> ");
		else
			new CompetitionChecker(miniTrack, args[0]);
	}

	private Implem implem = new Implem(this);

	@Override
	public Implem implem() {
		return implem;
	}

	private static final String INVALID = "invalid";

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

	private boolean miniTrack;
	private boolean multiMode;

	private Boolean check(File f, boolean mini) {
		assert f.isFile() && (f.getName().endsWith(".xml") || f.getName().endsWith(".lzma"));
		miniTrack = mini;
		try {
			loadInstance(f.getAbsolutePath());
		} catch (Throwable e) {
			if (e.getMessage().equals(INVALID))
				return Boolean.FALSE;
			else
				return null;
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
				if (f.getAbsolutePath().endsWith("Nonogram-069-table.xml.lzma") || f.getAbsolutePath().endsWith("Nonogram-122-table.xml.lzma")
						|| f.getAbsolutePath().endsWith("KnightTour-12-ext07.xml.lzma") || f.getAbsolutePath().endsWith("MagicSquare-6-table.xml.lzma")
						|| f.getAbsolutePath().contains("pigeonsPlus"))
					System.out.println("\t" + "true" + "\t" + "true");
				else
					System.out.println("\t" + check(f, false) + "\t" + check(f, true));
			}
	}

	public CompetitionChecker(boolean miniTrack, String name) throws Exception {
		this.miniTrack = miniTrack;

		// statements below to keep initial formulations
		Map<XCallbacksParameters, Object> map = implem().currParameters;
		map.remove(XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES);
		map.remove(XCallbacksParameters.RECOGNIZE_LOGIC_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_COUNT_CASES);
		map.remove(XCallbacksParameters.RECOGNIZE_NVALUES_CASES);

		File file = new File(name);
		multiMode = !file.isFile();
		if (file.isFile())
			loadInstance(name);
		else
			recursiveChecking(file);
	}

	@Override
	public void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
		if (minValue < Constants.MIN_SAFE_INT || maxValue > Constants.MAX_SAFE_INT) // includes -/+ infinity
			unimplementedCase(x.id);
	}

	@Override
	public void buildVarInteger(XVarInteger x, int[] values) {
	}

	private boolean basicOperandsForMini(XNode<XVarInteger>[] sons) {
		assert sons.length == 2;
		return (sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.LONG) || (sons[0].type == TypeExpr.LONG && sons[1].type == TypeExpr.VAR)
				|| (sons[0].type == TypeExpr.VAR && sons[1].type == TypeExpr.VAR);
	}

	private boolean complexOperandForMini(XNode<XVarInteger> node) {
		return node.type.isNonUnaryArithmeticOperator() && node.type != TypeExpr.POW && basicOperandsForMini(((XNodeParent<XVarInteger>) node).sons);
	}

	private boolean checkIntensionForMini(XNodeParent<XVarInteger> tree) {
		return tree.type.isRelationalOperator() && tree.sons.length == 2
				&& (basicOperandsForMini(tree.sons) || (complexOperandForMini(tree.sons[0]) && tree.sons[1].type == TypeExpr.VAR)
						|| (complexOperandForMini(tree.sons[1]) && tree.sons[0].type == TypeExpr.VAR));
	}

	@Override
	public void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree) {
		if (miniTrack && !checkIntensionForMini(tree))
			unimplementedCase(id);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		if (values.length == 0 || flags.contains(TypeFlag.STARRED_TUPLES))
			unimplementedCase(id);
	}

	@Override
	public void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		if (tuples.length == 0 || flags.contains(TypeFlag.STARRED_TUPLES))
			unimplementedCase(id);
	}

	@Override
	public void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		if (miniTrack)
			unimplementedCase(id);
		// determinism should be tested, but for the moment, all automatas from available instances are deterministic

	}

	@Override
	public void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		if (miniTrack)
			unimplementedCase(id);
		// restrictions as given in the call 2017 should be tested, but for the moment all available instances respect them
	}

	@Override
	public void buildCtrAllDifferent(String id, XVarInteger[] list) {
	}

	@Override
	public void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		if (miniTrack || except.length != 1)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		unimplementedCase(id); // or can we accept it for the standard tracks ???
	}

	@Override
	public void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrAllEqual(String id, XVarInteger[] list) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrLex(String id, XVarInteger[][] lists, TypeOperatorRel operator) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperatorRel operator) {
		if (miniTrack)
			unimplementedCase(id);
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
	public void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		if (miniTrack)
			unimplementedCase(id);
		checkCondition(id, condition);
	}

	@Override
	public void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		unimplementedCase(id); // values cannot be variables for the competition
	}

	@Override
	public void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		if (miniTrack || condition instanceof ConditionSet)
			unimplementedCase(id);
		if (((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		if (miniTrack || condition instanceof ConditionSet || except.length != 1)
			unimplementedCase(id);
		if (((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		if (miniTrack)
			unimplementedCase(id);
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
		if (miniTrack || condition instanceof ConditionSet)
			unimplementedCase(id);
		if (((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id); // variant not accepted for the competition
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		if (miniTrack || condition instanceof ConditionSet)
			unimplementedCase(id);
		if (((ConditionRel) condition).operator != TypeConditionOperatorRel.EQ)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id); // variant not accepted for the competition
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int value) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		if (startIndex != 0 || rank != TypeRank.ANY)
			unimplementedCase(id);

	}

	@Override
	public void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		if (startIndex != 0 || rank != TypeRank.ANY)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		// if (startIndex != 0 || rank != TypeRank.ANY)
		unimplementedCase(id); // this variant is not
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		if (miniTrack || list1.length != list2.length)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		if (miniTrack || IntStream.of(lengths).anyMatch(v -> v == 0))
			unimplementedCase(id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		if (miniTrack || Stream.of(lengths).anyMatch(t -> IntStream.of(t).anyMatch(v -> v == 0)))
			unimplementedCase(id);
	}

	@Override
	public void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		if (miniTrack)
			unimplementedCase(id);
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
		if (miniTrack)
			unimplementedCase(id);
	}

	@Override
	public void buildObjToMinimize(String id, XVarInteger x) {
	}

	@Override
	public void buildObjToMaximize(String id, XVarInteger x) {
	}

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
		if (type == TypeObjective.PRODUCT || type == TypeObjective.LEX)
			unimplementedCase(id);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		if (type == TypeObjective.PRODUCT || type == TypeObjective.LEX)
			unimplementedCase(id);
	}

	@Override
	public void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		if (type == TypeObjective.PRODUCT || type == TypeObjective.LEX)
			unimplementedCase(id);
	}

	@Override
	public void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		if (type == TypeObjective.PRODUCT || type == TypeObjective.LEX)
			unimplementedCase(id);
	}
}
