/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.common.structures;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.xcsp.common.Utilities;

/**
 * This class allows us to represent finite automatons that are useful for posting {@code regular} constraints. An automaton is composed of an initial
 * state, a finite set of final states and a finite set of transitions.
 */
public final class Automaton {
	/**
	 * The start (initial) state of the automaton.
	 */
	public final String startState;

	/**
	 * The set (array) of transitions. Each transition is an object composed of a first state, a symbol (that may be an integer or a string) and a
	 * second state that is reached from the first state after reading the symbol.
	 */
	public final Transition[] transitions;

	/**
	 * The set (array) of final states of the automaton, i.e., accepting stated of the automaton.
	 */
	public final String[] finalStates;

	/**
	 * Constructs an automaton from the specified arguments.
	 * 
	 * @param startState
	 *            the start state of the automaton
	 * @param transitions
	 *            the set (array) of transitions where each transition is an object composed of a first state, a symbol (that may be an integer or a
	 *            string) and a second state that is reached from the first state after reading the symbol
	 * @param finalStates
	 *            the set (array) of final states of the automaton, i.e., accepting stated of the automaton
	 */
	public Automaton(String startState, Transition[] transitions, String... finalStates) {
		this.startState = startState;
		this.transitions = transitions;
		this.finalStates = finalStates;
	}

	/**
	 * Constructs an automaton from the specified arguments.
	 * 
	 * @param startState
	 *            the start state of the automaton
	 * @param transitions
	 *            the object encapsulating the list of transitions where each transition is an object composed of a first state, a symbol (that may be
	 *            an integer or a string) and a second state that is reached from the first state after reading the symbol
	 * @param finalStates
	 *            the set (array) of final states of the automaton, i.e., accepting stated of the automaton
	 */
	public Automaton(String startState, Transitions transitions, String... finalStates) {
		this(startState, transitions.toArray(), finalStates);
	}

	/**
	 * Constructs an automaton from the specified regular expression, given as a string.
	 * 
	 * @param expression
	 *            the expression representing a regular expression.
	 */
	public Automaton(String expression) {
		Utilities.exit("Unimplemented code; converting regular expressions into an automaton");
		this.startState = null;
		this.transitions = null;
		this.finalStates = null;
	}

	@Override
	public String toString() {
		return "startState=" + startState + " finalStates={" + Utilities.join(finalStates) + "} " + "\ntransitions={"
				+ Stream.of(transitions).map(t -> t.toString()).collect(Collectors.joining()) + "}";
	}

}