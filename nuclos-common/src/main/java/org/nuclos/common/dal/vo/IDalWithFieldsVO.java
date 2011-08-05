package org.nuclos.common.dal.vo;

import java.util.Map;

public interface IDalWithFieldsVO<T> extends IDalVO {

	boolean hasFields();
	
	void initFields(int maxFieldCount, int maxFieldIdCount);
	
	Map<String, T> getFields();
	
	Map<String, Long> getFieldIds();
	
	Long getFieldId(String fieldName);

	<S> S getField(String fieldName, Class<S> cls);
	
	/**
	 * @since Nuclos 3.1.01
	 */
	<S> S getField(String fieldName);
	
	/**
	 * Like {@link #getField(String)} but also includes system fields.
	 * 
	 * @since Nuclos 3.1.01
	 */
	<S> S getRealField(String fieldName, Class<S> cls);
	
	/**
	 * Like {@link #getField(String)} but also includes system fields.
	 * 
	 * @since Nuclos 3.1.01
	 */
	<S> S getRealField(String fieldName);
	
}
