//Copyright (C) 2011  Novabit Informationssysteme GmbH
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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.data.pivot.PivotInfo;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.IdUtils;

public class PivotController extends SelectFixedColumnsController {
	
	private class ShowPivotListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			final JCheckBox src = (JCheckBox) e.getSource();
			resultController.clearPivotInfo();
			if (src.isSelected()) {
				// Make all pivot
				// TODO: Make sth sensible here!
				final String entityName = resultController.getEntity().getName();
				final EntityMetaDataVO entityMd = MetaDataClientProvider.getInstance().getEntity(entityName);
				final Set<String> subforms = GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(
						IdUtils.unsafeToId(entityMd.getId()));
				for (String subform: subforms) {
					resultController.putPivotInfo(new PivotInfo(subform, null, null));
				}
			}
			/*
			resultController.initializeFields(resultController.getEntity(), 
					(CollectController) resultController.getCollectController(), resultController.getCollectController().getPreferences());
			 */
			resultController.getFields().set(
					resultController.getFieldsAvailableForResult(resultController.getEntity()), 
					resultController.getFields().getSelectedFields(), 
					resultController.getFields().getComparatorForAvaible());
			/*
			resultController.initializeFields(
					resultController.getFields(),
					resultController.getCollectController(), 
					getSelectedObjects(), getFixedObjects(), 
					// resultController.getGenericObjectCollectController().getSearchResultTemplateController().getSelectedSearchResultTemplate().getListColumnsWidths()
					null);
			 */
			updateModel(resultController.getFields());
		}
		
	}
	
	private final GenericObjectResultController<CollectableGenericObjectWithDependants> resultController;
	
	public PivotController(Component parent, PivotPanel panel, List<? extends CollectableEntityField> base, List<? extends CollectableEntityField> subforms) {
		super(parent, panel);
		resultController = null;
	}
	
	public PivotController(Component parent, PivotPanel panel, GenericObjectResultController<CollectableGenericObjectWithDependants> resultController) {
		super(parent, panel);
		this.resultController = resultController;
		panel.addActionListener(new ShowPivotListener());
	}
	
	private PivotPanel getPivotPanel() {
		return (PivotPanel) getPanel();
	}

}
