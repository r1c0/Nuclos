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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JButton;
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
import org.apache.log4j.Logger;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.nuclos.client.attribute.AttributeDelegate;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.scripting.ScriptEditor;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Bubble.Position;
import org.nuclos.client.ui.DateChooser;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.labeled.LabeledComponentSupport;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.ValueList;
import org.nuclos.client.wizard.util.DefaultValue;
import org.nuclos.client.wizard.util.DoubleFormatDocument;
import org.nuclos.client.wizard.util.MoreOptionPanel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.client.wizard.util.NumericFormatDocument;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosScript;
import org.nuclos.common2.RelativeDate;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;
import org.springframework.beans.factory.annotation.Configurable;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
@Configurable
public class NuclosEntityAttributeCommonPropertiesStep extends NuclosEntityAttributeAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityAttributeCommonPropertiesStep.class);

	static final String[] forbiddenNames = {"INTID","DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION",
		"STRSYSTEMID", "INTID_T_MD_PROCESS", "STRORIGIN", "BLNNUCLOSDELETED", "INTID_T_MD_STATE"};
	
	//

	private JLabel lbLabel;
	private JTextField tfLabel;
	private JLabel lbDefaultValue;
	private JTextField tfDefaultValue;
	private JComboBox cbxDefaultValue;
	private DateChooser dateDefaultValue;
	private JCheckBox cbDefaultValue;
	private JLabel lbDBFieldName;
	private JTextField tfDBFieldName;
	private JLabel lbDBFieldNameComplete;
	private JTextField tfDBFieldNameComplete;

	private JLabel lbDistinct;
	private JCheckBox cbDistinct;
	private JLabel lbLogBook;
	private JCheckBox cbLogBook;
	private JLabel lbMandatory;
	private JCheckBox cbMandatory;

	private JTextField tfMandatory;
	private JComboBox cbxMandatory;
	private DateChooser dateMandatory;
	private JCheckBox cbMandatoryValue;

	private JLabel lbIndexed;
	private JCheckBox cbIndexed;

	private JLabel lbAttributeGroup;
	private JComboBox cbxAttributeGroup;

	private JLabel lbCalcFunction;
	private JComboBox cbxCalcFunction;

	private JLabel lbCalculationScript;
	private JButton btCalculationScript;

	private boolean blnLabelModified;
	private boolean blnDefaultSelected;

	private JPanel pnlMoreOptions;

	private NuclosEntityWizardStaticModel parentWizardModel;


	public NuclosEntityAttributeCommonPropertiesStep() {
		// initComponents();
	}

	public NuclosEntityAttributeCommonPropertiesStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityAttributeCommonPropertiesStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}

	@PostConstruct
	@Override
	protected void initComponents() {
		double size [][] = {{150,20, TableLayout.FILL}, {20,20,20,20,20,20,20,20,90, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		
		final LabeledComponentSupport support = new LabeledComponentSupport();

		lbLabel = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.10", "Feldname")+": ");
		tfLabel = new JTextField();
		tfLabel.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfLabel.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.10", "Feldname"));

		lbDefaultValue = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.11", "Standardwert")+": ");
		tfDefaultValue = new JTextField();
		tfDefaultValue.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));
		tfDefaultValue.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		cbxDefaultValue = new JComboBox();
		cbxDefaultValue.setVisible(false);
		cbxDefaultValue.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		dateDefaultValue = new DateChooser(support, true);
		dateDefaultValue.setVisible(false);
		dateDefaultValue.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		cbDefaultValue = new JCheckBox();
		cbDefaultValue.setVisible(false);
		cbDefaultValue.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.11", "Standardwert"));

		lbDistinct = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.7", "Eindeutig")+": ");
		cbDistinct = new JCheckBox();
		cbDistinct.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.7", "Eindeutig"));

		lbLogBook = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.8", "Logbuch")+": ");
		cbLogBook = new JCheckBox();
		cbLogBook.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.8", "Logbuch"));

		lbMandatory = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.9", "Pflichtfeld")+": ");
		cbMandatory = new JCheckBox();
		cbMandatory.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.9", "Pflichtfeld"));

		tfMandatory = new JTextField();
		tfMandatory.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfMandatory.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));

		cbxMandatory = new JComboBox();
		cbxMandatory.setVisible(false);
		cbxMandatory.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));
		dateMandatory = new DateChooser(support);
		dateMandatory.setVisible(false);
		dateMandatory.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));
		cbMandatoryValue = new JCheckBox();
		cbMandatoryValue.setVisible(false);
		cbMandatoryValue.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.27", "Defaultwert für Pflichtfeld"));

		lbDBFieldName = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.12", "DB-Spaltename"));
		tfDBFieldName = new JTextField();
		tfDBFieldName.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.12", "DB-Spaltename"));
		tfDBFieldName.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbDBFieldNameComplete = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.18", "Vollst\u00e4ndiger Spaltenname"));
		tfDBFieldNameComplete = new JTextField();
		tfDBFieldNameComplete.setEnabled(false);

		lbAttributeGroup = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.19", "Attributegruppe"));
		cbxAttributeGroup = new JComboBox();
		cbxAttributeGroup.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.19", "Attributegruppe"));

		lbCalcFunction = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.20", "Berechungsvorschrift"));
		cbxCalcFunction = new JComboBox();
		cbxCalcFunction.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.20", "Berechungsvorschrift"));

		lbCalculationScript = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.calculationscript.label", "Berechnungsausdruck"));
		lbCalculationScript.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.calculationscript.description", "Berechnungsausdruck"));
		btCalculationScript = new JButton("...");
		btCalculationScript.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.calculationscript.description", "Berechnungsausdruck"));
		btCalculationScript.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ScriptEditor editor = new ScriptEditor();
				if (getModel().getAttribute().getCalculationScript() != null) {
					editor.setScript(getModel().getAttribute().getCalculationScript());
				}
				editor.run();
				NuclosScript script = editor.getScript();
				if (org.nuclos.common2.StringUtils.isNullOrEmpty(script.getSource())) {
					script = null;
				}
				getModel().getAttribute().setCalculationScript(script);
			}
		});

		lbIndexed = new JLabel(localeDelegate.getMessage("wizard.step.attributeproperties.26", "Indiziert"));
		cbIndexed = new JCheckBox();
		cbIndexed.setToolTipText(localeDelegate.getMessage("wizard.step.attributeproperties.tooltip.26", "Indiziert"));

		pnlMoreOptions = new JPanel();

		double sizeMoreOptions [][] = {{150, TableLayout.FILL}, {20,20,20, TableLayout.FILL}};

		TableLayout tlMoreOptions = new TableLayout(sizeMoreOptions);
		tlMoreOptions.setVGap(3);
		tlMoreOptions.setHGap(5);

		pnlMoreOptions.setLayout(tlMoreOptions);
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
		this.add(lbCalculationScript, "0,7");
		this.add(btCalculationScript, "1,7");

		this.add(optionPanel, "0,8, 2,8");


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
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setInternalName(e.getDocument().getText(0, e.getDocument().getLength()));
					if(!NuclosEntityAttributeCommonPropertiesStep.this.getModel().isEditMode()) {
						String sPrefix = Attribute.getDBPrefix(NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute());
						tfDBFieldName.setText(sPrefix + e.getDocument().getText(0, e.getDocument().getLength()));
					}

				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
				}
			}
		});



		tfDefaultValue.getDocument().addDocumentListener(new DefaultValueDocumentListener());

		tfMandatory.getDocument().addDocumentListener(new MandatoryValueDocumentListener());
		tfMandatory.setLocale(localeDelegate.getLocale());


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
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDbName(e.getDocument().getText(0, e.getDocument().getLength()));

					String s = e.getDocument().getText(0, e.getDocument().getLength());
					if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() != null){
						s = "STRVALUE_" + s;
					}
					else if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() == null){
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
				NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDistinct(cb.isSelected());
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
				NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setLogBook(cb.isSelected());
			}
		});

		cbMandatory.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				final JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatory(cb.isSelected());
				if(NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.isEditMode() && cb.isSelected() && !parentWizardModel.isVirtual()) {
					if(NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().getMandatoryValue() == null) {
						(new Bubble(cb, localeDelegate.getMessage(
								"wizard.step.attributeproperties.tooltip.28", "Bitte tragen Sie einen Wert ein mit dem das Feld vorbelegt werden kann!"), 3, Position.UPPER)).setVisible(true);
					}

				}
			}
		});

		cbIndexed.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setIndexed(cb.isSelected());
			}
		});

		cbxDefaultValue.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					getModel().getAttribute().setIdDefaultValue((DefaultValue)e.getItem());
				}
			}
		});

		cbxMandatory.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					getModel().getAttribute().setMandatoryValue(((DefaultValue)e.getItem()).getId());
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
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDefaultValue(value);
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
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDefaultValue("ja");
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDefaultValue("nein");
				}
			}
		});

		cbMandatoryValue.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				if(cb.isSelected()) {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(Boolean.TRUE);
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(Boolean.FALSE);
				}
			}
		});

		cbxAttributeGroup.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setAttributeGroup((String)e.getItem());
					NuclosEntityAttributeCommonPropertiesStep.this.setComplete(true);
				}
			}
		});

		cbxCalcFunction.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setCalcFunction((String)e.getItem());
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

		if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer")) {
			final NumericFormatDocument nfd = new NumericFormatDocument();
			nfd.addDocumentListener(new DefaultValueDocumentListener());
			tfDefaultValue.setDocument(nfd);

			final NumericFormatDocument nfdMandatory = new NumericFormatDocument();
			nfdMandatory.addDocumentListener(new MandatoryValueDocumentListener());
			tfMandatory.setDocument(nfdMandatory);
		}
		else if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Double")) {
			final DoubleFormatDocument dfd = new DoubleFormatDocument();
			dfd.addDocumentListener(new DefaultValueDocumentListener());
			tfDefaultValue.setDocument(dfd);

			final DoubleFormatDocument dfdMandatory = new DoubleFormatDocument();
			dfdMandatory.addDocumentListener(new MandatoryValueDocumentListener());
			tfMandatory.setDocument(dfdMandatory);

		}

		if(getModel().isEditMode()) {
			tfLabel.setText(getModel().getAttribute().getInternalName());
			cbxCalcFunction.setSelectedItem(getModel().getAttribute().getCalcFunction());
			cbxAttributeGroup.setSelectedItem(getModel().getAttribute().getAttributeGroup());
			cbDistinct.setSelected(getModel().getAttribute().isDistinct());
			ItemListener ilArray[] = cbMandatory.getItemListeners();
			for(ItemListener il : ilArray) {
				cbMandatory.removeItemListener(il);
			}
			cbMandatory.setSelected(getModel().getAttribute().isMandatory());
			for(ItemListener il : ilArray) {
				cbMandatory.addItemListener(il);
			}
			cbLogBook.setSelected(getModel().getAttribute().isLogBook());
			cbIndexed.setSelected(getModel().getAttribute().isIndexed());
			if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.util.Date")) {
				String str = getModel().getAttribute().getDefaultValue();
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
						LOG.warn("prepare failed: " + e, e);
					}
				}
			}
			else if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Boolean")) {
			   String value = getModel().getAttribute().getDefaultValue();
				if(value != null && value.equalsIgnoreCase("ja")) {
					cbDefaultValue.setSelected(true);
				}
				else {
					cbDefaultValue.setSelected(false);
				}
			}
			else {
				tfDefaultValue.setText(getModel().getAttribute().getDefaultValue());
			}

			if(getModel().getAttribute().getDbName() != null) {
				String sModifiedDBName = new String(getModel().getAttribute().getDbName());

				tfDBFieldName.setText(sModifiedDBName.replaceFirst("^STRVALUE_", "").replaceFirst("^INTID_", ""));
				if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() != null){
					tfDBFieldNameComplete.setText("STRVALUE_"+ getModel().getAttribute().getDbName());
				}
				else if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() == null){
					tfDBFieldNameComplete.setText("INTID_"+ getModel().getAttribute().getDbName());
				}
			}
		}
		else {
			if(!blnLabelModified)
				tfLabel.setText(getModel().getName().toLowerCase());

			if(getModel().getAttributeCount() == 0 && !blnDefaultSelected) {
				cbDistinct.setSelected(true);
				cbMandatory.setSelected(true);
				blnDefaultSelected = true;
			}
			cbDistinct.setEnabled(!this.parentWizardModel.hasRows());

			cbIndexed.setSelected(getModel().isRefernzTyp());

			if(cbxAttributeGroup.getModel().getSize() > 1) {
				if(this.parentWizardModel.isStateModel())
					cbxAttributeGroup.setSelectedIndex(1);
			}

			if(getModel().getAttribute().getDbName() != null) {
				String sModifiedDBName = new String(getModel().getAttribute().getDbName());

				tfDBFieldName.setText(sModifiedDBName.replaceFirst("^STRVALUE_", "").replaceFirst("^INTID_", ""));
				if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() != null){
					tfDBFieldNameComplete.setText("STRVALUE_"+ getModel().getAttribute().getDbName());
				}
				else if(getModel().getAttribute().getMetaVO() != null && getModel().getAttribute().getField() == null){
					tfDBFieldNameComplete.setText("INTID_"+ getModel().getAttribute().getDbName());
				}
			}
		}

		Object objMandatoryValue = getModel().getAttribute().getMandatoryValue();

		if(getModel().isRefernzTyp()) {

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
			Collection<MasterDataVO> colVO = MasterDataDelegate.getInstance().getMasterData(getModel().getAttribute().getMetaVO().getEntity());
			for (MasterDataVO vo : colVO) {
				String sField = getModel().getAttribute().getField();
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
			cbxDefaultValue.setSelectedItem(getModel().getAttribute().getIdDefaultValue());

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
		else if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.util.Date")) {
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
		else if(getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Boolean")) {
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
		else if(getModel().getAttribute().isValueList()) {
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
			for(ValueList valueList : getModel().getAttribute().getValueList()) {
				cbxDefaultValue.addItem(new DefaultValue(valueList.getId() != null ? valueList.getId().intValue() : null, valueList.getLabel()));
			}

			cbxDefaultValue.setSelectedItem(getModel().getAttribute().getDefaultValue());
			for(ItemListener il : listener){
				cbxDefaultValue.addItemListener(il);
			}

			listener = cbxMandatory.getItemListeners();
			for(ItemListener il : listener){
				cbxMandatory.removeItemListener(il);
			}

			for(ValueList valueList : getModel().getAttribute().getValueList()) {
				DefaultValue dv = new DefaultValue(valueList.getId() != null ? valueList.getId().intValue() : null, valueList.getLabel());
				cbxMandatory.addItem(dv);
				if(dv.getId() != null && dv.getId().equals(objMandatoryValue)) {
					cbxMandatory.setSelectedItem(dv);
				}
			}

			for(ItemListener il : listener){
				cbxMandatory.addItemListener(il);
			}

			cbMandatory.setEnabled(getModel().getAttribute().getId() != null);
			cbxMandatory.setVisible(parentWizardModel.isEditMode() && getModel().getAttribute().getId() != null);
			tfMandatory.setVisible(false);
			dateMandatory.setVisible(false);
			cbMandatoryValue.setVisible(false);
		}
		else if(getModel().getAttribute().isImage() || getModel().getAttribute().isPasswordField() || getModel().getAttribute().isFileType()) {
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

		Attribute attr = NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute();
		if(NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.hasRows() && attr.getId() != null ) {
			boolean blnAllowed = MetaDataDelegate.getInstance().isChangeDatabaseColumnToUniqueAllowed(
				NuclosEntityAttributeCommonPropertiesStep.this.parentWizardModel.getEntityName(), attr.getInternalName());

			if(!blnAllowed && !attr.isDistinct()) {
				cbDistinct.setSelected(false);
				cbDistinct.setEnabled(false);
				cbDistinct.setToolTipText(localeDelegate.getMessage(
						"wizard.step.entitysqllayout.6", "Das Feld {0} kann nicht auf ein eindeutiges Feld umgestellt werden.", attr.getLabel()));
			}
		}

		if (parentWizardModel.isVirtual()) {
			cbDistinct.setSelected(false);
			cbDistinct.setEnabled(false);
			tfMandatory.setEnabled(false);
			dateMandatory.setEnabled(false);
			cbMandatory.setEnabled(false);
			cbxMandatory.setEnabled(false);
			cbMandatoryValue.setEnabled(false);
			cbxCalcFunction.setEnabled(false);
			tfDBFieldName.setEnabled(false);
			cbIndexed.setEnabled(false);
			tfDefaultValue.setEnabled(false);
			dateDefaultValue.setEnabled(false);
			cbxDefaultValue.setEnabled(false);
			cbDefaultValue.setEnabled(false);
			cbLogBook.setEnabled(false);
		}
	}

	public void setParentWizardModel(NuclosEntityWizardStaticModel model) {
		this.parentWizardModel = model;
	}
	
	@Override
	public void close() {
		lbLabel = null;
		tfLabel = null;
		lbDefaultValue = null;
		tfDefaultValue = null;
		cbxDefaultValue = null;
		dateDefaultValue = null;
		cbDefaultValue = null;
		lbDBFieldName = null;
		tfDBFieldName = null;
		lbDBFieldNameComplete = null;
		tfDBFieldNameComplete = null;

		lbDistinct = null;
		cbDistinct = null;
		lbLogBook = null;
		cbLogBook = null;
		lbMandatory = null;
		cbMandatory = null;

		tfMandatory = null;
		cbxMandatory = null;
		dateMandatory = null;
		cbMandatoryValue = null;

		lbIndexed = null;
		cbIndexed = null;

		lbAttributeGroup = null;
		cbxAttributeGroup = null;

		lbCalcFunction = null;
		cbxCalcFunction = null;

		lbCalculationScript = null;
		btCalculationScript = null;

		pnlMoreOptions = null;

		parentWizardModel = null;
		
		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		Attribute attr = getModel().getAttribute();

		String sDBName = attr.getDbName();

		if(!getModel().isEditMode()) {
			for(Attribute attribute : this.parentWizardModel.getAttributeModel().getAttributes()) {
				if(attribute.getDbName().equals(sDBName)) {
					String sMessage = localeDelegate.getMessage(
							"wizard.step.attributeproperties.15", "<html>Der Spaltenname existiert schon in der Tabelle.<p> " +
						"Bitte \u00e4ndern Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");

					JLabel lb = new JLabel(sMessage);
					JOptionPane.showMessageDialog(this, lb);
					throw new InvalidStateException("");
				}
			}
		}

		if(getModel().getAttribute().getInternalName().length() > 250) {
			String sMessage = localeDelegate.getMessage(
					"wizard.step.attributeproperties.15", "<html>Der Feldname ist zu lang.<p> " +
			"Bitte \u00e4ndern Sie den Namen.</html>");

			JLabel lb = new JLabel(sMessage);
			JOptionPane.showMessageDialog(this, lb);
			throw new InvalidStateException("");
		}

		for(String sFieldName : forbiddenNames) {
			if(sFieldName.equals(sDBName.toUpperCase())) {
				String sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.16", "<html>Der Spaltenname wird vom System vergeben.<p> " +
					"Bitte \u00e4ndern Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

		int iCheck = 30;
		if(getModel().getAttribute().getMetaVO() != null) {
			iCheck -= 9;
		}

		if(sDBName.length() > iCheck) {
			String sMessage = localeDelegate.getMessage(
					"wizard.step.attributeproperties.17", "<html>Der Spaltenname ist zu lang.<p> " +
				"Bitte k\u00fcrzen Sie den Namen oder vergeben einen im Expertenmodus selbst.</html>");
			JLabel lb = new JLabel(sMessage);
			JOptionPane.showMessageDialog(this, lb);
			throw new InvalidStateException("");
		}

		Boolean blnNullable;
		if(attr.getId() != null)
			blnNullable = MetaDataDelegate.getInstance().getEntityField(parentWizardModel.getEntityName(), attr.getOldInternalName()).isNullable();
		else {
			blnNullable = Boolean.TRUE;
		}

		Boolean requiresDefault = !blnNullable && cbMandatory.isSelected() && !parentWizardModel.isVirtual();

		if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.util.Date") && this.parentWizardModel.isEditMode() && requiresDefault) {
			Date date;
			try {
				date = dateMandatory.getDate();
			}
			catch(CommonValidationException e) {
				LOG.warn("applyState failed: " + e, e);
				String sMessage = e.toString();
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException(sMessage);
			}
			if(date == null) {
				String sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
			else {
				this.getModel().getAttribute().setMandatoryValue(date);
			}
		}
		else if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Integer") && this.parentWizardModel.isEditMode() && requiresDefault) {
			int iScale = this.getModel().getAttribute().getDatatyp().getScale();
			if(tfMandatory.getText().length() > iScale) {
				String sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}
		else if(this.getModel().getAttribute().getDatatyp().getJavaType().equals("java.lang.Double") && this.parentWizardModel.isEditMode() && requiresDefault) {
			int iScale = this.getModel().getAttribute().getDatatyp().getScale();
			int iPrecision = this.getModel().getAttribute().getDatatyp().getPrecision();
			String sValue = tfMandatory.getText().replace(',', '.');
			String s[] = sValue.split("\\.");
			if(s.length != 2) {
				String sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
			if(s[0].length() > iScale-iPrecision || s[1].length() > iPrecision) {
				String sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.27", "Der angegebene Defaultwert hat keinen gültigen Wert!");
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

		if(this.parentWizardModel.isEditMode() && requiresDefault) {
			Object obj = getModel().getAttribute().getMandatoryValue();
			String sMessage = null;
			if(obj != null && obj instanceof String) {
				String s = (String)obj;
				if(s.length() < 1)
					sMessage = localeDelegate.getMessage(
							"wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
			}
			else if(obj == null) {
				sMessage = localeDelegate.getMessage(
						"wizard.step.attributeproperties.tooltip.29", "Sie haben keinen Wert für das Pflichtfeld angegeben!");
			}

			if(sMessage != null) {
				JLabel lb = new JLabel(sMessage);
				JOptionPane.showMessageDialog(this, lb);
				throw new InvalidStateException("");
			}
		}

		if (parentWizardModel.isVirtual()) {
			attr.setReadonly(parentWizardModel.isEditable());
		}
		
		super.applyState();
	}

	static class LimitCharacterDocument extends PlainDocument {

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
				NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setDefaultValue(e.getDocument().getText(0, e.getDocument().getLength()));

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
						NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(i);
					}
					catch(Exception e1){
						LOG.warn("doSomeWork failed: " + e1, e1);
						NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(null);
					}
				}
				else if(sJavaType.equals("java.lang.Double")) {
					sValue = sValue.replace(',', '.');
					try {
						Double d = new Double(sValue);
						NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(d);
					}
					catch(Exception e1) {
						LOG.warn("doSomeWork failed: " + e1, e1);
						NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(null);
					}
				}
				else {
					NuclosEntityAttributeCommonPropertiesStep.this.getModel().getAttribute().setMandatoryValue(sValue);
				}

			} catch (BadLocationException ex) {
				Errors.getInstance().showExceptionDialog(NuclosEntityAttributeCommonPropertiesStep.this, ex);
			}
		}

	}

	static class SpecialCharacterDocument extends PlainDocument {

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
