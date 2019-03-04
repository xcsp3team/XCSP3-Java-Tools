/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL
 * LIBRE CeCILL which accompanies this distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.enumerations;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to iterate over all permutations of a given set of integer values. See the Johnson-Trotter algorithm (H. F. Trotter[1962], S.
 * M. Johnson[1963]). Execute the main method for an illustration.
 */
public class EnumerationOfPermutations extends EnumerationAbstract {

	/**
	 * The values used to form permutations.
	 */
	private final int[] values;

	private final boolean[] currDirectionOfIndexes; // false=left ; true =right

	/**
	 * Builds an object that can be used for enumerating permutations, using the specified array of values.
	 * 
	 * @param values
	 *            the values used to form permutations
	 */
	public EnumerationOfPermutations(int... values) {
		super(values.length, IntStream.range(0, values.length).allMatch(i -> i == values[i]));
		this.values = values;
		this.currDirectionOfIndexes = new boolean[values.length];
		Utilities.control(IntStream.of(values).distinct().count() == values.length, "Values should all be different");
		reset();
	}

	/**
	 * Builds an object that can be used for enumerating permutations, using the specified number of values. All values are taken in the range 0 to
	 * {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form permutations
	 */
	public EnumerationOfPermutations(int nValues) {
		this(IntStream.range(0, nValues).toArray());
	}

	@Override
	protected int valAt(int i) {
		return values[currTupleOfIdxs[i]];
	}

	@Override
	protected void computeFirstTuple() {
		for (int i = 0; i < currTupleOfIdxs.length; i++)
			currTupleOfIdxs[i] = i;
		Arrays.fill(currDirectionOfIndexes, false);

	}

	private int findLargestMobileIndexPosition() {
		int best = -1;
		for (int p = 0; p < currTupleOfIdxs.length; p++) {
			int q = currDirectionOfIndexes[p] ? p + 1 : p - 1;
			if (q < 0 || q > currTupleOfIdxs.length - 1)
				continue;
			if (currTupleOfIdxs[p] > currTupleOfIdxs[q])
				if (best == -1 || currTupleOfIdxs[p] > currTupleOfIdxs[best])
					best = p;
		}
		return best;
	}

	@Override
	public boolean hasNext() {
		if (nextTuple != null)
			return nextTuple == Boolean.TRUE;
		int p = findLargestMobileIndexPosition();
		if (p == -1) {
			nextTuple = Boolean.FALSE;
			return false;
		}
		int q = currDirectionOfIndexes[p] ? p + 1 : p - 1;
		int tmp = currTupleOfIdxs[p];
		boolean tmpD = currDirectionOfIndexes[p];
		currTupleOfIdxs[p] = currTupleOfIdxs[q];
		currDirectionOfIndexes[p] = currDirectionOfIndexes[q];
		currTupleOfIdxs[q] = tmp;
		currDirectionOfIndexes[q] = tmpD;
		for (int i = 0; i < currDirectionOfIndexes.length; i++)
			if (currTupleOfIdxs[i] > tmp)
				currDirectionOfIndexes[i] = !currDirectionOfIndexes[i];
		nextTuple = Boolean.TRUE;
		return true;
	}

	// @Override
	// public int[][] toArray() {
	// reset();
	// int nPermutations = Utilities.factorial(values.length);
	// int[][] m = new int[nPermutations][values.length];
	// int cnt = 0;
	// while (hasNext()) {
	// m[cnt] = next().clone();
	// cnt++;
	// }
	// return Stream.of(m).sorted(Utilities.lexComparatorInt).toArray(int[][]::new);
	// }

	public static void main(String[] args) {
		new EnumerationOfPermutations(1, 2, 3, 4, 5).displayAllTuples();
		new EnumerationOfPermutations(3).displayAllTuples();
	}
}
