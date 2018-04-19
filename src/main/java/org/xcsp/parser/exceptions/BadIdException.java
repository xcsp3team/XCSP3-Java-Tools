package org.xcsp.parser.exceptions;

/**
 * An exception thrown when more than one elements declare the same id.
 * 
 * This exception was created after the first version of the parser was written. It extends {@link RuntimeException} to minimize the changes to be
 * made elsewhere in the code; a refactoring may be useful here.
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class BadIdException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Builds a new {@link BadIdException}, indicating that the specified id is invalid.
	 * 
	 * @param id
	 *            an id
	 */
	public BadIdException(final String id) {
		super("Bad form of id : " + id);
	}

}
