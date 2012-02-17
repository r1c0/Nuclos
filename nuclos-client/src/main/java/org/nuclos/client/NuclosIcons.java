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
package org.nuclos.client;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Icons;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Singleton containing Nucleus specific icons.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Component
public class NuclosIcons {
	
	private static final Logger LOG = Logger.getLogger(NuclosIcons.class);

	private static NuclosIcons INSTANCE;
	
	//
	
	private ResourceCache resourceCache;

	/**
	 * icon cache
	 */
	private final Map<String, ImageIcon> mpIcons = new HashMap<String, ImageIcon>();
	
	NuclosIcons() {
		INSTANCE = this;
	}
	
	@Autowired
	void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}
	
	/**
	 * @return the one and only instance of <code>Icons</code>
	 */
	public static NuclosIcons getInstance() {
		return INSTANCE;
	}
	
	void setResourceCache(ResourceCache resourceCache) {
		this.resourceCache = resourceCache;
	}
	
	public Icon getIconCustomer() {
		final String sCustomerIconFilename = LangUtils.defaultIfNull(
				ApplicationProperties.getInstance().getCustomerIconFileName(),
				"org/nuclos/client/images/eplus-logo-scaled.png");
		return this.getCachedImageIcon(sCustomerIconFilename);		
	}

	/**
	 * @return an opaque 16x16 icon that should be used as frame icon.
	 */
	public ImageIcon getFrameIcon() {
		ImageIcon resourceIcon = getResource("NCL_MAINFRAME_TITLE");
		if (resourceIcon != null)
			return resourceIcon;
		return getDefaultFrameIcon();
	}

	public ImageIcon getDefaultFrameIcon() {
		String sFrameIconFileName = LangUtils.defaultIfNull(
			ApplicationProperties.getInstance().getFrameIconFileName(),
			"org/nuclos/client/images/nucleus-16x16-whitecircle.png"
		);
		return this.getCachedImageIcon(sFrameIconFileName);
	}
	
	
	/**
	 * @return an image icon that should be watermark image in search panels. 
	 */
	public ImageIcon getSearchWatermarkIcon() {
		ImageIcon resourceIcon = getResource("NCL_SEARCH_ICON");
		if (resourceIcon != null)
			return resourceIcon;
		return Icons.getInstance().getDefaultSearchWatermark();
	}
	
	public ImageIcon getScaledDialogIcon(int size) {
		ImageIcon icon = getResource("NCL_DIALOG_ICONS");
		if (icon == null) {
			icon = getBigTransparentApplicationIcon();
		}
		return new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
	}
	
	
	/**
	 * @return a transparent (preferably) 96x96 or greater icon.
	 * Scaled-down instances will be used in the splash screen (32x32) and in the about dialog (48x48).
	 */
	public ImageIcon getBigTransparentApplicationIcon() {
		// @todo Use a Nucleus icon as default
		final String sBigIconFilename = LangUtils.defaultIfNull(
				ApplicationProperties.getInstance().getBigTransparentIconFileName(),
				"org/nuclos/client/images/nucleus-96x96-transparent.png"
		);
		return this.getCachedImageIcon(sBigIconFilename);
	}
	
	/**
	 * @return a transparent (preferably) 512x512 or greater icon.
	 */
	public ImageIcon getBigTransparentApplicationIcon512() {
		// @todo Use a Nucleus icon as default
		final String sBigIconFilename = LangUtils.defaultIfNull(
				ApplicationProperties.getInstance().getBigTransparentIcon512FileName(),
				"org/nuclos/client/images/nucleus-96x96-transparent.png"
		);
		return this.getCachedImageIcon(sBigIconFilename);
	}
	
	private synchronized ImageIcon getCachedImageIcon(String sFileName) {
		ImageIcon result = this.mpIcons.get(sFileName);
		if (result == null) {
			result = this.getImageIcon(sFileName);
			this.mpIcons.put(sFileName, result);
		}
		return result;
	}

	public Icon getSplashIcon() {
	   String fn = ApplicationProperties.getInstance().getSplashIconFileName();
	   return fn == null ? null : getCachedImageIcon(fn);
	}
	
	public ImageIcon getDesktopBackgroundIcon() {
	   String fn = ApplicationProperties.getInstance().getDesktopPaneBgImageFileName();
	   return fn == null ? null : getCachedImageIcon(fn);
	}
	
	private ImageIcon getImageIcon(String sFileName) {
		try {
			return new ImageIcon(this.getClass().getClassLoader().getResource(sFileName));
		}
		catch(NullPointerException e) {
			return new ImageIcon(Toolkit.getDefaultToolkit().createImage(sFileName));
		}
	}
	
	private ImageIcon getResource(String resourceName) {
		return resourceCache.getIconResource(resourceName);
	}
	
}	// class NuclosIcons
