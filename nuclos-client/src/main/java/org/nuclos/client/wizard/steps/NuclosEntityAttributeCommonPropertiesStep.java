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

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;
import info.clearthought.layout.TableLayout;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.lang.StringUtils;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.nuclos.client.attribute.AttributeDelegate;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Bubble.Position;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.ValueList;
import org.nuclos.client.wizard.util.DefaultValue;
import org.nuclos.client.wizard.util.DoubleFormatDocument;
import org.nuclos.client.wizard.util.MoreOptionPanel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.client.wizard.util.NumericFormatDocument;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityAttributeCommonPropertiesStep extends NuclosEntityAttributeAbstractStep {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	static String[] forbiddenNames = {"INTID","DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION",
		"STRSYSTEMID", "INTID_T_MD_PROCESS", "STRORIGIN", "BLNDELETED", "INTID_T_MD_STATE"};

	JLabel lbLabel;
	JTextField tfLabel;
	JLabel lbDefaultValue;
	JTextField tfDefaultValue;
	JComboBox cbxDefaultValue;
	DateChooser dateDefaultValue;
	JCheckBox cbDefaultValue;
	JLabel lbDBFieldName;
	JTextField tfDBFieldName;
	JLabel lbDBFieldNameComplete;
	JTextField tfDBFieldNameComplete;

	JLabel lbDistinct;
	JCheckBox cbDistinct;
	JLabel lbLogBook;
	JCheckBox cbLogBook;
	JLabel lbMandatory;
	JCheckBox cbMandatory;

	JTextField tfMandatory;
	JComboBox cbxMandatory;
	DateChooser dateMandatory;
	JCheckBox cbMandatoryValue;


	JLabel lbIndexed;
	JCheckBox cbIndexed;

	JLabel lbAttributeGroup;
	JComboBox cbxAttributeGroup;

	JLabel lbCalcFunction;
	JComboBox cbxCalcFunction;

	boolean blnLabelModified;
	boolean blnDefaultSelected;

	JPanel pnlMoreOptions;

	NuclosEntityWizardStaticModel parentWizardModel;


	public NuclosEntityAttributeCommonPropertiesStep() {
		initComponents();

	}

	public NuclosEntityAttributeCommonPropertiesStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityAttributeCommonPropertiesStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}

	@Override
	protected void initComponents() {
		double size [][] = {{150,20, TableLayout.FILL}, {20,20,20,20,20,20,20,20,20,20,20, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);

		lbLabel = new JLabel(getMessage("wizard.step.attributeproperties.10", "Feldname")+": ");
		tfLabel = new JTextField();
		tfLabel.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfLabel.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.10", "Feldname"));

		lbDefaultValue = new JLabel(getMessage("wizard.step.attributeproperties.11", "Standardwert")+": ");
		tfDefaultValue = new JTextField();
		tfDefaultValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));
		tfDefaultValue.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		cbxDefaultValue = new JComboBox();
		cbxDefaultValue.setVisible(false);
		cbxDefaultValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		dateDefaultValue = new DateChooser(true);
		dateDefaultValue.setVisible(false);
		dateDefaultValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		cbDefaultValue = new JCheckBox();
		cbDefaultValue.setVisible(false);
		cbDefaultValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		lbDistinct = new JLabel(getMessage("wizard.step.attributeproperties.7", "Eindeutig")+": ");
		cbDistinct = new JCheckBox();
		cbDistinct.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.7", "Eindeutig"));

		lbLogBook = new JLabel(getMessage("wizard.step.attributeproperties.8", "Logbuch")+": ");
		cbLogBook = new JCheckBox();
		cbLogBook.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.8", "Logbuch"));

		lbMandatory = new JLabel(getMessage("wizard.step.attributeproperties.9", "Pflichtfeld")+": ");
		cbMandatory = new JCheckBox();
		cbMandatory.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.9", "Pflichtfeld"));

		tfMandatory = new JTextField();
		tfMandatory.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfMandatory.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));

		cbxMandatory = new JComboBox();
		cbxMandatory.setVisible(false);
		cbxMandatory.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));
		dateMandatory = new DateChooser();
		dateMandatory.setVisible(false);
		dateMandatory.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));
		cbMandatoryValue = new JCheckBox();
		cbMandatoryValue.setVisible(false);
		cbMandatoryValue.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));

		lbDBFieldName = new JLabel(getMessage("wizard.step.attributeproperties.12", "DB-Spaltename"));
		tfDBFieldName = new JTextField();
		tfDBFieldName.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.12", "DB-Spaltename"));
		tfDBFieldName.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbDBFieldNameComplete = new JLabel(getMessage("wizard.step.attributeproperties.18", "Vollst\u00e4ndiger Spaltenname"));
		tfDBFieldNameComplete = new JTextField();
		tfDBFieldNameComplete.setEnabled(false);

		lbAttributeGroup = new JLabel(getMessage("wizard.step.attributeproperties.19", "Attributegruppe"));
		cbxAttributeGroup = new JComboBox();
		cbxAttributeGroup.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.19", "Attributegruppe"));

		lbCalcFunction = new JLabel(getMessage("wizard.step.attributeproperties.20", "Berechungsvorschrift"));
		cbxCalcFunction = new JComboBox();
		cbxCalcFunction.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.20", "Berechungsvorschrift"));

		lbIndexed = new JLabel(getMessage("wizard.step.attributeproperties.26", "Indiziert"));
		cbIndexed = new JCheckBox();
		cbIndexed.setToolTipText(getMessage("wizard.step.attributeproperties.tooltip.26", "Indiziert"));

		pnlMoreOptions = new JPanel();

		double sizeMoreOptions [][] = {{155, TableLayout.FILL}, {20,20,20, TableLayout.FILL}};

		pnlMoreOptions.setLayout(new TableLayout(sizeMoreOptions));
		pnlMoreOptions.add(lbDBFieldName, "0,0");
		pnlMoreOptions.add(tfDBFieldName, "1,0");
		pnlMoreOptions.add(lbDBFieldNameComplete, "0,1");
		pnlMoreOptions.add(tfDBFieldNameComplete, "1,1");
		pnlMoreOptions.add(lbIndexed, "0,2");
		pnlMoreOptions.add(cbIndexed, "1,2");

		MoreOptionPanel optionPanel = new MoreOptionPanel(pnlMoreOptions);

		this.add(lbLabel, "0,0");
		this.add(tfLabel, "1,0 , 2,0");
		this.add(lbDefaultValue, "0,1");
		this.add(tfDefaultValue, "1,1 , 2,1");
		this.add(cbxDefaultValue, "1,1 , 2,1");
		this.add(dateDefaultValue, "1,1 , 2,1");
		this.add(cbDefaultValue, "1,1");
		this.add(lbDistinct, "0,2");
		this.add(cbDistinct, "1,2");
		this.add(lbLogBook, "0,3");
		this.add(cbLogBook, "1,3");
		this.add(lbMandatory, "0,4");
		this.add(cbMandatory, "1,4");
		this.add(tfMandatory, "2,4");
		this.add(cbxMandatory, "2,4");
		this.add(dateMandatory, "2,4");
		this.add(cbMandatoryValue, "2,4");

		this.add(lbAttributeGroup, "0,5");
		this.add(cbxAttributeGroup, "1,5 , 2,5");
		this.add(lbCalcFunction, "0,6");
		this.add(cbxCalcFunction, "1,6 , 2,6");

		this.add(optionPanel, "0,7, 2,10");


		tfLabel.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				doSomeWork();
			}

			@Override
			public void keyReleased(KeyEvent e) {
				doSomeWork();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				doSomeWork();
			}

			protected void doSomeWork() {
				blnLabelModified = true;
			}

		});

		tfLabel.setDocument(new SpecialCharacterDocument());
		tfLabel.getDocument().addDocumentListener(new DocumentListener() {

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
				int size = e.getDocument().getLength();
				if(size > 0) {
					NuclosEntityAttributeCommonPropertiesStep.this.setComplete(true);
				}
				else  {
					NuclosEntityAttributeCommonPropertiesStep.this.setComplete(false);
				}
				try {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setInternalName(e.getDocument().getText(0, e.getDocument().getLength()));
					if(!NuclosEntityAttributeCommonPropertiesStep.this.model.isEditMode()) {
						String sPrefix = Attribute.getDBPrefix(NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute());
						tfDBFieldName.setText(sPrefix + e.getDocument().getText(0, e.getDocument().getLength()));
					}

				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
				}
			}
		});



		tfDefaultValue.getDocument().addDocumentListener(new DefaultValueDocumentListener());

		tfMandatory.getDocument().addDocumentListener(new MandatoryValueDocumentListener());
		tfMandatory.setLocale(CommonLocaleDelegate.getLocale());


		tfDBFieldName.setDocument(new LimitCharacterDocument());
		tfDBFieldName.getDocument().addDocumentListener(new DocumentListener() {

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
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDbName(e.getDocument().getText(0, e.getDocument().getLength()));

					String s = e.getDocument().getText(0, e.getDocument().getLength());
					if(model.getAttribute().getMetaVO() != null && model.getAttribute().getField() != null){
						s = "STRVALUE_" + s;
					}
					else if(model.getAttribute().getMetaVO() != null && model.getAttribute().getField() == null){
						s = "INTID_" + s;
					}
					tfDBFieldNameComplete.setText(s);


				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
				}
			}
		});


		cbDistinct.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				final JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDistinct(cb.isSelected());
				if(!cb.isSelected()) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							cbMandatory.setEnabled(true);
						}
					});
				}
			}
		});

		cbLogBook.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setLogBook(cb.isSelected());
			}
		});

		cbMandatory.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				final JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatory(cb.isSelected());
				if(NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.isEditMode() && cb.isSelected()) {
					if(NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().getMandatoryValue() == null) {
						(new Bubble(cb, getMessage("wizard.step.attributeproperties.tooltip.28", "Bitte tragen Sie einen Wert ein mit dem das Feld vorbelegt werden kann!"), 3, Position.UPPER)).setVisible(true);
					}

				}
			}
		});

		cbIndexed.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setIndexed(cb.isSelected());
			}
		});

		cbxDefaultValue.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == e.SELECTED) {
					model.getAttribute().setIdDefaultValue((DefaultValue)e.getItem());
				}
			}
		});

		cbxMandatory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == e.SELECTED) {
					model.getAttribute().setMandatoryValue(((DefaultValue)e.getItem()).getId());
				}
			}
		});

		dateDefaultValue.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);		}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				try {
					String value = e.getDocument().getText(0, e.getDocument().getLength());
					if("Heute".equalsIgnoreCase(value)) {
						value = RelativeDate.today().toString();
					}
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDefaultValue(value);
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
				}
			}

		});

		cbDefaultValue.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				if(cb.isSelected()) {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDefaultValue("ja");
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDefaultValue("nein");
				}
			}
		});

		cbMandatoryValue.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				if(cb.isSelected()) {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(Boolean.TRUE);
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(Boolean.FALSE);
				}
			}
		});

		cbxAttributeGroup.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setAttributeGroup((String)e.getItem());
					NuclosEntityAttributeCommonPropertiesStep.this.setComplete(true);
				}
			}
		});

		cbxCalcFunction.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setCalcFunction((String)e.getItem());
				}
			}
		});

	}

	private void fillAttributeGroupBox() {
		ItemListener ilArray[] = cbxAttributeGroup.getItemListeners();
		for(ItemListener il : ilArray) {
			cbxAttributeGroup.removeItemListener(il);
		}

		cbxAttributeGroup.removeAllItems();
		cbxAttributeGroup.addItem("");

		List<MasterDataVO> lstAttributeGroup = new ArrayList<MasterDataVO>(MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ENTITYFIELDGROUP.getEntityName()));
		Collections.sort(lstAttributeGroup, new Comparator<MasterDataVO>() {
			@Override
            public int compare(MasterDataVO o1, MasterDataVO o2) {
	            String sField1 = (String)o1.getField("name");
	            String sField2 = (String)o2.getField("name");
	            return sField1.toUpperCase().compareTo(sField2.toUpperCase());
            }
		});
		for(MasterDataVO voAttributeGroup : lstAttributeGroup) {
			cbxAttributeGroup.addItem(voAttributeGroup.getField("name"));
		}
		cbxAttributeGroup.setSelectedIndex(0);
		for(ItemListener il : ilArray) {
			cbxAttributeGroup.addItemListener(il);
		}
	}

	private void fillCalcFunctionBox() {
		ItemListener ilArray[] = cbxCalcFunction.getItemListeners();
		for(ItemListener il : ilArray) {
			cbxCalcFunction.removeItemListener(il);
		}
		cbxCalcFunction.removeAllItems();
		cbxCalcFunction.addItem("");
		for(String sFunction : AttributeDelegate.getInstance().getCalculationFunctions()) {
			cbxCalcFunction.addItem(sFunction);
		}

		for(ItemListener il : ilArray) {
			cbxCalcFunction.addItemListener(il);
		}
	}

	@Override
	public void prepare() {
		super.prepare();

		cbMandatory.setEnabled(true);

		fillCalcFunctionBox();

		if(!this.parentWizardModel.isStateModel()){
			cbxAttributeGroup.setEnabled(false);
		}
		else{
			fillAttributeGroupBox();
		}

		if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer")) {
			final NumericFormatDocument nfd = new NumericFormatDocument();
			nfd.addDocumentListener(new DefaultValueDocumentListener());
			tfDefaultValue.setDocument(nfd);

			final NumericFormatDocument nfdMandatory = new NumericFormatDocument();
			nfdMandatory.addDocumentListener(new MandatoryValueDocumentListener());
			tfMandatory.setDocument(nfdMandatory);
		}
		else if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Double")) {
			final DoubleFormatDocument dfd = new DoubleFormatDocument();
			dfd.addDocumentListener(new DefaultValueDocumentListener());
			tfDefaultValue.setDocument(dfd);

			final DoubleFormatDocument dfdMandatory = new DoubleFormatDocument();
			dfdMandatory.addDocumentListener(new MandatoryValueDocumentListener());
			tfMandatory.setDocument(dfdMandatory);

		}

		if(this.model.isEditMode()) {
			tfLabel.setText(this.model.getAttribute().getInternalName());
			cbxCalcFunction.setSelectedItem(this.model.getAttribute().getCalcFunction());
			cbxAttributeGroup.setSelectedItem(this.model.getAttribute().getAttributeGroup());
			cbDistinct.setSelected(this.model.getAttribute().isDistinct());
			ItemListener ilArray[] = cbMandatory.getItemListeners();
			for(ItemListener il : ilArray) {
				cbMandatory.removeItemListener(il);
			}
			cbMandatory.setSelected(this.model.getAttribute().isMandatory());
			for(ItemListener il : ilArray) {
				cbMandatory.addItemListener(il);
			}
			cbLogBook.setSelected(this.model.getAttribute().isLogBook());
			cbIndexed.setSelected(this.model.getAttribute().isIndexed());
			if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.util.Date")) {
				String str = this.model.getAttribute().getDefaultValue();
				if(RelativeDate.today().toString().equals(str)) {
					dateDefaultValue.setDate(new Date(System.currentTimeMillis()));
					dateDefaultValue.getJTextField().setText("Heute");
				}
				else if ("Heute".equalsIgnoreCase(str)) {
					dateDefaultValue.setDate(new Date(System.currentTimeMillis()));
					dateDefaultValue.getJTextField().setText("Heute");
				}
				else {
					SimpleDateFormat result = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
					result.setLenient(false);
					try {
						dateDefaultValue.setDate(result.parse(str));
					}
					catch(Exception e) {
						// set no day
					}
				}
			}
			else if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Boolean")) {
			   String value = this.model.getAttribute().getDefaultValue();
				if(value != null && value.equalsIgnoreCase("ja")) {
					cbDefaultValue.setSelected(true);
				}
				else {
					cbDefaultValue.setSelected(false);
				}
			}
			else {
				tfDefaultValue.setText(this.model.getAttribute().getDefaultValue());
			}

			if(this.model.getAttribute().getDbName() != null) {
				String sModifiedDBName = new String(this.model.getAttribute().getDbName());

				tfDBFieldName.setText(sModifiedDBName.replaceFirst("STRVALUE_", "").replaceFirst("INTID_", ""));
				if(this.model.getAttribute().getMetaVO() != null && this.model.getAttribute().getField() != null){
					tfDBFieldNameComplete.setText("STRVALUE_"+ this.model.getAttribute().getDbName());
				}
				else if(this.model.getAttribute().getMetaVO() != null && this.model.getAttribute().getField() == null){
					tfDBFieldNameComplete.setText("INTID_"+ this.model.getAttribute().getDbName());
				}

			}


		}
		else {
			if(!blnLabelModified)
				tfLabel.setText(this.model.getName().toLowerCase());

			if(this.model.getAttributeCount() == 0 && !blnDefaultSelected) {
				cbDistinct.setSelected(true);
				cbMandatory.setSelected(true);
				blnDefaultSelected = true;
			}
			cbDistinct.setEnabled(!this.parentWizardModel.hasRows());

			cbIndexed.setSelected(this.model.isRefernzTyp());

			if(cbxAttributeGroup.getModel().getSize() > 1) {
				if(this.parentWizardModel.isStateModel())
					cbxAttributeGroup.setSelectedIndex(1);
			}

			if(this.model.getAttribute().getDbName() != null) {
				String sModifiedDBName = new String(this.model.getAttribute().getDbName());

				tfDBFieldName.setText(sModifiedDBName.replaceFirst("STRVALUE_", "").replaceFirst("INTID_", ""));
				if(this.model.getAttribute().getMetaVO() != null && this.model.getAttribute().getField() != null){
					tfDBFieldNameComplete.setText("STRVALUE_"+ this.model.getAttribute().getDbName());
				}
				else if(this.model.getAttribute().getMetaVO() != null && this.model.getAttribute().getField() == null){
					tfDBFieldNameComplete.setText("INTID_"+ this.model.getAttribute().getDbName());
				}

			}

		}

		Object objMandatoryValue = getModel().getAttribute().getMandatoryValue();

		if(this.model.isRefernzTyp()) {

			ItemListener listener[] = cbxDefaultValue.getItemListeners();
			for(ItemListener il : listener){
				cbxDefaultValue.removeItemListener(il);
			}
			ItemListener listenerMandatory[] = cbxDefaultValue.getItemListeners();
			for(ItemListener il : listenerMandatory){
				cbxMandatory.removeItemListener(il);
			}

			cbxDefaultValue.setVisible(true);
			tfDefaultValue.setVisible(false);
			dateDefaultValue.setVisible(false);
			cbDefaultValue.setVisible(false);

			cbxMandatory.setVisible(this.parentWizardModel.isEditMode());
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(false);

			List<DefaultValue> defaultModel = new ArrayList<DefaultValue>();
			List<DefaultValue> mandatoryModel = new ArrayList<DefaultValue>();

			DefaultValue mandatoryValue = null;
			Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(this.model.getAttribute().getMetaVO().getEntity());
			for (MasterDataVO vo : colVO) {
				String sField = this.model.getAttribute().getField();
				if (sField == null)
					break;
				Pattern referencedEntityPattern = Pattern.compile("[$][{][\\w\\[\\]]+[}]");
				Matcher referencedEntityMatcher = referencedEntityPattern.matcher(sField);
				StringBuffer sb = new StringBuffer();

				while (referencedEntityMatcher.find()) {
					Object value = referencedEntityMatcher.group().substring(2, referencedEntityMatcher.group().length() - 1);

					String sName = value.toString();
					Object fieldValue = vo.getField(sName);
					if (fieldValue != null)
						referencedEntityMatcher.appendReplacement(sb, fieldValue.toString());
					else
						referencedEntityMatcher.appendReplacement(sb, "");
				}

				// complete the transfer to the StringBuffer
				referencedEntityMatcher.appendTail(sb);
				sField = sb.toString();
				DefaultValue dv = new DefaultValue(vo.getIntId(), sField);
				defaultModel.add(dv);
				mandatoryModel.add(dv);
				if (dv.getId().equals(objMandatoryValue)) {
					mandatoryValue = dv;
				}
			}

			Collections.sort(defaultModel);
			Collections.sort(mandatoryModel);

			defaultModel.add(0, new DefaultValue(null, null));
			mandatoryModel.add(0, new DefaultValue(null, null));

			cbxDefaultValue.setModel(new ListComboBoxModel<DefaultValue>(defaultModel));
			cbxDefaultValue.setSelectedItem(this.model.getAttribute().getIdDefaultValue());

			cbxMandatory.setModel(new ListComboBoxModel<DefaultValue>(mandatoryModel));
			if (mandatoryValue != null) {
				cbxMandatory.setSelectedItem(mandatoryValue);
			}

			for(ItemListener il : listener){
				cbxDefaultValue.addItemListener(il);
			}

			for(ItemListener il : listenerMandatory){
				cbxMandatory.addItemListener(il);
			}
		}
		else if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.util.Date")) {
			dateDefaultValue.setVisible(true);
			cbxDefaultValue.setVisible(false);
			tfDefaultValue.setVisible(false);
			cbDefaultValue.setVisible(false);

			cbxMandatory.setVisible(false);
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(this.parentWizardModel.isEditMode());
			if(objMandatoryValue != null && objMandatoryValue instanceof Date) {
				dateMandatory.setDate((Date)objMandatoryValue);
			}

			cbMandatoryValue.setVisible(false);
		}
		else if(this.model.getAttribute().getDatatyp().getJavaType().equals("java.lang.Boolean")) {
			cbxDefaultValue.setVisible(false);
			tfDefaultValue.setVisible(false);
			dateDefaultValue.setVisible(false);
			cbDefaultValue.setVisible(true);

			cbxMandatory.setVisible(false);
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(this.parentWizardModel.isEditMode());
			if(objMandatoryValue != null && objMandatoryValue instanceof Boolean) {
				cbMandatoryValue.setSelected((Boolean)objMandatoryValue);
			}
		}
		else if(this.model.getAttribute().isValueList()) {
			ItemListener listener[] = cbxDefaultValue.getItemListeners();
			for(ItemListener il : listener){
				cbxDefaultValue.removeItemListener(il);
			}

			cbxCalcFunction.setEnabled(false);
			cbxDefaultValue.setVisible(true);
			tfDefaultValue.setVisible(false);
			dateDefaultValue.setVisible(false);
			cbDefaultValue.setVisible(false);
			cbxDefaultValue.removeAllItems();
			cbxDefaultValue.addItem(new DefaultValue(null, null));
			cbxMandatory.addItem(new DefaultValue(null, null));
			for(ValueList valueList : this.model.getAttribute().getValueList()) {
				cbxDefaultValue.addItem(new DefaultValue(valueList.getId() != null ? valueList.getId().intValue() : null, valueList.getLabel()));
			}

			cbxDefaultValue.setSelectedItem(this.model.getAttribute().getDefaultValue());
			for(ItemListener il : listener){
				cbxDefaultValue.addItemListener(il);
			}

			listener = cbxMandatory.getItemListeners();
			for(ItemListener il : listener){
				cbxMandatory.removeItemListener(il);
			}

			for(ValueList valueList : this.model.getAttribute().getValueList()) {
				DefaultValue dv = new DefaultValue(valueList.getId() != null ? valueList.getId().intValue() : null, valueList.getLabel());
				cbxMandatory.addItem(dv);
				if(dv.getId() != null && dv.getId().equals(objMandatoryValue)) {
					cbxMandatory.setSelectedItem(dv);
				}
			}

			for(ItemListener il : listener){
				cbxMandatory.addItemListener(il);
			}

			cbMandatory.setEnabled(model.getAttribute().getId() != null);
			cbxMandatory.setVisible(this.parentWizardModel.isEditMode() && model.getAttribute().getId() != null);
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(false);
		}
		else if(model.getAttribute().isImage() || model.getAttribute().isPasswordField() || model.getAttribute().isFileType()) {
			cbMandatory.setEnabled(false);
			cbxMandatory.setVisible(false);
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(false);
		}
		else {
			cbxDefaultValue.setVisible(false);
			tfDefaultValue.setVisible(true);
			dateDefaultValue.setVisible(false);
			cbDefaultValue.setVisible(false);

			cbxMandatory.setVisible(false);
			tfMandatory.setVisible(this.parentWizardModel.isEditMode());
			if(objMandatoryValue != null) {
				tfMandatory.setText(objMandatoryValue.toString());
			}
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(false);
		}

		Attribute attr = NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute();
		if(NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.hasRows() && attr.getId() != null ) {
			boolean blnAllowed = MetaDataDelegate.getInstance().isChangeDatabaseColumnToUniqueAllowed(
				NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.getEntityName(), attr.getInternalName());

			if(!blnAllowed && !attr.isDistinct()) {
				cbDistinct.setSelected(false);
				cbDistinct.setEnabled(false);
				cbDistinct.setToolTipText(getMessage("wizard.step.entitysqllayout.6", "Das Feld {0} kann nicht auf ein eindeutiges Feld umgestellt werden.", attr.getLabel()));
			}
		}
	}

	public void setParentWizardModel(NuclosEntityWizardStaticModel model) {
		this.parentWizardModel = model;
	}

	@Override
	public void applyState() throws InvalidStateException {
		Attribute attr = this.model.getAttribute();

		String sDBName = attr.getDbName();

		if(!this.model.isEditMode()) {
			for(Attribute attribute : this.parentWizardModel.getAttributeModel().getAttributes()) {
				if(attribute.getDbName().equals(sDBName)) {
					String sMessage = getMessage("wizard.step.attributeproperties.15", "<html>Der Spaltenname existiert schon in der Tabelle.<p> " +
						"Bitte \u00e4ndern Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");

					JLabel lb = new JLabel(sMessage);
					JOptionPane.showMessageDialog(this, lb);
					throw new InvalidStateException("");
				}
			}
		}

		if(this.model.getAttribute().getInternalName().length() > 250) {
			String sMessage = getMessage("wizard.step.attributeproperties.15", "<html>Der Feldname ist zu lang.<p> " +
			"Bitte \u00e4ndern Sie den Namen.</html>");

			JLabel lb = new JLabel(sMessage);
			JOptionPane.showMessageDialog(this, lb);
			throw new InvalidStateException("");
		}

		for(String sFieldName : forbiddenNames) {
			if(sFieldName.equals(sDBName.toUpperCase())) {
				String sMessage = getMessage("wizard.step.attributeproperties.16", "<html>Der Spaltenname wird vom System vergeben.<p> " +
					"Bitte \u00e4ndern Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

		int iCheck = 30;
		if(this.model.getAttribute().getMetaVO() != null) {
			iCheck -= 9;
		}

		if(sDBName.length() > iCheck) {
			String sMessage = getMessage("wizard.step.attributeproperties.17", "<html>Der Spaltenname ist zu lang.<p> " +
				"Bitte k\u00fcrzen Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");
			JLabel lb = new JLabel(sMessage);
			JOptionPane.showMessageDialog(this, lb);
			throw new InvalidStateException("");
		}
		else {
			super.applyState();
		}

		Boolean blnNullable;
		if(attr.getId() != null)
			blnNullable = MetaDataDelegate.getInstance().getEntityField(parentWizardModel.getEntityName(), attr.getOldInternalName()).isNullable();
		else {
			blnNullable = Boolean.TRUE;
		}

		if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.util.Date") && this.parentWizardModel.isEditMode() && cbMandatory.isSelected()) {
			Date date;
			try {
				date = dateMandatory.getDate();
			}
			catch(CommonValidationException e) {
				String sMessage = e.getMessage();
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
			if(date == null) {
				String sMessage = getMessage("wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
			else {
				this.getModel().getAttribute().setMandatoryValue(date);
			}
		}
		else if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer") && this.parentWizardModel.isEditMode() && cbMandatory.isSelected()) {
			int iScale = this.getModel().getAttribute().getDatatyp().getScale();
			if(tfMandatory.getText().length() > iScale) {
				String sMessage = getMessage("wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}
		else if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Double") && this.parentWizardModel.isEditMode() && cbMandatory.isSelected()) {
			int iScale = this.getModel().getAttribute().getDatatyp().getScale();
			int iPrecision = this.getModel().getAttribute().getDatatyp().getPrecision();
			String sValue = tfMandatory.getText().replace(',', '.');
			String s[] = sValue.split("\\.");
			if(s.length != 2) {
				String sMessage = getMessage("wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
			if(s[0].length() > iScale-iPrecision || s[1].length() > iPrecision) {
				String sMessage = getMessage("wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

		if(this.parentWizardModel.isEditMode() && cbMandatory.isSelected()) {
			Object obj = this.model.getAttribute().getMandatoryValue();
			String sMessage = null;
			if(obj != null && obj instanceof String) {
				String s = (String)obj;
				if(s.length() < 1)
					sMessage = getMessage("wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
			}
			else if(obj == null) {
				sMessage = getMessage("wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
			}

			if(sMessage != null) {
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

	}

	static class LimitCharacterDocument extends PlainDocument {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
			if(offset > 22) {
				return;
			}
			super.insertString(offset, str, a);
	    }

	}

	class DefaultValueDocumentListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		protected void doSomeWork(DocumentEvent e) {
			try {
				NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setDefaultValue(e.getDocument().getText(0, e.getDocument().getLength()));

			} catch (BadLocationException ex) {
				Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
			}
		}

	}

	class MandatoryValueDocumentListener implements DocumentListener {

		@Override
		public void changedUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			doSomeWork(e);
		}

		protected void doSomeWork(DocumentEvent e) {
			try {
				String sJavaType = NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().getDatatyp().getJavaType();
				String sValue = e.getDocument().getText(0, e.getDocument().getLength());
				if(sJavaType.equals("java.lang.Integer")) {
					try {
						Integer i = new Integer(sValue);
						NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(i);
					}
					catch(Exception ex){
						NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(null);
					}

				}
				else if(sJavaType.equals("java.lang.Double")) {
					sValue = sValue.replace(',', '.');
					try {
						Double d = new Double(sValue);
						NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(d);
					}
					catch(Exception ex) {
						NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(null);
					}
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.model.getAttribute().setMandatoryValue(sValue);
				}

			} catch (BadLocationException ex) {
				Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
			}
		}

	}

	static class SpecialCharacterDocument extends PlainDocument {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public void insertString(int offset, String str, javax.swing.text.AttributeSet a)  throws javax.swing.text.BadLocationException {
			str = StringUtils.replace(str, "\u00e4", "ae");
			str = StringUtils.replace(str, "\u00f6", "oe");
			str = StringUtils.replace(str, "\u00fc", "ue");
			str = StringUtils.replace(str, "\u00c4", "ae");
			str = StringUtils.replace(str, "\u00d6", "oe");
			str = StringUtils.replace(str, "\u00dc", "ue");
			str = StringUtils.replace(str, "\u00df", "ss");
			str = str.replaceAll("[^\\w]", "");
			super.insertString(offset, str, a);
	    }

	}

}
