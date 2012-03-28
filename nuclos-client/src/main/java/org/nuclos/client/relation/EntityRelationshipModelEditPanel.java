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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.GenerationCollectController;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MetaDataDelegate;
import org.nuclos.client.ui.collect.CollectableComponentsProvider;
import org.nuclos.client.ui.collect.DefaultCollectableComponentsProvider;
import org.nuclos.client.ui.collect.component.CollectableTextField;
import org.nuclos.client.wizard.ShowNuclosWizard;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.transport.vo.EntityFieldMetaDataTO;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxModelCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

/**
 * Details edit panel for state model administration. Contains the state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
@Configurable
public class EntityRelationshipModelEditPanel extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(EntityRelationshipModelEditPanel.class);

	public static String[] labels = TranslationVO.labelsField;
	public static String ENTITYSTYLE = "rounded=1";
	public static String DIAMONDARROW = "endArrow=diamond";
	public static String OPENARROW = "endArrow=open";
	public static String OVALARROW = "endArrow=oval";
	public static String EDGESTYLE = "edgeStyle";
	public static String ELBOWCONNECTOR = "mxEdgeStyle.ElbowConnector";
	public static String SYMBOLCOLOR = "#6482B9";
	
	private int xPos; 
	private int yPos;
	
	private JPanel mainPanel;
	
	private JPanel panelHeader;
	
	private CollectableTextField clcttfName = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("name"));
	
	private CollectableTextField clcttfDescription = new CollectableTextField(
		EntityRelationshipModel.clcte.getEntityField("description"));
	
	private mxGraphComponent graphComponent;
	
	private MainFrame mf;
	
	private List<ChangeListener> lstChangeListener;
	
	private List<mxCell> lstRelations;
	
	private Map<EntityMetaDataVO, Set<EntityFieldMetaDataVO>> mpRemoveRelation;
	
	private boolean isPopupShown;
	
	private SpringLocaleDelegate localeDelegate;
	

	public EntityRelationshipModelEditPanel(MainFrame mf) {
		super(new BorderLayout());
		this.mf = mf;
		lstChangeListener = new ArrayList<ChangeListener>();
		lstRelations = new ArrayList<mxCell>();
		mpRemoveRelation = new HashMap<EntityMetaDataVO, Set<EntityFieldMetaDataVO>>();
		if (localeDelegate == null)
			localeDelegate = SpringLocaleDelegate.getInstance();
		init();		
	}
	
	@Autowired
	void setSpringLocaleDelegate(SpringLocaleDelegate cld) {
		this.localeDelegate = cld;
	}
	
	public void setIsPopupShown(boolean shown) {
		this.isPopupShown = shown;
	}
	
	class MyGraph extends mxGraph {
		
		public MyGraph(mxGraphModel model) {
			super(model);
		}
		
		@Override
		public String getToolTipForCell(Object obj) {
			
			mxCell cell = (mxCell)obj;
			
			boolean blnShow = true;
			
			StringBuffer sb = new StringBuffer();
			sb.append("<html><body>");
			
			if(cell.getValue() != null && ENTITYSTYLE.equals(cell.getStyle())) {
				if(cell.getValue() instanceof EntityMetaDataVO) {
					EntityMetaDataVO vo = (EntityMetaDataVO)cell.getValue();
					sb.append(vo.getEntity());
				}
			}
			else if(cell.getStyle() != null) {
				String sStyle = cell.getStyle();
				if(sStyle.indexOf(OPENARROW) >= 0) {
					if(cell.getSource() != null && cell.getTarget() != null) {
						EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
						EntityMetaDataVO voTarget = (EntityMetaDataVO)cell.getTarget().getValue();
						sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.2", "", voSource.getEntity(), voTarget.getEntity()));
					}
					else 
						sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.1", "Bezug zu Stammdaten"));
				}
				else if(sStyle.indexOf(DIAMONDARROW) >= 0) {
					if(cell.getSource() != null && cell.getTarget() != null) {
						EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
						EntityMetaDataVO voTarget = (EntityMetaDataVO)cell.getTarget().getValue();
						sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.4", "", voSource.getEntity(), voTarget.getEntity()));						
					}
					else
					sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.3", "Bezug zu Vorg\u00e4ngen (Unterformularbezug)"));
				}
				else if(sStyle.indexOf(OVALARROW) >= 0) {
					if(cell.getSource() != null && cell.getTarget() != null) {
						EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
						EntityMetaDataVO voTarget = (EntityMetaDataVO)cell.getTarget().getValue();
						sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.6", "", voSource.getEntity(), voTarget.getEntity()));
					}
					else
						sb.append(localeDelegate.getMessage("nuclos.entityrelation.editor.5", ""));
				}
			}
			else {
				blnShow = false;
			}
			sb.append("</body></html>");
			
			return blnShow ? sb.toString() : "";
		}
		
	}
	
	protected void init() {
		mainPanel = new JPanel();
		
		double sizeHeader [][] = {{TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10}, {10, 25,10}};		
		panelHeader = new JPanel();
		panelHeader.setLayout(new TableLayout(sizeHeader));
		clcttfName.setLabelText(localeDelegate.getMessage("nuclos.entityfield.entityrelation.name.label","Name"));
		clcttfName.setToolTipText(localeDelegate.getMessage("nuclos.entityfield.entityrelation.name.description","Name"));
		clcttfName.setColumns(20);
		panelHeader.add(this.clcttfName.getJComponent(), "0,1");
		clcttfDescription.setLabelText(localeDelegate.getMessage("nuclos.entityfield.entityrelation.description.label","Beschreibung"));
		clcttfDescription.setToolTipText(localeDelegate.getMessage("nuclos.entityfield.entityrelation.description.description","Beschreibung"));
		clcttfDescription.setColumns(20);
		panelHeader.add(this.clcttfDescription.getJComponent(), "2,1");
		
		double size [][] = {{5,TableLayout.FILL, 5}, {35,5, TableLayout.FILL, 5}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		mainPanel.setLayout(layout);
		
		mainPanel.add(panelHeader, "1,0");
		MyGraphModel model = new MyGraphModel(graphComponent, this, mf);
		
		mxGraph myGraph = new MyGraph(model);

		mxCodecRegistry.register(new mxModelCodec(model));
		mxCodecRegistry.register(new mxModelCodec(new java.sql.Date(System.currentTimeMillis())));
		mxCodecRegistry.register(new mxModelCodec(new Integer(0)));
		
			
		addEventListener(myGraph);
		
		graphComponent = new mxGraphComponent(myGraph);
		graphComponent.setGridVisible(true);
		graphComponent.getViewport().setOpaque(false);
		graphComponent.setBackground(Color.WHITE);
		graphComponent.setToolTips(true);
		
		graphComponent.setCellEditor(new MyCellEditor(graphComponent));
		
		model.setGraphComponent(graphComponent);
		
		graphComponent.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_DELETE) {
					mxCell cell = (mxCell)graphComponent.getGraph().getSelectionModel().getCell();
					if(cell.getValue() instanceof EntityMetaDataVO) {
						int iEdge = cell.getEdgeCount();
						for(int i = 0; i < iEdge; i++){
							mxCell cellRelation = (mxCell)cell.getEdgeAt(i);
							getGraphModel().remove(cellRelation);
						}
						getGraphModel().remove(cell);				
						fireChangeListenEvent();
					}
					else if(cell.getValue() instanceof EntityFieldMetaDataVO) {
						int opt = JOptionPane.showConfirmDialog(mainPanel, localeDelegate.getMessage(
								"nuclos.entityrelation.editor.7", "M\u00f6chten Sie die Verbindung wirklich l\u00f6sen?"));
						if(opt != 0){
							return;
						}
						mxCell cellSource = (mxCell)cell.getSource();
						if(cellSource != null && cellSource.getValue() instanceof EntityMetaDataVO) {
							EntityMetaDataVO metaSource = (EntityMetaDataVO)cellSource.getValue();
							if(cell.getValue() instanceof EntityFieldMetaDataVO) {
								EntityFieldMetaDataVO voField = (EntityFieldMetaDataVO)cell.getValue();
								voField.flagRemove();
								
								List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
								EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
								toField.setEntityFieldMeta(voField);
								toList.add(toField);
								
								MetaDataDelegate.getInstance().modifyEntityMetaData(metaSource, toList);
								
								if(mpRemoveRelation.containsKey(metaSource)){
									mpRemoveRelation.get(metaSource).add(voField);							
								}
								else {
									Set<EntityFieldMetaDataVO> s = new HashSet<EntityFieldMetaDataVO>();
									s.add(voField);
									mpRemoveRelation.put(metaSource, s);
								}
								
							}
						}
						
						mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
						model.remove(cell);				
						EntityRelationshipModelEditPanel.this.fireChangeListenEvent();
					}
					else if(cell.getValue() != null && cell.getValue() instanceof String){
						String sValue = (String)cell.getValue();
						if(sValue.length() == 0) {
							getGraphModel().remove(cell);
							EntityRelationshipModelEditPanel.this.fireChangeListenEvent();
						}
					}
				}
			}
			
		});
		
		createMouseWheelListener();
		
		createMouseListener();		
		
		//mainPanel.add(graphComponent, "1,2, 4,4");
		mainPanel.add(graphComponent, "1,2");
		
		this.add(mainPanel);
		
	}
	
	mxGraphComponent getGraphComponent() {
		return graphComponent;
	}
	
	public mxGraphModel getGraphModel() {
		return (mxGraphModel)graphComponent.getGraph().getModel();
	}
	
	public void clearRelationModel() {
		try {
			mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
			model.clear();
			fireChangeListenEvent();
		}
		catch(Exception e) {
			LOG.warn("clearRelationModel failed: " + e, e);
		}
	}
	
	public void refresh() {
		graphComponent.repaint();
		graphComponent.getGraph().getView().reload();
	}
	 

	private void createMouseWheelListener() {
		graphComponent.getGraphControl().addMouseWheelListener(new MouseWheelListener() {
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				
				if(e.getModifiers() == InputEvent.CTRL_MASK) {
					if(e.getWheelRotation() <= 0) {						
						graphComponent.zoomIn();
					}
					else {
						if(graphComponent.getGraph().getView().getScale() > 0.2) 
							graphComponent.zoomOut();
					}
				}
			
			}
		});
	}

	private void createMouseListener() {
		graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if(isPopupShown) {
					isPopupShown = false;
					mxCell cell = (mxCell)graphComponent.getGraph().getSelectionModel().getCell();
					getGraphModel().remove(cell);
					return;
				}
				if(SwingUtilities.isRightMouseButton(e)) {
					xPos = e.getX();
					yPos = e.getY();
					Object obj = graphComponent.getCellAt(e.getX(), e.getY());
					
					if(obj instanceof mxCell) {
						mxCell cell = (mxCell)obj;
						if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0 && cell.getValue() instanceof EntityMetaDataVO) {
							JPopupMenu pop = createPopupMenuEntity(cell, false);
							pop.show(e.getComponent(), e.getX(), e.getY());
						}
						else if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) {
							JPopupMenu pop = createPopupMenuEntity(cell, true);
							pop.show(e.getComponent(), e.getX(), e.getY());
						}
						else {
							if(cell.getStyle() != null && cell.getStyle().indexOf("oval") >= 0) {
								JPopupMenu pop = createRelationPopupMenu(cell, true, true);
								pop.show(e.getComponent(), e.getX(), e.getY());
							}
							else {
								JPopupMenu pop = createRelationPopupMenu(cell, true, false);
								pop.show(e.getComponent(), e.getX(), e.getY());
							}
						}
					}
					else {
						JPopupMenu pop = createPopupMenu();
						pop.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				else if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					mxCell cell = (mxCell)graphComponent.getGraph().getSelectionModel().getCell();
					if(cell == null)
						return;
					if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
						EntityMetaDataVO voMeta = (EntityMetaDataVO)cell.getValue();						
						EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(voMeta.getEntity());
						new ShowNuclosWizard.NuclosWizardEditRunnable(false, mf.getHomePane(), vo).run();
					}
					else if(cell.getValue() != null && cell.getValue() instanceof EntityFieldMetaDataVO) {
						if(cell.getStyle() != null && cell.getStyle().indexOf(OPENARROW) >= 0)
							editMasterdataRelation(cell);
						else if(cell.getStyle() != null && cell.getStyle().indexOf(DIAMONDARROW) >= 0) {
							editSubformRelation(cell);
						}
						 
							
					}
					else if(cell.getValue() != null) {
						if(cell.getStyle() != null && cell.getStyle().indexOf(OVALARROW) >= 0) {
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
								LOG.warn("mousePressed failed: " + e1, e1);
							}
							catch(CommonPermissionException e1) { 
								LOG.warn("mousePressed failed: " + e1, e1);
							}
							catch(CommonFatalException e1) { 
								LOG.warn("mousePressed failed: " + e1, e1);
							}
							catch(CommonBusinessException e1) { 
								LOG.warn("mousePressed failed: " + e1, e1);
							}				
						}
					}
				}
			}

		
		});
	}

	private void addEventListener(mxGraph myGraph) {
		myGraph.addListener(mxEvent.ADD_CELLS, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		myGraph.addListener(mxEvent.CELLS_ADDED, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		myGraph.addListener(mxEvent.ADD, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		myGraph.addListener(mxEvent.CONNECT_CELL, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		
		
		myGraph.addListener(mxEvent.CELL_CONNECTED, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				if(sender instanceof MyGraph) {
					MyGraph graph = (MyGraph)sender;
					mxCell cell = (mxCell)graph.getSelectionModel().getCell();					
					if(cell != null && cell.getStyle() != null) {
						if(cell.getStyle().indexOf(OPENARROW) >= 0 || cell.getStyle().indexOf(DIAMONDARROW) >= 0
							|| cell.getStyle().indexOf(OVALARROW) >= 0) {
							if(cell.getValue() != null && cell.getValue() instanceof EntityFieldMetaDataVO) {
								EntityFieldMetaDataVO voField = (EntityFieldMetaDataVO)cell.getValue();
								Boolean blnSource = (Boolean)evt.getProperty("source");
								mxCell cellPrevious = (mxCell)evt.getProperty("previous");
								mxCell cellTerminal = (mxCell)evt.getProperty("terminal");
								if(blnSource){
									if(cellTerminal != null && cellPrevious != null) {
										EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(voField.getEntityId());
										if(!vo.getEntity().equals(voField.getForeignEntity())) {
											cell.setSource(cellPrevious);
										}
									}
									else if(cellTerminal == null) {
										cell.setSource(cellPrevious);
									}
								}
								else {
									if(cellTerminal != null && cellPrevious != null) {
										EntityMetaDataVO vo = (EntityMetaDataVO)cellTerminal.getValue();
										if(!vo.getEntity().equals(voField.getForeignEntity())) {
											cell.setTarget(cellPrevious);
										}
									}
									else if(cellTerminal == null) {
										cell.setTarget(cellPrevious);
									}
								}
							}
							else if(cell.getValue() != null && cell.getValue() instanceof EntityFieldMetaDataVO) {
								
							}
						}
					}
				}
				fireChangeListenEvent();
			}
		});
		
		myGraph.addListener(mxEvent.CELLS_MOVED, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				graphComponent.getGraph().refresh();
				fireChangeListenEvent();
			}
		});
		
		myGraph.addListener(mxEvent.MOVE_CELLS, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				graphComponent.getGraph().refresh();
				fireChangeListenEvent();
			}
		});
		
		myGraph.addListener(mxEvent.CELLS_REMOVED, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();
			}
		});
		
		
	}
	
	protected JPopupMenu createPopupMenu() {
		
		JPopupMenu pop = new JPopupMenu();
		
		JMenuItem i1 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.16", "neue Entit\u00e4t"));
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int x = xPos;				
				int y = yPos;
				
				mxGeometry mxgeo = new mxGeometry(x, y, 100, 80);
				mxgeo.setSourcePoint(new mxPoint(100,100));
				mxgeo.setTargetPoint(new mxPoint(150,150));
				
				mxCell cell = new mxCell("", mxgeo, ENTITYSTYLE);
				cell.setVertex(true);
				mxCell cellRoot = (mxCell)graphComponent.getGraph().getModel().getRoot();
				mxCell cellContainer = (mxCell)cellRoot.getChildAt(0);
				int childcount = cellContainer.getChildCount();
				getGraphModel().add(cellContainer, cell, childcount);
				getGraphComponent().refresh();
				
			}
		});
		
		JMenuItem i3 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.8", "zoom in"));
		i3.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				graphComponent.zoomIn();
			}
		});
		
		JMenuItem i4 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.9", "zoom out"));
		i4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				graphComponent.zoomOut();
			}
		});
		
		pop.add(i1);
		pop.addSeparator();
		pop.add(i3);
		pop.add(i4);
		
		return pop;	
		
	}
	
	
	
	protected JPopupMenu createRelationPopupMenu(final mxCell cell, boolean delete, boolean objectGeneration) {
		
		JPopupMenu pop = new JPopupMenu();
		JMenuItem i1 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.10","Bezug zu Stammdaten bearbeiten"));
		
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
					editMasterdataRelation(cell);
			}
		});
		
		JMenuItem i2 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.11", "Unterfomularbezug bearbeiten"));
		i2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				editSubformRelation(cell);
			}

		});
		
		JMenuItem i4 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.12", "Verbindung l\u00f6sen"));
		i4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				int opt = JOptionPane.showConfirmDialog(mainPanel, localeDelegate.getMessage(
						"nuclos.entityrelation.editor.7", "M\u00f6chten Sie die Verbindung wirklich l\u00f6sen?")); 
				if(opt != 0){
					return;
				}
				mxCell cellSource = (mxCell)cell.getSource();
				if(cellSource != null && cellSource.getValue() instanceof EntityMetaDataVO) {
					EntityMetaDataVO metaSource = (EntityMetaDataVO)cellSource.getValue();
					if(cell.getValue() instanceof EntityFieldMetaDataVO) {
						EntityFieldMetaDataVO voField = (EntityFieldMetaDataVO)cell.getValue();
						voField.flagRemove();
						
						List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
						EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
						toField.setEntityFieldMeta(voField);
						toList.add(toField);
						
						MetaDataDelegate.getInstance().modifyEntityMetaData(metaSource, toList);
						
						if(mpRemoveRelation.containsKey(metaSource)){
							mpRemoveRelation.get(metaSource).add(voField);							
						}
						else {
							Set<EntityFieldMetaDataVO> s = new HashSet<EntityFieldMetaDataVO>();
							s.add(voField);
							mpRemoveRelation.put(metaSource, s);
						}
					}
					else if(cell.getValue() != null && cell.getValue() instanceof String) {
						
					}
				}
				
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				model.remove(cell);				
				EntityRelationshipModelEditPanel.this.fireChangeListenEvent();
			}
		});
		
		JMenuItem i5 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.13", "Arbeitsschritt bearbeiten"));
		i5.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
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
					LOG.warn("actionPerformed failed: " + e1, e1);
				}
				catch(CommonPermissionException e1) {
					LOG.warn("actionPerformed failed: " + e1, e1);
				}
				catch(CommonFatalException e1) {
					LOG.warn("actionPerformed failed: " + e1, e1);
				}
				catch(CommonBusinessException e1) {
					LOG.warn("actionPerformed failed: " + e1, e1);
				}				
			}
		});
		
		if(cell.getStyle() != null && cell.getStyle().indexOf(OPENARROW) >= 0) {
			i1.setSelected(true);
			pop.add(i1);
		}
		else if(cell.getStyle() != null && cell.getStyle().indexOf(DIAMONDARROW) >= 0) {
			i2.setSelected(true);
			pop.add(i2);
		}	
		
		if(objectGeneration) {
			//pop.addSeparator();
			pop.add(i5);
		}		
		
		if(delete) {
			pop.addSeparator();
			pop.add(i4);
		}	
		return pop;	
	}

	public Map<EntityMetaDataVO, Set<EntityFieldMetaDataTO>> getMetaDataModel() {
		Map<EntityMetaDataVO, Set<EntityFieldMetaDataTO>> mpModel = new HashMap<EntityMetaDataVO, Set<EntityFieldMetaDataTO>>();
		List<mxCell> lstRelation = new ArrayList<mxCell>();
		List<mxCell> lstRelationNotValid = new ArrayList<mxCell>();
		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			for(int i = 0; i < childCount; i++) {
				mxCell cell = (mxCell)cellContainer.getChildAt(i);
				if((cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) && cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
					EntityMetaDataVO metaVO = (EntityMetaDataVO)cell.getValue();
					EntityMetaDataVO voMap = MetaDataDelegate.getInstance().getEntityByName(metaVO.getEntity());
					mpModel.put(metaVO, new HashSet<EntityFieldMetaDataTO>());
				}
				else if(cell.getStyle() != null && cell.getSource() != null && cell.getTarget() != null) {
					lstRelation.add(cell);
				}
				else if(cell.getStyle() != null && (cell.getSource() == null || cell.getTarget() == null)){
					lstRelationNotValid.add(cell);
				}
			}			
		}
		
		for(mxCell cell : lstRelationNotValid) {
			if(cell.getStyle().indexOf(OPENARROW) >= 0 || cell.getStyle().indexOf(DIAMONDARROW) >= 0) {
				if(cell.getValue() instanceof EntityFieldMetaDataVO){
					EntityFieldMetaDataVO voField = (EntityFieldMetaDataVO)cell.getValue();
					String sMessage = "Verbindung von Referenz " + voField.getField() + " sind nicht alle gegeben!";
					throw new NuclosFatalException(sMessage);
				}
				else {
					throw new NuclosFatalException("Nicht alle Verbindungen gesetzt");
				}
			}
		}
		
		for(mxCell cell : lstRelation) {
			if(cell.getStyle().indexOf(OPENARROW) >= 0 || cell.getStyle().indexOf(DIAMONDARROW) >= 0) {
				mxCell cellSource = (mxCell)cell.getSource();
				EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
				EntityFieldMetaDataVO voField = new EntityFieldMetaDataVO();
				
				if(cellSource.getValue() instanceof EntityMetaDataVO) {					
					EntityMetaDataVO metaSource = (EntityMetaDataVO)cellSource.getValue(); 
					if(cell.getValue() instanceof EntityFieldMetaDataVO) {
						EntityFieldMetaDataVO vo = (EntityFieldMetaDataVO)cell.getValue();
						voField = vo;
					
						boolean blnHasThisRelation = false;
						
						for(EntityFieldMetaDataVO voMetaField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(metaSource.getEntity()).values()) {
							if(voField.getForeignEntity().equals(voMetaField.getForeignEntity())) {
								blnHasThisRelation = true;
								break;
							}
						}
						if(!blnHasThisRelation) {
							
						}

						toField.setEntityFieldMeta(voField);
						
						MyGraphModel model = (MyGraphModel)graph.getModel();
						if(model.getTranslation().size() != 0)
							toField.setTranslation(model.getTranslation().get(voField));
						
						mpModel.get(metaSource).add(toField);

					}
				}
				
			}
			
		}
		
		for(EntityMetaDataVO voMeta : mpRemoveRelation.keySet()) {
			for(EntityFieldMetaDataVO voField : mpRemoveRelation.get(voMeta)) {
				voField.flagRemove();
				EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
				toField.setEntityFieldMeta(voField);
				mpModel.get(voMeta).add(toField);
			}
		}
		
		return mpModel;
	}
	
	public void clearModel() {
		mpRemoveRelation = new HashMap<EntityMetaDataVO, Set<EntityFieldMetaDataVO>>();
		clcttfName.getJTextField().setText("");
		clcttfDescription.setField(new CollectableValueField(null));
		clcttfDescription.getJTextField().setText("");
		
	}
	
	public List<EntityMetaDataVO> getEntitiesInModel() {
		List<EntityMetaDataVO> lstEntites = new ArrayList<EntityMetaDataVO>();
		
		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			for(int i = 0; i < childCount; i++) {
				mxCell cell = (mxCell)cellContainer.getChildAt(i);
				if((cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) && cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
					EntityMetaDataVO metaVO = (EntityMetaDataVO)cell.getValue();
					lstEntites.add(metaVO);
				}
			}
		}
		
		return lstEntites;
	}
	
	public List<EntityFieldMetaDataVO> getEntityFieldsInModel() {
		List<EntityFieldMetaDataVO> lstEntites = new ArrayList<EntityFieldMetaDataVO>();
		
		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			for(int i = 0; i < childCount; i++) {
				mxCell cell = (mxCell)cellContainer.getChildAt(i);
				if((cell.getStyle() != null && (cell.getStyle().indexOf(OPENARROW) >= 0 || cell.getStyle().indexOf(DIAMONDARROW) >= 0)) && cell.getValue() != null && cell.getValue() instanceof EntityFieldMetaDataVO) {
					EntityFieldMetaDataVO metaVO = (EntityFieldMetaDataVO)cell.getValue();
					lstEntites.add(metaVO);
				}
			}
		}
		
		return lstEntites;
	}


	protected JPopupMenu createPopupMenuEntity(final mxCell cell, boolean newCell) {
		
		JPopupMenu pop = new JPopupMenu();
		JMenuItem i1 = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.14","Symbol l\u00f6schen"));
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				int iEdge = cell.getEdgeCount();
				for(int i = 0; i < iEdge; i++){
					mxCell cellRelation = (mxCell)cell.getEdgeAt(0);
					model.remove(cellRelation);
				}
				model.remove(cell);				
				fireChangeListenEvent();
			}
		});
		
		if(!newCell)
			pop.add(i1);
		
		if(cell.getStyle() == null || !(cell.getStyle().indexOf(ENTITYSTYLE) >= 0)) {
			return pop;
		}
		
		
		
		JMenuItem iWizard = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.15","Wizard \u00f6ffnen"));
		iWizard.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
					String sValue = ((EntityMetaDataVO)cell.getValue()).getEntity();
					if(sValue.length() > 0) {
						try {
							final EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(sValue);
							new ShowNuclosWizard.NuclosWizardEditRunnable(false, mf.getHomePane(), vo).run();
						}
						catch(Exception e1) {
							// neue Entity
							LOG.info("actionPerformed: " + e1 + " (new entity?)");
						}			
					}
				}
			}
		});
		
		if(!newCell) {
			//pop.addSeparator();
			pop.add(iWizard);			
		}
		else {			
			JMenuItem iNew = new JMenuItem(localeDelegate.getMessage("nuclos.entityrelation.editor.16","neue Entit\u00e4t"));
			iNew.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
						final EntityMetaDataVO voTMP = (EntityMetaDataVO)cell.getValue();
						final EntityMetaDataVO vo = MetaDataClientProvider.getInstance().getEntity(voTMP.getEntity());
						new ShowNuclosWizard.NuclosWizardEditRunnable(false, mf.getHomePane(), vo).run();
					}
					else {
						cell.setValue(localeDelegate.getMessage("nuclos.entityrelation.editor.16","neue Entit\u00e4t"));
						mxGraph graph = graphComponent.getGraph();
						graph.refresh();
					}
				}
			});			
			pop.add(iNew);
		}
		//pop.addSeparator();
		
		Collection<EntityMetaDataVO> colMetaVO = MetaDataClientProvider.getInstance().getAllEntities();
		
		
		List<EntityMetaDataVO> lst = new ArrayList<EntityMetaDataVO>(colMetaVO);
		
		Collections.sort(lst, new Comparator<EntityMetaDataVO>() {

			@Override
			public int compare(EntityMetaDataVO o1, EntityMetaDataVO o2) {
				return o1.getEntity().toLowerCase().compareTo(o2.getEntity().toLowerCase());
			}
			
			
		});
		
		for(final EntityMetaDataVO vo : lst) {
			if(vo.getEntity().startsWith("nuclos_"))
				continue;

			JCheckBoxMenuItem menu = new JCheckBoxMenuItem(vo.getEntity());
			menu.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					final JMenuItem item = (JMenuItem)e.getSource();
					SwingUtilities.invokeLater(new Runnable() {
						
						@Override
						public void run() {
							cell.setValue(vo);							
							graphComponent.repaint();
							fireChangeListenEvent();
						}
					});
					
				}
			});
			if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
				EntityMetaDataVO sValue = (EntityMetaDataVO)cell.getValue();
				if(vo.getEntity().equals(sValue.getEntity())) {
					menu.setSelected(true);
				}
			}
			//pop.add(menu);
		}
		
		
	
		return pop;	
		
	}

	public CollectableComponentsProvider newCollectableComponentsProvider() {
		return new DefaultCollectableComponentsProvider(clcttfName, clcttfDescription);
	}
	
	public void addChangeListener(ChangeListener cl) {
		this.lstChangeListener.add(cl);
	}
	
	public void removeChangeListener(ChangeListener cl) {
		this.lstChangeListener.remove(cl);
	}
	
	public void fireChangeListenEvent() {
		for(ChangeListener cl : lstChangeListener) {
			cl.stateChanged(new ChangeEvent(this));
		}
	}
	
	private int getBestYPoint() {
		int y = 10;

		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();		
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			if(childCount == 0) {
				return 10;
			}
				
			for(int i = 0; i < childCount; i++) {
				mxCell cellTmp = (mxCell)cellContainer.getChildAt(i);
				if(cellTmp.getValue() instanceof EntityMetaDataVO) {
					if(cellTmp.getGeometry().getY() >= y) {
						y += 100;
					}
				}
			}
		}
		
		return y;
	}
	
	
	public void showDataModel(List<EntityMetaDataVO> lstEntites, boolean blnNew) {
		
		mxCell cellRoot = (mxCell)graphComponent.getGraph().getModel().getRoot();
		mxCell cellContainer = (mxCell)cellRoot.getChildAt(0);
		
		
		int x = 10;
		int y = getBestYPoint();
		int index = 0;		
		
		int maxInARow = 8;		
		
		for(EntityMetaDataVO voMeta : lstEntites) {
			
			mxGeometry mxgeo = new mxGeometry(x, y, 100, 80);
			
			mxCell child = new mxCell(voMeta, mxgeo, ENTITYSTYLE);
			child.setVertex(true);
			graphComponent.getGraph().getModel().add(cellContainer, child, index++);
			x += 150;
			if(index % maxInARow == 0){
				y += 100;
				x = 10;
			}
		}
		if(!blnNew)
			lstEntites.addAll(getEntitiesInModel());
		
		for(EntityMetaDataVO voMeta : lstEntites) {
			if(voMeta.getEntity().startsWith("nuclos_")|| voMeta.getEntity().equals("entityfields"))
				continue;
			
			boolean relation = false;
			String sForeign = null;
			EntityFieldMetaDataVO voForeignField = null;
			for(EntityFieldMetaDataVO voField : MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(voMeta.getEntity()).values()) {
				if(voField.getForeignEntity() != null) {
					boolean blnNextRelation = false;
					for(EntityFieldMetaDataVO voFieldInModel : getEntityFieldsInModel()) {
						if(voFieldInModel.getId().equals(voField.getId())) {
							blnNextRelation = true;
							break;
						}								
					}
					if(blnNextRelation)
						continue;
					relation = true;
					sForeign = voField.getForeignEntity();
					voForeignField = voField;
					if(relation) {
						if(voForeignField.getEntityId() < 0) {
							continue;
						}
						mxGeometry mxgeo = new mxGeometry(x, y, 100, 80);
						mxgeo.setSourcePoint(new mxPoint(100,100));
						mxgeo.setTargetPoint(new mxPoint(150,150));
						
						mxCell child = new mxCell(voForeignField, mxgeo, mxConstants.ARROW_OPEN);
						if(voField.getDbColumn().startsWith("INTID_")){
							child = new mxCell(voForeignField, mxgeo, mxConstants.ARROW_DIAMOND);
						}
						boolean targetFound = false;
						boolean sourceFound = false;
						for(EntityMetaDataVO vo : lstEntites){
							if(vo.getEntity().equals(sForeign))
								targetFound = true;
							if(vo.getEntity().equals(voMeta.getEntity()))
								sourceFound = true;
							
						}
						if(targetFound && sourceFound){
							child.setTarget(getMasterDataMetaVOCell(sForeign));
							child.setSource(getMasterDataMetaVOCell(voMeta.getEntity()));
							
							mxCell[] cells = {child};
							
							if(voField.getDbColumn().startsWith("INTID_")){
								mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
							}
							else {
								mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
							}
							
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDSIZE, "12");
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, EDGESTYLE , ELBOWCONNECTOR);
							child.setEdge(true);
							graphComponent.getGraph().getModel().add(cellContainer, child, index++);
							x += 120;
							y += 100;
						}
					}
				}
					
			}
			
		}		
		
		try {
			for(MasterDataVO voGeneration : MasterDataCache.getInstance().get(NuclosEntity.GENERATION.getEntityName())) {
				String sSourceModule = (String)voGeneration.getField("sourceModule");
				String sTargetModule = (String)voGeneration.getField("targetModule");
				boolean targetFound = false;
				boolean sourceFound = false;
				for(EntityMetaDataVO vo : lstEntites){
					if(vo.getEntity().equals(sTargetModule))
						targetFound = true;
					if(vo.getEntity().equals(sSourceModule))
						sourceFound = true;
					
				}
				if(targetFound && sourceFound){
					mxGeometry mxgeo = new mxGeometry(x, y, 100, 80);
					mxgeo.setSourcePoint(new mxPoint(100,100));
					mxgeo.setTargetPoint(new mxPoint(150,150));
					
					mxCell child = new mxCell("", mxgeo, mxConstants.ARROW_OPEN);
					child.setSource(getMasterDataMetaVOCell(sSourceModule));
					child.setTarget(getMasterDataMetaVOCell(sTargetModule));
					
					mxCell[] cells = {child};
					mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
					mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ENDSIZE, "12");
					mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
					mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
					mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, EDGESTYLE , ELBOWCONNECTOR);
					child.setEdge(true);
					graphComponent.getGraph().getModel().add(cellContainer, child, index++);
					x += 120;
					y += 100;
				}
				
			}
		}
		catch(CommonFinderException e) {
			LOG.info("showDataModel: " + e);
		}		
		this.fireChangeListenEvent();
		
	}
	
	public void removeNotExistentEntitiesFromModel() {
		Collection<EntityMetaDataVO> lstEntities = MetaDataDelegate.getInstance().getAllEntities();
		for(EntityMetaDataVO voInModel : getEntitiesInModel()) {
			if(!lstEntities.contains(voInModel)) {
				mxCell cellRemove = getCellByEntityName(voInModel.getEntity());
				int iEdge = cellRemove.getEdgeCount();
				for(int i = 0; i < iEdge; i++){
					mxCell cellRelation = (mxCell)cellRemove.getEdgeAt(i);
					getGraphModel().remove(cellRelation);
				}
				getGraphModel().remove(cellRemove);				
				fireChangeListenEvent();
			}
				
		}

	}
	
	public void loadReferenz() {
		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			for(int i = 0; i < childCount; i++) {
				mxCell cell = (mxCell)cellContainer.getChildAt(i);
				if(cell.getValue() instanceof EntityFieldMetaDataVO) {
					EntityFieldMetaDataVO voField = (EntityFieldMetaDataVO)cell.getValue();
					if(cell.getSource() == null) {
						Long sourceId = new Long(voField.getEntityIdAsString());
						EntityMetaDataVO voSource = MetaDataDelegate.getInstance().getEntityById(sourceId);
						mxCell cellSource = getCellByEntityName(voSource.getEntity());
						cell.setSource(cellSource);
					}
					if(cell.getTarget() == null) {
						EntityMetaDataVO voTarget = MetaDataDelegate.getInstance().getEntityByName(voField.getForeignEntity());
						mxCell cellTarget = getCellByEntityName(voTarget.getEntity());
						cell.setTarget(cellTarget);
					}
					
					if(cell.getSource().getValue() instanceof EntityMetaDataVO) {
						EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
						try {
							voField = MetaDataDelegate.getInstance().getEntityField(voSource.getEntity(), voField.getField());
							cell.setValue(voField);
						}
						catch(Exception e) {
							LOG.info("loadReferenz: " + e);
						}
					}
				}
				
			}			
		}
		
		graph.refresh();
		
	}
	
	private mxCell getCellByEntityName(String name) {
		mxCell cell = null;
		
		mxGraph graph = graphComponent.getGraph();
		mxCell root = (mxCell)graph.getModel().getRoot();
		int rootChildCount = root.getChildCount();
		if(rootChildCount == 1) {
			mxCell cellContainer = (mxCell)root.getChildAt(0);
			int childCount = cellContainer.getChildCount();
			for(int i = 0; i < childCount; i++) {
				mxCell cellTmp = (mxCell)cellContainer.getChildAt(i);
				if(cellTmp.getValue() instanceof EntityMetaDataVO) {
					EntityMetaDataVO vo = (EntityMetaDataVO)cellTmp.getValue();
					if(vo.getEntity().equals(name)) {
						cell = cellTmp;
					}						
				}
			}
		}
		
		return cell;
	}
	
	protected mxCell getMasterDataMetaVOCell(String sEntity) {
		mxCell cellRoot = (mxCell)graphComponent.getGraph().getModel().getRoot();
		mxCell cellContainer = (mxCell)cellRoot.getChildAt(0);
		int count = cellContainer.getChildCount();
		for(int i = 0; i < count; i++) {
			mxCell cell = (mxCell)cellContainer.getChildAt(i);
			if(cell.getValue() != null && cell.getValue() instanceof EntityMetaDataVO) {
				EntityMetaDataVO voMeta = (EntityMetaDataVO)cell.getValue();
				if(voMeta.getEntity().equals(sEntity)) {
					return cell;
				}
			}
		}
		
		return null;		
	}
	
	private void editSubformRelation(final mxCell cell) {
        if(cell.getValue() != null && (cell.getValue() instanceof String || cell.getValue() instanceof EntityFieldMetaDataVO)) {
			mxCell target = (mxCell)cell.getTarget();
			mxCell source = (mxCell)cell.getSource();
			EntityMetaDataVO voSource = (EntityMetaDataVO)source.getValue();
			EntityMetaDataVO voTarget = (EntityMetaDataVO)target.getValue();
			String sFieldName = null;
			boolean blnNotSet = true;
			while(blnNotSet) {
				if(cell.getValue() instanceof EntityFieldMetaDataVO) {
					String sDefault = ((EntityFieldMetaDataVO)cell.getValue()).getField();
					sFieldName = JOptionPane.showInputDialog(EntityRelationshipModelEditPanel.this, 
						localeDelegate.getMessage("nuclos.entityrelation.editor.17", "Bitte geben Sie den Namen des Feldes an!"), sDefault);
				}
				else 
					sFieldName = JOptionPane.showInputDialog(EntityRelationshipModelEditPanel.this, localeDelegate.getMessage(
							"nuclos.entityrelation.editor.17", "Bitte geben Sie den Namen des Feldes an!"));
				if(sFieldName == null || sFieldName.length() < 1) {
					if(cell.getValue() instanceof String)
						getGraphModel().remove(cell);					
					return;
				}
				else if(sFieldName != null) {
					blnNotSet = false;
				}
				
				for(EntityFieldMetaDataVO voField : MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(voSource.getEntity()).values()){
					if(voField.getField().equals(sFieldName)){
						JOptionPane.showMessageDialog(EntityRelationshipModelEditPanel.this, localeDelegate.getMessage(
								"nuclos.entityrelation.editor.18", "Der Feldname ist schon vorhanden"));
						blnNotSet = true;
						break;
					}
				}
				
			}
			EntityFieldMetaDataVO vo = null;
			if(cell.getValue() instanceof EntityFieldMetaDataVO) {
				vo = (EntityFieldMetaDataVO)cell.getValue();
				vo.flagUpdate();
			}
			else {
				vo = new EntityFieldMetaDataVO();
				vo.setModifiable(true);
			   vo.setLogBookTracking(false);
			   vo.setReadonly(false);
			   vo.setShowMnemonic(true);
			   vo.setInsertable(true);
			   vo.setSearchable(true);
			   vo.setNullable(false);
			   vo.setUnique(true);
			   vo.setDataType("java.lang.String");
			}
		   
		   
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
		   if(cell.getValue() instanceof String) {
			   
			   vo.setDbColumn("INTID_" + sFieldName);
		   }
			   
			   
			cell.setValue(vo);
			
			List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
			
			EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
			toField.setEntityFieldMeta(vo);
			toField.setTranslation(lstTranslation);
			toList.add(toField);
			
			MetaDataDelegate.getInstance().modifyEntityMetaData(voSource, toList);
			EntityRelationshipModelEditPanel.this.loadReferenz();
		}
    }

	
	private void editMasterdataRelation(mxCell cell) {
        EntityFieldMetaDataVO voField = null;
        RelationAttributePanel panel = new RelationAttributePanel(RelationAttributePanel.TYPE_ENTITY);
        String sSource = "";
        String sTarget = "";
        EntityMetaDataVO voSourceModify = null;
        if(cell.getValue() != null && cell.getValue() instanceof EntityFieldMetaDataVO) {
        	voField = (EntityFieldMetaDataVO)cell.getValue();
        	
        	EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
        	voSourceModify = voSource;
        	EntityMetaDataVO voTarget = (EntityMetaDataVO)cell.getTarget().getValue();
        	sSource = voSource.getEntity();
        	sTarget = voTarget.getEntity();
        	EntityMetaDataVO voForeign = MetaDataClientProvider.getInstance().getEntity(voSource.getEntity());
        	EntityMetaDataVO voEntity = MetaDataClientProvider.getInstance().getEntity(voTarget.getEntity());
         	panel.setEntity(voEntity);
         	panel.setEntitySource(voForeign);
         	panel.setEntityFields(MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(voForeign.getEntity()).values());

         	if(voField.getId() != null) {
         		voField = MetaDataClientProvider.getInstance().getEntityField(voForeign.getEntity(), voField.getField());
         		List<TranslationVO> lstTranslation = new ArrayList<TranslationVO>();
        		for(LocaleInfo lInfo : LocaleDelegate.getInstance().getAllLocales(false)) {
        			Map<String, String> mp = new HashMap<String, String>();
        			
        			mp.put(TranslationVO.labelsField[0], LocaleDelegate.getInstance().getResourceByStringId(lInfo, voField.getLocaleResourceIdForLabel()));
        			mp.put(TranslationVO.labelsField[1], LocaleDelegate.getInstance().getResourceByStringId(lInfo, voField.getLocaleResourceIdForDescription()));
        			TranslationVO voTrans = new TranslationVO(lInfo.localeId, lInfo.title, lInfo.language, mp);
        			lstTranslation.add(voTrans);
        		}	
        		panel.setTranslation(lstTranslation);
        		panel.setFieldValues(voField);
         	}
         	else {
         		MyGraphModel model = (MyGraphModel)graphComponent.getGraph().getModel();
         		panel.setFieldValues(voField);	
        		panel.setTranslationAndMore(model.getTranslation().get(voField));
         	}
        }
        else if(cell.getValue() != null && cell.getValue() instanceof String) {
        	EntityMetaDataVO voSource = (EntityMetaDataVO)cell.getSource().getValue();
        	EntityMetaDataVO voTarget = (EntityMetaDataVO)cell.getTarget().getValue();
        	sSource = voSource.getEntity();
        	sTarget = voTarget.getEntity();
        	
        	EntityMetaDataVO voForeign = MetaDataClientProvider.getInstance().getEntity(voSource.getEntity());
        	EntityMetaDataVO voEntity = MetaDataClientProvider.getInstance().getEntity(voTarget.getEntity());
        	voSourceModify = voForeign;
        	panel.setEntity(voEntity);
         	panel.setEntitySource(voForeign);
         	panel.setEntityFields(MetaDataDelegate.getInstance().getAllEntityFieldsByEntity(voSource.getEntity()).values());
        }
        
        double cellsDialog [][] = {{5, TableLayout.PREFERRED, 5}, {5, TableLayout.PREFERRED,5}};	
        JDialog dia = new JDialog(mf);
        dia.setLayout(new TableLayout(cellsDialog));
        dia.setTitle("Verbindung von " + sSource +" zu "+ sTarget + " bearbeiten");
        dia.setLocationRelativeTo(EntityRelationshipModelEditPanel.this);
        dia.add(panel, "1,1");
        dia.setModal(true);
        panel.setDialog(dia);
        dia.pack();
        dia.setVisible(true);	

        if(panel.getState() == 1) {
        	EntityFieldMetaDataVO vo = panel.getField();
        	cell.setValue(vo);
        	
        	EntityRelationshipModelEditPanel.this.fireChangeListenEvent();
        	
        	List<EntityFieldMetaDataTO> toList = new ArrayList<EntityFieldMetaDataTO>();
        	
        	EntityFieldMetaDataTO toField = new EntityFieldMetaDataTO();
        	toField.setEntityFieldMeta(vo);
        	toField.setTranslation(panel.getTranslation().getRows());
        	toList.add(toField);
        	
        	MetaDataDelegate.getInstance().modifyEntityMetaData(voSourceModify, toList);
        	
        	MyGraphModel model = (MyGraphModel)graphComponent.getGraph().getModel();
        	model.getTranslation().put(vo,panel.getTranslation().getRows());
        	getGraphComponent().refresh();
        	
        	loadReferenz();
        	
        }
        else {
        	if(cell.getValue() instanceof String)
        		getGraphModel().remove(cell);					
        }
    }	
	
	class MyCellEditor extends mxCellEditor {

		public MyCellEditor(mxGraphComponent graphComponent) {
			super(graphComponent);
		}

		@Override
		public Component getEditor() {
			return new JLabel();
		}

		@Override
		public void startEditing(Object cell, EventObject trigger) {
			stopEditing(true);
		}
	}

	
}	// class StateModelEditPanel