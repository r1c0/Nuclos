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

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.nuclos.client.common.DependantCollectableMasterDataMap;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.fileimport.ImportDelegate;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.ui.collect.component.CollectableCheckBox;
import org.nuclos.client.ui.collect.component.CollectableComboBox;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.fileimport.ImportMode;
import org.nuclos.common2.KeyEnum;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

/**
 * Controller for creation of structure definition for module object import.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class GenericObjectImportStructureCollectController extends MasterDataCollectController {

	private ImportDelegate delegate = ImportDelegate.getInstance();

	private TableColumn tablecolumnPreserve = null;

	private CollectableComponentModelAdapter modeListener = new CollectableComponentModelAdapter() {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			CollectableField field = ev.getNewValue();
			setMode(KeyEnum.Utils.findEnum(ImportMode.class, (String) field.getValue()), true);
		}
	};

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GenericObjectImportStructureCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.IMPORT, tabIfAny);
		this.getCollectStateModel().addCollectStateListener(new CollectStateAdapter(){
			@Override
            public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
	            super.detailsModeEntered(ev);
	            if (!SecurityCache.getInstance().isSuperUser()) {
		            for (CollectableComponent c : getDetailCollectableComponentsFor("mode")) {
		            	if (c instanceof CollectableComboBox) {
		            		c.setEnabled(false);
		            	}
		            }
	            }
	            getDetailsComponentModel("mode").addCollectableComponentModelListener(modeListener);
	            setMode(KeyEnum.Utils.findEnum(ImportMode.class, (String) getDetailsComponentModel("mode").getField().getValue()), false);
            }

			@Override
			public void detailsModeLeft(CollectStateEvent ev) throws CommonBusinessException {
				getDetailsComponentModel("mode").removeCollectableComponentModelListener(modeListener);
			}
		});
	}

	@Override
    protected CollectableMasterDataWithDependants newCollectableWithDefaultValues() {
		final CollectableMasterDataWithDependants result = super.newCollectableWithDefaultValues();
		result.setField("mode", new CollectableValueField(ImportMode.NUCLOSIMPORT.getValue()));
		return result;
    }

	@Override
	public CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if(clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		// We have to clear the ids for cloned objects:
		/**
		 * @todo eliminate this workaround - this is the wrong place. The right
		 *       place is the Clone action!
		 */
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());

		final MasterDataVO mdvoInserted = delegate.createImportStructure(new MasterDataWithDependantsVO(clctNew.getMasterDataCVO(), mpmdvoDependants));

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
		final DependantCollectableMasterDataMap mpclctDependants = (DependantCollectableMasterDataMap)oAdditionalData;

		final Object oId = this.delegate.modifyImportStructure(new MasterDataWithDependantsVO(clct.getMasterDataCVO(), mpclctDependants.toDependantMasterDataMap()));

		final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);
		return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		this.delegate.removeImportStructure(clct.getMasterDataCVO());
	}

	/**
	 * set the structures import mode (after selecting an import mode or on entering details view)
	 *
	 * @param mode the new import mode
	 * @param setDelete if the delete option should be set to false (not on entering details mode as the model would be changed)
	 */
	private void setMode(ImportMode mode, boolean setDelete) {
		final JTable tbl = getSubFormController(NuclosEntity.IMPORTATTRIBUTE.getEntityName()).getSubForm().getJTable();
		final TableColumnModel columnmodel = tbl.getColumnModel();

		if (mode != null && ImportMode.DBIMPORT.equals(mode)) {
			for (CollectableComponent c : getDetailCollectableComponentsFor("delete")) {
            	if (c instanceof CollectableCheckBox) {
            		c.setEnabled(false);
            	}
            }
			if (setDelete) {
				getDetailsComponentModel("delete").setField(new CollectableValueField(Boolean.valueOf(false)));
			}
			if (tablecolumnPreserve == null) {
				tablecolumnPreserve = tbl.getColumn("preserve");
				columnmodel.removeColumn(tablecolumnPreserve);
			}
		}
		else {
			for (CollectableComponent c : getDetailCollectableComponentsFor("delete")) {
            	if (c instanceof CollectableCheckBox) {
            		c.setEnabled(true);
            	}
            }
			if (tablecolumnPreserve != null) {
				columnmodel.addColumn(tablecolumnPreserve);
				tablecolumnPreserve = null;
			}
		}
	}

	/**
	 * Show all removed columns again, so they can be saved with their width, and are restored when opened again.
	 */
	@Override
	public void close() {
		final JTable tbl = getSubFormController(NuclosEntity.IMPORTATTRIBUTE.getEntityName()).getSubForm().getJTable();
		final TableColumnModel columnmodel = tbl.getColumnModel();
		if (tablecolumnPreserve != null) {
			columnmodel.addColumn(tablecolumnPreserve);
			tablecolumnPreserve = null;
		}

		super.close();
	}
}
