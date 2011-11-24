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
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.common2.CommonLocaleDelegate;

public class ColorChooserButton extends JButton {

	private Color color;
	
	public ColorChooserButton(String text, Color color, final JDialog parent) {
		super(text);
		this.color = color;
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JPanel contentPanel = new JPanel(new BorderLayout());
				JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
				JButton btSave = new JButton(CommonLocaleDelegate.getMessage("ColorChooserButton.2","Speichern"));
				JButton btCancel = new JButton(CommonLocaleDelegate.getMessage("ColorChooserButton.3","Abbrechen"));
				actionsPanel.add(btSave);
				actionsPanel.add(btCancel);
				contentPanel.add(actionsPanel, BorderLayout.SOUTH);
				
				final JColorChooser colorChooser = getColor()!=null?new JColorChooser(getColor()):new JColorChooser();
				contentPanel.add(colorChooser, BorderLayout.CENTER);
				
				final JDialog dialog;
				if (parent==null) {
					dialog = new JDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("ColorChooserButton.1","Farbe ändern"), true);
				} else {
					dialog = new JDialog(parent, CommonLocaleDelegate.getMessage("ColorChooserButton.1","Farbe ändern"), true);
				}
				dialog.setContentPane(contentPanel);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.getRootPane().setDefaultButton(btSave);
				Rectangle mfBounds = Main.getMainFrame().getBounds();
				dialog.setBounds(mfBounds.x+(mfBounds.width/2)-300, mfBounds.y+(mfBounds.height/2)-200, 600, 400);
				dialog.setResizable(false);
				
				btSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setColor(colorChooser.getColor());
						dialog.dispose();
					}
				});
				
				btCancel.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.dispose();
					}
				});
				
				dialog.setVisible(true);
			}
		});
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
		repaint();
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		Graphics2D g2 = (Graphics2D) g;
		Object renderingHint = g2
				.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		Rectangle bounds = getBounds();
		
		int height = bounds.height-6;
		int width = height;
		int y = 3;
		int x = bounds.width-3-width;
		
		if (color != null) {
			g2.setColor(color);
			g2.fillRoundRect(0, 0, x, y, width, height);
		}

		g2.setColor(NuclosSyntheticaConstants.BACKGROUND_DARK);
		g2.drawRoundRect(0, 0, x, y, width, height);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				renderingHint);
	}
	
	
}
