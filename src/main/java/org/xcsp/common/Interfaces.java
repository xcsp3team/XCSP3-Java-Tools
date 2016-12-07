package org.xcsp.common;

import java.util.List;
import java.util.stream.IntStream;

import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;

public interface Interfaces {

	@FunctionalInterface
	interface Intx1Predicate {
		boolean test(int i);

		static <T> List<T> select(T[] vars, Intx1Predicate p, List<T> list) {
			IntStream.range(0, vars.length).filter(i -> p.test(i)).forEach(i -> list.add(vars[i]));
			return list;
		}
	}

	@FunctionalInterface
	interface Intx2Predicate {
		boolean test(int i, int j);

		static <T> List<T> select(T[][] vars, Intx2Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx1Predicate.select(vars[i], j -> p.test(i, j), list));
			return list;
		}
	}

	@FunctionalInterface
	interface Intx3Predicate {
		boolean test(int i, int j, int k);

		static <T extends IVar> List<T> select(T[][][] vars, Intx3Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx2Predicate.select(vars[i], (j, k) -> p.test(i, j, k), list));
			return list;
		}
	}

	@FunctionalInterface
	interface Intx4Predicate {
		boolean test(int i, int j, int k, int l);

		static <T extends IVar> List<T> select(T[][][][] vars, Intx4Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx3Predicate.select(vars[i], (j, k, l) -> p.test(i, j, k, l), list));
			return list;
		}
	}

	@FunctionalInterface
	interface Intx5Predicate {
		boolean test(int i, int j, int k, int l, int m);

		static <T extends IVar> List<T> select(T[][][][][] vars, Intx5Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx4Predicate.select(vars[i], (j, k, l, m) -> p.test(i, j, k, l, m), list));
			return list;
		}
	}

	@FunctionalInterface
	interface IntToDomInteger {
		XDomInteger apply(int i);
	}

	@FunctionalInterface
	interface Intx2ToDomInteger {
		XDomInteger apply(int i, int j);
	}

	@FunctionalInterface
	interface Intx3ToDomInteger {
		XDomInteger apply(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4ToDomInteger {
		XDomInteger apply(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5ToDomInteger {
		XDomInteger apply(int i, int j, int k, int l, int m);
	}

	@FunctionalInterface
	interface IntToDomSymbolic {
		XDomSymbolic apply(int i);
	}

	@FunctionalInterface
	interface Intx2Function<R> {
		R apply(int i, int j);
	}

	@FunctionalInterface
	interface Intx3Function<R> {
		R apply(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4Function<R> {
		R apply(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5Function<R> {
		R apply(int i, int j, int k, int l, int m);
	}

	@FunctionalInterface
	interface Intx2Consumer {
		void accept(int i, int j);
	}

	@FunctionalInterface
	interface Intx3Consumer {
		void accept(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4Consumer {
		void accept(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5Consumer {
		void accept(int i, int j, int k, int l, int m);
	}

	interface IVar {
		String id();
	};

	interface IVarInteger extends IVar {
	};

	interface IVarSymbolic extends IVar {
	};
}
