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
package org.nuclos.client.resource.admin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.resource.ResourceDelegate;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.resource.valueobject.ResourceVO;

public class ResourceCollectController extends MasterDataCollectController{

	private static final ResourceDelegate delegate = ResourceDelegate.getInstance();
	
	protected List<CollectableResouceSaveListener> listResourceSaveListener;
	
	public ResourceCollectController(JComponent parent, MainFrameTab tabIfAny){
		super(parent, NuclosEntity.RESOURCE, tabIfAny);
		
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) {
				int iDetailsMode = ev.getNewCollectState().getInnerState();
				if (iDetailsMode == CollectState.DETAILSMODE_VIEW || iDetailsMode == CollectState.DETAILSMODE_EDIT) {
					Integer iId = getSelectedCollectable().getMasterDataWithDependantsCVO().getIntId();
					ResourceVO resourcevo = ResourceCache.getResourceById(iId);
					final boolean bIsSystemResource = (resourcevo == null) ? false : resourcevo.isSystemResource();
					if (bIsSystemResource) {
						setComponentsEnabled(false);
					}
					else {
						setComponentsEnabled(true);
					}
				}
				else {
					setComponentsEnabled(true);
				}
				
			}
		});	
		
		listResourceSaveListener = new ArrayList<CollectableResouceSaveListener>();		
	}
	
	public void addResouceSaveListener(CollectableResouceSaveListener saveListener) {
		listResourceSaveListener.add(saveListener);
		this.getFrame().addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				fireResourceSaveEvent(null);
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});
	}
	
	private void fireResourceSaveEvent(Collectable clt) {
		for(CollectableResouceSaveListener listener : listResourceSaveListener) {
			listener.fireSaveEvent(clt, this);
		}
	}
	
	private void setComponentsEnabled(boolean bEnabled) {
		for (CollectableComponent clct : getDetailsPanel().getEditView().getCollectableComponents()) {
			if (clct.getFieldName().equals("name")) {
				clct.setEnabled(bEnabled);
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
//		 We have to clear the ids for cloned objects:
		/** @todo eliminate this workaround - this is the wrong place. The right place is the Clone action! */

		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		validateResource(clctNew);
				
		final MasterDataVO mdvoInserted = delegate.create(this.getEntityName(), clctNew.getMasterDataCVO(), mpmdvoDependants);

		CollectableMasterDataWithDependants cmdwd = new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId()))); 
		
		fireResourceSaveEvent(cmdwd);
		
		return cmdwd; 
	}
	
	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oAdditionalData;

		validateResource(clct);
		
		final Object oId = delegate.modify(this.getEntityName(), clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap());

		final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);

		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}
	
	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		delegate.remove(this.getEntityName(), clct.getMasterDataCVO());
	}
	
   private void validateResource(CollectableMasterDataWithDependants clct) throws CollectableFieldValidationException{
//		final ResourceFile resourceFile = (ResourceFile)clct.getField("file").getValue();
//		if (resourceFile != null) {
//			String sExtension = resourceFile.getFilename().substring(resourceFile.getFilename().indexOf('.')+1).toLowerCase();
//			if (clct.getField("type").getValue().equals("Icon") && (!sExtension.equals("png") &&
//					!sExtension.equals("jpg") && !sExtension.equals("bmp") && !sExtension.equals("jpeg") && !sExtension.equals("gif"))) {
//				throw new CollectableFieldValidationException(CommonLocaleDelegate.getMessage("ResourceCollectController.1", "Die Datei {0} ist kein Bild.", resourceFile.getFilename()));
//			}
//		}		
    }
}
