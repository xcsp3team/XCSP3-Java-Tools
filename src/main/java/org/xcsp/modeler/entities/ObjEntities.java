package org.xcsp.modeler.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.xcsp.common.Types.TypeClass;
import org.xcsp.modeler.definitions.IObj;

public class ObjEntities {

	public List<ObjEntity> allEntities = new ArrayList<>();

	public class ObjEntity extends ModelingEntity {
		public IObj obj;

		public ObjEntity(IObj obj, TypeClass... classes) {
			super(classes);
			this.obj = obj;
			allEntities.add(this);
		}
	}

	public boolean active() {
		return allEntities.size() > 0;
	}

	@Override
	public String toString() {
		return allEntities.stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.joining(" "));
	}
}
