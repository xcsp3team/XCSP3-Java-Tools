package org.xcsp.parser.loaders;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import org.xcsp.common.Types.TypeChild;
import org.xcsp.common.predicates.XNodeParent;
import org.xcsp.parser.callbacks.XCallbacks;
import org.xcsp.parser.entries.XConstraints.CChild;
import org.xcsp.parser.entries.XConstraints.XCtr;
import org.xcsp.parser.entries.XVariables.XVarSymbolic;

public class CtrLoaderSymbolic {
	private XCallbacks xc;

	public CtrLoaderSymbolic(XCallbacks xc) {
		this.xc = xc;
	}

	public void load(XCtr c) {
		switch (c.getType()) {
		case intension:
			intension(c);
			break;
		case extension:
			extension(c);
			break;
		case allDifferent:
			allDifferent(c);
			break;
		default:
			xc.unimplementedCase(c);
		}
	}

	private void intension(XCtr c) {
		xc.buildCtrIntension(c.id, Stream.of(c.vars()).toArray(XVarSymbolic[]::new), (XNodeParent<XVarSymbolic>) c.childs[0].value);
	}

	private void extension(XCtr c) {
		CChild c1 = c.childs[1];
		boolean positive = c1.type == TypeChild.supports;
		if (c1.value == null || Array.getLength(c1.value) == 0) { // 0 tuple
			if (positive)
				xc.buildCtrFalse(c.id, c.vars());
			else
				xc.buildCtrTrue(c.id, c.vars());
		} else {
			XVarSymbolic[] list = (XVarSymbolic[]) c.childs[0].value;
			if (list.length == 1) // unary constraint
				xc.buildCtrExtension(c.id, list[0], (String[]) c1.value, positive, c1.flags);
			else
				xc.buildCtrExtension(c.id, list, (String[][]) c1.value, positive, c1.flags);
		}
	}

	private void allDifferent(XCtr c) {
		if (c.childs.length == 1 && c.childs[0].type == TypeChild.list)
			xc.buildCtrAllDifferent(c.id, (XVarSymbolic[]) (c.childs[0].value));
		else
			xc.unimplementedCase(c);
	}

}