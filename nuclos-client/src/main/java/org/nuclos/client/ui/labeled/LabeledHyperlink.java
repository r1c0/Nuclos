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

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.HyperlinkTextFieldWithButton;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.StringUtils;

/**
 * A labeled <code>Hyperlink</code>, a component to display a hyperlink.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class LabeledHyperlink extends LabeledTextComponent {

	private final HyperlinkTextFieldWithButton hyperlinkField;

	public LabeledHyperlink(LabeledComponentSupport support) {
		this(support, true, false);
	}

	public LabeledHyperlink(LabeledComponentSupport support, boolean isNullable, 
			boolean bSearchable) {
		this(support, isNullable, bSearchable, new HyperlinkTextFieldWithButton(support, bSearchable));
	}
	
	protected LabeledHyperlink(LabeledComponentSupport support, boolean isNullable, 
			boolean bSearchable, HyperlinkTextFieldWithButton hyperlinkTextFieldWithButton) {
		
		super(support, isNullable, String.class, null, bSearchable);
		this.hyperlinkField = hyperlinkTextFieldWithButton;
		initValidation(isNullable, String.class, null);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.hyperlinkField);
		}
		this.getJLabel().setLabelFor(this.hyperlinkField);
	}

	@Override
	protected JComponent getLayeredComponent(){
		return this.getHyperlink();
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.hyperlinkField;
	}

	@Override
	protected void setToolTipTextProviderForControl(final ToolTipTextProvider tooltiptextprovider) {
		ToolTipTextProvider labeledHyperlinkToolTipTextProvider = new ToolTipTextProvider() {
			@Override
            public String getDynamicToolTipText() {
				return StringUtils.concatHtml(tooltiptextprovider.getDynamicToolTipText(), 
						support.getValidationToolTip());
			}
		};
		super.setToolTipTextProviderForControl(labeledHyperlinkToolTipTextProvider);
	}

	public HyperlinkTextFieldWithButton getHyperlink() {
		return this.hyperlinkField;
	}

	@Override
	public JComponent getControlComponent() {
		return this.hyperlinkField;
	}

	@Override
	public JTextComponent getJTextComponent() {
		return this.hyperlinkField;
	}

	@Override
	protected void setControlsEditable(boolean bEditable) {
		this.hyperlinkField.setEditable(bEditable);
	}

	@Override
	public void setColumns(int iColumns) {
		this.hyperlinkField.setColumns(iColumns);
	}

	/*
	@Override
	public void setBackgroundColorProvider(ColorProvider colorproviderBackground) {
		super.setBackgroundColorProvider(colorproviderBackground);
		this.datechooser.setBackgroundColorProviderForTextField(colorproviderBackground);
	}
	 */

	@Override
	public void setName(String sName) {
		super.setName(sName);
		UIUtils.setCombinedName(this.hyperlinkField, sName, "hyperlink");
	}

}  // class LabeledHyperlink
