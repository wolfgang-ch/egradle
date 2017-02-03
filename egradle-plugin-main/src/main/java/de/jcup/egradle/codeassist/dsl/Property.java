package de.jcup.egradle.codeassist.dsl;

/**
 * Represents a property or a variable or a field or...
 * @author Albert Tregnaghi
 *
 */
public interface Property extends LanguageElement, TypeChild{

	/**
	 * Returns type of property itself
	 * @return type of property
	 */
	public Type getType();
	
	public String getTypeAsString();
	
}
