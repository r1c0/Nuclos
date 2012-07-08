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
package org.nuclos.client.help.releasenotes;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

import org.apache.log4j.Logger;
import org.nuclos.client.help.HtmlPanel;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.ApplicationProperties.Version;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.LangUtils;

/**
 * Controller for displaying the release notes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

public class ReleaseNotesController extends Controller<MainFrameTabbedPane> {
	
	private static final Logger LOG = Logger.getLogger(ReleaseNotesController.class);
		
	private static final String PREFS_KEY_VERSION = "version";
	private static final String PREFS_KEY_BOUNDS = "bounds";
	private static final String PREFS_NODE_RELEASENOTES = "releaseNotes";
	private final Preferences prefs = ClientPreferences.getUserPreferences().node(PREFS_NODE_RELEASENOTES);

	private static final String NUCLOS_RELEASENOTES_URL_PREFIX = "http://wiki.nuclos.de/Nuclos_";
	
	private static final String getNuclosReleaseNotesURL() {
		Version version = ApplicationProperties.getInstance().getNuclosVersion();
		String number = version.getVersionNumber();
		// If dev versions should link to the regular version:
		// if (number.indexOf('-') != -1) {
		// 	number = number.substring(0, number.indexOf('-'));
		// }
		return NUCLOS_RELEASENOTES_URL_PREFIX + number;
	}
	
	public static final void openReleaseNotesInBrowser() {
		try {
			Desktop.getDesktop().browse(new URI(getNuclosReleaseNotesURL()));
		} catch(URISyntaxException e) {
			LOG.warn("openReleaseNotesInBrowser failed: " + e, e);
		} catch(IOException e) {
			LOG.warn("openReleaseNotesInBrowser failed: " + e, e);
		}
	}	
	
	public ReleaseNotesController() {
		super(null);
	}

	@Override
	public MainFrameTabbedPane getParent() {
		return MainFrame.getHomePane();
	}

	/**
	 * shows the project release notes.
	 */
	public void showNuclosReleaseNotesNotice() {
		Version version = ApplicationProperties.getInstance().getCurrentVersion();
		
		final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(null, version.getShortName());
		String text =
			"<html><h1>" + version.getLongName() + "</h1>" +
			"<p>" + getSpringLocaleDelegate().getMessage("nuclos.newversion.releasenotes.notice", null).replace("\n", "<br>") + "</p>" + 
			"<p><a href='" + getNuclosReleaseNotesURL() + "'>" + 
			getSpringLocaleDelegate().getMessage("nuclos.newversion.releasenotes.open", null) + "</a></p></html>";
		
		final HtmlPanel pnlReleaseNotes = new HtmlPanel(text);
		pnlReleaseNotes.btnClose.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				ifrm.dispose();
			}
		});
		pnlReleaseNotes.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch(IOException e1) {
						LOG.warn("showNuclosReleaseNotesNotice failed: " + e1, e1);
					} catch(URISyntaxException e1) {
						LOG.warn("showNuclosReleaseNotesNotice failed: " + e1, e1);
					}
					ifrm.dispose();
				}
			}
		});
		ifrm.setLayeredComponent(pnlReleaseNotes, BorderLayout.CENTER);
		
		ifrm.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl) {
				// write current application version to properties so the release notes are not shown
				// on startup unless there is a newer version:
				prefs.put(PREFS_KEY_VERSION, ApplicationProperties.getInstance().getCurrentVersion().getVersionNumber());
				rl.done(true);
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				tab.removeMainFrameTabListener(this);
			}
		});

		getParent().add(ifrm);
		ifrm.setVisible(true);
	}
	
	
	/**
	 * shows the release notes if they haven't been shown for the current application version before.
	 */
	public void showReleaseNotesIfNewVersion() {
		final String sVersion = prefs.get(PREFS_KEY_VERSION, "0.0.000");
		Version currentVersion = ApplicationProperties.getInstance().getCurrentVersion();
		if (!sVersion.equals(currentVersion.getVersionNumber())) {
			if ("nuclos".equals(currentVersion.getAppId())) {
				this.showNuclosReleaseNotesNotice();
			} else {
				this.showReleaseNotes(ApplicationProperties.getInstance().getName());
			}
		}
	}

	/**
	 * shows the project release notes.
	 */
	public void showReleaseNotes(final String sProject) {
		UIUtils.runCommandLater(this.getParent().getComponentPanel(), new Runnable() {
			@Override
			public void run() {
				try {
					final MainFrameTab ifrm;
					final URL releaseNotesURL;
					
					if(!sProject.equals("nuclos")) {
						ifrm = Main.getInstance().getMainController().newMainFrameTab(null, ApplicationProperties.getInstance().getName() + " Release Notes");
						releaseNotesURL = this.getClass().getClassLoader().getResource(LangUtils.defaultIfNull(
								ApplicationProperties.getInstance().getReleaseNotesFileName(),
								"doc/releasenotes.html"));
					}else{
						ifrm = Main.getInstance().getMainController().newMainFrameTab(null, "Nuclos Release Notes");
						releaseNotesURL = new URL(getNuclosReleaseNotesURL());
					}

					final HtmlPanel pnlReleaseNotes = new HtmlPanel(releaseNotesURL);
					pnlReleaseNotes.btnClose.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent ev) {
							ifrm.dispose();
						}
					});
					//ifrm.getContentPane().add(pnlReleaseNotes, BorderLayout.CENTER);
					ifrm.setLayeredComponent(pnlReleaseNotes, BorderLayout.CENTER);

					ifrm.addMainFrameTabListener(new MainFrameTabAdapter() {
						@Override
						public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl) {
							// write current application version to properties so the release notes are not shown
							// on startup unless there is a newer version:
							prefs.put(PREFS_KEY_VERSION, ApplicationProperties.getInstance().getCurrentVersion().getVersionNumber());
							rl.done(true);
						}
						@Override
						public void tabClosed(MainFrameTab tab) {
							tab.removeMainFrameTabListener(this);
						}
					});

					getParent().add(ifrm);
					ifrm.setVisible(true);
				}
				catch (/* IO */ Exception ex) {
					final String sMessage = getSpringLocaleDelegate().getMessage(
							"ReleaseNotesController.1", "Die Release Notes k\u00f6nnen nicht angezeigt werden.");
					Logger.getLogger(ReleaseNotesController.class).debug(sMessage);
					//Errors.getInstance().showExceptionDialog(ReleaseNotesController.this.getParent(), sMessage, ex);
				}
			}
		});
	}

}	// class ReleaseNotesController
