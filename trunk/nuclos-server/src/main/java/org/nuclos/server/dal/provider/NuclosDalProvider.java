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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicMetaDataProcessor;
import org.nuclos.server.dal.processor.json.impl.EntityObjectProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclos.JsonEntityObjectProcessor;

/**
 * @deprecated Replace with pure Spring solution.
 */
public class NuclosDalProvider extends AbstractDalProvider {
	
	/**
	 * Singleton der auch in einer MultiThreading-Umgebung Threadsafe ist...
	 */
	private static final NuclosDalProvider singleton = new NuclosDalProvider();
	
	// instance variables
	
	protected final Map<String, JsonEntityObjectProcessor> mapEntityObject = new ConcurrentHashMap<String, JsonEntityObjectProcessor>();
	
	protected JsonEntityMetaDataProcessor entityMetaDataProcessor;
	protected JsonEntityFieldMetaDataProcessor entityFieldMetaDataProcessor;
	private DynamicMetaDataProcessor dynMetaDataProcessor;
	
	public static NuclosDalProvider getInstance() {
		return singleton;
	}	
	
	private NuclosDalProvider(){
	}
	
	/**
	 * Spring property.
	 */
	public void setEntityMetaDataProcessor(JsonEntityMetaDataProcessor processor) {
		this.entityMetaDataProcessor = processor;
	}
	
	/**
	 * Spring property.
	 */
	public void setEntityFieldMetaDataProcessor(JsonEntityFieldMetaDataProcessor processor) {
		this.entityFieldMetaDataProcessor = processor;
	}
	
	/**
	 * Spring property.
	 */
	public void setDynamicMetaDataProcessor(DynamicMetaDataProcessor processor) {
		this.dynMetaDataProcessor = processor;
	}	
	
	public void buildEOProcessors() {
		try {
			final Class<? extends JsonEntityObjectProcessor> clazz = EntityObjectProcessor.class;
			final Constructor<? extends JsonEntityObjectProcessor> constr = clazz.getConstructor(EntityMetaDataVO.class, Collection.class);
			for (EntityMetaDataVO eMeta : entityMetaDataProcessor.getAll()) {
				mapEntityObject.put(
						eMeta.getEntity(), constr.newInstance(eMeta, entityFieldMetaDataProcessor.getByParent(eMeta.getEntity())));
			}
		} catch (Exception ex) {
			throw new CommonFatalException(ex);
		}
	}
	
	public JsonEntityMetaDataProcessor getEntityMetaDataProcessor() {
		return entityMetaDataProcessor;
	}
	
	public JsonEntityFieldMetaDataProcessor getEntityFieldMetaDataProcessor() {
		return entityFieldMetaDataProcessor;
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
		if(dynMetaDataProcessor == null)
			dynMetaDataProcessor = new DynamicMetaDataProcessor();
	    return dynMetaDataProcessor;
    }
	
	public DynamicFieldMetaDataProcessor getDynamicFieldMetaDataProcessor(EntityMetaDataVO entity) {
		return new DynamicFieldMetaDataProcessor(entity);
	}
}
