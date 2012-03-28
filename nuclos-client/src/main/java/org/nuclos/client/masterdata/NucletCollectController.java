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
package org.nuclos.client.masterdata;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.dbtransfer.DBTransferExport;
import org.nuclos.client.dbtransfer.DBTransferImport;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.navigation.treenode.nuclet.NuclosInstanceTreeNode;

/**
 * <code>CollectController</code> for entity "Nuclet".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Maik.Stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class NucletCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(NucletCollectController.class);

	private JButton btnExport;
	private JButton btnImport;
	private JButton btnShowDependences;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public NucletCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.NUCLET.getEntityName(), tabIfAny);
	}

	@Override
	public void init() {
		super.init();
		this.setupDetailsToolBar();
		this.getResultTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateExportButtonState();
			}
		});

		btnExport.setFocusable(false);
		btnImport.setFocusable(false);
		btnShowDependences.setFocusable(false);
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct)
		throws CommonBusinessException {
		super.deleteCollectable(clct);
		updateExportButtonState();
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		CollectableMasterDataWithDependants result = super.insertCollectable(clctNew);
		updateExportButtonState();
		return result;
	}
	
	public static class DetailsToolbarPropertyChangeListener implements PropertyChangeListener {
		
		private final JButton newMakeTreeRoot;
		
		private DetailsToolbarPropertyChangeListener(JButton newMakeTreeRoot) {
			this.newMakeTreeRoot = newMakeTreeRoot;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			newMakeTreeRoot.setEnabled((Boolean) evt.getNewValue());
		}

	}

	/**
	 * @deprecated Move to DetailsController hierarchy.
	 */
	private void setupDetailsToolBar() {
		//final JToolBar toolbarCustomDetails = UIUtils.createNonFloatableToolBar();

		final JButton newMakeTreeRoot = new JButton(
				getSpringLocaleDelegate().getText("NucletCollectController.5", "Nuclet Bestandteile"),
				btnMakeTreeRoot.getIcon());

		for (ActionListener al : btnMakeTreeRoot.getActionListeners())
			newMakeTreeRoot.addActionListener(al);
		btnMakeTreeRoot.addPropertyChangeListener("enabled", 
				new DetailsToolbarPropertyChangeListener(newMakeTreeRoot));

		this.getDetailsPanel().addToolBarComponent(newMakeTreeRoot);

		btnShowDependences = new JButton(Icons.getInstance().getIconTree16());
		btnShowDependences.setText(getSpringLocaleDelegate().getText("NucletCollectController.6", "Baum der Nuclets"));
		btnShowDependences.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				cmdShowNucletDependences();
			}
		});
		this.getResultPanel().addToolBarComponent(btnShowDependences);

		btnImport = new JButton(Icons.getInstance().getIconImport16());
		btnImport.setToolTipText(getSpringLocaleDelegate().getText("NucletCollectController.1", "Nuclet importieren..."));
		btnImport.setText(getSpringLocaleDelegate().getText("NucletCollectController.3", "Importieren"));
		btnImport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new DBTransferImport(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							getResultController().getSearchResultStrategy().refreshResult();
							NucletCollectController.this.getNewAction().setEnabled(NucletCollectController.this.isNewAllowed());
						}
						catch(CommonBusinessException e1) {
							LOG.warn("actionPerformed: " + e1);
						}
					}
				}).showWizard(Main.getInstance().getMainFrame().getHomePane());
			}
		});
		btnImport.setEnabled(SecurityCache.getInstance().isSuperUser());
		//toolbarCustomDetails.add(btnImport);
		this.getResultPanel().addToolBarComponent(btnImport);

		btnExport = new JButton(Icons.getInstance().getIconExport16());
		btnExport.setToolTipText(getSpringLocaleDelegate().getText("NucletCollectController.2", "Nuclet exportieren..."));
		btnExport.setText(getSpringLocaleDelegate().getText("NucletCollectController.4", "Exportieren"));
		btnExport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Object oNucletId = NucletCollectController.this.getSelectedCollectableId();

				new DBTransferExport(oNucletId==null?null:((Integer)oNucletId).longValue()).showWizard(
						Main.getInstance().getMainFrame().getHomePane());
				try {
					getResultController().getSearchResultStrategy().refreshResult();
				}
				catch(CommonBusinessException e1) {
					LOG.warn("actionPerformed: " + e1);
				}
			}
		});
		updateExportButtonState();
		//toolbarCustomDetails.add(btnExport);
		this.getResultPanel().addToolBarComponent(btnExport);

		this.getResultPanel().btnExport.setVisible(false);
		this.getResultPanel().btnImport.setVisible(false);
		btnMakeTreeRoot.setVisible(false);
		btnShowResultInExplorer.setVisible(false);
	}

	private void updateExportButtonState() {
		btnExport.setEnabled(SecurityCache.getInstance().isSuperUser());
	}

	private void cmdShowNucletDependences() {
		UIUtils.runCommand(this.getTab(), new CommonRunnable() {
			@Override
			public void run() throws CommonFinderException, CommonPermissionException {
				Main.getInstance().getMainController().getExplorerController().cmdShowInOwnTab(new NuclosInstanceTreeNode());
			}
		});
	}

}
