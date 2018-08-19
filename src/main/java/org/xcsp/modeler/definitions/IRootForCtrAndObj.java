package org.xcsp.modeler.definitions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.xcsp.common.IVar;
import org.xcsp.common.Utilities;

public interface IRootForCtrAndObj {
	String SCOPE = "scope";

	static Map<String, Object> map(String[] keys, Object... values) {
		Utilities.control(keys.length == values.length && keys.length > 0, "Bad form");
		Map<String, Object> map = new LinkedHashMap<>();
		IntStream.range(0, keys.length).filter(i -> values[i] != null).forEach(i -> map.put(keys[i], values[i]));
		return map;
	}

	static Map<String, Object> map(String s, Object o) {
		return map(new String[] { s }, o);
	}

	static Map<String, Object> map(String s1, Object o1, String s2, Object o2) {
		return map(new String[] { s1, s2 }, o1, o2);
	}

	static Map<String, Object> map(String s1, Object o1, String s2, Object o2, String s3, Object o3) {
		return map(new String[] { s1, s2, s3 }, o1, o2, o3);
	}

	static Map<String, Object> map(String s1, Object o1, String s2, Object o2, String s3, Object o3, String s4, Object o4) {
		return map(new String[] { s1, s2, s3, s4 }, o1, o2, o3, o4);
	}

	static Map<String, Object> map(String s1, Object o1, String s2, Object o2, String s3, Object o3, String s4, Object o4, String s5, Object o5) {
		return map(new String[] { s1, s2, s3, s4, s5 }, o1, o2, o3, o4, o5);
	}

	static Map<String, Object> map(String s1, Object o1, String s2, Object o2, String s3, Object o3, String s4, Object o4, String s5, Object o5, String s6,
			Object o6) {
		return map(new String[] { s1, s2, s3, s4, s5, s6 }, o1, o2, o3, o4, o5, o6);
	}

	default Map<String, Object> mapXCSP() {
		return null;
	}

	default DefXCSP defXCSP() {
		return null;
	}

	default DefXCSP def(String name) {
		return new DefXCSP(name, mapXCSP());
	}

	default IVar[] scope() {
		return (IVar[]) mapXCSP().get(SCOPE);
	}
}
