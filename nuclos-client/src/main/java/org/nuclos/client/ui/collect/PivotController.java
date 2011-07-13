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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JCheckBox;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
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
					final Map<String, EntityFieldMetaDataVO> map = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(subform);
					
					resultController.putPivotInfo(new PivotInfo(subform, null, null));
				}
			}
			final Comparator<CollectableEntityField> comp = (Comparator<CollectableEntityField>) 
				resultController.getFields().getComparatorForAvaible();
			final SortedSet<CollectableEntityField> available = 
				resultController.getFieldsAvailableForResult(resultController.getEntity(), comp);
			// TODO: check if the unmodifiable List is necessary here
			final List<CollectableEntityField> selected = new ArrayList<CollectableEntityField>(
					resultController.getFields().getSelectedFields());
			
			// remove field that are not available any more from selected fields
			for(Iterator<CollectableEntityField> it = selected.iterator(); it.hasNext();) {
				final CollectableEntityField ef = it.next();
				if(!available.remove(ef)) {
					it.remove();
				}
			}
			
			getModel().set(available, selected, comp);
			/*
			resultController.initializeFields(
					resultController.getFields(),
					resultController.getCollectController(), 
					getSelectedObjects(), getFixedObjects(), 
					// resultController.getGenericObjectCollectController().getSearchResultTemplateController().getSelectedSearchResultTemplate().getListColumnsWidths()
					null);
			 */
			// TODO: ???
			setModel(getModel());
		}
		
	}
	
	private final GenericObjectResultController<CollectableGenericObjectWithDependants> resultController;

	/**
	 * @deprecated This would be far better. But is completely unrealistic at present. (Thomas Pasch)
	 */
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
