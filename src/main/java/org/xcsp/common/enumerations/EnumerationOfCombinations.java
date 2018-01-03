/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.enumerations;

import java.util.stream.IntStream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to iterate over all combinations of a given length from a given set of values. Execute the main method for an illustration.
 */
public class EnumerationOfCombinations extends EnumerationCartesian {

	/**
	 * Indicates if the numbers of possible values at each position are all equal.
	 */
	private final boolean uniform;

	/**
	 * Builds an object that can be used for enumerating combinations, using the specified numbers of values.
	 * 
	 * @param nValues
	 *            the number of possible different values at each position of the tuples. These numbers must be in an increasing order (and are
	 *            usually all equal)
	 */
	public EnumerationOfCombinations(int[] nValues) {
		super(nValues);
		this.uniform = IntStream.range(1, nValues.length).allMatch(i -> nValues[i] == nValues[0]);
		Utilities.control(IntStream.range(0, nValues.length - 1).allMatch(i -> nValues[i] <= nValues[i + 1]), "Numbers are not in an increasing order");
	}

	/**
	 * Builds an object that can be used for enumerating combinations, using the specified number of values. Each tuple (combination) has the
	 * specified length.
	 * 
	 * @param nValues
	 *            the number of values used to form combinations
	 * @param tupleLength
	 *            the length of each combination
	 */
	public EnumerationOfCombinations(int nValues, int tupleLength) {
		this(IntStream.range(0, tupleLength).map(i -> nValues).toArray());
	}

	@Override
	protected void computeFirstTuple() {
		for (int i = 0; nextTuple == Boolean.TRUE && i < values.length; i++)
			if (i >= values[i].length)
				nextTuple = Boolean.FALSE;
			else
				currTupleOfIdxs[i] = i;
	}

	/**
	 * Determines if it is possible to extend the current tuple from the given position
	 */
	private boolean isExtensibleFrom(int pos) {
		if (uniform)
			return (values[pos].length - currTupleOfIdxs[pos] > values.length - pos);
		int value = currTupleOfIdxs[pos];
		for (int i = pos; i < currTupleOfIdxs.length; i++)
			if (++value >= values[i].length)
				return false;
		return true;
	}

	@Override
	public boolean hasNext() {
		if (nextTuple != null)
			return nextTuple == Boolean.TRUE;
		int last = values.length - 1;
		while (last >= 0)
			if (!isExtensibleFrom(last))
				last--;
			else {
				currTupleOfIdxs[last] = currTupleOfIdxs[last] + 1;
				for (int i = last + 1; i < values.length; i++)
					currTupleOfIdxs[i] = currTupleOfIdxs[i - 1] + 1;
				nextTuple = Boolean.TRUE;
				return true;
			}
		nextTuple = Boolean.FALSE;
		return false;
	}

	public static void main(String[] args) {
		new EnumerationOfCombinations(7, 4).displayAllTuples();
		new EnumerationOfCombinations(new int[] { 3, 4, 4, 5 }).displayAllTuples();
	}
}
