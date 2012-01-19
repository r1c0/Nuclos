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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.jdesktop.jxlayer.JXLayer;
import org.nuclos.client.ui.ValidationLayerFactory;
import org.nuclos.client.ui.ValidationLayerFactory.InputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.NullableInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.RangeInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.RegExpInputValidator;
import org.nuclos.client.ui.ValidationLayerFactory.TypeInputValidator;

/**
 * A labeled text component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class LabeledTextComponent extends LabeledComponent {

	public LabeledTextComponent() {
		super();
	}

	public abstract JTextComponent getJTextComponent();

	@Override
	public JComponent getControlComponent() {
		return this.getJTextComponent();
	}

	@Override
	protected void setControlsEditable(boolean bEditable) {
		this.getJTextComponent().setEditable(bEditable);
	}

	// ***** Layered *****
	protected JXLayer<JComponent> validationLayer;
	protected List<InputValidator<JTextComponent>> inputValidators;
	protected boolean needLayeredValidation;

	public LabeledTextComponent(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super();
		this.needLayeredValidation = checkIfNeedLayeredValidation(isNullable, inputFormat, bSearchable);
	}

	private boolean checkIfNeedLayeredValidation(boolean isNullable, String inputFormat, boolean bSearchable) {
		return !bSearchable && (!isNullable || (inputFormat != null && inputFormat.trim().length() > 0));
	}

	protected abstract JComponent getLayeredComponent();
	protected abstract JTextComponent getLayeredTextComponent();

	protected void initValidators(boolean isNullable, Class<?> javaClass, String inputFormat){
		this.inputValidators = new ArrayList<InputValidator<JTextComponent>>();
		if(!isNullable){
			inputValidators.add(new NullableInputValidator<JTextComponent>(getLayeredTextComponent()));
		}
		if(inputFormat != null && inputFormat.trim().length() > 0){
			if(javaClass.equals(String.class)){
				inputValidators.add(new RegExpInputValidator<JTextComponent>(getLayeredTextComponent(), inputFormat));
			} else {
				inputValidators.add(new RangeInputValidator<JTextComponent>(getLayeredTextComponent(), javaClass, inputFormat));
			}
		} else {
			if(!javaClass.equals(String.class)){
				inputValidators.add(new TypeInputValidator<JTextComponent>(getLayeredTextComponent(), javaClass));
			}
		}
	}
	
	protected String getValidationToolTip(){
		StringBuffer validationToolTip = new StringBuffer("");
		validationToolTip.append("<html><body>");
		int i = 0;
		if(inputValidators != null){
			for(InputValidator<JTextComponent> validator : inputValidators){
				if (i > 0) validationToolTip.append("<br/>");
				validationToolTip.append(validator.getValidationMessage());
			}
			i++;
		}
		validationToolTip.append("</body></html>");
		return validationToolTip.toString();
	}

	protected void initValidation(boolean isNullable, Class<?> javaClass, String inputFormat) {
		//JComponent controlComponent = getLayeredComponent();
		if(this.needLayeredValidation){
			initValidators(isNullable, javaClass, inputFormat);
			if(this.inputValidators != null && this.inputValidators.size() > 0){
				this.validationLayer = ValidationLayerFactory.createValidationLayer(getLayeredComponent(), new ArrayList<InputValidator<?>>(inputValidators));
				//controlComponent = this.layer;
			}
		}
		//this.addControl(controlComponent);
	}	
}  // class LabeledTextComponent
