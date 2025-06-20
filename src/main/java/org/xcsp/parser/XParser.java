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

import static org.xcsp.common.Constants.ANNOTATIONS;
import static org.xcsp.common.Constants.BLOCK;
import static org.xcsp.common.Constants.CONSTRAINTS;
import static org.xcsp.common.Constants.DECISION;
import static org.xcsp.common.Constants.DELIMITER_LISTS;
import static org.xcsp.common.Constants.DELIMITER_MSETS;
import static org.xcsp.common.Constants.DELIMITER_SETS;
import static org.xcsp.common.Constants.DOMAIN;
import static org.xcsp.common.Constants.GROUP;
import static org.xcsp.common.Constants.MAX_SAFE_BYTE;
import static org.xcsp.common.Constants.MAX_SAFE_INT;
import static org.xcsp.common.Constants.MAX_SAFE_SHORT;
import static org.xcsp.common.Constants.MINIMIZE;
import static org.xcsp.common.Constants.MIN_SAFE_BYTE;
import static org.xcsp.common.Constants.MIN_SAFE_INT;
import static org.xcsp.common.Constants.MIN_SAFE_SHORT;
import static org.xcsp.common.Constants.OBJECTIVES;
import static org.xcsp.common.Constants.STAR;
import static org.xcsp.common.Constants.STAR_SYMBOL;
import static org.xcsp.common.Constants.STATIC;
import static org.xcsp.common.Constants.TIMES;
import static org.xcsp.common.Constants.VAL_HEURISTIC;
import static org.xcsp.common.Constants.VAR;
import static org.xcsp.common.Constants.VARIABLES;
import static org.xcsp.common.Types.TypeChild.FINAL;
import static org.xcsp.common.Types.TypeChild.arcs;
import static org.xcsp.common.Types.TypeChild.balance;
import static org.xcsp.common.Types.TypeChild.coeffs;
import static org.xcsp.common.Types.TypeChild.colOccurs;
import static org.xcsp.common.Types.TypeChild.condition;
import static org.xcsp.common.Types.TypeChild.conditions;
import static org.xcsp.common.Types.TypeChild.cost;
import static org.xcsp.common.Types.TypeChild.ends;
import static org.xcsp.common.Types.TypeChild.except;
import static org.xcsp.common.Types.TypeChild.function;
import static org.xcsp.common.Types.TypeChild.graph;
import static org.xcsp.common.Types.TypeChild.heights;
import static org.xcsp.common.Types.TypeChild.image;
import static org.xcsp.common.Types.TypeChild.index;
import static org.xcsp.common.Types.TypeChild.lengths;
import static org.xcsp.common.Types.TypeChild.limits;
import static org.xcsp.common.Types.TypeChild.list;
import static org.xcsp.common.Types.TypeChild.loads;
import static org.xcsp.common.Types.TypeChild.machines;
import static org.xcsp.common.Types.TypeChild.mapping;
import static org.xcsp.common.Types.TypeChild.matrix;
import static org.xcsp.common.Types.TypeChild.occurs;
import static org.xcsp.common.Types.TypeChild.operator;
import static org.xcsp.common.Types.TypeChild.origins;
import static org.xcsp.common.Types.TypeChild.patterns;
import static org.xcsp.common.Types.TypeChild.profits;
import static org.xcsp.common.Types.TypeChild.root;
import static org.xcsp.common.Types.TypeChild.rowOccurs;
import static org.xcsp.common.Types.TypeChild.rules;
import static org.xcsp.common.Types.TypeChild.set;
import static org.xcsp.common.Types.TypeChild.size;
import static org.xcsp.common.Types.TypeChild.sizes;
import static org.xcsp.common.Types.TypeChild.start;
import static org.xcsp.common.Types.TypeChild.terminal;
import static org.xcsp.common.Types.TypeChild.total;
import static org.xcsp.common.Types.TypeChild.transitions;
import static org.xcsp.common.Types.TypeChild.value;
import static org.xcsp.common.Types.TypeChild.values;
import static org.xcsp.common.Types.TypeChild.weights;
import static org.xcsp.common.Types.TypeChild.widths;
import static org.xcsp.common.Types.TypeConditionOperatorRel.EQ;
import static org.xcsp.common.Types.TypeConditionOperatorSet.IN;
import static org.xcsp.common.Types.TypeConditionOperatorSet.NOTIN;
import static org.xcsp.common.Utilities.childElementsOf;
import static org.xcsp.common.Utilities.control;
import static org.xcsp.common.Utilities.isLong;
import static org.xcsp.common.Utilities.isTag;
import static org.xcsp.common.Utilities.safeInt;
import static org.xcsp.common.Utilities.safeLong;
import static org.xcsp.common.Utilities.splitToInts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xcsp.common.Condition;
import org.xcsp.common.Condition.ConditionIntset;
import org.xcsp.common.Condition.ConditionIntvl;
import org.xcsp.common.Condition.ConditionPar1;
import org.xcsp.common.Condition.ConditionPar2;
import org.xcsp.common.Condition.ConditionVal;
import org.xcsp.common.Constants;
import org.xcsp.common.Softening;
import org.xcsp.common.Softening.SofteningExtension;
import org.xcsp.common.Softening.SofteningGlobal;
import org.xcsp.common.Softening.SofteningIntension;
import org.xcsp.common.Softening.SofteningSimple;
import org.xcsp.common.Types;
import org.xcsp.common.Types.TypeAtt;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.Types.TypeClass;
import org.xcsp.common.Types.TypeCombination;
import org.xcsp.common.Types.TypeConditionOperator;
import org.xcsp.common.Types.TypeConditionOperatorRel;
import org.xcsp.common.Types.TypeConditionOperatorSet;
import org.xcsp.common.Types.TypeCtr;
import org.xcsp.common.Types.TypeExpr;
import org.xcsp.common.Types.TypeFlag;
import org.xcsp.common.Types.TypeFramework;
import org.xcsp.common.Types.TypeMeasure;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Types.TypeOperator;
import org.xcsp.common.Types.TypeReification;
import org.xcsp.common.Types.TypeVar;
import org.xcsp.common.Utilities;
import org.xcsp.common.domains.Domains.Dom;
import org.xcsp.common.domains.Domains.DomBasic;
import org.xcsp.common.domains.Domains.DomGraph;
import org.xcsp.common.domains.Domains.DomSet;
import org.xcsp.common.domains.Domains.DomSymbolic;
import org.xcsp.common.domains.Domains.IDom;
import org.xcsp.common.domains.Values.Decimal;
import org.xcsp.common.domains.Values.IntegerEntity;
import org.xcsp.common.domains.Values.IntegerInterval;
import org.xcsp.common.domains.Values.Occurrences;
import org.xcsp.common.domains.Values.Rational;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.common.structures.AbstractTuple;
import org.xcsp.common.structures.AbstractTuple.HybridTuple;
import org.xcsp.common.structures.AbstractTuple.OrdinaryTuple;
import org.xcsp.common.structures.Transition;
import org.xcsp.parser.entries.ParsingEntry.AEntry;
import org.xcsp.parser.entries.ParsingEntry.CEntry;
import org.xcsp.parser.entries.ParsingEntry.OEntry;
import org.xcsp.parser.entries.ParsingEntry.VEntry;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.CEntryReifiable;
import org.xcsp.parser.entries.XConstraints.XBlock;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XConstraints.XGroup;
import org.xcsp.parser.entries.XConstraints.XLogic;
import org.xcsp.parser.entries.XConstraints.XParameter;
import org.xcsp.parser.entries.XConstraints.XReification;
import org.xcsp.parser.entries.XConstraints.XSeqbin;
import org.xcsp.parser.entries.XConstraints.XSlide;
import org.xcsp.parser.entries.XObjectives.OObjectiveExpr;
import org.xcsp.parser.entries.XObjectives.OObjectiveSpecial;
import org.xcsp.parser.entries.XVariables.XArray;
import org.xcsp.parser.entries.XVariables.XVar;
import org.xcsp.parser.entries.XVariables.XVarInteger;
import org.xcsp.parser.loaders.CtrLoaderInteger;

/**
 * This class corresponds to a Java parser that uses DOM (Document Object Model) to parse XCSP3 instances. <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author Christophe Lecoutre, CRIL-CNRS - lecoutre@cril.fr
 * @version 1.3
 */
public class XParser {
	public static boolean VERBOSE = false;

	public static String HYBRID = "hybrid";
	public static String HYBRID1 = "hybrid-1";
	public static String HYBRID2 = "hybrid-2";

	/** The document to be parsed. */
	private Document document; //

	/** An XPath object that is useful for some tasks (queries). */
	private XPath xpath = XPathFactory.newInstance().newXPath();

	/** The map that stores pairs (id,variable). */
	public Map<String, XVar> mapForVars = new LinkedHashMap<>();

	/** The map that stores pairs (id,array). */
	private Map<String, XArray> mapForArrays = new LinkedHashMap<>();

	/**
	 * A map used as a cache for avoiding building several times the same domain objects; it stores pairs (textualContent,domain).
	 */
	private Map<String, IDom> cacheForContentToDomain = new LinkedHashMap<>();

	/**
	 * The list of entries of the element <variables>. It contains variables and arrays.
	 */
	public List<VEntry> vEntries = new ArrayList<>();

	/**
	 * The list of entries of the element <constraints>. It contains stand-alone constraints (extension, intension, allDifferent, ...), groups of constraints,
	 * and meta-constraints (sliding and logical constructions).
	 */
	public List<CEntry> cEntries = new ArrayList<>();

	/**
	 * The list of objectives of the element <objectives>. Typically, it contains 0 or 1 objective.
	 */
	public List<OEntry> oEntries = new ArrayList<>();

	public List<AEntry> aEntries = new ArrayList<>();

	/** The type of the framework used for the loaded instance. */
	public TypeFramework typeFramework;

	/**
	 * In case of multi-objective optimization, indicates the type that must be considered.
	 */
	public TypeCombination typeCombination;

	/**
	 * The classes that must be discarded. Used just before posting variables, constraints and objectives.
	 **/
	public TypeClass[] discardedClasses;

	/**********************************************************************************************
	 * Parsing of Variables (and Domains)
	 *********************************************************************************************/

	/**
	 * Returns the value of the specified attribute for the specified element, if it exists, the specified default value otherwise.
	 */
	private <T extends Enum<T>> T giveAttributeValue(Element elt, String attName, Class<T> clazz, T defaultValue) {
		String s = elt.getAttribute(attName);
		return s.length() == 0 ? defaultValue : Types.valueOf(clazz, s.replaceFirst("\\s+", "_"));
	}

	/**
	 * Parses a basic domain, i.e., a domain for an integer, symbolic, float or stochastic variable (or array).
	 */
	private DomBasic parseDomBasic(Element elt, TypeVar type) {
		String content = elt.getTextContent().trim();
		return (DomBasic) cacheForContentToDomain.computeIfAbsent(content, k -> DomBasic.parse(content, type));
	}

	/** Parse a complex domain for a set variable (or array). */
	private DomSet parseDomSet(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt);
		String req = childs[0].getTextContent().trim(), pos = childs[1].getTextContent().trim(), content = req + " | " + pos;
		return (DomSet) cacheForContentToDomain.computeIfAbsent(content, k -> DomSet.parse(req, pos, type));
	}

	/** Parse a complex domain for a graph variable (or array). */
	private DomGraph parseDomGraph(Element elt, TypeVar type) {
		Element[] childs = childElementsOf(elt), req = childElementsOf(childs[0]), pos = childElementsOf(childs[1]);
		String reqV = req[0].getTextContent().trim(), reqE = req[1].getTextContent().trim();
		String posV = pos[0].getTextContent().trim(), posE = pos[1].getTextContent().trim();
		String content = reqV + " | " + reqE + " | " + posV + " | " + posE;
		return (DomGraph) cacheForContentToDomain.computeIfAbsent(content, k -> DomGraph.parse(reqV, reqE, posV, posE, type));
	}

	/** Parse a domain for any type of variable (or array). */
	private IDom parseDomain(Element elt, TypeVar type) {
		return type.isBasic() ? parseDomBasic(elt, type) : type.isSet() ? parseDomSet(elt, type) : parseDomGraph(elt, type);
	}

	/**
	 * Gives the 'size' (an array of integers as defined in XCSP3) of the array of variables.
	 */
	private int[] giveArraySize(Element elt) {
		StringTokenizer st = new StringTokenizer(elt.getAttribute(TypeAtt.size.name()), "[]");
		return IntStream.range(0, st.countTokens()).map(i -> Integer.parseInt(st.nextToken())).toArray();
	}

	/**
	 * Allows us to manage aliases, i.e., indirection due to the use of the 'as' attribute.
	 */
	private Element getActualElementToAnalyse(Element elt) {
		try {
			String id = elt.getAttribute(TypeAtt.as.name());
			return id.length() == 0 ? elt : (Element) xpath.evaluate("//*[@id='" + id + "']", document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
			return (Element) Utilities.control(false, "Bad use of 'as'" + elt.getTagName());
		}
	}

	/** Parses all elements inside the element <variables>. */
	public void parseVariables() {
		Map<String, IDom> cacheForId2Domain = new LinkedHashMap<>(); // a map for managing pairs (id,domain); remember that aliases can be encountered
		for (Element elt : childElementsOf((Element) document.getElementsByTagName(VARIABLES).item(0))) {
			VEntry entry = null;
			String id = elt.getAttribute(TypeAtt.id.name());
			TypeVar type = elt.getAttribute(TypeAtt.type.name()).length() == 0 ? TypeVar.integer : TypeVar.valueOf(elt.getAttribute(TypeAtt.type.name()));
			Element actualForElt = getActualElementToAnalyse(elt); // managing aliases, i.e., 'as' indirection
			Utilities.control(actualForElt != null, "in attribute \"as\" of variable with id \"" + id + "\"");
			IDom dom = cacheForId2Domain.get(actualForElt.getAttribute(TypeAtt.id.name())); // necessary not null when 'as' indirection
			if (elt.getTagName().equals(VAR)) {
				if (dom == null && !type.isQualitative()) {
					try {
						cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
					} catch (WrongTypeException e) {
						throw new WrongTypeException("for variable with id \"" + id + "\": " + e.getMessage());
					}
				}
				entry = XVar.build(id, type, dom);
			} else {
				int[] size = giveArraySize(elt);
				if (dom == null && !type.isQualitative()) {
					Element[] childs = childElementsOf(actualForElt);
					if (childs.length > 0 && childs[0].getTagName().equals(DOMAIN)) { // we have to deal with mixed domains
						XArray array = new XArray(id, type, size);
						Stream.of(childs).forEach(child -> {
							Element actualForChild = getActualElementToAnalyse(child);
							IDom domChild = cacheForId2Domain.get(actualForChild.getAttribute(TypeAtt.id.name()));
							if (domChild == null) {
								domChild = parseDomain(actualForChild, type);
								String idChild = child.getAttribute(TypeAtt.id.name());
								if (idChild.length() > 0)
									cacheForId2Domain.put(idChild, domChild);
							}
							array.setDom(child.getAttribute("for"), domChild);
						});
						entry = array;
					} else {
						cacheForId2Domain.put(id, dom = parseDomain(actualForElt, type));
						entry = new XArray(id, type, size, dom);
					}
				} else
					entry = new XArray(id, type, size, dom);
			}
			entry.copyAttributesOf(elt); // we copy the attributes for the variable or array
			if (!TypeClass.intersect(entry.classes, discardedClasses))
				vEntries.add(entry);
		}
		for (VEntry entry : vEntries)
			if (entry instanceof XVar)
				mapForVars.put(entry.id, (XVar) entry);
			else {
				Stream.of(((XArray) entry).vars).filter(x -> x != null).forEach(x -> mapForVars.put(x.id, x));
				mapForArrays.put(entry.id, (XArray) entry);
			}
		// entriesOfVariables.stream().forEach(e -> System.out.println(e));
	}

	/**********************************************************************************************
	 * General Parsing Methods (for basic entities, conditions, simple and double sequences)
	 *********************************************************************************************/

	/**
	 * Parse the specified token, as a variable, an interval, a rational, a decimal, a long, a set (literal), a parameter, a functional expression or an object
	 * 'Occurrences'. If nothing above matches, the token is returned (and considered as a symbolic value).
	 */
	private Object parseData(String tok) {
		if (mapForVars.get(tok) != null)
			return mapForVars.get(tok);
		if ((tok.charAt(0) == '*' || tok.charAt(0) == '-' || Character.isDigit(tok.charAt(0))) && tok.contains(TIMES)) {
			// to deal with compact forms of values (e.g.in solutions)
			String[] t = tok.split(TIMES);
			assert t.length == 2;
			return new Occurrences(t[0].equals("*") ? t[0] : safeLong(t[0]), Integer.parseInt(t[1]));
		}
		if (Character.isDigit(tok.charAt(0)) || tok.charAt(0) == '+' || tok.charAt(0) == '-') {
			String[] t = tok.split("\\.\\.");
			if (t.length == 2)
				return new IntegerInterval(safeLong(t[0]), safeLong(t[1]));
			t = tok.split("/");
			if (t.length == 2)
				return new Rational(safeLong(t[0]), safeLong(t[1]));
			t = tok.split("\\.");
			if (t.length == 2)
				return new Decimal(safeLong(t[0]), safeLong(t[1]));
			return safeLong(tok);
		}
		if (tok.charAt(0) == '{') { // set value
			String sub = tok.substring(1, tok.length() - 1); // empty set if sub.length() = 0
			return sub.length() == 0 ? new Object[] {} : Stream.of(sub.split("\\s*,\\s*")).mapToLong(s -> safeLong(s)).toArray();
		}
		if (tok.charAt(0) == '(') // condition
			return parseCondition(tok);
		if (tok.charAt(0) == '%')
			return new XParameter(tok.equals("%...") ? -1 : Integer.parseInt(tok.substring(1)));
		if (tok.indexOf("(") != -1)
			return parseExpression(tok);
		return tok; // tok must be a symbolic value (or *)
	}

	private Object parseData(Element elt) {
		return parseData(elt.getTextContent().trim());
	}

	/** Parses a pair of the form (operator, operand) */
	private Condition parseCondition(String tok) {
		int pos = tok.indexOf(',');
		String left = tok.substring(tok.charAt(0) != '(' ? 0 : 1, pos).trim();
		String right = tok.substring(pos + 1, tok.length() - (tok.charAt(tok.length() - 1) == ')' ? 1 : 0)).trim();
		TypeConditionOperator op = TypeConditionOperator.valueOf(left.trim().toUpperCase());
		return Condition.buildFrom(op, parseData(right));
	}

	/** Parses a pair of the form (operator, operand) */
	private Condition parseCondition(Element elt) {
		return parseCondition(elt.getTextContent().trim());
	}

	/** Parses a sequence of pairs of the form (operator, operand) */
	private Condition[] parseConditions(Element elt) {
		return Stream.of(elt.getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(tok -> parseCondition(tok)).toArray(Condition[]::new);
	}

	/**
	 * Parse a sequence of tokens (separated by the specified delimiter). Each token can represent a compact list of array variables, or a basic entity.
	 */
	public Object[] parseSequence(String seq, String delimiter) {
		List<Object> list = new ArrayList<>();
		for (String tok : seq.split(delimiter)) {
			int pos = tok.indexOf("[");
			XArray array = pos == -1 ? null : mapForArrays.get(tok.substring(0, pos));
			try {
				if (array != null)
					list.addAll(array.getVarsFor(tok));
				else if ((tok.charAt(0) == '-' || tok.charAt(0) == '+' || Character.isDigit(tok.charAt(0))) && tok.contains(TIMES)) {
					// we need to handle compact forms with 'x' as e.g. 1x12
					String[] t = tok.split(TIMES);
					long value = safeLong(t[0]), repeat = safeLong(t[1]);
					for (int i = 0; i < repeat; i++)
						list.add(value);
				} else
					list.add(parseData(tok));
			} catch (WrongTypeException e) {
				throw new WrongTypeException("in sequence \"" + seq + "\": " + e.getMessage());
			}
		}
		boolean presentVariable = false, presentTree = false, other = false;
		for (Object obj : list)
			if (obj instanceof XVar)
				presentVariable = true;
			else if (obj instanceof XNode)
				presentTree = true;
			else {
				other = true;
				break;
			}
		if (!other && presentVariable && presentTree)
			return list.stream().map(obj -> obj instanceof XVar ? new XNodeLeaf<XVar>(TypeExpr.VAR, obj) : obj).toArray(XNode[]::new);

		return Utilities.specificArrayFrom(list);
	}

	public Object[] parseSequence(Element elt) {
		return parseSequence(elt.getTextContent().trim(), "\\s+");
	}

	/**
	 * Parse a double sequence, i.e. a sequence of tokens separated by the specified delimiter, and composed of entities separated by ,
	 */
	private Object[][] parseDoubleSequence(Element elt, String delimiter) {
		String content = elt.getTextContent().trim();
		List<Object[]> list = Stream.of(content.split(delimiter)).skip(1).map(tok -> parseSequence(tok, "\\s*,\\s*")).collect(Collectors.toList());
		return Utilities.specificArray2DFrom(list);
	}

	/**
	 * Parse a double sequence of variables. Either the double sequence only contains simple variables, or is represented by a compact form.
	 */
	private Object[][] parseDoubleSequenceOfVars(Element elt) {
		String content = elt.getTextContent().trim();
		Utilities.control(content.charAt(0) != '%', "It is currently not possible to make abstraction of double sequences of variables");
		if (content.charAt(0) == '(') {
			List<Object[]> list = Stream.of(content.split(DELIMITER_LISTS)).skip(1).map(tok -> parseSequence(tok, "\\s*,\\s*")).collect(Collectors.toList());
			return Utilities.specificArray2DFrom(list);
		}
		XArray array = mapForArrays.get(content.substring(0, content.indexOf("[")));
		IntegerEntity[] indexRanges = array.buildIndexRanges(content);
		int first = -1, second = -1;
		for (int i = 0; first == -1 && i < indexRanges.length; i++)
			if (!indexRanges[i].isSingleton())
				first = i;
		for (int i = indexRanges.length - 1; second == -1 && i >= 0; i--)
			if (!indexRanges[i].isSingleton())
				second = i;
		int length1 = Math.toIntExact(indexRanges[first].width()), length2 = Math.toIntExact(indexRanges[second].width());
		Utilities.control(length1 != -1 && length2 != -1, "");
		List<Object[]> list2D = new ArrayList<>();
		int[] indexes = Stream.of(indexRanges).mapToInt(it -> (int) it.smallest()).toArray(); // first index
		for (int i = 0; i < length1; i++) {
			List<Object> list = new ArrayList<>();
			indexes[first] = i + (int) indexRanges[first].smallest();
			if (first == second)
				list.add(array.varAt(indexes));
			else
				for (int j = 0; j < length2; j++) {
					indexes[second] = j + (int) indexRanges[second].smallest();
					list.add(array.varAt(indexes));
				}
			list2D.add(Utilities.specificArrayFrom(list));
		}
		return Utilities.specificArray2DFrom(list2D);
	}

	/**
	 * The enum type describing the different types of primitives that can be used for representing arrays of integer tuples.
	 */
	public static enum TypePrimitive {
		BYTE, SHORT, INT, LONG;

		/**
		 * Returns the smallest primitive that can be used for representing values lying within the specified bounds.
		 */
		public static TypePrimitive whichPrimitiveFor(long inf, long sup) {
			if (MIN_SAFE_BYTE <= inf && sup <= MAX_SAFE_BYTE)
				return BYTE;
			if (MIN_SAFE_SHORT <= inf && sup <= MAX_SAFE_SHORT)
				return SHORT;
			if (MIN_SAFE_INT <= inf && sup <= MAX_SAFE_INT)
				return INT;
			// if (MIN_SAFE_LONG <= inf && sup <= MAX_SAFE_LONG)
			return LONG; // else return null;
		}

		/**
		 * Returns the smallest primitive that can be used for representing the specified value.
		 */
		public static TypePrimitive whichPrimitiveFor(long val) {
			return whichPrimitiveFor(val, val);
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is not integer,
		 * null is returned.
		 */
		static TypePrimitive whichPrimitiveFor(XVar[] vars) {
			if (Stream.of(vars).anyMatch(x -> x.type != TypeVar.integer))
				return null;
			return TypePrimitive.values()[Stream.of(vars).mapToInt(x -> ((XVarInteger) x).whichPrimitive().ordinal()).max()
					.orElse(TypePrimitive.LONG.ordinal())];
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is not integer,
		 * null is returned.
		 */
		static TypePrimitive whichPrimitiveFor(XVar[][] varss) {
			if (whichPrimitiveFor(varss[0]) == null)
				return null;
			return TypePrimitive.values()[Stream.of(varss).mapToInt(t -> whichPrimitiveFor(t).ordinal()).max().orElse(TypePrimitive.LONG.ordinal())];
		}

		/** Returns true iff the primitive can represent the specified value. */
		private boolean canRepresent(long val) {
			return this.ordinal() >= whichPrimitiveFor(val).ordinal();
		}

		/**
		 * Parse the specified string that denotes a sequence of values. In case we have at least one interval, we just return an array of IntegerEntity (as for
		 * integer domains), and no validity test on values is performed. Otherwise, we return an array of integer (either long[] or int[]). It is possible that
		 * some values are discarded because either they do not belong to the specified domain (test performed if this domain is not null), or they cannot be
		 * represented by the primitive.
		 */
		Object parseSeq(String s, Dom dom) {
			if (s.indexOf("..") != -1)
				return IntegerEntity.parseSeq(s);
			int nbDiscarded = 0;
			List<Long> list = new ArrayList<>();
			for (String tok : s.split("\\s+")) {
				assert !tok.equals("*") : "STAR not handled in unary lists";
				long l = Utilities.safeLong(tok);
				if (canRepresent(l) && (dom == null || dom.contains(l)))
					list.add(l);
				else
					nbDiscarded++;
			}
			if (nbDiscarded > 0)
				System.out.println(nbDiscarded + " discarded values in the unary list " + s);
			if (this == LONG)
				return list.stream().mapToLong(i -> i).toArray();
			else
				return list.stream().mapToInt(i -> i.intValue()).toArray();
			// TODO possible refinement for returning byte[] and short[]
		}

		/**
		 * Parse the specified string, and builds a tuple of (long) integers put in the specified array t. If the tuple is not valid wrt the specified domains
		 * or the primitive, false is returned, in which case, the tuple can be discarded. If * is encountered, the specified modifiable boolean is set to true.
		 */
		boolean parseOrdinaryTuple(String s, long[] t, DomBasic[] doms, AtomicBoolean ab) {
			String[] toks = s.split("\\s*,\\s*");
			assert toks.length == t.length : toks.length + " " + t.length;
			boolean starred = false;
			for (int i = 0; i < toks.length; i++) {
				if (toks[i].equals("*")) {
					t[i] = this == BYTE ? Constants.STAR_BYTE : this == SHORT ? Constants.STAR_SHORT : this == INT ? Constants.STAR : Constants.STAR_LONG;
					starred = true;
				} else {
					long l = Utilities.safeLong(toks[i]);
					if (canRepresent(l) && (doms == null || ((Dom) doms[i]).contains(l)))
						t[i] = l;
					else
						return false; // because the tuple can be discarded
				}
			}
			if (starred)
				ab.set(true);
			return true;
		}
	}

	/**********************************************************************************************
	 * Generic Constraints : Extension and Intension
	 *********************************************************************************************/

	private static final char UTF_NE = '\u2260';
	private static final char UTF_LT = '\uFE64';
	private static final char UTF_LE = '\u2264';
	private static final char UTF_GE = '\u2265';
	private static final char UTF_GT = '\uFE65';
	private static final char UTF_COMPLEMENT = '\u2201';
	private static final char HYBRID_COLUMN_SYMBOL = 'c';

	private static TypeConditionOperatorRel relOp(char c) {
		if (c == '=')
			return TypeConditionOperatorRel.EQ;
		if (c == UTF_NE)
			return TypeConditionOperatorRel.NE;
		if (c == UTF_LT)
			return TypeConditionOperatorRel.LT;
		if (c == UTF_LE)
			return TypeConditionOperatorRel.LE;
		if (c == UTF_GE)
			return TypeConditionOperatorRel.GE;
		if (c == UTF_GT)
			return TypeConditionOperatorRel.GT;
		return null;
	}

	private Object parseHybridCondition(String s) {
		assert s.length() > 0;
		if (s.equals(STAR_SYMBOL)) // if we have *
			return STAR;
		if (isLong(s)) // if we have an integer
			return safeLong(s);
		if (s.charAt(0) == HYBRID_COLUMN_SYMBOL)
			s = "=" + s; // we add the implicit operator (=)
		TypeConditionOperatorRel relop = relOp(s.charAt(0));
		if (relop == null) { // if we have a unary membership restriction ('in' or 'notin' in a range or set)
			TypeConditionOperatorSet setop = IN;
			if (s.charAt(0) == UTF_COMPLEMENT) {
				setop = NOTIN;
				s = s.substring(1);
			}
			if (s.indexOf("..") != -1) { // if we have a range of integers
				String[] t = s.split("\\.\\.");
				return new ConditionIntvl(setop, safeLong(t[0]), safeLong(t[1]));
			}
			control(s.charAt(0) == '{' && s.charAt(s.length() - 1) == '}', "a set was expected " + s); // we must have a set of integers
			return new ConditionIntset(setop, splitToInts(s.substring(1, s.length() - 1), "\\s"));
		}
		s = s.substring(1); // we discard the operator (first character) because we have relop (not null)
		if (s.charAt(0) != HYBRID_COLUMN_SYMBOL) // if we have a unary relational restriction
			return relop == EQ ? safeLong(s) : new ConditionVal(relop, safeLong(s));
		Boolean pos = s.contains("+") ? Boolean.TRUE : s.contains("-") ? Boolean.FALSE : null;
		if (pos == null) // if we have a basic binary restriction (i.e., without +/- k)
			return new ConditionPar1(relop, new XParameter(safeInt(safeLong(s.substring(1)))));
		String[] t = s.split(pos ? "\\+" : "\\-"); // it is either + or -
		control(t.length == 2 && t[0].charAt(0) == HYBRID_COLUMN_SYMBOL, "Bad form");
		int c0 = safeInt(safeLong(t[0].substring(1)));
		// long nColumns = s.codePoints().filter(c -> c == HYBRID_COLUMN_SYMBOL).count();
		if (t[1].charAt(0) == HYBRID_COLUMN_SYMBOL) { // if we have a ternary restriction
			control(relop == EQ, "For the moment, only EQ is managed");
			return new ConditionPar2(relop, new XParameter(c0), pos, new XParameter(safeInt(safeLong(t[1].substring(1)))));
		}
		control(Utilities.isLong(t[1]), "Bad form");
		return new ConditionPar2(relop, new XParameter(c0), pos, safeInt(safeLong(t[1])));
	}

	private String replaceInternCommas(String s) {
		boolean processing = false;
		String ps = "";
		for (char c : s.toCharArray()) {
			ps += processing && c == ',' ? ' ' : c;
			if (c == '{' || c == '}')
				processing = !processing;
		}
		return ps;
	}

	private AbstractTuple[] parseHybridTuples(Element elt) {
		String text = elt.getTextContent().trim();
		if (text.length() == 0)
			return null;
		List<Object> list = new ArrayList<>();
		for (String[] t : Stream.of(text.split(DELIMITER_LISTS)).skip(1).map(tok -> replaceInternCommas(tok).split("\\s*,\\s*")).toArray(String[][]::new)) {
			if (Stream.of(t).allMatch(s -> Utilities.isInteger(s) || s.equals("*"))) {
				list.add(new OrdinaryTuple(Stream.of(t).mapToInt(s -> s.equals("*") ? STAR : Utilities.toInteger(s)).toArray()));
			} else {
				list.add(new HybridTuple(Stream.of(t).map(s -> parseHybridCondition(s)).toArray()));
			}
		}
		return list.stream().toArray(AbstractTuple[]::new); // peek(t -> System.out.println("ttt=" + t)
	}

	private boolean parseSymbolicTuple(String[] t, DomBasic[] doms, AtomicBoolean ab) {
		boolean starred = false;
		for (int i = 0; i < t.length; i++)
			if (t[i].equals("*"))
				starred = true;
			else if (doms != null
					&& !(doms[i] instanceof DomSymbolic ? (((DomSymbolic) doms[i]).contains(t[i])) : ((Dom) doms[i]).contains(Integer.parseInt(t[i]))))
				return false;
		if (starred)
			ab.set(true);
		return true;
	}

	/**
	 * Parse the tuples contained in the specified element. A 2-dimensional array of String, byte, short, int or long is returned, depending of the specified
	 * primitive (primitive set to null stands for String). The specified array of domains, if not null, can be used to filter out some tuples.
	 */
	private Object parseTuples(Element elt, TypePrimitive primitive, DomBasic[] doms, AtomicBoolean ab) {
		String s = elt.getTextContent().trim();
		if (s.length() == 0)
			return null;
		if (s.charAt(0) != '(') { // necessarily a unary constraint if '(' not present as first character
			if (primitive == null) // case SYMBOLIC, so we return an array of string
				return Stream.of(s.split("\\s+")).filter(tok -> doms == null || ((DomSymbolic) doms[0]).contains(tok)).toArray(String[]::new);
			else
				return primitive.parseSeq(s, doms == null ? null : (Dom) doms[0]);
		}
		if (primitive == null) {
			// in that case, we keep String (although integers can also be present at some places with hybrid constraints)
			return Stream.of(s.split(DELIMITER_LISTS)).skip(1).map(tok -> tok.split("\\s*,\\s*")).filter(t -> parseSymbolicTuple(t, doms, ab))
					.toArray(String[][]::new);
		}
		List<Object> list = new ArrayList<>();
		int leftParenthesis = 0, rightParenthesis = leftParenthesis + 1;
		while (s.charAt(rightParenthesis) != ')')
			rightParenthesis++;
		String tok = s.substring(leftParenthesis + 1, rightParenthesis).trim();
		long[] tmp = new long[tok.split("\\s*,\\s*").length];
		while (tok != null) {
			if (primitive.parseOrdinaryTuple(tok, tmp, doms, ab)) // if not filtered-out parsed tuple
				if (primitive == TypePrimitive.BYTE) {
					byte[] t = new byte[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (byte) tmp[i];
					list.add(t);
				} else if (primitive == TypePrimitive.SHORT) {
					short[] t = new short[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (short) tmp[i];
					list.add(t);
				} else if (primitive == TypePrimitive.INT) {
					int[] t = new int[tmp.length];
					for (int i = 0; i < t.length; i++)
						t[i] = (int) tmp[i];
					list.add(t);
				} else
					list.add(tmp.clone());
			for (leftParenthesis = rightParenthesis + 1; leftParenthesis < s.length() && s.charAt(leftParenthesis) != '('; leftParenthesis++)
				;
			if (leftParenthesis == s.length())
				tok = null;
			else {
				for (rightParenthesis = leftParenthesis + 1; s.charAt(rightParenthesis) != ')'; rightParenthesis++)
					;
				tok = s.substring(leftParenthesis + 1, rightParenthesis).trim();
			}
		}
		// returns a 2-dimensional array of byte, short, int or long
		return list.size() == 0 ? new long[0][] : list.toArray((Object[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size()));
	}

	/**
	 * Returns the sequence of basic domains for the variables in the specified array.
	 */
	private DomBasic[] domainsFor(XVar[] vars) {
		return Stream.of(vars).map(x -> ((DomBasic) x.dom)).toArray(DomBasic[]::new);
	}

	/**
	 * Returns the sequence of basic domains for the variables in the first row of the specified two-dimensional array, provided that variables of the other
	 * rows have similar domains. Returns null otherwise.
	 */
	private DomBasic[] domainsFor(XVar[][] varss) {
		DomBasic[] doms = domainsFor(varss[0]);
		for (XVar[] vars : varss)
			if (IntStream.range(0, vars.length).anyMatch(i -> doms[i] != vars[i].dom))
				return null;
		return doms;
	}

	/** Parses a constraint <extension>. */
	private void parseExtension(Element elt, Element[] sons, Object[][] args) {
		boolean hybrid = elt.getAttribute(TypeAtt.type.name()).equals(HYBRID) || elt.getAttribute(TypeAtt.type.name()).equals(HYBRID1)
				|| elt.getAttribute(TypeAtt.type.name()).equals(HYBRID2);
		add(list, sons[0]);
		TypeChild typeTuples = TypeChild.valueOf(sons[1].getTagName());
		if (!hybrid) {
			XVar[] vars = leafs.get(0).value instanceof XVar[] ? (XVar[]) leafs.get(0).value : null; // may be null if a constraint template
			TypePrimitive primitive = args != null ? TypePrimitive.whichPrimitiveFor((XVar[][]) args)
					: vars != null ? TypePrimitive.whichPrimitiveFor(vars) : null;
			DomBasic[] doms = args != null ? domainsFor((XVar[][]) args) : vars != null ? domainsFor(vars) : null;
			AtomicBoolean ab = new AtomicBoolean();
			// We use doms to possibly filter out some tuples, and primitive to build an array of values of this primitive (short, byte, int or long)
			CChild tuples = addLeaf(typeTuples, parseTuples(sons[1], primitive, doms, ab));
			if (doms == null || tuples.value instanceof IntegerEntity[])
				tuples.flags.add(TypeFlag.UNCLEAN_TUPLES); // we inform solvers that some tuples can be invalid (wrt the domains of variables)
			if (ab.get())
				tuples.flags.add(TypeFlag.STARRED_TUPLES); // we inform solvers that the table (list of tuples) contains the special value *
		} else {
			// System.out.println(HYBRID);
			CChild tuples = addLeaf(typeTuples, parseHybridTuples(sons[1]));
			tuples.flags.add(TypeFlag.SMART_TUPLES); // we inform solvers that the table (list of tuples) contains hybrid tuples
		}
	}

	/**
	 * Parses a functional expression, as used for example in elements <intension>.
	 */
	private XNode<XVar> parseExpression(String s) {
		// System.out.println("parsing " + s);
		int leftParenthesisPosition = s.indexOf('(');
		if (leftParenthesisPosition == -1) { // i.e., if leaf
			XVar var = mapForVars.get(s);
			if (var != null)
				return new XNodeLeaf<XVar>(TypeExpr.VAR, var);
			if (s.charAt(0) == '%') {
				long l = safeLong(s.substring(1));
				Utilities.control(Utilities.isSafeInt(l), "Bad value (index) for the parameter");
				return new XNodeLeaf<XVar>(TypeExpr.PAR, l); // for simplicity, we only record Long, although we know
																// here that we necessarily have an int
			}
			String[] t = s.split("\\.");
			if (t.length == 2)
				return new XNodeLeaf<XVar>(TypeExpr.DECIMAL, new Decimal(safeLong(t[0]), safeLong(t[1])));
			if (Character.isDigit(s.charAt(0)) || s.charAt(0) == '+' || s.charAt(0) == '-')
				return new XNodeLeaf<XVar>(TypeExpr.LONG, safeLong(s));
			return new XNodeLeaf<XVar>(TypeExpr.SYMBOL, s);
		} else {
			int rightParenthesisPosition = s.lastIndexOf(")");
			TypeExpr operator = TypeExpr.valueOf(s.substring(0, leftParenthesisPosition).toUpperCase());
			if (leftParenthesisPosition == rightParenthesisPosition - 1) { // actually, this is also a leaf which is set(), the empty set
				control(operator == TypeExpr.SET, " Erreur");
				return new XNodeLeaf<XVar>(TypeExpr.SET, (Object) null);
			}
			String content = s.substring(leftParenthesisPosition + 1, rightParenthesisPosition);
			List<XNode<XVar>> nodes = new ArrayList<>();
			for (int right = 0; right < content.length(); right++) {
				int left = right;
				for (int nbOpens = 0; right < content.length(); right++) {
					if (content.charAt(right) == '(')
						nbOpens++;
					else if (content.charAt(right) == ')')
						nbOpens--;
					else if (content.charAt(right) == ',' && nbOpens == 0)
						break;
				}
				nodes.add(parseExpression(content.substring(left, right).trim()));
			}
			return new XNodeParent<XVar>(operator, nodes);
		}
	}

	/** Parses a constraint <intension>. */
	private void parseIntension(Element elt, Element[] sons) {
		add(function, sons.length == 0 ? elt : sons[0]);
	}

	/** Parses a constraint <smart>. Will be included in specifications later. */
	private void parseSmart(Element elt, Element[] sons) {
		for (Element son : sons)
			add(list, son);
	}

	/** Parses a constraint <adhoc>. */
	private void parseAdhoc(Element elt, Element[] sons) {
		addLeaf(TypeChild.form, sons[0].getTextContent().trim());
		Map<String, Object> map = new LinkedHashMap<>();
		for (int i = 1; i < sons.length; i++) {
			Element son = sons[i];
			TypeChild type = TypeChild.valueOf(son.getTagName());
			if (type == TypeChild.note)
				continue;
			map.put(son.getTagName(), parsing(type, son));
		}
		addLeaf(TypeChild.map, map);
	}

	/**********************************************************************************************
	 * Language-based Constraints
	 *********************************************************************************************/

	private void parseRegular(Element elt, Element[] sons) {
		add(list, sons[0]);
		Transition[] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> replaceInternCommas(t)).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = parseHybridCondition(tr[1]);
			// Object value = general ? Utilities.splitToInts(tr[1].substring(1,tr[1].length() - 1))
			// : Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ?
			// safeLong(tr[1]) : tr[1];
			return new Transition(tr[0], value, tr[2]);
		}).toArray(Transition[]::new);
		addLeaf(transitions, trans);
		addLeaf(start, sons[2].getTextContent().trim());
		addLeaf(FINAL, sons[3].getTextContent().trim().split("\\s+"));
	}

	private void parseGrammar(Element elt, Element[] sons) {
		add(list, sons[0]);
		addLeaf(terminal, sons[1].getTextContent().trim().split("\\s+"));
		String[][][] rrules = Stream.of(sons[2].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] sp = t.split("\\s*,\\s*");
			String[] leftWord = sp[0].split("\\s+"), rightWord = sp.length == 1 ? new String[] { "" } : sp[1].split("\\s+");
			return new String[][] { leftWord, rightWord };
		}).toArray(String[][][]::new);
		addLeaf(rules, rrules);
		addLeaf(start, sons[3].getTextContent().trim());
	}

	private void parseMDD(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		Transition[] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ? safeLong(tr[1]) : tr[1];
			return new Transition(tr[0], value, tr[2]);
		}).toArray(Transition[]::new);
		// String[][] trans =
		// Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t
		// ->
		// t.split("\\s*,\\s*")).toArray(String[][]::new);
		addLeaf(transitions, trans);
	}

	/**********************************************************************************************
	 * Comparison-based Constraints
	 *********************************************************************************************/

	private void parseAllDifferent(Element elt, Element[] sons, int lastSon) {
		// System.out.println(TypeCtr.valueOf(elt.getTagName()));
		if (sons.length == 0)
			add(list, elt);
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			Element exceptSon = isTag(sons[lastSon], except) ? sons[lastSon] : null;
			if (type == matrix) {
				add(matrix, sons[0]);
				if (lastSon == 1)
					addLeaf(except, leafs.get(0).setVariableInvolved() ? parseDoubleSequence(exceptSon, DELIMITER_SETS) : parseSequence(exceptSon));
			} else {
				for (int i = 0, limit = lastSon - (exceptSon != null ? 1 : 0); i <= limit; i++)
					addLeaf(type, parseSequence(sons[i]));
				if (exceptSon != null) {
					if (lastSon == 1)
						addLeaf(except, leafs.get(0).setVariableInvolved() ? parseDoubleSequence(exceptSon, DELIMITER_SETS) : parseSequence(exceptSon));
					else
						addLeaf(except, parseDoubleSequence(exceptSon, type == list ? DELIMITER_LISTS : type == set ? DELIMITER_SETS : DELIMITER_MSETS));
				}
			}
		}
	}

	private void parseAllEqual(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			add(list, elt);
		else
			for (int i = 0; i <= lastSon; i++)
				add(sons[i]);
	}

	private void parseAllDistant(Element elt, Element[] sons, int lastSon) {
		for (int i = 0; i < lastSon; i++)
			add(sons[i]);
		add(condition, sons[lastSon]);
	}

	private void parseOrdered(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		if (type == matrix)
			add(matrix, sons[0]);
		else
			for (int i = 0; i < lastSon; i++)
				add(sons[i]);
		add(operator, sons[lastSon]);
	}

	private void parseLex(Element elt, Element[] sons, int lastSon) {
		parseOrdered(elt, sons, lastSon);
	}

	private void parseAllIncomparable(Element elt, Element[] sons, int lastSon) {
		for (int i = 0; i <= lastSon; i++)
			add(sons[i]);
	}

	/**********************************************************************************************
	 * Counting and Summing Constraints
	 *********************************************************************************************/

	private void parseSum(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], list))
			add(list, sons[0]);
		else
			add(index, sons[0]);
		if (isTag(sons[1], coeffs)) // if (lastSon == 2)
			add(coeffs, sons[1]);
		add(condition, sons[lastSon]);
	}

	private void parseCount(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		add(values, sons[1]);
		add(condition, sons[lastSon]);
	}

	private void parseNValues(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		Element exceptSon = isTag(sons[lastSon - 1], except) ? sons[lastSon - 1] : null;
		for (int i = 0, limit = lastSon - (exceptSon != null ? 2 : 1); i <= limit; i++)
			addLeaf(type, parseSequence(sons[i]));
		if (exceptSon != null)
			addLeaf(except, lastSon == 2 ? parseSequence(exceptSon)
					: parseDoubleSequence(exceptSon, type == list ? DELIMITER_LISTS : type == set ? DELIMITER_SETS : DELIMITER_MSETS));
		add(condition, sons[lastSon]);
	}

	private void parseCardinality(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], matrix)) {
			add(matrix, sons[0]);
			add(values, sons[1]);
			addLeaf(rowOccurs, parseDoubleSequenceOfVars(sons[2]));
			addLeaf(colOccurs, parseDoubleSequenceOfVars(sons[3]));
		} else {
			add(list, sons[0]);
			add(values, sons[1]);
			addLeaf(occurs, parseSequence(sons[2]));
		}
	}

	private void parseBalance(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		if (isTag(sons[1], values))
			add(values, sons[1]);
		add(condition, sons[lastSon]);
	}

	private void parseSpread(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		if (isTag(sons[1], total))
			add(total, sons[1]);
		add(condition, sons[lastSon]);
	}

	private void parseDeviation(Element elt, Element[] sons, int lastSon) {
		parseSpread(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Connection Constraints
	 *********************************************************************************************/

	private void parseMaximum(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		if (isTag(sons[1], index))
			add(index, sons[1]);
		if (isTag(sons[lastSon], condition))
			add(condition, sons[lastSon]);
	}

	private void parseMinimum(Element elt, Element[] sons, int lastSon) {
		parseMaximum(elt, sons, lastSon); // because similar parsing
	}

	private void parseMaximumArg(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		if (isTag(sons[lastSon], condition))
			add(condition, sons[lastSon]);
	}

	private void parseMinimumArg(Element elt, Element[] sons, int lastSon) {
		parseMaximumArg(elt, sons, lastSon); // because similar parsing
	}

	private void parseElement(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], matrix)) {
			add(matrix, sons[0]);
			if (isTag(sons[1], index))
				addLeaf(index, parseSequence(sons[1])); // not default case for index
		} else {
			add(list, sons[0]);
			if (isTag(sons[1], index))
				add(index, sons[1]);
		}
		if (isTag(sons[lastSon], value))
			add(value, sons[lastSon]);
		else
			add(condition, sons[lastSon]);
	}

	private void parseChannel(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			add(list, elt);
		else {
			add(list, sons[0]);
			if (lastSon == 1) {
				if (isTag(sons[1], list))
					add(list, sons[1]);
				else
					add(value, sons[1]);
			}
		}
	}

	private void parsePermutation(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		add(list, sons[1]);
		if (lastSon == 2)
			add(mapping, sons[2]);
	}

	private void parsePrecedence(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			add(list, elt);
		else {
			add(list, sons[0]);
			if (lastSon == 1)
				add(values, sons[1]);
			// if (lastSon == 2)
			// addLeaf(operator, TypeOperator.valOf(sons[lastSon].getTextContent()));
		}
	}

	/**********************************************************************************************
	 * Packing and Scheduling Constraints
	 *********************************************************************************************/

	private void parseStretch(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		add(values, sons[1]);
		add(widths, sons[2]);
		if (lastSon == 3)
			addLeaf(patterns, parseDoubleSequence(sons[3], DELIMITER_LISTS));
	}

	private void parseNoOverlap(Element elt, Element[] sons) {
		boolean multiDimensional = sons[1].getTextContent().trim().charAt(0) == '(';
		addLeaf(origins, multiDimensional ? parseDoubleSequenceOfVars(sons[0]) : parseSequence(sons[0])); // not default case
		addLeaf(lengths, multiDimensional ? parseDoubleSequence(sons[1], DELIMITER_LISTS) : parseSequence(sons[1])); // not default case
	}

	private void parseCumulative(Element elt, Element[] sons) {
		int cnt = 0;
		add(origins, sons[cnt++]);
		add(lengths, sons[cnt++]);
		if (isTag(sons[cnt], ends))
			add(ends, sons[cnt++]);
		add(heights, sons[cnt++]);
		if (isTag(sons[cnt], machines)) {
			add(machines, sons[cnt++]);
			add(conditions, sons[cnt++]);
		} else
			add(condition, sons[cnt++]);
	}

	private void parseBinPacking(Element elt, Element[] sons) {
		add(list, sons[0]);
		add(sizes, sons[1]);
		if (isTag(sons[2], limits))
			add(limits, sons[2]);
		else if (isTag(sons[2], loads))
			add(loads, sons[2]);
		else if (isTag(sons[2], condition))
			add(condition, sons[2]);
		else
			add(conditions, sons[2]);
	}

	private void parseKnapsack(Element elt, Element[] sons) {
		add(list, sons[0]);
		add(weights, sons[1]);
		addLeaf(condition, parseData(sons[2])); // not default case for condition
		add(profits, sons[3]);
		add(condition, sons[4]);
	}

	private void parseFlow(Element elt, Element[] sons) {
		add(list, sons[0]);
		add(balance, sons[1]);
		addLeaf(arcs, parseDoubleSequence(sons[2], DELIMITER_LISTS));
		if (sons.length == 5) {
			add(weights, sons[3]);
			add(condition, sons[4]);
		}
	}

	/**********************************************************************************************
	 * Graph Constraints
	 *********************************************************************************************/

	private CChild listOrGraph(Element elt) {
		return isTag(elt, list) ? new CChild(list, parseSequence(elt)) : new CChild(graph, parseData(elt));
	}

	private void parseCircuit(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			add(list, elt);
		else {
			leafs.add(listOrGraph(sons[0]));
			if (lastSon == 1)
				add(size, sons[1]);
		}
	}

	private void parseNCircuits(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		add(condition, sons[1]);
	}

	private void parsePath(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		addLeaf(start, parseData(sons[1]));
		addLeaf(FINAL, parseData(sons[2]));
		if (lastSon == 3)
			add(size, sons[3]);
	}

	private void parseNPaths(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseTree(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		addLeaf(root, parseData(sons[1]));
		if (lastSon == 2)
			add(size, sons[2]);
	}

	private void parseArbo(Element elt, Element[] sons, int lastSon) {
		parseTree(elt, sons, lastSon);
	}

	private void parseNTrees(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseNArbos(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseNCliques(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Elementary Constraints
	 *********************************************************************************************/

	private void parseClause(Element elt, Element[] sons, int lastSon) {
		add(list, sons.length == 0 ? elt : sons[0]);
	}

	private void parseInstantiation(Element elt, Element[] sons, int lastSon) {
		add(list, sons[0]);
		add(values, sons[1]);
	}

	/**********************************************************************************************
	 * Set Constraints
	 *********************************************************************************************/

	private void parseAllIntersecting(Element elt, Element[] sons) {
		if (sons.length == 0)
			add(list, elt); // necessary, case disjoint or overlapping
		else {
			add(list, sons[0]);
			add(condition, sons[1]);
		}
	}

	private void parseRange(Element elt, Element[] sons) {
		add(list, sons[0]);
		add(index, sons[1]);
		add(image, sons[2]);
	}

	private void parseRoots(Element elt, Element[] sons) {
		parseRange(elt, sons);
	}

	private void parsePartition(Element elt, Element[] sons) {
		add(list, sons[0]);
		add(value, sons[1]);
	}

	/**********************************************************************************************
	 ***** Main methods for constraints
	 *********************************************************************************************/

	private List<CChild> leafs; // is you want to avoid this field, just pass it through as argument of every
								// method called in the long sequence of 'if' below

	private CChild addLeaf(TypeChild tc, Object value) {
		CChild child = new CChild(tc, value);
		leafs.add(child);
		return child;
	}

	private Object parsing(TypeChild tc, Element elt) {
		switch (tc) {
		case function:
			return parseExpression(elt.getTextContent().trim());
		case list:
			return parseSequence(elt);
		case matrix:
			return parseDoubleSequenceOfVars(elt);
		case condition:
			return parseCondition(elt);
		case conditions:
			return parseConditions(elt);
		case operator:
			return TypeOperator.valOf(elt.getTextContent());
		case index:
		case value:
		case size:
		case total:
		case image:
			return parseData(elt);
		default:
			// case coeffs values mapping widths sizes limits loads weights heights profits balance origins lengths ends machines
			return parseSequence(elt);
		}
	}

	private CChild add(TypeChild tc, Element elt) {
		return addLeaf(tc, parsing(tc, elt));
	}

	private CChild add(Element elt) {
		return addLeaf(TypeChild.valueOf(elt.getTagName()), parseSequence(elt));
	}

	private int getIntValueOf(Element element, String attName, int defaultValue) {
		return element.getAttribute(attName).length() > 0 ? Integer.parseInt(element.getAttribute(attName)) : defaultValue;
	}

	/**
	 * Parses an entry of <constraints>, except that soft and reification features are managed apart (in the calling method).
	 * 
	 * @param elt
	 *            The element to parse (must be a group, a meta-constraint or a constraint)
	 * @param args
	 *            Only useful for extension constraints, so as to possibly filter tuples, when analyzing the possible args (scopes)
	 * @param sons
	 *            The set of child elements of elt
	 * @param lastSon
	 *            The position of the last son to handle when parsing here (since <cost>, if present, is managed apart)
	 * @return the parsed entry
	 */
	private CEntry parseCEntry(Element elt, Object[][] args, Element[] sons, int lastSon) {
		if (elt.getTagName().equals(GROUP)) {
			List<Object[]> l = IntStream.range(1, lastSon + 1).mapToObj(i -> parseSequence(sons[i])).collect(Collectors.toList());
			Object[][] groupArgs = l.stream().noneMatch(o -> !(o instanceof XVar[])) ? l.toArray(new XVar[0][])
					: l.stream().noneMatch(o -> !(o instanceof XNode[])) ? l.toArray(new XNode[0][]) : l.toArray(new Object[0][]);
			return new XGroup((CEntryReifiable) parseCEntryOuter(sons[0], groupArgs), groupArgs);
		}
		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.slide) {
			CChild[] lists = IntStream.range(0, lastSon).mapToObj(i -> new CChild(list, parseSequence(sons[i]))).toArray(CChild[]::new);
			int[] offset = Stream.of(sons).limit(lists.length).mapToInt(s -> getIntValueOf(s, TypeAtt.offset.name(), 1)).toArray();
			int[] collect = Stream.of(sons).limit(lists.length).mapToInt(s -> getIntValueOf(s, TypeAtt.collect.name(), 1)).toArray();
			if (lists.length == 1) { // we need to compute the value of collect[0], which corresponds to the arity of the constraint template
				XCtr ctr = (XCtr) parseCEntryOuter(sons[lastSon], null);
				Utilities.control(ctr.abstraction.abstractChilds.length == 1, "Other cases must be implemented");
				if (ctr.getType() == TypeCtr.intension)
					collect[0] = ((XNode<?>) (ctr.childs[0].value)).maxParameterNumber() + 1;
				else {
					XParameter[] pars = (XParameter[]) ctr.abstraction.abstractChilds[0].value;
					Utilities.control(Stream.of(pars).noneMatch(p -> p.number == -1), "One parameter is %..., which is forbidden in slide");
					collect[0] = Stream.of(pars).mapToInt(p -> p.number + 1).max().orElseThrow(() -> new RuntimeException());
				}
			}
			XVar[][] scopes = XSlide.buildScopes(Stream.of(lists).map(ls -> (XVar[]) ls.value).toArray(XVar[][]::new), offset, collect,
					elt.getAttribute(TypeAtt.circular.name()).equals(Boolean.TRUE.toString()));
			return new XSlide(lists, offset, collect, (XCtr) parseCEntryOuter(sons[lastSon], scopes), scopes);
		}

		if (type == TypeCtr.seqbin) {
			CChild list = new CChild(TypeChild.list, parseSequence(sons[0]));
			XVar[] t = (XVar[]) list.value;
			XVar[][] scopes = IntStream.range(0, t.length - 1).mapToObj(i -> new XVar[] { t[i], t[i + 1] }).toArray(XVar[][]::new);
			CChild number = new CChild(TypeChild.number, parseData(sons[3]));
			return new XSeqbin(list, (XCtr) parseCEntryOuter(sons[1], scopes), (XCtr) parseCEntryOuter(sons[2], scopes), number, scopes);
		}

		if (type.isLogical() || type.isControl())
			return new XLogic(type, IntStream.range(0, lastSon + 1).mapToObj(i -> parseCEntryOuter(sons[i], args)).toArray(CEntryReifiable[]::new));

		leafs = new ArrayList<>();
		if (type == TypeCtr.extension)
			parseExtension(elt, sons, args);
		else if (type == TypeCtr.intension)
			parseIntension(elt, sons);
		else if (type == TypeCtr.regular)
			parseRegular(elt, sons);
		else if (type == TypeCtr.grammar)
			parseGrammar(elt, sons);
		else if (type == TypeCtr.mdd)
			parseMDD(elt, sons, lastSon);
		else if (type == TypeCtr.allDifferent)
			parseAllDifferent(elt, sons, lastSon);
		else if (type == TypeCtr.allEqual)
			parseAllEqual(elt, sons, lastSon);
		else if (type == TypeCtr.allDistant)
			parseAllDistant(elt, sons, lastSon);
		else if (type == TypeCtr.ordered)
			parseOrdered(elt, sons, lastSon);
		else if (type == TypeCtr.lex)
			parseLex(elt, sons, lastSon);
		else if (type == TypeCtr.allIncomparable)
			parseAllIncomparable(elt, sons, lastSon);
		else if (type == TypeCtr.sum)
			parseSum(elt, sons, lastSon);
		else if (type == TypeCtr.count)
			parseCount(elt, sons, lastSon);
		else if (type == TypeCtr.nValues)
			parseNValues(elt, sons, lastSon);
		else if (type == TypeCtr.cardinality)
			parseCardinality(elt, sons, lastSon);
		else if (type == TypeCtr.balance)
			parseBalance(elt, sons, lastSon);
		else if (type == TypeCtr.spread)
			parseSpread(elt, sons, lastSon);
		else if (type == TypeCtr.deviation)
			parseDeviation(elt, sons, lastSon);
		else if (type == TypeCtr.maximum)
			parseMaximum(elt, sons, lastSon);
		else if (type == TypeCtr.minimum)
			parseMinimum(elt, sons, lastSon);
		else if (type == TypeCtr.maximumArg)
			parseMaximumArg(elt, sons, lastSon);
		else if (type == TypeCtr.minimumArg)
			parseMinimumArg(elt, sons, lastSon);
		else if (type == TypeCtr.element)
			parseElement(elt, sons, lastSon);
		else if (type == TypeCtr.channel)
			parseChannel(elt, sons, lastSon);
		else if (type == TypeCtr.permutation)
			parsePermutation(elt, sons, lastSon);
		else if (type == TypeCtr.precedence)
			parsePrecedence(elt, sons, lastSon);
		else if (type == TypeCtr.stretch)
			parseStretch(elt, sons, lastSon);
		else if (type == TypeCtr.noOverlap)
			parseNoOverlap(elt, sons);
		else if (type == TypeCtr.cumulative)
			parseCumulative(elt, sons);
		else if (type == TypeCtr.binPacking)
			parseBinPacking(elt, sons);
		else if (type == TypeCtr.knapsack)
			parseKnapsack(elt, sons);
		else if (type == TypeCtr.flow)
			parseFlow(elt, sons);
		else if (type == TypeCtr.circuit)
			parseCircuit(elt, sons, lastSon);
		else if (type == TypeCtr.nCircuits)
			parseNCircuits(elt, sons, lastSon);
		else if (type == TypeCtr.path)
			parsePath(elt, sons, lastSon);
		else if (type == TypeCtr.nPaths)
			parseNPaths(elt, sons, lastSon);
		else if (type == TypeCtr.tree)
			parseTree(elt, sons, lastSon);
		else if (type == TypeCtr.nTrees)
			parseNTrees(elt, sons, lastSon);
		else if (type == TypeCtr.arbo)
			parseArbo(elt, sons, lastSon);
		else if (type == TypeCtr.nArbos)
			parseNArbos(elt, sons, lastSon);
		else if (type == TypeCtr.nCliques)
			parseNCliques(elt, sons, lastSon);
		else if (type == TypeCtr.clause)
			parseClause(elt, sons, lastSon);
		else if (type == TypeCtr.instantiation)
			parseInstantiation(elt, sons, lastSon);
		else if (type == TypeCtr.allIntersecting)
			parseAllIntersecting(elt, sons);
		else if (type == TypeCtr.range)
			parseRange(elt, sons);
		else if (type == TypeCtr.roots)
			parseRoots(elt, sons);
		else if (type == TypeCtr.partition)
			parsePartition(elt, sons);
		else if (type == TypeCtr.smart)
			parseSmart(elt, sons);
		else if (type == TypeCtr.adhoc)
			parseAdhoc(elt, sons);
		return new XCtr(type, leafs.toArray(new CChild[leafs.size()]));
	}

	// condition at null means a cost function (aka weighted constraint), otherwise
	// a cost-integrated soft constraint
	private Softening buildSoftening(Element elt, Map<TypeAtt, String> attributes, Condition cost) {
		if (attributes.containsKey(TypeAtt.violationCost)) {
			int violationCost = Utilities.safeInt(safeLong(attributes.get(TypeAtt.violationCost)));
			return cost == null ? new SofteningSimple(violationCost) : new SofteningSimple(cost, violationCost);
		}
		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.intension)
			return cost == null ? new SofteningIntension() : new SofteningIntension(cost);
		if (type == TypeCtr.extension) {
			int defaultCost = attributes.containsKey(TypeAtt.defaultCost) ? Utilities.safeInt(safeLong(attributes.get(TypeAtt.defaultCost))) : -1;
			return cost == null ? new SofteningExtension(defaultCost) : new SofteningExtension(cost, defaultCost);
		}
		TypeMeasure typeMeasure = attributes.containsKey(TypeAtt.violationMeasure) ? Types.valueOf(TypeMeasure.class, attributes.get(TypeAtt.violationMeasure))
				: null;
		String parameters = attributes.get(TypeAtt.violationParameters);
		return cost == null ? new SofteningGlobal(typeMeasure, parameters) : new SofteningGlobal(cost, typeMeasure, parameters);
	}

	/**
	 * Called to parse any constraint entry in <constraints> , that can be a group, a constraint, or a meta-constraint. This method calls parseCEntry.
	 */
	private CEntry parseCEntryOuter(Element elt, Object[][] args) {
		Element[] sons = childElementsOf(elt);
		boolean soft = elt.getAttribute(TypeAtt.type.name()).equals("soft");
		int lastSon = sons.length - 1 - (sons.length > 1 && isTag(sons[sons.length - 1], cost) ? 1 : 0);
		// last son position, excluding <cost> that is managed apart
		CEntry entry = parseCEntry(elt, args, sons, lastSon);
		entry.copyAttributesOf(elt); // we copy the attributes
		if (entry instanceof XCtr) {
			if (((XCtr) entry).type != TypeCtr.adhoc)
				for (int i = 0; i <= lastSon; i++)
					((XCtr) entry).childs[i].copyAttributesOf(sons[i]); // we copy the attributes for each parameter of the constraint
		} else if (entry instanceof XSlide)
			for (int i = 0; i < lastSon; i++)
				((XSlide) entry).lists[i].copyAttributesOf(sons[i]); // we copy the attributes for the list(s) involved
																		// in slide
		// Note that for seqbin and logic entries, no need to copy any attributes at this place

		if (entry instanceof CEntryReifiable) {
			CEntryReifiable entryReifiable = (CEntryReifiable) entry;
			Map<TypeAtt, String> attributes = entryReifiable.attributes;
			if (soft) { // dealing with softening
				Condition cost = lastSon == sons.length - 1 ? null : parseCondition(sons[sons.length - 1]);
				// condition at null means a cost function (aka weighted constraint), otherwise
				// a cost-integrated soft
				// constraint
				entryReifiable.softening = buildSoftening(elt, attributes, cost);
				// } else {
				// assert lastSon == sons.length - 2; // cost-integrated soft constraint
				//
				// Integer defaultCost = attributes.containsKey(TypeAtt.defaultCost) ?
				// Integer.parseInt(attributes.get(TypeAtt.defaultCost))
				// : null;
				// NamedNodeMap al = sons[sons.length - 1].getAttributes();
				// TypeMeasure type = al.getNamedItem(TypeAtt.violationMeasure.name()) == null ?
				// null :
				// XEnums.valueOf(TypeMeasure.class,
				// al.getNamedItem(TypeAtt.violationMeasure.name()).getNodeValue());
				// String parameters = al.getNamedItem(TypeAtt.violationParameters.name()) ==
				// null ? null : al
				// .getNamedItem(TypeAtt.violationParameters.name()).getNodeValue();
				// Condition condition = parseCondition(sons[sons.length - 1]);
				// entryReifiable.softening = new XSoftening(type, parameters, condition,
				// defaultCost);
				// }
			}
			// dealing with reification
			if (attributes.containsKey(TypeAtt.reifiedBy))
				entryReifiable.reification = new XReification(TypeReification.FULL, mapForVars.get(attributes.get(TypeAtt.reifiedBy)));
			else if (attributes.containsKey(TypeAtt.hreifiedFrom))
				entryReifiable.reification = new XReification(TypeReification.HALF_FROM, mapForVars.get(attributes.get(TypeAtt.hreifiedFrom)));
			else if (attributes.containsKey(TypeAtt.hreifiedTo))
				entryReifiable.reification = new XReification(TypeReification.HALF_TO, mapForVars.get(attributes.get(TypeAtt.hreifiedTo)));
		}
		return entry;
	}

	/** Recursive parsing, traversing possibly multiple blocks */
	private void recursiveParsingOfConstraints(Element elt, List<CEntry> list) {
		if (elt.getTagName().equals(BLOCK)) {
			List<CEntry> blockEntries = new ArrayList<>();
			Stream.of(childElementsOf(elt)).forEach(child -> recursiveParsingOfConstraints(child, blockEntries));
			XBlock ctrBlock = new XBlock(blockEntries);
			ctrBlock.copyAttributesOf(elt);
			if (!TypeClass.intersect(ctrBlock.classes, discardedClasses))
				list.add(ctrBlock);
		} else {
			CEntry entry = parseCEntryOuter(elt, null);
			if (!TypeClass.intersect(entry.classes, discardedClasses))
				list.add(entry);
		}
	}

	/** Parses the element <constraints> of the document. */
	private void parseConstraints() {
		NodeList nl = document.getElementsByTagName(CONSTRAINTS);
		if (nl.getLength() > 0)
			Stream.of(childElementsOf((Element) nl.item(0))).forEach(elt -> recursiveParsingOfConstraints(elt, cEntries));
		// updateVarDegreesWith(cEntries);
	}

	/** Parses the element <objectives> (if it exists) of the document. */
	private void parseObjectives() {
		NodeList nl = document.getDocumentElement().getElementsByTagName(OBJECTIVES);
		if (nl.getLength() == 1) {
			Element objectives = (Element) nl.item(0);
			typeCombination = giveAttributeValue(objectives, TypeAtt.combination.name(), TypeCombination.class, TypeCombination.PARETO);
			for (Element elt : childElementsOf(objectives)) {
				OEntry entry = null;
				boolean minimize = elt.getTagName().equals(MINIMIZE);
				TypeObjective type = giveAttributeValue(elt, TypeAtt.type.name(), TypeObjective.class, TypeObjective.EXPRESSION);
				if (type == TypeObjective.EXPRESSION) {
					entry = new OObjectiveExpr(minimize, type, parseExpression(elt.getTextContent().trim()));
				} else {
					Element[] sons = childElementsOf(elt);
					Object[] terms = parseSequence(sons.length == 0 ? elt : sons[0]);
					Object[] coeffs = sons.length != 2 ? null : parseSequence(sons[1]);
					// SimpleValue[] coeffs = sons.length != 2 ? null :
					// SimpleValue.parseSeq(sons[1].getTextContent().trim());
					entry = new OObjectiveSpecial(minimize, type, terms, coeffs);
				}
				entry.copyAttributesOf(elt);
				if (!TypeClass.intersect(entry.classes, discardedClasses))
					oEntries.add(entry);
			}
		}
	}

	/** Parses the element <annotations> (if it exists) of the document. */
	private void parseAnnotations() {
		NodeList nl = document.getDocumentElement().getElementsByTagName(ANNOTATIONS);
		if (nl.getLength() == 1) {
			Element annotations = (Element) nl.item(0);
			for (Element elt : childElementsOf(annotations)) {
				if (elt.getTagName().equals(DECISION)) {
					XVar[] vars = (XVar[]) parseSequence(elt);
					aEntries.add(new AEntry(DECISION, vars));
				} else if (elt.getTagName().equals(VAL_HEURISTIC)) {
					// for the moment, only static ordering
					List<Object> statics = new ArrayList<>();
					for (Element son : childElementsOf(elt)) {
						if (son.getTagName() == STATIC) {
							XVarInteger[] list = (XVarInteger[]) (XVarInteger[]) parseSequence(son);
							int[] order = CtrLoaderInteger.trIntegers(parseSequence(son.getAttribute(TypeAtt.order.name()), "\\s+"));
							statics.add(new Object[] { list, order });
						}
					}
					aEntries.add(new AEntry(VAL_HEURISTIC, new Object[] { STATIC, statics }));
				}
			}
		}
	}

	/**
	 * Updates the degree of each variable occurring somewhere in the specified list.
	 */
	private void updateVarDegreesWith(List<CEntry> list) {
		for (CEntry entry : list)
			if (entry instanceof XBlock)
				updateVarDegreesWith(((XBlock) entry).subentries);
			else if (entry instanceof XGroup) {
				XGroup group = (XGroup) entry;
				for (int i = 0; i < group.argss.length; i++)
					for (XVar var : group.getScope(i))
						var.degree++;
			} else
				for (XVar var : entry.vars())
					var.degree++;
	}

	/**
	 * Computes the degree of each variable. Important for being aware of the useless variables (variables of degree 0).
	 */
	private void computeVarDegrees() {
		updateVarDegreesWith(cEntries);
		for (OEntry entry : oEntries) {
			if (entry instanceof OObjectiveExpr)
				for (XVar x : ((OObjectiveExpr) entry).rootNode.listOfVars())
					x.degree++;
			else
				for (XVar x : ((OObjectiveSpecial) entry).vars())
					x.degree++;
		}
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of TypeClass denotes the classes that must
	 * be discarded (e.g., symmetryBreaking).
	 */
	public XParser(Document document, TypeClass[] discardedClasses) throws Exception {
		this.document = document;
		this.discardedClasses = discardedClasses;
		typeFramework = giveAttributeValue(document.getDocumentElement(), TypeAtt.type.name(), TypeFramework.class, TypeFramework.CSP);

		parseVariables();
		parseConstraints();
		parseObjectives();
		parseAnnotations();
		computeVarDegrees();
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of strings denotes the classes that must be
	 * discarded (e.g., symmetryBreaking).
	 */
	public XParser(Document document, String... discardedClasses) throws Exception {
		this(document, TypeClass.classesFor(discardedClasses));
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of TypeClass denotes the classes that
	 * must be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inpuStream, TypeClass[] discardedClasses) throws Exception {
		this(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inpuStream), discardedClasses);
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of strings denotes the classes that must
	 * be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inputStream, String... discardedClasses) throws Exception {
		this(inputStream, TypeClass.classesFor(discardedClasses));
	}

}
