package com.github.sabomichal.springinjector;

import java.lang.reflect.Field;

/**
 * Factory object used by injector to generate values for fields of the object being injected.
 *
 * @author Igor Vaynberg (ivaynberg)
 *
 */
public interface IFieldValueFactory
{
	/**
	 * Returns the value the field will be set to
	 *
	 * @param field
	 *            field being injected
	 * @return new field value
	 */
	Object getFieldValue(Field field);

	/**
	 * Returns true if the factory can generate a value for the field, false otherwise.
	 *
	 * If this method returns false, getFieldValue() will not be called on this factory
	 *
	 * @param field
	 *            field
	 * @return true if the factory can generate a value for the field, false otherwise
	 */
	boolean supportsField(Field field);
}
