/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.modeler.problems;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.ProblemAPI;

// 48 solutions
public class Zebra implements ProblemAPI {

	@Override
	public void model() {
		Var yellow = var("yellow", dom(range(1, 5)));
		Var green = var("green", dom(range(1, 5)));
		Var red = var("red", dom(range(1, 5)));
		Var white = var("white", dom(range(1, 5)));
		Var blue = var("blue", dom(range(1, 5)));

		Var italy = var("italy", dom(range(1, 5)));
		Var spain = var("spain", dom(range(1, 5)));
		Var japan = var("japan", dom(range(1, 5)));
		Var england = var("england", dom(range(1, 5)));
		Var norway = var("norway", dom(range(1, 5)));

		Var painter = var("painter", dom(range(1, 5)));
		Var sculptor = var("sculptor", dom(range(1, 5)));
		Var diplomat = var("diplomat", dom(range(1, 5)));
		Var pianist = var("pianist", dom(range(1, 5)));
		Var doctor = var("doctor", dom(range(1, 5)));

		Var cat = var("cat", dom(range(1, 5)));
		Var zebra = var("zebra", dom(range(1, 5)));
		Var bear = var("bear", dom(range(1, 5)));
		Var snails = var("snails", dom(range(1, 5)));
		Var horse = var("horse", dom(range(1, 5)));

		Var milk = var("milk", dom(range(1, 5)));
		Var water = var("water", dom(range(1, 5)));
		Var tea = var("tea", dom(range(1, 5)));
		Var coffee = var("coffee", dom(range(1, 5)));
		Var juice = var("juice", dom(range(1, 5)));

		allDifferent(yellow, green, red, white, blue);
		allDifferent(italy, spain, japan, england, norway);
		allDifferent(painter, sculptor, diplomat, pianist, doctor);
		allDifferent(cat, zebra, bear, snails, horse);
		allDifferent(milk, water, tea, coffee, juice);

		equal(painter, horse);
		equal(diplomat, coffee);
		equal(white, milk);
		equal(spain, painter);
		equal(england, red);
		equal(snails, sculptor);
		equal(add(green, 1), red);
		equal(add(blue, 1), norway);
		equal(doctor, milk);
		equal(japan, diplomat);
		equal(norway, zebra);
		equal(dist(green, white), 1);
		belong(horse, set(sub(diplomat, 1), add(diplomat, 1)));
		belong(italy, set(red, white, green));
	}
}
