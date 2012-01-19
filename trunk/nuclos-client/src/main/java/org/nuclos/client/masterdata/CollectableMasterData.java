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
package org.nuclos.client.masterdata;

import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.AbstractCollectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityProvider;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldFormat;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Removable;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Makes a <code>MasterDataVO</code> <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class CollectableMasterData extends AbstractCollectable implements Removable {

	private static final Logger LOG = Logger.getLogger(CollectableMasterData.class);

	public static final String FIELDNAME_NAME = MasterDataVO.FIELDNAME_NAME;
	public static final String FIELDNAME_MNEMONIC = MasterDataVO.FIELDNAME_MNEMONIC;
	public static final String FIELDNAME_DESCRIPTION = MasterDataVO.FIELDNAME_DESCRIPTION;

	private final MasterDataVO mdvo;
	private final CollectableEntity clcte;
	private final Map<String, CollectableField> mpFields = CollectionUtils.newHashMap();

	// map for dependant child subform data
	private DependantCollectableMasterDataMap depclctmd = new DependantCollectableMasterDataMap();
	private DependantMasterDataMap depmd = new DependantMasterDataMap();

	public CollectableMasterData(CollectableEntity clcte, MasterDataVO mdvo) {
		this.mdvo = mdvo;
		this.clcte = clcte;
	}

	@Override
	public void markRemoved() {
		this.getMasterDataCVO().remove();
	}

	@Override
	public boolean isMarkedRemoved() {
		return this.getMasterDataCVO().isRemoved();
	}

	@Override
	public Object getId() {
		return this.getMasterDataCVO().getId();
	}

	public CollectableEntity getCollectableEntity() {
		return this.clcte;
	}

	public MasterDataVO getMasterDataCVO() {
		return this.mdvo;
	}

	@Override
	public Object getValue(String sFieldName) {
		return this.mdvo.getField(sFieldName);
	}

	/**
	 * @return the value of the identifying field, if any. Otherwise the id as String.
	 */
	@Override
	public String getIdentifierLabel() {
		final String sNameField = this.getCollectableEntity().getIdentifierFieldName();
		return (sNameField != null) ? this.getField(sNameField).toString() : this.mdvo.getId().toString();
	}

	@Override
	public int getVersion() {
		return this.mdvo.getVersion();
	}

	@Override
	public CollectableField getField(String sFieldName) {
		// mpFields is used as a cache:
		CollectableField result = this.mpFields.get(sFieldName);
		if (result == null) {
			result = new CollectableMasterDataField(this, sFieldName);
			this.mpFields.put(sFieldName, result);
		}
		assert result != null;

		return result;
	}

	@Override
	public void setField(String sFieldName, CollectableField clctfValue) {
		if (sFieldName.equals("entityfieldDefault")) {
			try {
				String s = CollectableFieldFormat.getInstance(Class.forName(mdvo.getField("datatype").toString())).format(null, clctfValue.getValue());
				this.mdvo.setField(sFieldName, s);
			}
			catch(ClassNotFoundException e) {
				throw new NuclosFatalException(e);
			}
		}
		else {
			this.mdvo.setField(sFieldName, clctfValue.getValue());
		}
		if (clcte.getEntityField(sFieldName).isIdField()) {
			this.mdvo.setField(sFieldName + "Id", clctfValue.getValueId());
		}


		// remove the entry from the cache, if there is one already:
		this.mpFields.remove(sFieldName);

		assert this.getField(sFieldName).equals(clctfValue);
	}

	public DependantCollectableMasterDataMap getDependantCollectableMasterDataMap() {
		return this.depclctmd;
	}

	public DependantMasterDataMap getDependantMasterDataMap() {
		return this.depmd;
	}

	public void setDependantMasterDataMap(DependantMasterDataMap depmd) {
		this.depmd = depmd;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("entity=").append(getCollectableEntity());
		result.append(",vo=").append(getMasterDataCVO());
		result.append(",dep=").append(getDependantMasterDataMap());
		result.append(",cdep=").append(getDependantCollectableMasterDataMap());
		result.append(",id=").append(getId());
		result.append(",label=").append(getIdentifierLabel());
		result.append(",complete=").append(isComplete());
		result.append("]");
		return result.toString();
	}

	/**
	 * inner class MakeCollectable: makes a <code>MasterDataVO</code> <code>Collectable</code>.
	 */
	public static class MakeCollectable implements Transformer<MasterDataVO, CollectableMasterData> {
		final CollectableMasterDataEntity clctmde;

		public MakeCollectable(CollectableEntityProvider clcteprovider, String sEntityName) {
			this((CollectableMasterDataEntity) clcteprovider.getCollectableEntity(sEntityName));
		}

		public MakeCollectable(CollectableMasterDataEntity clctmde) {
			this.clctmde = clctmde;
		}

		@Override
		public CollectableMasterData transform(MasterDataVO mdvo) {
			return new CollectableMasterData(this.clctmde, mdvo);
		}

	}

	/**
	 * inner class ExtractMasterDataVO: the inverse operation of <code>MakeCollectable</code>.
	 */
	public static class ExtractMasterDataVO implements Transformer<CollectableMasterData, MasterDataVO> {
		@Override
		public MasterDataVO transform(CollectableMasterData clctmd) {
			DependantMasterDataMap depmdmp = clctmd.getDependantMasterDataMap();
			MasterDataVO mdVO = clctmd.getMasterDataCVO();
			mdVO.setDependants(depmdmp);
			return mdVO;
		}
	}

}	// class CollectableMasterData
