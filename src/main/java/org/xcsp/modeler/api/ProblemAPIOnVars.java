package org.xcsp.modeler.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
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

public interface ProblemAPIOnVars extends ProblemAPIBase {

	// ************************************************************************
	// ***** Methods vars()
	// ************************************************************************

	// COMMENT : I was unable to keep only one generic method
	// because it makes calls ambiguous due to type erasure (the bounded return type is not sufficient)

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. All variables encountered in the parameters,
	 * extracting them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a
	 * 1-dimensional array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param first
	 *            a first object that may involve one or several variables (possibly in arrays, collections and streams)
	 * @param others
	 *            other objects that may involve one or several variables (possibly in arrays, collections and streams)
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, Object... others) {
		return imp().vars(IntStream.range(0, others.length + 1).mapToObj(i -> i == 0 ? first : others[i - 1]));
	}

	/**
	 * Returns a 1-dimensional array containing the specified variables.
	 * 
	 * @param x
	 *            a first variable
	 * @param others
	 *            a sequence of other variables
	 * @return a 1-dimensional array containing the specified variables
	 */
	default <T extends IVar> T[] vars(T x, T... others) {
		return vars((Object) x, others);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. All variables are collected in order, and {@code null} values
	 * are simply discarded.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(T[][] vars) {
		return vars((Object) vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. All variables are collected in order, and {@code null} values
	 * are simply discarded.
	 * 
	 * @param vars
	 *            a 3-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(T[][][] vars) {
		return vars((Object) vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. All variables encountered in the first parameter,
	 * extracting them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a
	 * 1-dimensional array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays, collections and streams)
	 * @param x
	 *            a variable
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T x) {
		return vars(first, (Object) x);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. All variables encountered in the parameters, extracting
	 * them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a 1-dimensional
	 * array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays, collections and streams)
	 * @param x
	 *            a 1-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T[] x) {
		return vars(first, (Object) x);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. All variables encountered in the parameters, extracting
	 * them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a 1-dimensional
	 * array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays, collections and streams)
	 * @param x
	 *            a 2-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T[][] x) {
		return vars(first, (Object) x);
	}

	/**
	 * Returns a 1-dimensional array of variables by collecting them in order from the specified stream.
	 * 
	 * @param stream
	 *            a stream of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Stream<T> stream) {
		return vars((Object) stream);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified 1-dimensional array of variables, by discarding {@code null} values.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @return a 1-dimensional array of variables after discarding all occurrences of {@code null}
	 */
	default <T extends IVar> T[] clean(T[] vars) {
		return imp().clean(vars);
	}

	// ************************************************************************
	// ***** Methods variablesIn() and variablesFrom()
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. All variables encountered in the parameters,
	 * extracting them from arrays (of any dimension), collections and streams, are recursively collected in order, and concatenated to form a
	 * 1-dimensional array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param object
	 *            a first object that may involve one or several variables (possibly in arrays, collections and streams)
	 * @param otherObjects
	 *            other objects that may involve one or several variables (possibly in arrays, collections and streams)
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] variablesIn(Object object, Object... otherObjects) {
		return vars(object, otherObjects);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified stream. Each object of the stream is mapped to another object by the
	 * specified function. Then, all variables are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param stream
	 *            a stream of objects
	 * @param f
	 *            a function mapping objects of the stream into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] variablesFrom(Stream<U> stream, Function<U, Object> f) {
		return variablesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified stream. Each integer of the stream is mapped to another object by the
	 * specified function. Then, all variables are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param stream
	 *            a stream of integers
	 * @param f
	 *            a function mapping integers of the stream into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] variablesFrom(IntStream stream, Function<Integer, Object> f) {
		return variablesFrom(stream.boxed(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. Each object of the array is mapped to another object by the
	 * specified function. Then, all variables are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param t
	 *            an array of objects
	 * @param f
	 *            a function mapping objects of the array into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] variablesFrom(U[] t, Function<U, Object> f) {
		return variablesFrom(Stream.of(t), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified collection. Each object of the collection is mapped to another object
	 * by the specified function. Then, all variables are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param c
	 *            a collection of objects
	 * @param f
	 *            a function mapping objects of the collection into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] variablesFrom(Collection<U> c, Function<U, Object> f) {
		return variablesFrom(c.stream(), f);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. Each integer of the array is mapped to another object by the
	 * specified function. Then, all variables are collected and concatenated to form a 1-dimensional array. {@code null} values are discarded.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param f
	 *            a function mapping integers of the array into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar> T[] variablesFrom(int[] t, Function<Integer, Object> f) {
		return variablesFrom(IntStream.of(t).boxed(), f);
	}

	/**
	 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
	 * values in this range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no variable is obtained,
	 * {@code null} is returned.
	 * 
	 * @param r
	 *            a range of integers
	 * @param f
	 *            a function to convert integer values into objects (typically, variables, but can also be structures containing variables)
	 * @return a non-empty 1-dimensional array of variables or {@code null}
	 */
	default <T extends IVar> T[] variablesFrom(Range r, Function<Integer, Object> f) {
		return variablesFrom(r.stream(), f);

	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. All variables encountered in the parameters,
	 * extracting them from arrays (of any dimension), collections and streams, are recursively collected in order, sorted, made distinct, so as to
	 * form a 1-dimensional array. Note that {@code null} values, as well as any simple object not implementing {@code IVar}, are simply discarded.
	 * 
	 * @param objects
	 *            a sequence of objects that may involve one or several variables (possibly in arrays, collections and streams)
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] singleVariablesIn(Object... objects) {
		return vars(Stream.of((Object[]) variablesIn(objects)).sorted().distinct());
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified stream. Each object of the stream is mapped to another object by the
	 * specified function. Then, all variables are collected, sorted and made distinct so as to form a 1-dimensional array. {@code null} values are
	 * discarded.
	 * 
	 * @param stream
	 *            a stream of objects
	 * @param f
	 *            a function mapping objects of the stream into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] singleVariablesFrom(Stream<U> stream, Function<U, Object> f) {
		return singleVariablesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified collection. Each object of the collection is mapped to another object
	 * by the specified function. Then, all variables are collected, sorted and made distinct so as to form a 1-dimensional array. {@code null} values
	 * are discarded.
	 * 
	 * @param c
	 *            a collection of objects
	 * @param f
	 *            a function mapping objects of the stream into other objects
	 * @return a 1-dimensional array formed of collected variables (occurrences of {@code null} being discarded}
	 */
	default <T extends IVar, U> T[] singleVariablesFrom(Collection<U> c, Function<U, Object> f) {
		return singleVariablesFrom(c.stream(), f);
	}

	// default Var[] variablesFrom(Rangesx2 r2, BiFunction<Integer, Integer, Object> f) {
	// List<Object> list = new ArrayList<>();
	// for (int i : r2.items[0])
	// for (int j : r2.items[1]) {
	// Object t = f.apply(i, j);
	// if (t != null)
	// list.add(t);
	// }
	// return variablesIn(list);
	// }

	// ************************************************************************
	// ***** Selecting Objects from Arrays, to form new arrays.
	// ***** Most of the time, these methods are used for building arrays of variables
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
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code i} that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
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
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code (i,j)} that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
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
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code (i,j,k)} that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
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
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code (i,j,k,l)} that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
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
	 * Builds and returns a 1-dimensional array of objects (e.g., variables), obtained by selecting from the specified array any object at an index
	 * {@code (i,j,k,l,m)} that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
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
	default <T> T[] select(T[] vars, Range range) {
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
	default <T> T[] select(T[][][][] vars, Rangesx4 rangesx4) {
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
	default <T> T[] columnOf(T[][] vars, int idColumn) {
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
	default <T> T[] diagonalDown(T[][] vars) {
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

	/**
	 * Returns the transpose of the specified 2-dimensional array of objects (e.g., variables).
	 * 
	 * @param vars
	 *            a 2-dimensional array of objects
	 * @return the transpose of the specified 2-dimensional array of objects
	 */
	default <T> T[][] transpose(T[]... vars) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[][] t = Utilities.buildArray(Utilities.firstNonNull(vars).getClass(), vars[0].length, vars.length);
		IntStream.range(0, t.length).forEach(i -> IntStream.range(0, t[0].length).forEach(j -> t[i][j] = vars[j][i]));
		return t;
	}

	/**
	 * Returns a 2-dimensional array of objects (e.g., variables) obtained from the specified 3-dimensional array of objects by eliminating the second
	 * dimension after fixing it to the {@code idx} argument. The array {@code t} returned by this function is such that
	 * {@code t[i][j]=vars[i][idx][j]}.
	 * 
	 * @param vars
	 *            a 3-dimensional array of objects
	 * @param idx
	 *            the index that is fixed for the second dimension
	 * @return a 2-dimensional array of objects corresponding to the elimination of the second dimension by fixing it to the specified index
	 */
	default <T> T[][] eliminateDim2(T[][][] vars, int idx) {
		control(Utilities.isRegular(vars), "The specified array must be regular");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[][] m = Utilities.buildArray(vars[0][0][0].getClass(), vars.length, vars[0][0].length);
		IntStream.range(0, m.length).forEach(i -> IntStream.range(0, m[0].length).forEach(j -> m[i][j] = vars[i][idx][j]));
		return m;
	}

	/**
	 * Returns a 2-dimensional array of objects (e.g., variables) obtained from the specified 3-dimensional array of objects by eliminating the third
	 * dimension after fixing it to the {@code idx} argument. The array {@code t} returned by this function is such that
	 * {@code t[i][j]=vars[i][j][idx]}.
	 * 
	 * @param vars
	 *            a 3-dimensional array of objects
	 * @param idx
	 *            the index that is fixed for the third dimension
	 * @return a 2-dimensional array of objects corresponding to the elimination of the third dimension by fixing it to the specified index
	 */
	default <T> T[][] eliminateDim3(T[][][] vars, int idx) {
		control(Utilities.isRegular(vars), "The specified array must be regular");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		T[][] m = Utilities.buildArray(vars[0][0][0].getClass(), vars.length, vars[0].length);
		IntStream.range(0, m.length).forEach(i -> IntStream.range(0, m[0].length).forEach(j -> m[i][j] = vars[i][j][idx]));
		return m;
	}

	/**
	 * Inserts the specified object in the specified array at the specified index. The new array is returned.
	 * 
	 * @param t
	 *            a 1-dimensional array
	 * @param object
	 *            an object to be inserted
	 * @param index
	 *            the index at which the object must be inserted
	 * @return an array obtained after the insertion of the specified object in the specified array at the specified index
	 */
	default <T> T[] addObject(T[] t, T object, int index) {
		control(t != null && object != null, "The two first parameters must be different from null");
		control(0 <= index && index <= t.length, "The specified index is not valid");
		T[] tt = Utilities.buildArray(object.getClass(), t.length + 1);
		for (int i = 0; i < tt.length; i++)
			tt[i] = i < index ? t[i] : i == index ? object : t[i - 1];
		return tt;
	}

	/**
	 * Appends the specified object to the specified array. The new array is returned.
	 * 
	 * @param t
	 *            a 1-dimensional array
	 * @param object
	 *            an object to be inserted
	 * @return an array obtained after appending the specified object to the specified array
	 */
	default <T> T[] addObject(T[] t, T object) {
		control(t != null && object != null, "The two first parameters must be different from null");
		return addObject(t, object, t.length);
	}

	/**
	 * Returns {@code true} iff the specified object is contained in the specified array
	 * 
	 * @param t
	 *            a 1-dimensional array of objects
	 * @param object
	 *            an object
	 * @return {@code true} iff the specified object is contained in the specified array
	 */
	default boolean contains(Object[] t, Object object) {
		control(t != null && object != null, "The two first parameters must be different from null");
		return Stream.of(t).anyMatch(o -> o == object);
	}

	/**
	 * Returns the first object in the specified array that satisfies the specified predicate, if any, or {@code null}.
	 * 
	 * @param t
	 *            a 1-dimensional array of objects
	 * @param p
	 *            a predicate
	 * @return the first object in the specified array that satisfies the specified predicate, if any, or {@code null}
	 */
	default <T> T firstFrom(T[] t, Predicate<T> p) {
		return t == null ? null : Stream.of(t).filter(v -> p.test(v)).findFirst().orElse(null);
	}

}
