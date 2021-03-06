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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.nuclos.common.CommonMetaDataServerProvider;
import org.nuclos.common.EntityLafParameterVO;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.LafParameter;
import org.nuclos.common.LafParameterHashMap;
import org.nuclos.common.LafParameterMap;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.transport.GzipMap;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.processor.jdbc.impl.ChartMetaDataProcessor;
import org.nuclos.server.dal.processor.jdbc.impl.DynamicMetaDataProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.dal.provider.NuclosDalProvider;
import org.nuclos.server.database.SpringDataBaseHelper;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.genericobject.GenericObjectMetaDataCache;
import org.nuclos.server.genericobject.Modules;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.report.SchemaCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * An caching singleton for accessing the meta data information
 * on the server side.
 */
@Component("metaDataProvider") 
public class MetaDataServerProvider implements MetaDataProvider<EntityMetaDataVO, EntityFieldMetaDataVO>, CommonMetaDataServerProvider<EntityMetaDataVO, EntityFieldMetaDataVO> {
	
	private static final Logger LOG = Logger.getLogger(MetaDataServerProvider.class);
	
	private static MetaDataServerProvider INSTANCE;
	
	//

	private NucletDalProvider nucletDalProvider;
	
	private NuclosDalProvider nuclosDalProvider;
	
	private DynamicMetaDataProcessor dynamicMetaDataProcessor;
	
	private ChartMetaDataProcessor chartMetaDataProcessor;
	
	private DataCache dataCache;
	
	private SpringDataBaseHelper dataBaseHelper;
	
	private MetaDataServerProvider(){
		INSTANCE = this;
	}
	
	@PostConstruct
	final void init() {
		dataCache = new DataCache();
		dataCache.buildMaps();
	}
	
	@Autowired
	final void setNucletDalProvider(NucletDalProvider nucletDalProvider) {
		this.nucletDalProvider = nucletDalProvider;
	}
	
	@Autowired
	final void setDataBaseHelper(SpringDataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@Autowired
	final void setNuclosDalProvider(NuclosDalProvider nuclosDalProvider) {
		this.nuclosDalProvider = nuclosDalProvider;
	}
	
	@Autowired
	final void setDynamicMetaDataProcessor(DynamicMetaDataProcessor dynamicMetaDataProcessor) {
		this.dynamicMetaDataProcessor = dynamicMetaDataProcessor;
	}
	
	@Autowired
	final void setChartMetaDataProcessor(ChartMetaDataProcessor chartMetaDataProcessor) {
		this.chartMetaDataProcessor = chartMetaDataProcessor;
	}

	public static MetaDataServerProvider getInstance() {
		return INSTANCE;
	}

	@Override
    public Collection<EntityMetaDataVO> getAllEntities() {
		return  new ArrayList<EntityMetaDataVO>(dataCache.getMapMetaDataByEntity().values());
	}

	@Override
    public EntityMetaDataVO getEntity(Long id) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataById().get(id);
		if (result == null) {
			throw new CommonFatalException("entity with id " + id + " does not exists.");
		}
		return result;
	}

	@Override
    public EntityMetaDataVO getEntity(String entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity);
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	@Override
    public EntityMetaDataVO getEntity(NuclosEntity entity) {
		final EntityMetaDataVO result = dataCache.getMapMetaDataByEntity().get(entity.getEntityName());
		if (result == null) {
			throw new CommonFatalException("entity " + entity + " does not exists.");
		}
		return result;
	}

	@Override
    public Map<String, EntityFieldMetaDataVO> getAllEntityFieldsByEntity(String entity) {
		final Map<String, EntityFieldMetaDataVO> result = dataCache.getMapFieldMetaData().get(entity);
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	@Override
	public Map<String, EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info) {
		// According to the interface spec, we have to ignore value and value type.
		final PivotInfo cacheInfo = new PivotInfo(info.getSubform(), info.getKeyField(), null, null);

		final EntityMetaDataVO subform = getEntity(info.getSubform());
		final String subformTable = EntityObjectMetaDbHelper.getViewName(subform);
		final EntityFieldMetaDataVO keyField = getEntityField(info.getSubform(), info.getKeyField());
		// final EntityFieldMetaDataVO valueField = getEntityField(info.getSubform(), info.getValueField());

		Map<String, EntityFieldMetaDataVO> result = dataCache.getMapPivotMetaData().get(cacheInfo);
		if (result == null) {
			// get 'real' meta data
			final EntityFieldMetaDataVO mdKeyField = getEntityField(cacheInfo.getSubform(), cacheInfo.getKeyField());
			// The key column must contain a String.
			if (!mdKeyField.getDataType().equals("java.lang.String")) {
				result = Collections.emptyMap();
			}
			else {
				final String keyType = mdKeyField.getDataType();
				final Class<? extends Object> keyTypeClass;
				try {
					keyTypeClass = Class.forName(keyType);
				} catch (ClassNotFoundException e) {
					throw new CommonFatalException(e);
				}

				// select distinct p.<keyfield> from <subform> p
				final DbQuery<? extends Object> query = dataBaseHelper.getDbAccess().getQueryBuilder().createQuery(Object.class);
				final DbFrom from = query.distinct(true).from(subformTable).alias("p");
				query.selectLiberate(from.baseColumn(keyField.getDbColumn(), keyTypeClass)).maxResults(250);
				final List<? extends Object> columns = dataBaseHelper.getDbAccess().executeQuery(query);
				//
				final EntityObjectVO vo = new EntityObjectVO();
				vo.initFields(columns.size(), 1);
				vo.setEntity(cacheInfo.getSubform());
				// vo.setDependants(mpDependants);

				result = new HashMap<String, EntityFieldMetaDataVO>(columns.size());
				for (Object c: columns) {
					if (isBlank(c)) continue;
					final EntityFieldMetaDataVO md = new EntityFieldMetaDataVO(vo);
					final String pseudoFieldName = c.toString();
					md.setDynamic(false);
					md.setDbColumn(keyField.getDbColumn());
					md.setField(pseudoFieldName);
					md.setFallbacklabel(cacheInfo.getSubform() + ":" + cacheInfo.getKeyField() + ":" + c);
					md.setNullable(Boolean.TRUE);
					// md.setDataType(valueField.getDataType());
					md.setPivotInfo(cacheInfo);
					md.setEntityId(subform.getId());
					// ???
					// md.setReadonly(valueField.isReadonly() != null ? valueField.isReadonly() : Boolean.FALSE);
					md.setReadonly(Boolean.FALSE);

					result.put(pseudoFieldName, md);
				}
			}
			dataCache.getMapPivotMetaData().put(cacheInfo, result);
		}
		return result;
	}

	public EntityFieldMetaDataVO getRefField(String baseEntity, String subform) {
		// TODO: caching
		final Map<String, EntityFieldMetaDataVO>  fields = getAllEntityFieldsByEntity(subform);
		EntityFieldMetaDataVO result = null;
		for (EntityFieldMetaDataVO f: fields.values()) {
			if (baseEntity.equals(f.getForeignEntity())) {
				result = f;
				break;
			}
		}
		return result;
	}
	
	@Override
	public List<String> getPossibleIdFactories() {
		return new ArrayList<String>(dataBaseHelper.getDbAccess().getCallableNames());
	}

	private static boolean isBlank(Object o) {
		if (o == null) return true;
		if (o instanceof String) {
			return "".equals(o);
		}
		return false;
	}

	public Map<String, Map<String, EntityFieldMetaDataVO>> getAllEntityFieldsByEntitiesGz(Collection<String> entities) {
		// We can simply iterate most inefficiently over the single get results,
		// as these depend on caches themselves. All in all, the only thing
		// happening here is an in-memory cache transformation
	    GzipMap<String, Map<String, EntityFieldMetaDataVO>> res = new GzipMap<String, Map<String,EntityFieldMetaDataVO>>();
	    for(String entityName : entities)
	    	res.put(entityName, getAllEntityFieldsByEntity(entityName));
	    return res;
    }


	@Override
    public EntityFieldMetaDataVO getEntityField(String entity, String field) {
		final EntityFieldMetaDataVO result = getAllEntityFieldsByEntity(entity).get(field);
		if (result == null) {
			throw new CommonFatalException("entity field " + field + " in " + entity+ " does not exists.");
		}
		return result;
	}

	public EntityFieldMetaDataVO getEntityField(NuclosEntity entity, String field) {
		return getEntityField(entity.getEntityName(), field);
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, Long fieldId) {
		for(EntityFieldMetaDataVO fieldMeta : getAllEntityFieldsByEntity(entity).values())
			if(fieldMeta.getId().equals(fieldId))
				return fieldMeta;
		throw new CommonFatalException("entity field with id=" + fieldId + " in " + entity + " does not exists.");
	}
	
	@Override
	public Map<Long, LafParameterMap> getAllLafParameters() {
		final Map<Long, LafParameterMap> result = dataCache.getMapLafParameters();
		if (result == null) {
			return Collections.emptyMap();
		}
		return result;
	}

	@Override
	public LafParameterMap getLafParameters(Long entityId) {
		return getAllLafParameters().get(entityId);
	}

	public synchronized void revalidate(boolean otherCaches) {
		dataCache.buildMaps();
		nucletDalProvider.revalidate();

		if (otherCaches) {
			/** re-/invalidate old caches */
			SchemaCache.getInstance().invalidate();
			MasterDataMetaCache.getInstance().revalidate();
			AttributeCache.getInstance().revalidate();
			Modules.getInstance().invalidate();
			GenericObjectMetaDataCache.getInstance().layoutChanged(null);
		}

		LOG.info("JMS send: notify clients that meta data changed: " + this);
		LocalCachesUtil.getInstance().updateLocalCacheRevalidation(JMSConstants.TOPICNAME_METADATACACHE);
		NuclosJMSUtils.sendOnceAfterCommitDelayed("Meta data changed.", JMSConstants.TOPICNAME_METADATACACHE);
	}

	@Override
	protected void finalize() throws Throwable {
		//this.clientnotifier.close();
		super.finalize();
	}

	class DataCache {
		private volatile boolean revalidating = false;

		private long startRevalidating;

		private Map<String, List<String>> mapEntitiesByNuclets = null;

		private Map<String, EntityMetaDataVO> mapMetaDataByEntity = null;
		private Map<Long, EntityMetaDataVO> mapMetaDataById = null;
		private Map<String, Map<String, EntityFieldMetaDataVO>> mapFieldMetaData = null;
		private ConcurrentHashMap<PivotInfo, Map<String, EntityFieldMetaDataVO>> mapPivotMetaData = new ConcurrentHashMap<PivotInfo, Map<String,EntityFieldMetaDataVO>>();
		
		private Map<Long, LafParameterMap> mapLafParameter = null;

		private DataCache() {
		}

		public Map<String, List<String>> getMapEntitiesByNuclets() {
			if (isRevalidating()) {
				return getMapEntitiesByNuclets();
			} else {
				return mapEntitiesByNuclets;
			}
		}

		/**
		 * @deprecated Risk of spring circular references. Avoid this.
		 */
		public Map<String, EntityMetaDataVO> getMapMetaDataByEntity() {
			if (isRevalidating()) {
				return getMapMetaDataByEntity();
			}
			return mapMetaDataByEntity;
		}

		private Map<String, EntityMetaDataVO> buildMapMetaDataByEntity() {
			Map<String, EntityMetaDataVO> result = new HashMap<String, EntityMetaDataVO>();
			
			/*
			 * Nuclet Entities
			 */
			for (EntityMetaDataVO eMeta : nucletDalProvider.getEntityMetaDataProcessor().getAll()){
				result.put(eMeta.getEntity(), eMeta);
			}

			/*
			 * Nuclos Entities
			 */
			for (EntityMetaDataVO eMeta : nuclosDalProvider.getEntityMetaDataProcessor().getAll()){
				result.put(eMeta.getEntity(), eMeta);
			}

			/*
			 * Dynamic Entities
			 */
			for(EntityMetaDataVO meta : dynamicMetaDataProcessor.getDynamicEntities())
				result.put(meta.getEntity(), meta);

			/*
			 * Chart Entities
			 */
			for(EntityMetaDataVO meta : chartMetaDataProcessor.getChartEntities())
				result.put(meta.getEntity(), meta);

			return result;
		}

		public Map<Long, EntityMetaDataVO> getMapMetaDataById() {
			if (isRevalidating()) {
				return getMapMetaDataById();
			} else {
				return mapMetaDataById;
			}
		}

		private Map<Long, EntityMetaDataVO> buildMapMetaDataById(Map<String, EntityMetaDataVO> metaDataByEntity) {
			Map<Long, EntityMetaDataVO> result = new HashMap<Long, EntityMetaDataVO>();
			for(EntityMetaDataVO v : metaDataByEntity.values())
				result.put(v.getId(), v);
			return result;
		}

		public Map<String, Map<String, EntityFieldMetaDataVO>> getMapFieldMetaData() {
			if (isRevalidating()) {
				return getMapFieldMetaData();
			} else {
				return mapFieldMetaData;
			}
		}

		public Map<PivotInfo, Map<String, EntityFieldMetaDataVO>> getMapPivotMetaData() {
			if (isRevalidating()) {
				return getMapPivotMetaData();
			} else {
				return mapPivotMetaData;
			}
		}
		
		public Map<Long, LafParameterMap> getMapLafParameters() {
			if (isRevalidating()) {
				return getMapLafParameters();
			} else {
				return mapLafParameter;
			}
		}

		private Map<String, Map<String, EntityFieldMetaDataVO>> buildMapFieldMetaData(Map<String, EntityMetaDataVO> mapMetaDataByEntity) {
			Map<String, Map<String, EntityFieldMetaDataVO>> result = new HashMap<String, Map<String,EntityFieldMetaDataVO>>();

			/*
			 * Nuclet Entities
			 */
			for (EntityMetaDataVO eMeta : nucletDalProvider.getEntityMetaDataProcessor().getAll()){
				List<EntityFieldMetaDataVO> entityFields = nucletDalProvider.getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());
				DalUtils.addNucletEOSystemFields(entityFields, eMeta);

				result.put(eMeta.getEntity(), new HashMap<String, EntityFieldMetaDataVO>());
				for (EntityFieldMetaDataVO efMeta : entityFields) {
					result.get(eMeta.getEntity()).put(efMeta.getField(), efMeta);
				}
			}

			/*
			 * Nuclos Entities
			 */
			for (EntityMetaDataVO eMeta : nuclosDalProvider.getEntityMetaDataProcessor().getAll()){
				List<EntityFieldMetaDataVO> entityFields = nuclosDalProvider.getEntityFieldMetaDataProcessor().getByParent(eMeta.getEntity());

				result.put(eMeta.getEntity(), new ConcurrentHashMap<String, EntityFieldMetaDataVO>());
				for (EntityFieldMetaDataVO efMeta : entityFields) {
					result.get(eMeta.getEntity()).put(efMeta.getField(), efMeta);
				}
			}
			
			/*
			 * Dynamic Entities
			 */
			DynamicMetaDataProcessor.getInstance().addDynamicEntities(result, mapMetaDataByEntity);

			
			/*
			 * Chart Entities
			 */
			ChartMetaDataProcessor.getInstance().addChartEntities(result, mapMetaDataByEntity);

			return result;
		}

		private Map<String, List<String>> buildMapEntitiesByNuclets() {
			HashMap<String, List<String>> entitiesByNuclets = new HashMap<String, List<String>>();
			entitiesByNuclets.put(NAMESPACE_NUCLOS, new ArrayList<String>());
			for (EntityMetaDataVO meta : nuclosDalProvider.getEntityMetaDataProcessor().getAll()) {
				entitiesByNuclets.get(NAMESPACE_NUCLOS).add(meta.getEntity());
			}

			for (EntityMetaDataVO meta : nucletDalProvider.getEntityMetaDataProcessor().getAll()) {
				String nuclet = LangUtils.defaultIfNull(meta.getNuclet(), NAMESPACE_DEFAULT);
				if (!entitiesByNuclets.containsKey(nuclet)) {
					entitiesByNuclets.put(nuclet, new ArrayList<String>());
				}
				if (entitiesByNuclets.containsKey(nuclet)) {
					entitiesByNuclets.get(nuclet).add(meta.getEntity());
				}
			}

			for (String nuclet : entitiesByNuclets.keySet()) {
				entitiesByNuclets.put(nuclet, Collections.unmodifiableList(entitiesByNuclets.get(nuclet)));
			}

			return entitiesByNuclets;
		}
		
		@SuppressWarnings("unchecked")
		private Map<Long, LafParameterMap> buildMapLafParameters() {
			Map<Long, LafParameterMap> result = new HashMap<Long, LafParameterMap>();
			for (EntityLafParameterVO vo : nucletDalProvider.getEntityLafParameterProcessor().getAll()) {
				if (LafParameter.PARAMETERS.containsKey(vo.getParameter())) {
					@SuppressWarnings("rawtypes")
					LafParameter lafParameter = LafParameter.PARAMETERS.get(vo.getParameter());					
					if (!result.containsKey(vo.getEntity())) {
						result.put(vo.getEntity(), new LafParameterHashMap());
					}
					result.get(vo.getEntity()).putValue(lafParameter, lafParameter.parse(vo.getValue()));
				} else {
					LOG.warn(String.format("LafParameter %s unkonwn. (entityId=%s)", vo.getParameter(), vo.getEntity()));
				}
			}
			return result;
		}

		public synchronized void buildMaps() {
			startRevalidating = System.currentTimeMillis();
			revalidating = true;
			mapEntitiesByNuclets = Collections.unmodifiableMap(buildMapEntitiesByNuclets());
			mapMetaDataByEntity = Collections.unmodifiableMap(buildMapMetaDataByEntity());
			mapMetaDataById = Collections.unmodifiableMap(buildMapMetaDataById(mapMetaDataByEntity));
			mapFieldMetaData = Collections.unmodifiableMap(buildMapFieldMetaData(mapMetaDataByEntity));
			mapLafParameter = Collections.unmodifiableMap(buildMapLafParameters());
			mapPivotMetaData.clear();
			revalidating = false;
		}

		private boolean isRevalidating() {
			if (revalidating) {
				try {
					if (startRevalidating + 1000l*60 < System.currentTimeMillis())
						throw new NuclosFatalException("nuclos.metadata.revalidation.error.2");
					Thread.sleep(1000);
					return true;
				}
				catch(InterruptedException e) {
					throw new NuclosFatalException(e);
				}
			} else {
				return false;
			}
		}

	} // class DataCache

	@Override
	public List<String> getEntities(String nuclet) {
		return dataCache.getMapEntitiesByNuclets().get(nuclet);
	}

}
