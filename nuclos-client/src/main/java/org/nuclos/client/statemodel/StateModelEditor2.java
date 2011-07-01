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
package org.nuclos.client.statemodel;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.relation.editor.EditorPalette;
import org.nuclos.client.statemodel.models.StatePropertiesPanelModel;
import org.nuclos.client.statemodel.panels.StateModelEditorPropertiesPanel;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.w3c.dom.Document;

import com.mxgraph.io.mxCodec;
import com.mxgraph.io.mxCodecRegistry;
import com.mxgraph.io.mxModelCodec;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxGraphHandler;
import com.mxgraph.swing.view.mxCellEditor;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxUtils;
import com.mxgraph.util.mxEventSource.mxIEventListener;
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
public class StateModelEditor2 extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int clientId = -1;
	
	private List<CollectableEntityObject> usages;
	
	class MyAddEventListener implements mxIEventListener {
		
		mxCell addedRoundedCell;
		
		@Override
		public void invoke(Object sender, mxEventObject evt) {
			
			Object obj = evt.getProperty("cells");
			
			if(obj instanceof Object[]) {
				Object objArray[] = (Object[])obj;
				if(objArray.length > 0) {
					Object ob = objArray[0];
					if(ob instanceof mxCell) {
						mxCell cellAdded = (mxCell)ob;
						if(ENTITYSTYLE.equals(cellAdded.getStyle())) {
							if(cellAdded.getValue() instanceof String) {
								StateVO vo = new StateVO(clientId--, null, "", "", null);
								cellAdded.setValue(vo);					
								final StatePropertiesPanelModel model = pnlProperties.getStatePropertiesPanel().getModel();
								
								model.setName(vo.getStatename());
								model.setNumeral(vo.getNumeral());
								model.setDescription(vo.getDescription());
								model.setTab(vo.getTabbedPaneName());
								
								addFirstTransitionIfNecessary(cellAdded);

								addStatePanelListeners();
								setupRightsPanel(vo);
								// setupSubforms(vo);

								pnlProperties.setPanel("State");
							}
							else {
								StateVO vo = new StateVO(clientId--, null, "", "", null);
								try {
									mxCell cell = (mxCell)cellAdded.clone();
								
									cell.setValue(vo);					
									final StatePropertiesPanelModel model = pnlProperties.getStatePropertiesPanel().getModel();
									
									model.setName(vo.getStatename());
									model.setNumeral(vo.getNumeral());
									model.setDescription(vo.getDescription());
									
									
									mxGraphModel graphModel = (mxGraphModel)graphComponent.getGraph().getModel(); 
									mxCell parent = (mxCell)((mxCell)graphModel.getRoot()).getChildAt(0);
									
									int index = graphModel.getChildCount(parent);
									graphModel.add(parent, cell, index);
									graphModel.remove(cellAdded);
									
									addedRoundedCell = cell;
								
									addStatePanelListeners();
									setupRightsPanel(vo);
									// setupSubforms(vo);
	
									pnlProperties.setPanel("State");
								}
								catch(CloneNotSupportedException e) {
									e.printStackTrace();
								}
							}
						}
						else if(cellAdded.getStyle() == null) {
							cellAdded.setStyle("endArrow=open;endSize=12");
							Object cells[] = {cellAdded};
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "edgeStyle" , "mxEdgeStyle.ElbowConnector");
							mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_HORIZONTAL);
							StateTransitionVO vo = new StateTransitionVO(clientId--, null, null, "", false);
							cellAdded.setValue(vo);
							pnlProperties.setPanel("Transition");
							try {
								StateTransitionVO voAdded = (StateTransitionVO)cellAdded.getValue();
								
								mxCell cellSource = (mxCell)cellAdded.getSource();
								mxCell cellTarget = (mxCell)cellAdded.getTarget();
								
								if(addedRoundedCell != null) {
									cellAdded.setTarget(addedRoundedCell);
									StateVO stateSource = (StateVO)cellSource.getValue();
									StateVO stateTarget = (StateVO)addedRoundedCell.getValue();
									
									voAdded.setStateSource(stateSource.getClientId());
									voAdded.setStateTarget(stateTarget.getClientId());
									
									cellAdded.getGeometry().setTargetPoint(addedRoundedCell.getGeometry().getTargetPoint());
									
									pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(voAdded.getRuleIdsWithRunAfterwards()));
									TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRulePanel().getTblRules(), 10, 10);
									pnlProperties.getTransitionRulePanel().getBtnAutomatic().setSelected(voAdded.isAutomatic());
									pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(voAdded.getRoleIds()));
									TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRolePanel().getTblRoles(), 10, 10);
									addedRoundedCell = null;
									//setupTransitionSubform(voAdded);
								}
								else {
									StateVO stateSource = (StateVO)cellSource.getValue();
									StateVO stateTarget = (StateVO)cellTarget.getValue();
									
									voAdded.setStateSource(stateSource.getClientId());
									voAdded.setStateTarget(stateTarget.getClientId());
									
									pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(voAdded.getRuleIdsWithRunAfterwards()));
									TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRulePanel().getTblRules(), 10, 10);
									pnlProperties.getTransitionRulePanel().getBtnAutomatic().setSelected(voAdded.isAutomatic());
									pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(voAdded.getRoleIds()));
									TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRolePanel().getTblRoles(), 10, 10);
									//setupTransitionSubform(voAdded);
								}
							}
							catch (CreateException ex) {
								Errors.getInstance().showExceptionDialog(StateModelEditor2.this, ex.getMessage(), ex);
							}
							catch (RemoteException ex) {
								Errors.getInstance().showExceptionDialog(StateModelEditor2.this, ex.getMessage(), ex);
							}
						}
					}
				}
			}
			
			fireChangeListenEvent();				
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
	
	class MyGraphHandler extends mxGraphHandler {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;



		public MyGraphHandler(mxGraphComponent graphComponent) {
			super(graphComponent);			
		}
		

		@Override
		public void mousePressed(MouseEvent e) {
			
			
			mxCell cell = (mxCell)MyGraphHandler.this.getGraphComponent().getCellAt(e.getX(), e.getY());
			if(cell == null) {
				super.mousePressed(e);
				return;
			}
			
			if(cell.getStyle() != null && (cell.getStyle().indexOf("startArrow=oval;startSize=25") >= 0
				|| cell.getStyle().indexOf("endArrow=open") >= 0 || cell.getStyle().indexOf("endArrow=oval") >= 0)) {
					getGraphComponent().getGraphHandler().setMoveEnabled(false);
					return;
			}
			else if(cell.getValue() instanceof StateVO) {
				int count = cell.getEdgeCount();
				for(int i = 0; i < count; i++) {
					mxCell cellEdge = (mxCell)cell.getEdgeAt(i);
					if(cellEdge.getStyle() != null) {
						if(cellEdge.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
							getGraphComponent().getGraphHandler().setMoveEnabled(false);
							return;
						}
						else {
							getGraphComponent().getGraphHandler().setMoveEnabled(true);
						}
					}
				}
			}
			else {
				getGraphComponent().getGraphHandler().setMoveEnabled(true);
			}			
			super.mousePressed(e);
		}
		


		@Override
		public void mouseMoved(MouseEvent e) {
			mxCell cell = (mxCell)MyGraphHandler.this.getGraphComponent().getCellAt(e.getX(), e.getY());
			if(cell == null) {
				super.mouseMoved(e);
				return;
			}
			
			if(cell.getStyle() != null && (cell.getStyle().indexOf("startArrow=oval;startSize=25") >= 0
				|| cell.getStyle().indexOf("endArrow=open") >= 0 || cell.getStyle().indexOf("endArrow=oval") >= 0)) {
					getGraphComponent().getGraphHandler().setMoveEnabled(false);
					return;
			}
			else if(cell.getValue() instanceof StateVO) {
				int count = cell.getEdgeCount();
				for(int i = 0; i < count; i++) {
					mxCell cellEdge = (mxCell)cell.getEdgeAt(i);
					if(cellEdge.getStyle() != null) {
						if(cellEdge.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
							getGraphComponent().getGraphHandler().setMoveEnabled(false);
							return;
						}
						else {
							getGraphComponent().getGraphHandler().setMoveEnabled(true);
						}
					}
				}
			}
			else {
				getGraphComponent().getGraphHandler().setMoveEnabled(true);
			}			
			super.mouseMoved(e);
		}
	}
	
	public static String ENTITYSTYLE = "rounded=1";
	public static String SYMBOLCOLOR = "#6482B9";
	
	JPanel mainPanel;
	
	private StateModelEditorPropertiesPanel pnlProperties;
	
	StateGraphVO stateGraphVO;
	
	JPanel propertiesPanel;
	JLabel lbProp;	
	
	protected JTabbedPane libraryPane;
	
	mxGraphComponent graphComponent;
	
	List<ChangeListener> lstChangeListener;
	
	List<mxCell> lstRelations;
	

	public StateModelEditor2() {
		super(new BorderLayout());		
		lstChangeListener = new ArrayList<ChangeListener>();
		lstRelations = new ArrayList<mxCell>();
		init();		
	}
	
	protected void init() {
		
		mainPanel = new JPanel();
		
		propertiesPanel = new JPanel();
		propertiesPanel.setBorder(new LineBorder(Color.BLACK));
		lbProp = new JLabel("PROPERTIES EDIT PANEL");
		propertiesPanel.add(lbProp);		
		
		double size [][] = {{TableLayout.PREFERRED, 100, 10,TableLayout.FILL,300, 10}, {10,10, 300,300,TableLayout.FILL}};
		
		TableLayout layout = new TableLayout(size);
		layout.setVGap(3);
		layout.setHGap(5);
		mainPanel.setLayout(layout);
		
		libraryPane = new JTabbedPane();
		
		EditorPalette shapesPalette = insertPalette("Symbole");
		
		fillPalette(shapesPalette);
		
		mxGraphModel model = new mxGraphModel();
		model.addListener(mxEvent.CHANGE, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		StatemodelGraph myGraph = new StatemodelGraph(model);
		
		

		mxCodecRegistry.register(new mxModelCodec(model));
		
		graphComponent = new mxGraphComponent(myGraph);
		graphComponent.setGridVisible(true);
		graphComponent.getViewport().setOpaque(false);
		graphComponent.setBackground(Color.WHITE);
		graphComponent.setToolTips(true);
		graphComponent.getConnectionHandler().setCreateTarget(true);
		graphComponent.setCellEditor(new MyCellEditor(graphComponent));
		graphComponent.addKeyListener(new KeyAdapter()  {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_DELETE) {
					mxCell cell = (mxCell)graphComponent.getGraph().getSelectionModel().getCell();
					if(cell.getValue() != null && cell.getValue() instanceof StateVO) {
						int count = cell.getEdgeCount();
						for(int i = 0; i < count; i++) {
							mxCell cellEdge = (mxCell)cell.getEdgeAt(i);
							if(cellEdge.getStyle() != null) {
								if(cellEdge.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
									JOptionPane.showMessageDialog(StateModelEditor2.this, "1. Status kann nicht gel\u00f6scht werden!");
									return;
								}
							}
						}
						StateVO voRemove = (StateVO)cell.getValue();
						voRemove.remove();
						stateGraphVO.getStates().add(voRemove);
					}
					else if(cell.getValue() != null && cell.getValue() instanceof StateTransitionVO) {
						if(cell.getStyle() != null) {
							if(cell.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
								JOptionPane.showMessageDialog(StateModelEditor2.this, "Element kann nicht gel\u00f6scht werden!");
								return;
							}
						}
						StateTransitionVO voRemove = (StateTransitionVO)cell.getValue();
						voRemove.remove();
						stateGraphVO.getTransitions().add(voRemove);
						
					}
					graphComponent.getGraph().getModel().remove(cell);
				}
			}
			
		});
		
		//pnlProperties = new StateModelEditorPropertiesPanel(this);
		//pnlProperties.setPanel("None");

		addEventListener(myGraph);
		
		createMouseWheelListener();
		
		createMouseListener();		
		
		mainPanel.add(graphComponent, "1,1, 3,4");
		mainPanel.add(pnlProperties, "4,1, 4,4");
		
		this.add(mainPanel);
		
	}
	
	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}
	

	
	public void refresh() {
		graphComponent.repaint();
		graphComponent.getGraph().getView().reload();
	}
	
	public void clearModel() {
		mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
		model.clear();
	}
	
	public void setStateGraph(StateGraphVO vo) {
		this.stateGraphVO = vo;
		
		Set<StateVO> setStates = stateGraphVO.getStates();
		Set<StateTransitionVO> setTransitions = stateGraphVO.getTransitions();
		StateModelVO stateModelVO = stateGraphVO.getStateModel();
		
		mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
		mxGraphModel loadedModel = new mxGraphModel();
		if(stateModelVO.getXMLLayout() != null && stateModelVO.getXMLLayout().length() > 0) {
			Document document = mxUtils.parse(stateModelVO.getXMLLayout());
			
			mxCodec codec = new mxCodec(document);
			codec.decode(document.getDocumentElement(), loadedModel);			
		}
		
		
		
		int index = 0;
		for (StateVO statevo : setStates) {
			
			mxCell parent = (mxCell)((mxCell)model.getRoot()).getChildAt(0);			
			mxGeometry mxgeo = new mxGeometry(50, 50, 100, 80);
			mxGeometry geo = getGeometryForCell(loadedModel, statevo.getStatename());
			if(geo != null) {
				mxgeo = geo;
			}
			mxCell child = new mxCell(statevo, mxgeo, ENTITYSTYLE);
			child.setVertex(true);
			model.add(parent, child, index++);
					
			statevo.setStatename(CommonLocaleDelegate.getResource(StateDelegate.getInstance().getResourceSIdForName(statevo.getId()), statevo.getStatename()));//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForName(statevo.getId())));
			statevo.setDescription(CommonLocaleDelegate.getResource(StateDelegate.getInstance().getResourceSIdForDescription(statevo.getId()), statevo.getDescription()));//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForDescription(statevo.getId())));
		}
		
		for (StateTransitionVO statetransitionvo : setTransitions) {

			final Integer iSourceStateId = statetransitionvo.getStateSource();
			final Integer iTargetStateId = statetransitionvo.getStateTarget();
			
			mxCell parent = (mxCell)((mxCell)model.getRoot()).getChildAt(0);
			
			mxGeometry geo = new mxGeometry(100, 100, 100, 100);
			mxGeometry geoTransition = getGeometryForCell(loadedModel, String.valueOf(statetransitionvo.getId()));
			if(geoTransition == null) {
				geo.setSourcePoint(new mxPoint(100,100));
				geo.setTargetPoint(new mxPoint(150,150));
			}
			else {
				geo = geoTransition;
			}
			
			
			
			mxCell child = null;
			
			if(statetransitionvo.getRuleIdsWithRunAfterwards() != null && statetransitionvo.getRuleIdsWithRunAfterwards().size() > 0){
				child = new mxCell(statetransitionvo, geo, "endArrow=oval;endSize=12");
			}			
			else {
				child = new mxCell(statetransitionvo, geo, "endArrow=open;endSize=12");
			}
			Object cells[] = {child};
			mxUtils.setCellStyles(this.graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
			mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_HORIZONTAL);
			mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "edgeStyle" , "mxEdgeStyle.ElbowConnector");
			if (iSourceStateId == null && iTargetStateId != null) {
				// Initial transition to start state
				
				mxGeometry geoPoint = new mxGeometry(20, 20, 20, 20);
				geoPoint.setSourcePoint(new mxPoint(20,20));
				geoPoint.setTargetPoint(new mxPoint(150,150));
				
				child = new mxCell(statetransitionvo, geoPoint, child.getStyle() + ";startArrow=oval;startSize=25;dashed=1");
				
			}
			else {
				if(statetransitionvo.isAutomatic()) {
					child.setStyle(child.getStyle() + ";dashed=1");
				}
			}
			
			child.setEdge(true);


			if (iSourceStateId != null) {
				int childCount = ((mxCell)model.getRoot()).getChildAt(0).getChildCount();
				for(int i = 0; i < childCount; i++) {
					mxCell targetCell = (mxCell)((mxCell)model.getRoot()).getChildAt(0).getChildAt(i);
					if(targetCell.getValue() != null && targetCell.getValue() instanceof StateVO) {
						StateVO value = (StateVO)targetCell.getValue();
						if(value.getId().equals(iSourceStateId)) {
							child.setSource(targetCell);
							break;
						}
					}					
				}
			}
			if (iTargetStateId != null) {
				int childCount = ((mxCell)model.getRoot()).getChildAt(0).getChildCount();
				for(int i = 0; i < childCount; i++) {
					mxCell targetCell = (mxCell)((mxCell)model.getRoot()).getChildAt(0).getChildAt(i);
					if(targetCell.getValue() != null && targetCell.getValue() instanceof StateVO) {
						StateVO value = (StateVO)targetCell.getValue();
						if(value.getId().equals(iTargetStateId)) {
							child.setTarget(targetCell);
							break;
						}
					}					
				}
			}
			
			model.add(parent, child, index++);
		}
		
		pnlProperties.setPanel("None");
		
	}
	
	public mxGeometry getGeometryForCell(mxGraphModel model, String name) {
		
		mxCell root = (mxCell)model.getRoot();
		mxCell containerCell = (mxCell)root.getChildAt(0);
		int childCount = containerCell.getChildCount();
		for(int i = 0; i < childCount; i++) {
			mxCell cell = (mxCell)containerCell.getChildAt(i);
			if(name.equals(cell.getValue())) {
				return cell.getGeometry();
			}
		}
		
		
		return null;
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
				if(SwingUtilities.isRightMouseButton(e)) {
					Object obj = graphComponent.getCellAt(e.getX(), e.getY());
					
					if(obj instanceof mxCell) {
						mxCell cell = (mxCell)obj;
						if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) { 
							JPopupMenu pop = createPopupMenuEntity(cell, false);
							pop.show(e.getComponent(), e.getX(), e.getY());
						}
						else {
							JPopupMenu pop = createRelationPopupMenu(cell, true);
							pop.show(e.getComponent(), e.getX(), e.getY());
						}
					}
					else {
						JPopupMenu pop = createPopupMenu();
						pop.show(e.getComponent(), e.getX(), e.getY());
					}
				}
				else if(SwingUtilities.isLeftMouseButton(e)) {
					removeStatePanelListeners();
					updateStateProperties();
					// closeSubForms();
					getGraphComponent().getGraphHandler().setMoveEnabled(true);
					Object obj = graphComponent.getCellAt(e.getX(), e.getY());					
					if(obj instanceof mxCell) {						
						mxCell cell = (mxCell)obj;
						if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) {
							Object value = cell.getValue();
							if(value != null) {
								if(value instanceof StateVO) {
									int count = cell.getEdgeCount();
									for(int i = 0; i < count; i++) {
										mxCell cellEdge = (mxCell)cell.getEdgeAt(i);
										if(cellEdge.getStyle() != null) {
											if(cellEdge.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
												getGraphComponent().getGraphHandler().setMoveEnabled(false);
												break;
											}
											else {
												getGraphComponent().getGraphHandler().setMoveEnabled(true);
											}
										}
									}
									pnlProperties.setPanel("State");
									StateVO vo = (StateVO)value;
									final StatePropertiesPanelModel model = pnlProperties.getStatePropertiesPanel().getModel();
									
									model.setName(vo.getStatename());
									model.setNumeral(vo.getNumeral());
									model.setDescription(vo.getDescription());
									model.setTab(vo.getTabbedPaneName());

									addStatePanelListeners();
									setupRightsPanel(vo);
									// setupSubforms(vo);

								}
								else if(value instanceof String) {
									
								}
								else {
									lbProp.setText("PROPERTIES EDIT PANEL");
								}
							}
							else {
								lbProp.setText("PROPERTIES EDIT PANEL");
							}
						}
						else if(cell.getStyle() != null && (cell.getStyle().indexOf(mxConstants.ARROW_OPEN) >= 0 || 
							cell.getStyle().indexOf(mxConstants.ARROW_OVAL) >= 0)) {
							if(cell.getStyle() != null) {
								if(cell.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
									getGraphComponent().getGraphHandler().setMoveEnabled(false);
								}
								else {
									getGraphComponent().getGraphHandler().setMoveEnabled(true);
								}
							}
							pnlProperties.setPanel("Transition");
							try {
								StateTransitionVO vo = (StateTransitionVO)cell.getValue();
								pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(vo.getRuleIdsWithRunAfterwards()));
								TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRulePanel().getTblRules(), 10, 10);
								pnlProperties.getTransitionRulePanel().getBtnAutomatic().setSelected(vo.isAutomatic());
								pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(vo.getRoleIds()));
								TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRolePanel().getTblRoles(), 10, 10);
							}
							catch (CreateException ex) {
								Errors.getInstance().showExceptionDialog(StateModelEditor2.this, ex.getMessage(), ex);
							}
							catch (RemoteException ex) {
								Errors.getInstance().showExceptionDialog(StateModelEditor2.this, ex.getMessage(), ex);
							}
						}
						else {
							lbProp.setText("PROPERTIES EDIT PANEL");
						}
					}
					else {
						pnlProperties.setPanel("None");						
					}
				}
			}			
		});
	}

//	private mxGraphModel createGraphModel() {
//		mxGraphModel model = new mxGraphModel() {
//
//			@Override
//			public Object add(Object parent, Object child, int index) {
//				
//				Object obj = super.add(parent, child, index);
//				if(obj instanceof mxCell) {
//					mxCell cell = (mxCell)obj;
//					if(StringUtils.looksEmpty(cell.getStyle())) {
//						JPopupMenu pop = createRelationPopupMenu(cell, false);
//						pop.show(graphComponent, graphComponent.getGraphControl().getMousePosition().x, graphComponent.getGraphControl().getMousePosition().y);
//					}
//					else if (mxConstants.ARROW_DIAMOND.equals(cell.getStyle())) {
//						Object cells[] = {cell};
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_DIAMOND);
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_HORIZONTAL);
//					}
//					else if (mxConstants.ARROW_OPEN.equals(cell.getStyle())) {
//						Object cells[] = {cell};
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OPEN);
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_HORIZONTAL);
//					}
//					else if (mxConstants.ARROW_OVAL.equals(cell.getStyle())) {
//						Object cells[] = {cell};
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDARROW, mxConstants.ARROW_OVAL);
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_ENDSIZE, "12");
//						mxUtils.setCellStyles(this, cells, mxConstants.STYLE_STROKECOLOR, SYMBOLCOLOR);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_VERTICAL);
//						mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, mxConstants.STYLE_ELBOW , mxConstants.ELBOW_HORIZONTAL);
//					}
//					else if(cell.getStyle() != null && cell.getStyle().indexOf(ENTITYSTYLE) >= 0) {
//						JPopupMenu pop = createPopupMenuEntity(cell, true);
//						pop.show(graphComponent, graphComponent.getGraphControl().getMousePosition().x, graphComponent.getGraphControl().getMousePosition().y);
//					}
//				}
//				return obj;
//			}
//		};
//		return model;
//	}

	private void addEventListener(mxGraph myGraph) {
		myGraph.addListener(mxEvent.ADD_CELLS, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		myGraph.addListener(mxEvent.CELLS_ADDED, new MyAddEventListener());
		
		
		
		myGraph.addListener(mxEvent.ADD, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				fireChangeListenEvent();				
			}
		});
		
		myGraph.addListener(mxEvent.CELL_CONNECTED, new mxIEventListener() {
			
			@Override
			public void invoke(Object sender, mxEventObject evt) {
				if(sender instanceof StatemodelGraph) {
					StatemodelGraph graph = (StatemodelGraph)sender;
					mxCell cell = (mxCell)graph.getSelectionModel().getCell();					
					if(cell != null && cell.getStyle() != null) {
						if(cell.getStyle().indexOf("endArrow=open") >= 0 || cell.getStyle().indexOf("endArrow=oval") >= 0) {
							if(cell.getValue() != null && cell.getValue() instanceof StateTransitionVO) {
								Boolean blnSource = (Boolean)evt.getProperty("source");
								mxCell cellPrevious = (mxCell)evt.getProperty("previous");
								mxCell cellTerminal = (mxCell)evt.getProperty("terminal");
								if(blnSource){
									if(cellTerminal != null && cellPrevious != null) {
										
									}
									else if(cellTerminal == null) {
										cell.setSource(cellPrevious);
									}
								}
								else {
									if(cellTerminal != null && cellPrevious != null) {
									}
									else if(cellTerminal == null) {
										cell.setTarget(cellPrevious);
									}
								}
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
		JMenuItem i1 = new JMenuItem("clear");
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				model.clear();
				fireChangeListenEvent();
			}
		});
		
				
		JMenuItem i2 = new JMenuItem("zoom out");
		i2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				graphComponent.zoomIn();
			}
		});
		
		JMenuItem i3 = new JMenuItem("zoom in");
		i3.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				graphComponent.zoomOut();
			}
		});
	
		
		pop.add(i2);
		pop.add(i3);
		pop.addSeparator();
		pop.add(i1);
		
		return pop;	
		
	}
	
	protected JPopupMenu createRelationPopupMenu(final mxCell cell, boolean delete) {
		
		JPopupMenu pop = new JPopupMenu();
	
		JMenuItem i4 = new JMenuItem("Status\u00fcbergang l\u00f6schen");
		i4.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cell.getStyle() != null) {
					if(cell.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
						JOptionPane.showMessageDialog(StateModelEditor2.this, "Element kann nicht gel\u00f6scht werden!");
						return;
					}
				}
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();				
				StateTransitionVO vo = (StateTransitionVO)cell.getValue();
				vo.remove();
				stateGraphVO.getTransitions().add(vo);				
				model.remove(cell);
				fireChangeListenEvent();
			}
		});
		
		pop.add(i4);
		
		return pop;	
		
	}

	protected JPopupMenu createPopupMenuEntity(final mxCell cell, boolean newCell) {
		
		JPopupMenu pop = new JPopupMenu();
		JMenuItem i1 = new JMenuItem("Status l\u00f6schen");
		i1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				int count = cell.getEdgeCount();
				for(int i = 0; i < count; i++) {
					mxCell cellEdge = (mxCell)cell.getEdgeAt(i);
					if(cellEdge.getStyle() != null) {
						if(cellEdge.getStyle().indexOf("startArrow=oval;startSize=25") >= 0) {
							JOptionPane.showMessageDialog(StateModelEditor2.this, "1. Status kann nicht gel\u00f6scht werden!");
							return;
						}
					}
				}
				mxGraphModel model =  (mxGraphModel)graphComponent.getGraph().getModel();
				StateVO vo =  (StateVO)cell.getValue();
				vo.remove();
				stateGraphVO.getStates().add(vo);
				model.remove(cell);			
				fireChangeListenEvent();
			}
		});
		
		if(!newCell)
			pop.add(i1);
		
		if(cell.getStyle() == null || !(cell.getStyle().indexOf(ENTITYSTYLE) >= 0)) {
			return pop;
		}
	
		return pop;	
		
	}

	
	protected void fillPalette(EditorPalette shapesPalette) {
		shapesPalette.addTemplate("Status",	new ImageIcon(StateModelEditor2.class.getResource("/org/nuclos/client/relation/images/rounded.png")),	ENTITYSTYLE, 100, 80, "");
	}
	
	public EditorPalette insertPalette(String title)
	{
		final EditorPalette palette = new EditorPalette(60);
		final JScrollPane scrollPane = new JScrollPane(palette);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		libraryPane.add(title, scrollPane);

		// Updates the widths of the palettes if the container size changes
		libraryPane.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				int w = scrollPane.getWidth()
						- scrollPane.getVerticalScrollBar().getWidth();
				palette.setPreferredWidth(w);
			}

		});

		return palette;
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
	
	protected void addFirstTransitionIfNecessary(mxCell cellAdded) {
		
		mxCell cellRoot = (mxCell)graphComponent.getGraph().getModel().getRoot();
		mxCell cellContainer = (mxCell)cellRoot.getChildAt(0);
		if(cellContainer.getChildCount() == 1) {
			StateTransitionVO vo = new StateTransitionVO(clientId--, null, null, "", false);
			
			mxGeometry geo = new mxGeometry(20, 20, 20, 20);
			geo.setSourcePoint(new mxPoint(20,20));
			geo.setTargetPoint(new mxPoint(150,150));
			mxCell cellTransition = new mxCell(vo, geo, "dashed=1;endArrow=open;endSize=12;startArrow=oval;startSize=25");
			cellTransition.setTarget(cellAdded);
			cellTransition.setEdge(true);
			graphComponent.getGraph().getModel().add(cellContainer, cellTransition, 1);
		}
		
	}
	
	public void createNewStateModel(StateModelVO statemodelvo) {
		
		this.stateGraphVO = new StateGraphVO(statemodelvo);
		((mxGraphModel)this.graphComponent.getGraph().getModel()).clear();
		
	}

	
	
	
	// old stuff
	
	
	public StateGraphVO prepareForSaving(StateModelVO statemodelvo) throws CommonBusinessException {

		validateStates();
		
		adjustModelForSaving();
		
		StateGraphVO vo = new StateGraphVO(statemodelvo);
		vo.setStates(this.stateGraphVO.getStates());
		vo.setTransitions(this.stateGraphVO.getTransitions());
		
		return vo;
		
	}
	
	private Map<Integer, String> validateStates() throws CommonValidationException {
		final Map<Integer, String> result = CollectionUtils.newHashMap();
		final Map<Integer, String> numerals = CollectionUtils.newHashMap();
		for (StateVO statevo : this.getStates()) {
			if (!statevo.isRemoved()) {		//ignore removed states
				if (StringUtils.isNullOrEmpty(statevo.getStatename())) {
					throw new CommonValidationException("statemachine.error.validation.graph.statename");
				}
				if (StringUtils.isNullOrEmpty(statevo.getDescription())) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statedescription",statevo.getStatename()));
				}
				if(statevo.getNumeral() == null) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statenumeral",statevo.getStatename()));
				}
				/** numeral is set as maximum 3,0 in the database, everything higher than 999 will cause a exception */
				if(statevo.getNumeral().intValue() > 999) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.statenumeral.toolong",statevo.getStatename()));
				}

				/** @todo ? */
				/*if ((statevo.getButtonlabel() == null) || (statevo.getButtonlabel().equals(""))) {
					 for (Iterator j = this.getTransitionIds().iterator(); j.hasNext();) {
						 StateTransitionVO voStateTransition = (StateTransitionVO) j.next();
						 if ((voStateTransition.getStateTarget() == statevo.getClientId()) && (!voStateTransition.isAutomatic())) {
							 throw new CommonValidationException(
									 NuclosServerResources.getString("statemachine.error.validation.graph.statelabel"));
						 }
					 }
				 }*/

				if (result.containsKey(statevo.getClientId())) {
					throw new CommonValidationException("statemachine.error.validation.graph.duplicateid");
				}
				if (result.containsValue(statevo.getStatename())) {
					throw new CommonValidationException("statemachine.error.validation.graph.duplicatestate");
				}
				if (numerals.containsKey(statevo.getNumeral())) {
					throw new CommonValidationException(StringUtils.getParameterizedExceptionMessage("statemachine.error.validation.graph.duplicatenumerals",
						 numerals.get(statevo.getNumeral()),  statevo.getStatename()));
				}
				result.put(statevo.getClientId(), statevo.getStatename());
				numerals.put(statevo.getNumeral(), statevo.getStatename());
			}
		}
		return result;
	}
	
	private List<StateVO> getStates() {
		List<StateVO> lstStates = new ArrayList<StateVO>();
		mxGraphModel model = new mxGraphModel(this.graphComponent.getGraph().getModel().getRoot());

		mxCell root = (mxCell)model.getRoot();
		mxCell containerCell = (mxCell)root.getChildAt(0);
		
		int childcount = containerCell.getChildCount();
		for(int i = 0; i < childcount; i++) {
			mxCell child = (mxCell)containerCell.getChildAt(i);			
			if(child.getValue() != null && child.getValue() instanceof StateVO) {
				lstStates.add((StateVO)child.getValue());
			}
		}
		
		return lstStates;
	}

	
	protected void adjustModelForSaving() {
		mxGraphModel model = new mxGraphModel(this.graphComponent.getGraph().getModel().getRoot());

		mxCell root = (mxCell)model.getRoot();
		mxCell containerCell = (mxCell)root.getChildAt(0);
		
		int childcount = containerCell.getChildCount();
		for(int i = 0; i < childcount; i++) {
			mxCell child = (mxCell)containerCell.getChildAt(i);
			
			if(child.getValue() != null && child.getValue() instanceof StateVO) {
								
			}
			else if(child.getValue() != null && child.getValue() instanceof StateTransitionVO) {
				StateTransitionVO transVO = (StateTransitionVO)child.getValue();
				
				
				mxCell cellSource = (mxCell)child.getSource();
				if(cellSource != null) {
					StateVO voSource = (StateVO)cellSource.getValue();
					transVO.setStateSource(voSource.getClientId());
				}
				mxCell cellTarget = (mxCell)child.getTarget();
				
				StateVO voTarget = (StateVO)cellTarget.getValue();
				
				transVO.setStateTarget(voTarget.getClientId());
			}
		}

	}
	
	public String getModelAsXML() {
		mxCodec codec = new mxCodec();
		return mxUtils.getXml(codec.encode(convertModelForSaving()));
	}
	
	protected mxGraphModel convertModelForSaving() {
		
		mxGraphModel model = new mxGraphModel(this.graphComponent.getGraph().getModel().getRoot());

		mxCell root = (mxCell)model.getRoot();
		mxCell containerCell = (mxCell)root.getChildAt(0);
		
		int childcount = containerCell.getChildCount();
		for(int i = 0; i < childcount; i++) {
			mxCell child = (mxCell)containerCell.getChildAt(i);
			
			if(child.getValue() != null && child.getValue() instanceof StateVO) {
				StateVO stateVO = (StateVO)child.getValue();
				String sStateName = stateVO.getStatename();
				
				if(stateVO.getId() == null) {
					this.stateGraphVO.getStates().add(stateVO);
				}
				
				
				child.setValue(sStateName);
			}
			else if(child.getValue() != null && child.getValue() instanceof StateTransitionVO) {
				StateTransitionVO transVO = (StateTransitionVO)child.getValue();
				String sTransition = String.valueOf(transVO.getId());
				child.setValue(sTransition);
				if(transVO.getId() == null) {
					this.stateGraphVO.getTransitions().add(transVO);
				}
			}	
			
		}
		
		
		
		return model;		
	}
	
	
	public void removeRule(SortedRuleVO vo) throws CreateException, RemoteException {

		final mxCell selectedCell = (mxCell)graphComponent.getGraph().getSelectionCell();

		if(selectedCell.getValue() instanceof StateTransitionVO) {			
			StateTransitionVO voTransition = (StateTransitionVO)selectedCell.getValue();
			voTransition.removeRule(vo.getId());
			pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(voTransition.getRuleIdsWithRunAfterwards()));
			if(voTransition.getRuleIdsWithRunAfterwards().size() == 0) {
				mxCell[] cells = {selectedCell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "endArrow", "open");				
			}
			else {
				mxCell[] cells = {selectedCell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "endArrow", "oval");
			}
			fireChangeListenEvent();
		}
		
	}

	public void addRule(final SortedRuleVO vo) throws CreateException, RemoteException {
		
		final mxCell selectedCell = (mxCell)graphComponent.getGraph().getSelectionCell();

		if(selectedCell.getValue() instanceof StateTransitionVO) {
			StateTransitionVO voTransition = (StateTransitionVO)selectedCell.getValue();
			voTransition.getRuleIdsWithRunAfterwards().add(new Pair<Integer, Boolean>(vo.getId(), vo.isRunAfterwards()));
			pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(voTransition.getRuleIdsWithRunAfterwards()));
			if(voTransition.getRuleIdsWithRunAfterwards().size() == 0) {
				mxCell[] cells = {selectedCell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "endArrow", "open");				
			}
			else {
				mxCell[] cells = {selectedCell};
				mxUtils.setCellStyles(graphComponent.getGraph().getModel(), cells, "endArrow", "oval");
			}
			fireChangeListenEvent();
		}
		
	}
	
	
	public void addRole(MasterDataVO mdvo) throws CreateException, RemoteException {
		
		final mxCell selectedCell = (mxCell)graphComponent.getGraph().getSelectionCell();
		if(selectedCell.getValue() instanceof StateTransitionVO) {
			StateTransitionVO voTransition = (StateTransitionVO)selectedCell.getValue();
			voTransition.getRoleIds().add(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(voTransition.getRoleIds()));
			fireChangeListenEvent();
		}
		
	}

	public void removeRole(MasterDataVO mdvo) throws CreateException, RemoteException {
		
		final mxCell selectedCell = (mxCell)graphComponent.getGraph().getSelectionCell();
		if(selectedCell.getValue() instanceof StateTransitionVO) {
			StateTransitionVO voTransition = (StateTransitionVO)selectedCell.getValue();
			voTransition.getRoleIds().remove(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(voTransition.getRoleIds()));
			fireChangeListenEvent();
		}

	}
	
	
	public StateModelEditorPropertiesPanel getStateModelEditorPropertiesPanel(){
		return this.pnlProperties;
	}

	public void addPrintEventListener(ActionListener al) {
		lstPrintEventListeners.add(al);
	}

	public void removePrintEventListener(ActionListener al) {
		lstPrintEventListeners.remove(al);
	}

	/**
	 * adds listeners for the state (properties) panel.
	 */
	private void addStatePanelListeners() {
		pnlProperties.getStatePropertiesPanel().getModel().docName.addDocumentListener(nameDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docMnemonic.addDocumentListener(mnemonicDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docDescription.addDocumentListener(descriptionDocumentListener);
//		pnlProperties.getStatePropertiesPanel().getModel().modelReserve0.addListDataListener(reserve0DataListener);
//		pnlProperties.getStatePropertiesPanel().getModel().modelReserve1.addListDataListener(reserve1DataListener);
	}

	/**
	 * removes the listeners for the state (properties) panel.
	 */
	private void removeStatePanelListeners() {
		pnlProperties.getStatePropertiesPanel().getModel().docName.removeDocumentListener(nameDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docMnemonic.removeDocumentListener(mnemonicDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docDescription.removeDocumentListener(descriptionDocumentListener);
//		pnlProperties.getStatePropertiesPanel().getModel().modelReserve0.removeListDataListener(reserve0DataListener);
//		pnlProperties.getStatePropertiesPanel().getModel().modelReserve1.removeListDataListener(reserve1DataListener);
	}

//	/**
//	 * adds listeners for the note (properties) panel.
//	 */
//	private void addNotePanelListeners() {
//		pnlProperties.getNotePanel().getModel().docText.addDocumentListener(noteDocumentListener);
//	}
//
//	/**
//	 * removes the listeners for the note (properties) panel.
//	 */
//	private void removeNotePanelListeners() {
//		pnlProperties.getNotePanel().getModel().docText.removeDocumentListener(noteDocumentListener);
//	}

	
//	private void setupSubforms(StateVO statevo) {
//
//		/** @todo this is just a test - correct this */
//		ctlsubformRole = new StateRoleSubFormController(statevo);
//		ctlsubformAttributeGroup = new StateRoleAttributeGroupSubFormController(statevo);
//		ctlsubformSubForm = new StateRoleSubFormsSubFormController(statevo);
//	}
//	
////	private void setupTransitionSubform(StateTransitionVO transitionvo) {
////		ctlsubformRuleTransition = new TransitionRuleSubFormController(transitionvo);
////				
////		ctlsubformRuleTransition.getSubForm().btnFilter.setVisible(false);
////		
////	}

//	private void closeSubForms() {
//	
//		if (ctlsubformAttributeGroup != null) {
//			/** @todo endEditing here? */
//			ctlsubformAttributeGroup.close();
//			ctlsubformAttributeGroup = null;
//		}
//		if(ctlsubformSubForm != null) {
//			/** @todo endEditing here? */
//			ctlsubformSubForm.close();
//			ctlsubformSubForm = null;
//		}
//		if (ctlsubformRole != null) {
//			/** @todo endEditing here? */
//			ctlsubformRole.close();
//			ctlsubformRole = null;
//		}
////		if(ctlsubformRuleTransition != null) {
////			ctlsubformRuleTransition.close();
////			ctlsubformRuleTransition = null;
////		}
//	}


	public void createNewStatemodel() {
		mxCell cellRoot = (mxCell)graphComponent.getGraph().getModel().getRoot();
		mxCell cellContainer = (mxCell)cellRoot.getChildAt(0);
		int index = 0;
		
		mxGeometry mxgeo = new mxGeometry(60, 60, 100, 80);
		StateVO voState = new StateVO(clientId--, null, "", "", null);
		
		mxCell child = new mxCell(voState, mxgeo, ENTITYSTYLE);
		child.setVertex(true);
		graphComponent.getGraph().getModel().add(cellContainer, child, index++);
		
		mxGeometry geo = new mxGeometry(20, 20, 20, 20);
		geo.setSourcePoint(new mxPoint(20,20));
		geo.setTargetPoint(new mxPoint(150,150));
		StateTransitionVO vo = new StateTransitionVO(clientId--, null, null, "", false);
		mxCell cellTransition = new mxCell(vo, geo, "dashed=1;endArrow=open;endSize=12;startArrow=oval;startSize=25");
		cellTransition.setTarget(child);
		cellTransition.setEdge(true);
		graphComponent.getGraph().getModel().add(cellContainer, cellTransition, index++);
	
	}


	public void printStateModel() {
		for (ActionListener al : lstPrintEventListeners) {
			al.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}


	@SuppressWarnings("deprecation")
	private static CollectableField getCollectableFieldRole(final Integer iRoleId) {
		try {
			return MasterDataDelegate.getInstance().getCollectableField(NuclosEntity.ROLE.getEntityName(), iRoleId);
		}
		catch (CommonBusinessException ex) {
			throw new NuclosFatalException(ex);
		}
	}

	public boolean stopEditing() {
		/** @todo It might be better to programmatically deselect the currently selected object(s) here.
		 * closeSubForms() would then be called if needed. */
		updateStateProperties();
		// this.closeSubForms();
		return true;
	}
	
	private void setupRightsPanel(StateVO statevo) {
		if (usages != null) 
			pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().setup(usages, statevo);
	}
	
	private void updateStateProperties() {
		pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().updateStateVO();
	}
	
	public void setUsages(List<CollectableEntityObject> usages) {
		this.usages = usages;
		if (usages != null)
			pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().setup(usages);
	}

//	private class StateRoleSubFormController extends DetailsSubFormController<CollectableStateRole> {
//		private final StateVO statevo;
//
//		private ListSelectionListener listselectionlistener;
//		private ChangeListener changelistener;
//
//		StateRoleSubFormController(StateVO statevo) {
//			/** @todo fix this */
//			super(StateModelEditor2.this, null, null, NuclosEntity.STATE.getEntityName(),
//					pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformRoles(),
//					ClientPreferences.getUserPreferences().node("tmp"),
//					MasterDataCollectableFieldsProviderFactory.newFactory(null));
//			this.statevo = statevo;
//
//			this.readFromStateVO();
//
//			this.listselectionlistener = new ListSelectionListener() {
//				@Override
//                public void valueChanged(ListSelectionEvent ev) {
//					if (!ev.getValueIsAdjusting()) {
//						log.debug("listselectionlistener for roles");
//						StateRoleSubFormController.this.stopEditing();
////						final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();
////						ctlsubformAttributeGroup.getSubForm().setVisible(!lsm.isSelectionEmpty());
////						ctlsubformAttributeGroup.setRole(StateRoleSubFormController.this.getSelectedRole());
//						StateRoleSubFormController.this.setRole(StateRoleSubFormController.this.getSelectedRole());
//						fireChangeListenEvent();
//					}
//				}
//			};
//			this.getSubForm().getJTable().getSelectionModel().addListSelectionListener(listselectionlistener);
//
//			this.changelistener = new ChangeListener() {
//				@Override
//                public void stateChanged(ChangeEvent ev) {
//					log.debug("changelistener for roles");
//					// Note that stopEditing causes a recursive call to stateChanged.
//					// It's necessary to call stopEditing here because otherwise the changed value
//					// won't be transferred to the table model.
//					StateRoleSubFormController.this.stopEditing();
//
//
//					//ctlsubformAttributeGroup.setRole(StateRoleSubFormController.this.getSelectedRole());
//					StateRoleSubFormController.this.setRole(StateRoleSubFormController.this.getSelectedRole());
//					
//					fireChangeListenEvent();
//				}
//			};
//			this.getSubForm().addChangeListener(changelistener);
//		}
//
////		fill the subforms (attributegroups and subforms) corresponding to the selected role
//		private void setRole(CollectableField selectedField) {
//			if(selectedField == null)
//				return;
//			Integer iSelectedSubform = StateModelEditor2.this.pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSelectedSubform();
//			if(iSelectedSubform == 0) {
//				ctlsubformAttributeGroup.setRole(selectedField);
//				ctlsubformSubForm.setRole(selectedField);
//			}
//			else {
//				ctlsubformSubForm.setRole(selectedField);
//				ctlsubformAttributeGroup.setRole(selectedField);
//			}
//		}
//
//		@Override
//		public void close() {
//			this.getSubForm().getJTable().getSelectionModel().removeListSelectionListener(this.listselectionlistener);
//			this.listselectionlistener = null;
//
//			this.getSubForm().removeChangeListener(this.changelistener);
//			this.changelistener = null;
//
//			super.close();
//		}
//
//		@Override
//		public CollectableStateRole newCollectable() {
//			return new CollectableStateRole();
//		}
//
//		@Override
//		protected ValueObjectList<CollectableStateRole> newValueObjectList(List<CollectableStateRole> lstclct) {
//			return new NonMarkingValueObjectList<CollectableStateRole>(lstclct);
//		}
//
//		private void readFromStateVO() {
//			// fill the subform roles from the statevo:
//			final Integer iStateId = statevo.getId();
//
//			class MakeCollectable implements Transformer<Integer, CollectableStateRole> {
//				@Override
//                public CollectableStateRole transform(Integer iRoleId) {
//					final CollectableStateRole result = new CollectableStateRole();
//					result.setField(CollectableStateRole.FIELDNAME_STATE, new CollectableValueIdField(iStateId, null));
//					result.setField(CollectableStateRole.FIELDNAME_ROLE, getCollectableFieldRole(iRoleId));
//					return result;
//				}
//			}
//
//			this.fillSubForm(iStateId, CollectionUtils.transform(this.getRoleIdsFromStateVO(statevo), new MakeCollectable()));
//		}
//
//		private Collection<Integer> getRoleIdsFromStateVO(StateVO statevo) {
//			Collection<Integer> collRoleIds = new ArrayList<Integer>(statevo.getUserRights().keySet());
//			for(Integer iRoleId : statevo.getUserSubformRights().keySet()) {
//				if(!collRoleIds.contains(iRoleId)) {
//					collRoleIds.add(iRoleId);
//				}
//			}
//			return collRoleIds;
//		}
//
//		private void fillSubForm(Object oParentId, List<CollectableStateRole> lstclct) {
//			this.setParentId(oParentId);
//
//			this.updateTableModel(lstclct);
//		}
//
//		/**
//		 * @return the selected role id, if any
//		 */
//		public CollectableField getSelectedRole() {
//			final Collectable clctSelected = this.getSelectedCollectable();
//			return (clctSelected == null) ? null : clctSelected.getField(CollectableStateRole.FIELDNAME_ROLE);
//		}
//
//		public Set<Integer> getRoleIds() {
//			class ExtractRoleId implements Transformer<Collectable, Integer> {
//				@Override
//                public Integer transform(Collectable clct) {
//					return (Integer) clct.getField(CollectableStateRole.FIELDNAME_ROLE).getValueId();
//				}
//			}
//			return new HashSet<Integer>(CollectionUtils.transform(getCollectableTableModel().getCollectables(), new ExtractRoleId()));
//		}
//
////		private void writeToStateVO() {
////			final StateVO.UserRights userrights = new StateVO.UserRights();
////			for (Iterator iter = this.getRoleIds().iterator(); iter.hasNext();) {
////				final Integer iRoleId = (Integer) iter.next();
////				/** @todo null is not a valid entry */
////				userrights.put(iRoleId, null);
////			}
////			statevo.setUserRights(userrights);
////		}
//
//		@Override
//		protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactory() {
//			return MasterDataCollectableFieldsProviderFactory.newFactory(null);
//		}
//	}	// inner class StateRoleSubFormController

//	private class StateRoleAttributeGroupSubFormController extends DetailsSubFormController<CollectableStateRoleAttributeGroup> {
//		private final StateVO statevo;
//
//		private Map<CollectableField, List<CollectableStateRoleAttributeGroup>> mpModel;
//
//		private CollectableField clctfRole;
//		private ChangeListener changelistener;
//
//		StateRoleAttributeGroupSubFormController(StateVO statevo) {
//			super(StateModelEditor2.this, null, null, "staterole", pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformAttributeGroups(),
//					ClientPreferences.getUserPreferences().node("tmp"),
//					MasterDataCollectableFieldsProviderFactory.newFactory(null));
//			this.statevo = statevo;
//			this.readFromStateVO();
//			this.getSubForm().setVisible(false);
//
//			this.changelistener = new ChangeListener() {
//				@Override
//                public void stateChanged(ChangeEvent ev) {
//					writeToStateVO();
//					StateModelEditor2.this.fireChangeListenEvent();
//				}
//			};
//		}
//
//		private CollectableField getRole() {
//			return this.clctfRole;
//		}
//
//		private void setRole(CollectableField clctfRole) {
//			final boolean bEditingStopped = this.stopEditing();
//			assert bEditingStopped;
//
//			ctlsubformAttributeGroup.getSubForm().setVisible(clctfRole != null && !clctfRole.isNull());
//
//			if (LangUtils.equals(clctfRole, this.getRole())) {
//				log.debug("setRole called with current role.");
//			}
//			else {
//				log.debug("setRole: OLD ROLE: " + this.getRole() + " - NEW ROLE: " + clctfRole);
//				if (this.getRole() != null) {
//					// transfer current list (table model) to the (intermediate) model:
//					mpModel.put(this.getRole(), new ArrayList<CollectableStateRoleAttributeGroup>(getCollectableTableModel().getCollectables()));
//				}
//
//				this.clctfRole = clctfRole;
//
//				this.getSubForm().removeChangeListener(changelistener);
//
//				// transfer intermediate model for the new role id to the table model:
//				final List<CollectableStateRoleAttributeGroup> lstclct = mpModel.get(this.getRole());
//				this.getCollectableTableModel().setCollectables((lstclct == null) ? new ArrayList<CollectableStateRoleAttributeGroup>() : lstclct);
//
//				this.getSubForm().addChangeListener(changelistener);
//			}
//		}
//
//		@Override
//		public CollectableStateRoleAttributeGroup newCollectable() {
//			return new CollectableStateRoleAttributeGroup(this.getRole(),
//					new AttributegroupPermissionVO(null, null, null, null, null, null, false));
//		}
//
//		@Override
//		protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactory() {
//			return MasterDataCollectableFieldsProviderFactory.newFactory(null);
//		}
//
//		@Override
//		protected ValueObjectList<CollectableStateRoleAttributeGroup> newValueObjectList(List<CollectableStateRoleAttributeGroup> lstclct) {
//			return new NonMarkingValueObjectList<CollectableStateRoleAttributeGroup>(lstclct);
//		}
//
//		private void readFromStateVO() {
//			this.mpModel = CollectionUtils.newHashMap();
//			for (Integer iRoleId : this.statevo.getUserRights().keySet()) {
//				final CollectableField clctfRole = getCollectableFieldRole(iRoleId);
//				final Collection<AttributegroupPermissionVO> collagpvo = this.statevo.getUserRights().getValues(iRoleId);
//				mpModel.put(clctfRole, CollectionUtils.transform(collagpvo, new CollectableStateRoleAttributeGroup.MakeCollectable(clctfRole)));
//			}
//		}
//
//		private void writeToStateVO() {
//			this.stopEditing();
//			if (this.getRole() != null) {
//				// transfer current list (table model) to the (intermediate) model:
//				mpModel.put(this.getRole(), new ArrayList<CollectableStateRoleAttributeGroup>(getCollectableTableModel().getCollectables()));
//			}
//
//			final StateVO.UserRights userrights = new StateVO.UserRights();
//			for (Integer iRoleId : ctlsubformRole.getRoleIds()) {
//				final List<? extends Collectable> lst = mpModel.get(getCollectableFieldRole(iRoleId));
//				if (lst != null) {
//					for (Collectable clct : lst) {
//						final CollectableStateRoleAttributeGroup clctsrag = (CollectableStateRoleAttributeGroup) clct;
//						userrights.addValue(iRoleId, clctsrag.getAttributegroupPermissionVO());
//					}
//				}
//			}
//			this.statevo.setUserRights(userrights);
//		}
//
//		@Override
//		public void close() {
//			this.writeToStateVO();
//			this.getSubForm().removeChangeListener(changelistener);
//			this.getSubForm().setVisible(false);
//			super.close();
//		}
//
//	}	// inner class StateRoleAttributeGroupSubFormController

//	private class StateRoleSubFormsSubFormController extends DetailsSubFormController<CollectableStateRoleSubForm> {
//		private final StateVO statevo;
//
//		private Map<CollectableField, List<CollectableStateRoleSubForm>> mpModel;
//
//		private CollectableField clctfRole;
//		private ChangeListener changelistener;
//
//		StateRoleSubFormsSubFormController(StateVO statevo) {
//			super(StateModelEditor2.this, null, null, "staterole", pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformSubForms(),
//					ClientPreferences.getUserPreferences().node("tmp"),
//					new MasterDataCollectableFieldsProviderFactory(null));
//
//			this.statevo = statevo;
//			this.readFromStateVO();
//
//			this.changelistener = new ChangeListener() {
//				@Override
//                public void stateChanged(ChangeEvent ev) {
//					writeToStateVO();
//				}
//			};
//		}
//
//		private CollectableField getRole() {
//			return this.clctfRole;
//		}
//
//		private void setRole(CollectableField clctfRole) {
//			final boolean bEditingStopped = this.stopEditing();
//			assert bEditingStopped;
//
//			this.getSubForm().setVisible(clctfRole != null && !clctfRole.isNull());
//
//			if (LangUtils.equals(clctfRole, this.getRole())) {
//				log.debug("setRole called with current role.");
//			}
//			else {
//				log.debug("setRole: OLD ROLE: " + this.getRole() + " - NEW ROLE: " + clctfRole);
//				if (this.getRole() != null) {
//					// transfer current list (table model) to the (intermediate) model:
//					mpModel.put(this.getRole(), new ArrayList<CollectableStateRoleSubForm>(getCollectableTableModel().getCollectables()));
//				}
//
//				this.clctfRole = clctfRole;
//
//				this.getSubForm().removeChangeListener(changelistener);
//
//				// transfer intermediate model for the new role id to the table model:
//				final List<CollectableStateRoleSubForm> lstclct = mpModel.get(this.getRole());
//				this.getCollectableTableModel().setCollectables((lstclct == null) ? new ArrayList<CollectableStateRoleSubForm>() : lstclct);
//
//				this.getSubForm().addChangeListener(changelistener);
//			}
//		}
//
//		@Override
//		public CollectableStateRoleSubForm newCollectable() {
//			return new CollectableStateRoleSubForm(this.getRole(),
//					new SubformPermissionVO(null, null, null, null, null, null, false));
//		}
//
//		@Override
//		protected CollectableFieldsProviderFactory getCollectableFieldsProviderFactory() {
//			return new MasterDataCollectableFieldsProviderFactory(null);
//		}
//
//		@Override
//		protected ValueObjectList<CollectableStateRoleSubForm> newValueObjectList(List<CollectableStateRoleSubForm> lstclct) {
//			return new NonMarkingValueObjectList<CollectableStateRoleSubForm>(lstclct);
//		}
//
//		private void readFromStateVO() {
//			this.mpModel = new HashMap<CollectableField, List<CollectableStateRoleSubForm>>();
//			for (Integer iRoleId : this.statevo.getUserSubformRights().keySet()) {
//				final CollectableField clctfRole = getCollectableFieldRole(iRoleId);
//				final Collection<SubformPermissionVO> collagpvo = this.statevo.getUserSubformRights().getValues(iRoleId);
//				mpModel.put(clctfRole, CollectionUtils.transform(collagpvo, new CollectableStateRoleSubForm.MakeCollectable(clctfRole)));
//			}
//		}
//
//		private void writeToStateVO() {
//			this.stopEditing();
//			if (this.getRole() != null) {
//				// transfer current list (table model) to the (intermediate) model:
//				mpModel.put(this.getRole(), new ArrayList<CollectableStateRoleSubForm>(getCollectableTableModel().getCollectables()));
//			}
//
//			final StateVO.UserSubformRights usersubformrights = new StateVO.UserSubformRights();
//			for (Integer iRoleId : ctlsubformRole.getRoleIds()) {
//				final List<? extends Collectable> lst = mpModel.get(getCollectableFieldRole(iRoleId));
//				if (lst != null) {
//					for (Collectable clct : lst) {
//						final CollectableStateRoleSubForm clctsrag = (CollectableStateRoleSubForm) clct;
//						usersubformrights.addValue(iRoleId, clctsrag.getSubformPermissionVO());
//					}
//				}
//			}
//			this.statevo.setUserSubformRights(usersubformrights);
//		}
//
//		@Override
//		public void close() {
//			this.writeToStateVO();
//			this.getSubForm().removeChangeListener(changelistener);
//			this.getSubForm().setVisible(false);
//			super.close();
//		}
//
//	}  // inner class StateRoleSubFormsSubFormController


	
	private class NameDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateName(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateName(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateName(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}
	}
	
	public void changeStateName(String name) {
		mxCell cell = (mxCell)graphComponent.getGraph().getSelectionCell();
		if(cell == null)
			return;
		StateVO vo = (StateVO)cell.getValue();
		vo.setStatename(name);
		graphComponent.getGraph().refresh();
		fireChangeListenEvent();
	}

	private class MnemonicDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateMnemonic(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}			
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateMnemonic(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateMnemonic(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}
	}
	
	public void changeStateMnemonic(String mnemonic) {
		mxCell cell = (mxCell)graphComponent.getGraph().getSelectionCell();
		if(cell == null)
			return;
		StateVO vo = (StateVO)cell.getValue();
		try {
			vo.setNumeral(new Integer(mnemonic));
			fireChangeListenEvent();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}

	private class DescriptionDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateDescription(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateDescription(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			try {
				StateModelEditor2.this.changeStateDescription(e.getDocument().getText(0, e.getDocument().getLength()));
			}
			catch(BadLocationException e1) {
			}
		}
	}
	
	public void changeStateDescription(String desc) {
		mxCell cell = (mxCell)graphComponent.getGraph().getSelectionCell();
		if(cell == null)
			return;
		StateVO vo = (StateVO)cell.getValue();
		vo.setDescription(desc);
		fireChangeListenEvent();
	}

//	private class Reserve0DataListener implements ListDataListener {
//		@Override
//        public void contentsChanged(ListDataEvent e) {
//			//StateModelEditor.this.changeStateReserve0();
//		}
//
//		@Override
//        public void intervalAdded(ListDataEvent e) {
//		}
//
//		@Override
//        public void intervalRemoved(ListDataEvent e) {
//		}
//	}

//	private class Reserve1DataListener implements ListDataListener {
//		@Override
//        public void contentsChanged(ListDataEvent e) {
//			//StateModelEditor.this.changeStateReserve1();
//		}
//
//		@Override
//        public void intervalAdded(ListDataEvent e) {
//		}
//
//		@Override
//        public void intervalRemoved(ListDataEvent e) {
//		}
//	}

//	private class NoteDocumentListener implements DocumentListener {
//		@Override
//        public void changedUpdate(DocumentEvent e) {
//			//StateModelEditor.this.changeNoteText();
//		}
//
//		@Override
//        public void insertUpdate(DocumentEvent e) {
//			//StateModelEditor.this.changeNoteText();
//		}
//
//		@Override
//        public void removeUpdate(DocumentEvent e) {
//			//StateModelEditor.this.changeNoteText();
//		}
//	}
	
//	private StateRoleSubFormController ctlsubformRole;
//	private StateRoleAttributeGroupSubFormController ctlsubformAttributeGroup;
//	private StateRoleSubFormsSubFormController ctlsubformSubForm;
//	private TransitionRuleSubFormController ctlsubformRuleTransition;

//	private final List<ChangeListener> lstChangeListeners = new Vector<ChangeListener>();
	
	private final List<ActionListener> lstPrintEventListeners = new Vector<ActionListener>();
	private final NameDocumentListener nameDocumentListener = new NameDocumentListener();
	private final MnemonicDocumentListener mnemonicDocumentListener = new MnemonicDocumentListener();
	private final DescriptionDocumentListener descriptionDocumentListener = new DescriptionDocumentListener();
//	private final Reserve0DataListener reserve0DataListener = new Reserve0DataListener();
//	private final Reserve1DataListener reserve1DataListener = new Reserve1DataListener();
//	private final NoteDocumentListener noteDocumentListener = new NoteDocumentListener();

	
	class StatemodelGraph extends mxGraph {
		
		public StatemodelGraph(mxGraphModel model) {
			super(model);
		}
		
	}
	
	
}	// class StateModelEditPanel
