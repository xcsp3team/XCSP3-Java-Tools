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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeCombination;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeEqNeOperator;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeLogicalOperator;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Types.TypeUnaryArithmeticOperator;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.entries.ParsingEntry.CEntry;
import org.xcsp.parser.entries.ParsingEntry.OEntry;
import org.xcsp.parser.entries.ParsingEntry.VEntry;
import org.xcsp.parser.entries.XConstraints.XBlock;
import org.xcsp.parser.entries.XConstraints.XGroup;
import org.xcsp.parser.entries.XConstraints.XLogic;
import org.xcsp.parser.entries.XConstraints.XSlide;
import org.xcsp.parser.entries.XVariables.XArray;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

/**
 * @author Christophe Lecoutre
 */
public interface XCallbacks2 extends XCallbacks {

	/**********************************************************************************************
	 ***** Methods called at Specific Moments
	 *********************************************************************************************/

	@Override
	default void beginInstance(TypeFramework type) {}

	@Override
	default void endInstance() {}

	@Override
	default void beginVariables(List<VEntry> vEntries) {}

	@Override
	default void endVariables() {}

	@Override
	default void beginArray(XArray a) {}

	@Override
	default void endArray(XArray a) {}

	@Override
	default void beginConstraints(List<CEntry> cEntries) {}

	@Override
	default void endConstraints() {}

	@Override
	default void beginBlock(XBlock b) {}

	@Override
	default void endBlock(XBlock b) {}

	@Override
	default void beginGroup(XGroup g) {}

	@Override
	default void endGroup(XGroup g) {}

	@Override
	default void beginSlide(XSlide s) {}

	@Override
	default void endSlide(XSlide s) {}

	@Override
	default void beginLogic(XLogic l) {
		unimplementedCase(l.id);
	}

	@Override
	default void endLogic(XLogic l) {
		unimplementedCase(l.id);
	}

	@Override
	default void beginObjectives(List<OEntry> oEntries, TypeCombination type) {}

	@Override
	default void endObjectives() {}

	@Override
	default void beginAnnotations(Map<String, Object> aEntries) {}

	@Override
	default void endAnnotations() {}

	/**********************************************************************************************
	 ***** Methods to be implemented on integer variables/constraints
	 *********************************************************************************************/

	@Override
	default Object unimplementedCase(Object... objects) {
		System.out.println("\n\n**********************");
		System.out.println("Missing Implementation");
		StackTraceElement[] t = Thread.currentThread().getStackTrace();
		System.out.println("  Method " + t[2].getMethodName());
		System.out.println("  Class " + t[2].getClassName());
		System.out.println("  Line " + t[2].getLineNumber());
		System.out.println("**********************");
		System.out.println(Stream.of(objects).filter(o -> o != null).map(o -> o.toString()).collect(Collectors.joining("\n")));
		// throw new RuntimeException();
		System.exit(1);
		return null;
	}

	@Override
	default void buildVarInteger(XVarInteger x, int minValue, int maxValue) {
		unimplementedCase(x.id);
	}

	@Override
	default void buildVarInteger(XVarInteger x, int[] values) {
		unimplementedCase(x.id);
	}

	// ************************************************************************
	// ***** Constraint intension
	// ************************************************************************

	@Override
	default void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorSet op, int[] t) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorSet op, int min, int max) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorRel op, int k) {
		unimplementedCase(id);
	}

	// @Override
	// default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorSet op, int[] t) {
	// unimplementedCase(id);
	// }

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeUnaryArithmeticOperator aop, XVarInteger y) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, XVarInteger y, TypeConditionOperatorRel op, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorRel op, XVarInteger y) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, XVarInteger y, TypeConditionOperatorRel op, XVarInteger z) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrLogic(String id, TypeLogicalOperator op, XVarInteger[] vars) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrLogic(String id, XVarInteger x, TypeEqNeOperator op, TypeLogicalOperator lop, XVarInteger[] vars) {
		unimplementedCase(id);
	}

	// ************************************************************************
	// ***** Constraint extension
	// ************************************************************************

	@Override
	default void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferent(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferentList(String id, XVarInteger[][] lists) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferent(String id, XNodeParent<XVarInteger>[] trees) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllEqual(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrOrdered(String id, XVarInteger[] list, int[] lengths, TypeOperatorRel operator) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrOrdered(String id, XVarInteger[] list, XVarInteger[] lengths, TypeOperatorRel operator) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrLex(String id, XVarInteger[][] lists, TypeOperatorRel operator) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperatorRel operator) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrSum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrSum(String id, XVarInteger[] list, XVarInteger[] coeffs, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrSum(String id, XNodeParent<XVarInteger>[] trees, int[] coeffs, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAtMost(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrExactly(String id, XVarInteger[] list, int value, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNValues(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNotAllEqual(String id, XVarInteger[] list) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrMaximum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrMinimum(String id, XVarInteger[] list, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrElement(String id, XVarInteger[] list, XVarInteger value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrElement(String id, XVarInteger[] list, int value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrChannel(String id, XVarInteger[] list, int startIndex) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrInstantiation(String id, XVarInteger[] list, int[] values) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCircuit(String id, XVarInteger[] list, int startIndex) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, int size) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, XVarInteger size) {
		unimplementedCase(id);
	}

	@Override
	default void buildBinPacking(String id, XVarInteger[] list, int[] sizes, Condition condition) {
		unimplementedCase(id);
	}

	@Override
	default void buildBinPacking(String id, XVarInteger[] list, int[] sizes, Condition[] conditions, int startIndex) {
		unimplementedCase(id);
	}

	/**********************************************************************************************
	 ***** Methods to be implemented for managing objectives
	 *********************************************************************************************/

	@Override
	default void buildObjToMinimize(String id, XVarInteger x) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMaximize(String id, XVarInteger x) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMinimize(String id, XNodeParent<XVarInteger> tree) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMaximize(String id, XNodeParent<XVarInteger> tree) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCase(id);
	}

	@Override
	default void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs) {
		unimplementedCase(id);
	}

	/**********************************************************************************************
	 * Methods to be implemented on symbolic variables/constraints
	 *********************************************************************************************/

	@Override
	default void buildVarSymbolic(XVarSymbolic x, String[] values) {
		unimplementedCase(x.id);
	}

	@Override
	default void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent<XVarSymbolic> syntaxTreeRoot) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags) {
		unimplementedCase(id);
	}

	@Override
	default void buildCtrAllDifferent(String id, XVarSymbolic[] list) {
		unimplementedCase(id);
	}

	/**********************************************************************************************
	 * Methods to be implemented on Annotations
	 *********************************************************************************************/

	@Override
	default void buildAnnotationDecision(XVarInteger[] list) {}
}
