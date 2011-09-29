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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.ProcessorFactorySingleton;
import org.nuclos.server.dal.processor.nuclet.IEOGenericObjectProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityFieldMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityMetaDataProcessor;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.database.DataBaseHelper;

/**
 * @deprecated Replace with pure Spring solution.
 */
public class NucletDalProvider extends AbstractDalProvider {
	
	/**
	 * Singleton der auch in einer MultiThreading-Umgebung Threadsafe ist...
	 */
	private static final NucletDalProvider singleton = new NucletDalProvider();
	
	
	// instance variables
	
	private final Map<String, JdbcEntityObjectProcessor> mapEntityObject = new HashMap<String, JdbcEntityObjectProcessor>();
	
	private JdbcEntityMetaDataProcessor entityMetaDataProcessor;
	private JdbcEntityFieldMetaDataProcessor entityFieldMetaDataProcessor;
	private IEOGenericObjectProcessor eoGenericObjectProcessor;
	
	public static NucletDalProvider getInstance() {
		return singleton;
	}	
	
	private NucletDalProvider(){
	}
	
	/**
	 * Spring property.
	 */
	public void setEntityMetaDataProcessor(JdbcEntityMetaDataProcessor processor) {
		this.entityMetaDataProcessor = processor;
	}
	
	/**
	 * Spring property.
	 */
	public void setEntityFieldMetaDataProcessor(JdbcEntityFieldMetaDataProcessor processor) {
		this.entityFieldMetaDataProcessor = processor;
	}
	
	/**
	 * Spring property.
	 */
	public void setGenericObjectProcessor(IEOGenericObjectProcessor processor) {
		this.eoGenericObjectProcessor = processor;
	}	
	
	public void buildEOProcessors() {
		synchronized (mapEntityObject) {
			mapEntityObject.clear();
			
			final ProcessorFactorySingleton processorFac = ProcessorFactorySingleton.getInstance();
			try {
				// Constructor<?> entityObjectProcessorConstructor = Class.forName(getDalProperties().getProperty("entity.object.nuclet")).getConstructor(EntityMetaDataVO.class, Collection.class);
				
				/**
				 * Konfigurierte Entitäten
				 */
				for (EntityMetaDataVO eMeta : entityMetaDataProcessor.getAll()) {
					List<EntityFieldMetaDataVO> entityFields = entityFieldMetaDataProcessor.getByParent(eMeta.getEntity());
					DalUtils.addNucletEOSystemFields(entityFields, eMeta);
					
					mapEntityObject.put(eMeta.getEntity(), processorFac.newEntityObjectProcessor(eMeta, entityFields, true));
				}
				
				/**
				 * Systementitäten
				 */
				for (EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getEntityMetaDataProcessor().getAll()) {
					List<EntityFieldMetaDataVO> entityFields = NuclosDalProvider.getInstance().getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());
					DalUtils.addNucletEOSystemFields(entityFields, eMeta);
					
					mapEntityObject.put(eMeta.getEntity(), (JdbcEntityObjectProcessor) processorFac.newEntityObjectProcessor(eMeta, entityFields, true));
				}
				
				/**
				 * 
				 */
				for(EntityMetaDataVO eMeta : NuclosDalProvider.getInstance().getDynamicEntityMetaProcessor().getAll()) {
					List<EntityFieldMetaDataVO> entityFields = NuclosDalProvider.getInstance().getDynamicFieldMetaDataProcessor(eMeta).getAll();
					
					mapEntityObject.put(eMeta.getEntity(), processorFac.newDynamicEntityObjectProcessor(eMeta, entityFields));
				}
	
			} catch (Exception ex) {
				throw new CommonFatalException(ex);
			}
		}
	}
	
	public JdbcEntityMetaDataProcessor getEntityMetaDataProcessor() {
		return entityMetaDataProcessor;
	}
	
	public JdbcEntityFieldMetaDataProcessor getEntityFieldMetaDataProcessor() {
		return entityFieldMetaDataProcessor;
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
		return eoGenericObjectProcessor;
	}

	public void revalidate() {
		buildEOProcessors();
	}
}
