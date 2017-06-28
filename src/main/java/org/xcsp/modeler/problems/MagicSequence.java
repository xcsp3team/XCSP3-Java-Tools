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

public class MagicSequence implements ProblemAPI {
	int n;

	@Override
	public void model() {
		Var[] x = array("x", size(n), dom(range(n)));

		cardinality(x, range(n), occurrences(x));

		block(() -> {
			sum(x, EQ, n);
			sum(x, range(-1, n - 2), EQ, 0);
		}).tag(REDUNDANT_CONSTRAINTS);
	}
}
