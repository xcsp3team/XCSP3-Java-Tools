package org.xcsp.common;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import org.xcsp.common.FunctionalInterfaces.Intx1Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx2Function;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx3Function;
import org.xcsp.common.FunctionalInterfaces.Intx4Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx4Function;
import org.xcsp.common.FunctionalInterfaces.Intx5Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx5Function;
import org.xcsp.modeler.entities.CtrEntities.CtrArray;
import org.xcsp.modeler.implementation.ProblemIMP;

/**
 * This class includes all functionalities that are necessary to deal with ranges of integers. Inner classes represent double, triple, quadruple and
 * quintuple forms of ranges, used to represent Cartesian products.
 * 
 * @author Christophe Lecoutre
 *
 */
public class Range implements Iterable<Integer> {

	/**
	 * The lower bound (inclusive) of this range.
	 */
	public final int minIncluded;

	/**
	 * The upper bound (inclusive) of this range.
	 */
	public final int maxIncluded;

	/**
	 * The step of this range (difference between each two successive numbers in this range).
	 */
	public final int step;

	/**
	 * Constructs an object {@code Range} from the specified bounds and step.
	 * 
	 * @param minIncluded
	 *            the lower bound (inclusive) of this range
	 * @param maxIncluded
	 *            the upper bound (inclusive) of this range
	 * @param step
	 *            the step of this range
	 */
	public Range(int minIncluded, int maxIncluded, int step) {
		this.minIncluded = minIncluded;
		this.maxIncluded = maxIncluded;
		this.step = step;
		Utilities.control(step > 0 && minIncluded <= maxIncluded, "Bad values of parameters");
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds (using implicitly a step equal to 1).
	 * 
	 * @param minIncluded
	 *            the lower bound (inclusive) of this range
	 * @param maxIncluded
	 *            the upper bound (inclusive) of this range
	 */
	public Range(int minIncluded, int maxIncluded) {
		this(minIncluded, maxIncluded, 1);
	}

	/**
	 * Constructs an object {@code Range} from the specified length (using implicitly a lower bound equal to 0 and a step equal to 1).
	 * 
	 * @param length
	 *            the length of this range
	 */
	public Range(int length) {
		this(0, length - 1, 1);
	}

	/**
	 * Returns a double range obtained by combining this range with a range built from the specified bounds and step.
	 * 
	 * @param minIncluded
	 *            the lower bound (inclusive) of the second range to be built
	 * @param maxIncluded
	 *            the upper bound (inclusive) of the second range to be built
	 * @param step
	 *            the step of the second range to be built
	 * @return a double range obtained by combining this range with a range built from the specified bounds and step
	 */
	public Rangesx2 range(int minIncluded, int maxIncluded, int step) {
		return new Rangesx2(this, new Range(minIncluded, maxIncluded, step));
	}

	/**
	 * Returns a double range obtained by combining this range with a range built from the specified bounds (using implicitly a step equal to 1).
	 * 
	 * @param minIncluded
	 *            the lower bound (inclusive) of the second range to be built
	 * @param maxIncluded
	 *            the upper bound (inclusive) of the second range to be built
	 * @return a double range obtained by combining this range with a range built from the specified bounds
	 */
	public Rangesx2 range(int minIncluded, int maxIncluded) {
		return new Rangesx2(this, new Range(minIncluded, maxIncluded));
	}

	/**
	 * Returns a double range obtained by combining this range with a range built from the specified length (using implicitly a lower bound equal to 0
	 * and a step equal to 1).
	 * 
	 * @param length
	 *            the length of the second range
	 * @return a double range by combining this range with a range built from the specified length
	 */
	public Rangesx2 range(int length) {
		return new Rangesx2(this, new Range(length));
	}

	/**
	 * Returns {@code true} iff this range is basic, i.e., starts at 0 and admits a step equal to 1
	 * 
	 * @return {@code true} iff this range is basic,
	 */
	public boolean isBasic() {
		return minIncluded == 0 && step == 1;
	}

	/**
	 * Returns {@code true} iff this range contains the specified value
	 * 
	 * @param i
	 *            an integer
	 * @return {@code true} iff this range contains the specified value
	 */
	public boolean contains(int i) {
		return minIncluded <= i && i <= maxIncluded && ((i - minIncluded) % step == 0);
	}

	/**
	 * Returns the length (number of integers) in this range.
	 * 
	 * @return the length (number of integers) in this range
	 */
	public int length() {
		return (maxIncluded - minIncluded + 1) / step;
	}

	/**
	 * Converts this range into a stream.
	 * 
	 * @return the stream corresponding to this range
	 */
	public IntStream stream() {
		return IntStream.of(toArray());
	}

	/** Start Experimental **/

	private ProblemIMP imp;

	public Range setImp(ProblemIMP imp) {
		this.imp = imp;
		return this;
	}

	public CtrArray forall(IntConsumer c) {
		return imp.manageLoop(() -> this.execute(c));
	}

	/** End Experimental **/

	/**
	 * Builds and returns a 1-dimensional array of integers, obtained by selecting from this range any integer that satisfies the specified predicate.
	 * 
	 * @param p
	 *            a predicate allowing us to test if a value in this range must be selected
	 * @return a 1-dimensional array of integers (possibly, of length 0)
	 */
	public int[] select(Intx1Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i : this)
			if (p.test(i))
				list.add(i);
		return list.stream().mapToInt(i -> i).toArray();
	}

	private <T> List<T> provideObjects(IntFunction<T> op, List<T> list) {
		for (int i : this) {
			T x = op.apply(i);
			if (x != null)
				list.add(x);
		}
		return list;
	}

	/**
	 * Returns a 1-dimensional array of objects (from class T), obtained after collecting the objects returned by the specified function when executed
	 * on all values in this range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no variable is
	 * obtained, {@code null} is returned.
	 * 
	 * @param f
	 *            a function to convert integer values into objects of class T
	 * @return a non-empty 1-dimensional array of objects or {@code null}
	 */
	public <T> T[] provideObjects(IntFunction<T> f) {
		return Utilities.convert(provideObjects(f, new ArrayList<>()));
	}

	/**
	 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
	 * values in this range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no variable is obtained,
	 * {@code null} is returned.
	 * 
	 * @param f
	 *            a function to convert integer values into variables
	 * @return a non-empty 1-dimensional array of variables or {@code null}
	 */
	public <T extends IVar> T[] provideVars(IntFunction<T> f) {
		return Utilities.convert(provideObjects(f, new ArrayList<>()));
	}

	/**
	 * Returns a 1-dimensional array of integers, obtained after collecting the integers returned by the specified function when executed on all
	 * values in this range. Note that {@code null} values are simply discarded, if ever generated.
	 * 
	 * @param f
	 *            a function to convert integer values into integer values
	 * @return a 1-dimensional array of integers (possibly, of length 0)
	 */
	public int[] provideVals(IntFunction<Integer> f) {
		List<Integer> list = new ArrayList<>();
		for (int i : this) {
			Integer v = f.apply(i);
			if (v != null)
				list.add(v);
		}
		return list.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Returns a 2-dimensional array of integers, obtained after collecting the tuples (1-dimensional of integers) returned by the specified function
	 * when executed on all values in this range. Note that {@code null} values are simply discarded, if ever generated.
	 * 
	 * @param f
	 *            a function to convert integer values into tuples of integer values
	 * @return a 2-dimensional array of integers (possibly, of length 0)
	 */
	public int[][] provideTuples(IntFunction<int[]> f) {
		List<int[]> list = new ArrayList<>();
		for (int i : this) {
			int[] v = f.apply(i);
			if (v != null)
				list.add(v);
		}
		return list.stream().toArray(int[][]::new);
	}

	/**
	 * Returns a 1-dimensional array of integers, obtained after mapping every integer in this range in a value given by the specified unary operator.
	 * 
	 * @param op
	 *            a unary operator that converts an {@code int} into another {@code int}
	 * @return a 1-dimensional array of integers
	 */
	public int[] map(IntUnaryOperator op) {
		// return IntStream.iterate(minIncluded, n -> n + step).takeWhile(n -> n <= maxIncluded).toArray(); // WAIT FOR JDK9
		List<Integer> list = new ArrayList<>();
		for (int i : this)
			list.add(op.applyAsInt(i));
		return list.stream().mapToInt(i -> i).toArray();
	}

	/**
	 * Returns a 1-dimensional array containing all integers in this range.
	 * 
	 * @return a 1-dimensional array containing all integers in this range
	 */
	public int[] toArray() {
		return map(i -> i);
	}

	/**
	 * Executes the specified consumer on each integer value contained in this range.
	 * 
	 * @param c
	 *            an integer value consumer
	 */
	public void execute(IntConsumer c) {
		for (int i : this)
			c.accept(i);
	}

	/**
	 * Returns an iterator over all integers in this range.
	 */
	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int cursor = minIncluded;

			@Override
			public boolean hasNext() {
				return cursor <= maxIncluded;
			}

			@Override
			public Integer next() {
				assert hasNext();
				int v = cursor;
				cursor += step;
				return v;
			}
		};
	}

	/**
	 * The abstract root class of multiple ranges.
	 */
	private static abstract class Ranges {
		/**
		 * The sequence (array) of simple ranges, that when combined form this multiple range.
		 */
		protected Range[] items;

		protected Ranges(Range[] items) {
			this.items = items;
		}

		/**
		 * Returns {@code true} iff this multiple range contains the specified tuple
		 * 
		 * @param tuple
		 *            a tuple of integer values
		 * @return {@code true} iff this multiple range contains the specified tuple
		 */
		public boolean contains(int... tuple) {
			Utilities.control(tuple.length == items.length, "bad number of indexes");
			return IntStream.range(0, tuple.length).allMatch(i -> items[i].contains(tuple[i]));
		}
	}

	/**
	 * A class denoting a double range.
	 */
	public static class Rangesx2 extends Ranges {

		private Rangesx2(Range range1, Range range2) {
			super(new Range[] { range1, range2 });
		}

		/**
		 * Returns a triple range obtained by combining this double range with a range built from the specified bounds and step.
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the third range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the third range to be built
		 * @param step
		 *            the step of the third range to be built
		 * @return a triple range obtained by combining this double range with a range built from the specified bounds and step
		 */
		public Rangesx3 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx3(items[0], items[1], new Range(minIncluded, maxIncluded, step));
		}

		/**
		 * Returns a triple range obtained by combining this double range with a range built from the specified bounds (using implicitly a step equal
		 * to 1).
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the third range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the third range to be built
		 * @return a triple range obtained by combining this double range with a range built from the specified bounds
		 */
		public Rangesx3 range(int minIncluded, int maxIncluded) {
			return new Rangesx3(items[0], items[1], new Range(minIncluded, maxIncluded));
		}

		/**
		 * Returns a triple range obtained by combining this double range with a range built from the specified length (using implicitly a lower bound
		 * equal to 0 and a step equal to 1).
		 * 
		 * @param length
		 *            the length of the third range
		 * @return a triple range obtained by combining this double range with a range built from the specified length
		 */
		public Rangesx3 range(int length) {
			return new Rangesx3(items[0], items[1], new Range(length));
		}

		/**
		 * Builds and returns a 2-dimensional array of integers, obtained by selecting from this double range any pair that satisfies the specified
		 * predicate.
		 * 
		 * @param p
		 *            a predicate allowing us to test if a pair of values in this double range must be selected
		 * @return a 2-dimensional array of integers (possibly, of length 0)
		 */
		public int[][] select(Intx2Predicate p) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1])
					if (p.test(i, j))
						list.add(new int[] { i, j });
			return list.stream().toArray(int[][]::new);
		}

		private <T> List<T> provideVars(Intx2Function<T> f, List<T> list) {
			for (int i : items[0])
				items[1].provideObjects(j -> f.apply(i, j), list);
			return list;
		}

		/**
		 * Returns a 1-dimensional array of objects, obtained after collecting the objects returned by the specified function when executed on all
		 * pairs of values in this double range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no
		 * variable is obtained, {@code null} is returned.
		 * 
		 * @param f
		 *            a function to convert pairs of integers into objects of class T
		 * @return a non-empty 1-dimensional array of objects or {@code null}
		 */
		public <T> T[] provideObjects(Intx2Function<T> f) {
			return Utilities.convert(provideVars(f, new ArrayList<>()));
		}

		/**
		 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
		 * pairs of values in this double range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no
		 * variable is obtained, {@code null} is returned.
		 * 
		 * @param f
		 *            a function to convert pairs of integers into variables
		 * @return a non-empty 1-dimensional array of variables or {@code null}
		 */
		public <T extends IVar> T[] provideVars(Intx2Function<T> f) {
			return Utilities.convert(provideVars(f, new ArrayList<>()));
		}

		/**
		 * Returns a 1-dimensional array of integers, obtained after collecting the integers returned by the specified function when executed on all
		 * pairs of values in this double range. Note that {@code null} values are simply discarded, if ever generated.
		 * 
		 * @param f
		 *            a function to convert pairs of integer values into integer values
		 * @return a 1-dimensional array of integers (possibly, of length 0)
		 */
		public int[] provideVals(Intx2Function<Integer> f) {
			List<Integer> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1]) {
					Integer t = f.apply(i, j);
					if (t != null)
						list.add(t);
				}
			return list.stream().mapToInt(i -> i).toArray();
		}

		/**
		 * Returns a 2-dimensional array of integers, obtained after collecting the tuples returned by the specified function when executed on all
		 * pairs of values in this double range. Note that {@code null} values are simply discarded, if ever generated.
		 * 
		 * @param f
		 *            a function to convert pairs of integer values into tuples of integer values
		 * @return 2-dimensional array of integers (possibly, of length 0)
		 */
		public int[][] provideTuples(Intx2Function<int[]> f) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1]) {
					int[] t = f.apply(i, j);
					if (t != null)
						list.add(t);
				}
			return list.stream().toArray(int[][]::new);
		}

		/**
		 * Returns a 2-dimensional array of integers, obtained after mapping every pair of values from this double range in a value given by the
		 * specified binary operator. If {@code v1} is the ith value in the first range, and {@code v2} is the jth value in the second range, the
		 * value {@code op(v1,v2)} is put in the 2-dimensional array at index {@code (i,j)}. Do note that the number of rows of the built array is
		 * given by the length of the first range and the number of columns is given by the length of the second range.
		 * 
		 * @param op
		 *            a binary operator that converts a pair of integers into another integer
		 * @return a 2-dimensional array of integers
		 */
		public int[][] map(IntBinaryOperator op) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				list.add(items[1].map(j -> op.applyAsInt(i, j)));
			return list.stream().toArray(int[][]::new);
		}

		/**
		 * Returns a 2-dimensional array of variables, obtained after mapping every pair of integers from this double range in a variable given by the
		 * specified binary function. If {@code v1} is the ith value in the first range, and {@code v2} is the jth value in the second range, the
		 * value {@code f(v1,v2)} is put in the 2-dimensional array of variables at index {@code (i,j)}. Do note that the number of rows of the built
		 * array is given by the length of the first range and the number of columns is given by the length of the second range. Also, note that
		 * {@code null} may be present in some squares of the built array.
		 * 
		 * @param op
		 *            a binary function that converts a pair of integers into a variable (possibly {@code null})
		 * @return a 2-dimensional array of variables
		 */
		public <T extends IVar> T[][] mapToVars(Intx2Function<T> op) {
			List<T> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1])
					list.add(op.apply(i, j));
			T first = list.stream().filter(o -> o != null).findFirst().orElse(null);
			Utilities.control(first != null, "At least one variable should have been generated");
			T[][] t = (T[][]) Array.newInstance(first.getClass(), items[0].length(), items[1].length());
			int m = items[0].length();
			IntStream.range(0, t.length).forEach(i -> IntStream.range(0, m).forEach(j -> t[i][j] = list.get(i * m + j)));
			return t;
		}

		/**
		 * Executes the specified consumer on each pair of values contained in this range.
		 * 
		 * @param c2
		 *            an object consuming pairs of integers.
		 */
		public void execute(Intx2Consumer c2) {
			for (int i : items[0])
				items[1].execute(j -> c2.accept(i, j));
		}
	}

	/**
	 * A class denoting a triple range.
	 */
	public static class Rangesx3 extends Ranges {
		private Rangesx3(Range range1, Range range2, Range range3) {
			super(new Range[] { range1, range2, range3 });
		}

		/**
		 * Returns a quadruple range obtained by combining this triple range with a range built from the specified bounds and step.
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the fourth range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the fourth range to be built
		 * @param step
		 *            the step of the fourth range to be built
		 * @return a quadruple range obtained by combining this triple range with a range built from the specified bounds and step
		 */
		public Rangesx4 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx4(items[0], items[1], items[2], new Range(minIncluded, maxIncluded, step));
		}

		/**
		 * Returns a quadruple range obtained by combining this triple range with a range built from the specified bounds (using implicitly a step
		 * equal to 1).
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the fourth range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the fourth range to be built
		 * @return a quadruple range obtained by combining this triple range with a range built from the specified bounds
		 */
		public Rangesx4 range(int minIncluded, int maxIncluded) {
			return new Rangesx4(items[0], items[1], items[2], new Range(minIncluded, maxIncluded));
		}

		/**
		 * Returns a quadruple range obtained by combining this triple range with a range built from the specified length (using implicitly a lower
		 * bound equal to 0 and a step equal to 1).
		 * 
		 * @param length
		 *            the length of the fourth range
		 * @return a quadruple range obtained by combining this triple range with a range built from the specified length
		 */
		public Rangesx4 range(int length) {
			return new Rangesx4(items[0], items[1], items[2], new Range(length));
		}

		private <T> List<T> provideVars(Intx3Function<T> f, List<T> list) {
			for (int i : items[0])
				new Rangesx2(items[1], items[2]).provideVars((j, k) -> f.apply(i, j, k), list);
			return list;
		}

		/**
		 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
		 * triples of integers in this triple range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case, no
		 * variable is obtained, {@code null} is returned.
		 * 
		 * @param f
		 *            a function to convert triples of integer values into variables
		 * @return a non-empty 1-dimensional array of variables or {@code null}
		 */
		public <T extends IVar> T[] provideVars(Intx3Function<T> f) {
			return Utilities.convert(provideVars(f, new ArrayList<>()));
		}

		/**
		 * Executes the specified consumer on each triple contained in this range.
		 * 
		 * @param c3
		 *            an object consuming triples of integers.
		 */
		public void execute(Intx3Consumer c3) {
			for (int i : items[0])
				new Rangesx2(items[1], items[2]).execute((j, k) -> c3.accept(i, j, k));
		}

	}

	/**
	 * A class denoting a quadruple range.
	 */
	public static class Rangesx4 extends Ranges {
		private Rangesx4(Range range1, Range range2, Range range3, Range range4) {
			super(new Range[] { range1, range2, range3, range4 });
		}

		/**
		 * Returns a quintuple range obtained by combining this quadruple range with a range built from the specified bounds and step.
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the fifth range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the fifth range to be built
		 * @param step
		 *            the step of the fifth range to be built
		 * @return a quintuple range obtained by combining this quadruple range with a range built from the specified bounds and step
		 */
		public Rangesx5 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(minIncluded, maxIncluded, step));
		}

		/**
		 * Returns a quintuple range obtained by combining this quadruple range with a range built from the specified bounds (using implicitly a step
		 * equal to 1).
		 * 
		 * @param minIncluded
		 *            the lower bound (inclusive) of the fifth range to be built
		 * @param maxIncluded
		 *            the upper bound (inclusive) of the fifth range to be built
		 * @return a quintuple range obtained by combining this quadruple range with a range built from the specified bounds
		 */
		public Rangesx5 range(int minIncluded, int maxIncluded) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(minIncluded, maxIncluded));
		}

		/**
		 * Returns a quintuple range obtained by combining this quadruple range with a range built from the specified length (using implicitly a lower
		 * bound equal to 0 and a step equal to 1).
		 * 
		 * @param length
		 *            the length of the fifth range
		 * @return a quintuple range obtained by combining this quadruple range with a range built from the specified length
		 */
		public Rangesx5 range(int length) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(length));
		}

		private <T extends IVar> List<T> provideVars(Intx4Function<T> op, List<T> list) {
			for (int i : items[0])
				new Rangesx3(items[1], items[2], items[3]).provideVars((j, k, l) -> op.apply(i, j, k, l), list);
			return list;
		}

		/**
		 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
		 * quadruples of integers in this quadruple range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in case,
		 * no variable is obtained, {@code null} is returned.
		 * 
		 * @param f
		 *            a function to convert quadruples of integer values into variables
		 * @return a non-empty 1-dimensional array of variables or {@code null}
		 */
		public <T extends IVar> T[] provideVars(Intx4Function<T> f) {
			return Utilities.convert(provideVars(f, new ArrayList<>()));
		}

		/**
		 * Executes the specified consumer for each quadruple of integers contained in this range.
		 * 
		 * @param c4
		 *            an object consuming quadruples of integers.
		 */
		public void execute(Intx4Consumer c4) {
			for (int i : items[0])
				new Rangesx3(items[1], items[2], items[3]).execute((j, k, l) -> c4.accept(i, j, k, l));
		}

	}

	/**
	 * A class denoting a quintuple range.
	 */
	public static class Rangesx5 extends Ranges {
		private Rangesx5(Range range1, Range range2, Range range3, Range range4, Range range5) {
			super(new Range[] { range1, range2, range3, range4, range5 });
		}

		private <T extends IVar> List<T> provideVars(Intx5Function<T> op, List<T> list) {
			for (int i : items[0])
				new Rangesx4(items[1], items[2], items[3], items[4]).provideVars((j, k, l, m) -> op.apply(i, j, k, l, m), list);
			return list;
		}

		/**
		 * Returns a 1-dimensional array of variables, obtained after collecting the variables returned by the specified function when executed on all
		 * quintuples of integer values in this quintuple range. Note that {@code null} values are simply discarded, if ever generated. Be careful: in
		 * case, no variable is obtained, {@code null} is returned.
		 * 
		 * @param f
		 *            a function to convert quintuples of integer values into variables
		 * @return a non-empty 1-dimensional array of variables or {@code null}
		 */
		public <T extends IVar> T[] provideVars(Intx5Function<T> f) {
			return Utilities.convert(provideVars(f, new ArrayList<>()));
		}

		/**
		 * Executes the specified consumer on each quintuple of integers contained in this range.
		 * 
		 * @param c5
		 *            an object consuming quintuples of integers.
		 */
		public void execute(Intx5Consumer c5) {
			for (int i : items[0])
				new Rangesx4(items[1], items[2], items[3], items[4]).execute((j, k, l, m) -> c5.accept(i, j, k, l, m));
		}
	}
}
