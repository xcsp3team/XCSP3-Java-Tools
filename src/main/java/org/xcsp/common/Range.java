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

import org.xcsp.common.Interfaces.IVar;
import org.xcsp.common.Interfaces.Intx1Predicate;
import org.xcsp.common.Interfaces.Intx2Consumer;
import org.xcsp.common.Interfaces.Intx2Function;
import org.xcsp.common.Interfaces.Intx2Predicate;
import org.xcsp.common.Interfaces.Intx3Consumer;
import org.xcsp.common.Interfaces.Intx3Function;
import org.xcsp.common.Interfaces.Intx4Consumer;
import org.xcsp.common.Interfaces.Intx4Function;
import org.xcsp.common.Interfaces.Intx5Consumer;
import org.xcsp.common.Interfaces.Intx5Function;

public class Range implements Iterable<Integer> {
	public int minIncluded, maxIncluded, step;

	public Range(int minIncluded, int maxIncluded, int step) {
		this.minIncluded = minIncluded;
		this.maxIncluded = maxIncluded;
		this.step = step;
		Utilities.control(step > 0 && minIncluded <= maxIncluded, "Bad values of parameters");
	}

	public Range(int minIncluded, int maxIncluded) {
		this(minIncluded, maxIncluded, 1);
	}

	public Range(int length) {
		this(0, length - 1, 1);
	}

	public Rangesx2 range(int minIncluded, int maxIncluded, int step) {
		return new Rangesx2(this, new Range(minIncluded, maxIncluded, step));
	}

	public Rangesx2 range(int minIncluded, int maxIncluded) {
		return new Rangesx2(this, new Range(minIncluded, maxIncluded));
	}

	public Rangesx2 range(int length) {
		return new Rangesx2(this, new Range(length));
	}

	public boolean isBasic() {
		return minIncluded == 0 && step == 1;
	}

	public boolean contains(int i) {
		return minIncluded <= i && i <= maxIncluded && ((i - minIncluded) % step == 0);
	}

	public int length() {
		return (maxIncluded - minIncluded + 1) / step;
	}

	public int[] toArray(IntUnaryOperator op) {
		// return IntStream.iterate(minIncluded, n -> n + step).takeWhile(n -> n <= maxIncluded).toArray(); // WAIT FOR JDK9
		List<Integer> list = new ArrayList<>();
		for (int i : this)
			list.add(op.applyAsInt(i));
		return list.stream().mapToInt(i -> i).toArray();
	}

	public int[] toArray() {
		return toArray(i -> i);
	}

	public IntStream stream() {
		return IntStream.of(toArray());
	}

	private <T extends IVar> List<T> selectVars(IntFunction<T> op, List<T> list) {
		for (int i : this) {
			T x = op.apply(i);
			if (x != null)
				list.add(x);
		}
		return list;
	}

	public <T extends IVar> T[] selectVars(IntFunction<T> f) {
		return Utilities.convert(selectVars(f, new ArrayList<>()));
	}

	public int[] select(Intx1Predicate p) {
		List<Integer> list = new ArrayList<>();
		for (int i : this)
			if (p.test(i))
				list.add(i);
		return list.stream().mapToInt(i -> i).toArray();
	}

	public int[] generate(IntFunction<Integer> f) {
		List<Integer> list = new ArrayList<>();
		for (int i : this) {
			Integer v = f.apply(i);
			if (v != null)
				list.add(v);
		}
		return list.stream().mapToInt(i -> i).toArray();
	}

	public void execute(IntConsumer c) {
		for (int i : this)
			c.accept(i);
	}

	@Override
	public Iterator<Integer> iterator() {
		return new Iterator<Integer>() {
			int cursor = minIncluded;

			public boolean hasNext() {
				return cursor <= maxIncluded;
			}

			public Integer next() {
				assert hasNext();
				int v = cursor;
				cursor += step;
				return v;
			}
		};
	}

	public static abstract class Ranges {
		public Range[] items;

		protected Ranges(Range[] items) {
			this.items = items;
		}

		public boolean contains(int... indexes) {
			Utilities.control(indexes.length == items.length, "bad number of indexes");
			return IntStream.range(0, indexes.length).allMatch(i -> items[i].contains(indexes[i]));
		}
	}

	public static class Rangesx2 extends Ranges {
		public Rangesx2(Range range1, Range range2) {
			super(new Range[] { range1, range2 });
		}

		public Rangesx3 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx3(items[0], items[1], new Range(minIncluded, maxIncluded, step));
		}

		public Rangesx3 range(int minIncluded, int maxIncluded) {
			return new Rangesx3(items[0], items[1], new Range(minIncluded, maxIncluded));
		}

		public Rangesx3 range(int length) {
			return new Rangesx3(items[0], items[1], new Range(length));
		}

		private <T extends IVar> List<T> selectVars(Intx2Function<T> op, List<T> list) {
			for (int i : items[0])
				items[1].selectVars(j -> op.apply(i, j), list);
			return list;
		}

		public <T extends IVar> T[] selectVars(Intx2Function<T> f) {
			return Utilities.convert(selectVars(f, new ArrayList<>()));
		}

		public <T extends IVar> T[][] selectVars2D(Intx2Function<T> op) {
			T[] vars = selectVars(op);
			int m = items[0].length();
			Utilities.control(vars.length % m == 0, "Bad value of m");
			T[][] t = (T[][]) Array.newInstance(vars[0].getClass(), vars.length / m, m);
			IntStream.range(0, t.length).forEach(i -> IntStream.range(0, m).forEach(j -> t[i][j] = vars[i * m + j]));
			return t;
		}

		public void execute(Intx2Consumer c2) {
			for (int i : items[0])
				items[1].execute(j -> c2.accept(i, j));
		}

		public int[][] toArray(IntBinaryOperator op) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				list.add(items[1].toArray(j -> op.applyAsInt(i, j)));
			return list.stream().toArray(int[][]::new);
		}

		public int[][] select(Intx2Predicate p) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1])
					if (p.test(i, j))
						list.add(new int[] { i, j });
			return list.stream().toArray(int[][]::new);
		}

		public int[][] generate(Intx2Function<int[]> f) {
			List<int[]> list = new ArrayList<>();
			for (int i : items[0])
				for (int j : items[1]) {
					int[] t = f.apply(i, j);
					if (t != null)
						list.add(t);
				}
			return list.stream().toArray(int[][]::new);
		}
	}

	public static class Rangesx3 extends Ranges {
		public Rangesx3(Range range1, Range range2, Range range3) {
			super(new Range[] { range1, range2, range3 });
		}

		public Rangesx4 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx4(items[0], items[1], items[2], new Range(minIncluded, maxIncluded, step));
		}

		public Rangesx4 range(int minIncluded, int maxIncluded) {
			return new Rangesx4(items[0], items[1], items[2], new Range(minIncluded, maxIncluded));
		}

		public Rangesx4 range(int length) {
			return new Rangesx4(items[0], items[1], items[2], new Range(length));
		}

		private <T extends IVar> List<T> selectVars(Intx3Function<T> op, List<T> list) {
			for (int i : items[0])
				new Rangesx2(items[1], items[2]).selectVars((j, k) -> op.apply(i, j, k), list);
			return list;
		}

		public <T extends IVar> T[] selectVars(Intx3Function<T> f) {
			return Utilities.convert(selectVars(f, new ArrayList<>()));
		}

		public void execute(Intx3Consumer c3) {
			for (int i : items[0])
				new Rangesx2(items[1], items[2]).execute((j, k) -> c3.accept(i, j, k));
		}

	}

	public static class Rangesx4 extends Ranges {
		public Rangesx4(Range range1, Range range2, Range range3, Range range4) {
			super(new Range[] { range1, range2, range3, range4 });
		}

		public Rangesx5 range(int minIncluded, int maxIncluded, int step) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(minIncluded, maxIncluded, step));
		}

		public Rangesx5 range(int minIncluded, int maxIncluded) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(minIncluded, maxIncluded));
		}

		public Rangesx5 range(int length) {
			return new Rangesx5(items[0], items[1], items[2], items[3], new Range(length));
		}

		private <T extends IVar> List<T> selectVars(Intx4Function<T> op, List<T> list) {
			for (int i : items[0])
				new Rangesx3(items[1], items[2], items[3]).selectVars((j, k, l) -> op.apply(i, j, k, l), list);
			return list;
		}

		public <T extends IVar> T[] selectVars(Intx4Function<T> f) {
			return Utilities.convert(selectVars(f, new ArrayList<>()));
		}

		public void execute(Intx4Consumer c4) {
			for (int i : items[0])
				new Rangesx3(items[1], items[2], items[3]).execute((j, k, l) -> c4.accept(i, j, k, l));
		}

	}

	public static class Rangesx5 extends Ranges {
		public Rangesx5(Range range1, Range range2, Range range3, Range range4, Range range5) {
			super(new Range[] { range1, range2, range3, range4, range5 });
		}

		private <T extends IVar> List<T> selectVars(Intx5Function<T> op, List<T> list) {
			for (int i : items[0])
				new Rangesx4(items[1], items[2], items[3], items[4]).selectVars((j, k, l, m) -> op.apply(i, j, k, l, m), list);
			return list;
		}

		public <T extends IVar> T[] selectVars(Intx5Function<T> f) {
			return Utilities.convert(selectVars(f, new ArrayList<>()));
		}

		public void execute(Intx5Consumer c4) {
			for (int i : items[0])
				new Rangesx4(items[1], items[2], items[3], items[4]).execute((j, k, l, m) -> c4.accept(i, j, k, l, m));
		}
	}
}
