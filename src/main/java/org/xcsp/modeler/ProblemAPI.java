package org.xcsp.modeler;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Constants;
import org.xcsp.common.FunctionalInterfaces.IntToDomInteger;
import org.xcsp.common.FunctionalInterfaces.IntToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx1Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDomInteger;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx3Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx3Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3ToDomInteger;
import org.xcsp.common.FunctionalInterfaces.Intx4Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx4Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx4ToDomInteger;
import org.xcsp.common.FunctionalInterfaces.Intx5Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx5Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx5ToDomInteger;
import org.xcsp.common.IVar;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Range.Rangesx3;
import org.xcsp.common.Range.Rangesx4;
import org.xcsp.common.Range.Rangesx5;
import org.xcsp.common.Size.Size1D;
import org.xcsp.common.Size.Size2D;
import org.xcsp.common.Size.Size3D;
import org.xcsp.common.Size.Size4D;
import org.xcsp.common.Size.Size5D;
import org.xcsp.common.Types.StandardClass;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.Automaton;
import org.xcsp.common.structures.TableInteger;
import org.xcsp.common.structures.TableSymbolic;
import org.xcsp.common.structures.Transition;
import org.xcsp.modeler.ProblemAPI.Occurrences.OccurrencesIntBasic;
import org.xcsp.modeler.ProblemAPI.Occurrences.OccurrencesIntDouble;
import org.xcsp.modeler.ProblemAPI.Occurrences.OccurrencesIntRange;
import org.xcsp.modeler.ProblemAPI.Occurrences.OccurrencesIntSimple;
import org.xcsp.modeler.ProblemAPI.Occurrences.OccurrencesVar;
import org.xcsp.modeler.entities.CtrEntities.CtrAlone;
import org.xcsp.modeler.entities.CtrEntities.CtrArray;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;
import org.xcsp.modeler.entities.ObjEntities.ObjEntity;
import org.xcsp.modeler.implementation.ProblemIMP;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;

public interface ProblemAPI {

	/**
	 * Controls that the specified {@code boolean} argument is {@code true}. If it is not the case, the program will stop and specified objects will
	 * be displayed.
	 * 
	 * @param b
	 *            a {@code boolean} value to be controlled to be {@code true}
	 * @param objects
	 *            a sequence of objects used for displaying information when the specified {@code boolean} argument is {@code false}
	 */
	default void control(boolean b, Object... objects) {
		ProblemIMP.control(b, objects);
	}

	/**
	 * <b>Advanced Use</b>: you shouldn't normally use this map that relates {@code ProblemAPI} objects with {@code ProblemIMP} objects.
	 */
	static Map<ProblemAPI, ProblemIMP> api2imp = new HashMap<>();

	/**
	 * <b>Advanced Use</b>: you shouldn't normally use the {@code ProblemIMP} object that offers implementation stuff for this object.
	 * 
	 * @return the {@code ProblemIMP} object that offers implementation stuff for this {@code ProblemAPI} object
	 */
	default ProblemIMP imp() {
		control(api2imp.get(this) != null, "The method has been called before the associated problem implementation object was created.");
		return api2imp.get(this);
	}

	/**
	 * Returns the name of this object (i.e., the name of this problem instance). By default, this is the name of the class implementing
	 * {@code ProblemAPI} followed by the values of all parameters (separated by the symbol '-'). The parameters are the fields, used as data, which
	 * are declared in the class implementing {@code ProblemAPI}. Possibly, the name of a model variant, if used, is inserted after the name of the
	 * class.
	 */
	default String name() {
		return imp().name();
	}

	/**
	 * Returns {@code true} iff the user has indicated (through the compiler by using the argument -model=) that the model corresponds to the value of
	 * the specified string.
	 * 
	 * @param s
	 *            a string representing the name of a model (variant)
	 * @return {@code true} iff the model corresponds to the specified string
	 */
	default boolean isModel(String s) {
		return s.equals(imp().model);
	}

	// ************************************************************************
	// ***** Constants
	// ************************************************************************

	/**
	 * A constant denoting the relational operator "strictly Less Than", which is useful for expressing conditions, as for example in
	 * {@code sum(x, LT, 10)} or {@code count(x, 0, LT, 5)}.
	 */
	TypeConditionOperatorRel LT = TypeConditionOperatorRel.LT;

	/**
	 * A constant denoting the relational operator "Less than or Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, LE, 10) </code> or <code> count(x, 0, LE, 5) </code>.
	 */
	TypeConditionOperatorRel LE = TypeConditionOperatorRel.LE;

	/**
	 * A constant denoting the relational operator "Greater than or Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, GE, 10) </code> or <code> count(x, 0, GE, 5) </code>.
	 */
	TypeConditionOperatorRel GE = TypeConditionOperatorRel.GE;

	/**
	 * A constant denoting the relational operator "strictly Greater Than", which is useful for expressing conditions, as for example in
	 * <code> sum(x, GT, 10) </code> or <code> count(x, 0, GT, 5) </code>.
	 */
	TypeConditionOperatorRel GT = TypeConditionOperatorRel.GT;

	/**
	 * A constant denoting the relational operator "Not Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, NE, 10) </code> or <code> count(x, 0, NE, 5) </code>.
	 */
	TypeConditionOperatorRel NE = TypeConditionOperatorRel.NE;

	/**
	 * A constant denoting the relational operator "Equal", which is useful for expressing conditions, as for example in <code> sum(x, EQ, 10) </code>
	 * or <code> count(x, 0, EQ, 5) </code>.
	 */
	TypeConditionOperatorRel EQ = TypeConditionOperatorRel.EQ;

	/**
	 * A constant denoting the set operator "In", which is useful for expressing conditions, as for example in <code> sum(x, IN, 5, 10) </code> or
	 * <code> count(x, 0, IN, 5, 10) </code>.
	 */
	TypeConditionOperatorSet IN = TypeConditionOperatorSet.IN;

	/**
	 * A constant denoting the set operator "Not In", which is useful for expressing conditions, as for example in <code> sum(x, NOTIN, 5, 10) </code>
	 * or <code> count(x, 0, NOTIN, 5, 10) </code>.
	 */
	TypeConditionOperatorSet NOTIN = TypeConditionOperatorSet.NOTIN;

	/**
	 * A constant denoting the relational operator "strictly Less Than", which is useful for expressing an ordering, as for example in
	 * <code> ordered(x, STRICTLY_INCREASING) </code> or <code> lex(x, STRICTLY_INCREASING) </code>.
	 */
	TypeOperatorRel STRICTLY_INCREASING = TypeOperatorRel.LT;

	/**
	 * A constant denoting the relational operator "Less than or Equal", which is useful for expressing an ordering, as for example in
	 * <code> ordered(x, INCREASING) </code> or <code> lex(x, INCREASING) </code>.
	 */
	TypeOperatorRel INCREASING = TypeOperatorRel.LE;

	/**
	 * A constant denoting the relational operator "Greater than or Equal", which is useful for expressing an ordering, as for example in
	 * <code> ordered(x, DECREASING) </code> or <code> lex(x, DECREASING) </code>.
	 */
	TypeOperatorRel DECREASING = TypeOperatorRel.GE;

	/**
	 * A constant denoting the relational operator "strictly Greater Than", which is useful for expressing an ordering, as for example in
	 * <code> ordered(x, STRICTLY_DECREASING) </code> or <code> lex(x, STRICTLY_DECREASING) </code>.
	 */
	TypeOperatorRel STRICTLY_DECREASING = TypeOperatorRel.GT;

	/**
	 * A constant denoting the type "expression" for an objective function, as for example in <code> minimize(EXPRESSION, add(x,mul(y,3)) </code>.
	 */
	TypeObjective EXPRESSION = TypeObjective.EXPRESSION;

	/**
	 * A constant denoting the type "sum" for an objective function, as for example in <code> minimize(SUM, x, y, z) </code>.
	 */
	TypeObjective SUM = TypeObjective.SUM;

	/**
	 * A constant denoting the type "product" for an objective function, as for example in <code> minimize(PRODUCT, x, y, z) </code>.
	 */
	TypeObjective PRODUCT = TypeObjective.PRODUCT;

	/**
	 * A constant denoting the type "minimum" for an objective function, as for example in <code> maximize(MINIMUM, x, y, z) </code>.
	 */
	TypeObjective MINIMUM = TypeObjective.MINIMUM;

	/**
	 * A constant denoting the type "maximum" for an objective function, as for example in <code> minimize(MAXIMUM, x, y, z) </code>.
	 */
	TypeObjective MAXIMUM = TypeObjective.MAXIMUM;

	/**
	 * A constant denoting the type "nValues" for an objective function, as for example in <code> minimize(NVALUES, x) </code>.
	 */
	TypeObjective NVALUES = TypeObjective.NVALUES;

	/**
	 * A constant denoting the type "lex" for an objective function, as for example in <code> minimize(LEX, x, y, z) </code>.
	 */
	TypeObjective LEX = TypeObjective.LEX;

	/**
	 * The constant "channeling" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass CHANNELING = StandardClass.CHANNELING;

	/**
	 * The constant "clues" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass CLUES = StandardClass.CLUES;

	/**
	 * The constant "rows" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass ROWS = StandardClass.ROWS;

	/**
	 * The constant "columns" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass COLUMNS = StandardClass.COLUMNS;

	/**
	 * The constant "blocks" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass BLOCKS = StandardClass.BLOCKS;

	/**
	 * The constant "diagonals" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass DIAGONALS = StandardClass.DIAGONALS;

	/**
	 * The constant "symmetryBreaking" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass SYMMETRY_BREAKING = StandardClass.SYMMETRY_BREAKING;

	/**
	 * The constant "redundantConstraints" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass REDUNDANT_CONSTRAINTS = StandardClass.REDUNDANT_CONSTRAINTS;

	/**
	 * The constant "nogoods" that can be used for tagging elements such as variables, constraints, blocks, groups, ...
	 */
	TypeClass NOGOODS = StandardClass.NOGOODS;

	/**
	 * A constant denoting that a search is conducted with respect to the first object (typically, variable) of a structure (typically, a
	 * 1-dimensional array of variables) having a certain property.
	 */
	TypeRank FIRST = TypeRank.FIRST;

	/**
	 * A constant denoting that a search is conducted with respect to the last object (typically, variable) of a structure (typically, a 1-dimensional
	 * array of variables) having a certain property.
	 */
	TypeRank LAST = TypeRank.LAST;

	/**
	 * A constant denoting that a search is conducted with respect to any object (typically, variable) of a structure (typically, a 1-dimensional
	 * array of variables) having a certain property.
	 */
	TypeRank ANY = TypeRank.ANY;

	/**
	 * A constant, equal to Boolean.TRUE, that can be used to indicate that a set of tuples corresponds to supports.
	 */
	Boolean POSITIVE = Boolean.TRUE;

	/**
	 * A constant, equal to Boolean.FALSE, that can be used to indicate that a set of tuples corresponds to conflicts.
	 */
	Boolean NEGATIVE = Boolean.FALSE;

	/**
	 * The constant used for denoting "*" in integer tuples.
	 */
	int STAR_INT = Constants.STAR_INT;

	/**
	 * The constant used for denoting the symbol "*".
	 */
	String STAR_SYMBOL = Constants.STAR_SYMBOL;

	// ************************************************************************
	// ***** Selecting (Arrays of) Variables
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable at an index {@code i} going
	 * from the specified {@code fromIndex} (inclusive) to the specified {@code toIndex} (exclusive). Note that {@code null} values are simply
	 * discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @param fromIndex
	 *            the index of the first variable (inclusive) to be selected
	 * @param toIndex
	 *            the index of the last variable (exclusive) to be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[] vars, int fromIndex, int toIndex) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		control(0 <= fromIndex && fromIndex < toIndex && toIndex <= vars.length, "The specified indexes are not correct.");
		T[] t = Utilities.convert(IntStream.range(fromIndex, toIndex).mapToObj(i -> vars[i]).filter(x -> x != null).collect(Collectors.toList()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable at an index {@code i}
	 * present in the {@code indexes} argument. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @param indexes
	 *            the indexes of the variables to be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[] vars, int[] indexes) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		// indexes = IntStream.of(indexes).sorted().distinct().toArray();
		control(IntStream.of(indexes).allMatch(i -> 0 <= i && i < vars.length), "The indexes in the specified array are not correct.");
		T[] t = Utilities.convert(Arrays.stream(indexes).mapToObj(i -> vars[i]).filter(x -> x != null).collect(Collectors.toList()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code i}, that
	 * satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @param p
	 *            a predicate allowing us to test if a variable at index {@code i} must be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[] vars, Intx1Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[] t = Utilities.convert(Intx1Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j)},
	 * that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @param p
	 *            a predicate allowing us test if a variable at index {@code (i,j)} must be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][] vars, Intx2Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[] t = Utilities.convert(Intx2Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j,k)},
	 * that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 3-dimensional array of variables
	 * @param p
	 *            a predicate allowing us test if a variable at index {@code (i,j,k)} must be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][] vars, Intx3Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[] t = Utilities.convert(Intx3Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j,k,l)},
	 * that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 4-dimensional array of variables
	 * @param p
	 *            a predicate allowing us to test if a variable at index {@code (i,j,k,l)} must be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][][] vars, Intx4Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[] t = Utilities.convert(Intx4Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index
	 * {@code (i,j,k,l,m)}, that satisfies the specified predicate. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 5-dimensional array of variables
	 * @param p
	 *            a predicate allowing us to test if a variable at index {@code (i,j,k,l,m)} must be selected
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][][][] vars, Intx5Predicate p) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[] t = Utilities.convert(Intx5Predicate.select(vars, p, new ArrayList<>()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), 0);
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code i}, that
	 * belongs to the specified range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @param range
	 *            an object representing a range of indexes
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[] vars, Range range) {
		return select(vars, i -> range.contains(i));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j)},
	 * that belongs to the specified double range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @param rangesx2
	 *            an object representing a double range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][] vars, Rangesx2 rangesx2) {
		return select(vars, (i, j) -> rangesx2.contains(i, j));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j,k)},
	 * that belongs to the specified triple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 3-dimensional array of variables
	 * @param rangesx3
	 *            an object representing a triple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][] vars, Rangesx3 rangesx3) {
		return select(vars, (i, j, k) -> rangesx3.contains(i, j, k));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index {@code (i,j,k,l)},
	 * that belongs to the specified quadruple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 4-dimensional array of variables
	 * @param rangesx4
	 *            an object representing a quadruple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][][] vars, Rangesx4 rangesx4) {
		return select(vars, (i, j, k, l) -> rangesx4.contains(i, j));
	}

	/**
	 * Builds and returns a 1-dimensional array of variables, obtained by selecting from the specified array any variable, at index
	 * {@code (i,j,k,l,m)}, that belongs to the specified quintuple range. Note that {@code null} values are simply discarded, if ever present.
	 * 
	 * @param vars
	 *            a 5-dimensional array of variables
	 * @param rangesx5
	 *            an object representing a quintuple range of indexes (seen as a Cartesian product)
	 * @return a 1-dimensional array of variables (possibly, of length 0)
	 */
	default <T extends IVar> T[] select(T[][][][][] vars, Rangesx5 rangesx5) {
		return select(vars, (i, j, k, l, m) -> rangesx5.contains(i, j, k, l, m));
	}

	/**
	 * Selects from the specified 2-dimensional array of variables the column at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @param idColumn
	 *            the index of a column
	 * @return the column from the specified 2-dimensional array of variables, at the specified index
	 */
	default <T extends IVar> T[] columnOf(T[][] vars, int idColumn) {
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null object.");
		control(0 <= idColumn && Stream.of(vars).allMatch(t -> t != null && idColumn < t.length), "The specified index is not valid.");
		T[] t = Utilities.convert(Stream.of(vars).map(p -> p[idColumn]).collect(Collectors.toList()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of variables the downward diagonal at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @param idDiagonal
	 *            the index of a downward diagonal
	 * @return the downward diagonal from the specified 2-dimensional array of variables, at the specified index
	 */
	default <T extends IVar> T[] diagonalDown(T[][] vars, int idDiagonal) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(0 <= idDiagonal && idDiagonal < vars.length, "The specified index is not valid.");
		T[] t = Utilities.convert(IntStream.range(0, vars.length).mapToObj(i -> vars[i][i < idDiagonal ? vars.length - (idDiagonal - i) : i - idDiagonal])
				.collect(Collectors.toList()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of variables the upward diagonal at the specified index.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @param idDiagonal
	 *            the index of an upward diagonal
	 * @return the upward diagonal from the specified 2-dimensional array of variables, at the specified index
	 */
	default <T extends IVar> T[] diagonalUp(T[][] vars, int idDiagonal) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(0 <= idDiagonal && idDiagonal < vars.length, "The specified index is not valid.");
		T[] t = Utilities.convert(IntStream.range(0, vars.length)
				.mapToObj(i -> vars[i][i < vars.length - idDiagonal ? vars.length - idDiagonal - i - 1 : 2 * vars.length - idDiagonal - i - 1])
				.collect(Collectors.toList()));
		return t != null ? t : (T[]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), vars.length);
	}

	/**
	 * Selects from the specified 2-dimensional array of variables the main downward diagonal.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @return the main downward diagonal
	 */
	default <T extends IVar> T[] diagonalDown(T[][] vars) {
		return diagonalDown(vars, 0);
	}

	/**
	 * Selects from the specified 2-dimensional array of variables the main upward diagonal.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @return the main upward diagonal
	 */
	default <T extends IVar> T[] diagonalUp(T[][] vars) {
		return diagonalUp(vars, 0);
	}

	default <T extends IVar> T[] diagonalDown(T[][] vars, int i, int j) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "");
		control(i == 0 && 0 <= j && j < vars.length - 1 || j == 0 && 0 <= i && i < vars.length - 1, "");
		return Utilities.convert(IntStream.range(0, vars.length - Math.max(i, j)).mapToObj(k -> vars[i + k][j + k]).collect(Collectors.toList()));

	}

	default <T extends IVar> T[] diagonalUp(T[][] vars, int i, int j) {
		control(Utilities.isRegular(vars) && vars.length == vars[0].length, "");
		control(j == 0 && 0 < i && j < vars.length || i == vars.length - 1 && 0 <= j && j < vars.length - 1, "");
		return Utilities.convert(IntStream.range(0, Math.min(i + 1, vars.length - j)).mapToObj(k -> vars[i - k][j + k]).collect(Collectors.toList()));
	}

	/**
	 * Returns the transpose of the specified 2-dimensional array of variables.
	 * 
	 * @param vars
	 *            a 2-dimensional array of variables
	 * @return the transpose of the specified 2-dimensional array of variables
	 */
	default <T extends IVar> T[][] transpose(T[]... vars) {
		control(Utilities.isRegular(vars), "The specified array must have the same number of rows and columns");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[][] t = (T[][]) Array.newInstance(Utilities.firstNonNull(vars).getClass(), vars[0].length, vars.length);
		IntStream.range(0, t.length).forEach(i -> IntStream.range(0, t[0].length).forEach(j -> t[i][j] = vars[j][i]));
		return t;
	}

	/**
	 * Returns a 2-dimensional array of variables obtained from the specified 3-dimensional array of variables by eliminating the second dimension
	 * after fixing it to the {@code idx} argument. The array {@code t} returned by this function is such that {@code t[i][j]=vars[i][idx][j]}.
	 * 
	 * @param vars
	 *            a 3-dimensional array of variables
	 * @param idx
	 *            the index that is fixed for the second dimension
	 * @return a 2-dimensional array of variables corresponding to the elimination of the second dimension by fixing it to the specified index
	 */
	default <T extends IVar> T[][] eliminateDim2(T[][][] vars, int idx) {
		control(Utilities.isRegular(vars), "The specified array must be regular");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[][] m = (T[][]) Array.newInstance(vars[0][0][0].getClass(), vars.length, vars[0][0].length);
		IntStream.range(0, m.length).forEach(i -> IntStream.range(0, m[0].length).forEach(j -> m[i][j] = vars[i][idx][j]));
		return m;
	}

	/**
	 * Returns a 2-dimensional array of variables obtained from the specified 3-dimensional array of variables by eliminating the third dimension
	 * after fixing it to the {@code idx} argument. The array {@code t} returned by this function is such that {@code t[i][j]=vars[i][j][idx]}.
	 * 
	 * @param vars
	 *            a 3-dimensional array of variables
	 * @param idx
	 *            the index that is fixed for the third dimension
	 * @return a 2-dimensional array of variables corresponding to the elimination of the third dimension by fixing it to the specified index
	 */
	default <T extends IVar> T[][] eliminateDim3(T[][][] vars, int idx) {
		control(Utilities.isRegular(vars), "The specified array must be regular");
		control(Utilities.firstNonNull(vars) != null, "The specified array must contain at least one non-null variable.");
		T[][] m = (T[][]) Array.newInstance(vars[0][0][0].getClass(), vars.length, vars[0].length);
		IntStream.range(0, m.length).forEach(i -> IntStream.range(0, m[0].length).forEach(j -> m[i][j] = vars[i][j][idx]));
		return m;
	}

	/**
	 * Builds and returns a 1-dimensional array of variables from the specified sequence of parameters. Each element of the sequence must only contain
	 * variables (and possibly {@code null} values), either stand-alone or present in arrays (of any dimension) or streams. All variables are
	 * collected in order, and concatenated to form a 1-dimensional array. Note that {@code null} values are simply discarded.
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

	/**
	 * Builds and returns a sorted 1-dimensional array of disrinct variables from the specified 1-dimensional array of variables.
	 * 
	 * @param vars
	 *            a 1-dimensional array of variables
	 * @return a sorted 1-dimensional array of distinct variables
	 */
	default <T extends IVar> T[] distinctSorted(T[] vars) {
		return imp().distinctSorted(vars);
	}

	// ************************************************************************
	// ***** Managing values and Tuples
	// ************************************************************************

	/**
	 * Builds and returns a 1-dimensional array of integers from the specified sequence of parameters. Each element of the sequence must be an
	 * {@code Integer}, a {@code Range} or a k-dimensional array of {@code int} (with {@code k=1, k=2 or k=3}). All integers are collected and
	 * concatenated to form a 1-dimensional array.
	 * 
	 * @param objects
	 *            a sequence of objects, each being an {@code Integer}, a {@code Range} or a k-dimensional array of {@code int}
	 * @return a 1-dimensional array formed of collected integers
	 */
	default int[] vals(Object... objects) {
		return Utilities.collectVals(objects);
	}

	/**
	 * Returns a tuple (array) of integers from the specified parameters.
	 * 
	 * @param val
	 *            an integer
	 * @param otherVals
	 *            a sequence of integers
	 * @return a 1-dimensional array of {@code int}
	 */
	default int[] tuple(int val, int... otherVals) {
		return IntStream.range(0, otherVals.length + 1).map(i -> i == 0 ? val : otherVals[i - 1]).toArray();
	}

	/**
	 * Returns a 2-dimensional array of integers from the specified tuples.
	 * 
	 * @param tuple
	 *            a tuple
	 * @param otherTuples
	 *            a sequence of tuples
	 * @return a 2-dimensional array of {@code int}
	 */
	default int[][] tuples(int[] tuple, int[]... otherTuples) {
		return IntStream.range(0, otherTuples.length + 1).mapToObj(i -> i == 0 ? tuple : otherTuples[i - 1]).toArray(int[][]::new);
	}

	/**
	 * Returns a tuple (array) of strings from the specified parameters.
	 * 
	 * @param symbol
	 *            a string
	 * @param otherSymbols
	 *            a sequence of strings
	 * @return a 1-dimensional array of strings
	 */
	default String[] tuple(String symbol, String... otherSymbols) {
		return IntStream.range(0, otherSymbols.length + 1).mapToObj(i -> i == 0 ? symbol : otherSymbols[i - 1]).toArray(String[]::new);
	}

	/**
	 * Returns a 2-dimensional array of strings from the specified tuples.
	 * 
	 * @param tuple
	 *            a symbolic tuple
	 * @param otherTuples
	 *            a sequence of symbolic tuples
	 * @return a 2-dimensional array of strings
	 */
	default String[][] tuples(String[] tuple, String[]... otherTuples) {
		return IntStream.range(0, otherTuples.length + 1).mapToObj(i -> i == 0 ? tuple : otherTuples[i - 1]).toArray(String[][]::new);
	}

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
		return Arrays.stream(values).mapToObj(v -> tuple(v)).toArray(int[][]::new);
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
	 * Returns the transpose of the specified 2-dimensional array.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @return the transpose of the specified 2-dimensional array
	 */
	default int[][] transpose(int[]... m) {
		control(Utilities.isRegular(m), "The specified array must have the same number of rows and columns");
		return IntStream.range(0, m[0].length).mapToObj(i -> IntStream.range(0, m.length).map(j -> m[j][i]).toArray()).toArray(int[][]::new);
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
	 * Returns in increasing order all distinct values from the specified 1-dimensional array.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return all distinct values from the specified 1-dimensional array in increasing order
	 */
	default int[] distinctSorted(int... t) {
		return IntStream.of(t).sorted().distinct().toArray();
	}

	/**
	 * Returns in increasing order all distinct values from the specified array such that the specified predicate accepts them.
	 * 
	 * @param t
	 *            an array of integers
	 * @param p
	 *            a predicate allowing us to test if a value must be accepted
	 * @return in increasing order all distinct values from the specified array such that the specified predicate accepts them
	 */
	default int[] distinctSorted(int[] t, Intx1Predicate p) {
		return IntStream.of(t).sorted().distinct().filter(v -> p.test(v)).toArray();
	}

	/**
	 * Returns in increasing order all distinct values from the specified 2-dimensional array.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @return all distinct values from the specified 2-dimensional array in increasing order
	 */
	default int[] distinctSorted(int[][] m) {
		return Stream.of(m).map(t -> Arrays.stream(t)).flatMapToInt(i -> i).distinct().sorted().toArray();
	}

	/**
	 * Returns a 2-dimensional array obtained from the specified 1-dimensional array after replacing each value {@code v} into a pair {@code (v,w)}
	 * where {@code w} is the result of applying the specified function on {@code v}. For example, if the specified operator is the increment
	 * operator, then {@code [2,4,8]} becomes {@code [[2,3],[4,5],[9,9]]} after calling this function.
	 * 
	 * @param t
	 *            a 1 -dimensional array of integers
	 * @param f
	 *            a unary operator on integers
	 * @return a 2-dimensional array obtained after adding a column computed by the specified operator
	 */
	default int[][] addColumn(int[] t, IntUnaryOperator f) {
		return IntStream.of(t).mapToObj(p -> tuple(p, f.applyAsInt(p))).toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified 1-dimensional array by replacing each value {@code v} at
	 * index {@code i} with a pair {@code (i,v)}. For example, numbering {@code [2,4,1]} yields {@code [[0,2],[1,4],[2,1]]}.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	default int[][] number(int... t) {
		return IntStream.range(0, t.length).mapToObj(i -> tuple(i, t[i])).toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified 1-dimensional array by replacing each value {@code v} at
	 * index {@code i} into a pair {@code (i,v)}, provided that the specified predicate accepts the index {@code i}. For example, if the predicate
	 * only accepts odd integers, numbering {@code [2,4,1]} yields {@code [[0,2],[2,1]]}.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code i} must be considered
	 * @return a 2-dimensional array of integers
	 */
	default int[][] number(int[] t, Intx1Predicate p) {
		return IntStream.range(0, t.length).filter(i -> p.test(i)).mapToObj(i -> tuple(i, t[i])).toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified 2-dimensional array by collecting triplets {@code (i,j,v)}
	 * where {@code v} is the value v at index {@code (i,j)} of the array. For example, numbering {@code [[1,2,1],[2,5,1]]} yields
	 * {@code [[0,0,1],[0,1,2],[0,2,1],[1,0,2],[1,1,5],[1,2,1]]}.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @param p
	 *            a predicate allowing us to test if a value at index {@code (i,j)} must be considered
	 * @return a 2-dimensional array of integers
	 */
	default int[][] numberx2(int[][] m, Intx2Predicate p) {
		return IntStream.range(0, m.length).mapToObj(i -> IntStream.range(0, m[i].length).filter(j -> p.test(i, j)).mapToObj(j -> vals(i, j, m[i][j])))
				.flatMap(s -> s).toArray(int[][]::new);
	}

	/**
	 * Returns an array of tuples in lexicographic order, and without any duplicates.
	 * 
	 * @param tuples
	 *            an array of tuples
	 * @return an array of tuples in lexicographic order, and without any duplicates
	 */
	default int[][] clean(int[]... tuples) {
		Set<int[]> set = new TreeSet<>(Utilities.lexComparatorInt);
		for (int i = 0; i < tuples.length - 1; i++)
			if (set.size() > 0)
				set.add(tuples[i]);
			else if (Utilities.lexComparatorInt.compare(tuples[i], tuples[i + 1]) >= 0)
				for (int j = 0; j <= i; j++)
					set.add(tuples[j]);
		if (set.size() > 0)
			set.add(tuples[tuples.length - 1]);
		return set.size() == 0 ? tuples : set.stream().toArray(int[][]::new);
	}

	/**
	 * Returns an array of tuples in lexicographic order, and without any duplicates.
	 * 
	 * @param tuples
	 *            a list of tuples
	 * @return an array of tuples in lexicographic order, and without any duplicates
	 */
	default int[][] clean(List<int[]> tuples) {
		return clean(tuples.stream().toArray(int[][]::new));
	}

	/**
	 * Returns an array of tuples in lexicographic order, and without any duplicates.
	 * 
	 * @param tuples
	 *            an array of tuples
	 * @return an array of tuples in lexicographic order, and without any duplicates
	 */
	default String[][] clean(String[][] tuples) {
		Set<String[]> set = new TreeSet<>(Utilities.lexComparatorString);
		for (int i = 0; i < tuples.length - 1; i++)
			if (set.size() > 0)
				set.add(tuples[i]);
			else if (Utilities.lexComparatorString.compare(tuples[i], tuples[i + 1]) >= 0)
				for (int j = 0; j <= i; j++)
					set.add(tuples[j]);
		if (set.size() > 0)
			set.add(tuples[tuples.length - 1]);
		return set.size() == 0 ? tuples : set.stream().toArray(String[][]::new);
	}

	// default String[][] clean(List<String[]> tuples) {
	// return clean(tuples.stream().toArray(String[][]::new));
	// }

	/**
	 * Builds an empty integer table that can be fed with tuples.
	 * 
	 * @return an object {@code TableInteger}
	 */
	default TableInteger table() {
		return new TableInteger();
	}

	/**
	 * Builds an integer table containing the specified tuple.
	 * 
	 * @param tuple
	 *            a tuple
	 * @return an integer table with one tuple
	 */
	default TableInteger table(int... tuple) {
		return new TableInteger().add(tuple);
	}

	/**
	 * Builds an integer table containing the specified tuples.
	 * 
	 * @param tuples
	 *            a sequence of tuples
	 * @return an integer table with the specified tuples
	 */
	default TableInteger table(int[]... tuples) {
		return new TableInteger().add(tuples);
	}

	/**
	 * Builds an integer table after parsing the specified string. The string is what can be expected in XCSP3, as for example {@code (1,2)(1,3)(2,3)}
	 * for an integer table.
	 * 
	 * @param tuples
	 *            a string representing a sequence of integer tuples.
	 * @return a table containing the parsed specified tuples
	 */
	default TableInteger table(String tuples) {
		return new TableInteger().add(tuples);
	}

	// /**
	// * Builds an integer table containing the tuples respecting the specified tree
	// *
	// * @param tree
	// * a syntactic tree
	// * @return an integer table with the tuples respecting the specified tree
	// */
	// default TableInteger table(XNodeParent<IVar> tree) {
	// return (TableInteger) imp().tableFor(tree);
	// }

	/**
	 * Builds an empty symbolic table that can be fed with tuples.
	 * 
	 * @return an object {@code TableSymbolic}
	 */
	default TableSymbolic tableSymbolic() {
		return new TableSymbolic();
	}

	/**
	 * Builds a symbolic table containing the specified tuple.
	 * 
	 * @param tuple
	 *            a tuple
	 * @return a symbolic table with one tuple
	 */
	default TableSymbolic tableSymbolic(String... tuple) {
		return new TableSymbolic().add(tuple);
	}

	/**
	 * Builds a symbolic table containing the specified tuples.
	 * 
	 * @param tuples
	 *            a sequence of tuples
	 * @return a symbolic table with the specified tuples
	 */
	default TableSymbolic tableSymbolic(String[]... tuples) {
		return new TableSymbolic().add(tuples);
	}

	/**
	 * Builds a symbolic table after parsing the specified string. The string is what can be expected in XCSP3, as for example
	 * {@code (green,red)(yellow,blue)} for a symbolic table.
	 * 
	 * @param tuples
	 *            a string representing a sequence of symbolic tuples.
	 * @return a table containing the parsed specified tuples
	 */
	default TableSymbolic tableSymbolic(String tuples) {
		return new TableSymbolic().addSequence(tuples);
	}

	/**
	 * Builds and returns an array of object {@code Transition} after parsing the specified string. The string is what can be expected in XCSP3, as
	 * for example {@code "(q0,0,q1)(q0,2,q2)(q1,0,q3)"}.
	 * 
	 * @param transitions
	 *            a string representing the transitions
	 * @return an array of objects {@code Transition}
	 */
	default Transition[] transitions(String transitions) {
		Stream<String> st = Stream.of(transitions.trim().split(Constants.DELIMITER_LISTS)).skip(1);
		return st.map(tok -> {
			String[] t = tok.split("\\s*,\\s*");
			control(t.length == 3, "Pb with a transition, which is not formed of 3 peices");
			return new Transition(t[0], Utilities.isInteger(t[1]) ? Integer.parseInt(t[1]) : t[1], t[2]);

		}).toArray(Transition[]::new);
	}

	/**
	 * Builds an {@code Automaton} from the specified transitions, start and final states.
	 * 
	 * @param transitions
	 *            the transitions of the automaton
	 * @param startState
	 *            the start state
	 * @param finalStates
	 *            the array with the final states
	 * @return an automaton
	 */
	default Automaton automaton(Transition[] transitions, String startState, String[] finalStates) {
		return new Automaton(transitions, startState, finalStates);
	}

	/**
	 * Inserts the specified object in the specified array at the specified index. The new array is returned.
	 * 
	 * @param t
	 *            a 1 -dimensional array
	 * @param object
	 *            an object to be inserted
	 * @param index
	 *            the index at which the object must be inserted
	 * @return an array obtained after the insertion of the specified object in the specified array at the specified index
	 */
	default <T> T[] addObject(T[] t, T object, int index) {
		control(t != null && object != null, "The two first parameters must be diffrent from null");
		control(0 <= index && index <= t.length, "The sepcified index is not valid");
		T[] tt = (T[]) Array.newInstance(object.getClass(), t.length + 1);
		for (int i = 0; i < tt.length; i++)
			tt[i] = i < index ? t[i] : i == index ? object : t[i - 1];
		return tt;
	}

	default XNodeParent<Var>[] trees(XNodeParent<?>... trees) {
		return (XNodeParent<Var>[]) trees;
	}

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
	default XDomInteger dom(int[] values, Intx1Predicate p) {
		control(values.length > 0, "At least one value must be spedified");
		values = IntStream.of(values).sorted().distinct().filter(v -> p == null || p.test(v)).toArray();
		return new XDomInteger(values);
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param values
	 *            a 1-dimensional array of integers
	 * @return an integer domain composed of the sorted distinct values that come from the specified array
	 */
	default XDomInteger dom(int[] values) {
		return dom(values, null);
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified values.
	 * 
	 * @param val
	 *            a first integer (value)
	 * @param otherVals
	 *            a sequence of other integers (values)
	 * @return an integer domain composed of the sorted distinct values that come from the specified values
	 */
	default XDomInteger dom(int val, int... otherVals) {
		return dom(IntStream.range(0, otherVals.length + 1).map(i -> i == 0 ? val : otherVals[i - 1]).toArray());
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified collection.
	 * 
	 * @param values
	 *            a collection of integers (values)
	 * @return an integer domain composed of the sorted distinct values that come from the specified collection
	 */
	default XDomInteger dom(Collection<Integer> values) {
		return dom(values.stream().mapToInt(i -> i).toArray());
	}

	/**
	 * Returns an integer domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param m
	 *            a 2-dimensional array of variables
	 * @return an integer domain composed of the sorted distinct values that come from the specified array
	 */
	default XDomInteger dom(int[][] m) {
		return dom(Stream.of(m).map(t -> Arrays.stream(t)).flatMapToInt(i -> i).toArray());
	}

	/**
	 * Returns an integer domain composed of the values contained in the specified range.
	 * 
	 * @param range
	 *            the range of values to be considered for the domain
	 * @return an integer domain composed of the values contained in the specified range
	 */
	default XDomInteger dom(Range range) {
		return range.step == 1 ? new XDomInteger(range.minIncluded, range.maxIncluded) : new XDomInteger(range.toArray());
	}

	/**
	 * Returns a symbolic domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param values
	 *            a 1-dimensional array of strings
	 * @return a symbolic domain composed of the sorted distinct values that come from the specified array
	 */
	default XDomSymbolic dom(String[] values) {
		control(values.length > 0, "At least one value must be spedified");
		values = Stream.of(values).distinct().toArray(String[]::new);
		return new XDomSymbolic(values);
	}

	/**
	 * Returns a symbolic domain composed of the sorted distinct values that come from the specified values.
	 * 
	 * @param val
	 *            a first string (value)
	 * @param otherVals
	 *            a sequence of other strings (values)
	 * @return a symbolic domain composed of the sorted distinct values that come from the specified values
	 */
	default XDomSymbolic dom(String val, String... otherVals) {
		return new XDomSymbolic(IntStream.range(0, otherVals.length + 1).mapToObj(i -> i == 0 ? val : otherVals[i - 1]).toArray(String[]::new));
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
	 * @param minIncluded
	 *            the lower bound (inclusive) of this range
	 * @param maxIncluded
	 *            the upper bound (inclusive) of this range
	 * @param step
	 *            the step of this range
	 * @return the object {@code Range} that represents an interval of values (while considering the specified step)
	 * 
	 */
	default Range range(int minIncluded, int maxIncluded, int step) {
		return imp().range(minIncluded, maxIncluded, step);
	}

	/**
	 * Constructs an object {@code Range} from the specified bounds (using implicitly a step equal to 1).
	 * 
	 * @param minIncluded
	 *            the lower bound (inclusive) of this range
	 * @param maxIncluded
	 *            the upper bound (inclusive) of this range
	 * @return the object {@code Range} that represents an interval of values
	 */
	default Range range(int minIncluded, int maxIncluded) {
		return imp().range(minIncluded, maxIncluded);
	}

	/**
	 * Constructs an object {@code Range} from the specified length (using implicitly a lower bound equal to 0 and a step equal to 1).
	 * 
	 * @param length
	 *            the length of this range
	 * @return the object {@code Range} that represents an interval of values, from 0 to the specified value (excluded)
	 */
	default Range range(int length) {
		return imp().range(length);
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
	default Var var(String id, XDomInteger dom, String note, TypeClass... classes) {
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
	default Var var(String id, XDomInteger dom, TypeClass... classes) {
		return var(id, dom, null, classes);
	}

	/**
	 * Builds a stand-alone symbolic variable with the specified id, domain, note (short comment) and classes. Use methods {@code dom()} for building
	 * symbolic domains. For example:
	 * 
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	VarSymbolic x = var("x", dom("red", "green", "blue"), "x is the color of the house");
	 * }
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the variable
	 * @param dom
	 *            the symbolic domain of the variable
	 * @param note
	 *            a short comment about the variable
	 * @param classes
	 *            the tags (possibly, none) associated with the variable
	 * @return a stand-alone symbolic variable
	 */
	default VarSymbolic var(String id, XDomSymbolic dom, String note, TypeClass... classes) {
		VarSymbolic x = imp().buildVarSymbolic(id, dom);
		if (x != null)
			imp().varEntities.newVarAloneEntity(id, x, note, classes);
		return x;
	}

	/**
	 * Builds a stand-alone symbolic variable with the specified id, domain and classes. Use methods {@code dom()} for building symbolic domains. For
	 * example:
	 * 
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	VarSymbolic x = var("x", dom("red", "green", "blue"));
	 * }
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the variable
	 * @param dom
	 *            the symbolic domain of the variable
	 * @param classes
	 *            the tags (possibly, none) associated with the variable
	 * @return a stand-alone symbolic variable
	 */
	default VarSymbolic var(String id, XDomSymbolic dom, TypeClass... classes) {
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
	default Var[] array(String id, Size1D size, IntToDomInteger f, String note, TypeClass... classes) {
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
	default Var[] array(String id, Size1D size, IntToDomInteger f, TypeClass... classes) {
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
	default Var[] array(String id, Size1D size, XDomInteger dom, String note, TypeClass... classes) {
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
	default Var[] array(String id, Size1D size, XDomInteger dom, TypeClass... classes) {
		return array(id, size, i -> dom, null, classes);
	}

	/**
	 * Builds a 1-dimensional array of symbolic variables with the specified id, size, note (short comment) and classes. Use Method {@code size(int)}
	 * for building the size (length) of the array. The specified function {@code f} associates a symbolic domain with each variable at index
	 * {@code i} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the following
	 * example, the first five variables have a domain containing 3 values whereas the next five variables have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code VarSymbolic[] = arraySymbolic("x", size(10), i -> i < 5 ? dom("red","gren","blue") : dom("yellow","orange"), 
	 *    "x[i] is the color of the ith rabbit");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param f
	 *            a function that associates a symbolic domain with any possible index {@code i} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of symbolic variables
	 */
	default VarSymbolic[] arraySymbolic(String id, Size1D size, IntToDomSymbolic f, String note, TypeClass... classes) {
		VarSymbolic[] t = imp().fill(id, size, f, (VarSymbolic[]) Array.newInstance(imp().classVS(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, t, note, classes); // TODO indicate not same domains ?
		return t;
	}

	/**
	 * Builds a 1-dimensional array of symbolic variables with the specified id, size, and classes. Use Method {@code size(int)} for building the size
	 * (length) of the array. The specified function {@code f} associates a symbolic domain with each variable at index {@code i} of the array. In
	 * case the specified function {@code f} return the value {@code null}, the variable is not built. In the following example, the first five
	 * variables have a domain containing 3 values whereas the next five variables have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code VarSymbolic[] = arraySymbolic("x", size(10), i -> i < 5 ? dom("red","gren","blue") : dom("yellow","orange"));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the length of the array
	 * @param f
	 *            a function that associates a symbolic domain with any possible index {@code i} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 1-dimensional array of symbolic variables
	 */
	default VarSymbolic[] arraySymbolic(String id, Size1D size, IntToDomSymbolic f, TypeClass... classes) {
		return arraySymbolic(id, size, f, null, classes);
	}

	/**
	 * Adds a 1-dimensional array of symbolic variables with the specified id, size, note and classes. Each variable of the array has the specified
	 * domain.
	 */

	/**
	 * Builds a 1-dimensional array of symbolic variables with the specified id, size, domain, note and classes. Use Method {@code size(int)} for
	 * building the size (length) of the array. Each variable of the array has the specified symbolic domain. In the following example, the ten
	 * variables have a domain containing 3 values:
	 * 
	 * <pre>
	 * {@code VarSymbolic[] = arraySymbolic("x", size(10), dom("red","gren","blue"),"x[i] is the color of the ith rabbit");}
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
	 * @return a 1-dimensional array of symbolic variables
	 */
	default VarSymbolic[] arraySymbolic(String id, Size1D size, XDomSymbolic dom, String note, TypeClass... classes) {
		return arraySymbolic(id, size, i -> dom, note, classes);
	}

	/**
	 * Builds a 1-dimensional array of symbolic variables with the specified id, size, domain, and classes. Use Method {@code size(int)} for building
	 * the size (length) of the array. Each variable of the array has the specified symbolic domain. In the following example, the ten variables have
	 * a domain containing 3 values:
	 * 
	 * <pre>
	 * {@code VarSymbolic[] = arraySymbolic("x", size(10), dom("red","gren","blue"));}
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
	 * @return a 1-dimensional array of symbolic variables
	 */
	default VarSymbolic[] arraySymbolic(String id, Size1D size, XDomSymbolic dom, TypeClass... classes) {
		return arraySymbolic(id, size, i -> dom, null, classes);
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
	default Var[][] array(String id, Size2D size, Intx2ToDomInteger f, String note, TypeClass... classes) {
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
	default Var[][] array(String id, Size2D size, Intx2ToDomInteger f, TypeClass... classes) {
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
	default Var[][] array(String id, Size2D size, XDomInteger dom, String note, TypeClass... classes) {
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
	default Var[][] array(String id, Size2D size, XDomInteger dom, TypeClass... classes) {
		return array(id, size, (i, j) -> dom, null, classes);
	}

	/**
	 * Builds a 2-dimensional array of symbolic variables with the specified id, size, note (short comment) and classes. Use Method
	 * {@code size(int,int)} for building the size (length of each dimension) of the array. The specified function {@code f} associates a symbolic
	 * domain with each variable at index {@code (i,j)} of the array. In case the specified function {@code f} return the value {@code null}, the
	 * variable is not built. In the following example, some variables have a domain containing 3 values whereas others have a domain containing two
	 * values only:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][] = arraySymbolic("x", size(10, 5), (i,j) -> i < j ? dom("red","green","blue") : dom("yellow","orange"), 
	 *   "x[i][j] is the color of the jth rabbit at the ith hole");}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates a symbolic domain with any possible index {@code (i,j)} of a variable in the array
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of symbolic variables
	 */
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, Intx2ToDomSymbolic f, String note, TypeClass... classes) {
		VarSymbolic[][] m = imp().fill(id, size, f, (VarSymbolic[][]) Array.newInstance(imp().classVS(), size.lengths));
		imp().varEntities.newVarArrayEntity(id, size, m, note, classes); // TODO indicate not same domains somewhere ?
		return m;
	}

	/**
	 * Builds a 2-dimensional array of symbolic variables with the specified id, size, and classes. Use Method {@code size(int,int)} for building the
	 * size (length of each dimension) of the array. The specified function {@code f} associates a symbolic domain with each variable at index
	 * {@code (i,j)} of the array. In case the specified function {@code f} return the value {@code null}, the variable is not built. In the following
	 * example, some variables have a domain containing 10 values whereas others have a domain containing two values only:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][] = arraySymbolic("x", size(10, 5), (i,j) -> i < j ? dom("red","green","blue") : dom("yellow","orange"));}
	 * </pre>
	 * 
	 * @param id
	 *            the id (unique name) of the array
	 * @param size
	 *            the size (length of each dimension) of the array
	 * @param f
	 *            a function that associates a symbolic domain with any possible index {@code (i,j)} of a variable in the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 2-dimensional array of symbolic variables
	 */
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, Intx2ToDomSymbolic f, TypeClass... classes) {
		return arraySymbolic(id, size, f, null, classes);
	}

	/**
	 * Builds a 2-dimensional array of symbolic variables with the specified id, size, domain, note (short comment) and classes. Use Method
	 * {@code size(int,int)} for building the size (length of each dimension) of the array. Each variable of the array has the specified symbolic
	 * domain. In the following example, all variables have a domain containing 3 values:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][] = arraySymbolic("x", size(10, 5), dom("red","green","blue"), 
	 *   "x[i][j] is the color of the jth rabbit at the ith hole");}
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
	 * @return a 2-dimensional array of symbolic variables
	 */
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, XDomSymbolic dom, String note, TypeClass... classes) {
		return arraySymbolic(id, size, (i, j) -> dom, note, classes);
	}

	/**
	 * Builds a 2-dimensional array of symbolic variables with the specified id, size, domain, and classes. Use Method {@code size(int,int)} for
	 * building the size (length of each dimension) of the array. Each variable of the array has the specified symbolic domain. In the following
	 * example, all variables have a domain containing 3 values:
	 * 
	 * <pre>
	 * {@code VarSymbolic[][] = arraySymbolic("x", size(10, 5), dom("red","green","blue"));}
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
	 * @return a 2-dimensional array of symbolic variables
	 */
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, XDomSymbolic dom, TypeClass... classes) {
		return arraySymbolic(id, size, (i, j) -> dom, null, classes);
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
	default Var[][][] array(String id, Size3D size, Intx3ToDomInteger f, String note, TypeClass... classes) {
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
	default Var[][][] array(String id, Size3D size, Intx3ToDomInteger f, TypeClass... classes) {
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
	default Var[][][] array(String id, Size3D size, XDomInteger dom, String note, TypeClass... classes) {
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
	default Var[][][] array(String id, Size3D size, XDomInteger dom, TypeClass... classes) {
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
	default Var[][][][] array(String id, Size4D size, Intx4ToDomInteger f, String note, TypeClass... classes) {
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
	default Var[][][][] array(String id, Size4D size, Intx4ToDomInteger f, TypeClass... classes) {
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
	default Var[][][][] array(String id, Size4D size, XDomInteger dom, String note, TypeClass... classes) {
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
	 * @param note
	 *            a short comment about the array
	 * @param classes
	 *            the tags (possibly, none) associated with the array
	 * @return a 4-dimensional array of integer variables
	 */
	default Var[][][][] array(String id, Size4D size, XDomInteger dom, TypeClass... classes) {
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
	default Var[][][][][] array(String id, Size5D size, Intx5ToDomInteger f, String note, TypeClass... classes) {
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
	default Var[][][][][] array(String id, Size5D size, Intx5ToDomInteger f, TypeClass... classes) {
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
	default Var[][][][][] array(String id, Size5D size, XDomInteger dom, String note, TypeClass... classes) {
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
	default Var[][][][][] array(String id, Size5D size, XDomInteger dom, TypeClass... classes) {
		return array(id, size, (i, j, k, l, m) -> dom, null, classes);
	}

	/*********************************************************************************************/
	/**** Posting Constraints */
	/*********************************************************************************************/

	/**
	 * Class that is useful to represent objects wrapping indexing information. Basically, this is used as syntactic sugar.
	 */
	public static class Index {

		public Var variable;

		public TypeRank rank;

		public Index(Var index, TypeRank rank) {
			this.variable = index;
			this.rank = rank;
		}

		public Index(Var index) {
			this(index, TypeRank.ANY);
		}

	}

	/**
	 * Class that is useful to represent objects wrapping indexing information. Basically, this is used as syntactic sugar.
	 */
	public static class StartIndex {

		public int value;

		public StartIndex(int val) {
			this.value = val;
		}
	}

	public static interface Occurrences {

		public static class OccurrencesIntBasic implements Occurrences {
			public int occurs;

			public OccurrencesIntBasic(int occurs) {
				this.occurs = occurs;
			}
		}

		public static class OccurrencesIntRange implements Occurrences {
			public int occursMin;
			public int occursMax;

			public OccurrencesIntRange(int occursMin, int occursMax) {
				this.occursMin = occursMin;
				this.occursMax = occursMax;
			}
		}

		public static class OccurrencesIntSimple implements Occurrences {
			public int[] occurs;

			public OccurrencesIntSimple(int[] occurs) {
				this.occurs = occurs;
			}
		}

		public static class OccurrencesIntDouble implements Occurrences {
			public int[] occursMin;
			public int[] occursMax;

			public OccurrencesIntDouble(int[] occursMin, int[] occursMax) {
				this.occursMin = occursMin;
				this.occursMax = occursMax;
			}
		}

		public static class OccurrencesVar implements Occurrences {
			public Var[] occurs;

			public OccurrencesVar(Var[] occurs) {
				this.occurs = occurs;
			}
		}
	}

	/**
	 * Returns an object {@code Occurrences} that represents the number of times each value of a given set in a certain context (when posting a
	 * constraint {@code cardinality}) must occur.
	 * 
	 * @param occurs
	 *            an integer
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occursEachExactly(int occurs) {
		return new OccurrencesIntBasic(occurs);
	}

	/**
	 * Returns an object {@code Occurrences} that represents the bounds about the number of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur each.
	 * 
	 * @param occursMin
	 *            the lower bound for the number of occurrences
	 * @param occursMax
	 *            the upper bound for the number of occurrences
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occursEachBetween(int occursMin, int occursMax) {
		return new OccurrencesIntRange(occursMin, occursMax);
	}

	/**
	 * Returns an object {@code Occurrences} that represents the respective numbers of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param o1
	 *            a first integer
	 * @param o2
	 *            a second integer
	 * @param o3
	 *            a third integer
	 * @param others
	 *            a sequence of other integers
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occurrences(int o1, int o2, int o3, int... others) {
		return new OccurrencesIntSimple(IntStream.range(0, others.length + 3).map(i -> i == 0 ? o1 : i == 1 ? o2 : i == 2 ? o3 : others[i - 3]).toArray());
	}

	/**
	 * Returns an object {@code Occurrences} that represents the respective number of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param occurs
	 *            a 1-dimensional array of integers representing the respective numbers of occurrences
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occurrences(int[] occurs) {
		return new OccurrencesIntSimple(occurs);
	}

	/**
	 * Returns an object {@code Occurrences} that represents the respective bounds about the number of times each value of a given set in a certain
	 * context (when posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param occursMin
	 *            the lower bounds for the number of occurrences
	 * @param occursMax
	 *            the upper bounds for the number of occurrences
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occursBetween(int[] occursMin, int[] occursMax) {
		return new OccurrencesIntDouble(occursMin, occursMax);
	}

	/**
	 * Returns an object {@code Occurrences} that represents the respective numbers of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param occurs
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occurrences(Var... occurs) {
		return new OccurrencesVar(occurs);
	}

	/**
	 * Returns an object {@code Condition} composed of the specified relational operator and value (right operand). Such object can be used when
	 * posting constraints.
	 * 
	 * @param op
	 *            a relational operator
	 * @param limit
	 *            an integer
	 * @return an object {@code Condition} composed of the specified relational operator and value
	 */
	default Condition condition(TypeConditionOperatorRel op, long limit) {
		return new ConditionVal(op, limit);
	}

	/**
	 * Returns an object {@code Condition} composed of the specified relational operator and variable (right operand). Such object can be used when
	 * posting constraints.
	 * 
	 * @param op
	 *            a relational operator
	 * @param limit
	 *            an integer variable
	 * @return an object {@code Condition} composed of the specified relational operator and variable
	 */
	default Condition condition(TypeConditionOperatorRel op, Var limit) {
		return new ConditionVar(op, limit);
	}

	/**
	 * Returns an object {@code Condition} composed of the specified set operator and interval (defined from the two specified bounds). Such object
	 * can be used when posting constraints.
	 * 
	 * @param op
	 *            a set operator
	 * @param range
	 *            a range (interval) of values
	 * @param max_vars
	 *            the upper bound (inclusive) of the interval
	 * @return an object {@code Condition} composed of the specified set operator and interval
	 */
	default Condition condition(TypeConditionOperatorSet op, Range range) {
		control(range.step == 1 && range.length() >= 1, "Bad form of range");
		return new ConditionIntvl(op, range.minIncluded, range.maxIncluded);
	}

	/**
	 * Returns an object {@code Condition} composed of the specified set operator and array of integers (right operand). Such object can be used when
	 * posting constraints.
	 * 
	 * @param op
	 *            a set operator
	 * @param values
	 *            an array of integers
	 * @return an object {@code Condition} composed of the specified set operator and array of integers
	 */
	default Condition condition(TypeConditionOperatorSet op, int[] values) {
		return new ConditionIntset(op, distinctSorted(values));
	}

	/**
	 * Returns an object {@code Index} wrapping the specified variable (and the default value ANY). Such object can be used when posting constraints.
	 * 
	 * @param variable
	 *            an integer variable
	 * @return an object {@code Index} wrapping the specified variable
	 */
	default Index index(Var variable) {
		return new Index(variable);
	}

	/**
	 * Returns an object {@code Index} wrapping the specified variable and the specified rank type. In the context of looking for an object with a
	 * certain property P in a structure (typically, a variable with property P in a 1-dimensional array of variables), the value of {@code rank}
	 * indicates if {@code variable} must be:
	 * <ul>
	 * <li>the smallest valid index number (FIRST), meaning that {@code variable} must refer to the first object in the structure with property P</li>
	 * <li>the greatest valid index number (LAST), meaning that {@code variable} must refer to the last variable in the structure with property P</li>
	 * <li>or any valid index number (ANY), meaning that {@code variable} can refer to any variable in the structure with property P.</li>
	 * </ul>
	 * 
	 * @param variable
	 *            an integer variable
	 * @param rank
	 *            the way indexing search is considered (FIRST, LAST or ANY)
	 * @return an object {@code Index} wrapping the specified variable and the specified rank type
	 */
	default Index index(Var variable, TypeRank rank) {
		return new Index(variable, rank);
	}

	/**
	 * Returns an object {@code StartIndex} wrapping the specified value. Such object can be used when posting constraints.
	 * 
	 * @param value
	 *            an integer
	 * @return an object {@code StartIndex} wrapping the specified value
	 */
	default StartIndex startIndex(int value) {
		return new StartIndex(value);
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
	 * Builds a disentailed symbolic constraint, i.e., a special constraint that always returns {@code false}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by means of method chaining.
	 */
	default CtrEntity ctrFalse(VarSymbolic[] scp) {
		return extension(scp, new String[0][], POSITIVE);
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

	/**
	 * Builds an entailed symbolic constraint, i.e., a special constraint that always returns {@code true}. For example, it may be useful to achieve
	 * some sophisticated tasks related to some forms of consistency.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by means of method chaining.
	 */
	default CtrEntity ctrTrue(VarSymbolic[] scp) {
		return extension(scp, new String[0][], NEGATIVE);
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

	// or a string (the latter for symbolic reasoning)
	/**
	 * Returns the root of a syntactic tree built with the unary operator <code>abs</code> applied to the specified operand. For example, one possible
	 * call is <code>abs(sub(x,y))</code> that represents <code>|x-y|</code>
	 * 
	 * @param operand
	 *            an object that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code XNodeParent} that represents the root of a syntactic tree
	 */
	default XNodeParent<IVar> abs(Object operand) {
		return imp().abs(operand);
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
		return imp().neg(operand);
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
		return imp().sqr(operand);
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
		return imp().add(operands);
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
		return imp().sub(operand1, operand2);
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
		return imp().mul(operands);
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
		return imp().div(operand1, operand2);
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
		return imp().mod(operand1, operand2);
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
		return imp().pow(operand1, operand2);
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
		return imp().min(operands);
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
		return imp().max(operands);
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
		return imp().dist(operand1, operand2);
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
		return imp().lt(operand1, operand2);
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
		return imp().le(operand1, operand2);
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
		return imp().ge(operand1, operand2);
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
		return imp().gt(operand1, operand2);
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
		return imp().ne(operands);
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
		return imp().eq(operands);
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
		return imp().set(operands);
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
		return imp().set(operands);
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
		return imp().in(var, set);
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
		return imp().notin(var, set);
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
		return imp().not(operand);
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
		return operands.length == 1 && operands[0] instanceof Stream ? imp().and(((Stream<?>) operands[0]).toArray()) : imp().and(operands);
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
		return operands.length == 1 && operands[0] instanceof Stream ? imp().or(((Stream<?>) operands[0]).toArray()) : imp().or(operands);
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
		return imp().xor(operands);
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
		return imp().iff(operands);
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
		return imp().imp(operand1, operand2);
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
	 * @return
	 */
	default XNodeParent<IVar> ifThenElse(Object operand1, Object operand2, Object operand3) {
		return imp().ifThenElse(operand1, operand2, operand3);
	}

	// default XNodeParent<IVar> scalar(int[] t1, Object[] t2) {
	// return imp().scalar(t1, t2);
	// }

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
		return imp().lessThan(operand1, operand2);
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
		return imp().lessEqual(operand1, operand2);
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
		return imp().greaterEqual(operand1, operand2);
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
		return imp().greaterThan(operand1, operand2);
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
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity equal(Object... operands) {
		return imp().equal(operands);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code ne} applied
	 * to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code notEqual(x,y);}
	 * </pre>
	 * 
	 * is equivalent (a shortcut) to:
	 * 
	 * <pre>
	 * {@code intension(ne(x,y));}
	 * </pre>
	 * 
	 * @param operand1
	 *            the first operand that can be an integer, a variable, or an object {@code XNode}
	 * @param operand2
	 *            the second operand that can be an integer, a variable, or an object {@code XNode}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity notEqual(Object... operands) {
		return imp().notEqual(operands);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/intension">{@code intension}</a>, while considering the operator {@code imp}
	 * applied to the specified arguments. This is a modeling ease of use. As an illustration,
	 * 
	 * <pre>
	 * {@code imply(eq(x,y),lt(z,3));}
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
	default CtrEntity imply(Object operand1, Object operand2) {
		return imp().imply(operand1, operand2);
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
		return imp().belong(operand1, operand2);
	}

	// ************************************************************************
	// ***** Converting intension to extension
	// ************************************************************************

	default CtrAlone extension(XNodeParent<IVar> tree) {
		return imp().extension(tree);
	}

	default CtrAlone extension(List<XNodeParent<IVar>> trees) {
		return imp().extension(trees);
	}

	default CtrAlone extension(XNodeParent<IVar>... trees) {
		return extension(Arrays.asList(trees));
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
	default CtrEntity extension(Var[] scp, TableInteger table) {
		// control(!(table instanceof TableSymbolic), "That shouldn't be a symbolic table here");
		return extension(scp, table instanceof TableInteger ? table.toArray() : new int[0][], table.positive);
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
	 * Builds a symbolic constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the
	 * specified array of symbolic tuples, seen as either supports (when {@code positive} is {@code true}) or conflicts (when {@code positive} is
	 * {@code false}). Note that you can use constants {@code POSITIVE} and {@code NEGATIVE}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the semantics of the constraint
	 * @param positive
	 *            boolean value indicating if the tuples are supports or conflicts
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(VarSymbolic[] scp, String[][] tuples, Boolean positive) {
		return imp().extension(scp, tuples, positive);
	}

	/**
	 * Builds a symbolic constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the
	 * specified array of symbolic tuples, seen as supports.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param tuples
	 *            the tuples defining the supports of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(VarSymbolic[] scp, String[]... tuples) {
		return extension(scp, tuples, POSITIVE);
	}

	/**
	 * Builds a symbolic constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified scope and the
	 * specified table, whose elements are seen as supports. An example of integer table that can be constructed is {@code table("(a,b,a)(b,a,b)")}
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @param table
	 *            the table containing the tuples defining the supports of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(VarSymbolic[] scp, TableSymbolic table) {
		// control(!(table instanceof TableInteger), "That shouldn't be an integer table here");
		return extension(scp, table instanceof TableSymbolic ? table.toArray() : new String[0][], table.positive);
	}

	/**
	 * Builds a unary symbolic constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified variable and the
	 * specified array of symbolic values, seen as either supports (when {@code positive} is {@code true}) or conflicts (when {@code positive} is
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
	default CtrEntity extension(VarSymbolic x, String[] values, Boolean positive) {
		return extension(vars(x), dub(values), positive);
	}

	/**
	 * Builds a unary symbolic constraint <a href="http://xcsp.org/specifications/extension">{@code extension}</a> from the specified variable and the
	 * specified array of symbolic values, seen as supports.
	 * 
	 * @param x
	 *            the variable involved in this unary constraint
	 * @param values
	 *            the values defining the semantics of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity extension(VarSymbolic x, String... values) {
		return extension(x, values, POSITIVE);
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
		return imp().allDifferent(distinctSorted(list));
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
	default CtrEntity allDifferent(Var[][] list) {
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

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified symbolic variables: the
	 * variables must all take different values.
	 * 
	 * @param list
	 *            the involved symbolic variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(VarSymbolic[] list) {
		return imp().allDifferent(list);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified symbolic variables: the
	 * variables must all take different values.
	 * 
	 * @param x
	 *            a first symbolic variable
	 * @param others
	 *            a sequence of other symbolic variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(VarSymbolic x, VarSymbolic... others) {
		return allDifferent((VarSymbolic[]) vars(x, (Object) others)); // loader().varsTyped(loader().classVS(), others));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified symbolic variables: the
	 * variables must all take different values. Note that the specified 2-dimensional array of variables will be flattened (i.e., converted into a
	 * 1-dimensional array of variables). Do not mistake this form with {@code allDifferentList}
	 * 
	 * @param list
	 *            the involved symbolic variables (a 2-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(VarSymbolic[][] list) {
		return allDifferent(vars(list));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferent}</a> on the specified symbolic variables: the
	 * variables must all take different values. Note that the specified 3-dimensional array of variables will be flattened (i.e., converted into a
	 * 1-dimensional array of variables).
	 * 
	 * @param list
	 *            the involved symbolic variables (a 3-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferent(VarSymbolic[][][] list) {
		return allDifferent(vars(list));
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allDifferent">{@code allDifferentExcept}</a> on the specified integer variables:
	 * the variables must take different values, except those that take one of the specified 'zero' values.
	 * 
	 * @param list
	 *            the involved integer variables
	 * @param zeroValues
	 *            the values that must be ignored
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allDifferentExcept(Var[] list, int... zeroValues) {
		return imp().allDifferentExcept(list, zeroValues);
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

	default CtrEntity allDifferent(XNodeParent<IVar>[] trees) {
		return imp().allDifferent(trees);
	}

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
	 * Builds a constraint <a href="http://xcsp.org/specifications/allEqual">{@code allEqual}</a> on the specified symbolic variables: the variables
	 * must all take the same value. Basically, this is a modeling ease of use.
	 * 
	 * @param list
	 *            the involved symbolic variables
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allEqual(VarSymbolic... list) {
		return imp().allEqual(list);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/allEqual">{@code allEqual}</a> on the specified symbolic variables: the variables
	 * must all take the same value. Basically, this is a modeling ease of use. Note that the specified 2-dimensional array of variables will be
	 * flattened (i.e., converted into a 1-dimensional array of variables). Do not mistake this form with {@code allEqualList}
	 * 
	 * @param list
	 *            the involved symbolic variables (a 2-dimensional array)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity allEqual(VarSymbolic[][] list) {
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
	 * {@code for any i in 0..list.length-1, list[i] + lengths[i] <op> list[i+1]
	 * 
	</pre>
	
	 * 
	 * 
	 * Basically, this is a modeling ease of use.
	 * 
	 * @param list the involved integer variables
	 * 
	 * @param lengths
	 * @param operator
	 *            a relational operator (STRICTLY_INCREASING, INCREASING, DECREASING or STRICTLY_DECREASING)
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ordered(Var[] list, int[] lengths, TypeOperatorRel operator) {
		control(list.length == lengths.length + 1, "The size of list must be the size of lengths, plus 1");
		return imp().ordered(list, lengths, operator);
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
	 * @param list
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
	 * @param list
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
	 * @param list
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
	 * @param list
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
	 * @param list
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
		control(list.length == coeffs.length, "Pb because the number of variables is different form the number of coefficients");
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

	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, Condition condition) {
		return imp().sum(trees, coeffs == null ? repeat(1, trees.length) : coeffs, condition);
	}

	default CtrEntity sum(XNodeParent<IVar>[] trees, Condition condition) {
		return sum(trees, null, condition);
	}

	default CtrEntity sum(XNodeParent<IVar>[] trees, TypeConditionOperatorRel op, long limit) {
		return sum(trees, condition(op, limit));
	}

	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, TypeConditionOperatorRel op, long limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	default CtrEntity sum(XNodeParent<IVar>[] trees, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, condition(op, limit));
	}

	default CtrEntity sum(XNodeParent<IVar>[] trees, int[] coeffs, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, int[] coeffs, Condition condition) {
		XNodeParent[] atrees = trees.toArray(XNodeParent[]::new);
		return sum(atrees, coeffs, condition);
	}

	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, Condition condition) {
		return sum(trees, null, condition);
	}

	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, TypeConditionOperatorRel op, long limit) {
		return sum(trees, condition(op, limit));
	}

	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, int[] coeffs, TypeConditionOperatorRel op, long limit) {
		return sum(trees, coeffs, condition(op, limit));
	}

	default CtrEntity sum(Stream<XNodeParent<IVar>> trees, TypeConditionOperatorRel op, Var limit) {
		return sum(trees, condition(op, limit));
	}

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
	 * @param valuel
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
	 * @param valuel
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
	 * @param values
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
	 * @param values
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
	 * {@code nValues(x,IN,1,3);}
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
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValuesExcept}</a> from the specified arguments: the number of
	 * distinct values that are taken by variables of the specified list and that do not occur among those specified must respect the specified
	 * condition.
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @param exceptValues
	 *            a sequence of integers
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity nValuesExcept(Var[] list, Condition condition, int... exceptValues) {
		return imp().nValues(list, condition);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValuesExcept}</a> from the specified arguments: the number of
	 * distinct values that are taken by variables of the specified list and that do not occur among those specified must respect the condition
	 * expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,GT,3,0);}
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
	default CtrEntity nValuesExcept(Var[] list, TypeConditionOperatorRel op, int limit, int... exceptValues) {
		return nValuesExcept(list, condition(op, limit), exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValuesExcept}</a> from the specified arguments: the number of
	 * distinct values that are taken by variables of the specified list and that do not occur among those specified must respect the condition
	 * expressed by the specified operator and the specified limit. As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,GT,k,0);}
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
	default CtrEntity nValuesExcept(Var[] list, TypeConditionOperatorRel op, Var limit, int... exceptValues) {
		return nValuesExcept(list, condition(op, limit), exceptValues);
	}

	/**
	 * Builds a constraint <a href="http://xcsp.org/specifications/nValues">{@code nValuesExcept}</a> from the specified arguments: the number of
	 * distinct values that are taken by variables of the specified list and that do not occur among those specified must respect the condition
	 * expressed by the specified operator and the specified interval (range). As an illustration,
	 * 
	 * <pre>
	 * {@code nValuesExcept(x,IN,1,3,0);}
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
	default CtrEntity nValuesExcept(Var[] list, TypeConditionOperatorSet op, Range range, int... exceptValues) {
		return nValuesExcept(list, condition(op, range), exceptValues);
	}

	default CtrEntity nValuesExcept(Var[] list, TypeConditionOperatorSet op, int[] set, int... exceptValues) {
		return nValuesExcept(list, condition(op, set), exceptValues);
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

	// CtrEntity notAllEqual(IVarSymbolic... list);

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
	 * {@code occurrences(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occurrences(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurrences(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurrences(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
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
	 * {@code cardinality(x,vals(1,2),true,occurrences(y1,y2));}
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
		if (occurrences instanceof OccurrencesIntBasic)
			return imp().cardinality(clean(list), values, mustBeClosed, repeat(((OccurrencesIntBasic) occurrences).occurs, values.length));
		if (occurrences instanceof OccurrencesIntSimple)
			return imp().cardinality(clean(list), values, mustBeClosed, ((OccurrencesIntSimple) occurrences).occurs);
		if (occurrences instanceof OccurrencesIntRange)
			return imp().cardinality(clean(list), values, mustBeClosed, repeat(((OccurrencesIntRange) occurrences).occursMin, values.length),
					repeat(((OccurrencesIntRange) occurrences).occursMax, values.length));
		if (occurrences instanceof OccurrencesIntDouble)
			return imp().cardinality(clean(list), values, mustBeClosed, ((OccurrencesIntDouble) occurrences).occursMin,
					((OccurrencesIntDouble) occurrences).occursMax);
		// if (occurrences instanceof OccurrencesVar)
		return imp().cardinality(clean(list), values, mustBeClosed, clean(((OccurrencesVar) occurrences).occurs));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurrences(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occurrences(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurrences(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurrences(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
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
	 * {@code cardinality(x,vals(1,2),occurrences(y1,y2));}
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
	 * {@code occurrences(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occurrences(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurrences(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurrences(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
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
	 * {@code cardinality(x,range(1,4),occurrences(y));}
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
	 * {@code occurrences(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occurrences(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurrences(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurrences(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
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
	 * {@code cardinality(x,y,true,occurrences(z));}
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
		if (occurrences instanceof OccurrencesIntBasic)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, repeat(((OccurrencesIntBasic) occurrences).occurs, values.length));
		if (occurrences instanceof OccurrencesIntSimple)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, ((OccurrencesIntSimple) occurrences).occurs);
		if (occurrences instanceof OccurrencesIntRange)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, repeat(((OccurrencesIntRange) occurrences).occursMin, values.length),
					repeat(((OccurrencesIntRange) occurrences).occursMax, values.length));
		if (occurrences instanceof OccurrencesIntDouble)
			return imp().cardinality(clean(list), clean(values), mustBeClosed, ((OccurrencesIntDouble) occurrences).occursMin,
					((OccurrencesIntDouble) occurrences).occursMax);
		// if (occurrences instanceof OccurrencesVar)
		return imp().cardinality(clean(list), clean(values), mustBeClosed, clean(((OccurrencesVar) occurrences).occurs));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/cardinality">{@code cardinality}</a> from the specified arguments: when
	 * considering the sequence of values assigned to the variables of {@code list}, each integer in {@code values} at index {@code i} must occur a
	 * number of times {@code k_i} that respects the conditions imposed by the object {@code Occurrences}. These conditions can be stated as follows:
	 * <ul>
	 * <li>when the object {@code Occurrences} represents a simple integer {@code v}, obtained by calling Method {@code occursEachExactly(int)},
	 * {@code k_i} must be exactly {@code v}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integers {@code t}, obtained by calling Method
	 * {@code occurrences(int[])}, {@code k_i} must be exactly {@code t[i]}</li>
	 * <li>when the object {@code OccursEachBetweeen} represents an interval of integers {@code v..w}, obtained by calling Method
	 * {@code occurrences(int,int)}, {@code k_i} must belong to {@code v..w}</li>
	 * <li>when the object {@code OccursBetween} represents two 1-dimensional arrays of integers {@code t1 and t2}, obtained by calling Method
	 * {@code occurrences(int[],int[])}, {@code k_i} must belong to {@code t1[i]..t2[i]}</li>
	 * <li>when the object {@code Occurrences} represents a 1-dimensional array of integer variables {@code x}, obtained by calling Method
	 * {@code occurrences(Var[])}, {@code k_i} must be the same value as {@code x[i]}</li>
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
	 * @param occurrences
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
	 * values assigned to the variables of {@code list} must be equal to the value assigned to the variable {@code max}. Note that the array
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
	 * @param max
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Var max) {
		return maximum(list, condition(EQ, max));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that {@code startIndex.value} indicates the number used to access the first variable in {@code list} whereas {@code index.rank} indicates if
	 * {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with maximum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with maximum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with maximum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)} and for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,startIndex(1),index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,startIndex(10),index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, StartIndex startIndex, Index index) {
		return imp().maximum(list, startIndex.value, index.variable, index.rank);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/maximum">{@code maximum}</a> from the specified arguments: the maximum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that indexing starts at 0 (default value) and that {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with maximum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with maximum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with maximum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x with maximum value is given by:
	 * 
	 * <pre>
	 * {@code maximum(x,index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            the object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Index index) {
		return maximum(list, startIndex(0), index);
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, StartIndex startIndex, Index index, Condition condition) {
		return imp().maximum(list, startIndex.value, index.variable, index.rank, condition);
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
	 * {@code maximum(x,index(i),condition(GT,10));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing starting at 0, by default) with maximum value strictly greater than 10 is
	 * given by:
	 * 
	 * <pre>
	 * {@code maximum(x,index(i,FIRST),condition(GT,10));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with maximum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity maximum(Var[] list, Index index, Condition condition) {
		return maximum(list, startIndex(0), index, condition);
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
	 * values assigned to the variables of {@code list} must be equal to the value assigned to the variable {@code min}. Note that the array
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
	 * @param max_vars
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Var min) {
		return minimum(list, condition(EQ, min));
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that {@code startIndex.value} indicates the number used to access the first variable in {@code list} whereas {@code index.rank} indicates if
	 * {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with minimum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with minimum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with minimum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code StartIndex}, use Method {@code startIndex(int)} and for building an object {@code Index}, use
	 * Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing being started at 1) with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,startIndex(1),index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing being started at 10) with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,startIndex(10),index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param startIndex
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, StartIndex startIndex, Index index) {
		return imp().minimum(list, startIndex.value, index.variable, index.rank);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/minimum">{@code minimum}</a> from the specified arguments: the minimum of the
	 * values assigned to the variables of {@code list} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note
	 * that indexing starts at 0 (default value) and that {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with minimum
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with minimum
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with minimum value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i));}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x with minimum value is given by:
	 * 
	 * <pre>
	 * {@code minimum(x,index(i,FIRST));}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            the object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Index index) {
		return minimum(list, startIndex(0), index);
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, StartIndex startIndex, Index index, Condition condition) {
		return imp().minimum(list, startIndex.value, index.variable, index.rank, condition);
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
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with minimum value
	 * @param condition
	 *            an object {@code condition} composed of an operator and an operand
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity minimum(Var[] list, Index index, Condition condition) {
		return minimum(list, startIndex(0), index, condition);
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, StartIndex startIndex, Index index, int value) {
		return imp().element(list, startIndex.value, index.variable, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index.variable}. Note that indexing starts at 0 (default value) and that
	 * {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with the specified
	 * value</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with the specified
	 * value</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with the specified value.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value 10 is given by:
	 * 
	 * <pre>
	 * {@code element(x,index(i),10);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing starting at 0, by default) with value 2 is given by:
	 * 
	 * <pre>
	 * {@code element(x,index(i,FIRST),2);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, Index index, int value) {
		return element(list, startIndex(0), index, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value 10 is given by:
	 * 
	 * <pre>
	 * {@code element(x,i,10);}
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, StartIndex startIndex, Index index, Var value) {
		return imp().element(list, startIndex.value, index.variable, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value assigned to the variable of {@code list} at {@code index.variable}. Note that indexing starts at 0
	 * (default value) and that {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first variable in {@code list} with the value
	 * assigned to {@code value}</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last variable in {@code list} with the value
	 * assigned to {@code value}</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any variable in {@code list} with the value assigned to
	 * {@code value}.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value (of variable) v is given
	 * by:
	 * 
	 * <pre>
	 * {@code element(x,index(i),v);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in x (indexing starting at 0, by default) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(x,index(i,FIRST),v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integer variables
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a variable in {@code list} with the specified value
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(Var[] list, Index index, Var value) {
		return element(list, startIndex(0), index, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the specified value
	 * must be the value assigned to the variable of {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in x (indexing starting at 0, by default) with value (of variable) v is given
	 * by:
	 * 
	 * <pre>
	 * {@code element(x,i,v);}
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a value in {@code list} equal to {@code value}
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(int[] list, StartIndex startIndex, Index index, Var value) {
		return imp().element(list, startIndex.value, index.variable, index.rank, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value in {@code list} at {@code index.variable}. Note that indexing starts at 0 (default value) and that
	 * {@code index.rank} indicates if {@code index.variable} must be:
	 * <ul>
	 * <li>the smallest valid number (FIRST), meaning that {@code index.variable} must refer to the first value in {@code list} equal to
	 * {@code value}</li>
	 * <li>the greatest valid number (LAST), meaning that {@code index.variable} must refer to the last value in {@code list} equal to
	 * {@code value}</li>
	 * <li>or any valid number (ANY), meaning that {@code index.variable} can refer to any value in {@code list} equal to {@code value}.</li>
	 * </ul>
	 * <b>Important:</b> for building an object {@code Index}, use Method {@code index(Var)} or Method {@code index(Var,TypeRank)}. <br>
	 * 
	 * As an illustration, enforcing i to be the index of any value in t (indexing starting at 0, by default) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(t,index(i),v);}
	 * </pre>
	 * 
	 * Enforcing i to be the index of the first variable in t (indexing starting at 0, by default) with value (of variable) v is given by:
	 * 
	 * <pre>
	 * {@code element(t,index(i,FIRST),v);}
	 * </pre>
	 * 
	 * @param list
	 *            a 1-dimensional array of integers
	 * @param index
	 *            an object wrapping the variable corresponding to the index of a value in {@code list} equal to {@code value}
	 * @param value
	 *            an integer variable
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity element(int[] list, Index index, Var value) {
		return element(list, startIndex(0), index, value);
	}

	/**
	 * Builds a constraint <a href= "http://xcsp.org/specifications/element">{@code element}</a> from the specified arguments: the value assigned to
	 * the variable {@code value} must be the value in {@code list} at {@code index}. Note that indexing starts at 0 (default value). <br>
	 * 
	 * As an illustration, enforcing i to be the index of any variable in t (indexing starting at 0, by default) with value (of variable) v is given
	 * by:
	 * 
	 * <pre>
	 * {@code element(t,i,v);}
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list, StartIndex startIndex) {
		return imp().channel(list, startIndex.value);
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
	 *            an object wrapping the number used to access the first variable in {@code list1}
	 * @param list2
	 *            a second 1-dimensional array of integer variables
	 * @param startIndex2
	 *            an object wrapping the number used to access the first variable in {@code list2}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list1, StartIndex startIndex1, Var[] list2, StartIndex startIndex2) {
		control(list1.length <= list2.length, "The size of the first list must be less than or equal to the size of the second list");
		return imp().channel(list1, startIndex1.value, list2, startIndex2.value);
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
	 *            an object wrapping the number used to access the first variable in {@code list}
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by method chaining
	 */
	default CtrEntity channel(Var[] list, StartIndex startIndex, Var value) {
		return imp().channel(list, startIndex.value, value);
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
	default CtrEntity noOverlap(Var[] origins, int[] lengths) {
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
	default CtrEntity noOverlap(Var[] origins, Var[] lengths) {
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
	default CtrEntity noOverlap(Var[][] origins, int[][] lengths) {
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
	default CtrEntity noOverlap(Var[][] origins, Var[][] lengths) {
		return noOverlap(origins, lengths, true);
	}

	// ************************************************************************
	// ***** Constraint cumulative
	// ************************************************************************

	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, int[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, int[] lengths, int[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, int[] lengths, int[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, int[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, Var[] lengths, int[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, Var[] lengths, int[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, int[] lengths, Var[] heights, long limit) {
		return cumulative(origins, lengths, null, heights, condition(LE, limit));
	}

	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] ends, Var[] heights, Condition condition) {
		return imp().cumulative(origins, lengths, ends, heights, condition);
	}

	default CtrEntity cumulative(Var[] origins, Var[] lengths, Var[] heights, Condition condition) {
		return cumulative(origins, lengths, null, heights, condition);
	}

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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, int[] values) {
		list = list == null ? list : clean(list);
		control(list == null && values.length == 0 || list.length == values.length, "The length of list is diffrent from the length of values");
		if (values.length == 0)
			return imp().dummyConstraint("A constraint instantiation with a scope of 0 variable.");
		return imp().instantiation(list, values);
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Stream<Var> list, IntStream values) {
		return instantiation(vars(list), values.toArray());
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[] list, Range values) {
		return instantiation(list, values.toArray());
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[][] list, int[][] values, Intx2Predicate p) {
		control(list != null && values != null, "One array is null");
		return instantiation(select(list, p), select(values, p));
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity instantiation(Var[][][] list, int[][][] values, Intx3Predicate p) {
		return instantiation(select(list, p), select(values, p));
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity slide(IVar[] list, Range range, IntFunction<CtrEntity> template) {
		control(range.minIncluded == 0 && range.length() > 0, "Bad form of range");
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrEntity} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrEntity ifThenElse(CtrEntity c1, CtrEntity c2, CtrEntity c3) {
		return imp().ifThenElse(c1, c2, c3);
	}

	// ************************************************************************
	// ***** Managing loops/groups (forall) and blocks
	// ************************************************************************

	/**
	 * Builds a <a href="http://xcsp.org/specifications/blocks">block</a> by executing the specified runnable object. For example:
	 * 
	 * <pre>
	 * {@code 
	 * block(() -> { 
	 *    instantiation(x, t); 
	 *    lexMatrix(x, INCREASING);
	 * }).tag(SYMMETRY_BREAKING); }
	 * </pre>
	 * 
	 * @param r
	 *            an object to run
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray block(Runnable r) {
		return imp().manageLoop(r);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each value of the
	 * specified range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n - 1), i -> equal(x[i], x[i + 1]));}
	 * </pre>
	 * 
	 * @param range
	 *            a range of values
	 * @param c
	 *            a consumer
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Range range, IntConsumer c) {
		return imp().forall(range, c);
	}

	/** Builds constraints by considering the specified ranges and soliciting the specified function. */

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each double value of
	 * the specified double range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n).range(n), (i,j) -> lessThan(x[i], y[j]));}
	 * </pre>
	 * 
	 * @param rangesx2
	 *            a double range of values
	 * @param c2
	 *            a consumer that accepts two integers
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx2 rangesx2, Intx2Consumer c2) {
		return imp().forall(rangesx2, c2);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each triple value of
	 * the specified triple range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n).range(n).range(2), (i,j,k) -> lessThan(x[i], add(y[j],k)));}
	 * </pre>
	 * 
	 * @param rangesx3
	 *            a triple range of values
	 * @param c3
	 *            a consumer that accepts three integers
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx3 rangesx3, Intx3Consumer c3) {
		return imp().forall(rangesx3, c3);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each quadruple value
	 * of the specified quadruple range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n).range(n).range(2).range(2), (i,j,k,l) -> lessThan(add(x[i],l), add(y[j],k)));}
	 * </pre>
	 * 
	 * @param rangesx4
	 *            a quadruple range of values
	 * @param c4
	 *            a consumer that accepts four integers
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx4 rangesx4, Intx4Consumer c4) {
		return imp().forall(rangesx4, c4);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each quintuple value
	 * of the specified quintuple range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n).range(n).range(2).range(2), (i,j,k,l) -> lessThan(add(x[i],l), add(y[j],k)));}
	 * </pre>
	 * 
	 * @param rangesx5
	 *            a quintuple range of values
	 * @param c5
	 *            a consumer that accepts five integers
	 * @return an object {@code CtrArray} that wraps the built block and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx5 rangesx5, Intx5Consumer c5) {
		return imp().forall(rangesx5, c5);
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
		return minimize(type, select(list, p), select(coeffs, p));
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
		return minimize(type, select(list, p), select(coeffs, p));
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
		return maximize(type, select(list, p), select(coeffs, p));
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
		return maximize(type, select(list, p), select(coeffs, p));
	}

	/**
	 * Builds the model. You have to declare variables, constraints and objectives in this method.
	 */
	void model();

	default void prettyDisplay() {}

	// ************************************************************************
	// ***** Managing Annotations
	// ************************************************************************

	default void decisionVariables(IVar[] list) {
		imp().decisionVariables(list);
	}

}
