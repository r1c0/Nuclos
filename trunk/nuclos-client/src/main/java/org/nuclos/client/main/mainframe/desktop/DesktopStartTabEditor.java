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
package org.nuclos.client.main.mainframe.desktop;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.resource.ResourceDelegate;
import org.nuclos.client.ui.ColorChooserButton;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.CommonLocaleDelegate;

public class DesktopStartTabEditor {
	
	private static final Logger LOG = Logger.getLogger(DesktopStartTabEditor.class);
	
	private final JDialog dialog;
	private final JPanel contentPanel;
	
	private final JRadioButton rbWrapLayout, rbOneRowLayout, rbMenuItemTextHorizontalAlignLeft, rbMenuItemTextHorizontalAlignCenter, rbMenuItemTextHorizontalAlignRight;
	private final JTextField tfHorizontalGab, tfVerticalGap, tfMenuItemTextSize, tfMenuItemTextHorizontalPadding;
	private final ColorChooserButton btMenuItemTextColor, btMenuItemTextHoverColor;
	private final ResourceIconChooser.Button btResourceMenuBackground, btResourceMenuBackgroundHover, btResourceBackground;	
	
	private final JButton btSave;
	private final JButton btCancel;
	
	private boolean saved;
	
	private int iHorizontalGap, iVerticalGap, iLayout, iMenuItemTextSize, iMenuItemTextHorizontalPadding, iMenuItemTextHorizontalAlignment;
	private Color colorMenuItemText, colorMenuItemTextHover;
	private String sResourceMenuBackground, sResourceMenuBackgroundHover, sResourceBackground;
	
	public DesktopStartTabEditor(WorkspaceDescription.Desktop desktopPrefs) {
		iLayout = desktopPrefs.getLayout();
		iHorizontalGap = desktopPrefs.getHorizontalGap();
		iVerticalGap = desktopPrefs.getVerticalGap();
		iMenuItemTextHorizontalPadding = desktopPrefs.getMenuItemTextHorizontalPadding();
		iMenuItemTextHorizontalAlignment = desktopPrefs.getMenuItemTextHorizontalAlignment();
		if (desktopPrefs.getMenuItemTextColor() != null)
			colorMenuItemText = desktopPrefs.getMenuItemTextColor().toColor();
		if (desktopPrefs.getMenuItemTextHoverColor() != null)
			colorMenuItemTextHover = desktopPrefs.getMenuItemTextHoverColor().toColor();
		iMenuItemTextSize = desktopPrefs.getMenuItemTextSize();
		sResourceMenuBackground = desktopPrefs.getResourceMenuBackground();
		sResourceMenuBackgroundHover = desktopPrefs.getResourceMenuBackgroundHover();
		sResourceBackground = desktopPrefs.getResourceBackground();
		
		dialog = new JDialog(Main.getMainFrame(), CommonLocaleDelegate.getMessage("DesktopStartTabEditor.1","Desktop bearbeiten"), true);
		contentPanel = new JPanel();
		initJPanel(contentPanel,
				new double[] {TableLayout.PREFERRED, 10, TableLayout.FILL},
				new double[] {TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  10,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.FILL,
							  10,
							  TableLayout.PREFERRED});
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel lbLayout = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.2","Layout"), JLabel.TRAILING);
        contentPanel.add(lbLayout, "0, 0");
        ButtonGroup bgLayout = new ButtonGroup();
        rbOneRowLayout = new JRadioButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.3","Einzeilig zentriert"));
        rbWrapLayout = new JRadioButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.4","Mit Umbruch"));
        bgLayout.add(rbOneRowLayout);
        bgLayout.add(rbWrapLayout);
        switch (iLayout) {
        case WorkspaceDescription.Desktop.LAYOUT_ONE_ROW:
        	rbOneRowLayout.setSelected(true);
        	break;
        case WorkspaceDescription.Desktop.LAYOUT_WRAP:
        	rbWrapLayout.setSelected(true);
        	break;
        }
        JPanel layoutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        layoutPanel.add(rbOneRowLayout);
        layoutPanel.add(rbWrapLayout);
        contentPanel.add(layoutPanel, "2, 0");
        
        JPanel backgroundPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btResourceBackground = new ResourceIconChooser.Button(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.21","Hintergrundbild"), sResourceBackground);
        backgroundPanel.add(btResourceBackground);
        contentPanel.add(backgroundPanel, "2, 1");
        
        JLabel lbGabs = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.5","Abstände"), JLabel.TRAILING);
        contentPanel.add(lbGabs, "0, 2");
        JLabel lbHorizontal = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.6","Horizontal"), JLabel.TRAILING);
        JLabel lbVertical = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.7","Vertikal"), JLabel.TRAILING);
        tfHorizontalGab = new JTextField(iHorizontalGap+"", 5);
        tfVerticalGap = new JTextField(iVerticalGap+"", 5);
        JPanel gapsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gapsPanel.add(lbHorizontal);
        gapsPanel.add(tfHorizontalGab);
        gapsPanel.add(lbVertical);
        gapsPanel.add(tfVerticalGap);
        contentPanel.add(gapsPanel, "2, 2");
        
        contentPanel.add(new JSeparator(JSeparator.HORIZONTAL), "2, 3, 2, 3");
        
        JLabel lbMenuButtonLabel = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.8","Menu Button Beschriftung"), JLabel.TRAILING);
        contentPanel.add(lbMenuButtonLabel, "0, 4");
        JLabel lbFontSize = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.10","Größe"), JLabel.TRAILING);
        tfMenuItemTextSize = new JTextField(iMenuItemTextSize + "", 5);
        btMenuItemTextColor = new ColorChooserButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.9","Farbe"), colorMenuItemText, dialog);
        btMenuItemTextHoverColor = new ColorChooserButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.11","Mouseover"), colorMenuItemTextHover, dialog);
        JPanel menuButtonPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel1.add(lbFontSize);
        menuButtonPanel1.add(tfMenuItemTextSize);
        menuButtonPanel1.add(btMenuItemTextColor);
        menuButtonPanel1.add(btMenuItemTextHoverColor);
        contentPanel.add(menuButtonPanel1, "2, 4");
        
        ButtonGroup bgMenuItemTextAlignment = new ButtonGroup();
        rbMenuItemTextHorizontalAlignLeft = new JRadioButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.17","Links"));
        rbMenuItemTextHorizontalAlignCenter = new JRadioButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.18","Zentriert"));
        rbMenuItemTextHorizontalAlignRight = new JRadioButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.19","Rechts"));
        bgMenuItemTextAlignment.add(rbMenuItemTextHorizontalAlignLeft);
        bgMenuItemTextAlignment.add(rbMenuItemTextHorizontalAlignCenter);
        bgMenuItemTextAlignment.add(rbMenuItemTextHorizontalAlignRight);
        switch (iMenuItemTextHorizontalAlignment) {
        case (WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_LEFT):
        	rbMenuItemTextHorizontalAlignLeft.setSelected(true);
        	break;
        case (WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_CENTER):
        	rbMenuItemTextHorizontalAlignCenter.setSelected(true);
        	break;
        case (WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_RIGHT):
        	rbMenuItemTextHorizontalAlignRight.setSelected(true);
        	break;
        }
        JPanel menuButtonPanel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel4.add(rbMenuItemTextHorizontalAlignLeft);
        menuButtonPanel4.add(rbMenuItemTextHorizontalAlignCenter);
        menuButtonPanel4.add(rbMenuItemTextHorizontalAlignRight);
        contentPanel.add(menuButtonPanel4, "2, 5");
        
        JLabel lbMenuItemTextHorizontalPadding = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.20","Horizontaler Abstand"), JLabel.TRAILING);
        tfMenuItemTextHorizontalPadding = new JTextField(iMenuItemTextHorizontalPadding + "", 5);
        JPanel menuButtonPanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel5.add(lbMenuItemTextHorizontalPadding);
        menuButtonPanel5.add(tfMenuItemTextHorizontalPadding);
        contentPanel.add(menuButtonPanel5, "2, 6");
        
        JLabel lbMenuItemBackground = new JLabel(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.22","Menu Button Hintergründe"), JLabel.TRAILING);
        contentPanel.add(lbMenuItemBackground, "0, 7");
        btResourceMenuBackground = new ResourceIconChooser.Button(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.12","Bild"), sResourceMenuBackground);
        JPanel menuButtonPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel2.add(btResourceMenuBackground);
        contentPanel.add(menuButtonPanel2, "2, 7");
        btResourceMenuBackgroundHover = new ResourceIconChooser.Button(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.13","Bild Mouseover"), sResourceMenuBackgroundHover);
        JPanel menuButtonPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel3.add(btResourceMenuBackgroundHover);
        contentPanel.add(menuButtonPanel3, "2, 8");
        
        contentPanel.add(new JSeparator(JSeparator.VERTICAL), "1, 0, 1, 9");
        contentPanel.add(new JSeparator(JSeparator.HORIZONTAL), "0, 10, 2, 10");
        
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		btSave = new JButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.14","Speichern"));
		btCancel = new JButton(CommonLocaleDelegate.getMessage("DesktopStartTabEditor.15","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, "0, 11, 2, 11");
		
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-300, mfBounds.y+(mfBounds.height/2)-200, 600, 400);
		dialog.setResizable(false);
		
		initListener();
		if (!ResourceDelegate.getInstance().containsIconResources()) {
			btResourceBackground.setEnabled(false);
			btResourceMenuBackground.setEnabled(false);
			btResourceMenuBackgroundHover.setEnabled(false);
		}
		dialog.setVisible(true);
	}
	
	private void initListener() {
		btSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					iHorizontalGap = Integer.parseInt(tfHorizontalGab.getText());
					iVerticalGap = Integer.parseInt(tfVerticalGap.getText());
					iMenuItemTextSize = Integer.parseInt(tfMenuItemTextSize.getText());
					iMenuItemTextHorizontalPadding = Integer.parseInt(tfMenuItemTextHorizontalPadding.getText());
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(contentPanel, CommonLocaleDelegate.getMessage("DesktopStartTabEditor.16","Abstände und Schriftgröße dürfen nur Zahlen enthalten!"));
					return;
				}
				if (rbWrapLayout.isSelected()) {
					iLayout = WorkspaceDescription.Desktop.LAYOUT_WRAP;
				} else {
					iLayout = WorkspaceDescription.Desktop.LAYOUT_ONE_ROW; //default
				}
				
				if (rbMenuItemTextHorizontalAlignCenter.isSelected()) {
					iMenuItemTextHorizontalAlignment = WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_CENTER;
				} else if (rbMenuItemTextHorizontalAlignRight.isSelected()) {
					iMenuItemTextHorizontalAlignment = WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_RIGHT;
				} else {
					iMenuItemTextHorizontalAlignment = WorkspaceDescription.Desktop.HORIZONTAL_ALIGNMENT_LEFT; // default
				}
				
				colorMenuItemText = btMenuItemTextColor.getColor();
				colorMenuItemTextHover = btMenuItemTextHoverColor.getColor();
				sResourceMenuBackground = btResourceMenuBackground.getResource();
				sResourceMenuBackgroundHover = btResourceMenuBackgroundHover.getResource();
				sResourceBackground = btResourceBackground.getResource();
				
				saved = true;
				dialog.dispose();
			}
		});
		
		btCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispose();
			}
		});
	}
	
	public boolean isSaved() {
		return saved;
	}	
	
	public int getHorizontalGap() {
		return iHorizontalGap;
	}

	public int getVerticalGap() {
		return iVerticalGap;
	}

	public int getLayout() {
		return iLayout;
	}

	public int getMenuItemTextSize() {
		return iMenuItemTextSize;
	}

	public int getMenuItemTextHorizontalPadding() {
		return iMenuItemTextHorizontalPadding;
	}

	public int getMenuItemTextHorizontalAlignment() {
		return iMenuItemTextHorizontalAlignment;
	}

	public Color getColorMenuItemText() {
		return colorMenuItemText;
	}

	public Color getColorMenuItemTextHover() {
		return colorMenuItemTextHover;
	}

	public String getResourceMenuBackground() {
		return sResourceMenuBackground;
	}

	public String getResourceMenuBackgroundHover() {
		return sResourceMenuBackgroundHover;
	}

	public String getResourceBackground() {
		return sResourceBackground;
	}

	protected void initJPanel(JPanel panel, double[] cols, double[] rows) {	
		final double size [][] = {cols, rows};
		final TableLayout layout = new TableLayout(size);
		
		layout.setVGap(3);
		layout.setHGap(5);
		
		panel.setLayout(layout);
	}
}
