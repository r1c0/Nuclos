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

import java.awt.Cursor;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JToolBar;

import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.console.NuclosConsole;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;

/**
 * @author Marc.Finke
 * CollectControler for Processmonitor Designer
 *
 */
public class ProcessMonitorCollectController extends NuclosCollectController<CollectableProcessMonitorModel> {
	
	private final CollectPanel<CollectableProcessMonitorModel> pnlCollect = new CollectPanel<CollectableProcessMonitorModel>(false);
	private final MainFrameTab ifrm;
	@SuppressWarnings("unused")
	private final MasterDataSubFormController subformctlUsages;	
	private final ProcessMonitorEditPanel pnlEdit;
	
	public ProcessMonitorCollectController(JComponent parent, MainFrameTab tabIfAny) {
		this(parent, CollectableProcessMonitorModel.clcte, tabIfAny);
	}

	/**
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public ProcessMonitorCollectController(JComponent parent,
			CollectableEntity clcte, MainFrameTab tabIfAny) {
		super(parent, clcte);
		ifrm = tabIfAny!=null ? tabIfAny : newInternalFrame("Prozesse designen");
		
		this.initialize(this.pnlCollect);
		
		final SubForm subformUsages = new SubForm(NuclosEntity.STATEMODELUSAGE.getEntityName(), JToolBar.VERTICAL, "statemodel");
		
		this.subformctlUsages = new MasterDataSubFormController(getFrame(), parent, this.getDetailsEditView().getModel(),
				this.getEntityName(), subformUsages, this.getPreferences(), valueListProviderCache);
		
		pnlEdit = new ProcessMonitorEditPanel(subformUsages);
		
		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlEdit, pnlEdit.pnlHeader.newCollectableComponentsProvider()));
					
		ifrm.setLayeredComponent(pnlCollect);
		this.setInternalFrame(ifrm, tabIfAny==null);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void addAdditionalChangeListenersForDetails() {		
		this.pnlEdit.getProcessMonitorEditor().addChangeListener(this.changelistenerDetailsChanged);
	}

	@Override
	protected void removeAdditionalChangeListenersForDetails() {
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

	/**
	 * @deprecated Move to ResultController hierarchy.
	 */
	@Override
	protected void search() throws CommonBusinessException {
		try {
			this.ifrm.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			Collection<ProcessMonitorVO> col = ProcessMonitorDelegate.getInstance().getProcessModels();
			
			this.fillResultPanel(CollectionUtils.transform(col, new CollectableProcessMonitorModel.MakeCollectable()));
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(this.ifrm, null, ex);
		}
		finally {
			this.ifrm.setCursor(Cursor.getDefaultCursor());
		}
		
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
