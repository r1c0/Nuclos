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
package org.nuclos.client.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.CreateException;
import javax.security.auth.login.LoginException;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.attribute.AttributeDelegate;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.layout.LayoutDelegate;
import org.nuclos.client.login.ServerConfiguration;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.rule.TimelimitRuleDelegate;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.updatejobs.MigrateSearchFilterPreferences;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.ConsoleConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.Priority;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.attribute.valueobject.LayoutVO;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeRemote;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeRemote;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.ruleengine.valueobject.RuleWithUsagesVO;

/**
 * Management console for Nucleus.
 * @todo the JNDI URL is read from jndi.properties currently. It should be given on the command line instead.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class NuclosConsole extends ConsoleConstants {

	private static String sUserName;

	/**
	 * this list is used by NuclosConsoleGui to show the available commands
	 */
	public static final List<String> LSTCOMMANDS = Arrays.asList(
			CMD_INVALIDATE_ATTRIBUTECACHE, CMD_INVALIDATE_METADATACACHE, CMD_SHOWDEVELOPERCOMMANDS,
			CMD_REFRESHVIEWS, CMD_SCHEDULE_REPORT_JOB, CMD_UNSCHEDULE_JOB, CMD_SHOWJOBS,
			CMD_SHOWREPORTS,
			CMD_IMPORTLAYOUTS, CMD_EXPORTLAYOUTS,
			CMD_IMPORTRULES, CMD_EXPORTRULES,
			CMD_SCHEDULE_TIMELIMIT_JOB, CMD_SETUSERPREFERENCES,
			CMD_RESETUSERPREFERENCES, CMD_VALIDATEOBJECTGENERATIONS,
			CMD_CHECKMASTERDATAVALUES, CMD_COMPILEDBOBJECTS,
			CMD_SENDMESSAGE, CMD_KILLSESSION, CMD_CHANGEPASSWORD,
			CMD_IMPORTTIMELIMITRULES, CMD_EXPORTTIMELIMITRULES, CMD_EXECUTE_TIMELIMITRULE_NOW
	);

	private static NuclosConsole singleton;

	public static synchronized NuclosConsole getInstance() {
		if (singleton == null) {
			singleton = newNuclosConsole();
		}
		return singleton;
	}

	private static NuclosConsole newNuclosConsole() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getConsoleClassName(),
					NuclosConsole.class.getName());

			return (NuclosConsole) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("Console could not be created.", ex);
		}
	}

	protected NuclosConsole() {
	}

	private static void login(String sUser, String sPassword, String sServer) throws LoginException {
		sUserName = sUser;
		final Map<String, ServerConfiguration> mpConfigurations = ServerConfiguration.getServerConfigurations();
		if (mpConfigurations.containsKey(sServer)) {	
			NuclosRemoteServerSession.login(sUser, sPassword);
		}
		else {
			System.out.println("Configuration not found. Available configurations are: " + CollectionUtils.getSeparatedList(mpConfigurations.keySet(), ", "));
			System.exit(-1);
		}
	}

	private static void logout() throws LoginException {
		NuclosRemoteServerSession.logout();
	}

	/**
	 * Invalidates the attribute cache
	 */
	private static void invalidateAttributeCache() {
		System.out.println("Invalidating attribute cache...");
		AttributeDelegate.getInstance().invalidateCache();
		System.out.println("done");
	}

	/**
	 * Invalidates the rule cache
	 */
	private static void invalidateRuleCache() {
		System.out.println("Invalidating rule cache...");
		RuleDelegate.getInstance().invalidateCache();
		System.out.println("done");
	}

	/**
	 * Refreshes the dynamic generic object views
	 */
	private static void refreshViews() {
		System.out.println("Refreshing generic object views...");
		LayoutDelegate.getInstance().refreshAll();
		System.out.println("done");
	}

	/**
	 * schedule a report
	 * @param sReportName
	 * @throws CommonFatalException
	 * @throws CommonPermissionException
	 * @throws CommonRemoteException
	 */
	private static void scheduleReportJob(String sReportName, int iHour, int iMinute) throws RemoteException, CreateException, CommonRemoteException, CommonPermissionException, CommonFatalException {
		// check if reportName is valid
		if (reportExists(sReportName)) {
			final SchedulerControlFacadeRemote schedulercontrol = ServiceLocator.getInstance().getFacade(SchedulerControlFacadeRemote.class);
			final Date dateScheduledFor = schedulercontrol.scheduleReportJob(sReportName, iHour, iMinute);
			if (dateScheduledFor != null) {
				System.out.println("Scheduled report " + sReportName + " for " + dateScheduledFor.toString());
			}
			else {
				System.out.println("Schedule report " + sReportName + " failed");
			}
		}
		else {
			System.out.println("Unable to schedule report \"" + sReportName + "\".\n" +
					"There is no report with this name defined in the application or you have no right to access it.");
		}
	}

	/**
	 * delete a scheduled job
	 * @param sJobName
	 */
	private static void unscheduleJob(String sJobName) throws CommonBusinessException, RemoteException, CreateException {
		final SchedulerControlFacadeRemote schedulercontrol = ServiceLocator.getInstance().getFacade(SchedulerControlFacadeRemote.class);
		if (schedulercontrol.unscheduleJob(sJobName)) {
			System.out.println("Successfully unscheduled job: " + sJobName);
		}
		else {
			System.out.println("Failed to unschedule job: " + sJobName);
		}
	}

	/**
	 * schedules TimeLimitJob
	 */
	private static void scheduleTimelimitJob(int iHour, int iMinute) throws RemoteException, CreateException {
		final SchedulerControlFacadeRemote schedulercontrol = ServiceLocator.getInstance().getFacade(SchedulerControlFacadeRemote.class);
		final Date dateScheduledFor = schedulercontrol.scheduleTimelimitJob(iHour, iMinute);
		if (dateScheduledFor != null) {
			System.out.println("Scheduled TimelimitJob for " + dateScheduledFor.toString());
		}
		else {
			System.out.println("Scheduling TimelimitJob failed");
		}
	}

	private static void executeTimelimitJobNow(String sRuleName) throws RemoteException, CreateException, CommonBusinessException {
		final TimelimitRuleFacadeRemote timelimitrulefacade = ServiceLocator.getInstance().getFacade(TimelimitRuleFacadeRemote.class);
		try {
			timelimitrulefacade.executeTimelimitRule(sRuleName);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex.getMessage());
		}
	}

	/**
	 * get a list of all scheduled jobs
	 */
	private static void showJobs() throws RemoteException, CreateException {
		final SchedulerControlFacadeRemote schedulercontrol = ServiceLocator.getInstance().getFacade(SchedulerControlFacadeRemote.class);
		final String[] asJobs = schedulercontrol.getJobNames();
		if (asJobs.length == 0) {
			System.out.println("No jobs scheduled.");
		}
		else {
			for (String sJob : asJobs) {
				System.out.println("\"" + sJob + "\"");
			}
		}
	}

	/**
	 * List of all reports defined in the application
	 * @throws CommonPermissionException
	 */
	private static void showReports() throws CommonPermissionException {
		for (final ReportVO report : getReports()) {
			if (report.getType() == ReportType.REPORT) {
				System.out.println(report.getName());
			}
		}
	}

	/**
	 * @param sReportName
	 * @return Does a report with the given name exist?
	 * @throws CommonPermissionException
	 */
	private static boolean reportExists(String sReportName) throws CommonPermissionException {
		boolean result = false;
		for (ReportVO report : getReports()) {
			if (report.getName().compareTo(sReportName) == 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * @return all reports from the server
	 * @throws CommonPermissionException
	 */
	private static Collection<ReportVO> getReports() throws CommonPermissionException {
		return ReportDelegate.getInstance().getReports();
	}

	private static void importLayouts(String sInputDir) throws CommonBusinessException {
		final File fileInputDir = testForNonEmptyDirectory(sInputDir);

		try {
			LayoutDelegate.getInstance().importLayouts(readLayouts(fileInputDir));
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * @param fileInputDir
	 * @return Collection<LayoutVO>
	 * @throws IOException
	 */
	private static Collection<LayoutVO> readLayouts(File fileInputDir) throws IOException {
		final Collection<LayoutVO> result = new ArrayList<LayoutVO>();

		final FileFilter filter = new SuffixFileFilter(".layoutml");
		for (File file : fileInputDir.listFiles(filter)) {
			final String sLayoutName = getNameWithoutSuffix(file);
			final String sLayoutML = readFromTextFile(file);
			result.add(new LayoutVO(sLayoutName, sLayoutName, sLayoutML));
		}
		return result;
	}

	private static String getNameWithoutSuffix(File file) {
		final int iDotPosition = file.getName().lastIndexOf('.');
		assert iDotPosition >= 0;
		return file.getName().substring(0, iDotPosition);
	}

	/**
	 * Export all Layouts
	 * @throws CommonBusinessException
	 */
	public static void exportLayouts(String sOutputDir) throws CommonBusinessException {
		exportLayouts(sOutputDir, NuclosEntity.LAYOUT);
	}

	private static void exportLayouts(String sOutputDir, final NuclosEntity entity) throws CommonBusinessException {
		final File fileOutputDir = testForEmptyDirectory(sOutputDir);

		System.out.println("Exporting Layouts to " + sOutputDir + "...");

		try {
			for (MasterDataVO mdvoLayout : MasterDataDelegate.getInstance().getMasterData(entity.getEntityName())) {
				final String sLayoutName = mdvoLayout.getField("name", String.class);
				final String sLayoutML = mdvoLayout.getField("layoutML", String.class);
				// no need to write layouts containing no data
				if (sLayoutML != null) {
					final String sFileName = sLayoutName + ".layoutml";
					final File fileLayout = new File(fileOutputDir, sFileName);
					IOUtils.writeToTextFile(fileLayout, sLayoutML, "ISO-8859-1");
				}
			}
		}
		catch (IOException ex) {
			throw new CommonFatalException(ex);
		}

		System.out.println("Layouts successfully exported.");
	}

	private static void exportRules(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForEmptyDirectory(sOutputDir);

		try {
			for (RuleVO rulevo : RuleDelegate.getInstance().getAllRules()) {
				final String sFileName = rulevo.getName() + ".txt";
				IOUtils.writeToTextFile(new File(fileOutputDir, sFileName), rulevo.getRuleSource(), "ISO-8859-1");
			}
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}

		/** @todo	*/
	}

	private static void exportTimelimitRules(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForEmptyDirectory(sOutputDir);

		try {
			for (RuleVO rulevo : TimelimitRuleDelegate.getInstance().getAllTimelimitRules()) {
				final String sFileName = rulevo.getName() + ".txt";
				IOUtils.writeToTextFile(new File(fileOutputDir, sFileName), rulevo.getRuleSource(), "ISO-8859-1");
			}
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}
	}

	// Only used for exports, so target directory will be created if necessary
	private static File testForEmptyDirectory(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForDirectory(sOutputDir, true);

		for(File file : fileOutputDir.listFiles())
			if(!file.isHidden())
				throw new CommonBusinessException("The output directory must be empty.");

		return fileOutputDir;
	}

	// Only used for imports, so target directory must exist and will not be created
	private static File testForNonEmptyDirectory(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForDirectory(sOutputDir, false);

		if (fileOutputDir.listFiles().length == 0) {
			throw new CommonBusinessException("The input directory is empty.");
		}
		return fileOutputDir;
	}

	/**
	 * Check for existence of a directory and create it, if desired.
	 * @param sOutputDir path of directory
	 * @param bCreate create directory if true
	 * @return directory as file
	 * @throws CommonBusinessException
	 */
	private static File testForDirectory(String sOutputDir, boolean bCreate) throws CommonBusinessException {
		final File fileOutputDir = new File(sOutputDir);
		if (!fileOutputDir.exists() && bCreate) {
			fileOutputDir.mkdir();
		}

		if (!fileOutputDir.isDirectory()) {
			throw new CommonBusinessException("The specified path does not denote a directory.");
		}
		return fileOutputDir;
	}

	private static void importRules(String sInputDir) throws CommonBusinessException, CreateException {
		final File fileInputDir = testForNonEmptyDirectory(sInputDir);

		try {
			System.out.println("Importing rules from " + sInputDir + "...");
			RuleDelegate.getInstance().importRules(readRules(fileInputDir));
			System.out.println("Done.");
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private static void importTimelimitRules(String sInputDir) throws CommonBusinessException, CreateException {
		final File fileInputDir = testForNonEmptyDirectory(sInputDir);

		try {
			System.out.println("Importing TimelimitRules from " + sInputDir + "...");
			TimelimitRuleDelegate.getInstance().importTimelimitRules(readTimelimitRules(fileInputDir));
			System.out.println("Done.");
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private static Collection<RuleVO> readTimelimitRules(File fileInputDir) throws IOException {
		final Collection<RuleVO> result = new ArrayList<RuleVO>();

		for (File file : fileInputDir.listFiles(new SuffixFileFilter(".txt"))) {
			final String sRuleName = getNameWithoutSuffix(file);
			final String sRuleSource = readFromTextFile(file);
			final RuleVO rulevo = new RuleVO(sRuleName, sRuleName, sRuleSource, null, Boolean.TRUE);

			result.add(rulevo);
		}
		return result;
	}

	private static Collection<RuleWithUsagesVO> readRules(File fileInputDir) throws IOException {
		final Collection<RuleWithUsagesVO> result = new ArrayList<RuleWithUsagesVO>();

		for (File file : fileInputDir.listFiles(new SuffixFileFilter(".txt"))) {
			final String sRuleName = getNameWithoutSuffix(file);
			final String sRuleSource = readFromTextFile(file);
			final RuleVO rulevo = new RuleVO(sRuleName, sRuleName, sRuleSource, null, true);
			result.add(new RuleWithUsagesVO(rulevo, new ArrayList<RuleEventUsageVO>()));
		}
		return result;
	}

	private static void setUserPreferences(String sUserName, String sFileName) {
		final File filePreferencesXml = new File(sFileName);
		try {
			final byte[] bytes = IOUtils.readFromBinaryFile(filePreferencesXml);
			internalSetUserPreferences(sUserName, new PreferencesVO(bytes));
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private static void resetUserPreferences(String sUserName) {
		internalSetUserPreferences(sUserName, null);
	}

	private static void internalSetUserPreferences(String sUserName, PreferencesVO prefsvo) {
		try {
			ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).setPreferencesForUser(sUserName, prefsvo);
		}
		catch (CommonFinderException ex) {
			throw new NuclosFatalException(ex);
		}
		catch (RuntimeException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	/**
	 * reads the contents of a text file, using the default encoding.
	 *
	 * @param file a File that must have a size < 2GB.
	 * @return a String containing the contents of the file.
	 * @throws java.io.IOException
	 * @todo move to IOUtils
	 * @todo Is it really true that it's not possible with BufferedReader.readLine()?
	 */
	public static String readFromTextFile(File file) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final Reader br = new BufferedReader(new FileReader(file));

		try {
			appendText(br, sb);
		}
		finally {
			br.close();
		}
		return sb.toString();
	}

	/**
	 * @param reader
	 * @param sb
	 * @throws IOException
	 * @see #readFromTextFile(File)
	 */
	private static void appendText(Reader reader, StringBuilder sb) throws IOException {
		int i;
		boolean bLastCharWasCR = false;
		while ((i = reader.read()) != -1) {
			final char c = (char) i;
			if (bLastCharWasCR) {
				bLastCharWasCR = false;
				if (c == '\n') {
					sb.append(c);
					continue;
				}
				else {
					sb.append('\n');
				}
			}
			bLastCharWasCR = (c == '\r');
			if (!bLastCharWasCR) {
				sb.append(c);
			}
		}
	}

	private static String getUsage() {
		final StringBuilder sb = new StringBuilder();

		sb.append("Usage: NuclosConsole <username> <password> <serverconfig> <command>\n");
		sb.append("The configuration for the application server must be one contained in the file nuclos-client.properties, which must be in the CLASSPATH.\n");
		sb.append("<command> is one of the following:\n");
		sb.append("==========================================================================\n");
		sb.append("\t-invalidateAttributeCache\n");
		sb.append("\t\t\tInvalidates the attribute cache.\n");
		sb.append("\t-refreshViews\n");
		sb.append("\t\t\tRefreshes the dynamic leased object views.\n");
		sb.append("\t-scheduleReportJob <ReportName> [-hour <hh>] [-minute <mm]>\n");
		sb.append("\t\t\tSchedules <ReportName> daily at specified hour and specified minute.\n");
		sb.append("\t-scheduleTimelimitJob -hour <hh> [-minute <mm>]\n");
		sb.append("\t\t\tSchedules the Timelimit job daily at specified hour and specified minute.\n");
		sb.append("\t-unscheduleJob <Job Name>\n");
		sb.append("\t\t\tRemoves scheduling for <Job Name>. Use -showJobs to get a list of scheduled jobs.\n");
		sb.append("\t-showJobs\n");
		sb.append("\t\t\tLists names of all scheduled jobs. Use these names for -unscheduleJob.\n");
		sb.append("\t-showReports\n");
		sb.append("\t\t\tLists all reports defined in the application.\n");
		sb.append("\t-importLayouts <directory>\n");
		sb.append("\t\t\tImports all layouts contained in the given directory (on the client side).\n");
		sb.append("\t-exportLayouts <directory>\n");
		sb.append("\t\t\tExports all layouts to the given output directory (on the client side).\n");
		sb.append("\t-importRules <input directory>\n");
		sb.append("\t\t\tImports all business rules (without usages) contained in the given directory (on the client side).\n");
		sb.append("\t-exportRules <output directory>\n");
		sb.append("\t\t\tExports all business rules (without usages) to an empty output directory (on the client side).\n");
		sb.append("\t-executeTimelimitRule <rule name>\n");
		sb.append("\t\t\tExecutes a timelimit rule immediately.\n");
		sb.append("\t-importTimelimitRules <input directory>\n");
		sb.append("\t\t\tImports all timelimit rules contained in the given directory (on the client side).\n");
		sb.append("\t-exportTimelimitRules <output directory>\n");
		sb.append("\t\t\tExports all timelimit rules to an empty output directory (on the client side).\n");
		sb.append("\t-setUserPreferences <user name> <file name>\n");
		sb.append("\t\t\tStores the contents of the file with the given name as the preferences for the user with the given name.\n");
		sb.append("\t-resetUserPreferences <user name>\n");
		sb.append("\t\t\tResets the preferences for the user with the given name.\n");
		sb.append("\t-compileAllRules\n");
		sb.append("\t\t\tCompiles all rules.\n");
		sb.append("\t-compilealltimelimitrules\n");
		sb.append("\t\t\tCompiles all Timelimit rules.\n");
		sb.append("\t-checkMasterDataValues\n");
		sb.append("\t\t\tChecks all masterdata records against masterdata meta information (field length, not null etc.). Will take a long time!\n");
		sb.append("\t-developer\n");
		sb.append("\t\t\tShows commands only relevant for developing.\n");
		sb.append("\t\t\tPrefix, suffix and extension may contain wildcards, but prefix must not be or contain *.\n");
		sb.append("\t\t\tSpecification of logfile path and/or name without extension, default is ImportLog.\n");
		sb.append("\t\t\tIndication of path for logging is recommended. Path or Filenames with blanks have to be quoted.\n");
		sb.append("\t\t\tIf no remark text is given, default is \"Migration Altakten\". Quote text if more than one word.\n");
		sb.append("\t\t\tDefault for deletion of source files ist false.\n");
		sb.append("\t-removeimporteddocuments [-remark <text>] [-logfile <base file name or path>]\n");
		sb.append("\t\t\tRemoves prior added documents from assets, optional filtert by the given remark during import.\n");
		sb.append("\t\t\tIf no remark text is given, default is \"Migration Altakten\". Quote text if more than one word.\n");
		sb.append("\t\t\tSpecification of logfile path and/or name without extension, default is Remove.\n");
		sb.append("\t\t\tIndication of path for logging is recommended. Path or Filenames with blanks have to be quoted.\n");
		sb.append("\t-migrationService [-logfile <filename>] [-undo]\n");
		sb.append("\t\t\tMigrate entries from assetinformation to assetservice.\n");
		sb.append("\t\t\tUse -undo to revert all changes.\n");
		sb.append("\t-compileDbObjects\n");
		sb.append("\t\t\tcompile all invalid db functins and views.\n");
		sb.append("\t-sendMessage -message <message text> [-user <username>] [-priority <high, normal, low>] [-author <author>]\n");
		sb.append("\t\t\tsend a message to the specified user (or all users if no one is specified).\n");
		sb.append("\t-killSession [-user <username>]\n");
		sb.append("\t\t\tkill the client of the specified user (or all users if no one is specified).\n");
		sb.append("\t-invalidatAllCaches\n");
		sb.append("\t\t\tinvalide all server side caches.\n");

		return sb.toString();
	}

	/**
	 * directly called by GUI (already logged in) - called by console's main method
	 * @param asArgs
	 * @param bCalledByConsole
	 */
	public void parseAndInvoke(String[] asArgs, boolean bCalledByConsole) throws Exception {
		final long lStartTime = new Date().getTime();
		if (asArgs == null || asArgs.length == 0) {
			System.out.println(getUsage());
			return;
		}
		final String sCommand = asArgs[0];
		final String sCommandLowerCase = sCommand.toLowerCase();

		parseAndInvoke(sCommand, sCommandLowerCase, asArgs, bCalledByConsole);

		System.out.println("NuclosConsole finished in " + (DateUtils.now().getTime() - lStartTime) + " ms");
	}

	protected void parseAndInvoke(String sCommand, String sCommandLowerCase, String[] asArgs, boolean bCalledByConsole) throws Exception {
		if (sCommandLowerCase.equals(CMD_INVALIDATE_ATTRIBUTECACHE)) {
			invalidateAttributeCache();
		}
		else if (sCommandLowerCase.equals(CMD_INVALIDATE_RULECACHE)) {
			invalidateRuleCache();
		}
		else if (sCommandLowerCase.equals(CMD_REFRESHVIEWS)) {
			refreshViews();
		}
		else if (sCommandLowerCase.equals(CMD_SCHEDULE_REPORT_JOB)) {
			if (asArgs.length < 2) {
				System.out.println("Missing argument <ReportName> for command " + sCommand + "\n");
				System.out.println(getUsage());
				if (bCalledByConsole) {
					System.exit(-1);
				}
				else {
					return;
				}
			}
			int iHour = -1;
			int iMinute = -1;
			for (int i = 2; i < asArgs.length; i++) {
				if (asArgs[i].equalsIgnoreCase("-hour") && i + 1 < asArgs.length) {
					iHour = Integer.parseInt(asArgs[i + 1]);
				}
				else if (asArgs[i].equalsIgnoreCase("-minute") && i + 1 < asArgs.length) {
					iMinute = Integer.parseInt(asArgs[i + 1]);
				}
			}

			if (iHour < 0) {
				iHour = 16;
				System.out.println("No hour specified. Using default: " + iHour);
			}
			if (iMinute < 0) {
				iMinute = 0;
				System.out.println("No minute specified. Using default: " + iMinute);
			}

			scheduleReportJob(asArgs[1], iHour, iMinute);
		}
		else if (sCommandLowerCase.equals(CMD_UNSCHEDULE_JOB)) {
			if (asArgs.length < 2) {
				System.out.println("Missing argument <ReportName> for command " + sCommand + "\n");
				System.out.println(getUsage());
				if (bCalledByConsole) {
					System.exit(-1);
				}
				else {
					return;
				}
			}
			unscheduleJob(asArgs[1]);
		}
		else if (sCommandLowerCase.equals(CMD_SHOWJOBS)) {
			showJobs();
		}
		else if (sCommandLowerCase.equals(CMD_SHOWREPORTS)) {
			showReports();
		}
		else if (sCommandLowerCase.equals(CMD_IMPORTLAYOUTS)) {
			checkForImportExportParameters(asArgs, false);

			final String sInputDir = asArgs[1];
			importLayouts(sInputDir);
		}
		else if (sCommandLowerCase.equals(CMD_EXPORTLAYOUTS)) {
			checkForImportExportParameters(asArgs, true);

			final String sOutputDir = asArgs[1];
			exportLayouts(sOutputDir);
		}
		else if (sCommandLowerCase.equals(CMD_IMPORTRULES)) {
			checkForImportExportParameters(asArgs, false);

			final String sInputDir = asArgs[1];
			importRules(sInputDir);
		}
		else if (sCommandLowerCase.equals(CMD_EXPORTRULES)) {
			checkForImportExportParameters(asArgs, true);

			final String sOutputDir = asArgs[1];
			exportRules(sOutputDir);
		}
		else if (sCommandLowerCase.equals(CMD_IMPORTTIMELIMITRULES)) {
			checkForImportExportParameters(asArgs, false);

			final String sInputDir = asArgs[1];
			importTimelimitRules(sInputDir);
		}
		else if (sCommandLowerCase.equals(CMD_EXPORTTIMELIMITRULES)) {
			checkForImportExportParameters(asArgs, true);

			final String sOutputDir = asArgs[1];
			exportTimelimitRules(sOutputDir);
		}
		else if (sCommandLowerCase.equals(CMD_SCHEDULE_TIMELIMIT_JOB)) {
			int iHour = -1;
			int iMinute = -1;
			for (int i = 1; i < asArgs.length; i++) {
				if (asArgs[i].equalsIgnoreCase("-hour") && i + 1 < asArgs.length) {
					iHour = Integer.parseInt(asArgs[i + 1]);
				}
				else if (asArgs[i].equalsIgnoreCase("-minute") && i + 1 < asArgs.length) {
					iMinute = Integer.parseInt(asArgs[i + 1]);
				}
			}
			if (iHour < 0) {
				throw new CommonBusinessException("-hour is a necessary parameter for command \"" + sCommandLowerCase + "\"");
			}
			if (iMinute < 0) {
				iMinute = 0;
				System.out.println("no minute specified. Using default: " + iMinute);
			}
			if (sCommandLowerCase.equals(CMD_SCHEDULE_TIMELIMIT_JOB)) {
				scheduleTimelimitJob(iHour, iMinute);
			}
		}
		else if (sCommandLowerCase.equals(CMD_EXECUTE_TIMELIMITRULE_NOW)) {
			if (asArgs.length < 2) {
				System.out.println("Missing argument <rule name> for command " + sCommand + "\n");
				System.out.println(getUsage());
				if (bCalledByConsole) {
					System.exit(-1);
				}
				else {
					return;
				}
			}
			executeTimelimitJobNow(asArgs[1]);
		}
		else if (sCommandLowerCase.equals(CMD_SETUSERPREFERENCES)) {
			if (asArgs.length < 3) {
				System.out.println(getUsage());
			}
			final String sUserName = asArgs[1];
			final String sFileName = asArgs[2];
			setUserPreferences(sUserName, sFileName);
		}
		else if (sCommandLowerCase.equals(CMD_RESETUSERPREFERENCES)) {
			if (asArgs.length < 2) {
				System.out.println(getUsage());
			}
			final String sUserName = asArgs[1];
			resetUserPreferences(sUserName);
		}
		else if (sCommandLowerCase.equals("-dosomethinguseful")) {
			doSomethingUseful(asArgs);
		}
		else if (sCommandLowerCase.equals(CMD_SHOWDEVELOPERCOMMANDS)) {
			printDeveloperCommands();
		}
		else if (sCommandLowerCase.equals(CMD_INVALIDATE_METADATACACHE)) {
			System.out.println("Invalidating metadata cache...");
			final MasterDataFacadeRemote mdfacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeRemote.class);
			mdfacade.revalidateMasterDataMetaCache();
			System.out.println("done");
		}
		else if (sCommandLowerCase.equals(CMD_INVALIDATE_DATASOURCECACHE)) {
			System.out.println("Invalidating datasource cache...");
			final DatasourceFacadeRemote datasourcefacade = ServiceLocator.getInstance().getFacade(DatasourceFacadeRemote.class);
			datasourcefacade.invalidateCache();
			System.out.println("done");
		}
		else if (sCommandLowerCase.equals(CMD_VALIDATEOBJECTGENERATIONS)) {
			validateObjectGenerations();
		}
		else if (sCommandLowerCase.equals(CMD_CHECKMASTERDATAVALUES)) {
			String filename = "masterdata.csv";
			if (asArgs.length < 2) {
				System.out.println("No file name specified. Default is: " + filename + "\n");
			}
			else {
				filename = asArgs[1];
			}
			checkMasterDataValues(filename);
		}
		else if (sCommandLowerCase.equals(CMD_COMPILEDBOBJECTS)) {
			compileDBObjects();
		}
		else if (sCommandLowerCase.equals(CMD_SENDMESSAGE)) {
			String sMessage = null;
			String sUser = null;
			String sAuthor = "Administrator";
			Priority priority = Priority.HIGH;

			if (asArgs.length >= 2) {
				for (int i = 1; i < asArgs.length; i++) {
					if (asArgs[i].equalsIgnoreCase("-user") && i + 1 < asArgs.length) {
						sUser = asArgs[i + 1];
					}
					else if (asArgs[i].equalsIgnoreCase("-priority") && i + 1 < asArgs.length) {
						String sPrio = asArgs[i + 1];
						if (sPrio.equalsIgnoreCase("hoch") || sPrio.equalsIgnoreCase("high")) {
							priority = Priority.HIGH;
						}
						else if (sPrio.equalsIgnoreCase("normal")) {
							priority = Priority.NORMAL;
						}
						else if (sPrio.equalsIgnoreCase("niedrig") || sPrio.equalsIgnoreCase("low")) {
							priority = Priority.LOW;
						}
						else {
							throw new CommonBusinessException("Unknown priority \"" + sPrio + "\". Use one of: " + Priority.HIGH + ", " + Priority.NORMAL + ", " + Priority.LOW);
						}
					}
					else if (asArgs[i].equalsIgnoreCase("-message") && i + 1 < asArgs.length) {
						sMessage = asArgs[i + 1];
					}
					else if (asArgs[i].equalsIgnoreCase("-author") && i + 1 < asArgs.length) {
						sAuthor = asArgs[i + 1];
					}
				}
				if (sMessage != null) {
					sendMessage(sMessage, sUser, priority, sAuthor);
				}
				else {
					throw new CommonBusinessException("Missing argument -message\n");
				}

			}
			else {
				throw new CommonBusinessException("Missing arguments for sendMessage\n" + getUsage());
			}
		}
		else if (sCommandLowerCase.equals(CMD_KILLSESSION)) {
			String sUser = null;
			if (asArgs.length >= 2) {
				for (int i = 1; i < asArgs.length; i++) {
					if (asArgs[i].equalsIgnoreCase("-user") && i + 1 < asArgs.length) {
						sUser = asArgs[i + 1];
					}
				}
			}
			killSession(sUser);
		} else if (sCommandLowerCase.equals(CMD_INVALIDATEALLCACHES)) {
			invalidatAllCaches();
		}
		else if (sCommandLowerCase.equals(CMD_CHANGEPASSWORD)) {
			String sUser = null;
			String sPassword = null;
			if (asArgs.length >= 2) {
				for (int i = 1; i < asArgs.length; i++) {
					if (asArgs[i].equalsIgnoreCase("-user") && i + 1 < asArgs.length) {
						sUser = asArgs[i + 1];
					} else if (asArgs[i].equalsIgnoreCase("-password") && i + 1 < asArgs.length) {
						sPassword = asArgs[i + 1];
					}
				}
			} else {
				throw new CommonBusinessException("Missing arguments for changePassword\n" + getUsage());
			}
			changePassword(sUser, sPassword);
		}
		else if (sCommandLowerCase.equals(CMD_MIGRATESEARCHFILTER)) {
			MigrateSearchFilterPreferences.migrate(sUserName);
		}
		else {
			throw new CommonBusinessException("Unknown command: " + sCommand + "\n" + getUsage());
		}
	}


	private static void invalidatAllCaches() throws CommonRemoteException, RemoteException, CommonFatalException, CreateException {
		System.out.println(getConsoleFacade().invalidateAllCaches());
	}

	private static void changePassword(String sUser, String sPassword) throws NuclosBusinessException {
		try {
			ServiceLocator.getInstance().getFacade(SecurityFacadeRemote.class).changeUserPassword(sUser, sPassword);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	private static void killSession(String user) throws RemoteException {
		getConsoleFacade().killSession(user);
	}

	private static void sendMessage(String sMessage, String sUser, Priority priority, String sAuthor) throws RemoteException {
		getConsoleFacade().sendClientNotification(sMessage, sUser, priority, sAuthor);
	}

	/**
	 * Check the given parameters for import or export use cases. Give explicit error description if necessary.
	 * @param args all arguments to the use case
	 * @param bDirectoryMayBeEmpty
	 * @throws CommonBusinessException
	 */
	private static void checkForImportExportParameters(String[] args, boolean bDirectoryMayBeEmpty) throws CommonBusinessException {
		if (args.length < 2) {
			throw new CommonBusinessException("Name of " + (bDirectoryMayBeEmpty ? "empty " : "") + "directory must be given.");
		}
		if (args.length > 2) {
			throw new CommonBusinessException("Too many parameters - maybe enclosing quotation marks missing?");
		}
	}

	private static void compileDBObjects() throws RemoteException {
		getConsoleFacade().compileInvalidDbObjects();
	}

	private static ConsoleFacadeRemote getConsoleFacade() {
		try {
			return ServiceLocator.getInstance().getFacade(ConsoleFacadeRemote.class);
		}
		catch (RuntimeException ex) {
			throw new CommonRemoteException(ex);
		}
	}

	/**
	 * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	 * @param filename the name of the csv file to which the results are written.
	 */
	private static void checkMasterDataValues(String filename) {
		// iterate over masterdata meta cache; masterdatafacade.get(entity) to get all; validate each cvo
		MasterDataDelegate.getInstance().checkMasterDataValues(filename);
	}

	/**
	 * Check for attributes which are not used in any layout and test, if they have contents.
	 * @param sFileName
	 */
//	private static void checkAttributeUsage(String sFileName) {
//		final GenericObjectMetaDataVO lometacvo = GenericObjectDelegate.getInstance().getMetaDataCVO();
//
//		final Map<String, Integer> mpAttributeUsages = GenericObjectDelegate.getInstance().getAllAttributeUsageCount();
//
//		final Set<String> stAttributesNotInLayout = new HashSet<String>(mpAttributeUsages.keySet());
//		for (int iLayoutId : lometacvo.getAllLayoutIds()) {
//			stAttributesNotInLayout.removeAll(lometacvo.getAttributeNamesByLayoutId(iLayoutId));
//		}
//
//		final PrintStream ps;
//		try {
//			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(sFileName)), true);
//		}
//		catch (FileNotFoundException ex) {
//			throw new NuclosFatalException("Cannot create file " + sFileName + ".", ex);
//		}
//
//		AttributeCache.initialize();
//		final AttributeCache attrcache = AttributeCache.getInstance();
//		ps.println("Attribute name; Usages count; Occurs in no layout");
//		for (String sAttributename : mpAttributeUsages.keySet()) {
//			if (stAttributesNotInLayout.contains(sAttributename) || mpAttributeUsages.get(sAttributename).equals(0)) {
//				ps.print(sAttributename);
//				ps.print(';');
//				if (attrcache.getAttribute(sAttributename).getCalcFunction() == null) {
//					ps.print(mpAttributeUsages.get(sAttributename));
//				}
//				else {
//					ps.print("Calculated");
//				}
//				ps.print(';');
//				if (stAttributesNotInLayout.contains(sAttributename)) {
//					ps.print("X");
//				}
//				ps.println();
//			}
//		}
//		ps.close();
//		if (ps.checkError()) {
//			throw new NuclosFatalException("Failed to close PrintStream.");
//		}
//	}

	/**
	 * Check and eliminate attributes from object generation which are not valid in the context
	 * todo: check validity of subentities here also!
	 */
	private static void validateObjectGenerations() {
		System.out.println("validateObjectGenerations start ...");
		AttributeCache.initialize();
		final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();
		for (MasterDataVO mdvoGeneration : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.GENERATION.getEntityName())) {
			final Collection<String> collSourceAttributeNames = lometacache.getAttributeNamesByModuleId(mdvoGeneration.getField("sourceModuleId", Integer.class), Boolean.FALSE);
			final Collection<String> collTargetAttributeNames = lometacache.getAttributeNamesByModuleId(mdvoGeneration.getField("targetModuleId", Integer.class), Boolean.FALSE);
			final Collection<String> collAttributeNames = CollectionUtils.intersection(collSourceAttributeNames, collTargetAttributeNames);
			final Collection<EntityObjectVO> coll = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", mdvoGeneration.getId());
			for (EntityObjectVO mdvoAttribute : coll) {
				if (!collAttributeNames.contains(mdvoAttribute.getField("attribute", String.class))) {
					try {
						MasterDataDelegate.getInstance().remove(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), DalSupportForMD.wrapEntityObjectVO(mdvoAttribute));
					}
					catch (CommonBusinessException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		System.out.println("validateObjectGenerations finished");
	}

	private static void printDeveloperCommands() {
		System.out.println("Developer commands:\n");
		System.out.println("\t-invalidatemasterdatametacache");
		System.out.println("\t\t\tFills the masterdata cache of the server newly with values, so it is not necessary to restart the server (but the client still)");
	}

	/**
	 * This is a temporary hack that may be used for quick migrations etc.
	 * @param asArgs
	 * @todo create a server-side pendant doSomethingUseful(asArgs) in your favorite FacadeBean.
	 */
	private static void doSomethingUseful(String[] asArgs) {
		// today, we're generating stupid sql inserts out of metadata information:
	}

	/**
	 * called if real console is required (shell)
	 * @param asArgs
	 */
	public static void main(String[] asArgs) {
		try {
			if (asArgs.length < 4) {
				System.out.println("Missing command.\n\n");
				System.out.println(getUsage());
				System.exit(-1);
			}

			login(asArgs[0], asArgs[1], asArgs[2]);
			try {
				final String[] asParamsWithoutLoginInfo = new String[asArgs.length - 3];
				for (int i = 3; i < asArgs.length; i++) {
					asParamsWithoutLoginInfo[i - 3] = asArgs[i];
				}
				NuclosConsole.getInstance().parseAndInvoke(asParamsWithoutLoginInfo, true);
			}
			finally {
				logout();
			}
			System.exit(0);
		}
		catch (CommonBusinessException ex) {
			System.err.println(ex.getMessage());
			System.exit(-1);
		}
		catch (Exception ex) {
			ex.printStackTrace(System.err);
			System.exit(-1);
		}
	}

	/**
	 * A FileFilter accepting all files that end with the given suffix.
	 */
	private static class SuffixFileFilter implements FileFilter {
		private final String sSuffix;

		/**
		 * @param sSuffix
		 * @precondition sSuffix.startsWith(".")
		 */
		SuffixFileFilter(String sSuffix) {
			if (sSuffix == null) {
				throw new NullArgumentException("sSuffix");
			}
			if (!sSuffix.startsWith(".")) {
				throw new IllegalArgumentException("sSuffix");
			}
			this.sSuffix = sSuffix;
		}

		@Override
		public boolean accept(File file) {
			return file.getName().toLowerCase().endsWith(sSuffix);
		}
	}

}	// class NuclosConsole
