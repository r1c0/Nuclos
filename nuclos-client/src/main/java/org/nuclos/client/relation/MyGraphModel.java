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
package org.nuclos.client.relation;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.GenerationCollectController;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.client.wizard.util.NuclosWizardUtils;
import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common.transport.vo.EntityMetaDataTO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

@Configurable
public class MyGraphModel extends mxGraphModel {
	
	private static final Logger LOG = Logger.getLogger(MyGraphModel.class);

	public static String[] labels = TranslationVO.labelsField;
	
	public static String ENTITYSTYLE = "rounded=1";
	public static String SYMBOLCOLOR = "#6482B9";
	public static String DIAMONDARROW = "endArrow=diamond";
	public static String OPENARROW = "endArrow=open";
	public static String OVALARROW = "endArrow=oval";
	public static String EDGESTYLE = "edgeStyle";
	public static String ELBOWCONNECTOR = "mxEdgeStyle.ElbowConnector";
	
	//
	
	private mxGraphComponent graphComponent;
	private EntityRelationshipModelEditPanel editPanel;
	private MainFrame mf; 
	
	boolean blnIgnoreAdd;
	
	Map<EntityFieldMetaDataVO, List<TranslationVO>> mpTransation;
	
	private SpringLocaleDelegate localeDelegate;

	public MyGraphModel() {
	}
	
	public MyGraphModel(mxGraphComponent graphComponent, EntityRelationshipModelEditPanel panel, MainFrame mf) {
		this.graphComponent = graphComponent;
		this.editPanel = panel;
		this.mf = mf;
		mpTransation = new HashMap<EntityFieldMetaDataVO, List<TranslationVO>>();
	}

	public MyGraphModel(Object root) {
		super(root);
		mpTransation = new HashMap<EntityFieldMetaDataVO, List<TranslationVO>>();
	}
	
	public void setGraphComponent(mxGraphComponent graphComponent) {
		this.graphComponent = graphComponent;
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	@Override
	public Object add(Object parent, Object child, int index) {
		
		Object obj = super.add(parent, child, index);
		if(obj instanceof mxCell) {
			mxCell cell = (mxCell)obj;
			if(StringUtils.looksEmpty(cell.getStyle())) {
				JPopupMenu pop = createRelationPopupMenu(cell, false);
				editPanel.setIsPopupShown(true);
				pop.show(graphComponent, graphComponent.getGraphControl().getMousePosition().x, graphComponent.getGraphControl().getMousePosition().y);
			}
			else if (mxConstants.ARROW_DIAMOND.equals(cell.getStyle())) {
				Object cells[] = {cell};
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
			}
			else if (mxConstants.ARROW_OPEN.equals(cell.getStyle())) {
				Object cells[] = {cell};
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
			}
			else if (mxConstants.ARROW_OVAL.equals(cell.getStyle())) {
				Object cells[] = {cell};
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
				mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
			}
			else if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) {
				try {
					if(cell.getValue() != null && cell.getValue() instanceof String) {
						String sEntity = (String)cell.getValue();
						if(sEntity.length() == 0) {
							boolean blnNotSet = true;
							while(blnNotSet) {
								sEntity = JOptionPane.showInputDialog(editPanel, localeDelegate.getMessage(
										"nuclos.entityrelation.editor.20", "Bitte geben Sie den Namen der neuen Entit\u00e4t an!"));
								for(EntityMetaDataVO voMeta : MetaDataDelegate.getInstance().getAllEntities()) {
									if(voMeta.getEntity().equals(sEntity)){
										JOptionPane.showMessageDialog(editPanel, localeDelegate.getMessage(
												"nuclos.entityrelation.editor.19", "Entit\u00e4t schon vorhanden"));
										blnNotSet = true;
										break;
									}
									blnNotSet = false;
								}
							}
							if(sEntity == null) {
								this.remove(cell);								
							}
							if(sEntity.length() == 0)
								this.remove(cell);
							else {
								EntityMetaDataVO voEntity = new EntityMetaDataVO();
								voEntity.setEntity(sEntity);
								voEntity.setDbEntity("V_EO_" + NuclosWizardUtils.replace(sEntity));
								
								voEntity.setEditable(true);
								voEntity.setImportExport(true);
								voEntity.setSearchable(true);
								voEntity.setStateModel(false);
								voEntity.setTreeGroup(false);
								voEntity.setTreeRelation(false);
								voEntity.setLogBookTracking(true);
								voEntity.setCacheable(false);
								voEntity.setFieldValueEntity(false);
								
								EntityMetaDataTO toEntity = new EntityMetaDataTO();
								toEntity.setTreeView(new ArrayList<EntityTreeViewVO>());
								toEntity.setEntityMetaVO(voEntity);		
								List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
								for(LocaleInfo info : LocaleDelegate.getInstance().getAllLocales(false)){
									Map<String, String> mpValues = new HashMap<String, String>();
									mpValues.put(TranslationVO.labelsEntity[0], sEntity);
									mpValues.put(TranslationVO.labelsEntity[2], sEntity);
									TranslationVO vo = new TranslationVO(info.localeId, info.title, info.language, mpValues);
									lstTranslation.add(vo);
								}
								toEntity.setTranslation(lstTranslation);
								
								String sResult = MetaDataDelegate.getInstance().createOrModifyEntity(null, toEntity, null, new ArrayList<EntityFieldMetaDataTO>(), true, null, null);
								
								EntityMetaDataVO voEntityCreated = MetaDataDelegate.getInstance().getEntityByName(sEntity);
								cell.setValue(voEntityCreated);
								
								editPanel.loadReferenz();						
							}							
						}
					}
				}
				catch(Exception e) {
					LOG.warn("add: " + e);
				}
			}
		}
		return obj;
	}

	public JPopupMenu createRelationPopupMenu(final mxCell cell, boolean delete) {
		
		final JPopupMenu pop = new JPopupMenu();
		JMenuItem i1 = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.1", "Bezug zu Stammdaten"));
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editPanel.setIsPopupShown(false);
				if(cell.getTarget() == null || cell.getSource() == null){
					remove(cell);
					return;
				}				
				Object cells[] = {cell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, EDGESTYLE , ELBOWCONNECTOR);
				RelationAttributePanel panel = new RelationAttributePanel(RelationAttributePanel.TYPE_ENTITY);
				mxCell target = (mxCell)cell.getTarget();
				mxCell source = (mxCell)cell.getSource();
				panel.setEntity((EntityMetaDataVO)target.getValue());	
				panel.setEntitySource((EntityMetaDataVO)source.getValue());
				EntityMetaDataVO voSource = (EntityMetaDataVO)source.getValue();
				EntityMetaDataVO voTarget = (EntityMetaDataVO)target.getValue();
				panel.setEntityFields(MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(voSource.getEntity()).values());
				double cellsDialog [][] = {{5, TableLayout.PREFERRED, 5}, {5, TableLayout.PREFERRED,5}};	
				JDialog dia = new JDialog(mf);
				dia.setLayout(new TableLayout(cellsDialog));
				dia.setTitle(localeDelegate.getMessage(
						"nuclos.entityrelation.editor.10",  "Bezug zu Stammdaten bearbeiten"));
				dia.setLocationRelativeTo(editPanel);
				dia.add(panel, "1,1");
				dia.setModal(true);
				panel.setDialog(dia);
				dia.pack();
				dia.setVisible(true);	

				if(panel.getState() == 1) {
					EntityFieldMetaDataVO vo = panel.getField();
					cell.setValue(vo);
					mpTransation.put(vo, panel.getTranslation().getRows());
					
					List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
					
					EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
					toField.setEntityFieldMeta(vo);
					toField.setTranslation(panel.getTranslation().getRows());
					toList.add(toField);
					
					MetaDataDelegate.getInstance().modifyEntityMetaData(voSource, toList);
					editPanel.loadReferenz();
					
				}
				else {
					remove(cell);					
				}
				
			}
		});
		
		final JMenuItem i2 = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.3", "Bezug zu Vorg\u00e4ngen (Unterformularbezug)"));
		i2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editPanel.setIsPopupShown(false);
				if(cell.getTarget() == null || cell.getSource() == null){
					remove(cell);
					return;
				}				
				Object cells[] = {cell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, EDGESTYLE , ELBOWCONNECTOR);
				mxCell target = (mxCell)cell.getTarget();
				mxCell source = (mxCell)cell.getSource();
				EntityMetaDataVO voSource = (EntityMetaDataVO)source.getValue();
				EntityMetaDataVO voTarget = (EntityMetaDataVO)target.getValue();
				String sFieldName = null;
				boolean blnNotSet = true;
				while(blnNotSet) {   
					sFieldName = JOptionPane.showInputDialog(editPanel, localeDelegate.getMessage(
							"nuclos.entityrelation.editor.1","Bitte geben Sie den Namen des Feldes an!"));
					if(sFieldName == null || sFieldName.length() < 1) {
						MyGraphModel.this.remove(cell);					
						return;
					}
					else if(sFieldName != null) {
						blnNotSet = false;
					}
				}
			   EntityFieldMetaDataVO vo = new EntityFieldMetaDataVO();
			   vo.setModifiable(true);
			   vo.setLogBookTracking(false);
			   vo.setReadonly(false);
			   vo.setShowMnemonic(true);
			   vo.setInsertable(true);
			   vo.setSearchable(true);
			   vo.setNullable(false);
			   vo.setUnique(true);
			   vo.setDataType("java.lang.String");
			   
			   List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
			   for(LocaleInfo voLocale : LocaleDelegate.getInstance().getAllLocales(false)) {
					String sLocaleLabel = voLocale.language; 
					Integer iLocaleID = voLocale.localeId;  
					String sCountry = voLocale.title;
					Map<String, String> map = new HashMap<String, String>();
					
					TranslationVO translation = new TranslationVO(iLocaleID, sCountry, sLocaleLabel, map);
					for(String sLabel : labels) {									
						translation.getLabels().put(sLabel, sFieldName);
					}
					lstTranslation.add(translation);
				}
			   
			   vo.setForeignEntity(voTarget.getEntity());
			   vo.setField(sFieldName);
			   vo.setDbColumn("INTID_" + sFieldName);
				   
				   
				cell.setValue(vo);
				
				List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
				
				EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
				toField.setEntityFieldMeta(vo);
				toField.setTranslation(lstTranslation);
				toList.add(toField);
				
				MetaDataDelegate.getInstance().modifyEntityMetaData(voSource, toList);
				editPanel.loadReferenz();
			}
		});
		
		final JMenuItem i4 = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.5", "Arbeitsschritt"));
		i4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editPanel.setIsPopupShown(false);
				if(cell.getTarget() == null || cell.getSource() == null){
					remove(cell);
					return;
				}	
				Object cells[] = {cell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDSIZE, "12");
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, EDGESTYLE , ELBOWCONNECTOR);
				
				try {
					
					mxCell cellSource = (mxCell)cell.getSource();
					mxCell cellTarget = (mxCell)cell.getTarget();
					
					EntityMetaDataVO sourceModule = (EntityMetaDataVO)cellSource.getValue();
					EntityMetaDataVO targetModule = (EntityMetaDataVO)cellTarget.getValue();
					
					String sSourceModule = sourceModule.getEntity();
					String sTargetModule = targetModule.getEntity();
					
					boolean blnFound = false;
					
					for(MasterDataVO voGeneration : MasterDataCache.getInstance().get(NuclosEntity.GENERATION.getEntityName())) {
						String sSource = (String)voGeneration.getField("sourceModule");
						String sTarget = (String)voGeneration.getField("targetModule");
						
						if(org.apache.commons.lang.StringUtils.equals(sSource, sSourceModule) && 
							org.apache.commons.lang.StringUtils.equals(sTarget, sTargetModule)){
							GenerationCollectController gcc = (GenerationCollectController)NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(NuclosEntity.GENERATION.getEntityName(), null);
							gcc.runViewSingleCollectableWithId(voGeneration.getId());
							blnFound = true;
							break;
						}
						
					}					
					if(!blnFound) {
						GenerationCollectController gcc = (GenerationCollectController)NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(NuclosEntity.GENERATION.getEntityName(), null);
						Map<String, Object> mp = new HashMap<String, Object>();
						mp.put("sourceModule", sSourceModule);
						mp.put("sourceModuleId", new Integer(MetaDataClientProvider.getInstance().getEntity(sSourceModule).getId().intValue()));
						mp.put("targetModule", sTargetModule);
						mp.put("targetModuleId", new Integer(MetaDataClientProvider.getInstance().getEntity(sTargetModule).getId().intValue()));
						MasterDataVO vo = new MasterDataVO(null, null, null, null, null, null, mp);
						gcc.runWithNewCollectableWithSomeFields(vo);
					}
				}
				catch(NuclosBusinessException e1) {
					LOG.warn("actionPerformed: " + e1);
				}
				catch(CommonPermissionException e1) {
					LOG.warn("actionPerformed: " + e1);
				}
				catch(CommonFatalException e1) {
					LOG.warn("actionPerformed: " + e1);
				}
				catch(CommonBusinessException e1) {
					LOG.warn("actionPerformed: " + e1);
				}				
			}
		});
		
		JMenuItem i5 = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.12","Verbindung l\u00f6sen"));
		i5.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				model.remove(cell);				
			}
		});
		
		pop.add(i1);
		pop.add(i2);
		//pop.add(i3);
		pop.add(i4);
		if(delete) {
			pop.addSeparator();
			pop.add(i5);
		}
		
		return pop;	
		
	}
	
	protected JPopupMenu createPopupMenuEntity(final mxCell cell, boolean newCell) {
		
		JPopupMenu pop = new JPopupMenu();
		JMenuItem i1 = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.14", "Symbol l\u00f6schen"));
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				model.remove(cell);				
			}
		});
		
		if(!newCell)
			pop.add(i1);
		
		if(cell.getStyle() == null || !(cell.getStyle().indexOf(ENTITYSTYLE) >= 0)) {
			return pop;
		}
		
		JMenuItem iWizard = new JMenuItem(localeDelegate.getMessage(
				"nuclos.entityrelation.editor.15","Wizard \u00f6ffnen"));
		iWizard.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ShowNuclosWizard wizard = new ShowNuclosWizard(false);
				if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
					String sValue = ((EntityMetaDataVO)cell.getValue()).getEntity();
					if(sValue.length() > 0) {
						EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(sValue);
						wizard.setEntityToEdit(vo);
					}
				}
				wizard.showWizard(mf.getHomePane(), mf);
			}
		});
		
		if(!newCell) {
			pop.addSeparator();
			pop.add(iWizard);			
		}
		else {			
			JMenuItem iNew = new JMenuItem(localeDelegate.getMessage(
					"nuclos.entityrelation.editor.16","neue Entit\u00e4t"));
			iNew.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					ShowNuclosWizard wizard = new ShowNuclosWizard(false);
					if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
						String sValue = ((EntityMetaDataVO)cell.getValue()).getEntity();
						if(sValue.length() > 0) {
							try {
								EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(sValue);
								wizard.setEntityToEdit(vo);
							}
							catch(CommonFatalException e1) {
								// do noting here Entity does not exist
								LOG.warn("actionPerformed: " + e1 + "(entity does not exist?)");
							}
						}
					}
					else {
						cell.setValue(localeDelegate.getMessage(
								"nuclos.entityrelation.editor.16","neue Entit\u00e4t"));
						mxGraph graph = graphComponent.getGraph();
						graph.refresh();
					}
					wizard.showWizard(mf.getHomePane(), mf);
				}
			});			
			pop.add(iNew);
		}
		
	
		return pop;	
	}

	public Map<EntityFieldMetaDataVO, List<TranslationVO>> getTranslation() {
		return this.mpTransation;
	}

}
