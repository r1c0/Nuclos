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
package org.nuclos.client.wizard.steps;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.wizard.model.EntityTranslationTableModel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityTranslationStep extends NuclosEntityAbstractStep {

	JScrollPane scrolPane;
	JTable attributeTable;
	
	EntityTranslationTableModel tablemodel;
	
	public static String[] labels = TranslationVO.labelsEntity;
	
	public NuclosEntityTranslationStep() {	
		initComponents();		
	}

	public NuclosEntityTranslationStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityTranslationStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}
	
	
	@Override
	protected void initComponents() {
		double size [][] = {{TableLayout.FILL, 130,130, TableLayout.FILL, 10}, {TableLayout.FILL, 10}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		setLayout(layout);
		
		tablemodel = new EntityTranslationTableModel();
		List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
		
		for(LocaleInfo voLocale : loadLocales()) {
			String sLocaleLabel = voLocale.language; 
			Integer iLocaleID = voLocale.localeId; 
			String sCountry = voLocale.title;
			Map<String, String> map = new HashMap<String, String>();
			
			TranslationVO translation = new TranslationVO(iLocaleID, sCountry, sLocaleLabel, map);
			for(String sLabel : labels) {									
				translation.getLabels().put(sLabel, "");
			}
			lstTranslation.add(translation);
		}
		tablemodel.setRows(lstTranslation);
		
		attributeTable = new JTable(tablemodel);
		JTextField txtField = new JTextField();
		txtField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		DefaultCellEditor editor = new DefaultCellEditor(txtField);
		editor.setClickCountToStart(1);
		
		for(TableColumn col : CollectionUtils.iterableEnum(attributeTable.getColumnModel().getColumns()))
			col.setCellEditor(editor);
		
		attributeTable.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				stopCellEditing();
			}
		});
		
		scrolPane = new JScrollPane(attributeTable);
		
		add(scrolPane, new TableLayoutConstraints(0, 0, 3, 0));
	}
	
	private Collection<LocaleInfo> loadLocales() {
		return LocaleDelegate.getInstance().getAllLocales(false);
	}
	
	private void stopCellEditing() {
        for(TableColumn col : CollectionUtils.iterableEnum(attributeTable.getColumnModel().getColumns())) {
        	TableCellEditor cellEditor = col.getCellEditor();
			if(cellEditor != null)
        		cellEditor.stopCellEditing();
        }
	}

	@Override
	public void prepare() {
		super.prepare();		
		
		List<TranslationVO> lst = model.getTranslation();
		if(lst != null && lst.size() > 0) {
			tablemodel.setRows(lst);
			return;
		}
		
		LocaleInfo userLocaleInfo = CommonLocaleDelegate.getUserLocaleInfo();
		
		for(LocaleInfo voLocale : loadLocales()) {
			String sLocaleLabel = voLocale.language; 
			
			if(model.isEditMode()) {
				EntityMetaDataVO voMeta = MetaDataClientProvider.getInstance().getEntity(model.getEntityName());
					
				String sResourceIdForLabel = voMeta.getLocaleResourceIdForLabel();
				String sResourceIdForMenuPath = voMeta.getLocaleResourceIdForMenuPath();
				String sResourceIdForTreeView = voMeta.getLocaleResourceIdForTreeView();
				String sResourceIdForViewDescription = voMeta.getLocaleResourceIdForTreeViewDescription();				

				String sLabel = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForLabel);				
				String sMenuPath = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForMenuPath);
				String sTreeview = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForTreeView);
				String sTooltip = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForViewDescription);
				if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], sLabel);
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], sMenuPath);
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[2], sTreeview);
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[3], sTooltip);
				}
				if(userLocaleInfo.localeId.equals(voLocale.localeId) || model.getAttributeModel().getRemoveAttributes().size() > 0
					|| model.getAttributeModel().hasAttributeChangeName()) {
					if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], model.getLabelSingular());
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], model.getMenuPath());
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[2], model.getNodeLabel());
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[3], model.getNodeTooltip());
					}
				}
			}
			else {			
				if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], model.getLabelSingular());
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], model.getMenuPath());
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[2], model.getNodeLabel());
					tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[3], model.getNodeTooltip());
				}
			}
		}
	}

	@Override
	public void applyState() throws InvalidStateException {
		stopCellEditing();
		model.setTranslation(tablemodel.getRows());
		boolean delResource = checkTranslationForNull(labels[0]);
		if(delResource)
			model.setLabelSingular(null);
		
		delResource = checkTranslationForNull(labels[1]);
		if(delResource)
			model.setMenuPathResource(null);
		
		delResource = checkTranslationForNull(labels[2]);
		if(delResource)
			model.setNodeLabelResource(null);
		
		delResource = checkTranslationForNull(labels[3]);
		if(delResource)
			model.setNodeTooltipResource(null);
		
	}

	private boolean checkTranslationForNull(String key) {
	    boolean delResource = false;
		for(LocaleInfo voLocale : loadLocales()) {
			String sLocaleLabel = voLocale.language;
			String sMenupath = tablemodel.getTranslationByName(sLocaleLabel).getLabels().get(key);
			if(sMenupath == null || sMenupath.length() == 0)
				delResource = true;				
			else 
				delResource = false;
		}
	    return delResource;
    }
}
