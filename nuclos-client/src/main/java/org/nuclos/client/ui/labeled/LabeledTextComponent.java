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

	// ***** Layered *****
	protected JXLayer<JComponent> validationLayer;
	protected boolean needLayeredValidation;

	protected LabeledTextComponent() {
		super();
	}

	protected LabeledTextComponent(LabeledComponentSupport support) {
		super(support);
	}

	protected LabeledTextComponent(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		this.needLayeredValidation = checkIfNeedLayeredValidation(isNullable, inputFormat, bSearchable);
	}

	protected LabeledTextComponent(LabeledComponentSupport support, boolean isNullable, 
			Class<?> javaClass, String inputFormat, boolean bSearchable) {
		
		this.needLayeredValidation = checkIfNeedLayeredValidation(isNullable, inputFormat, bSearchable);
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

	private boolean checkIfNeedLayeredValidation(boolean isNullable, String inputFormat, boolean bSearchable) {
		return !bSearchable && (!isNullable || (inputFormat != null && inputFormat.trim().length() > 0));
	}

	protected abstract JComponent getLayeredComponent();
	protected abstract JTextComponent getLayeredTextComponent();

	protected void initValidation(boolean isNullable, Class<?> javaClass, String inputFormat) {
		if (needLayeredValidation){
			support.initValidators(getLayeredTextComponent(), isNullable, javaClass, inputFormat);
			final List<InputValidator<JTextComponent>> inputValidators = support.getInputValidators();
			if (inputValidators != null && inputValidators.size() > 0){
				validationLayer = ValidationLayerFactory.createValidationLayer(getLayeredComponent(), 
						new ArrayList<InputValidator<?>>(inputValidators));
			}
		}
	}
	
}  // class LabeledTextComponent
