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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
public class NuclosEntityAttributeLookupShipStep extends NuclosEntityAttributeAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityAttributeLookupShipStep.class);

	private JLabel lbEntity;
	private JComboBox cbxEntity;

	private JScrollPane scrollPane;
	private JList listFields;
	private JButton btSelect;

	private JLabel lbInfo;

	private JTextField tfAlternativeLabel;

	private JLabel lbValueListProvider;
	private JCheckBox cbValueListProvider;

	private List<Attribute> lstAttributes;

	private NuclosEntityWizardStaticModel parentWizardModel;


	public NuclosEntityAttributeLookupShipStep() {
		initComponents();
	}

	public NuclosEntityAttributeLookupShipStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityAttributeLookupShipStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}

	public void setAttributeList(List<Attribute> lst) {
		this.lstAttributes = lst;
	}

	@Override
	protected void initComponents() {
		double size [][] = {{TableLayout.PREFERRED,40, TableLayout.FILL}, {20,20,20,20,100,20,TableLayout.PREFERRED, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);

		lbEntity = new JLabel(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.1", "Verweis auf Entit\u00e4t")+": ");
		cbxEntity = new JComboBox();
		cbxEntity.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.tooltip.1", "Verweis auf Entit\u00e4t"));

		tfAlternativeLabel = new JTextField();
		tfAlternativeLabel.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.tooltip.3", "Fremdschl\u00fcsselaufbau"));
		tfAlternativeLabel.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbValueListProvider = new JLabel(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.4", "Suchfeld"));
		cbValueListProvider = new JCheckBox();
		cbValueListProvider.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.tooltip.4", "Suchfeld"));

		lbInfo = new JLabel();

		listFields = new JList();
		listFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane = new JScrollPane(listFields);
		scrollPane.setPreferredSize(new Dimension(150, 25));
		btSelect = new JButton(">");

		this.add(lbEntity, "0,0, 1,0");
		this.add(cbxEntity, "2,0");

		this.add(lbValueListProvider, "0,1, 1,1");
		this.add(cbValueListProvider, "2,1");


		this.add(scrollPane, "0,2, 0,7");

		this.add(btSelect, "1,2");


		this.add(tfAlternativeLabel, "2,2");
		this.add(lbInfo, "0,6, 2,6");

		cbxEntity.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final Object obj = e.getItem();
					if(obj instanceof EntityMetaDataVO) {
						EntityMetaDataVO vo = (EntityMetaDataVO)obj;
						Set<String> setFieldNames = new HashSet<String>();
						List<EntityFieldMetaDataVO> lstFields = new ArrayList<EntityFieldMetaDataVO>(MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(vo.getEntity()).values());
						Collections.sort(lstFields, new Comparator<EntityFieldMetaDataVO>() {

							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								return o1.getField().toUpperCase().compareTo(o2.getField().toUpperCase());
							}

						});
						for(EntityFieldMetaDataVO voField : lstFields) {
							//@see NUCLOSINT-1232
							if(voField.getForeignEntity() == null
									//&& voField.getLookupEntity() == null
									&& voField.getCalcFunction() == null
									&& voField.getCalculationScript() == null)
								setFieldNames.add(voField.getField());
						}
						if(!NuclosEntityAttributeLookupShipStep.this.model.isEditMode())
							tfAlternativeLabel.setText(SpringLocaleDelegate.getInstance().getResource(vo.getLocaleResourceIdForTreeView(), ""));
						try {
							checkReferenceField();
						}
						catch(Exception e1) {
							LOG.info("itemStateChanged failed: " + e1, e1);
							tfAlternativeLabel.setText("");
							NuclosEntityAttributeLookupShipStep.this.model.getAttribute().setField(null);
						}

						List<String> lstItems = new ArrayList<String>();
						lstItems.addAll(setFieldNames);
						Collections.sort(lstItems);
						listFields.setListData(lstItems.toArray());
						NuclosEntityAttributeLookupShipStep.this.model.getAttribute().setLookupMetaVO(vo);
						NuclosEntityAttributeLookupShipStep.this.setComplete(true);
					}
					else {
						NuclosEntityAttributeLookupShipStep.this.model.getAttribute().setLookupMetaVO(null);
						NuclosEntityAttributeLookupShipStep.this.setComplete(false);
					}
				}
			}
		});

		btSelect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
 				 setKeyDescription();
			}
		});

		listFields.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {

				if(SwingUtilities.isLeftMouseButton(e)) {
					if(e.getClickCount() == 2) {
						 setKeyDescription();
					}
				}
			}



		});

		cbValueListProvider.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeLookupShipStep.this.model.getAttribute().setValueListProvider(cb.isSelected());
			}
		});

		tfAlternativeLabel.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					model.getAttribute().setField(e.getDocument().getText(0, e.getDocument().getLength()));
					NuclosEntityAttributeLookupShipStep.this.model.getAttribute().setField(e.getDocument().getText(0, e.getDocument().getLength()));
				}
				catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributeLookupShipStep.this, ex);
				}
			}
		});

	}

	@Override
	public void prepare() {
		super.prepare();
		fillEntityCombobox();

		if(this.model.getAttribute().getLookupMetaVO() != null) {
			if(model.getAttribute().getField() != null) {
				String sField = new String(model.getAttribute().getField());
				cbxEntity.setSelectedItem(this.model.getAttribute().getLookupMetaVO());
				tfAlternativeLabel.setText(sField);
				cbValueListProvider.setSelected(this.model.getAttribute().isValueListProvider());
			}
			else {
				cbxEntity.setSelectedItem(this.model.getAttribute().getLookupMetaVO());
			}
		}

		if(parentWizardModel.hasRows() && model.isEditMode() && !parentWizardModel.isVirtual()) {
			cbxEntity.setEnabled(false);
			cbxEntity.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.tooltip.5", "Verweis kann nicht geändert werden. Da bereits Datensätze vorhanden sind."));
		}
		else {
			cbxEntity.setEnabled(true);
			cbxEntity.setToolTipText(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.tooltip.1", "Verweis auf Entit\u00e4t"));
		}

	}

	private void fillEntityCombobox() {

		ItemListener ilArray[] = cbxEntity.getItemListeners();
		for(ItemListener il : ilArray) {
			cbxEntity.removeItemListener(il);
		}

		cbxEntity.removeAllItems();

		Collection<EntityMetaDataVO> colMasterdata = MetaDataClientProvider.getInstance().getAllEntities();
		List<EntityMetaDataVO> lstMasterdata = new ArrayList<EntityMetaDataVO>(colMasterdata);
		Collections.sort(lstMasterdata, new Comparator<EntityMetaDataVO>() {

			@Override
			public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
				return o1.toString().compareTo(o2.toString());
			}

		});
		cbxEntity.addItem("");
		for(EntityMetaDataVO vo : lstMasterdata) {
			if(NuclosEntity.PROCESS.getEntityName().equals(vo.getEntity()) ||
			   NuclosEntity.USER.getEntityName().equals(vo.getEntity()) ||
			   NuclosEntity.ROLE.getEntityName().equals(vo.getEntity()) ||
			   vo.getId() > 0)
				cbxEntity.addItem(vo);
		}

		for(ItemListener il : ilArray) {
			cbxEntity.addItemListener(il);
		}
	}

	public void setParentWizardModel(NuclosEntityWizardStaticModel model) {
		this.parentWizardModel = model;
	}

	@Override
	public void close() {
		lbEntity = null;
		cbxEntity = null;

		scrollPane = null;
		listFields = null;
		btSelect = null;

		lbInfo = null;

		tfAlternativeLabel = null;

		lbValueListProvider = null;
		cbValueListProvider = null;

		lstAttributes = null;

		parentWizardModel = null;
		
		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		checkReferenceField();
		
		super.applyState();
	}


	private void checkReferenceField() throws InvalidStateException {
		String sField = this.model.getAttribute().getField();
		if(sField == null || sField.length() < 1)
			return;
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    boolean invalid = true;
		while (referencedEntityMatcher.find()) {
		  String sName = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  try {
			  MetaDataDelegate.getInstance().getEntityField(model.getAttribute().getLookupMetaVO().getEntity(), sName);
			  invalid = false;
		  }
		  catch(Exception e){
			  throw new InvalidStateException(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.7", "Es wurde ein ungültiger Eintrag gefunden: " + sName, sName));
		  }
		}

		if(invalid){
			throw new InvalidStateException(SpringLocaleDelegate.getInstance().getMessage("wizard.step.attributerelationship.8", "Es wurde kein gültiger Referenzeintrag gefunden!"));
		}

	}

	private void setKeyDescription() {
		String sField = (String)listFields.getSelectedValue();
		 if(sField == null) {
			 return;
		 }
		 String strText = tfAlternativeLabel.getText();
		 strText += "${" + sField + "}";

		 tfAlternativeLabel.setText(strText);
	}

}
