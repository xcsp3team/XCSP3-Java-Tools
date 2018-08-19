package org.xcsp.modeler.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.FunctionalInterfaces.Intx1Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3Predicate;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Utilities;
import org.xcsp.common.enumerations.EnumerationCartesian;
import org.xcsp.common.enumerations.EnumerationOfCombinations;
import org.xcsp.common.enumerations.EnumerationOfPermutations;

public interface ProblemAPIOnVals extends ProblemAPIBase {

	// ************************************************************************
	// ***** Methods valuesIn() and valuesFrom()
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified sequence of parameters. Each element of the sequence can be an
	 * {@code Integer}, a {@code Range}, an array (of any dimension), a Stream (or IntStream), a collection, etc. All integers are collected and
	 * concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param objects
	 *            a sequence of objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] vals(Object... objects) {
		return Utilities.collectInt(objects);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified sequence of parameters. Each element of the sequence can be an
	 * {@code Integer}, a {@code Range}, an array (of any dimension), a Stream (or IntStream), a collection, etc. All integers are collected and
	 * concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param object
	 *            an object
	 * @param otherObjects
	 *            a sequence of objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] valuesIn(Object object, Object... otherObjects) {
		return vals(object, otherObjects);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified stream. Each object of the stream is mapped to another object by the
	 * specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param stream
	 *            a stream of objects
	 * @param f
	 *            a function mapping objects of the stream into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] valuesFrom(Stream<T> stream, Function<T, Object> f) {
		return valuesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified stream. Each integer of the stream is mapped to another object by the
	 * specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param stream
	 *            a stream of integers
	 * @param f
	 *            a function mapping integers of the stream into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] valuesFrom(IntStream stream, Function<Integer, Object> f) {
		return valuesFrom(stream.boxed(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified array. Each object of the array is mapped to another object by the
	 * specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param t
	 *            a 1-dimensional array of objects
	 * @param f
	 *            a function mapping objects of the array into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] valuesFrom(T[] t, Function<T, Object> f) {
		return valuesFrom(Stream.of(t), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified collection. Each object of the collection is mapped to another object
	 * by the specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param c
	 *            a collection of objects
	 * @param f
	 *            a function mapping objects of the collection into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] valuesFrom(Collection<T> c, Function<T, Object> f) {
		return valuesFrom(c.stream(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified array. Each integer of the array is mapped to another object by the
	 * specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param f
	 *            a function mapping integers of the array into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] valuesFrom(int[] t, Function<Integer, Object> f) {
		return valuesFrom(IntStream.of(t).boxed(), f);
	}

	default int[] valuesFrom(char[] t, Function<Character, Object> f) {
		return valuesIn(IntStream.range(0, t.length).mapToObj(i -> f.apply(t[i])));
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified range. Each integer of the range is mapped to another object by the
	 * specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param r
	 *            a range
	 * @param f
	 *            a function mapping integers of the range into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] valuesFrom(Range r, Function<Integer, Object> f) {
		return valuesFrom(r.stream(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified double range. Each pair of integers of the double range is mapped to
	 * another object by the specified function. Then, all integers are collected and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param r2
	 *            a double range
	 * @param f
	 *            a function mapping pairs of integers of the double range into other objects
	 * @return a 1-dimensional array formed of collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] valuesFrom(Rangesx2 r2, BiFunction<Integer, Integer, Object> f) {
		List<Object> list = new ArrayList<>();
		for (int i : r2.items[0])
			for (int j : r2.items[1]) {
				Object t = f.apply(i, j);
				if (t != null)
					list.add(t);
			}
		return valuesIn(list);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified sequence of parameters. Each element of the sequence can be an
	 * {@code Integer}, a {@code Range}, an array (of any dimension), a Stream (or IntStream), a collection, etc. All integers are collected, sorted,
	 * made distinct and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param objects
	 *            an array (varargs) of objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] singleValuesIn(Object... objects) {
		return IntStream.of(valuesIn(objects)).sorted().distinct().toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified stream. Each object of the stream is mapped to another object by the
	 * specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param stream
	 *            a stream of objects
	 * @param f
	 *            a function mapping objects of the stream into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] singleValuesFrom(Stream<T> stream, Function<T, Object> f) {
		return singleValuesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified stream. Each integer of the stream is mapped to another object by the
	 * specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param stream
	 * @param f
	 *            a function mapping integers of the stream into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] singleValuesFrom(IntStream stream, Function<Integer, Object> f) {
		return singleValuesFrom(stream.boxed(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified array. Each object of the stream is mapped to another object by the
	 * specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param t
	 *            a 1-dimensional array of objects
	 * @param f
	 *            a function mapping objects of the array into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] singleValuesFrom(T[] t, Function<T, Object> f) {
		return singleValuesFrom(Stream.of(t), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified collection. Each object of the collection is mapped to another object
	 * by the specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null}
	 * values are discarded.
	 * 
	 * @param c
	 *            a collection of objects
	 * @param f
	 *            a function mapping objects of the collection into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default <T> int[] singleValuesFrom(Collection<T> c, Function<T, Object> f) {
		return singleValuesFrom(c.stream(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified array. Each integer of the array is mapped to another object by the
	 * specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param f
	 *            a function mapping integers of the array into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] singleValuesFrom(int[] t, Function<Integer, Object> f) {
		return singleValuesFrom(IntStream.of(t).boxed(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified range. Each integer of the range is mapped to another object by the
	 * specified function. Then, all integers are collected, sorted, made distinct and concatenated to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param r
	 *            a range
	 * @param f
	 *            a function mapping integers of the range into other objects
	 * @return a 1-dimensional array formed of distinct sorted collected integers (occurrences of {@code null} being discarded}
	 */
	default int[] singleValuesFrom(Range r, Function<Integer, Object> f) {
		return singleValuesFrom(r.stream(), f);
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
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value that satisfies the specified
	 * predicate.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value v in the array must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] select(int[] t, Intx1Predicate p) {
		return IntStream.of(t).filter(v -> p.test(v)).toArray();
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained by selecting from the specified array any row (tuple) that satisfies the
	 * specified predicate.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a row (tuple) in the array must be selected
	 * @return a 2-dimensional array of integers
	 */
	default int[][] select(int[][] m, Predicate<int[]> p) {
		return Stream.of(m).filter(t -> t != null && p.test(t)).toArray(int[][]::new);
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
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value at an index {@code i} that
	 * satisfies the specified predicate.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code i} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] selectFromIndexing(int[] t, Intx1Predicate p) {
		return IntStream.range(0, t.length).filter(i -> p.test(i)).map(i -> t[i]).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value at an index {@code (i,j)} that
	 * satisfies the specified predicate.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code (i,j)} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] selectFromIndexing(int[][] m, Intx2Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < m.length; i++)
			for (int j = 0; j < m[i].length; j++)
				if (p.test(i, j))
					list.add(m[i][j]);
		return list.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from the specified array any value at an index {@code (i,j,k)} that
	 * satisfies the specified predicate.
	 * 
	 * @param c
	 *            a 3-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code (i,j,k)} must be selected
	 * @return a 1-dimensional array of integers
	 */
	default int[] selectFromIndexing(int[][][] c, Intx3Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < c.length; i++)
			for (int j = 0; j < c[i].length; j++)
				for (int k = 0; k < c[i][j].length; k++)
					if (p.test(i, j, k))
						list.add(c[i][j][k]);
		return list.stream().mapToInt(i -> i).toArray();
	}

	// ************************************************************************
	// ***** other methods
	// ************************************************************************

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

	/**
	 * Builds a 1-dimensional array of in by putting/repeating in it {@code length} occurrences of {@code value}.
	 * 
	 * @param value
	 *            the value to be repeated
	 * @param length
	 *            the number of times the value must be repeated
	 * @return a 1-dimensional array of the specified length only containing the specified value
	 */
	default int[] repeat(int value, int length) {
		return IntStream.generate(() -> value).limit(length).toArray();
	}

	/**
	 * Returns a 2-dimensional array obtained from the specified 1-dimensional array after replacing each value with an array of length 1 only
	 * containing this value. For example, dubbing {@code [2,3,1]} yields {@code [[2],[3],[1]]}.
	 * 
	 * @param values
	 *            a 1 dimensional array of integers
	 * @return a 2-dimensional array of integers by replacing each value of the specified array into an array simply containing this value
	 */
	default int[][] dub(int[] values) {
		return Arrays.stream(values).mapToObj(v -> new int[] { v }).toArray(int[][]::new);
	}

	/**
	 * Returns a 2-dimensional array obtained from the specified 1-dimensional array after replacing each value with an array of length 1 only
	 * containing this value. For example, dubbing {@code ["red","green","blue"]} yields {@code [["red"],["green"],["blue"]]}.
	 * 
	 * @param values
	 *            a 1 -dimensional n array of strings
	 * @return a 2-dimensional array of strings by replacing each value of the specified array into an array simply containing this value
	 */
	default String[][] dub(String[] values) {
		return Arrays.stream(values).map(s -> new String[] { s }).toArray(String[][]::new);
	}

	/**
	 * Returns the transpose of the specified 2-dimensional array.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @return the transpose of the specified 2-dimensional array
	 */
	default int[][] transpose(int[]... m) {
		control(Utilities.isRegular(m), "The specified array must be regular");
		return IntStream.range(0, m[0].length).mapToObj(i -> IntStream.range(0, m.length).map(j -> m[j][i]).toArray()).toArray(int[][]::new);
	}

	@Deprecated
	/**
	 * Use {@code singleValuesIn()} instead. This method will be discarded in Version 1.2.
	 */
	default int[] distinctSorted(int... t) {
		return singleValuesIn(t); // IntStream.of(t).sorted().distinct().toArray();
	}

	@Deprecated
	/**
	 * Use {@code singleValuesIn()} instead. This method will be discarded in Version 1.2.
	 */
	default int[] distinctSorted(int[][] m) {
		return singleValuesIn((Object) m); // Stream.of(m).map(t -> Arrays.stream(t)).flatMapToInt(i -> i).distinct().sorted().toArray();
	}

	/**
	 * Builds an array containing all tuples from the Cartesian product defined from the specified numbers of values. Each tuple will contain a value
	 * at position {@code i} in the range 0 to {@code nValues[i].length-1}.
	 * 
	 * @param nValues
	 *            indicates how many values are possible at each position
	 * @return an array containing all tuples from the Cartesian product defined from the specified number of values
	 */
	default int[][] allCartesian(int[] nValues) {
		return new EnumerationCartesian(nValues).toArray();
	}

	/**
	 * Builds an array containing the tuples from the Cartesian product (defined from the specified numbers of values) that respect the specified
	 * predicate. Each tuple will contain a value at position {@code i} in the range 0 to {@code nValues[i].length-1}.
	 * 
	 * @param nValues
	 *            indicates how many values are possible at each position
	 * @param p
	 *            a predicate used to select tuples
	 * @return an array containing the tuples from the Cartesian product (defined from the specified number of values) that respect the specified
	 *         predicate
	 */
	default int[][] allCartesian(int[] nValues, Predicate<int[]> p) {
		return new EnumerationCartesian(nValues).toArray(p);
	}

	/**
	 * Builds an array containing all tuples from the Cartesian product defined from the specified number of values. Each tuple has the specified
	 * length, and all values are taken in the range 0 to {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form tuples
	 * @param tupleLength
	 *            the length of each tuple
	 * @return an array containing all tuples from the Cartesian product defined from the specified number of values and length
	 */
	default int[][] allCartesian(int nValues, int tupleLength) {
		return new EnumerationCartesian(nValues, tupleLength).toArray();
	}

	/**
	 * Builds an array containing the tuples from the Cartesian product (defined from the specified numbers of values and length) that respect the
	 * specified predicate. Each tuple has the specified length, and all values are taken in the range 0 to {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form tuples
	 * @param tupleLength
	 *            the length of each tuple
	 * @param p
	 *            a predicate used to select tuples
	 * @return an array containing the tuples from the Cartesian product (defined from the specified number of values and length) that respect the
	 *         specified predicate
	 */
	default int[][] allCartesian(int nValues, int tupleLength, Predicate<int[]> p) {
		return new EnumerationCartesian(nValues, tupleLength).toArray(p);
	}

	/**
	 * Builds an array containing all combinations that can be obtained from the specified number of values.
	 * 
	 * @param nValues
	 *            the number of possible different values at each position of the tuples. These numbers must be in an increasing order (and are
	 *            usually all equal)
	 * @return an array containing all combinations obtained from the specified number of values
	 */
	default int[][] allCombinations(int[] nValues) {
		return new EnumerationOfPermutations(nValues).toArray();
	}

	/**
	 * Builds an array containing all combinations that can be obtained from the specified number of values. Each tuple (combination) has the
	 * specified length, and all values are taken in the range 0 to {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form combinations
	 * @param tupleLength
	 *            the length of each combination
	 * @return an array containing all combinations obtained from the specified number of values and length
	 */
	default int[][] allCombinations(int nValues, int tupleLength) {
		return new EnumerationOfCombinations(nValues, tupleLength).toArray();
	}

	/**
	 * Builds an array containing all permutations that can be obtained from the specified number of values. Each tuple will contain a value at
	 * position {@code i} in the range 0 to {@code nValues[i].length-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form permutations
	 * @return an array containing all permutations obtained from the specified number of values
	 */
	default int[][] allPermutations(int[] nValues) {
		return new EnumerationOfPermutations(nValues).toArray();
	}

	/**
	 * Builds an array containing all permutations that can be obtained from the specified number of values. All values are taken in the range 0 to
	 * {@code nValues-1}.
	 * 
	 * @param nValues
	 *            the number of values used to form permutations
	 * @return an array containing all permutations that can be obtained from the specified number of values
	 */
	default int[][] allPermutations(int nValues) {
		return new EnumerationOfPermutations(nValues).toArray();
	}

	/**
	 * Inserts the specified value in the specified array at the specified index. The new array is returned.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param value
	 *            an integer to be inserted
	 * @param index
	 *            the index at which the value must be inserted
	 * @return an array obtained after the insertion of the specified value in the specified array at the specified index
	 */
	default int[] addInt(int[] t, int value, int index) {
		control(t != null, "The first parameter must be different from null");
		control(0 <= index && index <= t.length, "The specified index is not valid");
		return IntStream.range(0, t.length + 1).map(i -> i < index ? t[i] : i == index ? value : t[i - 1]).toArray();
	}

	/**
	 * Appends the specified value to the specified array. The new array is returned.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param value
	 *            an integer to be inserted
	 * @return an array obtained after appending the specified value to the specified array
	 */
	default int[] addInt(int[] t, int value) {
		control(t != null, "The first parameter must be different from null");
		return addInt(t, value, t.length);
	}

	/**
	 * Returns {@code true} iff the specified value is contained in the specified array
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param v
	 *            an integer
	 * @return {@code true} iff the specified value is contained in the specified array
	 */
	default boolean contains(int[] t, int v) {
		return IntStream.of(t).anyMatch(w -> w == v);
	}

	/**
	 * Returns the sum of the integers in the specified array.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return the sum of the integers in the specified array
	 */
	default int sumOf(int[] t) {
		return IntStream.of(t).sum();
	}

	/**
	 * Returns the sum of the integers in the specified range.
	 * 
	 * @param r
	 *            a range
	 * @return the sum of the integers in the specified range
	 */
	default int sumOf(Range r) {
		return r.stream().sum();
	}

	/**
	 * Returns the minimum value in the specified array.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return the minimum value in the specified array
	 */
	default int minOf(int[] t) {
		return IntStream.of(t).min().getAsInt();
	}

	/**
	 * Returns the maximum value in the specified array.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return the maximum value in the specified array
	 */
	default int maxOf(int[] t) {
		return IntStream.of(t).max().getAsInt();
	}

	/**
	 * Returns the first value in the specified range that satisfies the specified predicate.
	 * 
	 * @param r
	 *            a range
	 * @param p
	 *            a predicate on integers
	 * @return the first value in the specified range that satisfies the specified predicate
	 */
	default int firstFrom(Range r, Intx1Predicate p) {
		return r.stream().filter(i -> p.test(i)).findFirst().getAsInt();
	}

	/**
	 * Returns the first value in the specified range that satisfies the specified predicate, if one is found. Otherwise teh specified default value
	 * is returned.
	 * 
	 * @param r
	 *            a range
	 * @param p
	 *            a predicate on integers
	 * @param defaultValue
	 *            an integer
	 * @return the first value in the specified range that satisfies the specified predicate, if any, or the specified default value otherwise
	 */
	default int firstFrom(Range r, Intx1Predicate p, int defaultValue) {
		return r.stream().filter(i -> p.test(i)).findFirst().orElse(defaultValue);
	}

}
