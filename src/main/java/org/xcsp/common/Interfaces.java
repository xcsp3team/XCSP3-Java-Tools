package org.xcsp.common;

import org.xcsp.parser.entries.XDomains.XDomInteger;
import org.xcsp.parser.entries.XDomains.XDomSymbolic;

public interface Interfaces {

	@FunctionalInterface
	interface Intx2Predicate {
		boolean test(int i, int j);
	}

	@FunctionalInterface
	interface Intx3Predicate {
		boolean test(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4Predicate {
		boolean test(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5Predicate {
		boolean test(int i, int j, int k, int l, int m);
	}

	@FunctionalInterface
	interface IntToDomInteger {
		XDomInteger apply(int i);
	}

	@FunctionalInterface
	interface Intx2ToDomInteger {
		XDomInteger apply(int i, int j);
	}

	@FunctionalInterface
	interface Intx3ToDomInteger {
		XDomInteger apply(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4ToDomInteger {
		XDomInteger apply(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5ToDomInteger {
		XDomInteger apply(int i, int j, int k, int l, int m);
	}

	@FunctionalInterface
	interface IntToDomSymbolic {
		XDomSymbolic apply(int i);
	}

	@FunctionalInterface
	interface Intx2Function<R> {
		R apply(int i, int j);
	}

	@FunctionalInterface
	interface Intx3Function<R> {
		R apply(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4Function<R> {
		R apply(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5Function<R> {
		R apply(int i, int j, int k, int l, int m);
	}

	@FunctionalInterface
	interface Intx2Consumer {
		void accept(int i, int j);
	}

	@FunctionalInterface
	interface Intx3Consumer {
		void accept(int i, int j, int k);
	}

	@FunctionalInterface
	interface Intx4Consumer {
		void accept(int i, int j, int k, int l);
	}

	@FunctionalInterface
	interface Intx5Consumer {
		void accept(int i, int j, int k, int l, int m);
	}

	interface IVar {
		String id();
	};

	interface IVarInteger extends IVar {
	};

	interface IVarSymbolic extends IVar {
	};
}
