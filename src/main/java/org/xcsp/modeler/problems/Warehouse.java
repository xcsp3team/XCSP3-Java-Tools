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

// Problem defined at http://csplib.org/Problems/prob034/
public class Warehouse implements ProblemAPI {
	int fixedCost;
	int[] warehouseCapacities;
	int[][] storeSupplyCosts;

	@Override
	public void model() {
		int nWarehouses = warehouseCapacities.length, nStores = storeSupplyCosts.length;

		Var[] s = array("s", size(nStores), dom(range(nWarehouses)), "s[i] is the warehouse supplier of the ith store");
		Var[] c = array("c", size(nStores), i -> dom(storeSupplyCosts[i]), "c[i] is the cost of supplying the ith store");
		Var[] o = array("o", size(nWarehouses), dom(0, 1), "o[i] is 1 if the ith warehouse is open");

		forall(range(nWarehouses), i -> atMost(s, takingValue(i), warehouseCapacities[i])).note("Capacities of warehouses must not be exceeded.");
		forall(range(nStores), i -> element(o, at(s[i]), takingValue(1))).note("The warehouse supplier of the ith store must be open.");
		forall(range(nStores), i -> element(storeSupplyCosts[i], at(s[i]), takingValue(c[i]))).note("Computing the cost of supplying the ith store.");

		int[] coeffs = vals(repeat(1, nStores), repeat(fixedCost, nWarehouses));
		minimize(SUM, vars(c, o), weightedBy(coeffs)).note("minimizing the overall cost");
	}
}
