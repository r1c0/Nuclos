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
package org.nuclos.client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import org.nuclos.common2.CommonLocaleDelegate;

/**
 * A panel to display exceptions to the user.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

class ExceptionMessagePanel extends JPanel {

	private JDialog dlg;
	private final JPanel pnl1 = new JPanel();
	private final JPanel pnlMain = new JPanel();
	private final JPanel pnlDetails = new JPanel();
	private final JPanel pnlMainMessage = new JPanel();
	private final JPanel pnlMainButtons = new JPanel();
	private final GridBagLayout gbl = new GridBagLayout();
	private final JPanel pnlDetailsButtons = new JPanel();
	final JButton btnCopy = new JButton();
	private final JButton btnSend = new JButton();
	private final JPanel pnlDetailsMessage = new JPanel();
	final JScrollPane scrlpnDetails = new JScrollPane();
	private JLabel labIcon;
	private final JToggleButton btnDetails = new JToggleButton();
	final JButton btnOK = new JButton();

	final JTextArea taMessage = new JTextArea();
	final JEditorPane epDetails = new JEditorPane();

	public ExceptionMessagePanel(JDialog dlg, int iMessageType) {
		this.dlg = dlg;

		// set icon:
		// @todo use look and feel specific icons
		if (iMessageType != JOptionPane.PLAIN_MESSAGE) {
			labIcon = new JLabel(JOptionPaneIcons.getImageIcon(LookAndFeel.METAL, iMessageType));
			labIcon.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
		}

		this.init();

		taMessage.setBackground(this.getBackground());
		pnlDetails.setVisible(false);
		// the details section is initially invisible
	}

	private void init() {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		
		this.setLayout(new BorderLayout());
		this.add(pnl1, BorderLayout.CENTER);

		pnl1.setLayout(this.gbl);
		pnl1.add(pnlMain, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));
		pnl1.add(pnlDetails, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(0, 0, 0, 0), 0, 0));

		// main:
		pnlMain.setLayout(new BorderLayout());
		pnlMain.add(pnlMainMessage, BorderLayout.CENTER);
		pnlMain.add(pnlMainButtons, BorderLayout.SOUTH);

		pnlMainMessage.setLayout(new GridBagLayout());
		pnlMainMessage.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		pnlMainMessage.add(labIcon, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		// error message scrollable
		JScrollPane scrollable = new JScrollPane(taMessage);
		pnlMainMessage.add(scrollable, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

//		taMessage.setColumns(35);
//		taMessage.setWrapStyleWord(true);
//		taMessage.setLineWrap(true);
		taMessage.setEditable(false);

		pnlMainButtons.setLayout(new GridBagLayout());
		JPanel pnlMainButtons2 = new JPanel();
		pnlMainButtons2.setLayout(new GridLayout(1, 0, 5, 0));
		pnlMainButtons2.add(btnOK, null);
		pnlMainButtons2.add(btnDetails, null);
		pnlMainButtons.add(pnlMainButtons2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(10, 20, 10, 20), 0, 0));
		btnOK.setText("OK");
		btnOK.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				dlg.setVisible(false);
				dlg.dispose();
			}
		});
		btnDetails.setText(cld.getMessage("ExceptionMessagePanel.1", "Details")+" >>");
		btnDetails.addItemListener(new java.awt.event.ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent ev) {
				// actionPerformed would also work
				final boolean bDetailsVisible = (ev.getStateChange() == ItemEvent.SELECTED);
				adjustDialog(bDetailsVisible);
			}
		});

		// details:
		pnlDetails.setLayout(new BorderLayout());
		final Border borderDetails = BorderFactory.createCompoundBorder(new TitledBorder(
				BorderFactory.createEtchedBorder(Color.white, new Color(142, 142, 142)), "Details"),
				BorderFactory.createEmptyBorder(0, 5, 0, 5));
		pnlDetails.setBorder(borderDetails);
		pnlDetails.add(pnlDetailsMessage, BorderLayout.CENTER);
		pnlDetails.add(pnlDetailsButtons, BorderLayout.SOUTH);

		pnlDetailsMessage.setBorder(BorderFactory.createEmptyBorder());
		pnlDetailsMessage.setLayout(new BorderLayout());
		pnlDetailsMessage.add(scrlpnDetails, BorderLayout.CENTER);

		scrlpnDetails.getViewport().add(epDetails, null);
//		pnlDetailsMessage.add(epDetails, BorderLayout.CENTER);
		scrlpnDetails.getViewport().setPreferredSize(new Dimension(500, 200));
		scrlpnDetails.getViewport().setViewPosition(new Point(0, 0));
		epDetails.setEditable(false);
		epDetails.setContentType("text/html");

		pnlDetailsButtons.add(btnCopy);
		pnlDetailsButtons.add(btnSend);

		btnCopy.setText(cld.getMessage("ClipboardUtils.Copy", "Kopieren"));
		btnCopy.setMnemonic('C');
		btnCopy.setToolTipText(cld.getMessage("ExceptionMessagePanel.2", "Fehlerbericht in die Zwischenablage kopieren"));

		btnSend.setEnabled(false);
		btnSend.setText(cld.getMessage("ExceptionMessagePanel.3", "Senden"));
		btnSend.setMnemonic('S');
		btnSend.setToolTipText(cld.getMessage("ExceptionMessagePanel.4", "Fehlerbericht senden"));
	}

	void adjustDialog(boolean bDetailsVisible) {
		// if the details pane is visible, it fills all the vertical space.
		// Otherwise, the main panel should fill all vertical space.
		final GridBagConstraints gbcPnlMain = gbl.getConstraints(pnlMain);
		gbcPnlMain.weighty = (bDetailsVisible ? 0.0 : 1.0);
		gbl.setConstraints(pnlMain, gbcPnlMain);
		pnlMain.invalidate();

		// adjust dialog size:
		final Dimension sizeDlg = dlg.getSize();

		pnlDetails.setVisible(bDetailsVisible);
		epDetails.invalidate();

		final Dimension newSize;
		if (bDetailsVisible) {
			final Dimension prefSizeDetails = pnlDetails.getPreferredSize();
			final int iHeightDetails = Math.max(pnlDetails.getHeight(), prefSizeDetails.height);
			newSize = new Dimension(Math.max(sizeDlg.width, prefSizeDetails.width), sizeDlg.height + iHeightDetails);
		}
		else {
			final int iHeightDetails = pnlDetails.getHeight();
			newSize = new Dimension(sizeDlg.width, sizeDlg.height - iHeightDetails);
			// leave width unchanged
		}
		dlg.setSize(newSize);

		btnDetails.setText(bDetailsVisible ? CommonLocaleDelegate.getInstance().getMessage(
				"ExceptionMessagePanel.1", "Details")+" <<" 
				: CommonLocaleDelegate.getInstance().getMessage("ExceptionMessagePanel.1", "Details")+" >>");

		scrlpnDetails.getViewport().setViewPosition(new Point(0, 0));

		// recalculate layout and repaint:
		dlg.validate();
	}

}  // class ExceptionMessagePanel
