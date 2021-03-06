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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.nuclos.client.main.Main;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.common2.SpringLocaleDelegate;

public class ColorChooserButton extends JButton {

	private Color color;
	
	private final JMenuItem miDelete;
	
	private final List<ColorChangeListener> colorChangeListeners = new ArrayList<ColorChangeListener>();
	
	@SuppressWarnings("serial")
	public ColorChooserButton(String text, Color color, final JDialog parent) {
		super(text);
		this.color = color;
		
		addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
				JPanel contentPanel = new JPanel(new BorderLayout());
				JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
				JButton btSave = new JButton(localeDelegate.getMessage("ColorChooserButton.2","Speichern"));
				JButton btCancel = new JButton(localeDelegate.getMessage("ColorChooserButton.3","Abbrechen"));
				actionsPanel.add(btSave);
				actionsPanel.add(btCancel);
				contentPanel.add(actionsPanel, BorderLayout.SOUTH);
				
				final JColorChooser colorChooser = getColor()!=null?new JColorChooser(getColor()):new JColorChooser();
				contentPanel.add(colorChooser, BorderLayout.CENTER);
				
				final JDialog dialog;
				if (parent==null) {
					dialog = new JDialog(Main.getInstance().getMainFrame(), localeDelegate.getMessage("ColorChooserButton.1","Farbe ändern"), true);
				} else {
					dialog = new JDialog(parent, localeDelegate.getMessage("ColorChooserButton.1","Farbe ändern"), true);
				}
				dialog.setContentPane(contentPanel);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.getRootPane().setDefaultButton(btSave);
				Rectangle mfBounds = Main.getInstance().getMainFrame().getBounds();
				dialog.setBounds(mfBounds.x+(mfBounds.width/2)-300, mfBounds.y+(mfBounds.height/2)-200, 600, 400);
				dialog.setResizable(false);
				
				btSave.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						setColor(colorChooser.getColor());
						dialog.dispose();
						fireColorChanged();
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
		
		final JPopupMenu popup = new JPopupMenu();
		miDelete = new JMenuItem(new AbstractAction(SpringLocaleDelegate.getInstance().getMessage("ColorChooserButton.4", "Löschen"), Icons.getInstance().getIconRealDelete16()) {
			@Override
			public void actionPerformed(ActionEvent e) {
				setColor(null);
				fireColorChanged();
			}
		});
		miDelete.setEnabled(color != null);
		popup.add(miDelete);
		setComponentPopupMenu(popup);
	}

	public Color getColor() {
		return color;
	}
	
	public void setColor(Color color) {
		this.color = color;
		miDelete.setEnabled(color != null);
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

		g2.setColor(NuclosThemeSettings.BACKGROUND_COLOR3);
		g2.drawRoundRect(0, 0, x, y, width, height);
		
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				renderingHint);
	}
	
	public void addColorChangeListener(ColorChangeListener ccl) {
		this.colorChangeListeners.add(ccl);
	}
	
	public void removeColorChangeListener(ColorChangeListener ccl) {
		this.colorChangeListeners.remove(ccl);
	}
	
	private void fireColorChanged() {
		for (ColorChangeListener ccl : colorChangeListeners) {
			ccl.colorChanged(color);
		}
	}
	
	public static interface ColorChangeListener {
		public void colorChanged(Color newColor);
	}
}
