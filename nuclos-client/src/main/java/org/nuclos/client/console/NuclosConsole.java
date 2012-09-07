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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.attribute.AttributeDelegate;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.layout.LayoutDelegate;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.nuclet.generator.NucletGenerator;
import org.nuclos.client.report.ReportDelegate;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.rule.TimelimitRuleDelegate;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.updatejobs.MigrateSearchFilterPreferences;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.ConsoleConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.Priority;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.DateUtils;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.server.attribute.valueobject.LayoutVO;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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

	/**
	 * this list is used by NuclosConsoleGui to show the available commands
	 */
	public static final List<String> LSTCOMMANDS = Arrays.asList(
			CMD_INVALIDATE_ATTRIBUTECACHE, CMD_INVALIDATE_METADATACACHE, CMD_SHOWDEVELOPERCOMMANDS,
			CMD_REFRESHVIEWS, CMD_UNSCHEDULE_JOB, CMD_SHOWJOBS,
			CMD_SHOWREPORTS,
			CMD_IMPORTLAYOUTS, CMD_EXPORTLAYOUTS,
			CMD_IMPORTRULES, CMD_EXPORTRULES,
			CMD_SETUSERPREFERENCES,
			CMD_RESETUSERPREFERENCES, CMD_VALIDATEOBJECTGENERATIONS,
			CMD_CHECKMASTERDATAVALUES, CMD_COMPILEDBOBJECTS,
			CMD_SENDMESSAGE, CMD_KILLSESSION,
			CMD_IMPORTTIMELIMITRULES, CMD_EXPORTTIMELIMITRULES, CMD_EXECUTE_TIMELIMITRULE_NOW,
			CMD_NUCLET_GENERATION_FROM_XLSX, CMD_NUCLET_GENERATION_CREATE_EMPTY_XLSX_FILE,
			CMD_REBUILD_CONSTRAINTS
	);

	private static NuclosConsole INSTANCE;

	private static String sUserName;
	
	// 
	
	// Spring injection
	
	private AttributeCache attributeCache;
	
	private SchedulerControlFacadeRemote schedulerControlFacadeRemote;
	
	private TimelimitRuleFacadeRemote timelimitRuleFacadeRemote;
	
	private PreferencesFacadeRemote preferencesFacadeRemote;
	
	private MasterDataFacadeRemote masterDataFacadeRemote;
	
	private DatasourceFacadeRemote datasourceFacadeRemote;
	
	private ConsoleFacadeRemote consoleFacadeRemote;
	
	private NuclosRemoteServerSession nuclosRemoteServerSession;
	
	// end of Spring injection
	
	public static NuclosConsole getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	protected static NuclosConsole newNuclosConsole() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getConsoleClassName(),
					NuclosConsole.class.getName());

			INSTANCE = (NuclosConsole) Class.forName(sClassName).newInstance();
			return INSTANCE;
		}
		catch (Exception ex) {
			throw new CommonFatalException("Console could not be created.", ex);
		}
	}

	protected NuclosConsole() {
	}
	
	@Autowired
	@Qualifier("attributeCache")
	final void setAttributeCache(AttributeCache attributeCache) {
		this.attributeCache = attributeCache;
	}
	
	@Autowired
	final void setSchedulerControlFacadeRemote(SchedulerControlFacadeRemote schedulerControlFacadeRemote) {
		this.schedulerControlFacadeRemote = schedulerControlFacadeRemote;
	}
	
	@Autowired
	final void setTimelimitRuleFacadeRemote(TimelimitRuleFacadeRemote timelimitRuleFacadeRemote) {
		this.timelimitRuleFacadeRemote = timelimitRuleFacadeRemote;
	}
	
	@Autowired
	final void setPreferencesFacadeRemote(PreferencesFacadeRemote preferencesFacadeRemote) {
		this.preferencesFacadeRemote = preferencesFacadeRemote;
	}
	
	@Autowired
	final void setMasterDataFacadeRemote(MasterDataFacadeRemote masterDataFacadeRemote) {
		this.masterDataFacadeRemote = masterDataFacadeRemote;
	}
	
	@Autowired
	final void setDatasourceFacadeRemote(DatasourceFacadeRemote datasourceFacadeRemote) {
		this.datasourceFacadeRemote = datasourceFacadeRemote;
	}
	
	@Autowired
	final void setConsoleFacadeRemote(ConsoleFacadeRemote consoleFacadeRemote) {
		this.consoleFacadeRemote = consoleFacadeRemote;
	}
	
	@Autowired
	final void setNuclosRemoteServerSession(NuclosRemoteServerSession NuclosRemoteServerSession) {
		this.nuclosRemoteServerSession = nuclosRemoteServerSession;
	}

	private String login(String sUser, String sPassword) throws LoginException {
		 sUserName = nuclosRemoteServerSession.login(sUser, sPassword);
		 return sUserName;
	}

	private void logout() throws LoginException {
		nuclosRemoteServerSession.logout();
	}

	public String invalidateAllCaches() {
		return consoleFacadeRemote.invalidateAllCaches();
	}
	
	/**
	 * Invalidates the attribute cache
	 */
	private void invalidateAttributeCache() {
		System.out.println("Invalidating attribute cache...");
		AttributeDelegate.getInstance().invalidateCache();
		System.out.println("done");
	}

	/**
	 * Invalidates the rule cache
	 */
	private void invalidateRuleCache() {
		System.out.println("Invalidating rule cache...");
		RuleDelegate.getInstance().invalidateCache();
		System.out.println("done");
	}

	/**
	 * Refreshes the dynamic generic object views
	 */
	private void refreshViews() {
		System.out.println("Refreshing generic object views...");
		LayoutDelegate.getInstance().refreshAll();
		System.out.println("done");
	}

	/**
	 * delete a scheduled job
	 * @param sJobName
	 */
	private void unscheduleJob(String sJobName) throws CommonBusinessException, RemoteException {
		schedulerControlFacadeRemote.deleteJob(sJobName);
		System.out.println("Successfully deleted job: " + sJobName);
		System.out.println(schedulerControlFacadeRemote.getSchedulerSummary());
	}

	private void executeTimelimitJobNow(String sRuleName) throws RemoteException, CommonBusinessException {
		try {
			timelimitRuleFacadeRemote.executeTimelimitRule(sRuleName);
		}
		catch (RuntimeException ex) {
			throw new CommonBusinessException(ex.getMessage());
		}
	}

	/**
	 * get a list of all scheduled jobs
	 */
	private void showJobs() throws RemoteException {
		System.out.println(schedulerControlFacadeRemote.getSchedulerSummary());
	}

	/**
	 * List of all reports defined in the application
	 * @throws CommonPermissionException
	 */
	private void showReports() throws CommonPermissionException {
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
	private boolean reportExists(String sReportName) throws CommonPermissionException {
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
	private Collection<ReportVO> getReports() throws CommonPermissionException {
		return ReportDelegate.getInstance().getReports();
	}

	private void importLayouts(String sInputDir) throws CommonBusinessException {
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
	private Collection<LayoutVO> readLayouts(File fileInputDir) throws IOException {
		final Collection<LayoutVO> result = new ArrayList<LayoutVO>();

		final FileFilter filter = new SuffixFileFilter(".layoutml");
		for (File file : fileInputDir.listFiles(filter)) {
			final String sLayoutName = getNameWithoutSuffix(file);
			final String sLayoutML = readFromTextFile(file);
			result.add(new LayoutVO(sLayoutName, sLayoutName, sLayoutML));
		}
		return result;
	}

	private String getNameWithoutSuffix(File file) {
		final int iDotPosition = file.getName().lastIndexOf('.');
		assert iDotPosition >= 0;
		return file.getName().substring(0, iDotPosition);
	}

	/**
	 * Export all Layouts
	 * @throws CommonBusinessException
	 */
	public void exportLayouts(String sOutputDir) throws CommonBusinessException {
		exportLayouts(sOutputDir, NuclosEntity.LAYOUT);
	}

	private void exportLayouts(String sOutputDir, final NuclosEntity entity) throws CommonBusinessException {
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

	private void exportRules(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForEmptyDirectory(sOutputDir);

		try {
			for (RuleVO rulevo : RuleCache.getInstance().getAllRules()) {
				final String sFileName = rulevo.getRule() + ".txt";
				IOUtils.writeToTextFile(new File(fileOutputDir, sFileName), rulevo.getSource(), "ISO-8859-1");
			}
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}

		/** @todo	*/
	}

	private void exportTimelimitRules(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForEmptyDirectory(sOutputDir);

		try {
			for (RuleVO rulevo : TimelimitRuleDelegate.getInstance().getAllTimelimitRules()) {
				final String sFileName = rulevo.getRule() + ".txt";
				IOUtils.writeToTextFile(new File(fileOutputDir, sFileName), rulevo.getSource(), "ISO-8859-1");
			}
		}
		catch (IOException e) {
			throw new CommonFatalException(e);
		}
	}

	// Only used for exports, so target directory will be created if necessary
	private File testForEmptyDirectory(String sOutputDir) throws CommonBusinessException {
		final File fileOutputDir = testForDirectory(sOutputDir, true);

		for(File file : fileOutputDir.listFiles())
			if(!file.isHidden())
				throw new CommonBusinessException("The output directory must be empty.");

		return fileOutputDir;
	}

	// Only used for imports, so target directory must exist and will not be created
	private File testForNonEmptyDirectory(String sOutputDir) throws CommonBusinessException {
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
	private File testForDirectory(String sOutputDir, boolean bCreate) throws CommonBusinessException {
		final File fileOutputDir = new File(sOutputDir);
		if (!fileOutputDir.exists() && bCreate) {
			fileOutputDir.mkdir();
		}

		if (!fileOutputDir.isDirectory()) {
			throw new CommonBusinessException("The specified path does not denote a directory.");
		}
		return fileOutputDir;
	}

	private void importRules(String sInputDir) throws CommonBusinessException {
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

	private void importTimelimitRules(String sInputDir) throws CommonBusinessException {
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

	private Collection<RuleVO> readTimelimitRules(File fileInputDir) throws IOException {
		final Collection<RuleVO> result = new ArrayList<RuleVO>();

		for (File file : fileInputDir.listFiles(new SuffixFileFilter(".txt"))) {
			final String sRuleName = getNameWithoutSuffix(file);
			final String sRuleSource = readFromTextFile(file);
			final RuleVO rulevo = new RuleVO(sRuleName, sRuleName, sRuleSource, null, Boolean.TRUE);

			result.add(rulevo);
		}
		return result;
	}

	private Collection<RuleWithUsagesVO> readRules(File fileInputDir) throws IOException {
		final Collection<RuleWithUsagesVO> result = new ArrayList<RuleWithUsagesVO>();

		for (File file : fileInputDir.listFiles(new SuffixFileFilter(".txt"))) {
			final String sRuleName = getNameWithoutSuffix(file);
			final String sRuleSource = readFromTextFile(file);
			final RuleVO rulevo = new RuleVO(sRuleName, sRuleName, sRuleSource, null, true);
			result.add(new RuleWithUsagesVO(rulevo, new ArrayList<RuleEventUsageVO>()));
		}
		return result;
	}

	private void setUserPreferences(String sUserName, String sFileName) {
		final File filePreferencesXml = new File(sFileName);
		try {
			final byte[] bytes = IOUtils.readFromBinaryFile(filePreferencesXml);
			internalSetUserPreferences(sUserName, new PreferencesVO(bytes));
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	private void resetUserPreferences(String sUserName) {
		internalSetUserPreferences(sUserName, null);
	}

	private void internalSetUserPreferences(String sUserName, PreferencesVO prefsvo) {
		try {
			preferencesFacadeRemote.setPreferencesForUser(sUserName, prefsvo);
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
	public String readFromTextFile(File file) throws IOException {
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
	private void appendText(Reader reader, StringBuilder sb) throws IOException {
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

	private String getUsage() {
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
		sb.append("\t-rebuildConstraints\n");
		sb.append("\t\t\rebuilds all unique and foreign constraints.\n");

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
			masterDataFacadeRemote.revalidateMasterDataMetaCache();
			System.out.println("done");
		}
		else if (sCommandLowerCase.equals(CMD_INVALIDATE_DATASOURCECACHE)) {
			System.out.println("Invalidating datasource cache...");
			datasourceFacadeRemote.invalidateCache();
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
		else if (sCommandLowerCase.equals(CMD_REBUILD_CONSTRAINTS.toLowerCase())) {
			rebuildConstraints();
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
		else if (sCommandLowerCase.equals(CMD_MIGRATESEARCHFILTER)) {
			MigrateSearchFilterPreferences.getInstance().migrate(sUserName);
		}
		else if (sCommandLowerCase.equals(CMD_NUCLET_GENERATION_CREATE_EMPTY_XLSX_FILE.toLowerCase())) {
			NucletGenerator generator = new NucletGenerator();
			if (asArgs.length == 2) {
				generator.createEmptyXLSXFile(asArgs[1]);
			} else {
				generator.createEmptyXLSXFile();
			}
		}
		else if (sCommandLowerCase.equals(CMD_NUCLET_GENERATION_FROM_XLSX.toLowerCase())) {
			NucletGenerator generator = new NucletGenerator();
			if (asArgs.length == 2) {
				generator.generateNucletFromXLSX(asArgs[1]);
			} else {
				generator.generateNucletFromXLSX();
			}
		}
		else {
			throw new CommonBusinessException("Unknown command: " + sCommand + "\n" + getUsage());
		}
	}


	private void invalidatAllCaches() throws CommonRemoteException, RemoteException, CommonFatalException {
		System.out.println(consoleFacadeRemote.invalidateAllCaches());
	}

	private void killSession(String user) throws RemoteException {
		consoleFacadeRemote.killSession(user);
	}

	private void sendMessage(String sMessage, String sUser, Priority priority, String sAuthor) throws RemoteException {
		consoleFacadeRemote.sendClientNotification(sMessage, sUser, priority, sAuthor);
	}

	/**
	 * Check the given parameters for import or export use cases. Give explicit error description if necessary.
	 * @param args all arguments to the use case
	 * @param bDirectoryMayBeEmpty
	 * @throws CommonBusinessException
	 */
	private void checkForImportExportParameters(String[] args, boolean bDirectoryMayBeEmpty) throws CommonBusinessException {
		if (args.length < 2) {
			throw new CommonBusinessException("Name of " + (bDirectoryMayBeEmpty ? "empty " : "") + "directory must be given.");
		}
		if (args.length > 2) {
			throw new CommonBusinessException("Too many parameters - maybe enclosing quotation marks missing?");
		}
	}

	private void compileDBObjects() throws RemoteException, SQLException {
		consoleFacadeRemote.compileInvalidDbObjects();
	}
	
	private void rebuildConstraints() {
		String[] result = consoleFacadeRemote.rebuildConstraints(); 
		if (result != null) {
			for (String s : result) {
				System.out.println(s);
			}
		}
	}

	/**
	 * Validate all masterdata entries against their meta information (length, format, min, max etc.).
	 * @param filename the name of the csv file to which the results are written.
	 */
	private void checkMasterDataValues(String filename) {
		// iterate over masterdata meta cache; masterdatafacade.get(entity) to get all; validate each cvo
		MasterDataDelegate.getInstance().checkMasterDataValues(filename);
	}

	/**
	 * Check and eliminate attributes from object generation which are not valid in the context
	 * todo: check validity of subentities here also!
	 */
	private void validateObjectGenerations() {
		System.out.println("validateObjectGenerations start ...");
		attributeCache.fill();
		
		final GenericObjectMetaDataCache lometacache = GenericObjectMetaDataCache.getInstance();
		for (MasterDataVO mdvoGeneration : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.GENERATION.getEntityName())) {
			final Collection<String> collSourceAttributeNames = lometacache.getAttributeNamesByModuleId(mdvoGeneration.getField("sourceModuleId", Integer.class), Boolean.FALSE);
			final Collection<String> collTargetAttributeNames = lometacache.getAttributeNamesByModuleId(mdvoGeneration.getField("targetModuleId", Integer.class), Boolean.FALSE);
			final Collection<String> collAttributeNames = CollectionUtils.intersection(collSourceAttributeNames, collTargetAttributeNames);
			final Collection<EntityObjectVO> coll = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), "generation", mdvoGeneration.getId());
			for (EntityObjectVO mdvoAttribute : coll) {
				if (!collAttributeNames.contains(mdvoAttribute.getField("attribute", String.class))) {
					try {
						MasterDataDelegate.getInstance().remove(NuclosEntity.GENERATIONATTRIBUTE.getEntityName(), DalSupportForMD.wrapEntityObjectVO(mdvoAttribute), null);
					}
					catch (CommonBusinessException e) {
						// Ok! (tp)
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("validateObjectGenerations finished");
	}

	private void printDeveloperCommands() {
		System.out.println("Developer commands:\n");
		System.out.println("\t-invalidatemasterdatametacache");
		System.out.println("\t\t\tFills the masterdata cache of the server newly with values, so it is not necessary to restart the server (but the client still)");
	}

	/**
	 * This is a temporary hack that may be used for quick migrations etc.
	 * @param asArgs
	 * @todo create a server-side pendant doSomethingUseful(asArgs) in your favorite FacadeBean.
	 */
	private void doSomethingUseful(String[] asArgs) {
		// today, we're generating stupid sql inserts out of metadata information:
	}

	/**
	 * called if real console is required (shell)
	 * @param asArgs
	 */
	public static void main(String[] asArgs) {
		final NuclosConsole dut = NuclosConsole.getInstance();
		try {
			if (asArgs.length < 4) {
				System.out.println("Missing command.\n\n");
				System.out.println(dut.getUsage());
				System.exit(-1);
			}

			dut.login(asArgs[0], asArgs[1]);
			try {
				final String[] asParamsWithoutLoginInfo = new String[asArgs.length - 3];
				for (int i = 3; i < asArgs.length; i++) {
					asParamsWithoutLoginInfo[i - 3] = asArgs[i];
				}
				NuclosConsole.getInstance().parseAndInvoke(asParamsWithoutLoginInfo, true);
			}
			finally {
				dut.logout();
			}
			System.exit(0);
		}
		catch (CommonBusinessException ex) {
			// Ok! (tp)
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			System.exit(-1);
		}
		catch (Exception ex) {
			// Ok! (tp)
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
