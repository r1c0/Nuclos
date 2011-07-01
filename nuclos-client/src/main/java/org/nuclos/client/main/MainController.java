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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
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
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.application.assistant.NuclosStartupPanel;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectController;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.common.TopicNotificationReceiver;
import org.nuclos.client.common.prefs.WebAccessPrefs;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.console.NuclosConsoleGui;
import org.nuclos.client.customcomp.CustomComponentCache;
import org.nuclos.client.customcomp.CustomComponentController;
import org.nuclos.client.customcomp.resplan.ResPlanAction;
import org.nuclos.client.customcomp.wizard.CustomComponentWizard;
import org.nuclos.client.explorer.ExplorerController;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.help.HtmlPanel;
import org.nuclos.client.help.internalinfo.InternalInfoController;
import org.nuclos.client.help.releasenotes.ReleaseNotesController;
import org.nuclos.client.login.LoginController;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.workspace.RestoreUtils;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.relation.EntityRelationShipCollectController;
import org.nuclos.client.report.reportrunner.AbstractReportExporter;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.task.TaskController;
import org.nuclos.client.ui.ClipboardUtils;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.TopController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectStateModel;
import org.nuclos.client.ui.collect.DetailsCollectableEventListener;
import org.nuclos.client.wiki.WikiController;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.common.Actions;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.CommandInformationMessage;
import org.nuclos.common.CommandMessage;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.Priority;
import org.nuclos.common.RuleNotification;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.ejb3.TestFacadeRemote;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
 * The main controller for the Nucleus client. Controller of the <code>MainFrame</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class MainController {
	private static final Logger log = Logger.getLogger(MainController.class);

	/**
	 * the preferences key for the frame state.
	 */
	private static final String PREFS_NODE_MAINFRAME = "mainFrame";
	@Deprecated
	private static final String PREFS_NODE_MDIWINDOWS = "mdiWindows";

	private final String sUserName;
	private final String sNuclosServerName;

	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_MAINFRAME);

	private static MainFrame frm;

	private SwingDebugFrame debugFrame = new SwingDebugFrame(this);

	/**
	 * controller for explorer panel
	 */
	private ExplorerController ctlExplorer;

	/**
	 * controller for task panel
	 */
	protected TaskController ctlTasks;

	private final NuclosNotificationDialog notificationdlg;

	/**
	 * maps opened internal frames to their controllers.
	 */
	private final Map<MainFrameTab, TopController> mpActiveControllers = CollectionUtils.newHashMap();

	private final MessageListener messagelistener = new MessageListener() {
		@Override
		public void onMessage(final Message msg) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					handleMessge(msg);
				}
			});
		}
	};

	private DirectHelpActionListener dha = new DirectHelpActionListener();



	/**
	 * @param sUserName name of the logged in user
	 * @param sNuclosServerName name of the Nucleus server connected to.
	 * @throws BackingStoreException
	 */
	public MainController(String sUserName, String sNuclosServerName, final LoginController loginController) throws CommonPermissionException, BackingStoreException {

		this.sUserName = sUserName;
		this.sNuclosServerName = sNuclosServerName;
		/** @todo this is a workaround - because Main.getMainController() is called to get the user name */
		Main.setMainController(this);

		log.debug(">>> read user rights...");
		SecurityCache.initialize();
		loginController.increaseLoginProgressBar(StartUp.PROGRESS_INIT_SECURITYCACHE);

		if (!SecurityCache.getInstance().isActionAllowed(Actions.ACTION_SYSTEMSTART)) {
			throw new CommonPermissionException(CommonLocaleDelegate.getMessage("MainController.23", "Sie haben nicht das Recht, {0} zu benutzen.", ApplicationProperties.getInstance().getName()));
		}

		loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_ATTRIBUTES);

		DefaultCollectableEntityProvider.setInstance(NuclosCollectableEntityProvider.getInstance());

		Thread threadGenericObjectMetaDataCache = new Thread() {

			@Override
			public void run() {
				log.debug(">>> read metadata...");
				GenericObjectMetaDataCache.getInstance();
			}
		};

		loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_LOMETA);

		Thread threadSearchFilterCache = new Thread() {

			@Override
			public void run() {
				log.debug(">>> read searchfilter...");
				SearchFilterCache.getInstance();
			}
		};

		loginController.increaseLoginProgressBar(StartUp.PROGRESS_READ_SEARCHFILTER);

		List<Thread> lstCacheThreads = new ArrayList<Thread>();
		lstCacheThreads.add(threadGenericObjectMetaDataCache);
		lstCacheThreads.add(threadSearchFilterCache);
		threadGenericObjectMetaDataCache.start();
		threadSearchFilterCache.start();

		for(Thread t : lstCacheThreads) {
			try {
				t.join();
			}
			catch(InterruptedException e) {
				// do noting here
			}
		}

		log.debug(">>> create mainframe...");
		this.frm = new MainFrame(this.getUserName(), this.getNuclosServerName());

		this.frm.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.frm.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				cmdWindowClosing();
			}
		});
		loginController.increaseLoginProgressBar(StartUp.PROGRESS_CREATE_MAINFRAME);

		log.debug(">>> init client communication...");
		this.notificationdlg = new NuclosNotificationDialog(this.frm, this.getDesktopPane());
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_RULENOTIFICATION, messagelistener);
		loginController.increaseLoginProgressBar(StartUp.PROGRESS_INIT_NOTIFICATION);

		log.debug(">>> setup menus...");
		this.setupMenus();
		loginController.increaseLoginProgressBar(StartUp.PROGRESS_CREATE_MAINMENU);

		log.debug(">>> create explorer controller...");
		this.ctlExplorer = new ExplorerController(frm);

		log.debug(">>> create task controller...");
		this.ctlTasks = new TaskController(frm, getUserName());

		this.ctlTasks.setExplorerController(ctlExplorer);
		this.ctlExplorer.setTaskController(ctlTasks);

		log.debug(">>> restore last workspace...");
		try {
			MainFrame.readMainFramePreferences(prefs);
			RestoreUtils.restoreWorkspaceThreaded(MainFrame.getLastWorkspaceFromPreferences());
		}
		catch (Exception ex) {
			final String sMessage = CommonLocaleDelegate.getMessage("MainController.4","Die in der letzten Sitzung ge\u00f6ffneten Fenster konnten nicht wiederhergestellt werden.");
			Errors.getInstance().showExceptionDialog(null, sMessage, ex);
		}
		finally {
			loginController.increaseLoginProgressBar(StartUp.PROGRESS_RESTORE_WORKSPACE);
		}

		log.debug(">>> show mainFrame...");
		frm.setVisible(true);

		try {
			log.debug(">>> restore last controllers (for migration only)...");
			reopenAllControllers(ClientPreferences.getUserPreferences());
		}
		catch (Exception ex) {
			final String sMessage = CommonLocaleDelegate.getMessage("MainController.4","Die in der letzten Sitzung ge\u00f6ffneten Fenster konnten nicht wiederhergestellt werden.");
			Errors.getInstance().showExceptionDialog(null, sMessage, ex);
		}

		log.debug(">>> restore task views (for migration only)...");
		try {
			ctlTasks.restoreGenericObjectTaskViewsFromPreferences();
		}
		catch (Exception ex) {
			final String sMessage = CommonLocaleDelegate.getMessage("tasklist.error.restore", "Die Aufgabenlisten konnten nicht wiederhergestellt werden.");
			log.error(sMessage, ex);
			Errors.getInstance().showExceptionDialog(null, sMessage, ex);
		}

		Thread theadExplorerController = new Thread() {
			@Override
			public void run() {
				try {
					log.debug(">>> restore explorer views (for migration only)");
					ctlExplorer.restoreExplorerViewsFromPreferences();
				}
				catch (Exception ex) {
					final String sMessage = CommonLocaleDelegate.getMessage("MainController.2","Die Fenster der Explorerleiste konnten nicht wiederhergestellt werden.");
					log.error(sMessage, ex);
					Errors.getInstance().showExceptionDialog(null, sMessage, ex);
				}
			}
		};
		theadExplorerController.start();

		Thread theadTaskController = new Thread() {
			@Override
			public void run() {
				log.debug(">>> refresh tasks...");
				ctlTasks.run();
			}
		};
		theadTaskController.start();

		// Show the release notes for this version, if the user hasn't seen it yet.
		showReleaseNotesIfNewVersion();

		// Show the internal informations if they have changed since the last start of the client
		showInternalInfoIfChanged();

		// Show startup panel to configure an application
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				showStartupPanel(true);
			}
		});

		// Debug purposes
		final String sKeyWindowShow = "CtlShiftF11";
		frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)), sKeyWindowShow);
		frm.getRootPane().getActionMap().put(sKeyWindowShow, new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {
				debugFrame.showComponentDetails(frm.findComponentAt(frm.getMousePosition()));
			}
		});

		//Call wikipage
		final String sKeyWikiShow = "CtlShiftF1";
		frm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, (KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK)), sKeyWikiShow);
		frm.getRootPane().getActionMap().put(sKeyWikiShow, new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent ev) {

//				Component comp = frm.findComponentAt(frm.getMousePosition());
				Component fundComponent = frm.getFocusOwner() != null ? frm.getFocusOwner() : frm.findComponentAt(frm.getMousePosition());
				CollectController<?> clctctrl = getControllerForInternalFrame(UIUtils.getInternalFrameForComponent(fundComponent));

				WikiController wikiCtrl = WikiController.getInstance();
				wikiCtrl.openURLinBrowser(wikiCtrl.getWikiPageForComponent(fundComponent, clctctrl));

			}
		});
	}

	public void addForcedContent() {
		ctlTasks.addForcedFilters();
	}

	private Action cmdDirectHelp = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			dha.actionPerformed(e);
		}
	};

	private Action cmdShowTimelimitTasks = new AbstractAction(
		CommonLocaleDelegate.getMessage("miShowTimelimitTasks","Fristen anzeigen"),
		Icons.getInstance().getIconTabTimtlimit()) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			MainController.this.getTaskController().getTimelimitTaskController().cmdShowTimelimitTasks();
		}
		@Override
		public boolean isEnabled() {
			return SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TIMELIMIT_LIST);
		}
	};

	private Action cmdShowPersonalTasks = new AbstractAction(
		CommonLocaleDelegate.getMessage("miShowPersonalTasks","Meine Aufgaben anzeigen"),
		Icons.getInstance().getIconTabTask()) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			MainController.this.getTaskController().getPersonalTaskController().cmdShowPersonalTasks();
		}
		@Override
		public boolean isEnabled() {
			return SecurityCache.getInstance().isActionAllowed(Actions.ACTION_TASKLIST);
		}
	};

	private Action cmdShowPersonalSearchFilters = new AbstractAction(
		CommonLocaleDelegate.getMessage("ExplorerPanel.3","Meine Suchfilter anzeigen"),
		Icons.getInstance().getIconFilter16()) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			MainController.this.getExplorerController().cmdShowPersonalSearchFilters();
		}
	};

	private Action cmdChangePassword = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			ChangePasswordPanel panel = new ChangePasswordPanel();
			if(JOptionPane.showConfirmDialog(getFrame(), panel,
				CommonLocaleDelegate.getMessage("MainController.1","Benutzerpasswort \u00e4ndern"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
			  try {
				  MainController.this.prefs.flush();
				  SecurityDelegate.getInstance().changePassword(panel.getOldPassword(), panel.getNewPassword());
				  LocalUserProperties props = LocalUserProperties.getInstance();
				  props.setUserPasswd("");
				  props.store();
				} catch (NuclosBusinessException e) {
					Errors.getInstance().showExceptionDialog(panel, e);
				} catch (BackingStoreException e) {
					Errors.getInstance().showExceptionDialog(panel, e);
				}
			}
		}};

	private Action cmdOpenManagementConsole = new AbstractAction(
		CommonLocaleDelegate.getMessage("miManagementConsole", "Management Console"),
		MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.158-wrench-2.png"))) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			UIUtils.runCommand(frm, new Runnable() {
				@Override
				public void run() {
					NuclosConsoleGui.showInFrame(frm.getHomePane());
				}
			});
		}};

	private Action cmdOpenEntityWizard = new AbstractAction(
			CommonLocaleDelegate.getMessage("miEntityWizard", "Entity Wizard"),
			MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.81-dashboard.png"))) {
		/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			UIUtils.runCommand(frm, new Runnable() {
				@Override
				public void run() {
					ShowNuclosWizard w = new ShowNuclosWizard(false);
					w.showWizard(MainController.this.getDesktopPane(), MainController.this.getFrame());
				}
			});
		}};

	private Action cmdOpenCustomComponentWizard = new AbstractAction(
			CommonLocaleDelegate.getMessage("miResPlanWizard", "Ressourcenplanung"),
			MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.83-calendar.png"))) {
		/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(final ActionEvent evt) {
			UIUtils.runCommand(frm, new Runnable() {
				@Override
				public void run() {
					CustomComponentWizard.run();
				}
			});
		}};

	private Action cmdOpenRelationEditor = new AbstractAction(
		CommonLocaleDelegate.getMessage("miRelationEditor", "Relationeneditor"),
		MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.55-network.png"))) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				UIUtils.runCommand(frm, new Runnable() {
					@Override
					public void run() {
						try {

							Collection<MasterDataVO> colRelation = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ENTITYRELATION.getEntityName());
							EntityRelationShipCollectController result = new EntityRelationShipCollectController(MainController.this.getFrame().getHomePane(), MainController.this.getFrame(), null);
							if(colRelation.size() > 0) {
								MasterDataVO vo = colRelation.iterator().next();
								result.runViewSingleCollectableWithId(vo.getId());
							}
							else {

								result.runNew();
							}

						}
						catch(CommonBusinessException e1) {
						}
						finally {

						}
					}
				});
			}};

	private Action cmdOpenNucletWizard = new AbstractAction(
		CommonLocaleDelegate.getMessage("miNucletWizard", "Nuclet Wizard"),
		MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.73-radar.png"))) {
		/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			UIUtils.runCommand(frm, new Runnable() {
				@Override
				public void run() {
					showStartupPanel(false);
				}
			});
		}};

	private Action cmdOpenSettings = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			cmdOpenSettings();
		}
	};

	public static void cmdOpenSettings() {
		NuclosSettingsContainer panel = new NuclosSettingsContainer(frm);

		JOptionPane p = new JOptionPane(panel,
			JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION,	null);
		JDialog dlg = p.createDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("R00022927", "Einstellungen"));
		dlg.pack();
		dlg.setResizable(true);
		dlg.setVisible(true);
		Object o = p.getValue();
		int res = ((o instanceof Integer)
			? ((Integer) o).intValue()
				: JOptionPane.CANCEL_OPTION);

		if(res != JOptionPane.OK_OPTION)
			panel.restoreSettings();
	}

	public Action cmdRefreshClientCaches = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			UIUtils.runCommandLater(getFrame(), new CommonRunnable() {
				@Override
				public void run() throws CommonBusinessException {
					invalidateAllClientCaches();
					JOptionPane.showMessageDialog(getFrame(),
						CommonLocaleDelegate.getMessage("MainController.3","Die folgenden Aktionen wurden erfolgreich durchgef\u00fchrt:\n" +
							"Caches aktualisiert: MasterDataCache, SecurityCache, AttributeCache, GenericObjectLayoutCache, GeneratorCache, MetaDataCache, ResourceCache, SearchFilterCache.\n"+
						"Men\u00fcs aktualisiert."));
				}
			});
		}
	};

	private Action cmdSelectAll = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			// select all rows in the Result panel of the current CollectController (if any):
			final MainFrameTab ifrm = (MainFrameTab) MainController.this.frm.getHomePane().getSelectedComponent();
			if (ifrm != null) {
				final CollectController<?> ctl = getControllerForInternalFrame(ifrm);
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

	private Action cmdHelpContents = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			WikiController.getInstance().openURLinBrowser(ClientParameterProvider.getInstance().getValue(ClientParameterProvider.KEY_WIKI_STARTPAGE));
		}
	};

	private Action cmdShowAboutDialog  = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			cmdShowAboutDialog();
		}
	};

	public static void cmdShowAboutDialog() {
		try {
			final MainFrameTab internalFrame = MainController.newMainFrameTab(null, "Info");
			String html = IOUtils.readFromTextStream(Main.getMainController().getClass().getClassLoader().getResourceAsStream("org/nuclos/client/help/about/about.html"), null);
			HtmlPanel htmlPanel = new HtmlPanel(
				String.format(
					html,
					ApplicationProperties.getInstance().getCurrentVersion(), // %1$s
					Main.getMainController().getUserName(),                                           // %2$s
					Main.getMainController().getNuclosServerName(),                                   // %3$s
					System.getProperty("java.version")                       // %4$s
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
						} catch(IOException ex) {
							ex.printStackTrace();
						} catch(URISyntaxException ex) {
							ex.printStackTrace();
						}
					}
				}
			});
			internalFrame.setLayeredComponent(htmlPanel);
			Main.getMainFrame().getHomePane().add(internalFrame);
			internalFrame.setVisible(true);
        }
        catch(Exception e) {
        	Errors.getInstance().showExceptionDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("MainController.26", "Die Infos k\u00f6nnen nicht angezeigt werden."), e);
        }
	}

	private Action cmdShowProjectReleaseNotes  = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			new ReleaseNotesController(getFrame(), getDesktopPane()).showReleaseNotes(ApplicationProperties.getInstance().getName());
		}
	};

	private Action cmdShowNuclosReleaseNotes  = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent evt) {
			ReleaseNotesController.openReleaseNotesInBrowser();
		}
	};

	private Action cmdWindowClosing = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			cmdWindowClosing();
		}
	};

	private Action cmdLogoutExit = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			cmdLogoutExit();
		}
	};

	public static void cmdLogoutExit() {
		LocalUserProperties props = LocalUserProperties.getInstance();
		props.setUserPasswd("");
		props.store();
		Main.getMainController().cmdWindowClosing();
	}

	private Action cmdShowInternalInfo = new AbstractAction() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent e) {
			cmdShowInternalInfo();
		}
	};

	private Action cmdExecuteRport = createEntityAction(NuclosEntity.REPORTEXECUTION);

   private static class UIDefTableModel extends AbstractTableModel {
      /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
      /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

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
		mainController.put("cmdOpenNucletWizard", cmdOpenNucletWizard);
		//mainController.put("cmdRefreshClientCaches", cmdRefreshClientCaches);
		mainController.put("cmdSelectAll", cmdSelectAll);
		mainController.put("cmdHelpContents", cmdHelpContents);
		mainController.put("cmdShowAboutDialog", cmdShowAboutDialog);
		mainController.put("cmdShowProjectReleaseNotes", cmdShowProjectReleaseNotes);
		mainController.put("cmdShowNuclosReleaseNotes", cmdShowNuclosReleaseNotes);
		mainController.put("cmdLogoutExit", cmdLogoutExit);
		mainController.put("cmdWindowClosing", cmdWindowClosing);
		mainController.put("cmdShowInternalInfo", cmdShowInternalInfo);
		mainController.put("cmdExecuteRport", cmdExecuteRport);

		for(Method m : getClass().getDeclaredMethods()) {
			if(m.getName().startsWith("cmd")) {
				Class<?>[] pt = m.getParameterTypes();
				if(pt.length == 0 || (pt.length == 1 && pt[0].isAssignableFrom(ActionEvent.class))) {
					final Method fm = m;
					Action a = new AbstractAction(m.getName()) {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

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
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent e) {
					String s = JOptionPane.showInputDialog(frm, "Topic: Message");
					if(s == null)
						return;
					String[] a = s.split(": *");
					if(a.length == 2) {
						ServiceLocator.getInstance().getFacade(TestFacadeRemote.class).testClientNotification(a[0], a[1]);
					}
					else {
						JOptionPane.showMessageDialog(frm, "Wrong input format");
					}
				}
			});

		dev.put("webPrefs", new AbstractAction("Test Web Prefs-Access") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String s = JOptionPane.showInputDialog(frm, "Access-Path");
				if(s == null)
					return;
				WebAccessPrefs p = new WebAccessPrefs();
				try {
					Map<String, String> m = p.getPrefsMap(s);
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					AbstractReportExporter.checkJawin();
					JOptionPane.showMessageDialog(Main.getMainFrame(), "Jawin ok");
				} catch (Exception ex) {
					Errors.getInstance().showDetailedExceptionDialog(Main.getMainFrame(), ex);
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

		dev.put("memoryMonitor", new MemoryMonitor());
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
	public static MainFrameTab newMainFrameTab(TopController ctl) {
		return newMainFrameTab(ctl, null);
	}

	/**
	 * @param ctl may be <code>null</code>.
	 * @param sTitle
	 */
	public static MainFrameTab newMainFrameTab(TopController ctl, String sTitle) {
		final MainFrameTab result = new MainFrameTab(sTitle);
		initMainFrameTab(ctl, result);
		return result;
	}

	public static void initMainFrameTab(TopController ctl, MainFrameTab tab) {
		Object lock = new Object();
		synchronized(lock) {
			ImageIcon icon = ctl != null ? ctl.getIcon() : null;
			tab.setTabIcon(icon != null ? icon : Icons.getInstance().getIconTabGeneric());
			addMainFrameTabListener(tab, ctl);
		}
	}

	/**
	 * @param tab
	 * @param ctl may be <code>null</code>.
	 */
	private static void addMainFrameTabListener(final MainFrameTab tab, final TopController ctl) {
		tab.addMainFrameTabListener(new MainFrameTabAdapter() {

			@Override
			public void tabAdded(MainFrameTab tab) {
				Main.getMainController().addMainFrameTab(tab, ctl);
			}

			@Override
			public void tabClosed(MainFrameTab tab) {
				Main.getMainController().removeMainFrameTab(tab);
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

	public List<Pair<String[], Action>> getAdministrationMenuActions() {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuAdministration()};

		addActionIfAllowed(menuActions, menuPath, NuclosEntity.USER);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.ROLE);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.SEARCHFILTER);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.JOBCONTROLLER);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.PARAMETER);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.LDAPSERVER);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.WEBSERVICE);

		if (SecurityCache.getInstance().isActionAllowed("UseManagementConsole")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenManagementConsole));
		}

		return menuActions;
	}

	public List<Pair<String[], Action>> getConfigurationMenuActions() {
		List<Pair<String[], Action>> menuActions = new ArrayList<Pair<String[],Action>>();

		final String[] menuPath = new String[] {getMainMenuConfiguration()};

		if (SecurityCache.getInstance().isActionAllowed("EntityWizard")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenEntityWizard));
		}

		addActionIfAllowed(menuActions, menuPath, NuclosEntity.LAYOUT);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.STATEMODEL);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.GENERATION);

		if (SecurityCache.getInstance().isActionAllowed("RelationEditor")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenRelationEditor));
		}
		if (SecurityCache.getInstance().isActionAllowed("ResPlanWizard")) {
			menuActions.add(new Pair<String[], Action>(menuPath, cmdOpenCustomComponentWizard));
		}

		addActionIfAllowed(menuActions, menuPath, NuclosEntity.REPORT);
		addActionIfAllowed(menuActions, menuPath, NuclosEntity.NUCLET);

		return menuActions;
	}

	private void addActionIfAllowed(List<Pair<String[], Action>> menuActions, String[] menuPath, NuclosEntity entity) {
		Action act = createEntityAction(MetaDataClientProvider.getInstance().getEntity(entity));
		if (act != null) {
			menuActions.add(new Pair<String[], Action>(menuPath, act));
		}
	}

	public List<Pair<String[], Action>> getEntityMenuActions() {
		List<Pair<String[], Action>> entityMenuActions = new ArrayList<Pair<String[], Action>>();
		for (final EntityMetaDataVO entitymetavo : MetaDataDelegate.getInstance().getAllEntities()) {
			String[] menuPath = splitMenuPath(CommonLocaleDelegate.getResource(entitymetavo.getLocaleResourceIdForMenuPath(), null));
			Action action = createEntityAction(entitymetavo);
			if (menuPath != null && menuPath.length > 0 && action != null) {
				entityMenuActions.add(Pair.makePair(menuPath, action));
			}
		}
		return entityMenuActions;
	}

	public List<Pair<String[], Action>> getCustomComponentMenuActions() {
		List<Pair<String[], Action>> customComponentMenuAction = new ArrayList<Pair<String[], Action>>();
		for (CustomComponentVO ccvo : CustomComponentCache.getInstance().getAll()) {
			String[] menuPath = splitMenuPath(ccvo.getDefaultMenuPath());
			Action action = new ResPlanAction(ccvo);
			// If the component is not allowed to run (due to missing permissions), the action is disabled and skipped
			if (menuPath != null && menuPath.length > 0 && action != null && action.isEnabled()) {
				customComponentMenuAction.add(Pair.makePair(menuPath, action));
			}
		};
		return customComponentMenuAction;
	}

	private Action createEntityAction(NuclosEntity entity) {
		return createEntityAction(MetaDataClientProvider.getInstance().getEntity(entity));
	}

	private Action createEntityAction(EntityMetaDataVO entitymetavo) {
		String entity = entitymetavo.getEntity();
		if (!SecurityCache.getInstance().isReadAllowedForEntity(entity)) {
			return null;
		}

		Action action = new AbstractAction(
				CommonLocaleDelegate.getLabelFromMetaDataVO(entitymetavo)) {
			/**
					 * 
					 */
					private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent evt) {
				cmdCollectMasterData(evt);
			}
		};
		action.setEnabled(true);
		action.putValue(Action.SMALL_ICON, MainFrame.resizeAndCacheTabIcon(MainFrame.getEntityIcon(entity)));
		action.putValue(Action.ACTION_COMMAND_KEY, entity);
		if (!StringUtils.isNullOrEmpty(entitymetavo.getAccelerator()) && entitymetavo.getAcceleratorModifier() != null) {
			int keycode = entitymetavo.getAccelerator().charAt(0);
			if(keycode > 90)
				keycode -= 32;

			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keycode, entitymetavo.getAcceleratorModifier().intValue()));
		} else if (!StringUtils.isNullOrEmpty(entitymetavo.getAccelerator())) {
			action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(entitymetavo.getAccelerator().charAt(0)));
		}
		return action;
	}

	private static String[] splitMenuPath(String menuPath) {
		return (menuPath != null && !menuPath.isEmpty()) ? menuPath.split("\\\\") : null;
	}

	private void setupMenuBar(){
		frm.menuSetup(getCommandMap(), getComponentMap(), getNotificationDialog());
	}

	/**
	 * the main frame is about to close
	 */
	public boolean cmdWindowClosing() {
		if (allControllersMayBeClosed()) {
			try {

				MainFrame.writeMainFramePreferences(prefs);
				RestoreUtils.storeWorkspace(MainFrame.getWorkspace());

				if (this.ctlTasks != null)	{
					this.ctlTasks.close();
				} else {
					log.debug("TaskController is null!");
				}

				log.debug("removes unused preferences...");
				removeUnusedPreferences();
			}
			catch (Exception ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("MainController.20","Die Sitzungsdaten, die Informationen \u00fcber die zuletzt ge\u00f6ffneten Fenster enthalten,\n" +
						"konnten nicht geschrieben werden. Bei der n\u00e4chsten Sitzung k\u00f6nnen nicht alle Fenster\n" +
						"wiederhergestellt werden. Bitte \u00f6ffnen Sie diese Fenster in der n\u00e4chsten Sitzung erneut.");
				Errors.getInstance().showExceptionDialog(frm, sMessage, ex);
			}
			catch (Error error) {
				log.error("Beim Beenden des Clients ist ein fataler Fehler aufgetreten.", error);
			}
			finally {
				// exit even on <code>Error</code>s, especially <code>NoClassDefFoundError</code>s,
				// which may result from installing a different version while a client is running.
				this.cmdExit();
			}

			return true;
		} else {
			return false;
		}
	}

	public static void cmdCycleThroughWindows(boolean forward) {
		//frm.cmdCycleThroughWindows(forward);
	}

	protected void showStartupPanel(boolean respectPreferences) {


		if(!SecurityCache.getInstance().isActionAllowed("NucletWizard")) {
			return;
		}


		boolean open = true;

		if(respectPreferences) {
			String load = ClientPreferences.getUserPreferences().node("nuclet").get("loadonstart", "true");
			if("false".equals(load)) {
				open = false;
			}
		}

		if(open) {
			NuclosStartupPanel panel = new NuclosStartupPanel(this.getFrame(), getUserName());
			JScrollPane scroll = new JScrollPane(panel);
			scroll.getHorizontalScrollBar().setUnitIncrement(20);
			scroll.getVerticalScrollBar().setUnitIncrement(20);
			final MainFrameTab ifrm = MainController.newMainFrameTab(null, "Nuclet Wizard");
			ifrm.setTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.73-radar.png"));
			ifrm.setLayeredComponent(scroll);
			panel.setParent(ifrm);

			int x = this.getDesktopPane().getWidth()/2-panel.getPreferredSize().width/2;
			int y = this.getDesktopPane().getHeight()/2-panel.getPreferredSize().height/2;
			x = x<0?0:x;
			y = y<0?0:y;
			ifrm.setBounds(x, y, panel.getWidth(), panel.getHeight());

			this.getDesktopPane().add(ifrm);
			ifrm.setVisible(true);
		}
	}


	public void invalidateAllClientCaches() {
		Modules.getInstance().invalidate();
		MasterDataCache.getInstance().invalidate(null);
		MasterDataDelegate.getInstance().invalidateLayoutCache();
		MetaDataCache.getInstance().invalidate();
		SecurityCache.getInstance().revalidate();
		AttributeCache.getInstance().revalidate();
		GenericObjectLayoutCache.getInstance().invalidate();
		GeneratorActions.invalidateCache();
		ResourceCache.invalidate();
		SearchFilterCache.getInstance().validate();
		GenericObjectDelegate.getInstance().invalidateCaches();
		LocaleDelegate.getInstance().flush();
		refreshMenus();
		refreshTaskController();
		frm.setTitle(getUserName(), getNuclosServerName());
	}

	public void refreshMenus(){
		setupMenuBar();
	}

	public void refreshTaskController() {
		this.ctlTasks.refreshAllTaskViews();
	}

	protected boolean allControllersMayBeClosed() {
		boolean result = true;
		final Iterator<TopController> iter = mpActiveControllers.values().iterator();
		while (result && iter.hasNext()) {
			final TopController ctl = iter.next();
			result = ctl.askAndSaveIfNecessary();
		}
		return result;
	}

	/**
	 * @return List<CollectController> the CollectControllers for the internal frames,
	 * starting with the bottom frame in Z-order.
	 * @postcondition result != null
	 */
	private List<TopController> getTopControllersForInternalFrames() {
		final List<TopController> result = new LinkedList<TopController>();

		for (MainFrameTab ifrm : frm.getAllTabs()) {
			final TopController ctl = this.getControllerForInternalFrame(ifrm, TopController.class);
			if (ctl != null) {
				result.add(ctl);
			}
		}
		assert result != null;
		return result;
	}

	/**
	 * @param ifrm
	 * @return the <code>CollectController</code> (if any) for the given frame.
	 */
	public CollectController<?> getControllerForInternalFrame(MainFrameTab ifrm) {
		return getControllerForInternalFrame(ifrm, CollectController.class);
	}

	public <C extends TopController> C getControllerForInternalFrame(MainFrameTab ifrm, Class<? extends C> clazz) {
		Controller ctl = this.mpActiveControllers.get(ifrm);
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

	public void showDetails(String entityName, Long id) throws CommonBusinessException {
		showDetails(entityName, id.intValue());
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 * @precondition oId != null
	 */
	public void showDetails(String sEntityName, Object oId) throws CommonBusinessException {
		showDetails(sEntityName, oId, null);
	}

	/**
	 * @param sEntityName
	 * @param oId
	 * @param listeningController
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 * @precondition oId != null
	 */
	public void showDetails(String sEntityName, Object oId, CollectController<?> listeningController) throws CommonBusinessException {
		final CollectController<?> ctlExisting = this.findCollectControllerDisplaying(sEntityName, oId);
		// reuse existing window if the object is already displayed in Details:
		if (ctlExisting != null) {
				if (listeningController != null) {
					ctlExisting.addCollectableEventListener(new DetailsCollectableEventListener(listeningController, ctlExisting));
				}
				MainFrame.setSelectedTab(ctlExisting.getFrame());
		}
		else {
			NuclosCollectController<?> controller = NuclosCollectControllerFactory.getInstance().newCollectController(MainFrame.getPredefinedEntityOpenLocation(sEntityName), sEntityName, null);
			if (listeningController != null) {
				controller.addCollectableEventListener(new DetailsCollectableEventListener(listeningController, controller));
			}
			controller.runViewSingleCollectableWithId(oId);
		}
	}

	/**
	 * @param sEntityName
	 * @throws CommonBusinessException
	 * @precondition sEntityName != null
	 */
	public CollectController<? extends Collectable> showDetails(String sEntityName) throws CommonBusinessException {
		CollectController<? extends Collectable> clctcontroller = NuclosCollectControllerFactory.getInstance().newCollectController(this.getDesktopPane(), sEntityName, null);
		clctcontroller.runNew();
		return clctcontroller;
	}

	private JTabbedPane getDesktopPane() {
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
		new ReleaseNotesController(this.getFrame(), this.getDesktopPane()).showReleaseNotesIfNewVersion();
	}

	protected void cmdShowInternalInfo() {
		new InternalInfoController(this.getFrame(), this.getDesktopPane()).showInternalInfo();
	}

	private void showInternalInfoIfChanged() {
		new InternalInfoController(this.getFrame(), this.getDesktopPane()).showInternalInfoIfChanged();
	}


	private void cmdCollectMasterData(final ActionEvent ev) {
		UIUtils.runCommand(frm, new Runnable() {
			@Override
			public void run() {
				try {
					String entity = ev.getActionCommand();
					NuclosCollectController<?> ncc = NuclosCollectControllerFactory.getInstance().newCollectController(frm.getPredefinedEntityOpenLocation(entity), entity, null);
					if(ncc != null)
						ncc.run();
				}
				catch (CommonBusinessException ex) {
					final String sErrorMsg = CommonLocaleDelegate.getMessage("MainController.21","Die Stammdaten k\u00f6nnen nicht bearbeitet werden.");
					Errors.getInstance().showExceptionDialog(frm, sErrorMsg, ex);
				}
			}
		});
	}


	private void handleMessge(final Message msg) {
		try {
			if (msg.getJMSCorrelationID() != null && (msg.getJMSCorrelationID().equals(MainController.this.getUserName()) || msg.getJMSCorrelationID().equals(JMSConstants.BROADCAST_MESSAGE)) && msg instanceof ObjectMessage)
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
							log.warn("Undefined message priority: " + notification.getPriority());
					}
				}
				else if (objMessage.getClass().equals(CommandMessage.class)) {
					final CommandMessage command = (CommandMessage) ((ObjectMessage) msg).getObject();
					switch (command.getCommand()) {
						case CommandMessage.CMD_SHUTDOWN :
							getNotificationDialog().addMessage(new RuleNotification(Priority.HIGH, CommonLocaleDelegate.getMessage("MainController.19","Der Client wird auf Anweisung des Administrators in 10 Sekunden beendet."), "Administrator"));
							getNotificationDialog().setVisible(true);

							new Thread(new Runnable() {
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
							}).run();
							break;
					}
				} else if(objMessage.getClass().equals(CommandInformationMessage.class)) {
					final CommandInformationMessage command = (CommandInformationMessage)((ObjectMessage) msg).getObject();
					switch(command.getCommand()) {
						case CommandInformationMessage.CMD_INFO_SHUTDOWN :
							Object[] options = { "OK" };
							int decision = JOptionPane.showOptionDialog(frm, command.getInfo(), CommonLocaleDelegate.getMessage("MainController.17","Administrator - Passwort\u00e4nderung"),
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
								  }).run();
							}
						break;
					}
				}
			}
			else {
				log.warn(CommonLocaleDelegate.getMessage("MainController.14","Message of type {0} received, while an ObjectMessage was expected.", msg.getClass().getName()));
			}
		}
		catch (JMSException ex) {
			log.warn("Exception thrown in JMS message listener.", ex);
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
			/*
			if (ctl instanceof CustomComponentController) {
				prefs.put(PREFS_KEY_CUSTOMCOMP, ((CustomComponentController) ctl).getCustomComponentName());
				((CustomComponentController) ctl).storeInstanceState(prefs);
			} if (ctl instanceof NuclosCollectController) {
				((NuclosCollectController<?>) ctl).writeToPreferences(prefs);
			}
			*/
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
//				Errors.getInstance().showExceptionDialog(frm, CommonLocaleDelegate.getMessage("MainController.22","Keine Information \u00fcber das Feld verf\u00fcgbar"), e);
//			}
		}
	} //DirectHelpActionListener

	private static class MemoryMonitor extends JPanel implements Runnable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
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

			Thread dm = new Thread(this, "MemoryMonitor");
			dm.setDaemon(true);
			dm.start();
		}

		private AbstractAction gcAction = new AbstractAction("GC") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
				}
				catch(InterruptedException e) {}
				Runtime r = Runtime.getRuntime();
				total = r.totalMemory();
				free = r.freeMemory();
				inuse = total - free;
				SwingUtilities.invokeLater(swingUpd);
			}
		}

		private Runnable swingUpd = new Runnable() {
			@Override
			public void run() {
				content.setText(String.format("%,.2f KB", inuse / 1024.0));
				content.setToolTipText("<html><b>Free</b>: " + String.format("%,.2f KB", free / 1024.0) + "<br /><b>Total</b>: " + String.format("%,.2f KB", total / 1024.0) + "</html>");
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
			log.error(e.getMessage(), e);
		}
	}

	public String getMainMenuAdministration() {
		return CommonLocaleDelegate.getMessage("MainMenuAdministration", "Administration").replace("^", "");
	}

	public String getMainMenuConfiguration() {
		return CommonLocaleDelegate.getMessage("MainMenuConfiguration", "Konfiguration").replace("^", "");
	}

}	// class MainControlle
