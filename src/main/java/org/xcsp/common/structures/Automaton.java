/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.structures;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to represent finite automatons that are useful for posting {@code regular} constraints. An automaton is composed of
 * an initial state, a finite set of final states and a finite set of transitions.
 */
public final class Automaton {
	/**
	 * The set (array) of transitions. Each transition is an object composed of a first state, a symbol (that may be an integer or a string)
	 * and a second state that is reached from the first state after reading the symbol.
	 */
	public final Transition[] transitions;

	/**
	 * The start (initial) state of the automaton.
	 */
	public final String startState;

	/**
	 * The set (array) of final states of the automaton, i.e., accepting stated of the automaton.
	 */
	public final String[] finalStates;

	/**
	 * Constructs an automaton from the specified arguments.
	 * 
	 * @param transitions
	 *            the set (array) of transitions where each transition is an object composed of a first state, a symbol (that may be an
	 *            integer or a string) and a second state that is reached from the first state after reading the symbol
	 * @param startState
	 *            the start state of the automaton
	 * @param finalStates
	 *            the set (array) of final states of the automaton, i.e., accepting stated of the automaton
	 */
	public Automaton(Transition[] transitions, String startState, String... finalStates) {
		this.transitions = transitions;
		this.startState = startState;
		this.finalStates = finalStates;
	}

	/**
	 * Constructs an automaton from the specified arguments.
	 * 
	 * @param transitions
	 *            the object encapsulating the list of transitions where each transition is an object composed of a first state, a symbol
	 *            (that may be an integer or a string) and a second state that is reached from the first state after reading the symbol
	 * @param startState
	 *            the start state of the automaton
	 * @param finalStates
	 *            the set (array) of final states of the automaton, i.e., accepting stated of the automaton
	 */
	public Automaton(Transitions transitions, String startState, String... finalStates) {
		this(transitions.toArray(), startState, finalStates);
	}

	/**
	 * Constructs an automaton from the specified arguments. Be careful: this method is deprecated (and will even be deleted in the future)
	 * because we shall use a string representing the regular expression instead.
	 * 
	 * @param nonogramPattern
	 */
	@Deprecated
	public Automaton(int[] nonogramPattern) {
		Function<Integer, String> q = i -> "q" + i;
		int nbStates = 0;
		Transitions transitions = new Transitions();
		if (nonogramPattern.length == 0) {
			nbStates = 1;
			transitions.add(q.apply(0), 0, q.apply(0));
		} else {
			nbStates = IntStream.of(nonogramPattern).sum() + nonogramPattern.length;
			int num = 0;
			for (int i = 0; i < nonogramPattern.length; i++) {
				transitions.add(q.apply(num), 0, q.apply(num));
				for (int j = 0; j < nonogramPattern[i]; j++) {
					transitions.add(q.apply(num), 1, q.apply(num + 1));
					num++;
				}
				if (i < nonogramPattern.length - 1) {
					transitions.add(q.apply(num), 0, q.apply(num + 1));
					num++;
				}
			}
			transitions.add(q.apply(num), 0, q.apply(num));
		}
		this.transitions = transitions.toArray();
		this.startState = q.apply(0);
		this.finalStates = new String[] { q.apply(nbStates - 1) };
	}

	/**
	 * Constructs an automaton from the specified regular expression, given as a string.
	 * 
	 * @param expression
	 *            the expression representing a regular expression.
	 */
	public Automaton(String expression) {
		Utilities.exit("Unimplemented code; converting regular expressions into an automaton");
		this.transitions = null;
		this.startState = null;
		this.finalStates = null;
	}

	@Override
	public String toString() {
		return "startState=" + startState + " finalStates={" + Utilities.join(finalStates) + "} " + "\ntransitions={"
				+ Stream.of(transitions).map(t -> t.toString()).collect(Collectors.joining()) + "}";
	}

}