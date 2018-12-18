package org.xcsp.modeler.api;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.FunctionalInterfaces.IntToDom;
import org.xcsp.common.FunctionalInterfaces.Intx1Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx3Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx4ToDom;
import org.xcsp.common.FunctionalInterfaces.Intx5ToDom;
import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.Range;
import org.xcsp.common.Size.Size1D;
import org.xcsp.common.Size.Size2D;
import org.xcsp.common.Size.Size3D;
import org.xcsp.common.Size.Size4D;
import org.xcsp.common.Size.Size5D;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.Automaton;
import org.xcsp.common.structures.Table;
import org.xcsp.common.structures.Transition;
import org.xcsp.common.structures.Transitions;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesInt;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesInt1D;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesIntRange;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesIntRange1D;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesVar1D;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;

public interface ProblemAPI extends ProblemAPIOnVars, ProblemAPIOnVals, ProblemAPISymbolic {

	// ************************************************************************
	// ***** Methods for defining domains, sizes and ranges
	// ************************************************************************

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified array and that respect the specified predicate.
	 * 
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value {@code v} must be kept in the domain
	 * @return an integer domain composed of the sorted distinct values that come from the specified array and that respect the specified predicate
	 */
	default Dom dom(int[] values, Intx1Predicate p) {
		control(values.length > 0, "At least one value must be specified");
		values = IntStream.of(values).sorted().distinct().filter(v -> p == null || p.test(v)).toArray();
		return new Dom(values);
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param values
	 *            a 1-dimensional array of integers
	 * @return an integer domain composed of the sorted distinct values that come from the specified array
	 */
	default Dom dom(int[] values) {
		if (values == null || values.length == 0) {
			System.out.println("Empty domain when calling dom(). Is that correct?");
			return null;
		}
		return dom(values, null);
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified values.
	 * 
	 * @param value
	 *            a first integer (value)
	 * @param otherValues
	 *            a sequence of other integers (values)
	 * @return an integer domain composed of the sorted distinct values that come from the specified values
	 */
	default Dom dom(int value, int... otherValues) {
		return dom(IntStream.range(0, otherValues.length + 1).map(i -> i == 0 ? value : otherValues[i - 1]).toArray());
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified collection.
	 * 
	 * @param values
	 *            a collection of integers (values)
	 * @return an integer domain composed of the sorted distinct values that come from the specified collection
	 */
	default Dom dom(Collection<Integer> values) {
		return dom(values.stream().mapToInt(i -> i).toArray());
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified stream
	 * 
	 * @param values
	 *            a stream of integer values
	 * @return an integer domain composed of the sorted distinct values that come from the specified stream
	 */
	default Dom dom(IntStream values) {
		return dom(values.toArray());
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param m
	 *            a 2-dimensional array of variables
	 * @return an integer domain composed of the sorted distinct values that come from the specified array
	 */
	default Dom dom(int[][] m) {
		return dom(Stream.of(m).map(t -> Arrays.stream(t)).flatMapToInt(i -> i).toArray());
	}

	/**
	 * Returns an integer domain composed of the values contained in the specified range.
	 * 
	 * @param range
	 *            the range of values to be considered for the domain
	 * @return an integer domain composed of the values contained in the specified range
	 */
	default Dom dom(Range range) {
		return range.length() == 1 ? dom(range.startInclusive)
				: range.step == 1 ? new Dom(range.startInclusive, range.endExclusive - 1) : new Dom(range.toArray());
	}

	/**
	 * Builds an object that represents the size (length) of a 1-dimensional array.
	 * 
	 * @param length
	 *            the size (length) of the array
	 * @return an object that represents the size (length) of a 1-dimensional array
	 */
	default Size1D size(int length) {
		control(length > 0, "The specified length must be strictly positive");
		return Size1D.build(length);
	}

	/**
	 * Builds an object that represents the size (i.e., length of each dimension) of a 2-dimensional array.
	 * 
	 * @param length0
	 *            the size (length) of the first dimension of a 2-dimensional array
	 * @param length1
	 *            the size (length) of the second dimension of a 2-dimensional array
	 * @return an object that represents the size (i.e., length of each dimension) of a 2-dimensional array
	 */
	default Size2D size(int length0, int length1) {
		control(length0 > 0 && length1 > 0, "The specified lengths must be strictly positive");
		return Size2D.build(length0, length1);
	}

	/**
	 * Builds an object that represents the size (i.e., length of each dimension) of a 3-dimensional array.
	 * 
	 * @param length0
	 *            the size (length) of the first dimension of a 3-dimensional array
	 * @param length1
	 *            the size (length) of the second dimension of a 3-dimensional array
	 * @param length2
	 *            the size (length) of the third dimension of a 3-dimensional array
	 * @return an object that represents the size (i.e., length of each dimension) of a 3-dimensional array
	 */
	default Size3D size(int length0, int length1, int length2) {
		control(length0 > 0 && length1 > 0 && length2 > 0, "The specified lengths must be strictly positive");
		return Size3D.build(length0, length1, length2);
	}

	/**
	 * Builds an object that represents the size (i.e., length of each dimension) of a 4-dimensional array.
	 * 
	 * @param length0
	 *            the size (length) of the first dimension of a 4-dimensional array
	 * @param length1
	 *            the size (length) of the second dimension of a 4-dimensional array
	 * @param length2
	 *            the size (length) of the third dimension of a 4-dimensional array
	 * @param length3
	 *            the size (length) of the fourth dimension of a 4-dimensional array
	 * @return an object that represents the size (i.e., length of each dimension) of a 4-dimensional array
	 */
	default Size4D size(int length0, int length1, int length2, int length3) {
		control(length0 > 0 && length1 > 0 && length2 > 0 && length3 > 0, "The specified lengths must be strictly positive");
		return Size4D.build(length0, length1, length2, length3);
	}

	/**
	 * Builds an object that represents the size (i.e., length of each dimension) of a 5-dimensional array.
	 * 
	 * @param length0
	 *            the size (length) of the first dimension of a 5-dimensional array
	 * @param length1
	 *            the size (length) of the second dimension of a 5-dimensional array
	 * @param length2
	 *            the size (length) of the third dimension of a 5-dimensional array
	 * @param length3
	 *            the size (length) of the fourth dimension of a 5-dimensional array
	 * @param length4
	 *            the size (length) of the fifth dimension of a 5-dimensional array
	 * @return an object that represents the size (i.e., length of each dimension) of a 5-dimensional array
	 */
	default Size5D size(int length0, int length1, int length2, int length3, int length4) {
		control(length0 > 0 && length1 > 0 && length2 > 0 && length3 > 0 && length4 > 0, "The specified lengths must be strictly positive");
		return Size5D.build(length0, length1, length2, length3, length4);
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds and step (difference between each two successive numbers).
	 * 
	 * @param startInclusive
	 *            the lower bound (inclusive) of this range
	 * @param endExclusive
	 *            the upper bound (exclusive) of this range
	 * @param step
	 *            the step of this range
	 * @return the object {@code Range} that represents an interval of values (while considering the specified step)
	 * 
	 */
	default Range range(int startInclusive, int endExclusive, int step) {
		return new Range(startInclusive, endExclusive, step);
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds and step (difference between each two successive numbers).
	 * 
	 * @param startInclusive
	 *            the lower bound (inclusive) of this range
	 * @param endInclusive
	 *            the upper bound (inclusive) of this range
	 * @param step
	 *            the step of this range
	 * @return the object {@code Range} that represents an interval of values (while considering the specified step)
	 * 
	 */
	default Range rangeClosed(int startInclusive, int endInclusive, int step) {
		return range(startInclusive, endInclusive + 1, step);
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds (using implicitly a step equal to 1).
	 * 
	 * @param startInclusive
	 *            the lower bound (inclusive) of this range
	 * @param endExclusive
	 *            the upper bound (exclusive) of this range
	 * @return the object {@code Range} that represents an interval of values
	 */
	default Range range(int startInclusive, int endExclusive) {
		return new Range(startInclusive, endExclusive);
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds (using implicitly a step equal to 1).
	 * 
	 * @param startInclusive
	 *            the lower bound (inclusive) of this range
	 * @param endInclusive
	 *            the upper bound (inclusive) of this range
	 * @return the object {@code Range} that represents an interval of values
	 */
	default Range rangeClosed(int startInclusive, int endInclusive) {
		return range(startInclusive, endInclusive + 1);
	}

	/**
	 * Constructs an object {@code Range} from the specified length (using implicitly a lower bound equal to 0 and a step equal to 1).
	 * 
	 * @param length
	 *            the length of this range
	 * @return the object {@code Range} that represents an interval of values, from 0 to the specified value (excluded)
	 */
	default Range range(int length) {
		return new Range(length);
	}

	// ************************************************************************
	// ***** Managing Variables
	// ************************************************************************

	/**
	 * Builds a stand-alone integer variable with the specified id, domain, note (short comment) and classes. Use methods {@code dom()} for building
	 * integer domains. For example:
	 * 
	 * <pre>
	 * {@code Var x = var("x", dom(range(10), "x is the number of products");}
	 * </pre>
	 * 
	 * On our example, we build a stand-alone variable whose domain contains 10 values.
	 * 
	 * @param id
	 *            the id (unique name) of the variable
	 * @param dom
	 *            the integer domain of the variable
	 * @param note
	 *            a short comment about the variable
	 * @param classes
	 *            the tags (possibly, none) associated with the variable
	 * @return a stand-alone integer variable
	 */
	default Var var(String id, Dom dom, String note, TypeClass... classes) {
		Var x = imp().buildVarInteger(id, dom);
		if (x != null)
			imp().varEntities.newVarAloneEntity(id, x, note, classes);
		return x;
	}

	/**
	 * Builds a stand-alone integer variable with the specified id, domain and classes. Use methods {@code dom()} for building integer domains. For
	 * example:
	 * 
	 * <pre>
	 * {@code Var x = var("x", dom(range(10));}
	 * </pre>
	 * 
	 * On our example, we build a stand-alone variable whose domain contains 10 values.
	 * 
	 * @param id
	 *            the id (unique name) of the variable
	 * @param dom
	 *            the integer domain of the variable
	 * @param classes
	 *            the tags (possibly, none) associated with the variable
	 * @return a stand-alone integer variable
	 */
	default Var var(String id, Dom dom, TypeClass... classes) {
		return var(id, dom, null, classes);
	}

	/**
	 * Builds a 1-dimensional array of integer variables with the specified id, size, note (short comment) and classes. Use Method {@code size(int)}
	 * for building the size (length) of the array. The specified function {@code f} associates an integer domain with each variable at index
	 * {@code i} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the following
	 * example, the first five variables have a domain containing 10 values whereas the next five variables have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[] = array("x", size(10), i -> i < 5 ? dom(range(10)) : dom(0,1),"x[i] is the number of carrots eaten by the ith rabbit");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code i} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of integer variables
	 */
	default Var[] array(String id, Size1D size, IntToDom f, String note, TypeClass... classes) {
		Var[] t = imp().fill(id, size, f, (Var[]) Array.newInstance(imp().classVI(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, t, note, classes); // TODO indicate not same domains ?
		return t;
	}

	/**
	 * Builds a 1-dimensional array of integer variables with the specified id, size, and classes. Use Method {@code size(int)} for building the size
	 * (length) of the array. The specified function {@code f} associates an integer domain with each variable at index {@code i} of the array. In
	 * case the specified function {@code f} return the value {@code null}, the variable is not built. In the following example, the first five
	 * variables have a domain containing 10 values whereas the next five variables have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[] = array("x", size(10), i -> i < 5 ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code i} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of integer variables
	 */
	default Var[] array(String id, Size1D size, IntToDom f, TypeClass... classes) {
		return array(id, size, f, null, classes);
	}

	/**
	 * Builds a 1-dimensional array of integer variables with the specified id, size, domain, note and classes. Use Method {@code size(int)} for
	 * building the size (length) of the array. Each variable of the array has the specified integer domain. In the following example, the ten
	 * variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[] = array("x", size(10), dom(range(10), "x[i] is the number of carrots eaten by the ith rabbit");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of integer variables
	 */
	default Var[] array(String id, Size1D size, Dom dom, String note, TypeClass... classes) {
		return array(id, size, i -> dom, note, classes);
	}

	/**
	 * Builds a 1-dimensional array of integer variables with the specified id, size, domain, and classes. Use Method {@code size(int)} for building
	 * the size (length) of the array. Each variable of the array has the specified integer domain. In the following example, the ten variables have a
	 * domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[] = array("x", size(10), dom(range(10));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of integer variables
	 */
	default Var[] array(String id, Size1D size, Dom dom, TypeClass... classes) {
		return array(id, size, i -> dom, null, classes);
	}

	/**
	 * Builds a 2-dimensional array of integer variables with the specified id, size, note (short comment) and classes. Use Method
	 * {@code size(int,int)} for building the size (length of each dimension) of the array. The specified function {@code f} associates an integer
	 * domain with each variable at index {@code (i,j)} of the array. In case the specified function {@code f} return the value {@code null}, the
	 * variable is not built. In the following example, some variables have a domain containing 10 values whereas others have a domain containing two
	 * values only:
	 * 
	 * <pre>
	 * {@code Var[][] = array("x", size(10, 5), (i,j) -> i < j ? dom(range(10)) : dom(0,1), 
	 *   "x[i][j] is the number of forks built by the jth worker at the ith factory");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j)} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of integer variables
	 */
	default Var[][] array(String id, Size2D size, Intx2ToDom f, String note, TypeClass... classes) {
		Var[][] m = imp().fill(id, size, f, (Var[][]) Array.newInstance(imp().classVI(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, m, note, classes); // TODO indicate not same domains somewhere ?
		return m;
	}

	/**
	 * Builds a 2-dimensional array of integer variables with the specified id, size, and classes. Use Method {@code size(int,int)} for building the
	 * size (length of each dimension) of the array. The specified function {@code f} associates an integer domain with each variable at index
	 * {@code (i,j)} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the following
	 * example, some variables have a domain containing 10 values whereas others have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][] = array("x", size(10, 5), (i,j) -> i < j ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j)} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of integer variables
	 */
	default Var[][] array(String id, Size2D size, Intx2ToDom f, TypeClass... classes) {
		return array(id, size, f, null, classes);
	}

	/**
	 * Builds a 2-dimensional array of integer variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified integer
	 * domain. In the following example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][] = array("x", size(10, 5), dom(range(10)),
	 *   "x[i][j] is the number of forks built by the jth worker at the ith factory");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of integer variables
	 */
	default Var[][] array(String id, Size2D size, Dom dom, String note, TypeClass... classes) {
		return array(id, size, (i, j) -> dom, note, classes);
	}

	/**
	 * Builds a 2-dimensional array of integer variables with the specified id, size, domain, and classes. Use Method {@code size(int,int)} for
	 * building the size (length of each dimension) of the array. Each variable of the array has the specified integer domain. In the following
	 * example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][] = array("x", size(10, 5), dom(range(10)));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of integer variables
	 */
	default Var[][] array(String id, Size2D size, Dom dom, TypeClass... classes) {
		return array(id, size, (i, j) -> dom, null, classes);
	}

	/**
	 * Builds a 3-dimensional array of integer variables with the specified id, size, note (short comment) and classes. Use Method
	 * {@code size(int,int,int)} for building the size (length of each dimension) of the array. The specified function {@code f} associates an integer
	 * domain with each variable at index {@code (i,j,k)} of the array. In case the specified function {@code f} return the value {@code null}, the
	 * variable is not built. In the following example, some variables have a domain containing 10 values whereas others have a domain containing two
	 * values only:
	 * 
	 * <pre>
	 * {@code Var[][][] = array("x", size(10, 5, 5), (i,j,k) -> i+j == k ? dom(range(10)) : dom(0,1), 
	 *   "x[i][j][k] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k)} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 3-dimensional array of integer variables
	 */
	default Var[][][] array(String id, Size3D size, Intx3ToDom f, String note, TypeClass... classes) {
		Var[][][] m = imp().fill(id, size, f, (Var[][][]) Array.newInstance(imp().classVI(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, m, note, classes); // TODO indicate not same domains?
		return m;
	}

	/**
	 * Builds a 3-dimensional array of integer variables with the specified id, size, and classes. Use Method {@code size(int,int,int)} for building
	 * the size (length of each dimension) of the array. The specified function {@code f} associates an integer domain with each variable at index
	 * {@code (i,j,k)} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the
	 * following example, some variables have a domain containing 10 values whereas others have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][][] = array("x", size(10, 5, 5), (i,j,k) -> i+j == k ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k)} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 3-dimensional array of integer variables
	 */
	default Var[][][] array(String id, Size3D size, Intx3ToDom f, TypeClass... classes) {
		return array(id, size, f, null, classes);
	}

	/**
	 * Builds a 3-dimensional array of integer variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified integer
	 * domain. In the following example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][] = array("x", size(10, 5, 2), dom(range(10)),
	 *   "x[i][j][k] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 3-dimensional array of integer variables
	 */
	default Var[][][] array(String id, Size3D size, Dom dom, String note, TypeClass... classes) {
		return array(id, size, (i, j, k) -> dom, note, classes);
	}

	/**
	 * Builds a 3-dimensional array of integer variables with the specified id, size, domain, and classes. Use Method {@code size(int,int,int)} for
	 * building the size (length of each dimension) of the array. Each variable of the array has the specified integer domain. In the following
	 * example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][] = array("x", size(10, 5, 2), dom(range(10)));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 3-dimensional array of integer variables
	 */
	default Var[][][] array(String id, Size3D size, Dom dom, TypeClass... classes) {
		return array(id, size, (i, j, k) -> dom, null, classes);
	}

	/**
	 * Builds a 4-dimensional array of integer variables with the specified id, size, note (short comment) and classes. Use Method
	 * {@code size(int,int,int,int)} for building the size (length of each dimension) of the array. The specified function {@code f} associates an
	 * integer domain with each variable at index {@code (i,j,k,l)} of the array. In case the specified function {@code f} return the value
	 * {@code null}, the variable is not built. In the following example, some variables have a domain containing 10 values whereas others have a
	 * domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][][][] = array("x", size(10, 5, 5, 2), (i,j,k,l) -> i+j == k+l ? dom(range(10)) : dom(0,1), 
	 *   "x[i][j][k][l] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k,l)} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 4-dimensional array of integer variables
	 */
	default Var[][][][] array(String id, Size4D size, Intx4ToDom f, String note, TypeClass... classes) {
		Var[][][][] m = imp().fill(id, size, f, (Var[][][][]) Array.newInstance(imp().classVI(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, m, note, classes); // TODO indicate not same domains
		return m;
	}

	/**
	 * Builds a 4-dimensional array of integer variables with the specified id, size, and classes. Use Method {@code size(int,int,int,int)} for
	 * building the size (length of each dimension) of the array. The specified function {@code f} associates an integer domain with each variable at
	 * index {@code (i,j,k,l)} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the
	 * following example, some variables have a domain containing 10 values whereas others have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][][][] = array("x", size(10, 5, 5, 2), (i,j,k,l) -> i+j == k+l ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k,l)} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 4-dimensional array of integer variables
	 */
	default Var[][][][] array(String id, Size4D size, Intx4ToDom f, TypeClass... classes) {
		return array(id, size, f, null, classes);
	}

	/**
	 * Builds a 4-dimensional array of integer variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int,int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified
	 * integer domain. In the following example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][][] = array("x", size(10, 5, 2, 2), dom(range(10)),
	 *   "x[i][j][k][l] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 4-dimensional array of integer variables
	 */
	default Var[][][][] array(String id, Size4D size, Dom dom, String note, TypeClass... classes) {
		return array(id, size, (i, j, k, l) -> dom, note, classes);
	}

	/**
	 * Builds a 4-dimensional array of integer variables with the specified id, size, domain, and classes. Use Method {@code size(int,int,int,int)}
	 * for building the size (length of each dimension) of the array. Each variable of the array has the specified integer domain. In the following
	 * example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][][] = array("x", size(10, 5, 2, 2), dom(range(10)));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 4-dimensional array of integer variables
	 */
	default Var[][][][] array(String id, Size4D size, Dom dom, TypeClass... classes) {
		return array(id, size, (i, j, k, l) -> dom, null, classes);
	}

	/**
	 * Builds a 5-dimensional array of integer variables with the specified id, size, note (short comment) and classes. Use Method
	 * {@code size(int,int,int,int,int)} for building the size (length of each dimension) of the array. The specified function {@code f} associates an
	 * integer domain with each variable at index {@code (i,j,k,l,m)} of the array. In case the specified function {@code f} return the value
	 * {@code null}, the variable is not built. In the following example, some variables have a domain containing 10 values whereas others have a
	 * domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][][][][] = array("x", size(10, 5, 5, 2, 2), (i,j,k,l) -> i+j == k+l ? dom(range(10)) : dom(0,1), 
	 *   "x[i][j][k][l] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k,l),m} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 5-dimensional array of integer variables
	 */
	default Var[][][][][] array(String id, Size5D size, Intx5ToDom f, String note, TypeClass... classes) {
		Var[][][][][] q = imp().fill(id, size, f, (Var[][][][][]) Array.newInstance(imp().classVI(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, q, note, classes); // TODO indicate not same domains
		return q;
	}

	/**
	 * Builds a 5-dimensional array of integer variables with the specified id, size, and classes. Use Method {@code size(int,int,int,int,int)} for
	 * building the size (length of each dimension) of the array. The specified function {@code f} associates an integer domain with each variable at
	 * index {@code (i,j,k,l,m)} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In
	 * the following example, some variables have a domain containing 10 values whereas others have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code Var[][][][][] = array("x", size(10, 5, 5, 2, 2), (i,j,k,l) -> i+j == k+l ? dom(range(10)) : dom(0,1));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates an integer domain with any possible index {@code (i,j,k,l,m)} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 5-dimensional array of integer variables
	 */
	default Var[][][][][] array(String id, Size5D size, Intx5ToDom f, TypeClass... classes) {
		return array(id, size, f, null, classes);
	}

	/**
	 * Builds a 5-dimensional array of integer variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int,int,int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified
	 * integer domain. In the following example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][][][] = array("x", size(10, 5, 2, 2, 2), dom(range(10)),
	 *   "x[i][j][k][l][m] is something I can't reveal");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 5-dimensional array of integer variables
	 */
	default Var[][][][][] array(String id, Size5D size, Dom dom, String note, TypeClass... classes) {
		return array(id, size, (i, j, k, l, m) -> dom, note, classes);
	}

	/**
	 * Builds a 5-dimensional array of integer variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int,int,int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified
	 * integer domain. In the following example, all variables have a domain containing 10 values:
	 * 
	 * <pre>
	 * {@code Var[][][][][] = array("x", size(10, 5, 2, 2, 2), dom(range(10)));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param dom
	 *            the domain of each variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 5-dimensional array of integer variables
	 */
	default Var[][][][][] array(String id, Size5D size, Dom dom, TypeClass... classes) {
		return array(id, size, (i, j, k, l, m) -> dom, null, classes);
	}

	// ************************************************************************
	// ***** Special Constraints
	// ************************************************************************

	/**
	 * Builds a disentailed integer constraint, i.e., a special constraint that always returns {@code false}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by means of method chaining.
	 */
	default CtrEntity ctrFalse(Var[] scp) {
		return extension(scp, new int[0][], POSITIVE);
	}

	/**
	 * Builds an entailed integer constraint, i.e., a special constraint that always returns {@code true}. For example, it may be useful to achieve
	 * some sophisticated tasks related to some forms of consistency.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by means of method chaining.
	 */
	default CtrEntity ctrTrue(Var[] scp) {
		return extension(scp, new int[0][], NEGATIVE);
	}

	// ************************************************************************
	// ***** Constraint intension
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a> from the specified argument that represents the
	 * root of a syntactic tree. This method can be used with integer variables but also with symbolic variables (but currently, it is not allowed to
	 * have both integer and symbolic variables involved simultaneously). As an illustration,
	 * 
	 * <pre>
	 * {@code intension(eq(x,add(y,z)))}
	 * </pre>
	 * 
	 * allows us to post: <code> x = y+z </code>
	 * 
	 * @param tree
	 *            a syntactic tree
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining.
	 */
	default CtrEntity intension(XNodeParent<IVar> tree) {
		return imp().intension(tree);
	}

	/**
	 * Returns the root of a syntactic tree built with the unary operator <code>abs</code> applied to the specified operand. For example, one possible
	 * call is <code>abs(sub(x,y))</code> that represents <code>|x-y|</code>
	 * 
	 * @param operand
	 *            an object that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> abs(Object operand) {
		return XNodeParent.abs(operand);
	}

	/**
	 * Returns the root of a syntactic tree built with the unary operator <code>neg</code> applied to the specified operand. For example, one possible
	 * call is <code>neg(add(x,y))</code> that represents <code>-(x+y)</code>
	 * 
	 * @param operand
	 *            an object that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> neg(Object operand) {
		return XNodeParent.neg(operand);
	}

	/**
	 * Returns the root of a syntactic tree built with the unary operator <code>sqr</code> applied to the specified operand. For example, one possible
	 * call is <code>sqr(x)</code> that represents <code>x*x</code>
	 * 
	 * @param operand
	 *            an object that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> sqr(Object operand) {
		return XNodeParent.sqr(operand);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>add</code> applied to the specified operands. For example, one possible call
	 * is <code>add(x,y,z)</code> that represents <code>x+y+z</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> add(Object... operands) {
		return XNodeParent.add(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>sub</code> applied to the specified operands. For example, one
	 * possible call is <code>sub(x,y)</code> that represents <code>x-y</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> sub(Object operand1, Object operand2) {
		return XNodeParent.sub(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>mul</code> applied to the specified operands. For example, one possible call
	 * is <code>mul(x,y)</code> that represents <code>x*y</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> mul(Object... operands) {
		return XNodeParent.mul(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>div</code> applied to the specified operands. For example, one
	 * possible call is <code>div(x,y)</code> that represents <code>x/y</code> (integer division)
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> div(Object operand1, Object operand2) {
		return XNodeParent.div(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>mod</code> applied to the specified operands. For example, one
	 * possible call is <code>mod(x,2)</code> that represents <code>x%2</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> mod(Object operand1, Object operand2) {
		return XNodeParent.mod(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>pow</code> applied to the specified operands. For example, one
	 * possible call is <code>pow(x,3)</code> that represents <code>x^3</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> pow(Object operand1, Object operand2) {
		return XNodeParent.pow(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>min</code> applied to the specified operands. For example, one possible call
	 * is <code>min(x,y,z)</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> min(Object... operands) {
		return XNodeParent.min(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>max</code> applied to the specified operands. For example, one possible call
	 * is <code>max(x,y,z)</code>.
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> max(Object... operands) {
		return XNodeParent.max(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>dist</code> applied to the specified operands. For example, one
	 * possible call is <code>dist(x,y)</code> that represents <code>|x-y|</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> dist(Object operand1, Object operand2) {
		return XNodeParent.dist(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>lt</code> applied to the specified operands. For example, one
	 * possible call is <code>lt(x,10 </code> that represents <code>x<10</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> lt(Object operand1, Object operand2) {
		return XNodeParent.lt(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>le</code> applied to the specified operands. For example, one
	 * possible call is <code>le(x,10)</code> that represents <code>x&le;10</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> le(Object operand1, Object operand2) {
		return XNodeParent.le(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>ge</code> applied to the specified operands. For example, one
	 * possible call is <code>ge(x,10)</code> that represents <code>x&ge;10</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> ge(Object operand1, Object operand2) {
		return XNodeParent.ge(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>gt</code> applied to the specified operands. For example, one
	 * possible call is <code>gt(x,10)</code> that represents <code>x>10</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> gt(Object operand1, Object operand2) {
		return XNodeParent.gt(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>ne</code> applied to the specified operands. For example, one possible call
	 * is <code>ne(x,y)</code> that represents <code>x&ne;y</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> ne(Object... operands) {
		return XNodeParent.ne(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>eq</code> applied to the specified operands. For example, one possible call
	 * is <code>eq(x,y)</code> that represents <code>x=y</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> eq(Object... operands) {
		return XNodeParent.eq(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>set</code> applied to the specified operands. For example, one possible call
	 * is <code>set(1,2,3)</code> that represents <code>{1,2,3}</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNode<IVar> set(Object... operands) {
		return XNodeParent.set(operands);
	}

	/**
	 * Returns the node of a syntactic tree built with the operator <code>set</code> applied to the integers from the specified array. For example, if
	 * {@code t} id an array containing values 1, 2 and 3, then the call <code>set(t)</code> represents <code>{1,2,3}</code>
	 * 
	 * @param operands
	 *            an array of integers
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNode<IVar> set(int[] operands) {
		return XNodeParent.set(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>in</code> applied to the specified operands. For example, one possible call
	 * is <code>in(x,set(1,2,3))</code> that represents <code>x&in;{1,2,3}</code>
	 * 
	 * @param var
	 *            the first operand that is typically a variable but can also can be an object {@code XNode}
	 * @param set
	 *            the second operand that must have been built with the operator <code> set </code>
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> in(Object var, Object set) {
		return XNodeParent.in(var, set);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>notin</code> applied to the specified operands. For example, one possible
	 * call is <code>notin(x,set(1,2,3))</code> that represents <code>x&notin;{1,2,3}</code>
	 * 
	 * @param var
	 *            the first operand that is typically a variable but can also can be an object {@code XNode}
	 * @param set
	 *            the second operand that must have been built with the operator <code> set </code>
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> notin(Object var, Object set) {
		return XNodeParent.notin(var, set);
	}

	/**
	 * Returns the root of a syntactic tree built with the unary operator <code>not</code> applied to the specified operand. For example, one possible
	 * call is <code>not(eq(x,y))</code> that represents <code>&not;(x=y)</code>
	 * 
	 * @param operand
	 *            an object that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> not(Object operand) {
		return XNodeParent.not(operand);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>and</code> applied to the specified operands. For example, one possible call
	 * is <code>and(eq(x,y),lt(z,3))</code> that represents <code>x=y&and;z<3</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNodeParent<IVar> and(Object... operands) {
		return operands.length == 1 && operands[0] instanceof Stream ? XNodeParent.and(((Stream<?>) operands[0]).toArray()) : XNodeParent.and(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>or</code> applied to the specified operands. For example, one possible call
	 * is <code>or(eq(x,y),lt(z,3))</code> that represents <code>x=y&or;z<3</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNodeParent<IVar> or(Object... operands) {
		return operands.length == 1 && operands[0] instanceof Stream ? XNodeParent.or(((Stream<?>) operands[0]).toArray()) : XNodeParent.or(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>xor</code> applied to the specified operands. For example, one possible call
	 * is <code>xor(eq(x,y),lt(z,3))</code> that represents <code>x=y&xoplus;z<3</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNodeParent<IVar> xor(Object... operands) {
		return XNodeParent.xor(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the operator <code>iff</code> applied to the specified operands. For example, one possible call
	 * is <code>iff(eq(x,y),lt(z,3))</code> that represents <code>x=y&hArr;z<3</code>
	 * 
	 * @param operands
	 *            a sequence of operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code XNode} that represents the node of a syntactic tree
	 */
	default XNodeParent<IVar> iff(Object... operands) {
		return XNodeParent.iff(operands);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>imp</code> applied to the specified operands. For example, one
	 * possible call is <code>imp(eq(x,y),lt(z,3)</code> that represents <code>x=y&rArr;z<3</code>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> imp(Object operand1, Object operand2) {
		return XNodeParent.imp(operand1, operand2);
	}

	/**
	 * Returns the root of a syntactic tree built with the binary operator <code>if</code> applied to the specified operands. For example, one
	 * possible call is <code>if(eq(x,y),2,4)</code> that represents <code>x=y?2:4</code>
	 * 
	 * @param operand1
	 *            the first operand that must correspond to a Boolean expression (values 0 or 1)
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand3
	 *            the third operand that can be an integer, a variable, or an object {@code XNode}
	 * @return the root of a syntactic tree built from the specified operands
	 */
	default XNodeParent<IVar> ifThenElse(Object operand1, Object operand2, Object operand3) {
		return XNodeParent.ifThenElse(operand1, operand2, operand3);
	}

	/**
	 * Returns the root of a syntactic tree that represents the predicate ensuring that the specified variables are put in two cells of a flattened
	 * matrix (whose order is specified) at a knight distance.
	 * 
	 * @param x
	 *            a first variable
	 * @param y
	 *            a second variable
	 * @param order
	 *            the order of the matrix
	 * @return the root of a syntactic tree that represents the predicate ensuring that the specified variables are put in two distinct cells of a
	 *         flattened matrix (whose order is specified) at a knight distance
	 */
	default XNodeParent<IVar> knightAttack(IVar x, IVar y, int order) {
		XNodeParent<IVar> rowDist = dist(div(x, order), div(y, order));
		XNodeParent<IVar> colDist = dist(mod(x, order), mod(y, order));
		return or(and(eq(rowDist, 1), eq(colDist, 2)), and(eq(rowDist, 2), eq(colDist, 1)));
	}

	/**
	 * Returns the root of a syntactic tree that represents the predicate ensuring that the specified variables are put in two distinct cells of a
	 * flattened matrix (whose order is specified) on the same row, column or diagonal.
	 * 
	 * @param x
	 *            a first variable
	 * @param y
	 *            a second variable
	 * @param order
	 *            the order of the matrix
	 * @return the root of a syntactic tree that represents the predicate ensuring that the specified variables are put in two cells of a flattened
	 *         matrix (whose order is specified) at a knight distance
	 */
	default XNodeParent<IVar> queenAttack(IVar x, IVar y, int order) {
		XNodeParent<IVar> rowDist = dist(div(x, order), div(y, order));
		XNodeParent<IVar> colDist = dist(mod(x, order), mod(y, order));
		return and(ne(x, y), or(eq(mod(x, order), mod(y, order)), eq(div(x, order), div(y, order)), eq(rowDist, colDist)));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code lt} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code lessThan(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(lt(x,y));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity lessThan(Object operand1, Object operand2) {
		return intension(lt(operand1, operand2));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code le} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code lessEqual(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(le(x,y));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity lessEqual(Object operand1, Object operand2) {
		return intension(le(operand1, operand2));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code ge} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code greaterEqual(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(ge(x,y));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity greaterEqual(Object operand1, Object operand2) {
		return intension(ge(operand1, operand2));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code gt} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code greaterThan(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(gt(x,y));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity greaterThan(Object operand1, Object operand2) {
		return intension(gt(operand1, operand2));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code eq} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code equal(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(eq(x,y));}
	 * </pre>
	 * 
	 * @param operands
	 *            the operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity equal(Object... operands) {
		return intension(eq(operands));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code ne} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code different(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(ne(x,y));}
	 * </pre>
	 * 
	 * @param operands
	 *            the operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity different(Object... operands) {
		return intension(ne(operands)); // imp().different(operands);
	}

	@Deprecated
	/**
	 * Call {@code different} instead.
	 */
	default CtrEntity notEqual(Object... operands) {
		return different(operands);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code in} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code belong(x,set(1,2,3));}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(in(x,set(1,2,3)));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that must have been built with the operator <code>set</code>
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity belong(Object operand1, Object operand2) {
		return intension(in(operand1, operand2));
	}

	@Deprecated
	/**
	 * Call {@code implication} instead.
	 */
	default CtrEntity imply(Object operand1, Object operand2) {
		return implication(operand1, operand2);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code imp}
	 * applied to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code implication(eq(x,y),lt(z,3));}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(imp(eq(x,y),lt(z,3)));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity implication(Object operand1, Object operand2) {
		return intension(imp(operand1, operand2));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code iff}
	 * applied to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code equivalence(eq(x,y),lt(z,3));}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(iff(eq(x,y),lt(z,3)));}
	 * </pre>
	 * 
	 * @param operands
	 *            the operands that can be integers, variables, or objects {@code XNode}he operands that can be integers, variables, or objects
	 *            {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity equivalence(Object... operands) {
		return intension(iff(operands));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code add}
	 * applied to the specified arguments. This is a modeling ease of use.
	 * 
	 * @param operands
	 *            the operands that can be integers, variables, or objects {@code XNode}he operands that can be integers, variables, or objects
	 *            {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity conjunction(Object... operands) {
		return intension(and(operands));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code or} applied
	 * to the specified arguments. This is a modeling ease of use.
	 * 
	 * @param operands
	 *            the operands that can be integers, variables, or objects {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity disjunction(Object... operands) {
		return intension(or(operands));
	}

	/**
	 * Returns a stream of syntactic trees (predicates) built by applying the specified function to each variable of the specified array. {@code null}
	 * values in the array are discarded.
	 * 
	 * @param t
	 *            an array of variables
	 * @param f
	 *            a function mapping variables into syntactic trees (predicates)
	 * @return a stream of syntactic trees built by applying the specified function to each variable of the specified array
	 */
	default Stream<XNodeParent<IVar>> treesFrom(IVar[] t, Function<IVar, XNodeParent<IVar>> f) {
		return Stream.of(t).filter(x -> x != null).map(x -> f.apply(x));
	}

	/**
	 * Returns a stream of syntactic trees (predicates) built by applying the specified function to each integer of the specified stream.
	 * 
	 * @param stream
	 *            a stream of integers
	 * @param f
	 *            a function mapping integers into syntactic trees (predicates)
	 * @return a stream of syntactic trees built by applying the specified function to each integer of the specified stream
	 */
	default Stream<XNodeParent<IVar>> treesFrom(IntStream stream, Function<Integer, XNodeParent<IVar>> f) {
		return stream.mapToObj(x -> f.apply(x));
	}

	/**
	 * Returns a stream of syntactic trees (predicates) built by applying the specified function to each integer of the specified collection.
	 * {@code null} values in the collection are discarded.
	 * 
	 * @param c
	 *            a collection of integers
	 * @param f
	 *            a function mapping integers into syntactic trees (predicates)
	 * @return a stream of syntactic trees built by applying the specified function to each integer of the specified collection
	 */
	default Stream<XNodeParent<IVar>> treesFrom(Collection<Integer> c, Function<Integer, XNodeParent<IVar>> f) {
		return treesFrom(c.stream().filter(x -> x != null).mapToInt(i -> i), f);
	}

	/**
	 * Returns a stream of syntactic trees (predicates) built by applying the specified function to each integer of the specified array.
	 * 
	 * @param t
	 *            an array of integers
	 * @param f
	 *            a function mapping integers into syntactic trees (predicates)
	 * @return a stream of syntactic trees built by applying the specified function to each integer of the specified array
	 */
	default Stream<XNodeParent<IVar>> treesFrom(int[] t, Function<Integer, XNodeParent<IVar>> f) {
		return treesFrom(IntStream.of(t), f);
	}

	/**
	 * Returns a stream of syntactic trees (predicates) built by applying the specified function to each integer of the specified range.
	 * 
	 * @param r
	 *            a range
	 * @param f
	 *            a function mapping integers into syntactic trees (predicates)
	 * @return a stream of syntactic trees built by applying the specified function to each integer of the specified range
	 */
	default Stream<XNodeParent<IVar>> treesFrom(Range r, Function<Integer, XNodeParent<IVar>> f) {
		return treesFrom(r.stream(), f);
	}

	// default CtrEntity post(Object leftOperand, String operator, Object rightOperand) {
	// if (operator.equals("!="))
	// return different(leftOperand, rightOperand);
	// return null;
	// }

	// ************************************************************************
	// ***** Converting intension to extension
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified syntactic tree (predicate). The
	 * specified intentional constraint is converted in extensional form.
	 * 
	 * @param tree
	 *            the root of a syntactic tree (predicate)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrAlone extension(XNodeParent<IVar> tree) {
		return imp().extension(tree);
	}

	default CtrAlone extensionDisjunction(List<XNodeParent<IVar>> trees) {
		// System.out.println("One " + Stream.of(trees).map(t -> t.toString()).collect(Collectors.joining(" ")));
		return imp().extensionDisjunction(trees);
	}

	default CtrAlone extensionDisjunction(XNodeParent<IVar>... trees) {
		return extensionDisjunction(Arrays.asList(trees));
	}
	// ************************************************************************
	// ***** Constraint extension
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the specified array
	 * of tuples, seen as either supports (when {@code positive} is {@code true}) or conflicts (when {@code positive} is {@code false}). Note that you
	 * can use constants {@code POSITIVE} and {@code NEGATIVE}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the semantics of the constraint
	 * @param positive
	 *            boolean value indicating if the tuples are supports or conflicts
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var[] scp, int[][] tuples, Boolean positive) {
		return imp().extension(scp, tuples, positive);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the specified array
	 * of tuples, seen as supports.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the supports of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var[] scp, int[]... tuples) {
		return extension(scp, tuples, POSITIVE);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the specified
	 * collection of tuples, seen as either supports (when {@code positive} is {@code true}) or conflicts (when {@code positive} is {@code false}).
	 * Note that you can use constants {@code POSITIVE} and {@code NEGATIVE}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the semantics of the constraint
	 * @param positive
	 *            boolean value indicating if the tuples are supports or conflicts
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var[] scp, Collection<int[]> tuples, Boolean positive) {
		return extension(scp, tuples.toArray(new int[0][]), positive);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the specified
	 * collection of tuples, seen as supports.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the supports of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var[] scp, Collection<int[]> tuples) {
		return extension(scp, tuples, POSITIVE);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the specified table,
	 * whose elements are seen as supports. An example of integer table that can be constructed is
	 * 
	 * <pre>
	 * {@code table("(1,2,3)(2,1,1)")}
	 * </pre>
	 * 
	 * For a negative table, you can write:
	 * 
	 * <pre>
	 * {@code table("(1,2,3)(2,1,1)").positive(false)}
	 * </pre>
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param table
	 *            the table containing the tuples defining the supports of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var[] scp, Table table) {
		return extension(scp, table.toArray(), table.positive);
	}

	/**
	 * Builds a unary constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified variable and the
	 * specified array of values, seen as either supports (when {@code positive} is {@code true}) or conflicts (when {@code positive} is
	 * {@code false}). Note that you can use constants {@code POSITIVE} and {@code NEGATIVE}.
	 * 
	 * @param x
	 *            the variable involved in this unary constraint
	 * @param values
	 *            the values defining the semantics of the constraint
	 * @param positive
	 *            boolean value indicating if the values are supports or conflicts
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var x, int[] values, Boolean positive) {
		return extension(vars(x), dub(values), positive);
	}

	/**
	 * Builds a unary constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified variable and the
	 * specified array of values, seen as supports.
	 * 
	 * @param x
	 *            the variable involved in this unary constraint
	 * @param values
	 *            the values defining the semantics of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var x, int... values) {
		return extension(x, values, POSITIVE);
	}

	/**
	 * Builds a unary constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified variable and the
	 * specified table.
	 * 
	 * @param x
	 *            the variable involved in this unary constraint
	 * @param table
	 *            the table defining the semantics of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(Var x, Table table) {
		return extension(vars(x), table.toArray(), table.positive);
	}

	// ************************************************************************
	// ***** Constraint regular
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/regular">{@code regular}</a> from the specified scope and the specified automaton.
	 * Note that an automaton can be built as in the following example:
	 * 
	 * <pre>
	 * {@code Transitions transitions = Transitions.parse("(q0,0,q1)(q0,2,q2)(q1,0,q3)(q2,2,q3)"); 
	 * Automaton automata = new Automaton(transitions, "q0", tuple("q2", "q3"));)}
	 * </pre>
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param automaton
	 *            the automaton defining the semantics of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity regular(Var[] scp, Automaton automaton) {
		return imp().regular(scp, automaton);
	}

	// ************************************************************************
	// ***** Constraint mdd
	// ************************************************************************

	default CtrEntity mdd(Var[] scp, Transitions transitions) {
		return imp().mdd(scp, transitions.toArray());
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/mdd">{@code mdd}</a> from the specified scope and the specified transitions. Note
	 * that transitions can be built as in the following example:
	 * 
	 * <pre>
	 * <code>
	 * 	Transitions transitions = Transitions.parse("(q0,0,q1)(q0,2,q2)(q1,0,q3)(q2,2,q3)");
	 * </code>
	 * </pre>
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param transitions
	 *            the transitions defining the MDD, and consequently the semantics of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity mdd(Var[] scp, Transition[] transitions) {
		return imp().mdd(scp, transitions);
	}

	// ************************************************************************
	// ***** Constraint allDifferent
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified integer variables: the
	 * variables must all take different values.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Var[] list) {
		// return imp().allDifferent(imp().distinct(list));
		return imp().allDifferent(imp().distinctSorted(list));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified integer variables: the
	 * variables must all take different values.
	 * 
	 * @param x
	 *            a first integer variable
	 * @param others
	 *            a sequence of other integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Var x, Var... others) {
		return allDifferent((Var[]) vars(x, (Object) others));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified integer variables: the
	 * variables must all take different values. Note that the specified 2-dimensional array of variables will be flattened (i.e., converted into a
	 * 1-dimensional array of variables). Do not mistake this form with {@code allDifferentList}
	 * 
	 * @param list
	 *            the involved integer variables (a 2-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Var[]... list) {
		return allDifferent(vars(list));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified integer variables: the
	 * variables must all take different values. Note that the specified 3-dimensional array of variables will be flattened (i.e., converted into a
	 * 1-dimensional array of variables).
	 * 
	 * @param list
	 *            the involved integer variables (a 3-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Var[][][] list) {
		return allDifferent(vars(list));
	}

	@Deprecated
	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferentExcept}</a> on the specified integer variables:
	 * the variables must take different values, except those that take one of the specified 'exceptionnal' values.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @param exceptValues
	 *            the values that must be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferentExcept(Var[] list, int... exceptValues) {
		return allDifferent(list, exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified integer variables: the
	 * variables must take different values, except those that take one of the specified 'exceptionnal' values.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @param exceptValues
	 *            the values that must be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Var[] list, int... exceptValues) {
		return imp().allDifferent(list, exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferentList}</a> on the specified lists of variables: all
	 * tuples formed by the different lists must be different.
	 * 
	 * @param lists
	 *            a 2-dimensional array of variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferentList(Var[]... lists) {
		return imp().allDifferentList(lists);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferentMatrix}</a> on the specified matrix of variables.
	 * On each row and on each column, the variables must all take different values.
	 * 
	 * @param matrix
	 *            a 2-dimensional array of variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferentMatrix(Var[][] matrix) {
		return imp().allDifferentMatrix(matrix);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified syntactic trees
	 * (predicates): the predicates, when evaluated, must all take different values.
	 * 
	 * @param trees
	 *            an array of syntactic trees (predicates)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(XNodeParent<IVar>[] trees) {
		return imp().allDifferent(trees);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified (stream of) syntactic trees
	 * (predicates): the predicates, when evaluated, must all take different values.
	 * 
	 * @param trees
	 *            a stream of syntactic trees (predicates)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(Stream<XNodeParent<IVar>> trees) {
		XNodeParent<IVar>[] atrees = trees.toArray(XNodeParent[]::new);
		return imp().allDifferent(atrees);
	}

	// ************************************************************************
	// ***** Constraint allEqual
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allEqual">{@code allEqual}</a> on the specified integer variables: the variables
	 * must all take the same value. Basically, this is a modeling ease of use.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allEqual(Var... list) {
		return imp().allEqual(list);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allEqual">{@code allEqual}</a> on the specified integer variables: the variables
	 * must all take the same value. Basically, this is a modeling ease of use. Note that the specified 2-dimensional array of variables will be
	 * flattened (i.e., converted into a 1-dimensional array of variables). Do not mistake this form with {@code allEqualList}
	 * 
	 * @param list
	 *            the involved integer variables (a 2-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allEqual(Var[][] list) {
		return allEqual(vars(list));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allEqual">{@code allEqualList}</a> on the specified lists of variables: all tuples
	 * formed by the different lists must be equal.
	 * 
	 * @param lists
	 *            a 2-dimensional array of variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allEqualList(Var[]... lists) {
		return imp().allEqualList(lists);
	}

	// ************************************************************************
	// ***** Constraint ordered and lex
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables: any two
	 * successive variables must respect the specified operator, while considering the specified lengths. We have:
	 * 
	 * <pre>
	 * {@code for any i in 0..list.length-1, list[i] + lengths[i] <op> list[i+1]}
	 * </pre>
	 * 
	 * In general, the size of {@code lengths} is the size of {@code list} minus 1. But, for simplicity, it is authorized to have the size of
	 * {@code lengths} being equal to that of {@code list}, in which case the last value of the integer array is simply ignored.
	 * 
	 * Basically, this constraint is a modeling ease of use.
	 * 
	 * @param list
	 *            the involved integer variables
	 * 
	 * @param lengths
	 *            the lengths used
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ordered(Var[] list, int[] lengths, TypeOperatorRel operator) {
		control(list.length == lengths.length || list.length == lengths.length + 1, "The size of list must be the size of lengths (possibly, plus 1)");
		return imp().ordered(list, list.length == lengths.length ? Arrays.copyOf(lengths, list.length - 1) : lengths, operator);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables: any two
	 * successive variables must respect the specified operator, while considering the specified lengths. We have:
	 * 
	 * <pre>
	 * {@code for any i in 0..list.length-1, list[i] + lengths[i] <op> list[i+1]}
	 * </pre>
	 * 
	 * In general, the size of {@code lengths} is the size of {@code list} minus 1. But, for simplicity, it is authorized to have the size of
	 * {@code lengths} being equal to that of {@code list}, in which case the last value of the integer array is simply ignored.
	 * 
	 * Basically, this constraint is a modeling ease of use.
	 * 
	 * @param list
	 *            the involved integer variables
	 * 
	 * @param lengths
	 *            the lengths used
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ordered(Var[] list, Var[] lengths, TypeOperatorRel operator) {
		control(list.length == lengths.length || list.length == lengths.length + 1, "The size of list must be the size of lengths (possibly, plus 1)");
		return imp().ordered(list, list.length == lengths.length ? Arrays.copyOf(lengths, list.length - 1) : lengths, operator);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables: any two
	 * successive variables must respect the specified operator. Basically, this is a modeling ease of use.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ordered(Var[] list, TypeOperatorRel operator) {
		return ordered(list, new int[list.length - 1], operator);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables, while considering
	 * a strict increasing order. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code strictlyIncreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,STRICTLY_INCREASING);}
	 * </pre>
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity strictlyIncreasing(Var... list) {
		return ordered(list, STRICTLY_INCREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables, while considering
	 * an increasing order. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code increasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,INCREASING);}
	 * </pre>
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity increasing(Var... list) {
		return ordered(list, INCREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables, while considering
	 * a decreasing order. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code decreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,DECREASING);}
	 * </pre>
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity decreasing(Var... list) {
		return ordered(list, DECREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code ordered}</a> on the specified lists of variables, while considering
	 * a strict decreasing order. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code strictlydecreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,STRICTLY_DeCREASING);}
	 * </pre>
	 * 
	 * @param list
	 *            the involved integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity strictlyDecreasing(Var... list) {
		return ordered(list, STRICTLY_DECREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lex}</a> on the specified 2-dimensional array of variables: any two
	 * successive rows of variables must respect the specified operator. Basically, this is a modeling ease of use.
	 * 
	 * @param lists
	 *            a 2-dimensional array of integer variables
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity lex(Var[][] lists, TypeOperatorRel operator) {
		return imp().lex(lists, operator);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lex}</a> on the specified 2-dimensional array of variables, while
	 * considering a strict increasing order on rows. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code strictlyIncreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,STRICTLY_INCREASING);}
	 * </pre>
	 * 
	 * @param lists
	 *            a 2-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity strictlyIncreasing(Var[]... lists) {
		return lex(lists, STRICTLY_INCREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lex}</a> on the specified 2-dimensional array of variables, while
	 * considering an increasing order on rows. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code increasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,INCREASING);}
	 * </pre>
	 * 
	 * @param lists
	 *            a 2-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity increasing(Var[]... lists) {
		return lex(lists, INCREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lex}</a> on the specified 2-dimensional array of variables, while
	 * considering a decreasing order on rows. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code decreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,DeCREASING);}
	 * </pre>
	 * 
	 * @param lists
	 *            a 2-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity decreasing(Var[]... lists) {
		return lex(lists, DECREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lex}</a> on the specified 2-dimensional array of variables, while
	 * considering a strict decreasing order on rows. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code strictlyDecreasing(x);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code ordered(x,STRICTLY_DECREASING);}
	 * </pre>
	 * 
	 * @param lists
	 *            a 2-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity strictlyDecreasing(Var[]... lists) {
		return lex(lists, STRICTLY_DECREASING);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/ordered">{@code lexMatrix}</a> on the specified matrix of variables. Any two
	 * successive rows of variables and any two successive columns of variables must respect the specified operator.
	 *
	 * @param matrix
	 *            a 2-dimensional array of integer variables
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity lexMatrix(Var[][] matrix, TypeOperatorRel operator) {
		return imp().lexMatrix(matrix, operator);
	}

	// ************************************************************************
	// ***** Constraint sum
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the specified condition.
	 *
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, int[] coeffs, Condition condition) {
		return imp().sum(list, coeffs, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the sum must respect the
	 * specified condition.
	 *
	 * @param list
	 *            the summed variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Condition condition) {
		Var[] t = clean(list);
		return sum(t, repeat(1, t.length), condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,t,EQ,10);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, int[] coeffs, TypeConditionOperatorRel op, long limit) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit. The coefficients are given under the form of a range. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code sum(x,range(5),EQ,10);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the range of values used as coefficients for the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Range coeffs, TypeConditionOperatorRel op, long limit) {
		return sum(list, vals(coeffs), op, limit);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,EQ,10);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, TypeConditionOperatorRel op, long limit) {
		return sum(list, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,t,GT,z);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, int[] coeffs, TypeConditionOperatorRel op, Var limit) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,GT,z);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, TypeConditionOperatorRel op, Var limit) {
		return sum(list, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified set operator and the specified interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,t,IN,range(1,4));}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, int[] coeffs, TypeConditionOperatorSet op, Range range) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified set operator and the specified interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, TypeConditionOperatorSet op, Range range) {
		return sum(list, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,t,NOTIN,vals(1,3,5,6));}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, int[] coeffs, TypeConditionOperatorSet op, int[] set) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,NOTIN,vals(1,3,5,6));}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, TypeConditionOperatorSet op, int[] set) {
		return sum(list, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the specified condition.
	 *
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Var[] coeffs, Condition condition) {
		return imp().sum(list, coeffs, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,y,LE,100);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Var[] coeffs, TypeConditionOperatorRel op, long limit) {
		control(list.length == coeffs.length,
				"Pb because the number of variables is different form the number of coefficients: " + list.length + " vs " + coeffs.length);
		return sum(list, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,y,LE,z);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Var[] coeffs, TypeConditionOperatorRel op, Var limit) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified set operator and the specified interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,y,IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Var[] coeffs, TypeConditionOperatorSet op, Range range) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code sum(x,y,NOTIN,vals(1,3,5,6));}
	 * </pre>
	 * 
	 * @param list
	 *            the summed variables
	 * @param coeffs
	 *            the coefficients associated with the summed variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Var[] list, Var[] coeffs, TypeConditionOperatorSet op, int[] set) {
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
		return sum(list, coeffs, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the specified condition.
	 *
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, Condition condition) {
		return imp().sum(trees, coeffs == null ? repeat(1, trees.length) : coeffs, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the specified condition.
	 *
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, Condition condition) {
		return sum(trees, null, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, TypeConditionOperatorRel op, long limit) {
		return sum(trees, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, TypeConditionOperatorRel op, long limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            an array of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the specified condition.
	 *
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, int[] coeffs, Condition condition) {
		XNodeParent<IVar>[] atrees = trees.toArray(XNodeParent[]::new);
		return sum(atrees, coeffs, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the specified condition.
	 *
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, Condition condition) {
		return sum(trees, null, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, TypeConditionOperatorRel op, long limit) {
		return sum(trees, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, int[] coeffs, TypeConditionOperatorRel op, long limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the (simple) sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/sum">{@code sum}</a> from the specified arguments: the weighted sum must respect
	 * the condition expressed by the specified operator and the specified limit.
	 * 
	 * @param trees
	 *            a stream of syntactic trees (usually, predicates)
	 * @param coeffs
	 *            the coefficients associated with the syntactic trees
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the sum is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, int[] coeffs, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	// ************************************************************************
	// ***** Constraint count
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the specified condition.
	 *
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int[] values, Condition condition) {
		return imp().count(clean(list), values, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the condition expressed by the specified operator and the specified
	 * limit. As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,vals(0,1),LE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int[] values, TypeConditionOperatorRel op, int limit) {
		return count(list, values, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,0,LE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int value, TypeConditionOperatorRel op, int limit) {
		return count(list, vals(value), condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the condition expressed by the specified operator and the specified
	 * limit. As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,vals(0,1),LE,z);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int[] values, TypeConditionOperatorRel op, Var limit) {
		return count(list, values, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,0,LE,z);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int value, TypeConditionOperatorRel op, Var limit) {
		return count(list, vals(value), condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the condition expressed by the specified set operator and the specified
	 * interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,vals(0,1),IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int[] values, TypeConditionOperatorSet op, Range range) {
		return count(list, values, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value must respect the condition expressed by the specified set operator and the specified interval
	 * (range). As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,0,IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int value, TypeConditionOperatorSet op, Range range) {
		return count(list, vals(value), condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the condition expressed by the specified set operator and the specified
	 * set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,vals(0,1),IN,t);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int[] values, TypeConditionOperatorSet op, int[] set) {
		return count(list, values, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value must respect the condition expressed by the specified set operator and the specified set of
	 * values. As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,1,IN,t);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, int value, TypeConditionOperatorSet op, int[] set) {
		return count(list, vals(value), condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the values must respect the specified condition.
	 *
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var[] values, Condition condition) {
		return imp().count(clean(list), clean(values), condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the values must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,LE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var[] values, TypeConditionOperatorRel op, int limit) {
		return count(list, values, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the assigned value must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,LE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var value, TypeConditionOperatorRel op, int limit) {
		return count(list, vars(value), condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the values must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,LE,z);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var[] values, TypeConditionOperatorRel op, Var limit) {
		return count(list, values, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the assigned value must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,LE,z);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the count is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var value, TypeConditionOperatorRel op, Var limit) {
		return count(list, vars(value), condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the values must respect the condition expressed by the specified set operator and the specified interval
	 * (range). As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var[] values, TypeConditionOperatorSet op, Range range) {
		return count(list, values, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the value must respect the condition expressed by the specified set operator and the specified interval (range).
	 * As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,IN,1,4);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var value, TypeConditionOperatorSet op, Range range) {
		return count(list, vars(value), condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values must respect the condition expressed by the specified set operator and the specified
	 * set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,IN,t);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var[] values, TypeConditionOperatorSet op, int[] set) {
		return count(list, values, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the value must respect the condition expressed by the specified set operator and the specified set of values. As
	 * an illustration,
	 * 
	 * <pre>
	 * {@code count(x,y,IN,t);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity count(Var[] list, Var value, TypeConditionOperatorSet op, int[] set) {
		return count(list, vars(value), condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at least equal to {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code atLeast(x,0,5);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,GE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atLeast(Var[] list, int value, int k) {
		return count(list, value, GE, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at most equal to {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code atMost(x,0,5);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,LE,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atMost(Var[] list, int value, int k) {
		return count(list, value, LE, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is exactly {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code exactly(x,0,5);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,EQ,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity exactly(Var[] list, int value, int k) {
		return count(list, value, EQ, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values is exactly {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code among(x,vals(2,3,4),5);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,vals(2,3,4),EQ,5);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param k
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity among(Var[] list, int[] values, int k) {
		return count(list, values, EQ, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at least equal to {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code atLeast(x,0,k);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,GE,k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atLeast(Var[] list, int value, Var k) {
		return count(list, value, GE, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at most equal to {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code atMost(x,0,k);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,LE,k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atMost(Var[] list, int value, Var k) {
		return count(list, value, LE, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is exactly {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code exactly(x,0,k);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,0,EQ,k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @param k
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity exactly(Var[] list, int value, Var k) {
		return count(list, value, EQ, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take one of the specified values is exactly {@code k}. As an illustration,
	 * 
	 * <pre>
	 * {@code among(x,vals(2,3,4),k);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,vals(2,3,4),EQ,k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param k
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity among(Var[] list, int[] values, Var k) {
		return count(list, values, EQ, k);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at least 1. As an illustration,
	 * 
	 * <pre>
	 * {@code atLeast1(x,2);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,2,GE,1);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atLeast1(Var[] list, int value) {
		return atLeast(list, value, 1);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is at most 1. As an illustration,
	 * 
	 * <pre>
	 * {@code atMost1(x,2);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,2,LE,1);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity atMost1(Var[] list, int value) {
		return atMost(list, value, 1);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/count">{@code count}</a> from the specified arguments: the number of variables in
	 * the specified list that take the specified value is exactly 1. As an illustration,
	 * 
	 * <pre>
	 * {@code exactly1(x,2);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code count(x,2,EQ,1);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity exactly1(Var[] list, int value) {
		return exactly(list, value, 1);
	}

	// ************************************************************************
	// ***** Constraint nValues
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values taken by variables of the specified list must respect the specified condition.
	 *
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, Condition condition) {
		return imp().nValues(list, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values taken by variables of the specified list must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code nValues(x,GE,3);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the number is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorRel op, int limit) {
		return nValues(list, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values taken by variables of the specified list must respect the condition expressed by the specified operator and the specified limit. As an
	 * illustration,
	 * 
	 * <pre>
	 * {@code nValues(x,GE,k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the number is compared
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorRel op, Var limit) {
		return nValues(list, condition(op, limit));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values taken by variables of the specified list must respect the condition expressed by the specified set operator and the specified interval
	 * (range). As an illustration,
	 * 
	 * <pre>
	 * {@code nValues(x,IN,range(1,3));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorSet op, Range range) {
		return nValues(list, condition(op, range));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values taken by variables of the specified list must respect the condition expressed by the specified set operator and the specified set of
	 * values. As an illustration,
	 * 
	 * <pre>
	 * {@code nValues(x,IN,vals(2,3,4));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorSet op, int[] set) {
		return nValues(list, condition(op, set));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values that are taken by variables of the specified list and that do not occur among those specified must respect the specified condition.
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, Condition condition, int... exceptValues) {
		return imp().nValues(list, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values that are taken by variables of the specified list and that do not occur among those specified must respect the condition expressed by
	 * the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code nValues(x,GT,3,exceptValue(0));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the number is compared
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorRel op, int limit, int... exceptValues) {
		return nValues(list, condition(op, limit), exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values that are taken by variables of the specified list and that do not occur among those specified must respect the condition expressed by
	 * the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,GT,k,exceptValue(0));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a relational operator (LT, LE, GE, GT, NE, or EQ)
	 * @param limit
	 *            the right operand to which the number is compared
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorRel op, Var limit, int... exceptValues) {
		return nValues(list, condition(op, limit), exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values that are taken by variables of the specified list and that do not occur among those specified must respect the condition expressed by
	 * the specified operator and the specified interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,IN,range(1,3),exceptValue(0));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param range
	 *            a range (interval) of values involved in the comparison
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorSet op, Range range, int... exceptValues) {
		return nValues(list, condition(op, range), exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: the number of distinct
	 * values that are taken by variables of the specified list and that do not occur among those specified must respect the condition expressed by
	 * the specified operator and the specified set of values. As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,IN,vals(1,3,5),exceptValue(0));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param op
	 *            a set operator (IN or NOTIN)
	 * @param set
	 *            the set of values involved in the comparison
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValues(Var[] list, TypeConditionOperatorSet op, int[] set, int... exceptValues) {
		return nValues(list, condition(op, set), exceptValues);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/nValues">{@code nValues}</a> from the specified arguments: at least two distinct
	 * values are assigned to the variables of the specified list.
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity notAllEqual(Var... list) {
		return nValues(list, condition(GT, 1));
	}

	// ************************************************************************
	// ***** Constraint cardinality
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurExactly(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occursEachBetween(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurBetween(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurExactly(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
	 * </ul>
	 * Note that when the specified boolean is {@code true}, it is required that all variables in {@code list} are assigned a value in {@code values}.
	 * <br>
	 * 
	 * As an illustration, enforcing values 1 and 2 to occur exactly 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),true,occursEachExactly(5));}
	 * </pre>
	 * 
	 * Enforcing values 1 and 2 to occur between 3 and 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),true,occursEachBetween(3,5));}
	 * </pre>
	 * 
	 * Enforcing values 1 and 2 to occur exactly y1 and y2 times, with y1 and y2 two variables, each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),true,occurExactly(y1,y2));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param mustBeClosed
	 *            {@code true} iff all variables of {@code list} must necessarily be assigned to a value in {@code values}
	 * @param occurrences
	 *            an object {@code Occurrences}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cardinality(Var[] list, int[] values, boolean mustBeClosed, Occurrences occurrences) {
		// controls to be added
		if (occurrences instanceof OccurrencesInt)
			return imp().cardinality(clean(list), values, mustBeClosed, repeat(((OccurrencesInt) occurrences).occurs, values.length));
		if (occurrences instanceof OccurrencesInt1D)
			return imp().cardinality(clean(list), values, mustBeClosed, ((OccurrencesInt1D) occurrences).occurs);
		if (occurrences instanceof OccurrencesIntRange)
			return imp().cardinality(clean(list), values, mustBeClosed, repeat(((OccurrencesIntRange) occurrences).occursMin, values.length),
					repeat(((OccurrencesIntRange) occurrences).occursMax, values.length));
		if (occurrences instanceof OccurrencesIntRange1D)
			return imp().cardinality(clean(list), values, mustBeClosed, ((OccurrencesIntRange1D) occurrences).occursMin,
					((OccurrencesIntRange1D) occurrences).occursMax);
		// if (occurrences instanceof OccurrencesVar)
		return imp().cardinality(clean(list), values, mustBeClosed, clean(((OccurrencesVar1D) occurrences).occurs));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurExactly(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occursEachBetween(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurBetween(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurExactly(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
	 * </ul>
	 * 
	 * As an illustration, enforcing values 1 and 2 to occur exactly 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),occursEachExactly(5));}
	 * </pre>
	 * 
	 * Enforcing values 1 and 2 to occur between 3 and 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),occursEachBetween(3,5));}
	 * </pre>
	 * 
	 * Enforcing values 1 and 2 to occur exactly y1 and y2 times, with y1 and y2 two variables, each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,vals(1,2),occurExactly(y1,y2));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param occurrences
	 *            an object {@code Occurrences}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cardinality(Var[] list, int[] values, Occurrences occurrences) {
		return cardinality(list, values, false, occurrences);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in the range {@code values} at index {@code i} must
	 * occur a number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as
	 * follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurExactly(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occursEachBetween(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurBetween(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurExactly(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
	 * </ul>
	 * 
	 * As an illustration, enforcing values 1, 2, 3 and 4 to occur exactly 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,range(1,4),occursEachExactly(5));}
	 * </pre>
	 * 
	 * Enforcing values 1, 2, 3 and 4 to occur between 3 and 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,range(1,4),occursEachBetween(3,5));}
	 * </pre>
	 * 
	 * Enforcing values 1, 2, 3 and 4 to occur exactly y[i] times, with y an array of variables and i the index of each value in turn, each is given
	 * by:
	 * 
	 * <pre>
	 * {@code cardinality(x,range(1,4),occurExactly(y));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a range of integers
	 * @param occurrences
	 *            an object {@code Occurrences}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cardinality(Var[] list, Range values, Occurrences occurrences) {
		return cardinality(list, vals(values), occurrences);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurExactly(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occursEachBetween(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurBetween(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurExactly(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
	 * </ul>
	 * Note that when the specified boolean is {@code true}, it is required that all variables in {@code list} are assigned a value in {@code values}.
	 * <br>
	 * 
	 * As an illustration, enforcing values from array y to occur exactly 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,true,occursEachExactly(5));}
	 * </pre>
	 * 
	 * Enforcing values from array y to occur between 3 and 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,true,occursEachBetween(3,5));}
	 * </pre>
	 * 
	 * Enforcing value y[i] to occur exactly z[i] times is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,true,occurExactly(z));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param mustBeClosed
	 *            {@code true} iff all variables of {@code list} must necessarily be assigned to a value in {@code values}
	 * @param occurrences
	 *            an object {@code Occurrences}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cardinality(Var[] list, Var[] values, boolean mustBeClosed, Occurrences occurrences) {
		// controls to be added
		if (occurrences instanceof OccurrencesInt)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, repeat(((OccurrencesInt) occurrences).occurs, values.length));
		if (occurrences instanceof OccurrencesInt1D)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, ((OccurrencesInt1D) occurrences).occurs);
		if (occurrences instanceof OccurrencesIntRange)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, repeat(((OccurrencesIntRange) occurrences).occursMin, values.length),
					repeat(((OccurrencesIntRange) occurrences).occursMax, values.length));
		if (occurrences instanceof OccurrencesIntRange1D)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, ((OccurrencesIntRange1D) occurrences).occursMin,
					((OccurrencesIntRange1D) occurrences).occursMax);
		// if (occurrences instanceof OccurrencesVar)
		return imp().cardinality(clean(list), clean(values), mustBeClosed, clean(((OccurrencesVar1D) occurrences).occurs));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurExactly(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occursEachBetween(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurBetween(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurExactly(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
	 * </ul>
	 * 
	 * As an illustration, enforcing values from array y to occur exactly 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,occursEachExactly(5));}
	 * </pre>
	 * 
	 * Enforcing values from array y to occur between 3 and 5 times each is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,occursEachBetween(3,5));}
	 * </pre>
	 * 
	 * Enforcing value y[i] to occur exactly z[i] times is given by:
	 * 
	 * <pre>
	 * {@code cardinality(x,y,occurrences(z));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param values
	 *            a 1-dimensional array of integer variables
	 * @param occurs
	 *            an object {@code Occurrences}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cardinality(Var[] list, Var[] values, Occurrences occurs) {
		return cardinality(list, values, false, occurs);
	}

	// ************************************************************************
	// ***** Constraint maximum
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must respect the specified condition. Note that the array {@code list} is cleaned (i.e.,
	 * {@code null} values are discarded). <br>
	 * As an illustration, enforcing the maximum value of x to be strictly less than 10 is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,condition(LT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code Condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Condition condition) {
		return imp().maximum(clean(list), condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be equal to the value assigned to the variable {@code value}. Note that the array
	 * {@code list} is cleaned (i.e., {@code null} values are discarded). <br>
	 * As an illustration, enforcing the maximum value of x to be m is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,m);}
	 * </pre>
	 * 
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Var value) {
		return maximum(list, condition(EQ, value));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with maximum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with maximum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with maximum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 0) with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 0) with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Index index) {
		return imp().maximum(list, 0, index.var, index.rank);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable} and
	 * besides this value must respect the specified condition. Note that {@code startIndex.value} indicates the number used to access the first
	 * variable in {@code list} whereas {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with maximum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with maximum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with maximum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)}, for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)} and for building an object {@code Condition} use methods {@code condition()}.
	 * <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with maximum value strictly greater than 10
	 * is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,startIndex(1),index(i),condition(GT,10));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with maximum value strictly greater than 10 is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,startIndex(10),index(i,FIRST),condition(GT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, int startIndex, Index index, Condition condition) {
		return imp().maximum(list, startIndex, index.var, index.rank, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable} and
	 * besides this value must respect the specified condition. Note that indexing starts at 0 and that {@code index.rank} indicates if
	 * {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with maximum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with maximum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with maximum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)} and for building an
	 * object {@code Condition} use methods {@code condition()}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with maximum value strictly greater
	 * than 10 is given by:
	 * 
	 * <pre>
	 * {@code maximum(x, at(i),condition(GT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Var index, Condition condition) {
		return maximum(list, 0, index(index), condition);
	}

	// ************************************************************************
	// ***** Constraint minimum
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must respect the specified condition. Note that the array {@code list} is cleaned (i.e.,
	 * {@code null} values are discarded). <br>
	 * As an illustration, enforcing the minimum value of x to be strictly less than 10 is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,condition(LT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code Condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Condition condition) {
		return imp().minimum(list, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be equal to the value assigned to the variable {@code value}. Note that the array
	 * {@code list} is cleaned (i.e., {@code null} values are discarded). <br>
	 * As an illustration, enforcing the minimum value of x to be m is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,m);}
	 * </pre>
	 * 
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Var value) {
		return minimum(list, condition(EQ, value));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with minimum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with minimum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with minimum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 0) with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 0) with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Index index) {
		return imp().minimum(list, 0, index.var, index.rank);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable} and
	 * besides this value must respect the specified condition. Note that {@code startIndex.value} indicates the number used to access the first
	 * variable in {@code list} whereas {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with minimum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with minimum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with minimum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)}, for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)} and for building an object {@code Condition} use methods {@code condition()}.
	 * <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with minimum value strictly greater than 10
	 * is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,startIndex(1),index(i),condition(GT,10));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with minimum value strictly greater than 10 is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,startIndex(10),index(i,FIRST),condition(GT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, int startIndex, Index index, Condition condition) {
		return imp().minimum(list, startIndex, index.var, index.rank, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable} and
	 * besides this value must respect the specified condition. Note that indexing starts at 0 and that {@code index.rank} indicates if
	 * {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with minimum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with minimum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with minimum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)} and for building an
	 * object {@code Condition} use methods {@code condition()}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with minimum value strictly greater
	 * than 10 is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i),condition(GT,10));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing starting at 0, by default) with minimum value strictly greater than 10 is
	 * given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i,FIRST),condition(GT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Var index, Condition condition) {
		return minimum(list, 0, index(index), condition);
	}

	// ************************************************************************
	// ***** Constraint element
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be assigned to at least one of the specified variables. <br>
	 * 
	 * As an illustration, enforcing 10 to be present in x is given by:
	 * 
	 * <pre>
	 * {@code element(x,10);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer, that must be member of {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, int value) {
		return imp().element(list, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * {@code value} must also be assigned to at least one of the specified variables. <br>
	 * As an illustration, enforcing the value of v to be also present in x is given by:
	 * 
	 * <pre>
	 * {@code element(x,v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param value
	 *            an integer variable, whose value must be member of {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, Var value) {
		return imp().element(list, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index.variable}. Note that {@code startIndex.value} indicates the number
	 * used to access the first variable in {@code list} whereas {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with the specified
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with the specified
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with the specified value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)} and for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with value 10 is given by:
	 * 
	 * <pre>
	 * {@code element(x,startIndex(1),index(i),10);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with value 2 is given by:
	 * 
	 * <pre>
	 * {@code element(x,startIndex(10),index(i,FIRST),2);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, int startIndex, Index index, int value) {
		return imp().element(list, startIndex, index.var, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value 10 is given by:
	 * 
	 * <pre>
	 * {@code element(x, at(i), takingValue(10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an integer variable
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, Var index, int value) {
		return element(list, startIndex(0), index(index), value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note that
	 * {@code startIndex.value} indicates the number used to access the first variable in {@code list} whereas {@code index.rank} indicates if
	 * {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with the value
	 * assigned to {@code value}</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with the value
	 * assigned to {@code value}</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with the value assigned to
	 * {@code value}.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)} and for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(x,startIndex(1),index(i),v);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(x,startIndex(10),index(i,FIRST),v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, int startIndex, Index index, Var value) {
		return imp().element(list, startIndex, index.var, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value (of variable) v is given
	 * by:
	 * 
	 * <pre>
	 * {@code element(x, at(i), takingValue(v));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an integer variable
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, Var index, Var value) {
		return element(list, startIndex(0), index(index), value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value in {@code list} at {@code index.variable}. Note that {@code startIndex.value} indicates the number
	 * used to access the first variable in {@code list} whereas {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first value in {@code list} equal to
	 * {@code value}</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last value in {@code list} equal to
	 * {@code value}</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any value in {@code list} equal to {@code value}.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)} and for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any value in t (indexing being started at 1) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(t,startIndex(1),index(i),v);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in t (indexing being started at 10) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(t,startIndex(10),index(i,FIRST),v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integers
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a value in {@code list} equal to {@code value}
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(int[] list, int startIndex, Index index, Var value) {
		return imp().element(list, startIndex, index.var, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value in {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in t (indexing starting at 0, by default) with value (of variable) v is given
	 * by:
	 * 
	 * <pre>
	 * {@code element(t, at(i), takingValue(v));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an integer variable
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(int[] list, Var index, Var value) {
		return element(list, startIndex(0), index(index), value);
	}

	// ************************************************************************
	// ***** Constraint channel
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments: the value {@code j} is
	 * assigned to the ith variable of {@code list} iff the value {@code i} is assigned to the jth variable of {@code list}. Note that
	 * {@code startIndex.value} indicates the number used to access the first variable in {@code list}. <b>Important:</b> for building an object
	 * {@code StartIndex}, use Method {@code startIndex(int)} <br>
	 * 
	 * As an illustration, enforcing a channeling constraint on x (indexing starting at 1) is given by:
	 * 
	 * <pre>
	 * {@code channel(x,startIndex(1));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list, int startIndex) {
		return imp().channel(list, startIndex);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments: the value {@code j} is
	 * assigned to the ith variable of {@code list} iff the value {@code i} is assigned to the jth variable of {@code list}. Note that indexing starts
	 * at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing a channeling constraint on x (indexing starting at 0, by default) is given by:
	 * 
	 * <pre>
	 * {@code channel(x);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list) {
		return channel(list, startIndex(0));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments: assuming for simplicity
	 * that indexing start at 0, the value {@code j} is assigned to the ith variable of {@code list1} iff the value {@code i} is assigned to the jth
	 * variable of {@code list2}.
	 * 
	 * <pre>
	 * {@code list1[i] = j => list2|j] = i}
	 * </pre>
	 * 
	 * The size of the array {@code list1} must be less than or equal to the size of {@code list2}. Still, assuming for simplicity that indexing
	 * starts at 0, When both arrays have the same size, we have:
	 * 
	 * <pre>
	 * {@code list1[i] = j <=> list2|j] = i}
	 * </pre>
	 * 
	 * 
	 * Note that {@code startIndex1.value} indicates the number used to access the first variable in {@code list1}, and similarly
	 * {@code startIndex2.value} indicates the number used to access the first variable in {@code list2}. <b>Important:</b> for building an object
	 * {@code StartIndex}, use Method {@code startIndex(int)} <br>
	 * 
	 * As an illustration, enforcing a channeling constraint between x and y (indexing starting at 1 and 0, respectively) is given by:
	 * 
	 * <pre>
	 * {@code channel(x,startIndex(1),y,startIndex(0));}
	 * </pre>
	 * 
	 * @param list1
	 *            a first 1-dimensional array of integer variables
	 * @param startIndex1
	 *            the number used to access the first variable in {@code list1}
	 * @param list2
	 *            a second 1-dimensional array of integer variables
	 * @param startIndex2
	 *            the number used to access the first variable in {@code list2}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list1, int startIndex1, Var[] list2, int startIndex2) {
		control(list1.length <= list2.length, "The size of the first list must be less than or equal to the size of the second list");
		return imp().channel(list1, startIndex1, list2, startIndex2);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments. The value {@code j} is
	 * assigned to the ith variable of {@code list1} implies that the value {@code i} is assigned to the jth variable of {@code list2}.
	 * 
	 * <pre>
	 * {@code list1[i] = j => list2|j] = i}
	 * </pre>
	 * 
	 * The size of the array {@code list1} must be less than or equal to the size of {@code list2}. When both arrays have the same size, we have:
	 * 
	 * <pre>
	 * {@code list1[i] = j <=> list2|j] = i}
	 * </pre>
	 * 
	 * Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing a channeling constraint between x and y (indexing starting at 0, by default) is given by:
	 * 
	 * <pre>
	 * {@code channel(x,y);}
	 * </pre>
	 * 
	 * @param list1
	 *            a first 1-dimensional array of integer variables
	 * @param list2
	 *            a second 1-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list1, Var[] list2) {
		return channel(list1, startIndex(0), list2, startIndex(0));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments: the value {@code i} is
	 * assigned to {@code value} iff only the ith variable of {@code list} is assigned the value 1 (0 is the value assigned to the other variables of
	 * {@code list}). Note that {@code list} must only contain variables with domain 0/1 and that {@code startIndex.value} indicates the number used
	 * to access the first variable in {@code list}. <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)}
	 * <br>
	 * 
	 * As an illustration, enforcing a channeling constraint between x (indexing starting at 1) and v is given by:
	 * 
	 * <pre>
	 * {@code channel(x,startIndex(1),v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            the number used to access the first variable in {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list, int startIndex, Var value) {
		return imp().channel(list, startIndex, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/channel">{@code channel}</a> from the specified arguments: the value {@code i} is
	 * assigned to {@code value} iff only the ith variable of {@code list} is assigned the value 1 (0 is the value assigned to the other variables of
	 * {@code list}). Note that {@code list} must only contain variables with domain 0/1 and that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing a channeling constraint between x (indexing starting at 0, by default) and v is given by:
	 * 
	 * <pre>
	 * {@code channel(x,v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list, Var value) {
		return channel(list, startIndex(0), value);
	}

	// ************************************************************************
	// ***** Constraint stretch
	// ************************************************************************

	default CtrEntity stretch(Var[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns) {
		return imp().stretch(list, values, widthsMin, widthsMax, patterns);
	}

	default CtrEntity stretch(Var[] list, int[] values, int[] widthsMin, int[] widthsMax) {
		return stretch(list, values, widthsMin, widthsMax, null);
	}

	// ************************************************************************
	// ***** Constraint noOverlap
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. When {@code zeroIgnored} is {@code false}, it means that
	 * zero-length tasks cannot be packed anywhere (cannot overlap with other tasks). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and t (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,t,true);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 1-dimensional array of integer variables
	 * @param lengths
	 *            a 1-dimensional array of integer integers
	 * @param zeroIgnored
	 *            indicates if tasks of length 0 can be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[] origins, int[] lengths, boolean zeroIgnored) {
		return imp().noOverlap(origins, lengths, zeroIgnored);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. Note that zero-length tasks are simply ignored (default
	 * value). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and t (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,t);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 1-dimensional array of integer variables
	 * @param lengths
	 *            a 1-dimensional array of integer integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[] origins, int... lengths) {
		return noOverlap(origins, lengths, true);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given two
	 * tasks, defined by their origins and durations (lengths), which must not overlap. <br>
	 * 
	 * As an illustration, enforcing that the task of origin x1 and length 5 must not overlap with the task of origin x2 and length 3 is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x1,x2,5,3);}
	 * </pre>
	 * 
	 * @param x1
	 *            a first integer variable, denoting the origin of a first task
	 * @param x2
	 *            a second integer variable, denoting the origin of a second task
	 * @param length1
	 *            the length associated with the first variable
	 * @param length2
	 *            the length associated with the second variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var x1, Var x2, int length1, int length2) {
		control(length1 > 0 && length2 > 0, "It is not relevant to have a length which is not strictly positive");
		return noOverlap(vars(x1, x2), vals(length1, length2));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. When {@code zeroIgnored} is {@code false}, it means that
	 * zero-length tasks cannot be packed anywhere (cannot overlap with other tasks). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y,true);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 1-dimensional array of integer variables
	 * @param lengths
	 *            a 1-dimensional array of integer variables
	 * @param zeroIgnored
	 *            indicates if tasks of length 0 can be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[] origins, Var[] lengths, boolean zeroIgnored) {
		return imp().noOverlap(origins, lengths, zeroIgnored);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. Note that zero-length tasks are simply ignored (default
	 * value). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 1-dimensional array of integer variables
	 * @param lengths
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[] origins, Var... lengths) {
		return noOverlap(origins, lengths, true);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given two
	 * tasks, defined by their origins and durations (lengths), which must not overlap. Note that zero-length tasks are simply ignored (default
	 * value). <br>
	 * 
	 * As an illustration, enforcing that the task of origin x1 and length y1 must not overlap with the task of origin x2 and length y2 is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x1,x2,y1,y2);}
	 * </pre>
	 * 
	 * @param x1
	 *            a first integer variable, denoting the origin of a first task
	 * @param x2
	 *            a second integer variable, denoting the origin of a second task
	 * @param length1
	 *            the length associated with {@code x1}
	 * @param length2
	 *            the length associated with {@code x2}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var x1, Var x2, Var length1, Var length2) {
		return noOverlap(vars(x1, x2), vars(length1, length2));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. The ith task is defined by its origin {@code origins[i][j]}
	 * wrt the jth axis and its length {@code lengths[i][j]} wrt the jth axis. When {@code zeroIgnored} is {@code false}, it means that tasks with a
	 * length 0 on some axis cannot be packed anywhere (cannot overlap with other tasks). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y,true);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 2-dimensional array of integer variables
	 * @param lengths
	 *            a 2-dimensional array of integer integers
	 * @param zeroIgnored
	 *            indicates if tasks of length 0 (on any axis) can be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[][] origins, int[][] lengths, boolean zeroIgnored) {
		return imp().noOverlap(origins, lengths, zeroIgnored);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. The ith task is defined by its origin {@code origins[i][j]}
	 * wrt the jth axis and its length {@code lengths[i][j]} wrt the jth axis. Note that tasks of length 0 on some axis are simply ignored (default
	 * value). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 2-dimensional array of integer variables
	 * @param lengths
	 *            a 2-dimensional array of integer integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[][] origins, int[]... lengths) {
		return noOverlap(origins, lengths, true);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. The ith task is defined by its origin {@code origins[i][j]}
	 * wrt the jth axis and its length {@code lengths[i][j]} wrt the jth axis. When {@code zeroIgnored} is {@code false}, it means that tasks with a
	 * length 0 on some axis cannot be packed anywhere (cannot overlap with other tasks). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y,true);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 2-dimensional array of integer variables
	 * @param lengths
	 *            a 2-dimensional array of integer variables
	 * @param zeroIgnored
	 *            indicates if tasks of length 0 (on any axis) can be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[][] origins, Var[][] lengths, boolean zeroIgnored) {
		return imp().noOverlap(origins, lengths, zeroIgnored);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/noOverlap">{@code noOverlap}</a> from the specified arguments: we are given a set
	 * of tasks, defined by their origins and durations (lengths), which must not overlap. The ith task is defined by its origin {@code origins[i][j]}
	 * wrt the jth axis and its length {@code lengths[i][j]} wrt the jth axis. Note that tasks of length 0 on some axis are simply ignored (default
	 * value). <br>
	 * 
	 * As an illustration, enforcing that tasks defined by x (origins) and y (lengths) must not overlap is given by:
	 * 
	 * <pre>
	 * {@code noOverlap(x,y);}
	 * </pre>
	 * 
	 * @param origins
	 *            a 2-dimensional array of integer variables
	 * @param lengths
	 *            a 2-dimensional array of integer variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity noOverlap(Var[][] origins, Var[]... lengths) {
		return noOverlap(origins, lengths, true);
	}

	// ************************************************************************
	// ***** Constraint cumulative
	// ************************************************************************

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), ends and heights. The constraint enforces that at each point in time, the summed
	 * height of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a
	 * given limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param ends
	 *            the end of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, int[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a given
	 * limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, int[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point does not exceed the specified limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param limit
	 *            the limit that must not be exceeded
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, int[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), ends and heights. The constraint enforces that at each point in time, the summed
	 * height of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a
	 * given limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param ends
	 *            the end of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, int[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a given
	 * limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, int[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point does not exceed the specified limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param limit
	 *            the limit that must not be exceeded
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, int[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), ends and heights. The constraint enforces that at each point in time, the summed
	 * height of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a
	 * given limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param ends
	 *            the end of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a given
	 * limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point does not exceed the specified limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param limit
	 *            the limit that must not be exceeded
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), ends and heights. The constraint enforces that at each point in time, the summed
	 * height of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a
	 * given limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param ends
	 *            the end of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point, respects a numerical condition. When the operator “le” is used, this corresponds to not exceeding a given
	 * limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cumulative">{@code cumulative}</a> from the specified arguments: we are given a
	 * set of tasks, defined by their origins, durations (lengths), and heights. The constraint enforces that at each point in time, the summed height
	 * of tasks that overlap that point does not exceed the specified limit.
	 * 
	 * @param origins
	 *            the origin (beginning) of each task
	 * @param lengths
	 *            the duration (length) of each task
	 * @param heights
	 *            the height of each task
	 * @param limit
	 *            the limit that must not be exceeded
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	// ************************************************************************
	// ***** Constraint circuit
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/circuit">{@code circuit}</a> from the specified arguments.
	 * 
	 * @param list
	 *            an array of variables
	 * @param startIndex
	 *            the index used to refer to the first variable of the array
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity circuit(Var[] list, int startIndex) {
		return imp().circuit(list, startIndex);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/circuit">{@code circuit}</a> on the specified array of variables. Note that 0 is
	 * the index used to refer to the first variable of the array.
	 * 
	 * @param list
	 *            an array of variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity circuit(Var[] list) {
		return circuit(list, 0);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/circuit">{@code circuit}</a> from the specified arguments.
	 * 
	 * @param list
	 *            an array of variables
	 * @param startIndex
	 *            the index used to refer to the first variable of the array
	 * @param size
	 *            the size of the circuit
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity circuit(Var[] list, int startIndex, int size) {
		return imp().circuit(list, startIndex, size);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/circuit">{@code circuit}</a> from the specified arguments.
	 * 
	 * @param list
	 *            an array of variables
	 * @param startIndex
	 *            the index used to refer to the first variable of the array
	 * @param size
	 *            the size of the circuit
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity circuit(Var[] list, int startIndex, Var size) {
		return imp().circuit(list, startIndex, size);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/circuit">{@code circuit}</a> from the specified arguments. Note that 0 is the index
	 * used to refer to the first variable of the array
	 * 
	 * @param list
	 *            an array of variables
	 * @param size
	 *            the size of the circuit
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity circuit(Var[] list, Var size) {
		return circuit(list, 0, size);
	}

	// ************************************************************************
	// ***** Constraint clause
	// ************************************************************************

	/**
	 * Builds a constraint {@code clause} from the specified arguments. For each variable and its corresponding phase (i.e., at the same index), a
	 * literal is present in the clause: a positive literal when the phase is {@code true} and a negative literal when the phase is {@code false}.
	 * 
	 * @param list
	 *            an array of variables
	 * @param phases
	 *            an array of boolean values
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity clause(Var[] list, Boolean[] phases) {
		control(Stream.of(list).noneMatch(x -> x == null) && Stream.of(phases).noneMatch(x -> x == null), "No null values is allowed in the specified arrays.");
		return imp().clause(list, phases);
	}

	/**
	 * Builds a constraint {@code clause} from the specified arguments. For each variable, a literal is present in the clause: a positive literal when
	 * the variable belongs to the first specified array and a negative literal when the variable belongs to the second specified array.
	 * 
	 * @param pos
	 *            a first array of variables involved in positive literals
	 * @param neg
	 *            a second array of variables involved in negative literals
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity clause(Var[] pos, Var[] neg) {
		control(Stream.of(pos).noneMatch(x -> x == null) && Stream.of(neg).noneMatch(x -> x == null), "No null values is allowed in the specified arrays.");
		Boolean[] phases = IntStream.range(0, pos.length + neg.length).mapToObj(i -> (Boolean) (i < pos.length)).toArray(Boolean[]::new);
		return clause(vars(pos, (Object) neg), phases);
	}

	// ************************************************************************
	// ***** Constraint instantiation
	// ************************************************************************

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * its corresponding value. For example:
	 * 
	 * <pre>
	 * {@code instantiation(x, t);}
	 * </pre>
	 * 
	 * @param list
	 *            an array of variables
	 * @param values
	 *            an array of integers
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, int... values) {
		list = list == null ? list : clean(list);
		control(list == null && values.length == 0 || list.length == values.length, "The length of list is different from the length of values");
		if (values.length == 0)
			return imp().dummyConstraint("A constraint instantiation with a scope of 0 variable.");
		return imp().instantiation(list, values);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * its corresponding value (from the range). For example:
	 * 
	 * <pre>
	 * {@code instantiation(x, range(10));}
	 * </pre>
	 * 
	 * @param list
	 *            an array of variables
	 * @param values
	 *            a range of values
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, Range values) {
		return instantiation(list, values.toArray());
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * its corresponding value in the specified stream.
	 * 
	 * @param list
	 *            an array of variables
	 * @param values
	 *            a stream of integers
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, IntStream values) {
		return instantiation(list, values.toArray());
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * its corresponding value in the specified collection.
	 * 
	 * @param list
	 *            an array of variables
	 * @param values
	 *            a collection of integers
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, Collection<Integer> values) {
		return instantiation(list, values.stream().mapToInt(i -> i));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * the specified value. For example:
	 * 
	 * <pre>
	 * {@code instantiation(x, 0);}
	 * </pre>
	 * 
	 * @param list
	 *            an array of variables
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, int value) {
		list = list == null ? list : clean(list);
		if (list == null || list.length == 0)
			return imp().dummyConstraint("A constraint instantiation with a scope of 0 variable.");
		return instantiation(list, repeat(value, list.length));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable with
	 * its corresponding value. For example:
	 * 
	 * <pre>
	 * {@code instantiation(x, t);}
	 * </pre>
	 * 
	 * @param list
	 *            a stream of variables
	 * @param values
	 *            a stream of integers
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Stream<Var> list, IntStream values) {
		return instantiation(vars(list), values.toArray());
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable at
	 * index {@code i} with its corresponding value at index {@code i}, provided that the specified predicate accepts {@code i}. For example:
	 * 
	 * <pre>
	 * {@code instantiation(x, t, i -> i%2 == 0);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of variables
	 * @param values
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a variable (and its value) at index {@code i} must be considered
	 * 
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, int[] values, Intx1Predicate p) {
		if (list == null || values == null)
			return imp().dummyConstraint("A constraint instantiation with a scope of 0 variable.");
		return instantiation(select(list, p), selectFromIndexing(values, p));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable at
	 * index {@code (i,j)} with its corresponding value at index {@code (i,j)}, provided that the specified predicate accepts {@code (i,j)}. For
	 * example:
	 * 
	 * <pre>
	 * {@code instantiation(x, t, (i,j) -> i < j);}
	 * </pre>
	 * 
	 * @param list
	 *            a 2-dimensional array of variables
	 * @param values
	 *            a 2-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a variable (and its value) at index {@code (i,j)} must be considered
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[][] list, int[][] values, Intx2Predicate p) {
		if (list == null || values == null)
			return imp().dummyConstraint("A constraint instantiation with a scope of 0 variable.");
		return instantiation(select(list, p), selectFromIndexing(values, p));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/instantiation">{@code instantiation}</a>, assigning each specified variable at
	 * index {@code (i,j,k)} with its corresponding value at index {@code (i,j,k)}, provided that the specified predicate accepts {@code (i,j,k)}. For
	 * example:
	 * 
	 * <pre>
	 * {@code instantiation(x, t, (i,j,k) -> i == j+k);}
	 * </pre>
	 * 
	 * @param list
	 *            a 3-dimensional array of variables
	 * @param values
	 *            a 3-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a variable (and its value) at index {@code (i,j,k)} must be considered
	 * @return an object {@code CtrEntity} that wraps the build constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[][][] list, int[][][] values, Intx3Predicate p) {
		return instantiation(select(list, p), selectFromIndexing(values, p));
	}

	// ************************************************************************
	// ***** Meta-Constraint slide
	// ************************************************************************

	/**
	 * Builds a meta-constraint <a href="http://xcsp.org/specifications/slide">{@code slide}</a> from the specified arguments. The specified template
	 * represents a sliding constraint over the specified variables. More precisely, for each value of the specified range, the specified template is
	 * called to generate a constraint using the specified variables. For example:
	 * 
	 * <pre>
	 * {@code slide(x, range(nCards - 1), i -> extension(vars(x[i], x[i + 1]), tuples)); }
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of variables
	 * @param range
	 *            a range of values
	 * @param template
	 *            a lambda used to generate a constraint for each value of the range
	 * @return an object {@code CtrEntity} that wraps the build meta-constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity slide(IVar[] list, Range range, IntFunction<CtrEntity> template) {
		control(range.startInclusive == 0 && range.length() > 0, "Bad form of range");
		return imp().slide(list, range, template);
	}

	// ************************************************************************
	// ***** Meta-Constraint ifThen
	// ************************************************************************

	/**
	 * Builds a meta-constraint {@code ifThen} from the two specified constraints. This is equivalent to:
	 * 
	 * <pre>
	 * {@code
	 * if c1 then
	 *   c2
	 * }
	 * </pre>
	 * 
	 * @param c1
	 *            a first constraint (used as condition of the alternative)
	 * @param c2
	 *            a second constraint (used as "then" part of the alternative)
	 * @return an object {@code CtrEntity} that wraps the built meta-constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ifThen(CtrEntity c1, CtrEntity c2) {
		return imp().ifThen(c1, c2);
	}

	// ************************************************************************
	// ***** Meta-Constraint ifThenElse
	// ************************************************************************

	/**
	 * Builds a meta-constraint {@code ifThenElse} from the three specified constraints. This is equivalent to:
	 * 
	 * <pre>
	 * {@code
	 * if c1 then
	 *   c2
	 * else
	 *   c3
	 * }
	 * </pre>
	 * 
	 * @param c1
	 *            a first constraint (used as condition of the alternative)
	 * @param c2
	 *            a second constraint (used as "then" part of the alternative)
	 * @param c3
	 *            a third constraint (used as "else" part of the alternative)
	 * @return an object {@code CtrEntity} that wraps the built meta-constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ifThenElse(CtrEntity c1, CtrEntity c2, CtrEntity c3) {
		return imp().ifThenElse(c1, c2, c3);
	}

	// ************************************************************************
	// ***** Managing objectives
	// ************************************************************************

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the value of the specified variable.
	 * 
	 * @param x
	 *            a variable
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(IVar x) {
		return imp().minimize(x);
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the value of the specified variable.
	 * 
	 * @param x
	 *            a variable
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(IVar x) {
		return imp().maximize(x);
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified array of variables. For example:
	 * 
	 * <pre>
	 * {@code minimize(SUM,x,y,z);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar... list) {
		return imp().minimize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables that will be flattened (i.e., converted into a 1-dimensional array of variables).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][] list) {
		return minimize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables that will be flattened (i.e., converted into a 1-dimensional array of variables).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][][] list) {
		return minimize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified array of variables. For example:
	 * 
	 * <pre>
	 * {@code maximize(SUM,x,y,z);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar... list) {
		return imp().maximize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables that will be flattened (i.e., converted into a 1-dimensional array of variables).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][] list) {
		return maximize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables that will be flattened (i.e., converted into a 1-dimensional array of variables).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][][] list) {
		return maximize(type, vars(list));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified array of variables, each of them being given a coefficient. For example:
	 * 
	 * <pre>
	 * {@code minimize(SUM,vars(x,y,z),vals(1,2,1));}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[] list, int[] coeffs) {
		control(list.length == coeffs.length, "Size of list and coeffs are different");
		return imp().minimize(type, list, coeffs);
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][] list, int[][] coeffs) {
		return minimize(type, vars(list), vals((Object) coeffs));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays). Only variables at index accepted by the specified predicate are considered. For example:
	 * 
	 * <pre>
	 * {@code minimize(SUM,x,t,(i,j)->i<j);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @param p
	 *            a predicate allowing us to test if a variable (and its coefficient) at index {@code (i,j)} must be considered.
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][] list, int[][] coeffs, Intx2Predicate p) {
		return minimize(type, select(list, p), selectFromIndexing(coeffs, p));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][][] list, int[][][] coeffs) {
		return minimize(type, vars(list), vals((Object) coeffs));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to minimize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays). Only variables at index accepted by the specified predicate are considered. For example:
	 * 
	 * <pre>
	 * {@code minimize(SUM,x,t,(i,j,k)->i<j+k);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @param p
	 *            a predicate allowing us to test if a variable (and its coefficient) at index {@code (i,j,k)} must be considered
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity minimize(TypeObjective type, IVar[][][] list, int[][][] coeffs, Intx3Predicate p) {
		return minimize(type, select(list, p), selectFromIndexing(coeffs, p));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified array of variables, each of them being given a coefficient. For example:
	 * 
	 * <pre>
	 * {@code maximize(SUM,vars(x,y,z),vals(1,2,1));}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[] list, int[] coeffs) {
		control(list.length == coeffs.length, "Size of list and coeffs are different");
		return imp().maximize(type, list, coeffs);
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][] list, int[][] coeffs) {
		return maximize(type, vars(list), vals((Object) coeffs));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 2-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays). Only variables at index accepted by the specified predicate are considered. For example:
	 * 
	 * <pre>
	 * {@code maximize(SUM,x,t,(i,j)->i<j);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @param p
	 *            a predicate allowing us to test if a variable (and its coefficient) at index {@code (i,j)} must be considered.
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][] list, int[][] coeffs, Intx2Predicate p) {
		return maximize(type, select(list, p), selectFromIndexing(coeffs, p));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays).
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][][] list, int[][][] coeffs) {
		return maximize(type, vars(list), vals((Object) coeffs));
	}

	/**
	 * Builds an <a href="http://xcsp.org/specifications/objectives">objective</a> (function) to maximize: the objective is defined by the specified
	 * type on the specified 3-dimensional array of variables, each of them being given a coefficient. The arrays will be flattened (i.e., converted
	 * into 1-dimensional arrays). Only variables at index accepted by the specified predicate are considered. For example:
	 * 
	 * <pre>
	 * {@code maximize(SUM,x,t,(i,j,k)->i<j+k);}
	 * </pre>
	 * 
	 * @param type
	 *            the type of the objective
	 * @param list
	 *            the involved variables
	 * @param coeffs
	 *            the coefficients associated with the variables
	 * @param p
	 *            a predicate allowing us to test if a variable (and its coefficient) at index {@code (i,j,k)} must be considered.
	 * @return an object {@code ObjEntity} that wraps the built objective and allows us to provide note and tags by method chaining
	 */
	default ObjEntity maximize(TypeObjective type, IVar[][][] list, int[][][] coeffs, Intx3Predicate p) {
		return maximize(type, select(list, p), selectFromIndexing(coeffs, p));
	}

	/**
	 * Builds the model. You have to declare variables, constraints and objectives in this method.
	 */
	void model();

	/**
	 * Called to display a solution given by the specified array. Advanced use: relevant if a solver is plugged. By default, it does nothing.
	 * 
	 * @param values
	 *            the values assigned to the variables
	 */
	default void prettyDisplay(String[] values) {}

	// ************************************************************************
	// ***** Managing Annotations
	// ************************************************************************

	/**
	 * Sets the specified variables as those on which a solver should branch in priority. This generates an annotation.
	 * 
	 * @param list
	 *            a 1-dimensional array of variables
	 */
	default void decisionVariables(IVar[] list) {
		imp().decisionVariables(list);
	}

	/**
	 * Sets the specified variables as those on which a solver should branch in priority. This generates an annotation.
	 * 
	 * @param list
	 *            a 2-dimensional array of variables
	 */
	default void decisionVariables(IVar[][] list) {
		imp().decisionVariables(vars(list));
	}

}
