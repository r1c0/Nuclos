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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.model.EntityAttributeSelectTableModel;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.LocaleInfo;
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
public class NuclosEntityOptionStep extends NuclosEntityAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityOptionStep.class);

	JLabel lbName;

	ButtonGroup bGroup;
	JRadioButton rbGatherAttributes;
	JRadioButton rbCopyAttributes;
	JRadioButton rbAssignAttributes;
	JRadioButton rbTemplateAttributes;
	JRadioButton rbImportTable;

	JLabel lbEntity;
	JComboBox cbxEntity;
	JScrollPane scrollFields;
	JTable tblFields;

	JComboBox cbxTable;

	JPanel pnlImport;

	JLabel lbImportServer;
	JTextField tfImportServer;
	JLabel lbImportPort;
	JTextField tfImportPort;
	JLabel lbImportDatabase;
	JComboBox cbxImportDatabase;
	JLabel lbImportUser;
	JTextField tfImportUser;
	JLabel lbImportPassword;
	JTextField tfImportPassword;
	JLabel lbImportSSID;
	JTextField tfImportSSID;

	JLabel lbUrl;
	JTextField tfUrl;

	JButton btConnect;

	JLabel lbImportTables;
	JComboBox cbxImportTables;

	public NuclosEntityOptionStep() {
		// initComponents();
	}

	public NuclosEntityOptionStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityOptionStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}

	protected void initPanel() {
		double size [][] = {{TableLayout.PREFERRED, TableLayout.PREFERRED,TableLayout.PREFERRED,TableLayout.FILL}, {20,20,20,20,20,20,20,20,20,200, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);

		pnlImport = new JPanel();
		pnlImport.setLayout(layout);

		lbImportServer = new JLabel(cld.getMessage(
				"wizard.step.entityoption.1", "Server")+":");
		tfImportServer = new JTextField();
		tfImportServer.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbImportPort = new JLabel(cld.getMessage(
				"wizard.step.entityoption.2", "Port")+":");
		tfImportPort = new JTextField();
		tfImportPort.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbImportDatabase = new JLabel(cld.getMessage(
				"wizard.step.entityoption.3", "Datenbanktyp")+":");
		cbxImportDatabase = new JComboBox();
		cbxImportDatabase.addItem("Oracle");
		cbxImportDatabase.addItem("MS SQL");
		cbxImportDatabase.addItem("Postgres");
		cbxImportDatabase.addItem("Sybase");
		lbImportUser = new JLabel(cld.getMessage(
				"wizard.step.entityoption.4", "User"));
		tfImportUser = new JTextField();
		tfImportUser.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbImportPassword = new JLabel(cld.getMessage(
				"wizard.step.entityoption.5", "Passwort"));
		tfImportPassword = new JTextField();
		tfImportPassword.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbImportSSID = new JLabel(cld.getMessage(
				"wizard.step.entityoption.6", "Datenbank"));
		tfImportSSID = new JTextField();
		lbUrl = new JLabel(cld.getMessage(
				"wizard.step.entityoption.7", "Datenbank URL"));
		tfUrl = new JTextField();
		tfUrl.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		btConnect = new JButton(cld.getMessage(
				"wizard.step.entityoption.8", "Verbindung \u00f6ffnen"));
		btConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String jdbcUrl = getJDBCUrl();
					if(tfUrl.getText() != null && tfUrl.getText().length() > 0)
						jdbcUrl = tfUrl.getText().trim();
					List<String> lstTables = MetaDataDelegate.getInstance().getTablesFromSchema(jdbcUrl, tfImportUser.getText().trim(),
						tfImportPassword.getText().trim(), tfImportUser.getText().trim());
					cbxImportTables.removeAllItems();
					for(String strTable : lstTables) {
						cbxImportTables.addItem(strTable);
					}
					tfUrl.setText(jdbcUrl);
				}
				catch(Exception e1) {
					LOG.info("actionPerformed failed: " + e1, e1);
					JOptionPane.showMessageDialog(NuclosEntityOptionStep.this, "Datenbankverbindung konnte nicht erstellt werden!");
				}
			}
		});


		lbImportTables = new JLabel(cld.getMessage(
				"wizard.step.entityoption.9", "Tabelle"));
		cbxImportTables = new JComboBox();


		cbxImportTables.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				NuclosEntityOptionStep.this.setComplete(true);
			}
		});

		pnlImport.add(lbImportServer, "0,0");
		pnlImport.add(tfImportServer, "1,0");
		pnlImport.add(lbImportPort, "2,0");
		pnlImport.add(tfImportPort, "3,0");
		pnlImport.add(lbImportUser, "0,1");
		pnlImport.add(tfImportUser, "1,1");
		pnlImport.add(lbImportPassword, "2,1");
		pnlImport.add(tfImportPassword, "3,1");
		pnlImport.add(lbImportDatabase, "0,2");
		pnlImport.add(cbxImportDatabase, "1,2");
		pnlImport.add(lbImportSSID, "2,2");
		pnlImport.add(tfImportSSID, "3,2");
		pnlImport.add(btConnect, "0,3");
		pnlImport.add(lbImportTables, "2,3");
		pnlImport.add(cbxImportTables, "3,3");
		pnlImport.add(lbUrl, "0,4");
		pnlImport.add(tfUrl, "1,4,3,4");


		pnlImport.setVisible(false);

	}

	@PostConstruct
	@Override
	protected void initComponents() {

		double size [][] = {{TableLayout.PREFERRED, TableLayout.FILL}, {20,20,20,20,20,20,20, TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		lbName = new JLabel(cld.getMessage(
				"wizard.step.entityoption.10", "Wie m\u00f6chten Sie Attribute erfassen")+" ");
		rbGatherAttributes = new JRadioButton(cld.getMessage(
				"wizard.step.entityoption.11", "Attribute manuell erfassen"));
		rbGatherAttributes.setToolTipText(cld.getMessage(
				"wizard.step.entityoption.tooltip.11", "Attribute manuell erfassen"));
		rbCopyAttributes = new JRadioButton(cld.getMessage(
				"wizard.step.entityoption.12", "Attribute von anderer Entit\u00e4t selektiv \u00fcbernehmen"));
		rbCopyAttributes.setToolTipText(cld.getMessage(
				"wizard.step.entityoption.tooltip.12", "Attribute von anderer Entit\u00e4t selektiv \u00fcbernehmen"));
		rbAssignAttributes = new JRadioButton(cld.getMessage(
				"wizard.step.entityoption.13", "\u00dcbernahme aus bestehender Datenbanktabelle"));
		rbAssignAttributes.setToolTipText(cld.getMessage(
				"wizard.step.entityoption.tooltip.13", "\u00dcbernahme aus bestehender Datenbanktabelle"));
		rbTemplateAttributes = new JRadioButton(cld.getMessage(
				"wizard.step.entityoption.14", "Vorlage \u00fcbernehmen"));
		rbTemplateAttributes.setToolTipText(cld.getMessage(
				"wizard.step.entityoption.tooltip.14", "Vorlage \u00fcbernehmen"));
		rbImportTable = new JRadioButton(cld.getMessage(
				"wizard.step.entityoption.15", "Entit\u00e4t importieren"));
		rbImportTable.setToolTipText(cld.getMessage(
				"wizard.step.entityoption.tooltip.15", "Entit\u00e4t importieren"));
		rbImportTable.setVisible(false);
		lbEntity = new JLabel(cld.getMessage(
				"wizard.step.entityoption.16", "Bitte w\u00e4hlen Sie eine Entit\u00e4t aus")+":");
		lbEntity.setVisible(false);
		cbxEntity = new JComboBox();
		cbxEntity.setVisible(false);
		tblFields = new JTable(new EntityAttributeSelectTableModel());
		tblFields.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblFields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		cbxTable = new JComboBox();
		cbxTable.setVisible(false);

		scrollFields = new JScrollPane(tblFields);
		scrollFields.setVisible(false);

		bGroup = new ButtonGroup();
		bGroup.add(rbGatherAttributes);
		bGroup.add(rbCopyAttributes);
		bGroup.add(rbAssignAttributes);
		bGroup.add(rbTemplateAttributes);
		bGroup.add(rbImportTable);

		rbGatherAttributes.setSelected(true);
		NuclosEntityOptionStep.this.setComplete(true);

		rbGatherAttributes.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						int state = e.getStateChange();
						if (state == ItemEvent.SELECTED) {
							NuclosEntityOptionStep.this.setComplete(true);
							pnlImport.setVisible(false);
							cbxTable.setVisible(false);
							cbxEntity.setVisible(false);
							scrollFields.setVisible(false);
						}
					}
				});

			}
		});

		rbImportTable.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						int state = e.getStateChange();
						if (state == ItemEvent.SELECTED) {
							pnlImport.setVisible(true);
							NuclosEntityOptionStep.this.setComplete(false);
						}
						else {
							pnlImport.setVisible(false);
							NuclosEntityOptionStep.this.setComplete(true);
						}
					}
				});

			}
		});

		rbCopyAttributes.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						int state = e.getStateChange();
						if (state == ItemEvent.SELECTED) {
							NuclosEntityOptionStep.this.setComplete(true);
							tblFields.setModel(new EntityAttributeSelectTableModel());
							lbEntity.setVisible(true);
							cbxEntity.setSelectedIndex(0);
							cbxEntity.setVisible(true);
							cbxTable.setVisible(false);
							scrollFields.setVisible(true);
						}
						else {
							lbEntity.setVisible(false);
							cbxEntity.setVisible(false);
							cbxTable.setVisible(false);
							scrollFields.setVisible(false);
						}

					}
				});

			}
		});

		rbAssignAttributes.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						int state = e.getStateChange();
						if (state == ItemEvent.SELECTED) {
							NuclosEntityOptionStep.this.setComplete(true);
							tblFields.setModel(new EntityAttributeSelectTableModel());
							cbxTable.setSelectedIndex(0);
							cbxTable.setVisible(true);
							cbxEntity.setVisible(false);
							scrollFields.setVisible(true);
						}
					}
				});

			}
		});

		rbTemplateAttributes.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						int state = e.getStateChange();
						if (state == ItemEvent.SELECTED) {
							lbEntity.setVisible(true);
							cbxEntity.setSelectedIndex(0);
							cbxEntity.setVisible(true);
							cbxTable.setVisible(false);
							pnlImport.setVisible(false);
							scrollFields.setVisible(false);
						}
						else {
							lbEntity.setVisible(false);
							cbxEntity.setVisible(false);
							cbxTable.setVisible(false);
						}
					}
				});

			}
		});

		cbxEntity.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final Object obj = e.getItem();
					try {
						if(obj instanceof EntityMetaDataVO) {
							final EntityAttributeSelectTableModel model = new EntityAttributeSelectTableModel();
							EntityMetaDataVO vo = (EntityMetaDataVO)obj;
							Collection<EntityFieldMetaDataVO> fields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(vo.getEntity()).values();

							ArrayList<EntityFieldMetaDataVO> lstSorted = CollectionUtils.sorted(fields, new Comparator<EntityFieldMetaDataVO>() {

								@Override
								public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
									return o1.getField().toLowerCase().compareTo(o2.getField().toLowerCase());
								}

							});
							for(EntityFieldMetaDataVO field: lstSorted) {
								if(NuclosWizardUtils.isSystemField(field))
									continue;
								Attribute attr = new Attribute();
								attr.setLabel(cld.getResource(
										field.getLocaleResourceIdForLabel(), field.getFallbacklabel()));
								attr.setDescription(cld.getResource(
										field.getLocaleResourceIdForDescription(), ""));
								attr.setDistinct(field.isUnique());
								attr.setMandatory(!field.isNullable());
								attr.setLogBook(field.isLogBookTracking());
								attr.setInternalName(field.getField());
								attr.setDbName(field.getDbColumn());
								attr.setField(field.getField());
								attr.setOldInternalName(field.getField());
								attr.setValueListProvider(field.isSearchable());
								attr.setModifiable(field.isModifiable());
								attr.setDefaultValue(field.getDefaultValue());
								attr.setCalcFunction(field.getCalcFunction());
								attr.setOutputFormat(field.getFormatOutput());
								attr.setInputValidation(field.getFormatInput());
								attr.setIndexed(Boolean.TRUE.equals(field.isIndexed()));
								attr.setCalculationScript(field.getCalculationScript());
								String sForeignEntity = field.getForeignEntity();
								if(sForeignEntity != null) {
									attr.setMetaVO(MetaDataClientProvider.getInstance().getEntity(sForeignEntity));
									attr.setOnDeleteCascade(field.isOnDeleteCascade());
									attr.setField(field.getForeignEntityField());
									attr.setDatatyp(DataTyp.getReferenzTyp());
									if(!Modules.getInstance().isModuleEntity(sForeignEntity) && field.getForeignEntityField() != null) {
										String sForeignField = field.getForeignEntityField();
										if(sForeignField.indexOf("${") >= 0) {
											attr.setDatatyp(DataTyp.getReferenzTyp());
										}
										else {
											EntityFieldMetaDataVO voField = MetaDataClientProvider.getInstance().getEntityField(sForeignEntity, field.getForeignEntityField());

											attr.getDatatyp().setJavaType(voField.getDataType());
											if(voField.getPrecision() != null)
												attr.getDatatyp().setPrecision(voField.getPrecision());
											if(voField.getScale() != null)
												attr.getDatatyp().setScale(voField.getScale());
											if(voField.getFormatOutput() != null)
												attr.getDatatyp().setOutputFormat(voField.getFormatOutput());
										}
									}
								}
								else {
									attr.setDatatyp(NuclosWizardUtils.getDataTyp(field.getDataType(),
										field.getScale(), field.getPrecision(), field.getFormatInput(),
										field.getFormatOutput()));
								}
								model.addAttribute(attr);
							}
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									tblFields.setModel(model);
									TableUtils.setOptimalColumnWidths(tblFields);
								}
							});
						}
						else if(obj instanceof String) {
							EntityAttributeSelectTableModel model = new EntityAttributeSelectTableModel();
							tblFields.setModel(model);
						}
					}
					catch(Exception e1) {
						LOG.info("itemStateChanged failed: " + e1, e1);
					}
				}
			}
		});

		cbxTable.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final Object obj = e.getItem();
					if(obj instanceof String) {
						String strTable = (String)obj;
						final EntityAttributeSelectTableModel model = new EntityAttributeSelectTableModel();
						if(strTable.length() == 0) {
							tblFields.setModel(model);
							return;
						}

						Map<String, MasterDataVO> mp = MetaDataDelegate.getInstance().getColumnsFromTable(strTable);
						for(String sCol : mp.keySet()) {
							if(NuclosWizardUtils.isSystemField(sCol))
								continue;
							Attribute attr = new Attribute();
							attr.setLabel(sCol);
							attr.setDbName(sCol);
							attr.setDescription(sCol);
							attr.setInternalName(sCol);
							MasterDataVO vo = mp.get(sCol);

							DataTyp typ = new DataTyp();
							typ.setPrecision((Integer)vo.getField("precision"));
							typ.setScale((Integer)vo.getField("scale"));
							typ.setName((String)vo.getField("name"));
							typ.setJavaType((String)vo.getField("javatyp"));

							attr.setDatatyp(typ);
							setTranslationForAttribute(attr, NuclosEntityOptionStep.this.model.getAttributeModel());

							model.addAttribute(attr);
						}
						SwingUtilities.invokeLater(new Runnable() {

							@Override
							public void run() {
								tblFields.setModel(model);
							}
						});


					}
				}
			}
		});

		initPanel();

		this.add(lbName, "0,0");
		this.add(rbGatherAttributes, "1,0");
		this.add(rbCopyAttributes, "1,1");
		this.add(rbTemplateAttributes, "1,2");
		this.add(rbAssignAttributes, "1,3");
		this.add(rbImportTable, "1,4");
		this.add(lbEntity, "0,5");
		this.add(cbxEntity, "0,6");
		this.add(cbxTable, "0,6");
		this.add(pnlImport, "0,7, 1,7");
		this.add(scrollFields, "0,7 ,1,7");

		fillEntityCombobox();
		fillTableCombobox();

	}

	@Override
	public void applyState() throws InvalidStateException {
		super.applyState();

		if(rbCopyAttributes.isSelected() || rbAssignAttributes.isSelected()) {
			EntityAttributeSelectTableModel model =  (EntityAttributeSelectTableModel)tblFields.getModel();
			List<Attribute> lstAttribute = model.getAttributes();
			EntityAttributeTableModel modelAttribute = new EntityAttributeTableModel();
			for(Attribute attr : lstAttribute) {
				if(attr.isForResume())
					modelAttribute.addAttribute(attr);
			}
			for(Attribute attr : modelAttribute.getAttributes()) {
				if(!existAttribute(attr)) {
					if(attr.getInternalId() == null)
						attr.setInternalId(getHighestInternalId());
					this.model.getAttributeModel().addAttribute(attr);
					setTranslationForAttribute(attr, this.model.getAttributeModel());
				}
			}
		}
		else if(rbTemplateAttributes.isSelected()) {
			EntityAttributeSelectTableModel model =  (EntityAttributeSelectTableModel)tblFields.getModel();
			List<Attribute> lstAttribute = model.getAttributes();
			EntityAttributeTableModel modelAttribute = new EntityAttributeTableModel();
			for(Attribute attr : this.model.getAttributeModel().getAttributes()) {
				modelAttribute.addAttribute(attr);
				modelAttribute.addTranslation(attr, this.model.getAttributeModel().getTranslation().get(attr));
			}

			for(Attribute attr : lstAttribute) {
				if(!existAttribute(attr)) {
					attr.setInternalId(getHighestInternalId());
					modelAttribute.addAttribute(attr);
					setTranslationForAttribute(attr, modelAttribute);
				}
			}
			this.model.setAttributeModel(modelAttribute);
		}
		else if(rbImportTable.isSelected()) {
			String jdbc = getJDBCUrl();

			this.model.setJdbcUrl(jdbc);
			this.model.setExternalUser(tfImportUser.getText().trim());
			this.model.setExternalPassword(tfImportPassword.getText().trim());
			this.model.setExternalTable((String)cbxImportTables.getSelectedItem());
			this.model.setImportTable(true);
			List<MasterDataVO> lstVO = MetaDataDelegate.getInstance().transformTable(jdbc, model.getExternalUser(), model.getExternalPassword(),
				model.getExternalUser().trim().toUpperCase(), model.getExternalTable());
			EntityAttributeTableModel modelAttribute = new EntityAttributeTableModel();
			for(MasterDataVO vo: lstVO) {
				Attribute attr = new Attribute();
				attr.setField((String)vo.getField("name"));
				attr.setInternalName((String)vo.getField("name"));
				attr.setDbName((String)vo.getField("dbfield"));
				attr.setDescription((String)vo.getField("description"));
				DataTyp datatyp = new DataTyp();
				datatyp.setJavaType((String)vo.getField("datatype"));
				datatyp.setPrecision((Integer)vo.getField("dataprecision"));
				datatyp.setScale((Integer)vo.getField("datascale"));
				attr.setDatatyp(datatyp);
				attr.setLogBook((Boolean)vo.getField("logbook"));
				attr.setDistinct((Boolean)vo.getField("unique"));
				attr.setMandatory((Boolean)vo.getField("nullable"));
				attr.setLabel((String)vo.getField("label"));
				modelAttribute.addAttribute(attr);
			}
			this.model.setAttributeModel(modelAttribute);
		}
	}

	private Long getHighestInternalId() {
		Long l = new Long(0);
		for(Attribute attr : model.getAttributeModel().getAttributes()) {
			if(attr.getInternalId() != null) {
				if(l.longValue() < attr.getInternalId()) {
					l = attr.getInternalId();
					l++;
				}
			}
		}
		return l;
	}

	private boolean existAttribute(Attribute attr) {
	    boolean exist = false;
	    if(this.model.getAttributeModel() != null) {
	    	for(Attribute attrexist : this.model.getAttributeModel().getAttributes()) {
	    		if(attrexist.getInternalName().equals(attr.getInternalName())) {
	    			exist = true;
	    		}
	    	}
	    }
	    return exist;
    }

	private void setTranslationForAttribute(Attribute attr, EntityAttributeTableModel model) {
	    List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();

	    for(LocaleInfo info : LocaleDelegate.getInstance().getAllLocales(false)){
	    	Map<String, String> mpValues = new HashMap<String, String>();
	    	mpValues.put(TranslationVO.labelsField[0], attr.getLabel());
	    	mpValues.put(TranslationVO.labelsField[1], attr.getDescription());
	    	TranslationVO voTranslation = new TranslationVO(info.localeId, info.title, info.language, mpValues);
	    	lstTranslation.add(voTranslation);
	    }

	   model.addTranslation(attr, lstTranslation);
    }

	private void fillTableCombobox() {
		List<String> lstTables = MetaDataDelegate.getInstance().getDBTables();
		cbxTable.addItem("");
		for(String str : lstTables) {
			cbxTable.addItem(str);
		}
	}

	private void fillEntityCombobox() {
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
			if(!vo.getEntity().startsWith("general"))
				cbxEntity.addItem(vo);
		}
	}

	protected String getJDBCUrl() {
		String sDB = (String)cbxImportDatabase.getSelectedItem();
		StringBuffer jdbcUrl = new StringBuffer();
		if(sDB.equals("Oracle")) {
			jdbcUrl.append("jdbc:oracle:thin:@");
			jdbcUrl.append(tfImportServer.getText().trim());
			jdbcUrl.append(":");
			jdbcUrl.append(tfImportPort.getText().trim());
			jdbcUrl.append(":");
			jdbcUrl.append(tfImportSSID.getText().trim());
		}
		else if(sDB.equals("MS SQL")) {
			jdbcUrl.append("jdbc:sqlserver://");
			jdbcUrl.append(tfImportServer.getText().trim());
			jdbcUrl.append(":");
			jdbcUrl.append(tfImportPort.getText().trim());
			jdbcUrl.append(";DatabaseName=");
			jdbcUrl.append(tfImportSSID.getText().trim());
		}

		return jdbcUrl.toString();
	}

}
