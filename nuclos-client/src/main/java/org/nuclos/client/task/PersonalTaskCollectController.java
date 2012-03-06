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
package org.nuclos.client.task;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.SubForm.ToolbarFunction;
import org.nuclos.client.ui.collect.SubForm.ToolbarFunctionState;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.table.TableCellRendererProvider;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class PersonalTaskCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(PersonalTaskCollectController.class);

	private static class CustomTableCellRendererProvider implements TableCellRendererProvider {

		DetailsSubFormController<?> subcontroller;

		public CustomTableCellRendererProvider(DetailsSubFormController<?> subcontroller){
			this.subcontroller = subcontroller;
		}

		@Override
		public TableCellRenderer getTableCellRenderer(CollectableEntityField clctef) {
			return subcontroller.getSubForm().getTableCellRenderer(clctef);
		}
	}

	private final TaskDelegate delegate;
	private JButton sSingletaskButton;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public PersonalTaskCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.TASKLIST, tabIfAny);
		delegate = new TaskDelegate();
		setupDetailsToolBar();
		if(SecurityCache.getInstance().isSuperUser()){
			List<CollectableComponent> delegatorCollectableComponents = getDetailCollectableComponentsFor("taskdelegator");
			for(CollectableComponent component : delegatorCollectableComponents){
				component.setEnabled(true);
			}
		}

		getSubFormController(NuclosEntity.TASKOBJECT.getEntityName()).getSubForm().getSubformTable().setTableCellRendererProvider(new CustomTableCellRendererProvider(getSubFormController(NuclosEntity.TASKOBJECT.getEntityName())));
		getSubFormController(NuclosEntity.TASKOBJECT.getEntityName()).getSubForm().setToolbarFunctionState(ToolbarFunction.REMOVE, ToolbarFunctionState.ACTIVE);
		getSubFormController(NuclosEntity.TASKOWNER.getEntityName()).getSubForm().getSubformTable().getModel().addTableModelListener( new TableModelListener() {
 			@Override
			public void tableChanged(TableModelEvent e) {
 				if(sSingletaskButton != null){
 					sSingletaskButton.setEnabled(getUserIds().size() > 1);
 				}
			}
        });
		this.addCollectableEventListener(Main.getInstance().getMainController().getTaskController().getPersonalTaskController());

		getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				super.detailsModeEntered(ev);
				if (ev.getNewCollectState().isDetailsMode()) {
					PersonalTaskCollectController.this.setDetailsChangedIgnored(true);
					if (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_NEW) {
						for (DetailsSubFormController<CollectableEntityObject> sf : getSubFormControllersInDetails()) {
							if (sf.getCollectableEntity().getName().equals(NuclosEntity.TASKOWNER.getEntityName())) {
								CollectableMasterData md = sf.insertNewRow();
								String sUser = Main.getInstance().getMainController().getUserName();
								Long iUser = delegate.getUserId(sUser);
								md.setField("user", new CollectableValueIdField(iUser, sUser));
							}
						}
					}
					PersonalTaskCollectController.this.setDetailsChangedIgnored(false);
				}
			}
		});
	}

	private void setupDetailsToolBar() {
		// final JToolBar toolbar = (JToolBar)this.getDetailsPanel().getCustomToolBarArea();
		final String sSingletaskButtonName = getSpringLocaleDelegate().getMessage("EditPersonalTaskDefinitionPanel.Button.Singletask",null);
		sSingletaskButton = new JButton(
			new AbstractAction(sSingletaskButtonName) {

				@Override
				public void actionPerformed(ActionEvent e) {
					CollectableMasterDataWithDependants selectedCollectable = PersonalTaskCollectController.this.getSelectedCollectable();
					MasterDataWithDependantsVO mdvo = selectedCollectable.getMasterDataWithDependantsCVO();
					try {
						boolean bSplitted = splitTaskForOwners(mdvo.getId() == null, mdvo, sSingletaskButtonName);
						if(bSplitted){
							broadcastCollectableEvent(selectedCollectable, MessageType.EDIT_DONE);
						}
						refreshCurrentCollectable();
					} catch(CommonBusinessException ex) {
						throw new NuclosFatalException(ex);
					}
				}
			}
		);
		// toolbar.add(sSingletaskButton, null, 0);
		//getDetailsPanel().setCustomToolBarArea(toolbar);

		getDetailsPanel().addToolBarComponent(sSingletaskButton);
	}

	@Override
	protected CollectableMasterDataWithDependants newCollectableWithDefaultValues() {
		CollectableMasterDataWithDependants newCollectableWithDefaultValues = super.newCollectableWithDefaultValues();
		newCollectableWithDefaultValues.setField(CollectableTask.FIELDNAME_DELEGATOR, 
				new CollectableValueIdField(SecurityDelegate.getInstance().getUserId(
						Main.getInstance().getMainController().getUserName()), ""));
		return newCollectableWithDefaultValues;
	}

	private boolean splitTaskForOwners(boolean createMode, MasterDataWithDependantsVO mdvo, final String sSingletaskButtonName)
		throws CommonBusinessException {
		boolean result = false;
		final String sMessage = getSpringLocaleDelegate().getMessage("EditPersonalTaskDefinitionPanel.Singletask.Secure", null);
		final String sSecureTitle = sSingletaskButtonName;
		int createSingleTasksSecure = JOptionPane.showConfirmDialog(this.getTab(), sMessage, sSecureTitle, JOptionPane.YES_NO_OPTION);
		switch(createSingleTasksSecure) {
			case JOptionPane.YES_OPTION:
				Collection<TaskVO> taskvos = null;
				if(createMode){
					taskvos = this.delegate.create(mdvo, getUserIds(), true);
				} else {
					taskvos = this.delegate.update(mdvo, getUserIds(), true);
				}
				result = (taskvos != null && !taskvos.isEmpty());
				break;
			case JOptionPane.NO_OPTION:
			default: ;
		}
		return result;
	}

	public Set<Long> getUserIds() {
		// A potential problem here is that the result set could contain null as element.
		// Perhaps it would be better to further restrict the transformer? (Thomas Pasch)
		List<Long> userIds = CollectionUtils.transform(getSubFormController(NuclosEntity.TASKOWNER.getEntityName()).getCollectables(),
			new Transformer<CollectableMasterData, Long>(){
				@Override
				public Long transform(CollectableMasterData clmd) {
					return IdUtils.toLongId(clmd.getField("user").getValueId());
				}}
		);
		return new HashSet<Long>(userIds);
	}

}
