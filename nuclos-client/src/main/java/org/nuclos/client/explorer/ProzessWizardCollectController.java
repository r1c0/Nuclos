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

import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosResultPanel;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.relation.EntityRelationshipModel;
import org.nuclos.client.relation.EntityRelationshipModelEditPanel;
import org.nuclos.client.statemodel.admin.CollectableStateModel;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.result.ResultPanel;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for collecting state models.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * 
 * @deprecated Probably not in use.
 */
public class ProzessWizardCollectController extends NuclosCollectController<EntityRelationshipModel> {
	
	final CollectableTextField clcttfName = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("name"));

	final CollectableTextField clcttfDescription = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("description"));

	private final CollectPanel<EntityRelationshipModel> pnlCollect = new EntityRelationshipCollectPanel(false);
	private final EntityRelationshipModelEditPanel pnlEdit;
	
	private MainFrame mf;
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected ProzessWizardCollectController(MainFrame mf, MainFrameTab tabIfAny) {
		super(CollectableStateModel.clcte, tabIfAny);
		
		this.initialize(this.pnlCollect);
		
		this.mf = mf;
		
		getTab().setLayeredComponent(pnlCollect);
		
		pnlEdit = new EntityRelationshipModelEditPanel(mf);
		
		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.newCollectableComponentsProvider()));
		
		getTab().setTitle("Relationen-Editor");
	}
	
	
	private class EntityRelationshipCollectPanel extends CollectPanel<EntityRelationshipModel> {

		EntityRelationshipCollectPanel(boolean bSearchPanelAvailable) {
			super(bSearchPanelAvailable, false);
		}

		@Override
		public ResultPanel<EntityRelationshipModel> newResultPanel() {
			return new NuclosResultPanel<EntityRelationshipModel>();
		}
	}
	
	
	@Override
	protected boolean isDeleteAllowed(EntityRelationshipModel clct) {
		return false;
	}

	@Override
	protected boolean isDeleteSelectedCollectableAllowed() {
		return false;
	}

	@Override
	protected boolean isCloneAllowed() {
		return false;
	}

	@Override
	protected boolean isNewAllowed() {
		return false;
	}

	@Override
	protected void deleteCollectable(EntityRelationshipModel clct)
		throws CommonBusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected EntityRelationshipModel findCollectableById(String sEntity,
		Object oId) throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EntityRelationshipModel findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getEntityLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EntityRelationshipModel insertCollectable(
		EntityRelationshipModel clctNew) throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityRelationshipModel newCollectable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected EntityRelationshipModel updateCollectable(
		EntityRelationshipModel clct, Object oAdditionalData)
		throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}


}	// class StateModelCollectController
