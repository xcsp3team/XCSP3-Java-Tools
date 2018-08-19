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
import static org.xcsp.common.Constants.VAR;
import static org.xcsp.common.Constants.VARIABLES;
import static org.xcsp.common.Utilities.childElementsOf;
import static org.xcsp.common.Utilities.control;
import static org.xcsp.common.Utilities.isTag;
import static org.xcsp.common.Utilities.safeLong;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.xcsp.common.domains.Values.Rational;
import org.xcsp.common.domains.Values.SimpleValue;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;
import org.xcsp.common.predicates.XNodeParent;
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

/**
 * This class corresponds to a Java parser that uses DOM (Document Object Model) to parse XCSP3 instances. <br>
 * Here, we assume that the instance is well-formed (valid). This class is given for illustration purpose. Feel free to adapt it !
 * 
 * @author Christophe Lecoutre, CRIL-CNRS - lecoutre@cril.fr
 * @version 1.3
 */
public class XParser {

	/** The document to be parsed. */
	private Document document; //

	/** An XPath object that is useful for some tasks (queries). */
	private XPath xpath = XPathFactory.newInstance().newXPath();

	/** The map that stores pairs (id,variable). */
	public Map<String, XVar> mapForVars = new HashMap<>();

	/** The map that stores pairs (id,array). */
	private Map<String, XArray> mapForArrays = new HashMap<>();

	/** A map used as a cache for avoiding building several times the same domain objects; it stores pairs (textualContent,domain). */
	private Map<String, IDom> cacheForContentToDomain = new HashMap<>();

	/** The list of entries of the element <variables>. It contains variables and arrays. */
	public List<VEntry> vEntries = new ArrayList<>();

	/**
	 * The list of entries of the element <constraints>. It contains stand-alone constraints (extension, intension, allDifferent, ...), groups of
	 * constraints, and meta-constraints (sliding and logical constructions).
	 */
	public List<CEntry> cEntries = new ArrayList<>();

	/** The list of objectives of the element <objectives>. Typically, it contains 0 or 1 objective. */
	public List<OEntry> oEntries = new ArrayList<>();

	public Map<String, Object> aEntries = new HashMap<>();

	/** The type of the framework used for the loaded instance. */
	public TypeFramework typeFramework;

	/** In case of multi-objective optimization, indicates the type that must be considered. */
	public TypeCombination typeCombination;

	/** The classes that must be discarded. Used just before posting variables, constraints and objectives. **/
	public TypeClass[] discardedClasses;

	/**********************************************************************************************
	 * Parsing of Variables (and Domains)
	 *********************************************************************************************/

	/** Returns the value of the specified attribute for the specified element, if it exists, the specified default value otherwise. */
	private <T extends Enum<T>> T giveAttributeValue(Element elt, String attName, Class<T> clazz, T defaultValue) {
		String s = elt.getAttribute(attName);
		return s.length() == 0 ? defaultValue : Types.valueOf(clazz, s.replaceFirst("\\s+", "_"));
	}

	/** Parses a basic domain, i.e., a domain for an integer, symbolic, float or stochastic variable (or array). */
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

	/** Gives the 'size' (an array of integers as defined in XCSP3) of the array of variables. */
	private int[] giveArraySize(Element elt) {
		StringTokenizer st = new StringTokenizer(elt.getAttribute(TypeAtt.size.name()), "[]");
		return IntStream.range(0, st.countTokens()).map(i -> Integer.parseInt(st.nextToken())).toArray();
	}

	/** Allows us to manage aliases, i.e., indirection due to the use of the 'as' attribute. */
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
		Map<String, IDom> cacheForId2Domain = new HashMap<>(); // a map for managing pairs (id,domain); remember that aliases can be encountered
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
	 * Parse the specified token, as a variable, an interval, a rational, a decimal, a long, a set (literal), a parameter, or a functional expression.
	 * If nothing above matches, the token is returned (and considered as a symbolic value).
	 */
	private Object parseData(String tok) {
		if (mapForVars.get(tok) != null)
			return mapForVars.get(tok);
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
		String left = tok.substring(tok.charAt(0) != '(' ? 0 : 1, pos);
		String right = tok.substring(pos + 1, tok.length() - (tok.charAt(tok.length() - 1) == ')' ? 1 : 0));
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
	 * Parse a sequence of tokens (separated by the specified delimiter). Each token can represent a compact list of array variables, or a basic
	 * entity.
	 */
	public Object[] parseSequence(String seq, String delimiter) {
		List<Object> list = new ArrayList<>();
		for (String tok : seq.split(delimiter)) {
			int pos = tok.indexOf("[");
			XArray array = pos == -1 ? null : mapForArrays.get(tok.substring(0, pos));
			try {
				if (array != null)
					list.addAll(array.getVarsFor(tok));
				else
					list.add(parseData(tok));
			} catch (WrongTypeException e) {
				throw new WrongTypeException("in sequence \"" + seq + "\": " + e.getMessage());
			}
		}
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
			for (int j = 0; j < length2; j++) {
				indexes[second] = j + (int) indexRanges[second].smallest();
				list.add(array.varAt(indexes));
			}
			list2D.add(Utilities.specificArrayFrom(list));
		}
		return Utilities.specificArray2DFrom(list2D);
	}

	/** The enum type describing the different types of primitives that can be used for representing arrays of integer tuples. */
	public static enum TypePrimitive {
		BYTE, SHORT, INT, LONG;

		/** Returns the smallest primitive that can be used for representing values lying within the specified bounds. */
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

		/** Returns the smallest primitive that can be used for representing the specified value. */
		public static TypePrimitive whichPrimitiveFor(long val) {
			return whichPrimitiveFor(val, val);
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is
		 * not integer, null is returned.
		 */
		static TypePrimitive whichPrimitiveFor(XVar[] vars) {
			if (Stream.of(vars).anyMatch(x -> x.type != TypeVar.integer))
				return null;
			return TypePrimitive.values()[Stream.of(vars).mapToInt(x -> ((XVarInteger) x).whichPrimitive().ordinal()).max()
					.orElse(TypePrimitive.LONG.ordinal())];
		}

		/**
		 * Returns the smallest primitive that can be used for representing any value of the domains of the specified variables. If one variable is
		 * not integer, null is returned.
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
		 * Parse the specified string that denotes a sequence of values. In case we have at least one interval, we just return an array of
		 * IntegerEntity (as for integer domains), and no validity test on values is performed. Otherwise, we return an array of integer (either
		 * long[] or int[]). It is possible that some values are discarded because either they do not belong to the specified domain (test performed
		 * if this domain is not null), or they cannot be represented by the primitive.
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
		 * Parse the specified string, and builds a tuple of (long) integers put in the specified array t. If the tuple is not valid wrt the specified
		 * domains or the primitive, false is returned, in which case, the tuple can be discarded. If * is encountered, the specified modifiable
		 * boolean is set to true.
		 */
		boolean parseTuple(String s, long[] t, DomBasic[] doms, AtomicBoolean ab) {
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
	 * Parse the tuples contained in the specified element. A 2-dimensional array of String, byte, short, int or long is returned, depending of the
	 * specified primitive (primitive set to null stands for String). The specified array of domains, if not null, can be used to filter out some
	 * tuples.
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
		if (primitive == null) { // in that case, we keep String (although integers can also be present at some places with hybrid
									// constraints)
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
			if (primitive.parseTuple(tok, tmp, doms, ab)) // if not filtered-out parsed tuple
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

	/** Returns the sequence of basic domains for the variables in the specified array. */
	private DomBasic[] domainsFor(XVar[] vars) {
		return Stream.of(vars).map(x -> ((DomBasic) x.dom)).toArray(DomBasic[]::new);
	}

	/**
	 * Returns the sequence of basic domains for the variables in the first row of the specified two-dimensional array, provided that variables of the
	 * other rows have similar domains. Returns null otherwise.
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
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		XVar[] vars = leafs.get(0).value instanceof XVar[] ? (XVar[]) leafs.get(0).value : null; // may be null if a constraint template
		TypePrimitive primitive = args != null ? TypePrimitive.whichPrimitiveFor((XVar[][]) args) : vars != null ? TypePrimitive.whichPrimitiveFor(vars) : null;
		DomBasic[] doms = args != null ? domainsFor((XVar[][]) args) : vars != null ? domainsFor(vars) : null;
		AtomicBoolean ab = new AtomicBoolean();
		// We use doms to possibly filter out some tuples, and primitive to build an array of values of this primitive (short, byte, int or long)
		leafs.add(new CChild(isTag(sons[1], TypeChild.supports) ? TypeChild.supports : TypeChild.conflicts, parseTuples(sons[1], primitive, doms, ab)));
		if (doms == null || leafs.get(1).value instanceof IntegerEntity[])
			leafs.get(1).flags.add(TypeFlag.UNCLEAN_TUPLES); // we inform solvers that some tuples can be invalid (wrt the domains of variables)
		if (ab.get())
			leafs.get(1).flags.add(TypeFlag.STARRED_TUPLES); // we inform solvers that the table (list of tuples) contains the special value *
	}

	/** Parses a functional expression, as used for example in elements <intension>. */
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
				return new XNodeLeaf<XVar>(TypeExpr.PAR, l); // for simplicity, we only record Long, although we know here that we
																// necessarily have an int
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
				return new XNodeLeaf<XVar>(TypeExpr.SET, null);
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
		leafs.add(new CChild(TypeChild.function, parseExpression((sons.length == 0 ? elt : sons[0]).getTextContent().trim())));
	}

	/** Parses a constraint <smart>. Will be included in specifications later. */
	private void parseSmart(Element elt, Element[] sons) {
		for (Element son : sons)
			leafs.add(new CChild(TypeChild.list, parseSequence(son)));
	}

	/**********************************************************************************************
	 * Language-based Constraints
	 *********************************************************************************************/

	private void parseRegular(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		Object[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ? safeLong(tr[1]) : tr[1];
			return new Object[] { tr[0], value, tr[2] };
		}).toArray(Object[][]::new);
		leafs.add(new CChild(TypeChild.transitions, trans));
		leafs.add(new CChild(TypeChild.start, sons[2].getTextContent().trim()));
		leafs.add(new CChild(TypeChild.FINAL, sons[3].getTextContent().trim().split("\\s+")));
	}

	private void parseGrammar(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.terminal, sons[1].getTextContent().trim().split("\\s+")));
		String[][][] rules = Stream.of(sons[2].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] sp = t.split("\\s*,\\s*");
			String[] leftWord = sp[0].split("\\s+"), rightWord = sp.length == 1 ? new String[] { "" } : sp[1].split("\\s+");
			return new String[][] { leftWord, rightWord };
		}).toArray(String[][][]::new);
		leafs.add(new CChild(TypeChild.rules, rules));
		leafs.add(new CChild(TypeChild.start, sons[3].getTextContent().trim()));
	}

	private void parseMDD(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		Object[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t -> {
			String[] tr = t.split("\\s*,\\s*");
			Object value = Character.isDigit(tr[1].charAt(0)) || tr[1].charAt(0) == '+' || tr[1].charAt(0) == '-' ? safeLong(tr[1]) : tr[1];
			return new Object[] { tr[0], value, tr[2] };
		}).toArray(Object[][]::new);
		// String[][] trans = Stream.of(sons[1].getTextContent().trim().split(DELIMITER_LISTS)).skip(1).map(t ->
		// t.split("\\s*,\\s*")).toArray(String[][]::new);
		leafs.add(new CChild(TypeChild.transitions, trans));
	}

	/**********************************************************************************************
	 * Comparison-based Constraints
	 *********************************************************************************************/

	private void parseAllDifferent(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			if (type == TypeChild.matrix)
				leafs.add(new CChild(type, parseDoubleSequenceOfVars(sons[0])));
			else {
				Element except = isTag(sons[lastSon], TypeChild.except) ? sons[lastSon] : null;
				for (int i = 0, limit = lastSon - (except != null ? 1 : 0); i <= limit; i++)
					leafs.add(new CChild(type, parseSequence(sons[i])));
				if (except != null) {
					if (lastSon == 1)
						leafs.add(new CChild(TypeChild.except,
								leafs.get(0).setVariableInvolved() ? parseDoubleSequence(except, DELIMITER_SETS) : parseSequence(except)));
					else
						leafs.add(new CChild(TypeChild.except, parseDoubleSequence(except,
								type == TypeChild.list ? DELIMITER_LISTS : type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
				}
			}
		}
	}

	private void parseAllEqual(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			TypeChild type = TypeChild.valueOf(sons[0].getTagName());
			for (int i = 0; i <= lastSon; i++)
				leafs.add(new CChild(type, parseSequence(sons[i])));
		}
	}

	private void parseAllDistant(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i < lastSon; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseOrdered(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		if (type == TypeChild.matrix)
			leafs.add(new CChild(type, parseDoubleSequenceOfVars(sons[0])));
		else
			for (int i = 0; i < lastSon; i++)
				leafs.add(new CChild(TypeChild.valueOf(sons[i].getTagName()), parseSequence(sons[i])));
		leafs.add(new CChild(TypeChild.operator, TypeOperator.valOf(sons[lastSon].getTextContent())));
	}

	private void parseLex(Element elt, Element[] sons, int lastSon) {
		parseOrdered(elt, sons, lastSon);
	}

	private void parseAllIncomparable(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		for (int i = 0; i <= lastSon; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
	}

	/**********************************************************************************************
	 * Counting and Summing Constraints
	 *********************************************************************************************/

	private void parseSum(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.list))
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		else
			leafs.add(new CChild(TypeChild.index, parseData(sons[0])));
		if (isTag(sons[1], TypeChild.coeffs)) // if (lastSon == 2)
			leafs.add(new CChild(TypeChild.coeffs, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCount(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseNValues(Element elt, Element[] sons, int lastSon) {
		TypeChild type = TypeChild.valueOf(sons[0].getTagName());
		Element except = isTag(sons[lastSon - 1], TypeChild.except) ? sons[lastSon - 1] : null;
		for (int i = 0, limit = lastSon - (except != null ? 2 : 1); i <= limit; i++)
			leafs.add(new CChild(type, parseSequence(sons[i])));
		if (except != null)
			leafs.add(new CChild(TypeChild.except, lastSon == 2 ? parseSequence(except)
					: parseDoubleSequence(except, type == TypeChild.list ? DELIMITER_LISTS : type == TypeChild.set ? DELIMITER_SETS : DELIMITER_MSETS)));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseCardinality(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new CChild(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new CChild(TypeChild.rowOccurs, parseDoubleSequenceOfVars(sons[2])));
			leafs.add(new CChild(TypeChild.colOccurs, parseDoubleSequenceOfVars(sons[3])));
		} else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
			leafs.add(new CChild(TypeChild.occurs, parseSequence(sons[2])));
		}
	}

	private void parseBalance(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.values))
			leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseSpread(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.total))
			leafs.add(new CChild(TypeChild.total, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseDeviation(Element elt, Element[] sons, int lastSon) {
		parseSpread(elt, sons, lastSon);
	}

	/**********************************************************************************************
	 * Connection Constraints
	 *********************************************************************************************/

	private void parseMaximum(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		if (isTag(sons[1], TypeChild.index))
			leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		if (isTag(sons[lastSon], TypeChild.condition))
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[lastSon])));
	}

	private void parseMinimum(Element elt, Element[] sons, int lastSon) {
		parseMaximum(elt, sons, lastSon);
	}

	private void parseElement(Element elt, Element[] sons, int lastSon) {
		if (isTag(sons[0], TypeChild.matrix)) {
			leafs.add(new CChild(TypeChild.matrix, parseDoubleSequenceOfVars(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new CChild(TypeChild.index, parseSequence(sons[1])));
		} else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			if (isTag(sons[1], TypeChild.index))
				leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		}
		leafs.add(new CChild(TypeChild.value, parseData(sons[lastSon])));
	}

	private void parseChannel(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			if (lastSon == 1) {
				if (isTag(sons[1], TypeChild.list))
					leafs.add(new CChild(TypeChild.list, parseSequence(sons[1])));
				else
					leafs.add(new CChild(TypeChild.value, parseData(sons[1])));
			}
		}
	}

	private void parsePermutation(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.mapping, parseSequence(sons[2])));
	}

	private void parsePrecedence(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.operator, TypeOperator.valOf(sons[lastSon].getTextContent())));
	}

	/**********************************************************************************************
	 * Packing and Scheduling Constraints
	 *********************************************************************************************/

	private void parseStretch(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.widths, parseSequence(sons[2])));
		if (lastSon == 3)
			leafs.add(new CChild(TypeChild.patterns, parseDoubleSequence(sons[3], DELIMITER_LISTS)));
	}

	private void parseNoOverlap(Element elt, Element[] sons) {
		boolean multiDimensional = sons[1].getTextContent().trim().charAt(0) == '(';
		leafs.add(new CChild(TypeChild.origins, multiDimensional ? parseDoubleSequenceOfVars(sons[0]) : parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.lengths, multiDimensional ? parseDoubleSequence(sons[1], DELIMITER_LISTS) : parseSequence(sons[1])));
	}

	private void parseCumulative(Element elt, Element[] sons) {
		int cnt = 0;
		leafs.add(new CChild(TypeChild.origins, parseSequence(sons[cnt++])));
		leafs.add(new CChild(TypeChild.lengths, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.ends))
			leafs.add(new CChild(TypeChild.ends, parseSequence(sons[cnt++])));
		leafs.add(new CChild(TypeChild.heights, parseSequence(sons[cnt++])));
		if (isTag(sons[cnt], TypeChild.machines)) {
			leafs.add(new CChild(TypeChild.machines, parseSequence(sons[cnt++])));
			leafs.add(new CChild(TypeChild.conditions, parseConditions(sons[cnt++])));
		} else
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[cnt++])));
	}

	private void parseBinPacking(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.sizes, parseSequence(sons[1])));
		if (isTag(sons[2], TypeChild.condition))
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[2])));
		else
			leafs.add(new CChild(TypeChild.conditions, parseConditions(sons[2])));
	}

	private void parseKnapsack(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.weights, parseSequence(sons[1])));
		leafs.add(new CChild(TypeChild.profits, parseSequence(sons[2])));
		leafs.add(new CChild(TypeChild.limit, parseData(sons[3])));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[4])));
	}

	/**********************************************************************************************
	 * Graph Constraints
	 *********************************************************************************************/

	private CChild listOrGraph(Element elt) {
		return isTag(elt, TypeChild.list) ? new CChild(TypeChild.list, parseSequence(elt)) : new CChild(TypeChild.graph, parseData(elt));
	}

	private void parseCircuit(Element elt, Element[] sons, int lastSon) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt)));
		else {
			leafs.add(listOrGraph(sons[0]));
			if (lastSon == 1)
				leafs.add(new CChild(TypeChild.size, parseData(sons[1])));
		}
	}

	private void parseNCircuits(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.condition, parseCondition(sons[1])));
	}

	private void parsePath(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.start, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.FINAL, parseData(sons[2])));
		if (lastSon == 3)
			leafs.add(new CChild(TypeChild.size, parseData(sons[3])));
	}

	private void parseNPaths(Element elt, Element[] sons, int lastSon) {
		parseNCircuits(elt, sons, lastSon);
	}

	private void parseTree(Element elt, Element[] sons, int lastSon) {
		leafs.add(listOrGraph(sons[0]));
		leafs.add(new CChild(TypeChild.root, parseData(sons[1])));
		if (lastSon == 2)
			leafs.add(new CChild(TypeChild.size, parseData(sons[2])));
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
		leafs.add(new CChild(TypeChild.list, parseSequence((sons.length == 0 ? elt : sons[0]))));
	}

	private void parseInstantiation(Element elt, Element[] sons, int lastSon) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.values, parseSequence(sons[1])));
	}

	/**********************************************************************************************
	 * Set Constraints
	 *********************************************************************************************/

	private void parseAllIntersecting(Element elt, Element[] sons) {
		if (sons.length == 0)
			leafs.add(new CChild(TypeChild.list, parseSequence(elt))); // necessary, case disjoint or overlapping
		else {
			leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
			leafs.add(new CChild(TypeChild.condition, parseCondition(sons[1])));
		}
	}

	private void parseRange(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.index, parseData(sons[1])));
		leafs.add(new CChild(TypeChild.image, parseData(sons[2])));
	}

	private void parseRoots(Element elt, Element[] sons) {
		parseRange(elt, sons);
	}

	private void parsePartition(Element elt, Element[] sons) {
		leafs.add(new CChild(TypeChild.list, parseSequence(sons[0])));
		leafs.add(new CChild(TypeChild.value, parseData(sons[1])));
	}

	/**********************************************************************************************
	 ***** Main methods for constraints
	 *********************************************************************************************/

	private List<CChild> leafs; // is you want to avoid this field, just pass it through as argument of every method called in the long
								// sequence of 'if' below

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
			Object[][] groupArgs = l.stream().noneMatch(o -> !(o instanceof XVar[])) ? l.toArray(new XVar[0][]) : l.toArray(new Object[0][]);
			return new XGroup((CEntryReifiable) parseCEntryOuter(sons[0], groupArgs), groupArgs);
		}
		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.slide) {
			CChild[] lists = IntStream.range(0, lastSon).mapToObj(i -> new CChild(TypeChild.list, parseSequence(sons[i]))).toArray(CChild[]::new);
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
		return new XCtr(type, leafs.toArray(new CChild[leafs.size()]));
	}

	// condition at null means a cost function (aka weighted constraint), otherwise a cost-integrated soft constraint
	private Softening buildSoftening(Element elt, Map<TypeAtt, String> attributes, Condition cost) {
		if (attributes.containsKey(TypeAtt.violationCost)) {
			int violationCost = Utilities.safeLong2Int(safeLong(attributes.get(TypeAtt.violationCost)), true);
			return cost == null ? new SofteningSimple(violationCost) : new SofteningSimple(cost, violationCost);
		}
		TypeCtr type = TypeCtr.valueOf(elt.getTagName());
		if (type == TypeCtr.intension)
			return cost == null ? new SofteningIntension() : new SofteningIntension(cost);
		if (type == TypeCtr.extension) {
			int defaultCost = attributes.containsKey(TypeAtt.defaultCost) ? Utilities.safeLong2Int(safeLong(attributes.get(TypeAtt.defaultCost)), true) : -1;
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
		int lastSon = sons.length - 1 - (sons.length > 1 && isTag(sons[sons.length - 1], TypeChild.cost) ? 1 : 0); // last son position,
																													// excluding <cost> that
																													// is managed apart
		CEntry entry = parseCEntry(elt, args, sons, lastSon);
		entry.copyAttributesOf(elt); // we copy the attributes
		if (entry instanceof XCtr)
			for (int i = 0; i <= lastSon; i++)
				((XCtr) entry).childs[i].copyAttributesOf(sons[i]); // we copy the attributes for each parameter of the constraint
		else if (entry instanceof XSlide)
			for (int i = 0; i < lastSon; i++)
				((XSlide) entry).lists[i].copyAttributesOf(sons[i]); // we copy the attributes for the list(s) involved in slide
		// Note that for seqbin and logic entries, no need to copy any attributes at this place

		if (entry instanceof CEntryReifiable) {
			CEntryReifiable entryReifiable = (CEntryReifiable) entry;
			Map<TypeAtt, String> attributes = entryReifiable.attributes;
			if (soft) { // dealing with softening
				Condition cost = lastSon == sons.length - 1 ? null : parseCondition(sons[sons.length - 1]);
				// condition at null means a cost function (aka weighted constraint), otherwise a cost-integrated soft constraint
				entryReifiable.softening = buildSoftening(elt, attributes, cost);
				// } else {
				// assert lastSon == sons.length - 2; // cost-integrated soft constraint
				//
				// Integer defaultCost = attributes.containsKey(TypeAtt.defaultCost) ? Integer.parseInt(attributes.get(TypeAtt.defaultCost))
				// : null;
				// NamedNodeMap al = sons[sons.length - 1].getAttributes();
				// TypeMeasure type = al.getNamedItem(TypeAtt.violationMeasure.name()) == null ? null : XEnums.valueOf(TypeMeasure.class,
				// al.getNamedItem(TypeAtt.violationMeasure.name()).getNodeValue());
				// String parameters = al.getNamedItem(TypeAtt.violationParameters.name()) == null ? null : al
				// .getNamedItem(TypeAtt.violationParameters.name()).getNodeValue();
				// Condition condition = parseCondition(sons[sons.length - 1]);
				// entryReifiable.softening = new XSoftening(type, parameters, condition, defaultCost);
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
		Stream.of(childElementsOf((Element) document.getElementsByTagName(CONSTRAINTS).item(0))).forEach(elt -> recursiveParsingOfConstraints(elt, cEntries));
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
					XVar[] vars = (XVar[]) parseSequence(sons.length == 0 ? elt : sons[0]);
					SimpleValue[] coeffs = sons.length != 2 ? null : SimpleValue.parseSeq(sons[1].getTextContent().trim());
					entry = new OObjectiveSpecial(minimize, type, vars, coeffs);
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
					aEntries.put(DECISION, vars);
				}
			}
		}
	}

	/** Updates the degree of each variable occurring somewhere in the specified list. */
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

	/** Computes the degree of each variable. Important for being aware of the useless variables (variables of degree 0). */
	private void computeVarDegrees() {
		updateVarDegreesWith(cEntries);
		for (OEntry entry : oEntries) {
			if (entry instanceof OObjectiveExpr)
				for (XVar x : ((OObjectiveExpr) entry).rootNode.listOfVars())
					x.degree++;
			else
				for (XVar x : ((OObjectiveSpecial) entry).vars)
					x.degree++;
		}
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of TypeClass denotes the classes
	 * that must be discarded (e.g., symmetryBreaking).
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
	 * Loads and parses the XCSP3 file corresponding to the specified document. The specified array (possibly empty) of strings denotes the classes
	 * that must be discarded (e.g., symmetryBreaking).
	 */
	public XParser(Document document, String... discardedClasses) throws Exception {
		this(document, TypeClass.classesFor(discardedClasses));
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of TypeClass denotes the
	 * classes that must be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inpuStream, TypeClass[] discardedClasses) throws Exception {
		this(DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inpuStream), discardedClasses);
	}

	/**
	 * Loads and parses the XCSP3 file corresponding to the specified inputStream. The specified array (possibly empty) of strings denotes the classes
	 * that must be discarded (e.g., symmetryBreaking).
	 */
	public XParser(InputStream inputStream, String... discardedClasses) throws Exception {
		this(inputStream, TypeClass.classesFor(discardedClasses));
	}

}
