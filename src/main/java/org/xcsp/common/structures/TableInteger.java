package org.xcsp.common.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Utilities;
import org.xcsp.common.enumerations.EnumerationCartesian;

/**
 * This class allows us to represent integer tables that are useful objects when defining {@code extension} constraints.
 */
public class TableInteger extends Table {

	public static int[][] toOrdinaryTable(int[][] shortTable, int[][] values) {
		List<int[]> tuples = new ArrayList<>();
		for (int[] t : shortTable) {
			int[] pos = IntStream.range(0, t.length).filter(i -> t[i] == Constants.STAR_INT).toArray();
			if (pos.length == 0)
				tuples.add(t.clone());
			else {
				EnumerationCartesian ec = new EnumerationCartesian(
						IntStream.range(0, t.length).mapToObj(i -> t[i] == Constants.STAR_INT ? values[i] : new int[] { t[i] }).toArray(int[][]::new));
				while (ec.hasNext())
					tuples.add(ec.next().clone());
			}
		}
		return tuples.stream().sorted(Utilities.lexComparatorInt).distinct().toArray(int[][]::new);
		// return Kit.intArray2D(tuples);
	}

	public static int[][] toOrdinaryTable(int[][] shortTable, int... nValues) {
		return toOrdinaryTable(shortTable, IntStream.of(nValues).mapToObj(i -> IntStream.range(0, i).toArray()).toArray(int[][]::new));
	}

	@Override
	public TableInteger positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	private List<int[]> list = new ArrayList<>();

	/**
	 * Adds an integer tuple to the table.
	 * 
	 * @param tuple
	 *            an integer tuple
	 * @return this integer table
	 */
	public TableInteger add(int... tuple) {
		Utilities.control(tuple.length > 0, "A tuple of length 0 has been encoutered during parsing.\n" + TABLE_SYNTAX_PB);
		Utilities.control(list.size() == 0 || list.get(0).length == tuple.length, "The tuple has a different length from those already recorded");
		list.add(tuple);
		return this;
	}

	/**
	 * Adds the specified integer tuples to the table.
	 * 
	 * @param tuples
	 *            a sequence of other tuples
	 * @return this integer table
	 */
	public TableInteger add(int[]... tuples) {
		Stream.of(tuples).forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds all tuples of the specified stream to the table.
	 * 
	 * @param stream
	 *            a stream of tuples to be added to the table
	 * @return this integer table
	 */
	public TableInteger add(Stream<int[]> stream) {
		stream.forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds the tuples obtained after parsing the specified string. The string must represent a sequence of tuples as defined in XCSP3. For
	 * example, it could be {@code "(0,0,1)(0,2,0)(1,0,1)(1,1,2)"}.
	 * 
	 * @param tuples
	 *            a string representing a sequence of integer tuples
	 * @return this integer table
	 */
	public TableInteger add(String s) {
		boolean b = controlStringRepresentationOfTuples(s);
		Utilities.control(b, "The specified string is not correct, as it does not correspond to a sequence of integer tuples");
		int[][] tuples = Stream.of(s.split(Constants.DELIMITER_LISTS)).skip(1).map(
				tok -> Stream.of(tok.split("\\s*,\\s*")).mapToInt(v -> v.equals(Constants.STAR_SYMBOL) ? Constants.STAR_INT : Integer.parseInt(v)).toArray())
				.toArray(int[][]::new);
		Stream.of(tuples).forEach(tuple -> add(tuple));
		return this;
	}

	/**
	 * Returns a 2-dimensional array corresponding to the collected tuples. Tuples are sorted and made distinct.
	 * 
	 * @return a 2-dimensional array corresponding to the collected tuples
	 */
	public int[][] toArray() {
		return list.stream().sorted(Utilities.lexComparatorInt).distinct().toArray(int[][]::new);
	}

	public int[][] toOrdinaryTableArray(int[][] values) {
		return TableInteger.toOrdinaryTable(toArray(), values);
	}

	public int[][] toOrdinaryTableArray(int... nValues) {
		return TableInteger.toOrdinaryTable(toArray(), nValues);
	}

}