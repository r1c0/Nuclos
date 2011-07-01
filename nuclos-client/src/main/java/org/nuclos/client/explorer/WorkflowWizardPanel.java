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
package org.nuclos.client.explorer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import org.nuclos.client.attribute.AttributeDelegate;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.Main;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilters;
import org.nuclos.client.searchfilter.SearchFilters.EntitySearchFilters;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.task.TaskController;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.StateVO;

public class WorkflowWizardPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	String sUsername;
	
	final static String[] sFields = {NuclosEOField.STATE.getMetaData().getField() };
	

	public WorkflowWizardPanel(String userName) {
		super();
		this.sUsername = userName;
		init();
	}
	

	protected void init() {		
		
	}
	
	
	public void buildSearchFilter() {
		
		Collection<MasterDataVO> colSearchFilter = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.SEARCHFILTER.getEntityName());
		
		for(MasterDataVO vo : Modules.getInstance().getModules(false)) {
			String sModule = (String)vo.getField("entity");
			Integer iModule = Modules.getInstance().getModuleIdByEntityName(sModule);			
			
			for(StateVO voState : StateDelegate.getInstance().getStatesByModule(iModule)) {
				
				Collection<MasterDataVO> colRoleAttrGroup = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), SearchConditionUtils.newComparison(NuclosEntity.ROLEATTRIBUTEGROUP.getEntityName(), "state", ComparisonOperator.EQUAL, voState.getStatename()));
				
				
				for(MasterDataVO voRoleAttrGroup : colRoleAttrGroup) {
					EntitySearchFilters filters = SearchFilters.forEntity(sModule);
					EntitySearchFilter f = new EntitySearchFilter();
					
					f.setName(sModule + " im Status " + voState.getNumeral());
					
					boolean blnaddFilter = true;
					for(MasterDataVO voSearchFilter : colSearchFilter) {
						if(f.getName().equals(voSearchFilter.getField("name"))) {
							blnaddFilter = false;
							break;
						}							
					}
					
					if(!blnaddFilter)
						continue;
					
					f.setDescription(sModule + " im Status " + voState.getNumeral());					
					f.setEntityName(sModule);
					f.setEditable(false);
					f.setForced(false);
					
					f.setSearchCondition(SearchConditionUtils.newComparison(sModule, NuclosEOField.STATENUMBER.getMetaData().getField(), ComparisonOperator.EQUAL, voState.getNumeral()));
					
					List<CollectableEntityFieldWithEntity> lst = new ArrayList<CollectableEntityFieldWithEntity>();
					
					for(String sAttr : AttributeDelegate.getInstance().getAttributeForModule(sModule)) {
						CollectableEntityFieldWithEntity fe = new CollectableEntityFieldWithEntity(CollectableGenericObjectEntity.getByModuleId(iModule), sAttr);	
						lst.add(fe);
					}
					for(String sAttr : sFields) {
						CollectableEntityFieldWithEntity fe = new CollectableEntityFieldWithEntity(CollectableGenericObjectEntity.getByModuleId(iModule), sAttr);	
						lst.add(fe);
					}
					
					f.setVisibleColumns(lst);
					
					try {
						filters.put(f);		
						
						EntitySearchFilter filter = filters.get(f.getName(), sUsername);
						
						TaskController taskCtrl = Main.getMainController().getExplorerController().getTaskController();
						taskCtrl.cmdShowFilterInTaskPanel(filter);
						
					}
					catch(Exception e) {
						e.printStackTrace();
					}
					
					String sGroup = (String)voRoleAttrGroup.getField("role");
					Collection<MasterDataVO> colRoleUser = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLEUSER.getEntityName(), SearchConditionUtils.newComparison(NuclosEntity.ROLEUSER.getEntityName(), "role", ComparisonOperator.EQUAL, sGroup));
					for(MasterDataVO voRoleUser : colRoleUser) {
						
						if(sUsername.equals(voRoleUser.getField("user"))) 
							continue;
						
						try {
							MasterDataVO voSearchFilter = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.SEARCHFILTER.getEntityName(), 
								SearchConditionUtils.newComparison(NuclosEntity.SEARCHFILTER.getEntityName(), "name", ComparisonOperator.EQUAL, f.getName())).iterator().next();
							
							MasterDataMetaVO metaVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.SEARCHFILTERUSER.getEntityName());
							CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(metaVO);
							CollectableMasterData masterData = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataEntity.getMasterDataMetaCVO(), false));
							MasterDataVO mdvo = masterData.getMasterDataCVO();
							mdvo.setField("searchfilter", f.getName());
							mdvo.setField("searchfilterId", voSearchFilter.getIntId());
							mdvo.setField("user", voRoleUser.getField("user"));
							mdvo.setField("userId", voRoleUser.getField("userId"));							
							mdvo.setField("forcefilter", false);
							mdvo.setField("compulsoryFilter", false);
							mdvo.setField("editable", false);
							mdvo.setField("validFrom", null);
							mdvo.setField("validUntil", null);
							
							DependantMasterDataMap mp = voSearchFilter.getDependants();
							mp.addValue(NuclosEntity.SEARCHFILTERUSER.getEntityName(), mdvo);
							
							
							MasterDataDelegate.getInstance().update(NuclosEntity.SEARCHFILTER.getEntityName(), voSearchFilter, mp);
							
						}
						catch(Exception e) {

						}
						
					}
				}
				
			}			
		}
		
				
	}
	
	
	

}
