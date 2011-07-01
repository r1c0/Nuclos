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
package org.nuclos.client.processmonitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.gef.AbstractController;
import org.nuclos.client.gef.AbstractShapeViewer;
import org.nuclos.client.gef.DefaultShapeViewer;
import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.ShapeControllerException;
import org.nuclos.client.gef.ShapeModel;
import org.nuclos.client.gef.ShapeModelListener;
import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.gef.shapes.ConnectionPoint;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.processmonitor.shapes.SubProcessTransition;
import org.nuclos.client.processmonitor.shapes.SubProcessViewShape;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.statemodel.StateModelEditor;
import org.nuclos.client.statemodel.models.NotePropertiesPanelModel;
import org.nuclos.client.statemodel.shapes.NoteShape;
import org.nuclos.client.statemodel.shapes.StateShape;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.InstanceConstants;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.statemodel.valueobject.NoteLayout;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateLayout;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.TransitionLayout;

/**
 * The Processmonitor model viewer.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * central class for ProcessMonitor Designer 
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik Stueker</a>
 * @version 01.00.00
 */
public class InstanceViewer extends JPanel implements ShapeModelListener{
	
	private final Logger log = Logger.getLogger(this.getClass());

	private final DefaultShapeViewer pnlShapeViewer;
	private final JScrollPane scrollPane;
	private final InstanceViewerPropertiesPanel pnlProperties; 
	private final JSplitPane splitpnMain;
	private final JToolBar toolbar = new JToolBar();
	private final Action actZoomIn = new ZoomInAction();
	private final Action actZoomOut = new ZoomOutAction();
	private final Action actPrint = new PrintAction();
	private final JLabel labZoom = new JLabel("100%");
	private final double[] adZoomSteps = {30d, 50d, 75d, 100d, 125d, 150d, 200d, 300d};
	private int iCurrentZoom = 3;
	private final JButton btnBackward = new JButton(new BackwardAction());
	
	private StateModelLayout layoutinfo;
	private final List<ActionListener> lstPrintEventListeners = new Vector<ActionListener>();
	
	private final ForwardAction forwardAction = new ForwardAction();
	
	private Integer iInstanceId;
	
	public InstanceViewer(){
		super(new BorderLayout());
		
		pnlShapeViewer = new DefaultShapeViewer();
		pnlShapeViewer.setController(new InstanceViewerShapeController(pnlShapeViewer));
		scrollPane = new JScrollPane(pnlShapeViewer);
		pnlProperties = new InstanceViewerPropertiesPanel();
		splitpnMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scrollPane, pnlProperties);
		
		UIUtils.clearKeymaps(splitpnMain);
	
		pnlShapeViewer.getModel().addShapeModelListener(this);
		pnlShapeViewer.setExtents(new Extents2D(1024, 1024));
		
		this.init();
	}
	
	private void init(){
		btnBackward.setEnabled(false);
		btnBackward.setText("");
		btnBackward.setToolTipText("Zur\u00fcck zur Instanz");
		toolbar.add(btnBackward);
		
		toolbar.addSeparator();
		JButton btn = toolbar.add(actPrint);
		btn.setText("");
		btn.setToolTipText("Drucken");
		
		toolbar.addSeparator();
		labZoom.setFont(new Font("Dialog", Font.PLAIN, 8));
		btn = toolbar.add(actZoomIn);
		btn.setText("");
		btn.setToolTipText("Zoom +");
		toolbar.add(labZoom);
		
		btn = toolbar.add(actZoomOut);
		btn.setText("");
		btn.setToolTipText("Zoom -");
		
		toolbar.setOrientation(JToolBar.VERTICAL);
		
		this.add(toolbar, BorderLayout.WEST);
		this.add(splitpnMain, BorderLayout.CENTER);

		splitpnMain.setResizeWeight(0.8d);
		
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
		
		
	}
	
	private static StateModelLayout newLayoutInfo(ProcessMonitorGraphVO processModelGraphVO) {
		final StateModelLayout result = new StateModelLayout();
		for (SubProcessVO statevo : processModelGraphVO.getStates()) {
			result.insertStateLayout(statevo.getWorkingId(), new StateLayout(0d, 0d, 120d, 48d));
		}

		// StateModelStartShape
		//result.insertStateLayout(STARTING_STATE_ID, new StateLayout(8d, 8d, 12d, 12d));

		for (ProcessTransitionVO statetransitionvo : processModelGraphVO.getTransitions()) {
			final int iConnectionStart = (statetransitionvo.getStateSource() != null) ? AbstractShape.CONNECTION_NE : -1;
			final int iConnectionEnd = (statetransitionvo.getStateTarget() != null) ? AbstractShape.CONNECTION_N : -1;
			final TransitionLayout transitionlayout = new TransitionLayout(statetransitionvo.getId(), iConnectionStart, iConnectionEnd);

			result.insertTransitionLayout(statetransitionvo.getId(), transitionlayout);
		}
		return result;
	}
	
	public void showInstanceStatus(Integer iInstanceId){
		this.iInstanceId = iInstanceId;
		final ShapeModel shapemodel = pnlShapeViewer.getModel();
		
		try {
			shapemodel.setActiveLayer("Default");
		} catch (ShapeControllerException e) {
			throw new CommonFatalException(e);
		}
		for (Iterator<Shape> it = shapemodel.getActiveLayer().getShapes().iterator(); it.hasNext(); ){
			Shape shape = it.next();
			if (shape instanceof SubProcessViewShape){
				SubProcessViewShape subProcessViewShape = (SubProcessViewShape) shape;
				final Integer iStateModelUsageId = subProcessViewShape.getStateVO().getStateModelUsageId();
				
				final int status = InstanceDelegate.getInstance().getInstanceStatus(iInstanceId, iStateModelUsageId);
				subProcessViewShape.showInstanceStatus(status);
			}
		}
	}
	
	/*
	 * paints ProcessModel onto Editor
	 */
	public void setProcessmodelGraph(ProcessMonitorGraphVO processModelGraphVO) throws NuclosBusinessException {
		this.clear();
		if (processModelGraphVO == null){
			return;
		} 
		
		final ShapeModel shapemodel = pnlShapeViewer.getModel();
		final Map<Integer, SubProcessEntry> mpShapes = CollectionUtils.newHashMap();

		shapemodel.clear();
		selectionChanged(null);
		try {
			shapemodel.setActiveLayer("Default");

			layoutinfo = processModelGraphVO.getStateModel().getLayout();
			if (layoutinfo == null) {
				layoutinfo = newLayoutInfo(processModelGraphVO);
			}


			// add subporcess to shape model:
			for (SubProcessVO statevo : processModelGraphVO.getStates()) {
				final SubProcessViewShape subShape = new SubProcessViewShape(statevo);
				subShape.setForwardAction(forwardAction);
				subShape.getStateVO().setStateModelVO(statevo.getStateModelVO());
				shapemodel.addShape(subShape);

				final StateLayout layout = layoutinfo.getStateLayout(statevo.getId());
				if (layout != null) {
					subShape.setDimension(new Rectangle2D.Double(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight()));
				}
				else {
					subShape.setDimension(new Rectangle2D.Double(0d, 0d, 120d, 48d));
				}

				final SubProcessEntry entry = new SubProcessEntry();
				entry.setVo(statevo);
				entry.setShape(subShape);
				mpShapes.put(statevo.getId(), entry);
			}

			// add transitions to shape model:
			for (ProcessTransitionVO processtransitionvo : processModelGraphVO.getTransitions()) {
				final SubProcessTransition processtransition = new SubProcessTransition(processtransitionvo);
				processtransition.setView(pnlShapeViewer);
				final Integer iSourceStateId = processtransitionvo.getStateSource();
				final Integer iTargetStateId = processtransitionvo.getStateTarget();

				final TransitionLayout transitionlayout = layoutinfo.getTransitionLayout(processtransitionvo.getId());
				int start = AbstractShape.CONNECTION_NE;
				int end = AbstractShape.CONNECTION_N;
				if (transitionlayout != null) {
					start = transitionlayout.getConnectionStart();
					end = transitionlayout.getConnectionEnd();
				}

//				if (iSourceStateId == null && iTargetStateId != null) {
//					// Initial transition to start state
//					processtransition.setSourceConnection(new ConnectionPoint(newStartShape(layoutinfo), AbstractShape.CONNECTION_CENTER));
//				}

				if (iSourceStateId != null) {
					final SubProcessEntry entry = mpShapes.get(iSourceStateId);
					if (start < 0) {
						start = 0;
					}
					processtransition.setSourceConnection(new ConnectionPoint(entry.getShape(), start));
				}
				if (iTargetStateId != null) {
					final SubProcessEntry entry = mpShapes.get(iTargetStateId);
					if (end < 0) {
						end = 0;
					}
					processtransition.setDestinationConnection(new ConnectionPoint(entry.getShape(), end));
				}

				shapemodel.setActiveLayer("Connectors");
				shapemodel.addShape(processtransition);
			}

			// add notes to shape model:
			for (NoteLayout notelayout : layoutinfo.getNotes()) {
				final NoteShape noteshape = new NoteShape();
				noteshape.setDimension(new Rectangle2D.Double(notelayout.getX(), notelayout.getY(), notelayout.getWidth(), notelayout.getHeight()));
				noteshape.setText(notelayout.getText());
				shapemodel.setActiveLayer("Notes");
				shapemodel.addShape(noteshape);
			}
			pnlShapeViewer.repaint();
		}
		catch (ShapeControllerException ex) {
			throw new NuclosBusinessException(ex);
		}

	}
	
	public void clear() {
		pnlShapeViewer.getModel().clear();
		scrollPane.setViewportView(pnlShapeViewer);
		btnBackward.setEnabled(false);
		pnlShapeViewer.repaint();
	}

	@Override
    public void modelChanged() {
		// could not happen
		
	}

	@Override
    public void multiSelectionChanged(Collection<Shape> collShapes) {
		// could not happen
		
	}

	@Override
    public void selectionChanged(Shape shape) {
		if (pnlShapeViewer.getModel().isMultiSelected()) {
			pnlProperties.setPanel("None");
		}
		else if (shape == null) {
			log.debug("selectionChanged: nothing selected");
			pnlProperties.setPanel("None");
		}
		else if (shape instanceof NoteShape) {
			log.debug("selectionChanged: single note shape selected");
			pnlProperties.setPanel("Note");

			final NotePropertiesPanelModel model = pnlProperties.getNotePanel().getModel();
			model.setText(((NoteShape) shape).getText());
		}
		else if (shape instanceof SubProcessViewShape) {
			log.debug("selectionChanged: SubProcessViewShape selected");			
			pnlProperties.setPanel("SubProcessObject");
			
			final InstanceViewObjectPropertiesPanel pnlSubProcessObject = pnlProperties.getSubProcessObjectPanel();
			pnlSubProcessObject.clear();
			
			SubProcessViewShape subProcessViewShape = (SubProcessViewShape) shape;
			Integer iObjectId = InstanceDelegate.getInstance().getObjectId(iInstanceId, subProcessViewShape.getSubProcessUsageCriteria());
			if (iObjectId != null){
				try {
					final AttributeProvider attrprovider = AttributeCache.getInstance();
					
					GenericObjectVO goVO = GenericObjectDelegate.getInstance().get(iObjectId);
					DynamicAttributeVO attrIdentifier = goVO.getAttribute(NuclosEOField.SYSTEMIDENTIFIER.getMetaData().getField(), attrprovider);
					if (attrIdentifier != null && attrIdentifier.getValue() != null){
						pnlSubProcessObject.setIdentifier(attrIdentifier.getValue().toString());
					}
					DynamicAttributeVO attrPlanStart = goVO.getAttribute("[plan_start]", attrprovider);
					if (attrPlanStart != null && attrPlanStart.getValue() != null){
						pnlSubProcessObject.setPlanStart(attrPlanStart.getValue().toString());
					}
					DynamicAttributeVO attrPlanEnd = goVO.getAttribute("[plan_end]", attrprovider);
					if (attrPlanEnd != null && attrPlanEnd.getValue() != null){
						pnlSubProcessObject.setPlanEnd(attrPlanEnd.getValue().toString());
					}
					DynamicAttributeVO attrPlanRuntime = goVO.getAttribute("[plan_runtime]", attrprovider);
					if (attrPlanRuntime != null && attrPlanRuntime.getValue() != null){
						pnlSubProcessObject.setPlanRuntime(((Double)attrPlanRuntime.getValue()).doubleValue());
					}
					DynamicAttributeVO attrRealStart = goVO.getAttribute("[real_start]", attrprovider);
					if (attrRealStart != null && attrRealStart.getValue() != null){
						pnlSubProcessObject.setRealStart(attrRealStart.getValue().toString());
					}
					DynamicAttributeVO attrRealEnd = goVO.getAttribute("[real_end]", attrprovider);
					if (attrRealEnd != null && attrRealEnd.getValue() != null){
						pnlSubProcessObject.setRealEnd(attrRealEnd.getValue().toString());
					}
					DynamicAttributeVO attrRealRuntime = goVO.getAttribute("[real_runtime]", attrprovider);
					if (attrRealRuntime != null && attrRealRuntime.getValue() != null){
						pnlSubProcessObject.setRealRuntime(((Double)attrRealRuntime.getValue()).doubleValue());
					}
					
				} catch (CommonFinderException e) {
					throw new CommonFatalException(e);
				} catch (CommonPermissionException e) {
					throw new CommonFatalException(e);
				}
			} else {
				pnlSubProcessObject.setIdentifier("Objekt noch nicht generiert.");
			}
		}
	}

	@Override
    public void shapeDeleted(Shape shape) {
		// could not happen
		
	}

	@Override
    public void shapesDeleted(Collection<Shape> collShapes) {
		// could not happen
		
	}
	
	private class ForwardAction extends AbstractAction {

		@Override
        public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof SubProcessVO) {
				SubProcessVO subProcessVO = (SubProcessVO) e.getSource();
				
				Dimension dim = new Dimension(1024, 1024);
				
				DefaultShapeViewer pnlStateModelShapeViewer = new DefaultShapeViewer();
				pnlStateModelShapeViewer.setController(new InstanceViewerShapeController(pnlStateModelShapeViewer));
				pnlStateModelShapeViewer.setExtents(new Extents2D(dim.getWidth(), dim.getHeight()));
				pnlStateModelShapeViewer.setSize(dim);
				

				try {
					StateGraphVO stategraphvo = null;
					try {
						stategraphvo = StateDelegate.getInstance().getStateGraph(subProcessVO.getStateModelVO().getId());
					}
					catch(CommonBusinessException e1) {
						Errors.getInstance().showExceptionDialog(InstanceViewer.this, e1);
						return;
					}
					StateModelEditor.showLayout(stategraphvo, pnlStateModelShapeViewer, log);

					// mark current state...
					Integer iObjectId = InstanceDelegate.getInstance().getObjectId(iInstanceId, subProcessVO.getStateModelUsageId());
					Integer iObjectStateId = null;
					if (iObjectId != null){
						iObjectStateId = GenericObjectDelegate.getInstance().getStateIdByGenericObject(iObjectId);
					}

					final ShapeModel shapemodel = pnlStateModelShapeViewer.getModel();
					shapemodel.setActiveLayer("Default");
					for (Iterator<Shape> it = shapemodel.getActiveLayer().getShapes().iterator(); it.hasNext(); ){
						Shape shape = it.next();
						if (shape instanceof StateShape){
							StateShape stateShape = (StateShape) shape;
							
							if (iObjectStateId != null && iObjectStateId.equals(stateShape.getStateVO().getId())){
								stateShape.markState(InstanceConstants.STATE_IS_CURRENT);
							} else {
								stateShape.markState(InstanceConstants.STATE_IS_NOT_CURRENT);
							}
						}
					}
					
					pnlStateModelShapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
					InstanceViewer.this.scrollPane.setViewportView(pnlStateModelShapeViewer);
					InstanceViewer.this.btnBackward.setEnabled(true);
					
//				} catch (CommonFinderException e1) {
//					throw new CommonFatalException("Statusmodell mit der ID " + subProcessVO.getStateModelVO().getId() + " wurde nicht gefunden!", e1);
				} catch (ShapeControllerException e1) {
					throw new CommonFatalException("Es gab Probleme beim Erzeugen der Ansicht des Statusmodells.", e1);
				}
				
			} else {
				throw new CommonFatalException("Source of ForwardAction is not a SubProcessVO!");
			}
		}
		
	}
	
	private class BackwardAction extends AbstractAction {
		BackwardAction() {
			super("Zur\u00fcck", Icons.getInstance().getIconLeft16());
		}
		
		@Override
        public void actionPerformed(ActionEvent e) {
			pnlShapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
			InstanceViewer.this.btnBackward.setEnabled(false);
			InstanceViewer.this.scrollPane.setViewportView(pnlShapeViewer);
		}
		
	}
	
	private class ZoomInAction extends AbstractAction {
		ZoomInAction() {
			super("Zoom +", Icons.getInstance().getIconZoomIn());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			zoomIn();
		}
	}

	private class ZoomOutAction extends AbstractAction {
		ZoomOutAction() {
			super("Zoom -", Icons.getInstance().getIconZoomOut());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			zoomOut();
		}
	}

	private class PrintAction extends AbstractAction {
		PrintAction() {
			super("Drucken...", Icons.getInstance().getIconPrint16());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			print();
		}
	}
	
	public void zoomIn() {
		if (scrollPane.getViewport().getView() instanceof AbstractShapeViewer){
			AbstractShapeViewer shapeViewer = (AbstractShapeViewer) scrollPane.getViewport().getView();
			if (iCurrentZoom < adZoomSteps.length - 1) {
				iCurrentZoom++;
				actZoomOut.setEnabled(true);
				shapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
				labZoom.setText(new String(new Double(adZoomSteps[iCurrentZoom]).intValue() + "%"));
			}
			else {
				actZoomIn.setEnabled(false);
			}
		}
	}

	public void zoomOut() {
		if (scrollPane.getViewport().getView() instanceof AbstractShapeViewer){
			AbstractShapeViewer shapeViewer = (AbstractShapeViewer) scrollPane.getViewport().getView();
			if (iCurrentZoom > 0) {
				iCurrentZoom--;
				actZoomIn.setEnabled(true);
				shapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
				labZoom.setText(new String(new Double(adZoomSteps[iCurrentZoom]).intValue() + "%"));
			}
			else {
				actZoomOut.setEnabled(false);
			}
		}
	}

	public void print() {
		for (ActionListener al : lstPrintEventListeners) {
			al.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}
	
	/**
	 * 
	 * controller with disabled mouse drag and drop function
	 */
	private class InstanceViewerShapeController extends AbstractController{
		
		public InstanceViewerShapeController(AbstractShapeViewer viewer) {
			super(viewer);
		}

		@Override
		protected void mouseDraggedInsertConnector(MouseEvent e) {
			// do nothing
		}

		@Override
		protected void mouseDraggedRubberband(MouseEvent e) {
			// do nothing
		}

		@Override
		protected void mouseDraggedScale(MouseEvent e) {
			// do nothing
		}

		@Override
		protected void mouseDraggedTranslate(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// do nothing
		}
	}
}
