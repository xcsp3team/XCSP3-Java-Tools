package org.xcsp.common.structures;

/**
 * This class allows us to represent a transition that is a useful object when defining finite automatons.
 */
public class Transition {

	/**
	 * The first state, where the transition begins.
	 */
	public String start;

	/**
	 * The symbol labeling the transition.
	 */
	public Object symbol;

	/**
	 * The second state, where the transition ends.
	 */
	public String end;

	/**
	 * Constructs a transition from the specified arguments.
	 * 
	 * @param firstState
	 *            the first state, where the transition begins
	 * @param symbol
	 *            the symbol labeling the transition
	 * @param secondState
	 *            the second state, where the transition ends
	 */
	public Transition(String firstState, Object symbol, String secondState) {
		this.start = firstState;
		this.symbol = symbol;
		this.end = secondState;
	}

	@Override
	public String toString() {
		return "(" + start + "," + symbol.toString() + "," + end + ")";
	}
}
