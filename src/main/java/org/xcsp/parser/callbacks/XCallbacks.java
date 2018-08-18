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
package org.xcsp.parser.callbacks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.w3c.dom.Document;
import org.xcsp.common.Condition;
import org.xcsp.common.Constants;
import org.xcsp.common.Types.TypeArithmeticOperator;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeCombination;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeEqNeOperator;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeLogicalOperator;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperatorRel;
import org.xcsp.common.Types.TypeRank;
import org.xcsp.common.Types.TypeUnaryArithmeticOperator;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.domains.Domains.IDom;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.common.domains.Values.IntegerValue;
import org.xcsp.common.domains.Values.SimpleValue;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.WrongTypeException;
import org.xcsp.parser.XParser;
import org.xcsp.parser.entries.ParsingEntry;
import org.xcsp.parser.entries.ParsingEntry.CEntry;
import org.xcsp.parser.entries.ParsingEntry.OEntry;
import org.xcsp.parser.entries.ParsingEntry.VEntry;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.XBlock;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XConstraints.XGroup;
import org.xcsp.parser.entries.XConstraints.XLogic;
import org.xcsp.parser.entries.XConstraints.XSlide;
import org.xcsp.parser.entries.XObjectives.OObjectiveExpr;
import org.xcsp.parser.entries.XObjectives.OObjectiveSpecial;
import org.xcsp.parser.entries.XObjectives.XObj;
import org.xcsp.parser.entries.XVariables.XArray;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;
import org.xcsp.parser.loaders.CtrLoaderInteger;
import org.xcsp.parser.loaders.CtrLoaderSymbolic;

/**
 * This interface can be implemented to benefit from the parsing process of a Java parser. Many callback functions are automatically called and can
 * then be intercepted.
 * 
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
		RECOGNIZE_UNARY_PRIMITIVES,
		RECOGNIZE_BINARY_PRIMITIVES,
		RECOGNIZE_TERNARY_PRIMITIVES,
		RECOGNIZE_LOGIC_CASES,
		RECOGNIZE_EXTREMUM_CASES, // minimum and maximum
		RECOGNIZE_SUM_CASES,
		RECOGNIZE_COUNT_CASES,
		RECOGNIZE_NVALUES_CASES,
		CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT, // set it to 0 for deactivating "intension to extension" conversion
		CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT,
		RECOGNIZING_BEFORE_CONVERTING;
	}

	/**
	 * The class that contains all data structures (objects) that are used during the process of loading the XCSP3 instance.
	 * 
	 * @author lecoutre
	 *
	 */
	static class Implem {
		/** The object used to load integer constraints. */
		public final CtrLoaderInteger ctrLoaderInteger;

		/** The object used to load symbolic constraints. */
		public final CtrLoaderSymbolic ctrLoaderSymbolic;

		/** The cache used to avoid creating several times similar domains. */
		public Map<IDom, Object> cache4DomObject;

		/** The cache used to avoid creating several times similar tables (arrays of tuples). */
		public Map<Object, int[][]> cache4Tuples;

		/** The map containing the current parameters that are used to pilot the parser. */
		public final Map<XCallbacksParameters, Object> currParameters;

		public Set<String> allIds;

		/** The set that is used to determine if a "recognized" constraint has really be posted or not. */
		public Set<String> postedRecognizedCtrs;

		/** The limit on the size of the Cartesian product of the domains of the variables, for trying a conversion (intension to extension). */
		public static final Long CONVERSION_SPACE_LIMIT = new Long(1000000);

		/**
		 * Makes current parameters in raw form, meaning that constraints will be given in their very original forms.
		 */
		public void rawParameters() {
			currParameters.clear(); // we don't try to recognize anything
			currParameters.put(XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT, 0); // we don't make any conversion (since arity 0)
			currParameters.put(XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT, CONVERSION_SPACE_LIMIT);
			currParameters.put(XCallbacksParameters.RECOGNIZING_BEFORE_CONVERTING, Boolean.TRUE);
		}

		/**
		 * Returns a map with the default parameters that can be used to pilot the parser. When parsing, by default the parser will try for example to
		 * recognize primitives and special cases of constraints count and nValues.
		 * 
		 * @return a map with the default values that can be used to pilot the parser.
		 */
		private Map<XCallbacksParameters, Object> defaultParameters() {
			Object dummy = new Object();
			Map<XCallbacksParameters, Object> map = new HashMap<>();
			// we need a dummy object to put (deactivate) all these properties.
			map.put(XCallbacksParameters.RECOGNIZE_UNARY_PRIMITIVES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_BINARY_PRIMITIVES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_TERNARY_PRIMITIVES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_LOGIC_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_SUM_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_EXTREMUM_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_COUNT_CASES, dummy);
			map.put(XCallbacksParameters.RECOGNIZE_NVALUES_CASES, dummy);
			map.put(XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_ARITY_LIMIT, 0); // no conversion by default (since arity 0)
			map.put(XCallbacksParameters.CONVERT_INTENSION_TO_EXTENSION_SPACE_LIMIT, CONVERSION_SPACE_LIMIT);
			map.put(XCallbacksParameters.RECOGNIZING_BEFORE_CONVERTING, Boolean.TRUE);
			return map;
		}

		/**
		 * Resets the structures used when parsing a specific instance (e.g., caches for ids and tables).
		 */
		public void resetStructures() {
			cache4DomObject = new HashMap<>();
			cache4Tuples = new HashMap<>();
			allIds = new HashSet<>();
			postedRecognizedCtrs = new HashSet<>();
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
			currParameters = defaultParameters();
			resetStructures();
		}

		private int nextCtrId, nextLogId;

		public String manageIdFor(ParsingEntry ae) {
			if (ae.id != null) {
				Utilities.control(!allIds.contains(ae.id), "Duplicate id " + ae.id);
				Utilities.control(Stream.of(Constants.KEYWORDS).allMatch(k -> !k.equals(ae.id)), "The id " + ae.id + " is a keyword, and so cannot be used.");
			}
			if (ae.id != null)
				allIds.add(ae.id);
			else {
				// we want an id for each constraint (note that each variable has necessary already an id)
				if (ae instanceof XCtr) {
					while (allIds.contains("c_" + nextCtrId))
						nextCtrId++;
					ae.id = "c_" + nextCtrId;
					allIds.add(ae.id);
					nextCtrId++;
				}
				if (ae instanceof XLogic) {
					while (allIds.contains("m_" + nextLogId))
						nextLogId++;
					ae.id = "m_" + nextLogId;
					allIds.add(ae.id);
					nextLogId++;
				}
			}
			return ae.id;
		}
	}

	/**
	 * Returns the object that implements necessary data structures during the loading process. In your class implementing XCallbacks, you should
	 * simply write something like:
	 * 
	 * <pre>
	 * {@code
	 * 	 Implem implem = new Implem(this);
	 *   
	 *   &#64;Override
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
	 * Method that must be called when an intercepted ("recognized") constraint cannot be dealt with, and so the constraint must be resent to the
	 * parser so as to be treated classically.
	 * 
	 * @param constraintId
	 *            the id of a constraint
	 */
	default void repost(String constraintId) {
		// System.out.println("REPOSTING " + constraintId);
		implem().postedRecognizedCtrs.remove(constraintId);
	}

	/**
	 * Throws a runtime exception because a piece of code is not implemented. The specified objects are simply displayed to give information about the
	 * problem to fix.
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
	 * Loads the XML document corresponding to the XCSP3 instance whose filename is given. This method has to be overridden when special tools are
	 * required to load the file.
	 * 
	 * @param fileName
	 *            the name of an XCSP3 file
	 * @return the document corresponding to the XCSP3 file whose filename is given
	 */
	default Document loadDocument(String fileName) throws Exception {
		return Utilities.loadDocument(fileName);
	}

	/**
	 * Loads and parses the XCSP3 instance represented by the specified document. The optional specified classes indicate which elements (variables,
	 * constraints) must be discarded when parsing; for example, one may wish to ignore all constraints related to "symmetryBreaking". Normally, this
	 * method should not be overridden.
	 * 
	 * @param document
	 *            the document representing the XCSP3 instance
	 * @param discardedClasses
	 *            the name of the classes (tags) of elements (variables, constraints) that must be discarded when parsing
	 * @throws Exception
	 */
	default void loadInstance(Document document, String... discardedClasses) throws Exception {
		implem().resetStructures();
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
		beginAnnotations(parser.aEntries);
		loadAnnotations(parser);
		endAnnotations();
		endInstance();
	}

	/**
	 * Loads and parses the XCSP3 instance whose filename is given. The optional specified classes indicate which elements (variables, constraints)
	 * must be discarded when parsing; for example, one may wish to ignore all constraints related to "symmetryBreaking". Normally, this method should
	 * not be overridden.
	 * 
	 * @param fileName
	 *            the name of an XCSP3 file
	 * @param discardedClasses
	 *            the name of the classes (tags) of elements (variables, constraints) that must be discarded when parsing
	 * @throws Exception
	 */
	default void loadInstance(String fileName, String... discardedClasses) throws Exception {
		loadInstance(loadDocument(fileName), discardedClasses);
	}

	/**
	 * Loads all elements that are contained in the element &lt;variables&gt; of the XCSP3 instance, which have been parsed by the specified parser
	 * object. Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <variables>
	 */
	default void loadVariables(XParser parser) {
		for (VEntry entry : parser.vEntries) {
			try {
				if (entry instanceof XVar)
					loadVar((XVar) entry);
				else {
					beginArray((XArray) entry);
					loadArray((XArray) entry);
					endArray((XArray) entry);
				}
			} catch (ClassCastException e) {
				throw new WrongTypeException("in declaration of variable with id \"" + entry.id + "\": does domain correspond to the declared type ?");
			}
		}
		// for (VEntry entry : parser.vEntries) {
		// if (entry instanceof XArray) {
		// int n = (int) Stream.of(((XArray) entry).vars).filter(x -> x == null).count();
		// System.out.println("N=" + n);
		// }
		// }

	}

	/**
	 * Loads the specified variable. One callback function 'buildVarInteger' or 'buildVarSymbolic' is called when this method is executed. Except for
	 * some advanced uses, this method should not be overridden.
	 * 
	 * @param v
	 *            the variable to be loaded
	 */
	default void loadVar(XVar v) {
		implem().manageIdFor(v);
		if (v.degree == 0)
			return;
		Object domObject = implem().cache4DomObject.get(v.dom);
		if (domObject == null) {
			if (v.dom instanceof Dom) {
				IntegerEntity[] pieces = (IntegerEntity[]) ((Dom) v.dom).values;
				if (pieces.length == 1 && pieces[0] instanceof IntegerInterval)
					domObject = pieces[0];
				else {
					int[] values = IntegerEntity.toIntArray(pieces, CtrLoaderInteger.N_MAX_VALUES);
					Utilities.control(values != null, "Too many values. You have to extend the parser.");
					domObject = values;
				}
			} else if (v.dom instanceof DomSymbolic)
				domObject = ((DomSymbolic) v.dom).values;
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
	 * Loads the specified array of variables. All non-null variables of the array are iterated over and loaded. Except for some advanced uses, this
	 * method should not be overridden.
	 * 
	 * @param va
	 *            the array of variables to be loaded
	 */
	default void loadArray(XArray va) {
		implem().manageIdFor(va);
		Stream.of(va.vars).filter(v -> v != null).forEach(v -> loadVar(v));
	}

	/**
	 * Loads all elements that are contained in the element &lt;constraints&gt; of the XCSP3 instance, which have been parsed by the specified parser
	 * object. Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <constraints>
	 */
	default void loadConstraints(XParser parser) {
		loadConstraints(parser.cEntries); // recursive loading process (through potential blocks)
	}

	/**
	 * Loads all constraints that can be found in the specified list. This method is recursive, allowing us to deal with blocks and groups. Normally,
	 * this method should not be overridden.
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
			else if (entry instanceof XLogic) {
				loadLogic((XLogic) entry);
			} else if (entry instanceof XCtr) {
				try {
					loadCtr((XCtr) entry);
				} catch (ClassCastException e) {
					e.printStackTrace();
					throw new WrongTypeException("Wrong parameter type in constraint:\n" + entry + "\n" + e);
				}
			} else
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
		implem().manageIdFor(b);
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
		implem().manageIdFor(g);
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
		implem().manageIdFor(s);
		beginSlide(s);
		loadCtrs((XCtr) s.template, s.scopes, s);
		endSlide(s);
	}

	/**
	 * Loads a meta-constraint based on a logical form (including control ones). This corresponds to meta-constraints
	 * {@code <and>, <or>, <iff>, <not>, <ifThen> or <ifThenElse>}. Normally, this method should not be overridden.
	 * 
	 * @param l
	 *            the logic-based meta-constraint to be loaded.
	 */
	default void loadLogic(XLogic l) {
		implem().manageIdFor(l);
		beginLogic(l);
		loadConstraints(Arrays.asList(l.components)); // recursive call
		endLogic(l);
	}

	/**
	 * Loads all constraints that can be built from the specified template and the specified array of arguments. For each value between 0 and
	 * argss.length, a constraint is built. Normally, this method should not be overridden.
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
			template.id = null; // because the template object is shared
			template.abstraction.concretize(args);
			loadCtr(template);
		}); // TODO : be careful, the object template is simply modified; for parallel stuff, it should be rebuilt entirely
	}

	/**
	 * Loads the specified constraint. One callback function (for example, builCtrIntension or buildCtrAllDifferent) is called when this method is
	 * executed. Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param c
	 *            the constraint to be loaded
	 */
	default void loadCtr(XCtr c) {
		implem().manageIdFor(c);
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
	 * Loads all elements that are contained in the element <objectives> of the XCSP3 instance, which have been parsed by the specified parser object.
	 * Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param parser
	 *            the object used to parse the element <objectives>
	 */
	default void loadObjectives(XParser parser) {
		parser.oEntries.stream().forEach(entry -> loadObj((XObj) entry));
	}

	/**
	 * Loads the specified objective. One callback function (for example, builObjToMinimize or buildObjToMaximize) is called when this method is
	 * executed. Except for some advanced uses, this method should not be overridden.
	 * 
	 * @param o
	 *            the objective to be loaded
	 */
	default void loadObj(XObj o) {
		implem().manageIdFor(o);
		if (o.type == TypeObjective.EXPRESSION) {
			XNode<?> node = ((OObjectiveExpr) o).rootNode;
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

	default void loadAnnotations(XParser parser) {
		Object obj = parser.aEntries.get(Constants.DECISION);
		if (obj != null)
			buildAnnotationDecision((XVarInteger[]) obj);
	}

	/**********************************************************************************************
	 * Methods called at Specific Moments
	 *********************************************************************************************/

	/**
	 * Method called at the very beginning of the process of loading the XCSP3 instance. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param type
	 *            the framework of the XCSP3 instance
	 */
	void beginInstance(TypeFramework type);

	/**
	 * Method called at the end of the process of loading the XCSP3 instance. Implement (or redefine) this method (if you implement XCallbacks2) in
	 * case you want some special operation to be executed (for example, for debugging).
	 */
	void endInstance();

	/**
	 * Method called at the beginning of the process of loading the variables of the XCSP3 instance. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
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
	 * Method called at the beginning of the process of loading the specified array of variables. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param a
	 *            an object representing an array of variables
	 */
	void beginArray(XArray a);

	/**
	 * Method called at the end of the process of loading the specified array of variables. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param a
	 *            an object representing an array of variables
	 */
	void endArray(XArray a);

	/**
	 * Method called at the beginning of the process of loading the constraints of the XCSP3 instance. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
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
	 * Method called at the beginning of the process of loading the specified block. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param b
	 *            a block to be loaded
	 */
	void beginBlock(XBlock b);

	/**
	 * Method called at the end of the process of loading the specified block. Implement (or redefine) this method (if you implement XCallbacks2) in
	 * case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param b
	 *            a block
	 */
	void endBlock(XBlock b);

	/**
	 * Method called at the beginning of the process of loading the specified group of constraints. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param g
	 *            a group to be loaded
	 */
	void beginGroup(XGroup g);

	/**
	 * Method called at the end of the process of loading the specified group of constraints. Implement (or redefine) this method (if you implement
	 * XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param g
	 *            a group
	 */
	void endGroup(XGroup g);

	/**
	 * Method called at the beginning of the process of loading the specified meta-constraint slide. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
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
	 * Method called at the beginning of the process of loading the specified logic-based meta-constraint
	 * {@code <and>, <or>, <iff>, <not>, <ifThen> or <ifThenElse>}. Implement (or redefine) this method (if you implement XCallbacks2).
	 * 
	 * @param l
	 *            a logic-based meta-constraint to be loaded
	 */
	void beginLogic(XLogic l);

	/**
	 * Method called at the end of the process of loading the specified logic-based meta-constraint
	 * {@code <and>, <or>, <iff>, <not>, <ifThen> or <ifThenElse>}. Implement (or redefine) this method (if you implement XCallbacks2) in case you
	 * want some special operation to be executed (for example, for debugging).
	 * 
	 * @param l
	 *            a logic-based meta-constraint
	 */
	void endLogic(XLogic l);

	/**
	 * Method called at the beginning of the process of loading the objectives (if any) of the XCSP3 instance. Implement (or redefine) this method (if
	 * you implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 * 
	 * @param oEntries
	 *            the list of objects found in <objectives>
	 * @param type
	 *            the type indicating how to manage multi-optimization; this parameter is irrelevant in case of mono-optimization
	 */
	void beginObjectives(List<OEntry> oEntries, TypeCombination type);

	/**
	 * Method called at the end of the process of loading the objectives (if any) of the XCSP3 instance. Implement (or redefine) this method (if you
	 * implement XCallbacks2) in case you want some special operation to be executed (for example, for debugging).
	 */
	void endObjectives();

	void beginAnnotations(Map<String, Object> aEntries);

	void endAnnotations();

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
	 * Callback method for building in the solver an initially entailed (i.e., universally satisfied) constraint. By default, this method does
	 * nothing. You should redefine it if you need to preserve all constraints (e.g, for MaxCSP).
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the list of variables of the constraint
	 */
	default void buildCtrTrue(String id, XVar[] list) {}

	/**
	 * Callback method for building in the solver an initially disentailed (i.e., universally unsatisfied) constraint. By default, this method throws
	 * an exception. You should redefine it if you can deal with such very special constraints.
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
	 * Callback method for building a constraint <code>intension</code> from the specified syntactic tree. Variables of the specified array of
	 * variables are exactly those that are present in the tree.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param scope
	 *            the list of integer variables of the constraint
	 * @param tree
	 *            the root of a syntactic tree representing the predicate associated with the constraint
	 */
	void buildCtrIntension(String id, XVarInteger[] scope, XNodeParent<XVarInteger> tree);

	/**
	 * Callback method for building an unary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>x < k</li>
	 * <li>x &le; k</li>
	 * <li>x &ge; k</li>
	 * <li>x > k</li>
	 * <li>x = k</li>
	 * <li>x &ne; k</li>
	 * </ul>
	 * with x being an integer variable and k a constant (integer).
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param op
	 *            a relational operator
	 * @param k
	 *            a constant (integer)
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorRel op, int k);

	/**
	 * Callback method for building an unary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>x &isin; t</li>
	 * <li>x &notin; t</li>
	 * </ul>
	 * with x being an integer variable and t a set of constants (integers) represented by an array.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param op
	 *            a set operator
	 * @param t
	 *            a set (array) of constants (integers)
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorSet op, int[] t);

	/**
	 * Callback method for building an unary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>x &isin; min..max</li>
	 * <li>x &notin; min..max</li>
	 * </ul>
	 * with x being an integer variable and min and max two constants (integers) denoting the bounds of an integer interval.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param op
	 *            a set operator
	 * @param min
	 *            the minimum value of the interval
	 * @param max
	 *            the maximum value of the interval
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeConditionOperatorSet op, int min, int max);

	/**
	 * Callback method for building an unary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>(x + p) &odot; k</li>
	 * <li>(x - p) &odot; k</li>
	 * <li>(x * p) &odot; k</li>
	 * <li>(x / p) &odot; k</li>
	 * <li>(x % p) &odot; k</li>
	 * <li>(x ^ p) &odot; k</li>
	 * <li>|x - p| &odot; k</li>
	 * </ul>
	 * with x being an integer variable, p and k constants (integers) and &odot; a relational operator in {<,&le;,&ge;,>,=,&ne;}
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param aop
	 *            an arithmetic operator
	 * @param p
	 *            a constant (integer)
	 * @param op
	 *            a relational operator
	 * @param k
	 *            a constant (integer)
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorRel op, int k);

	// /**
	// * Callback method for building an unary primitive constraint with one of the following forms:
	// * <ul>
	// * <li>(x + p) &odot; t</li>
	// * <li>(x - p) &odot; t</li>
	// * <li>(x * p) &odot; t</li>
	// * <li>(x / p) &odot; t</li>
	// * <li>(x % p) &odot; t</li>
	// * <li>|x - p| &odot; t</li>
	// * </ul>
	// * with x being an integer variable, p a constant (integer), &odot; a set operator in {&isin;&notin;} and t a set of constants
	// (integers) represented by
	// an
	// * array.
	// *
	// * @param id
	// * the id of the constraint
	// * @param x
	// * an integer variable
	// * @param aop
	// * an arithmetic operator
	// * @param p
	// * a constant (integer)
	// * @param op
	// * a relational operator
	// * @param t
	// * a set (array) of constants (integers)
	// */
	// void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorSet op, int[] t);

	/**
	 * Callback method for building a binary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>x = |y|</li>
	 * <li>x = -y</li>
	 * <li>x = y*y</li>
	 * </ul>
	 * with x, and y being two integer variables.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param aop
	 *            a unary arithmetic operator
	 * @param y
	 *            an integer variable
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeUnaryArithmeticOperator aop, XVarInteger y);

	/**
	 * Callback method for building a binary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>(x + y) &odot; k</li>
	 * <li>(x - y) &odot; k</li>
	 * <li>(x * y) &odot; k</li>
	 * <li>(x / y) &odot; k</li>
	 * <li>(x % y) &odot; k</li>
	 * <li>(x ^ y) &odot; k</li>
	 * <li>|x - y| &odot; k</li>
	 * </ul>
	 * with x and y being two integer variables, k a constant (integer) and &odot; a relational operator in {<,&le;,&ge;,>,=,&ne;}
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param aop
	 *            an arithmetic operator
	 * @param y
	 *            an integer variable
	 * @param op
	 *            a relational operator
	 * @param k
	 *            a constant (integer)
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, XVarInteger y, TypeConditionOperatorRel op, int k);

	/**
	 * Callback method for building a binary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>(x + p) &odot; y</li>
	 * <li>(x - p) &odot; y</li>
	 * <li>(x * p) &odot; y</li>
	 * <li>(x / p) &odot; y</li>
	 * <li>(x % p) &odot; y</li>
	 * <li>(x ^ p) &odot; y</li>
	 * <li>|x - p| &odot; y</li>
	 * </ul>
	 * with x and y being two integer variables, p a constant (integer) and &odot; a relational operator in {<,&le;,&ge;,>,=,&ne;}
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param aop
	 *            an arithmetic operator
	 * @param p
	 *            a constant (integer)
	 * @param op
	 *            a relational operator
	 * @param y
	 *            an integer variable
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, int p, TypeConditionOperatorRel op, XVarInteger y);

	/**
	 * Callback method for building a ternary primitive constraint with one of the following forms:
	 * <ul>
	 * <li>(x + y) &odot; z</li>
	 * <li>(x - y) &odot; z</li>
	 * <li>(x * y) &odot; z</li>
	 * <li>(x / y) &odot; z</li>
	 * <li>(x % y) &odot; z</li>
	 * <li>(x ^ y) &odot; z</li>
	 * <li>|x - y| &odot; z</li>
	 * </ul>
	 * with x, y and z being three integer variables, and &odot; a relational operator in {<,&le;,&ge;,>,=,&ne;}
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param aop
	 *            an arithmetic operator
	 * @param y
	 *            an integer variable
	 * @param op
	 *            a relational operator
	 * @param z
	 *            an integer variable
	 */
	void buildCtrPrimitive(String id, XVarInteger x, TypeArithmeticOperator aop, XVarInteger y, TypeConditionOperatorRel op, XVarInteger z);

	/**
	 * Callback method for building a logic constraint with one of the following forms: *
	 * <ul>
	 * <li>and(x1,x2,...,xr)</li>
	 * <li>or(x1,x2,...,xr)</li>
	 * <li>xor(x1,x2,...,xr)</li>
	 * <li>iff(x1,x2,...,xr)</li>
	 * <li>imp(x1,x2)</li>
	 * </ul>
	 * with x1,x2,..., xr being 0/1 variables
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param lop
	 *            a logical operator
	 * @param vars
	 *            a set (array) of 0/1 variables
	 */
	void buildCtrLogic(String id, TypeLogicalOperator lop, XVarInteger[] vars);

	/**
	 * Callback method for building a logic constraint with one of the following forms: *
	 * <ul>
	 * <li>x = and(x1,x2,...,xr)</li>
	 * <li>x = or(x1,x2,...,xr)</li>
	 * <li>x = xor(x1,x2,...,xr)</li>
	 * <li>x = iff(x1,x2,...,xr)</li>
	 * <li>x = imp(x1,x2)</li>
	 * <li>x &ne; and(x1,x2,...,xr)</li>
	 * <li>x &ne; or(x1,x2,...,xr)</li>
	 * <li>x &ne; xor(x1,x2,...,xr)</li>
	 * <li>x &ne; iff(x1,x2,...,xr)</li>
	 * <li>x &ne; imp(x1,x2)</li>
	 * </ul>
	 * with x1,x2,..., xr being 0/1 variables
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            a 0/1 variable
	 * @param op
	 *            either the operator EQ or the operator NE
	 * @param lop
	 *            a logical operator
	 * @param vars
	 *            a set (array) of 0/1 variables
	 */
	void buildCtrLogic(String id, XVarInteger x, TypeEqNeOperator op, TypeLogicalOperator lop, XVarInteger[] vars);

	/**
	 * Callback method for building a unary extensional constraint. Values are supports (accepted by the constraint) iff the specified Boolean is
	 * true, otherwise they are conflicts (not accepted by the constraint). The flag STARRED_TUPLES cannot appear in the specified set (because this
	 * is a unary constraint). The flag UNCLEAN_TUPLES, if present, indicates that all specified values do not necessarily belong to the (initial)
	 * domain of the specified variable. More information in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. Quick
	 * information available at <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications).
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param x
	 *            an integer variable
	 * @param values
	 *            supports or conflicts
	 * @param positive
	 *            values are supports iff this value is true
	 * @param flags
	 *            set of flags giving information about the values
	 */
	void buildCtrExtension(String id, XVarInteger x, int[] values, boolean positive, Set<TypeFlag> flags);

	/**
	 * Callback method for building a (non-unary) extensional constraint. Tuples are supports (accepted by the constraint) iff the specified Boolean
	 * is true, otherwise they are conflicts (not accepted by the constraint). The flag STARRED_TUPLES indicates if the symbol * (denoted by
	 * Constants.STAR_INT, whose value is Integer.MAX_VALUE - 1) is present in some tuple(s). The flag UNCLEAN_TUPLES, if present, indicates that all
	 * specified tuples do not necessarily belong to the (initial) domains of the specified variables. More information in
	 * <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. Quick information available at
	 * <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications).
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the scope of the constraint
	 * @param tuples
	 *            supports or conflicts
	 * @param positive
	 *            tuples are supports iff this value is true
	 * @param flags
	 *            set of flags giving information about the tuples
	 */
	void buildCtrExtension(String id, XVarInteger[] list, int[][] tuples, boolean positive, Set<TypeFlag> flags);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrRegular(String id, XVarInteger[] list, Object[][] transitions, String startState, String[] finalStates);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrMDD(String id, XVarInteger[] list, Object[][] transitions);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAllDifferent(String id, XVarInteger[] list);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAllDifferentExcept(String id, XVarInteger[] list, int[] except);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAllDifferentList(String id, XVarInteger[][] lists);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAllDifferentMatrix(String id, XVarInteger[][] matrix);

	void buildCtrAllDifferent(String id, XNodeParent<XVarInteger>[] trees);

	/**
	 * Callback method for building a constraint <code>allEqual</code>.
	 * 
	 * @param id
	 *            the id of the constraint
	 * @param list
	 *            the list of variables of the constraint
	 */
	void buildCtrAllEqual(String id, XVarInteger[] list);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrOrdered(String id, XVarInteger[] list, TypeOperatorRel operator);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrOrdered(String id, XVarInteger[] list, int[] lengths, TypeOperatorRel operator);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrOrdered(String id, XVarInteger[] list, XVarInteger[] lengths, TypeOperatorRel operator);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrLex(String id, XVarInteger[][] lists, TypeOperatorRel operator);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrLexMatrix(String id, XVarInteger[][] matrix, TypeOperatorRel operator);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrSum(String id, XVarInteger[] list, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrSum(String id, XVarInteger[] list, int[] coeffs, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrSum(String id, XVarInteger[] list, XVarInteger[] coeffs, Condition condition);

	void buildCtrSum(String id, XNodeParent<XVarInteger>[] trees, int[] coeffs, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCount(String id, XVarInteger[] list, int[] values, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCount(String id, XVarInteger[] list, XVarInteger[] values, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAtLeast(String id, XVarInteger[] list, int value, int k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAtMost(String id, XVarInteger[] list, int value, int k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrExactly(String id, XVarInteger[] list, int value, int k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrExactly(String id, XVarInteger[] list, int value, XVarInteger k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAmong(String id, XVarInteger[] list, int[] values, int k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrAmong(String id, XVarInteger[] list, int[] values, XVarInteger k);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNValues(String id, XVarInteger[] list, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNValuesExcept(String id, XVarInteger[] list, int[] except, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNotAllEqual(String id, XVarInteger[] list);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, XVarInteger[] occurs);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occurs);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, int[] values, int[] occursMin, int[] occursMax);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, XVarInteger[] occurs);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occurs);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCardinality(String id, XVarInteger[] list, boolean closed, XVarInteger[] values, int[] occursMin, int[] occursMax);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrMaximum(String id, XVarInteger[] list, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrMaximum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrMinimum(String id, XVarInteger[] list, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrMinimum(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrElement(String id, XVarInteger[] list, XVarInteger value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrElement(String id, XVarInteger[] list, int value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrElement(String id, XVarInteger[] list, int startIndex, XVarInteger index, TypeRank rank, int value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrElement(String id, int[] list, int startIndex, XVarInteger index, TypeRank rank, XVarInteger value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrChannel(String id, XVarInteger[] list, int startIndex);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrChannel(String id, XVarInteger[] list1, int startIndex1, XVarInteger[] list2, int startIndex2);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrChannel(String id, XVarInteger[] list, int startIndex, XVarInteger value);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrStretch(String id, XVarInteger[] list, int[] values, int[] widthsMin, int[] widthsMax, int[][] patterns);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNoOverlap(String id, XVarInteger[] origins, int[] lengths, boolean zeroIgnored);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNoOverlap(String id, XVarInteger[] origins, XVarInteger[] lengths, boolean zeroIgnored);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNoOverlap(String id, XVarInteger[][] origins, int[][] lengths, boolean zeroIgnored);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrNoOverlap(String id, XVarInteger[][] origins, XVarInteger[][] lengths, boolean zeroIgnored);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, int[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, int[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, int[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, int[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCumulative(String id, XVarInteger[] origins, XVarInteger[] lengths, XVarInteger[] ends, XVarInteger[] heights, Condition condition);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrInstantiation(String id, XVarInteger[] list, int[] values);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrClause(String id, XVarInteger[] pos, XVarInteger[] neg);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, int size);

	/**
	 * Full information about the constraint (this form) in <a href="http://xcsp.org/format3.pdf"> the specifications (Chapter 4)</a>. <br>
	 * Quick information available on the <a href="http://xcsp.org/specifications"> XCSP3 website (Tab Specifications) </a>. <br>
	 * Select the constraint after opening the left navigation bar below heading XCSP3-core.
	 * 
	 */
	void buildCtrCircuit(String id, XVarInteger[] list, int startIndex, XVarInteger size);

	void buildBinPacking(String id, XVarInteger[] list, int[] sizes, Condition condition);

	void buildBinPacking(String id, XVarInteger[] list, int[] sizes, Condition[] conditions, int startIndex);

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

	/**********************************************************************************************
	 * Methods to be implemented on Annotations
	 *********************************************************************************************/

	void buildAnnotationDecision(XVarInteger[] list);
}
