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

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.SelectUserController;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
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
import org.nuclos.client.ui.collect.component.CollectableCheckBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectablePasswordField;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.result.NuclosSearchResultStrategy;
import org.nuclos.client.ui.collect.result.UserResultController;
import org.nuclos.client.ui.model.ChoiceList;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.UserVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
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

	private static final Logger LOG = Logger.getLogger(UserCollectController.class);

	public final static String FIELD_PREFERENCES = "preferences";
	public final static String FIELD_PASSWORD = "password";

	private static final String PLACEHOLDER_SETPASSWORD = "PlaceholderSetPassword";
	private static final String PLACEHOLDER_SENDEMAIL = "PlaceholderSendEmail";
	private static final String PLACEHOLDER_PASSWORD = "PlaceholderPassword";
	private static final String PLACEHOLDER_PASSWORDREPEAT = "PlaceholderPasswordRepeat";

	private CollectableEntityField clctefSetPassword;
	private CollectableEntityField clctefSendPassword;
	private CollectableEntityField clctefNewPassword;
	private CollectableEntityField clctefNewPasswordRepeat;

	private CollectableCheckBox chkSetPassword;
	private CollectableCheckBox chkSendPassword;
	private CollectablePasswordField pwdPassword;
	private CollectablePasswordField pwdPasswordRepeat;

	protected final boolean ldapAuthentication = isLdapAuthenticationEnabled();
	protected final boolean ldapSynchronization = isLdapSynchronizationEnabled();

	protected LDAPDataDelegate ldapdelegate = null;
	private List<MasterDataWithDependantsVO> ldapRegisteredUsers = null;

	CollectableComponentModelListener ccml_superuser = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			Boolean superuser = LangUtils.defaultIfNull((Boolean)ev.getCollectableComponentModel().getField().getValue(), Boolean.FALSE);
			if (ldapAuthentication && !isMultiEdit()) {
				chkSetPassword.setEnabled(superuser);
				chkSetPassword.setField(new CollectableValueField(isNew()));
				setDependantControlStates();
			}
		}
	};

	CollectableComponentModelListener ccml_locked = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			Boolean locked = LangUtils.defaultIfNull((Boolean)ev.getOldValue().getValue(), Boolean.FALSE);
			Integer attempts = LangUtils.defaultIfNull((Integer)getDetailsComponentModel("loginattempts").getField().getValue(), new Integer(0));
			if (locked && attempts > 0) {
				getDetailsComponentModel("loginattempts").setField(new CollectableValueField(new Integer(0)));
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
	public UserCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.USER, tabIfAny,
				new UserResultController<CollectableMasterDataWithDependants>(NuclosEntity.USER.getEntityName(),
				new NuclosSearchResultStrategy<CollectableMasterDataWithDependants>()));
		this.setupDetailsToolBar();
		if(this.ldapSynchronization){
			this.ldapdelegate = LDAPDataDelegate.getInstance();
		}
	}

	@Override
	protected void initialize(CollectPanel<CollectableMasterDataWithDependants> pnlCollect) {
		super.initialize(pnlCollect);
		final String entity = "User";
		clctefSetPassword = new DefaultCollectableEntityField(UserVO.FIELD_SETPASSWORD, Boolean.class, 
				"lblSetPassword", "tltSetPassword", null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity);
		clctefSendPassword = new DefaultCollectableEntityField(UserVO.FIELD_NOTIFYUSER, Boolean.class, 
				"lblSendPassword", "tltSendPassword", null, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity);
		clctefNewPassword = new DefaultCollectableEntityField(UserVO.FIELD_NEWPASSWORD, String.class, 
				"lblNewPassword", "tltNewPassword", 255, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity);
		clctefNewPasswordRepeat = new DefaultCollectableEntityField(UserVO.FIELD_NEWPASSWORD, String.class, 
				"lblNewPasswordRepeat", "tltNewPasswordRepeat", 255, null, true, CollectableField.TYPE_VALUEFIELD, null, null, entity);

		chkSetPassword = new CollectableCheckBox(clctefSetPassword);
		chkSendPassword = new CollectableCheckBox(clctefSendPassword);
		pwdPassword = new CollectablePasswordField(clctefNewPassword);
		pwdPasswordRepeat = new CollectablePasswordField(clctefNewPasswordRepeat);

		((JCheckBox)chkSetPassword.getControlComponent()).addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDependantControlStates();
				boolean isSelected = false;
				try {
					isSelected = LangUtils.defaultIfNull((Boolean)chkSetPassword.getField().getValue(), Boolean.FALSE);
				} catch (CollectableFieldFormatException ex) {
					throw new NuclosFatalException(ex);
				}
				CollectableComponentModel model = getDetailsEditView().getModel().getCollectableComponentModelFor("requirepasswordchange");
				if (!isNew() && model.getField() != null && !LangUtils.defaultIfNull(model.getField().getValue(), Boolean.FALSE).equals(Boolean.valueOf(isSelected))) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("requirepasswordchange").setField(new CollectableValueField(isSelected));
				}
			}
		});

		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter() {

			@Override
			public void resultModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				int resultMode = ev.getNewCollectState().getInnerState();
				copyPrefsAction.setEnabled((resultMode == CollectState.RESULTMODE_SINGLESELECTION)
					|| (resultMode == CollectState.RESULTMODE_MULTISELECTION));
			}

			@Override
			public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
				// replace components
				replace(PLACEHOLDER_SETPASSWORD, chkSetPassword.getControlComponent());
				replace(PLACEHOLDER_SENDEMAIL, chkSendPassword.getControlComponent());
				replace(PLACEHOLDER_PASSWORD, pwdPassword.getControlComponent());
				replace(PLACEHOLDER_PASSWORDREPEAT, pwdPasswordRepeat.getControlComponent());

				// setup component values
				if (!CollectState.isDetailsModeChangesPending(ev.getNewCollectState().getInnerState())) {
					// enable components (depending on collect state)
					chkSetPassword.setEnabled(!ev.getNewCollectState().isDetailsModeNew() && !ev.getNewCollectState().isDetailsModeMultiViewOrEdit() && (!ldapAuthentication || isSuperuser()));
					chkSetPassword.setField(new CollectableValueField(!chkSetPassword.getControlComponent().isEnabled() && ev.getNewCollectState().isDetailsModeNew()));
					setDependantControlStates();
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("superuser") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("superuser").addCollectableComponentModelListener(ccml_superuser);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("locked") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("locked").addCollectableComponentModelListener(ccml_locked);
				}
			}

			@Override
			public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
				if (getDetailsEditView().getModel().getCollectableComponentModelFor("superuser") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("superuser").removeCollectableComponentModelListener(ccml_superuser);
				}

				if (getDetailsEditView().getModel().getCollectableComponentModelFor("locked") != null) {
					getDetailsEditView().getModel().getCollectableComponentModelFor("locked").removeCollectableComponentModelListener(ccml_locked);
				}
			}
		});
	}

	@Override
	public CollectableMasterDataWithDependants newCollectable() {
		CollectableMasterDataWithDependants clctNew = super.newCollectable();
		clctNew.setField(UserVO.FIELD_REQUIREPASSWORDCHANGE, new CollectableValueField(true));
		return clctNew;
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		UserVO user = new UserVO(clctNew.getMasterDataCVO());
		getAdditionalDataFromView(user);

		final MasterDataVO mdvoInserted = UserDelegate.getInstance().create(user, mpmdvoDependants).toMasterDataVO();

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap) oAdditionalData;

		UserVO user = new UserVO(clct.getMasterDataCVO());
		getAdditionalDataFromView(user);

		final MasterDataVO mdvoUpdated = UserDelegate.getInstance().modify(user, mpclctDependants.toDependantMasterDataMap()).toMasterDataVO();

		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		UserDelegate.getInstance().remove(new UserVO(clct.getMasterDataCVO()));
	}

	private void getAdditionalDataFromView(UserVO user) throws CommonBusinessException {
		user.setSetPassword((Boolean)chkSetPassword.getFieldFromView().getValue());
		user.setNotifyUser((Boolean)chkSendPassword.getFieldFromView().getValue());
		if (user.getSetPassword()) {
			String passwd1 = (String)pwdPassword.getFieldFromView().getValue();
			String passwd2 = (String)pwdPasswordRepeat.getFieldFromView().getValue();

			if (!LangUtils.equals(passwd1, passwd2)) {
				throw new CommonValidationException("exception.password.match");
			}
			user.setNewPassword(passwd1);
		}
	}

	protected void setupDetailsToolBar() {
		if(this.ldapSynchronization){
			final JButton btnSynchronizeWithLDAP = new JButton();
			btnSynchronizeWithLDAP.setName("btnSynchronizeWithLDAP");
			btnSynchronizeWithLDAP.setIcon(Icons.getInstance().getIconLDAP());
			btnSynchronizeWithLDAP.setToolTipText(getCommonLocaleDelegate().getMessage(
					"UserCollectController.1", "Mit LDAP synchronisieren"));

			final UserCollectController uctl = this;
			// action: Select Columns
			btnSynchronizeWithLDAP.setAction(new CommonAbstractAction(btnSynchronizeWithLDAP) {

				@Override
				public void actionPerformed(ActionEvent ev) {
					uctl.cmdSynchronizeUser(uctl);
				}
			});

			this.getDetailsPanel().addToolBarComponent(btnSynchronizeWithLDAP);
		}

		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_MANAGEMENT_CONSOLE)) {
			this.getDetailsPanel().addToolBarComponent(new JButton(copyPrefsAction));
		}
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 * @throws CommonBusinessException
	 *
	 * @deprecated Move to ResultController hierarchy.
	 */
	public void cmdSynchronizeUser(final UserCollectController clctctl) {
		if(!synchronizeWithLDAP()){
			return;
		}
		final SelectUserController<MasterDataVO> ctl = new SelectUserController<MasterDataVO>(clctctl.getFrame(), 
				getCommonLocaleDelegate().getMessage("UserCollectController.2", "LDAP Benutzer"),
				getCommonLocaleDelegate().getMessage(
						"UserCollectController.3", "Ausgew\u00e4hlte Benutzer synchronisieren"), null, null);

		final List<MasterDataVO> lstAvailable = CollectionUtils.typecheck(this.ldapRegisteredUsers, MasterDataVO.class);

		final JTable tbl = getResultTable();

		final ChoiceList<MasterDataVO> ro = new ChoiceList<MasterDataVO>();
		ro.set(lstAvailable, new MasterDataVO.NameComparator());
		ctl.setModel(ro);
		final boolean bOK = ctl.run(getCommonLocaleDelegate().getMessage(
				"SelectUserController.7", "Mit LDAP Synchronisieren"));

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
					getResultController().getSearchResultStrategy().refreshResult();

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
					LOG.warn("synchronizeWithLDAP failed: " + e, e);
					final String sMessage = getCommonLocaleDelegate().getMessage("UserCollectController.4",
						"LDAP Synchronisierung ist gescheitert.\nEine Liste der in LDAP registrierten Benutzer kann nicht dargestellt werden.");
					Errors.getInstance().showExceptionDialog(getFrame(), sMessage, e);
					synchronizedWithLDAP[0] = false;
				}
			}
		});
		return synchronizedWithLDAP[0];
	}

	private void filterOutLDAPUsers(Collection<MasterDataWithDependantsVOWrapper> ldapusers) throws CommonBusinessException {
		final CollectableSearchCondition currentcondition = getSearchStrategy().getCollectableSearchCondition();

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

	private static boolean isLdapAuthenticationEnabled() {
		return SecurityDelegate.getInstance().isLdapAuthenticationActive();
	}

	private static boolean isLdapSynchronizationEnabled() {
	 	return SecurityDelegate.getInstance().isLdapSynchronizationActive();
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
					throw new CommonBusinessException(getCommonLocaleDelegate().getMessage("UserCollectController.5",
						"Ein Benutzer namens \"{0}\" ist bereits im System vorhanden.", clct.getValue("name")));
				}
				break;
			default:
				throw new CommonBusinessException(getCommonLocaleDelegate().getMessage("UserCollectController.6",
					"Es sind bereits mehrere Benutzer(!) unter dem Namen \"{0}\" im System vorhanden.", clct.getValue("name")));
		}
	}

	final Action copyPrefsAction = new AbstractAction() {

		{
			putValue(Action.SHORT_DESCRIPTION, getCommonLocaleDelegate().getMessage("nuclos.preferences.transfer", null));
			putValue(Action.SMALL_ICON, Icons.getInstance().getIconPrefsCopy());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			List<CollectableMasterDataWithDependants> selectedUsers = getSelectedCollectables();
			CopyPreferencesPanel panel = new CopyPreferencesPanel(Main.getInstance().getMainController().getUserName());
			int opt = JOptionPane.showConfirmDialog(UserCollectController.this.getParent(), panel,
					getCommonLocaleDelegate().getMessage("nuclos.preferences.transfer", null),
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
								getCommonLocaleDelegate().getMessage("nuclos.preferences.transfer.error", userName), ex);
					}
				}
			}
		}
	};

	private void replace(String placeholdername, JComponent component) {
		JComponent placeholder = UIUtils.findJComponent(getDetailsPanel(), placeholdername);
		if (placeholder != null) {
			Container container = placeholder.getParent();
			TableLayout layoutManager = (TableLayout) container.getLayout();
			TableLayoutConstraints constraints = layoutManager.getConstraints(placeholder);

			container.remove(placeholder);
			container.add(component, constraints);
		}
	}

	private void setDependantControlStates() {
		boolean isSelected = false;
		try {
			isSelected = LangUtils.defaultIfNull((Boolean)chkSetPassword.getField().getValue(), Boolean.FALSE);
		} catch (CollectableFieldFormatException e) {
			throw new NuclosFatalException(e);
		}
		chkSendPassword.setEnabled(isSelected);
		chkSendPassword.setField(new CollectableValueField(isSelected));
		pwdPassword.setEnabled(isSelected);
		pwdPasswordRepeat.setEnabled(isSelected);
		pwdPassword.setField(new CollectableValueField(null));
		pwdPasswordRepeat.setField(new CollectableValueField(null));
		for (CollectableComponent cc : getDetailsEditView().getCollectableComponentsFor("requirepasswordchange")) {
			cc.getControlComponent().setEnabled(!ldapAuthentication || isSuperuser());
		}
	}

	private boolean isMultiEdit() {
		return getCollectState().isDetailsModeMultiViewOrEdit();
	}

	private boolean isNew() {
		return getCollectState().isDetailsModeNew();
	}

	private boolean isSuperuser() {
		CollectableField cf = getDetailsEditView().getModel().getCollectableComponentModelFor("superuser").getField();
		if (cf != null) {
			return LangUtils.defaultIfNull((Boolean) cf.getValue(), Boolean.FALSE);
		}
		return Boolean.FALSE;
	}
}	// class UserCollectController
