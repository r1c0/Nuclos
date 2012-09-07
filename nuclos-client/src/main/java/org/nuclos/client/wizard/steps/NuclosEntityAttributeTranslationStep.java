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
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.EntityAttributeTranslationTableModel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
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
public class NuclosEntityAttributeTranslationStep extends NuclosEntityAttributeAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityAttributeTranslationStep.class);

	public static final String[] labels = TranslationVO.labelsField;
	
	private JScrollPane scrolPane;
	private JTable tblAttributes;
	
	private EntityAttributeTranslationTableModel tablemodel;
	
	private NuclosEntityWizardStaticModel parentWizardModel;
	
	
	public NuclosEntityAttributeTranslationStep() {	
		initComponents();		
	}

	public NuclosEntityAttributeTranslationStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityAttributeTranslationStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}
	
	@Override
	protected void initComponents() {
		
		double size [][] = {{130, 130,130, TableLayout.FILL}, {300, 25}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		
		tablemodel = new EntityAttributeTranslationTableModel();
		
		tblAttributes = new JTable(tablemodel);
		
		JTextField txtField = new JTextField();
		txtField.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		DefaultCellEditor editor = new DefaultCellEditor(txtField);
		editor.setClickCountToStart(1);
		
		for(TableColumn col : CollectionUtils.iterableEnum(tblAttributes.getColumnModel().getColumns()))
			col.setCellEditor(editor);
		
		scrolPane = new JScrollPane(tblAttributes);
		
		this.add(scrolPane, new TableLayoutConstraints(0, 0, 3, 0));
		
	}
	
	private Collection<LocaleInfo> loadLocales() {
		return LocaleDelegate.getInstance().getAllLocales(false);		
	}

	@Override
	public void prepare() {
		super.prepare();		
		initTransalationModel();		
	}

	private void initTransalationModel() {
		Collection<LocaleInfo> colLocales = loadLocales();
		
		List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
				
		for(LocaleInfo voLocale : colLocales) {
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
		
		for(LocaleInfo voLocale : colLocales) {
			
			String sLocaleLabel = voLocale.language; 
			boolean blnSet = false;
			
			if(this.model.isEditMode() && this.model.getAttribute().getId() != null) {	
				if(model.getTranslation() != null && model.getTranslation().size() > 0) {
					for(TranslationVO voTranslation : model.getTranslation()) {
						if(voTranslation.getLanguage().equals(voLocale.language)) {
							if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], voTranslation.getLabels().get(labels[0]));
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], voTranslation.getLabels().get(labels[1]));
							}
						}
					}
				}
				else {				
					EntityMetaDataVO voMeta = MetaDataClientProvider.getInstance().getEntity(parentWizardModel.getEntityName());			
					if(voMeta != null) {
						EntityFieldMetaDataVO voMetaField = null;
						try {
							voMetaField = MetaDataClientProvider.getInstance().getEntityField(voMeta.getEntity(), this.model.getAttribute().getInternalName());
						}
						catch(Exception e) {
							LOG.info("initTransalationModel: " + e, e);
							continue;
						}
						if(voMetaField != null) {
							String sResourceIdForLabel = voMetaField.getLocaleResourceIdForLabel();
							String sResourceIdForDesc = voMetaField.getLocaleResourceIdForDescription();
							String sLabel = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForLabel);				
							if (sLabel == null)
								sLabel = model.getName();
							String sDesc = LocaleDelegate.getInstance().getResourceByStringId(voLocale, sResourceIdForDesc);
							if (sDesc == null)
								sDesc = model.getDesc();
							if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], sLabel);
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], sDesc);
								blnSet = true;
							}
						}
						
					}
					if(!blnSet) {
						if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
							tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], this.model.getName());
							tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], this.model.getDesc());
						}
					}
				}
			}
			else {		
				if(model.getTranslation() != null && model.getTranslation().size() > 0) {
					for(TranslationVO voTranslation : model.getTranslation()) {
						if(voTranslation.getLanguage().equals(voLocale.language)) {
							if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], voTranslation.getLabels().get(labels[0]));
								tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], voTranslation.getLabels().get(labels[1]));
							}
						}
					}
				}
				else {
					if(tablemodel.getTranslationByName(sLocaleLabel) != null) {
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[0], this.model.getName());
						tablemodel.getTranslationByName(sLocaleLabel).getLabels().put(labels[1], this.model.getDesc());
					}
				}
			}
		}
	}

	@Override
	public void close() {
		scrolPane = null;
		tblAttributes = null;
		
		tablemodel = null;
		
		parentWizardModel = null;
				
		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		if(this.tblAttributes.getCellEditor() != null)
			this.tblAttributes.getCellEditor().stopCellEditing();
		this.model.setTranslation(tablemodel.getRows());
		
		super.applyState();
	}
	
	public void setParentWizardModel(NuclosEntityWizardStaticModel model) {
		this.parentWizardModel = model;
	}
	

}
