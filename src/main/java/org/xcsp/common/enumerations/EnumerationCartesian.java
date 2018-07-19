/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.enumerations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Utilities;

/**
 * This class allows iterating over all tuples of a given length from a given set of numbers (of possibilities at each position). Execute the main
 * method for an illustration.
 */
public class EnumerationCartesian extends EnumerationAbstract {

	private static Map<String, int[][]> cacheOfTuples;

	/**
	 * Returns an array with all tuples of the specified length such that for each tuple:
	 * <ul>
	 * <li>at each position, a value is between 0 (inclusive) and {@code nValues} (exclusive)</li>
	 * <li>all values are all different</li>
	 * <li>the sum of the values in the tuple is equal to the specified limit</li>
	 * </ul>
	 * 
	 * @param limit
	 *            the integer denoting the limit of the sum
	 * @param nValues
	 *            indicates how many values are possible at each position
	 * @param tupleLength
	 *            the length of each tuple in the enumeration
	 * @param offset
	 *            the value that must be systematically added to each value of each tuple
	 * @return an array with all tuples of the specified length that respect a sum equality and an allDifferent restriction
	 */
	public static int[][] tuplesWithDiffValuesSummingTo(int limit, int nValues, int tupleLength, int offset) {
		if (cacheOfTuples == null)
			cacheOfTuples = new HashMap<>();
		String key = limit + "_" + nValues + "_" + tupleLength + "_" + offset;
		int[][] tuples = cacheOfTuples.get(key);
		if (tuples != null)
			return tuples;
		List<int[]> list = new ArrayList<>();
		int[][] combinations = new EnumerationOfCombinations(nValues, tupleLength).toArray();
		int[][] permutations = new EnumerationOfPermutations(tupleLength).toArray();
		for (int[] t : combinations) {
			if (offset != 0)
				for (int i = 0; i < t.length; i++)
					t[i] += offset;
			if (IntStream.of(t).sum() == limit)
				for (int[] perm : permutations)
					list.add(IntStream.range(0, t.length).map(i -> t[perm[i]]).toArray());
		}
		tuples = list.stream().sorted(Utilities.lexComparatorInt).toArray(int[][]::new);
		cacheOfTuples.put(key, tuples);
		return tuples;
	}

	/**********************************************************************************************
	 * End of static section
	 *********************************************************************************************/

	/**
	 * Gives the sets of values on which the tuples will be built. {@code values[i]} is a 1-dimensional array with all possibles (ordered) values at
	 * position {@code i} in a tuple.
	 */
	protected final int[][] values;

	/**
	 * Builds an object that can be used for enumerating tuples, using the specified sets of values. If the specified Boolean is {@code true} the
	 * specified 2-dimensional array is cloned.
	 * 
	 * @param values
	 *            the sets of values on which the tuples will be built
	 * @param clone
	 *            if {@code true} the specified 2-dimensional array is cloned
	 */
	public EnumerationCartesian(int[][] values, boolean clone) {
		super(values.length, Stream.of(values).allMatch(t -> IntStream.range(0, t.length).allMatch(i -> t[i] == i)));
		this.values = clone ? Stream.of(values).map(t -> t.clone()).toArray(int[][]::new) : values;
		Utilities.control(Stream.of(values).allMatch(t -> t.length > 0 && IntStream.range(0, t.length - 1).allMatch(i -> t[i] < t[i + 1])),
				"values are not correctly formed (order,...)");
		reset();
	}

	/**
	 * Builds an object that can be used for enumerating tuples, using the specified sets of values.
	 * 
	 * @param values
	 *            the sets of values on which the tuples will be built
	 */
	public EnumerationCartesian(int[][] values) {
		this(values, true);
	}

	/**
	 * Builds an object that can be used for enumerating tuples, using the specified numbers of values. Each tuple will contain a value at position
	 * {@code i} in the range 0 to {@code nValues[i].length-1}.
	 * 
	 * @param nValues
	 *            indicates how many values are possible at each position
	 */
	public EnumerationCartesian(int... nValues) {
		this(IntStream.range(0, nValues.length).mapToObj(i -> IntStream.range(0, nValues[i]).toArray()).toArray(int[][]::new), false);
	}

	/**
	 * Builds an object that can be used for enumerating tuples, using the specified numbers of values. Each tuple has the specified length and will
	 * contain a value at position {@code i} in the range 0 to {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form tuples
	 * @param tupleLength
	 *            the length of each tuple
	 */
	public EnumerationCartesian(int nValues, int tupleLength) {
		this(IntStream.range(0, tupleLength).map(i -> nValues).toArray());
	}

	@Override
	protected int valAt(int i) {
		return values[i][currTupleOfIdxs[i]];
	}

	@Override
	protected void computeFirstTuple() {
		Arrays.fill(currTupleOfIdxs, 0);
	}

	@Override
	public boolean hasNext() {
		if (nextTuple != null)
			return nextTuple == Boolean.TRUE;
		for (int i = values.length - 1; i >= 0; i--)
			if (currTupleOfIdxs[i] + 1 == values[i].length)
				currTupleOfIdxs[i] = 0;
			else {
				currTupleOfIdxs[i]++;
				nextTuple = Boolean.TRUE;
				return true;
			}
		nextTuple = Boolean.FALSE;
		return false;
	}

	public static void main(String[] args) {
		new EnumerationCartesian(7, 7, 7, 7).displayAllTuples();
		new EnumerationCartesian(new int[][] { { 2, 3, 4 }, { 1, 5 }, { 12, 20 } }).displayAllTuples();
	}
}
