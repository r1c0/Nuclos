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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.CommandMessage;
import org.nuclos.common.ConsoleConstants;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.Priority;
import org.nuclos.common.RuleNotification;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.autosync.SchemaValidator;
import org.nuclos.server.common.AttributeCache;
import org.nuclos.server.common.DatasourceCache;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.StateCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbStructureChange.Type;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbForeignKeyConstraint;
import org.nuclos.server.dblayer.structure.DbConstraint.DbUniqueConstraint;
import org.nuclos.server.dblayer.structure.DbSimpleView;
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
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("UseManagementConsole")
public class ConsoleFacadeBean extends NuclosFacadeBean implements ConsoleFacadeRemote {
	
	private static final Logger LOG = Logger.getLogger(ConsoleFacadeBean.class);
	
	public ConsoleFacadeBean() {
	}

	/**
	 *
	 * @param sMessage the message to send
	 * @param sUser the receiver of this message (all users if null)
	 * @param priority
	 * @param sAuthor the author of the message
	 */
	public void sendClientNotification(String sMessage, String sUser, Priority priority, String sAuthor) {
		LOG.info("JMS send client notification to user " + sUser + ": " + sMessage + ": " + this);
		final RuleNotification rn = new RuleNotification(priority, sMessage, sAuthor);
		if (sUser != null) {
			NuclosJMSUtils.sendObjectMessageAfterCommit(
					rn, JMSConstants.TOPICNAME_RULENOTIFICATION, sUser);
		}
		else {
			NuclosJMSUtils.sendOnceAfterCommitDelayed(rn, JMSConstants.TOPICNAME_RULENOTIFICATION);
		}
	}

	/**
	 * end all the clients of sUser
	 * @param sUser if null for all users
	 */
	public void killSession(String sUser) {
		LOG.info("JMS send killSession " + sUser + ": " + this);
		final CommandMessage cm = new CommandMessage(CommandMessage.CMD_SHUTDOWN);
		if (sUser != null) {
			NuclosJMSUtils.sendObjectMessageAfterCommit(
					cm, JMSConstants.TOPICNAME_RULENOTIFICATION, sUser);
		}
		else {
			NuclosJMSUtils.sendOnceAfterCommitDelayed(
					cm, JMSConstants.TOPICNAME_RULENOTIFICATION);
		}
	}

	/**
	 * check for VIEWS and FUNCTIONS which are invalid and compile them
	 * @throws SQLException 
	 */
	public void compileInvalidDbObjects() throws SQLException {
		info("compiling invalid db objects (views and functions)");
		dataBaseHelper.getDbAccess().validateObjects();
	}

	/**
	 * invalidateAllServerSide Caches
	 */
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
		ServerServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).flushInternalCaches();

		sbResult.append("ready");
		return sbResult.toString();
	}

	/**
	 * get Infomation about the database in use
	 */
	public String getDatabaseInformationAsHtml() {
		final StringBuilder sb = new StringBuilder("<b>Database Meta Information</b><br>");
		sb.append("<HTML><table border=\"1\">");
		for (Map.Entry<String, Object> e : dataBaseHelper.getDbAccess().getMetaDataInfo().entrySet()) {
			sb.append(String.format("<tr><td><b>%s</b></td><td>%s</td></tr>", e.getKey(), e.getValue()));
		}

		sb.append("</table><br><b>Vendor specific parameters:<br><table border=\"1\">");
		final Map<String, String> mpDbParameters = dataBaseHelper.getDbAccess().getDatabaseParameters();
		for (String sParameter : mpDbParameters.keySet()) {
			sb.append("<tr><td><b>" + sParameter + "</b></td><td>" + mpDbParameters.get(sParameter) + "</td></tr>");
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	/**
	 * get the system properties of the server
	 */
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
	public void executeCommand(String sCommand) throws CommonBusinessException {

		String sCommandLowerCase = sCommand.toLowerCase();
		if (sCommandLowerCase.equals(ConsoleConstants.CMD_MIGRATE_METADATA_AND_GO_TO_NUC_2_5.toLowerCase())) {
			MigrationVm2m5 mig = new MigrationVm2m5();
			mig.startMigration();
		}
		else if (sCommandLowerCase.equals(ConsoleConstants.CMD_REFRESH_VIEWS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstDropStructureChanges = SchemaUtils.drop(dbHelper.getSchema().values());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());

			for (DbStructureChange sc : lstDropStructureChanges) {
				DbArtifact af = sc.getArtifact1();
				if (af instanceof DbSimpleView) {
					try {
						info("DROP " + ((DbSimpleView)af).getViewName());
						dataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}

			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbSimpleView) {
					try {
						info("CREATE " + ((DbSimpleView)af).getViewName());
						dataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}

		}
		else if(sCommandLowerCase.equals(ConsoleConstants.CMD_CREATE_UNIQUECONSTRAINTS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());
			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbUniqueConstraint) {
					try {
						DbUniqueConstraint c = (DbUniqueConstraint)af;
						info("CREATE " + c.getConstraintName());
						dataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}
		}
		else if(sCommandLowerCase.equals(ConsoleConstants.CMD_CREATE_FOREIGNCONSTRAINTS.toLowerCase())) {
			EntityObjectMetaDbHelper dbHelper = new EntityObjectMetaDbHelper(dataBaseHelper.getDbAccess(), MetaDataServerProvider.getInstance());
			List<DbStructureChange> lstCreateStructureChanges = SchemaUtils.create(dbHelper.getSchema().values());
			for (DbStructureChange sc : lstCreateStructureChanges) {
				DbArtifact af = sc.getArtifact2();
				if (af instanceof DbForeignKeyConstraint) {
					try {
						DbForeignKeyConstraint c = (DbForeignKeyConstraint)af;
						info("CREATE " + c.getConstraintName());
						dataBaseHelper.getDbAccess().execute(sc);
					} catch (Exception ex) {};
				}
			}
		}
		else if(sCommandLowerCase.startsWith(ConsoleConstants.CMD_VALIDATE_SCHEMA.toLowerCase())) {
			SchemaValidator validator = new SchemaValidator(dataBaseHelper.getDbAccess(), sCommandLowerCase.split("\\s+"));
			validator.validate();
		}
		else {
			throw new CommonBusinessException("Unknown command: " + sCommand + "\n");
		}
	}

	@Override
	public String[] rebuildConstraints() {
		DbAccess dbAccess = dataBaseHelper.getDbAccess();
		List<String> result = new ArrayList<String>();
		for (DbConstraint c : getConstraints()) {
			String name = c.getConstraintName();
			String details = c.toString();
			try {
				dbAccess.execute(new DbStructureChange(Type.DROP, c));
				LOG.info(name + " successfully dropped.");
				result.add(name + " successfully dropped.");
			} catch (Exception ex) {
				LOG.error(name + " not dropped! " + details, ex);
				result.add(name + " not dropped! " + details + " (" + ex.getMessage() + ")");
			}
			try {
				dbAccess.execute(new DbStructureChange(Type.CREATE, c));
				LOG.info(name + " successfully created.");
				result.add(name + " successfully created.");
			} catch (Exception ex) {
				LOG.error(name + " not created! " + details, ex);
				result.add(name + " not created! " + details + " (" + ex.getMessage() + ")");
			}
		}
		return result.toArray(new String[]{});
	}
	
	private List<DbConstraint> getConstraints() {
		DbAccess dbAccess = dataBaseHelper.getDbAccess();
		EntityObjectMetaDbHelper helper = new EntityObjectMetaDbHelper(dbAccess, MetaDataServerProvider.getInstance());
		
		List<DbConstraint> result = new ArrayList<DbConstraint>();
		for (EntityMetaDataVO eMeta : MetaDataServerProvider.getInstance().getAllEntities()) {
			result.addAll(helper.getDbTable(eMeta).getTableArtifacts(DbForeignKeyConstraint.class));
			result.addAll(helper.getDbTable(eMeta).getTableArtifacts(DbUniqueConstraint.class));
		}
		return result;
	}
}
