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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.resource.admin.CollectableResouceSaveListener;
import org.nuclos.client.resource.admin.ResourceCollectController;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectControllerFactorySingleton;
import org.nuclos.client.ui.resource.ResourceIconChooser;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.util.ModifierMap;
import org.nuclos.client.wizard.util.MoreOptionPanel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
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

public class NuclosEntityCommonPropertiesStep extends NuclosEntityAbstractStep implements CollectableResouceSaveListener {

	private static final Logger LOG = Logger.getLogger(NuclosEntityCommonPropertiesStep.class);

	private static final String[] VIRTUAL_STATICFIELDS = new String[] {"INTID", "DATCREATED", "STRCREATED", "DATCHANGED", "STRCHANGED", "INTVERSION"};

	private JLabel lbLabelSingular;
	private JTextField tfLabelSingular;
	private JLabel lbMenupath;
	private JComboBox cbMenupath;
	private JButton btMenupath;
	private JLabel lbSystemIdPrefix;
	private JTextField tfSystemIdPrefix;
	private JLabel lbIconCustom;
	private JComboBox cbxIcon;
	private JButton btNewIcon;

	private JLabel lbAccelerator;
	private JComboBox cbxModifier;
	private JTextField tfMnemonic;

	private JLabel lbLogbook;
	private JCheckBox cbLogbook;
	private JLabel lbSearchable;
	private JCheckBox cbSearchable;
	private JLabel lbEditable;
	private JCheckBox cbEditable;

	private JLabel lbShowRelation;
	private JCheckBox cbShowRelation;
	private JLabel lbShowGroups;
	private JCheckBox cbShowGroups;

	private JLabel lbStateModel;
	private JCheckBox cbStateModel;

	private JLabel lbVirtual;
	private JComboBox cbxVirtual;
	private JLabel lbIdFactory;
	private JComboBox cbxIdFactory;
	
	private JLabel lbCache;
	private JCheckBox cbCache;
	private JLabel lbTableName;
	private JTextField tfTableName;
	private JLabel lbInternalEntityName;
	private JTextField tfInternalEntityName;

	private JLabel lbIcon;
	private ResourceIconChooser nuclosIconChooser;

	private JPanel pnlMoreOptions;

	private boolean blnSingular;
	private boolean blnSingularModified;
	private boolean blnMenuPathModified;
	private boolean blnIconModified;
	private boolean blnSystemId;


	public NuclosEntityCommonPropertiesStep() {
		initComponents();
	}

	public NuclosEntityCommonPropertiesStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntityCommonPropertiesStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}

	@Override
	public void prepare() {
		super.prepare();
		initMenupathElement();
		if(!model.isEditMode()) {
			if(!blnSingularModified)
				tfLabelSingular.setText(model.getLabelSingular());
			if(!blnIconModified)
				this.model.setIcon(ResourceCache.getResourceByName("NCL_MAINFRAME_TITLE"));
			if(!blnMenuPathModified) {
				JTextField field = (JTextField)cbMenupath.getEditor().getEditorComponent();
				field.setText(model.getMenuPath());
			}
			cbMenupath.setSelectedItem(model.getMenuPath());
			tfInternalEntityName.setText(model.getEntityName());
			setComplete(true);
			tfTableName.setText(NuclosWizardUtils.replace(model.getTableOrViewName()));
			tfSystemIdPrefix.setText(model.getSystemIdPrefix());
			tfMnemonic.setText(model.getAccelerator());
			cbxModifier.setSelectedItem(model.getModifierAsString());
			cbStateModel.setSelected(false);
		}
		else {
			cbStateModel.setEnabled(!model.hasRows());
			tfLabelSingular.setText(model.getLabelSingular());
			this.model.setIcon(ResourceCache.getResourceByName("NCL_MAINFRAME_TITLE"));
			JTextField field = (JTextField)cbMenupath.getEditor().getEditorComponent();
			field.setText(model.getMenuPath());
			cbMenupath.setSelectedItem(model.getMenuPath());
			tfTableName.setText(model.getTableOrViewName());
			tfTableName.setEnabled(false);
			cbCache.setSelected(model.isCachable());
			cbSearchable.setSelected(model.isSearchable());
			cbEditable.setSelected(model.isEditable());
			cbShowRelation.setSelected(model.isShowRelation());
			cbShowGroups.setSelected(model.isShowGroups());
			cbLogbook.setSelected(model.isLogbook());
			tfInternalEntityName.setText(model.getEntityName());
			tfInternalEntityName.setEnabled(false);
			tfSystemIdPrefix.setText(model.getSystemIdPrefix());
			cbShowGroups.setSelected(model.isShowGroups());
			cbShowRelation.setSelected(model.isShowRelation());
			cbStateModel.setSelected(model.isStateModel());
			if(!model.isStateModel()){
				lbSystemIdPrefix.setText(getMessage("wizard.step.entitycommonproperties.12", "K\u00fcrzel f\u00fcr Identifizierer:"));
			}
			else {
				lbSystemIdPrefix.setText(getMessage("wizard.step.entitycommonproperties.13", "K\u00fcrzel f\u00fcr Identifizierer *:"));
			}

			tfMnemonic.setText(model.getAccelerator());

			cbxModifier.setSelectedItem(model.getModifierAsString());
			try {
				Integer iResourceID = model.getResourceId();
				if(iResourceID != null) {
					MasterDataVO vo = MasterDataDelegate.getInstance().get(NuclosEntity.RESOURCE.getEntityName(), iResourceID);
					cbxIcon.setSelectedItem(vo);
				}
			}
			catch(CommonFinderException e) {
				// do nothing here
				LOG.info("prepare failed: " + e, e);
			}
			catch(CommonPermissionException e) {
				// do nothing here
				LOG.info("prepare failed: " + e, e);
			}

			nuclosIconChooser.setSelected(model.getNuclosResourceName());

			if(hasDocumentType()) {
				cbStateModel.setEnabled(false);
			}
			else {
				cbStateModel.setEnabled(true);
			}

			final boolean virtual = model.isVirtual();
			if (virtual) {
				cbxVirtual.setSelectedItem(model.getVirtualentity());
				cbxIdFactory.setSelectedItem(model.getIdFactory());
				cbStateModel.setEnabled(false);
				cbLogbook.setEnabled(false);
				cbEditable.setEnabled(false);
				final boolean emptyIdFactory = StringUtils.isNullOrEmpty(model.getIdFactory());
				cbEditable.setSelected(!emptyIdFactory);
			}
			cbxVirtual.setEnabled(false);
		}
		model.setEditable(cbEditable.isSelected());
		model.setSearchable(cbSearchable.isSelected());
		model.setStateModel(cbStateModel.isSelected());
		model.setShowGroups(cbShowGroups.isSelected());
		model.setShowRelation(cbShowRelation.isSelected());
		model.setLogbook(cbLogbook.isSelected());

		lbSystemIdPrefix.setEnabled(model.isStateModel());
		tfSystemIdPrefix.setEnabled(model.isStateModel());
		lbShowGroups.setEnabled(model.isStateModel());
		cbShowGroups.setEnabled(model.isStateModel());
		lbShowRelation.setEnabled(model.isStateModel());
		cbShowRelation.setEnabled(model.isStateModel());


		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setComplete(true);
			}
		});

	}

	private void initMenupathElement() {
		cbMenupath.removeAllItems();
		cbMenupath.addItem("");
		for(String sMenu : NuclosWizardUtils.getExistingMenuPaths()) {
			cbMenupath.addItem(sMenu);
		}
	}

	private boolean hasDocumentType() {
		for(EntityFieldMetaDataVO voField : MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(model.getEntityName()).values()) {
			if(voField.getDataType().equals("org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile"))
				return true;
		}
		return false;
	}



	@Override
	protected void initComponents() {

		double size [][] = {{170,200,20, TableLayout.FILL, 20}, {20,20,20,20,20,20,20,20,20,20,20,20,120,20,20, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		lbLabelSingular = new JLabel(getMessage("wizard.step.entitycommonproperties.1", "Beschriftung Fenstertitel")+": ");
		tfLabelSingular = new JTextField();
		tfLabelSingular.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.1", "Beschriftung Fenstertitel"));
		tfLabelSingular.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbMenupath = new JLabel(getMessage("wizard.step.entitycommonproperties.2", "Pfad im Men\u00fc")+": ");

		cbMenupath = new JComboBox();
		cbMenupath.setEditable(true);
		cbMenupath.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.2", "Pfad im Men\u00fc"));
		cbMenupath.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());

		lbSystemIdPrefix = new JLabel(getMessage("wizard.step.entitycommonproperties.12", "K\u00fcrzel f\u00fcr Identifizierer:"));
		tfSystemIdPrefix = new JTextField();
		tfSystemIdPrefix.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.12", "K\u00fcrzel f\u00fcr Identifizierer:"));
		tfSystemIdPrefix.setDocument(new LimitDocument(3));
		btMenupath = new JButton("...");
		btMenupath.setVisible(false);
		lbIconCustom = new JLabel(getMessage("wizard.step.entitycommonproperties.20", "Benutzerdefiniertes Icon")+": ");
		cbxIcon = new JComboBox();
		cbxIcon.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.3", "Icon"));
		btNewIcon = new JButton("...");
		btNewIcon.setToolTipText(getMessage("wizard.step.entitycommonproperties.4", "Neues Icon erstellen"));

		lbAccelerator = new JLabel(getMessage("wizard.step.entitycommonproperties.5", "Tastenk\u00fcrzel"));
		cbxModifier = new JComboBox();
		cbxModifier.addItem("");
		for(String str : ModifierMap.getModifierMap().keySet()) {
			cbxModifier.addItem(str);
		}
		tfMnemonic = new JTextField();
		tfMnemonic.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.5", "Tastenk\u00fcrzel"));
		tfMnemonic.setDocument(new LimitDocument(2));

		lbLogbook = new JLabel(getMessage("wizard.step.entitycommonproperties.14", "Logbuch:"));
		cbLogbook = new JCheckBox();
		cbLogbook.setSelected(true);
		cbLogbook.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.14", "Logbuch:"));

		lbSearchable = new JLabel(getMessage("wizard.step.entitycommonproperties.6", "Wird eine Suchmaske ben\u00f6tigt")+":");
		cbSearchable = new JCheckBox();
		cbSearchable.setSelected(true);
		cbSearchable.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.6", "Wird eine Suchmaske ben\u00f6tigt"));

		lbEditable = new JLabel(getMessage("wizard.step.entitycommonproperties.7", "Ist die Entit\u00e4t modifizierbar")+":");
		cbEditable = new JCheckBox();
		cbEditable.setSelected(true);
		cbEditable.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.7", "Ist die Entit\u00e4t modifizierbar"));

		lbShowRelation = new JLabel(getMessage("wizard.step.entitycommonproperties.15", "Zeige Relationen:"));
		cbShowRelation = new JCheckBox();
		cbShowRelation.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.15", "Zeige Relationen:"));

		lbShowGroups = new JLabel(getMessage("wizard.step.entitycommonproperties.16", "Zeige Gruppen:"));
		cbShowGroups = new JCheckBox();
		cbShowGroups.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.16", "Zeige Gruppen:"));

		lbStateModel = new JLabel(getMessage("wizard.step.entitycommonproperties.17", "Statusmodell:"));
		cbStateModel = new JCheckBox();
		cbStateModel.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.17", "Statusmodell:"));

		lbVirtual = new JLabel(getMessage("wizard.step.entitycommonproperties.virtual.label", "Virtual entity")+":");
		final List<String> ves = MetaDataDelegate.getInstance().getVirtualEntities();
		ves.add("");
		Collections.sort(ves);
		final ListComboBoxModel<String> virtualentitymodel = new ListComboBoxModel<String>(ves);
		cbxVirtual = new JComboBox(virtualentitymodel);
		cbxVirtual.setToolTipText(getMessage("wizard.step.entitycommonproperties.virtual.description", "Virtual entity (use an existing view)"));
		// enable virtual entity name for new entities
		cbxVirtual.setEnabled(true);
		
		lbIdFactory = new JLabel(getMessage("wizard.step.entitycommonproperties.idfactory.label", 
				"Primary key factory for VE") + ":");	
		final List<String> pif = MetaDataDelegate.getInstance().getPossibleIdFactories();
		pif.add("");
		Collections.sort(pif);
		final ListComboBoxModel<String> pifModel = new ListComboBoxModel<String>(pif);
		cbxIdFactory = new JComboBox(pifModel);
		cbxIdFactory.setToolTipText(getMessage("wizard.step.entitycommonproperties.idfactory.description", 
				"DB object to use for Id generation (if you want to write to VE)"));
		// disable id factories for new entities (will be enabled if a virtual entity is chosen)
		cbxIdFactory.setEnabled(false);

		lbCache = new JLabel(getMessage("wizard.step.entitycommonproperties.9", "Entit\u00e4t cachen")+":");
		cbCache = new JCheckBox();
		cbCache.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.9", "Entit\u00e4t cachen"));

		lbTableName = new JLabel(getMessage("wizard.step.entitycommonproperties.10", "Tabellenname")+":");
		tfTableName = new JTextField();
		tfTableName.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.10", "Tabellenname"));
		tfTableName.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		tfTableName.setDocument(new LimitDocument(31));

		lbInternalEntityName = new JLabel(getMessage("wizard.step.entitycommonproperties.11", "Interner Entity-Name"));
		tfInternalEntityName = new JTextField();
		tfInternalEntityName.setToolTipText(getMessage("wizard.step.entitycommonproperties.tooltip.11", "Interner Entity-Name"));

		lbIcon = new JLabel(getMessage("wizard.step.entitycommonproperties.3", "Icon")+": ");
		nuclosIconChooser = new ResourceIconChooser();

		this.add(lbLabelSingular, "0,0");
		this.add(tfLabelSingular, "1,0");

		this.add(lbMenupath, "0,1");
		this.add(cbMenupath, "1,1");
		this.add(btMenupath, "2,1");

		this.add(lbIconCustom, "0,2");
		this.add(cbxIcon, "1,2");
		this.add(btNewIcon, "2,2");

		this.add(lbAccelerator, "0,3");
		this.add(cbxModifier, "1,3");
		this.add(tfMnemonic, "2,3");

		this.add(lbSearchable, "0,4");
		this.add(cbSearchable, "1,4");

		this.add(lbEditable, "0,5");
		this.add(cbEditable, "1,5");

		this.add(lbLogbook, "0,6");
		this.add(cbLogbook, "1,6");

		this.add(lbStateModel, "0,7");
		this.add(cbStateModel, "1,7");

		this.add(lbSystemIdPrefix, "0,8");
		this.add(tfSystemIdPrefix, "1,8");

		this.add(lbShowRelation, "0,9");
		this.add(cbShowRelation, "1,9");

		this.add(lbShowGroups, "0,10");
		this.add(cbShowGroups, "1,10");

		this.add(lbIcon, "3,1");
		this.add(nuclosIconChooser, "3,2 , 3,15");

		double sizeMoreOptions [][] = {{170, 200, 20}, {20,20,20,20, TableLayout.FILL}};

		pnlMoreOptions = new JPanel();

		TableLayout tlMoreOptions = new TableLayout(sizeMoreOptions);
		tlMoreOptions.setVGap(3);
		tlMoreOptions.setHGap(5);
		pnlMoreOptions.setLayout(tlMoreOptions);
		pnlMoreOptions.add(lbVirtual, "0,0");
		pnlMoreOptions.add(cbxVirtual, "1,0");
		pnlMoreOptions.add(lbIdFactory, "0,1");
		pnlMoreOptions.add(cbxIdFactory, "1,1");
		pnlMoreOptions.add(lbCache, "0,2");
		pnlMoreOptions.add(cbCache, "1,2");
		pnlMoreOptions.add(lbTableName, "0,3");
		pnlMoreOptions.add(tfTableName, "1,3");
		pnlMoreOptions.add(lbInternalEntityName, "0,4");
		pnlMoreOptions.add(tfInternalEntityName, "1,4");

		MoreOptionPanel optionPanel = new MoreOptionPanel(pnlMoreOptions);

		this.add(optionPanel, "0,12,2,12");

		fillIconCombobox();

		cbLogbook.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setLogbook(cb.isSelected());
			}
		});

		cbSearchable.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setSearchable(cb.isSelected());
			}
		});

		cbEditable.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setEditable(cb.isSelected());
			}
		});

		cbStateModel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setStateModel(cb.isSelected());
				final boolean selected = cb.isSelected();
				if(selected) {
					lbSystemIdPrefix.setText(getMessage("wizard.step.entitycommonproperties.13", "K\u00fcrzel f\u00fcr Identifizierer *:"));
					if(tfSystemIdPrefix.getText().isEmpty()){
						NuclosEntityCommonPropertiesStep.this.setComplete(false);
					}
					else {
						NuclosEntityCommonPropertiesStep.this.setComplete(true);
					}
				}
				else {
					lbSystemIdPrefix.setText(getMessage("wizard.step.entitycommonproperties.12", "K\u00fcrzel f\u00fcr Identifizierer:"));
					NuclosEntityCommonPropertiesStep.this.setComplete(true);
				}
				lbSystemIdPrefix.setEnabled(selected);
				tfSystemIdPrefix.setEnabled(selected);
				lbShowGroups.setEnabled(selected);
				cbShowGroups.setEnabled(selected);
				lbShowRelation.setEnabled(selected);
				cbShowRelation.setEnabled(selected);
				
				// in case of a state entity, no virtual entity is possible
				lbVirtual.setEnabled(!selected);
				cbxVirtual.setEnabled(!selected);
				lbIdFactory.setEnabled(!selected);
				cbxIdFactory.setEnabled(!selected);
			}
		});

		cbCache.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setCachable(cb.isSelected());
			}
		});

		cbShowRelation.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setShowRelation(cb.isSelected());
			}
		});

		cbShowGroups.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setShowGroups(cb.isSelected());
			}
		});

		cbStateModel.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntityCommonPropertiesStep.this.model.setStateModel(cb.isSelected());
			}
		});

		tfLabelSingular.addKeyListener(new KeyListener() {

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
				blnSingularModified = true;
			}

		});


		tfLabelSingular.getDocument().addDocumentListener(new DocumentListener() {

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
					blnSingular = true;
				}
				else  {
					blnSingular = false;

				}
				if(blnSingular && (blnSystemId || !model.isStateModel())) {
					NuclosEntityCommonPropertiesStep.this.setComplete(true);
				}
				else {
					NuclosEntityCommonPropertiesStep.this.setComplete(false);
				}
				try {
					NuclosEntityCommonPropertiesStep.this.model.setLabelSingular(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
			}
		});

		tfTableName.getDocument().addDocumentListener(new DocumentListener() {

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
					NuclosEntityCommonPropertiesStep.this.model.setTableOrViewName(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
			}
		});

		tfInternalEntityName.getDocument().addDocumentListener(new DocumentListener() {

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
					NuclosEntityCommonPropertiesStep.this.model.setModifiedEntityName(e.getDocument().getText(0, e.getDocument().getLength()));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
			}
		});

		tfMnemonic.getDocument().addDocumentListener(new DocumentListener() {

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
					NuclosEntityCommonPropertiesStep.this.model.setAccelerator(e.getDocument().getText(0, 1));
				} catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
			}
		});

		cbxIcon.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					if (isNotifyIconSelectionChange()) {
						setNotifyIconSelectionChange(false);
						nuclosIconChooser.setSelected(null);
						setNotifyIconSelectionChange(true);
					}
					SwingUtilities.invokeLater(new Runnable() {

						@Override
						public void run() {
							final Object obj = e.getItem();
							if(obj instanceof MasterDataVO) {
								MasterDataVO vo = (MasterDataVO)obj;
								NuclosEntityCommonPropertiesStep.this.model.setResourceId(vo.getIntId());
								NuclosEntityCommonPropertiesStep.this.model.setResourceName((String)vo.getField("name"));
								blnIconModified = true;
							}
							else if(obj instanceof String) {
								NuclosEntityCommonPropertiesStep.this.model.setResourceId(null);
								NuclosEntityCommonPropertiesStep.this.model.setResourceName(null);
							}
						}
					});
				}

			}
		});

		nuclosIconChooser.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				if (isNotifyIconSelectionChange()) {
					setNotifyIconSelectionChange(false);
					cbxIcon.setSelectedIndex(0);
					setNotifyIconSelectionChange(true);
				}
				NuclosEntityCommonPropertiesStep.this.model.setNuclosResourceName(nuclosIconChooser.getSelectedResourceIconName());
			}
		});

		cbxModifier.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String sModifier = (String)e.getItem();
					Integer iModifier = ModifierMap.getModifierMap().get(sModifier);
					NuclosEntityCommonPropertiesStep.this.model.setModifier(iModifier);
				}
			}
		});

		((JTextField)cbMenupath.getEditor().getEditorComponent()).addKeyListener(new KeyListener() {

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
				blnMenuPathModified = true;
				String str = ((JTextField)cbMenupath.getEditor().getEditorComponent()).getText();
				lbSearchable.setEnabled(str.length() > 0);
				cbSearchable.setEnabled(str.length() > 0);

			}

		});

		cbMenupath.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String)e.getItem();
					lbSearchable.setEnabled(s != null && s.length() != 0);
					cbSearchable.setEnabled(s != null && s.length() != 0);
				}
			}
		});

		btNewIcon.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					 final CollectControllerFactorySingleton factory = CollectControllerFactorySingleton.getInstance();
					 ResourceCollectController rcc = factory.newResourceCollectController(parent, null);
					 rcc.addResouceSaveListener(NuclosEntityCommonPropertiesStep.this);
					 rcc.runNew();
				}
				catch(NuclosBusinessException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
				catch(CommonPermissionException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
				catch(CommonFatalException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
				catch(CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, ex);
				}
			}
		});

		tfSystemIdPrefix.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doSomeWork(e);			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doSomeWork(e);			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doSomeWork(e);
			}

			protected void doSomeWork(DocumentEvent e) {
				if(e.getDocument().getLength() < 1) {
					if(model.isStateModel())
						blnSystemId = false;
					else {
						blnSystemId = true;
					}
				}
				else {
					blnSystemId = true;
				}
				if(blnSingular && blnSystemId) {
					NuclosEntityCommonPropertiesStep.this.setComplete(true);
				}
				else {
					NuclosEntityCommonPropertiesStep.this.setComplete(false);
				}

				try {
					NuclosEntityCommonPropertiesStep.this.getModel().setSystemIdPrefix(e.getDocument().getText(0, e.getDocument().getLength()));
				}
				catch(BadLocationException e1) {
					LOG.warn("doSomeWork failed: " + e1, e1);
				}
			}

		});

		cbxVirtual.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				final String item = (String) e.getItem();
				final boolean empty = StringUtils.isNullOrEmpty(item);
				final boolean idEmpty = StringUtils.isNullOrEmpty((String) cbxIdFactory.getSelectedItem());
				
				cbEditable.setSelected(empty || !idEmpty);
				cbEditable.setEnabled(empty);
				
				model.setEditable(cbEditable.isSelected());
				cbLogbook.setSelected(empty);
				cbLogbook.setEnabled(empty);
				model.setEditable(cbLogbook.isSelected());
				cbCache.setSelected(empty);
				cbCache.setEnabled(empty);
				model.setCachable(cbCache.isSelected());
				cbStateModel.setEnabled(empty);
				
				// Modify table name (tp)
				if (empty) {
					model.setVirtualentity(null);
				}
				else {
					model.setVirtualentity(item);
				}
				tfTableName.setText(model.getTableOrViewName());
				
				// id factory is only possible for virtual entities
				cbxIdFactory.setEnabled(!empty);
				if (empty) {
					cbxIdFactory.setSelectedIndex(0);
				}
			}
		});
		cbxIdFactory.addItemListener(new ItemListener() {	
			@Override
			public void itemStateChanged(ItemEvent e) {
				final String item = (String) e.getItem();
				final boolean empty = StringUtils.isNullOrEmpty(item);
				final boolean veEmpty = StringUtils.isNullOrEmpty((String) cbxVirtual.getSelectedItem());
				cbEditable.setSelected(!empty || veEmpty);
				// model.setIdFactory(item);
			}
		});
	}

	protected void fillIconCombobox() {

		List<MasterDataVO> lstIcon = new ArrayList<MasterDataVO>(MasterDataDelegate.getInstance().getMasterData(NuclosEntity.RESOURCE.getEntityName()));
		Collections.sort(lstIcon, new Comparator<MasterDataVO>() {
			@Override
            public int compare(MasterDataVO o1, MasterDataVO o2) {
	            String sField1 = (String)o1.getField("name");
	            String sField2 = (String)o2.getField("name");
	            return sField1.toUpperCase().compareTo(sField2.toUpperCase());
            }
		});


		cbxIcon.addItem(" ");
		for(MasterDataVO vo : lstIcon) {
			cbxIcon.addItem(vo);
		}

	}

	@Override
    public void applyState() throws InvalidStateException {
	    super.applyState();

	    model.setMenuPath((String)cbMenupath.getSelectedItem());
	    model.setVirtualentity((String)cbxVirtual.getSelectedItem());
	    model.setIdFactory((String) cbxIdFactory.getSelectedItem());

	    String sTable = tfTableName.getText();
	    for(EntityMetaDataVO vo : MetaDataClientProvider.getInstance().getAllEntities()) {
	    	if(sTable.equalsIgnoreCase(vo.getDbEntity())) {
	    		if(this.model.getEntityName().equalsIgnoreCase(vo.getEntity()))
	    			continue;
	    		JOptionPane.showMessageDialog(this, getMessage("wizard.step.entitycommonproperties.18", "Der vergebene Tabellenname ist schon vorhanden. Bitte ändern Sie ihn in den erweiterten Einstellungen!"),
	    			getMessage("wizard.step.entitycommonproperties.19", "Achtung!"), JOptionPane.OK_OPTION);
	 	        throw new InvalidStateException();
	    	}
	    }

	    if(model.isVirtual()) {
	    	// initialize/update attributes from table/view metadata
	    	final List<String> staticfields = CollectionUtils.asList(VIRTUAL_STATICFIELDS);
	    	final List<EntityFieldMetaDataVO> attributes;
	    	Map<String, EntityFieldMetaDataVO> fields = new HashMap<String, EntityFieldMetaDataVO>();
	    	if (model.isEditMode()) {
	    		attributes = new ArrayList<EntityFieldMetaDataVO>();
		    	fields = new HashMap<String, EntityFieldMetaDataVO>();
		    	for (EntityFieldMetaDataVO field : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(model.getEntityName()).values()) {
		    		if (staticfields.contains(field.getDbColumn().toUpperCase())) {
		    			continue;
		    		}
		    		fields.put(field.getDbColumn().toUpperCase(), field);
		    	}
		    	for (EntityFieldMetaDataVO dbattribute : MetaDataDelegate.getInstance().getVirtualEntityFields(model.getVirtualentity())) {
		    		String name = dbattribute.getDbColumn().toUpperCase();
		    		if (fields.containsKey(name)) {
		    			EntityFieldMetaDataVO field = fields.get(name);
		    			field.setDataType(dbattribute.getDataType());
		    			field.setScale(dbattribute.getScale());
		    			field.setPrecision(dbattribute.getPrecision());
		    			attributes.add(field);
		    			fields.remove(name);
		    		}
		    		else if (name.startsWith("INTID_") && fields.containsKey(name.replaceFirst("^INTID_", "STRVALUE_"))) {
		    			name = name.replaceFirst("^INTID_", "STRVALUE_");
		    			// was defined as reference field
		    			EntityFieldMetaDataVO field = fields.get(name);
		    			attributes.add(field);
		    			fields.remove(name);
		    		}
	    			else {
		    			attributes.add(dbattribute);
		    		}
		    	}
	    	}
	    	else {
	    		attributes = MetaDataDelegate.getInstance().getVirtualEntityFields(model.getVirtualentity());
	    	}
	    	EntityAttributeTableModel attributeModel = new EntityAttributeTableModel();
			for(EntityFieldMetaDataVO attribute : attributes) {
				String dbcolumn = attribute.getDbColumn().toUpperCase();
				if (staticfields.contains(dbcolumn)) {
					staticfields.remove(dbcolumn);
	    			continue;
	    		}
				try {
					Attribute attr = NuclosEntityNameStep.wrapEntityMetaFieldVO(attribute);
					if (StringUtils.isNullOrEmpty(attr.getLabel())) {
						attr.setLabel(attribute.getField());
					}
					if (StringUtils.isNullOrEmpty(attr.getDescription())) {
						attr.setDescription(attribute.getField());
					}
					if (attribute.getDbColumn().toUpperCase().startsWith("INTID_")) {
						attr.setDatatyp(DataTyp.getReferenzTyp());
					}
					attributeModel.addAttribute(attr);
				}
				catch(CommonFinderException e1) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, e1);
				}
				catch(CommonPermissionException e1) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, e1);
				}
			}
			for (EntityFieldMetaDataVO attribute : fields.values()) {
				try {
					Attribute attr = NuclosEntityNameStep.wrapEntityMetaFieldVO(attribute);
					attributeModel.addAttribute(attr);
					attributeModel.removeRow(attributeModel.getRowCount() - 1, true);
				}
				catch(CommonFinderException e1) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, e1);
				}
				catch(CommonPermissionException e1) {
					Errors.getInstance().showExceptionDialog(NuclosEntityCommonPropertiesStep.this, e1);
				}
			}
			if (staticfields.size() > 0) {
				String message = getMessage("wizard.step.entitycommonproperties.error.virtual.fields", "Virtual entity is missing required attributes ({0}).", StringUtils.join(", ", staticfields));
				JOptionPane.showMessageDialog(this, message, getMessage("wizard.step.entitycommonproperties.19", "Achtung!"), JOptionPane.OK_OPTION);
		 	    throw new InvalidStateException();
			}
			else {
				// check if select is possible
				try {
					EntityMetaDataVO virtualentity = new EntityMetaDataVO();
					virtualentity.setVirtualentity(model.getVirtualentity());
					virtualentity.setIdFactory(model.getIdFactory());
					virtualentity.setEntity(model.getEntityName());
					virtualentity.setDbEntity(model.getVirtualentity());
					MetaDataDelegate.getInstance().tryVirtualEntitySelect(virtualentity);
				}
				catch (NuclosBusinessException ex) {
					String message = CommonLocaleDelegate.getMessageFromResource(ex.getMessage());
					JOptionPane.showMessageDialog(this, message, getMessage("wizard.step.entitycommonproperties.19", "Achtung!"), JOptionPane.OK_OPTION);
			 	    throw new InvalidStateException();
				}
				model.setAttributeModel(attributeModel);
				model.nextStep();
				model.refreshModelState();
			}
		}
    }


	@Override
	public void fireSaveEvent(Collectable clt, ResourceCollectController clct) {
		if(clt != null) {
			CollectableMasterDataWithDependants mdwd = (CollectableMasterDataWithDependants)clt;
			MasterDataVO vo = mdwd.getMasterDataCVO();
			cbxIcon.addItem(vo);
			cbxIcon.setSelectedItem(vo);
		}
		clct.getFrame().dispose();
		this.requestFocus();
	}

	private class LimitDocument extends PlainDocument {

		private int max;

		public LimitDocument(int max) {
	        this.max = max;
        }

		@Override
		public void insertString(int offs, String str, AttributeSet a)	throws BadLocationException {
			if(str == null)
				return;
			if(getLength() + str.length() < max) {
				super.insertString(offs, str, a);
			}
		}
	}

	boolean notifyIconSelectionChanged = true;
	private boolean isNotifyIconSelectionChange() {
		return notifyIconSelectionChanged;
	}
	private void setNotifyIconSelectionChange(boolean notifyIconSelectionChanged) {
		this.notifyIconSelectionChanged = notifyIconSelectionChanged;
	}

}
