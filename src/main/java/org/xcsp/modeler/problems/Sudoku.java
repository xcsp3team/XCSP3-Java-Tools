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

public class Sudoku implements ProblemAPI {

	int n; // order of the grid (typically, 9)
	int[][] clues; // if not 0, clues[i][j] is a value imposed at row i and col j

	@Override
	public void model() {
		int base = (int) Math.sqrt(n);

		Var[][] x = array("x", size(n, n), dom(range(1, n)));

		allDifferentMatrix(x);
		forall(range(0, n - 1, base).range(0, n - 1, base), (i, j) -> allDifferent(select(x, range(i, i + base - 1).range(j, j + base - 1)))).tag(BLOCKS);

		if (clues != null)
			instantiation(x, clues, (i, j) -> clues[i][j] != 0).tag(CLUES);
	}
}
