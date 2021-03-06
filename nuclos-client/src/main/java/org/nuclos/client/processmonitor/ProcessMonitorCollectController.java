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
/**
 * 
 */
package org.nuclos.client.processmonitor;

import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LafParameterProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.console.NuclosConsole;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.common.LafParameter;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;

/**
 * @author Marc.Finke
 * CollectControler for Processmonitor Designer
 *
 */
public class ProcessMonitorCollectController extends NuclosCollectController<CollectableProcessMonitorModel> {
	
	private static final Logger LOG = Logger.getLogger(ProcessMonitorCollectController.class);

	private final CollectPanel<CollectableProcessMonitorModel> pnlCollect = new CollectPanel<CollectableProcessMonitorModel>(-1l, false, LafParameterProvider.getInstance().getValue(LafParameter.nuclos_LAF_Details_Overlay));
	// @SuppressWarnings("unused")
	private final MasterDataSubFormController subformctlUsages;	
	private final ProcessMonitorEditPanel pnlEdit;
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ProcessMonitorCollectController(MainFrameTab tabIfAny) {
		this(CollectableProcessMonitorModel.clcte, tabIfAny);
	}

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	protected ProcessMonitorCollectController(CollectableEntity clcte, MainFrameTab tabIfAny) {
		super(clcte, tabIfAny);
		
		this.initialize(this.pnlCollect);
		
		final SubForm subformUsages = new SubForm(NuclosEntity.STATEMODELUSAGE.getEntityName(), JToolBar.VERTICAL, "statemodel");
		
		this.subformctlUsages = new MasterDataSubFormController(getTab(), this.getDetailsEditView().getModel(),
				this.getEntityName(), subformUsages, this.getPreferences(), this.getEntityPreferences(), valueListProviderCache);
		
		pnlEdit = new ProcessMonitorEditPanel(subformUsages);
		
		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(
				pnlEdit, pnlEdit.getHeader().newCollectableComponentsProvider()));
					
		getTab().setLayeredComponent(pnlCollect);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @deprecated Move to DetailsController hierarchy and make protected again.
	 */
	@Override
	public void addAdditionalChangeListenersForDetails() {		
		this.pnlEdit.getProcessMonitorEditor().addChangeListener(this.changelistenerDetailsChanged);
	}

	/**
	 * @deprecated Move to DetailsController and make protected again.
	 */
	@Override
	public void removeAdditionalChangeListenersForDetails() {
		this.pnlEdit.getProcessMonitorEditor().removeChangeListener(this.changelistenerDetailsChanged);
	}


	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#deleteCollectable(org.nuclos.common.collect.collectable.Collectable)
	 */
	@Override
	protected void deleteCollectable(CollectableProcessMonitorModel clct)
			throws CommonBusinessException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#findCollectableById(java.lang.String, java.lang.Object)
	 */
	@Override
	protected CollectableProcessMonitorModel findCollectableById(String entity, Object id)
			throws CommonBusinessException {
		// TODO Auto-generated method stub		
		return new CollectableProcessMonitorModel(ProcessMonitorDelegate.getInstance().getStateGraph((Integer)id));
	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#getEntityLabel()
	 */
	@Override
	protected String getEntityLabel() {
		// TODO Auto-generated method stub
		return "Prozess Designer";
	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#insertCollectable(org.nuclos.common.collect.collectable.Collectable)
	 */
	@Override
	protected CollectableProcessMonitorModel insertCollectable(CollectableProcessMonitorModel clctNew)
			throws CommonBusinessException {	
		
		Integer iInsertedModelId = saveProcessModelAndUsages(this.pnlEdit.getProcessMonitorEditor(), clctNew.getProcessMonitorModel());
		
		return new CollectableProcessMonitorModel(ProcessMonitorDelegate.getInstance().getStateGraph(iInsertedModelId));
	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#newCollectable()
	 */
	@Override
	public CollectableProcessMonitorModel newCollectable() {
		// TODO Auto-generated method stub
		return new CollectableProcessMonitorModel(new ProcessMonitorVO());
	}

	/* (non-Javadoc)
	 * @see org.nuclos.client.ui.collect.CollectController#updateCollectable(org.nuclos.common.collect.collectable.Collectable, java.lang.Object)
	 */
	@Override
	protected CollectableProcessMonitorModel updateCollectable(CollectableProcessMonitorModel clct,
			Object additionalData) throws CommonBusinessException {
		// TODO Auto-generated method stub
		return null;
	}
	
	

	@Override
	protected CollectableProcessMonitorModel updateCurrentCollectable(CollectableProcessMonitorModel clctCurrent)
			throws CommonBusinessException {
		// TODO Auto-generated method stub
		Integer iInsertedModelId = saveProcessModelAndUsages(this.pnlEdit.getProcessMonitorEditor(), clctCurrent.getProcessMonitorModel());
		
		return new CollectableProcessMonitorModel(ProcessMonitorDelegate.getInstance().getStateGraph(iInsertedModelId));
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableProcessMonitorModel clct)
			throws CommonBusinessException {
		// TODO Auto-generated method stub
		super.unsafeFillDetailsPanel(clct);
		
		//final StateModelVO statemodelvo = clct.getStateModelVO();
		final ProcessMonitorVO monitorvo = clct.getProcessMonitorModel();		
		//final ProcessMonitorGraphVO graphvo = new ProcessMonitorGraphVO(monitorvo);		
		
		if (monitorvo.getId() == null) {
			// @todo this should also be done via setStateGraph:
			this.pnlEdit.getProcessMonitorEditor().createNewStateModel(monitorvo);
			//this.pnlEdit.getStateModelEditor().createNewStateModel(statemodelvo);
		}
		else {
			//this.pnlEdit.getStateModelEditor().setStateGraph(clct.getStateGraphVO());
			//this.pnlEdit.getProcessMonitorEditor().setStateGraph(clct.getProcessMonitorGraphVO());
			final ProcessMonitorGraphVO graphvo = ProcessMonitorDelegate.getInstance().getStateGraph(monitorvo.getId());
			this.pnlEdit.getProcessMonitorEditor().setStateGraph(graphvo);
		}
		
	}

	/*
	 * insert or update a process model over ProcessMonitorDelegate
	 */
	private Integer saveProcessModelAndUsages(ProcessMonitorEditor statemodeleditor, ProcessMonitorVO statemodelvo) throws CommonBusinessException {
		// prepare the statemodeleditor for saving:
		final ProcessMonitorGraphVO stategraphvo = statemodeleditor.prepareForSaving(statemodelvo);		

		Integer intid = ProcessMonitorDelegate.getInstance().setStateGraph(stategraphvo);
		
		// invalidate server caches
		try {
			NuclosConsole.getInstance().parseAndInvoke(new String[]{NuclosConsole.CMD_INVALIDATEALLCACHES}, false);
		}
		catch(Exception e) {
			throw new CommonBusinessException("Der serverseitige Cache konnte nicht invalidiert werden!", e);
		}
		return intid;
	}

	@Override
	protected CollectableProcessMonitorModel findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}
	
	
}
