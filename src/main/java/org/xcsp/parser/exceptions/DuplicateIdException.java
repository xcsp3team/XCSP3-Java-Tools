package org.xcsp.parser.exceptions;

/**
 * An exception thrown when more than one elements declare the same id.
 * 
 * This exception was created after the first version of the parser was written.
 * It extends {@link RuntimeException} to minimize the changes to be made elsewhere in the code; a refactoring may be useful here. 
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class DuplicateIdException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Build a new {@link DuplicateIdException} given the duplicated id.
	 * 
	 * @param id the id
	 */
	public DuplicateIdException(final String id) {
		super("more than one element is declared with the id \""+id+"\"");
	}

}
