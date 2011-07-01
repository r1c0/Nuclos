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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicEntityObjectProcessor;
import org.nuclos.server.dal.processor.nuclet.IEOGenericObjectProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;

public class NucletDalProvider extends AbstractDalProvider {
	
	/**
	 * Singleton der auch in einer MultiThreading-Umgebung Threadsafe ist...
	 */
	private static NucletDalProvider singleton = new NucletDalProvider();
	
	public static NucletDalProvider getInstance() {
		return singleton;
	}	
	
	private final JdbcEntityMetaDataProcessor entityMetaData;
	private final JdbcEntityFieldMetaDataProcessor entityFieldMetaData;
	private final Map<String, JdbcEntityObjectProcessor> mapEntityObject = new HashMap<String, JdbcEntityObjectProcessor>();
	private final IEOGenericObjectProcessor eoGenericObject;
	
	private NucletDalProvider(){
		Properties dalProperties = getDalProperties();
		
		try {
			entityMetaData = (JdbcEntityMetaDataProcessor) Class.forName(dalProperties.getProperty("entity.meta.data.nuclet")).newInstance();
			entityFieldMetaData = (JdbcEntityFieldMetaDataProcessor) Class.forName(dalProperties.getProperty("entity.field.meta.data.nuclet")).newInstance();
			eoGenericObject = (IEOGenericObjectProcessor) Class.forName(dalProperties.getProperty("eo.generic.object.nuclet")).newInstance();
		} catch (Exception ex) {
			throw new CommonFatalException(ex);
		}
		
		buildEOProcessors();
	}
	
	private void buildEOProcessors() {
		synchronized (mapEntityObject) {
			mapEntityObject.clear();
			
			try {
				Constructor<?> entityObjectProcessorConstructor = Class.forName(getDalProperties().getProperty("entity.object.nuclet")).getConstructor(EntityMetaDataVO.class, Collection.class);
				
				/**
				 * Konfigurierte Entit\u00e4ten
				 */
				for (EntityMetaDataVO eMeta : entityMetaData.getAll()) {
					List<EntityFieldMetaDataVO> entityFields = entityFieldMetaData.getByParent(eMeta.getEntity());
					DalUtils.addNucletEOSystemFields(entityFields, eMeta);
					
					mapEntityObject.put(eMeta.getEntity(), (JdbcEntityObjectProcessor) entityObjectProcessorConstructor.newInstance(eMeta, entityFields));
				}
				
				/**
				 * Systementit\u00e4ten
				 */
				for (EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
					List<EntityFieldMetaDataVO> entityFields = NuclosDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());
					DalUtils.addNucletEOSystemFields(entityFields, eMeta);
					
					mapEntityObject.put(eMeta.getEntity(), (JdbcEntityObjectProcessor) entityObjectProcessorConstructor.newInstance(eMeta, entityFields));
				}
				
				/**
				 * 
				 */
				for(EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getDynamicEntityMetaProcessor().getAll()) {
					List<EntityFieldMetaDataVO> entityFields = NuclosDalProvider.getInstance().getDynamicFieldMetaDataProcessor(eMeta).getAll();
					
					mapEntityObject.put(eMeta.getEntity(), new DynamicEntityObjectProcessor(eMeta, entityFields));
				}
	
			} catch (Exception ex) {
				throw new CommonFatalException(ex);
			}
		}
	}
	
	public JdbcEntityMetaDataProcessor getEntityMetaDataProcessor() {
		return entityMetaData;
	}
	
	public JdbcEntityFieldMetaDataProcessor getEntityFieldMetaDataProcessor() {
		return entityFieldMetaData;
	}
	
	public JdbcEntityObjectProcessor getEntityObjectProcessor(NuclosEntity entity) {
		return this.getEntityObjectProcessor(entity.getEntityName());
	}
	
	public JdbcEntityObjectProcessor getEntityObjectProcessor(String entity) {
		JdbcEntityObjectProcessor proc;
		synchronized (mapEntityObject) {
			proc = mapEntityObject.get(entity);
		}
		if (proc == null) {
			throw new CommonFatalException("No processor for entity " + entity + " registered.");
		}
		return proc;
	}
	
	public IEOGenericObjectProcessor getEOGenericObjectProcessor() {
		return eoGenericObject;
	}

	public void revalidate() {
		buildEOProcessors();
	}
}
