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
package org.nuclos.server.dal;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.NuclosPassword;
import org.nuclos.common.dal.vo.AbstractDalVOWithVersion;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.server.dal.specification.IDalVersionSpecification;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.resource.valueobject.ResourceFile;

public class DalUtils {
	
	public static List<Integer> convertLongIdList(List<Long> listIds) {
		if (listIds == null) {
			return null;
		}
		List<Integer> result = new ArrayList<Integer>(listIds.size());
		for (Long id : listIds) {
			result.add(LangUtils.convertId(id));
		}
		return result;
	}
	
	public static List<Long> convertIntegerIdList(List<Integer> listIds) {
		if (listIds == null) {
			return null;
		}
		List<Long> result = new ArrayList<Long>(listIds.size());
		for (Integer id : listIds) {
			result.add(LangUtils.convertId(id));
		}
		return result;
	}
	
	public static void addNucletEOSystemFields(List<EntityFieldMetaDataVO> entityFields, EntityMetaDataVO eMeta) {
		entityFields.add(NuclosEOField.CHANGEDAT.getMetaData());
		entityFields.add(NuclosEOField.CHANGEDBY.getMetaData());
		entityFields.add(NuclosEOField.CREATEDAT.getMetaData());
		entityFields.add(NuclosEOField.CREATEDBY.getMetaData());
		if (eMeta.isStateModel()) {
			entityFields.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData());
			entityFields.add(NuclosEOField.PROCESS.getMetaData());
			entityFields.add(NuclosEOField.ORIGIN.getMetaData());
			entityFields.add(NuclosEOField.LOGGICALDELETED.getMetaData());
			entityFields.add(NuclosEOField.STATE.getMetaData());
			entityFields.add(NuclosEOField.STATENUMBER.getMetaData());
		}
	}
	
	public static boolean isNucletEOSystemField(EntityFieldMetaDataVO voField) {
		for(NuclosEOField field : NuclosEOField.values()) {
			if(field.getMetaData().getField().equals(voField.getField()))
				return true;
		}
		return false;
	}
	
	public static Long getNextId() {
		return LangUtils.convertId(DataBaseHelper.getNextIdAsInteger(DataBaseHelper.DEFAULT_SEQUENCE));
	}
	
	public static void handleVersionUpdate(IDalVersionSpecification processor, 
		EntityObjectVO vo, String user) throws CommonStaleVersionException {
		
		if (vo.getId() != null) {
			final Integer oldVersion = processor.getVersion(vo.getId());
			if (!vo.getVersion().equals(oldVersion)) {
				throw new CommonStaleVersionException();
			}
		}
		
		updateVersionInformation(vo, user);
		vo.flagUpdate();
	}

	public static void updateVersionInformation(AbstractDalVOWithVersion vo, String user) {
		Date sysdate = new Date();
		if (vo.getCreatedBy() == null) {
			vo.setCreatedBy(user);
		}
		if (vo.getCreatedAt() == null) {
			vo.setCreatedAt(InternalTimestamp.toInternalTimestamp(sysdate));
		}
		if (vo.getVersion() == null) {
			vo.setVersion(1);
		} else {
			vo.setVersion(vo.getVersion()+1);
		}
		
		vo.setChangedBy(user);
		vo.setChangedAt(InternalTimestamp.toInternalTimestamp(sysdate));
	}

	public static boolean isNuclosProcessor(AbstractDalVOWithVersion dalVO) {
		return dalVO.processor() != null && isNuclosProcessor(dalVO.processor());
	}
	
	public static boolean isNuclosProcessor(String processor) {
		try {
			Class<?> processorClzz = Class.forName(processor);
			for (Class<?> processorInterface : processorClzz.getInterfaces()){
				if ("org.nuclos.server.dal.processor.nuclos".equals(processorInterface.getPackage().getName())){
					return true;
				}
			}
			return false;
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}
	}
	
	public static String getDbIdFieldName(String fieldName) {
		final String uFieldName = fieldName.toUpperCase();
		final int index_ = uFieldName.indexOf("_");
		if (index_ < 0) {
			return "INTID_"+uFieldName;
		}
		return "INTID"+uFieldName.substring(index_);
	}
	
	public static boolean isDbIdField(String fieldName) {
		return fieldName.startsWith("INTID_");
	}
	
   public static Class<?> getDbType(Class<?> javaType) {
      if (javaType == ByteArrayCarrier.class || javaType == Object.class || javaType == NuclosImage.class) {
         javaType = byte[].class;   // Column stores data (blob)
      } else if (javaType == ResourceFile.class || javaType == GenericObjectDocumentFile.class) {
      	return String.class;       // Column stores filename
      } else if (javaType == DateTime.class) {
      	return InternalTimestamp.class;
      } else if (javaType == NuclosPassword.class) {
      	return NuclosPassword.class;
      }
      return javaType;
   }
   
}
