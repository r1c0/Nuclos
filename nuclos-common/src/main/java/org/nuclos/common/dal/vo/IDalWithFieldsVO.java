//Copyright (C) 2011  Novabit Informationssysteme GmbH
//
//This file is part of Nuclos.
//
//Nuclos is free software: you can redistribute it and/or modify
//it under the terms of the GNU Affero General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//Nuclos is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Affero General Public License for more details.
//
//You should have received a copy of the GNU Affero General Public License
//along with Nuclos.  If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.common.dal.vo;

import java.util.Map;

/**
 * Base interface for all <em>data</em> value objects (VOs) transfered between client and server. 
 * <p>
 * On the server side, these VOs get persisted into the DB. These VOs represent the normal entities 
 * of a Nuclos instance, with their (custom) fields filled.
 * </p>
 * @since Nuclos 3.1.01
 * @author Thomas Pasch 
 */
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
