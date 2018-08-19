package org.xcsp.modeler.definitions;

import static org.xcsp.modeler.definitions.IRootForCtrAndObj.map;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;

import org.xcsp.common.IVar;
import org.xcsp.common.Types.TypeObjective;
import org.xcsp.common.predicates.XNode;

public interface IObj extends IRootForCtrAndObj {

	String MINIMIZE = "minimize";
	String MAXIMIZE = "maximize";
	String FUNCTION = ICtr.FUNCTION;
	String TYPE = "type";
	String LIST = ICtr.LIST;
	String COEFFS = ICtr.COEFFS;

	public interface IObjFunctional extends IObj {

		public static IObjFunctional buildFrom(IVar[] scope, boolean minimize, XNode<IVar> tree) {
			return new IObjFunctional() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, MINIMIZE, minimize, FUNCTION, tree);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			Map<String, Object> map = mapXCSP();
			DefXCSP def = def((Boolean) map.get(MINIMIZE) ? MINIMIZE : MAXIMIZE);
			return def.add(FUNCTION);
		}
	}

	public interface IObjSpecialized extends IObj {

		public static IObjSpecialized buildFrom(IVar[] scope, boolean minimize, TypeObjective type, String list, String coeffs) {
			return new IObjSpecialized() {
				@Override
				public Map<String, Object> mapXCSP() {
					return map(SCOPE, scope, MINIMIZE, minimize, TYPE, type, LIST, list, COEFFS, coeffs);
				}
			};
		}

		@Override
		default DefXCSP defXCSP() {
			Map<String, Object> map = mapXCSP();
			DefXCSP def = def((Boolean) map.get(MINIMIZE) ? MINIMIZE : MAXIMIZE);
			if (map.containsKey(TYPE)) {
				String t = ((TypeObjective) map.get(TYPE)).name().toLowerCase();
				def.attributes.add(new SimpleEntry<>(TYPE, t.equals("nvalues") ? "nValues" : t));
			}
			return def.add(LIST, COEFFS);
		}
	}
}
