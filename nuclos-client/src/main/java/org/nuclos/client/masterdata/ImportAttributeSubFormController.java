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
package org.nuclos.client.masterdata;

import java.awt.Component;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;

/**
 * MasterDataSubFormController for import attributes.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class ImportAttributeSubFormController extends MasterDataSubFormController {

   public ImportAttributeSubFormController(Component parent, JComponent parentMdi,
      CollectableComponentModelProvider clctcompmodelproviderParent,
      String sParentEntityName, SubForm subform,
      Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
      super(parent, parentMdi, clctcompmodelproviderParent,
         sParentEntityName, subform, prefsUserParent, entityPrefs, valueListProviderCache);
   }

   @Override
	public void addChildSubFormController(MasterDataSubFormController childSubFormController) {
		super.addChildSubFormController(childSubFormController);

		if (childSubFormController instanceof ImportForeignEntityIdentifierSubFormController) {
			((ImportForeignEntityIdentifierSubFormController)childSubFormController).setParentController(this);
		}
	}
}
