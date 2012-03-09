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
package org.nuclos.client.i18n.ui;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableColumn;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.SystemMetaDataProvider;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.CollectStateAdapter;
import org.nuclos.client.ui.collect.CollectStateEvent;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;

public class TranslationsController extends CollectStateAdapter {

	private final static String PLACEHOLDER_RESOURCETABLE = "NUCLOS_RESOURCES";
	
	private final MasterDataCollectController ctl;
	
	private final TranslationsTableModel model;
	private final JTable table;

	public TranslationsController(MasterDataCollectController controller) {
		ctl = controller;
		
		if (NuclosEntity.getByName(ctl.getEntityName()) == null) {
			throw new IllegalStateException(TranslationsController.class.getName() + " is not allowed for nuclet entities.");
		}
		
		JComponent placeholder = UIUtils.findJComponent(ctl.getDetailsPanel(), PLACEHOLDER_RESOURCETABLE);
		
		model = new TranslationsTableModel(SystemMetaDataProvider.getInstance().getAllEntityFieldsByEntity(ctl.getEntityName()));
		table = new JTable(model);
		
		JTextField txtField = new JTextField();
		txtField.getDocument().addDocumentListener(new ResourceDocumentListener());

		txtField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		DefaultCellEditor editor = new DefaultCellEditor(txtField);
		editor.setClickCountToStart(1);

		for(TableColumn col : CollectionUtils.iterableEnum(table.getColumnModel().getColumns())) {
			col.setCellEditor(editor);
		}

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stopEditing();
			}
		});

		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		if (placeholder != null) {
			TableLayout layout = (TableLayout) placeholder.getParent().getLayout();
    		TableLayoutConstraints constraints = layout.getConstraints(placeholder);
			
			Container container = placeholder.getParent();
			container.remove(placeholder);
			
			JScrollPane pane = new JScrollPane(table);
			
			container.add(pane, constraints);
		}
	}

	public void setResources(List<TranslationVO> resources) {
		model.setRows(resources);
	}
	
	public List<TranslationVO> getResources() {
		stopEditing();
		return model.getRows();
	}
	
	@Override
	public void detailsModeEntered(CollectStateEvent ev) throws CommonBusinessException {
		if (ev.getNewCollectState().isDetailsModeMultiViewOrEdit()) {
			table.setEnabled(false);
			setResources(new ArrayList<TranslationVO>());
		}
		else {
			table.setEnabled(true);
			List<TranslationVO> resources = new ArrayList<TranslationVO>();
			if (ev.getNewCollectState().isDetailsModeNew()) {
				for (LocaleInfo li : LocaleDelegate.getInstance().getAllLocales(false)) {
					resources.add((new TranslationVO(li.localeId, li.country, li.language, new HashMap<String, String>())));
				}
			}
			else {
				Integer id = IdUtils.unsafeToId(ctl.getSelectedCollectableId());
				resources = LocaleDelegate.getInstance().getResources(ctl.getEntityName(), id);
			}
			setResources(resources);
		}
	}
	
	public void stopEditing() {
		if (table.getCellEditor() != null) {
			table.getCellEditor().stopCellEditing();
		}
	}
	
	public void enterEditMode() {
		if (ctl.getCollectState().getInnerState() == CollectState.DETAILSMODE_VIEW) {
			try {
				ctl.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_EDIT);
			}
			catch (CommonBusinessException ex) {
				Errors.getInstance().showExceptionDialog(ctl.getTab(), ex);
			}
		}
	}

	private class ResourceDocumentListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			enterEditMode();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			enterEditMode();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			enterEditMode();
		}
	}
}
