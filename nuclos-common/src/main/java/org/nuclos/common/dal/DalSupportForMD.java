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
package org.nuclos.common.dal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.attribute.valueobject.AttributeValueVO;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class DalSupportForMD {

	private static final Logger LOG = Logger.getLogger(DalSupportForMD.class);

	private DalSupportForMD() {
		// Never invoked.
	}

	public static EntityObjectVO getEntityObjectVO(MasterDataVO md) {
		final EntityObjectVO eo = new EntityObjectVO();

		eo.setId(IdUtils.toLongId(md.getId()));
		eo.setCreatedBy(md.getCreatedBy());
		eo.setCreatedAt(InternalTimestamp.toInternalTimestamp(md.getCreatedAt()));
		eo.setChangedBy(md.getChangedBy());
		eo.setChangedAt(InternalTimestamp.toInternalTimestamp(md.getChangedAt()));
		eo.setVersion(md.getVersion());
		eo.setDependants(md.getDependants());
		if(md.isRemoved()) {
			eo.flagRemove();
		}
		else if(md.getId() == null){
			eo.flagNew();
		}
		else if (md.isChanged()) {
			eo.flagUpdate();
		}

		eo.initFields(10, 10);
		for (Entry<String, Object> field : md.getFields().entrySet()) {
			if (field.getKey().endsWith("Id")) {
				final String objectfieldname = field.getKey().substring(0, field.getKey().length()-2);
				if (field.getValue() instanceof Integer) {
					eo.getFieldIds().put(objectfieldname, IdUtils.toLongId(field.getValue()));
				} else if (field.getValue() instanceof Long) {
					eo.getFieldIds().put(objectfieldname, (Long) field.getValue());
				}
			}
			eo.getFields().put(field.getKey(), field.getValue());
		}

		return eo;
	}

	public static MasterDataVO wrapEntityObjectVO(EntityObjectVO eo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();

		for (Entry<String, Object> field : eo.getFields().entrySet()) {
			mpFields.put(field.getKey(), field.getValue());
		}
		for (Entry<String, Long> field : eo.getFieldIds().entrySet()) {
			mpFields.put(field.getKey()+"Id", IdUtils.unsafeToId(field.getValue()));
		}

		mpFields.put(NuclosEOField.CREATEDAT.getMetaData().getField(), eo.getCreatedAt());
		mpFields.put(NuclosEOField.CREATEDBY.getMetaData().getField(), eo.getCreatedBy());
		mpFields.put(NuclosEOField.CHANGEDAT.getMetaData().getField(), eo.getChangedAt());
		mpFields.put(NuclosEOField.CHANGEDBY.getMetaData().getField(), eo.getChangedBy());
		MasterDataVO vo = new MasterDataVO(IdUtils.unsafeToId(eo.getId()), eo.getCreatedAt(), eo.getCreatedBy(), eo.getChangedAt(), eo.getChangedBy(), eo.getVersion(), mpFields);
		vo.setChanged(eo.isFlagNew() || eo.isFlagUpdated());
		vo.setDependants(eo.getDependants());
		if(eo.isFlagRemoved())
			vo.remove();
		return vo;
	}

	public static MasterDataWithDependantsVO getMasterDataWithDependantsVO(EntityObjectVO eo) {
		final MasterDataVO base = wrapEntityObjectVO(eo);
		final MasterDataWithDependantsVO result = new MasterDataWithDependantsVO(base, eo.getDependants());
		return result;
	}

	public static EntityTreeViewVO wrapEntityObjectVOAsSubNode(EntityObjectVO eo) {
		EntityTreeViewVO vo = new EntityTreeViewVO(eo.getId(),
			IdUtils.toLongId(eo.getField(EntityTreeViewVO.ENTITY_FIELD, Object.class)),
			eo.getField(EntityTreeViewVO.SUBFORM_ENTITY_FIELD, String.class),
			eo.getField(EntityTreeViewVO.SUBFORM2ENTITY_REF_FIELD, String.class),
			eo.getField(EntityTreeViewVO.FOLDERNAME_FIELD, String.class),
			eo.getField(EntityTreeViewVO.ACTIVE_FIELD, Boolean.class),
			eo.getField(EntityTreeViewVO.SORTORDER_FIELD, Integer.class)
		);

		return vo;
	}

	public static MasterDataVO wrapEntityMetaDataVOInModule(EntityMetaDataVO eMeta) {
		Map<String, Object> mpFields = new HashMap<String, Object>();

		mpFields.put("treeview", eMeta.getLocaleResourceIdForTreeView());
		mpFields.put("showrelations", eMeta.isTreeRelation());
		mpFields.put("showgroups", eMeta.isTreeGroup());
		mpFields.put("usesStateModel", eMeta.isStateModel());
		mpFields.put("logbookTracking", eMeta.isLogBookTracking());
		mpFields.put("accelerator", eMeta.getAccelerator());
		mpFields.put("systemIdentifierMnemonic", eMeta.getSystemIdPrefix());
		mpFields.put("menuMnemonic", eMeta.getMenuShortcut());
		mpFields.put("name", eMeta.getLocaleResourceIdForLabel());
		mpFields.put("entity", eMeta.getEntity());
		mpFields.put("description", eMeta.getLocaleResourceIdForDescription());
		mpFields.put("menupath", eMeta.getLocaleResourceIdForMenuPath());
		mpFields.put("resourceId", eMeta.getResourceId());
		mpFields.put("importexport", eMeta.isImportExport());
		mpFields.put("treeviewdescription", eMeta.getLocaleResourceIdForTreeViewDescription());
		mpFields.put("documentPath", eMeta.getDocumentPath());
		mpFields.put("reportFilename", eMeta.getReportFilename());

		MasterDataVO md = new MasterDataVO(
			/*Object oId,*/ IdUtils.unsafeToId(eMeta.getId()),
			/*Date dateCreatedAt,*/ eMeta.getCreatedAt(),
			/*String sCreatedBy,*/ eMeta.getCreatedBy(),
			/*Date dateChangedAt,*/ eMeta.getChangedAt(),
			/*String sChangedBy,*/ eMeta.getChangedBy(),
			/*Integer iVersion,*/ eMeta.getVersion(),
			/*Map<String, Object> mpFields*/ mpFields
		);

		return md;
	}

	public static MasterDataMetaVO wrapEntityMetaDataVOInMasterData(EntityMetaDataVO eMeta, Collection<EntityFieldMetaDataVO> efsMeta) {
		Map<String, MasterDataMetaFieldVO> mpFields = new HashMap<String, MasterDataMetaFieldVO>();

		for (EntityFieldMetaDataVO efMeta : efsMeta) {
			mpFields.put(efMeta.getField(), wrapEntityFieldMetaDataVO(efMeta));
		}

		List<String> lstFieldsForEquality = new ArrayList<String>();
		if(eMeta.getFieldsForEquality() != null)
			lstFieldsForEquality = new ArrayList<String>(Arrays.asList(eMeta.getFieldsForEquality().split(";")));

		MasterDataMetaVO result;
		if (!eMeta.isDynamic()) {
			result = new MasterDataMetaVO(
				/*Integer iId,*/ IdUtils.unsafeToId(eMeta.getId()),
				/*String sEntityName,*/ eMeta.getEntity(),
				/*String sDBEntityName,*/ eMeta.getDbEntity(),
				/*String sMenuPath,*/ null,
				/*boolean bSearchable,*/ eMeta.isSearchable(),
				/*boolean bEditable,*/ eMeta.isEditable(),
				/*String sLabel,*/ null,
				/*Collection<String> collFieldsForEquality,*/ lstFieldsForEquality,
				/*boolean bCacheable,*/ eMeta.isCacheable(),
				/*java.util.Date dateCreatedAt,*/ eMeta.getCreatedAt(),
				/*String sCreatedBy,*/ eMeta.getCreatedBy(),
				/*java.util.Date dateChangedAt,*/ eMeta.getChangedAt(),
				/*String sChangedBy,*/ eMeta.getChangedBy(),
				/*Integer iVersion,*/ eMeta.getVersion(),
				/*Map<String, MasterDataMetaFieldVO> mpFields,*/ mpFields,
				/*String sTreeView,*/ null,
				/*String sDescription,*/ null,
				/*Boolean bSystemEntity,*/ false,
				/*String sResourceName,*/ null,
				/*String sNuclosResource,*/ null,
				/*Boolean bImportExport,*/ eMeta.isImportExport(),
				/*String sLabelPlural,*/ null,
				/*Integer iAccModifier,*/ eMeta.getAcceleratorModifier(),
				/*String accelerator,*/ eMeta.getAccelerator(),
				/*String sResourceIdForLabel,*/ eMeta.getLocaleResourceIdForLabel(),
				/*String sResourceIdForMenuPath,*/ eMeta.getLocaleResourceIdForMenuPath(),
				/*String sResourceIdForLabelPlural,*/ null, /** nicht verwendet!*/
				/*String sResourceIdForTreeView,*/ eMeta.getLocaleResourceIdForTreeView(),
				/*String sResourceIdForTreeViewDescription*/ eMeta.getLocaleResourceIdForTreeViewDescription()
				);
		}
		else {
			result = new MasterDataMetaVO(IdUtils.unsafeToId(eMeta.getId()), eMeta.getDbEntity(), mpFields);
		}
		return result;
	}

	public static MasterDataMetaFieldVO wrapEntityFieldMetaDataVO(EntityFieldMetaDataVO efMeta) {
		Class<?> clsDataType;
		try {
			clsDataType = Class.forName(efMeta.getDataType());
		}
		catch(ClassNotFoundException e) {
			throw new CommonFatalException(e);
		}

		MasterDataMetaFieldVO mdfmeta = new MasterDataMetaFieldVO(
			/*Integer iId,*/ IdUtils.unsafeToId(efMeta.getId()),
			/*String sFieldName,*/ efMeta.getField(),
			/*String sDbFieldName,*/ efMeta.getDbColumn(),
			/*String sLabel,*/ efMeta.getFallbacklabel(),
			/*String sDescription,*/ null,
			/*String sDefaultValue,*/ efMeta.getDefaultValue(),
			/*String sForeignEntityName,*/ efMeta.getForeignEntity(),
			/*String sForeignEntityFieldName,*/ efMeta.getForeignEntityField(),
			/*String sUnreferencedForeignEntityName, */ efMeta.getUnreferencedForeignEntity(),
			/*String sUnreferencedForeignEntityFieldName, */ efMeta.getUnreferencedForeignEntityField(),
			/*Class<?> clsDataType,*/ clsDataType,
			/*Integer iDataScale,*/ efMeta.getScale(),
			/*Integer iDataPrecision,*/ efMeta.getPrecision(),
			/*String sInputFormat,*/ efMeta.getFormatInput(),
			/*String sOutputFormat,*/ efMeta.getFormatOutput(),
			/*boolean bNullable,*/ efMeta.isNullable(),
			/*boolean bSearchable,*/ efMeta.isSearchable(),
			/*boolean bUnique,*/ efMeta.isUnique(),
			/*boolean bInvariant, */ false,
			/*boolean bLogToLogbook,*/ efMeta.isLogBookTracking(),
			/*java.util.Date dateCreatedAt,*/ efMeta.getCreatedAt(),
			/*String sCreatedBy,*/ efMeta.getCreatedBy(),
			/*java.util.Date dateChangedAt,*/ efMeta.getChangedAt(),
			/*String sChangedBy,*/ efMeta.getChangedBy(),
			/*Integer iVersion,*/ efMeta.getVersion(),
			/*String resourceIdForLabel,*/ efMeta.getLocaleResourceIdForLabel(),
			/*String resourceIdForDescription*/ efMeta.getLocaleResourceIdForDescription(),
			/*boolean bIndexed*/ Boolean.TRUE.equals(efMeta.isIndexed()),
			/*Integer iOrder*/ efMeta.getOrder()
		);

		return mdfmeta;
	}

	/**
	 *
	 * @param efValue
	 * @return
	 */
	public static AttributeValueVO getAttributeValueVO(MasterDataVO md) {
		AttributeValueVO vo = new AttributeValueVO(
			new NuclosValueObject(
				md.getIntId(),
				md.getCreatedAt(),
				md.getCreatedBy(),
				md.getChangedAt(),
				md.getChangedBy(),
				md.getVersion()),
			md.getField("name", String.class),
			md.getField("mnemonic", String.class),
			md.getField("description", String.class),
			md.getField("validFrom", Date.class),
			md.getField("validUntil", Date.class));

		return vo;
	}

	/**
	 *
	 * @return
	 */
	public static Transformer<MasterDataVO, EntityObjectVO> getTransformerToEntityObjectVO() {
		return new Transformer<MasterDataVO, EntityObjectVO>() {
			@Override
            public EntityObjectVO transform(MasterDataVO md) {
                return DalSupportForMD.getEntityObjectVO(md);
            }
		};
	}

}
