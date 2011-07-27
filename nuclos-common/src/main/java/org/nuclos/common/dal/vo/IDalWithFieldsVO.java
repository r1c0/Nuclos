package org.nuclos.common.dal.vo;

import java.util.Map;

public interface IDalWithFieldsVO<T> extends IDalVO {

	boolean hasFields();
	
	void initFields(int maxFieldCount, int maxFieldIdCount);
	
	Map<String, T> getFields();
	
	Map<String, Long> getFieldIds();
	
}
