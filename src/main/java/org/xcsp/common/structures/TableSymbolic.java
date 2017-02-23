package org.xcsp.common.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Utilities;

public class TableSymbolic extends Table {
	@Override
	public TableSymbolic positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	private List<String[]> list = new ArrayList<>();

	@Override
	public TableInteger add(int[] tuple) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableInteger add(int val, int... otherVals) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableInteger add(int[] tuple, int[]... otherTuples) {
		throw new RuntimeException("Should not be called (mixing integer and symbolic tuples is not valid");
	}

	@Override
	public TableSymbolic add(String[] tuple) {
		Utilities.control(tuple.length > 0, "The tuple has a length equal to 0");
		Utilities.control(list.size() == 0 || list.get(0).length == tuple.length, "The tuple has a different length from those already recorded");
		list.add(tuple);
		return this;
	}

	@Override
	public TableSymbolic add(String symbol, String... otherSymbols) {
		return add(IntStream.range(0, otherSymbols.length + 1).mapToObj(i -> i == 0 ? symbol : otherSymbols[i - 1]).toArray(String[]::new));
	}

	@Override
	public TableSymbolic add(String[] tuple, String[]... otherTuples) {
		String[][] tuples = IntStream.range(0, otherTuples.length + 1).mapToObj(i -> i == 0 ? tuple : otherTuples[i - 1]).toArray(String[][]::new);
		Stream.of(tuples).forEach(t -> add(t));
		return this;
	}

	@Override
	public TableSymbolic add(String s) {
		String[][] tuples = Stream.of(s.split(Constants.DELIMITER_LISTS)).skip(1).map(tok -> Stream.of(tok.split("\\s*,\\s*")).toArray(String[]::new))
				.toArray(String[][]::new);
		Stream.of(tuples).forEach(tuple -> add(tuple));
		return this;
	}

	/**
	 * Returns a 2-dimensional array corresponding to the collected tuples.
	 * 
	 * @return a 2-dimensional array corresponding to the collected tuples
	 */
	public String[][] toArray() {
		return list.stream().toArray(String[][]::new);
	}

}