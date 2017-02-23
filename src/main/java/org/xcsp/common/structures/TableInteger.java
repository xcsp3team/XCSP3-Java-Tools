package org.xcsp.common.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Utilities;

/**
 * This class allows us to represent integer tables that are useful objects when defining {@code extension} constraints.
 */
public class TableInteger extends Table {

	@Override
	public TableInteger positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	private List<int[]> list = new ArrayList<>();

	@Override
	public TableInteger add(int[] tuple) {
		Utilities.control(tuple.length > 0, "The tuple has a length equal to 0");
		Utilities.control(list.size() == 0 || list.get(0).length == tuple.length, "The tuple has a different length from those already recorded");
		list.add(tuple);
		return this;
	}

	@Override
	public TableInteger add(int val, int... otherVals) {
		return add(IntStream.range(0, otherVals.length + 1).map(i -> i == 0 ? val : otherVals[i - 1]).toArray());
	}

	@Override
	public TableInteger add(int[] tuple, int[]... otherTuples) {
		int[][] tuples = IntStream.range(0, otherTuples.length + 1).mapToObj(i -> i == 0 ? tuple : otherTuples[i - 1]).toArray(int[][]::new);
		Stream.of(tuples).forEach(t -> add(t));
		return this;
	}

	@Override
	public TableSymbolic add(String[] tuple) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableSymbolic add(String symbol, String... otherSymbols) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableSymbolic add(String[] tuple, String[]... otherTuples) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableInteger add(String s) {
		int[][] tuples = Stream.of(s.split(Constants.DELIMITER_LISTS)).skip(1).map(
				tok -> Stream.of(tok.split("\\s*,\\s*")).mapToInt(v -> v.equals(Constants.STAR_SYMBOL) ? Constants.STAR_INT : Integer.parseInt(v)).toArray())
				.toArray(int[][]::new);
		Stream.of(tuples).forEach(tuple -> add(tuple));
		return this;
	}

	/**
	 * Returns a 2-dimensional array corresponding to the collected tuples.
	 * 
	 * @return a 2-dimensional array corresponding to the collected tuples
	 */
	public int[][] toArray() {
		return list.stream().toArray(int[][]::new);
	}

}