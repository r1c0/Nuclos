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
package org.nuclos.server.statemodel.valueobject;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collection.multimap.MultiListHashMap;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.resource.valueobject.ResourceVO;

/**
 * Value object representing a state.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.00
 */
public class StateVO extends NuclosValueObject {

	private Integer iClientId;
	private Integer iNumeral;
	private String sStatename;
	private String sDescription;
	private Integer iModelId;
	private NuclosImage bIcon;
	private String sButtonLabel;
	private ResourceVO resButtonIcon;
	private String sColor;
	private String sTab;
	private boolean bAuto;
	private UserRights userrights = new UserRights();
	private UserFieldRights userfieldrights = new UserFieldRights();
	private UserSubformRights usersubformrights = new UserSubformRights();
	private Set<MandatoryFieldVO> mandatoryFields = new HashSet<MandatoryFieldVO>();
	private Set<MandatoryColumnVO> mandatoryColumns = new HashSet<MandatoryColumnVO>();

	/**
	 * constructor to be called by server only
	 *
	 * @param iNumeral				state mnemonic of underlying database record
	 * @param sStatename			state name of underlying database record
	 * @param sDescription		model description of underlying database record
	 */
	public StateVO(NuclosValueObject nvo, Integer iNumeral, String sStatename, String sDescription, NuclosImage bIcon, Integer iModelId) {
		super(nvo);
		this.iClientId = nvo.getId();
		this.iNumeral = iNumeral;
		this.bIcon = bIcon;
		this.sStatename = sStatename;
		this.sDescription = sDescription;
		this.iModelId = iModelId;
	}

	/**
	 * constructor to be called by client only
	 *
	 * @param iClientId			primary key of underlying database record
	 * @param iNumeral			 state mnemonic of underlying database record
	 * @param sStatename		 state name of underlying database record
	 * @param sDescription	 model description of underlying database record
	 */
	public StateVO(Integer iClientId, Integer iNumeral, String sStatename, String sDescription, NuclosImage bIcon, Integer iModelId) {
		super();
		this.iClientId = iClientId;
		this.iNumeral = iNumeral;
		this.bIcon = bIcon;
		this.sStatename = sStatename;
		this.sDescription = sDescription;
		this.iModelId = iModelId;
	}

	/**
	 * get copy of primary key of underlying database record (or id of new states inserted by client)
	 *
	 * @return primary key of underlying database record
	 */
	public Integer getClientId() {
		return iClientId;
	}

	/**
	 * get mnemonic of underlying database record
	 *
	 * @return mnemonic of underlying database record
	 */
	public Integer getNumeral() {
		return iNumeral;
	}

	/**
	 * set mnemonic of underlying database record
	 *
	 * @param iNumeral mnemonic of underlying database record
	 */
	public void setNumeral(Integer iNumeral) {
		this.iNumeral = iNumeral;
	}

	/**
	 * get icon of underlying database record
	 *
	 * @return icon of underlying database record
	 */
	public NuclosImage getIcon() {
		return bIcon;
	}

	/**
	 * set icon of underlying database record
	 *
	 * @param bIcon icon of underlying database record
	 */
	public void setIcon(NuclosImage bIcon) {
		this.bIcon = bIcon;
	}
	
	/**
	 * get button icon of underlying database record
	 *
	 * @return button icon of underlying database record
	 */
	public ResourceVO getButtonIcon() {
		return resButtonIcon;
	}

	/**
	 * set button icon of underlying database record
	 *
	 * @param resButtonIcon button icon of underlying database record
	 */
	public void setButtonIcon(ResourceVO resButtonIcon) {
		this.resButtonIcon = resButtonIcon;
	}

	/**
	 * get color of underlying database record
	 *
	 * @return color of underlying database record
	 */
	public String getColor() {
		return sColor;
	}

	/**
	 * set color of underlying database record
	 *
	 * @param sColor color of underlying database record
	 */
	public void setColor(String sColor) {
		this.sColor = sColor;
	}

	/**
	 * get state name of underlying database record
	 *
	 * @return state name of underlying database record
	 */
	public String getStatename() {
		return sStatename;
	}

	/**
	 * set state name of underlying database record
	 *
	 * @param sStatename state name of underlying database record
	 */
	public void setStatename(String sStatename) {
		this.sStatename = sStatename;
	}

	public String getButtonLabel() {
		return sButtonLabel;
	}

	public void setButtonLabel(String sButtonLabel) {
		this.sButtonLabel = sButtonLabel;
	}

	/**
	 * get state description of underlying database record
	 *
	 * @return state description of underlying database record
	 */
	public String getDescription() {
		return sDescription;
	}

	/**
	 * set state description of underlying database record
	 *
	 * @param sDescription state description of underlying database record
	 */
	public void setDescription(String sDescription) {
		this.sDescription = sDescription;
	}

	public UserRights getUserRights() {
		return userrights;
	}

	/**
	 * get id of the related state model
	 *
	 * @return id of the related state model
	 */
	public Integer getModelId() {
		return iModelId;
	}

	/**
	 * set id of the related state model
	 *
	 * @param iModelId id of the related state model
	 */
	public void setModelId(Integer iModelId) {
		this.iModelId = iModelId;
	}

	/**
	 * @param userrights
	 * @precondition userrights != null
	 */
	public void setUserRights(UserRights userrights) {
		if (userrights == null) {
			throw new NullArgumentException("userrights");
		}
		this.userrights = userrights;
	}

	/**
     * @return the userfieldrights
     */
    public UserFieldRights getUserFieldRights() {
    	return userfieldrights;
    }

	/**
     * @param userfieldrights the userfieldrights to set
     */
    public void setUserFieldRights(UserFieldRights userfieldrights) {
    	if (userfieldrights == null) {
			throw new NullArgumentException("userfieldrights");
		}
    	this.userfieldrights = userfieldrights;
    }

	public UserSubformRights getUserSubformRights() {
		return usersubformrights;
	}

	/**
	 * @param usersubformrights
	 * @precondition usersubformrights != null
	 */
	public void setUserSubformRights(UserSubformRights usersubformrights) {
		if (usersubformrights == null) {
			throw new NullArgumentException("usersubformrights");
		}
		this.usersubformrights = usersubformrights;
	}

	@Override
	public String toString() {
		return this.getNumeral() == null ? "" : this.getNumeral() + " " + this.getStatename();
	}
	
	public void setTabbedPaneName(String sTab) {
		this.sTab = sTab;
	}
	
	public String getTabbedPaneName() {
		return sTab;
	}
	
	public void setFromAutomatic(boolean bAuto) {
		this.bAuto = bAuto;
	}
	
	public boolean isFromAutomatic() {
		return bAuto;
	}
	
	/**
     * @param mandatoryFields the mandatoryFields to set
     */
    public void setMandatoryFields(Set<MandatoryFieldVO> mandatoryFields) {
    	if (mandatoryFields == null) {
			throw new NullArgumentException("mandatoryFields");
		}
	    this.mandatoryFields = mandatoryFields;
    }

	/**
     * @return the mandatoryFields
     */
    public Set<MandatoryFieldVO> getMandatoryFields() {
	    return mandatoryFields;
    }

	/**
     * @param mandatoryColumns the mandatoryColumns to set
     */
    public void setMandatoryColumns(Set<MandatoryColumnVO> mandatoryColumns) {
    	if (mandatoryColumns == null) {
			throw new NullArgumentException("mandatoryColumns");
		}
	    this.mandatoryColumns = mandatoryColumns;
    }

	/**
     * @return the mandatoryColumns
     */
    public Set<MandatoryColumnVO> getMandatoryColumns() {
	    return mandatoryColumns;
    }

	/**
	 * maps a role id to a collection of <code>AttributegroupPermissionVO</code>.
	 */
	public static class UserRights extends MultiListHashMap<Integer, AttributegroupPermissionVO> {
	}
	
	/**
	 * maps a role id to a collection of <code>AttributegroupPermissionVO</code>.
	 */
	public static class UserFieldRights extends MultiListHashMap<Integer, EntityFieldPermissionVO> {
	}

	/**
	 * maps a role id to a collection of <code>SubformPermissionVO</code>.
	 */
	public static class UserSubformRights extends MultiListHashMap<Integer, SubformPermissionVO> {
	}

}	// class StateVO
