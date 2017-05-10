package org.xcsp.parser.exceptions;

/**
 * An exception thrown when a reference to an unknown id is maid.
 * 
 * This exception was created after the first version of the parser was written.
 * It extends {@link RuntimeException} to minimize the changes to be made elsewhere in the code; a refactoring may be useful here. 
 * 
 * @author Emmanuel Lonca - lonca@cril.fr
 */
public class UnknownIdException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Builds a new {@link UnknownIdException} given the unknown id.
	 * 
	 * @param id the id
	 */
	public UnknownIdException(final String id) {
		super("unknown id: \""+id+"\"");
	}
	
	/**
	 * Builds a new {@link UnknownIdException} given the unknown id and where it is referenced.
	 * The generated exception message is of the form "unknown id WHERE: ID"
	 * 
	 * @param id the id
	 * @param where where the unknown id is referenced
	 */
	public UnknownIdException(final String id, final String where) {
		super("unknown id "+where+": \""+id+"\"");
	}

}
