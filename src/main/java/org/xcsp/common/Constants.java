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
package org.xcsp.common;

import java.math.BigInteger;

/**
 * @author Christophe Lecoutre
 */
public interface Constants {
	String EMPTY_STRING = "";

	String REG_WS = "\\s+";
	String WHITE_SPACE = " \t\n\r\f";

	// For each primitive type, we can safely use all values except the extreme ones (as defined by SAFETY_MARGIN)
	// so as to be able to use special values (for example, for representing +infinity and -infinity)
	int SAFETY_MARGIN = 10;
	long MIN_SAFE_BYTE = Byte.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_BYTE = Byte.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_SHORT = Short.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_SHORT = Short.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_INT = Integer.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_INT = Integer.MAX_VALUE - SAFETY_MARGIN;
	long MIN_SAFE_LONG = Long.MIN_VALUE + SAFETY_MARGIN, MAX_SAFE_LONG = Long.MAX_VALUE - SAFETY_MARGIN;
	BigInteger BIG_MIN_SAFE_LONG = BigInteger.valueOf(MIN_SAFE_LONG), BIG_MAX_SAFE_LONG = BigInteger.valueOf(MAX_SAFE_LONG);

	String MINUS_INFINITY = "-infinity";
	String PLUS_INFINITY = "+infinity";
	long VAL_MINUS_INFINITY = Long.MIN_VALUE;
	long VAL_PLUS_INFINITY = Long.MAX_VALUE;
	int VAL_MINUS_INFINITY_INT = Integer.MIN_VALUE;
	int VAL_PLUS_INFINITY_INT = Integer.MAX_VALUE;

	// We use the maximum value of each primitive type, minus 1, to denote STAR (related to the concept of short tuples)
	String STAR_SYMBOL = "*";

	byte STAR_BYTE = Byte.MAX_VALUE - 1;
	short STAR_SHORT = Short.MAX_VALUE - 1;
	int STAR_INT = Integer.MAX_VALUE - 1;
	long STAR_LONG = Long.MAX_VALUE - 1;
	int STAR = STAR_INT;

	/** We use the minimum long value, plus 1, to denote that a value is outside bounds (e.g., of a domain) */
	long OUTSIDE_BOUNDS = Long.MIN_VALUE + 1;

	// Constants used for some first-level elements of the instances
	String INSTANCE = "instance";
	String VARIABLES = "variables";
	String VAR = "var";
	String ARRAY = "array";
	String DOMAIN = "domain";
	String REQUIRED = "required";
	String POSSIBLE = "possible";
	String CONSTRAINTS = "constraints";
	String BLOCK = "block";
	String GROUP = "group";
	String ARGS = "args";
	String OBJECTIVES = "objectives";
	String OBJECTIVE = "objective";
	String MINIMIZE = "minimize";
	String MAXIMIZE = "maximize";
	String SOFT = "soft";
	String ANNOTATIONS = "annotations";
	String DECISION = "decision";

	/** A regex for denoting delimiters used in lists (elements separated by commas and surrounded by parentheses) */
	String DELIMITER_LISTS = "\\s*\\)\\s*\\(\\s*|\\s*\\(\\s*|\\s*\\)\\s*";

	/** A regex for denoting delimiters used in sets (elements separated by a comma and surrounded by brace brackets) */
	String DELIMITER_SETS = "\\s*\\}\\s*\\{\\s*|\\s*\\{\\s*|\\s*\\}\\s*";

	/** A regex for denoting delimiters used in msets (elements separated by a comma and surrounded by double brace brackets) */
	String DELIMITER_MSETS = "\\s*\\}\\}\\s*\\{\\{\\s*|\\s*\\{\\{\\s*|\\s*\\}\\}\\s*";

	String[] KEYWORDS = { "neg", "abs", "add", "sub", "mul", "div", "mod", "sqr", "pow", "min", "max", "dist", "lt", "le", "ge", "gt", "ne", "eq", "set", "in", "not", "and", "or", "xor", "iff", "imp", "if", "card", "union", "inter", "diff", "sdiff", "hull", "djoint", "subset", "subseq", "supseq", "supset", "convex", "PI", "E", "fdiv", "fmod", "sqrt", "nroot", "exp", "ln", "log", "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "others" };
}
