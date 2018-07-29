package org.xcsp.common.structures;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Constants;
import org.xcsp.common.Range;
import org.xcsp.common.Utilities;

/**
 * An object encapsulating a list of transitions. This is sometimes useful when defining finite automatons.
 */
public final class Transitions {

	/**
	 * Parses the specified string and returns an object {@code Transitions} that contains a list of transitions. The string must represent a sequence
	 * of transitions as defined in XCSP3. For example, it could be {@code "(q0,0,q2)(q0,1,q3)(q1,0,q3)(q2,1,q3)"} when symbols are defined by
	 * integers or {@code "(q0,a,q2)(q0,b,q3)(q1,a,q3)(q2,b,q3)"} when symbols are defined by strings.
	 * 
	 * @param transitions
	 *            a string representing a sequence of transitions
	 * @return an object {@code Transitions} containing a list of transitions after parsing the specified argument
	 */
	public static Transitions parse(String transitions) {
		return new Transitions().add(transitions);
	}

	private List<Transition> list = new ArrayList<>();

	/**
	 * Adds transitions to this object.
	 * 
	 * @param transitions
	 *            a string representing a sequence of transitions
	 * @return this object (for chaining)
	 */
	public Transitions add(String transitions) {
		Stream<String> st = Stream.of(transitions.trim().split(Constants.DELIMITER_LISTS)).skip(1);
		st.forEach(tok -> {
			String[] t = tok.split("\\s*,\\s*");
			Utilities.control(t.length == 3, "Pb with a transition, which is not formed of 3 pieces");
			add(t[0], Utilities.isInteger(t[1]) ? Integer.parseInt(t[1]) : t[1], t[2]);
		});
		return this;
	}

	/**
	 * Adds a transition to this object.
	 * 
	 * @param transition
	 *            a transition to be added
	 * @return this object (for chaining)
	 */
	public Transitions add(Transition transition) {
		list.add(transition);
		return this;
	}

	/**
	 * Adds transitions from a stream to this object.
	 * 
	 * @param transitions
	 *            a stream of transitions
	 * @return this object (for chaining)
	 */
	public Transitions add(Stream<Transition> transitions) {
		transitions.forEach(t -> add(t));
		return this;
	}

	/**
	 * Adds a transition to this object.
	 * 
	 * @param firstState
	 *            the first state, where the transition begins
	 * @param symbol
	 *            the symbol labeling the transition
	 * @param secondState
	 *            the second state, where the transition ends
	 * @return this object (for chaining)
	 */
	public Transitions add(String firstState, Object symbol, String secondState) {
		return add(new Transition(firstState, symbol, secondState));
	}

	/**
	 * Adds some transitions to this object, one for each (integer) symbol.
	 * 
	 * @param firstState
	 *            the first state, where the transition begins
	 * @param symbols
	 *            the different symbols (here, integers) labeling the transition
	 * @param secondState
	 *            the second state, where the transition ends
	 * @return this object (for chaining)
	 */
	public Transitions add(String firstState, int[] symbols, String secondState) {
		IntStream.of(symbols).forEach(v -> add(new Transition(firstState, v, secondState)));
		return this;
	}

	/**
	 * Adds some transitions to this object, one for each (integer) symbol contained in the specified range.
	 * 
	 * @param firstState
	 *            the first state, where the transition begins
	 * @param range
	 *            the different symbols (here, integers contained in the range) labeling the transition
	 * @param secondState
	 *            the second state, where the transition ends
	 * @return this object (for chaining)
	 */
	public Transitions add(String firstState, Range range, String secondState) {
		add(firstState, range.toArray(), secondState);
		return this;
	}

	/**
	 * Returns an array with collected objects {@code Transition}
	 * 
	 * @return an array with collected objects {@code Transition}
	 */
	public Transition[] toArray() {
		return list.stream().toArray(Transition[]::new);
	}

	@Override
	public String toString() {
		return list.stream().map(t -> t.toString()).collect(Collectors.joining());
	}

}