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
import info.clearthought.layout.TableLayoutConstraints;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.nuclos.client.application.assistant.ApplicationChangedEvent;
import org.nuclos.client.application.assistant.ApplicationObserver;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.layout.wysiwyg.LayoutMLGenerator;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGMetaInformation;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.PROPERTY_LABELS;
import org.nuclos.client.layout.wysiwyg.component.ComponentProcessors;
import org.nuclos.client.layout.wysiwyg.component.TitledBorderWithTranslations;
import org.nuclos.client.layout.wysiwyg.component.TranslationMap;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGCollectableComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubForm;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGSubFormColumn;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGTabbedPane;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyUtils;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBoolean;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueBorder;
import org.nuclos.client.layout.wysiwyg.component.properties.PropertyValueString;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.util.InterfaceGuidelines;
import org.nuclos.client.layout.wysiwyg.editor.util.TableLayoutUtil;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRule;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRuleAction;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.statemodel.RoleRepository;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Bubble.Position;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.wizard.NuclosEntityWizardStaticModel;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.model.ValueList;
import org.nuclos.client.wizard.util.NuclosWizardConstants;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.searchcondition.CollectableComparison;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common.transport.vo.EntityMetaDataTO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.InternalTimestamp;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.layoutml.LayoutMLConstants;
import org.nuclos.common2.layoutml.LayoutMLParser;
import org.nuclos.common2.layoutml.exception.LayoutMLException;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.pietschy.wizard.InvalidStateException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntitySQLLayoutStep extends NuclosEntityAbstractStep {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	static String[] labels = NuclosWizardConstants.labels;
	static String[] labelsFields = {getMessage("wizard.step.entitytranslationstable.1", "Anzeigename"), getMessage("wizard.step.entitytranslationstable.2", "Beschreibung")};
	static String[] sEditFields = {"STRCREATED", "DATCREATED", "STRCHANGED", "DATCHANGED" };

	static int I_CELL_HEIGHT = 35;
	static int I_CELL_HEIGHT_EXT = 30;
	static int I_CELL_WIDTH = 35;
	static int I_CELL_WIDTH_EXT = 50;
	static int I_MAX_WIDTH_GROUP = 35;
	static int I_SUBFORM_PANEL_HEIGHT = 300;
	static int I_SUBFORM_HEIGHT = 250;
	static int I_PANEL_SUBFORM_HEIGHT = 200;
	static int I_PANEL_WIDTH = 150;
	static int I_EDITCELL_WIDTH = 250;
	static int I_EDITCELL_HEIGTH = 50;
	static int I_CREATEAT_WIDTH = 85;
	static int I_CREATEBY_WIDTH = 75;


	JLabel lbLayout;
	JCheckBox cbLayout;
	JLabel lbAttributeGroup;
	JCheckBox cbAttributeGroup;
	JLabel lbSubforms;
	JCheckBox cbSubforms;
	JLabel lbEditFields;
	JCheckBox cbEditFields;

	JLabel lbInfo;
	JLabel lbAttributeText;

	boolean changeLayout;
	boolean hasEntityLayout;

	JPanel panelAttributes;
	JScrollPane sPane;
	JList listAttributeOrder;
	JTree treeAttributeOrder;
	JButton btUp;
	JButton btDown;

	Map<String, String> mpFieldNameChanged;

	MyTreeModel treeModel;

	public NuclosEntitySQLLayoutStep() {
		initComponents();
		mpFieldNameChanged = new HashMap<String, String>();
	}

	public NuclosEntitySQLLayoutStep(String name, String summary) {
		super(name, summary);
		initComponents();
	}

	public NuclosEntitySQLLayoutStep(String name, String summary, Icon icon) {
		super(name, summary, icon);
		initComponents();
	}


	@Override
	protected void initComponents() {

		double size [][] = {{TableLayout.FILL, 20, TableLayout.FILL}, {TableLayout.PREFERRED, 20,20,20,20,20,5,TableLayout.PREFERRED,20 }};

		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		this.setLayout(layout);

		lbLayout = new JLabel(getMessage("wizard.step.entitysqllayout.2", "M\u00f6chten Sie eine Standard-Maske generieren lassen"));
		cbLayout = new JCheckBox();
		cbLayout.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.2", "M\u00f6chten Sie eine Standard-Maske generieren lassen"));

		lbAttributeGroup = new JLabel(getMessage("wizard.step.entitysqllayout.12", "Attributegruppen werden zusammengefasst"));
		cbAttributeGroup = new JCheckBox();
		cbAttributeGroup.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.3", "Attributegruppen werden zusammengefasst"));

		lbAttributeText = new JLabel(getMessage("wizard.step.entitysqllayout.13", "Geben Sie die Reihenfolge an, in der die Felder in der Maske erstellt werden sollen"));

		lbSubforms = new JLabel(getMessage("wizard.step.entitysqllayout.14", "Vorhandene Unterformular mit ins Layout aufnehmen"));
		cbSubforms = new JCheckBox();
		cbSubforms.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.4", "Vorhandene Unterformulare mit ins Layout aufnehmen"));

		lbEditFields = new JLabel(getMessage("wizard.step.entitysqllayout.15","Editierungsfelder erstellen:"));
		cbEditFields = new JCheckBox();
		cbEditFields.setSelected(true);
		cbEditFields.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.7","Editierungsfelder mit in das Layout aufnehmen"));

		lbInfo = new JLabel(getMessage("wizard.step.entitysqllayout.9", "Achtung! Ihr bestehendes Layout wird überschrieben!"));
		lbInfo.setVisible(false);
		lbInfo.setForeground(Color.RED);

		listAttributeOrder = new JList();

		treeModel = new MyTreeModel();

		treeAttributeOrder = new JTree(treeModel);
		treeAttributeOrder.setCellRenderer(new MyTreeCellRenderer());
		treeAttributeOrder.setRootVisible(false);
		treeAttributeOrder.setExpandsSelectedPaths(true);

		sPane = new JScrollPane(listAttributeOrder);

		panelAttributes = new JPanel();
		double sizePanel [][] = {{TableLayout.FILL, 3, 20}, {20,3,20,3,TableLayout.PREFERRED }};
		panelAttributes.setLayout(new TableLayout(sizePanel));

		btUp = new JButton(Icons.getInstance().getIconSortAscending());
		btUp.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.5", "Attribut nach oben schieben"));
		btDown = new JButton(Icons.getInstance().getIconSortDescending());
		btDown.setToolTipText(getMessage("wizard.step.entitysqllayout.tooltip.6", "Attribut nach unten schieben"));

		panelAttributes.add(sPane, "0,0, 0,4");
		panelAttributes.add(btUp, "2,0");
		panelAttributes.add(btDown, "2,2");

		this.add(lbInfo, "0,0, 1,0");
		this.add(lbLayout, "0,1");
		this.add(cbLayout, "1,1");
		this.add(lbAttributeGroup, "0,2");
		this.add(cbAttributeGroup, "1,2");
		this.add(lbSubforms, "0,3");
		this.add(cbSubforms, "1,3");
		this.add(lbEditFields, "0,4");
		this.add(cbEditFields, "1,4");
		this.add(lbAttributeText, "0,5, 1,5");
		this.add(panelAttributes, "0,6, 1,7");

		enableLayoutOptions(false);

//		cbAttributeGroup.addItemListener(new ItemListener() {
//
//			@Override
//			public void itemStateChanged(ItemEvent e) {
//				JCheckBox cb = (JCheckBox)e.getItem();
//				if(cb.isSelected()) {
//					sPane.setViewportView(treeAttributeOrder);
//				}
//				else {
//					sPane.setViewportView(listAttributeOrder);
//				}
//			}
//		});

		cbLayout.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				JCheckBox cb = (JCheckBox)e.getItem();
				NuclosEntitySQLLayoutStep.this.model.setCreateLayout(cb.isSelected());
				if(cb.isSelected() && hasEntityLayout) {
					(new Bubble(cbLayout, getMessage("wizard.step.entitysqllayout.9", "Achtung! Ihr bestehendes Layout wird überschrieben!"), 5, Position.UPPER)).setVisible(true);
				}
				enableLayoutOptions(cb.isSelected());
			}
		});

		btUp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				if(cbAttributeGroup.isSelected()) {
//					buttonUpAction2();
//				}
//				else {
					buttonUpAttributeAction();
//				}

			}
		});

		btDown.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
//				if(cbAttributeGroup.isSelected()) {
//					buttonDownAction2();
//				}
//				else {
					buttonDownAttributeAction();
				}
//			}
		});

	}

	private void buttonUpGroupAction() {
		int count = treeAttributeOrder.getSelectionPath().getPathCount();
		Object selected = treeAttributeOrder.getSelectionPath().getPathComponent(count-1);
		if(selected instanceof Attribute) {
			Attribute attr = (Attribute)selected;
			String sGroup = (String)treeAttributeOrder.getSelectionPath().getParentPath().getLastPathComponent();
			List<Attribute> lst = treeModel.getAttributeGroupList(sGroup);
			int index = lst.indexOf(attr);
			if(index == 0)
				return;
			lst.remove(index);
			lst.add(index-1, attr);
			treeModel.fireTreeModelChanged();
		}
		else if(selected instanceof String) {
			String group = (String)selected;
			if("root".equals(group))
				return;
			treeModel.groupUpIfPossible(group);
			treeModel.fireTreeModelChanged();
		}
	}

	private void buttonDownGroupAction() {
		int count = treeAttributeOrder.getSelectionPath().getPathCount();
		Object selected = treeAttributeOrder.getSelectionPath().getPathComponent(count-1);
		if(selected instanceof Attribute) {
			Attribute attr = (Attribute)selected;
			String sGroup = (String)treeAttributeOrder.getSelectionPath().getParentPath().getLastPathComponent();
			List<Attribute> lst = treeModel.getAttributeGroupList(sGroup);
			int index = lst.indexOf(attr);
			if(index == lst.size()-1)
				return;
			lst.remove(index);
			lst.add(index+1, attr);
			treeModel.fireTreeModelChanged();
		}
		else if(selected instanceof String) {
			String group = (String)selected;
			if("root".equals(group))
				return;
			treeModel.groupDownIfPossible(group);
			treeModel.fireTreeModelChanged();
		}
	}

	private void buttonDownAttributeAction() {
		int iSelected = listAttributeOrder.getSelectedIndex();
		if(iSelected < 0 || iSelected >= listAttributeOrder.getModel().getSize()-1)
			return;

		DefaultListModel model = (DefaultListModel)listAttributeOrder.getModel();
		Object obj = model.remove(iSelected);
		model.add(iSelected+1, obj);
		listAttributeOrder.getSelectionModel().setSelectionInterval(iSelected+1, iSelected+1);
	}

	private void buttonUpAttributeAction() {
		int iSelected = listAttributeOrder.getSelectedIndex();
		if(iSelected < 1 || iSelected > listAttributeOrder.getModel().getSize())
			return;

		DefaultListModel model = (DefaultListModel)listAttributeOrder.getModel();
		Object obj = model.remove(iSelected);
		model.add(iSelected-1, obj);

		listAttributeOrder.getSelectionModel().setSelectionInterval(iSelected-1, iSelected-1);
	}


	private void enableLayoutOptions(boolean enable) {
		lbAttributeGroup.setEnabled(enable && getModel().isStateModel());
		lbSubforms.setEnabled(enable);
		lbEditFields.setEnabled(enable);
		cbAttributeGroup.setEnabled(enable && getModel().isStateModel());
		cbSubforms.setEnabled(enable);
		cbEditFields.setEnabled(enable);
		lbAttributeText.setEnabled(enable);
		listAttributeOrder.setEnabled(enable);
		btDown.setEnabled(enable);
		btUp.setEnabled(enable);
	}

	@Override
	public void applyState() throws InvalidStateException {
		super.applyState();

		if(!createOrModifyEntity()) {
			return;
		}

	}

	@Override
	public void prepare() {
		this.setComplete(true);
		mpFieldNameChanged = new HashMap<String, String>();

		if(this.model.getAttributeModel().getAttributes().size() == 0)
			cbLayout.setEnabled(false);
		else
			cbLayout.setEnabled(true);


		hasEntityLayout = hasEntityLayout();
		if(hasEntityLayout){
			lbLayout.setText(getMessage("wizard.step.entitysqllayout.16", "Wollen Sie die bestehende Maske aktualisieren:"));
		}

		DefaultListModel listmodel = new DefaultListModel();

		List<Attribute> lstAttr = new ArrayList<Attribute>(this.model.getAttributeModel().getAttributes());

		Collections.sort(lstAttr, new Comparator<Attribute>() {

			@Override
            public int compare(Attribute o1, Attribute o2) {
				Long l1 = (Long)ObjectUtils.defaultIfNull(o1.getInternalId(), 0L);
			    Long l2 = (Long)ObjectUtils.defaultIfNull(o2.getInternalId(), 0L);
				return l1.compareTo(l2);
            }

		});

		for(Attribute attr : lstAttr) {
			listmodel.addElement(attr);
		}

		treeModel = new MyTreeModel(model.getAttributeModel().getAttributeMap());

		try {
			treeAttributeOrder.setModel(treeModel);
			treeModel.expandWholeTree();
		}catch(Exception e) {}

		listAttributeOrder.setModel(listmodel);

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
			        validateEntity();
		        }
		        catch(CommonValidationException ex) {
		        	NuclosEntitySQLLayoutStep.this.setComplete(false);
		        	Errors.getInstance().showExceptionDialog(NuclosEntitySQLLayoutStep.this, ex);
		        }
			}
		});

	}



	private void validateEntity() throws CommonValidationException {
		// validate removed Attributes
		for(TranslationVO voTranslation : this.model.getTranslation()) {
			for(String sKey : voTranslation.getLabels().keySet()) {
				final String sValue = voTranslation.getLabels().get(sKey);
				StringBuffer sb = new StringBuffer();
				if(hasStringRemovedAttributes(sValue, sb)) {
					final String sMessage = sb.toString();
					throw new CommonValidationException(sMessage);
				}
			}
		}
		// validate attributes that changed the name
		for(Attribute attr : model.getAttributeModel().getAttributes()) {
			if(!attr.hasInternalNameChanged())
				continue;
			for(TranslationVO voTranslation : this.model.getTranslation()) {
				for(String sKey : voTranslation.getLabels().keySet()) {
					final String sValue = voTranslation.getLabels().get(sKey);
					StringBuffer sb = new StringBuffer();
					if(hasStringChangedAttributes(sValue, sb)) {
						final String sMessage = sb.toString();
						throw new CommonValidationException(sMessage);
					}
				}
			}
		}

		// validate MultiEditEquation
		if(model.getMultiEditEquation() != null && model.getMultiEditEquation().length() > 0) {
			for(String sField : model.getMultiEditEquation().split(",")) {
				for(Attribute attr : this.model.getAttributeModel().getRemoveAttributes()) {
					if(attr.getInternalName().equals(sField)) {
						String sMessage = getMessage("wizard.step.entitysqllayout.10", "Das gelöschte Attribut " + attr.getInternalName() + " befindet sich in Ihren Angaben!\nBitte überprüfen Sie Ihre Eintragungen!", attr.getInternalName());
						throw new CommonValidationException(sMessage);
					}
				}
			}

			for(String sField : model.getMultiEditEquation().split(",")) {
				for(Attribute attr : this.model.getAttributeModel().getAttributes()) {
					if(!attr.hasInternalNameChanged())
						continue;
					if(attr.getOldInternalName().equals(sField)) {
						String sMessage = getMessage("wizard.step.entitysqllayout.11", "Das geänderte Attribut " + attr.getOldInternalName() + " befindet sich in Ihren Angaben!\nBitte überprüfen Sie Ihre Eintragungen!", attr.getOldInternalName());
						throw new CommonValidationException(sMessage);
					}
				}
			}
		}

		// validate document path
		StringBuffer sb = new StringBuffer();
		if(hasStringRemovedAttributes(model.getDocumentPath(), sb)) {
			final String sMessage = sb.toString();
			throw new CommonValidationException(sMessage);
		}
		sb = new StringBuffer();
		if(hasStringChangedAttributes(model.getDocumentPath(), sb)) {
			final String sMessage = sb.toString();
			throw new CommonValidationException(sMessage);
		}

		// validate report name
		sb = new StringBuffer();
		if(hasStringRemovedAttributes(model.getReportFilename(), sb)) {
			final String sMessage = sb.toString();
			throw new CommonValidationException(sMessage);
		}
		sb = new StringBuffer();
		if(hasStringChangedAttributes(model.getReportFilename(), sb)) {
			final String sMessage = sb.toString();
			throw new CommonValidationException(sMessage);
		}

	}

	private boolean hasStringChangedAttributes(String sNodeLabel, StringBuffer sb) {
		if(sNodeLabel == null)
			return false;
		String sField = sNodeLabel;
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);

		while (referencedEntityMatcher.find()) {
		  Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getAttributes()) {
			  if(!attr.hasInternalNameChanged())
				  continue;
			  if(attr.getOldInternalName().equals(sName)){
				  String str = getMessage("wizard.step.entitysqllayout.11", "Das geänderte Attribut " + attr.getOldInternalName() + " befindet sich in Ihren Angaben!\nBitte überprüfen Sie Ihre Eintragungen!", attr.getOldInternalName());
				  sb.append(str);
				  return true;
			  }
		  }

		}

		return false;
	}

	private boolean hasStringRemovedAttributes(String sNodeLabel, StringBuffer sb) {
		if(sNodeLabel == null)
			return false;
		String sField = sNodeLabel;
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);

		while (referencedEntityMatcher.find()) {
		  Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  String sName = value.toString();
		  for(Attribute attr : this.model.getAttributeModel().getRemoveAttributes()) {
			  if(attr.getInternalName().equals(sName)){
				  String str = getMessage("wizard.step.entitysqllayout.10", "Das gelöschte Attribut " + attr.getInternalName() + " befindet sich in Ihren Angaben!\nBitte überprüfen Sie Ihre Eintragungen!", attr.getInternalName());
				  sb.append(str);
				  return true;
			  }
		  }

		}
      return false;
	}


	protected boolean createOrModifyEntity() {

		buildValueListIfNeeded();

		List<MasterDataVO> lstLayoutToChange = new ArrayList<MasterDataVO>();
		changeLayout = false;
		NuclosEntityWizardStaticModel wizardModel = this.getModel();
		EntityMetaDataVO metaVOOld = null;
		if(wizardModel.isEditMode()) {
			metaVOOld = MetaDataClientProvider.getInstance().getEntity(wizardModel.getEntityName());
		}

		EntityAttributeTableModel attributeModel = wizardModel.getAttributeModel();

		EntityMetaDataVO metaVO = new EntityMetaDataVO();

		if(metaVOOld != null){
			metaVO = MetaDataClientProvider.getInstance().getEntity(metaVOOld.getEntity());
		}

		metaVO.setEntity(wizardModel.getEntityName());

		metaVO.setDbEntity(NuclosWizardUtils.replace(wizardModel.getTableOrViewName()));
		metaVO.setSearchable(wizardModel.isSearchable());
		metaVO.setCacheable(wizardModel.isCachable());
		metaVO.setResourceId(wizardModel.getResourceId());
		metaVO.setNuclosResource(wizardModel.getNuclosResourceName());
		metaVO.setLocaleResourceIdForLabel(wizardModel.getLabelSingularResource());
		metaVO.setLocaleResourceIdForMenuPath(wizardModel.getMenuPathResource());
		metaVO.setLocaleResourceIdForTreeView(wizardModel.getNodeLabelResource());
		metaVO.setLocaleResourceIdForTreeViewDescription(wizardModel.getNodeTooltipResource());
		metaVO.setDocumentPath(wizardModel.getDocumentPath());
		metaVO.setReportFilename(wizardModel.getReportFilename());
		metaVO.setEditable(wizardModel.isEditable());
		metaVO.setSystemIdPrefix(wizardModel.getSystemIdPrefix());
		metaVO.setLogBookTracking(wizardModel.isLogbook());
		metaVO.setTreeRelation(wizardModel.isShowRelation());
		metaVO.setTreeGroup(wizardModel.isShowGroups());
		metaVO.setStateModel(wizardModel.isStateModel());
		metaVO.setImportExport(true);
		metaVO.setDynamic(false);
		metaVO.setAccelerator(wizardModel.getAccelerator());
		metaVO.setFieldValueEntity(Boolean.FALSE);
		if(wizardModel.getAccelerator() != null)
			metaVO.setAcceleratorModifier(wizardModel.getModifier());
		else
			metaVO.setAcceleratorModifier(null);

		if(metaVOOld != null) {
			metaVO.setId(metaVOOld.getId());
		}
		else {
			metaVO.setId(null);
		}

		metaVO.setFieldsForEquality(wizardModel.getMultiEditEquation());
		metaVO.setVirtualentity(wizardModel.getVirtualentity());

		List<EntityFieldMetaDataTO> lstEntityFields = new ArrayList<EntityFieldMetaDataTO>();

		for(Attribute attr : attributeModel.getAttributes()) {
			EntityFieldMetaDataVO mdFieldVO = buildMasterDataField(attr, metaVO, metaVOOld);
			EntityFieldMetaDataTO to = new EntityFieldMetaDataTO();
			to.setEntityFieldMeta(mdFieldVO);
			to.setTranslation(attributeModel.getTranslation().get(attr));
			lstEntityFields.add(to);
		}
		if(wizardModel.isEditMode()) {
			for(Attribute attr : attributeModel.getRemoveAttributes()) {
				EntityFieldMetaDataVO mdFieldVO = buildMasterDataField(attr, metaVO, metaVOOld);
				mdFieldVO.flagRemove();
				EntityFieldMetaDataTO to = new EntityFieldMetaDataTO();
				to.setEntityFieldMeta(mdFieldVO);
				to.setTranslation(attributeModel.getTranslation().get(attr));

				lstEntityFields.add(to);
			}
		}

		try {
			lstLayoutToChange = searchParentLayouts();
			EntityMetaDataTO to = new EntityMetaDataTO();
			to.setEntityMetaVO(metaVO);
			to.setTranslation(wizardModel.getTranslation());
			to.setTreeView(wizardModel.getTreeView());
			if (metaVO.isStateModel()) {
				to.setProcesses(wizardModel.getProcesses());
			}
			to.setMenus(wizardModel.getEntityMenus());
			String sResult = MetaDataDelegate.getInstance().createOrModifyEntity(metaVOOld, to, null, lstEntityFields, true, null, null);
			if(sResult != null) {
				Errors.getInstance().showExceptionDialog(this, new CommonFatalException(sResult));
				throw new CommonFatalException(sResult);
			}
			this.model.setResultText(sResult);
		}
		catch(CommonBusinessException bex) {
			 Errors.getInstance().showExceptionDialog(this, bex);
			 return false;
		}
		catch(Exception e) {
			Errors.getInstance().showExceptionDialog(this, e);
			return false;
		}
		finally {
			NuclosWizardUtils.flushCaches();
		}


		Long iEntityId = MetaDataDelegate.getInstance().getEntityIdByName(metaVO.getEntity());


		try {
			RoleRepository.getInstance().updateRoles();
	   }
	   catch(Exception e) {
	   	throw new NuclosFatalException(e);
	   }

		try {
			 for(MasterDataVO vo : wizardModel.getUserRights()) {
				 if(wizardModel.isStateModel()) {
					Integer iRoleId = (Integer)vo.getField("roleId");
					MasterDataVO voRole = null;
 					try {
	 					voRole = RoleRepository.getInstance().getRole(iRoleId);
	 				}
	 				catch(Exception e) {
	 					throw new NuclosFatalException(e);
	 				}

					 Integer permission = (Integer)vo.getField("modulepermission");

					 if(permission == null)
						 vo.remove();
					 else
						 vo.setField("modulepermission", permission);

					 vo.setField("module", metaVO.getEntity());
					 vo.setField("moduleId", iEntityId);

					 DependantMasterDataMap mp = new DependantMasterDataMap(NuclosEntity.ROLEMODULE.getEntityName(),
						 Collections.singletonList(DalSupportForMD.getEntityObjectVO(vo)));

					 MasterDataDelegate.getInstance().update(NuclosEntity.ROLE.getEntityName(), voRole, mp);
				 }
				 else {
					 MasterDataVO voRole = null;
					 Integer iRoleId = (Integer)vo.getField("roleId");
 					 try {
 						 voRole = RoleRepository.getInstance().getRole(iRoleId);
	 				 }
	 				 catch(Exception e) {
	 					 throw new NuclosFatalException(e);
	 				 }
					 Integer permission = (Integer)vo.getField("masterdatapermission");
					 vo.setField("entity", metaVO.getEntity());

					 vo.setField("masterdatapermission", permission);
					 if(permission == null)
						 vo.remove();
					 else
						 vo.setField("masterdatapermission", permission);

					 DependantMasterDataMap mp = new DependantMasterDataMap(NuclosEntity.ROLEMASTERDATA.getEntityName(), Collections.singletonList(DalSupportForMD.getEntityObjectVO(vo)));
					 MasterDataDelegate.getInstance().update(NuclosEntity.ROLE.getEntityName(), voRole, mp);

				 }
				 try {
						RoleRepository.getInstance().updateRoles();
				 }
				 catch(Exception e) {
						 throw new NuclosFatalException(e);
				 }
			 }

		}
		catch(CommonBusinessException bex) {
			// do nothing here
      }


		if(wizardModel.isImportTable()) {
			MetaDataDelegate.getInstance().transferTable(wizardModel.getJdbcUrl(), wizardModel.getExternalUser(),
				wizardModel.getExternalPassword(), null, wizardModel.getExternalTable(),
				wizardModel.getEntityName());
		}

		if(wizardModel.isCreateLayout()) {
			MasterDataMetaVO masterVO = MasterDataDelegate.getInstance().getMetaData(NuclosEntity.ENTITY.getEntityName());
			CollectableMasterDataEntity masterDataEntity = new CollectableMasterDataEntity(masterVO);
			buildLayoutML(wizardModel, masterDataEntity, iEntityId.intValue());
		}

		if(wizardModel.getAttributeModel().getRemoveAttributes().size() > 0 && !wizardModel.isCreateLayout()) {
			for(Attribute attr : wizardModel.getAttributeModel().getRemoveAttributes()) {

				Set<Integer> lstLayouts = new HashSet<Integer>();
				CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "entity", ComparisonOperator.EQUAL, wizardModel.getEntityName());
				for(MasterDataVO layout : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
					lstLayouts.add((Integer)layout.getField("layoutId"));
				}

				for(Integer iLayoutId : lstLayouts) {
					MasterDataVO voLayout = null;
					try {
						voLayout = MasterDataDelegate.getInstance().get(NuclosEntity.LAYOUT.getEntityName(), iLayoutId);
					}
					catch(CommonFinderException ex) {
						throw new NuclosFatalException(ex);
					}
					catch(CommonPermissionException ex) {
						throw new NuclosFatalException(ex);
					}
					String sLayout = (String)voLayout.getField("layoutML");
					if(sLayout == null)
						continue;
					sLayout = modifyLayout(sLayout, attr.getInternalName(), null, true);
					if(sLayout != null) {
						voLayout.setField("layoutML", sLayout);
						try {
							MasterDataDelegate.getInstance().update(NuclosEntity.LAYOUT.getEntityName(), voLayout, null);
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}
				}
			}
		}

		if(mpFieldNameChanged.size() > 0 && !wizardModel.isCreateLayout()) {
			for(String sField : mpFieldNameChanged.keySet()) {
				String newField = mpFieldNameChanged.get(sField);

				Set<Integer> lstLayouts = new HashSet<Integer>();
				CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "entity", ComparisonOperator.EQUAL, wizardModel.getEntityName());
				for(MasterDataVO layout : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
					lstLayouts.add((Integer)layout.getField("layoutId"));
				}

				for(Integer iLayoutId : lstLayouts) {
					MasterDataVO voLayout = null;
					try {
						voLayout = MasterDataDelegate.getInstance().get(NuclosEntity.LAYOUT.getEntityName(), iLayoutId);
					}
					catch(CommonFinderException ex) {
						throw new NuclosFatalException(ex);
					}
					catch(CommonPermissionException ex) {
						throw new NuclosFatalException(ex);
					}
					String sLayout = (String)voLayout.getField("layoutML");
					if(sLayout == null)
						continue;

					sLayout = modifyLayout(sLayout, sField, newField, false);

					if(sLayout != null) {
						voLayout.setField("layoutML", sLayout);
						try {
							MasterDataDelegate.getInstance().update(NuclosEntity.LAYOUT.getEntityName(), voLayout, null);
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}
				}
			}
		}

		for(MasterDataVO voParentLayout : lstLayoutToChange) {
			try {
	            MasterDataDelegate.getInstance().update(NuclosEntity.LAYOUT.getEntityName(), voParentLayout, null);
            }
            catch(CommonBusinessException e) {
            	throw new NuclosFatalException(e);
            }
		}

		MasterDataCache.getInstance().invalidate(NuclosEntity.LAYOUT.getEntityName());
		MasterDataCache.getInstance().invalidate(NuclosEntity.LAYOUTUSAGE.getEntityName());
		MasterDataDelegate.getInstance().invalidateCaches();
		MasterDataDelegate.getInstance().invalidateLayoutCache();
		MetaDataClientProvider.getInstance().revalidate();

		ServiceLocator.getInstance().getFacade(ConsoleFacadeRemote.class).invalidateAllCaches();

		ApplicationChangedEvent event = new ApplicationChangedEvent(ApplicationObserver.NEWENTITY, metaVO.getEntity());
		ApplicationObserver.getInstance().fireApplicationChangedEvent(event);

		return true;
	}

	private List<MasterDataVO> searchParentLayouts() {
		List<MasterDataVO> layoutToChange = new ArrayList<MasterDataVO>();
		for(Attribute attr : model.getAttributeModel().getRemoveAttributes()) {
			if(attr.getMetaVO() != null) {
				for(String sParentEntity : searchParentEntity(model.getEntityName())) {
					Set<Integer> lstLayouts = new HashSet<Integer>();
					CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "entity", ComparisonOperator.EQUAL, sParentEntity);
					for(MasterDataVO layout : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
						lstLayouts.add((Integer)layout.getField("layoutId"));
					}
					for(Integer iLayoutId : lstLayouts) {
						try {
							MasterDataVO voLayout = MasterDataDelegate.getInstance().get(NuclosEntity.LAYOUT.getEntityName(), iLayoutId);

							String sLayout = (String)voLayout.getField("layoutML");

							WYSIWYGLayoutControllingPanel ctrlPanel = new WYSIWYGLayoutControllingPanel(new WYSIWYGMetaInformation());

							ctrlPanel.setLayoutML(sLayout);

							if(attr.getField() != null && NuclosWizardUtils.searchParentEntity(getModel().getEntityName()).size() < 1) {
								List<WYSIWYGComponent> allCollectables = new ArrayList<WYSIWYGComponent>();
								ctrlPanel.getEditorPanel().getWYSIWYGComponents(WYSIWYGComponent.class, ctrlPanel.getEditorPanel().getMainEditorPanel(), allCollectables);
								for(WYSIWYGComponent collectable : allCollectables) {
									if(collectable.getLayoutMLRulesIfCapable() == null)
										continue;
									Collection<LayoutMLRule> copyOfRules = new ArrayList<LayoutMLRule>();
									copyOfRules.addAll(collectable.getLayoutMLRulesIfCapable().getRules());
									for(LayoutMLRule rule : collectable.getLayoutMLRulesIfCapable().getRules()) {
										for(LayoutMLRuleAction action : rule.getLayoutMLRuleActions().getSingleActions()) {
											if(action.getEntity() != null && action.getEntity().equals(model.getEntityName())) {
												if(action.getTargetComponent().equals(attr.getInternalName())) {
													copyOfRules.remove(rule);
												}
											}
										}
									}
									collectable.getLayoutMLRulesIfCapable().clearRulesForComponent();
									for(LayoutMLRule rule : copyOfRules) {
										collectable.getLayoutMLRulesIfCapable().addRule(rule);
									}
								}

								List<WYSIWYGSubForm> allSubForms = new ArrayList<WYSIWYGSubForm>();
								ctrlPanel.getEditorPanel().getWYSIWYGComponents(WYSIWYGSubForm.class, ctrlPanel.getEditorPanel().getMainEditorPanel(), allSubForms);
								for(WYSIWYGSubForm collectable : allSubForms) {
									Collection<WYSIWYGSubFormColumn> copyOfColumns = new ArrayList<WYSIWYGSubFormColumn>(collectable.getColumns());
									for(WYSIWYGSubFormColumn col : copyOfColumns) {
										CollectableEntityField field = col.getEntityField();
										if(field.getName().equals(attr.getInternalName())) {
											collectable.removeColumn(col.getName());
										}
									}

								}
							}
							else if(NuclosWizardUtils.searchParentEntity(getModel().getEntityName()).size() > 0) {
								List<WYSIWYGSubForm> allSubForms = new ArrayList<WYSIWYGSubForm>();
								ctrlPanel.getEditorPanel().getWYSIWYGComponents(WYSIWYGSubForm.class, ctrlPanel.getEditorPanel().getMainEditorPanel(), allSubForms);
								for(WYSIWYGSubForm collectable : allSubForms) {
									String sSubFormEntity = collectable.getEntityName();
									if(getModel().getEntityName().equals(sSubFormEntity) && attr.getMetaVO().getEntity().equals(sParentEntity)) {
										ctrlPanel.getEditorPanel().getTableLayoutUtil().removeComponentFromLayout(collectable);
									}
								}
							}

							sLayout = ctrlPanel.getLayoutML();

							voLayout.setField("layoutML", sLayout);
							layoutToChange.add(voLayout);
						}
						catch(Exception e) {
							   // don't modify layout
						}

					}

				}
			}
		}

		return layoutToChange;
	}

	private Set<String> searchParentEntity(String sEntity) {
		Set<String> setParents = new HashSet<String>();

		for(MasterDataVO vo : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUT.getEntityName())) {
			LayoutMLParser parser = new LayoutMLParser();
			try {
	            String sLayout = (String)vo.getField("layoutML");
	            if(sLayout == null)
	            	continue;
				Set<String> setSubforms = parser.getSubFormEntityNames(new InputSource(new StringReader(sLayout)));
	            if(setSubforms.contains(sEntity)) {
					CollectableComparison compare = SearchConditionUtils.newComparison(NuclosEntity.LAYOUTUSAGE.getEntityName(), "layout", ComparisonOperator.EQUAL, vo.getField("name"));
					for(MasterDataVO voUsage : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.LAYOUTUSAGE.getEntityName(), compare)) {
						setParents.add((String)voUsage.getField("entity"));
					}
	            }
            }
            catch(LayoutMLException e) {
	            // do nothing here
            }

		}


		return setParents;
	}

	private void buildLayoutML(NuclosEntityWizardStaticModel wizardModel, CollectableMasterDataEntity masterDataEntity, Integer iEntityId) {
		WYSIWYGLayoutEditorPanel panel = new WYSIWYGLayoutEditorPanel(new WYSIWYGMetaInformation());
		TableLayoutUtil util = panel.getTableLayoutUtil();
		util.createStandardLayout();

		try {

			 List<EntityFieldMetaDataVO> metaFields = new ArrayList<EntityFieldMetaDataVO>(MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(wizardModel.getEntityName()).values());

			 SortedMap<Integer, EntityFieldMetaDataVO> mpFields = new TreeMap<Integer, EntityFieldMetaDataVO>();

			 for(int i = 0; i < listAttributeOrder.getModel().getSize(); i++) {
				 Attribute attr = (Attribute)listAttributeOrder.getModel().getElementAt(i);
				 for(EntityFieldMetaDataVO voField : metaFields) {
					 if(voField.getField().equals(attr.getInternalName())) {
						 mpFields.put(i, voField);
						 break;
					 }
				 }
			 }

			 List<EntityFieldMetaDataVO> setEditFields = new ArrayList<EntityFieldMetaDataVO>();
			 for(EntityFieldMetaDataVO voField : metaFields) {
				 if(isEditField(voField)) {
					 setEditFields.add(voField);
				 }
			 }

			 int maxWidth = 0;

			 for(Integer iKey : mpFields.keySet()) {
				 EntityFieldMetaDataVO field = mpFields.get(iKey);
				 if(isSystemField(field))
					 continue;

				 String sdesc = CommonLocaleDelegate.getResource(field.getLocaleResourceIdForDescription(), "");
				 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());
				 Font font = new Font("Courier", Font.PLAIN, 12);
				 c.setFont(font);
				 FontMetrics fontmetrics = c.getFontMetrics(font);
				 if(maxWidth < fontmetrics.stringWidth(sdesc)) {
					 maxWidth = fontmetrics.stringWidth(sdesc);
				 }
			 }
			 if(maxWidth < InterfaceGuidelines.DEFAULT_COLUMN_WIDTH ) {
				 maxWidth = InterfaceGuidelines.DEFAULT_COLUMN_WIDTH;
			 }

			 int counter = 1;

			 if(cbAttributeGroup.isSelected()) {
				 counter = buildAttributeGroupsForLayout(util, mpFields, maxWidth, counter);
			 }
			 else {
				 counter = createComponentForLayout(panel, util, mpFields, maxWidth, counter);
			 }

			 if(cbSubforms.isSelected()) {

				 Map<String, String> mpSubEntites = getSubformEntities(wizardModel);
				 if(mpSubEntites.size() > 1) {
					 counter = buildSubformsLayout(util, maxWidth, counter,	mpSubEntites);
				 }
				 else if(mpSubEntites.size() == 1) {
					 counter = buildSubformLayout(util, counter, mpSubEntites);
				 }

				 if(mpSubEntites.size() > 0) {
					 LayoutCell cell = createBetweenCell(counter);
					 util.addRow(cell);
					 counter++;
				 }

			 }
			 // subforms end

			 if(cbEditFields.isSelected())
				 buildEditFieldsPanel(panel, util, setEditFields, maxWidth, counter);

			 createDefaultBorder(util);

			LayoutMLGenerator gen = new LayoutMLGenerator();
			String layoutml = gen.getLayoutML(panel);

			final String sLayoutType;
			final String sLayoutUsageType;
			sLayoutType = NuclosEntity.LAYOUT.getEntityName();
			sLayoutUsageType = NuclosEntity.LAYOUTUSAGE.getEntityName();

			MasterDataMetaVO metaLayout = MasterDataDelegate.getInstance().getMetaData(sLayoutType);
			CollectableMasterDataEntity masterDataLayout = new CollectableMasterDataEntity(metaLayout);
			CollectableMasterData masterDataLayouml = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataLayout.getMasterDataMetaCVO(), false));
			MasterDataVO mdLayout = masterDataLayouml.getMasterDataCVO();
			mdLayout.setField("name", wizardModel.getEntityName());
			mdLayout.setField("description", wizardModel.getEntityName());
			mdLayout.setField("layoutML", layoutml);

			MasterDataMetaVO metaLayoutUsage = MasterDataDelegate.getInstance().getMetaData(sLayoutUsageType);
			CollectableMasterDataEntity masterDataLayoutUsage = new CollectableMasterDataEntity(metaLayoutUsage);
			CollectableMasterData masterDataLayoumlUsage = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataLayoutUsage.getMasterDataMetaCVO(), false));
			MasterDataVO mdLayoutUsage = masterDataLayoumlUsage.getMasterDataCVO();
			DependantMasterDataMap dependMapLayoutUsage = new DependantMasterDataMap();

			mdLayoutUsage.setField("entity", wizardModel.getEntityName());

			mdLayoutUsage.setField("searchScreen", Boolean.FALSE);
			mdLayoutUsage.setField("layout", wizardModel.getEntityName());
			dependMapLayoutUsage.addData(sLayoutUsageType, DalSupportForMD.getEntityObjectVO(mdLayoutUsage));

			metaLayoutUsage = MasterDataDelegate.getInstance().getMetaData(sLayoutUsageType);
			masterDataLayoutUsage = new CollectableMasterDataEntity(metaLayoutUsage);
			masterDataLayoumlUsage = new CollectableMasterData(masterDataEntity, new MasterDataVO(masterDataLayoutUsage.getMasterDataMetaCVO(), false));
			mdLayoutUsage = masterDataLayoumlUsage.getMasterDataCVO();

			mdLayoutUsage.setField("entity", wizardModel.getEntityName());

			mdLayoutUsage.setField("searchScreen", Boolean.TRUE);
			mdLayoutUsage.setField("layout", wizardModel.getEntityName());
			dependMapLayoutUsage.addData(sLayoutUsageType, DalSupportForMD.getEntityObjectVO(mdLayoutUsage));

			try {
				String sCompareField = "entity";

				CollectableComparison compare = SearchConditionUtils.newMDComparison(metaLayoutUsage, sCompareField, ComparisonOperator.EQUAL, wizardModel.getEntityName());
				Collection<MasterDataVO> colLayout = MasterDataDelegate.getInstance().getMasterDataFacade().getMasterData(sLayoutUsageType, compare, true);

				if(colLayout.size() > 0) {
					MasterDataVO voLayoutUsage = colLayout.iterator().next();
					MasterDataVO voLayout = MasterDataDelegate.getInstance().get(sLayoutType, voLayoutUsage.getField("layoutId"));
					voLayout.setField("layoutML", layoutml);
					MasterDataDelegate.getInstance().update(sLayoutType, voLayout, voLayout.getDependants());
				}
				else {
					MasterDataDelegate.getInstance().create(sLayoutType, mdLayout, dependMapLayoutUsage);
				}


			}
			catch(Exception ex) {
				Errors.getInstance().showExceptionDialog(this, ex);
			}

		}
		catch(CommonValidationException e) {
			Errors.getInstance().showExceptionDialog(this, e);
		}catch(CommonBusinessException e1) {
			Errors.getInstance().showExceptionDialog(this, e1);
		}

	}

	private void createDefaultBorder(TableLayoutUtil util) {
		try {
			LayoutCell cell = util.getLayoutCellByPosition(0, util.getNumRows());
			util.delRow(cell);

			LayoutCell cellForEditing = new LayoutCell();
			cellForEditing.setCellX(util.getNumColumns()-1);
			cellForEditing.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
			if (util.getNumColumns() > 0)
				cellForEditing.setCellX(cellForEditing.getCellX() + 1);
			util.addCol(cellForEditing);

			cellForEditing = new LayoutCell();
			cellForEditing.setCellY(util.getNumRows()-1);
			cellForEditing.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
	        if (util.getNumRows() > 0)
				cellForEditing.setCellY(cellForEditing.getCellY() + 1);
			util.addRow(cellForEditing);
		}
		catch(Exception ex) {
			// don't create default border when exception
		}

	}

	private LayoutCell createBetweenCell(int counter) {
		LayoutCell cell = new LayoutCell();
		 cell.setCellX(counter);
		 cell.setCellY(counter);
		 cell.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
		 cell.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
		return cell;
	}

	private int createComponentForLayout(WYSIWYGLayoutEditorPanel panel, TableLayoutUtil util, SortedMap<Integer, EntityFieldMetaDataVO> mpFields, int maxWidth, int counter) throws CommonBusinessException {
		for(Integer iKey : mpFields.keySet()) {
			 EntityFieldMetaDataVO field = mpFields.get(iKey);
			 if(isEditField(field)) {
				 continue;
			 }
			 if(isSystemField(field) || isBinaryField(field))
				 continue;
			 if(field.getForeignEntity() != null && field.getForeignEntityField() == null)
				 continue;

		  	 String controltype = getCollectableComponentType(field);

			 //label
		  	 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());

		  	 LayoutCell cell = new LayoutCell();
			 cell.setCellX(counter);
			 cell.setCellY(counter);
			 int iHeight = c.getPreferredSize().height;
			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA) || controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 iHeight *=3;

			 cell.setCellHeight(iHeight);
			 cell.setCellWidth(maxWidth);
			 c.setPreferredSize(new Dimension(maxWidth, iHeight));
			 util.addRow(cell);
			 if (counter == 1) {
			 	util.addCol(cell);
			 	cell = new LayoutCell();
				cell.setCellX(2);
				cell.setCellY(counter);
		 		cell.setCellWidth(maxWidth*2);
				util.addCol(cell);
			 	if(field.getDataType().equals("java.lang.String")) {
					cell = new LayoutCell();
					cell.setCellX(3);
					cell.setCellY(counter);
			 		cell.setCellWidth(maxWidth*2);
					util.addCol(cell);
			 	}
			 	else {
			 		util.addCol(cell);
			 	}
			 }

			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA) && !controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(1, counter, 1, counter, 2,0));
			 else if (!controltype.equals(LayoutMLConstants.CONTROLTYPE_IMAGE))
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(1, counter));
			 //component
			 c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, controltype, panel.getMetaInformation(), field.getField());

			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA)) {
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 4, counter));
				 util.modifyTableLayoutSizes(TableLayout.FILL, false, cell, true, false);
			 }
			 else if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTFIELD)){
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter, 3, counter,2,0));

				 //util.modifyTableLayoutSizes(TableLayout.PREFERRED, true, cell, true, false);
			 }
			 else
				 util.insertComponentTo((WYSIWYGComponent)c, new TableLayoutConstraints(2, counter));
			 counter++;
			 cell = new LayoutCell();
			 cell.setCellX(counter);
			 cell.setCellY(counter);
			 cell.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
			 if(field.getDataType().equals("java.lang.String")) {
				 cell.setCellWidth(c.getPreferredSize().width*2);
			 }
			 else
				 cell.setCellWidth(c.getPreferredSize().width);
			 util.addRow(cell);

			 counter++;
		 }
		return counter;
	}

	private void buildEditFieldsPanel(WYSIWYGLayoutEditorPanel panel, TableLayoutUtil util, List<EntityFieldMetaDataVO> setEditFields, int maxWidth, int counter) throws CommonBusinessException {
		int columncounter = 1;
		 int rowcounter = 1;
		 List<EntityFieldMetaDataVO> lstEditFields = sortEditFields(setEditFields);

		 WYSIWYGLayoutEditorPanel panelEdit = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "Editierung");
		 LayoutCell upperLeftCorner = panelEdit.getTableLayoutUtil().getLayoutCellByPosition(0, 0);
		 panelEdit.getTableLayoutUtil().modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, true, upperLeftCorner, false, false);
		 panelEdit.getTableLayoutUtil().modifyTableLayoutSizes(TableLayoutUtil.ACTION_TOGGLE_STANDARDBORDER, false, upperLeftCorner, false, false);

		 TranslationMap tm = new TranslationMap(LocaleDelegate.getInstance().getAllResourcesByStringId("nuclos.layout.editfield"));
		 Border border = new TitledBorderWithTranslations(tm.remove(LocaleInfo.I_DEFAULT_TAG), tm);

		 PropertyValueBorder prop = new PropertyValueBorder();
		 prop.setValue(border);


		 panelEdit.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);
		 LayoutCell cellEdit = new LayoutCell();
		 cellEdit.setCellX(columncounter);
		 cellEdit.setCell2X(columncounter + (cbAttributeGroup.isSelected() ? 1 : 3));
		 cellEdit.setCellY(counter);
		 cellEdit.setCell2Y(counter);

		 cellEdit.setCellHeight(I_EDITCELL_HEIGTH);
		 cellEdit.setCellWidth(I_EDITCELL_WIDTH);
		 panelEdit.setPreferredSize(new Dimension(I_CELL_WIDTH, I_CELL_HEIGHT));

		 util.addRow(cellEdit);

		 util.insertComponentTo(panelEdit, cellEdit);

		 TableLayoutUtil utilEdit = panelEdit.getTableLayoutUtil();

		 columncounter = 1;
		 rowcounter = 1;

		 for(EntityFieldMetaDataVO field : lstEditFields) {
			 String controltype = getCollectableComponentType(field);
			 // Label
			 Component c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, LayoutMLConstants.ELEMENT_LABEL, panel.getMetaInformation(), field.getField());
			 LayoutCell cell = new LayoutCell();
			 cell.setCellX(columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(75);
			 c.setPreferredSize(new Dimension(maxWidth, c.getPreferredSize().height));
			 utilEdit.addCol(cell);

			 utilEdit.insertComponentTo((WYSIWYGComponent)c, cell);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(InterfaceGuidelines.MARGIN_BETWEEN);
			 utilEdit.addCol(cell);

			 // Component
			 c = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_COLLECTABLECOMPONENT, controltype, panel.getMetaInformation(), field.getField());
			 ((WYSIWYGComponent)c).getProperties().setProperty(WYSIWYGComponent.PROPERTY_ENABLED, new PropertyValueBoolean(Boolean.FALSE), null);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 if(controltype.equals(LayoutMLConstants.CONTROLTYPE_DATECHOOSER))
				 cell.setCellWidth(I_CREATEAT_WIDTH);
			 else
				 cell.setCellWidth(I_CREATEBY_WIDTH);
			 c.setPreferredSize(new Dimension(maxWidth+I_CELL_HEIGHT_EXT, c.getPreferredSize().height));
			 utilEdit.addCol(cell);
			 utilEdit.insertComponentTo((WYSIWYGComponent)c, cell);

			 cell = new LayoutCell();
			 cell.setCellX(++columncounter);
			 cell.setCell2X(columncounter);
			 cell.setCellY(rowcounter);
			 cell.setCell2Y(rowcounter);
			 cell.setCellHeight(c.getPreferredSize().height);
			 cell.setCellWidth(InterfaceGuidelines.DISTANCE_TO_OTHER_OBJECTS);
			 utilEdit.addCol(cell);

			 columncounter++;
		 }
	}

	private int buildSubformLayout(TableLayoutUtil util, int counter, Map<String, String> mpSubEntites) throws CommonBusinessException {
		 String sSubEntity = mpSubEntites.keySet().iterator().next();
		 WYSIWYGLayoutEditorPanel panelSubEntites = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
		 LayoutCell cell = new LayoutCell();
		 cell.setCellX(1);
		 if(cbAttributeGroup.isSelected())
			 cell.setCell2X(2);
		 else
			 cell.setCell2X(4);
		 cell.setCellY(counter);
		 cell.setCell2Y(counter++);
		 cell.setCellHeight(I_SUBFORM_PANEL_HEIGHT);
		 cell.setCellWidth(I_MAX_WIDTH_GROUP);
		 util.addRow(cell);
		 util.insertComponentTo(panelSubEntites, cell);

		 WYSIWYGSubForm subForm = (WYSIWYGSubForm)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_SUBFORM, LayoutMLConstants.ELEMENT_SUBFORM, new WYSIWYGMetaInformation(), sSubEntity);
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_ENTITY, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_ENTITY));
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_FOREIGNKEY, new PropertyValueString(mpSubEntites.get(sSubEntity)), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_FOREIGNKEY));

		 subForm.setName(sSubEntity);
		 subForm.setProperty(WYSIWYGSubForm.PROPERTY_NAME, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_NAME));
		 subForm.setSize(I_SUBFORM_PANEL_HEIGHT, I_SUBFORM_HEIGHT);

		 TableLayoutConstraints constr = new TableLayoutConstraints(1, 1, 1, 1, 2, 2);
		 panelSubEntites.getTableLayoutUtil().insertComponentTo(subForm, constr);
		 return counter;
	}

	private int buildSubformsLayout(TableLayoutUtil util, int maxWidth,	int counter, Map<String, String> mpSubEntites)
		throws CommonBusinessException {
		WYSIWYGLayoutEditorPanel panelSubEntites = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
		TranslationMap tm = new TranslationMap();
		tm.put("de", "");
		tm.put("en", "");
		Border border = new TitledBorderWithTranslations("", tm);

		PropertyValueBorder prop = new PropertyValueBorder();
		prop.setValue(border);


		panelSubEntites.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);

		LayoutCell cell = new LayoutCell();
		 cell.setCellX(1);
		 if(cbAttributeGroup.isSelected())
			 cell.setCell2X(2);
		 else
			 cell.setCell2X(4);
		 cell.setCellY(counter);
		 cell.setCell2Y(counter);
		 int iHeight = I_PANEL_SUBFORM_HEIGHT;

		 cell.setCellHeight(iHeight+100);
		 cell.setCellWidth(maxWidth);
		 panelSubEntites.setPreferredSize(new Dimension(I_PANEL_WIDTH, iHeight+35));
		 util.addRow(cell);
		 if (counter == 1) {
		 	util.addCol(cell);
		 }

		 util.insertComponentTo(panelSubEntites, cell);
		 TableLayoutUtil utilSubEntities = panelSubEntites.getTableLayoutUtil();

		 cell = new LayoutCell();
		 cell.setCellX(1);
		 cell.setCell2X(2);
		 cell.setCellY(1);
		 cell.setCell2Y(1);
		 iHeight = I_PANEL_SUBFORM_HEIGHT;

		 cell.setCellHeight(iHeight+70);
		 cell.setCellWidth(maxWidth);
		 WYSIWYGTabbedPane tabPane = (WYSIWYGTabbedPane)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_TABBEDPANE, LayoutMLConstants.ELEMENT_TABBEDPANE, new WYSIWYGMetaInformation(), "tab");
		 utilSubEntities.addRow(cell);
		 utilSubEntities.addCol(cell);
		 utilSubEntities.insertComponentTo(tabPane, cell);

		 int tabcounter = 0;
		 for(String sSubEntity : mpSubEntites.keySet()) {
			 Component comp = null;
			 try {
				comp = tabPane.getComponent(tabcounter++);
			 }
			 catch(Exception ex) {
				 // new tab necessary
				 comp = ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), "subEntites");
				 tabPane.addTab(sSubEntity, comp);
			 }

			 if(!(comp instanceof WYSIWYGLayoutEditorPanel))
				 continue;
			 WYSIWYGSubForm subForm = (WYSIWYGSubForm)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_SUBFORM, LayoutMLConstants.ELEMENT_SUBFORM, new WYSIWYGMetaInformation(), sSubEntity);
			 subForm.setProperty(WYSIWYGSubForm.PROPERTY_ENTITY, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_ENTITY));
			 subForm.setProperty(WYSIWYGSubForm.PROPERTY_FOREIGNKEY, new PropertyValueString(mpSubEntites.get(sSubEntity)), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_FOREIGNKEY));

			 subForm.setName(sSubEntity);
			 subForm.setProperty(WYSIWYGSubForm.PROPERTY_NAME, new PropertyValueString(sSubEntity), PropertyUtils.getValueClass(subForm, WYSIWYGSubForm.PROPERTY_NAME));
			 subForm.setSize(tabPane.getWidth(), tabPane.getHeight()*2);

			 WYSIWYGLayoutEditorPanel tabPanel = (WYSIWYGLayoutEditorPanel)comp;

			 cell = new LayoutCell();
			 cell.setCellX(1);
			 cell.setCell2X(1);
			 cell.setCellY(1);
			 cell.setCell2Y(1);
			 cell.setCellHeight(tabPanel.getHeight()*2);
			 cell.setCellWidth(tabPanel.getWidth());
			 cell.setCellDimensionsHeight(tabPanel.getHeight()*2);
			 cell.setCellDimensionsWidth(tabPanel.getWidth());
			 TableLayoutConstraints constr = new TableLayoutConstraints(1, 1, 1, 1, 2, 2);

			 tabPanel.getTableLayoutUtil().insertComponentTo(subForm, constr);

			 tabPane.setTitleAt(tabcounter-1, sSubEntity);

		 }
		 counter++;
		return counter;
	}

	private Long getAttributeGroupId(String sGroup) {
		if(sGroup == null || sGroup.length() == 0)
			return -1L;
		Long l = -1L;
		for(MasterDataVO voGroup : MasterDataDelegate.getInstance().getMasterData("nuclos_entityfieldgroup")) {
			if(sGroup.equals(voGroup.getField("name")))
				return voGroup.getIntId().longValue();
		}
		return l;
	}

	private int buildAttributeGroupsForLayout(TableLayoutUtil util,	SortedMap<Integer, EntityFieldMetaDataVO> mpFields, int maxWidth, int counter) throws CommonFinderException, CommonPermissionException,	CommonBusinessException {
		 for(String sGroup : getAttributeGroups()) {
			 sGroup = org.nuclos.common2.StringUtils.emptyIfNull(sGroup);
			 Long groupId = getAttributeGroupId(sGroup);

			 WYSIWYGLayoutEditorPanel panelGroup = (WYSIWYGLayoutEditorPanel)ComponentProcessors.getInstance().createComponent(LayoutMLConstants.ELEMENT_PANEL, LayoutMLConstants.ELEMENT_LABEL, new WYSIWYGMetaInformation(), sGroup);
			 TranslationMap tm = new TranslationMap();
			 tm.put("de", sGroup);
			 tm.put("en", sGroup);
			 Border border = new TitledBorderWithTranslations(sGroup, tm);

			 PropertyValueBorder prop = new PropertyValueBorder();
			 prop.setValue(border);


			 panelGroup.setProperty(WYSIWYGComponent.PROPERTY_BORDER, prop, Border.class);

			 createAttributeGroupPanel(panelGroup, groupId, mpFields);
		  	 LayoutCell cell = new LayoutCell();
			 cell.setCellX(1);
			 cell.setCell2X(2);
			 cell.setCellY(counter);
			 cell.setCell2Y(counter);
			 int iHeight = I_CELL_HEIGHT;
			 iHeight *= getAttributeGroupCount(mpFields, groupId);

			 cell.setCellHeight(iHeight + I_CELL_HEIGHT_EXT);
			 cell.setCellWidth(maxWidth);
			 panelGroup.setPreferredSize(new Dimension(I_CELL_WIDTH, iHeight*2));
			 util.addRow(cell);
			 if (counter == 1) {
			 	util.addCol(cell);
			 }

			 util.insertComponentTo(panelGroup, cell);
			 counter++;

			 cell = new LayoutCell();
			 cell.setCellX(counter);
			 cell.setCellY(counter);
			 cell.setCellHeight(InterfaceGuidelines.MARGIN_BETWEEN);
			 cell.setCellWidth(I_CELL_WIDTH);
			 util.addRow(cell);

			 counter++;
		 }
		return counter;
	}

	private Map<String, String> getSubformEntities(NuclosEntityWizardStaticModel wizardModel) {
		Map<String, String> mpSubEntities = new HashMap<String, String>();
		final String sEntity = wizardModel.getEntityName();
		for(EntityMetaDataVO voEntity : MetaDataClientProvider.getInstance().getAllEntities()) {
			if(voEntity.equals(sEntity))
				continue;
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(voEntity.getEntity()).values()) {
				if(voField.getForeignEntity() != null && voField.getForeignEntity().equals(sEntity) && voField.getForeignEntityField() == null) {
					mpSubEntities.put(voEntity.getEntity(), voField.getField());
					break;
				}
			}
		}
		return mpSubEntities;
	}

	private void createAttributeGroupPanel(WYSIWYGLayoutEditorPanel panel, Long lAttributeGroup, SortedMap<Integer, EntityFieldMetaDataVO> mpFields) {
		try {
			TableLayoutUtil util = panel.getTableLayoutUtil();
			int counter = 1;
			int maxWidth = I_MAX_WIDTH_GROUP*2;

			SortedMap<Integer, EntityFieldMetaDataVO> mpFields1 = new TreeMap<Integer, EntityFieldMetaDataVO>();

			for(Integer iKey : mpFields.keySet()) {
				EntityFieldMetaDataVO field = mpFields.get(iKey);
				if(isSystemField(field))
					continue;
				if(field.getForeignEntity() != null && field.getForeignEntityField() == null)
					continue;
				if(lAttributeGroup.longValue() == -1L && field.getFieldGroupId() == null) {

				}
				else if(!lAttributeGroup.equals(field.getFieldGroupId()))
					continue;
				mpFields1.put(iKey, mpFields.get(iKey));
			}

			createComponentForLayout(panel, util, mpFields1, maxWidth, counter);

		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	private Set<String> getAttributeGroups() {
		Set<String> set = new HashSet<String>();

		for(Integer key : treeModel.getIndexMap().keySet()) {
			set.add(treeModel.getIndexMap().get(key));
		}

		return set;
	}

	private int getAttributeGroupCount(SortedMap<Integer, EntityFieldMetaDataVO> mpFields, Long lGroup) {
		int i = 0;
		for(EntityFieldMetaDataVO field : mpFields.values()) {
			if(lGroup.equals(field.getFieldGroupId()) || (lGroup.longValue() == -1L && field.getFieldGroupId() == null)) {
				i++;
				String controltype = getCollectableComponentType(field);
				if(controltype.equals(LayoutMLConstants.CONTROLTYPE_TEXTAREA))
					i++;
			}
		}

		return i;
	}

	private boolean isSystemField(EntityFieldMetaDataVO field) {
		 if(field.getField().equals(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.PROCESS.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATE.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATENUMBER.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.STATEICON.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.LOGGICALDELETED.getMetaData().getField()))
			 return true;
		 if(field.getField().equals(NuclosEOField.ORIGIN.getMetaData().getField()))
			 return true;
		 return false;
	}
	private boolean isBinaryField(EntityFieldMetaDataVO field) {
		if(field.getDataType().equals("[B"))
			return true;
		return false;
	}

	private String getCollectableComponentType(EntityFieldMetaDataVO f) {

		if(f.getForeignEntity() != null && !f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_COMBOBOX;

		if(f.getForeignEntity() != null && f.isSearchable())
			return LayoutMLConstants.CONTROLTYPE_LISTOFVALUES;

		if(Date.class.getName().equals(f.getDataType()) || InternalTimestamp.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_DATECHOOSER;

		if(Boolean.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_CHECKBOX;

		if(NuclosImage.class.getName().equals(f.getDataType()))
			return LayoutMLConstants.CONTROLTYPE_IMAGE;

		if(f.getScale() != null && f.getScale().intValue() > 255 || f.getDataType().equals("java.lang.String") && f.getScale() == null)
			return LayoutMLConstants.CONTROLTYPE_TEXTAREA;

		return LayoutMLConstants.CONTROLTYPE_TEXTFIELD;

	}

	private List<EntityFieldMetaDataVO> sortEditFields(List<EntityFieldMetaDataVO> lstFields) {
		List<EntityFieldMetaDataVO> lstNewList = new ArrayList<EntityFieldMetaDataVO>();

		for(int i = 0; i < this.sEditFields.length; i++) {
			for(EntityFieldMetaDataVO field : lstFields) {
				String sDBField = field.getDbColumn();
				if(sDBField.equalsIgnoreCase(this.sEditFields[i])){
					lstNewList.add(field);
					break;
				}
			}
		}

		return lstNewList;
	}

	private boolean isEditField(EntityFieldMetaDataVO f) {
		for(String s : this.sEditFields) {
			if(s.equals(f.getDbColumn())) {
				return true;
			}
		}
		return false;
	}

	private EntityFieldMetaDataVO buildMasterDataField(Attribute attr, EntityMetaDataVO parentVO, EntityMetaDataVO metaVOOld) {

		EntityFieldMetaDataVO metaFieldVO = new EntityFieldMetaDataVO();

		if(attr.getId() != null) {

			EntityFieldMetaDataVO v = null;
			if(attr.hasInternalNameChanged()) {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getOldInternalName());
			}
			else {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getInternalName());
			}
			metaFieldVO.setId(attr.getId());
			metaFieldVO.setChangedAt(v.getChangedAt());
			metaFieldVO.setChangedBy(v.getChangedBy());
			metaFieldVO.setCreatedAt(v.getCreatedAt());
			metaFieldVO.setCreatedBy(v.getCreatedBy());
			metaFieldVO.setVersion(v.getVersion());
			metaFieldVO.flagUpdate();

			if(!v.getField().equals(attr.getInternalName())) {
				mpFieldNameChanged.put(v.getField(), attr.getInternalName());
			}
		}
		else {
			metaFieldVO.flagNew();
		}

		if(attr.getId() != null && attr.isMandatory()) {
			EntityFieldMetaDataVO v = null;
			if(attr.hasInternalNameChanged()) {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getOldInternalName());
			}
			else {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getInternalName());
			}

		}

		if(attr.getId() != null && attr.isDistinct()) {
			EntityFieldMetaDataVO v = null;
			if(attr.hasInternalNameChanged()) {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getOldInternalName());
			}
			else {
				v = MetaDataClientProvider.getInstance().getEntityField(parentVO.getEntity(), attr.getInternalName());
			}
			if(attr.isDistinct() != v.isUnique()) {
				boolean blnAllowed = MetaDataDelegate.getInstance().isChangeDatabaseColumnToUniqueAllowed(metaVOOld.getEntity(), attr.getInternalName());
				if(!blnAllowed) {
					model.setResultText(getMessage("wizard.step.entitysqllayout.6", "Das Feld {0} kann nicht auf ein eindeutiges Feld umgestellt werden.", attr.getLabel())+ "\n");
					throw new CommonFatalException();
				}
			}
		}

		metaFieldVO.setEntityId(parentVO.getId());
		metaFieldVO.setForeignEntityField(null);
		metaFieldVO.setForeignEntity(null);
		metaFieldVO.setUnique(attr.isDistinct());
		metaFieldVO.setIndexed(attr.isIndexed());
		metaFieldVO.setLogBookTracking(attr.isLogBook());

		metaFieldVO.setFormatOutput(attr.getOutputFormat());
		metaFieldVO.setDataType(attr.getDatatyp().getJavaType());
		metaFieldVO.setPrecision(attr.getDatatyp().getPrecision());
		metaFieldVO.setScale(attr.getDatatyp().getScale());
		metaFieldVO.setNullable(!attr.isMandatory());
		metaFieldVO.setModifiable(attr.isModifiable());
		metaFieldVO.setFormatInput(attr.getInputValidation());
		if(attr.getCalcFunction() != null && attr.getCalcFunction().length() > 0) {
			metaFieldVO.setCalcFunction(attr.getCalcFunction());
			metaFieldVO.setReadonly(Boolean.TRUE);
		}
		else {
			metaFieldVO.setCalcFunction(null);
			metaFieldVO.setReadonly(parentVO.isVirtual() ? attr.isReadonly() : Boolean.FALSE);
		}
		metaFieldVO.setInsertable(false);


		metaFieldVO.setSearchable(attr.isValueListProvider());


		metaFieldVO.setShowMnemonic(false);

		metaFieldVO.setLocaleResourceIdForLabel(attr.getLabelResource());
		metaFieldVO.setLocaleResourceIdForDescription(attr.getDescriptionResource());

		for(MasterDataVO voAttributeGroup : MasterDataDelegate.getInstance().getMasterData(NuclosEntity.ENTITYFIELDGROUP.getEntityName())) {
			if(voAttributeGroup.getField("name").equals(attr.getAttributeGroup())) {
				metaFieldVO.setFieldGroupId(new Long(voAttributeGroup.getIntId()));
				break;
			}
		}


		String sDbFieldName = attr.getDbName();
		attr.setDbName(sDbFieldName.replaceAll("[^A-Za-z0-9_]", "_"));

		metaFieldVO.setDbColumn(attr.getDbName());
		metaFieldVO.setField(attr.getInternalName());
		if(attr.getIdDefaultValue() != null && attr.getIdDefaultValue().getId() != null){
			metaFieldVO.setDefaultForeignId(new Long(attr.getIdDefaultValue().getId()));
			metaFieldVO.setDefaultValue(attr.getIdDefaultValue().getValue());
		}
		else {
			if(attr.getMetaVO() != null && attr.getField() != null) {
				metaFieldVO.setDefaultForeignId(null);
				metaFieldVO.setDefaultValue(null);
			}
			else {
				metaFieldVO.setDefaultValue(attr.getDefaultValue());
			}

		}
		metaFieldVO.setSearchable(attr.isValueListProvider());
		if(attr.getField() != null && attr.getField().length() < 1)
			attr.setField(null);

		if(attr.getMetaVO() != null && attr.getField() != null) {
			metaFieldVO.setModifiable(false);
			metaFieldVO.setForeignEntity(attr.getMetaVO().getEntity());
			metaFieldVO.setForeignEntityField(attr.getField());
			if(!attr.getDbName().startsWith("STRVALUE_")) {
				metaFieldVO.setDbColumn("STRVALUE_"+ attr.getDbName().replaceFirst("INTID_", ""));
			}
		}
		else if (attr.getMetaVO() != null && attr.getField() == null) {
			metaFieldVO.setForeignEntity(attr.getMetaVO().getEntity());
			if(!attr.getDbName().startsWith("INTID_")) {
				metaFieldVO.setDbColumn("INTID_"+ attr.getDbName().replaceFirst("STRVALUE_", ""));
			}
			metaFieldVO.setForeignEntityField(null);
			metaFieldVO.setModifiable(false);
		}
		else {
			metaFieldVO.setModifiable(true);
		}

		setDefaultMandatoryValue(attr, metaFieldVO);

		return metaFieldVO;
	}

	private void setDefaultMandatoryValue(Attribute attr, EntityFieldMetaDataVO metaFieldVO) {
		if(attr.getMandatoryValue() == null)
			return;
		String sJavaType = attr.getDatatyp().getJavaType();
		if(sJavaType.equals("java.lang.Integer")) {
			metaFieldVO.setDefaultMandatory(attr.getMandatoryValue().toString());
		}
		else if(sJavaType.equals("java.lang.Double")) {
			metaFieldVO.setDefaultMandatory(attr.getMandatoryValue().toString());
		}
		else if(sJavaType.equals("java.util.Date")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
			String sDate = sdf.format((Date)(attr.getMandatoryValue()));
			metaFieldVO.setDefaultMandatory(sDate);
		}
		else if(sJavaType.equals("java.lang.Boolean")){
			metaFieldVO.setDefaultMandatory(attr.getMandatoryValue().toString());
		}
		else {
			metaFieldVO.setDefaultMandatory(attr.getMandatoryValue().toString());
		}


	}

	protected String replace(String str) {
		str = StringUtils.replace(str, "\u00e4", "ae");
		str = StringUtils.replace(str, "\u00f6", "oe");
		str = StringUtils.replace(str, "\u00fc", "ue");
		str = StringUtils.replace(str, "\u00c4", "Ae");
		str = StringUtils.replace(str, "\u00d6", "Oe");
		str = StringUtils.replace(str, "\u00dc", "Ue");
		str = StringUtils.replace(str, "\u00df", "ss");
		str = str.replaceAll("[^\\w]", "");
		return str;
	}

	private String modifyLayout(String layoutML, String oldField, String newField, boolean removeOldField) {
		String sLayout = null;

		WYSIWYGLayoutControllingPanel ctrlPanel = new WYSIWYGLayoutControllingPanel(new WYSIWYGMetaInformation());
		try {
			ctrlPanel.setLayoutML(layoutML);

			for(WYSIWYGComponent comp : ctrlPanel.getAllComponents()) {
				String sFieldName = (String)comp.getProperties().getProperty(PROPERTY_LABELS.NAME).getValue();
				if(oldField.equals(sFieldName)) {
					if(removeOldField)
						ctrlPanel.getEditorPanel().getTableLayoutUtil().removeComponentFromLayout(comp);
					else {
						comp.setProperty(WYSIWYGCollectableComponent.PROPERTY_NAME, new PropertyValueString(newField), String.class);
					}
				}
			}
			sLayout = ctrlPanel.getLayoutML();
		}
		catch(CommonBusinessException e) {
			// don't modify layout
		}
		catch(SAXException e) {
		   // don't modify layout
		}

		return sLayout;
	}


	private boolean hasEntityLayout() {
		return (MasterDataDelegate.getInstance().getLayoutId(model.getEntityName(), false) != null) ? true : false;
	}

	private void buildValueListIfNeeded() {

		List<EntityFieldMetaDataTO> lstFields = new ArrayList<EntityFieldMetaDataTO>();

		for(Attribute attr : getModel().getAttributeModel().getAttributes()) {

			if(!attr.isValueList())
				continue;

			if(attr.isValueListNew()) {
				createValueListEntity(lstFields, attr);
			}

            for(ValueList valueList: attr.getValueList()) {
            	Map<String, Object> mpFields = new HashMap<String, Object>();
            	mpFields.put("value", valueList.getLabel());
            	mpFields.put("mnemonic", valueList.getMnemonic());
            	mpFields.put("description", valueList.getDescription());
            	mpFields.put("validFrom", valueList.getValidFrom());
            	mpFields.put("validUntil", valueList.getValidUntil());

            	try {
            		if(valueList.getId() != null) {
            			MasterDataVO vo = new MasterDataVO(valueList.getId().intValue(), new Date(), "nuclos", new Date(), "nuclos", valueList.getVersionId(), mpFields);
            			MasterDataDelegate.getInstance().update(attr.getValueListName(), vo, null);
            		}
            		else {
            			MasterDataVO vo = new MasterDataVO(null, new Date(), "nuclos", new Date(), "nuclos", 1, mpFields);
            			MasterDataDelegate.getInstance().create(attr.getValueListName(), vo, null);
            		}
                }
                catch(CommonBusinessException e) {
	                e.printStackTrace();
                }
            }


		}
	}

	private void createValueListEntity(List<EntityFieldMetaDataTO> lstFields, Attribute attr) {

	    EntityMetaDataTO toEntity = new EntityMetaDataTO();
	    EntityMetaDataVO voEntity = new EntityMetaDataVO();
	    voEntity.flagNew();
	    voEntity.setEntity(attr.getValueListName());
	    voEntity.setDbEntity("V_EO_"+ attr.getValueListName());
	    voEntity.setSearchable(true);
	    voEntity.setCacheable(false);
	    voEntity.setEditable(true);
	    voEntity.setImportExport(true);
	    voEntity.setTreeRelation(false);
	    voEntity.setTreeGroup(false);
	    voEntity.setStateModel(false);
	    voEntity.setDynamic(false);
	    voEntity.setLogBookTracking(false);
	    voEntity.setFieldValueEntity(Boolean.TRUE);
	    toEntity.setEntityMetaVO(voEntity);
	    toEntity.setTranslation(new ArrayList<TranslationVO>());
	    toEntity.setTreeView(new ArrayList<EntityTreeViewVO>());

	    attr.setMetaVO(voEntity);
	    attr.setField("${value}");

	    EntityFieldMetaDataVO voField = new EntityFieldMetaDataVO();
	    buildValueListField("value","STRVALUE","java.lang.String", 255, attr, voField);
	    EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
	    toField.setEntityFieldMeta(voField);
	    toField.setTranslation(new ArrayList<TranslationVO>());
	    lstFields.add(toField);

	    voField = new EntityFieldMetaDataVO();
	    buildValueListField("mnemonic","STRMNEMONIC","java.lang.String", 255, attr, voField);
	    toField = new EntityFieldMetaDataTO();
	    toField.setEntityFieldMeta(voField);
	    toField.setTranslation(new ArrayList<TranslationVO>());
	    lstFields.add(toField);

	    voField = new EntityFieldMetaDataVO();
	    buildValueListField("description","STRDESCRIPTION","java.lang.String", 255, attr, voField);
	    toField = new EntityFieldMetaDataTO();
	    toField.setEntityFieldMeta(voField);
	    toField.setTranslation(new ArrayList<TranslationVO>());
	    lstFields.add(toField);

	    voField = new EntityFieldMetaDataVO();
	    buildValueListField("validFrom","DATVALIDFROM","java.util.Date", null, attr, voField);
	    toField = new EntityFieldMetaDataTO();
	    toField.setEntityFieldMeta(voField);
	    toField.setTranslation(new ArrayList<TranslationVO>());
	    lstFields.add(toField);

	    voField = new EntityFieldMetaDataVO();
	    buildValueListField("validUntil","DATVALIDUNTIL","java.util.Date", null, attr, voField);
	    toField = new EntityFieldMetaDataTO();
	    toField.setEntityFieldMeta(voField);
	    toField.setTranslation(new ArrayList<TranslationVO>());
	    lstFields.add(toField);

	    try {
	        String sResult = MetaDataDelegate.getInstance().createOrModifyEntity(null, toEntity, null, lstFields, true, null, null);
	    }
	    catch(NuclosBusinessException e) {
	        e.printStackTrace();
	    }
    }

	private void buildValueListField(String fieldname, String dbField, String javaType, Integer iScale, Attribute attr, EntityFieldMetaDataVO voField) {
	    voField.flagNew();
	    voField.setUnique(false);
	    voField.setLogBookTracking(false);
	    voField.setModifiable(false);
	    voField.setSearchable(false);
	    voField.setInsertable(false);
	    voField.setUnique(false);
	    voField.setShowMnemonic(false);
	    voField.setNullable(true);
	    voField.setReadonly(false);

	    voField.setDataType(javaType);
	    voField.setPrecision(null);
	    voField.setScale(iScale);
	    voField.setField(fieldname);
	    voField.setDbColumn(dbField);
    }

	class MyTreeCellRenderer extends DefaultTreeCellRenderer {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Icon getLeafIcon() {
			return Icons.getInstance().getIconStateNewNote();
		}

		@Override
		public Icon getClosedIcon() {
			return Icons.getInstance().getIconModule();
		}

		@Override
		public Icon getDefaultClosedIcon() {
			return Icons.getInstance().getIconModule();
		}

		@Override
		public Icon getDefaultOpenIcon() {
			return Icons.getInstance().getIconModule();
		}

		@Override
		public Icon getOpenIcon() {
			return Icons.getInstance().getIconModule();
		}

	}

	class MyTreeModel implements TreeModel {

		String root = "root";
		Map<String, List<Attribute>> mpGroup;
		Map<Integer, String> mpIndex;
		Collection<TreeModelListener> colListener;

		MyTreeModel() {
			mpGroup = new HashMap<String, List<Attribute>>();
			mpIndex = new HashMap<Integer, String>();
		}

		MyTreeModel(Map<String, List<Attribute>> mp) {
			mpGroup = mp;
			initIndexMap();
			expandWholeTree();
		}

		public Map<Integer, String> getIndexMap() {
			return mpIndex;
		}

		public void groupUpIfPossible(String group) {
			int index = getIndexOfGroup(group);
			if(index == -1 || index == 0)
				return;

			String obj1 = mpIndex.remove(index);
			String obj2 = mpIndex.remove(index-1);
			mpIndex.put(index , obj2);
			mpIndex.put(index-1, obj1);

		}

		public void groupDownIfPossible(String group) {
			int index = getIndexOfGroup(group);
			if(index == -1 || index == mpIndex.size()-1)
				return;

			String obj1 = mpIndex.remove(index);
			String obj2 = mpIndex.remove(index+1);
			mpIndex.put(index , obj2);
			mpIndex.put(index+1, obj1);
		}

		private Integer getIndexOfGroup(String group) {
			for(Integer index : mpIndex.keySet()) {
				if(mpIndex.get(index).equals(group))
					return index;
			}
			return -1;
		}

		public List<Attribute> getAttributeGroupList(String group) {
			return mpGroup.get(group);
		}

		private void initIndexMap() {
			mpIndex = new HashMap<Integer, String>();
			int counter = 0;
			for(String key : mpGroup.keySet()) {
				mpIndex.put(counter++, key);
			}
		}

		@Override
		public void addTreeModelListener(TreeModelListener l) {
			if(colListener == null)
				colListener = Collections.synchronizedList(new ArrayList<TreeModelListener>());
			colListener.add(l);
		}

		@Override
		public Object getChild(Object parent, int index) {
			if(mpGroup.get(parent) != null) {
				try {
					return mpGroup.get(parent).get(index);
				}
				catch(Exception e) {
					return null;
				}
			}
			if(parent instanceof String) {
				String s = (String)parent;
				if("root".equals(s)) {
					try {
						String group = mpIndex.get(index);
						return group;
					}
					catch(Exception e) {
						return null;
					}
				}
			}

			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if(parent instanceof String) {
				String sParent =(String)parent;
				if("root".equals(sParent))
					return mpGroup.size();
				if(mpGroup.get(parent) != null)
					return mpGroup.get(parent).size();
			}

			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if(child instanceof Attribute) {
				String sParent = (String)parent;
				return mpGroup.get(sParent).indexOf(child);
			}
			if(child instanceof String) {
				String group = (String)child;
				if("root".equals(group))
					return 0;

				for(Integer index : mpIndex.keySet()) {
					if(mpIndex.get(index).equals(group))
						return index;
				}
			}
			return 0;
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public boolean isLeaf(Object node) {
			if(node instanceof Attribute)
				return true;
			else
				return false;
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
			if(colListener == null)
				return;
			colListener.remove(l);

		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
			System.out.println(newValue);
		}

		public void setModelData(Map<String, List<Attribute>> mp) {
			this.mpGroup = mp;
			initIndexMap();
			fireTreeModelChanged();
		}

		public void fireTreeModelChanged() {
			TreePath pathSelected = treeAttributeOrder.getSelectionPath();
			TreePath path = pathSelected.getParentPath();

			for(TreeModelListener l : colListener) {
				l.treeStructureChanged(new TreeModelEvent(btUp, path));
			}
			treeAttributeOrder.setSelectionPath(pathSelected);
			expandWholeTree();
		}

		private void expandWholeTree() {
			int row = 0;
		    while (row < treeAttributeOrder.getRowCount()) {
		    	treeAttributeOrder.expandRow(row);
		    	row++;
		      }
		}

	}


}
