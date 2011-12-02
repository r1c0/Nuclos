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
package org.nuclos.client.ui.labeled;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.nuclos.client.synthetica.NuclosSyntheticaConstants;
import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.TextFieldWithButton;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;

/**
 * A labeled combobox.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LabeledComboBox extends LabeledComponent {
	
	private static final Logger LOG = Logger.getLogger(LabeledComboBox.class);
	
	public static final Dimension DEFAULT_PREFERRED_SIZE = (new JTextField()).getPreferredSize();
	
	private final TextFieldWithButton tfDisabled = new TextFieldWithButton(Icons.getInstance().getIconTextFieldButtonCombobox()) {
		
		@Override
		public boolean isButtonEnabled() {
			return false;
		}
		
		@Override
		public void buttonClicked() {
		}
		
		@Override
		public String getToolTipText(MouseEvent ev) {
			return cmbbx.getToolTipText(ev);
		}
		
		@Override
		public Color getBackground() {
			return NuclosSyntheticaConstants.BACKGROUND_INACTIVEFIELD;
		}
		
	};
	
	private final JComboBox cmbbx = new JComboBox() {

		/**
		 * Note that this (a dynamic tooltip) doesn't really work for JComboBox. The editor and the button (or the CellRenderer and the button,
		 * for a non-editable combobox) have their own tooltips, which are kept in sync with the JComboBox's tooltip.
		 * This method is only called when the mouse is over the border of the combobox.
		 * One workaround could be to use a custom editor, but the ComboBoxEditor is something that is look&feel dependent.
		 * We could, however, implement this workaround for BasicUI and for the other UIs, leave it as it is.
		 * @param ev
		 */
		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = LabeledComboBox.this.getToolTipTextProviderForControl();
			return (provider != null) ? provider.getDynamicToolTipText() : super.getToolTipText(ev);
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = LabeledComboBox.this.getBackgroundColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}

		@Override
		public void setFont(Font font) {
			super.setFont(font);
			tfDisabled.setFont(font);
		}

		@Override
		public void setComponentPopupMenu(JPopupMenu popup) {
			super.setComponentPopupMenu(popup);
			tfDisabled.setComponentPopupMenu(popup);
		}

		@Override
		public synchronized void addMouseListener(MouseListener l) {
			super.addMouseListener(l);
			if (l instanceof JPopupMenuListener) {
				tfDisabled.addMouseListener(l);
			}
		}

		@Override
		public Point getLocation() {
			if (blnControlsEnabled) {
				return super.getLocation();
			} else {
				return tfDisabled.getLocation();
			}
		}

		@Override
		public Point getLocationOnScreen() {
			if (blnControlsEnabled) {
				return super.getLocationOnScreen();
			} else {
				return tfDisabled.getLocationOnScreen();
			}
		}
		
		@Override
		public void setEnabled(boolean b) {
			// ignore !!!
		}
	};
	private final JTextField cmbbxTextField = (JTextField) cmbbx.getEditor().getEditorComponent();

	public LabeledComboBox() {
		super();

		this.cmbbx.setMinimumSize(new Dimension(this.cmbbx.getMinimumSize().width, DEFAULT_PREFERRED_SIZE.height));
		this.addControl(this.cmbbx);
		this.getJLabel().setLabelFor(this.cmbbx);
		
		this.tfDisabled.setEditable(false);
		this.tfDisabled.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {}
			@Override
			public void focusGained(FocusEvent e) {
				tfDisabled.selectAll();
			}
		});
		this.cmbbx.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				transferToDisabled();
			}
		});
	}
	
	public synchronized void setupJPopupMenuListener(JPopupMenuListener popupmenulistener) {
		final Component comp = getJComboBox().getEditor() != null ?
				getJComboBox().getEditor().getEditorComponent() :
				getControlComponent();

		comp.addMouseListener(popupmenulistener);
		tfDisabled.addMouseListener(popupmenulistener);
	}

	public JComboBox getJComboBox() {
		return this.cmbbx;
	}

	@Override
	public JComponent getControlComponent() {
		return this.cmbbx;
	}
	
	private boolean blnControlsEnabled = true;

	@Override
	protected void setControlsEnabled(boolean blnEnabled) {
		boolean blnUpdate = false;
		if (blnControlsEnabled != blnEnabled) {
			blnUpdate = true;
		}
		blnControlsEnabled = blnEnabled;
		
		if (blnUpdate) {
			if (blnControlsEnabled) {
				replaceControl(tfDisabled, cmbbx);
			} else {
				transferToDisabled();
				replaceControl(cmbbx, tfDisabled);
			}
		}
	}
	
	private void transferToDisabled() {
		final String text;
		if (cmbbx.getSelectedItem() != null) {
			text = cmbbx.getSelectedItem().toString();
		} else {
			text = cmbbxTextField.getText();
		}
		LOG.debug("Transfer Text id=" + cmbbx.getName() + ", text=" + text);
		tfDisabled.setText(text);
	}

	@Override
	protected void setControlsEditable(boolean bEditable) {
		this.getJComboBox().setEditable(bEditable);
	}

	@Override
	public void addMouseListenerToHiddenComponents(MouseListener l) {
		tfDisabled.addMouseListener(l);
		cmbbx.addMouseListener(l);
	}

	@Override
	public void removeMouseListenerFromHiddenComponents(MouseListener l) {
		tfDisabled.removeMouseListener(l);
		cmbbx.removeMouseListener(l);
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.cmbbx, sName, "cmbbx");
	}

}  // class LabeledComboBox
