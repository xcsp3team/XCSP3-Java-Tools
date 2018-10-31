package org.xcsp.common;

/**
 * This is the root interface of any variable, handled either in the parser or the modeler.
 */
public interface IVar {
	/**
	 * Returns the id (unique name) of the variable.
	 * 
	 * @return the id (unique name) of the variable
	 */
	String id();

	default String idPrefix() {
		String id = id();
		int pos = id.indexOf('[');
		return pos == -1 ? id : id.substring(0, pos);
	}

	/**
	 * This is the root interface of any integer variable, handled either in the parser or the modeler. One could have expected that the name be
	 * {@code IVarInteger}, but this would be annoying when modeling.
	 */
	interface Var extends IVar {
	}

	/**
	 * This is the root interface of any symbolic variable, handled either in the parser or the modeler. One could have expected that the name is
	 * {@code IVarSymbolic}, but we prefer to keep simple names for modeling.
	 */
	interface VarSymbolic extends IVar {
	}

}