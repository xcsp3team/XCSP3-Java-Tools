package org.xcsp.common.structures;

import java.util.stream.Stream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to represent tables that are useful objects when defining {@code extension} constraints.
 */
public abstract class Table {

	public final String TABLE_SYNTAX_PB = "The syntax used for listing tuples is not correct.\n"
			+ "For example, this should be (1,2)(2,1)(2,3) for an integer table and (a,b)(b,b)(c,a) for a symbolic table";

	public Boolean positive = Boolean.TRUE;

	public Table positive(Boolean positive) {
		this.positive = positive;
		return this;
	}

	/**
	 * Controls that the string is well-formed. The string must represent a sequence of tuples as defined in XCSP3. For example, it could be
	 * {@code "(0,0,1)(0,2,0)(1,0,1)(1,1,2)"} when tuples are integer or {@code "(a,a,b)(a,c,a)(b,a,b)(b,b,c)"} when tuples are symbolic.
	 * 
	 * @param tuples
	 *            a string representing a sequence of tuples
	 * @return {@code true} if the tuples are integer
	 */
	protected boolean controlStringRepresentationOfTuples(String tuples) {
		tuples = tuples.trim();
		Utilities.control(tuples.length() != 0, "The string is empty");
		Utilities.control(tuples.charAt(0) == '(' && tuples.charAt(tuples.length() - 1) == ')', "Parentheses are not correct in " + tuples);
		String[] toks = tuples.substring(1, tuples.indexOf(")")).split("\\s*,\\s*");
		Utilities.control(Stream.of(toks).anyMatch(tok -> !tok.equals("*")), "The tuple only contains *");
		return Stream.of(toks).anyMatch(tok -> Utilities.isInteger(tok));
	}

}