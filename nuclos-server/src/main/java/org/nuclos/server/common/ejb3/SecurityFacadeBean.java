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
package org.nuclos.server.common.ejb3;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.security.auth.login.LoginException;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.CommandInformationMessage;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common.transport.GzipMap;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for searchfilter. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Stateless
@Local(SecurityFacadeLocal.class)
@Remote(SecurityFacadeRemote.class)
@Transactional
public class SecurityFacadeBean extends NuclosFacadeBean implements SecurityFacadeLocal, SecurityFacadeRemote {

	@PostConstruct
	@Override
	public void postConstruct() {
      super.postConstruct();
      this.info("Authentication successful.");
	}

	/**
	 * logs the current user in.
	 * @return session id for the current user
	 */
	@Override
    @RolesAllowed("Login")
	public Integer login() {
		final Integer result = writeLoginProtocol(this.getCurrentUserName());
		this.info("User " + this.getCurrentUserName() + " successfully logged in.");
		return result;
	}

	/**
	 * logs the current user out.
	 * @param iSessionId session id for the current user
	 */
	@Override
    @RolesAllowed("Login")
	public void logout(Integer iSessionId) throws LoginException {
		this.writeLogoutProtocol(iSessionId);
		this.info("User " + this.getCurrentUserName() + " logged out.");
	}

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@Override
    @RolesAllowed("Login")
	public ApplicationProperties.Version getCurrentApplicationVersionOnServer() {
		return ApplicationProperties.getInstance().getNuclosVersion();
	}

	/**
	 * @return information about the current version of the application installed on the server.
	 */
	@Override
    @RolesAllowed("Login")
	public String getUserName() {
		return this.getCurrentUserName();
	}

	/**
	 * Get all actions that are allowed for the current user.
	 * @return set that contains the Actions objects (no duplicates).
	 */
	@Override
    @RolesAllowed("Login")
	public Set<String> getAllowedActions() {
		return SecurityCache.getInstance().getAllowedActions(this.getCurrentUserName());
	}

	/**
	 * @return the module permissions for the current user.
	 */
	@Override
    @RolesAllowed("Login")
	public ModulePermissions getModulePermissions() {
		return SecurityCache.getInstance().getModulePermissions(this.getCurrentUserName());
	}

	/**
	 * @return the masterdata permissions for the current user.
	 */
	@Override
    @RolesAllowed("Login")
	public MasterDataPermissions getMasterDataPermissions() {
		return SecurityCache.getInstance().getMasterDataPermissions(this.getCurrentUserName());
	}

	/**
	 * get a String representation of the users session context
	 */
	@Override
    @RolesAllowed("Login")
	public String getSessionContextAsString() {
		return this.getCurrentUserName();
	}

	/**
	 * write the successful login into the protocol table
	 */
	private Integer writeLoginProtocol(String sUserName) {
		try {
			final String sApplicationName = ApplicationProperties.getInstance().getName();

			DbId nextId = new DbId();
			DataBaseHelper.execute(DbStatementUtils.insertInto("T_AD_SESSION_STATISTIC",
				"SESSION_ID", nextId,
				"USER_ID", sUserName,
				"APPLICATION", sApplicationName,
				"LOGON", new InternalTimestamp(Calendar.getInstance().getTimeInMillis())));

			return nextId.getIdValueInt();
		}
		catch (Exception ex) {
			throw new CommonFatalException("Could not write login protocol", ex);//"Konnte Login nicht protokollieren."
		}
	}

	/**
	 * write the successful logout into the protocol table
	 */
	private void writeLogoutProtocol(Integer iSessionId) {
		DataBaseHelper.execute(DbStatementUtils.updateValues("T_AD_SESSION_STATISTIC",
			"LOGOFF", new InternalTimestamp(Calendar.getInstance().getTimeInMillis())).where("SESSION_ID", iSessionId));
	}

	/**
	 * change the password of the logged in user after validating the old password
	 * @param sOldPassword
	 * @param sNewPassword
	 * @throws NuclosBusinessException
	 */
	@Override
    @RolesAllowed("Login")
	public void changePassword(String sOldPassword, String sNewPassword) throws NuclosBusinessException {
		final String sUserName = this.getCurrentUserName();
		String sCurrentPassword = getEncryptedUserPassword(sUserName);
		if(sCurrentPassword==null)sCurrentPassword="";
		if(sCurrentPassword.equals(sOldPassword) || sCurrentPassword != null && sCurrentPassword.equals(StringUtils.encryptBase64(sUserName.toLowerCase() + sOldPassword))) {
			this.changeUserPassword(sUserName, sNewPassword);
		} else {
			throw new NuclosBusinessException("security.invalid.old.password.exception");
				//"Das angegebene alte Passwort ist nicht korrekt.");
		}
	}

	/**
	 * change the password of the given user without validating the old password used in the management console.
	 * The encrypted password is build with the username and the given password
	 * @param sOldPassword
	 * @param sNewPassword
	 * @return
	 * @throws NuclosBusinessException
   */
	@Override
    @RolesAllowed("UseManagementConsole")
	public void changeUserPassword(String sUserName, String sNewPassword) throws NuclosBusinessException {
		final String sNewPasswordEncrypted = StringUtils.encryptBase64(sUserName + sNewPassword);

		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias("t");
		query.select(t.column("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.column("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		Integer userId = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);

		DataBaseHelper.execute(DbStatementUtils.updateValues("T_MD_USER", "STRPASSWORD", sNewPasswordEncrypted).where("INTID", userId));

		SecurityCache.getInstance().invalidate();
		NuclosJMSUtils.sendObjectMessage(new CommandInformationMessage(
			CommandInformationMessage.CMD_INFO_SHUTDOWN,
			"The password has been successful changed.\n\n"
			+ "The application will be terminated automatically in 10 seconds.\n\n"
			+ "Log in please again.", false), JMSConstants.TOPICNAME_RULENOTIFICATION,
		sUserName);

//		final ClientNotifier clientNotifier = new ClientNotifier(JMSConstants.TOPICNAME_RULENOTIFICATION);
//		clientNotifier.notifyClients(
//			new CommandInformationMessage(
//				CommandInformationMessage.CMD_INFO_SHUTDOWN,
//				"The password has been successful changed.\n\n"
//				+ "The application will be terminated automatically in 10 seconds.\n\n"
//				+ "Log in please again.", false),
////				"Das Passwort wurde erfolgreich ge\u00e4ndert.\n\n"
////			   + "Die Applikation wird in 10 Sekunden automatisch beendet.\n\n"
////			   + "Loggen Sie sich bitte erneut ein.", false),
//			sUserName);
	}


	/**
	 * get the encrypted user password from the database
	 * @param sUserName the user name in lower case characters
	 * @return
	 * @throws CommonFatalException
	 */
	private String getEncryptedUserPassword(final String sUserName) {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_USER").alias("t");
		query.select(t.column("STRPASSWORD", String.class));
		query.where(builder.equal(builder.upper(t.column("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		return DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
	}

	/**
	 * get the user id from the database
	 * @param sUserName the user name in lower case characters
	 * @return
	 * @throws CommonFatalException
	 */
	@Override
	public Integer getUserId(final String sUserName) throws CommonFatalException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias("t");
		query.select(t.column("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.column("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		Integer executeQuerySingleResult = null;
		try{
			executeQuerySingleResult = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		} catch (DbInvalidResultSizeException e){
			throw new CommonFatalException("Could not find user "+sUserName);
		}
		return executeQuerySingleResult;
	}

	/**
	 * @return the readable subforms
	 */
	@Override
    public java.util.Map<Integer, org.nuclos.common.security.Permission> getSubFormPermission(String sEntityName) {
		return SecurityCache.getInstance().getSubForm(getCurrentUserName(), sEntityName);
	}

	/**
	 * determine the permission of an attribute regarding the state of a module for the current user
	 * @param sAttributeName
	 * @param iModuleId
	 * @param iStatusNumeral
	 * @return Permission
	 */
	@Override
    public Permission getAttributePermission(String sEntity, String sAttributeName, Integer iState) {
		// special behaviour for modules which have no statusnumeral (e.g. Rechnungsabschnitte)
		  if (iState == null) {
		   return Permission.READWRITE;
		  }

		  Integer iAttributeGroupId = AttributeCache.getInstance().getAttribute(sEntity, sAttributeName).getAttributegroupId();
		  Map<Integer, Permission> mpAttributePermission = SecurityCache.getInstance().getAttributeGroup(getCurrentUserName(), iAttributeGroupId);

		  return mpAttributePermission.get(iState);
	}


	@Override
	public Map<String, Permission> getAttributePermissionsByEntity(String entity, Integer stateId) {
		HashMap<String, Permission> res = new HashMap<String, Permission>();
		MetaDataServerProvider mdProv = MetaDataServerProvider.getInstance();
		SecurityCache secCache = SecurityCache.getInstance();
		String user = getCurrentUserName();

		Map<String, EntityFieldMetaDataVO> fields = mdProv.getAllEntityFieldsByEntity(entity);
		for(Map.Entry<String, EntityFieldMetaDataVO> e : fields.entrySet()) {
			Permission p;
			if(stateId == null)
				p = Permission.READWRITE;
			else
				p = secCache.getAttributeGroup(
						user,
						LangUtils.convertId(e.getValue().getFieldGroupId()))
					.get(stateId);
			res.put(e.getKey(), p);
		}
		return res;
	}


	@Override
    @RolesAllowed("Login")
	public Boolean isSuperUser() {
		return SecurityCache.getInstance().isSuperUser(this.getCurrentUserName());
	}

	@Override
    @RolesAllowed("Login")
	public void invalidateCache(){
		SecurityCache.getInstance().invalidate();
	}

	@Override
    public Map<String, Object> getInitialSecurityData() {
		GzipMap<String, Object> res = new GzipMap<String, Object>();
		res.put(SecurityFacadeRemote.IS_SUPER_USER, isSuperUser());
		res.put(SecurityFacadeRemote.ALLOWED_ACTIONS, getAllowedActions());
		res.put(SecurityFacadeRemote.MODULE_PERMISSIONS, getModulePermissions());
		res.put(SecurityFacadeRemote.MASTERDATA_PERMISSIONS, getMasterDataPermissions());
		return res;
    }

	@Override
	public Boolean isLdapAuthenticationActive() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_AD_LDAPSERVER").alias("t");
		query.select(t.column("INTID", Long.class));
		query.where(builder.and(builder.equal(t.column("BLNACTIVE", Boolean.class), true), builder.isNotNull(t.column("STRUSERFILTER", String.class))));

		return DataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}

	@Override
	public Boolean isLdapSynchronizationActive() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Long> query = builder.createQuery(Long.class);
		DbFrom t = query.from("T_AD_LDAPSERVER").alias("t");
		query.select(t.column("INTID", Long.class));
		query.where(builder.and(builder.equal(t.column("BLNACTIVE", Boolean.class), true), builder.isNotNull(t.column("SEARCHFILTER", String.class))));

		return DataBaseHelper.getDbAccess().executeQuery(query).size() > 0;
	}
}
