//Copyright (C) 2010  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dal.processor;

import java.util.Date;

import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;

public abstract class AbstractDalProcessor<DalVO extends IDalVO> {

	private final Class<DalVO> dalVOClzz;

	/**
	 * Data Types
	 */
	public final static Class<String>	        DT_STRING	         = java.lang.String.class;
	public final static Class<Integer>	        DT_INTEGER	         = java.lang.Integer.class;
	public final static Class<Long>	            DT_LONG	             = java.lang.Long.class;
	public final static Class<Date>	            DT_DATE	             = java.util.Date.class;
	public final static Class<InternalTimestamp>	DT_INTERNALTIMESTAMP = org.nuclos.common2.InternalTimestamp.class;
	public final static Class<Boolean>	        DT_BOOLEAN	         = java.lang.Boolean.class;

	protected AbstractDalProcessor(Class<DalVO> type) {
		this.dalVOClzz = type;
	}

	public final String getProcessor() {
		Class<?>[] interfaces = getClass().getInterfaces();
		if (interfaces.length == 0)
			return "<[none]>";
		return interfaces[0].getName();
	}
	
	public final Class<DalVO> getDalType() {
		return dalVOClzz;
	}

	protected DalVO newDalVOInstance() {
		try {
			DalVO newInstance = getDalType().newInstance();
			return newInstance;
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 * @deprecated Complete misuse of java generics.
	public Class<DalVO> getDalVOClass() {
		if (dalVOClzz == null) {
			Type genericSuperClass = getClass().getGenericSuperclass();
			while (!(genericSuperClass instanceof ParameterizedType) && genericSuperClass instanceof Class<?>) {
				genericSuperClass = ((Class<?>)genericSuperClass).getGenericSuperclass();
			}
			Type actualType = ((ParameterizedType)genericSuperClass).getActualTypeArguments()[0];
			if(actualType instanceof ParameterizedType) {
				dalVOClzz = (Class<?>) ((ParameterizedType) actualType).getRawType();
			}
			else {
				dalVOClzz = (Class<?>) actualType;
			}
		}
		return (Class<DalVO>) dalVOClzz;
	}
	 */

}
