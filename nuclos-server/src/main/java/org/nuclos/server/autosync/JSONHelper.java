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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class JSONHelper {

	public static MasterDataVO makeMasterDataVO(Object json, String entity, Map<String, SystemMasterDataMetaVO> metaData) {
		Map<?, ?> jsonProperties = (Map<?, ?>) json;
		MasterDataMetaVO metaVO = metaData.get(entity);

		if (metaVO == null) {
			return null;
		}

		Map<String, Object> fields = new HashMap<String, Object>();
		for (MasterDataMetaFieldVO field : metaVO.getFields()) {
			Class<?> javaClass = field.getJavaClass();
			Object value = jsonProperties.get(field.getFieldName());
			fields.put(field.getFieldName(), coerce(value, javaClass));
			if (field.getForeignEntity() != null) {
				Object valueId = jsonProperties.get(field.getFieldName() + "Id");
				fields.put(field.getFieldName() + "Id", coerce(valueId, Integer.class));
			}
		}

		DependantMasterDataMap dependantMap = null;
		for (Map.Entry<?, ?> e : jsonProperties.entrySet()) {
			String name = e.getKey().toString();
			Object value = e.getValue();
			// Dependant data
			if (value instanceof List<?> && !fields.containsKey(name)) {
				// at the moment, "uniquefields" is not real subentity
				if (NuclosEntity.MASTERDATA.checkEntityName(entity) && name.equals("uniquefields"))
					continue;
				if (NuclosEntity.MASTERDATA.checkEntityName(entity) && name.equals("logicaluniquefields"))
					continue;
				if (dependantMap == null)
					dependantMap = new DependantMasterDataMap();
				for (Object dependant : (List<?>) value) {
					MasterDataVO dependantVO = makeMasterDataVO(dependant, name, metaData);
					if (dependantVO != null) {
						dependantMap.addData(name, DalSupportForMD.getEntityObjectVO(dependantVO));
					}
				}
			} else {
				// field, was already handled
			}
		}
		Integer id = coerce(jsonProperties.get("id"), Integer.class, null);
		MasterDataVO mdvo = new SystemMasterDataVO(id, fields);
		if (dependantMap != null)
			mdvo = new MasterDataWithDependantsVO(mdvo, dependantMap);
		return mdvo;
	}

	public static class MasterDataMetaTransformer implements Transformer<Object, SystemMasterDataMetaVO> {

		private int nextId = -3;
		private int nextFieldId = -1;

		@Override
		public SystemMasterDataMetaVO transform(Object json) {
			Map<?, ?> obj = (Map<?, ?>) json;
			nextId = checkId(obj, nextId);
			return new SystemMasterDataMetaVO(
				coerce(obj.get("id"), Integer.class),
				(String) obj.get("entity"),
				(String) obj.get("dbentity"),
				(String) obj.get("menupath"),
				coerce(obj.get("searchable"), Boolean.class, false),
				coerce(obj.get("editable"), Boolean.class, false),
				(String) obj.get("name"),
				parseFieldsForEquality((String) obj.get("fields_for_equality")),
				coerce(obj.get("cacheable"), Boolean.class, false),
				makeFieldMap((List<?>) obj.get(NuclosEntity.ENTITYFIELD.getEntityName())),
				(String) obj.get("treeview"),
				(String) obj.get("treeviewdescription"),
				coerce(obj.get("blnsystementity"), Boolean.class, false),
				null, // TODO: (String) obj.get("resource"),
				(String) obj.get("nuclosresource"),
				coerce(obj.get("importexport"), Boolean.class, false),
				(String) obj.get("labelplural"),
				(Integer) obj.get("acceleratormodifier"),
				(String) obj.get("accelerator"),
				(String) obj.get("labelres"),
				(String) obj.get("menupathres"),
				(String) obj.get("labelpluralres"),
				(String) obj.get("treeviewres"),
				(String) obj.get("treeviewdescriptionres"),
				parseUniqueFieldCombinations((List<?>) obj.get("uniquefields")),
				parseUniqueFieldCombinations((List<?>) obj.get("logicaluniquefields")));
		}

		protected SystemMasterDataMetaFieldVO transformField(Object json) {
			Map<?, ?> obj = (Map<?, ?>) json;
			nextFieldId = checkId(obj, nextFieldId);
			return new SystemMasterDataMetaFieldVO(
				coerce(obj.get("id"), Integer.class),
				(String) obj.get("name"),
				(String) obj.get("dbfield"),
				(String) obj.get("label"),
				(String) obj.get("description"),
				(String) obj.get("entityfieldDefault"),
				(String) obj.get("foreignentity"),
				(String) obj.get("foreignentityfield"),
				(String) obj.get("unreferencedforeignentity"),
				(String) obj.get("unreferencedforeignentityfield"),
				parseDatatype((String) obj.get("datatype")),
				// eigentlich sind Precision und Scale vertauscht (vergleicht man die Semantik von SQL, BigDecimal etc.)
				coerce(obj.get("datascale"), Integer.class),
				coerce(obj.get("dataprecision"), Integer.class),
				(String) obj.get("formatinput"),
				(String) obj.get("formatoutput"),
				coerce(obj.get("nullable"), Boolean.class, false),
				coerce(obj.get("searchable"), Boolean.class, false),
				coerce(obj.get("unique"), Boolean.class, false),
				coerce(obj.get("invariant"), Boolean.class, false),
				coerce(obj.get("logbook"), Boolean.class, false),
				(String) obj.get("labelres"),
				(String) obj.get("descriptionres"),
				coerce(obj.get("indexed"), Boolean.class, false),
				coerce(obj.get("ondeletecascade"), Boolean.class, false),
				coerce(obj.get("order"), Integer.class));
		}

		protected int checkId(Map<?, ?> obj, int nextId) {
			if (!obj.containsKey("id")) {
				((Map<String, Object>) obj).put("id", nextId);
				return nextId - 1;
			}
			return nextId;
		}

		private Map<String, SystemMasterDataMetaFieldVO> makeFieldMap(List<?> json) {
			Map<String, SystemMasterDataMetaFieldVO> map = new HashMap<String, SystemMasterDataMetaFieldVO>();
			if (json != null) {
				for (Object fieldObj : json) {
					SystemMasterDataMetaFieldVO field = transformField(fieldObj);
					map.put(field.getFieldName(), field);
				}
			}
			return map;
		}
	};

	public static <T> T coerce(Object value, Class<T> clazz) {
		return coerce(value, clazz, null);
	}

	public static <T> T coerce(Object value, Class<T> clazz, T def) {
		if (value == null) {
			return def;
		} else if (clazz == Integer.class) {
			return (T) Integer.valueOf(((Number) value).intValue());
		} else if (clazz == Double.class) {
			return (T) Double.valueOf(((Number) value).doubleValue());
		} else if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}
		throw new ClassCastException("Cannot cast " + value + " to " + clazz);
	}

	static Set<String> parseFieldsForEquality(String fieldsForEquality) {
		return (fieldsForEquality != null) ? new HashSet<String>(Arrays.asList(fieldsForEquality.split(";"))) : null;
	}

	static Class<?> parseDatatype(String datatype) {
		try {
			return Class.forName(datatype);
		} catch(ClassNotFoundException e) {
			throw new CommonFatalException("Unsupported datatype " + datatype, e);
		}
	}

	static List<Set<String>> parseUniqueFieldCombinations(List<?> obj) {
		List<Set<String>> uniqueCombinations = new ArrayList<Set<String>>();
		if (obj != null) {
			for (Object item : obj) {
				uniqueCombinations.add(new LinkedHashSet<String>(
					CollectionUtils.typecheck((List<?>) item, String.class)));
			}
		}
		return uniqueCombinations;
	}
}
