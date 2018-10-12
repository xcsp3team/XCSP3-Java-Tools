package org.xcsp.modeler.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Condition.ConditionVar;
import org.xcsp.common.Constants;
import org.xcsp.common.FunctionalInterfaces.Intx2Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx2Predicate;
import org.xcsp.common.FunctionalInterfaces.Intx3Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx4Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx5Consumer;
import org.xcsp.common.FunctionalInterfaces.Intx6Consumer;
import org.xcsp.common.IVar.Var;
import org.xcsp.common.Range;
import org.xcsp.common.Range.Rangesx2;
import org.xcsp.common.Range.Rangesx3;
import org.xcsp.common.Range.Rangesx4;
import org.xcsp.common.Range.Rangesx5;
import org.xcsp.common.Range.Rangesx6;
import org.xcsp.common.Types.StandardClass;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.structures.Automaton;
import org.xcsp.common.structures.Table;
import org.xcsp.common.structures.Transition;
import org.xcsp.common.structures.Transitions;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesInt;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesInt1D;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesIntRange;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesIntRange1D;
import org.xcsp.modeler.api.ProblemAPIBase.Occurrences.OccurrencesVar1D;
import org.xcsp.modeler.entities.CtrEntities.CtrArray;
import org.xcsp.modeler.implementation.ProblemIMP;

public interface ProblemAPIBase {

	// ************************************************************************
	// ***** Constants
	// ************************************************************************

	/**
	 * A constant denoting the relational operator "strictly Less Than", which is useful for expressing conditions, as for example in
	 * {@code sum(x, LT, 10)} or {@code count(x, takingValue(0), LT, 5)}.
	 */
	TypeConditionOperatorRel LT = TypeConditionOperatorRel.LT;

	/**
	 * A constant denoting the relational operator "Less than or Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, LE, 10) </code> or <code> count(x, takingValue(0), LE, 5) </code>.
	 */
	TypeConditionOperatorRel LE = TypeConditionOperatorRel.LE;

	/**
	 * A constant denoting the relational operator "Greater than or Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, GE, 10) </code> or <code> count(x, takingValue(0), GE, 5) </code>.
	 */
	TypeConditionOperatorRel GE = TypeConditionOperatorRel.GE;

	/**
	 * A constant denoting the relational operator "strictly Greater Than", which is useful for expressing conditions, as for example in
	 * <code> sum(x, GT, 10) </code> or <code> count(x, takingValue(0), GT, 5) </code>.
	 */
	TypeConditionOperatorRel GT = TypeConditionOperatorRel.GT;

	/**
	 * A constant denoting the relational operator "Not Equal", which is useful for expressing conditions, as for example in
	 * <code> sum(x, NE, 10) </code> or <code> count(x, takingValue(takingValue(0), NE, 5) </code>.
	 */
	TypeConditionOperatorRel NE = TypeConditionOperatorRel.NE;

	/**
	 * A constant denoting the relational operator "Equal", which is useful for expressing conditions, as for example in <code> sum(x, EQ, 10) </code>
	 * or <code> count(x, takingValue(0), EQ, 5) </code>.
	 */
	TypeConditionOperatorRel EQ = TypeConditionOperatorRel.EQ;

	/**
	 * A constant denoting the set operator "In", which is useful for expressing conditions, as for example in <code> sum(x, IN, 5, 10) </code> or
	 * <code> count(x, takingValue(0), IN, 5, 10) </code>.
	 */
	TypeConditionOperatorSet IN = TypeConditionOperatorSet.IN;

	/**
	 * A constant denoting the set operator "Not In", which is useful for expressing conditions, as for example in <code> sum(x, NOTIN, 5, 10) </code>
	 * or <code> count(x, takingValue(0), NOTIN, 5, 10) </code>.
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
	 * A constant, equal to Boolean.TRUE, that can be used to indicate that some variables must take their values in some set of values (e.g., for the
	 * constraint {@code cardinality}.
	 */
	Boolean CLOSED = Boolean.TRUE;

	/**
	 * The constant used for denoting "*" in integer tuples.
	 */
	int STAR = Constants.STAR_INT;

	/**
	 * The constant used for denoting "*" in integer tuples.
	 */
	int STAR_INT = Constants.STAR_INT;

	/**
	 * The constant used for denoting the symbol "*".
	 */
	String STAR_SYMBOL = Constants.STAR_SYMBOL;

	// ************************************************************************
	// ***** Base Methods
	// ************************************************************************

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
	 * Returns the name of this object (i.e., the name of this problem instance). By default, this is the name of the class implementing
	 * {@code ProblemAPI} followed by the values of all parameters (separated by the symbol '-'). The parameters are the fields, used as data, which
	 * are declared in the class implementing {@code ProblemAPI}, or the name of a JSON file, if one is given. Possibly, the name of a model variant,
	 * if used, is inserted after the name of the class.
	 */
	default String name() {
		return imp().name();
	}

	/**
	 * Returns the name of the model variant. If no model variant has been explicitly specified, it is {@code null}.
	 * 
	 * @return the name of the model variant, or ({@code null} is no model variant has been explicitly specified)
	 */
	default String modelVariant() {
		return imp().modelVariant;
	}

	/**
	 * Returns {@code true} iff the user has indicated (through the compiler by using the argument -variant=) that the model variant corresponds to
	 * the value of the specified string.
	 * 
	 * @param s
	 *            a string representing the name of a model variant
	 * @return {@code true} iff the model variant corresponds to the specified string
	 */
	default boolean modelVariant(String s) {
		return s.equals(modelVariant());
	}

	@Deprecated
	/**
	 * Use {@code modelVariant} instead.
	 */
	default boolean isModel(String s) {
		return modelVariant(s);
	}

	/**
	 * Returns a stream of objects from class T, after converting each non-empty trimmed line of the specified file
	 * 
	 * @param filename
	 *            the name of a file
	 * @param f
	 *            a function mapping each line ({@code String}) into an object of class T
	 * @return a stream of objects from class T, after converting each non-empty trimmed line of the specified file
	 */
	default <T> Stream<T> readFileLines(String filename, Function<String, T> f) {
		try {
			return Files.lines(Paths.get(filename)).map(s -> s.trim()).filter(s -> s.length() > 0).map(s -> f.apply(s));
		} catch (IOException e) {
			System.out.println("Problem with file " + filename + " (or the specified function)");
			System.exit(1);
			return null;
		}
	}

	/**
	 * Returns a stream composed of the non-empty trimmed lines ({@code String}) of the specified file
	 * 
	 * @param filename
	 *            the name of a file
	 * @return a stream composed of the non-empty trimmed lines ({@code String}) of the specified file
	 */
	default Stream<String> readFileLines(String filename) {
		return readFileLines(filename, s -> s);
	}

	// ************************************************************************
	// ***** Auxiliary methods for handling Tuples and Automata
	// ************************************************************************

	/**
	 * Returns a tuple (array) of integers from the specified parameters.
	 * 
	 * @param value
	 *            an integer
	 * @param otherValues
	 *            a sequence of integers
	 * @return a 1-dimensional array of {@code int}
	 */
	default int[] tuple(int value, int... otherValues) {
		return IntStream.range(0, otherValues.length + 1).map(i -> i == 0 ? value : otherValues[i - 1]).toArray();
	}

	@Deprecated
	/**
	 * use {@code indexing} instead
	 */
	default int[][] number(int... t) {
		return indexing(t);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified 1-dimensional array by replacing each value {@code v} at
	 * index {@code i} with a pair {@code (i,v)}. For example, indexing {@code [2,4,1]} yields {@code [[0,2],[1,4],[2,1]]}.
	 * 
	 * @param t
	 *            a 1-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	default int[][] indexing(int... t) {
		return IntStream.range(0, t.length).mapToObj(i -> tuple(i, t[i])).toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained by replacing each value {@code v} at position {@code i} of the specified stream
	 * with a pair {@code (i,v)}. For example, indexing {@code [2,4,1]} from a stream yields {@code [[0,2],[1,4],[2,1]]}.
	 * 
	 * @param t
	 *            a stream of integer values
	 * @return A 2-dimensional array of integers
	 */
	default int[][] indexing(IntStream t) {
		return indexing(t.toArray());
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified 2-dimensional array by collecting triplets {@code (i,j,v)}
	 * where {@code v} is the value v at index {@code (i,j)} of the array. For example, indexing {@code [[1,2,1],[2,5,1]]} yields
	 * {@code [[0,0,1],[0,1,2],[0,2,1],[1,0,2],[1,1,5],[1,2,1]]}.
	 * 
	 * @param m
	 *            a 2-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	default int[][] indexing(int[]... m) {
		return IntStream.range(0, m.length).mapToObj(i -> IntStream.range(0, m[i].length).mapToObj(j -> tuple(i, j, m[i][j]))).flatMap(s -> s)
				.toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained from the specified array by replacing each tuple {@code (v1,v2,...,vr)} at index
	 * {@code i} with a new tuple {@code (i,v1,v2,...,vr)}. For example, indexing {@code [[0,3,1],[2,4,1]]} yields {@code [[0,0,3,1],[1,2,4,1]]}.
	 * 
	 * @param tuples
	 *            a 2-dimensional array of integers
	 * @return a 2-dimensional array of integers
	 */
	default int[][] indexingTuples(int[]... tuples) {
		return IntStream.range(0, tuples.length).mapToObj(i -> tuple(i, tuples[i])).toArray(int[][]::new);
	}

	/**
	 * Builds and returns a 2-dimensional array of integers, obtained by replacing each tuple {@code (v1,v2,...,vr)} at position {@code i} of the
	 * specified stream with a new tuple {@code (i,v1,v2,...,vr)}. For example, indexing {@code [[0,3,1],[2,4,1]]} from a stream yields
	 * {@code [[0,0,3,1],[1,2,4,1]]}.
	 * 
	 * @param tuples
	 *            a stream of arrays of integers
	 * @return a 2-dimensional array of integers
	 */
	default int[][] indexingTuples(Stream<int[]> tuples) {
		return indexingTuples(tuples.toArray(int[][]::new));
	}

	/**
	 * Builds an empty integer table that can be fed with tuples.
	 * 
	 * @return an object {@code TableInteger}
	 */
	default Table table() {
		return new Table();
	}

	/**
	 * Builds an empty integer table which is either positive (i.e, contains supports) or negative (i.e., contains conflicts) depending on the
	 * specified Boolean value.
	 * 
	 * @param positive
	 *            a Boolean value indicating if the created table is positive ({@code true}) or negative ({@code false})
	 * @return an empty integer table
	 */
	default Table table(boolean positive) {
		return new Table().positive(positive);
	}

	/**
	 * Builds an integer table containing the specified tuple.
	 * 
	 * @param value
	 *            an integer
	 * @param otherValues
	 *            a sequence of integers
	 * @return an integer table with one tuple
	 */
	default Table table(int value, int... otherValues) {
		return new Table().add(value, otherValues);
	}

	/**
	 * Builds an integer table containing the specified tuples.
	 * 
	 * @param tuples
	 *            a sequence of tuples
	 * @return an integer table with the specified tuples
	 */
	default Table table(int[]... tuples) {
		return new Table().add(tuples);
	}

	/**
	 * Builds an integer table containing the specified tuples.
	 * 
	 * @param stream
	 *            a stream of tuples
	 * @return an integer table with the specified tuples
	 */
	default Table table(Stream<int[]> stream) {
		return new Table().add(stream);
	}

	/**
	 * Builds an integer table containing the specified tuples.
	 * 
	 * @param collection
	 *            a collection of tuples
	 * @return an integer table with the specified tuples
	 */
	default Table table(Collection<int[]> collection) {
		return new Table().add(collection.stream());
	}

	/**
	 * Builds an integer table containing all tuples from the specified table.
	 * 
	 * @param table
	 *            an existing table
	 * @return an integer table with all tuples from the specified table.
	 */
	default Table table(Table table) {
		return new Table().add(table);
	}

	/**
	 * Returns a table corresponding to the intersection of the two specified tables
	 * 
	 * @param table1
	 *            a first integer table
	 * @param table2
	 *            a second integer table
	 * @return an integer table that represents the intersection of the two specified tables
	 */
	default Table tableIntersection(Table table1, Table table2) {
		return table1.intersectionWith(table2);
	}

	/**
	 * Returns a new integer table obtained after adding a new column at the specified table. The position of the new column is specified as well as
	 * the value that must be put in that column. For example, it can be useful for adding a column with '*' in tables.
	 * 
	 * @param table
	 *            an integer table
	 * @param position
	 *            the position of a new column where the value must be put
	 * @param value
	 *            the value that must be put in the column
	 * @return an integer table obtained after adding a new column at the specified table
	 */
	default Table tableWithNewColumn(Table table, int position, int value) {
		return table.addColumnWithValue(position, value);
	}

	/**
	 * Builds an integer table after parsing the specified string. The string is what can be expected in XCSP3, as for example {@code (1,2)(1,3)(2,3)}
	 * for an integer table.
	 * 
	 * @param tuples
	 *            a string representing a sequence of integer tuples.
	 * @return a table containing the parsed specified tuples
	 */
	default Table table(String tuples) {
		return new Table().add(tuples);
	}

	// /**
	// * Builds an integer table containing all tuples (supports) respecting the specified predicate
	// *
	// * @param tree
	// * a syntactic tree
	// * @return an integer table with all tuples respecting the specified predicate (tree)
	// */
	// default TableInteger table(XNodeParent<IVar> tree) {
	// return (TableInteger) imp().tableFor(tree);
	// }

	/**
	 * Builds and returns an empty object {@code Transitions}. It is then possible to add transitions.
	 * 
	 * @return an object {@code Transitions}
	 */
	default Transitions transitions() {
		return new Transitions();
	}

	/**
	 * Builds and returns an object {@code Transitions} after parsing the specified string. The string is what can be expected in XCSP3, as for
	 * example {@code "(q0,0,q1)(q0,2,q2)(q1,0,q3)"}.
	 * 
	 * @param transitions
	 *            a string representing the transitions
	 * @return an object {@code Transitions}
	 */
	default Transitions transitions(String transitions) {
		return Transitions.parse(transitions);
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the set of final states when building an
	 * automaton.
	 * 
	 * @param finalStates
	 *            a sequence of {@code String}
	 * @return an array of {@code String}
	 */
	default String[] finalStates(String... finalStates) {
		return finalStates;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the (unique) final state when building an
	 * automaton.
	 * 
	 * @param finalState
	 *            a {@code String}
	 * @return a {@code String}
	 */
	default String[] finalState(String finalState) {
		return finalStates(finalState);
	}

	/**
	 * Builds an {@code Automaton} from the specified transitions, start and final states.
	 * 
	 * @param startState
	 *            the start state
	 * @param transitions
	 *            the transitions of the automaton
	 * @param finalStates
	 *            the final states
	 * @return an automaton
	 */
	default Automaton automaton(String startState, Transition[] transitions, String... finalStates) {
		return new Automaton(startState, transitions, finalStates);
	}

	/**
	 * Builds an {@code Automaton} from the specified transitions, start and final states.
	 * 
	 * @param startState
	 *            the start state
	 * @param transitions
	 *            the object denoting the transitions
	 * @param finalStates
	 *            the final states
	 * @return an automaton
	 */
	default Automaton automaton(String startState, Transitions transitions, String... finalStates) {
		return automaton(startState, transitions.toArray(), finalStates);
	}

	/**
	 * Builds an {@code Automaton} from the specified transitions, start and final states.
	 * 
	 * @param startState
	 *            the start state
	 * @param transitions
	 *            the string denoting the transitions
	 * @param finalStates
	 *            the final states
	 * @return an automaton
	 */
	default Automaton automaton(String startState, String transitions, String... finalStates) {
		return automaton(startState, transitions(transitions), finalStates);
	}

	// ************************************************************************
	// ***** Auxiliary classes and methods for Constraints
	// ************************************************************************

	/**
	 * Class that is useful to represent objects wrapping indexing information. Basically, this is used as syntactic sugar.
	 */
	static class Index {

		public Var var;

		public TypeRank rank;

		public Index(Var var, TypeRank rank) {
			this.var = var;
			this.rank = rank;
		}

		public Index(Var var) {
			this(var, TypeRank.ANY);
		}

	}

	static interface Occurrences {

		static class OccurrencesInt implements Occurrences {
			public int occurs;

			public OccurrencesInt(int occurs) {
				this.occurs = occurs;
			}
		}

		static class OccurrencesIntRange implements Occurrences {
			public int occursMin;
			public int occursMax;

			public OccurrencesIntRange(int occursMin, int occursMax) {
				this.occursMin = occursMin;
				this.occursMax = occursMax;
			}
		}

		static class OccurrencesInt1D implements Occurrences {
			public int[] occurs;

			public OccurrencesInt1D(int[] occurs) {
				this.occurs = occurs;
			}
		}

		static class OccurrencesIntRange1D implements Occurrences {
			public int[] occursMin;
			public int[] occursMax;

			public OccurrencesIntRange1D(int[] occursMin, int[] occursMax) {
				this.occursMin = occursMin;
				this.occursMax = occursMax;
			}
		}

		static class OccurrencesVar1D implements Occurrences {
			public Var[] occurs;

			public OccurrencesVar1D(Var[] occurs) {
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
		return new OccurrencesInt(occurs);
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
	 * Returns an object {@code Occurrences} that represents the respective number of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param occurs
	 *            a 1-dimensional array of integers representing the respective numbers of occurrences
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occurExactly(int... occurs) {
		return new OccurrencesInt1D(occurs);
	}

	@Deprecated
	/**
	 * Use {@code occurExactly} instead.
	 */
	default Occurrences occurrences(int... occurs) {
		return occurExactly(occurs);
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
	default Occurrences occurBetween(int[] occursMin, int[] occursMax) {
		return new OccurrencesIntRange1D(occursMin, occursMax);
	}

	@Deprecated
	/**
	 * Use {@code occurBetween} instead.
	 */
	default Occurrences occursBetween(int[] occursMin, int[] occursMax) {
		return occurBetween(occursMin, occursMax);
	}

	/**
	 * Returns an object {@code Occurrences} that represents the respective numbers of times each value of a given set in a certain context (when
	 * posting a constraint {@code cardinality}) must occur.
	 * 
	 * @param occurs
	 *            a 1-dimensional array of integer variables
	 * @return an object {@code Occurrences} that can be used with constraint {@code cardinality}
	 */
	default Occurrences occurExactly(Var... occurs) {
		return new OccurrencesVar1D(occurs);
	}

	@Deprecated
	/**
	 * Use {@code occurExactly} instead.
	 */
	default Occurrences occurrences(Var... occurs) {
		return occurExactly(occurs);
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
	 * @return an object {@code Condition} composed of the specified set operator and interval
	 */
	default Condition condition(TypeConditionOperatorSet op, Range range) {
		control(range.step == 1 && range.length() >= 1, "Bad form of range");
		return new ConditionIntvl(op, range.startInclusive, range.endExclusive - 1);
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
		return new ConditionIntset(op, IntStream.of(Utilities.collectInt(values)).sorted().distinct().toArray()); // singleValuesIn(values));
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
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the value used as starting index when posting some
	 * constraints (e.g., {@code element}, {@code channel} or {@code minimum}).
	 * 
	 * @param value
	 *            an integer
	 * @return the same integer
	 */
	default int startIndex(int value) {
		return value;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a limitation for some constraints (e.g.,
	 * {@code instantiation}.
	 * 
	 * @param p
	 *            a predicate
	 * @return the same predicate
	 */
	default Intx2Predicate onlyOn(Intx2Predicate p) {
		return p;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a scalar product.
	 * 
	 * @param coeffs
	 *            a 1-dimensional array (varargs) of integers
	 * @return the same 1-dimensional array of integers
	 */
	default int[] weightedBy(int... coeffs) {
		return coeffs;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a scalar product.
	 * 
	 * @param coeffs
	 *            a 1-dimensional array (varargs) of variables
	 * @return the same 1-dimensional array of variables
	 */
	default Var[] weightedBy(Var... coeffs) {
		return coeffs;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a scalar product.
	 * 
	 * @param coeffs
	 *            a 2-dimensional array (varargs) of integers
	 * @return the same 2-dimensional array of integers
	 */
	default int[][] weightedBy(int[]... coeffs) {
		return coeffs;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a scalar product.
	 * 
	 * @param coeffs
	 *            a 2-dimensional array (varargs) of variables
	 * @return the same 2-dimensional array of variables
	 */
	default Var[][] weightedBy(Var[]... coeffs) {
		return coeffs;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the target when posting some constraints (e.g.,
	 * {@code count}, {@code element} or {@code instantiation}).
	 * 
	 * @param value
	 *            an integer
	 * @return the same integer
	 */
	default int takingValue(int value) {
		return value;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the target when posting some constraints (e.g.,
	 * {@code count}, {@code element} or {@code instantiation}).
	 * 
	 * @param value
	 *            a variable
	 * @return the same variable
	 */
	default Var takingValue(Var value) {
		return value;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful when posting constraints with an "exceptional" value to be
	 * indicated.
	 * 
	 * @param value
	 *            an integer
	 * @return the same integer
	 */
	default int exceptValue(int value) {
		return value;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful when posting constraints with "exceptional" values to be
	 * indicated.
	 * 
	 * @param values
	 *            a 1-dimensional array (varargs) of integers
	 * @return the same 1-dimensional array of integers
	 */
	default int[] exceptValues(int... values) {
		control(values.length > 1);
		return values;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the target when posting some constraints (e.g.,
	 * {@code count}, {@code element} or {@code instantiation}).
	 * 
	 * @param values
	 *            a 1-dimensional array (varargs) of integers
	 * @return the same 1-dimensional array of integers
	 */
	default int[] takingValues(int... values) {
		return values;
	}

	/**
	 * Syntactic Sugar: this method returns the 1-dimensional array of integers represented by the specified range. It can be useful to emphasize the
	 * target when posting some constraints (e.g., {@code count}, {@code element} or {@code instantiation}).
	 * 
	 * @param values
	 *            an object {@code Range}
	 * @return the 1-dimensional array of integers, represented by the specified range
	 */
	default int[] takingValues(Range values) {
		return values.toArray();
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize the target when posting some constraints (e.g.,
	 * {@code instantiation}).
	 * 
	 * @param values
	 *            a 2-dimensional array (varargs) of integers
	 * @return the same 2-dimensional array of integers
	 */
	default int[][] takingValues(int[]... values) {
		return values;
	}

	/**
	 * Pure Syntactic Sugar: this method simply returns its argument. It can be useful to emphasize a specific variable when posting some constraints
	 * (e.g., {@code element}).
	 * 
	 * @param index
	 *            a variable
	 * @return the same variable
	 */
	default Var at(Var index) {
		return index;
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
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
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
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx4 rangesx4, Intx4Consumer c4) {
		return imp().forall(rangesx4, c4);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each quintuple value
	 * of the specified quintuple range. For example:
	 * 
	 * <pre>
	 * {@code forall(range(n).range(n).range(2).range(2).range(10), (i,j,k,l,m) -> lessThan(add(x[i],dist(l,m)), add(y[j],k)));}
	 * </pre>
	 * 
	 * @param rangesx5
	 *            a quintuple range of values
	 * @param c5
	 *            a consumer that accepts five integers
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx5 rangesx5, Intx5Consumer c5) {
		return imp().forall(rangesx5, c5);
	}

	/**
	 * Builds a <a href="http://xcsp.org/specifications/groups">group</a> of constraints by executing the specified consumer on each sixtuple value of
	 * the specified sixtuple range.
	 * 
	 * @param rangesx6
	 *            a sixtuple range of values
	 * @param c6
	 *            a consumer that accepts six integers
	 * @return an object {@code CtrArray} that wraps the built group and allows us to provide note and tags by method chaining
	 */
	default CtrArray forall(Rangesx6 rangesx6, Intx6Consumer c6) {
		return imp().forall(rangesx6, c6);
	}

}