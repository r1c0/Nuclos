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

import java.util.Date;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.StringUtils;

/**
 * A labeled <code>DateChooser</code>, a component to display and enter a date.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class LabeledDateChooser extends LabeledTextComponent {

	private final DateChooser datechooser;

	public LabeledDateChooser(LabeledComponentSupport support) {
		this(support, false);
	}

	public LabeledDateChooser(LabeledComponentSupport support, boolean bTodayIsRelative) {
		this(support, bTodayIsRelative, true, null, false);
	}

	public LabeledDateChooser(LabeledComponentSupport support, boolean bTodayIsRelative, boolean isNullable, 
			String inputFormat, boolean bSearchable) {
		
		super(support, isNullable, Date.class, inputFormat, bSearchable);
		this.datechooser = new DateChooser(support, bTodayIsRelative);
		initValidation(isNullable, Date.class, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.datechooser);
		}
		this.getJLabel().setLabelFor(this.datechooser);
	}

	public LabeledDateChooser(LabeledComponentSupport support, boolean bTodayIsRelative, boolean isNullable, 
			String inputFormat, String outputFormat, boolean bSearchable) {
		
		super(support, isNullable, Date.class, inputFormat, bSearchable);
		this.datechooser = new DateChooser(support, bTodayIsRelative);
		if(outputFormat != null && outputFormat.length() > 1)
			this.datechooser.setOutputFormat(outputFormat);
		initValidation(isNullable, Date.class, inputFormat);
		if(this.validationLayer != null){
			this.addControl(this.validationLayer);
		} else {
			this.addControl(this.datechooser);
		}
		this.getJLabel().setLabelFor(this.datechooser);
	}

	@Override
	protected JComponent getLayeredComponent(){
		return this.getDateChooser();
	}

	@Override
	protected JTextComponent getLayeredTextComponent(){
		return this.getDateChooser().getJTextField();
	}

	@Override
	protected void setToolTipTextProviderForControl(final ToolTipTextProvider tooltiptextprovider) {
		ToolTipTextProvider labeledDateChooserToolTipTextProvider = new ToolTipTextProvider() {
			@Override
            public String getDynamicToolTipText() {
				return StringUtils.concatHtml(tooltiptextprovider.getDynamicToolTipText(), 
						support.getValidationToolTip());
			}
		};
		super.setToolTipTextProviderForControl(labeledDateChooserToolTipTextProvider);
		// this.datechooser.setToolTipTextProvider(labeledDateChooserToolTipTextProvider);
	}

	public DateChooser getDateChooser() {
		return this.datechooser;
	}

	@Override
	public JComponent getControlComponent() {
		return this.datechooser;
	}

	@Override
	public JTextComponent getJTextComponent() {
		return this.getDateChooser().getJTextField();
	}

	@Override
	protected void setControlsEditable(boolean bEditable) {
		this.getDateChooser().setEditable(bEditable);
	}

	@Override
	public void setColumns(int iColumns) {
		datechooser.setColumns(iColumns);
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
		UIUtils.setCombinedName(this.datechooser, sName, "datechooser");
	}

}  // class LabeledDateChooser
