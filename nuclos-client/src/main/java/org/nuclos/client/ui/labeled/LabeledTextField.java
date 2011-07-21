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
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.ColorProvider;
import org.nuclos.client.ui.CommonJTextField;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.URIMouseAdapter;
import org.nuclos.common2.StringUtils;

/**
 * <code>CollectableComponent</code> that presents a value in a <code>JTextField</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class LabeledTextField extends LabeledTextComponent {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JTextField tf = new CommonJTextField() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public String getToolTipText(MouseEvent ev) {
			final ToolTipTextProvider provider = LabeledTextField.this.getToolTipTextProviderForControl();
			return StringUtils.concatHtml(provider != null ? provider.getDynamicToolTipText() : super.getToolTipText(ev), LabeledTextField.this.getValidationToolTip());
		}

		@Override
		public Color getBackground() {
			final ColorProvider colorproviderBackground = LabeledTextField.this.getBackgroundColorProvider();
			final Color colorDefault = super.getBackground();
			return (colorproviderBackground != null) ? colorproviderBackground.getColor(colorDefault) : colorDefault;
		}
	};
	
	public LabeledTextField(){
		this(true, String.class, null, false);
	}
	
	public LabeledTextField(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super(isNullable, javaClass, inputFormat, bSearchable);
		initValidation(isNullable, javaClass, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.tf);
		}
		this.getJLabel().setLabelFor(this.tf);
		setMouseListenerOnComponent();
	}
	
	protected void setMouseListenerOnComponent() {
		if(getTextField() != null) {
			getTextField().addMouseListener(new URIMouseAdapter());
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
	
	public JTextField getTextField() {
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
