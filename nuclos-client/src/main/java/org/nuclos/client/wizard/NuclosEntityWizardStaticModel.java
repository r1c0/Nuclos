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
package org.nuclos.client.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.wizard.model.Attribute;
import org.nuclos.client.wizard.model.EntityAttributeTableModel;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeInputStep;
import org.nuclos.client.wizard.steps.NuclosEntityFinalStep;
import org.nuclos.client.wizard.steps.NuclosEntityMenuStep;
import org.nuclos.client.wizard.steps.NuclosEntityOptionStep;
import org.nuclos.client.wizard.steps.NuclosEntityProcessStep;
import org.nuclos.client.wizard.util.ModifierMap;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.resource.valueobject.ResourceVO;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardStep;
import org.pietschy.wizard.models.StaticModel;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/

public class NuclosEntityWizardStaticModel extends StaticModel {

	String entityName;
	String modifiedEntityName;
	String labelSingular;
	String nodeLabel;
	String nodeTooltip;
	String menuPath;
	String strTableName;
	String strMultiEditEquation;
	String systemIdPrefix;
	EntityAttributeTableModel attributeModel;
	List<MasterDataVO> lstUserRights;
	List<TranslationVO> lstTranslation;
	List<EntityTreeViewVO> lstTreeView;
	boolean blnLogbook;
	boolean blnSearchable;
	boolean blnEditable;
	boolean blnImExport;
	boolean blnShowRelation;
	boolean blnShowGroups;
	boolean blnStateModel;
	boolean blnCachable;
	ResourceVO icon;
	Integer iResourceId;
	String sResourceName;
	String sNuclosResourceName;
	boolean blnCreateSql;
	boolean blnExecuteSql;
	boolean blnCreateLayout;
	Integer iModifier;
	String sAccelerator;

	String labelSingularRes;
	String menuPathRes;
	String nodeLabelRes;
	String nodeTooltipRes;
	String documentPath;
	String reportFilename;
	String virtualentity;

	Collection<EntityObjectVO> processes;
	Collection<EntityObjectVO> entityMenus;

	boolean blnImportTable;
	String jdbcUrl;
	String externalUser;
	String externalPassword;
	String externalTable;

	boolean blnEditMode;
	boolean blnAdvancedMode;

	boolean blnHasRows;

	//DatabaseStructureChangeResultVO resultVO;
	String resultText;

	MainFrameTab ifrm;

	NuclosEntityWizard wizard;

	public NuclosEntityWizardStaticModel() {
		lstUserRights = new ArrayList<MasterDataVO>();
		lstTranslation = new ArrayList<TranslationVO>();
		lstTreeView = new ArrayList<EntityTreeViewVO>();
		resultText = new String();
	}

	public void setWizard(NuclosEntityWizard wizard) {
		this.wizard = wizard;
	}

	public void cancelWizard() {
		this.wizard.getCancelAction().actionPerformed(null);
	}

	public void setParentFrame(MainFrameTab frame) {
		ifrm = frame;
	}

	public MainFrameTab getParentFrame() {
		return ifrm;
	}

	public void setEditMode(boolean editMode) {
		this.blnEditMode = editMode;
	}

	public boolean isEditMode() {
		return blnEditMode;
	}

	public void setHasRows(boolean hasRows) {
		this.blnHasRows = hasRows;
	}

	public boolean hasRows() {
		return this.blnHasRows;
	}

	public String getTableOrViewName() {
		if(strTableName != null && !strTableName.startsWith("V_"))
			strTableName = "V_" + strTableName;
		if(strTableName != null && strTableName.length() > 30){
			strTableName.replaceAll(" " ,"").toUpperCase();
			if(strTableName.length() > 30)
				return strTableName.substring(0,30);
			else {
				return strTableName;
			}
		}
		else if (strTableName != null && strTableName.length() < 1) {
			String sTable = "V_EO_" + entityName.replaceAll(" " ,"").toUpperCase();
			return sTable;
		}
		else if(strTableName != null) {
			return strTableName;
		}
		else {
			String sTable = "V_EO_" + entityName.replaceAll(" " ,"").toUpperCase();
			if(sTable.length() > 30) {
				sTable = sTable.substring(0,30);
			}
			return sTable;
		}
	}

	public void setTableOrViewName(String sTableName) {
		strTableName = sTableName;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getModifiedEntityName() {
		return modifiedEntityName;
	}

	public void setModifiedEntityName(String entityName) {
		this.modifiedEntityName = entityName;
	}

	public String getLabelSingular() {
		return labelSingular;
	}

	public void setLabelSingular(String labelSingular) {
		this.labelSingular = labelSingular;
	}

	public EntityAttributeTableModel getAttributeModel() {
		if(attributeModel == null) {
			attributeModel = new EntityAttributeTableModel();
		}
		return attributeModel;
	}

	public void setAttributeModel(
		EntityAttributeTableModel attributeModel) {
		this.attributeModel = attributeModel;
	}

	public boolean isLogbook() {
		return blnLogbook;
	}

	public void setLogbook(boolean blnLogbook) {
		this.blnLogbook = blnLogbook;
	}

	public boolean isSearchable() {
		return blnSearchable;
	}

	public void setSearchable(boolean blnSearchable) {
		this.blnSearchable = blnSearchable;
	}

	public boolean isEditable() {
		return blnEditable;
	}

	public void setEditable(boolean blnEditable) {
		this.blnEditable = blnEditable;
	}

	public boolean isImExportable() {
		return blnImExport;
	}

	public void setImExport(boolean blnImExport) {
		this.blnImExport = blnImExport;
	}

	public boolean isShowRelation() {
		return blnShowRelation;
	}

	public void setShowRelation(boolean blnShowRelation) {
		this.blnShowRelation = blnShowRelation;
	}

	public boolean isShowGroups() {
		return blnShowGroups;
	}

	public void setShowGroups(boolean blnShowGroups) {
		this.blnShowGroups = blnShowGroups;
	}

	public boolean isStateModel() {
		return blnStateModel;
	}

	public void setStateModel(boolean blnStateModel) {
		this.blnStateModel = blnStateModel;
		if (overview != null) {
			overview.setEnabled(NuclosEntityProcessStep.class, blnStateModel);
		}
	}

	public boolean isCachable() {
		return blnCachable;
	}

	public void setCachable(boolean blnCachable) {
		this.blnCachable = blnCachable;
	}

	public String getNodeLabel() {
		return nodeLabel;
	}

	public String getNodeLabelForMasterDataTable() {
		String str = null;
		if(this.nodeLabel != null) {
			str = new String(this.nodeLabel);
		}

		return str;
	}

	public void setNodeLabel(String nodeLabel) {
		this.nodeLabel = nodeLabel;
	}

	public List<String> getFieldsInNodeLabel() {
		List<String> lstFields = new ArrayList<String>();

		String sField = getNodeLabel();
		Pattern referencedEntityPattern = Pattern.compile ("[$][{][\\w\\[\\]]+[}]");
	    Matcher referencedEntityMatcher = referencedEntityPattern.matcher (sField);
	    StringBuffer sb = new StringBuffer();

		while (referencedEntityMatcher.find()) {
		  Object value = referencedEntityMatcher.group().substring(2,referencedEntityMatcher.group().length()-1);

		  String sName = value.toString();
		  referencedEntityMatcher.appendReplacement (sb, sName);
		}

      // complete the transfer to the StringBuffer
      referencedEntityMatcher.appendTail (sb);
      sField = sb.toString();


		String s = StringUtils.replace(StringUtils.replace(getNodeLabel(), "]", " "), "[", " ");
		if(s != null) {
			StringTokenizer st = new StringTokenizer(s, " ");
			while(st.hasMoreTokens()) {
				lstFields.add(st.nextToken());
			}
		}
		return lstFields;
	}

	public List<String> getFieldsInNodeDirectory() {
		List<String> lstFields = new ArrayList<String>();

		String s = StringUtils.replace(StringUtils.replace(getDocumentPath(), "]", " "), "[", " ");
		if(s != null) {
		StringTokenizer st = new StringTokenizer(s, " ");
			while(st.hasMoreTokens()) {
				lstFields.add(st.nextToken());
			}
		}
		return lstFields;
	}

	public List<String> getFieldsInNodeReportName() {
		List<String> lstFields = new ArrayList<String>();

		String s = StringUtils.replace(StringUtils.replace(getReportFilename(), "]", " "), "[", " ");
		if(s != null) {
		StringTokenizer st = new StringTokenizer(s, " ");
			while(st.hasMoreTokens()) {
				lstFields.add(st.nextToken());
			}
		}
		return lstFields;
	}

	public List<String> getFieldsInNodeTooltip() {
		List<String> lstFields = new ArrayList<String>();

		String s = StringUtils.replace(StringUtils.replace(getNodeTooltip(), "]", " "), "[", " ");
		if(s != null) {
			StringTokenizer st = new StringTokenizer(s, " ");
			while(st.hasMoreTokens()) {
				lstFields.add(st.nextToken());
			}
		}
		return lstFields;
	}

	public List<String> getFieldsInMultiEditEquation() {
		List<String> lstFields = new ArrayList<String>();
		String s = getMultiEditEquation();
		if(s != null) {
			StringTokenizer st = new StringTokenizer(s, " ");
			while(st.hasMoreTokens()) {
				lstFields.add(st.nextToken());
			}
		}
		return lstFields;
	}

	public String getNodeTooltip() {
		return nodeTooltip;
	}

	public String getNodeTooltipForMasterDataTable() {
		String str = null;
		if(this.nodeTooltip != null) {
			str = new String(this.nodeTooltip);
		}


		return str;
	}

	public void setNodeTooltip(String nodeTooltip) {
		this.nodeTooltip = nodeTooltip;
	}

	public boolean isCreateSql() {
		return blnCreateSql;
	}

	public void setCreateSql(boolean blnCreateSql) {
		this.blnCreateSql = blnCreateSql;
	}

	public boolean isExecuteSql() {
		return blnExecuteSql;
	}

	public void setExecuteSql(boolean blnExecuteSql) {
		this.blnExecuteSql = blnExecuteSql;
	}

	public boolean isCreateLayout() {
		return blnCreateLayout;
	}

	public void setCreateLayout(boolean blnCreateLayout) {
		this.blnCreateLayout = blnCreateLayout;
	}

	public ResourceVO getIcon() {
		return icon;
	}

	public void setIcon(ResourceVO icon) {
		this.icon = icon;
	}

	public Integer getResourceId() {
		return iResourceId;
	}

	public void setResourceId(Integer iId) {
		this.iResourceId = iId;
	}

	public String getResourceName() {
		return sResourceName;
	}

	public void setResourceName(String name) {
		this.sResourceName = name;
	}

	public String getNuclosResourceName() {
		return sNuclosResourceName;
	}

	public void setNuclosResourceName(String sNuclosResourceName) {
		this.sNuclosResourceName = sNuclosResourceName;
	}

	public String getMenuPath() {
		return menuPath;
	}

	public void setMenuPath(String menuPath) {
		this.menuPath = menuPath;
	}

	public String getMultiEditEquation() {
		return strMultiEditEquation;
	}

	public void setMultiEditEquation(String multiEditEquation) {
		this.strMultiEditEquation = multiEditEquation;
	}

	public void setMultiEditEquation(Set<String> multiEditEquation) {
		if(multiEditEquation.size() < 1)
			return;
		String str = "";
		for(String s : multiEditEquation) {
			str += (" " + s);
		}
		this.strMultiEditEquation = str;

	}

	public Integer getModifier() {
		return iModifier;
	}

	public String getModifierAsString() {
		String sModifier = null;
		if(iModifier == null)
			return sModifier;
		for(String s : ModifierMap.getModifierMap().keySet()) {
			Integer modifier = ModifierMap.getModifierMap().get(s);
			if(iModifier.equals(modifier)) {
				sModifier = s;
				break;
			}
		}


		return sModifier;
	}

	public void setModifier(Integer iModifier) {
		this.iModifier = iModifier;
	}

	public String getAccelerator() {
		return sAccelerator;
	}

	public void setAccelerator(String sAccelerator) {
		this.sAccelerator = sAccelerator;
	}

	public void setUserRights(List<MasterDataVO> userRights) {
		this.lstUserRights = userRights;
	}

	public List<MasterDataVO> getUserRights() {
		return lstUserRights;
	}

	public void setTranslation(List<TranslationVO> translation) {
		this.lstTranslation = translation;
	}

	public List<TranslationVO> getTranslation() {
		return lstTranslation;
	}

	public boolean isImportTable() {
		return blnImportTable;
	}

	public void setImportTable(boolean blnImportTable) {
		this.blnImportTable = blnImportTable;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getExternalUser() {
		return externalUser;
	}

	public void setExternalUser(String externalUser) {
		this.externalUser = externalUser;
	}

	public String getExternalPassword() {
		return externalPassword;
	}

	public void setExternalPassword(String externalPassword) {
		this.externalPassword = externalPassword;
	}

	public String getExternalTable() {
		return externalTable;
	}

	public void setExternalTable(String externalTable) {
		this.externalTable = externalTable;
	}

	public String getResultText() {
		return resultText;
	}

	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

	public String getSystemIdPrefix() {
		return systemIdPrefix;
	}

	public void setSystemIdPrefix(String systemIdPrefix) {
		this.systemIdPrefix = systemIdPrefix;
	}

	public void resetModel() {
		setEntityName(null);
		setLabelSingular(null);
		setEditable(true);
		setSearchable(true);
		setAttributeModel(null);
		setTableOrViewName(null);
		setMenuPath(null);
		setStateModel(false);
		setSystemIdPrefix(null);
		setCachable(false);
		setDocumentPath(null);
		setImExport(true);
		setIcon(null);
		setModifier(null);
		setAccelerator(null);
		setShowRelation(false);
		setShowGroups(false);
		setLogbook(true);
		setLabelSingular(null);
		setNodeLabel(null);
		setNodeTooltip(null);
		setReportFilename(null);
		setLabelSingularResource(null);
		setMenuPathResource(null);
		setNodeLabelResource(null);
		setNodeTooltipResource(null);


		lstUserRights = new ArrayList<MasterDataVO>();
		lstTranslation = new ArrayList<TranslationVO>();
		lstTreeView = new ArrayList<EntityTreeViewVO>();

		Iterator it = this.stepIterator();
		while(it.hasNext()) {
			Object step =  it.next();
			if(step instanceof NuclosEntityAttributeInputStep) {
				NuclosEntityAttributeInputStep stepToReset = (NuclosEntityAttributeInputStep)step;
				stepToReset.resetStep();
			}
		}

	}

	private NuclosEntityWizardStaticModelOverview overview;

	@Override
	public JComponent getOverviewComponent() {
		if (overview == null) {
			overview = new NuclosEntityWizardStaticModelOverview(this);
			overview.setEnabled(NuclosEntityProcessStep.class, blnStateModel);
			overview.setEnabled(NuclosEntityOptionStep.class, isVirtual());
		}
		return overview;
	}

	public boolean hasReferenzTyp() {
		for(Attribute attr : this.attributeModel.getAttributes()) {
			if(attr.getDatatyp().isRefenceTyp()) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void refreshModelState() {
      super.refreshModelState();
      if(this.getActiveStep() instanceof NuclosEntityFinalStep) {
	      this.setPreviousAvailable(false);
	      this.wizard.enableCancelAction(false);
      }
   }

	public String getLabelSingularResource() {
		return labelSingularRes;
	}

	public void setLabelSingularResource(String labelSingularRes) {
		this.labelSingularRes = labelSingularRes;
	}

	public String getMenuPathResource() {
		return menuPathRes;
	}

	public void setMenuPathResource(String menuPathRes) {
		this.menuPathRes = menuPathRes;
	}

	public String getNodeLabelResource() {
		return nodeLabelRes;
	}

	public void setNodeLabelResource(String nodeLabelRes) {
		this.nodeLabelRes = nodeLabelRes;
	}

	public String getNodeTooltipResource() {
		return nodeTooltipRes;
	}

	public void setNodeTooltipResource(String nodeTooltipRes) {
		this.nodeTooltipRes = nodeTooltipRes;
	}

	public String getDocumentPath() {
		return documentPath;
	}

	public void setDocumentPath(String documentPath) {
		this.documentPath = documentPath;
	}

	public String getReportFilename() {
		return reportFilename;
	}

	public void setReportFilename(String reportFilename) {
		this.reportFilename = reportFilename;
	}

	public List<EntityTreeViewVO> getTreeView() {
		return lstTreeView;
	}

	public void setTreeView(List<EntityTreeViewVO> lstTreeView) {
		this.lstTreeView = lstTreeView;
	}

	public boolean isVirtual() {
		return !StringUtils.isBlank(virtualentity);
	}

	public String getVirtualentity() {
		return virtualentity;
	}

	public void setVirtualentity(String virtualentity) {
		this.virtualentity = virtualentity;
		if (overview != null) {
			overview.setEnabled(NuclosEntityOptionStep.class, org.nuclos.common2.StringUtils.isNullOrEmpty(virtualentity));
		}
	}

	public Collection<EntityObjectVO> getProcesses() {
		return processes;
	}

	public void setProcesses(Collection<EntityObjectVO> processes) {
		this.processes = processes;
	}

	public Collection<EntityObjectVO> getEntityMenus() {
		return entityMenus;
	}

	public void setEntityMenus(Collection<EntityObjectVO> entityMenus) {
		this.entityMenus = entityMenus;
	}

	@Override
	public void nextStep() {
		super.nextStep();
		WizardStep step = this.getActiveStep();
		if(step instanceof NuclosEntityProcessStep && !this.isStateModel()) {
			super.nextStep();
		}
	}

	@Override
	public void previousStep() {
		WizardStep step = this.getActiveStep();
		if (step instanceof NuclosEntityProcessStep || step instanceof NuclosEntityMenuStep) {
			try {
				step.applyState();
			} catch (InvalidStateException e) { }
		}
		if(step instanceof NuclosEntityAttributeInputStep && this.isVirtual()) {
			super.previousStep();
			super.previousStep();
		}
		else if(step instanceof NuclosEntityMenuStep && !this.isStateModel()) {
			super.previousStep();
			super.previousStep();
		}
		else {
			super.previousStep();
		}
   }
}
