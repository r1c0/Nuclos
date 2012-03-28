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
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.RuntimeTextField;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.StringUtils;

/**
 * <code>CollectableComponent</code> that presents a runtime in a <code>JTextField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version	01.00.00
 */

public class LabeledRuntimeTextField extends LabeledTextComponent {

	private final JTextField tf = new RuntimeTextField() {

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = support.getToolTipTextProvider();
			return StringUtils.concatHtml(provider != null ? provider.getDynamicToolTipText() : super.getToolTipText(ev), 
					support.getValidationToolTip());
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = support.getColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}
	};
	
	public LabeledRuntimeTextField(){
		this(true, String.class, null, false);
	}
	
	public LabeledRuntimeTextField(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		initValidation(isNullable, javaClass, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.tf);
		}
		this.getJLabel().setLabelFor(this.tf);
	}
	
	@Override
	protected JComponent getLayeredComponent(){
		return this.tf;
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.tf;
	}
	
	public JTextField getTextField() {
		return this.tf;
	}

	/**
	 * @return the text field
	 */
	@Override
	public JTextComponent getJTextComponent() {
		return this.getTextField();
	}

	/**
	 * sets the number of columns of the textfield
	 * @param iColumns
	 */
	@Override
	public void setColumns(int iColumns) {
		this.getTextField().setColumns(iColumns);
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.tf, sName, "tf");
	}

}  // class LabeledTextField
