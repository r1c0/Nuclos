//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.nuclos.api.context.InputDelegate;
import org.nuclos.api.context.InputValidationException;
import org.nuclos.client.main.Main;

@SuppressWarnings("serial")
public class InputDelegatePane extends JPanel {

	private final JButton ok;
	private final JButton cancel;

	private final InputDelegate ics;

	public InputDelegatePane(InputDelegate ics) {
		this.ics = ics;
		this.ok = new JButton("Ok");
		this.cancel = new JButton("Cancel");
		
		JPanel controlpane = new JPanel();
		controlpane.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
		controlpane.add(ok);
		controlpane.add(cancel);
		
		this.setLayout(new BorderLayout());
		this.add(controlpane, BorderLayout.SOUTH);
	}

	public Map<String, Serializable> show(final JDialog dialog, Map<String, Serializable> data, JComponent parent) {
		JPanel pane = ics.initialize(data);
		pane.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
		this.add(pane, BorderLayout.CENTER);
		dialog.setComponentOrientation(this.getComponentOrientation());
		Container contentPane = dialog.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(this, BorderLayout.CENTER);
		dialog.setModal(true);
		dialog.setResizable(true);

		final AtomicReference<Map<String, Serializable>> result = new AtomicReference<Map<String, Serializable>>(null);

		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});

		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					result.set(ics.evaluate());
					dialog.setVisible(false);
				}
				catch (InputValidationException ex) {
					JOptionPane.showMessageDialog(dialog, ex.getMessage(), Main.getInstance().getMainFrame().getTitle(), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		dialog.pack();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);

		return result.get();
	}
}
