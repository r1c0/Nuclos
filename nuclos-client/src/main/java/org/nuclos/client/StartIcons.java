package org.nuclos.client;

import java.awt.Toolkit;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.LangUtils;

public class StartIcons {

	private static final Logger LOG = Logger.getLogger(NuclosIcons.class);
	
	private static StartIcons INSTANCE;

	StartIcons() {
		INSTANCE = this;
	}
	
	/**
	 * @return the one and only instance of <code>Icons</code>
	 */
	public static StartIcons getInstance() {
		return INSTANCE;
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
		return getImageIcon(sBigIconFilename);
	}
	
	public Icon getIconCustomer() {
		final String sCustomerIconFilename = LangUtils.defaultIfNull(
				ApplicationProperties.getInstance().getCustomerIconFileName(),
				"org/nuclos/client/images/eplus-logo-scaled.png");
		return getImageIcon(sCustomerIconFilename);		
	}

	public ImageIcon getDefaultFrameIcon() {
		String sFrameIconFileName = LangUtils.defaultIfNull(
			ApplicationProperties.getInstance().getFrameIconFileName(),
			"org/nuclos/client/images/nucleus-16x16-whitecircle.png"
		);
		return getImageIcon(sFrameIconFileName);
	}
	
	private ImageIcon getImageIcon(String sFileName) {
		try {
			return new ImageIcon(getClass().getClassLoader().getResource(sFileName));
		}
		catch(NullPointerException e) {
			LOG.error("Can't find resource '" + sFileName + "' for icon");
			return new ImageIcon(Toolkit.getDefaultToolkit().createImage(sFileName));
		}
	}
	
}
