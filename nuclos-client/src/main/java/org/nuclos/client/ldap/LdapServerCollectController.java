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
package org.nuclos.client.ldap;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.PointerException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class LdapServerCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(LdapServerCollectController.class);
	
	private final Action actTestAuthentication = new CommonAbstractAction(Icons.getInstance().getIconValidate16(), CommonLocaleDelegate.getMessage("LdapServerCollectController.testauthentication", "Anmeldung testen")) {

		@Override
		public void actionPerformed(ActionEvent ev) {
			cmdTryAuthentication();
		}
	};

	private JButton btnTestAuthentication;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public LdapServerCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.LDAPSERVER.getEntityName(), tabIfAny);

		this.setupDetailsToolBar();

		getCollectStateModel().addCollectStateListener(new CollectStateAdapter(){

			@Override
            public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				Collectable clct = getSelectedCollectable();
	            if (ev.getNewCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW
	            	&& !clct.getField("userfilter").isNull()) {
	            	btnTestAuthentication.setEnabled(true);
	            }
	            else {
	            	btnTestAuthentication.setEnabled(false);
	            }
            }
		});
	}

	private void setupDetailsToolBar() {
		// additional functionality in Details panel:
		//final JToolBar toolbar = UIUtils.createNonFloatableToolBar();

		btnTestAuthentication = new JButton(this.actTestAuthentication);
		btnTestAuthentication.setName("btnTestAuthentication");
		btnTestAuthentication.setText(CommonLocaleDelegate.getMessage("LdapServerCollectController.testauthentication", "Anmeldung testen"));
		//toolbar.add(btnTestAuthentication);
		this.getDetailsPanel().addToolBarComponent(btnTestAuthentication);
		
		//this.getDetailsPanel().setCustomToolBarArea(toolbar);
	}

	@Override
	public CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if(clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		// We have to clear the ids for cloned objects:
		/**
		 * @todo eliminate this workaround - this is the wrong place. The right
		 *       place is the Clone action!
		 */
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		MasterDataVO mdvoInserted;
		try {
			mdvoInserted = LDAPDataDelegate.getInstance().create(clctNew.getMasterDataCVO(), mpmdvoDependants);
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage());
		}

		fireApplicationObserverEvent();
		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap)oAdditionalData;

		MasterDataVO mdvoUpdated;
		try {
			mdvoUpdated = LDAPDataDelegate.getInstance().modify(clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap());
		}
		catch (CommonBusinessException ex) {
			throw new PointerException(ex.getMessage());
		}
		fireApplicationObserverEvent();
		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	public void cmdTryAuthentication() {
		final String servername = (String)getSelectedCollectable().getField("servername").getValue();
		UIUtils.runCommand(getFrame(), new Runnable() {
			@Override
			public void run() {
				try {
					TryAuthenticationPanel inputpanel = new TryAuthenticationPanel();
	
					JOptionPane optpn = new JOptionPane(inputpanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	
					// perform the dialog:
					JDialog dialog = optpn.createDialog(getFrame(), CommonLocaleDelegate.getMessage("LdapServerCollectController.testauthentication", "Anmeldung testen"));
					dialog.setModal(true);
					dialog.setResizable(false);
					dialog.pack();
					dialog.setLocationRelativeTo(getFrame());
					dialog.setVisible(true);
	
					if (optpn.getValue() != null && JOptionPane.OK_OPTION == ((Integer)optpn.getValue()).intValue()) {
						if (!StringUtils.isNullOrEmpty(inputpanel.getUsername())) {
							String message;
							String title;
							int messagetype;
							try {
								if (LDAPDataDelegate.getInstance().tryAuthentication(servername, inputpanel.getUsername(), inputpanel.getPassword())) {
									title = CommonLocaleDelegate.getText("LdapServerCollectController.testsuccessful.title", "Test erfolgreich");
									message = CommonLocaleDelegate.getMessage("LdapServerCollectController.testsuccessful.message", "Der Login {0} wurde erfolgreich authentisiert.", inputpanel.getUsername());
									messagetype = JOptionPane.INFORMATION_MESSAGE;
								}
								else {
									title = CommonLocaleDelegate.getText("LdapServerCollectController.testerror.title", "Test fehlgeschlagen");
									message = CommonLocaleDelegate.getMessage("LdapServerCollectController.testfailed.message", "Authentisierung des Logins {0} ist fehlgeschlagen.", inputpanel.getUsername());
									messagetype = JOptionPane.ERROR_MESSAGE;
								}
							}
							catch (Exception ex) {
								LOG.error("cmdTryAuthentication failed: " + ex, ex);
								title = CommonLocaleDelegate.getText("LdapServerCollectController.testerror.title", "Test fehlgeschlagen");
								message = CommonLocaleDelegate.getMessage("LdapServerCollectController.testerror.message", "Der Test ist mit folgender Meldung fehlgeschlagen: {0}", ex.getMessage());
								messagetype = JOptionPane.ERROR_MESSAGE;
							}
							JOptionPane.showMessageDialog(getFrame(), message, title, messagetype);
						}
					}
				}
				catch (Exception e) {
					LOG.error("cmdTryAuthentication failed: " + e, e);
				}
			}
		});
	}
}
