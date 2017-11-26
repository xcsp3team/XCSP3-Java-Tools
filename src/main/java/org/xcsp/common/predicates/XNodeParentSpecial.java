package org.xcsp.common.predicates;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeExpr;

public final class XNodeParentSpecial<V extends IVar> extends XNodeParent<V> {

	public final String specialName;

	public XNodeParentSpecial(String specialName, XNode<V> son1, XNode<V> son2) {
		super(TypeExpr.SPECIAL, son1, son2);
		this.specialName = specialName;
	}

	public XNodeParentSpecial(String specialName, XNode<V> son) {
		super(TypeExpr.SPECIAL, son);
		this.specialName = specialName;
	}
}