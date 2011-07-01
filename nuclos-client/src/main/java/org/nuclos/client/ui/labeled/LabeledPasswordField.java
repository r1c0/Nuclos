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
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.CommonJPasswordField;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.StringUtils;

/**
 * NUCLEUSINT-1142
 * @author hartmut.beckschulze
 * @version	01.00.00
 */

public class LabeledPasswordField extends LabeledTextComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final CommonJPasswordField tf = new CommonJPasswordField() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = LabeledPasswordField.this.getToolTipTextProviderForControl();
			return StringUtils.concatHtml(provider != null ? provider.getDynamicToolTipText() : super.getToolTipText(ev), LabeledPasswordField.this.getValidationToolTip());
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = LabeledPasswordField.this.getBackgroundColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}
	};

	public LabeledPasswordField(){
		this(true, String.class, null, false);
	}

	public LabeledPasswordField(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		initValidation(isNullable, javaClass, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.tf);
		}
		this.getJLabel().setLabelFor(this.tf);

		/** no need to search in a password field */
		if (bSearchable) {
			tf.setEnabled(false);
		}
	}

	@Override
	protected JComponent getLayeredComponent(){
		return this.tf;
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.tf;
	}

	public CommonJPasswordField getPasswordField() {
		return this.tf;
	}

	public JLabel getLabel(){
		return this.getJLabel();
	}

	/**
	 * @return the text field
	 */
	@Override
	public JTextComponent getJTextComponent() {
		return this.getPasswordField();
	}

	/**
	 * sets the number of columns of the textfield
	 * @param iColumns
	 */
	@Override
	public void setColumns(int iColumns) {
		this.getPasswordField().setColumns(iColumns);
	}

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.tf, sName, "tf");
	}

}  // class LabeledTextField
