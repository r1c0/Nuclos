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
package org.nuclos.server.genericobject.valueobject;

import org.nuclos.common.AttributeProvider;
import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaFieldVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Value object representing a logbook entry.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class LogbookVO extends NuclosValueObject {
	private Integer iGenericObjectId;
	private Integer iAttributeId;
	private Integer iMasterDataMetaId;
	private Integer iMasterDataMetaFieldId;
	private Integer iMasterDataRecordId;
	private String sMasterDataAction;
	private Integer iOldValueId;
	private Integer iOldValueExternalId;
	private String sOldValue;
	private Integer iNewValueId;
	private Integer iNewValueExternalId;
	private String sNewValue;
	private String sLabel;

	private boolean isMigrated; // formerly expressed via label "(migriert)"

	/**
	 * constructor to be called by server only
	 * @param evo contains the common fields.
	 * @param iGenericObjectId leased object id of underlying database record
	 * @param iAttributeId attribute id of underlying database record
	 * @param iMasterDataMetaId id of the masterdata meta record
	 * @param iMasterDataMetaFieldId id of the masterdata meta field record
	 * @param iMasterDataRecordId id of the actual masterdata record
	 * @param sMasterDataAction action executed on masterdata record
	 * @param iOldValueId old value id of underlying database record
	 * @param iOldValueExternalId old value external id of underlying database record
	 * @param sOldValue old value of underlying database record
	 * @param iNewValueId new value id of underlying database record
	 * @param iNewValueExternalId new value external id of underlying database record
	 * @param sNewValue new value of underlying database record
	 */
	public LogbookVO(NuclosValueObject evo, Integer iGenericObjectId, Integer iAttributeId,
			Integer iMasterDataMetaId, Integer iMasterDataMetaFieldId, Integer iMasterDataRecordId, String sMasterDataAction,
			Integer iOldValueId, Integer iOldValueExternalId, String sOldValue, Integer iNewValueId, Integer iNewValueExternalId, String sNewValue) {
		super(evo);
		this.iGenericObjectId = iGenericObjectId;
		this.iAttributeId = iAttributeId;
		this.iMasterDataMetaId = iMasterDataMetaId;
		this.iMasterDataMetaFieldId = iMasterDataMetaFieldId;
		this.iMasterDataRecordId = iMasterDataRecordId;
		this.sMasterDataAction = sMasterDataAction;
		this.iOldValueId = iOldValueId;
		this.iOldValueExternalId = iOldValueExternalId;
		this.sOldValue = sOldValue;
		this.iNewValueId = iNewValueId;
		this.iNewValueExternalId = iNewValueExternalId;
		this.sNewValue = sNewValue;

		this.determineLabel();
	}

	private void determineLabel() {
		if (getAttribute() != null) {
			this.sLabel = SpringApplicationContextHolder.getBean(AttributeProvider.class).getAttribute(getAttribute()).getResourceSIdForLabel();
		}
		else if (getMasterDataMetaFieldId() != null) {
			MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
			if (cache != null) {
				final MasterDataMetaVO mdmetavo = cache.getMetaDataById(iMasterDataMetaId);
				final MasterDataMetaFieldVO mdmfvo = mdmetavo.getFieldById(iMasterDataMetaFieldId);
				//this.sLabel = mdmetavo.getLabel() + "." + mdmfvo.getLabel();
				if (!StringUtils.isNullOrEmpty(mdmetavo.getResourceSIdForLabel()) && !StringUtils.isNullOrEmpty(mdmfvo.getResourceSIdForLabel())) {
					this.sLabel = mdmetavo.getResourceSIdForLabel() + "." + mdmfvo.getResourceSIdForLabel();
				}
				else {
					this.sLabel = 	mdmetavo.getLabel() + "." + mdmfvo.getLabel();
				}
			}
		}
		else {
			isMigrated = true;
		}
	}

	/**
	 * get leased object id of underlying database record
	 * @return leased object id of underlying database record
	 */
	public Integer getGenericObject() {
		return iGenericObjectId;
	}

	/**
	 * get attribute id of underlying database record
	 * @return attribute id of underlying database record
	 */
	public Integer getAttribute() {
		return iAttributeId;
	}

	/**
	 * get id of underlying masterdata entity meta record
	 * @return id of underlying masterdata entity meta record
	 */
	public Integer getMasterDataMetaId() {
		return iMasterDataMetaId;
	}

	/**
	 * get id of underlying masterdata meta field record
	 * @return id of underlying masterdata meta field record
	 */
	public Integer getMasterDataMetaFieldId() {
		return iMasterDataMetaFieldId;
	}

	/**
	 * get id of underlying masterdata database record
	 * @return id of underlying masterdata database record
	 */
	public Integer getMasterDataRecordId() {
		return iMasterDataRecordId;
	}

	/**
	 * get action executed on protocolled masterdata record
	 * @return action executed on protocolled masterdata record
	 */
	public String getMasterDataAction() {
		return sMasterDataAction;
	}

	/**
	 * get old value id of underlying database record
	 * @return old value id of underlying database record
	 */
	public Integer getOldValueId() {
		return iOldValueId;
	}

	/**
	 * get old value external id of underlying database record
	 * @return old value external id of underlying database record
	 */
	public Integer getOldValueExternalId() {
		return iOldValueExternalId;
	}

	/**
	 * get old value of underlying database record
	 * @return old value of underlying database record
	 */
	public String getOldValue() {
		return sOldValue;
	}

	/**
	 * get new value id of underlying database record
	 * @return new value id of underlying database record
	 */
	public Integer getNewValueId() {
		return iNewValueId;
	}

	/**
	 * get new value external id of underlying database record
	 * @return new value external id of underlying database record
	 */
	public Integer getNewValueExternalId() {
		return iNewValueExternalId;
	}

	/**
	 * get new value of underlying database record
	 * @return new value of underlying database record
	 */
	public String getNewValue() {
		return sNewValue;
	}

	/**
	 * get the label of the logged component (either attribute or subform)
	 * @return the label of the logged component (either attribute or subform)
	 */
	public String getLabel() {
		if (sLabel.contains(".")) {
			String sResource_1 = sLabel.substring(0, sLabel.indexOf('.'));
			String sResource_2 = sLabel.substring(sLabel.indexOf('.')+1, sLabel.length());
			if (CommonLocaleDelegate.isResourceId(sResource_1) && CommonLocaleDelegate.isResourceId(sResource_2)) {
				sLabel = CommonLocaleDelegate.getText(sResource_1, null) + "." + CommonLocaleDelegate.getText(sResource_2, null);
			}
		}
		else {
			sLabel = CommonLocaleDelegate.getText(sLabel, sLabel);
		}
		return sLabel;
	}


	/**
	 * Returns true, if neither master data field id nor attribute are set
	 * @return
	 */
	public boolean isMigrated() {
		return isMigrated;
	}
}
