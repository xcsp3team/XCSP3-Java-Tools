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
package org.xcsp.parser;

import org.xcsp.common.XEnums.TypeObjective;
import org.xcsp.common.XUtility;
import org.xcsp.common.predicates.XNode;
import org.xcsp.parser.XParser.AnyEntry;
import org.xcsp.parser.XValues.SimpleValue;
import org.xcsp.parser.XVariables.XVar;

/**
 * @author Christophe Lecoutre
 */
public class XObjectives {

	/** The root class for representing objectives. */
	public static abstract class OEntry extends AnyEntry {

		/** Indicates whether the objective must be minimized or maximized. */
		public final boolean minimize;

		/** The type (expression, sum, minimum, ...) of the objective. */
		public final TypeObjective type;

		/** Returns The type (expression, sum, minimum, ...) of the objective. We need an accessor for Scala. */
		public final TypeObjective getType() {
			return type;
		}

		/** Builds an objective with the specified minimize value and type. */
		public OEntry(boolean minimize, TypeObjective type) {
			this.minimize = minimize;
			this.type = type;
		}

		@Override
		public String toString() {
			return id + " " + (minimize ? "minimize" : "maximize") + " " + type;
		}
	}

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
		public String toString() {
			return super.toString() + " " + rootNode.toString();
		}

	}

	/** The class for representing objectives defined from a list of variables, and possibly a list of coefficients. */
	public static final class OObjectiveSpecial extends XObj {
		/** The list of variables of the objective. */
		public final XVar[] vars;

		/** The list of coefficients. Either this field is null, or there are as many coefficients as variables. */
		public final SimpleValue[] coeffs;

		/** Builds an objective from the specified arrays of variables and coefficients. */
		public OObjectiveSpecial(boolean minimize, TypeObjective type, XVar[] vars, SimpleValue[] coeffs) {
			super(minimize, type);
			this.vars = vars;
			this.coeffs = coeffs;
		}

		@Override
		public String toString() {
			return super.toString() + "\n" + XUtility.join(vars) + (coeffs != null ? "\n" + XUtility.join(coeffs) : "");
		}
	}
}
