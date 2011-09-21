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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common.preferences.PreferencesConverter;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.nuclos.server.dal.DalUtils;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbInvalidResultSizeException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Facade bean for storing user preferences.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Remote(PreferencesFacadeRemote.class)
@Transactional
public class PreferencesFacadeBean extends NuclosFacadeBean implements PreferencesFacadeRemote {

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getUserPreferences()
	 */
	@Override
	@RolesAllowed("Login")
	public PreferencesVO getUserPreferences() throws CommonFinderException {
		// print out the default encoding on this platform:
		debug("PreferencesFacadeBean.getUserPreferences: Default character encoding on this platform:");
		debug("  System.getProperty(\"file.encoding\") = " + System.getProperty("file.encoding"));
		// debug("  sun.io.Converters.getDefaultEncodingName() = " + sun.io.Converters.getDefaultEncodingName());
		debug("  java.nio.charset.Charset.defaultCharset().name() = " + java.nio.charset.Charset.defaultCharset().name());

		return getUserPreferences(this.getCurrentUserName().toLowerCase());
	}
	
	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getUserPreferences()
	 */
	@Override
	@RolesAllowed("Login")
	public PreferencesVO getTemplateUserPreferences() throws NuclosBusinessException, CommonFinderException{
		PreferencesVO templateUserPrefs = null;
		String templateUser = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_TEMPLATE_USER);
		if (templateUser != null)
			templateUserPrefs = getUserPreferences(templateUser);
		
		return templateUserPrefs;
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#modifyUserPreferences(org.nuclos.server.common.valueobject.PreferencesVO)
	 */
	@Override
	@RolesAllowed("Login")
	public void modifyUserPreferences(PreferencesVO prefsvo) throws CommonFinderException {
		setPreferencesForUser(getCurrentUserName(),prefsvo);
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#setPreferencesForUser(java.lang.String, org.nuclos.server.common.valueobject.PreferencesVO)
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public void setPreferencesForUser(String sUserName, PreferencesVO prefsvo) throws CommonFinderException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));

		Integer userId = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
		if (userId != null) {
			byte[] data = prefsvo == null ? new byte[]{} : convertToBadBytes(prefsvo.getPreferencesBytes());
			DataBaseHelper.getDbAccess().execute(DbStatementUtils.updateValues("T_MD_USER", 
				"OBJPREFERENCES", data).where("INTID", userId));
		}
	}

	/* (non-Javadoc)
	 * @see org.nuclos.ejb3.PreferencesFacadeRemote#getPreferencesForUser(java.lang.String)
	 */
	@Override
	@RolesAllowed("UseManagementConsole")
	public PreferencesVO getPreferencesForUser(String sUserName) throws CommonFinderException {
		return getUserPreferences(sUserName);
	}
	
	@Override
	@RolesAllowed("UseManagementConsole")
	public void mergePreferencesForUser(String targetUser, Map<String, Map<String, String>> preferencesToMerge) throws CommonFinderException {
		try {
			NavigableMap<String, Map<String, String>> preferencesMap;
			PreferencesVO preferencesVO = getUserPreferences(targetUser);
			if (preferencesVO != null && preferencesVO.getPreferencesBytes().length > 0) {
				preferencesMap = PreferencesConverter.loadPreferences(
					new ByteArrayInputStream(preferencesVO.getPreferencesBytes()));
			} else {
				preferencesMap = new TreeMap<String, Map<String, String>>();
			}

			// Merge preferences
			preferencesMap.putAll(preferencesToMerge);
			
			// Save merged preferences
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PreferencesConverter.writePreferences(baos, preferencesMap, true);
			setPreferencesForUser(targetUser, new PreferencesVO(baos.toByteArray()));
		} catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}
	
	/**
	 * 
	 * @return
	 */
	@Override
	public List<String> getWorkspaceNames() {
		Integer userId = SecurityCache.getInstance().getUserId(getCurrentUserName());
		
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_WORKSPACE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRNAME", String.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_USER", Integer.class), userId));
		
		return DataBaseHelper.getDbAccess().executeQuery(query);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 * @throws CommonFinderException
	 */
	@Override
	public WorkspaceDescription getWorkspace(String name) throws CommonFinderException {
		Integer userId = SecurityCache.getInstance().getUserId(getCurrentUserName());
		
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("T_MD_WORKSPACE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("CLBWORKSPACE", String.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_USER", Integer.class), userId),
			builder.equal(t.baseColumn("STRNAME", String.class), name)));
		
		try {
			String xml = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			return (WorkspaceDescription) (new XStream(new DomDriver())).fromXML(xml);
		} 
		catch (DbInvalidResultSizeException e) {
			throw new CommonFinderException("There is no workspace stored for the current user and given name. (" 
					+ getCurrentUserName() + ", " + name + "): " + e.toString());
		}
		catch (XStreamException e) {
			throw new CommonFinderException("There is no workspace stored for the current user and given name. (" 
					+ getCurrentUserName() + ", " + name + "): " + e.toString());
		}
	}
	
	/**
	 * 
	 * @param wd
	 */
	@Override
	public void storeWorkspace(WorkspaceDescription wd) {
		if (wd.getName() == null) {
			throw new IllegalArgumentException("Name must not be null");
		}
		Integer userId = SecurityCache.getInstance().getUserId(getCurrentUserName());
		
		String xml = (new XStream(new DomDriver())).toXML(wd);
		
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_WORKSPACE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.and(
			builder.equal(t.baseColumn("INTID_T_MD_USER", Integer.class), userId),
			builder.equal(t.baseColumn("STRNAME", String.class), wd.getName())));
		
		try {
			Integer workspaceId = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			DataBaseHelper.getDbAccess().execute(DbStatementUtils.updateValues("T_MD_WORKSPACE", 
				"CLBWORKSPACE", xml).where("INTID", workspaceId));
		} catch (DbInvalidResultSizeException e) {
			EntityObjectVO eo = new EntityObjectVO();
			eo.initFields(3, 1);
			eo.flagNew();
			eo.setId(DalUtils.getNextId());
			eo.getFields().put("name", wd.getName());
			eo.getFields().put("clbworkspace", xml);
			eo.getFieldIds().put("user", LangUtils.convertId(userId));
			DalUtils.updateVersionInformation(eo, getCurrentUserName());
			NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.WORKSPACE).insertOrUpdate(eo);
		}
	}
	
	/**
	 * 
	 * @param name
	 */
	@Override
	public void removeWorkspace(String name) {
		Integer userId = SecurityCache.getInstance().getUserId(getCurrentUserName());
		
		DataBaseHelper.getDbAccess().execute(DbStatementUtils.deleteFrom("T_MD_WORKSPACE", "INTID_T_MD_USER", userId, "STRNAME", name));
	}

	/**
	 * @param sUserName
	 * @return the entity bean corresponding to the user with the given name
	 * @throws CommonFinderException
	 */
	private PreferencesVO getUserPreferences(String sUserName) throws CommonFinderException {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<byte[]> query = builder.createQuery(byte[].class);
		DbFrom t = query.from("T_MD_USER").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("OBJPREFERENCES", byte[].class));
		query.where(builder.equal(builder.upper(t.baseColumn("STRUSER", String.class)), builder.upper(builder.literal(sUserName))));
		
		try {
			byte[] b = DataBaseHelper.getDbAccess().executeQuerySingleResult(query);
			if (b != null) {
				return new PreferencesVO(convertFromBadBytes(b)); 
			}
		} catch (DbInvalidResultSizeException e) {
			throw new CommonFinderException("There are no stored preferences for the current user.");
		}
		return null;
	}
	
	// Previously, the preferences were transferred as Strings.  This is obviously wrong because they
	// are bytes (Java's Preferences export/import API works with byte streams).  Moreover, client and
	// servers used different charsets to encode/decode the bytes (UTF-8 on the client side, and the
	// server's native encoding in this Facade) 
	// => the stored BLOBs are broken (invalid XML w.r.t encoding)!
	// 
	// The following code imitates the old behavior to work around these issues.  If DB migrations are
	// possible, please migrate all BLOBs once using convertFromBadBytes (from native->UTF-8)
	//
	// For a test case, use preferences with some umlauts!
	
	@Deprecated
	private static byte[] convertFromBadBytes(byte[] b) {
		// interpret bytes as native encoded string and convert them back using UTF-8 
		return new String(b).getBytes(UTF_8);
	}
	
	@Deprecated
	private static byte[] convertToBadBytes(byte[] b) {
		// interpret bytes as UTF-8 string and convert them back using the native encoding
		return new String(b, UTF_8).getBytes();
	}
	
	private final static Charset UTF_8 = Charset.forName("UTF-8");
}
