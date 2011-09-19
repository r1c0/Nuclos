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

import java.awt.Component;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;

/**
 * Controller for collecting dependant data (in a one-to-many relationship) in a subform,
 * without the hassles concerning ValueObjectList or parent id.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public abstract class SimpleDetailsSubFormController<Clct extends Collectable> extends AbstractDetailsSubFormController<Clct> {

	protected SimpleDetailsSubFormController(Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, SubForm subform,
			Preferences prefsUserParent, CollectableFieldsProviderFactory clctfproviderfactory) {

		super(DefaultCollectableEntityProvider.getInstance().getCollectableEntity(subform.getEntityName()), parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform, prefsUserParent, clctfproviderfactory);

		this.postCreate();
	}

	/**
	 * @param lstclct
	 * @return the given list itself.
	 * @postcondition result == lstclct
	 */
	@Override
	protected List<Clct> newCollectableList(List<Clct> lstclct) {
		return lstclct;
	}

}	// class SimpleDetailsSubFormController
