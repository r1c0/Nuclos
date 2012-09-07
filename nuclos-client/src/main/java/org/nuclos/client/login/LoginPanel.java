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
// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.StartIcons;
import org.nuclos.client.ui.BackgroundPanel;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ApplicationProperties;

/**
 * Login panel. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LoginPanel extends BackgroundPanel {
	
	private static final Logger LOG = Logger.getLogger(LoginPanel.class);
	
	private static LoginPanel INSTANCE = null;

	//
	
	private final JPanel	   pnlLogin	     = new JPanel();
	private final JPanel	   pnlLogo	     = new JPanel();

	private final JLabel	   labUserName;
	private final JLabel	   labPassword;
	private final JLabel	   labMsgSpacer;
	private final JLabel	   labLanguage;

	private Bubble bubble;

	private final JTextField tfUserName;
	private final JPasswordField	   tfPassword	 = new JPasswordField();
	private final JComboBox	           cmbbxLanguage = new JComboBox();
	private final JCheckBox	           rememberPass	 = new JCheckBox();

	private final JProgressBar	progressbar	 = new JProgressBar();

	private LoginPanel() {
		tfUserName	 = new JTextField();
		
		addHierarchyListener(new HierarchyListener() {
			
			private int count = 0;
			
			@Override
			public void hierarchyChanged(HierarchyEvent e) {
				++count;
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
					if (e.getChanged().isShowing()) {
						LOG.info("SHOW LOGIN PANEL: hierarchy changed " + count + ": " + e);
					}
					else {
						LOG.info("HIDE LOGIN PANEL: hierarchy changed " + count + ": " + e);						
					}
				}
				if (count > 20) {
					removeHierarchyListener(this);
				}
			}
		});	
		
		Icon iconCustomer = StartIcons.getInstance().getIconCustomer();
		JLabel labLogo = new JLabel(iconCustomer);
		labLogo.setBorder(null);
		labLogo.setPreferredSize(new Dimension(iconCustomer.getIconWidth(),
		    iconCustomer.getIconHeight()));
		labLogo.setMinimumSize(new Dimension(iconCustomer.getIconWidth(),
		    iconCustomer.getIconHeight()));
		labLogo.setMaximumSize(new Dimension(iconCustomer.getIconWidth(),
		    iconCustomer.getIconHeight()));

		LocalUserProperties props = LocalUserProperties.getInstance();
		labUserName = new JLabel(
		    props.getLoginResource(LocalUserProperties.KEY_LAB_USERNAME));
		labPassword = new JLabel(
		    props.getLoginResource(LocalUserProperties.KEY_LAB_PASSWORD));
		labMsgSpacer = new JLabel(" ");
		labLanguage = new JLabel(
		    props.getLoginResource(LocalUserProperties.KEY_LANG_REGION));
		rememberPass.setText(props.getLoginResource(LocalUserProperties.KEY_LANG_AUTOLOGIN));
		rememberPass.setOpaque(false);
		rememberPass.setEnabled(false);
		rememberPass.setVisible(false);
		//rememberPass.addFocusListener(new BackgroundListener());

		Color tx = ApplicationProperties.getInstance().getLoginPanelTextColor(
		    Color.BLACK);

		for(JLabel lab : new JLabel[] { labUserName, labPassword, /* labSpacer, */
		    labLanguage })
			lab.setForeground(tx);

//		Color bhi = ApplicationProperties.getInstance().getLoginPanelBorderHiColor(
//		    null);
//		Color bsh = ApplicationProperties.getInstance().getLoginPanelBorderShadeColor(
//		    null);

		this.setName("pnlLogin");
		this.setLayout(new BorderLayout());
		this.setOpaque(true);
		this.setBackground(new Color(0, 0, 0, 0));
		/*this.setBorder(bhi != null && bsh != null
		    ? BorderFactory.createEtchedBorder(bhi, bsh)
		    : BorderFactory.createEtchedBorder());
		*/
		this.add(pnlLogo, BorderLayout.NORTH);
		this.add(pnlLogin, BorderLayout.CENTER);

		pnlLogo.setLayout(new GridBagLayout());
		pnlLogo.setOpaque(false);
		pnlLogo.add(labLogo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
		    GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0,
		        0, 0, 0), 0, 0));

		pnlLogin.setLayout(new GridBagLayout());
		pnlLogin.setOpaque(false);
		pnlLogin.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final int iInsetBottom = 10;
		int y = 0;

		pnlLogin.add(labUserName, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0,
		        iInsetBottom, 10), 0, 0));
		pnlLogin.add(tfUserName, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
		        0, 0, iInsetBottom, 0), 0, 0));
		y++;
		pnlLogin.add(labPassword, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0,
		        iInsetBottom, 10), 0, 0));
		pnlLogin.add(tfPassword, new GridBagConstraints(1, y, 1, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
		        0, 0, iInsetBottom, 0), 0, 0));
		y++;
		pnlLogin.add(rememberPass, new GridBagConstraints(1, y, 1, 1, 0.0,
			0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, iInsetBottom, 0), 0, 0));
		y++;
		pnlLogin.add(labLanguage, new GridBagConstraints(0, y, 1, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0,
		        iInsetBottom, 10), 0, 0));
		pnlLogin.add(cmbbxLanguage, new GridBagConstraints(1, y, 1, 1, 0.0,
		    0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL,
		    new Insets(0, 0, iInsetBottom, 0), 0, 0));
		y++;
		pnlLogin.add(progressbar, new GridBagConstraints(0, y, 2, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
		        0, 0, iInsetBottom, 0), 0, 0));
		labMsgSpacer.setPreferredSize(progressbar.getPreferredSize());
		pnlLogin.add(labMsgSpacer, new GridBagConstraints(0, y, 2, 1, 0.0, 0.0,
		    GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(
		        0, 0, iInsetBottom, 0), 0, 0));
	
		tfUserName.setName("tfUserName");
		tfUserName.setText("");
		tfUserName.setColumns(10);
		tfUserName.addFocusListener(new BackgroundListener());
		tfPassword.setName("tfPassword");
		tfPassword.setText("");
		tfPassword.setColumns(10);
		tfPassword.addFocusListener(new BackgroundListener());
		tfPassword.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {}
			@Override
			public void insertUpdate(DocumentEvent e) {
				if(bubble != null)
					bubble.dispose();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				if(bubble != null)
					bubble.dispose();
			}
		});
		cmbbxLanguage.setName("cmbbxLanguage");
		cmbbxLanguage.addFocusListener(new BackgroundListener());
		progressbar.setVisible(false);

		Color pbfg = ApplicationProperties.getInstance().getSplashProgressColor(
		    null);
		if(pbfg != null)
			progressbar.setForeground(pbfg);		
	}
	
	public static final synchronized LoginPanel getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new LoginPanel();
		}
		return INSTANCE;
	}
	
	void enableRememberCheckbox(boolean withRememberCheckbox) {
		rememberPass.setEnabled(withRememberCheckbox);
		rememberPass.setVisible(withRememberCheckbox);
	}
	
	JComboBox getLanguageComboBox() {
		return cmbbxLanguage;
	}
	
	JCheckBox getRememberPwCheckBox() {
		return rememberPass;
	}
	
	JPasswordField getPasswordField() {
		return tfPassword;
	}
	
	JTextField getUsernameField() {
		return tfUserName;
	}

	public void setProgressVisible(boolean b) {
		if(progressbar.isVisible() != b) {
			labMsgSpacer.setVisible(!b);
			progressbar.setVisible(b);
			validate();
		}
	}

	public void increaseProgress(int iProgress) {
		setProgressVisible(true);
		progressbar.setValue(progressbar.getValue() + iProgress);
		UIUtils.paintImmediately(progressbar);
	}

	public void hideLanguageSelection() {
		cmbbxLanguage.setVisible(false);
		labLanguage.setVisible(false);
	}

	public void setPasswordError(String msg) {
		if(bubble == null || !bubble.isVisible()) {
			bubble = new Bubble(tfPassword, "<html>" + msg + "</html>", 20);
			bubble.setVisible(true);
		}
	}

	private class BackgroundListener implements FocusListener {
		private Color selBg = new Color(255,255,200);
		private Color bg;
		@Override
        public void focusGained(FocusEvent e) {
			Component c = e.getComponent();
			if(c != null) {
				if(bg == null) bg = c.getBackground();
				c.setBackground(selBg);
			}
        }
		@Override
        public void focusLost(FocusEvent e) {
			Component c = e.getComponent();
			if(c != null) {
				c.setBackground(bg);
			}
        }
	}

} // class LoginPanel
