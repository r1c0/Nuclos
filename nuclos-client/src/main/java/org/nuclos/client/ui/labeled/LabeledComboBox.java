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
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;

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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static Dimension DEFAULT_PREFFERED_SIZE = (new JTextField()).getPreferredSize();

	private final JComboBox cmbbx = new JComboBox() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

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
	};

	public LabeledComboBox() {
		super();

		this.cmbbx.setMinimumSize(new Dimension(this.cmbbx.getMinimumSize().width, DEFAULT_PREFFERED_SIZE.height));
		this.addControl(this.cmbbx);
		this.getJLabel().setLabelFor(this.cmbbx);
	}

	public JComboBox getJComboBox() {
		return this.cmbbx;
	}

	@Override
	public JComponent getControlComponent() {
		return this.cmbbx;
	}

	@Override
	protected void setControlsEditable(boolean bEditable) {
		this.getJComboBox().setEditable(bEditable);
	}

//	public void setToolTipTextProviderForControl(ToolTipTextProvider tooltiptextprovider) {
//		super.setToolTipTextProviderForControl(tooltiptextprovider);
//
//		// workaround for stupid BasicComboBoxUI implementation:
//		// the problem is that BasicComboBoxUI has child components (panel, button) that have their own tooltips.
//		if (tooltiptextprovider != null) {
//			this.setToolTipText(null);
//		}
//
////		final JComponent comp = this.getControlComponent();
////		for (int i = 0; i < comp.getComponents().length; i++) {
////			final Component compChild = comp.getComponents()[i];
////			if(compChild instanceof JComponent) {
////				ToolTipManager.sharedInstance().unregisterComponent((JComponent) compChild);
////			}
////		}
//	}
//
//	public void setToolTipText(String sToolTipText) {
//		this.getJLabel().setToolTipText(sToolTipText);
//
//		if(this.getToolTipTextProviderForControl() == null && sToolTipText != null) {
//			this.getControlComponent().setToolTipText(sToolTipText);
//		}
//	}

//	private Component getEditorComponent() {
//		final ComboBoxEditor editor = this.cmbbx.getEditor();
//		return (editor != null) ? editor.getEditorComponent() : null;
//	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.cmbbx, sName, "cmbbx");
	}

}  // class LabeledComboBox
