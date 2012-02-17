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
package org.nuclos.common;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.nuclos.common2.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Application global properties.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Component
public class ApplicationProperties implements Serializable {

	private static final Logger LOG = Logger.getLogger(ApplicationProperties.class);

	private static final long serialVersionUID = 16362018323707955L;

	/**
	 * the color of the application logo
	 */
	public static final Color COLOR_APP = new Color(40, 60, 131);
	
	private static ApplicationProperties INSTANCE;

	//

	private Version nuclosVersion;
	private Version appVersion;
	private String sMainControllerClassName;
	private String sCollectControllerClassName;
	private String sCollectableComponentFactoryClassName;
	private String sGenericObjectTreeNodeFactoryClassName;
	private String sExplorerNodeFactoryClassName;
	private String sExplorerViewFactoryClassName;
	private String sFrameIconFileName;
	private String sBigIconFileName;
	private String sBigIcon512FileName;
	private String sCustomerIconFileName;
	private String sReleaseNotesFileName;
	private String sGenericObjectCollectableFieldsProviderFactoryClassName;
	private String sMasterDataCollectableFieldsProviderFactoryClassName;
	private String sCollectableComparatorFactoryClassName;
	private String sCollectableFieldComparatorFactoryClassName;
	private String sConsoleClassName;
	private boolean bFunctionBlockDev;
	private long    loginPanelBgColor;
	private long    loginPanelLogoBgColor;
	private long    loginPanelTextColor;
	private long    loginPanelBorderHiColor;
	private long    loginPanelBorderShadeColor;
	private long    splashBgColor;
	private String  splashIconFileName;
	private long    splashTitleColor;
	private long    splashVersionColor;
	private long    splashSteppingColor;
	private long    splashProgressColor;
	private long    desktopPaneBackgroundColor;
	private String  desktopPaneBgImageFileName;
	
	ApplicationProperties() {
		INSTANCE = this;
	}
	
	public static ApplicationProperties getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	@PostConstruct
	final void init() {
		Properties props = new Properties();
		props.put("application.icon.frame", "icons/nuclos-icon.png");
		props.put("application.icon.big.transparent", "icons/nuclos-icon.png");
		props.put("application.icon.big.transparent.512", "icons/nuclos-icon_512.png");
		props.put("application.icon.customer", "icons/nuclos-logo.png");
		props.put("releasenotes", "org/nuclos/client/help/releasenotes/releasenotes.html");

		try {
			props.load(ApplicationProperties.class.getClassLoader().getResourceAsStream("nuclos-version.properties"));
		} catch (Exception ex) {
			throw new NuclosFatalException("Error reading nuclos-version.properties", ex);
		}
		this.nuclosVersion = Version.fromProperties(props, "nuclos");

		InputStream is = ApplicationProperties.class.getClassLoader().getResourceAsStream("nuclos-app.properties");
		if (is != null) {
			try {
				props.load(is);
			} catch(IOException ex) {
				throw new NuclosFatalException("Error reading nuclos-app.properties", ex);
			}
			this.appVersion = Version.fromProperties(props, "application");
		} else {
			this.appVersion = this.nuclosVersion;
		}

		this.sMainControllerClassName = getOptional(props, "maincontroller");
		this.sCollectControllerClassName = getOptional(props, "collectcontrollerfactory");
		this.sCollectableComponentFactoryClassName = getOptional(props, "collectablecomponentfactory");
		this.sGenericObjectTreeNodeFactoryClassName = getOptional(props, "genericobjecttreenodefactory");
		this.sExplorerNodeFactoryClassName = getOptional(props, "explorernodefactory");
		this.sExplorerViewFactoryClassName = getOptional(props, "explorerviewfactory");
		this.sFrameIconFileName = getOptional(props, "application.icon.frame");
		this.sBigIconFileName = getOptional(props, "application.icon.big.transparent");
		this.sBigIcon512FileName = getOptional(props, "application.icon.big.transparent.512");
		this.sCustomerIconFileName = getOptional(props, "application.icon.customer");
		this.sReleaseNotesFileName = getOptional(props, "releasenotes");
		this.sGenericObjectCollectableFieldsProviderFactoryClassName = getOptional(props, "genericobjectcollectablefieldsproviderfactory");
		this.sMasterDataCollectableFieldsProviderFactoryClassName = getOptional(props, "masterdatacollectablefieldsproviderfactory");
		this.sCollectableComparatorFactoryClassName = getOptional(props, "collectablecomparatorfactory");
		this.sCollectableFieldComparatorFactoryClassName = getOptional(props, "collectablefieldcomparatorfactory");
		this.sConsoleClassName = getOptional(props, "console");
		this.bFunctionBlockDev = Boolean.getBoolean("functionblock.dev");

		this.loginPanelBgColor = parseColorHex(getOptional(props, "login.panel.color.background"));
		this.loginPanelLogoBgColor = parseColorHex(getOptional(props, "login.panel.logo.background"));
		this.loginPanelTextColor = parseColorHex(getOptional(props, "login.panel.color.text"));
		this.loginPanelBorderHiColor = parseColorHex(getOptional(props, "login.panel.border.highlight"));
		this.loginPanelBorderShadeColor = parseColorHex(getOptional(props, "login.panel.border.shadow"));

		this.splashBgColor = parseColorHex(getOptional(props, "splashscreen.color.background"));
		this.splashIconFileName = getOptional(props, "splashscreen.icon.customer");
		this.splashTitleColor = parseColorHex(getOptional(props, "splashscreen.color.text.title"));
		this.splashVersionColor = parseColorHex(getOptional(props, "splashscreen.color.text.version"));
		this.splashSteppingColor = parseColorHex(getOptional(props, "splashscreen.color.text.steps"));
		this.splashProgressColor = parseColorHex(getOptional(props, "splashscreen.color.progress"));

		this.desktopPaneBackgroundColor = parseColorHex(getOptional(props, "mainframe.desktoppane.background"));
		this.desktopPaneBgImageFileName = getOptional(props, "mainframe.desktoppane.bgimage");
	}

	private static String getOptional(Properties props, String key) {
		return StringUtils.trim(props.getProperty(key));
	}

	private static long parseColorHex(String s) {
		if (s != null)
			try {
				return Long.parseLong(s, 16);
			} catch (NumberFormatException e) {
				LOG.debug("parseColorHex: " + e);
			}
		return -1L;
	}

	/**
	 * @return the name of this application.
	 */
	public String getName() {
		return appVersion.getAppName();
	}

	public String getAppId() {
		return appVersion.getAppId();
	}

	/**
	 * @return the current version (including version number and version date) of this application.
	 */
	public Version getCurrentVersion() {
		return this.appVersion;
	}

	/**
	 * Returns the current Nuclos version.
	 */
	public Version getNuclosVersion() {
		return this.nuclosVersion;
	}

	/**
	 * @return the class name of the ExplorerNodeFactory to be used in this application.
	 */
	public String getMainControllerClassName() {
		return this.sMainControllerClassName;
	}

	public String getCollectControllerFactoryClassName() {
		return this.sCollectControllerClassName;
	}

	/**
	 * @return the class name of the CollectableComponentFactory to be used in this application.
	 */
	public String getCollectableComponentFactoryClassName() {
		return this.sCollectableComponentFactoryClassName;
	}

	/**
	 * @return the class name of the GenericObjectTreeNodeFactory to be used in this application.
	 */
	public String getGenericObjectTreeNodeFactoryClassName() {
		return this.sGenericObjectTreeNodeFactoryClassName;
	}

	/**
	 * @return the class name of the ExplorerNodeFactory to be used in this application.
	 */
	public String getExplorerNodeFactoryClassName() {
		return this.sExplorerNodeFactoryClassName;
	}

	/**
	 * @return the class name of the ExplorerViewFactory to be used in this application.
	 */
	public String getExplorerViewFactoryClassName() {
		return this.sExplorerViewFactoryClassName;
	}

	/**
	 * @return the file name of the icon to be used in all frames and internal frames in this application.
	 */
	public String getFrameIconFileName() {
		return this.sFrameIconFileName;
	}

	/**
	 * @return the file name of the big transparent icon to be used in this application.
	 */
	public String getCustomerIconFileName() {
		return this.sCustomerIconFileName;
	}

	/**
	 * @return the file name of the big transparent icon to be used in this application.
	 */
	public String getBigTransparentIconFileName() {
		return this.sBigIconFileName;
	}

	public String getBigTransparentIcon512FileName() {
		return this.sBigIcon512FileName;
	}

	public String getReleaseNotesFileName() {
		return this.sReleaseNotesFileName;
	}

	/**
	 * @return the class name of the GenericObjectCollectableFieldsProviderFactory to be used in this application.
	 */
	public String getGenericObjectCollectableFieldsProviderFactoryClassName() {
		return this.sGenericObjectCollectableFieldsProviderFactoryClassName;
	}

	/**
	 * @return the class name of the MasterDataCollectableFieldsProviderFactory to be used in this application.
	 */
	public String getMasterDataCollectableFieldsProviderFactoryClassName() {
		return this.sMasterDataCollectableFieldsProviderFactoryClassName;
	}

	/**
	 * @return the class name of the CollectableComparatorFactoryClassName to be used in this application.
	 */
	public String getCollectableComparatorFactoryClassName() {
		return this.sCollectableComparatorFactoryClassName;
	}

	/**
	 * @return the class name of the CollectableFieldComparatorFactoryClassName to be used in this application.
	 */
	public String getCollectableFieldComparatorFactoryClassName() {
		return this.sCollectableFieldComparatorFactoryClassName;
	}

	/**
	 * @return the class name of the ConsoleFactoryClassName to be used in this application.
	 */
	public String getConsoleClassName() {
		return this.sConsoleClassName;
	}

	public String toHtml() {
		final StringBuilder sb = new StringBuilder("<html><b>Application Properties</b></br><table border =\"1\">");
		Method[] methods = this.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].getName().startsWith("get") && methods[i].getReturnType().equals(String.class) && methods[i].getParameterTypes().length == 0) {
				try {
					sb.append("<tr><td><b>" + methods[i].getName().replaceAll("get", "") + "</b></td><td>" + methods[i].invoke(this, new Object[] {}) + "</td></tr>");
				} catch (Exception e) {
					sb.append("<tr><td>ERROR</td><td></td></tr>");
					LOG.warn("toHtml failed: " + e, e);
				}
			}
		}
		sb.append("</table></html>");
		return sb.toString();
	}

	private static Color getColor(long v, Color def) {
	   if(v < 0) return def;
	   return new Color((int) v);
	}

	public Color getLoginPanelBgColor(Color def) {
	   return getColor(loginPanelBgColor, def);
	}

	public Color getLoginPanelLogoBgColor(Color def) {
           return getColor(loginPanelLogoBgColor, def);
        }

	public Color getLoginPanelTextColor(Color def) {
	   return getColor(loginPanelTextColor, def);
	}

	public Color getLoginPanelBorderHiColor(Color def) {
	   return getColor(loginPanelBorderHiColor, def);
	}

	public Color getLoginPanelBorderShadeColor(Color def) {
	   return getColor(loginPanelBorderShadeColor, def);
	}

	public Color getSplashscreenBackgroundColor(Color def) {
	   return getColor(splashBgColor, def);
	}

	public String getSplashIconFileName() {
	   return splashIconFileName;
	}

	public Color getSplashTitleColor(Color def) {
	   return getColor(splashTitleColor, def);
	}

	public Color getSplashVersionColor(Color def) {
	   return getColor(splashVersionColor, def);
	}

	public Color getSplashSteppingColor(Color def) {
	   return getColor(splashSteppingColor, def);
	}

	public Color getSplashProgressColor(Color def) {
	   return getColor(splashProgressColor, def);
	}

	public Color getDesktopPaneBackgroundColor(Color def) {
	   return getColor(desktopPaneBackgroundColor, def);
	}

	public String getDesktopPaneBgImageFileName() {
		String ovr = System.getenv("nucleus.user.desktop.image");
		if(ovr != null)
			return ovr;
		return desktopPaneBgImageFileName;
	}

	public Boolean isFunctionBlockDev() {
		return bFunctionBlockDev;
	}

	/**
	 * inner class Version
	 */
	public static class Version implements Serializable {

		private final String appId;
		private final String appName;
		private final String sVersionNumber;
		private final String sVersionDate;
		private final String sSchemaVersion;

		private Version(String appId, String appName, String sVersionNumber, String sVersionDate, String sSchemaVersion) {
			this.appId = appId;
			this.appName = appName;
			this.sVersionNumber = sVersionNumber;
			this.sVersionDate = sVersionDate;
			this.sSchemaVersion = StringUtils.isNullOrEmpty(sSchemaVersion) ? "undefined" : sSchemaVersion;
		}

		public String getAppId() {
			return appId;
		}

		public String getAppName() {
			return appName;
		}

		public String getVersionNumber() {
			return this.sVersionNumber;
		}

		public Date getVersionDate() {
			try {
				return new SimpleDateFormat("dd.MM.yyyy").parse(sVersionDate);
			}
			catch(ParseException e) {
				throw new NuclosFatalException(e);
			}
		}

		public String getVersionDateString() {
			return this.sVersionDate;
		}

		public String getSchemaVersion() {
			return this.sSchemaVersion;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if ((o == null) || !(o instanceof Version)) {
				return false;
			}
			final Version that = (Version) o;
			return this.sVersionNumber.equals(that.sVersionNumber) && this.sVersionDate.equals(that.sVersionDate) && this.sSchemaVersion.equals(that.sSchemaVersion);
		}

		/**
		 * @return application name and version.
		 */
		public String getShortName() {
			return getAppName() + " V" + getVersionNumber();
		}

		/**
		 * @return application name and version, including version date.
		 */
		public String getLongName() {
			return this.getShortName() + " (" + getVersionDateString() + ")";
		}

		@Override
		public String toString() {
			return this.getLongName();
		}

		static Version fromProperties(Properties props, String prefix) {
			String id = props.getProperty(prefix + ".id");
			String name = props.getProperty(prefix + ".name");
			String version = props.getProperty(prefix + ".version.number");
			String bugfixVersion = props.getProperty(prefix + ".version.number.bugfix");
			if (bugfixVersion != null && !bugfixVersion.isEmpty()) {
				version = version + "." + bugfixVersion;
			}
			String versionDate = props.getProperty(prefix + ".version.date");
			String schemaVersion = props.getProperty(prefix + ".schema.version");
			return new Version(id != null ? id : name, name, version, versionDate, schemaVersion);
		}

	}	// inner class Version

}	// class ApplicationProperties
