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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.DefaultEditView;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common2.DateTime;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.InstanceVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;

/**
 * <code>CollectController</code> for entity "instance".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Maik.Stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceCollectController extends NuclosCollectController<CollectableInstanceModel> {
	
	private final MainFrameTab ifrm;
	private final CollectPanel<CollectableInstanceModel> pnlCollect = new CollectPanel<CollectableInstanceModel>(false);
	private final InstanceViewPanel pnlView;

	private JButton btnStart;
	private JButton btnSetPlanStart;
	
	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public InstanceCollectController(JComponent parent, String entityName, MainFrameTab tabIfAny) {
		super(parent, entityName);
		ifrm = tabIfAny!=null ? tabIfAny : newInternalFrame("InstanceViewer");
		this.initialize(this.pnlCollect);
		
		this.setupDetailsToolBar();
		btnStart.setEnabled(true);
		
		pnlView = new InstanceViewPanel();
		
		this.getDetailsPanel().setEditView(DefaultEditView.newDetailsEditView(pnlView, pnlView.pnlHeader.newCollectableComponentsProvider()));
		
		ifrm.setLayeredComponent(pnlCollect);
		this.setInternalFrame(ifrm, tabIfAny==null);
		
		this.getCollectStateModel().addCollectStateListener(new InstanceCollectStateListener());
	}
	
	public final MainFrameTab getMainFrameTab() {
		return ifrm;
	}
	
	/**
	 * 
	 */
	private void setupDetailsToolBar() {
		final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();
		
		btnSetPlanStart = new JButton(Icons.getInstance().getIconRuleNode());
		btnSetPlanStart.setToolTipText("Plan Start setzen...");
		btnSetPlanStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final InstanceSelectPlanStartController ctrl = new InstanceSelectPlanStartController(InstanceCollectController.this.getFrame());
				final CollectableInstanceModel clctSelected = InstanceCollectController.this.getSelectedCollectable();
				Integer iProcessMonitorId = (Integer)clctSelected.getValueId("processmonitor");
				
				try {
					SubProcessVO subProcess = ProcessMonitorDelegate.getInstance().findStartingSubProcess(iProcessMonitorId);
					boolean blnOK = ctrl.run(subProcess.getPlanStartSeries());
					if (blnOK){
						DateTime datePlanStart = ctrl.getPlanStart();
						if (datePlanStart != null) {
							DateTime datePlanEnd = ProcessMonitorDelegate.getInstance().getProcessPlanEnd(iProcessMonitorId, datePlanStart);
							
							InstanceCollectController.this.getDetailsComponentModel("planstart").setField(new CollectableValueField(datePlanStart));
							InstanceCollectController.this.getDetailsComponentModel("planend").setField(new CollectableValueField(datePlanEnd));
						} else {
							// message to user?
						}
					}
				} catch (CommonBusinessException e) {
					Errors.getInstance().showExceptionDialog(InstanceCollectController.this.getFrame(), "Beim Plan Start setzen ist ein Fehler aufgetreten!", e);
				}
			}
		});
		toolbarCustomDetails.add(btnSetPlanStart);

		btnStart = new JButton(Icons.getInstance().getIconPlay16());
		btnStart.setToolTipText("Instanz starten...");
		btnStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final CollectableInstanceModel clctSelected = InstanceCollectController.this.getSelectedCollectable();
				final Integer iInstanceId = (Integer)clctSelected.getId();
				
				DateTime datePlanStart = (DateTime) clctSelected.getValue("planstart");
				DateTime datePlanEnd = (DateTime) clctSelected.getValue("planend");
				
				if (datePlanStart == null || datePlanEnd == null){
					JOptionPane.showMessageDialog(InstanceCollectController.this.getFrame(), "Plan Start und Plan Ende m\u00fcssen gesetzt sein bevor eine Instanz gestartet werden kann!");
				} else {
					if (InstanceDelegate.getInstance().isProcessInstanceStarted(iInstanceId).booleanValue()){
						JOptionPane.showMessageDialog(InstanceCollectController.this.getFrame(), "Instanz ist bereits gestartet!");
					} else {
						try {
							InstanceDelegate.getInstance().createProcessInstance((Integer)clctSelected.getValueId("processmonitor"), (Integer)clctSelected.getId());
							InstanceCollectController.this.refreshCurrentCollectable();
						} catch (CommonBusinessException e) {
							Errors.getInstance().showExceptionDialog(InstanceCollectController.this.getFrame(), "Beim Starten der Instanz ist ein Fehler aufgetreten!", e);
						}
					}
				}
			}
		});
		toolbarCustomDetails.add(btnStart);
		
		//this.getDetailsPanel().setCustomToolBarArea(toolbarCustomDetails);
	}
	
	/**
	 * 
	 *
	 */
	private class InstanceCollectStateListener extends CollectStateAdapter {
		@Override
		public void searchModeEntered(CollectStateEvent ev) throws CommonBusinessException {
			super.searchModeEntered(ev);
			
		}
		@Override
		public void searchModeLeft(CollectStateEvent ev) throws CommonBusinessException {
			super.searchModeLeft(ev);
			
		}
	}

	@Override
	protected void deleteCollectable(CollectableInstanceModel clct) throws CommonBusinessException {
		//TODO remove...
	}

	@Override
	protected CollectableInstanceModel findCollectableById(String entity, Object id) throws CommonBusinessException {
		return new CollectableInstanceModel(MasterDataDelegate.getInstance().get(entity, id));
	}

	@Override
	protected String getEntityLabel() {
		return "Instanzen";
	}

	@Override
	protected CollectableInstanceModel insertCollectable(CollectableInstanceModel clctNew) throws CommonBusinessException {
		MasterDataVO newInstance =  MasterDataDelegate.getInstance().create(NuclosEntity.INSTANCE.getEntityName(), clctNew.getMasterDataVO(), null);
		return new CollectableInstanceModel(newInstance);
	}

	@Override
	public CollectableInstanceModel newCollectable() {
		return new CollectableInstanceModel(new InstanceVO());
	}

	@Override
	protected CollectableInstanceModel updateCollectable(CollectableInstanceModel clct, Object additionalData) throws CommonBusinessException {
		MasterDataDelegate.getInstance().update(NuclosEntity.INSTANCE.getEntityName(), clct.getMasterDataVO(), null);
		return findCollectableById(NuclosEntity.INSTANCE.getEntityName(), clct.getId());
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableInstanceModel clct) throws CommonBusinessException {
		super.unsafeFillDetailsPanel(clct);
		
		ProcessMonitorGraphVO processmodelGraphVO = clct.getProcessMonitorGraphVO();
		this.pnlView.getInstanceViewer().setProcessmodelGraph(processmodelGraphVO);
		this.pnlView.getInstanceViewer().showInstanceStatus((Integer) clct.getId());
	}

	@Override
	protected CollectableInstanceModel findCollectableByIdWithoutDependants(
		String sEntity, Object oId) throws CommonBusinessException {
		return findCollectableById(sEntity, oId);
	}
	
}
