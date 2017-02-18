package org.xcsp.common;

import java.util.List;
import java.util.stream.IntStream;

import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;

public interface Interfaces {

	/**
	 * Represents a predicate (boolean-valued function) of one int-valued argument. This is a functional interface whose functional method
	 * is <code> test(int) </code>.
	 */
	@FunctionalInterface
	interface Intx1Predicate {

		/**
		 * Returns true iff the predicate accepts the specified integer
		 * 
		 * @param i
		 *            an integer
		 * @return true iff the predicate accepts the specified integer
		 */
		boolean test(int i);

		/**
		 * Returns the specified list after any variable, at index i, that satisfies the predicate have been added to it. Note that null
		 * values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 1-dimensional array of variables
		 * @param p
		 *            a predicate testing if a variable at index i must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T> List<T> select(T[] vars, Intx1Predicate p, List<T> list) {
			IntStream.range(0, vars.length).filter(i -> vars[i] != null && p.test(i)).forEach(i -> list.add(vars[i]));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) two int-valued arguments. This is a functional interface whose functional method is
	 * <code> test(int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx2Predicate {
		/**
		 * Returns true iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @return true iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j);

		/**
		 * Returns the specified list after any variable, at index (i,j), that satisfies the predicate have been added to it. Note that null
		 * values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 2-dimensional array of variables
		 * @param p
		 *            a predicate testing if a variable at index (i,j) must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T> List<T> select(T[][] vars, Intx2Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx1Predicate.select(vars[i], j -> p.test(i, j), list));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) three int-valued arguments. This is a functional interface whose functional method
	 * is <code> test(int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx3Predicate {
		/**
		 * Returns true iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @return true iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k);

		/**
		 * Returns the specified list after any variable, at index (i,j,k), that satisfies the predicate have been added to it. Note that
		 * null values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 3-dimensional array of variables
		 * 
		 * @param p
		 *            a predicate testing if a variable at index (i,j,k) must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T extends IVar> List<T> select(T[][][] vars, Intx3Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx2Predicate.select(vars[i], (j, k) -> p.test(i, j, k), list));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) four int-valued arguments. This is a functional interface whose functional method is
	 * <code> test(int,int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx4Predicate {
		/**
		 * Returns true iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 * @return true iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k, int l);

		/**
		 * Returns the specified list after any variable, at index (i,j,k,l), that satisfies the predicate have been added to it. Note that
		 * null values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 4-dimensional array of variables
		 * @param p
		 *            a predicate testing if a variable at index (i,j,k,l) must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T extends IVar> List<T> select(T[][][][] vars, Intx4Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx3Predicate.select(vars[i], (j, k, l) -> p.test(i, j, k, l), list));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) five int-valued arguments. This is a functional interface whose functional method is
	 * <code> test(int,int,int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx5Predicate {
		/**
		 * Returns true iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 * @param m
		 *            a fifth integer
		 * @return true iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k, int l, int m);

		/**
		 * Returns the specified list after any variable, at index (i,j,k,l,m), that satisfies the predicate have been added to it. Note
		 * that null values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 5-dimensional array of variables
		 * @param p
		 *            a predicate testing if a variable at index (i,j,k,l,m) must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
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

	interface Var extends IVar {
	};

	interface VarSymbolic extends IVar {
	};
}
