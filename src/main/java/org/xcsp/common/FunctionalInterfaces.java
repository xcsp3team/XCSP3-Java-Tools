package org.xcsp.common;

import java.util.List;
import java.util.stream.IntStream;

import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomSymbolic;

/**
 * This is an interface that contains main functional interfaces, those that are mainly used by the modeler.
 */
public interface FunctionalInterfaces {

	/**
	 * Represents a predicate (boolean-valued function) of one integer argument. This is a functional interface whose functional method is
	 * <code> test(int) </code>.
	 */
	@FunctionalInterface
	interface Intx1Predicate {

		/**
		 * Returns {@code true} iff the predicate accepts the specified integer
		 * 
		 * @param i
		 *            an integer
		 * @return {@code true} iff the predicate accepts the specified integer
		 */
		boolean test(int i);

		/**
		 * Returns the specified list after any variable, at index {@code i}, that satisfies the specified predicate have been added to it. Note that
		 * {@code null} values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 1-dimensional array of variables
		 * @param p
		 *            a predicate allowing us to test if a variable at index {@code i} must be added to the list
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
	 * Represents a predicate (boolean-valued function) of two integer arguments. This is a functional interface whose functional method is
	 * <code> test(int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx2Predicate {
		/**
		 * Returns {@code true} iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @return {@code true} iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j);

		/**
		 * Returns the specified list after any variable, at index {@code (i,j)}, that satisfies the specified predicate have been added to it. Note
		 * that {@code null} values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 2-dimensional array of variables
		 * @param p
		 *            a predicate allowing us to test if a variable at index {@code (i,j)} must be added to the list
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
	 * Represents a predicate (boolean-valued function) of three integer arguments. This is a functional interface whose functional method is
	 * <code> test(int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx3Predicate {
		/**
		 * Returns {@code true} iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @return {@code true} iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k);

		/**
		 * Returns the specified list after any variable, at index {@code (i,j,k)}, that satisfies the predicate have been added to it. Note that
		 * {@code null} values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 3-dimensional array of variables
		 * 
		 * @param p
		 *            a predicate allowing us to test if a variable at index {@code (i,j,k)} must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T> List<T> select(T[][][] vars, Intx3Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx2Predicate.select(vars[i], (j, k) -> p.test(i, j, k), list));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) of four integer arguments. This is a functional interface whose functional method is
	 * <code> test(int,int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx4Predicate {
		/**
		 * Returns {@code true} iff the predicate accepts the specified integers
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 * @return {@code true} iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k, int l);

		/**
		 * Returns the specified list after any variable, at index {@code (i,j,k,l)}, that satisfies the specified predicate have been added to it.
		 * Note that {@code null} values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 4-dimensional array of variables
		 * @param p
		 *            a predicate allowing us to test if a variable at index {@code (i,j,k,l)} must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T> List<T> select(T[][][][] vars, Intx4Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx3Predicate.select(vars[i], (j, k, l) -> p.test(i, j, k, l), list));
			return list;
		}
	}

	/**
	 * Represents a predicate (boolean-valued function) of five integer arguments. This is a functional interface whose functional method is
	 * <code> test(int,int,int,int,int) </code>.
	 */
	@FunctionalInterface
	interface Intx5Predicate {
		/**
		 * Returns {@code true} iff the predicate accepts the specified integers
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
		 * @return {@code true} iff the predicate accepts the specified integers
		 */
		boolean test(int i, int j, int k, int l, int m);

		/**
		 * Returns the specified list after any variable, at index {@code (i,j,k,l,m)}, that satisfies the specified predicate have been added to it.
		 * Note that {@code null} values are simply discarded, if ever present.
		 * 
		 * @param vars
		 *            a 5-dimensional array of variables
		 * @param p
		 *            a predicate allowing us to test if a variable at index {@code (i,j,k,l,m)} must be added to the list
		 * @param list
		 *            a list where selected variables are added
		 * @return the specified list, after selected variables have been added
		 */
		static <T> List<T> select(T[][][][][] vars, Intx5Predicate p, List<T> list) {
			IntStream.range(0, vars.length).forEach(i -> Intx4Predicate.select(vars[i], (j, k, l, m) -> p.test(i, j, k, l, m), list));
			return list;
		}
	}

	/**
	 * Represents a function that associates an integer domain (possibly, {@code null}) with a given integer. This is a functional interface whose
	 * functional method is <code> apply(int) </code>. This may be useful when building 1-dimensional arrays of integer variables as for example in: *
	 * 
	 * <pre>
	 * {@code Var[] = array("x", size(10), i -> i < 5 ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * On our example, the first five variables have a domain containing 10 values whereas the next five variables have a domain containing two values
	 * only.
	 */
	@FunctionalInterface
	interface IntToDom {
		/**
		 * Returns an integer domain, computed from the specified integer.
		 * 
		 * @param i
		 *            an integer
		 * @return an integer domain (possibly {@code null}), computed from the specified integer
		 */
		Dom apply(int i);
	}

	/**
	 * Represents a function that associates an integer domain (possibly, {@code null}) with a given pair of integers. This is a functional interface
	 * whose functional method is <code> apply(int,int) </code>. This may be useful when building 2-dimensional arrays of integer variables as for
	 * example in:
	 * 
	 * <pre>
	 * {@code Var[][] = array("x", size(10, 5), (i,j) -> i < j ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 10 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx2ToDom {
		/**
		 * Returns an integer domain, computed from the specified integers.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @return an integer domain (possibly {@code null}), computed from the specified integers
		 */
		Dom apply(int i, int j);
	}

	/**
	 * Represents a function that associates an integer domain (possibly, {@code null}) with three given integers. This is a functional interface
	 * whose functional method is <code> apply(int,int,int) </code>. This may be useful when building 3-dimensional arrays of integer variables as for
	 * example in:
	 * 
	 * <pre>
	 * {@code Var[][][] = array("x", size(10, 5, 3), (i,j,k) -> i == j+k ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 10 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx3ToDom {
		/**
		 * Returns an integer domain, computed from the specified integers.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @return an integer domain (possibly {@code null}), computed from the specified integers
		 */
		Dom apply(int i, int j, int k);
	}

	/**
	 * Represents a function that associates an integer domain (possibly, {@code null}) with four given integers. This is a functional interface whose
	 * functional method is <code> apply(int,int,int,int) </code>. This may be useful when building 4-dimensional arrays of integer variables as for
	 * example in:
	 * 
	 * <pre>
	 * {@code Var[][][][] = array("x", size(10, 5, 3, 3), (i,j,k,l) -> i+j == k+l ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 10 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx4ToDom {
		/**
		 * Returns an integer domain, computed from the specified integers.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 * @return an integer domain (possibly {@code null}), computed from the specified integers
		 */
		Dom apply(int i, int j, int k, int l);
	}

	/**
	 * Represents a function that associates an integer domain (possibly, {@code null}) with five given integers. This is a functional interface whose
	 * functional method is <code> apply(int,int,int,int,int) </code>. This may be useful when building 5-dimensional arrays of integer variables as
	 * for example in:
	 * 
	 * <pre>
	 * {@code Var[][][][][] = array("x", size(10, 5, 3, 3,2), (i,j,k,l,m) -> i+j == k+l+m ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 10 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx5ToDom {
		/**
		 * Returns an integer domain, computed from the specified integers.
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
		 * @return an integer domain (possibly {@code null}), computed from the specified integers
		 */
		Dom apply(int i, int j, int k, int l, int m);
	}

	/**
	 * Represents a function that associates a symbolic domain (possibly, {@code null}) with a given integer. This is a functional interface whose
	 * functional method is <code> apply(int) </code>. This may be useful when building 1-dimensional arrays of symbolic variables as for example in:
	 * *
	 * 
	 * <pre>
	 * {@code VarSymbolic[] = array("x", size(10), i -> i < 5 ? dom("red","green","blue") : dom("yellow","orange"));}
	 * </pre>
	 * 
	 * On our example, the first five variables have a domain containing 3 values whereas the next five variables have a domain containing two values
	 * only.
	 */
	@FunctionalInterface
	interface IntToDomSymbolic {
		/**
		 * Returns a symbolic domain, computed from the specified integer.
		 * 
		 * @param i
		 *            an integer
		 * @return a symbolic domain (possibly {@code null}), computed from the specified integer
		 */
		DomSymbolic apply(int i);
	}

	/**
	 * Represents a function that associates a symbolic domain (possibly, {@code null}) with a given pair of integers. This is a functional interface
	 * whose functional method is <code> apply(int,int) </code>. This may be useful when building 2-dimensional arrays of symbolic variables as for
	 * example in:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][] = array("x", size(10, 5), (i,j) -> i < j ? dom("red","green","blue") : dom("yellow","orange"));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 3 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx2ToDomSymbolic {
		/**
		 * Returns a symbolic domain, computed from the specified integers.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @return a symbolic domain (possibly {@code null}), computed from the specified integers
		 */
		DomSymbolic apply(int i, int j);
	}

	/**
	 * Represents a function that associates a symbolic domain (possibly, {@code null}) with three given integers. This is a functional interface
	 * whose functional method is <code> apply(int,int,int) </code>. This may be useful when building 3-dimensional arrays of symbolic variables as
	 * for example in:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][][] = array("x", size(10, 5, 3), (i,j,k) -> i == j+k ? dom("red","green","blue") : dom("yellow","orange"));}
	 * </pre>
	 * 
	 * On our example, some variables have a domain containing 3 values whereas others have a domain containing two values only.
	 */
	@FunctionalInterface
	interface Intx3ToDomSymbolic {
		/**
		 * Returns a symbolic domain, computed from the specified integers.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @return a symbolic domain (possibly {@code null}), computed from the specified integers
		 */
		DomSymbolic apply(int i, int j, int k);
	}

	/**
	 * Represents a function that accepts two integers and returns an object {@code R}. This is a functional interface whose functional method is
	 * apply(int,int).
	 * 
	 * @param <R>
	 *            the type of the result of the function
	 */
	@FunctionalInterface
	interface Intx2Function<R> {
		/**
		 * Applies this function to the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @return the function result
		 */
		R apply(int i, int j);
	}

	/**
	 * Represents a function that accepts three integers and returns an object {@code R}. This is a functional interface whose functional method is
	 * apply(int,int,int).
	 * 
	 * @param <R>
	 *            the type of the result of the function
	 */
	@FunctionalInterface
	interface Intx3Function<R> {
		/**
		 * Applies this function to the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @return the function result
		 */
		R apply(int i, int j, int k);
	}

	/**
	 * Represents a function that accepts four integers and returns an object {@code R}. This is a functional interface whose functional method is
	 * apply(int,int,int,int).
	 * 
	 * @param <R>
	 *            the type of the result of the function
	 */
	@FunctionalInterface
	interface Intx4Function<R> {
		/**
		 * Applies this function to the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 * @return the function result
		 */
		R apply(int i, int j, int k, int l);
	}

	/**
	 * Represents a function that accepts five integers and returns an object {@code R}. This is a functional interface whose functional method is
	 * apply(int,int,int,int,int).
	 * 
	 * @param <R>
	 *            the type of the result of the function
	 */
	@FunctionalInterface
	interface Intx5Function<R> {
		/**
		 * Applies this function to the given arguments.
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
		 * @return the function result
		 */
		R apply(int i, int j, int k, int l, int m);
	}

	/**
	 * Represents a function that accepts six integers and returns an object {@code R}. This is a functional interface whose functional method is
	 * apply(int,int,int,int,int, int).
	 * 
	 * @param <R>
	 *            the type of the result of the function
	 */
	@FunctionalInterface
	interface Intx6Function<R> {
		/**
		 * Applies this function to the given arguments.
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
		 * @param n
		 *            a sixth integer
		 * @return the function result
		 */
		R apply(int i, int j, int k, int l, int m, int n);
	}

	/**
	 * Represents an operation that accepts two integers and returns no result. This is a functional interface whose functional method is
	 * accept(int,int).
	 * 
	 */
	@FunctionalInterface
	interface Intx2Consumer {
		/**
		 * Performs this operation on the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 */
		void accept(int i, int j);
	}

	/**
	 * Represents an operation that accepts three integers and returns no result. This is a functional interface whose functional method is
	 * accept(int,int,int).
	 * 
	 */
	@FunctionalInterface
	interface Intx3Consumer {
		/**
		 * Performs this operation on the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 */
		void accept(int i, int j, int k);
	}

	/**
	 * Represents an operation that accepts four integers and returns no result. This is a functional interface whose functional method is
	 * accept(int,int,int,int).
	 * 
	 */
	@FunctionalInterface
	interface Intx4Consumer {
		/**
		 * Performs this operation on the given arguments.
		 * 
		 * @param i
		 *            a first integer
		 * @param j
		 *            a second integer
		 * @param k
		 *            a third integer
		 * @param l
		 *            a fourth integer
		 */
		void accept(int i, int j, int k, int l);
	}

	/**
	 * Represents an operation that accepts five integers and returns no result. This is a functional interface whose functional method is
	 * accept(int,int,int,int,int).
	 * 
	 */
	@FunctionalInterface
	interface Intx5Consumer {
		/**
		 * Performs this operation on the given arguments.
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
		 */
		void accept(int i, int j, int k, int l, int m);
	};

	/**
	 * Represents an operation that accepts six integers and returns no result. This is a functional interface whose functional method is
	 * accept(int,int,int,int,int,int).
	 * 
	 */
	@FunctionalInterface
	interface Intx6Consumer {
		/**
		 * Performs this operation on the given arguments.
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
		 * @param n
		 *            a sixth integer
		 */
		void accept(int i, int j, int k, int l, int m, int n);
	};
}
