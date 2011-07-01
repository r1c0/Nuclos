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
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.Timer;

import org.apache.log4j.Logger;

import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">Florian.Speidel</a>
 * @version 01.00.00
 */
public class NuclosMessagePanel extends JPanel {
	private final Logger log = Logger.getLogger(this.getClass());

	/**
	 * the interval for flashing (1 second).
	 */
	private static final int FLASH_INTERVAL = 1000;

	/**
	 * the text to display in the "messages button".
	 */
	private static final String MESSAGESBUTTON_TEXT = CommonLocaleDelegate.getMessage("NucleusStatusBar.1","Meldungen");

	/**
	 * Is flashing activated currently?
	 */
	private boolean bFlashing = false;

	/**
	 * (When flashing is activated:) Is the button highlighted currently?
	 */
	private boolean bHighlighted = false;

	/**
	 * Timer that controls the flashing.
	 */
	private Timer timer;

	public final JToggleButton btnNotify = new JToggleButton(MESSAGESBUTTON_TEXT);

	public NuclosMessagePanel() {
		super(new GridBagLayout());

		this.init();
	}

	private void init() {
		setLayout(new BorderLayout());
		
		btnNotify.setBackground(Color.WHITE);
		btnNotify.setForeground(Color.BLACK);
		btnNotify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				// user hits the flashing button --> recognizes new message(s)
				stopFlashing();
				Main.getMainController().getNotificationDialog().setVisible(btnNotify.isSelected());
			}
		});
		btnNotify.setFocusPainted(false);
		
		add(btnNotify, BorderLayout.CENTER);
		setMaximumSize(getPreferredSize());
	}

	public synchronized void startFlashing() {
		if (!bFlashing) {
			bFlashing = true;
			if (this.timer != null) {
				this.timer.stop();
			}
			this.timer = new Timer(FLASH_INTERVAL, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent ev) {
					setHighlighted(!bHighlighted);
				}
			});
			this.timer.start();
		}
	}

	private void stopFlashing() {
		this.bFlashing = false;
		if (timer != null) {
			timer.stop();
		}
		this.setHighlighted(false);
	}

	private void setHighlighted(boolean bHighlighted) {
		log.debug("Highlighted: " + bHighlighted);
		if (UIUtils.isWindowsXPLookAndFeel()) {
			btnNotify.setForeground(bHighlighted ? Color.RED : Color.BLACK);
			UIUtils.setFontStyleBold(btnNotify, bHighlighted);
		}
		else {
			// As of Java 1.5, this doesn't work with the Windows XP Look&Feel:
			btnNotify.setBackground(bHighlighted ? Color.RED : Color.WHITE);
		}
		this.bHighlighted = bHighlighted;
	}

	public void toggleButton() {
		this.btnNotify.doClick();
	}

}
