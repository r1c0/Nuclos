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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.DataTyp;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.model.ValueList;
import org.nuclos.client.wizard.util.DefaultValue;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;
import org.springframework.beans.factory.annotation.Autowired;
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
public class NuclosEntityNameStep extends NuclosEntityAbstractStep {

	private static final Logger LOG = Logger.getLogger(NuclosEntityNameStep.class);

	private JLabel lbName;
	private JTextField tfName;

	private JLabel lbChoice;
	private JComboBox cmbEntity;

	private JLabel lbInfo;

	private String sEntityName;

	private JButton btnRemove;

	private Collection<EntityMetaDataVO> colMasterdata;
	private EntityMetaDataVO toEdit;
	
	// Spring injection
	
	private ConsoleFacadeRemote consoleFacadeRemote;
	
	// end of Spring injection

	public NuclosEntityNameStep() {
		// initComponents();
	}

	public NuclosEntityNameStep(String name, String summary) {
		super(name, summary);
		// initComponents();
	}

	public NuclosEntityNameStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		// initComponents();
	}

	public void setEntityToEdit(EntityMetaDataVO vo) {
		this.toEdit = vo;
	}
	
	@Autowired
	final void setConsoleFacadeRemote(ConsoleFacadeRemote consoleFacadeRemote) {
		this.consoleFacadeRemote = consoleFacadeRemote;
	}

	@PostConstruct
	@Override
	protected void initComponents() {

		colMasterdata =  MetaDataClientProvider.getInstance().getAllEntities();

		sEntityName = new String();

		double size [][] = {{TableLayout.PREFERRED, 200, TableLayout.FILL}, {20,20,20,20,20,TableLayout.FILL}};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);
		lbName = new JLabel(localeDelegate.getMessage(
				"wizard.step.entityname.1", "Bitte geben Sie den Namen der neuen Entit\u00e4t ein")+": ");
		tfName = new JTextField();
		tfName.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entityname.tooltip.1", "Namen der neuen Entit\u00e4t"));
		tfName.addFocusListener(NuclosWizardUtils.createWizardFocusAdapter());
		lbChoice = new JLabel(localeDelegate.getMessage(
				"wizard.step.entityname.2", "oder w\u00e4hlen Sie eine Entit\u00e4t die Sie ver\u00e4ndern m\u00f6chten")+": ");
		cmbEntity = new JComboBox();
		cmbEntity.setToolTipText(localeDelegate.getMessage(
				"wizard.step.entityname.tooltip.2", "Namen der Entit\u00e4t die Sie ver\u00e4ndern m\u00f6chten"));
		this.fillEntityCombobox();

		lbInfo = new JLabel();
		lbInfo.setVisible(false);
		lbInfo.setForeground(Color.RED);

		btnRemove = new JButton(localeDelegate.getMessage(
				"wizard.step.entityname.8", "Entit\u00e4t entfernen"));
		btnRemove.setVisible(false);

		this.add(lbName, "0,0");
		this.add(tfName, "1,0");
		this.add(lbChoice, "0,1");
		this.add(cmbEntity, "1,1");
		this.add(lbInfo, "0,2,1,2");
		this.add(btnRemove,"0,3");

		tfName.getDocument().addDocumentListener(new DocumentListener() {

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
				try {
					for(EntityMetaDataVO metaVO : colMasterdata) {
						if(e.getDocument().getLength() == 0) {
							lbInfo.setVisible(false);
							break;
						}
						if(e.getDocument().getLength() > 250) {
							lbInfo.setText(localeDelegate.getMessage(
									"wizard.step.entityname.12", "Der Name ist zu lang. Bitte k\u00fcrzen!"));
							lbInfo.setVisible(true);
							NuclosEntityNameStep.this.setComplete(false);
							return;
						}

						if(e.getDocument().getText(0, size).equals(metaVO.getEntity())
							|| e.getDocument().getText(0, size).equals(localeDelegate.getResource(
									metaVO.getLocaleResourceIdForLabel(), "") )) {
							NuclosEntityNameStep.this.setComplete(false);
							lbInfo.setText(localeDelegate.getMessage(
									"wizard.step.entityname.4", "Entit\u00e4t ist schon vorhanden"));
							lbInfo.setVisible(true);
							return;
						}
						else {
							lbInfo.setVisible(false);
						}
						if(e.getDocument().getLength() > 25) {
							lbInfo.setText(localeDelegate.getMessage(
									"wizard.step.entityname.13", "Der Tabellenname wird für interne Zwecke gekürzt!"));
							lbInfo.setVisible(true);
						}
						else {
							lbInfo.setVisible(false);
						}
					}
					if(size > 0) {
						if(cmbEntity.getSelectedIndex() > 0) {
							NuclosEntityNameStep.this.model.resetModel();
						}
						NuclosEntityNameStep.this.setComplete(true);
						cmbEntity.setSelectedIndex(0);
						cmbEntity.setEnabled(false);
					}
					else  {
						//NuclosEntityNameStep.this.model.resetModel();
						NuclosEntityNameStep.this.setComplete(false);
						model.setTableOrViewName(null);
						cmbEntity.setEnabled(true);
					}

					NuclosEntityNameStep.this.model.setEntityName(e.getDocument().getText(0, e.getDocument().getLength()));
					if(!NuclosEntityNameStep.this.model.isEditMode()) {
						NuclosEntityNameStep.this.model.setLabelSingular(e.getDocument().getText(0, e.getDocument().getLength()));
					}
				}
				catch (BadLocationException ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityNameStep.this, ex);
				}
			}
		});

		cmbEntity.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(final ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final Object obj = e.getItem();
					if(obj instanceof EntityWrapper) {
						try {
							EntityMetaDataVO vo = ((EntityWrapper)obj).getWrappedEntity();
							NuclosEntityNameStep.this.model.resetModel();
							if (!vo.isVirtual()) {
								NuclosEntityNameStep.this.model.setHasRows(hasEntityRows(vo));
							}
							else {
								NuclosEntityNameStep.this.model.setHasRows(true);
							}
							NuclosEntityNameStep.this.model.setEditMode(true);
							NuclosEntityNameStep.this.model.setEntityName(vo.getEntity());
							NuclosEntityNameStep.this.model.setLabelSingular(localeDelegate.getResource(
									vo.getLocaleResourceIdForLabel(), ""));
							NuclosEntityNameStep.this.model.setEditable(vo.isEditable());
							NuclosEntityNameStep.this.model.setSearchable(vo.isSearchable());
							NuclosEntityNameStep.this.model.setLogbook(vo.isLogBookTracking());
							NuclosEntityNameStep.this.model.setMenuPath(localeDelegate.getResource(
									vo.getLocaleResourceIdForMenuPath(), ""));
							NuclosEntityNameStep.this.model.setCachable(vo.isCacheable());
							NuclosEntityNameStep.this.model.setImExport(vo.isImportExport());
							NuclosEntityNameStep.this.model.setShowRelation(vo.isTreeRelation());
							NuclosEntityNameStep.this.model.setShowGroups(vo.isTreeGroup());
							NuclosEntityNameStep.this.model.setStateModel(vo.isStateModel());
							NuclosEntityNameStep.this.model.setTableOrViewName(vo.getDbEntity());
							NuclosEntityNameStep.this.model.setNodeLabel(localeDelegate.getTextForStaticLabel(
									vo.getLocaleResourceIdForTreeView()));
							NuclosEntityNameStep.this.model.setNodeTooltip(localeDelegate.getTextForStaticLabel(
									vo.getLocaleResourceIdForTreeViewDescription()));
							NuclosEntityNameStep.this.model.setMultiEditEquation(vo.getFieldsForEquality());
							NuclosEntityNameStep.this.model.setLabelSingularResource(vo.getLocaleResourceIdForLabel());
							NuclosEntityNameStep.this.model.setMenuPathResource(vo.getLocaleResourceIdForMenuPath());
							NuclosEntityNameStep.this.model.setNodeLabelResource(vo.getLocaleResourceIdForTreeView());
							NuclosEntityNameStep.this.model.setNodeTooltipResource(vo.getLocaleResourceIdForTreeViewDescription());
							NuclosEntityNameStep.this.model.setDocumentPath(vo.getDocumentPath());
							NuclosEntityNameStep.this.model.setReportFilename(vo.getReportFilename());
							NuclosEntityNameStep.this.model.setVirtualentity(vo.getVirtualentity());
							NuclosEntityNameStep.this.model.setIdFactory(vo.getIdFactory());
							NuclosEntityNameStep.this.model.setRowColorScript(vo.getRowColorScript());

							if(vo.getResourceId() != null) {
								NuclosEntityNameStep.this.model.setResourceName(
										ResourceCache.getInstance().getResourceById(vo.getResourceId()).getName());
							}

							NuclosEntityNameStep.this.model.setSystemIdPrefix(vo.getSystemIdPrefix());


							NuclosEntityNameStep.this.model.setAccelerator(vo.getAccelerator());
							NuclosEntityNameStep.this.model.setModifier(vo.getAcceleratorModifier());

						   NuclosEntityNameStep.this.model.setResourceId(vo.getResourceId());
						   NuclosEntityNameStep.this.model.setNuclosResourceName(vo.getNuclosResource());

						   loadUserRights(vo);
						   loadTreeView();
						   if (model.isStateModel()) {
							   loadProcesses(vo.getId());
						   }
						   loadEntityMenus(vo.getId());

							EntityAttributeTableModel attributeModel = new EntityAttributeTableModel();
							Collection<EntityFieldMetaDataVO> lstFields = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(vo.getEntity()).values();
							for(EntityFieldMetaDataVO fieldVO : CollectionUtils.sorted(lstFields,
										new Comparator<EntityFieldMetaDataVO>() {
									@Override
									public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
										Integer order1 = (o1.getOrder()==null)?0:o1.getOrder();
										Integer order2 = (o2.getOrder()==null)?0:o2.getOrder();
										return order1.compareTo(order2);
									}
								})) {
								if(fieldVO.getEntityId() == -100)
									continue;
								Attribute attr = new Attribute();

								attr = wrapEntityMetaFieldVO(fieldVO);

								attributeModel.addAttribute(attr);
							}
							NuclosEntityNameStep.this.model.setAttributeModel(attributeModel);
							NuclosEntityNameStep.this.setComplete(true);
						}
						catch(CommonFinderException e1) {
							Errors.getInstance().showExceptionDialog(NuclosEntityNameStep.this, e1);
						}
						catch(CommonPermissionException e1) {
							Errors.getInstance().showExceptionDialog(NuclosEntityNameStep.this, e1);
						}
						btnRemove.setVisible(true);
					}
					else if(obj instanceof String) {
						NuclosEntityNameStep.this.model.setEditMode(false);
						NuclosEntityNameStep.this.model.resetModel();
						tfName.setEnabled(true);
						btnRemove.setVisible(false);
						NuclosEntityNameStep.this.setComplete(false);
					}
				}
			}
		});
		ListenerUtil.registerActionListener(btnRemove, this, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					StringBuffer sbMessage = new StringBuffer();
					if(!dropEntityAllowed(((EntityWrapper)cmbEntity.getSelectedItem()).getWrappedEntity(), sbMessage)) {
						JOptionPane.showMessageDialog(NuclosEntityNameStep.this, sbMessage,
							localeDelegate.getMessage("wizard.step.inputattribute.12", "Entfernen nicht möglich!"), 
							JOptionPane.OK_OPTION);
						return;
					}

					boolean blnImportStructure = MetaDataDelegate.getInstance().hasEntityImportStructure(((EntityWrapper)cmbEntity.getSelectedItem()).getWrappedEntity().getId());
					if(blnImportStructure) {
						String sMessage = localeDelegate.getMessage(
								"wizard.step.entityname.14",
								"Diese Entität besitzt Strukturdefinitionen für Objektimporte. Diese wird gelöscht! Sie können den Vorgang abbrechen!");
						int abort = JOptionPane.showConfirmDialog(NuclosEntityNameStep.this, sMessage,
							localeDelegate.getMessage("wizard.step.entityname.16", "Achtung!"), 
							JOptionPane.OK_CANCEL_OPTION);
						if(abort != JOptionPane.OK_OPTION)
							return;
					}

					boolean blnWorkflow = MetaDataDelegate.getInstance().hasEntityWorkflow(((EntityWrapper)cmbEntity.getSelectedItem()).getWrappedEntity().getId());
					if(blnWorkflow) {
						String sMessage = localeDelegate.getMessage(
								"wizard.step.entityname.15",
								"Diese Entität ist in einem Arbeitsschritt integriert! Dieser wird gelöscht!");
						int abort = JOptionPane.showConfirmDialog(NuclosEntityNameStep.this, sMessage,
							localeDelegate.getMessage("wizard.step.entityname.16", "Achtung!"), JOptionPane.OK_CANCEL_OPTION);
						if(abort != JOptionPane.OK_OPTION)
							return;
					}

					final String sMessage = localeDelegate.getMessage(
							"wizard.step.entityname.9", 
							"Sind Sie sicher, dass Sie die Entit\u00e4t l\u00f6schen m\u00f6chten?");
					final String sTitle = localeDelegate.getMessage("wizard.step.entityname.10", "L\u00f6schen");

					int dropEntity = JOptionPane.showConfirmDialog(NuclosEntityNameStep.this, sMessage, sTitle, JOptionPane.YES_NO_OPTION);
					switch(dropEntity) {
					case JOptionPane.YES_OPTION:
						boolean bDropLayout = false;
						if(MetaDataDelegate.getInstance().hasEntityLayout(((EntityWrapper)cmbEntity.getSelectedItem()).getWrappedEntity().getId())) {
							int dropLayout = JOptionPane.showConfirmDialog(NuclosEntityNameStep.this,
									localeDelegate.getMessage("wizard.step.entityname.11", "Soll das Layout der Entität gelöscht werden?"), 
									sTitle, JOptionPane.YES_NO_OPTION);
							bDropLayout = (dropLayout == JOptionPane.YES_OPTION);
						}

						MetaDataDelegate.getInstance().removeEntity(((EntityWrapper)cmbEntity.getSelectedItem()).getWrappedEntity(), bDropLayout);
						NuclosWizardUtils.flushCaches();
						consoleFacadeRemote.invalidateAllCaches();
						NuclosEntityNameStep.this.model.cancelWizard();
						break;
					case JOptionPane.NO_OPTION:
					default:
						return;
					}

				}
				catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(NuclosEntityNameStep.this, ex);
					return;
				}
				finally {
					MasterDataDelegate.getInstance().invalidateLayoutCache();
				}
			}
		});

	}

	private static void loadValueList(Attribute attr) {
		if(attr.getMetaVO() == null)
			return;

		String sEntity = attr.getMetaVO().getEntity();

		Collection<MasterDataVO> colMasterdata = MasterDataDelegate.getInstance().getMasterData(sEntity);

		attr.setValueListName(sEntity);
		attr.setValueListNew(false);

		if(colMasterdata.size() == 0) {
			attr.setValueList(new ArrayList<ValueList>());
			return;
		}

		for(MasterDataVO vo : colMasterdata) {
			ValueList valueList = new ValueList();
			valueList.setId(vo.getIntId().longValue());
			valueList.setLabel((String)vo.getField("value"));
			valueList.setDescription((String)vo.getField("description"));
			valueList.setMnemonic((String)vo.getField("mnemonic"));
			valueList.setValidFrom((Date)vo.getField("validFrom"));
			valueList.setValidUntil((Date)vo.getField("validUntil"));
			valueList.setVersionId(vo.getVersion());
			attr.getValueList().add(valueList);
		}
	}

	private boolean dropEntityAllowed(EntityMetaDataVO voEntity, StringBuffer sb) {
		boolean blnAllowed = true;
		StringBuffer sbEntities = new StringBuffer();

		for(EntityMetaDataVO vo : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(vo.getEntity().equals(voEntity.getEntity()))
				continue;
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance()
					.getAllEntityFieldsByEntity(vo.getEntity()).values()) {
				if(voField.getForeignEntity() != null 
						&& voField.getForeignEntity().equals(voEntity.getEntity())) {
					sbEntities.append(localeDelegate.getTextFallback(
							vo.getLocaleResourceIdForLabel(), vo.getEntity()));
					sbEntities.append(" ");
					blnAllowed = false;
				}
			}
		}
		if(!blnAllowed) {
			sb.append(localeDelegate.getMessage("wizard.step.entityname.7", 
					"Die Entität wird referenziert von " + sbEntities.substring(0, sbEntities.length()-1) + ". Bitte entfernen Sie die Referenz vorher dort!", 
					sbEntities));
		}

		return blnAllowed;
	}

	protected static void setAttributeGroup(Attribute attr, EntityFieldMetaDataVO fieldVO) {
		try {
			for(MasterDataVO vo : MasterDataCache.getInstance().get(NuclosEntity.ENTITYFIELDGROUP.getEntityName())) {
				if(fieldVO.getFieldGroupId()  != null) {
					if(vo.getIntId().intValue() == fieldVO.getFieldGroupId().intValue()) {
						attr.setAttributeGroup((String)vo.getField("name"));
						break;
					}
				}
			}
		}
		catch(CommonFinderException e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void close() {
		lbName = null;
		tfName = null;

		lbChoice = null;
		cmbEntity = null;

		lbInfo = null;

		sEntityName = null;

		btnRemove = null;

		/*
		if (colMasterdata != null) {
			// unmodifiable 
			colMasterdata.clear();
		}
		 */
		colMasterdata = null;
		
		toEdit = null;

		super.close();
	}

	@Override
	public void applyState() throws InvalidStateException {
		if(this.model.getUserRights().size() < 1) {
			loadUserRights(null);
		}
		model.getParentFrame().setTitle(localeDelegate.getMessage(
				"wizard.step.entityname.5", "Nucleus Entit\u00e4tenwizard") 
				+ " " + model.getEntityName());
		
		super.applyState();
	}

	@Override
	public void prepare() {
		super.prepare();

		DocumentListener li[] = tfName.getListeners(DocumentListener.class);
		for(DocumentListener l : li)
			tfName.getDocument().removeDocumentListener(l);

		if(toEdit != null) {
			cmbEntity.setSelectedItem(new EntityWrapper(toEdit));
			return;
		}
		if(model.getEntityName() != null && !model.isEditMode()) {
			tfName.setText(model.getEntityName());
		}
		if(model.isEditMode()) {
			tfName.setEnabled(false);
		}

		for(DocumentListener l : li)
			tfName.getDocument().addDocumentListener(l);
	}

	private void loadUserRights(EntityMetaDataVO voEntity) {
		Collection<MasterDataVO> colRole = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLE.getEntityName());
		Collection<MasterDataVO> colRoleMasterdata = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLEMASTERDATA.getEntityName());
		if(this.model.isStateModel()) {
			colRoleMasterdata = MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ROLEMODULE.getEntityName());
		}
		this.model.getUserRights().clear();
		if(voEntity == null) {
			for(MasterDataVO vo : colRole) {
				String role = (String)vo.getField("name");
				MasterDataVO voAdd = null;
				voAdd = NuclosWizardUtils.setFieldsForUserRight(vo, role, voAdd, this.model);
				this.model.getUserRights().add(voAdd);
			}
		}
		else {
			for(MasterDataVO vo : colRole) {
				String role = (String)vo.getField("name");
				MasterDataVO voAdd = null;
				for(MasterDataVO voRole : colRoleMasterdata) {
					if(this.model.isStateModel()) {
						if(voRole.getField("moduleId").equals(new Integer(voEntity.getId().intValue()))&& role.equals(voRole.getField("role"))) {
							voAdd = voRole;
							break;
						}
					}
					else {
						String sEntity = (String)voRole.getField("entity");
						try {
							Long lEntity = MetaDataClientProvider.getInstance().getEntity(sEntity).getId();
							if(lEntity.equals(voEntity.getId()) && role.equals(voRole.getField("role"))) {
								voAdd = voRole;
								break;
							}
						}
						catch (CommonFatalException ex) {
							// entity in colRoleMasterdata does not exist anymore
							LOG.warn(ex.getMessage());
							continue;
						}
					}
				}
				voAdd = NuclosWizardUtils.setFieldsForUserRight(vo, role, voAdd, this.model);
				this.model.getUserRights().add(voAdd);
			}
		}
	}

	/**
	 * Load subnodes from t_md_entity_subnodes table.
	 */
	private void loadTreeView() {
		final MasterDataDelegate mdd = MasterDataDelegate.getInstance();
		int so = 0;
		final NavigableSet<EntityTreeViewVO> views = new TreeSet<EntityTreeViewVO>();
		for(MasterDataVO vo : mdd.getMasterData(NuclosEntity.ENTITYSUBNODES.getEntityName())) {
			final Long lEntity = IdUtils.toLongId(vo.getField(EntityTreeViewVO.ENTITY_FIELD + "Id"));
			if (MetaDataDelegate.getInstance().getEntityById(lEntity).getEntity().equals(model.getEntityName())) {
				final String entity = (String)vo.getField(EntityTreeViewVO.SUBFORM_ENTITY_FIELD);
				final String field = (String)vo.getField(EntityTreeViewVO.SUBFORM2ENTITY_REF_FIELD);
				final String foldername = (String)vo.getField(EntityTreeViewVO.FOLDERNAME_FIELD);
				final Boolean active = (Boolean) vo.getField(EntityTreeViewVO.ACTIVE_FIELD);

				// active and sortOrder are new fields.
				// Hence we need a way to default them. (Thomas Pasch)
				Integer sortOrder = (Integer) vo.getField(EntityTreeViewVO.SORTORDER_FIELD);
				if (sortOrder == null) {
					sortOrder = Integer.valueOf(++so);
				}

				views.add(new EntityTreeViewVO(IdUtils.toLongId(vo.getId()), lEntity, entity, field, foldername, active, sortOrder));
				so = sortOrder.intValue();
			}
		}
		model.getTreeView().addAll(views);
	}

	private void loadProcesses(Long entityId) {
		Collection<EntityObjectVO> processes = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.PROCESS.getEntityName(), "module", entityId);
		model.setProcesses(processes);
	}

	private void loadEntityMenus(Long entityId) {
		Collection<EntityObjectVO> entityMenus = MasterDataDelegate.getInstance().getDependantMasterData(NuclosEntity.ENTITYMENU.getEntityName(), "entity", entityId);
		for (EntityObjectVO menu : entityMenus) {
			for (LocaleInfo li : LocaleDelegate.getInstance().getAllLocales(false)) {
				menu.getFields().put("menupath_" + li.getTag(), localeDelegate.getResourceById(
						li, menu.getField("menupath", String.class)));
			}
		}
		model.setEntityMenus(entityMenus);
	}

	private boolean hasEntityRows(EntityMetaDataVO voEntity) {
		return MetaDataDelegate.getInstance().hasEntityRows(voEntity);
	}

	private void fillEntityCombobox() {
		List<EntityMetaDataVO> lstMasterdata = new ArrayList<EntityMetaDataVO>(colMasterdata);
		Collections.sort(lstMasterdata, new Comparator<EntityMetaDataVO>() {

			@Override
			public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
				String s1 = localeDelegate.getTextFallback(o1.getLocaleResourceIdForLabel(), o1.toString());
				String s2 = localeDelegate.getTextFallback(o2.getLocaleResourceIdForLabel(), o2.toString());
				return s1.toUpperCase().compareTo(s2.toUpperCase());
			}

		});
		cmbEntity.addItem("");

		for(EntityMetaDataVO vo : lstMasterdata) {
			if(vo.getEntity().startsWith("nuclos_")|| vo.getEntity().equals("entityfields")
				|| vo.isDynamic() || vo.isFieldValueEntity())
				continue;
			cmbEntity.addItem(new EntityWrapper(vo));
		}
	}

	public static Attribute wrapEntityMetaFieldVO(EntityFieldMetaDataVO fieldVO) throws CommonFinderException,
        CommonPermissionException {
		
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		Attribute attr = new Attribute();
        attr.setId(fieldVO.getId());
        attr.setDistinct(fieldVO.isUnique());
        attr.setLogBook(fieldVO.isLogBookTracking());
        attr.setMandatory(!fieldVO.isNullable());
        attr.setIndexed(Boolean.TRUE.equals(fieldVO.isIndexed()));
        attr.setLabel(localeDelegate.getResource(
        		fieldVO.getLocaleResourceIdForLabel(), fieldVO.getFallbacklabel()));
        attr.setDescription(localeDelegate.getResource(fieldVO.getLocaleResourceIdForDescription(), ""));
        attr.setLabelResource(fieldVO.getLocaleResourceIdForLabel());
        attr.setDescriptionResource(fieldVO.getLocaleResourceIdForDescription());

        attr.setInternalName(StringUtils.getModuleFieldName(fieldVO.getField()));
        attr.setOldInternalName(StringUtils.getModuleFieldName(fieldVO.getField()));
        attr.setDbName(fieldVO.getDbColumn());
        attr.setValueListProvider(fieldVO.isSearchable());
        attr.setDefaultValue(fieldVO.getDefaultValue());
        if(fieldVO.getDefaultForeignId() != null) {
        	attr.setIdDefaultValue(new DefaultValue(fieldVO.getDefaultForeignId().intValue(), fieldVO.getDefaultValue()));
        }
        attr.setModifiable(fieldVO.isModifiable());
        setAttributeGroup(attr, fieldVO);
        attr.setCalcFunction(fieldVO.getCalcFunction());
        attr.setOutputFormat(fieldVO.getFormatOutput());
        attr.setInputValidation(fieldVO.getFormatInput());
        attr.setCalculationScript(fieldVO.getCalculationScript());

        String sForeignEntity = fieldVO.getForeignEntity();
        if(sForeignEntity != null) {
        	EntityMetaDataVO voForeignEntity = MetaDataClientProvider.getInstance().getEntity(sForeignEntity);
        	if(voForeignEntity.isFieldValueEntity()) {
        		attr.setMetaVO(voForeignEntity);
        		attr.setField(fieldVO.getForeignEntityField());
        		attr.setDatatyp(DataTyp.getDefaultStringTyp());
        		loadValueList(attr);
        	}
        	else {
        		attr.setMetaVO(voForeignEntity);

        		attr.setOnDeleteCascade(fieldVO.isOnDeleteCascade());
        		attr.setField(fieldVO.getForeignEntityField());
        		attr.setDatatyp(DataTyp.getReferenzTyp());
        		if(!Modules.getInstance().isModuleEntity(sForeignEntity) && fieldVO.getForeignEntityField() != null) {
        			String sForeignField = fieldVO.getForeignEntityField();
        			if(sForeignField.indexOf("${") >= 0) {
        				attr.setDatatyp(DataTyp.getReferenzTyp());
        			}
        			else {
        				EntityMetaDataVO voEntity = MetaDataClientProvider.getInstance().getEntity((sForeignEntity));
        				EntityFieldMetaDataVO voField =  MetaDataClientProvider.getInstance().getEntityField(voEntity.getEntity(), sForeignField);

        				attr.getDatatyp().setJavaType(voField.getDataType());
        				if(voField.getPrecision() != null)
        					attr.getDatatyp().setPrecision(voField.getPrecision());
        				if(voField.getScale() != null)
        					attr.getDatatyp().setScale(voField.getScale());
        				if(voField.getFormatInput() != null)
        					attr.getDatatyp().setInputFormat(voField.getFormatInput());
        				if(voField.getFormatOutput() != null)
        					attr.getDatatyp().setOutputFormat(voField.getFormatOutput());
        			}

        		}
        	}
        }
        else {
            String sLookupEntity = fieldVO.getLookupEntity();
            if(sLookupEntity != null) {
            	EntityMetaDataVO voLookupEntity = MetaDataClientProvider.getInstance().getEntity(sLookupEntity);
            	if(voLookupEntity.isFieldValueEntity()) {
            		attr.setMetaVO(voLookupEntity);
            		attr.setField(fieldVO.getLookupEntityField());
            		attr.setDatatyp(DataTyp.getDefaultStringTyp());
            		loadValueList(attr);
            	}
            	else {
            		attr.setLookupMetaVO(voLookupEntity);

            		attr.setOnDeleteCascade(fieldVO.isOnDeleteCascade());
            		attr.setField(fieldVO.getLookupEntityField());
            		attr.setDatatyp(DataTyp.getLookupTyp());
            		if(!Modules.getInstance().isModuleEntity(sLookupEntity) && fieldVO.getLookupEntityField() != null) {
            			String sLookupField = fieldVO.getLookupEntityField();
            			if(sLookupField.indexOf("${") >= 0) {
            				attr.setDatatyp(DataTyp.getLookupTyp());
            			}
            			else {
            				EntityMetaDataVO voEntity = MetaDataClientProvider.getInstance().getEntity((sLookupEntity));
            				EntityFieldMetaDataVO voField =  MetaDataClientProvider.getInstance().getEntityField(voEntity.getEntity(), sLookupField);

            				attr.getDatatyp().setJavaType(voField.getDataType());
            				if(voField.getPrecision() != null)
            					attr.getDatatyp().setPrecision(voField.getPrecision());
            				if(voField.getScale() != null)
            					attr.getDatatyp().setScale(voField.getScale());
            				if(voField.getFormatInput() != null)
            					attr.getDatatyp().setInputFormat(voField.getFormatInput());
            				if(voField.getFormatOutput() != null)
            					attr.getDatatyp().setOutputFormat(voField.getFormatOutput());
            			}

            		}
            	}
            } 
            else {
	        	attr.setDatatyp(NuclosWizardUtils.getDataTyp(fieldVO.getDataType(), fieldVO.getDefaultComponentType(), 
	        		fieldVO.getScale(), fieldVO.getPrecision(), fieldVO.getFormatInput(),
	        		fieldVO.getFormatOutput()));
            }
        }

        try {
        	setDefaultMandatoryValue(fieldVO, attr);
        }
        catch (Exception e) {
			// value can't be set, don't worry
		}

        attr.setDescription(localeDelegate.getResource(fieldVO.getLocaleResourceIdForDescription(), ""));
        return attr;
    }

	private static void setDefaultMandatoryValue(EntityFieldMetaDataVO fieldVO,	Attribute attr) throws Exception {
		String sJavaType = attr.getDatatyp().getJavaType();
		if(sJavaType.equals("java.lang.Integer")) {
			attr.setMandatoryValue(new Integer(fieldVO.getDefaultMandatory()));
		}
		else if(sJavaType.equals("java.lang.Double")) {
			attr.setMandatoryValue(new Double(fieldVO.getDefaultMandatory().replace(',', '.')));
		}
		else if(sJavaType.equals("java.util.Date")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			Date date = sdf.parse(fieldVO.getDefaultMandatory());
			attr.setMandatoryValue(date);
		}
		else if(sJavaType.equals("java.lang.Boolean")){
			if(fieldVO.getDefaultMandatory().equals("true")){
				attr.setMandatoryValue(Boolean.TRUE);
			}
			else {
				attr.setMandatoryValue(Boolean.FALSE);
			}
		}
		else {
			attr.setMandatoryValue(fieldVO.getDefaultMandatory());
		}

	}

	private class EntityWrapper {

		EntityMetaDataVO voEntity;

		EntityWrapper(EntityMetaDataVO vo) {
			voEntity = vo;
		}

		@Override
        public String toString() {
			return localeDelegate.getTextFallback(voEntity.getLocaleResourceIdForLabel(), voEntity.getEntity());
		}

		public EntityMetaDataVO getWrappedEntity()  {
			return voEntity;
		}

		@Override
		public int hashCode() {
			return this.getWrappedEntity().getEntity().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof EntityWrapper) {
				EntityWrapper that = (EntityWrapper)obj;
				return org.apache.commons.lang.StringUtils.equals(this.getWrappedEntity().getEntity(), that.getWrappedEntity().getEntity());
			}
			return false;
		}

	}

}
