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
package org.nuclos.client.ui.collect;

import javax.swing.JComponent;

import org.nuclos.client.ui.collect.component.model.EditModel;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;

/**
 * Contains the visual fields (CollectableComponents and, optionally, other components) and SubForms to edit,
 * as part of a SearchPanel or a DetailsPanel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 * @todo getSubformProvider or implements SubFormProvider?
 */
public interface EditView extends CollectableComponentsProvider {

	/**
	 * @return the Swing root component containing the fields and subforms.
	 */
	JComponent getJComponent();

	/**
	 * @return the model containing the <code>CollectableComponentModel</code>s.
	 */
	EditModel getModel();

	/**
	 * makes the model of the components with the given field name consistent with their views.
	 * The views are iterated.
	 * @param sFieldName
	 * @throws CollectableFieldFormatException
	 */
	void makeConsistent(String sFieldName) throws CollectableFieldFormatException;

	/**
	 * makes the model consistent with this view. Iterates all collectable components in this view.
	 * @throws CollectableFieldFormatException
	 */
	public void makeConsistent() throws CollectableFieldFormatException;

	/**
	 * enables or disables all components.
	 * @param bEnabled
	 */
	void setComponentsEnabled(boolean bEnabled);

}  // interface EditView
