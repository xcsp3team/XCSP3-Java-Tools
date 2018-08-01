package org.xcsp.modeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.FunctionalInterfaces.Intx1Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx4Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx5Predicate;
import org.xcsp.common.IVar;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Range.Rangesx3;
import org.xcsp.common.Range.Rangesx4;
import org.xcsp.common.Range.Rangesx5;
import org.xcsp.common.Utilities;

public interface ProblemAPISelectMethods extends ProblemAPIBase {

	// ************************************************************************
	// ***** Selecting Objects from Arrays, to form new arrays.
	// ***** Most of time, these methods are used for variables
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code i} present in the {@code indexes} argument. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of objects
	 * @param indexes
	 *            the indexes of the objects to be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[] vars, int[] indexes) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		// indexes = IntStream.of(indexes).sorted().distinct().toArray();
		control(IntStream.of(indexes).allMatch(i -> 0 <= i && i < vars.length), "The indexes in the specified array are not correct.");
		T[] t = Utilities.convert(Arrays.stream(indexes).mapToObj(i -> vars[i]).filter(x -> x != null).collect(Collectors.toList()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code i} going from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive). Note that {@code null} values
	 * are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of objects
	 * @param fromIndex
	 *            the index of the first object (inclusive) to be selected
	 * @param toIndex
	 *            the index of the last object (exclusive) to be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[] vars, int fromIndex, int toIndex) {
		control(0 <= fromIndex && fromIndex < toIndex && toIndex <= vars.length, "The specified indexes are not correct.");
		return select(vars, IntStream.range(fromIndex, toIndex).toArray());
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code i} present in the {@code indexes} argument. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of objects
	 * @param indexes
	 *            the indexes of the objects to be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[] vars, Collection<Integer> indexes) {
		return select(vars, indexes.stream().mapToInt(i -> i).toArray());
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code i}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of objects
	 * @param p
	 *            a predicate allowing us to test if a object at index {@code i} must be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[] vars, Intx1Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[] t = Utilities.convert(Intx1Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j)}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param p
	 *            a predicate allowing us test if a object at index {@code (i,j)} must be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][] vars, Intx2Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[] t = Utilities.convert(Intx2Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k)}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 3-dimensional array of objects
	 * @param p
	 *            a predicate allowing us test if a object at index {@code (i,j,k)} must be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][][] vars, Intx3Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[] t = Utilities.convert(Intx3Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k,l)}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 4-dimensional array of objects
	 * @param p
	 *            a predicate allowing us to test if a object at index {@code (i,j,k,l)} must be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][][][] vars, Intx4Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[] t = Utilities.convert(Intx4Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k,l,m)}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 5-dimensional array of objects
	 * @param p
	 *            a predicate allowing us to test if a object at index {@code (i,j,k,l,m)} must be selected
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][][][][] vars, Intx5Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[] t = Utilities.convert(Intx5Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code i}, that belongs to the specified range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of objects
	 * @param range
	 *            an object representing a range of indexes
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[] vars, Range range) {
		return select(vars, i -> range.contains(i));
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j)}, that belongs to the specified double range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param rangesx2
	 *            an object representing a double range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][] vars, Rangesx2 rangesx2) {
		return select(vars, (i, j) -> rangesx2.contains(i, j));
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k)}, that belongs to the specified triple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 3-dimensional array of objects
	 * @param rangesx3
	 *            an object representing a triple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][][] vars, Rangesx3 rangesx3) {
		return select(vars, (i, j, k) -> rangesx3.contains(i, j, k));
	}

	/**
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k,l)}, that belongs to the specified quadruple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 4-dimensional array of objects
	 * @param rangesx4
	 *            an object representing a quadruple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][][] vars, Rangesx4 rangesx4) {
		return select(vars, (i, j, k, l) -> rangesx4.contains(i, j));
	}

	/**
	 * Builds and returns a 1-dimensional array of object (e.g., variables)s, obtained by selecting from the specified array any object, at index
	 * {@code (i,j,k,l,m)}, that belongs to the specified quintuple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 5-dimensional array of objects
	 * @param rangesx5
	 *            an object representing a quintuple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of objects (possibly, of length 0)
	 */
	default <T> T[] select(T[][][][][] vars, Rangesx5 rangesx5) {
		return select(vars, (i, j, k, l, m) -> rangesx5.contains(i, j, k, l, m));
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables) the column at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param idColumn
	 *            the index of a column
	 * @return the column from the specified 2-dimensional array of objects, at the specified index
	 */
	default <T extends IVar> T[] columnOf(T[][] vars, int idColumn) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		control(0 <= idColumn && Stream.of(vars).allMatch(t -> t != null && idColumn < t.length), "The specified index is not valid.");
		T[] t = Utilities.convert(Stream.of(vars).map(p -> p[idColumn]).collect(Collectors.toList()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables) the downward diagonal at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param idDiagonal
	 *            the index of a downward diagonal
	 * @return the downward diagonal from the specified 2-dimensional array of objects, at the specified index
	 */
	default <T> T[] diagonalDown(T[][] vars, int idDiagonal) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(0 <= idDiagonal && idDiagonal < vars.length, "The specified index is not valid.");
		T[] t = Utilities.convert(IntStream.range(0, vars.length).mapToObj(i -> vars[i][i < idDiagonal ? vars.length - (idDiagonal - i) : i - idDiagonal])
				.collect(Collectors.toList()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables) the upward diagonal at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param idDiagonal
	 *            the index of an upward diagonal
	 * @return the upward diagonal from the specified 2-dimensional array of objects, at the specified index
	 */
	default <T> T[] diagonalUp(T[][] vars, int idDiagonal) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(0 <= idDiagonal && idDiagonal < vars.length, "The specified index is not valid.");
		T[] t = Utilities.convert(IntStream.range(0, vars.length)
				.mapToObj(i -> vars[i][i < vars.length - idDiagonal ? vars.length - idDiagonal - i - 1 : 2 * vars.length - idDiagonal - i - 1])
				.collect(Collectors.toList()));
		return t != null ? t : Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables) the main downward diagonal.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @return the main downward diagonal
	 */
	default <T extends IVar> T[] diagonalDown(T[][] vars) {
		return diagonalDown(vars, 0);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables) the main upward diagonal.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @return the main upward diagonal
	 */
	default <T> T[] diagonalUp(T[][] vars) {
		return diagonalUp(vars, 0);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables), which must represent a square of size n*n, the downward diagonal
	 * that contains the cell at row i and column j. Either i=0 and j is in 0..n-2, or j=0 and i is in 0..n-2.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param i
	 *            the index of a row
	 * @param j
	 *            the index of a column
	 * @return the downward diagonal that includes the cell at row i and column j
	 */
	default <T> T[] diagonalDown(T[][] vars, int i, int j) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "Not a regular matrix (square)");
		control(i == 0 && 0 <= j && j < vars.length - 1 || j == 0 && 0 <= i && i < vars.length - 1, "Bad values for specified integers " + i + " and " + j);
		return Utilities.convert(IntStream.range(0, vars.length - Math.max(i, j)).mapToObj(k -> vars[i + k][j + k]).collect(Collectors.toList()));
	}

	/**
	 * Returns a 2-dimensional array of objects (e.g., variables) such that each intern array corresponds to the objects on a (non-unit) downward
	 * diagonal of the specified 2-dimensional array of objects (which must represent a square of size n*n). The length of the (first dimension) of
	 * the returned array is {@code 2*n -3}.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @return a 2-dimensional array of objects, each intern array corresponding to a (non-unit) downward diagonal.
	 */
	default <T> T[][] diagonalsDown(T[][] vars) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "Not a regular matrix (square)");
		List<T[]> list = new ArrayList<>();
		for (int i = vars.length - 2; i >= 0; i--)
			list.add(diagonalDown(vars, i, 0));
		for (int j = 1; j < vars.length - 1; j++)
			list.add(diagonalDown(vars, 0, j));
		return Utilities.convert(list);
	}

	/**
	 * Selects from the specified 2-dimensional array of objects (e.g., variables), which must represent a square, the upward diagonal that contains
	 * the cell at row i and column j. Either j=0 and i is in 1..n-1, or i=n-1 and j is in 0..n-2.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @param i
	 *            the index of a row
	 * @param j
	 *            the index of a column
	 * @return the upward diagonal that includes the cell at row i and column j
	 */
	default <T> T[] diagonalUp(T[][] vars, int i, int j) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "Not a regular matrix (square)");
		control(j == 0 && 0 < i && i < vars.length || i == vars.length - 1 && 0 <= j && j < vars.length - 1,
				"Bad values for specified integers " + i + " and " + j);
		return Utilities.convert(IntStream.range(0, Math.min(i + 1, vars.length - j)).mapToObj(k -> vars[i - k][j + k]).collect(Collectors.toList()));
	}

	/**
	 * Returns a 2-dimensional array of objects (e.g., variables) such that each intern array corresponds to the objects on a (non-unit) upward
	 * diagonal of the specified 2-dimensional array of objects (which must represent a square of size n*n). The length of the (first dimension) of
	 * the returned array is {@code 2*n -3}.
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @return a 2-dimensional array of objects, each intern array corresponding to a (non-unit) upward diagonal.
	 */
	default <T> T[][] diagonalsUp(T[][] vars) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "Not a regular matrix (square)");
		List<T[]> list = new ArrayList<>();
		for (int i = 1; i < vars.length; i++)
			list.add(diagonalUp(vars, i, 0));
		for (int j = 1; j < vars.length - 1; j++)
			list.add(diagonalUp(vars, vars.length - 1, j));
		return Utilities.convert(list);
	}

	// ************************************************************************
	// ***** Selecting integers from Arrays, to form new arrays.
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value at an index {@code i} going from
	 * the {@code fromIndex} argument (inclusive) to the {@code toIndex} argument (exclusive).
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param fromIndex
	 *            the index of the first value (inclusive) to be selected
	 * @param toIndex
	 *            the index of the last value (exclusive) to be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[] t, int fromIndex, int toIndex) {
		control(0 <= fromIndex && fromIndex < toIndex && toIndex <= t.length, "The specified indexes are not correct.");
		return IntStream.range(fromIndex, toIndex).map(i -> t[i]).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value at an index {@code i} present in
	 * the {@code indexes} argument.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param indexes
	 *            the indexes of the values to be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[] t, int[] indexes) {
		// indexes = IntStream.of(indexes).sorted().distinct().toArray();
		control(IntStream.of(indexes).allMatch(i -> 0 <= i && i < t.length), "The indexes in the specified array are not correct.");
		return IntStream.of(indexes).map(i -> t[i]).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified range any value that satisfies the specified
	 * predicate.
	 * 
	 * @param r
	 *            a range of integers
	 * @param p
	 *            a predicate allowing us to test if a value in the range must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(Range r, Intx1Predicate p) {
		return r.select(p);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value, at index {@code i}, that
	 * satisfies the specified predicate.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code i} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[] t, Intx1Predicate p) {
		return IntStream.range(0, t.length).filter(i -> p.test(i)).map(i -> t[i]).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value, at index {@code (i,j)}, that
	 * satisfies the specified predicate.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code (i,j)} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[][] m, Intx2Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++)
				if (p.test(i, j))
					list.add(m[i][j]);
		return list.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value, at index {@code (i,j,k)}, that
	 * satisfies the specified predicate.
	 * 
	 * @param c
	 *            a 3-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code (i,j,k)} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[][][] c, Intx3Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < c.length; i++)
			for (int j = 0; j < c[i].length; j++)
				for (int k = 0; k < c[i][j].length; k++)
					if (p.test(i, j, k))
						list.add(c[i][j][k]);
		return list.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Selects from the specified 2-dimensional array the column at the specified index.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @param idColumn
	 *            the index of a column
	 * @return the column from the specified 2-dimensional array, at the specified index
	 */
	default int[] columnOf(int[][] m, int idColumn) {
		control(0 <= idColumn && Stream.of(m).allMatch(t -> t != null && idColumn < t.length), "The specified index is not valid.");
		return Stream.of(m).mapToInt(t -> t[idColumn]).toArray();
	}
}
