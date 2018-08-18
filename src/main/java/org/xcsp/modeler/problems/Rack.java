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
import org.xcsp.modeler.api.ProblemAPI;

//java abscon.Resolution problems.acad.Rack -data=/home/lecoutre/instances/Rack/r2.json -ev -f=cop => 1100 in 40s
public class Rack implements ProblemAPI {
	int nRacks;
	int[][] models;
	int[][] cardTypes;

	@Override
	public void model() {
		models = addObject(models, tuple(0, 0, 0), 0); // we add first a dummy model (0,0,0)
		int nModels = models.length, nTypes = cardTypes.length;
		int[] powers = columnOf(models, 0), connectors = columnOf(models, 1), prices = columnOf(models, 2), cardPowers = columnOf(cardTypes, 0);
		int maxCapacity = maxOf(connectors);

		Var[] r = array("r", size(nRacks), dom(range(nModels)), "r[i] is the model used for the ith rack");
		Var[][] c = array("c", size(nRacks, nTypes), (i, j) -> dom(range(Math.min(maxCapacity, cardTypes[j][1]) + 1)),
				"c[i][j] is the number of cards of type j put in the ith rack");
		Var[] rpw = array("rpw", size(nRacks), dom(powers), "rpw[i] is the power of the ith rack");
		Var[] rcn = array("rcn", size(nRacks), dom(connectors), "rcn[i] is the number of connectors of the ith rack");
		Var[] rpr = array("rpr", size(nRacks), dom(prices), "rpr[i] is the price of the ith rack");

		forall(range(nRacks), i -> extension(vars(r[i], rpw[i]), indexing(powers))).note("linking the ith rack with its power");
		forall(range(nRacks), i -> extension(vars(r[i], rcn[i]), indexing(connectors))).note("linking the ith rack with its number of connectors");
		forall(range(nRacks), i -> extension(vars(r[i], rpr[i]), indexing(prices))).note("linking the ith rack with its price");

		forall(range(nRacks), i -> sum(c[i], LE, rcn[i])).note("connector-capacity constraints");
		forall(range(nRacks), i -> sum(c[i], weightedBy(cardPowers), LE, rpw[i])).note("power-capacity constraints");
		forall(range(nTypes), i -> sum(columnOf(c, i), EQ, cardTypes[i][1])).note("demand constraints");

		block(() -> {
			decreasing(r);
			disjunction(ne(r[0], r[1]), ge(c[0][0], c[1][0]));
		}).tag(SYMMETRY_BREAKING);

		minimize(SUM, rpr);
	}
}
