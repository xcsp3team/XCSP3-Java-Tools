/*
 * Copyright (c) 2016 XCSP3 Team (contact@xcsp.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xcsp.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xcsp.common.Condition;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeCombination;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.entries.AnyEntry.CEntry;
import org.xcsp.parser.entries.AnyEntry.OEntry;
import org.xcsp.parser.entries.AnyEntry.VEntry;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.XBlock;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XConstraints.XGroup;
import org.xcsp.parser.entries.XConstraints.XSlide;
import org.xcsp.parser.entries.XDomains.XDom;
import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;
import org.xcsp.parser.entries.XObjectives.OObjectiveExpr;
import org.xcsp.parser.entries.XObjectives.OObjectiveSpecial;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XValues.IntegerEntity;
import org.xcsp.parser.entries.XValues.IntegerInterval;
import org.xcsp.parser.entries.XValues.IntegerValue;
import org.xcsp.parser.entries.XValues.SimpleValue;
import org.xcsp.parser.entries.XVariables.XArray;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;
import org.xcsp.parser.loaders.CtrLoaderInteger;
import org.xcsp.parser.loaders.CtrLoaderSymbolic;

/**
 * @author Christophe Lecoutre
 */
public interface XCallbacks {

	/**********************************************************************************************
	 * Managing Data required for implementation
	 *********************************************************************************************/

	/**
	 * The constants that can be used to pilot the parser.
	 */
	enum XCallbacksParameters {
		RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES,
		RECOGNIZE_SPECIAL_COUNT_CASES,
		RECOGNIZE_SPECIAL_NVALUES_CASES,
		INTENSION_TO_EXTENSION_ARITY_LIMIT, // set it to 0 for deactivating "intension to extension" conversion
		INTENSION_TO_EXTENSION_SPACE_LIMIT,
		INTENSION_TO_EXTENSION_PRIORITY;
	}

	/**
	 * The class that contains all data structures (objects) that are used during the process of loading the XCSP3 instance.
	 * 
	 * @author lecoutre
	 *
	 */
	static class Implem {
		/** The object used to load integer constraints. */
		public CtrLoaderInteger ctrLoaderInteger;

		/** The object used to load symbolic constraints. */
		public CtrLoaderSymbolic ctrLoaderSymbolic;

		/** The cache used to avoid creating several times similar domains. */
		public Map<XDom, Object> cache4DomObject;

		/** The cache used to avoid creating several times similar tables (arrays of tuples). */
		public Map<Object, int[][]> cache4Tuples;

		/** The map containing the current parameters that are used to pilot the parser. */
		public Map<XCallbacksParameters, Object> currentParameters;

		/**
		 * Returns a map with the default parameters that can be used to pilot the parser. When parsing, by default the parser will try for example to recognize
		 * primitives and special cases of constraints count and nValues.
		 * 
		 * @return a map with the default values that can be used to pilot the parser.
		 */
		private Map<XCallbacksParameters, Object> defaultParameters() {
			Object dummy = new Object();
			Map<XCallbacksParameters, Object> map = new HashMap<>();
			map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_UNARY_INTENSION_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_BINARY_INTENSION_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_TERNARY_INTENSION_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_COUNT_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_SPECIAL_NVALUES_CASES, dummy);
			map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_ARITY_LIMIT, 0); // included
			map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_SPACE_LIMIT, 1000000);
			map.put(XCallbacksParameters.INTENSION_TO_EXTENSION_PRIORITY, Boolean.TRUE);
			return map;
		}

		/**
		 * Builds the object that will be used during the process of loading an XCSP3 instance.
		 * 
		 * @param xc
		 *            the object XCallbacks for which this object is attached to
		 */
		public Implem(XCallbacks xc) {
			ctrLoaderInteger = new CtrLoaderInteger(xc);
			ctrLoaderSymbolic = new CtrLoaderSymbolic(xc);
			cache4DomObject = new HashMap<>();
			cache4Tuples = new HashMap<>();
			currentParameters = defaultParameters();
		}
	}

	/**
	 * Returns the object that implements necessary data structures during the loading process. In your class implementing XCallbacks, you should simply write
	 * something like:
	 * 
	 * <pre>
	 * {@code
	 * 	 Implem implem = new Implem(this);
	 *   
	 *   @Override
	 *   public Implem implem() {
	 *   	return implem;
	 *   }
	 * }
	 * </pre>
	 * 
	 * @return the object that implements some data structures used during the loading process
	 */
	abstract Implem implem();

	/**
	 * Throws a runtime exception because a piece of code is not implemented. The specified objects are simply displayed to give information about the problem
	 * to fix.
	 * 
	 * @param objects
	 *            objects to be displayed (with toString())
	 * @return a fake object because the exception will quit first.
	 */
	default Object unimplementedCase(Object... objects) {
		throw new RuntimeException("Unimplemented case " + Stream.of(objects).map(o -> o.toString()).collect(Collectors.joining("\n")));
	}

	/**********************************************************************************************
	 * Main Methods for loading variables, constraints and objectives
	 *********************************************************************************************/

	/**
	 * Loads the XML document corresponding to the XCSP3 instance whose filename is given. This method has to be overridden when special tools are required to
	 * load the file.
	 * 
	 * @param fileName
	 *            the name of an XCSP3 file
	 * @return the document corresponding to the XCSP3 file whose filename is given
	 */
	default Document loadDocument(String fileName) throws Exception {
		return Utilities.loadDocument(fileName);
	}

	/**
	 * Loads and parses the XCSP3 instance whose filename is given. The optional specified classes indicate which elements (variables, constraints) must be
	 * discarded when parsing; for example, one may wish to ignore all constraints related to "symmetryBreaking". Normally, this method should not be
	 * overridden.
	 * 
	 * @param fileName
	 *            the name of an XCSP3 file
	 * @param discardedClasses
	 *            the name of the classes of elements (variables, constraints) that must be discarded when parsing
	 * @throws Exception
	 */
	default void loadInstance(String fileName, String... discardedClasses) throws Exception {
		Document document = loadDocument(fileName);
		XParser parser = new XParser(document, discardedClasses);
		beginInstance(parser.typeFramework);
		beginVariables(parser.vEntries);
		loadVariables(parser);
		endVariables();
		beginConstraints(parser.cEntries);
		loadConstraints(parser);
		endConstraints();
		beginObjectives(parser.oEntries, parser.typeCombination);
		loadObjectives(parser);
		endObjectives();
		// annotations
		endInstance();
	}

	/**
	 * Loads all elements that are contained in the element <variables> of the XCSP3 instance, which have been parsed by the specified parser object. Except for
	 * some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <variables>
	 */
	default void loadVariables(XParser parser) {
		for (VEntry entry : parser.vEntries) {
			if (entry instanceof XVar)
				loadVar((XVar) entry);
			else {
				beginArray((XArray) entry);
				loadArray((XArray) entry);
				endArray((XArray) entry);
			}
		}
	}

	/**
	 * Loads the specified variable. One callback function 'buildVarInteger' or 'buildVarSymbolic' is called when this method is executed. Except for some
	 * advanced uses, this method should not be overridden.
	 * 
	 * @param v
	 *            the variable to be loaded
	 */
	default void loadVar(XVar v) {
		if (v.degree == 0)
			return;
		Object domObject = implem().cache4DomObject.get(v.dom);
		if (domObject == null) {
			if (v.dom instanceof XDomInteger) {
				IntegerEntity[] pieces = (IntegerEntity[]) ((XDomInteger) v.dom).values;
				if (pieces.length == 1 && pieces[0] instanceof IntegerInterval)
					domObject = pieces[0];
				else {
					System.out.println("VAR=" + v);
					int[] values = IntegerEntity.toIntArray(pieces, CtrLoaderInteger.NB_MAX_VALUES);
					Utilities.control(values != null, "Too many values. You have to extend the parser.");
					domObject = values;
				}
			} else if (v.dom instanceof XDomSymbolic)
				domObject = ((XDomSymbolic) v.dom).values;
			else
				unimplementedCase(v.dom);
			implem().cache4DomObject.put(v.dom, domObject); // = trDom(v.dom));
		}
		if (domObject instanceof IntegerInterval) {
			IntegerInterval ii = (IntegerInterval) domObject;
			int min = Utilities.safeLong2IntWhileHandlingInfinity(ii.inf, true);
			int max = Utilities.safeLong2IntWhileHandlingInfinity(ii.sup, true);
			buildVarInteger((XVarInteger) v, min, max);
		} else if (domObject instanceof int[])
			buildVarInteger((XVarInteger) v, (int[]) domObject);
		else if (domObject instanceof String[])
			buildVarSymbolic((XVarSymbolic) v, (String[]) domObject);
		else
			unimplementedCase(v);
	}

	/**
	 * Loads the specified array of variables. All non-null variables of the array are iterated over and loaded. Except for some advanced uses, this method
	 * should not be overridden.
	 * 
	 * @param va
	 *            the array of variables to be loaded
	 */
	default void loadArray(XArray va) {
		Stream.of(va.vars).filter(v -> v != null).forEach(v -> loadVar(v));
	}

	/**
	 * Loads all elements that are contained in the element <constraints> of the XCSP3 instance, which have been parsed by the specified parser object. Except
	 * for some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <constraints>
	 */
	default void loadConstraints(XParser parser) {
		loadConstraints(parser.cEntries); // recursive loading process (through potential blocks)
	}

	/**
	 * Loads all constraints that can be found in the specified list. This method is recursive, allowing us to deal with blocks and groups. Normally, this
	 * method should not be overridden.
	 * 
	 * @param list
	 *            a list of elements from <constraints> that must be parsed.
	 */
	default void loadConstraints(List<CEntry> list) {
		for (CEntry entry : list) {
			if (entry instanceof XBlock)
				loadBlock((XBlock) entry);
			else if (entry instanceof XGroup)
				loadGroup((XGroup) entry);
			else if (entry instanceof XSlide)
				loadSlide((XSlide) entry);
			else if (entry instanceof XCtr)
				loadCtr((XCtr) entry);
			else
				unimplementedCase(entry);
		}
	}

	/**
	 * Loads a block from <constraints>. Normally, this method should not be overridden.
	 * 
	 * @param b
	 *            the block to be loaded.
	 */
	default void loadBlock(XBlock b) {
		beginBlock(b);
		loadConstraints(b.subentries); // recursive call
		endBlock(b);
	}

	/**
	 * Loads a group from <constraints>. Normally, this method should not be overridden.
	 * 
	 * @param g
	 *            the group to be loaded.
	 */
	default void loadGroup(XGroup g) {
		beginGroup(g);
		if (g.template instanceof XCtr)
			loadCtrs((XCtr) g.template, g.argss, g);
		else
			unimplementedCase(g);
		endGroup(g);
	}

	/**
	 * Loads a meta-constraint slide. Normally, this method should not be overridden.
	 * 
	 * @param s
	 *            the meta-constraint slide to be loaded.
	 */
	default void loadSlide(XSlide s) {
		beginSlide(s);
		loadCtrs((XCtr) s.template, s.scopes, s);
		endSlide(s);
	}

	/**
	 * Loads all constraints that can be built from the specified template and the specified array of arguments. For each value between 0 and argss.length, a
	 * constraint is built. Normally, this method should not be overridden.
	 * 
	 * @param template
	 *            a constraint template
	 * @param argss
	 *            an array of arguments
	 * @param entry
	 *            the object at the origin of the abstraction
	 */
	default void loadCtrs(XCtr template, Object[][] argss, CEntry entry) {
		Stream.of(argss).forEach(args -> {
			template.abstraction.concretize(args);
			loadCtr(template);
		});
	}

	/**
	 * Loads the specified constraint. One callback function (for example, builCtrIntension or buildCtrAllDifferent) is called when this method is executed.
	 * Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param c
	 *            the constraint to be loaded
	 */
	default void loadCtr(XCtr c) {
		CChild[] childs = c.childs;
		Utilities.control(Stream.of(TypeChild.cost, TypeChild.set, TypeChild.mset).noneMatch(t -> t == childs[childs.length - 1].type),
				"soft, set and mset currently not implemented");
		if (Stream.of(c.vars()).allMatch(x -> x instanceof XVarInteger)) {
			implem().ctrLoaderInteger.load(c);
		} else if (Stream.of(c.vars()).allMatch(x -> x instanceof XVarSymbolic)) {
			implem().ctrLoaderSymbolic.load(c);
		} else
			unimplementedCase(c);
	}

	/**
	 * Loads all elements that are contained in the element <objectives> of the XCSP3 instance, which have been parsed by the specified parser object. Except
	 * for some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <objectives>
	 */
	default void loadObjectives(XParser parser) {
		parser.oEntries.stream().forEach(entry -> loadObj((XObj) entry));
	}

	/**
	 * Loads the specified objective. One callback function (for example, builObjToMinimize or buildObjToMaximize) is called when this method is executed.
	 * Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param o
	 *            the objective to be loaded
	 */
	default void loadObj(XObj o) {
		if (o.type == TypeObjective.EXPRESSION) {
			XNode<?> node = (XNode<?>) ((OObjectiveExpr) o).rootNode;
			if (node.getType() == TypeExpr.VAR) {
				if (o.minimize)
					buildObjToMinimize(o.id, (XVarInteger) ((XNodeLeaf<?>) node).value);
				else
					buildObjToMaximize(o.id, (XVarInteger) ((XNodeLeaf<?>) node).value);
			} else {
				if (o.minimize)
					buildObjToMinimize(o.id, (XNodeParent<XVarInteger>) node);
				else
					buildObjToMaximize(o.id, (XNodeParent<XVarInteger>) node);
			}
		} else {
			XVarInteger[] vars = (XVarInteger[]) ((OObjectiveSpecial) o).vars;
			SimpleValue[] vals = ((OObjectiveSpecial) o).coeffs;
			int[] coeffs = vals == null ? null : Stream.of(vals).mapToInt(val -> Utilities.safeLong2Int(((IntegerValue) val).v, true)).toArray();
			if (coeffs == null) {
				if (o.minimize)
					buildObjToMinimize(o.id, o.type, vars);
				else
					buildObjToMaximize(o.id, o.type, vars);
			} else {
				if (o.minimize)
					buildObjToMinimize(o.id, o.type, vars, coeffs);
				else
					buildObjToMaximize(o.id, o.type, vars, coeffs);
			}
		}
	}

	/**********************************************************************************************
	 * Methods called at Specific Moments
	 *********************************************************************************************/

	/**
	 * Method called at the very beginning of the process of loading the XCSP3 instance. Implement (or redefine) this method (if you implement XCallbacks2) in
	 * case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param type
	 *            the framework of the XCSP3 instance
	 */
	void beginInstance(TypeFramework type);

	/**
	 * Method called at the end of the process of loading the XCSP3 instance. Implement (or redefine) this method (if you implement XCallbacks2) in case you
	 * want some special operation to be executed (for example, for debugging).
	 */
	void endInstance();

	/**
	 * Method called at the beginning of the process of loading the variables of the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param vEntries
	 *            the list of objects found in <variables>
	 */
	void beginVariables(List<VEntry> vEntries);

	/**
	 * Method called at the end of the process of loading the variables of the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 */
	void endVariables();

	/**
	 * Method called at the beginning of the process of loading the specified array of variables. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param a
	 *            an object representing an array of variables
	 */
	void beginArray(XArray a);

	/**
	 * Method called at the end of the process of loading the specified array of variables. Implement (or redefine) this method (if you implement XCallbacks2)
	 * in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param a
	 *            an object representing an array of variables
	 */
	void endArray(XArray a);

	/**
	 * Method called at the beginning of the process of loading the constraints of the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param cEntries
	 *            the list of objects found in <constraints>
	 */
	void beginConstraints(List<CEntry> cEntries);

	/**
	 * Method called at the end of the process of loading the constraints of the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 */
	void endConstraints();

	/**
	 * Method called at the beginning of the process of loading the specified block. Implement (or redefine) this method (if you implement XCallbacks2) in case
	 * you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param b
	 *            a block to be loaded
	 */
	void beginBlock(XBlock b);

	/**
	 * Method called at the end of the process of loading the specified block. Implement (or redefine) this method (if you implement XCallbacks2) in case you
	 * want some special operation to be executed (for example, for debugging).
	 * 
	 * @param b
	 *            a block
	 */
	void endBlock(XBlock b);

	/**
	 * Method called at the beginning of the process of loading the specified group of constraints. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param g
	 *            a group to be loaded
	 */
	void beginGroup(XGroup g);

	/**
	 * Method called at the end of the process of loading the specified group of constraints. Implement (or redefine) this method (if you implement XCallbacks2)
	 * in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param g
	 *            a group
	 */
	void endGroup(XGroup g);

	/**
	 * Method called at the beginning of the process of loading the specified meta-constraint slide. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param s
	 *            a meta-constraint slide to be loaded
	 */
	void beginSlide(XSlide s);

	/**
	 * Method called at the end of the process of loading the specified meta-constraint slide. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param s
	 *            a meta-constraint slide
	 */
	void endSlide(XSlide s);

	/**
	 * Method called at the beginning of the process of loading the objectives (if any) of the XCSP3 instance. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param oEntries
	 *            the list of objects found in <objectives>
	 * @param type
	 *            the type indicating how to manage multi-optimization; this parameter is irrelevant in case of mono-optimization
	 */
	void beginObjectives(List<OEntry> oEntries, TypeCombination type);

	/**
	 * Method called at the end of the process of loading the objectives (if any) of the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 */
	void endObjectives();

	// void beginAnnotations(List<AEntry> aEntries) ;
	// void endAnnotations() ;

	/**********************************************************************************************
	 * Methods to be implemented on integer variables/constraints
	 *********************************************************************************************/

	/**
	 * Callback method for building in the solver an integer variable whose domain contains all integer values between the two specified bounds.
	 * 
	 * @param x
	 *            an integer variable built by the parser
	 * @param minValue
	 *            the minimum value of the domain of x
	 * @param maxValue
	 *            the maximum value of the domain of x
	 */
	void buildVarInteger(XVarInteger x, int minValue, int maxValue);

	/**
	 * Callback method for building in the solver an integer variable whose domain is given by the specified array.
	 * 
	 * @param x
	 *            an integer variable built by the parser
	 * @param values
	 *            the values in the domain of x
	 */
	void buildVarInteger(XVarInteger x, int[] values);

	/**
	 * Callback method for building in the solver an initially entailed (i.e., universally satisfied) constraint. By default, this method does nothing. You
	 * should redefine it if you need to preserve all constraints (e.g, for MaxCSP).
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the list of variables of the constraint
	 */
	default void buildCtrTrue(String id, XVar[] list) {
	}

	/**
	 * Callback method for building in the solver an initially disentailed (i.e., universally unsatisfied) constraint. By default, this method throws an
	 * exception. You should redefine it if you can deal with such very special constraints.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the list of variables of the constraint
	 */
	default void buildCtrFalse(String id, XVar[] list) {
		throw new RuntimeException("Constraint with only conflicts");
	}

	/**
	 * Callback method for building in the solver a constraint <code>intension</code> from the specified syntactic tree. Variables of the specified array of
	 * variables are exactly those that are present in the tree.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param scope
	 *            the list of variables of the constraint
	 * @param tree
	 *            the root of a syntactic tree representing the predicate associated with the constraint
	 */
	void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree);

	/** Primitive constraint of the form x <op> k, with x a variable, k a constant (int) and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k);

	/** Primitive constraint of the form x <opa> y <op> k, with x and y variables, k a constant (int), <opa> in {+,-,*,/,%,dist} and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, int k);

	/** Primitive constraint of the form x <opa> y <op> z, with x y and z variables, k a constant (int), <opa> in {+,-,*,/,%,dist} and <op> in {<,<=,>=,>,=, !=} */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator opa, XVarInteger y, TypeConditionOperatorRel op, XVarInteger z);

	void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags);

	void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags);

	void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates);

	void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions);

	void buildCtrAllDifferent(String id, XVarInteger[] list);

	void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except);

	void buildCtrAllDifferentList(String id, XVarInteger[][] lists);

	void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix);

	/**
	 * Callback method for building in the solver a constraint <code>allEqual</code>.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the list of variables of the constraint
	 */
	void buildCtrAllEqual(String id, XVarInteger[] list);

	void buildCtrOrdered(String id, XVarInteger[] list, TypeOperator operator);

	void buildCtrLex(String id, XVarInteger[][] lists, TypeOperator operator);

	void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperator operator);

	void buildCtrSum(String id, XVarInteger[] list, Condition condition);

	void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition);

	void buildCtrSum(String id, XVarInteger[] list, XVarInteger[] coeffs, Condition condition);

	void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition);

	void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition);

	void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k);

	void buildCtrAtMost(String id, XVarInteger[] list, int value, int k);

	void buildCtrExactly(String id, XVarInteger[] list, int value, int k);

	void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k);

	void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k);

	void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k);

	void buildCtrNValues(String id, XVarInteger[] list, Condition condition);

	void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition);

	void buildCtrNotAllEqual(String id, XVarInteger[] list);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs);

	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax);

	void buildCtrMaximum(String id, XVarInteger[] list, Condition condition);

	void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	void buildCtrMinimum(String id, XVarInteger[] list, Condition condition);

	void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	void buildCtrElement(String id, XVarInteger[] list, XVarInteger value);

	void buildCtrElement(String id, XVarInteger[] list, int value);

	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value);

	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value);

	void buildCtrChannel(String id, XVarInteger[] list, int startIndex);

	void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2);

	void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value);

	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax);

	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns);

	void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored);

	void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	void buildCtrInstantiation(String id, XVarInteger[] list, int[] values);

	void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg);

	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex);

	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, int size);

	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, XVarInteger size);

	/**********************************************************************************************
	 * Methods to be implemented for managing objectives
	 *********************************************************************************************/

	void buildObjToMinimize(String id, XVarInteger x);

	void buildObjToMaximize(String id, XVarInteger x);

	void buildObjToMinimize(String id, XNodeParent<XVarInteger> tree);

	void buildObjToMaximize(String id, XNodeParent<XVarInteger> tree);

	void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list);

	void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list);

	void buildObjToMinimize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs);

	void buildObjToMaximize(String id, TypeObjective type, XVarInteger[] list, int[] coeffs);

	/**********************************************************************************************
	 * Methods to be implemented on symbolic variables/constraints
	 *********************************************************************************************/

	void buildVarSymbolic(XVarSymbolic x, String[] values);

	void buildCtrIntension(String id, XVarSymbolic[] scope, XNodeParent<XVarSymbolic> syntaxTreeRoot);

	void buildCtrExtension(String id, XVarSymbolic x, String[] values, boolean positive, Set<TypeFlag> flags);

	void buildCtrExtension(String id, XVarSymbolic[] list, String[][] tuples, boolean positive, Set<TypeFlag> flags);

	void buildCtrAllDifferent(String id, XVarSymbolic[] list);
}
