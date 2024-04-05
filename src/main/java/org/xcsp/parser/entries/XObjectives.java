/*
 * Copyright (c) 2016 XCSP3 Team (contact@xcsp.org)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.xcsp.parser.entries;

import java.util.LinkedHashSet;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.Utilities;
import org.xcsp.common.predicates.XNode;
import org.xcsp.parser.entries.ParsingEntry.OEntry;
import org.xcsp.parser.entries.XVariables.XVar;

/**
 * @author Christophe Lecoutre
 */
public class XObjectives {

	/** Intermediate class introduced only for clarity reasons. */
	public static abstract class XObj extends OEntry {
		public XObj(boolean minimize, TypeObjective type) {
			super(minimize, type);
		}
	}

	/** The class for representing objectives defined from functional expressions (can just be a variable). */
	public static final class OObjectiveExpr extends XObj {
		public final XNode<? extends XVar> rootNode;

		/** Builds an objective from the specified functional expression (given by the root of a syntactic tree). */
		public OObjectiveExpr(boolean minimize, TypeObjective type, XNode<? extends XVar> rootNode) {
			super(minimize, type);
			this.rootNode = rootNode;
		}

		@Override
		public XVar[] vars() {
			return rootNode.vars();
		}

		@Override
		public String toString() {
			return super.toString() + " " + rootNode.toString();
		}
	}

	/** The class for representing objectives defined from a list of variables, and possibly a list of coefficients. */
	public static final class OObjectiveSpecial extends XObj {
		/** The list of variables or trees of the objective. */
		public final Object[] terms;

		/** The list of coefficients. Either this field is null, or there are as many coefficients as variables. */
		public final Object[] coeffs;

		/** Builds an objective from the specified arrays of variables and coefficients. */
		public OObjectiveSpecial(boolean minimize, TypeObjective type, Object[] terms, Object[] coeffs) {
			super(minimize, type);
			this.terms = terms;
			this.coeffs = coeffs;
			// TODO adding a control on vars
		}

		@Override
		public XVar[] vars() {
			// if (terms[0] instanceof IVar)
			// return (XVar[]) terms;
			LinkedHashSet<IVar> set = new LinkedHashSet<>();
			for (Object term : terms) {
				if (term instanceof IVar)
					set.add((IVar) term);
				else {
					assert term instanceof XNode;
					((XNode<?>) term).listOfVars().stream().forEach(x -> set.add(x));
				}
			}
			if (coeffs != null)
				for (Object coeff : coeffs) {
					if (coeff instanceof IVar)
						set.add((IVar) coeff);
					else if (coeff instanceof XNode)
						((XNode<?>) coeff).listOfVars().stream().forEach(x -> set.add(x));
					else
						assert coeff instanceof Long;
				}
			return set.size() == 0 ? null : set.stream().toArray(s -> Utilities.buildArray(set.iterator().next().getClass(), s));
		}

		@Override
		public String toString() {
			return super.toString() + "\n\tTerms: " + Utilities.join(terms) + (coeffs != null ? "\n\tCoeffs: " + Utilities.join(coeffs) : "");
		}
	}
}
