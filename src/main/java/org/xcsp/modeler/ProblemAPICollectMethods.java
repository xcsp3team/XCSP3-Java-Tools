package org.xcsp.modeler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Utilities;

public interface ProblemAPICollectMethods extends ProblemAPIBase {

	// ************************************************************************
	// ***** Methods vars()
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. Each element of the sequence must only contain
	 * objects (and possibly {@code null} values), either stand-alone or present in arrays (of any dimension) or streams. All variables are collected
	 * in order, and concatenated to form a 1-dimensional array. Note that {@code null} values are simply discarded.
	 * 
	 * @param first
	 *            a first object that may involve one or several variables (possibly in arrays)
	 * @param others
	 *            other objects that may involve one or several variables (possibly in arrays)
	 * @return a 1-dimensional array of variables.
	 */
	default <T extends IVar> T[] vars(Object first, Object... others) {
		return imp().vars(first, others);
	}

	/**
	 * Returns a 1-dimensional array of variables by collecting them in order from the specified stream.
	 * 
	 * @param stream
	 *            a stream of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Stream<T> stream) {
		return imp().vars(stream);
	}

	/**
	 * Returns a 1-dimensional array only containing the specified variable.
	 * 
	 * @param x
	 *            a variable
	 * @return a 1-dimensional array containing one variable
	 */
	default <T extends IVar> T[] vars(T x) {
		return imp().vars(x);
	}

	/**
	 * Returns a 1-dimensional array containing the two specified variables.
	 * 
	 * @param x
	 *            a first variable
	 * @param y
	 *            a second variable
	 * @return a 1-dimensional array containing two variables
	 */
	default <T extends IVar> T[] vars(T x, T y) {
		return imp().vars(x, y);
	}

	/**
	 * Returns a 1-dimensional array containing the three specified variables.
	 * 
	 * @param x
	 *            a first variable
	 * @param y
	 *            a second variable
	 * @param z
	 *            a third variable
	 * @return a 1-dimensional array containing three variables
	 */
	default <T extends IVar> T[] vars(T x, T y, T z) {
		return imp().vars(x, y, z);
	}

	/**
	 * Returns a 1-dimensional array containing the specified variables.
	 * 
	 * @param x
	 *            a variable
	 * @param y
	 *            a second variable
	 * @param z
	 *            a third variable
	 * @param otherVars
	 *            a sequence of other variables
	 * @return a 1-dimensional array containing the specified variables
	 */
	default <T extends IVar> T[] vars(T x, T y, T z, T... otherVars) {
		return imp().vars(x, y, z, otherVars);
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
		return imp().vars(vars);
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
		return imp().vars(vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. All variables are collected in order, and {@code null} values
	 * are simply discarded.
	 * 
	 * @param vars
	 *            a 4-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(T[][][][] vars) {
		return imp().vars(vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified array. All variables are collected in order, and {@code null} values
	 * are simply discarded.
	 * 
	 * @param vars
	 *            a 5-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(T[][][][][] vars) {
		return imp().vars(vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. The first parameter must only contain variables (and
	 * possibly {@code null} values), either stand-alone or present in arrays (of any dimension). All variables are collected in order, and
	 * concatenated to form a 1-dimensional array. Note that {@code null} values are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays)
	 * @param x
	 *            a variable
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T x) {
		return imp().vars(first, x);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. The first parameter must only contain variables (and
	 * possibly {@code null} values), either stand-alone or present in arrays (of any dimension). All variables are collected in order, and
	 * concatenated to form a 1-dimensional array. Note that {@code null} values are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays)
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T[] vars) {
		return imp().vars(first, vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified parameters. The first parameter must only contain variables (and
	 * possibly {@code null} values), either stand-alone or present in arrays (of any dimension). All variables are collected in order, and
	 * concatenated to form a 1-dimensional array. Note that {@code null} values are simply discarded.
	 * 
	 * @param first
	 *            an object that may involve one or several variables (possibly in arrays)
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @return a 1-dimensional array of variables
	 */
	default <T extends IVar> T[] vars(Object first, T[][] vars) {
		return imp().vars(first, vars);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified 1-dimensional array of variables, by discarding {@code null} values.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @return a 1-dimensional array of variables with no occurrence of {@code null}
	 */
	default <T extends IVar> T[] clean(T[] vars) {
		return imp().clean(vars);
	}

	// ************************************************************************
	// ***** Methods variablesIn() and variablesFrom()
	// ************************************************************************

	default <T extends IVar> T[] variablesIn(Object object) {
		return vars(object);
	}

	default <T extends IVar> T[] variablesIn(Object object, Object... otherObjects) {
		return vars(object, otherObjects);
	}

	default <T extends IVar, U> T[] variablesFrom(Stream<U> stream, Function<U, Object> f) {
		return variablesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	default <T extends IVar, U> T[] variablesFrom(IntStream stream, Function<Integer, Object> f) {
		return variablesFrom(stream.boxed(), f);
	}

	default <T extends IVar, U> T[] variablesFrom(U[] t, Function<U, Object> f) {
		return variablesFrom(Stream.of(t), f);
	}

	default <T extends IVar, U> T[] variablesFrom(Collection<U> c, Function<U, Object> f) {
		return variablesFrom(c.stream(), f);
	}

	default <T extends IVar> T[] variablesFrom(int[] t, Function<Integer, Object> f) {
		return variablesFrom(IntStream.of(t).boxed(), f);
	}

	default Var[] variablesFrom(Rangesx2 r2, BiFunction<Integer, Integer, Object> f) {
		List<Object> list = new ArrayList<>();
		for (int i : r2.items[0])
			for (int j : r2.items[1]) {
				Object t = f.apply(i, j);
				if (t != null)
					list.add(t);
			}
		return variablesIn(list);
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
	default Var[] variablesFrom(Range r, Function<Integer, Object> f) {
		return variablesFrom(r.stream(), f);
	}

	// ************************************************************************
	// ***** Methods valuesIn() and valuesFrom()
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified sequence of parameters. Each element of the sequence can be an
	 * {@code Integer}, a {@code Range}, an array, a Stream (or IntStream), a collection, etc. All integers are collected and concatenated to form a
	 * 1-dimensional array.
	 * 
	 * @param objects
	 *            a sequence of objects, each being an {@code Integer}, a {@code Range} or a k-dimensional array of {@code int}
	 * @return a 1-dimensional array formed of collected integers
	 */
	default int[] vals(Object... objects) {
		return Utilities.collectInt(objects);
	}

	default int[] valuesIn(Object object) {
		return vals(object);
	}

	default int[] valuesIn(Object object, Object... otherObjects) {
		return vals(object, otherObjects);
	}

	default <T> int[] valuesFrom(Stream<T> stream, Function<T, Object> f) {
		return valuesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	default int[] valuesFrom(IntStream stream, Function<Integer, Object> f) {
		return valuesFrom(stream.boxed(), f);
	}

	default <T> int[] valuesFrom(T[] t, Function<T, Object> f) {
		return valuesFrom(Stream.of(t), f);
	}

	default <T> int[] valuesFrom(Collection<T> c, Function<T, Object> f) {
		return valuesFrom(c.stream(), f);
	}

	default int[] valuesFrom(int[] t, Function<Integer, Object> f) {
		return valuesFrom(IntStream.of(t).boxed(), f);
	}

	default int[] valuesFrom(Range r, Function<Integer, Object> f) {
		return valuesFrom(r.stream(), f);
	}

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

	default int[] singleValuesIn(Object... objects) {
		return IntStream.of(Utilities.collectInt(objects)).sorted().distinct().toArray();
	}

	default <T> int[] singleValuesFrom(Stream<T> stream, Function<T, Object> f) {
		return singleValuesIn(stream.filter(o -> o != null).map(o -> f.apply(o)));
	}

	default int[] singleValuesFrom(IntStream stream, Function<Integer, Object> f) {
		return singleValuesFrom(stream.boxed(), f);
	}

	default <T> int[] singleValuesFrom(T[] t, Function<T, Object> f) {
		return singleValuesFrom(Stream.of(t), f);
	}

	default <T> int[] singleValuesFrom(Collection<T> c, Function<T, Object> f) {
		return singleValuesFrom(c.stream(), f);
	}

	default int[] singleValuesFrom(int[] t, Function<Integer, Object> f) {
		return singleValuesFrom(IntStream.of(t).boxed(), f);
	}

	default int[] singleValuesFrom(Range r, Function<Integer, Object> f) {
		return singleValuesFrom(r.stream(), f);
	}

}
