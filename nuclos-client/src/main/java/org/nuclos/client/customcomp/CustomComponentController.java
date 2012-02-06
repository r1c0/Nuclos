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

package org.nuclos.client.customcomp;

import java.io.Serializable;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;

import org.apache.log4j.Logger;
import org.nuclos.client.customcomp.resplan.ResPlanController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.workspace.ITabStoreController;
import org.nuclos.client.main.mainframe.workspace.TabRestoreController;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.client.ui.CommonMultiThreader;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.TopController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.UIUtils.CommandHandler;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class CustomComponentController extends TopController {

	protected final Logger log = Logger.getLogger(this.getClass());

	private final String componentName;
	private MainFrameTab ifrm;

	protected CustomComponentController(JComponent parent, String componentName) {
		super(parent);
		this.componentName = componentName;
	}

	@Override
	public JComponent getParent() {
		return (JComponent) super.getParent();
	}

	public final String getCustomComponentName() {
		return componentName;
	}
	
	@Override
	public void writePreferencesWhileClosing() {
		
	}

	/**
	 * This method is one of the entry points. The component is initialized for presentation
	 * and the corresponding internal frame is shown.
	 * Note: Controller subclasses may provide alternative entry points which allows a more
	 * specific initialization.
	 */
	public void run() {
		ifrm.setVisible(true);
		MainFrame.setSelectedTab(ifrm);
	}

	protected abstract JComponent getComponent();

	protected void storeSharedState() throws PreferencesException {
	}

	protected void restoreSharedState() throws PreferencesException {
	}

	/** Returns the client preferences node used for storing common/shared preferences. */
	protected Preferences getPreferences() {
		return ClientPreferences.getUserPreferences().node("customcomponent").node(getCustomComponentName());
	}

	/** Runs the CommonRunnable and performs error handling.*/
	public void runCommandWithSpecialHandler(CommonRunnable runnable) {
		// This is similar to UIUtils.runCommand but handles
		try {
			CommandHandler cmdHandler = UIUtils.getCommandHandler();
			cmdHandler.commandStarted(ifrm);
			try {
				runnable.run();
			} finally {
				cmdHandler.commandFinished(ifrm);
			}
		} catch (Exception ex) {
			boolean expectionHandled = handleSpecialException(ex);
			if (!expectionHandled) {
				Errors.getInstance().showExceptionDialog(ifrm, ex);
			}
		} catch (Error error) {
			Errors.getInstance().getCriticalErrorHandler().handleCriticalError(ifrm, error);
		}
	}

	/** Can be overridden by subclasses to handle some exceptions differently. */
	protected boolean handleSpecialException(Exception ex) {
		return false;
	}

	/** Executes a background task in this controller. This method performs all relevant locking and error
	 * handling.
	 */
	protected void execute(final BackgroundTask task) {
		final MainFrameTab ifrm = this.ifrm;
		CommonMultiThreader.getInstance().execute(new CommonClientWorker() {
			@Override
			public void init() throws CommonBusinessException {
				if (ifrm != null)
					ifrm.lockLayer();
				task.init();
			}
			@Override
			public void work() throws CommonBusinessException {
				task.doInBackground();
			}
			@Override
			public void paint() throws CommonBusinessException {
				if (ifrm != null)
					ifrm.unlockLayer();
				task.done();
			}
			@Override
			public JComponent getResultsComponent() {
				return ifrm;
			}
			@Override
			public void handleError(Exception ex) {
				if (ifrm != null)
					ifrm.unlockLayer();
				Errors.getInstance().showExceptionDialog(getResultsComponent(), ex);
			}
		});
	}

	/**
	 * A background task is similar to a CommonClientWorker but is only responsible for performing task-related actions.
	 * In contrast to the CommonClientWorker(Adapter), the BackgroundTask should not perform any GUI locking or error
	 * handling. These will be performed by the particular executor in a uniform way.
	 */
	public static abstract class BackgroundTask {

		public void init() throws CommonBusinessException {
		}

		public abstract void doInBackground() throws CommonBusinessException;

		public void done() throws CommonBusinessException {
		}
	}

	/**
	 * Instantiates the given component controller.
	 * @param customComponent internal name of the component
	 * @param desktopPane the parent desktop pane
	 */
	public static CustomComponentController newController(String customComponent) {
		return newController(customComponent, CustomComponentController.class, null);
	}

	/**
	 * Instantiates the given component controller.  This method is identical to {@link #newController(String, JDesktopPane)}
	 * but ensures that the created controller is of the specified class.
	 * @param customComponent internal name of the component
	 * @param controllerClazz class of the controller
	 */
	public static CustomComponentController newController(String customComponent, Class<?> controllerClazz, MainFrameTab tabIfAny) {
		CustomComponentVO componentVO = CustomComponentCache.getInstance().getByName(customComponent);
		final CustomComponentController controller;
		if ("org.nuclos.resplan".equals(componentVO.getComponentType()) && controllerClazz.isAssignableFrom(ResPlanController.class)) {
			controller = new ResPlanController(MainFrame.getPredefinedEntityOpenLocation(customComponent), componentVO);
		} else {
			throw new NuclosFatalException("Component " + componentVO.getInternalName() + " has an unsupported or incompatible component type");
		}
		boolean newTab = false;
		final MainFrameTab mainFrameTab;
		String title = CommonLocaleDelegate.getInstance().getTextFallback(
				componentVO.getLabelResourceId(), componentVO.getLabelResourceId());
		if (tabIfAny == null) {
			newTab = true;
			mainFrameTab = Main.getInstance().getMainController().newMainFrameTab(controller, title);
		} else {
			mainFrameTab = tabIfAny;
			mainFrameTab.setTitle(title);
		}
		controller.ifrm = mainFrameTab;
		mainFrameTab.setLayeredComponent(controller.getComponent());
		if (controller.isRestoreTab()) {
			mainFrameTab.setTabStoreController(new CustomComponentTabStoreController(controller));
		}
		mainFrameTab.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public boolean tabClosing(MainFrameTab tab) {
				UIUtils.runShortCommand(mainFrameTab, new CommonRunnable() {
					@Override
					public void run() throws CommonBusinessException {
						try {
							controller.getPreferences().removeNode();
						} catch (BackingStoreException ex) {
							throw new PreferencesException(ex);
						}
						controller.storeSharedState();
					}
				});
				return true;
			}
			@Override
			public void tabClosed(MainFrameTab tab) {
				mainFrameTab.removeMainFrameTabListener(this);
			}
		});

		if (newTab)
		MainFrame.getPredefinedEntityOpenLocation(customComponent).add(mainFrameTab);
		UIUtils.runShortCommand(mainFrameTab, new CommonRunnable() {
			@Override
			public void run() throws CommonBusinessException {
				controller.restoreSharedState();
			}
		});
		return controller;
	}

	public abstract boolean isRestoreTab();

	/**
	 *
	 *
	 */
	private static class RestorePreferences implements Serializable {
		private static final long serialVersionUID = 6637996725938917463L;

		String customComponentName;
		String customComponentClass;
		String instanceStateXML;
	}

	private static String toXML(RestorePreferences rp) {
		XStream xstream = new XStream(new DomDriver());
		return xstream.toXML(rp);
	}

	private static RestorePreferences fromXML(String xml) {
		XStream xstream = new XStream(new DomDriver());
		return (RestorePreferences) xstream.fromXML(xml);
	}

	protected abstract String storeInstanceStateToXML();

	protected abstract void restoreInstanceStateFromXML(String xml);

	/**
	 *
	 *
	 */
	public static class CustomComponentTabStoreController implements ITabStoreController {
		private final CustomComponentController ctl;

		public CustomComponentTabStoreController(CustomComponentController ctl) {
			super();
			this.ctl = ctl;
		}

		@Override
		public String getPreferencesXML() {
			RestorePreferences rp = new RestorePreferences();
			rp.customComponentName = ctl.getCustomComponentName();
			rp.customComponentClass = ctl.getClass().getName();
			rp.instanceStateXML = ctl.storeInstanceStateToXML();
			return toXML(rp);
		}

		@Override
		public Class<?> getTabRestoreControllerClass() {
			return CustomComponentTabRestoreController.class;
		}

	}

	public static class CustomComponentTabRestoreController extends TabRestoreController {
		
		public CustomComponentTabRestoreController() {
		}
		
		@Override
		public void restoreFromPreferences(String preferencesXML, MainFrameTab tab) throws Exception {
			RestorePreferences rp = fromXML(preferencesXML);
			Class<?> customComponentControllerClass = Class.forName(rp.customComponentClass);
			CustomComponentController ctl = CustomComponentController.newController(rp.customComponentName, customComponentControllerClass, tab);

			Main.getInstance().getMainController().initMainFrameTab(ctl, tab);
			// Main.getMainController().addMainFrameTab would be called from listener inside of initMainFrameTab, but only when tab added.
			// During restore the tabs are already added, so we need to do this manually.
			Main.getInstance().getMainController().addMainFrameTab(tab, ctl);

			ctl.restoreInstanceStateFromXML(rp.instanceStateXML);
			ctl.run();
		}

	}

	@Override
	public ImageIcon getIcon() {
		return MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish.83-calendar.png"));
	}

	public MainFrameTab getFrame() {
		return ifrm;
	}
}