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
package org.nuclos.server.masterdata.ejb3;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
* Facade bean for all meta data management functions.
* <br>
* <br>Created by Novabit Informationssysteme GmbH
* <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
// @Stateless
// @Local(MasterDataModuleFacadeLocal.class)
// @Remote(MasterDataModuleFacadeRemote.class)
@Transactional
public class MasterDataModuleFacadeBean extends NuclosFacadeBean implements MasterDataModuleFacadeRemote {

	private final static String MODULE_TABLE = "t_md_entity";
	
	private static final String MODULE_RESOURCE_FIELDNAMES[] = {
		LocaleUtils.FIELD_LABEL,
		LocaleUtils.FIELD_MENUPATH,
		LocaleUtils.FIELD_DESCRIPTION,
		LocaleUtils.FIELD_TREEVIEW,
		LocaleUtils.FIELD_TREEVIEWDESCRIPTION
	};
	
	private LocaleFacadeLocal localeFacade;	
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public MasterDataModuleFacadeBean() {
	}
	
	@Autowired
	final void setLocaleFacade(LocaleFacadeLocal localeFacade) {
		this.localeFacade = localeFacade;
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	/**
	 * create a new master data record with the given id
	 * @param mdvo the master data record to be created
	 * @param mpDependants map containing dependant masterdata, if any
	 * @return master data value object containing the newly created record
	 * @throws NuclosBusinessRuleException
	 * @precondition sEntityName != null
	 * @precondition mdvo.getId() == null
	 * @precondition (mpDependants != null) --> mpDependants.areAllDependantsNew()
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public MasterDataVO create(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {

		MasterDataVO result = masterDataFacade.create(sEntityName, mdvo, mpDependants);

		LocaleFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		LocaleUtils.setResourceIdForField(MODULE_TABLE, result.getIntId(), LocaleUtils.FIELD_LABEL, facade.setDefaultResource(null, (String)result.getField(LocaleUtils.FIELD_LABEL)));
		LocaleUtils.setResourceIdForField(MODULE_TABLE, result.getIntId(), LocaleUtils.FIELD_DESCRIPTION, facade.setDefaultResource(null, (String)result.getField(LocaleUtils.FIELD_DESCRIPTION)));
		LocaleUtils.setResourceIdForField(MODULE_TABLE, result.getIntId(), LocaleUtils.FIELD_TREEVIEW, facade.setDefaultResource(null, (String)result.getField(LocaleUtils.FIELD_TREEVIEW)));
		LocaleUtils.setResourceIdForField(MODULE_TABLE, result.getIntId(), LocaleUtils.FIELD_TREEVIEWDESCRIPTION, facade.setDefaultResource(null, (String)result.getField(LocaleUtils.FIELD_TREEVIEWDESCRIPTION)));
		LocaleUtils.setResourceIdForField(MODULE_TABLE, result.getIntId(), LocaleUtils.FIELD_MENUPATH, facade.setDefaultResource(null, (String)result.getField(LocaleUtils.FIELD_MENUPATH)));

		return result;

	}

	/**
	 * modifies an existing master data record.
	 * @param sEntityName name of current entity
	 * @param mdvo the master data record
	 * @param mpDependants map containing dependant masterdata, if any
	 * @param mpDependants map containing dependant masterdata and its id
	 * @return id of the modified master data record
	 * @throws NuclosBusinessRuleException
	 * @precondition sEntityName != null
	 * @nucleus.permission checkWriteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public Object modify(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException,
			CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {
		Object result = masterDataFacade.modify(sEntityName, mdvo, mpDependants);

		
		for (String resFieldName : MODULE_RESOURCE_FIELDNAMES) {
			String resId = getResourceSIdByFieldName(resFieldName, mdvo.getIntId());
			
			updateResourceIdForField(resId, resFieldName, mdvo);
		}
		
		return result;
	}
	
	private void updateResourceIdForField(String resId, String resFieldName, MasterDataVO mdvo) {
		LocaleFacadeLocal locale = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);
		String text = (String) mdvo.getField(resFieldName);
		if (resId != null) {			
			if (StringUtils.isNullOrEmpty(text)) {
				LocaleUtils.setResourceIdForField(MODULE_TABLE, mdvo.getIntId(), resFieldName, null);
				locale.deleteResource(resId);				
			} 
			else {
				locale.updateResource(resId, text);
			}
		} 
		else {
			LocaleUtils.setResourceIdForField(MODULE_TABLE, mdvo.getIntId(), resFieldName, locale.setDefaultResource(null, text));
		}
	}
	
	private String getResourceSIdByFieldName(String resFieldName, Integer iId) {
		if (resFieldName.equals(LocaleUtils.FIELD_LABEL)) {
			return getResourceSIdForLabel(iId);
		} else if (resFieldName.equals(LocaleUtils.FIELD_MENUPATH)) {
			return getResourceSIdForMenuPath(iId);
		} else if (resFieldName.equals(LocaleUtils.FIELD_DESCRIPTION)) {
			return getResourceSIdForDescription(iId);
		} else if (resFieldName.equals(LocaleUtils.FIELD_TREEVIEW)) {
			return getResourceSIdForTreeView(iId);
		} else if (resFieldName.equals(LocaleUtils.FIELD_TREEVIEWDESCRIPTION)) {
			return getResourceSIdForTreeViewDescription(iId);
		}
		return null;
	}

	/**
	 * method to delete an existing master data record
	 * @param mdvo containing the master data record
	 * @param bRemoveDependants remove all dependants if true, else remove only given (single) mdvo record
	 * 			this is helpful for entities which have no layout
	 * @precondition sEntityName != null
	 * @nucleus.permission checkDeleteAllowed(sEntityName)
	 */
	@RolesAllowed("Login")
	public void remove(String sEntityName, MasterDataVO mdvo, boolean bRemoveDependants) throws NuclosBusinessRuleException, CommonPermissionException,
								CommonStaleVersionException, CommonRemoveException, CommonFinderException {

		LocaleFacadeLocal facade = ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		facade.deleteResource(getResourceSIdForLabel(mdvo.getIntId()));
		facade.deleteResource(getResourceSIdForDescription(mdvo.getIntId()));
		facade.deleteResource(getResourceSIdForTreeView(mdvo.getIntId()));
		facade.deleteResource(getResourceSIdForTreeViewDescription(mdvo.getIntId()));
		facade.deleteResource(getResourceSIdForMenuPath(mdvo.getIntId()));

		masterDataFacade.remove(sEntityName, mdvo, bRemoveDependants);
	}

	public String getResourceSIdForLabel(Integer iId) {
		return LocaleUtils.getResourceIdForField(MODULE_TABLE, iId, LocaleUtils.FIELD_LABEL);
	}

	public String getResourceSIdForDescription(Integer iId) {		
		return LocaleUtils.getResourceIdForField(MODULE_TABLE, iId, LocaleUtils.FIELD_DESCRIPTION);
	}

	public String getResourceSIdForTreeView(Integer iId) {		
		return LocaleUtils.getResourceIdForField(MODULE_TABLE, iId, LocaleUtils.FIELD_TREEVIEW);
	}

	public String getResourceSIdForTreeViewDescription(Integer iId) {		
		return LocaleUtils.getResourceIdForField(MODULE_TABLE, iId, LocaleUtils.FIELD_TREEVIEWDESCRIPTION);
	}

	public String getResourceSIdForMenuPath(Integer iId) {		
		return LocaleUtils.getResourceIdForField(MODULE_TABLE, iId, LocaleUtils.FIELD_MENUPATH);		
	}
}
