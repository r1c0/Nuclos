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
package org.nuclos.client.main;



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.annotation.PostConstruct;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.jfree.util.Log;
import org.nuclos.api.ui.MenuItem;
import org.nuclos.client.LocalUserCaches;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.prefs.WebAccessPrefs;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.console.NuclosConsoleGui;
import org.nuclos.client.customcomp.CustomComponentCache;
import org.nuclos.client.customcomp.CustomComponentController;
import org.nuclos.client.customcomp.resplan.ResPlanAction;
import org.nuclos.client.customcomp.wizard.CustomComponentWizard;
import org.nuclos.client.eventsupport.EventSupportManagementController;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.help.HtmlPanel;
import org.nuclos.client.help.releasenotes.ReleaseNotesController;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.login.LoginController;
import org.nuclos.client.main.mainframe.IconResolver;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.main.mainframe.workspace.RestoreUtils;
import org.nuclos.client.main.mainframe.workspace.WorkspaceChooserController;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.nuclet.NucletComponentRepository;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.report.admin.ReportExecutionCollectController;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.searchfilter.EntitySearchFilter;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.tasklist.TasklistAction;
import org.nuclos.client.tasklist.TasklistCache;
import org.nuclos.client.ui.ClipboardUtils;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.TopController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.collect.CollectStateModel;
import org.nuclos.client.ui.collect.detail.DetailsCollectableEventListener;
import org.nuclos.client.ui.collect.search.ReportExecutionSearchStrategy;
import org.nuclos.client.wiki.WikiController;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.common.Actions;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.CommandInformationMessage;
import org.nuclos.common.CommandMessage;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.Priority;
import org.nuclos.common.RuleNotification;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.ComparatorUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.RemoteAuthenticationManager;
import org.nuclos.common.tasklist.TasklistDefinition;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.Delayer;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.SystemUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;


/**
 * The main controller for the Nucleus client. Controller of the <code>MainFrame</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Configurable
public class MainController {

	private static final Logger LOG = Logger.getLogger(MainController.class);

	/**
	 * the preferences key for the frame state.
	 */
	private static final String PREFS_NODE_MAINFRAME = "mainFrame";
	@Deprecated
	private static final String PREFS_NODE_MDIWINDOWS = "mdiWindows";

	private String sUserName;
	private String sNuclosServerName;
	private LoginController loginController;

	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_MAINFRAME);

	private static MainFrame frm;

	private SwingDebugFrame debugFrame;

	/**
	 * controller for explorer panel
	 */
	private ExplorerController ctlExplorer;

	/**
	 * controller for task panel
	 */
	protected TaskController ctlTasks;

	private NuclosNotificationDialog notificationdlg;

	/**
	 * maps opened internal frames to their controllers.
	 */
	private final Map<MainFrameTab, TopController> mpActiveControllers = CollectionUtils.newHashMap();

	/**
	 * Subscribed to TOPICNAME_RULENOTIFICATION.
	 * 
	 * @see #init()
	 */
	private final MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(final Message msg) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						LOG.info("onMessage " + this + " handleMessage...");
						handleMessage(msg);
					}
					catch (Exception e) {
						LOG.error("onMessage: ParameterPanel failed: " + e, e);
					}
				}
			});
		}
	};
	
	private final Runnable refreshMenusLater = new Runnable() {
		@Override
		public void run() {
			LOG.info("refreshMenusLater");
			refreshMenus();
		}
	};

	private DirectHelpActionListener dha;
	
	// Spring injection

	private NucletComponentRepository nucletComponentRepository;

	private SpringLocaleDelegate localeDelegate;

	private MetaDataClientProvider mdProv;

	private TopicNotificationReceiver tnr;

	private ResourceCache resourceCache;

	private SecurityCache securityCache;
	
	private RestoreUtils restoreUtils;
	
	private NuclosRemoteServerSession nuclosRemoteServerSession;
	
	private WebAccessPrefs webAccessPrefs;
	
	private WorkspaceChooserController workspaceChooserController;
	
	// end of Spring injection

	/**
	 * @param sUserName name of the logged in user
	 * @param sNuclosServerName name of the Nucleus server connected to.
	 * @throws BackingStoreException
	 */
	public MainController(String sUserName, String sNuclosServerName, LoginController loginController) {
		this.sUserName = sUserName;
		this.sNuclosServerName = sNuclosServerName;
		this.loginController = loginController;
	}

	@Autowired
	final void setSecurityCache(SecurityCache securityCache) {
		this.securityCache = securityCache;
	}

	@Autowired
	final void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}

	@Autowired
	final void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}

	@Autowired
	final void setMetaDataClientProvider(MetaDataClientProvider mdProv) {
		this.mdProv = mdProv;
	}

	@Autowired
	final void setNucletComponentRepository(NucletComponentRepository ncr) {
		this.nucletComponentRepository = ncr;
	}

	@Autowired
	final void setMainFrame(@Value("#{mainFrameSpringComponent.mainFrame}") MainFrame mainFrame) {
		this.frm = mainFrame;
	}

	@Autowired
	final void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	@Autowired
	final void setRestoreUtils(RestoreUtils restoreUtils) {
		this.restoreUtils = restoreUtils;
	}
	
	@Autowired
	final void setNuclosRemoteServerSession(NuclosRemoteServerSession nuclosRemoteServerSession) {
		this.nuclosRemoteServerSession = nuclosRemoteServerSession;
	}
	
	@Autowired
	final void setWebAccessPrefs(WebAccessPrefs webAccessPrefs) {
		this.webAccessPrefs = webAccessPrefs;
	}
	
	@Autowired
	final void setWorkspaceChooserController(WorkspaceChooserController workspaceChooserController) {
		this.workspaceChooserController = workspaceChooserController;
	}
	
	@PostConstruct
	void init() throws CommonPermissionException, BackingStoreException {
		debugFrame = new SwingDebugFrame(this);
		try {
			// force to load real permission (tp)
			SecurityCache.getInstance().revalidate();
			
			cmdExecuteRport = createEntityAction(NuclosEntity.REPORTEXECUTION);

			/** @todo this is a workaround - because Main.getMainController() is called to get the user name */
			Main.getInstance().setMainController(this);

			LOG.debug(">>> read user rights...");
			loginController.increaseLoginProgressBar(StartUp.PROGRESS_INIT_SECURITYCACHE);

			if (!securityCache.isActionAllowed(Actions.ACTION_SYSTEMSTART)) {
				throw new CommonPermissionException(localeDelegate.getMessage(
						"MainController.23", "Sie haben nicht das Recht, {0} zu benutzen.", ApplicationProperties.getInstance().getName()));
			}

			loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_ATTRIBUTES);

			// DefaultCollectableEntityProvider.setInstance(NuclosCollectableEntityProvider.getInstance());

			Thread threadGenericObjectMetaDataCache = new Thread("MainController.readMetaData") {

				@Override
				public void run() {
					LOG.debug(">>> read metadata...");
					GenericObjectMetaDataCache.getInstance();
				}
			};

			loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_LOMETA);

			Thread threadSearchFilterCache = new Thread("MainController.readSearchFilter") {

				@Override
				public void run() {
					LOG.debug(">>> read searchfilter...");
					SearchFilterCache.getInstance();
				}
			};

			loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_SEARCHFILTER);

			Thread threadRuleCache = new Thread("MainController.readRules") {

				@Override
				public void run() {
					LOG.debug(">>> read rules...");
					RuleCache.getInstance();
				}
			};
			
			List<Thread> lstCacheThreads = new ArrayList<Thread>();
			lstCacheThreads.add(threadGenericObjectMetaDataCache);
			lstCacheThreads.add(threadSearchFilterCache);
			lstCacheThreads.add(threadRuleCache);
			threadGenericObjectMetaDataCache.start();
			threadSearchFilterCache.start();
			threadRuleCache.start();

			for(Thread t : lstCacheThreads) {
				try {
					t.join();
				}
				catch(InterruptedException e) {
					// do noting here
					LOG.warn("MainController: " + e);
				}
			}

			LOG.debug(">>> create mainframe...");
			// this.frm = new MainFrame(this.getUserName(), this.getNuclosServerName());

			this.frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
			// Attention: Do not use ListenerUtil here! (tp)
			frm.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent ev) {
					cmdWindowClosing(new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {}
					});
				}
			});
			loginController.increaseLoginProgressBar(StartUp.PROGRESS_CREATE_MAINFRAME);

			LOG.debug(">>> init client communication...");
			this.notificationdlg = new NuclosNotificationDialog(this.frm);
			tnr.subscribe(JMSConstants.TOPICNAME_RULENOTIFICATION, messagelistener);
			loginController.increaseLoginProgressBar(StartUp.PROGRESS_INIT_NOTIFICATION);

			LOG.debug(">>> setup menus...");
			this.setupMenus();
			loginController.increaseLoginProgressBar(StartUp.PROGRESS_CREATE_MAINMENU);

			LOG.debug(">>> create explorer controller...");
			this.ctlExplorer = new ExplorerController();

			LOG.debug(">>> create task controller...");
			this.ctlTasks = new TaskController(getUserName());

			this.ctlTasks.setExplorerController(ctlExplorer);
			this.ctlExplorer.setTaskController(ctlTasks);

			initActions();

			LOG.debug(">>> restore last workspace...");
			try {
				Main.getInstance().getMainFrame().readMainFramePreferences(prefs);
				restoreUtils.restoreWorkspaceThreaded(
						MainFrame.getLastWorkspaceIdFromPreferences(),
						MainFrame.getLastWorkspaceFromPreferences(),
						MainFrame.getLastAlwaysOpenWorkspaceIdFromPreferences(),
						MainFrame.getLastAlwaysOpenWorkspaceFromPreferences());
			}
			catch (Exception ex) {
				final String sMessage = localeDelegate.getMessage(
						"MainController.4","Die in der letzten Sitzung ge\u00f6ffneten Fenster konnten nicht wiederhergestellt werden.");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			finally {
				loginController.increaseLoginProgressBar(StartUp.PROGRESS_RESTORE_WORKSPACE);
			}

			LOG.debug(">>> show mainFrame...");
			frm.setVisible(true);

			try {
				LOG.debug(">>> restore last controllers (for migration only)...");
				reopenAllControllers(ClientPreferences.getUserPreferences());
			}
			catch (Exception ex) {
				final String sMessage = localeDelegate.getMessage(
						"MainController.4","Die in der letzten Sitzung ge\u00f6ffneten Fenster konnten nicht wiederhergestellt werden.");
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}

			LOG.debug(">>> restore task views (for migration only)...");
			try {
				ctlTasks.restoreGenericObjectTaskViewsFromPreferences();
			}
			catch (Exception ex) {
				final String sMessage = localeDelegate.getMessage(
						"tasklist.error.restore", "Die Aufgabenlisten konnten nicht wiederhergestellt werden.");
				LOG.error(sMessage, ex);
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}

			Thread theadTaskController = new Thread("MainController.refreshTasks") {
				@Override
				public void run() {
					LOG.debug(">>> refresh tasks...");
					ctlTasks.run();
				}
			};
			theadTaskController.start();

			/* Release note HACK:
			Caused by: java.lang.NullPointerException
            at org.nuclos.client.help.releasenotes.ReleaseNotesController.showNuclosReleaseNotesNotice(ReleaseNotesController.java:148)
            at org.nuclos.client.help.releasenotes.ReleaseNotesController.showReleaseNotesIfNewVersion(ReleaseNotesController.java:161)
            at org.nuclos.client.main.MainController.showReleaseNotesIfNewVersion(MainController.java:1752)
            at org.nuclos.client.main.MainController.<init>(MainController.java:382)
             */
			while (getHomePane() == null) {
				Thread.sleep(200);
			}

			// Show the release notes for this version, if the user hasn't seen it yet.
			showReleaseNotesIfNewVersion();

			// Debug purposes
			final String sKeyWindowShow = "CtlShiftF11";
			frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)), sKeyWindowShow);
			frm.getRootPane().getActionMap().put(sKeyWindowShow, new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					debugFrame.showComponentDetails(frm.findComponentAt(frm.getMousePosition()));
				}
			});

			//Call wikipage
			final String sKeyWikiShow = "CtlShiftF1";
			frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)), sKeyWikiShow);
			frm.getRootPane().getActionMap().put(sKeyWikiShow, new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent ev) {
					Component fundComponent = frm.getFocusOwner() != null ? frm.getFocusOwner() : frm.findComponentAt(frm.getMousePosition());
					CollectController<?> clctctrl = getControllerForTab(UIUtils.getTabForComponent(fundComponent));

					WikiController wikiCtrl = WikiController.getInstance();
					wikiCtrl.openURLinBrowser(wikiCtrl.getWikiPageForComponent(fundComponent, clctctrl));

				}
			});
		}
		catch (Throwable e) {
			LOG.fatal("Creating MainController failed, this is fatal: " + e.toString(), e);
			throw new ExceptionInInitializerError(e);
		}
	}

	private void initActions() {
		try {
			dha = new DirectHelpActionListener();

			// init Actions
			cmdDirectHelp = new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dha.actionPerformed(e);
				}
			};
			cmdShowTimelimitTasks = new AbstractAction(
					localeDelegate.getMessage("miShowTimelimitTasks","Fristen anzeigen"),
					Icons.getInstance().getIconTabTimtlimit()) {

					@Override
					public void actionPerformed(ActionEvent e) {
						MainController.this.getTaskController().getTimelimitTaskController().cmdShowTimelimitTasks();
					}
					@Override
					public boolean isEnabled() {
						return securityCache.isActionAllowed(Actions.ACTION_TIMELIMIT_LIST);
					}
				};
			cmdShowPersonalTasks = new AbstractAction(
					localeDelegate.getMessage("miShowPersonalTasks","Meine Aufgaben anzeigen"),
					Icons.getInstance().getIconTabTask()) {

					@Override
					public void actionPerformed(ActionEvent e) {
						MainController.this.getTaskController().getPersonalTaskController().cmdShowPersonalTasks();
					}
					@Override
					public boolean isEnabled() {
						return securityCache.isActionAllowed(Actions.ACTION_TASKLIST);
					}
				};
			cmdShowPersonalSearchFilters = new AbstractAction(
					localeDelegate.getMessage("ExplorerPanel.3","Meine Suchfilter anzeigen"),
					Icons.getInstance().getIconFilter16()) {

					@Override
					public void actionPerformed(ActionEvent e) {
						MainController.this.getExplorerController().cmdShowPersonalSearchFilters();
					}
				};
			cmdChangePassword = new AbstractAction() {

				private Boolean enabled;

				@Override
				public void actionPerformed(ActionEvent evt) {
					ChangePasswordPanel cpp = new ChangePasswordPanel(true, "", false);
					boolean result = cpp.showInDialog(getFrame(), new ChangePasswordPanel.ChangePasswordDelegate() {
						@Override
						public void changePassword(String oldPw, String newPw) throws CommonBusinessException {
							RemoteAuthenticationManager ram = SpringApplicationContextHolder.getBean(RemoteAuthenticationManager.class);
							ram.changePassword(sUserName, oldPw, newPw);
							nuclosRemoteServerSession.relogin(sUserName, newPw);
							try {
								MainController.this.prefs.flush();
							} catch (BackingStoreException e) {
								LOG.fatal("actionPerformed failed: " + e, e);
							}
							LocalUserProperties props = LocalUserProperties.getInstance();
							props.setUserPasswd("");
							props.store();
						}
					});
				}

				@Override
				public synchronized boolean isEnabled() {
					if (enabled == null) {
						enabled = !SecurityDelegate.getInstance().isLdapAuthenticationActive() || SecurityDelegate.getInstance().isSuperUser();
					}
					return LangUtils.defaultIfNull(enabled, Boolean.FALSE);
				}
			};
			cmdOpenManagementConsole = new AbstractAction(
					localeDelegate.getMessage("miManagementConsole", "Management Console"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.158-wrench-2.png"))) {

					@Override
					public void actionPerformed(ActionEvent evt) {
						UIUtils.runCommand(frm, new Runnable() {
							@Override
							public void run() {
								try {
									NuclosConsoleGui.showInFrame(frm.getHomePane().getComponentPanel());
								}
								catch (Exception e) {
									LOG.error("showInFrame failed: " + e, e);
								}
							}
						});
					}};
					
			cmdOpenEntityWizard = new AbstractAction(
					localeDelegate.getMessage("miEntityWizard", "Entity Wizard"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.81-dashboard.png"))) {

				@Override
				public void actionPerformed(ActionEvent evt) {
					final MainFrameTabbedPane desktopPane = MainController.this.getHomePane();
					UIUtils.runCommand(frm, new ShowNuclosWizard.NuclosWizardRoRunnable(desktopPane));
				}};
				
			cmdOpenEventSupportManagement = new AbstractAction(
					localeDelegate.getMessage("miEventSupportManagement", "EventSupport Management"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.34-coffee.png"))) {

				@Override
				public void actionPerformed(ActionEvent evt) {
					final MainFrameTabbedPane desktopPane = MainController.this.getHomePane();
					UIUtils.runCommand(frm, new EventSupportManagementController.NuclosESMRunnable(desktopPane));
				}};
			
			cmdOpenCustomComponentWizard = new AbstractAction(
					localeDelegate.getMessage("miResPlanWizard", "Ressourcenplanung"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.83-calendar.png"))) {

				@Override
				public void actionPerformed(final ActionEvent evt) {
					UIUtils.runCommand(frm, new Runnable() {
						@Override
						public void run() {
							try {
								CustomComponentWizard.run();
							}
							catch (Exception e) {
								LOG.error("CustomComponentWizard failed: " + e, e);
							}
						}
					});
				}};
			cmdOpenRelationEditor = new AbstractAction(
					localeDelegate.getMessage("miRelationEditor", "Relationeneditor"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.55-network.png"))) {

						@Override
						public void actionPerformed(final ActionEvent evt) {
							UIUtils.runCommand(frm, new Runnable() {
								@Override
								public void run() {
									try {
										final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
										Collection<MasterDataVO> colRelation = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ENTITYRELATION.getEntityName());
										EntityRelationShipCollectController result = factory.newEntityRelationShipCollectController(MainController.this.getFrame(), null);
										if(colRelation.size() > 0) {
											MasterDataVO vo = colRelation.iterator().next();
											result.runViewSingleCollectableWithId(vo.getId());
										}
										else {
											result.runNew();
										}
									}
									catch(/* CommonBusiness */ Exception e1) {
										LOG.error("actionPerformed " + evt + ": " + e1);
									}
								}
							});
						}};
			cmdOpenRelationEditor = new AbstractAction(
					localeDelegate.getMessage("miRelationEditor", "Relationeneditor"),
					MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.55-network.png"))) {

						@Override
						public void actionPerformed(final ActionEvent evt) {
							UIUtils.runCommand(frm, new Runnable() {
								@Override
								public void run() {
									try {
										final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
										Collection<MasterDataVO> colRelation = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ENTITYRELATION.getEntityName());
										EntityRelationShipCollectController result = factory.newEntityRelationShipCollectController(MainController.this.getFrame(), null);
										if(colRelation.size() > 0) {
											MasterDataVO vo = colRelation.iterator().next();
											result.runViewSingleCollectableWithId(vo.getId());
										}
										else {
											result.runNew();
										}
									}
									catch(/* CommonBusiness */ Exception e1) {
										LOG.error("actionPerformed " + evt + ": " + e1);
									}
								}
							});
						}};
			cmdOpenSettings = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cmdOpenSettings();
				}
			};
			cmdRefreshClientCaches = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					UIUtils.runCommandLater(getFrame(), new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							invalidateAllClientCaches();
							JOptionPane.showMessageDialog(getFrame(),
								localeDelegate.getMessage("MainController.3","Die folgenden Aktionen wurden erfolgreich durchgef\u00fchrt:\n" +
									"Caches aktualisiert: MasterDataCache, SecurityCache, AttributeCache, GenericObjectLayoutCache, GeneratorCache, MetaDataCache, ResourceCache, SearchFilterCache.\n"+
								"Men\u00fcs aktualisiert."));
						}
					});
				}
			};
			cmdSelectAll = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					// select all rows in the Result panel of the current CollectController (if any):
					final MainFrameTab ifrm = (MainFrameTab) MainController.this.frm.getHomePane().getSelectedComponent();
					if (ifrm != null) {
						final CollectController<?> ctl = getControllerForTab(ifrm);
						if (ctl != null && ctl.getCollectState().getOuterState() == CollectStateModel.OUTERSTATE_RESULT) {
							ctl.getResultTable().selectAll();
						}
						else if (ctl != null && ((ctl.getCollectState().getOuterState() == CollectStateModel.OUTERSTATE_DETAILS)
								|| ctl.getCollectState().getOuterState() == CollectStateModel.OUTERSTATE_SEARCH)){
							Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();

							if (focusOwner instanceof JTextComponent) {
								((JTextComponent)focusOwner).selectAll();
							}
						}
					}
				}
			};
			cmdHelpContents = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					WikiController.getInstance().openURLinBrowser(ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_STARTPAGE));
				}
			};
			cmdShowAboutDialog  = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					cmdShowAboutDialog();
				}
			};
			cmdShowProjectReleaseNotes  = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					new ReleaseNotesController().showReleaseNotes(ApplicationProperties.getInstance().getName());
				}
			};
			cmdShowNuclosReleaseNotes  = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent evt) {
					ReleaseNotesController.openReleaseNotesInBrowser();
				}
			};
			cmdWindowClosing = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cmdWindowClosing(new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {}
					});
				}
			};
			cmdLogoutExit = new AbstractAction() {

				@Override
				public void actionPerformed(ActionEvent e) {
					cmdLogoutExit();
				}
			};
			cmdExecuteRport = createEntityAction(NuclosEntity.REPORTEXECUTION);
		}
		catch (Throwable e) {
			LOG.fatal("Creating MainController failed, this is fatal: " + e.toString(), e);
			throw new ExceptionInInitializerError(e);
		}
	}

	private Action cmdDirectHelp;

	private Action cmdShowTimelimitTasks;

	private Action cmdShowPersonalTasks;

	private Action cmdShowPersonalSearchFilters;

	private Action cmdChangePassword;

	private Action cmdOpenManagementConsole;

	private Action cmdOpenEntityWizard;
	
	private Action cmdOpenEventSupportManagement;

	private Action cmdOpenCustomComponentWizard;

	private Action cmdOpenRelationEditor;

	private Action cmdOpenSettings;

	public Action cmdRefreshClientCaches;

	private Action cmdSelectAll;

	private Action cmdHelpContents;

	private Action cmdShowAboutDialog;

	private Action cmdShowProjectReleaseNotes;

	private Action cmdShowNuclosReleaseNotes;

	private Action cmdWindowClosing;

	private Action cmdLogoutExit;

	private Action cmdExecuteRport;

	public void cmdLogoutExit() {
		LocalUserProperties props = LocalUserProperties.getInstance();
		props.setUserPasswd("");
		props.store();
		cmdWindowClosing(new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {}
		});
	}

	public static void cmdOpenSettings() {
		NuclosSettingsContainer panel = new NuclosSettingsContainer(frm);

		JOptionPane p = new JOptionPane(panel,
			JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,	null);
		JDialog dlg = p.createDialog(Main.getInstance().getMainFrame(),
				SpringLocaleDelegate.getInstance().getMessage("R00022927", "Einstellungen"));
		dlg.pack();
		dlg.setResizable(true);
		dlg.setVisible(true);
		Object o = p.getValue();
		int res = ((o instanceof Integer)
			? ((Integer) o).intValue()
				: JOptionPane.CANCEL_OPTION);

		if(res == JOptionPane.OK_OPTION) {
			try {
				panel.save();
			}
			catch (PreferencesException e) {
				Errors.getInstance().showExceptionDialog(frm, e);
			}
		}
	}

	public void cmdShowAboutDialog() {
		try {
			final MainFrameTab internalFrame = newMainFrameTab(null, "Info");
			String html = IOUtils.readFromTextStream(getClass().getClassLoader().getResourceAsStream("org/nuclos/client/help/about/about.html"), null);

			Pair<String, String> clientLogFile = StartUp.getLogFile();
			String logDir = "";
			String logFilename = clientLogFile.x;
			String logDatePattern = clientLogFile.y;
			try {
				// try to extract dir and filename...
				logDir = clientLogFile.x.substring(0, clientLogFile.x.lastIndexOf("/"));
				logFilename = clientLogFile.x.substring(clientLogFile.x.lastIndexOf("/")+1) +
						logDatePattern.replace("'", "");
			} catch (Exception ex) {
				// do nothing
			}

			HtmlPanel htmlPanel = new HtmlPanel(
				String.format(
					html,
					ApplicationProperties.getInstance().getCurrentVersion(), // %1$s
					getUserName(),                                           // %2$s
					getNuclosServerName(),                                   // %3$s
					System.getProperty("java.version"),                      // %4$s
					logDir,							                         // %5$s
					logFilename						                         // %6$s
			));
			htmlPanel.btnClose.addActionListener(new ActionListener() {
				@Override
                public void actionPerformed(ActionEvent ev) {
					internalFrame.dispose();
				}});
			htmlPanel.addHyperlinkListener(new HyperlinkListener() {
				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == EventType.ACTIVATED) {
						try {
							Desktop.getDesktop().browse(e.getURL().toURI());
						} catch(IOException e1) {
							LOG.warn("cmdShowAboutDialog " + e1 + ": " + e1, e1);
						} catch(URISyntaxException e1) {
							LOG.warn("cmdShowAboutDialog " + e1 + ": " + e1, e1);
						}
					}
				}
			});
			internalFrame.setLayeredComponent(htmlPanel);
			Main.getInstance().getMainFrame().getHomePane().add(internalFrame);
			internalFrame.setVisible(true);
        }
        catch(Exception e) {
        	Errors.getInstance().showExceptionDialog(Main.getInstance().getMainFrame(),
        			localeDelegate.getMessage("MainController.26", "Die Infos k\u00f6nnen nicht angezeigt werden."), e);
        }
	}

   private static class UIDefTableModel extends AbstractTableModel {

	  private ArrayList<Pair<String, Object>> values;

      public UIDefTableModel() {
         values = new ArrayList<Pair<String, Object>>();
      }

      public void add(String key, Object val) {
         values.add(new Pair<String, Object>(key, val));
      }

      public void sort() {
         Collections.sort(values, new Comparator<Pair<String, Object>>() {
            @Override
            public int compare(Pair<String, Object> o1, Pair<String, Object> o2) {
               return o1.x.compareToIgnoreCase(o2.x);
            }});
      }

      @Override
      public int getColumnCount() {
         return 2;
      }

      @Override
      public int getRowCount() {
         return values.size();
      }

      @Override
      public Object getValueAt(int rowIndex, int columnIndex) {
    	  Pair<String, Object> o = values.get(rowIndex);
         return columnIndex == 0 ? o.x : o.y;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
         return columnIndex == 0 ? String.class : Object.class;
      }

      @Override
      public String getColumnName(int columnIndex) {
         return columnIndex == 0 ? "Key" : "Value";
      }

      public void forceValue(int rowIndex) {
    	  Pair<String, Object> pair = values.get(rowIndex);
    	  if (pair.y instanceof UIDefaults.LazyValue) {
    		  pair.y = ((UIDefaults.LazyValue) pair.y).createValue(UIManager.getDefaults());
    		  fireTableCellUpdated(rowIndex, 1);
    	  }
      }
   }

   private static class UIDefaultsRenderer extends DefaultTableCellRenderer {

	@Override
      public Component getTableCellRendererComponent(JTable table, Object val, boolean isSelected, boolean hasFocus, int row, int column) {
         JLabel c = (JLabel) super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
         c.setIcon(null);
         c.setFont(table.getFont());
         c.setForeground(table.getForeground());
         c.setBackground(table.getBackground());
         if(val == null) {
            c.setText("<null>");
         } else if(val instanceof Color) {
            Color col = (Color) val;
            c.setBackground(col);
            c.setText("Color: " + col.getRed() + "/" + col.getGreen() + "/" + col.getBlue());
         } else if(val instanceof Font) {
            c.setText(val.toString());
            c.setFont((Font) val);
         } else if (val instanceof Icon) {
        	c.setIcon((Icon) val);
            c.setText(val.toString());
         } else {
            c.setText(val.toString());
         }
         return c;
      }
   }

	private Map<String, Map<String, Action>> getCommandMap() {
		HashMap<String, Map<String, Action>> res = new HashMap<String, Map<String, Action>>();
		HashMap<String, Action> mainController = new HashMap<String, Action>();

		/* that's too cumbersome:
		mainController.put(
			"cmdChangePassword",
			new AbstractAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					cmdChangePassword();
				}
			});
		 */

		mainController.put("cmdDirectHelp", cmdDirectHelp);
		mainController.put("cmdShowPersonalTasks", cmdShowPersonalTasks);
		mainController.put("cmdShowTimelimitTasks", cmdShowTimelimitTasks);
		mainController.put("cmdShowPersonalSearchFilters", cmdShowPersonalSearchFilters);
		mainController.put("cmdChangePassword", cmdChangePassword);
		mainController.put("cmdOpenSettings", cmdOpenSettings);
		mainController.put("cmdOpenManagementConsole", cmdOpenManagementConsole);
		//mainController.put("cmdOpenEntityWizard", cmdOpenEntityWizard);
		mainController.put("cmdOpenRelationEditor", cmdOpenRelationEditor);
		mainController.put("cmdOpenCustomComponentWizard", cmdOpenCustomComponentWizard);
		//mainController.put("cmdRefreshClientCaches", cmdRefreshClientCaches);
		mainController.put("cmdSelectAll", cmdSelectAll);
		mainController.put("cmdHelpContents", cmdHelpContents);
		mainController.put("cmdShowAboutDialog", cmdShowAboutDialog);
		mainController.put("cmdShowProjectReleaseNotes", cmdShowProjectReleaseNotes);
		mainController.put("cmdShowNuclosReleaseNotes", cmdShowNuclosReleaseNotes);
		mainController.put("cmdLogoutExit", cmdLogoutExit);
		mainController.put("cmdWindowClosing", cmdWindowClosing);
		mainController.put("cmdExecuteRport", cmdExecuteRport);

		for(Method m : getClass().getDeclaredMethods()) {
			if(m.getName().startsWith("cmd")) {
				Class<?>[] pt = m.getParameterTypes();
				if(pt.length == 0 || (pt.length == 1 && pt[0].isAssignableFrom(ActionEvent.class))) {
					final Method fm = m;
					Action a = new AbstractAction(m.getName()) {

						@Override
						public void actionPerformed(ActionEvent e) {
							miDelegator(e, fm);
						}
					};
					mainController.put(m.getName(), a);
				}
			}
		}

		res.put("MainController", mainController);

		HashMap<String, Action> clipboardUtils = new HashMap<String, Action>();
		clipboardUtils.put("cutAction", new ClipboardUtils.CutAction());
		clipboardUtils.put("copyAction", new ClipboardUtils.CopyAction());
		clipboardUtils.put("pasteAction", new ClipboardUtils.PasteAction());

		res.put("ClipboardUtils", clipboardUtils);

		HashMap<String, Action> dev = new HashMap<String, Action>();
		dev.put("jmsNotification",
			new AbstractAction("Test JMS notification") {

				@Override
				public void actionPerformed(ActionEvent e) {
					String s = JOptionPane.showInputDialog(frm, "Topic: Message");
					if(s == null)
						return;
					String[] a = s.split(": *");
					if(a.length == 2) {
						// testFacadeRemote.testClientNotification(a[0], a[1]);
						throw new UnsupportedOperationException("TestFacade removed");
					}
					else {
						JOptionPane.showMessageDialog(frm, "Wrong input format");
					}
				}
			});

		dev.put("webPrefs", new AbstractAction("Test Web Prefs-Access") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(frm, "Access-Path");
				if(s == null)
					return;
				try {
					Map<String, String> m = webAccessPrefs.getPrefsMap(s);
					StringBuilder sb = new StringBuilder();
					for(String k : m.keySet())
						sb.append(k).append(": ").append(m.get(k)).append("\n");
					JOptionPane.showMessageDialog(frm, sb.toString());
				}
				catch(CommonBusinessException e1) {
					Errors.getInstance().showExceptionDialog(frm, e1);
				}
			}});

		dev.put("uiDefaults", new AbstractAction("UIDefaults") {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFrame out = new JFrame("UIDefaults");
		      out.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		      out.getContentPane().setLayout(new BorderLayout());
		      final UIDefTableModel mdl = new UIDefTableModel();
		      final JTable contentTable = new JTable(mdl);
		      JScrollPane sp = new JScrollPane(contentTable);
		      out.getContentPane().add(sp, BorderLayout.CENTER);

		      UIDefaults defs = UIManager.getDefaults();
		      for(Object key : CollectionUtils.iterableEnum((defs.keys())))
		         mdl.add(key.toString(), defs.get(key));
		      mdl.sort();

		      contentTable.getColumnModel().getColumn(1).setCellRenderer(new UIDefaultsRenderer());
		      contentTable.addMouseListener(new MouseAdapter() {
		    	  @Override
		    	public void mouseClicked(MouseEvent e) {
		    		int row = contentTable.rowAtPoint(e.getPoint());
		    		mdl.forceValue(contentTable.convertRowIndexToModel(row));
		    	}
		      });
		      out.pack();
		      out.setVisible(true);
			}
		});

		dev.put("checkJawin", new AbstractAction("Check Jawin") {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					SystemUtils.checkJawin();
					JOptionPane.showMessageDialog(Main.getInstance().getMainFrame(), "Jawin ok");
				} catch (Exception ex) {
					Errors.getInstance().showDetailedExceptionDialog(Main.getInstance().getMainFrame(), ex);
				}
			}
		});

		res.put("Dev", dev);

		return res;
	}

	private Map<String, Map<String, JComponent>> getComponentMap() {
		HashMap<String, Map<String, JComponent>> res = new HashMap<String, Map<String, JComponent>>();
		HashMap<String, JComponent> dev = new HashMap<String, JComponent>();

		Map<String, JComponent> mainFrame = frm.getComponentMap();
		res.put("MainFrame", mainFrame);

//		dev.put("memoryMonitor", new MemoryMonitor());
		res.put("Dev", dev);

		return res;
	}


	public void miDelegator(ActionEvent evt, Method m) {
		try {
			if(m.getParameterTypes().length == 0)
				m.invoke(MainController.this, new Object[0]);
			else
				m.invoke(MainController.this, new Object[] { evt });
		}
		catch(IllegalArgumentException e) {
			throw new CommonFatalException(e);
		}
		catch(IllegalAccessException e) {
			throw new CommonFatalException(e);
		}
		catch(InvocationTargetException e) {
			throw new RuntimeException(e.getCause());
		}
	}


	/**
	 * @return name of the logged in user.
	 */
	public String getUserName() {
		return sUserName;
	}

	/**
	 * @return name of the Nucleus server connected to.
	 */
	public String getNuclosServerName() {
		return sNuclosServerName;
	}

	/**
	 * @return the <code>ExplorerController</code>.
	 * @postcondition result != null
	 */
	public ExplorerController getExplorerController() {
		final ExplorerController result = this.ctlExplorer;
		assert result != null;
		return result;
	}

	/**
	 * @return the non-modal dialog containing messages from the server.
	 */
	public NuclosNotificationDialog getNotificationDialog() {
		return this.notificationdlg;
	}

	/**
	 * @param ctl may be <code>null</code>.
	 */
	public MainFrameTab newMainFrameTab(TopController ctl) {
		return newMainFrameTab(ctl, null);
	}

	/**
	 * @param ctl may be <code>null</code>.
	 * @param sTitle
	 */
	public MainFrameTab newMainFrameTab(TopController ctl, String sTitle) {
		final MainFrameTab result = new MainFrameTab(sTitle);
		initMainFrameTab(ctl, result);
		return result;
	}

	public void initMainFrameTab(TopController ctl, MainFrameTab tab) {
		Object lock = new Object();
		synchronized(lock) {
			Pair<IconResolver, String> iconAndResolver = ctl == null ? null : ctl.getIconAndResolver();
			if (iconAndResolver != null) {
				tab.setTabIcon(iconAndResolver.x, iconAndResolver.y);
			} else {
				if (ctl != null && ctl.getIconUnsafe() != null) {
					tab.setTabIconUnsafe(ctl.getIconUnsafe());
				} else {
					tab.setTabIconUnsafe(Icons.getInstance().getIconTabGeneric());
				}
			}
			addMainFrameTabListener(tab, ctl);
		}
	}

	/**
	 * @param tab
	 * @param ctl may be <code>null</code>.
	 */
	private void addMainFrameTabListener(final MainFrameTab tab, final TopController ctl) {
		tab.addMainFrameTabListener(new MainFrameTabAdapter() {

			@Override
			public void tabAdded(MainFrameTab tab) {
				addMainFrameTab(tab, ctl);
			}

			@Override
			public void tabClosed(MainFrameTab tab) {
				removeMainFrameTab(tab);
				tab.removeMainFrameTabListener(this);
			}

		});
	}

	/**
	 * @param tab
	 * @param ctl may be <code>null</code>.
	 */
	public void addMainFrameTab(final MainFrameTab tab, TopController ctl) {

		if (ctl != null) {
			this.mpActiveControllers.put(tab, ctl);
		}
	}

	private void removeMainFrameTab(MainFrameTab tab) {
		this.mpActiveControllers.remove(tab);
	}

	void setupMenus() {
		setupMenuBar();
	}

	public static final String GENERIC_ENTITY_ACTION = "nuclosGenericEntityAction";
	public static final String GENERIC_COMMAND_ACTION = "nuclosGenericCommandAction";
	public static final String GENERIC_NUCLETCOMPONENT_MENUITEM_ACTION = "nuclosGenericNucletComponentMenuItemAction";
	public static final String GENERIC_CUSTOMCOMPONENT_ACTION = "nuclosGenericCustomComponentAction";
	public static final String GENERIC_SEARCHFILTER_ACTION = "nuclosGenericSearchFilterAction";
	public static final String GENERIC_RESTORE_WORKSPACE_ACTION = "nuclosGenericRestoreWorkspaceAction";
	public static final String GENERIC_REPORT_ACTION = "nuclosGenericReportAction";
	public static final String GENERIC_TASKLIST_ACTION = "nuclosGenericTasklistAction";

	public List<GenericAction> getGenericActions() {
		List<GenericAction> result = new ArrayList<GenericAction>();

		getFileMenuActions(result);
		getAdministrationMenuActions(result);
		getConfigurationMenuActions(result);
		//getHelpMenuActions(result);

		List<GenericAction> sortedResult = new ArrayList<GenericAction>();
		getEntityMenuActions(sortedResult);
		getNucletComponentMenuActions(sortedResult);
		getCustomComponentMenuActions(sortedResult);
		getTasklistMenuActions(sortedResult);

		final Collator collator = Collator.getInstance(Locale.getDefault());
		final Comparator<String[]> arrayCollator = ComparatorUtils.arrayComparator(collator);
		Collections.sort(sortedResult, new Comparator<GenericAction>() {
			@Override
			public int compare(GenericAction p1, GenericAction p2) {
				int cmp = arrayCollator.compare(p1.y.x, p2.y.x);
				if (cmp == 0)
					cmp = collator.compare(p1.y.y.getValue(Action.NAME), p2.y.y.getValue(Action.NAME));
				return cmp;
			}
		});
		result.addAll(sortedResult);

		addSearchFilterActions(result);
		addReportActions(result);
		workspaceChooserController.addGenericActions(result);

		return result;
	}

	private void addGenericCommandAction(List<GenericAction> genericActions, String command, Action act, String[] menuPath) {
		if (genericActions != null && act != null) {
			WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
			wa.setAction(GENERIC_COMMAND_ACTION);
			wa.putStringParameter("command", command);
			genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, act)));
		}
	}	
	
	private List<Pair<String[], Action>> getFileMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuFile()};

		if (securityCache.isActionAllowed(Actions.ACTION_EXECUTE_REPORTS)) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdExecuteRport));
			addGenericCommandAction(genericActions, Actions.ACTION_EXECUTE_REPORTS, cmdExecuteRport, menuPath);
		}
		if (cmdShowPersonalSearchFilters.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowPersonalSearchFilters));
			addGenericCommandAction(genericActions, "", cmdShowPersonalSearchFilters, menuPath);
		}
		if (securityCache.isActionAllowed(Actions.ACTION_TASKLIST)) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowPersonalTasks));
			addGenericCommandAction(genericActions, Actions.ACTION_TASKLIST, cmdShowPersonalTasks, menuPath);
		}
		if (securityCache.isActionAllowed(Actions.ACTION_TIMELIMIT_LIST)) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowTimelimitTasks));
			addGenericCommandAction(genericActions, Actions.ACTION_TIMELIMIT_LIST, cmdShowTimelimitTasks, menuPath);
		}
		/*if (cmdChangePassword.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdChangePassword));
			addGenericCommandAction(genericActions, "", cmdChangePassword, menuPath);
		}
		if (cmdOpenSettings.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenSettings));
			addGenericCommandAction(genericActions, "", cmdOpenSettings, menuPath);
		}
		if (cmdLogoutExit.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdLogoutExit));
			addGenericCommandAction(genericActions, "", cmdLogoutExit, menuPath);
		}
		if (cmdWindowClosing.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdWindowClosing));
			addGenericCommandAction(genericActions, "", cmdWindowClosing, menuPath);
		}*/
		
		return menuActions;
	}

	private List<Pair<String[], Action>> getHelpMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuHelp()};

		/*if (cmdDirectHelp.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdDirectHelp));
			addGenericCommandAction(genericActions, "", cmdDirectHelp, menuPath);
		}*/
		if (cmdHelpContents.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdHelpContents));
			addGenericCommandAction(genericActions, "", cmdHelpContents, menuPath);
		}
		if (cmdShowAboutDialog.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowAboutDialog));
			addGenericCommandAction(genericActions, "", cmdShowAboutDialog, menuPath);
		}
		/*if (cmdShowProjectReleaseNotes.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowProjectReleaseNotes));
			addGenericCommandAction(genericActions, "", cmdShowProjectReleaseNotes, menuPath);
		}*/
		if (cmdShowNuclosReleaseNotes.isEnabled()) { // no Security
			menuActions.add(new Pair<String[], Action>(menuPath, cmdShowNuclosReleaseNotes));
			addGenericCommandAction(genericActions, "", cmdShowNuclosReleaseNotes, menuPath);
		}		

		return menuActions;
	}

	public List<Pair<String[], Action>> getAdministrationMenuActions() {
		return getAdministrationMenuActions(null);
	}

	private List<Pair<String[], Action>> getAdministrationMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuAdministration()};

		addActionIfAllowed(menuActions, menuPath, NuclosEntity.USER, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.ROLE, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.SEARCHFILTER, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.JOBCONTROLLER, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.PARAMETER, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.LDAPSERVER, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.WEBSERVICE, genericActions);

		if (securityCache.isActionAllowed("UseManagementConsole")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenManagementConsole));
			addGenericCommandAction(genericActions, "UseManagementConsole", cmdOpenManagementConsole, menuPath);
		}

		return menuActions;
	}

	public List<Pair<String[], Action>> getConfigurationMenuActions() {
		return getConfigurationMenuActions(null);
	}

	private List<Pair<String[], Action>> getConfigurationMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuConfiguration()};

		if (securityCache.isActionAllowed("EntityWizard")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenEntityWizard));
			addGenericCommandAction(genericActions, "EntityWizard", cmdOpenEntityWizard, menuPath);
		}

		if (ApplicationProperties.getInstance().isFunctionBlockDev()) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenEventSupportManagement));
			addGenericCommandAction(genericActions, "EventSupportManagement", cmdOpenEventSupportManagement, menuPath);
		}
	
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.LAYOUT, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.STATEMODEL, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.GENERATION, genericActions);

		if (securityCache.isActionAllowed("RelationEditor")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenRelationEditor));
			addGenericCommandAction(genericActions, "RelationEditor", cmdOpenRelationEditor, menuPath);
		}
		if (securityCache.isActionAllowed("ResPlanWizard")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenCustomComponentWizard));
			addGenericCommandAction(genericActions, "ResPlanWizard", cmdOpenCustomComponentWizard, menuPath);
		}

		//addActionIfAllowed(menuActions, menuPath, NuclosEntity.REPORT, genericActions);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.NUCLET, genericActions);

		return menuActions;
	}

	private void addActionIfAllowed(List<Pair<String[], Action>> menuActions, String[] menuPath, NuclosEntity entity, List<GenericAction> genericActions) {
		EntityMetaDataVO entitymetavo = mdProv.getEntity(entity);
		Action act = createEntityAction(entitymetavo, localeDelegate.getLabelFromMetaDataVO(entitymetavo), false, null);
		if (act != null) {
			menuActions.add(new Pair<String[], Action>(menuPath, act));
			if (genericActions != null) {
				WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
				wa.setAction(GENERIC_ENTITY_ACTION);
				wa.putStringParameter("entity", entity.getEntityName());
				genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, act)));
			}
		}
	}

	public List<Pair<String[], Action>> getEntityMenuActions() {
		return getEntityMenuActions(null);
	}

	private List<Pair<String[], Action>> getEntityMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> entityMenuActions = new ArrayList<Pair<String[], Action>>();

		Set<String> customConfigurationEntities = new HashSet<String>();

		for (EntityObjectVO conf : mdProv.getAllEntityMenus()) {
			EntityMetaDataVO meta = mdProv.getEntity(conf.getFieldId("entity"));
			String[] menuPath = splitMenuPath(localeDelegate.getResource(conf.getField("menupath", String.class), null));

			if (menuPath != null && menuPath.length > 0) {
				Action action = createEntityAction(meta, menuPath[menuPath.length - 1], conf.getField("new", Boolean.class), conf.getFieldId("process"));
				if (menuPath != null && menuPath.length > 0 && action != null) {
					entityMenuActions.add(Pair.makePair(Arrays.copyOf(menuPath, menuPath.length - 1), action));
					if (genericActions != null) {
						WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
						wa.setAction(GENERIC_ENTITY_ACTION);
						wa.putStringParameter("entity", meta.getEntity());
						wa.putStringParameter("process", conf.getField("process", String.class));
						wa.putBooleanParameter("new", conf.getField("new", Boolean.class));
						genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, action)));
					}
				}
				customConfigurationEntities.add(meta.getEntity());
			}
		}

		for (final EntityMetaDataVO entitymetavo : MetaDataClientProvider.getInstance().getAllEntities()) {
			if (customConfigurationEntities.contains(entitymetavo.getEntity())) {
				continue;
			}
			String[] menuPath = splitMenuPath(localeDelegate.getResource(entitymetavo.getLocaleResourceIdForMenuPath(), null));
			Action action = createEntityAction(entitymetavo, localeDelegate.getLabelFromMetaDataVO(entitymetavo), false, null);
			if (menuPath != null && menuPath.length > 0 && action != null) {
				entityMenuActions.add(Pair.makePair(menuPath, action));
				if (genericActions != null) {
					WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
					wa.setAction(GENERIC_ENTITY_ACTION);
					wa.putStringParameter("entity", entitymetavo.getEntity());
					genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, action)));
				}
			}
		}
		return entityMenuActions;
	}

	public List<Pair<String[], Action>> getNucletComponentMenuActions() {
		return getNucletComponentMenuActions(null);
	}

	private List<Pair<String[], Action>> getNucletComponentMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> nucletComponentMenuAction = new ArrayList<Pair<String[],Action>>();

		if (nucletComponentRepository != null) {
			for (MenuItem mi : nucletComponentRepository.getMenuItems()) {
				if (mi.isEnabled()) {
					String[] menuPath = mi.getMenuPath();
					Action action = mi.getAction();
					// If the component is not allowed to run (due to missing permissions), the action is disabled and skipped
					if (menuPath != null && menuPath.length > 0 && action != null && action.isEnabled()) {
						nucletComponentMenuAction.add(Pair.makePair(menuPath, action));
						if (genericActions != null) {
							WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
							wa.setAction(GENERIC_NUCLETCOMPONENT_MENUITEM_ACTION);
							wa.putStringParameter("nucletcomponent.menuitem", mi.getClass().getName());
							genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, action)));
						}
					}
				}
			}
		}

		return nucletComponentMenuAction;
	}

	public List<Pair<String[], Action>> getCustomComponentMenuActions() {
		return getCustomComponentMenuActions(null);
	}

	private List<Pair<String[], Action>> getCustomComponentMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> customComponentMenuAction = new ArrayList<Pair<String[], Action>>();
		for (CustomComponentVO ccvo : CustomComponentCache.getInstance().getAll()) {
			String[] menuPath = splitMenuPath(localeDelegate.getTextFallback(ccvo.getMenupathResourceId(), ccvo.getMenupathResourceId()));
			Action action = new ResPlanAction(ccvo);
			// If the component is not allowed to run (due to missing permissions), the action is disabled and skipped
			if (menuPath != null && menuPath.length > 0 && action != null && action.isEnabled()) {
				customComponentMenuAction.add(Pair.makePair(menuPath, action));
				if (genericActions != null) {
					WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
					wa.setAction(GENERIC_CUSTOMCOMPONENT_ACTION);
					wa.putStringParameter("customcomponent", "resPlan");
					genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, action)));
				}
			}
		};
		return customComponentMenuAction;
	}

	private void addSearchFilterActions(List<GenericAction> genericActions) {
		for (final EntitySearchFilter searchfilter : SearchFilterCache.getInstance().getAllEntitySearchFilters()) {
			Action action = new AbstractAction(searchfilter.getName(), MainFrame.resizeAndCacheIcon(
					NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish.06-magnify.png"), 16)) {
				@Override
				public void actionPerformed(ActionEvent e) {
					UIUtils.runCommand(frm, new CommonRunnable() {
						@Override
						public void run() throws CommonBusinessException {
							final String entity = searchfilter.getEntityName();
							if (Modules.getInstance().isModuleEntity(entity)) {
								final Integer iModuleId = Modules.getInstance().getModuleIdByEntityName(entity);
								final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
								newGenericObjectCollectController(iModuleId, null);
								ctlGenericObject.setSelectedSearchFilter(searchfilter);
								ctlGenericObject.runViewResults(searchfilter.getSearchCondition());
							}
							else {
								final MasterDataCollectController ctlMasterData = NuclosCollectControllerFactory.getInstance().
								newMasterDataCollectController(entity, null);
								ctlMasterData.setSelectedSearchFilter(searchfilter);
								ctlMasterData.runViewResults(searchfilter.getSearchCondition());
							}
						}
					});
				}
			};
			if (genericActions != null) {
				WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
				wa.setAction(GENERIC_SEARCHFILTER_ACTION);
				wa.putStringParameter("searchfilter", searchfilter.getName());
				genericActions.add(new GenericAction(wa, new ActionWithMenuPath(new String[]{
						localeDelegate.getMessage("nuclos.entity.searchfilter.label", "Suchfilter")}, action)));
			}
		}
	}

	private void addReportActions(List<GenericAction> genericActions) {
		try {
			for (final MasterDataVO mdReport : CollectionUtils.sorted(
					MasterDataDelegate.getInstance().getMasterData(NuclosEntity.REPORTEXECUTION.getEntityName(), ReportExecutionSearchStrategy.MAIN_CONDITITION),
					new Comparator<MasterDataVO>() {
						@Override
						public int compare(MasterDataVO o1, MasterDataVO o2) {
							return LangUtils.compareComparables(o1.getField("name", String.class), o2.getField("name", String.class));
						}
					})) {
				Action action = new AbstractAction(mdReport.getField("name", String.class), MainFrame.resizeAndCacheIcon(
						NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish.185-printer.png"), 16)) {
					@Override
					public void actionPerformed(ActionEvent e) {
						ReportExecutionCollectController.execReport(Main.getInstance().getMainFrame(), NuclosEntity.REPORTEXECUTION.getEntityName(), mdReport);
					}
				};
				if (genericActions != null) {
					WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
					wa.setAction(GENERIC_REPORT_ACTION);
					wa.putStringParameter("report", mdReport.getField("name", String.class));
					genericActions.add(new GenericAction(wa, new ActionWithMenuPath(new String[]{
							localeDelegate.getMessage("nuclos.entity.reportExecution.label", "Reporting ausfÃ¼hren")
							}, action)));
				}
			}
		} catch (Exception e) {
			Log.error(e.getMessage(), e);
		}
	}

	private Action createEntityAction(NuclosEntity entity) {
		EntityMetaDataVO entitymetavo = mdProv.getEntity(entity);
		return createEntityAction(entitymetavo, localeDelegate.getLabelFromMetaDataVO(entitymetavo), false, null);
	}

	private Action createEntityAction(EntityMetaDataVO entitymetavo, String label, final boolean isNew, final Long processId) {
		String entity = entitymetavo.getEntity();
		if (!securityCache.isReadAllowedForEntity(entity)) {
			return null;
		}
		
		if (isNew && entitymetavo.isStateModel() && !securityCache.isNewAllowedForModuleAndProcess(IdUtils.unsafeToId(entitymetavo.getId()), IdUtils.unsafeToId(processId))) {
			return null;
		}

		Action action = new AbstractAction() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				cmdCollectMasterData(evt, isNew, processId);
			}
		};
		Pair<String, Character> nameAndMnemonic = MenuGenerator.getMnemonic(label);
		action.putValue(Action.NAME, nameAndMnemonic.x);
		if (nameAndMnemonic.y != null) {
			action.putValue(Action.MNEMONIC_KEY, (int)nameAndMnemonic.y.charValue());
		}
		action.setEnabled(true);
		action.putValue(Action.SMALL_ICON, MainFrame.resizeAndCacheTabIcon(
				Main.getInstance().getMainFrame().getEntityIcon(entity)));
		action.putValue(Action.ACTION_COMMAND_KEY, entity);
		if (!isNew && processId == null) {
			if (!StringUtils.isNullOrEmpty(entitymetavo.getAccelerator()) && entitymetavo.getAcceleratorModifier() != null) {
				int keycode = entitymetavo.getAccelerator().charAt(0);
				if(keycode > 90)
					keycode -= 32;

				action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keycode, entitymetavo.getAcceleratorModifier().intValue()));
			} else if (!StringUtils.isNullOrEmpty(entitymetavo.getAccelerator())) {
				action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(entitymetavo.getAccelerator().charAt(0)));
			}
		}

		return action;
	}

	public List<Pair<String[], Action>> getTasklistMenuActions() {
		return getTasklistMenuActions(null);
	}

	private List<Pair<String[], Action>> getTasklistMenuActions(List<GenericAction> genericActions) {
		List<Pair<String[], Action>> result = new ArrayList<Pair<String[], Action>>();

		String miFile = localeDelegate.getText("miFile");
		String miTasklists = localeDelegate.getText("miTasklists");
		String[] mainPath = new String[] { miFile, miTasklists };

		for (TasklistDefinition def : TasklistCache.getInstance().getTasklists()) {
			Action action = new TasklistAction(def);
			result.add(Pair.makePair(mainPath, action));
			if (genericActions != null) {
				WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
				wa.setAction(GENERIC_TASKLIST_ACTION);
				wa.putStringParameter("name", def.getName());
				genericActions.add(new GenericAction(wa, new ActionWithMenuPath(mainPath, action)));
			}

			if (def.getMenupathResourceId() != null) {
				String[] menuPath = splitMenuPath(localeDelegate.getTextFallback(def.getMenupathResourceId(), def.getMenupathResourceId()));

				if (menuPath != null && menuPath.length > 0 && action != null) {
					result.add(Pair.makePair(menuPath, action));
					if (genericActions != null) {
						WorkspaceDescription.Action wa = new WorkspaceDescription.Action();
						wa.setAction(GENERIC_TASKLIST_ACTION);
						wa.putStringParameter("name", def.getName());
						genericActions.add(new GenericAction(wa, new ActionWithMenuPath(menuPath, action)));
					}
				}
			}
		};
		return result;
	}

	private static String[] splitMenuPath(String menuPath) {
		return (menuPath != null && !menuPath.isEmpty()) ? menuPath.split("\\\\") : null;
	}

	public void setupMenuBar(){
		frm.menuSetup(getCommandMap(), getComponentMap(), getNotificationDialog());
		LOG.info("Setup (refreshed) menu bar");
	}

	/**
	 * the main frame is about to close
	 */
	public void cmdWindowClosing(final ResultListener<Boolean> rl) {
		allControllersMayBeClosed(new ResultListener<Boolean>() {
			@Override
			public void done(Boolean result) {
				if (Boolean.TRUE.equals(result)) {
					try {

						frm.writeMainFramePreferences(prefs);
						restoreUtils.storeWorkspace(frm.getWorkspace());
						
						LocalUserCaches.getInstance().store();

						if (ctlTasks != null)	{
							ctlTasks.close();
						} else {
							LOG.debug("TaskController is null!");
						}

						LOG.debug("removes unused preferences...");
						removeUnusedPreferences();
						
						closeAllControllers();
					}
					catch (Exception ex) {
						final String sMessage = localeDelegate.getMessage("MainController.20","Die Sitzungsdaten, die Informationen \u00fcber die zuletzt ge\u00f6ffneten Fenster enthalten,\n" +
								"konnten nicht geschrieben werden. Bei der n\u00e4chsten Sitzung k\u00f6nnen nicht alle Fenster\n" +
								"wiederhergestellt werden. Bitte \u00f6ffnen Sie diese Fenster in der n\u00e4chsten Sitzung erneut.");
						Errors.getInstance().showExceptionDialog(frm, sMessage, ex);
					}
					catch (Error error) {
						LOG.error("Beim Beenden des Clients ist ein fataler Fehler aufgetreten.", error);
					}
					finally {
						// exit even on <code>Error</code>s, especially <code>NoClassDefFoundError</code>s,
						// which may result from installing a different version while a client is running.
						cmdExit();
					}

					rl.done(true);
				} else {
					rl.done(false);
				}
			}
		});
	}

	public static void cmdCycleThroughWindows(boolean forward) {
		//frm.cmdCycleThroughWindows(forward);
	}

	public void invalidateAllClientCaches() {
		Modules.getInstance().invalidate();
		MasterDataCache.getInstance().invalidate(null);
		MasterDataDelegate.getInstance().invalidateLayoutCache();
		MetaDataCache.getInstance().invalidate();
		
		// BOS problem with custom icon 
		MetaDataClientProvider.getInstance().revalidate();
		
		securityCache.revalidate();
		AttributeCache.getInstance().revalidate();
		GenericObjectLayoutCache.getInstance().invalidate();
		GeneratorActions.invalidateCache();
		resourceCache.invalidate();
		SearchFilterCache.getInstance().validate();
		GenericObjectDelegate.getInstance().invalidateCaches();
		LocaleDelegate.getInstance().flush();
		refreshMenus();
		refreshTaskController();
		frm.setTitle();
	}

	/**
	 * @deprecated Use setupMenuBar().
	 */
	public void refreshMenus() {
		setupMenuBar();
	}
	
	/**
	 * As setupMenuBar() is an extremly costly operation, we only do it once 
	 * within a grace period.
	 */
	public void refreshMenusLater() {
		Delayer.invokeLaterOnlyOnce(300L, refreshMenusLater);
	}

	public void refreshTaskController() {
		this.ctlTasks.refreshAllTaskViews();
	}

	protected void allControllersMayBeClosed(final ResultListener<Boolean> rl) {		
		if (mpActiveControllers.isEmpty()) {
			rl.done(true);
		} else {
			
			class Counter {
				boolean readyToReturn = false;
				int targetCount = 0;
				Map<TopController, Boolean> results = new HashMap<TopController, Boolean>();
				synchronized void addCount() {
					targetCount++;
				}
				synchronized void answered(TopController tc, boolean result) {
					results.put(tc, result);
					if (readyToReturn) {
						readyToReturn();
					}
				}
				synchronized void readyToReturn() {
					this.readyToReturn = true;
					if (targetCount == results.size()) {
						int trueCount = 0;
						for (TopController tcIter : results.keySet()) {
							if (results.get(tcIter)) {
								trueCount++;
							}
						}
						rl.done(targetCount == trueCount);
					}
				}
			}
			final Counter counter = new Counter();
			
			final Iterator<TopController> iter = mpActiveControllers.values().iterator();
			while (iter.hasNext()) {
				counter.addCount();
				final TopController ctl = iter.next();
				try {
					ctl.askAndSaveIfNecessary(true, new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {
							counter.answered(ctl, Boolean.TRUE.equals(result));
						}
					});
				} catch (Exception ex) {
					LOG.error("Error during askAndSaveIfNecessary: + " + ex.getMessage(), ex);
					counter.answered(ctl, false);
				}
			}
			counter.readyToReturn();
		}
	}

	private void closeAllControllers() throws IOException {
		for (TopController c: mpActiveControllers.values()) {
			c.close();
		}
	}

	/**
	 * @deprecated Not in use.
	 */
	private Collection<CollectController<Collectable>> getControllerForWritingPreferences() {
		Map<String, CollectController<Collectable>> mp = new HashMap<String, CollectController<Collectable>>();
		for(MainFrameTab tab : mpActiveControllers.keySet()) {
			TopController tp = mpActiveControllers.get(tab);
			if(tp instanceof CollectController<?>) {
				CollectController<Collectable> ctrl = (CollectController<Collectable>)tp;
				String sEntity = ctrl.getEntityName();
				if(mp.containsKey(sEntity)) {
					CollectController<Collectable> ctrl1 = mp.get(sEntity);
					if(ctrl1.getTab().isShowing()) {
						mp.put(sEntity, ctrl1);
					}
				}
				else {
					mp.put(sEntity, ctrl);
				}
			}
		}
		return mp.values();
	}

	/**
	 * @return List<CollectController> the CollectControllers for the internal frames,
	 * starting with the bottom frame in Z-order.
	 * @postcondition result != null
	 */
	private List<TopController> getTopControllersForInternalFrames() {
		final List<TopController> result = new LinkedList<TopController>();

		for (MainFrameTab ifrm : frm.getAllTabs()) {
			final TopController ctl = this.getControllerForTab(ifrm, TopController.class);
			if (ctl != null) {
				result.add(ctl);
			}
		}
		assert result != null;
		return result;
	}

	/**
	 * @param tab
	 * @return the <code>CollectController</code> (if any) for the given frame.
	 */
	public CollectController<?> getControllerForTab(MainFrameTab tab) {
		return getControllerForTab(tab, CollectController.class);
	}

	public <C extends TopController> C getControllerForTab(MainFrameTab tab, Class<? extends C> clazz) {
		Controller ctl = this.mpActiveControllers.get(tab);
		if (clazz.isInstance(ctl)) {
			return clazz.cast(ctl);
		}
		return null;
	}

	/**
	 * @param sEntityName
	 * @param iId
	 * @return the <code>CollectController</code>, if any, for the given entity, that is already displaying the
	 * object with the given id.
	 * @precondition sEntityName != null
	 * @precondition iId != null
	 */
	public CollectController<?> findCollectControllerDisplaying(String sEntityName, Object iId) {
		return (CollectController<?>) CollectionUtils.findFirst(this.getTopControllersForInternalFrames(),
			new IsCollectControllerDisplaying(sEntityName, iId));
	}

	public CollectController<?> findCollectControllerDisplaying(String sEntityName) {
		return (CollectController<?>) CollectionUtils.findFirst(this.getTopControllersForInternalFrames(),
			new IsCollectControllerDisplayingEntity(sEntityName));
	}

	/**
	 * Returns the first CustomComponentController for the given component name (configuration), or null.
	 */
	public TopController findCustomComponentControllerDisplaying(final String componentName) {
		return findCustomControllerDisplaying(componentName, CustomComponentController.class);
	}

	public <C extends TopController> C findCustomControllerDisplaying(final String componentName, final Class<C> clazz) {
		TopController ctl = CollectionUtils.findFirst(this.getTopControllersForInternalFrames(),
			new Predicate<TopController>() {
				@Override
				public boolean evaluate(TopController ctl) {
					if (clazz.isInstance(ctl)) {
						return componentName.equals(((CustomComponentController) ctl).getCustomComponentName());
					}
					return false;
				}
			});
		return clazz.cast(ctl);
	}

	public CollectController<?> findCollectControllerDisplayingDetails(String sEntityName) {
		return (CollectController<?>) CollectionUtils.findFirst(this.getTopControllersForInternalFrames(),
			new IsCollectControllerDisplayingDetails(sEntityName));
	}

	public void showDetails(String entityName, Long id) {
		showDetails(entityName, id.intValue());
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 * @precondition oId != null
	 */
	public void showDetails(String sEntityName, Object oId) {
		showDetails(sEntityName, oId, true);
	}

	public void showDetails(String sEntityName, Object oId, boolean newTab) {
		showDetails(sEntityName, oId, newTab, null);
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @param listeningController
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 * @precondition oId != null
	 */
	public void showDetails(final String sEntityName, final Object oId, final boolean newTab, final CollectController<?> listeningController, final CollectableEventListener... componentListener) {
		CollectController<?> ctl = null;
		boolean activateOnly = false;
		boolean stop = false;
		if (!newTab) {
			ctl = this.findCollectControllerDisplaying(sEntityName, oId);
			if (ctl == null) {
				ctl = this.findCollectControllerDisplayingDetails(sEntityName);
				if (ctl != null) {
					stop = true;
					final CollectController<?> ctlFinal = ctl;
					final boolean activateOnlyFinal = activateOnly;
					ctl.askAndSaveIfNecessary(new ResultListener<Boolean>() {
						@Override
						public void done(Boolean result) {
							if (Boolean.TRUE.equals(result)) {
								_showDetails(ctlFinal, activateOnlyFinal, sEntityName, oId, newTab, listeningController, componentListener);
							}
						}
					});
				}
				else {
					ctl = this.findCollectControllerDisplaying(sEntityName);
				}
			}
			else {
				activateOnly = true;
			}
		}
		if (stop) {
			return; 
		} else {
			_showDetails(ctl, activateOnly, sEntityName, oId, newTab, listeningController, componentListener);
		}
	}
	
	private void _showDetails(CollectController<?> ctl, boolean activateOnly, String sEntityName, Object oId, boolean newTab, CollectController<?> listeningController, CollectableEventListener... componentListener) {
		try {
			if (ctl == null) {
				ctl = NuclosCollectControllerFactory.getInstance().newCollectController(sEntityName, null);
			}
	
			if (listeningController != null) {
				ctl.addCollectableEventListener(new DetailsCollectableEventListener(listeningController, ctl));
			}
	
			for (CollectableEventListener l : componentListener) {
				ctl.addCollectableEventListener(l);
			}
	
			if (!activateOnly) {
				ctl.runViewSingleCollectableWithId(oId);
			}
	
			MainFrame.setSelectedTab(ctl.getTab());
	
			if (activateOnly) {
				ctl.getCollectStateModel().performVersionCheck();
			}
		} catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(getFrame(), ex);
		}
	}

	public void showList(String entity, List<Object> ids) throws CommonBusinessException {
		NuclosCollectController<?> controller = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null);
		controller.runViewResults(ids);
	}

//	public void showDetails(String entity, List<Object> ids) throws CommonBusinessException {
//		showDetails(entity, ids, false);
//	}
//
//	public void showDetails(final String entity, final List<Object> ids, boolean newTab) throws CommonBusinessException {
//		CollectController<?> ctl = null;
//		
//		boolean askAndWait = false;
//		if (!newTab) {
//			ctl = this.findCollectControllerDisplayingDetails(entity);
//			if (ctl != null) {
//				askAndWait = true;
//				final CollectController<?> ctlFinal = ctl;
//				ctl.askAndSaveIfNecessary(new ResultListener<Boolean>() {
//					@Override
//					public void done(Boolean result) {
//						if (Boolean.TRUE.equals(result)) {
//							CollectController<?> ctl2 = ctlFinal;
//							if (ctl2 == null) {
//								ctl2 = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null);
//							}
//							ctl2.runViewMultipleCollectablesWithIds(ids);
//						}
//					}
//				});
//			}
//		}
//		
//		if (!askAndWait) {
//			if (ctl == null) {
//				ctl = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null);
//			}
//			ctl.runViewMultipleCollectablesWithIds(ids);
//		}
//	}

	/**
	 * Open a new embedded (not in separate tab) dialog to create a new collectable.
	 * The embedded window will be closed later.
	 * @param entityname
	 * @param parent
	 * @param listener
	 * @throws CommonBusinessException
	 */
	public void showNew(String entityname, MainFrameTab parent, CollectableEventListener listener) throws CommonBusinessException {
		MainFrameTab tabIfAny = new MainFrameTab();
		final NuclosCollectController<?> controller = NuclosCollectControllerFactory.getInstance().newCollectController(entityname, tabIfAny);
		Main.getInstance().getMainController().initMainFrameTab(controller, tabIfAny);
		parent.add(tabIfAny);

		controller.addCollectableEventListener(listener);
		controller.addCollectableEventListener(new CollectableEventListener() {
			@Override
			public void handleCollectableEvent(Collectable collectable, MessageType messageType) {
				if (MessageType.NEW_DONE.equals(messageType)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							controller.getTab().dispose();
						}
					});
				}
			}
		});
		controller.runNew();
	}

	/**
	 * @param sEntityName
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 */
	public CollectController<? extends Collectable> showDetails(String sEntityName) throws CommonBusinessException {
		CollectController<? extends Collectable> clctcontroller = NuclosCollectControllerFactory.getInstance().newCollectController(sEntityName, null);
		clctcontroller.runNew();
		return clctcontroller;
	}

	private MainFrameTabbedPane getHomePane() {
		return this.getFrame().getHomePane();
	}

	/**
	 * ONLY FOR MIGRATION
	 * @param prefs
	 * @throws PreferencesException
	 * @throws BackingStoreException
	 */
	@Deprecated
	private void reopenAllControllers(Preferences prefs) throws PreferencesException, BackingStoreException {
		PreferencesUtils.getGenericList(prefs, PREFS_NODE_MDIWINDOWS, new TopControllerPreferencesIO());
	}

	/**
	 * exit, no doubt.
	 */
	private void cmdExit() {
		beforeExit();
		frm.dispose();
		Main.exit(Main.ExitResult.NORMAL);
	}

	/**
	 * this method cann be overwritten in project specific MainController. It will be called before window closing.
	 */
	protected void beforeExit() {
		//do nothing
	}

	protected MainFrame getFrame() {
		return this.frm;
	}

	private void showReleaseNotesIfNewVersion() {
		new ReleaseNotesController().showReleaseNotesIfNewVersion();
	}

	private void cmdCollectMasterData(final ActionEvent ev, final boolean isNew, final Long processId) {
		UIUtils.runCommand(frm, new Runnable() {
			@Override
			public void run() {
				try {
					String entity = ev.getActionCommand();
					NuclosCollectController<?> ncc = NuclosCollectControllerFactory.getInstance().newCollectController(entity, null);
					if(ncc != null) {
						if (processId != null && ncc instanceof GenericObjectCollectController) {
							GenericObjectCollectController gcc = (GenericObjectCollectController) ncc;
							gcc.setProcess(getProcessField(processId));
						}
						if (isNew) {
							ncc.runNew(true);
						}
						else {
							ncc.run();
						}
					}
				}
				catch (CommonBusinessException ex) {
					final String sErrorMsg = localeDelegate.getMessage(
							"MainController.21","Die Stammdaten k\u00f6nnen nicht bearbeitet werden.");
					Errors.getInstance().showExceptionDialog(frm, sErrorMsg, ex);
				}
			}
		});
	}

	private CollectableField getProcessField(Long processId) throws CommonBusinessException {
		return new CollectableValueIdField(processId.intValue(), MasterDataCache.getInstance().get(NuclosEntity.PROCESS.getEntityName(), processId.intValue()).getField("name"));
	}

	private void handleMessage(final Message msg) {
		try {
			if ((msg.getJMSCorrelationID() == null || msg.getJMSCorrelationID().equals(MainController.this.getUserName())) && msg instanceof ObjectMessage)
			{
				final Object objMessage = ((ObjectMessage) msg).getObject();

				if (objMessage.getClass().equals(RuleNotification.class)) {
					final RuleNotification notification = (RuleNotification) ((ObjectMessage) msg).getObject();
					getNotificationDialog().addMessage(notification);
					switch (notification.getPriority()) {
						case LOW:
							// just add the message, nothing else.
							break;
						case NORMAL:
							frm.getMessagePanel().startFlashing();
							break;
						case HIGH:
							getNotificationDialog().setVisible(true);
							break;
						default:
							LOG.warn("Undefined message priority: " + notification.getPriority());
					}
					LOG.info("Handled RuleNotification " + notification.toDescription());
				}
				else if (objMessage.getClass().equals(CommandMessage.class)) {
					final CommandMessage command = (CommandMessage) ((ObjectMessage) msg).getObject();
					switch (command.getCommand()) {
						case CommandMessage.CMD_SHUTDOWN :
							getNotificationDialog().addMessage(new RuleNotification(Priority.HIGH,
									localeDelegate.getMessage("MainController.19","Der Client wird auf Anweisung des Administrators in 10 Sekunden beendet."),
									"Administrator"));
							getNotificationDialog().setVisible(true);

							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep(10000);
									}
									catch (InterruptedException e) {
										// do nothing
									}
									finally {
										MainController.this.cmdExit();
									}
								}
							});
							break;
					}
					LOG.info("Handled CommandMessage " + command);
				} else if(objMessage.getClass().equals(CommandInformationMessage.class)) {
					final CommandInformationMessage command = (CommandInformationMessage)((ObjectMessage) msg).getObject();
					switch(command.getCommand()) {
						case CommandInformationMessage.CMD_INFO_SHUTDOWN :
							Object[] options = { "OK" };
							int decision = JOptionPane.showOptionDialog(frm, command.getInfo(), localeDelegate.getMessage(
									"MainController.17","Administrator - Passwort\u00e4nderung"),
									JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
							if (decision == 0 || decision == JOptionPane.CLOSED_OPTION || decision == JOptionPane.NO_OPTION) {
								  new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												Thread.sleep(10000);
											} catch (InterruptedException e) {
											  // do nothing
											} finally {
												MainController.this.cmdExit();
											}
										}
								  }, "MainController.handleMessage.cmdExit").run();
							}
						break;
					}
					LOG.info("Handled CommandInformationMessage " + command);
				}
			}
			else {
				LOG.warn(localeDelegate.getMessage(
						"MainController.14","Message of type {0} received, while an ObjectMessage was expected.", msg.getClass().getName()));
			}
		}
		catch (JMSException ex) {
			LOG.warn("Exception thrown in JMS message listener.", ex);
		}
	}

	/**
	 * specifies how to read/write 9a <code>TopController</code> from/to the preferences, so it can be restored
	 * when the application is restarted.
	 * Note that the dis
	 */
	private class TopControllerPreferencesIO implements PreferencesUtils.PreferencesIO<TopController> {

		static final String PREFS_KEY_CUSTOMCOMP = "customcomponent";

		@Override
		public TopController get(Preferences prefs) throws PreferencesException {
			String customComponent = prefs.get(PREFS_KEY_CUSTOMCOMP, null);
			if (customComponent != null) {
				CustomComponentController ctl = CustomComponentController.newController(customComponent);
				ctl.run();
				return ctl;
			} else {
				return NuclosCollectController.createFromPreferences(prefs);
			}
		}

		@Override
		public void put(Preferences prefs, TopController ctl) throws PreferencesException {
			throw new NotImplementedException("@deprecated: Use TabRestoreController");
		}
	}

	/**
	 * inner class IsDisplaying
	 */
	private static class IsCollectControllerDisplaying implements Predicate<TopController> {
		
		private final String sEntityName;
		private final Object iId;

		/**
		 * @param sEntityName
		 * @param iId
		 * @precondition sEntityName != null
		 * @precondition iId != null
		 */
		IsCollectControllerDisplaying(String sEntityName, Object iId) {
			this.sEntityName = sEntityName;
			this.iId = iId;
		}

		@Override
		public boolean evaluate(TopController ctl) {
			if (ctl instanceof CollectController) {
				CollectController<?> clctctl = (CollectController<?>) ctl;
				return this.sEntityName.equals(clctctl.getEntityName()) &&
					clctctl.getCollectState().isDetailsModeViewOrEdit() &&
					this.iId.equals(clctctl.getSelectedCollectableId());
			}
			return false;
		}
	}

	private static class IsCollectControllerDisplayingDetails implements Predicate<TopController> {
		private final String sEntityName;

		/**
		 * @param sEntityName
		 * @precondition sEntityName != null
		 */
		IsCollectControllerDisplayingDetails(String sEntityName) {
			this.sEntityName = sEntityName;
		}

		@Override
		public boolean evaluate(TopController ctl) {
			if (ctl instanceof CollectController) {
				CollectController<?> clctctl = (CollectController<?>) ctl;
				return this.sEntityName.equals(clctctl.getEntityName()) &&
					(clctctl.getCollectState().isDetailsModeViewOrEdit() || clctctl.getCollectState().isDetailsModeMultiViewOrEdit());
			}
			return false;
		}
	}

	private static class IsCollectControllerDisplayingEntity implements Predicate<TopController> {
		private final String sEntityName;

		/**
		 * @param sEntityName
		 * @precondition sEntityName != null
		 */
		IsCollectControllerDisplayingEntity(String sEntityName) {
			this.sEntityName = sEntityName;
		}

		@Override
		public boolean evaluate(TopController ctl) {
			if (ctl instanceof CollectController) {
				CollectController<?> clctctl = (CollectController<?>) ctl;
				return this.sEntityName.equals(clctctl.getEntityName());
			}
			return false;
		}
	}

	/**
	 * inner class DirectHelpActionListener
	 */
	private class DirectHelpActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent actionevent) {
			throw new UnsupportedOperationException("Direct help is not supported");

//			Cursor cursor = (Cursor) UIManager.get("HelpOnItemCursor");
//			if (cursor == null)
//				cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
//
//			<Set cursor, track mouse events, reset cursor>
//			Point mousePosition = ...;
//
//			Component fundComponent = frm.findComponentAt(mousePosition);
//			CollectController<?> clctctrl = getControllerForInternalFrame(UIUtils
//					.getInternalFrameForComponent(fundComponent));
//
//			try {
//				WikiController wikiCtrl = WikiController.getInstance();
//				wikiCtrl.openURLinBrowser(wikiCtrl.getWikiPageForComponent(fundComponent, clctctrl));
//				// --- WIKI Aufruf ---
//
//			} catch (Exception e) {
//				Errors.getInstance().showExceptionDialog(frm, SpringLocaleDelegate.getMessage("MainController.22","Keine Information \u00fcber das Feld verf\u00fcgbar"), e);
//			}
		}
	} //DirectHelpActionListener

	private static class MemoryMonitor extends JPanel implements Runnable {

		private JLabel  content;
		private JButton gc;
		private long inuse;
		private long total;
		private long free;

		public MemoryMonitor() {
			super(new BorderLayout());
			content = new JLabel("initiating MeMo...");
			Dimension d = content.getPreferredSize();
			content.setMaximumSize(d);
			content.setMinimumSize(d);
			content.setHorizontalTextPosition(JLabel.RIGHT);
			content.setForeground(Color.WHITE);
			content.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

			add(content, BorderLayout.CENTER);
			gc = new JButton(gcAction);
			gc.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
			add(gc, BorderLayout.EAST);

			validate();
			setOpaque(false);
			setMaximumSize(getPreferredSize());
			setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

			Thread dm = new Thread(this, "MainController.MemoryMonitor");
			dm.setDaemon(true);
			dm.start();
		}

		private AbstractAction gcAction = new AbstractAction("GC") {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.gc();
			}
		};

		@Override
		public void run() {
			while(true) {
				try {
					Thread.sleep(2000);
					Runtime r = Runtime.getRuntime();
					total = r.totalMemory();
					free = r.freeMemory();
					inuse = total - free;
					SwingUtilities.invokeLater(swingUpd);
				}
				catch(Exception e) {
					LOG.warn("MemoryMonitor.run: " + e, e);
				}
			}
		}

		private Runnable swingUpd = new Runnable() {
			@Override
			public void run() {
				try {
					content.setText(String.format("%,.2f KB", inuse / 1024.0));
					content.setToolTipText("<html><b>Free</b>: " + String.format("%,.2f KB", free / 1024.0) + "<br /><b>Total</b>: " + String.format("%,.2f KB", total / 1024.0) + "</html>");
				}
				catch (Exception e) {
					LOG.error("swingUpd failed: " + e, e);
				}
			}
		};
	}

	public TaskController getTaskController() {
		return this.ctlTasks;
	}

	/**
	 * - cleanup old preferences.
	 * - not in use any more.
	 */
	private void removeUnusedPreferences() {
		getExplorerController().prefs.remove(ExplorerController.PREFS_NODE_EXPLORERVIEWS_XML);
		getExplorerController().prefs.remove(ExplorerController.PREFS_NODE_EXPLORERVIEWS);
		getExplorerController().prefs.remove(ExplorerController.PREFS_NODE_EXPLORER_EXPANDEDPATHS);

		Preferences settingsPrefs = ClientPreferences.getUserPreferences().node("explorer").node("settings");
		settingsPrefs.remove("showExplorerMode");
		settingsPrefs.remove("showTaskpanelMode");
		settingsPrefs.remove("transparencyLevel");

		try {
			ClientPreferences.getUserPreferences().node(PREFS_NODE_MDIWINDOWS).removeNode();
		} catch(BackingStoreException e) {
			LOG.error("removeUnusedPreferences failed: " + e, e);
		}
	}

	public String getMainMenuFile() {
		return localeDelegate.getMessage("miFile", "Datei").replace("^", "");
	}
	
	public String getMainMenuHelp() {
		return localeDelegate.getMessage("miHelp", "Hilfe").replace("^", "");
	}

	public String getMainMenuAdministration() {
		return localeDelegate.getMessage("MainMenuAdministration", "Administration").replace("^", "");
	}

	public String getMainMenuConfiguration() {
		return localeDelegate.getMessage("MainMenuConfiguration", "Konfiguration").replace("^", "");
	}

	public Preferences getMainFramePreferences() {
		return prefs;
	}

}	// class MainControlle
