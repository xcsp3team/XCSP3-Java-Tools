package org.xcsp.common;

public interface Interfaces {
	interface IVar {
		String id();
	};

	interface IVarInteger extends IVar {
	};

	interface IVarSymbolic extends IVar {
	};
}
