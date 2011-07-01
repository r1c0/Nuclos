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
package org.nuclos.client.ui.collect.component;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.nuclos.client.ui.labeled.LabeledComponent;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * Default (abstract) implementation of a <code>CollectableComponent</code>,
 * consisting of a label and a second ("control") component.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class LabeledCollectableComponent extends AbstractCollectableComponent {

	protected LabeledCollectableComponent(CollectableEntityField clctef, LabeledComponent labcomp, boolean bSearchable) {
		super(clctef, labcomp, bSearchable);

		getLabeledComponent().setToolTipTextProviderForControl(this);

		getLabeledComponent().setBackgroundColorProvider(new BackgroundColorProvider());
	}

	public LabeledComponent getLabeledComponent() {
		return (LabeledComponent) getJComponent();
	}

	@Override
	public void setInsertable(boolean bInsertable) {
	}

	@Override
	public void setLabelText(String sLabel) {
		getLabeledComponent().setLabelText(sLabel);
	}

	@Override
	public void setMnemonic(char cMnemonic) {
		getLabeledComponent().setMnemonic(cMnemonic);
	}

	public JLabel getJLabel() {
		return getLabeledComponent().getJLabel();
	}

	@Override
	public JComponent getControlComponent() {
		return getLabeledComponent().getControlComponent();
	}

	@Override
	public void setFillControlHorizontally(boolean bFill) {
		getLabeledComponent().setFillControlHorizontally(bFill);
	}
}  // class LabeledCollectableComponent
