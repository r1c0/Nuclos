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
package org.nuclos.server.dal.provider;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityObjectProcessor;

public class NuclosDalProvider extends AbstractDalProvider {
	
	/**
	 * Singleton der auch in einer MultiThreading-Umgebung Threadsafe ist...
	 */
	private static NuclosDalProvider singleton = new NuclosDalProvider();
	
	public static NuclosDalProvider getInstance() {
		return singleton;
	}	
	
	protected JsonEntityMetaDataProcessor entityMetaData;
	protected JsonEntityFieldMetaDataProcessor entityFieldMetaData;
	protected final Map<String, JsonEntityObjectProcessor> mapEntityObject = new ConcurrentHashMap<String, JsonEntityObjectProcessor>();
	private DynamicMetaDataProcessor dynMetaDataProc;
	
	private NuclosDalProvider(){
		Properties dalProperties = getDalProperties();
		
		try {
			entityMetaData = (JsonEntityMetaDataProcessor) Class.forName(dalProperties.getProperty("entity.meta.data.nuclos")).newInstance();
			entityFieldMetaData = (JsonEntityFieldMetaDataProcessor) Class.forName(dalProperties.getProperty("entity.field.meta.data.nuclos")).newInstance();
			
			for (EntityMetaDataVO eMeta : entityMetaData.getAll()) {
				mapEntityObject.put(eMeta.getEntity(), 
					(JsonEntityObjectProcessor) Class.forName(dalProperties.getProperty("entity.object.nuclos")).getConstructor(
						EntityMetaDataVO.class, 	Collection.class).newInstance(
						eMeta, 							entityFieldMetaData.getByParent(eMeta.getEntity())));
			}
		} catch (Exception ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public JsonEntityMetaDataProcessor getEntityMetaDataProcessor() {
		return entityMetaData;
	}
	
	public JsonEntityFieldMetaDataProcessor getEntityFieldMetaDataProcessor() {
		return entityFieldMetaData;
	}
	
	public JsonEntityObjectProcessor getEntityObjectProcessor(NuclosEntity entity) {
		return this.getEntityObjectProcessor(entity.getEntityName());
	}
	
	public JsonEntityObjectProcessor getEntityObjectProcessor(String entity) {
		JsonEntityObjectProcessor proc = mapEntityObject.get(entity);
		if (proc == null) {
			throw new CommonFatalException("No processor for entity " + entity + " registered.");
		}
		return proc;
	}

	public DynamicMetaDataProcessor getDynamicEntityMetaProcessor() {
		if(dynMetaDataProc == null)
			dynMetaDataProc = new DynamicMetaDataProcessor();
	    return dynMetaDataProc;
    }
	
	public DynamicFieldMetaDataProcessor getDynamicFieldMetaDataProcessor(EntityMetaDataVO entity) {
		return new DynamicFieldMetaDataProcessor(entity);
	}
}
