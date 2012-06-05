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
package org.nuclos.client.common;

import javax.swing.JComponent;

import org.nuclos.client.ui.ToolTipTextProvider;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.ui.collect.component.DelegatingCollectableComponent;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Collectable component for the nuclosState and nuclosStateNumber fields.
 * In search mode this component displays a combobox with all possible states for the given module,
 * in details mode it displays a textfield (usually disabled).
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public class NuclosCollectableStateControl extends DelegatingCollectableComponent implements ToolTipTextProvider {

	private final LabeledCollectableComponent labeledclctcomp;

	public NuclosCollectableStateControl(CollectableEntityField clctef, Boolean bSearchable) {
		if (bSearchable) {
			this.labeledclctcomp = new NuclosCollectableStateComboBox(clctef, bSearchable);
		}
		else {
			this.labeledclctcomp = new CollectableTextField(clctef, bSearchable);
		}
	}

	@Override
	public LabeledCollectableComponent getWrappedCollectableComponent() {
		return this.labeledclctcomp;
	}

	@Override
	public JComponent getJComponent() {
		return this.getWrappedCollectableComponent().getJComponent();
	}

	@Override
	public String getDynamicToolTipText() {
		return this.getWrappedCollectableComponent().getDynamicToolTipText();
	}

	/**
	 * @param bScalable
	 */
	@Override
	public void setScalable(boolean bln) {
		// only for special components
	}

	@Override
	public void setKeepAspectRatio(boolean keepAspectRatio) {
		// only for special components
	}

	@Override
	public void setNextFocusComponent(String sNextFocusComponent) {
		// don't set here
	}
}	// class NuclosCollectableStateControl
