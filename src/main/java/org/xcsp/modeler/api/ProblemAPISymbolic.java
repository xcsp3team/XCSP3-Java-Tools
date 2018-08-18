package org.xcsp.modeler.api;

import java.lang.reflect.Array;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.FunctionalInterfaces.IntToDomSymbolic;
import org.xcsp.common.FunctionalInterfaces.Intx2ToDomSymbolic;
import org.xcsp.common.IVar.VarSymbolic;
import org.xcsp.common.Size.Size1D;
import org.xcsp.common.Size.Size2D;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.structures.TableSymbolic;
import org.xcsp.modeler.entities.CtrEntities.CtrEntity;

public interface ProblemAPISymbolic extends ProblemAPIOnVars, ProblemAPIOnVals {
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
	 * Returns a symbolic domain composed of the sorted distinct values that come from the specified array.
	 * 
	 * @param values
	 *            a 1-dimensional array of strings
	 * @return a symbolic domain composed of the sorted distinct values that come from the specified array
	 */
	default DomSymbolic dom(String[] values) {
		control(values.length > 0, "At least one value must be spedified");
		values = Stream.of(values).distinct().toArray(String[]::new);
		return new DomSymbolic(values);
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
	default DomSymbolic dom(String val, String... otherVals) {
		return new DomSymbolic(IntStream.range(0, otherVals.length + 1).mapToObj(i -> i == 0 ? val : otherVals[i - 1]).toArray(String[]::new));
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
	default VarSymbolic var(String id, DomSymbolic dom, String note, TypeClass... classes) {
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
	default VarSymbolic var(String id, DomSymbolic dom, TypeClass... classes) {
		return var(id, dom, null, classes);
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
	default VarSymbolic[] arraySymbolic(String id, Size1D size, DomSymbolic dom, String note, TypeClass... classes) {
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
	default VarSymbolic[] arraySymbolic(String id, Size1D size, DomSymbolic dom, TypeClass... classes) {
		return arraySymbolic(id, size, i -> dom, null, classes);
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
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, DomSymbolic dom, String note, TypeClass... classes) {
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
	default VarSymbolic[][] arraySymbolic(String id, Size2D size, DomSymbolic dom, TypeClass... classes) {
		return arraySymbolic(id, size, (i, j) -> dom, null, classes);
	}

	/**
	 * Builds a disentailed symbolic constraint, i.e., a special constraint that always returns {@code false}.
	 * 
	 * @param scp
	 *            the scope of the constraint
	 * @return an object {@code CtrEntity} that wraps the built constraint and allows us to provide note and tags by means of method chaining.
	 */
	default CtrEntity ctrFalse(VarSymbolic[] scp) {
		return extension(scp, new String[0][], ProblemAPI.POSITIVE);
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
		return extension(scp, new String[0][], ProblemAPI.NEGATIVE);
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
		return extension(scp, tuples, ProblemAPI.POSITIVE);
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
		return extension(x, values, ProblemAPI.POSITIVE);
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

	// CtrEntity notAllEqual(IVarSymbolic... list);

}
