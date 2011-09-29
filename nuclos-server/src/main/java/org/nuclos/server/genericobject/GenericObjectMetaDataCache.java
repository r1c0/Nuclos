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
package org.nuclos.server.genericobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.GenericObjectMetaDataProvider;
import org.nuclos.common.GenericObjectMetaDataVO;
import org.nuclos.common.NuclosAttributeNotFoundException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.UsageCriteria;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.EntityAndFieldName;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.attribute.valueobject.AttributeCVO;
import org.nuclos.server.attribute.valueobject.LayoutUsageVO;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;

/**
 * Server side leased object meta data cache (singleton).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectMetaDataCache implements GenericObjectMetaDataProvider {
	private static final Logger log = Logger.getLogger(GenericObjectMetaDataCache.class);

	private static GenericObjectMetaDataCache singleton;

	private GenericObjectMetaDataVO lometacvo;

	private static final Set<Integer> setExcludedAttributeIds = new HashSet<Integer>();
	static {
		setExcludedAttributeIds.add(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.CHANGEDAT.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.CHANGEDBY.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.CREATEDAT.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.CREATEDBY.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.STATE.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.STATENUMBER.getMetaData().getId().intValue());
		setExcludedAttributeIds.add(NuclosEOField.STATEICON.getMetaData().getId().intValue());
	}

	final Integer[] asExcludedAttributeIds = {
		NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getId().intValue(),
		NuclosEOField.CHANGEDAT.getMetaData().getId().intValue(),
		NuclosEOField.CHANGEDBY.getMetaData().getId().intValue(),
		NuclosEOField.CREATEDAT.getMetaData().getId().intValue(),
		NuclosEOField.CREATEDBY.getMetaData().getId().intValue(),
		NuclosEOField.STATE.getMetaData().getId().intValue(),
		NuclosEOField.STATENUMBER.getMetaData().getId().intValue(),
		NuclosEOField.STATEICON.getMetaData().getId().intValue()};

	public static synchronized GenericObjectMetaDataCache getInstance() {
		if (singleton == null) {
			singleton = new GenericObjectMetaDataCache();
		}
		return singleton;
	}

	private GenericObjectMetaDataCache() {
		this.lometacvo = newGenericObjectMetaDataCVO();
	}

	private static GenericObjectMetaDataVO newGenericObjectMetaDataCVO() {
		log.debug("Rebuilding generic object meta data cache");

		Map<Integer, Set<String>> mapModuleAtributes = new HashMap<Integer, Set<String>>();
		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			mapModuleAtributes.put(LangUtils.convertId(eMeta.getId()), new HashSet<String>());
			for (EntityFieldMetaDataVO efMeta : MetaDataServerProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity()).values()) {
				mapModuleAtributes.get(LangUtils.convertId(eMeta.getId())).add(efMeta.getField());
			}
		}

		return new GenericObjectMetaDataVO(mapModuleAtributes, getLayoutMap(), getLayoutUsageVOs());
	}

	/**
	 * @return Map<Integer iLayoutId, String sLayoutML>
	 * @postcondition result != null
	 */
	public static Map<Integer, String> getLayoutMap() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_LAYOUT").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("INTID", Integer.class), t.baseColumn("CLBLAYOUTML", String.class));

		Map<Integer, String> result = new HashMap<Integer, String>();
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			result.put(tuple.get(0, Integer.class), tuple.get(1, String.class));
		}
		return result;
	}

	/**
	 * @return Map<Integer, String>
	 */
	public Map<Integer, String> getResourceMap() {
		Map<Integer, String> result = new HashMap<Integer, String>();

//		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
//		DbQuery<DbTuple> query = builder.createTupleQuery();
//		DbFrom t = query.from("V_MD_MODULE").alias(ProcessorFactorySingleton.BASE_ALIAS);
//		query.multiselect(t.column("INTID", Integer.class), t.column("STRVALUE_T_MD_RESOURCE", String.class));
//
//		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
//			result.put(tuple.get(0, Integer.class), tuple.get(1, String.class));
//		}

		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			if (eMeta.isStateModel()) {
				if (eMeta.getResourceId() != null) {

					EntityObjectVO eo = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.RESOURCE.getEntityName()).getByPrimaryKey(eMeta.getResourceId().longValue());

					result.put(eMeta.getId().intValue(), (String) eo.getFields().get("name"));
				}
			}
		}

		return result;
	}

	/**
	 * @param iLayoutId
	 * @return result
	 */
	public static String getLayoutName(Integer iLayoutId){
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_LAYOUT").alias(SystemFields.BASE_ALIAS);
		query.multiselect(t.baseColumn("STRLAYOUT", String.class));
		query.where(builder.equal(t.baseColumn("INTID", Integer.class), iLayoutId));
		return CollectionUtils.getFirst(DataBaseHelper.getDbAccess().executeQuery(query));
	}

	/**
	 * @return all layout usages
	 * @postcondition result != null
	 */
	private static Collection<LayoutUsageVO> getLayoutUsageVOs() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_LAYOUTUSAGE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("INTID_T_MD_LAYOUT", Integer.class),
			t.baseColumn("STRENTITY", String.class),
			t.baseColumn("INTID_T_MD_PROCESS", Integer.class),
			t.baseColumn("BLNSEARCHSCREEN", Boolean.class));
		List<LayoutUsageVO> result = DataBaseHelper.getDbAccess().executeQuery(query, new Transformer<DbTuple, LayoutUsageVO>() {
			@Override
			public LayoutUsageVO transform(DbTuple tuple) {
				try {
					final Integer iEntityId = MetaDataServerProvider.getInstance().getEntity(tuple.get(1, String.class)).getId().intValue();
					return new LayoutUsageVO(
						tuple.get(0, Integer.class),
						new UsageCriteria(iEntityId, tuple.get(2, Integer.class)),
						Boolean.TRUE.equals(tuple.get(3, Boolean.class)));
				}
				catch (Exception ex) {
					return null;
				}
			}
		});

		CollectionUtils.retainAll(result, new Predicate<LayoutUsageVO>() {
			@Override
            public boolean evaluate(LayoutUsageVO t) {
	            if (t == null) {
	            	return false;
	            }
	            return true;
            }
		});
		return result;
	}
	/**
	 * Returns all LayoutIds for a specific Module
	 * @param ModuleId
	 * @return List of LayoutIds
	 */
	public static List<Integer> getLayoutIdsForModule(Object ModuleId){
		List<Integer> ret = new ArrayList<Integer>();
		Collection<LayoutUsageVO> col = getLayoutUsageVOs();
		Iterator<LayoutUsageVO> it = col.iterator();
		while(it.hasNext()){
			LayoutUsageVO luvo = it.next();
			if(luvo.getUsageCriteria().getModuleId() != null && luvo.getUsageCriteria().getModuleId().equals(ModuleId))
				ret.add(luvo.getLayoutId());
		}
		return ret;
	}

	/**
	 * @return ids of all attributes whose values cannot be changed directly by the user.
	 */
	public Set<Integer> getReadOnlyAttributeIds() {
		return setExcludedAttributeIds;
	}

	public GenericObjectMetaDataVO getMetaDataCVO() {
		return this.lometacvo;
	}

	private AttributeProvider getAttributeProvider() {
		return AttributeCache.getInstance();
	}

	@Override
	public AttributeCVO getAttribute(int iAttributeId) {
		return this.getAttributeProvider().getAttribute(iAttributeId);
	}

	@Override
	public AttributeCVO getAttribute(Integer iEntityId, String sAttributeName)
		throws NuclosAttributeNotFoundException {
		return this.getAttribute(Modules.getInstance().getEntityNameByModuleId(iEntityId), sAttributeName);
	}

	@Override
	public EntityFieldMetaDataVO getEntityField(String entity, String field) throws NuclosAttributeNotFoundException {
		return MetaDataServerProvider.getInstance().getEntityField(entity, field);
	}

	@Override
	public AttributeCVO getAttribute(String sEntity, String sAttributeName) throws NuclosAttributeNotFoundException {
		return this.getAttributeProvider().getAttribute(sEntity, sAttributeName);
	}

	@Override
	public Collection<AttributeCVO> getAttributes() {
		return this.getAttributeProvider().getAttributes();
	}

	@Override
	public Collection<AttributeCVO> getAttributeCVOsByModuleId(Integer iModuleId, Boolean bSearchable) {
		return this.getMetaDataCVO().getAttributeCVOsByModuleId(this.getAttributeProvider(), iModuleId, bSearchable);
	}

	@Override
	public Set<String> getAttributeNamesByModuleId(Integer iModuleId, Boolean bSearchable) {
		return this.getMetaDataCVO().getAttributeNamesByModuleId(iModuleId, bSearchable);
	}

//	public Collection<AttributeCVO> getAttributeCVOsByLayoutId(int iLayoutId) {
//		return this.getMetaDataCVO().getAttributeCVOsByLayoutId(this.getAttributeProvider(), iLayoutId);
//	}

	@Override
	public Set<String> getSubFormEntityNamesByLayoutId(int iLayoutId) {
		return this.getMetaDataCVO().getSubFormEntityNamesByLayoutId(iLayoutId);
	}

	@Override
	public Collection<EntityAndFieldName> getSubFormEntityAndForeignKeyFieldNamesByLayoutId(int iLayoutId) {
		return this.getMetaDataCVO().getSubFormEntityAndForeignKeyFieldNamesByLayoutId(iLayoutId);
	}

	@Override
	public Set<String> getBestMatchingLayoutAttributeNames(UsageCriteria usagecriteria) throws CommonFinderException {
		return this.getMetaDataCVO().getBestMatchingLayoutAttributeNames(usagecriteria);
	}

	public Set<String> getBestMatchingLayoutSubformEntityNames(UsageCriteria usagecriteria) throws CommonFinderException {
		return this.getMetaDataCVO().getBestMatchingLayoutSubformEntityNames(usagecriteria);
	}

	@Override
	public int getBestMatchingLayoutId(UsageCriteria usagecriteria, boolean bSearchScreen) throws CommonFinderException {
		return this.getMetaDataCVO().getBestMatchingLayoutId(usagecriteria, bSearchScreen);
	}

	@Override
	public Set<Integer> getLayoutIdsByModuleId(int iModuleId, boolean bSearchScreen) {
		return this.getMetaDataCVO().getLyoutIdsByModuleId(iModuleId, bSearchScreen);
	}

	@Override
	public String getLayoutML(int iLayoutId) {
		return this.getMetaDataCVO().getLayoutML(iLayoutId);
	}

	public void attributeChanged(Integer iAttributeId) {
		AttributeCache.getInstance().revalidate();
	}

	public void layoutChanged(Integer iLayoutId) {
		this.revalidate();
	}

	public void layoutUsageChanged(Integer iLayoutId) {
		this.revalidate();
	}

	private void revalidate() {
		// revalidate layout and module caches:
		this.lometacvo = newGenericObjectMetaDataCVO();
		// es gibt keine GO Views mehr --> GO Refactoring
//		this.refreshViews();
	}


}	// class GenericObjectLayoutCache
