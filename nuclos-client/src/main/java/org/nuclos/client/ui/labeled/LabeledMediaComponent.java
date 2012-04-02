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
import javax.swing.JLabel;

import org.jdesktop.jxlayer.JXLayer;

/**
 * A labeled text component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class LabeledMediaComponent extends LabeledComponent {

	public LabeledMediaComponent() {
		super();
	}

	public abstract JLabel getJMediaComponent();

	@Override
	public JComponent getControlComponent() {
		return this.getJMediaComponent();
	}

	// ***** Layered *****
	protected JXLayer<JComponent> validationLayer;
	protected boolean needLayeredValidation;

	public LabeledMediaComponent(boolean isNullable, Class<?> javaClass, String inputFormat, boolean bSearchable) {
		super();
		this.needLayeredValidation = checkIfNeedLayeredValidation(isNullable, inputFormat, bSearchable);
	}

	private boolean checkIfNeedLayeredValidation(boolean isNullable, String inputFormat, boolean bSearchable) {
		return !bSearchable && (!isNullable || (inputFormat != null && inputFormat.trim().length() > 0));
	}

	protected abstract JComponent getLayeredComponent();

	protected abstract JLabel getLayeredLabel();
}  // class LabeledTextComponent
