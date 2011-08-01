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
package org.nuclos.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Encapsulation of the (generic object related) modules in a Nucleus application.
 * Note that the entity corresponding to the module is contained in
 * <code>MasterDataVO.getField("entity").getFieldValue()</code>,
 * not in <code>MasterDataVO.getEntity()</code>.
 * There is an artificial ("pseudo-")entity named "generalsearch". This one is not contained in the module table in
 * the database and it does not count to the number of modules.
 * There is another artificial ("pseudo-")entity named "fulltextsearch". This one is not contained in the module table in
 * the database and it does not count to the number of modules. Its mudule ID is '-2'.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class ModuleProvider {

	private Map<Object, MasterDataVO> mpModulesById = CollectionUtils.newHashMap();
	private Map<String, MasterDataVO> mpModulesByEntityName = CollectionUtils.newHashMap();

	protected abstract Collection<MasterDataVO> getModules();

	protected void setModules(Collection<MasterDataVO> collmdvoModules) {
		mpModulesById = CollectionUtils.newHashMap();
		mpModulesByEntityName = CollectionUtils.newHashMap();
		for (MasterDataVO mdvo : collmdvoModules) {
			mpModulesById.put(mdvo.getId(), mdvo);
			mpModulesByEntityName.put(mdvo.getField("entity", String.class), mdvo);
		}
	}

	protected static Collection<MasterDataVO> fillLocales(Collection<MasterDataVO> collmdvoModules) {
		for (MasterDataVO mdvo : collmdvoModules) {
			if (mdvo.getField("name") != null )
				mdvo.setField("name", CommonLocaleDelegate.getText(mdvo.getField("name", String.class),null));
			if (mdvo.getField("description") != null )
				mdvo.setField("description", CommonLocaleDelegate.getText(mdvo.getField("description", String.class),null));
			if (mdvo.getField("menupath") != null )
				mdvo.setField("menupath", CommonLocaleDelegate.getText(mdvo.getField("menupath", String.class),null));
			if (mdvo.getField("treeview") != null )
				mdvo.setField("treeview", CommonLocaleDelegate.getText(mdvo.getField("treeview", String.class),null));
			if (mdvo.getField("treeviewdescription") != null )
				mdvo.setField("treeviewdescription", CommonLocaleDelegate.getText(mdvo.getField("treeviewdescription", String.class),null));
		}
		return collmdvoModules;
	}

	public void invalidate() {
		setModules(getModules());
	}

	/**
	 * @return List<MasterDataVO>. The list of modules, ordered by their ids.
	 * Note that the entity corresponding to the module is contained in
	 * <code>MasterDataVO.getField("entity")</code>, not in <code>MasterDataVO.getEntity()</code>.
	 * The artificial entity "generalsearch" is not contained in the result.
	 */
	public List<MasterDataVO> getModules(boolean bIncludeSubModules) {
		final List<MasterDataVO> result = new ArrayList<MasterDataVO>(this.mpModulesById.values());

		if (!bIncludeSubModules) {
			CollectionUtils.removeAll(result, new IsSubModule());
		}

		// sort by module id:
		Collections.sort(result, new Comparator<MasterDataVO>() {
			@Override
			public int compare(MasterDataVO mdvo1, MasterDataVO mdvo2) {
				return LangUtils.compare(mdvo1.getId(), mdvo2.getId());
			}
		});

		return result;
	}

	/**
	 * @return the number of modules in the application.
	 * The artificial entity "generalsearch" is not being counted in.
	 */
	public int getModuleCount(boolean bIncludeSubModules) {
		return bIncludeSubModules ? mpModulesById.size() : this.getModules(false).size();
	}

	/**
	 * @param iModuleId
	 * @return the module corresponding to the given module id.
	 * Note that the entity corresponding to the module is contained in
	 * <code>MasterDataVO.getField("entity")</code>, not in <code>MasterDataVO.getEntity()</code>.
	 * The artificial entity "generalsearch" is never found.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	public MasterDataVO getModuleById(int iModuleId) throws NoSuchElementException {

		final MasterDataVO result = this.mpModulesById.get(iModuleId);
		if (result == null) {
			throw new NoSuchElementException(String.format("A module with entity id %d does not exist", iModuleId));
		}
		assert result != null;
		return result;
	}

	/**
	 * @param sEntityName
	 * @return the module corresponding to the given entity name.
	 * The artificial entity "generalsearch" is never found.
	 * @throws NoSuchElementException if there is no module with the given entity name.
	 * @postcondition result != null
	 */
	public MasterDataVO getModuleByEntityName(String sEntityName) throws NoSuchElementException {
		final MasterDataVO result = this.mpModulesByEntityName.get(sEntityName);
		if (result == null) {
			throw new NoSuchElementException(String.format("A module with entity name %s does not exist", sEntityName));
		}
		assert result != null;
		return result;
	}

	public boolean existModule(String sEntityName) {
		return this.mpModulesByEntityName.containsKey(sEntityName) && this.isModuleEntity(sEntityName);
	}

	/**
	 * @param sEntityName
	 * @return Is the entity with the given name a module entity?
	 */
	public boolean isModuleEntity(String sEntityName) {
		return this.mpModulesByEntityName.get(sEntityName) != null;
	}

	/**
	 * @param sEntityName
	 * @return the module id corresponding to the given entity or <code>null</code> for "generalsearch".
	 * @throws NoSuchElementException if there is no module with the given entity name.
	 * @precondition sEntityName != null
	 */
	public Integer getModuleIdByEntityName(String sEntityName) throws NoSuchElementException {
		return this.getModuleByEntityName(sEntityName).getIntId();
	}

	/**
	 * @param iModuleId a valid module id or <code>null</code> for "generalsearch".
	 * @return the name of the entity corresponding to the given module id or "generalsearch" if the given module id is <code>null</code>.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	public String getEntityNameByModuleId(Integer iModuleId) throws NoSuchElementException {
		final String result = this.getModuleById(iModuleId).getField("entity", String.class);
		assert result != null;
		return result;
	}

	/**
	 * @param iModuleId a valid module id or <code>null</code> for "generalsearch".
	 * @return the name of the module (that is, the label of the entity) corresponding to the given module id
	 * or the name of the pseudo-module "generalsearch" if the given module id is <code>null</code>.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	public String getEntityLabelByModuleId(Integer iModuleId) throws NoSuchElementException {
		final String result = this.getModuleById(iModuleId).getField("name", String.class);
		assert result != null;
		return result;
	}

	/**
	 * @param sModuleName a valid module name or <code>null</code> for "generalsearch".
	 * @return the name of the module (that is, the label of the entity) corresponding to the given module id
	 * or the name of the pseudo-module "generalsearch" if the given module id is <code>null</code>.
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @postcondition result != null
	 */
	public String getEntityLabelByModuleName(String sModuleName) throws NoSuchElementException {
		Integer iModuleId = (sModuleName == null) ? null : this.getModuleIdByEntityName(sModuleName);
		final String result = this.getModuleById(iModuleId).getField("name", String.class);
		assert result != null;
		return result;
	}

	/**
	 * @param iModuleId
	 * @return Does the module with the given id use a state model?
	 */
	public boolean getUsesStateModel(int iModuleId) {
//		final Boolean bUsesStateModel = this.getModuleById(iModuleId).getField("usesStateModel", Boolean.class);
//		return bUsesStateModel != null && bUsesStateModel.booleanValue();
		MetaDataProvider metaprovider = SpringApplicationContextHolder.getBean(MetaDataProvider.class);
		return metaprovider.getEntity(LangUtils.convertId(iModuleId)).isStateModel();
	}

	/**
	 * @param iModuleId
	 * @return Is Logbook tracking enabled for the module with the given id?
	 */
	public boolean isLogbookTracking(int iModuleId) {
		final Boolean bLogbookTracking = this.getModuleById(iModuleId).getField("logbookTracking", Boolean.class);
		return bLogbookTracking != null && bLogbookTracking.booleanValue();
	}

	/**
	 * @param iModuleId
	 * @return Does the module with the given id use the rule engine?
	 */
	public boolean getUsesRuleEngine(int iModuleId) {
		/** @todo this could be a separate flag in T_MD_MODULE */
		return this.isMainModule(iModuleId);
	}

	/**
	 * @param iModuleId
	 * @return Is the module with the given id a main module?
	 */
	public boolean isMainModule(int iModuleId) {
		return !this.isSubModule(iModuleId);
	}

	/**
	 * @param iModuleId
	 * @return Is the module with the given id a submodule?
	 */
	public boolean isSubModule(int iModuleId) {
		return (iModuleId != -2) && isSubModule(this.getModuleById(iModuleId));
	}

	/**
	 * @param iModuleId
	 * @throws NoSuchElementException if there is no module with the given id.
	 * @return the mnemonic for the system identifer to be used for objects of the given module.
	 */
	public String getSystemIdentifierMnemonic(int iModuleId) {
		return this.getModuleById(iModuleId).getField("systemIdentifierMnemonic", String.class);
	}

	/**
	 * @param mdvoModule
	 * @return the name of the given module's parent module, if any.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public static String getParentModuleName(MasterDataVO mdvoModule) {
		return mdvoModule.getField("parentModule", String.class);
	}

	private static boolean isSubModule(MasterDataVO mdvoModule) {
		return getParentModuleName(mdvoModule) != null;
	}

	/**
	 * @param sEntityName
	 * @return the name of the parent entity, if any, of the given entity.
	 * 
	 * @deprecated Parent is no longer part of the entity model.
	 */
	public String getParentEntityName(String sEntityName) {
		return getParentModuleName(getModuleByEntityName(sEntityName));
	}

	public List<String> getMenuPath(MasterDataVO mdvoModule){
		final String sMenuPath = mdvoModule.getField("menupath", String.class);
		final List<String> result;
		if (sMenuPath == null) {
			result = null;
		}
		else {
			result = Arrays.asList(sMenuPath.split("\\\\"));
		}
		return result;
	}

	public String getTreeView(MasterDataVO mdvoModule) {
		return mdvoModule.getField("treeview", String.class);
	}

	private static class IsSubModule implements Predicate<MasterDataVO> {
		@Override
		public boolean evaluate(MasterDataVO mdvo) {
			return isSubModule(mdvo);
		}
	}

	public Boolean isImportExportable(String sEntityName) {
		return (Boolean)getModuleByEntityName(sEntityName).getField("importexport");
	}

	public static boolean isGeneralSearchModuleId(Integer iModuleId) {
		return iModuleId == null || iModuleId == -1;
	}

}	// class ModuleProvider
