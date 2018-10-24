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

public class Bibd implements ProblemAPI {

	int v, b, r, k, l; // l stands for lambda

	@Override
	public void model() {
		b = b != 0 ? b : (l * v * (v - 1)) / (k * (k - 1)); // when b is 0, we compute it
		r = r != 0 ? r : (l * (v - 1)) / (k - 1); // when r is 0, we compute it

		Var[][] x = array("x", size(v, b), dom(0, 1), "x[i][j] is the value of the matrix at row i and col j");

		forall(range(v), i -> sum(x[i], EQ, r));
		forall(range(b), j -> sum(columnOf(x, j), EQ, k));
		forall(range(v).range(v), (i, j) -> {
			if (i < j)
				sum(x[i], weightedBy(x[j]), EQ, l);
		});
		lexMatrix(x, INCREASING).tag(SYMMETRY_BREAKING);
	}
}
