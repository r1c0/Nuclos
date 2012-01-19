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

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.nuclos.common.collect.collectable.CollectableComponentTypes;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.client.genericobject.CollectableGenericObjectFileChooser;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentType;
import org.nuclos.client.ui.collect.component.CollectableDateChooser;
import org.nuclos.client.ui.collect.component.DefaultCollectableComponentFactory;
import org.nuclos.common.ParameterProvider;

/**
 * Factory that creates <code>CollectableComponent</code>s in Nucleus.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class NuclosCollectableComponentFactory extends DefaultCollectableComponentFactory {

	private Color colorFocusBackground = null;
	private FocusListener focuslistener = null;

	@Override
	public CollectableComponent newCollectableComponent(CollectableEntity clcte, String sFieldName, CollectableComponentType clctcomptype, boolean bSearchable) {
		final CollectableComponent result = this.newCollectableComponent(clcte.getEntityField(sFieldName), clctcomptype, bSearchable);
		result.setCollectableEntity(clcte);
		return result;
	}

	@Override
	public CollectableComponent newCollectableComponent(CollectableEntityField clctef,
			CollectableComponentType clctcomptype, boolean bSearchable) {

		final CollectableComponent result = super.newCollectableComponent(clctef, clctcomptype, bSearchable);

		this.setFocusListener(result);

		return result;
	}

	@Override
	protected CollectableComponent newCollectableComponentByEnumeratedControlType(CollectableEntityField clctef, int iiControlType, boolean bSearchable) {
		final CollectableComponent result;

		switch (iiControlType) {
			case CollectableComponentTypes.TYPE_COMBOBOX:
				result = new NuclosCollectableComboBox(clctef, bSearchable);
				break;

			case CollectableComponentTypes.TYPE_TEXTAREA:
				result = new NuclosCollectableTextArea(clctef, bSearchable);
				break;

			case CollectableComponentTypes.TYPE_DATECHOOSER:
				// NucleusCollectableDataChooser has set bTodayIsRelative=bSearchable
				result = new CollectableDateChooser(clctef, bSearchable, bSearchable);
				break;

			case CollectableComponentTypes.TYPE_IDTEXTFIELD:
				result = new NuclosCollectableIdTextField(clctef, bSearchable);
				break;

			case CollectableComponentTypes.TYPE_LISTOFVALUES:
				result = new NuclosCollectableListOfValues(clctef, bSearchable);
				break;

			case CollectableComponentTypes.TYPE_FILECHOOSER:
				result = new CollectableGenericObjectFileChooser(clctef, bSearchable);
				break;
				
			case CollectableComponentTypes.TYPE_IMAGE:
				result = new NuclosCollectableImage(clctef, bSearchable);
				break;
				
			case CollectableComponentTypes.TYPE_PASSWORDFIELD:
				//NUCLEUSINT-1142
				result = new NuclosCollectablePasswordField(clctef, bSearchable);
				break;
				
			default:
				result = super.newCollectableComponentByEnumeratedControlType(clctef, iiControlType, bSearchable);
		}

		return result;
	}

	private void setFocusListener(CollectableComponent clctcomp) {
		if (colorFocusBackground == null) {
			this.initializeFocusListener();
		}

		if (focuslistener != null) {
			clctcomp.getFocusableComponent().addFocusListener(focuslistener);
		}
	}

	private void initializeFocusListener() {
		colorFocusBackground = Utils.translateColorFromParameter(ParameterProvider.KEY_FOCUSSED_ITEM_BACKGROUND_COLOR);

		focuslistener = new FocusListener() {
			private Color color;

			@Override
            public void focusLost(FocusEvent ev) {
				ev.getComponent().setBackground(color);
			}

			@Override
            public void focusGained(FocusEvent ev) {
				color = ev.getComponent().getBackground();
				ev.getComponent().setBackground(colorFocusBackground);
			}
		};
	}

}	// class NuclosCollectableComponentFactory
