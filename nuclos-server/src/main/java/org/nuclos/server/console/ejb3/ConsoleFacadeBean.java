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
package org.nuclos.server.console.ejb3;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.CommandMessage;
import org.nuclos.common.ConsoleConstants;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.Priority;
import org.nuclos.common.RuleNotification;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.autosync.SchemaValidator;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbSimpleView;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.migration.vm2m5.MigrationVm2m5;
import org.nuclos.server.report.SchemaCache;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.statemodel.valueobject.StateModelUsagesCache;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all NuclosConsole functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Local(ConsoleFacadeLocal.class)
@Remote(ConsoleFacadeRemote.class)
@Transactional
@RolesAllowed("UseManagementConsole")
public class ConsoleFacadeBean extends NuclosFacadeBean implements ConsoleFacadeLocal, ConsoleFacadeRemote {

	@Override
	@PostConstruct
	public void postConstruct() {
		super.postConstruct();
	}

	/**
	 *
	 * @param sMessage the message to send
	 * @param sUser the receiver of this message (all users if null)
	 * @param priority
	 * @param sAuthor the author of the message
	 */
	@Override
	public void sendClientNotification(String sMessage, String sUser, Priority priority, String sAuthor) {
		NuclosJMSUtils.sendObjectMessage(new RuleNotification(priority, sMessage, sAuthor), JMSConstants.TOPICNAME_RULENOTIFICATION, LangUtils.defaultIfNull(sUser, JMSConstants.BROADCAST_MESSAGE));
//		final ClientNotifier clientNotifier = new ClientNotifier(JMSConstants.TOPICNAME_RULENOTIFICATION);
//		clientNotifier.notifyClients(new RuleNotification(priority, sMessage, sAuthor), LangUtils.defaultIfNull(sUser, JMSConstants.BROADCAST_MESSAGE));
	}

	/**
	 * end all the clients of sUser
	 * @param sUser if null for all users
	 */
	@Override
	public void killSession(String sUser) {
		
		NuclosJMSUtils.sendObjectMessage(new CommandMessage(CommandMessage.CMD_SHUTDOWN), JMSConstants.TOPICNAME_RULENOTIFICATION, LangUtils.defaultIfNull(sUser, JMSConstants.BROADCAST_MESSAGE));
		
//		final ClientNotifier clientNotifier = new ClientNotifier(JMSConstants.TOPICNAME_RULENOTIFICATION);
//		clientNotifier.notifyClients(new CommandMessage(CommandMessage.CMD_SHUTDOWN), LangUtils.defaultIfNull(sUser, JMSConstants.BROADCAST_MESSAGE));
	}

	/**
	 * check for VIEWS and FUNCTIONS which are invalid and compile them
	 * @throws SQLException 
	 */
	@Override
	public void compileInvalidDbObjects() throws SQLException {
		info("compiling invalid db objects (views and functions)");
		DataBaseHelper.getDbAccess().validateObjects();
	}

	/**
	 * finds attribute values which should be assigned to a value list entry and creates a script to assign them if possible
	 * @return the number of bad attribute values found
	 */
//	public int updateAttributeValueListAssignment(final String sOutputFileName) {
//			final String sSql = "SELECT "+
//					ApplicationProperties.getInstance().getName()+
//					".GET_NUCLEUS_ATTRIBUTE_VALUE(dplo.intid_t_ud_genericobject, 'nuclosSystemId') system_id," +
//							"dplo.intid record_id, a.strattribute, a.intid attribut_id," +
//							"dplo.strvalue attributwert, werteliste.intid werteliste_id," +
//							"werteliste.strvalue werteliste_wert,\n" +
//							"werteliste.strmnemonic werteliste_kuerzel\n" +
//							"FROM t_md_attributevalue werteliste," +
//							"(SELECT *\n" +
//							"FROM T_UD_GO_ATTRIBUTE\n " +
//							"WHERE intid_t_dp_value IS NULL\n" +
//							"AND intid_external IS NULL\n" +
//							"AND intid_t_md_attribute IN (\n" +
//							"SELECT DISTINCT intid\n" +
//							"         FROM t_md_attribute\n" +
//							"        WHERE intid IN (\n" +
//							"                 SELECT DISTINCT intid_t_md_attribute\n" +
//							"                            FROM t_md_attributevalue))) dplo,\n" +
//							"t_md_attribute a\n" +
//							"WHERE (   werteliste.strvalue LIKE dplo.strvalue\n" +
//							"OR werteliste.strmnemonic LIKE dplo.strvalue\n" +
//							")\n" +
//							"AND werteliste.intid_t_md_attribute = dplo.intid_t_md_attribute\n" +
//							"AND dplo.intid_t_md_attribute = a.intid\n";
//
//			return DataBaseHelper.runSQLSelect(NuclosDataSources.getDefaultDS(), sSql, new NovabitDataBaseRunnable<Integer>() {
//				public Integer run(ResultSet rs) {
//					PrintStream ps = null;
//					try {
//						ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(sOutputFileName)), true);
//						int count = 0;
//
//						while (rs.next()) {
//							final String sVlValue = rs.getString("werteliste_wert");
//							final String sVlMnemonic = rs.getString("werteliste_kuerzel");
//							final String sAttributeValue = rs.getString("attributwert");
//							final String sAttribute = rs.getString("strattribute");
//							final String sSystemId = rs.getString("system_id");
//							final AttributeCVO attrVo = AttributeCache.getInstance().getAttribute(sAttribute);
//							if ((attrVo.isShowMnemonic() && sVlMnemonic.compareTo(sAttributeValue) == 0) ||
//									(!attrVo.isShowMnemonic() && sVlValue.compareTo(sAttributeValue) == 0)) {
//								ps.println("-- Statement f\u00fcr Objekt: " + sSystemId + " - Attribut: " + sAttribute);
//								ps.println("UPDATE T_UD_GO_ATTRIBUTE set intid_t_dp_value = " + rs.getInt("werteliste_id") + " WHERE intid = " + rs.getInt("record_id") + ";");
//							}
//							else {
//								ps.println("-- " + sSystemId + ": By the attribute " + sAttribute + " cannot be assigned the value " + sAttributeValue + " (The state of the show mnemonic in the master data of the attribute does not match");//Zustand von zeige K\u00fcrzel in den Stammdaten des Attributs stimmt nicht \u00fcberein");
//							}
//							count++;
//						}
//						if (count == 0) {
//							ps.println("-- No missing references found");
//						}
//						return count;
//					}
//					catch (SQLException ex) {
//						throw new NuclosFatalException(ex);
//					}
//					catch (FileNotFoundException ex) {
//						throw new NuclosFatalException(ex);
//					}
//					finally {
//						if (ps != null) {
//							ps.close();
//						}
//						if (ps != null && ps.checkError()) {
//							throw new NuclosFatalException("Failed to close PrintStream.");
//						}
//					}
//				}
//			});
//	}

	/**
	 * invalidateAllServerSide Caches
	 */
	@Override
	public String invalidateAllCaches() {
		final StringBuilder sbResult = new StringBuilder("Revalidating ParameterCache\n");
		ServerParameterProvider.getInstance().revalidate();

		sbResult.append("Revalidating MasterDataMetaCache\n");
		MasterDataMetaCache.getInstance().revalidate();

		sbResult.append("Invalidating SecurityCache\n");
		SecurityCache.getInstance().invalidate();

		sbResult.append("Invalidating AttributeCache\n");
		AttributeCache.getInstance().revalidate();


		sbResult.append("Invalidating RuleCache\n");
		RuleCache.getInstance().invalidate();

		sbResult.append("Invalidating SchemaCache\n");
		SchemaCache.getInstance().invalidate();

		sbResult.append("Invalidating DatasourceCache\n");
		DatasourceCache.getInstance().invalidate();

		sbResult.append("Invalidating StateCache\n");
		StateCache.getInstance().invalidate();

		sbResult.append("Invalidating StateModelUsagesCache\n");
		StateModelUsagesCache.getInstance().revalidate();

		sbResult.append("Invalidating ResourceCache\n");
		ResourceCache.getInstance().invalidate();

		sbResult.append("Invalidating Locale Caches\n");
		ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).flushInternalCaches();

		sbResult.append("ready");
		return sbResult.toString();
	}

	/**
	 * get Infomation about the database in use
	 */
	@Override
	public String getDatabaseInformationAsHtml() {
		final StringBuilder sb = new StringBuilder("<b>Database Meta Information</b><br>");
		sb.append("<HTML><table border=\"1\">");
		for (Map.Entry<String, Object> e : DataBaseHelper.getDbAccess().getMetaDataInfo().entrySet()) {
			sb.append(String.format("<tr><td><b>%s</b></td><td>%s</td></tr>", e.getKey(), e.getValue()));
		}

		sb.append("</table><br><b>Vendor specific parameters:<br><table border=\"1\">");
		final Map<String, String> mpDbParameters = DataBaseHelper.getDbAccess().getDatabaseParameters();
		for (String sParameter : mpDbParameters.keySet()) {
			sb.append("<tr><td><b>" + sParameter + "</b></td><td>" + mpDbParameters.get(sParameter) + "</td></tr>");
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	/**
	 * get the system properties of the server
	 */
	@Override
	public String getSystemPropertiesAsHtml() {
		final StringBuilder sbClient = new StringBuilder();
		sbClient.append("<html><b>Java System Properties (Server):</b>");
		sbClient.append("<table border=\"1\">");
		for (final Object key : System.getProperties().keySet()) {
			sbClient.append("<tr><td><b>" + (String)key + "</b></td><td>" + System.getProperty((String)key) + "</td></tr>");
		}
		sbClient.append("</table></html>\n");
		return sbClient.toString();
	}

	/**
	 *
	 * @param sCommand
	 * @throws CommonBusinessException
	 */
	@Override
	public void executeCommand(String sCommand) throws CommonBusinessException {

		String sCommandLowerCase = sCommand.toLowerCase();
		if (sCommandLowerCase.equals(ConsoleConstants.CMD_MIGRATE_METADATA_AND_GO_TO_NUC_2_5.toLowerCase())) {
			MigrationVm2m5 mig = new MigrationVm2m5();
			mig.startMigration();
		}
		else if (sCommandLowerCase.equals(ConsoleConstants.CMD_REFRESH_VIEWS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(DataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstDropStructureChanges = SchemaUtils.drop(dbHelper.getSchema().values());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());

			for (DbStructureChange sc : lstDropStructureChanges) {
				DbArtifact af = sc.getArtifact1();
				if (af instanceof DbSimpleView) {
					try {
						info("DROP " + ((DbSimpleView)af).getViewName());
						DataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}

			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbSimpleView) {
					try {
						info("CREATE " + ((DbSimpleView)af).getViewName());
						DataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}

		}
		else if(sCommandLowerCase.equals(ConsoleConstants.CMD_CREATE_UNIQUECONSTRAINTS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(DataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());
			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbUniqueConstraint) {
					try {
						DbUniqueConstraint c = (DbUniqueConstraint)af;
						info("CREATE " + c.getConstraintName());
						DataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}
		}
		else if(sCommandLowerCase.equals(ConsoleConstants.CMD_CREATE_FOREIGNCONSTRAINTS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(DataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());
			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbForeignKeyConstraint) {
					try {
						DbForeignKeyConstraint c = (DbForeignKeyConstraint)af;
						info("CREATE " + c.getConstraintName());
						DataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}
		}
		else if(sCommandLowerCase.startsWith(ConsoleConstants.CMD_VALIDATE_SCHEMA.toLowerCase())) {
			SchemaValidator validator = new SchemaValidator(DataBaseHelper.getDbAccess(), sCommandLowerCase.split("\\s+"));
			validator.validate();
		}
		else {
			throw new CommonBusinessException("Unknown command: " + sCommand + "\n");
		}
	}
}
