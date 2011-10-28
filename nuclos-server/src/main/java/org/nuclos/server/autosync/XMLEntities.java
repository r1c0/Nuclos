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
package org.nuclos.server.autosync;

import static org.nuclos.common.collection.CollectionUtils.transform;
import static org.nuclos.common.collection.CollectionUtils.transformIntoMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.EntityObjectToMasterDataTransformer;
import org.nuclos.common.collection.MasterDataToEntityObjectTransformer;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class XMLEntities {

	private static final Logger LOG = Logger.getLogger(XMLEntities.class);

	public static boolean DEV = true;

	private static Map<NuclosEntity, SystemDataCache> internalDataCaches;

	private static Map<String, SystemMasterDataMetaVO> systemEntities;

	private static Map<NuclosEntity, Integer> idCounter = new EnumMap<NuclosEntity, Integer>(NuclosEntity.class);

	static {
		try {
			init();
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	private static void init() throws IOException, XMLStreamException, ParseException {
		internalDataCaches = new EnumMap<NuclosEntity, SystemDataCache>(NuclosEntity.class);

		// (Masterdata) Meta-data is special because it contains data necessary for the correct interpretation
		// of the system data (including itself).
		// Special care is needed for the handling of the ids.  The (raw) data doesn't contain any ids (except
		// for some rare special cases).  But after processing, the ids in the extracted metadata objects and
		// the ids in the corresponding masterdata objects must match!
		List<?> masterdata = (List<?>) readJSON("masterdata");
		systemEntities = transformIntoMap(transform(masterdata, new JSONHelper.MasterDataMetaTransformer()),
			new Transformer<SystemMasterDataMetaVO, String>() {
				@Override
				public String transform(SystemMasterDataMetaVO metaVO) {
					return metaVO.getEntityName();
				}
			});
		registerData(NuclosEntity.MASTERDATA, mdvoListFromJSON("nuclos_masterdata", masterdata));

		initInternalDataJSON(NuclosEntity.DBTYPE, "dbtype");
		initInternalDataJSON(NuclosEntity.DBOBJECTTYPE, "dbobjecttype");
		initInternalDataJSON(NuclosEntity.LOCALE, "locale");
		initInternalDataJSON(NuclosEntity.LOCALERESOURCE, "localeresource");
		initInternalDataJSON(NuclosEntity.ACTION, "action");
		initInternalDataJSON(NuclosEntity.EVENT, "event");
		initInternalDataJSON(NuclosEntity.LAYOUT, "layout");
		initInternalDataJSON(NuclosEntity.WIKI, "wiki");
		initInternalDataJSON(NuclosEntity.DATATYP, "datatype");
		initInternalDataJSON(NuclosEntity.RELATIONTYPE, "relationtype");

		// Add Layout ML strings from resources
		// TODO: Why is layoutML a String field?  It's a physical XML (i.e. it contains an encoding declaration(!))
		for (MasterDataVO mdvo : getOrCreate(NuclosEntity.LAYOUT).getAll()) {
			String layoutMLResource = "resources/layoutml/" + mdvo.getField("name", String.class) + ".layoutml";
			InputStream is = XMLEntities.class.getClassLoader().getResourceAsStream(layoutMLResource);
			if (is != null) {
				String layoutML = IOUtils.readFromTextStream(is, "ISO-8859-15");
				mdvo.setField("layoutML", layoutML);
			}
		}
	}

	public static void main(String[] args) {
		Map<NuclosEntity, SystemDataCache> internalDataCaches = XMLEntities.internalDataCaches;
		LOG.debug("internalDataCaches: " + internalDataCaches);
	}

	public static Map<String, SystemMasterDataMetaVO> getSystemEntities() {
		return systemEntities;
	}


	public static SystemDataCache getData(NuclosEntity entity) {
		return internalDataCaches.get(entity);
	}

	private static SystemDataCache getData(String entityName) {
		return internalDataCaches.get(NuclosEntity.getByName(entityName));
	}

	public static boolean hasSystemData(String entityName) {
		NuclosEntity nuclosEntity = NuclosEntity.getByName(entityName);
		return nuclosEntity != null && internalDataCaches.containsKey(nuclosEntity);
	}

	public static Collection<MasterDataVO> getSystemObjects(String entity, CollectableSearchCondition cond) {
		if (!XMLEntities.hasSystemData(entity))
			return Collections.emptyList();
		return getData(entity).findAllVO(cond);
	}

	public static Collection<MasterDataVO> getSystemObjectsWith(String entity, String field, Object value) {
		if (!XMLEntities.hasSystemData(entity))
			return Collections.emptyList();
		return getData(entity).findAllVO(field, value);
	}

	public static Collection<MasterDataVO> getSystemObjectsWith(String entity, String field, Object...values) {
		if (!XMLEntities.hasSystemData(entity))
			return Collections.emptyList();
		return getData(entity).findAllVOIn(field, Arrays.asList(values));
	}

	public static Collection<Object> getSystemObjectIds(String entity, CollectableSearchCondition cond) {
		return transform(getSystemObjects(entity, cond), new MasterDataVO.GetId());
	}

	public static Collection<Object> getSystemObjectIdsWith(String entity, String field, Object value) {
		return transform(getSystemObjectsWith(entity, field, value), new MasterDataVO.GetId());
	}

	public static MasterDataVO getSystemObjectById(String entity, Object id) {
		if (!XMLEntities.hasSystemData(entity))
			return null;
		return getData(entity).getById(id);
	}

	private static void registerData(NuclosEntity entity, Collection<MasterDataVO> mdvos) {
		for (MasterDataVO mdvo : mdvos) {
			if (mdvo.getId() == null) {
				Integer id = nextId(entity);
				mdvo.setId(id);
				if (mdvo.getFields().containsKey("id"))
					mdvo.setField("id", id);
			}
			DependantMasterDataMap dependants = mdvo.getDependants();
			if (dependants != null) {
				for (String dependantEntityName : dependants.getEntityNames()) {
					NuclosEntity dependantEntity = NuclosEntity.getByName(dependantEntityName);
					Collection<EntityObjectVO> dependantVOs = dependants.getData(dependantEntityName);
					MasterDataMetaFieldVO fieldRef = systemEntities.get(dependantEntityName).getFieldReferencing(entity.getEntityName());

					Collection<MasterDataVO> colVO = CollectionUtils.transform(dependantVOs, new EntityObjectToMasterDataTransformer());

					ensureAggregation(mdvo, colVO, fieldRef);
					registerData(dependantEntity, colVO);
				}
			}
		}

		SystemDataCache cache = getOrCreate(entity);
		cache.addAll(mdvos);
	}

	private static void ensureAggregation(MasterDataVO mdvo, Collection<MasterDataVO> dependantVOs, MasterDataMetaFieldVO refField) {
		String refFieldName = refField.getFieldName();
		Object refId = mdvo.getId();
		String fkField = refField.getForeignEntityField();
		Object refValue = mdvo.getField(fkField != null ? fkField : "name", refField.getJavaClass());
		for (MasterDataVO dependantVO : dependantVOs) {
			if (dependantVO.getField(refFieldName + "Id") != null)
				throw new IllegalStateException();
			dependantVO.setField(refFieldName + "Id", refId);
			dependantVO.setField(refFieldName, refValue);
		}
	}

	private static Integer nextId(NuclosEntity entity) {
		Integer lastId = idCounter.get(entity);
		Integer nextId = (lastId != null) ? lastId - 1 : -1;
		idCounter.put(entity, nextId);
		return nextId;
	}

	private static SystemDataCache getOrCreate(NuclosEntity entity) {
		SystemDataCache cache = internalDataCaches.get(entity);
		if (cache == null) {
			cache = new SystemDataCache(entity);
			internalDataCaches.put(entity, cache);
		}
		return cache;
	}

	private static void initInternalDataJSON(NuclosEntity entity, String file) throws IOException, ParseException {
		registerData(entity, parseJSON(entity.getEntityName(), file));
	}

	private static MasterDataVO readVO(XMLStreamReader reader, String entity, Map<String, Class<?>> t, MasterDataVOHandler handler) throws XMLStreamException {
		Map<String, Object> fields = new LinkedHashMap<String, Object>();
		DependantMasterDataMap dependantMap = null;
		while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
			String name = reader.getLocalName();
			if (name.equals("dependants")) {
				dependantMap = new DependantMasterDataMap();
				while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
					String dependantName = reader.getLocalName();
					List<MasterDataVO> list = readList(reader, t, handler);
					Collection<EntityObjectVO> colVO = CollectionUtils.transform(list, new MasterDataToEntityObjectTransformer());
					dependantMap.addAllData(dependantName, colVO);
				}
			} else {
				Class<?> type = t.get(entity + "." + name);
				String text = reader.getElementText();
				Object value;
				if (text.isEmpty()) {
					value = null;
				} else if (type == Integer.class) {
					value = Integer.parseInt(text);
				} else {
					value = text;
				}
				fields.put(name, value);
			}
		}
		Object id = fields.get("id");
		MasterDataVO mdvo = new SystemMasterDataVO(id, fields);
		if (dependantMap != null) {
			mdvo = new MasterDataWithDependantsVO(mdvo, dependantMap);
		}
		if (handler != null) {
			handler.handle(entity, mdvo);
		}
		return mdvo;
	}

	private static List<MasterDataVO> readList(XMLStreamReader reader, Map<String, Class<?>> t, MasterDataVOHandler handler) throws XMLStreamException {
		List<MasterDataVO> list = new ArrayList<MasterDataVO>();
		String entity = reader.getLocalName();
		while (reader.nextTag() == XMLStreamReader.START_ELEMENT) {
			list.add(readVO(reader, entity, t, handler));
		}
		return list;
	}

	public static Object readJSON(String file) throws IOException, ParseException {
		InputStream is = XMLEntities.class.getClassLoader().getResourceAsStream("resources/data/" + file + ".json");
		if (is == null) {
			return null;
		}
		return JSONValue.parseWithException(new InputStreamReader(is, "utf-8"));
	}

	public static List<MasterDataVO> parseJSON(String entity, String file) throws IOException, ParseException {
		return mdvoListFromJSON(entity, (List<?>) readJSON(file));
	}

	public static List<MasterDataVO> mdvoListFromJSON(String entity, List<?> list) {
		List<MasterDataVO> mdvos = new ArrayList<MasterDataVO>();
		for (Object obj : list) {
			MasterDataVO mdvo = mdvo(entity, (Map<?, ?>) obj);
			if (mdvo != null) {
				mdvos.add(mdvo(entity, (Map<?, ?>) obj));
			}
		}
		return mdvos;
	}

	public static MasterDataVO mdvo(String entity, Map<?, ?> map) {
		return JSONHelper.makeMasterDataVO(map, entity, systemEntities);
	}

	private static interface MasterDataVOHandler {

		public void handle(String entity, MasterDataVO mdvo);
	}
}
