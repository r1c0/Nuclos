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
package org.nuclos.client.masterdata.wiki;

import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;

/**
 * Controller for <code>Wiki</code>
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 * 
 * @deprecated Probably not in use.
 */
public class WikiCollectController extends MasterDataCollectController{

	private final CollectableComponentModelListener ccmlistenerEntityField = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			if (ev.getNewValue().getValue() != null) {
				setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, false);
				setSubFormVisibility(NuclosEntity.WIKIMAPPING, true);
				setComponentEnabled("general", false);		
			}	
			else {
				setComponentEnabled("general", true);
			}			
		}
			
	};
	
	private final CollectableComponentModelListener ccmlistenerGeneralField = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			if (ev.getNewValue().getValue() != null) {
				setSubFormVisibility(NuclosEntity.WIKIMAPPING, false);
				setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, true);
				setComponentEnabled("entity", false);				
			}	
			else {
				setSubFormVisibility(NuclosEntity.WIKIMAPPING, true);
				setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, false);
				setComponentEnabled("entity", true);		
			}			
		}
			
	};
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public WikiCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.WIKI, tabIfAny);
		
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter () {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {				
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("entity") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("entity").addCollectableComponentModelListener(ccmlistenerEntityField);
				}
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("general") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("general").addCollectableComponentModelListener(ccmlistenerGeneralField);
				}
				
				if (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_NEW) {
					for (CollectableComponent clctcomp : getDetailsPanel().getEditView().getCollectableComponents()) {
						clctcomp.setEnabled(true);
					}
					setSubFormVisibility(NuclosEntity.WIKIMAPPING, true);
					setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, false);
				}
				
				if (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW) {
					for (CollectableComponent clctcomp : getDetailsPanel().getEditView().getCollectableComponents()) {
						if (clctcomp.getFieldName().equals("general") && clctcomp.getField().getValue() != null) {
							setSubFormVisibility(NuclosEntity.WIKIMAPPING, false);
							setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, true);
							setComponentEnabled("entity", false);							
							clctcomp.setEnabled(true);
						}		
						else if (clctcomp.getFieldName().equals("entity") && clctcomp.getField().getValue() != null) {
							setSubFormVisibility(NuclosEntity.WIKIMAPPING, true);
							setSubFormVisibility(NuclosEntity.WIKIMAPPINGGENERAL, false);
							setComponentEnabled("general", false);
							clctcomp.setEnabled(true);
						}										
					}
				}
			}
			
			@Override
			public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("entity") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("entity").removeCollectableComponentModelListener(ccmlistenerEntityField);
				}
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("general") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("general").removeCollectableComponentModelListener(ccmlistenerGeneralField);
				}
			}
		});
		
	}
	
	private void setComponentEnabled(String sComponentName, boolean bEnabled) {
		for (CollectableComponent clct : getDetailsPanel().getEditView().getCollectableComponentsFor(sComponentName)) {
			clct.setEnabled(bEnabled);
		}
	}
	
	private void setSubFormVisibility(NuclosEntity entity, boolean bVisibility) {
		getMapOfSubFormControllersInDetails().get(entity.getEntityName()).getSubForm().setVisible(bVisibility);
	}
	
	@Override
	protected void prepareCollectableForSaving(CollectableMasterDataWithDependants clctCurrent, CollectableEntity clcte) {
		if (!getSearchStrategy().isCollectableComplete(clctCurrent)) {
			throw new IllegalArgumentException("clctCurrent");
		}
		super.prepareCollectableForSaving(clctCurrent, clcte);

		if (clctCurrent.getField("entity").getValue() != null) {
			MasterDataMetaVO mdmvo = MasterDataDelegate.getInstance().getMetaData((String) clctCurrent.getField("entity").getValue());
				
			for(CollectableMasterData clctmd :getMapOfSubFormControllersInDetails().get(NuclosEntity.WIKIMAPPING.getEntityName()).getCollectableTableModel().getCollectables()) {
				Object oAttributeId = clctmd.getField("attribute").getValueId();
				Object oAttributeValue = clctmd.getField("attribute").getValue();
				
				if(Modules.getInstance().isModuleEntity(mdmvo.getEntityName())) {
					clctmd.setField("attribute", new CollectableValueIdField(oAttributeId, oAttributeValue));
					clctmd.setField("field", new CollectableValueIdField(null, null));
				}
				else {
					clctmd.setField("field", new CollectableValueIdField(oAttributeId, oAttributeValue));
					clctmd.setField("attribute", new CollectableValueIdField(null, null));
				}				
				
			}
		}		
	}
}
