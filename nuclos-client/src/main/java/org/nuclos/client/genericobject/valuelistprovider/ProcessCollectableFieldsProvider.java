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
package org.nuclos.client.genericobject.valuelistprovider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.NuclosFatalException;

/**
 * Value list provider to get processes by usage.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">Christoph Radig</a>
 * @version 00.01.000
 */
public class ProcessCollectableFieldsProvider implements CollectableFieldsProvider {
	private static final Logger log = Logger.getLogger(ProcessCollectableFieldsProvider.class);

	public static final String NAME = "process";
	
	public static final String PARAM_ENTITY_ID = "entityId";
	public static final String PARAM_MODULE_ID = "moduleId";
	
	private Object oEntityId;
	private Object oEntityName;
	private boolean bSearchmode = false;

	/**
	 * valid parameters:
	 * <ul>
	 *   <li>"module" = name of module entity</li>
	 *   <li>"moduleId" = module id</li>
	 *   <li>"_searchmode" = collectable in search mask?
	 * </ul>
	 * @param sName parameter name
	 * @param oValue parameter value
	 */
	@Override
	public void setParameter(String sName, Object oValue) {
		log.debug("setParameter - sName = " + sName + " - oValue = " + oValue);
		if (sName.equals("module")) {
			try {
				final String sEntityName = (String) oValue;
				final Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(sEntityName);
				if (iModuleId == null) {
					throw new IllegalArgumentException("oValue");
				}
				this.oEntityId = iModuleId;
			}
			catch (Exception ex) {
				throw new NuclosFatalException(SpringLocaleDelegate.getInstance().getMessage(
						"ProcessCollectableFieldsProvider.1", "Der Parameter \"module\" muss den Namen einer Modul-Entit\u00e4t enthalten.\n\"{0}\" ist keine g\u00fcltige Modul-Entit\u00e4t.", oValue), ex) ;
			}
		}
		else if(sName.equals("relatedId")) {
			this.oEntityId = oValue;
		}
		else if (sName.equals(PARAM_ENTITY_ID) || sName.equals(PARAM_MODULE_ID)) {
			this.oEntityId = oValue;
		}
		else if (sName.equals("entityName")) {
			this.oEntityName = oValue;
		}
		else if (sName.equals("_searchmode")) {
			bSearchmode = (Boolean) oValue;
		}
		else {
			// ignore
		}
	}

	@Override
	public List<CollectableField> getCollectableFields() throws CommonBusinessException {
		Integer iEntityId = null;
		if(oEntityId instanceof Long){
			iEntityId = ((Long)oEntityId).intValue();
		}
		else {
			iEntityId = (Integer) oEntityId;
		}
		if (iEntityId == null && oEntityName != null) {
			if (oEntityName instanceof String) {
				final String sEntity = (String) oEntityName;
				iEntityId = MetaDataClientProvider.getInstance().getEntity(sEntity).getId().intValue();
			} else {
				iEntityId = Integer.parseInt(oEntityName.toString());
			}
		}
		log.debug("getCollectableFields - iModuleId = " + iEntityId);
		List<CollectableField> result = new ArrayList<CollectableField>();
		for (CollectableField processField : MasterDataDelegate.getInstance().getProcessByUsage(iEntityId, bSearchmode)) {
			if (bSearchmode || SecurityCache.getInstance().isNewAllowedForModuleAndProcess(iEntityId, (Integer) processField.getValueId())) {
				result.add(processField);
			}
		}
		Collections.sort(result);
		return result;
	}

}	// class ProcessCollectableFieldsProvider
