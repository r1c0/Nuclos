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
package org.nuclos.client.genericobject;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MasterDataModuleDelegate;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Encapsulation of the (generic object related) modules in a Nucleus application.
 * Note that the entity corresponding to the module is contained in
 * <code>MasterDataVO.getField("entity").getFieldValue()</code>,
 * not in <code>MasterDataVO.getEntity()</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Modules extends ModuleProvider {

	private final Logger log = Logger.getLogger(this.getClass());

	public static synchronized Modules getInstance() {
		return (Modules) SpringApplicationContextHolder.getBean("moduleProvider");
	}

	private final ConcurrentHashMap<Integer, String> labelsById = new ConcurrentHashMap<Integer, String>();


	@Override
	protected Collection<MasterDataVO> getModules() {
		return fillLocales(MasterDataDelegate.getInstance().getMasterData(NuclosEntity.MODULE.getEntityName()));
	}

	public static void initialize() throws Exception {
		getInstance().setModules(getInstance().getModules());
	}

	/**
	 * @param iModuleId a valid module id or <code>null</code> for "generalsearch".
	 * @return the name of the module (that is, the label of the entity) corresponding to the given module id
	 * or the name of the pseudo-module "generalsearch" if the given module id is <code>null</code>.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	@Override
	public String getEntityLabelByModuleId(Integer iModuleId) throws NoSuchElementException {
		String label = labelsById.get(iModuleId);
		if (label == null) {
			label = CommonLocaleDelegate.getResource(MasterDataModuleDelegate.getInstance().getResourceSIdForLabel(iModuleId), this.getModuleById(iModuleId).getField("name", String.class));
			labelsById.put(iModuleId, label);
			assert label != null;
		}
		return label;
	}

	/**
	 * @param sModuleName a valid module name or <code>null</code> for "generalsearch".
	 * @return the name of the module (that is, the label of the entity) corresponding to the given module id
	 * or the name of the pseudo-module "generalsearch" if the given module id is <code>null</code>.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	@Override
	public String getEntityLabelByModuleName(String sModuleName) throws NoSuchElementException {
		Integer iModuleId = (sModuleName == null) ? null : this.getModuleIdByEntityName(sModuleName);
		return getEntityLabelByModuleId(iModuleId);
	}

}	// class Modules
