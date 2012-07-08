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

import java.awt.Color;

import org.apache.log4j.Logger;
import org.nuclos.client.common.Utils;
import org.nuclos.client.ui.ColorChooserButton;
import org.nuclos.client.ui.ColorChooserButton.ColorChangeListener;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * A <code>CollectableComponent</code> that presents a color.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@nuclos.de">Maik Stueker</a>
 * @version	01.00.00
 */
public class CollectableColorChooserButton extends AbstractCollectableComponent {

	private static final Logger LOG = Logger.getLogger(CollectableColorChooserButton.class);

	private final ColorChangeListener colorChangeListener = new ColorChangeListener() {

		@Override
		public void colorChanged(Color newColor) {
			try {
				CollectableColorChooserButton.this.viewToModel();
			}
			catch (CollectableFieldFormatException ex) {
				assert false;
			}
		}
	};

	/**
	 * @param clctef
	 * @postcondition this.isDetailsComponent()
	 */
	public CollectableColorChooserButton(CollectableEntityField clctef) {
		this(clctef, false);

		assert this.isDetailsComponent();
	}

	public CollectableColorChooserButton(CollectableEntityField clctef, boolean bSearchable) {
		super(clctef, new ColorChooserButton(clctef.getLabel(), null, null), bSearchable);
		getColorChooserButton().addColorChangeListener(colorChangeListener);
	}

	@Override
	public ColorChooserButton getJComponent() {
		return (ColorChooserButton) super.getJComponent();
	}

	private ColorChooserButton getColorChooserButton() {
		return (ColorChooserButton) super.getJComponent();
	}

	@Override
	protected void updateView(CollectableField clctfValue) {
		final String sRes = (String) clctfValue.getValue();
		final Color color = sRes==null?null:Color.decode(sRes);
		getColorChooserButton().setColor(color);
	}

	@Override
    public CollectableField getFieldFromView() {
		final ColorChooserButton btn = this.getColorChooserButton();
		Color color = btn.getColor();
		Object result = color==null? null: Utils.colorToHexString(color);
		return new CollectableValueField(result);
	}

	@Override
	public void setLabelText(String sLabel) {
		this.getColorChooserButton().setText(sLabel);
	}

	@Override
	public void setInsertable(boolean bInsertable) {
		// do nothing
	}

}  // class CollectableCheckBox
