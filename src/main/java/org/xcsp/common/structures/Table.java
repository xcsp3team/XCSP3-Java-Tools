package org.xcsp.common.structures;

import java.util.stream.Stream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to represent tables that are useful objects when defining {@code extension} constraints.
 */
public class Table {
	public Boolean positive = Boolean.TRUE;

	public Table positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	/**
	 * Adds an integer tuple to the table.
	 * 
	 * @param tuple
	 *            an integer tuple
	 * @return this integer table
	 */
	public TableInteger add(int[] tuple) {
		return new TableInteger().add(tuple);
	}

	/**
	 * Adds an integer tuple (built from the specified arguments) to the table.
	 * 
	 * @param val
	 *            a first value
	 * @param otherVals
	 *            a sequence of other values
	 * @return this integer table
	 */
	public TableInteger add(int val, int... otherVals) {
		return new TableInteger().add(val, otherVals);
	}

	/**
	 * Adds the specified integer tuples to the table.
	 * 
	 * @param tuple
	 *            a first tuple
	 * @param otherTuples
	 *            a sequence of other tuples
	 * @return this integer table
	 */
	public TableInteger add(int[] tuple, int[]... otherTuples) {
		return new TableInteger().add(tuple, otherTuples);
	}

	/**
	 * Adds a symbolic tuple to the table.
	 * 
	 * @param tuple
	 *            a symbolic tuple
	 * @return this symbolic table
	 */
	public TableSymbolic add(String[] tuple) {
		return new TableSymbolic().add(tuple);
	}

	/**
	 * Adds a symbolic tuple (built from the specified arguments) to the table.
	 * 
	 * @param symbol
	 *            a first symbol
	 * @param otherSymbols
	 *            a sequence of other symbols
	 * @return this symbolic table
	 */
	public TableSymbolic add(String symbol, String... otherSymbols) {
		return new TableSymbolic().add(symbol, otherSymbols);
	}

	/**
	 * Adds the specified symbolic tuples to the table.
	 * 
	 * @param tuple
	 *            a first tuple
	 * @param otherTuples
	 *            a sequence of other tuples
	 * @return this symbolic table
	 */
	public TableSymbolic add(String[] tuple, String[]... otherTuples) {
		return new TableSymbolic().add(tuple, otherTuples);
	}

	/**
	 * Adds the tuples obtained after parsing the specified string. The string must represent a sequence of tuples as defined in XCSP3. For
	 * example, it could be {@code "(0,0,1)(0,2,0)(1,0,1)(1,1,2)"} when tuples are integer or {@code "(a,a,b)(a,c,a)(b,a,b)(b,b,c)"} when
	 * tuples are symbolic.
	 * 
	 * @param tuples
	 *            a string representing a sequence of tuples
	 * @return this table
	 */
	public Table add(String tuples) {
		tuples = tuples.trim();
		Utilities.control(tuples.length() != 0, "The string is empty");
		Utilities.control(tuples.charAt(0) == '(' && tuples.charAt(tuples.length() - 1) == ')', "Parentheses are not correct in " + tuples);
		String[] toks = tuples.substring(1, tuples.indexOf(")")).split("\\s*,\\s*");
		Utilities.control(Stream.of(toks).anyMatch(tok -> !tok.equals("*")), "The tuple only contains *");
		boolean b = Stream.of(toks).anyMatch(tok -> Utilities.isInteger(tok));
		return b ? new TableInteger().add(tuples) : new TableSymbolic().add(tuples);
	}

}