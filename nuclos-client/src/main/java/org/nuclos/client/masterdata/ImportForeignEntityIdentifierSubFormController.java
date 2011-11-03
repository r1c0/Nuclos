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
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.nuclos.client.masterdata.valuelistprovider.ReferencedEntityFieldCollectableFieldsProvider;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.collect.collectable.CollectableEntityField;

/**
 * MasterDataSubFormController for foreign entity identifiers.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
 * @version 01.00.00
 */
public class ImportForeignEntityIdentifierSubFormController extends MasterDataSubFormController {

	private CollectableComponentModelProvider clctcompmodelproviderParent;
	private ImportAttributeSubFormController parentController;

   public void setParentController(ImportAttributeSubFormController parentController) {
		this.parentController = parentController;
	}

	public ImportForeignEntityIdentifierSubFormController(Component parent, JComponent parentMdi,
      CollectableComponentModelProvider clctcompmodelproviderParent,
      String sParentEntityName, SubForm subform,
      Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
      super(parent, parentMdi, clctcompmodelproviderParent,
         sParentEntityName, subform, prefsUserParent, entityPrefs, valueListProviderCache);
      this.clctcompmodelproviderParent = clctcompmodelproviderParent;
   }

	@Override
	public TableCellEditor getTableCellEditor(JTable tbl, int row, CollectableEntityField clctefTarget) {
		TableCellEditor result = super.getTableCellEditor(tbl, row, clctefTarget);
		if (clctefTarget.getName().equals("attribute")) {
			CollectableComponentTableCellEditor celleditor = (CollectableComponentTableCellEditor)result;
			if (celleditor.getCollectableComponent() instanceof CollectableComboBox) {
				final CollectableComboBox comboBox = (CollectableComboBox) celleditor.getCollectableComponent();

				ReferencedEntityFieldCollectableFieldsProvider vp =
					(ReferencedEntityFieldCollectableFieldsProvider) comboBox.getValueListProvider();

				parentController.stopEditing();
				vp.setParameter("entityId", clctcompmodelproviderParent.getCollectableComponentModelFor("entity").getField().getValueId());
				vp.setParameter("field", parentController.getSelectedCollectable().getField("attribute").getValue());

				comboBox.refreshValueList(false);
			}
		}
		return result;
	}
}
