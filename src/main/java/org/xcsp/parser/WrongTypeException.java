package org.xcsp.parser;

/**
 * An exception thrown when a constraint parameter has not the expected type. 
 * 
 * This exception was created after the first version of the parser was written.
 * It extends {@link RuntimeException} to minimize the changes to be made elsewhere in the code; a refactoring may be useful here. 
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class WrongTypeException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new {@link WrongTypeException} given the reason it was thrown.
	 * 
	 * @param reason the reason
	 */
	public WrongTypeException(final String reason) {
		super(reason);
	}

}
