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
package org.nuclos.client.masterdata.user;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.nuclos.client.common.SelectUserController;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.ldap.LDAPDataDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

/**
 * <code>CollectController</code> for entity "user".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Lars.Rueckemann@novabit.de">Lars Rueckemann</a>
 * @version 01.00.00
 */
public class UserCollectController extends MasterDataCollectController {

	private final static String FIELD_PREFERENCES = "preferences";
	private final static String FIELD_PASSWORD = "password";

	protected final boolean useLDAP = useLDAP();
	protected LDAPDataDelegate ldapdelegate = null;
	private List<MasterDataWithDependantsVO> ldapRegisteredUsers = null;

	public UserCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.USER, tabIfAny);
		this.setupDetailsToolBar();
		if(this.useLDAP){
			this.ldapdelegate = LDAPDataDelegate.getInstance();
		}
	}

	@Override
	protected void initialize(CollectPanel<CollectableMasterDataWithDependants> pnlCollect) {
		super.initialize(pnlCollect);
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {
			@Override
			public void resultModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				int resultMode = ev.getNewCollectState().getInnerState();
				copyPrefsAction.setEnabled((resultMode == CollectState.RESULTMODE_SINGLESELECTION)
					|| (resultMode == CollectState.RESULTMODE_MULTISELECTION));
			}
		});
	}

	/**
	 *
	 */
	protected void setupDetailsToolBar() {
		//final JToolBar toolbarCustomResult = UIUtils.createNonFloatableToolBar();

		if(this.useLDAP){
			final JButton btnSynchronizeWithLDAP = new JButton();
			btnSynchronizeWithLDAP.setName("btnSynchronizeWithLDAP");
			btnSynchronizeWithLDAP.setIcon(Icons.getInstance().getIconLDAP());
			btnSynchronizeWithLDAP.setToolTipText(CommonLocaleDelegate.getMessage("UserCollectController.1", "Mit LDAP synchronisieren"));

			final UserCollectController uctl = this;
			// action: Select Columns
			btnSynchronizeWithLDAP.setAction(new CommonAbstractAction(btnSynchronizeWithLDAP) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent ev) {
					uctl.cmdSynchronizeUser(uctl);
				}
			});

			//toolbarCustomResult.add(btnSynchronizeWithLDAP);
			this.getDetailsPanel().addToolBarComponent(btnSynchronizeWithLDAP);
		}

		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_MANAGEMENT_CONSOLE)) {
			//toolbarCustomResult.add(copyPrefsAction);
			this.getDetailsPanel().addToolBarComponent(new JButton(copyPrefsAction));
		}

		//this.getResultPanel().setCustomToolBarArea(toolbarCustomResult);
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 * @throws CommonBusinessException
	 */
	public void cmdSynchronizeUser(final UserCollectController clctctl) {
		if(!synchronizeWithLDAP()){
			return;
		}
		final SelectUserController ctl = new SelectUserController(clctctl.getFrame(), CommonLocaleDelegate.getMessage("UserCollectController.2", "LDAP Benutzer"),
			CommonLocaleDelegate.getMessage("UserCollectController.3", "Ausgew\u00e4hlte Benutzer synchronisieren"), null, null);

		final List<MasterDataVO> lstAvailable = CollectionUtils.typecheck(this.ldapRegisteredUsers, MasterDataVO.class);

		final JTable tbl = getResultTable();

		final boolean bOK = ctl.run(lstAvailable, new ArrayList<MasterDataVO>(), new MasterDataVO.NameComparator());

		if (bOK) {
			UIUtils.runCommand(clctctl.getFrame(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					final int iSelectedRow = tbl.getSelectedRow();
					final List<MasterDataWithDependantsVOWrapper> selected = ctl.getSelectedColumns();
					for(MasterDataWithDependantsVOWrapper selectedWrapper : selected){
						if(selectedWrapper.isWrapped()){
							selectedWrapper.setId(null);
							if(selectedWrapper.getDependants() != null){
								for(EntityObjectVO dependant : selectedWrapper.getDependants().getAllData()){
									dependant.setId(null);
								}
							}
							// TODO: make default value customizable
							if (selectedWrapper.getField("send") == null) {
								selectedWrapper.setField("send", false);
							}
							if (selectedWrapper.getField("superuser") == null) {
								selectedWrapper.setField("superuser", false);
							}
							clctctl.mddelegate.create(NuclosEntity.USER.getEntityName(), selectedWrapper, selectedWrapper.getDependants());
						} else {
							if(selectedWrapper.isMapped()){
								selectedWrapper.replaceNativeFields();
								clctctl.mddelegate.update(NuclosEntity.USER.getEntityName(), selectedWrapper, selectedWrapper.getDependants());
							} else {
								if(selectedWrapper.isNative()){
									clctctl.mddelegate.remove(NuclosEntity.USER.getEntityName(), selectedWrapper);
								}
							}
						}

					}
					// refresh the result:
					clctctl.refreshResult();

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelectedRow != -1) {
						tbl.setRowSelectionInterval(iSelectedRow, iSelectedRow);
					}

					//restoreColumnWidths(ctl.getSelectedColumns(), mpWidths);
				}
			});
		}
	}

	private boolean synchronizeWithLDAP() {
		final boolean[] synchronizedWithLDAP = new boolean[] {false};
		UIUtils.runCommand(this.getFrame(), new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				try {
					Collection<MasterDataWithDependantsVOWrapper> mdwrapperlst = ldapdelegate.getUsers(null, null);
					filterOutLDAPUsers(mdwrapperlst);
					synchronizedWithLDAP[0] = true;
				} catch (CollectableFieldFormatException e) {
					e.printStackTrace();
					final String sMessage =CommonLocaleDelegate.getMessage("UserCollectController.4",
						"LDAP Synchronisierung ist gescheitert.\nEine Liste der in LDAP registrierten Benutzer kann nicht dargestellt werden.");
					Errors.getInstance().showExceptionDialog(getFrame(), sMessage, e);
					synchronizedWithLDAP[0] = false;
				}
			}
		});
		return synchronizedWithLDAP[0];
	}

	private void filterOutLDAPUsers(Collection<MasterDataWithDependantsVOWrapper> ldapusers) throws CommonBusinessException {
		final CollectableSearchCondition currentcondition = getCollectableSearchCondition();

		if (currentcondition != null) {
			final CollectableSearchCondition condition = SearchConditionUtils.not(currentcondition);
			final Collection<MasterDataVO> usersToRemove = mddelegate.getMasterData(getEntityName(), condition);

			final List<String> namestoremove = CollectionUtils.transform(usersToRemove, new Transformer<MasterDataVO, String>() {
				@Override
				public String transform(MasterDataVO i) {
					return ((String)i.getField(MasterDataVO.FIELDNAME_NAME)).toLowerCase();
				}
			});

			CollectionUtils.removeAll(ldapusers, new Predicate<MasterDataWithDependantsVOWrapper>() {
				@Override
				public boolean evaluate(MasterDataWithDependantsVOWrapper t) {
					return namestoremove.contains(t.getField(MasterDataVO.FIELDNAME_NAME).toString().toLowerCase());
				}
			});
		}

		this.ldapRegisteredUsers = new ArrayList<MasterDataWithDependantsVO>(ldapusers);
	}

	private boolean useLDAP(){
	 	return true;
	}

	@Override
	protected void validate(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		super.validate(clct);

		// check if a user with the given name exists already (ignoring case):
		final CollectableSearchCondition cond = SearchConditionUtils.newComparison(this.getEntityName(), "name", ComparisonOperator.EQUAL, clct.getValue("name"));
		final TruncatableCollection<MasterDataVO> collmdvo = this.mddelegate.getMasterData(this.getEntityName(), cond, true);
		switch (collmdvo.size()) {
			case 0:
				// OK
				break;
			case 1:
				final Object oIdExistingUser = collmdvo.iterator().next().getId();
				if (!oIdExistingUser.equals(clct.getId())) {
					throw new CommonBusinessException(CommonLocaleDelegate.getMessage("UserCollectController.5",
						"Ein Benutzer namens \"{0}\" ist bereits im System vorhanden.", clct.getValue("name")));
				}
				break;
			default:
				throw new CommonBusinessException(CommonLocaleDelegate.getMessage("UserCollectController.6",
					"Es sind bereits mehrere Benutzer(!) unter dem Namen \"{0}\" im System vorhanden.", clct.getValue("name")));
		}
	}

	final Action copyPrefsAction = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			putValue(Action.SHORT_DESCRIPTION, CommonLocaleDelegate.getMessage("nuclos.preferences.transfer", null));
			putValue(Action.SMALL_ICON, Icons.getInstance().getIconPrefsCopy());
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			List<CollectableMasterDataWithDependants> selectedUsers = getSelectedCollectables();
			CopyPreferencesPanel panel = new CopyPreferencesPanel(Main.getMainController().getUserName());
			int opt = JOptionPane.showConfirmDialog(UserCollectController.this.getParent(), panel,
				CommonLocaleDelegate.getMessage("nuclos.preferences.transfer", null),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if (opt == JOptionPane.OK_OPTION) {
				Map<String, Map<String, String>> selectedPreferences = panel.getSelectedPreferences();
				for (Collectable c : selectedUsers) {
					String userName = (String) c.getField("name").getValue();
					PreferencesFacadeRemote facade = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
					try {
						facade.mergePreferencesForUser(userName, selectedPreferences);
					} catch(CommonFinderException ex) {
						Errors.getInstance().showExceptionDialog(UserCollectController.this.getParent(),
							CommonLocaleDelegate.getMessage("nuclos.preferences.transfer.error", userName), ex);
					}
				}
			}
		}
	};

	@Override
	protected List<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (CollectableEntityField cef : super.getFieldsAvailableForResult(clcte)) {
			if (!FIELD_PREFERENCES.equals(cef.getName()) && !FIELD_PASSWORD.equals(cef.getName())) {
				result.add(cef);
			}
		}
		return result;
	}
}	// class UserCollectController
