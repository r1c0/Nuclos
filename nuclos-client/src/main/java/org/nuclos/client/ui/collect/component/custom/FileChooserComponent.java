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
package org.nuclos.client.ui.collect.component.custom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;

import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.TextFieldWithButton;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.common2.CommonLocaleDelegate;

/**
 * Component that displays a filename and lets the user choose a file via a JFileChooser.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 * @todo move to org.nuclos.client.ui or make package local
 */

public class FileChooserComponent extends JPanel {
	private final JLabel labIcon = new JLabel();
	
	private final TextFieldWithButton cmpFileName = new TextFieldWithButton(Icons.getInstance().getIconTextFieldButtonFile()) {
		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = FileChooserComponent.this.tooltiptextprovider;
			return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
		}

		@Override
		public boolean isButtonEnabled() {
			return FileChooserComponent.this.btnBrowse.isEnabled();
		}

		@Override
		public void buttonClicked() {
			FileChooserComponent.this.btnBrowse.doClick();
		}

		@Override
		protected boolean fadeLeft() {
			return false;
		}
	};

	private final JButton btnBrowse = new JButton("...");

	private ToolTipTextProvider tooltiptextprovider;

	public FileChooserComponent() {
		super(new BorderLayout(2, 0));
		this.setOpaque(true);
		this.init();
	}

	private void init() {
		this.add(labIcon, BorderLayout.WEST);
		this.add(cmpFileName, BorderLayout.CENTER);
		//this.add(btnBrowse, BorderLayout.EAST);
		this.btnBrowse.setToolTipText(CommonLocaleDelegate.getMessage("file.chooser.component.tooltip", "Datei ausw\u00e4hlen"));

		this.labIcon.setOpaque(false);

		//Color colorBack = cmpFileName.getBackground();
		this.cmpFileName.setBorder(new EmptyBorder(0,2,0,2));
		//cmpFileName.setBackground(colorBack);
		this.cmpFileName.setEditable(false);
//		this.cmpFileName.setOpaque(false);

		this.btnBrowse.setMargin(new Insets(0, 2, 0, 2));
		this.btnBrowse.setOpaque(false);
	}

	public String getFileName() {
		return this.cmpFileName.getText();
	}

	public void setFileName(String sFileName) {
		this.cmpFileName.setText(sFileName);
	}

	@Override
	public void setToolTipText(String sToolTip) {
		this.cmpFileName.setToolTipText(sToolTip);
	}

	public void setToolTipTextProviderForLabel(ToolTipTextProvider tooltiptextprovider) {
		this.tooltiptextprovider = tooltiptextprovider;
		if(tooltiptextprovider != null) {
			ToolTipManager.sharedInstance().registerComponent(this.cmpFileName);
		}
	}

	public void setIcon(Icon icon) {
		labIcon.setIcon(icon);
	}

	public JComponent getFileNameComponent() {
		return this.cmpFileName;
	}

	public JButton getBrowseButton() {
		return this.btnBrowse;
	}

	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		if(labIcon != null) {
			labIcon.setBackground(bg);
			cmpFileName.setBackground(bg);
			btnBrowse.setBackground(bg);
		}
	}

	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
		if(labIcon != null) {
			labIcon.setForeground(fg);
			cmpFileName.setForeground(fg);
			btnBrowse.setForeground(fg);
		}
	}

}  // class FileChooserComponent
