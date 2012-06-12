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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import org.nuclos.client.ui.util.TableLayoutBuilder;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common2.SpringLocaleDelegate;

public class DesktopStartTabEditor {
	
	private static final Logger LOG = Logger.getLogger(DesktopStartTabEditor.class);
	
	private final JDialog dialog;
	private final JPanel contentPanel;
	
	private final JCheckBox ckbxRootpaneBgColor, chbxStaticMenu;
	private final JRadioButton rbWrapLayout, rbOneRowLayout, rbMenuItemTextHorizontalAlignLeft, rbMenuItemTextHorizontalAlignCenter, rbMenuItemTextHorizontalAlignRight;
	private final JTextField tfHorizontalGab, tfVerticalGap, tfMenuItemTextSize, tfMenuItemTextHorizontalPadding;
	private final ColorChooserButton btMenuItemTextColor, btMenuItemTextHoverColor;
	private final ResourceIconChooser.Button btResourceMenuBackground, btResourceMenuBackgroundHover, btResourceBackground;	
	
	private final JButton btSave;
	private final JButton btCancel;
	
	private boolean saved;
	
	private boolean bRootpaneBackgroundColor, bStaticMenu;
	private int iHorizontalGap, iVerticalGap, iLayout, iMenuItemTextSize, iMenuItemTextHorizontalPadding, iMenuItemTextHorizontalAlignment;
	private Color colorMenuItemText, colorMenuItemTextHover;
	private String sResourceMenuBackground, sResourceMenuBackgroundHover, sResourceBackground;
	
	public DesktopStartTabEditor(WorkspaceDescription.Desktop desktopPrefs) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
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
		bRootpaneBackgroundColor = desktopPrefs.isRootpaneBackgroundColor();
		bStaticMenu = desktopPrefs.isStaticMenu();
		
		dialog = new JDialog(Main.getInstance().getMainFrame(), localeDelegate.getMessage(
				"DesktopStartTabEditor.1","Desktop bearbeiten"), true);
		contentPanel = new JPanel();
		initJPanel(contentPanel,
				new double[] {TableLayout.PREFERRED, 15, TableLayout.FILL},
				new double[] {TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  20,
							  TableLayout.PREFERRED,			 
							  TableLayout.PREFERRED,
							  TableLayout.PREFERRED,
							  TableLayout.FILL,
							  10,
							  TableLayout.PREFERRED});
		contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		JLabel lbLayout = createFrontLabel(localeDelegate.getMessage("DesktopStartTabEditor.2","Layout"));
        contentPanel.add(lbLayout, "0, 0");
        ButtonGroup bgLayout = new ButtonGroup();
        rbOneRowLayout = new JRadioButton(localeDelegate.getMessage("DesktopStartTabEditor.3","Einzeilig zentriert"));
        rbWrapLayout = new JRadioButton(localeDelegate.getMessage("DesktopStartTabEditor.4","Mit Umbruch"));
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
        btResourceBackground = new ResourceIconChooser.Button(localeDelegate.getMessage(
        		"DesktopStartTabEditor.21","Hintergrundbild"), sResourceBackground, null);
        backgroundPanel.add(btResourceBackground);
        contentPanel.add(backgroundPanel, "2, 1");
        
        ckbxRootpaneBgColor = new JCheckBox(localeDelegate.getMessage("DesktopStartTabEditor.23","Rootpane Hintergrund Farbe"));
        ckbxRootpaneBgColor.setSelected(bRootpaneBackgroundColor);
        contentPanel.add(ckbxRootpaneBgColor, "2, 2");
        
        JLabel lbGabs = createFrontLabel(localeDelegate.getMessage("DesktopStartTabEditor.5","Abstände"));
        contentPanel.add(lbGabs, "0, 3");
        JLabel lbHorizontal = new JLabel(localeDelegate.getMessage("DesktopStartTabEditor.6","Horizontal"), JLabel.TRAILING);
        JLabel lbVertical = new JLabel(localeDelegate.getMessage("DesktopStartTabEditor.7","Vertikal"), JLabel.TRAILING);
        tfHorizontalGab = new JTextField(iHorizontalGap+"", 5);
        tfVerticalGap = new JTextField(iVerticalGap+"", 5);
        JPanel gapsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        gapsPanel.add(lbHorizontal);
        gapsPanel.add(tfHorizontalGab);
        gapsPanel.add(lbVertical);
        gapsPanel.add(tfVerticalGap);
        contentPanel.add(gapsPanel, "2, 3");
        
        JLabel lbMenuButtonLabel = createFrontLabel(localeDelegate.getMessage("DesktopStartTabEditor.8","Menu Button"));
        contentPanel.add(lbMenuButtonLabel, "0, 5");
        chbxStaticMenu = new JCheckBox(localeDelegate.getMessage("DesktopStartTabEditor.25","Menus immer anzeigen"));
        chbxStaticMenu.setSelected(bStaticMenu);
        contentPanel.add(chbxStaticMenu, "2, 5");
        
        JPanel menuItem1 = new JPanel();
        menuItem1.setBorder(BorderFactory.createTitledBorder(localeDelegate.getMessage("DesktopStartTabEditor.24","Menu Einträge")));
        TableLayoutBuilder mi1 = new TableLayoutBuilder(menuItem1).columns(TableLayout.PREFERRED);
                
        JLabel lbFontSize = new JLabel(localeDelegate.getMessage("DesktopStartTabEditor.10","Größe"), JLabel.TRAILING);
        tfMenuItemTextSize = new JTextField(iMenuItemTextSize + "", 5);
        btMenuItemTextColor = new ColorChooserButton(localeDelegate.getMessage(
        		"DesktopStartTabEditor.9","Farbe"), colorMenuItemText, dialog);
        btMenuItemTextHoverColor = new ColorChooserButton(localeDelegate.getMessage(
        		"DesktopStartTabEditor.11","Mouseover"), colorMenuItemTextHover, dialog);
        JPanel menuButtonPanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel1.add(lbFontSize);
        menuButtonPanel1.add(tfMenuItemTextSize);
        menuButtonPanel1.add(btMenuItemTextColor);
        menuButtonPanel1.add(btMenuItemTextHoverColor);
        mi1.newRow().add(menuButtonPanel1);
        
        ButtonGroup bgMenuItemTextAlignment = new ButtonGroup();
        rbMenuItemTextHorizontalAlignLeft = new JRadioButton(
        		localeDelegate.getMessage("DesktopStartTabEditor.17","Links"));
        rbMenuItemTextHorizontalAlignCenter = new JRadioButton(
        		localeDelegate.getMessage("DesktopStartTabEditor.18","Zentriert"));
        rbMenuItemTextHorizontalAlignRight = new JRadioButton(
        		localeDelegate.getMessage("DesktopStartTabEditor.19","Rechts"));
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
        mi1.newRow().add(menuButtonPanel4);
        
        JLabel lbMenuItemTextHorizontalPadding = new JLabel(localeDelegate.getMessage(
        		"DesktopStartTabEditor.20","Horizontaler Abstand"), JLabel.TRAILING);
        tfMenuItemTextHorizontalPadding = new JTextField(iMenuItemTextHorizontalPadding + "", 5);
        JPanel menuButtonPanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel5.add(lbMenuItemTextHorizontalPadding);
        menuButtonPanel5.add(tfMenuItemTextHorizontalPadding);
        mi1.newRow().add(menuButtonPanel5);
        
        contentPanel.add(menuItem1, "2, 6");
        
        JPanel menuItem2 = new JPanel();
        menuItem2.setBorder(BorderFactory.createTitledBorder(localeDelegate.getMessage("DesktopStartTabEditor.22","Menu Eintrag Hintergrund")));
        TableLayoutBuilder mi2 = new TableLayoutBuilder(menuItem2).columns(TableLayout.PREFERRED);
        
        btResourceMenuBackground = new ResourceIconChooser.Button(localeDelegate.getMessage(
        		"DesktopStartTabEditor.12","Bild"), sResourceMenuBackground, null);
        JPanel menuButtonPanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel2.add(btResourceMenuBackground);
        mi2.newRow().add(menuButtonPanel2);
        btResourceMenuBackgroundHover = new ResourceIconChooser.Button(localeDelegate.getMessage(
        		"DesktopStartTabEditor.13","Bild Mouseover"), sResourceMenuBackgroundHover, null);
        JPanel menuButtonPanel3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        menuButtonPanel3.add(btResourceMenuBackgroundHover);
        mi2.newRow().add(menuButtonPanel3);
        
        contentPanel.add(menuItem2, "2, 7");
        
        contentPanel.add(new JSeparator(JSeparator.VERTICAL), new TableLayoutConstraints(1, 0, 1, 8));
        contentPanel.add(new JSeparator(JSeparator.HORIZONTAL), "0, 9, 2, 9");
        
		JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
		btSave = new JButton(localeDelegate.getMessage("DesktopStartTabEditor.14","Speichern"));
		btCancel = new JButton(localeDelegate.getMessage("DesktopStartTabEditor.15","Abbrechen"));
		actionsPanel.add(btSave);
		actionsPanel.add(btCancel);
		contentPanel.add(actionsPanel, "0, 10, 2, 10");
		
		dialog.setContentPane(contentPanel);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.getRootPane().setDefaultButton(btSave);
		Rectangle mfBounds = Main.getInstance().getMainFrame().getBounds();
		dialog.setBounds(mfBounds.x+(mfBounds.width/2)-300, mfBounds.y+(mfBounds.height/2)-250, 600, 500);
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
					JOptionPane.showMessageDialog(contentPanel, SpringLocaleDelegate.getInstance().getMessage(
							"DesktopStartTabEditor.16","Abstände und Schriftgröße dürfen nur Zahlen enthalten!"));
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
				bRootpaneBackgroundColor = ckbxRootpaneBgColor.isSelected();
				bStaticMenu = chbxStaticMenu.isSelected();
				
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
	
	private JLabel createFrontLabel(String label) {
		JLabel result = new JLabel(label, JLabel.TRAILING);
		result.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 15));
		return result;
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

	public boolean isRootpaneBackgroundColor() {
		return bRootpaneBackgroundColor;
	}

	public boolean isStaticMenu() {
		return bStaticMenu;
	}

	protected void initJPanel(JPanel panel, double[] cols, double[] rows) {	
		final double size [][] = {cols, rows};
		final TableLayout layout = new TableLayout(size);
		
		layout.setVGap(3);
		layout.setHGap(5);
		
		panel.setLayout(layout);
	}
}
