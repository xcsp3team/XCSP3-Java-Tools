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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.predicates.XNode;
import org.xcsp.common.predicates.XNodeLeaf;

/**
 * A class with some utility (static) methods.
 * 
 * @author Christophe Lecoutre
 */
public class Utilities {

	public static final Comparator<int[]> lexComparatorInt = (t1, t2) -> {
		for (int i = 0; i < t1.length; i++)
			if (t1[i] < t2[i])
				return -1;
			else if (t1[i] > t2[i])
				return +1;
		return 0;
	};

	public static final Comparator<String[]> lexComparatorString = (t1, t2) -> {
		for (int i = 0; i < t1.length; i++) {
			int res = t1[i].compareTo(t2[i]);
			if (res != 0)
				return res;
		}
		return 0;
	};

	public static <T> T[] buildArray(Class<?> cl, int length) {
		return (T[]) Array.newInstance(cl, length);
	}

	public static <T> T[][] buildArray(Class<?> cl, int length1, int length2) {
		return (T[][]) Array.newInstance(cl, length1, length2);
	}

	public static Object firstNonNull(Object array) {
		if (array != null && array.getClass().isArray())
			return IntStream.range(0, Array.getLength(array)).mapToObj(i -> firstNonNull(Array.get(array, i))).filter(o -> o != null).findFirst().orElse(null);
		return array;
	}

	/**
	 * Builds a one-dimensional array of T with the objects of the specified list. If the list does not contain any object other than null, null is
	 * returned.
	 */
	public static <T> T[] convert(Collection<T> list) {
		Object firstObject = list.stream().filter(o -> o != null).findFirst().orElse(null);
		if (firstObject == null)
			return null;
		T[] ts = buildArray(firstObject.getClass(), list.size());
		int i = 0;
		for (T x : list)
			ts[i++] = x;
		return ts;
	}

	/**
	 * Builds a one-dimensional array of T with the objects of the specified stream. If the stream does not contain any object other than null, null
	 * is returned.
	 */
	public static <T> T[] convert(Stream<T> stream) {
		return convert(stream.collect(Collectors.toList()));
	}

	public static <T> T[] convert(Object[] t) {
		Object firstObject = Stream.of(t).filter(o -> o != null).findFirst().orElse(null);
		Class<?> clazz = firstObject == null ? null
				: Stream.of(t).noneMatch(o -> o != null && o.getClass() != firstObject.getClass()) ? firstObject.getClass() : null;
		if (clazz == null)
			return null; // null is returned if the array has only null or elements of several types
		T[] ts = buildArray(firstObject.getClass(), t.length);
		int i = 0;
		for (Object x : t)
			ts[i++] = (T) x;
		return ts;
	}

	public static <T> T[][] convert(Object[][] t) {
		control(isRegular(t), " pb");
		// other controls to add
		T[][] m = buildArray(t[0][0].getClass(), t.length, t[0].length);
		for (int i = 0; i < t.length; i++)
			for (int j = 0; j < t[i].length; j++)
				m[i][j] = (T) t[i][j];
		return m;
	}

	public static Object[] specificArrayFrom(List<Object> list) {
		Object firstObject = list.stream().filter(o -> o != null).findFirst().orElse(null);
		Class<?> clazz = firstObject == null ? null
				: list.stream().noneMatch(o -> o != null && o.getClass() != firstObject.getClass()) ? firstObject.getClass() : null;
		return clazz == null ? list.toArray() : list.toArray((Object[]) Array.newInstance(clazz, list.size()));
	}

	public static Object[][] specificArray2DFrom(List<Object[]> list) {
		Class<?> clazz = list.stream().noneMatch(o -> o.getClass() != list.get(0).getClass()) ? list.get(0).getClass() : null;
		return clazz == null ? list.toArray(new Object[0][]) : list.toArray((Object[][]) Array.newInstance(clazz, list.size()));
	}

	private static <T> List<T> collectRec(Class<T> clazz, List<T> list, Object src) {
		if (src != null)
			if (src instanceof Collection)
				collectRec(clazz, list, ((Collection<?>) src).stream());
			else if (src instanceof Stream)
				((Stream<?>) src).forEach(o -> collectRec(clazz, list, o));
			else if (src.getClass().isArray())
				IntStream.range(0, Array.getLength(src)).forEach(i -> collectRec(clazz, list, Array.get(src, i)));
			else if (clazz.isAssignableFrom(src.getClass()))
				list.add(clazz.cast(src));
			else if (src instanceof IntStream)
				((IntStream) src).forEach(o -> collectRec(clazz, list, o));
			else if (src.getClass() == Range.class)
				collectRec(clazz, list, ((Range) src).toArray()); // in order to deal with clazz being Integer.class
		return list;
	}

	/**
	 * Returns a 1-dimensional array of objects of the specified type after collecting any object of this type being present in the specified objects.
	 * The specified objects can be stream (and IntStream), collections and arrays. The collecting process is made recursively.
	 * 
	 * @param clazz
	 *            the class of the objects to be collected
	 * @param src
	 *            the objects where to collect the objects
	 * @return a 1-dimensional array of objects of the specified type after collecting any object of this type being present in the specified objects
	 */
	public static <T> T[] collect(Class<T> clazz, Object... src) {
		List<T> list = new ArrayList<>();
		Stream.of(src).forEach(o -> collectRec(clazz, list, o));
		return convert(list.stream().collect(Collectors.toList()));
	}

	/**
	 * Builds a 1-dimensional array of integers (int) from the specified sequence of parameters. Each parameter can be an integer, a Range, an array,
	 * a stream, a collection, etc. All integers are collected and concatenated to form a 1-dimensional array.
	 *
	 * @param src
	 *            the objects where to collect the integers
	 * @return a 1-dimensional array of integers after collecting any encountered integer in the specified objects
	 */
	public static int[] collectInt(Object... src) {
		Integer[] t = collect(Integer.class, src);
		return t == null ? new int[0] : Stream.of(t).filter(i -> i != null).mapToInt(i -> i).toArray();
	}

	public static boolean isNumeric(String token) {
		return token.matches("-?\\d+(\\.\\d+)?"); // match a number with optional '-' and decimal.
	}

	public static boolean isNumericInterval(String token) {
		return token.matches("-?\\d+\\.\\.-?\\d+");
	}

	public static int[] splitToInts(String s, String regex) {
		return Arrays.stream(s.trim().split(regex)).filter(tok -> tok.length() > 0).mapToInt(tok -> Integer.parseInt(tok)).toArray();
	}

	public static int[] splitToInts(String s) {
		return splitToInts(s, Constants.REG_WS);
	}

	public static int splitToInt(String s, String regex) {
		int[] t = splitToInts(s, regex);
		control(t.length > 0, "Not possible to extract an int from this call");
		return t[0];
	}

	public static int[] wordAsIntArray(String s) {
		assert s.chars().allMatch(c -> ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z'));
		int[] t = new int[s.length()]; // We don't use streams here for efficiency reasons (when dealing with large sets of words)
		for (int i = 0; i < s.length(); i++)
			t[i] = s.charAt(i) - (Character.isUpperCase(s.charAt(i)) ? 'A' : 'a');
		return t;
	}

	public static Boolean toBoolean(String s) {
		s = s.toLowerCase();
		if (s.equals("yes") || s.equals("y") || s.equals("true") || s.equals("t") || s.equals("1"))
			return Boolean.TRUE;
		if (s.equals("no") || s.equals("n") || s.equals("false") || s.equals("f") || s.equals("0"))
			return Boolean.FALSE;
		return null;
	}

	public static boolean isInteger(String token) {
		try {
			Integer.parseInt(token);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
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

	public static BigInteger powerBig(long a, int b) {
		return BigInteger.valueOf(a).pow(b);
	}

	public static int power(long a, int b) {
		return powerBig(a, b).intValueExact();
	}

	private static BigInteger recursiveFactorial(long start, long n) {
		long i;
		if (n <= 16) {
			BigInteger r = BigInteger.valueOf(start);
			for (i = start + 1; i < start + n; i++)
				r = r.multiply(BigInteger.valueOf(i));
			return r;
		}
		i = n / 2;
		return recursiveFactorial(start, i).multiply(recursiveFactorial(start + i, n - i));
	}

	public static BigInteger factorialBig(int n) {
		return recursiveFactorial(1, n);
	}

	public static int factorial(int n) {
		return factorialBig(n).intValueExact();
	}

	public static BigInteger binomialBig(int n, int k) {
		if (k < 0 || n < k)
			return BigInteger.ZERO;
		if (k > n - k)
			k = n - k;
		BigInteger i = BigInteger.ONE;
		for (int v = 0; v < k; v++)
			i = i.multiply(BigInteger.valueOf(n - v)).divide(BigInteger.valueOf(v + 1));
		return i;
	}

	public static int binomial(int n, int k) {
		return binomialBig(n, k).intValueExact();
	}

	public static BigInteger nArrangementsFor(int[] nValues) {
		return IntStream.of(nValues).mapToObj(v -> BigInteger.valueOf(v)).reduce(BigInteger.ONE, (acc, v) -> acc.multiply(v));
	}

	public static boolean contains(int[] tab, int v, int from, int to) {
		return IntStream.rangeClosed(from, to).anyMatch(i -> tab[i] == v);
	}

	public static boolean contains(int[] tab, int v) {
		return contains(tab, v, 0, tab.length - 1);
	}

	public static int indexOf(String s, String... t) {
		return IntStream.range(0, t.length).filter(i -> t[i].equals(s)).findFirst().orElse(-1);
	}

	public static int indexOf(String s, List<Object> list) {
		return IntStream.range(0, list.size()).filter(i -> list.get(i).equals(s)).findFirst().orElse(-1);
	}

	public static int indexOf(int value, int[] t) {
		for (int i = 0; i < t.length; i++)
			if (value == t[i])
				return i;
		return -1;
		// return IntStream.range(0, t.length).filter(i -> t[i] == value).findFirst().orElse(-1);
	}

	public static int indexOf(Object value, Object[] t) {
		return IntStream.range(0, t.length).filter(i -> t[i] == value).findFirst().orElse(-1);
	}

	/**
	 * Returns true is the array is regular and matches exactly the specified size. For example, if size is [5,4] then the specified array must be a
	 * 2-dimensional array of 5 x 4 squares.
	 */
	public static boolean hasSize(Object array, int... size) {
		boolean b1 = array != null && array.getClass().isArray(), b2 = size.length > 0;
		if (!b1 && !b2)
			return true;
		if (b1 && !b2 || !b1 && b2 || Array.getLength(array) != size[0])
			return false;
		return IntStream.range(0, size[0]).noneMatch(i -> !hasSize(Array.get(array, i), Arrays.stream(size).skip(1).toArray()));
	}

	/**
	 * Returns true is the array is regular, that is to say has the form of a rectangle for a 2-dimensional array, a cube for a 3-dimensional array...
	 * For example, if the specified array is a 2-dimensional array of 5 x 4 squares, then it is regular. But it has 3 squares for the first row, and
	 * 4 squares for the second row, then it is not regular.
	 */
	public static boolean isRegular(Object array) {
		List<Integer> list = new ArrayList<>();
		for (Object a = array; a != null && a.getClass().isArray(); a = Array.getLength(a) == 0 ? null : Array.get(a, 0))
			list.add(Array.getLength(a));
		return hasSize(array, list.stream().mapToInt(i -> i).toArray());
	}

	/**
	 * Method that controls that the specified condition is verified. If it is not the case, a message is displayed and the program is stopped.
	 */
	public static Object control(boolean condition, String message) {
		if (!condition) {
			System.out.println("\n\nFatal Error: " + message);
			throw new RuntimeException();
			// System.exit(1);
		}
		return null;
	}

	public static Object exit(String message) {
		return control(false, message);
	}

	/**
	 * Checks if the specified {@code Runnable} object raises an {@code ArithmeticException} object, when run. The value {@code true} is returned iff
	 * no such exception is raised.
	 * 
	 * @param r
	 *            a {@code Runnable} object to be run
	 * @return {@code true} iff no {@code ArithmeticException} is raised when running the specified code
	 */
	public static boolean checkSafeArithmeticOperation(Runnable r) {
		try {
			r.run();
			return true;
		} catch (ArithmeticException e) {
			return false;
		}
	}

	/**
	 * Method that parses the specified string as a long integer. If the value is too small or too big, an exception is raised. The specified boolean
	 * allows us to indicate if some special values (such as +infinity) must be checked.
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
		// try { return Long.parseLong(s); } catch (NumberFormatException e) {
		// throw new WrongTypeException("\"" + s + "\" is not an integer expression"); }
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
	 * Converts the specified long to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe according to
	 * the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(long l, boolean useMargin) {
		control(isSafeInt(l, useMargin), "Too big integer value " + l);
		return (int) l;
	}

	/**
	 * Converts the specified number to int if it is safe to do it. When the specified boolean is set to true, we control that it is safe according to
	 * the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2Int(Number number, boolean useMargin) {
		return safeLong2Int(number.longValue(), useMargin);
	}

	/**
	 * Converts the specified long to int if it is safe to do it. Note that VAL_MINUS_INFINITY will be translated to VAL_MINUS_INFINITY_INT and that
	 * VAL_PLUS_INFINITY will be translated to VAL_PLUS_INFINITY_INT . When the specified boolean is set to true, we control that it is safe according
	 * to the constants MIN_SAFE_INT and MAX_SAFE_INT.
	 */
	public static int safeLong2IntWhileHandlingInfinity(long l, boolean useMargin) {
		return l == Constants.VAL_MINUS_INFINITY ? Constants.VAL_MINUS_INFINITY_INT
				: l == Constants.VAL_PLUS_INFINITY ? Constants.VAL_PLUS_INFINITY_INT : safeLong2Int(l, true);
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
		if (array instanceof IVar[])
			return LEFT + String.join(SEP, Stream.of((IVar[]) array).map(x -> x.toString()).toArray(String[]::new)) + RIGHT;

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

		if (array instanceof IVar[][])
			return LEFT + String.join(SEP, Stream.of((IVar[][]) array).map(t -> arrayToString(t, LEFT, RIGHT, SEP)).toArray(String[]::new)) + RIGHT;

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
	 * Returns true if inside the specified object, there is an element that checks the predicate. If syntactic trees are encountered, we check the
	 * leaves only.
	 */
	public static boolean check(Object obj, Predicate<Object> p) {
		if (obj instanceof Object[])
			return IntStream.range(0, Array.getLength(obj)).anyMatch(i -> check(Array.get(obj, i), p));
		if (obj instanceof XNode)
			return ((XNode<?>) obj).firstNodeSuchThat(n -> n instanceof XNodeLeaf && p.test(((XNodeLeaf<?>) n).value)) != null;
		// if (obj instanceof XNode)
		// return ((XNode<?>) obj).containsLeafSuchThat(leaf -> p.test(leaf.value));
		return p.test(obj);
	}

	public static class ModifiableBoolean {
		public Boolean value;

		public ModifiableBoolean(Boolean value) {
			this.value = value;
		}
	}

	// ************************************************************************
	// ***** Methods for XML
	// ************************************************************************

	/** Method that loads an XML document, using the specified file name. */
	public static Document loadDocument(String fileName) throws Exception {
		Utilities.control(new File(fileName).exists(), "Filename " + fileName + " not found\n");
		if (fileName.endsWith("xml.bz2") || fileName.endsWith("xml.lzma")) {
			Process p = Runtime.getRuntime().exec((fileName.endsWith("xml.bz2") ? "bunzip2 -c " : "lzma -c -d ") + fileName);
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(p.getInputStream());
			p.waitFor();
			return document;
		} else
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(new File(fileName)));
	}

	public static void save(Document document, PrintWriter out) {
		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(out));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String save(Document document, String fileName) {
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
			save(document, out);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fileName;
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

	public static Element element(Document doc, String tag, String attName, String attValue, Object textContent) {
		Element elt = element(doc, tag, textContent);
		elt.setAttribute(attName, attValue);
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

	public static Element element(Document doc, String tag, Collection<Entry<String, Object>> attributes) {
		Element elt = doc.createElement(tag);
		if (attributes != null)
			attributes.stream().forEach(e -> elt.setAttribute(e.getKey(), e.getValue().toString()));
		return elt;
	}

}
