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

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.StartIcons;
import org.nuclos.client.ui.BackgroundPanel;
import org.nuclos.client.ui.Bubble;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common2.exception.CommonBusinessException;
import org.springframework.security.core.AuthenticationException;

public class ChangePasswordPanel extends BackgroundPanel implements DocumentListener {

	private static final Logger LOG = Logger.getLogger(ChangePasswordPanel.class);

	private final JLabel lbOld = new JLabel();
	private final JLabel lbNew1 = new JLabel();
	private final JLabel lbNew2 = new JLabel();

	private final JPasswordField pfOld = new JPasswordField();
	private final JPasswordField pfNew1 = new JPasswordField();
	private final JPasswordField pfNew2 = new JPasswordField();

	private final LocalUserProperties props = LocalUserProperties.getInstance();

	private Bubble bubble;

	public ChangePasswordPanel(boolean enableOldPassword, String oldpassword, boolean expired) {
		super(new GridLayout(2, 1));

		int row = 1;

		TableLayout layout = new TableLayout(new double[]{ 5.0, TableLayout.FILL, TableLayout.FILL, 5.0}, new double[]{5.0, 20.0, 20.0, 20.0, 20.0, 5.0});
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);
        this.setOpaque(false);

        if (expired) {
        	JLabel info = new JLabel();
        	info.setText(props.getLoginResource(LocalUserProperties.KEY_CHANGEPASSWORD_EXPIRED));
        	this.add(info, "1," + row + " 2," + row);
        	row++;
        }

		lbOld.setText(props.getLoginResource(LocalUserProperties.KEY_LAB_OLDPW));
		this.add(lbOld, "1," + row);

		pfOld.setText(oldpassword);
		pfOld.setEnabled(enableOldPassword);
		pfOld.addFocusListener(new BackgroundListener());
		pfOld.getDocument().addDocumentListener(this);
		this.add(pfOld, "2," + row);

		row++;

		lbNew1.setText(props.getLoginResource(LocalUserProperties.KEY_LAB_NEWPW1));
		this.add(lbNew1, "1," + row);

		pfNew1.setText("");
		pfNew1.addFocusListener(new BackgroundListener());
		pfNew1.getDocument().addDocumentListener(this);
		this.add(pfNew1, "2," + row);

		row++;

		lbNew2.setText(props.getLoginResource(LocalUserProperties.KEY_LAB_NEWPW2));
		this.add(lbNew2, "1," + row);

		pfNew2.setText("");
		pfNew2.addFocusListener(new BackgroundListener());
		pfNew2.getDocument().addDocumentListener(this);
		this.add(pfNew2, "2," + row);

		row++;
	}

	public boolean showInDialog(JFrame parent, final ChangePasswordDelegate delegate) {
		final JOptionPane optpn = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optpn.setInitialValue(null);
		optpn.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		String title = props.getLoginResource(LocalUserProperties.KEY_CHANGEPASSWORD_TITLE);
		final JDialog dialog = new JDialog(parent, title, true);
		dialog.setName("dlgLogin");
		dialog.setIconImage(StartIcons.getInstance().getDefaultFrameIcon().getImage());
		dialog.getContentPane().add(optpn);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				optpn.setValue(JOptionPane.CANCEL_OPTION);
			}
		});

		optpn.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName()) && dialog.isVisible() && (e.getSource() == optpn)) {
					Integer result = (Integer) e.getNewValue();
					if (result != optpn.getInitialValue() && result == JOptionPane.OK_OPTION) {
						try {
							final String new1 = new String(pfNew1.getPassword());
							final String new2 = new String(pfNew2.getPassword());
							if (!new1.equals(new2)) {
								throw new NuclosBusinessException(props.getLoginResource(LocalUserProperties.KEY_ERR_PASSWORD_MATCH));
							}

							delegate.changePassword(new String(pfOld.getPassword()), new String(pfNew1.getPassword()));
							dialog.setVisible(false);
						}
						catch (AuthenticationException ex) {
							setInputError(pfOld, ex.getMessage());
						}
						catch (CommonBusinessException ex) {
							setInputError(pfNew1, ex.getMessage());
						}
						finally {
							if (dialog.isVisible()) {
								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										try {
											optpn.setValue(optpn.getInitialValue());
										}
										catch (Exception e) {
											LOG.error("ChangePasswordPanel.setValue: " + e, e);
										}
									}
								});
							}
						}
					}
					else if (result != optpn.getInitialValue()) {
						dialog.setVisible(false);
					}
				}
			}
		});

		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
		return JOptionPane.OK_OPTION == (Integer) optpn.getValue();
	}

	private void setInputError(JComponent input, String error) {
		if (bubble != null) {
			bubble.dispose();
		}
		String message = error;
		if (LocalUserProperties.getInstance().containsLoginResource(error)) {
			message = LocalUserProperties.getInstance().getLoginResource(error);
		}
		bubble = new Bubble(input, "<html>" + message + "</html>", 20);
		bubble.setVisible(true);
	}

	public interface ChangePasswordDelegate {
		public void changePassword(String oldPw, String newPw) throws CommonBusinessException;
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if(bubble != null) {
			bubble.dispose();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if(bubble != null) {
			bubble.dispose();
		}
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if(bubble != null) {
			bubble.dispose();
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
}
