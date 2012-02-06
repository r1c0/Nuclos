package org.nuclos.client.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.MetaDataProvider;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableEntityFieldPref;
import org.nuclos.common.collect.collectable.CollectableEntityPref;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.masterdata.CollectableMasterDataForeignKeyEntityField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

public class CollectableEntityFieldPreferencesUtil {
	
	private static final Logger LOG = Logger.getLogger(CollectableEntityFieldPreferencesUtil.class);
	
	private CollectableEntityFieldPreferencesUtil(MetaDataProvider mdProv) {
		// Never invoked.
	}
	
	public static List<CollectableEntityField> readList(Preferences prefs, String node, boolean ignoreErrors) throws PreferencesException {
		final List<?> raw = PreferencesUtils.getSerializableListXML(prefs, node, ignoreErrors);
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>(raw.size());
		// backward hack
		for (Object p: raw) {
			if (p instanceof CollectableEntityFieldPref) {
				final CollectableEntityFieldPref pref = (CollectableEntityFieldPref) p;
				try {
					result.add(fromPref(pref));
				}
				catch (PreferencesException e) {
					if (ignoreErrors) {
						LOG.warn("readList: fromPref fails on " + pref + ", " + e, e);
					}
					else {
						throw e;
					}
				}
				catch (CommonFatalException e) {
					if (ignoreErrors) {
						LOG.warn("readList: fromPref fails on " + pref + ", " + e, e);
					}
					else {
						throw new PreferencesException("readList: fromPref fails on " + pref, e);
					}
				}
			}
			// compatibility case
			else if (p instanceof CollectableEntityField) {
				result.add((CollectableEntityField) p);
			}
		}
		return result;
	}
	
	public static void writeList(Preferences prefs, String node, List<CollectableEntityField> fields) throws PreferencesException {
		final List<CollectableEntityFieldPref> converted = new ArrayList<CollectableEntityFieldPref>(fields.size());
		for (CollectableEntityField f: fields) {
			final CollectableEntityFieldPref conv = toPref(f);
			converted.add(conv);
		}
		PreferencesUtils.putSerializableListXML(prefs, node, converted);
	}
	
	public static CollectableEntityField fromPref(CollectableEntityFieldPref p) throws PreferencesException {
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		final CollectableEntityField result;
		if (CollectableEOEntityField.class.getName().equals(p.getType())) {
			final EntityFieldMetaDataVO efMeta;
			if (p.getPivot() == null) {
				efMeta = mdProv.getEntityField(p.getEntity(), p.getField());
			}
			else {
				final Collection<EntityFieldMetaDataVO> fields = mdProv.getAllPivotEntityFields(p.getPivot(), Collections.singletonList(p.getPivot().getValueField()));
				EntityFieldMetaDataVO rightField = null;
				for (EntityFieldMetaDataVO f: fields) {
					if (f.getField().equals(p.getField()) && f.getPivotInfo().equals(p.getPivot())) {
						rightField = f;
						break;
					}
				}
				if (rightField == null) {
					throw new PreferencesException("No pivot field found for " + p);
				}
				else {
					efMeta = rightField;
				}
			}
			result = new CollectableEOEntityField(efMeta, p.getEntity());
		}
		else if (CollectableGenericObjectEntityField.class.getName().equals(p.getType())) {
			result = new CollectableGenericObjectEntityField(
					AttributeCache.getInstance().getAttribute(p.getEntity(), p.getField()),
					mdProv.getEntityField(p.getEntity(), p.getField()),
					p.getEntity());			
		}
		else if (CollectableMasterDataForeignKeyEntityField.class.getName().equals(p.getType())) {
			result = new CollectableMasterDataForeignKeyEntityField(
					MasterDataDelegate.getInstance().getMetaData(p.getEntity()).getField(p.getField()), p.getEntity());			
		}
		/*
		else if (DefaultCollectableEntityField.class.getName().equals(p.getType())) {
			result = new DefaultCollectableEntityField(p.getField(), clctef.getJavaClass(), clctef.getLabel(), clctef.getDescription(), clctef.getMaxLength(),
					clctef.getPrecision(), clctef.isNullable(), clctef.getFieldType(), clctef.getReferencedEntityName(),
					clctef.getDefault(), clctef.getFormatInput(), clctef.getFormatOutput(), p.getEntity());	
		}
		 */
		else if (CollectableEntityFieldWithEntityForExternal.class.getName().equals(p.getType())) {
			final CollectableEntityPref cep = p.getCollectableEntity();
			if (cep == null) {
				throw new PreferencesException("No CollectableEntityPref for CollectableEntityFieldWithEntityForExternal: " + p);
			}
			final CollectableEntity ce = mkCe(cep);
			result = new CollectableEntityFieldWithEntityForExternal(ce, p.getField(), p.getBelongsToSubEntity(), p.getBelongsToMainEntity());
		}
		// probably not needed (tp)
		else if (CollectableEntityFieldWithEntity.class.getName().equals(p.getType())) {
			final CollectableEntityPref cep = p.getCollectableEntity();
			if (cep == null) {
				throw new PreferencesException("No CollectableEntityPref for CollectableEntityFieldWithEntityForExternal: " + p);
			}
			final CollectableEntity ce = mkCe(cep);
			result = new CollectableEntityFieldWithEntity(ce, p.getField());
		}
		else {
			throw new PreferencesException("Unknown CollectableEntityField of type " + p.getType());
		}
		return result;
	}
	
	private static CollectableEntity mkCe(CollectableEntityPref cep) throws PreferencesException {
		final MetaDataProvider mdProv = MetaDataClientProvider.getInstance();
		final CollectableEntity ce;
		if (CollectableEOEntity.class.getName().equals(cep.getType())) {
			ce = new CollectableEOEntity(mdProv.getEntity(cep.getEntity()), mdProv.getAllEntityFieldsByEntity(cep.getEntity()));
		}
		else if (CollectableGenericObjectEntity.class.getName().equals(cep.getType())) {
			final EntityMetaDataVO ceoe = mdProv.getEntity(cep.getEntity());
			final Integer iModuleId = IdUtils.unsafeToId(ceoe.getId());
			final Modules modules = Modules.getInstance();
			final String sName = modules.getEntityNameByModuleId(iModuleId);
			final String sLabel = modules.getEntityLabelByModuleId(iModuleId);
			// Note that only attributes occuring in Details layouts are taken into account for building the entity:
			/* the collection from the AttributeCache must not be modified. A new one is created instead */
			final Collection<String> collFieldNames = new HashSet<String>(
					GenericObjectMetaDataCache.getInstance().getAttributeNamesByModuleId(iModuleId, Boolean.FALSE));
			ce = new CollectableGenericObjectEntity(sName, sLabel, collFieldNames);
		}
		/*
		else if (CollectableGenericObjectEntityForAllAttributes.class.getName().equals(cep.getType())) {	
		}
		 */
		else if (CollectableMasterDataEntity.class.getName().equals(cep.getType())) {
			MasterDataMetaVO metaData = MasterDataDelegate.getInstance().getMetaData(cep.getEntity());
			ce = new CollectableMasterDataEntity(metaData);			
		}
		else if (DoNotUseCollectableEntity.class.getName().equals(cep.getType())) {
			final EntityMetaDataVO mdEntity = mdProv.getEntity(cep.getEntity());
			ce = new DoNotUseCollectableEntity(cep.getEntity(), CommonLocaleDelegate.getInstance().getLabelFromMetaDataVO(mdEntity));
		}
		else {
			throw new PreferencesException("Unknown CollectableEntity of type " + cep.getType());
		}
		return ce;
	}
	
	public static CollectableEntityFieldPref toPref(CollectableEntityField f) throws PreferencesException {
		final CollectableEntityFieldPref result;
		if (f instanceof CollectableEOEntityField) {
			result = toPref((CollectableEOEntityField) f);
		}
		else if (f instanceof CollectableGenericObjectEntityField) {
			result = toPref((CollectableGenericObjectEntityField) f);
		}
		else if (f instanceof CollectableMasterDataForeignKeyEntityField) {
			result = toPref((CollectableMasterDataForeignKeyEntityField) f);
		}
		else if (f instanceof CollectableEntityFieldWithEntityForExternal) {
			result = toPref((CollectableEntityFieldWithEntityForExternal) f);
		}
		// probably not needed (tp)
		else if (f instanceof CollectableEntityFieldWithEntity) {
			result = toPref((CollectableEntityFieldWithEntity) f);
		}
		else {
			throw new PreferencesException("Unknown CollectableEntityField of type " + f);
		}
		return result;
	}
	
	public static CollectableEntityFieldPref toPref(CollectableEOEntityField f) throws PreferencesException {
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				CollectableEOEntityField.class.getName(), f.getEntityName(), f.getName(), f.getMeta().getPivotInfo());
		return p;
	}
	
	public static CollectableEntityFieldPref toPref(CollectableGenericObjectEntityField f) throws PreferencesException {
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				CollectableGenericObjectEntityField.class.getName(), f.getEntityName(), f.getName(), null);
		return p;
	}
	
	public static CollectableEntityFieldPref toPref(CollectableMasterDataForeignKeyEntityField f) throws PreferencesException {
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				CollectableMasterDataForeignKeyEntityField.class.getName(), f.getEntityName(), f.getName(), null);
		return p;
	}
	
	/*
	public static CollectableEntityFieldPref toPref(DefaultCollectableEntityField f) throws PreferencesException {
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				DefaultCollectableEntityField.class.getName(), f.getEntityName(), f.getName(), null);
		return p;
	}
	 */
	
	public static CollectableEntityFieldPref toPref(CollectableEntityFieldWithEntityForExternal f) throws PreferencesException {
		final CollectableEntity ce = f.getCollectableEntity();
		final CollectableEntityPref cep = new CollectableEntityPref(ce.getClass().getName(), ce.getName());
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				CollectableEntityFieldWithEntityForExternal.class.getName(), cep, f.getName(), 
				f.fieldBelongsToSubEntity(), f.fieldBelongsToMainEntity());
		return p;
	}
	
	public static CollectableEntityFieldPref toPref(CollectableEntityFieldWithEntity f) throws PreferencesException {
		final CollectableEntity ce = f.getCollectableEntity();
		final CollectableEntityPref cep = new CollectableEntityPref(ce.getClass().getName(), ce.getName());
		final CollectableEntityFieldPref p = new CollectableEntityFieldPref(
				CollectableEntityFieldWithEntity.class.getName(), cep, f.getName(),
				// ???
				false, false);
		return p;
	}
	
}
