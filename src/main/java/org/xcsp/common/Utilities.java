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
package org.xcsp.common;

import static org.xcsp.common.Constants.BIG_MAX_SAFE_LONG;
import static org.xcsp.common.Constants.BIG_MIN_SAFE_LONG;
import static org.xcsp.common.Constants.MINUS_INFINITY;
import static org.xcsp.common.Constants.PLUS_INFINITY;
import static org.xcsp.common.Constants.VAL_MINUS_INFINITY;
import static org.xcsp.common.Constants.VAL_PLUS_INFINITY;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.predicates.XNode;
import org.xcsp.parser.entries.XVariables.XVar;

/**
 * A class with some utility (static) methods.
 * 
 * @author Christophe Lecoutre
 */
public class Utilities {

	/** Builds a one-dimensional array of T with the objects of the specified list. If the list is empty, null is returned. */
	public static <T> T[] convert(Collection<T> list) {
		if (list.size() == 0)
			return null;
		T[] ts = (T[]) Array.newInstance(list.iterator().next().getClass(), list.size());
		int i = 0;
		for (T x : list)
			ts[i++] = x;
		return ts;
	}

	private static <T> List<T> collectRec(Class<T> clazz, List<T> list, Object src) {
		if (src != null)
			if (src.getClass().isArray())
				IntStream.range(0, Array.getLength(src)).forEach(i -> collectRec(clazz, list, Array.get(src, i)));
			else if (clazz.isAssignableFrom(src.getClass()))
				list.add(clazz.cast(src));
		return list;
	}

	public static <T> T[] collect(Class<T> clazz, Object... src) {
		List<T> list = new ArrayList<>();
		Stream.of(src).forEach(o -> collectRec(clazz, list, o));
		return convert(list.stream().collect(Collectors.toList()));
	}

	public static <T> T[] collectDistinct(Class<T> clazz, Object... src) {
		List<T> list = new ArrayList<>();
		Stream.of(src).forEach(o -> collectRec(clazz, list, o));
		return (T[]) convert(list.stream().distinct().collect(Collectors.toList()));
	}

	/**
	 * Builds a 1-dimensional array of int from the specified sequence of parameters. Each element of the sequence must be either an int, an
	 * Integer, a Range or a 1-dimensional array of int (int[]). All integers are collected and concatenated to form a 1-dimensional array.
	 */
	public static int[] collectVals(Object... valsToConcat) {
		assert valsToConcat.length > 0 && Stream.of(valsToConcat).allMatch(o -> o instanceof Integer || o instanceof int[] || o instanceof Range);
		return Stream.of(valsToConcat).map(o -> o instanceof Integer ? new int[] { (Integer) o } : o instanceof Range ? ((Range) o).toArray() : (int[]) o)
				.flatMapToInt(t -> Arrays.stream(t)).toArray();
	}

	public static boolean isNumeric(String token) {
		return token.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

	public static boolean isNumericInterval(String token) {
		return token.matches("-?\\d+\\.\\.-?\\d+");
	}

	public static Boolean toBoolean(String s) {
		s = s.toLowerCase();
		if (s.equals("yes") || s.equals("y") || s.equals("true") || s.equals("t") || s.equals("1"))
			return Boolean.TRUE;
		if (s.equals("no") || s.equals("n") || s.equals("false") || s.equals("f") || s.equals("0"))
			return Boolean.FALSE;
		return null;
	}

	public static Integer toInteger(String token, Predicate<Integer> p) {
		try {
			Integer i = Integer.parseInt(token);
			Utilities.control(p == null || p.test(i), "Value " + i + " not accepted by " + p);
			return i;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static Integer toInteger(String token) {
		return toInteger(token, null);
	}

	public static Double toDouble(String token, Predicate<Double> p) {
		try {
			Double d = Double.parseDouble(token);
			Utilities.control(p == null || p.test(d), "Value " + d + " not accepted by " + p);
			return d;
		} catch (RuntimeException e) {
			return null;
		}
	}

	public static double toDouble(String token) {
		return toDouble(token, null);
	}

	public static Object[] specificArrayFrom(List<Object> list) {
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray() : list.toArray((Object[]) Array.newInstance(clazz, list.size()));
	}

	public static Object[][] specificArray2DFrom(List<Object[]> list) {
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray(new Object[0][]) : list.toArray((Object[][]) Array.newInstance(clazz, list.size()));
	}

	public static boolean contains(int[] tab, int v) {
		return contains(tab, v, 0, tab.length - 1);
	}

	public static boolean contains(int[] tab, int v, int from, int to) {
		return IntStream.range(from, to + 1).anyMatch(i -> tab[i] == v);
	}

	/**
	 * Method that controls that the specified condition is verified. If it is not the case, a message is displayed and the program is
	 * stopped.
	 */
	public static Object control(boolean condition, String message) {
		if (!condition) {
			// throw new RuntimeException();
			System.out.println("Fatal Error: " + message);
			System.exit(1);
		}
		return null;
	}

	public static Object exit(String message) {
		return control(false, message);
	}

	/**
	 * Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised. The
	 * specified boolean allows us to indicate if some special values (such as +infinity) must be checked.
	 */
	public static Long safeLong(String s, boolean checkSpecialValues) {
		if (checkSpecialValues) {
			if (s.equals(PLUS_INFINITY))
				return VAL_PLUS_INFINITY;
			if (s.equals(MINUS_INFINITY))
				return VAL_MINUS_INFINITY;
		}
		if (s.length() > 18) { // 18 because MAX_LONG and MIN_LONG are composed of at most 19 characters
			BigInteger big = new BigInteger(s);
			control(big.compareTo(BIG_MIN_SAFE_LONG) >= 0 && big.compareTo(BIG_MAX_SAFE_LONG) <= 0, "Too small or big value for this parser : " + s);
			return big.longValue();
		} else
			return Long.parseLong(s);
	}

	/** Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised. */
	public static Long safeLong(String s) {
		return safeLong(s, false);
	}

	public static boolean isSafeInt(long l, boolean useMargin) {
		return (useMargin ? Constants.MIN_SAFE_INT : Integer.MIN_VALUE) <= l && l <= (useMargin ? Constants.MAX_SAFE_INT : Integer.MAX_VALUE);
	}

	public static boolean isSafeInt(long l) {
		return isSafeInt(l, true);
	}

	/**
	 * Converts the specified long to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe
	 * according to the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(long l, boolean useMargin) {
		control(isSafeInt(l, useMargin), "Too big integer value " + l);
		return (int) l;
	}

	/**
	 * Converts the specified number to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe
	 * according to the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(Number number, boolean useMargin) {
		return safeLong2Int(number.longValue(), useMargin);
	}

	/**
	 * Converts the specified long to int if it is safe to do it. Note that VAL_MINUS_INFINITY will be translated to VAL_MINUS_INFINITY_INT
	 * and that VAL_PLUS_INFINITY will be translated to VAL_PLUS_INFINITY_INT . When the specified boolean is set to true, we control that
	 * it is safe according to the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2IntWhileHandlingInfinity(long l, boolean useMargin) {
		return l == Constants.VAL_MINUS_INFINITY ? Constants.VAL_MINUS_INFINITY_INT
				: l == Constants.VAL_PLUS_INFINITY ? Constants.VAL_PLUS_INFINITY_INT : safeLong2Int(l, true);
	}

	public static <T> T[] sort(T[] t) {
		Arrays.sort(t);
		return t;
	}

	public static <T> T[] swap(T[] t, int i, int j) {
		T tmp = t[i];
		t[i] = t[j];
		t[j] = tmp;
		return t;
	}

	/** Method that joins the elements of the specified array, using the specified delimiter to separate them. */
	public static String join(Object array, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, length = Array.getLength(array); i < length; i++) {
			Object item = Array.get(array, i);
			if (item != null && item.getClass().isArray())
				sb.append("[").append(join(item, delimiter)).append("]");
			else
				sb.append(item != null ? item.toString() : "null").append(i < length - 1 ? delimiter : "");
		}
		return sb.toString();
	}

	/** Method that joins the elements of the specified array, using a white-space as delimiter. */
	public static String join(Object array) {
		return join(array, " ");
	}

	public static String join(Collection<? extends Object> c) {
		return join(c.toArray());
	}

	/** Method that joins the elements of the specified map, using the specified separator and delimiter. */
	public static <K, V> String join(Map<K, V> m, String separator, String delimiter) {
		return m.entrySet().stream().map(e -> e.getKey() + separator + e.getValue()).reduce("", (n, p) -> n + (n.length() == 0 ? "" : delimiter) + p);
	}

	/** Method that joins the elements of the specified two-dimensional array, using the specified separator and delimiter. */
	public static String join(Object[][] m, String separator, String delimiter) {
		return Arrays.stream(m).map(t -> join(t, delimiter)).reduce("", (n, p) -> n + (n.length() == 0 ? "" : separator) + p);
	}

	public static String join(int[][] m, String separator, String delimiter) {
		return Arrays.stream(m).map(t -> join(t, delimiter)).reduce("", (n, p) -> n + (n.length() == 0 ? "" : separator) + p);
	}

	/**
	 * Returns the specified string in camel case form (with the first letter of the first word in lower case).
	 */
	public static String toCamelCase(String s) {
		String[] words = s.split("_");
		return IntStream.range(0, words.length)
				.mapToObj(i -> i == 0 ? words[i].toLowerCase() : words[i].substring(0, 1).toUpperCase() + words[i].substring(1).toLowerCase())
				.collect(Collectors.joining());
	}

	/** Method for converting an array into a string. */
	public static String arrayToString(Object array, final char LEFT, final char RIGHT, final String SEP) {
		assert array.getClass().isArray();

		if (array instanceof boolean[])
			return Arrays.toString((boolean[]) array);
		if (array instanceof byte[])
			return Arrays.toString((byte[]) array);
		if (array instanceof short[])
			return Arrays.toString((short[]) array);
		if (array instanceof int[])
			return Arrays.toString((int[]) array);
		if (array instanceof long[])
			return Arrays.toString((long[]) array);
		if (array instanceof String[])
			return LEFT + String.join(SEP, (String[]) array) + RIGHT;
		if (array instanceof XVar[])
			return LEFT + String.join(SEP, Stream.of((XVar[]) array).map(x -> x.toString()).toArray(String[]::new)) + RIGHT;

		if (array instanceof boolean[][])
			return LEFT + String.join(SEP, Stream.of((boolean[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof byte[][])
			return LEFT + String.join(SEP, Stream.of((byte[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof short[][])
			return LEFT + String.join(SEP, Stream.of((short[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof int[][])
			return LEFT + String.join(SEP, Stream.of((int[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof long[][])
			return LEFT + String.join(SEP, Stream.of((long[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof String[][])
			return LEFT + String.join(SEP, Stream.of((String[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof boolean[][][])
			return LEFT + String.join(SEP, Stream.of((boolean[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof byte[][][])
			return LEFT + String.join(SEP, Stream.of((byte[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof short[][][])
			return LEFT + String.join(SEP, Stream.of((short[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof int[][][])
			return LEFT + String.join(SEP, Stream.of((int[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof long[][][])
			return LEFT + String.join(SEP, Stream.of((long[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;
		if (array instanceof String[][][])
			return LEFT + String.join(SEP, Stream.of((String[][][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof Long[][])
			return LEFT + String.join(SEP, Stream.of((Long[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof XVar[][])
			return LEFT + String.join(SEP, Stream.of((XVar[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

		if (array instanceof Object[][])
			return LEFT + String.join(SEP, Stream.of((Object[][]) array).map(t -> arrayToString(t)).toArray(String[]::new)) + RIGHT;
		// return "(" + String.join(")(", Stream.of((Object[][]) array).map(t -> simplify(Arrays.toString(t))).toArray(String[]::new)) +
		// ")";
		if (array instanceof Object[])
			return String.join(SEP,
					Stream.of((Object[]) array).map(t -> t.getClass().isArray() ? LEFT + arrayToString(t) + RIGHT : t.toString()).toArray(String[]::new));
		return null;
	}

	/** Method for converting an array into a string. */
	public static String arrayToString(Object array) {
		return arrayToString(array, '[', ']', ", ");
	}

	/**
	 * Returns true if inside the specified object, there is an element that checks the predicate. If syntactic trees are encountered, we
	 * check the leaves only.
	 */
	public static boolean check(Object obj, Predicate<Object> p) {
		if (obj instanceof Object[])
			return IntStream.range(0, Array.getLength(obj)).anyMatch(i -> check(Array.get(obj, i), p));
		if (obj instanceof XNode)
			return ((XNode<?>) obj).containsLeafSuchThat(leaf -> p.test(leaf.value));
		return p.test(obj);
	}

	/** Method that loads an XML document, using the specified file name. */
	public static Document loadDocument(String fileName) throws Exception {
		if (fileName.endsWith("xml.bz2") || fileName.endsWith("xml.lzma")) {
			Process p = Runtime.getRuntime().exec((fileName.endsWith("xml.bz2") ? "bunzip2 -c " : "lzma -c -d ") + fileName);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.getInputStream());
			p.waitFor();
			return document;
		} else
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(fileName)));
	}

	/** Method that returns an array with the child elements of the specified element. */
	public static Element[] childElementsOf(Element element) {
		NodeList childs = element.getChildNodes();
		return IntStream.range(0, childs.getLength()).mapToObj(i -> childs.item(i)).filter(e -> e.getNodeType() == Node.ELEMENT_NODE).toArray(Element[]::new);
	}

	/** Determines whether the specified element has the specified type as tag name. */
	public static boolean isTag(Element elt, TypeChild type) {
		return elt.getTagName().equals(type.name());
	}

	public static Element element(Document doc, String tag, List<Element> sons) {
		Element elt = doc.createElement(tag);
		sons.stream().forEach(c -> elt.appendChild(c));
		return elt;
	}

	public static Element element(Document doc, String tag, Element son) {
		return element(doc, tag, Arrays.asList(son));
	}

	public static Element element(Document doc, String tag, Element son, Element... otherSons) {
		return element(doc, tag, IntStream.range(0, 1 + otherSons.length).mapToObj(i -> i == 0 ? son : otherSons[i - 1]).collect(Collectors.toList()));
	}

	public static Element element(Document doc, String tag, Element son, Stream<Element> otherSons) {
		return element(doc, tag, Stream.concat(Stream.of(son), otherSons).collect(Collectors.toList()));
	}

	public static Element element(Document doc, String tag, Object textContent) {
		Element elt = doc.createElement(tag);
		elt.setTextContent(" " + textContent + " ");
		return elt;
	}

	public static Element element(Document doc, String tag, String attName, String attValue) {
		Element elt = doc.createElement(tag);
		elt.setAttribute(attName, attValue);
		return elt;
	}

	public static Element element(Document doc, String tag, String attName1, String attValue1, String attName2, String attValue2) {
		Element elt = doc.createElement(tag);
		elt.setAttribute(attName1, attValue1);
		elt.setAttribute(attName2, attValue2);
		return elt;
	}

	public static Element element(Document doc, String tag, String attName, String attValue, Object textContent) {
		Element elt = element(doc, tag, textContent);
		elt.setAttribute(attName, attValue);
		return elt;
	}
}
