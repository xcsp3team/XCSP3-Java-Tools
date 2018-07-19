/**
 * AbsCon - Copyright (c) 2017, CRIL-CNRS - lecoutre@cril.fr
 * 
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the CONTRAT DE LICENCE DE LOGICIEL LIBRE CeCILL which accompanies this
 * distribution, and is available at http://www.cecill.info
 */
package org.xcsp.modeler.problems;

import java.util.stream.IntStream;

import org.xcsp.common.IVar.Var;
import org.xcsp.modeler.ProblemAPI;

public class Queens implements ProblemAPI {
	int n; // number of queens

	@Override
	public void model() {
		Var[] q = array("q", size(n), dom(range(n)), "q[i] is the column where is put the ith queen (at row i)");

		if (isModel("m1")) {
			forall(range(n).range(n), (i, j) -> {
				if (i < j)
					intension(and(ne(q[i], q[j]), ne(dist(q[i], q[j]), dist(i, j))));
			});
		}
		if (isModel("m2")) {
			allDifferent(q);
			forall(range(n).range(n), (i, j) -> {
				if (i < j)
					different(dist(q[i], q[j]), dist(i, j));
			});
		}
		if (isModel("m3")) {
			allDifferent(q);
			allDifferent(IntStream.range(0, n).mapToObj(i -> add(q[i], i)));
			allDifferent(IntStream.range(0, n).mapToObj(i -> sub(q[i], i)));
		}
	}
}
