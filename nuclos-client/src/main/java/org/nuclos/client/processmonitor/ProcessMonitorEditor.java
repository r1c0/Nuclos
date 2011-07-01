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
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.gef.AbstractShapeController;
import org.nuclos.client.gef.DefaultShapeViewer;
import org.nuclos.client.gef.Shape;
import org.nuclos.client.gef.ShapeControllerException;
import org.nuclos.client.gef.ShapeModel;
import org.nuclos.client.gef.ShapeModelListener;
import org.nuclos.client.gef.ShapeViewer;
import org.nuclos.client.gef.layout.Extents2D;
import org.nuclos.client.gef.shapes.AbstractConnector;
import org.nuclos.client.gef.shapes.AbstractShape;
import org.nuclos.client.gef.shapes.ConnectionPoint;
import org.nuclos.client.processmonitor.shapes.SubProcessShape;
import org.nuclos.client.processmonitor.shapes.SubProcessTransition;
import org.nuclos.client.statemodel.RoleRepository;
import org.nuclos.client.statemodel.RuleRepository;
import org.nuclos.client.statemodel.SortedRuleVO;
import org.nuclos.client.statemodel.models.NotePropertiesPanelModel;
import org.nuclos.client.statemodel.shapes.NoteShape;
import org.nuclos.client.statemodel.shapes.StateModelStartShape;
import org.nuclos.client.statemodel.shapes.StateShape;
import org.nuclos.client.statemodel.shapes.StateTransition;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorGraphVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.statemodel.valueobject.NoteLayout;
import org.nuclos.server.statemodel.valueobject.StateLayout;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.TransitionLayout;

/**
 * The Processmonitor model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * central class for ProcessMonitor Designer 
 *
 * @author	<a href="mailto:marc.finke@novabit.de">Marc Finke</a>
 * @version 01.00.00
 */
public class ProcessMonitorEditor extends JPanel implements ShapeModelListener, FocusListener {

	private final Logger log = Logger.getLogger(this.getClass());

	/**
	 * id of the starting state
	 */
	private static final Integer STARTING_STATE_ID = -666;

	private class SelectAction extends AbstractAction {
		SelectAction() {
			super("Auswahl", Icons.getInstance().getIconSelectObject());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnSelection.setSelected(true);
			setSelectionTool();
		}
	}

	
	/*
	 * action when a new SubProcess is inserted
	 */
	private class NewSubProcessAction extends AbstractAction {
		NewSubProcessAction() {
			super("Neuer Teilprozess", Icons.getInstance().getIconState());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertState.setSelected(true);
			setStateTool();
		}
	}

	/*
	 * action when a new SubProcess Transition is inserted
	 */
	private class NewTransitionAction extends AbstractAction {
		NewTransitionAction() {
			super("Neuer Prozess\u00fcbergang ", Icons.getInstance().getIconStateTransition());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertTransition.setSelected(true);
			setTransitionTool();
		}
	}
	

	private class NewNoteAction extends AbstractAction {
		NewNoteAction() {
			super("Neue Bemerkung", Icons.getInstance().getIconStateNewNote());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertNote.setSelected(true);
			setNoteTool();
		}
	}

	private class DeleteAction extends AbstractAction {
		DeleteAction() {
			super("Auswahl l\u00f6schen", Icons.getInstance().getIconDelete16());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
				deleteSelection();
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
			printStateModel();
		}
	}

	/*
	 * Listener: when Name has changed transfer from PropertiesPanel to Shape
	 */
	private class NameDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateName();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateName();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateName();
		}
	}
	
	/*
	 * Listener: when usage criteria has changed transfer from PropertiesPanel to Shape
	 */
	private class SubProcessUsageCriteriaListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeSubProcessUsageCriteria();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeSubProcessUsageCriteria();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeSubProcessUsageCriteria();
		}
	}
	
	/*
	 * Listener: when Durchlaufzeit has changed transfer from PropertiesPanel to Shape
	 */
	private class RuntimeDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntime();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntime();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntime();
		}
	}
	
	/*
	 * Listener: when Durchlaufformat has changed transfer from PropertiesPanel to Shape
	 */
	private class RuntimeFormatDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntimeFormat();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntimeFormat();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateRuntimeFormat();
		}
	}

	/*
	 * Listener: when Beschreibung has changed transfer from PropertiesPanel to Shape
	 */
	private class DescriptionDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateDescription();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateDescription();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateDescription();
		}
	}
	/*
	 * Listener: when Verantwortlicher has changed transfer from PropertiesPanel to Shape
	 */
	private class GuarantorDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateGuarantor();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateGuarantor();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateGuarantor();
		}
	}
	
	/*
	 * Listener: when Stellvertreter has changed transfer from PropertiesPanel to Shape
	 */
	private class SecondGuarantorDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSecondGuarantor();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSecondGuarantor();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSecondGuarantor();
		}
	}

	/*
	 * Listener: when Vorgesetzter has changed transfer from PropertiesPanel to Shape
	 */
	private class SupervisorDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSupervisor();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSupervisor();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateSupervisor();
		}
	}
	
	/*
	 * Listener: when "Original System" has changed transfer from PropertiesPanel to Shape
	 */
	private class OriginalSystemDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateOriginalSystem();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateOriginalSystem();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStateOriginalSystem();
		}
	}
	
	/*
	 * Listener: when Plan Start Series has changed transfer from PropertiesPanel to Shape
	 */
	private class PlanStartSeriesDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanStartSeries();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanStartSeries();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanStartSeries();
		}
	}
	
	/*
	 * Listener: when Plan End Series has changed transfer from PropertiesPanel to Shape
	 */
	private class PlanEndSeriesDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanEndSeries();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanEndSeries();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeStatePlanEndSeries();
		}
	}

	/*
	 * Listener: when StatusModel has changed transfer from PropertiesPanel to Shape
	 */
	private class StateModelDataListener implements ListDataListener {
		@Override
        public void contentsChanged(ListDataEvent e) {
			ProcessMonitorEditor.this.changeStateModel();
		}

		@Override
        public void intervalAdded(ListDataEvent e) {
		}

		@Override
        public void intervalRemoved(ListDataEvent e) {
		}
	}
	
	/*
	 * Listener: when Generator on State has changed transfer from PropertiesPanel to Transition
	 */
	private class TransitionGeneratorOnStateDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeTransitionState();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeTransitionState();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeTransitionState();
		}
	}


	private class NoteDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeNoteText();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeNoteText();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			ProcessMonitorEditor.this.changeNoteText();
		}
	}

	private final DefaultShapeViewer pnlShapeViewer = new DefaultShapeViewer();
	private final JScrollPane scrollPane = new JScrollPane(pnlShapeViewer);
	private final ProcessMonitorEditorPropertiesPanel pnlProperties; 
	private final JSplitPane splitpnMain;
	private final JToolBar toolbar = new JToolBar();
	private final Action actSelect = new SelectAction();
	private final Action actNewState = new NewSubProcessAction();
	private final Action actNewTransition = new NewTransitionAction();
	private final Action actNewNote = new NewNoteAction();
	private final Action actDelete = new DeleteAction();
	private final Action actZoomIn = new ZoomInAction();
	private final Action actZoomOut = new ZoomOutAction();
	private final Action actPrint = new PrintAction();
	private final JLabel labZoom = new JLabel("100%");
	private final JToggleButton btnSelection = new JToggleButton(actSelect);
	private final JToggleButton btnInsertState = new JToggleButton(actNewState);
	private final JToggleButton btnInsertTransition = new JToggleButton(actNewTransition);
	private final JToggleButton btnInsertNote = new JToggleButton(actNewNote);
	private final double[] adZoomSteps = {30d, 50d, 75d, 100d, 125d, 150d, 200d, 300d};
	private int iCurrentZoom = 3;
	private final List<ChangeListener> lstChangeListeners = new Vector<ChangeListener>();
	private Shape shapeSelected;

	/** @todo eliminate this field - use local variables instead */
	private ProcessMonitorGraphVO stategraphvo;

	private StateModelLayout layoutinfo;
	private final List<ActionListener> lstPrintEventListeners = new Vector<ActionListener>();
	private final NameDocumentListener nameDocumentListener = new NameDocumentListener();
	private final SubProcessUsageCriteriaListener subProcessUsageCriteriaListener = new SubProcessUsageCriteriaListener();
	private final DescriptionDocumentListener descriptionDocumentListener = new DescriptionDocumentListener();
	private final GuarantorDocumentListener guarantorDocumentListener = new GuarantorDocumentListener();
	private final SecondGuarantorDocumentListener secondguarantorDocumentListener = new SecondGuarantorDocumentListener();
	private final SupervisorDocumentListener supervisorDocumentListener = new SupervisorDocumentListener();
	private final PlanStartSeriesDocumentListener planStartSeriesDocumentListener = new PlanStartSeriesDocumentListener();
	private final PlanEndSeriesDocumentListener planEndSeriesDocumentListener = new PlanEndSeriesDocumentListener();
	private final OriginalSystemDocumentListener originalSystemDocumentListener = new OriginalSystemDocumentListener();

	private final RuntimeDocumentListener runtimeDocumentListener = new RuntimeDocumentListener();
	private final RuntimeFormatDocumentListener runtimeFormatDocumentListener = new RuntimeFormatDocumentListener();
	private final NoteDocumentListener noteDocumentListener = new NoteDocumentListener();
	private final StateModelDataListener statemodelDataListener = new StateModelDataListener();
	
	private final TransitionGeneratorOnStateDocumentListener transitionGeneratorOnStateDocumentListener = new TransitionGeneratorOnStateDocumentListener();

	public ProcessMonitorEditor() {
		super(new BorderLayout());

		pnlProperties = new ProcessMonitorEditorPropertiesPanel(this);
					
		splitpnMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, scrollPane, pnlProperties);
		UIUtils.clearKeymaps(splitpnMain);

		pnlShapeViewer.getModel().addShapeModelListener(this);
		pnlShapeViewer.setExtents(new Extents2D(1024, 1024));

		this.init();
	}

	public void changeStatePlanStartSeries() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setPlanStartSeries(pnlProperties.getSubProcessPropertiesPanel().getModel().getPlanStartSeries());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	public void changeStatePlanEndSeries() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setPlanEndSeries(pnlProperties.getSubProcessPropertiesPanel().getModel().getPlanEndSeries());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	private void init() {
		final ButtonGroup bg = new ButtonGroup();

		btnSelection.setSelected(true);
		btnSelection.setText("");
		btnSelection.setToolTipText("Auswahl");
		bg.add(btnSelection);
		toolbar.add(btnSelection);
		btnSelection.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertState.setText("");
		btnInsertState.setToolTipText("Teilprozess einf\u00fcgen");
		bg.add(btnInsertState);
		toolbar.add(btnInsertState);
		btnInsertState.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertTransition.setText("");
		btnInsertTransition.setToolTipText("\u00dcbergang einf\u00fcgen");
		bg.add(btnInsertTransition);
		toolbar.add(btnInsertTransition);
		btnInsertTransition.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertNote.setText("");
		btnInsertNote.setToolTipText("Kommentar einf\u00fcgen");
		bg.add(btnInsertNote);
		toolbar.add(btnInsertNote);
		btnInsertNote.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		JButton btn = new JButton(actDelete);
		btn.setText("");
		btn.setToolTipText("Auswahl l\u00f6schen");
		toolbar.addSeparator();
		toolbar.add(btn);
		btn.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		toolbar.addSeparator();
		btn = toolbar.add(actPrint);
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
		pnlProperties.setPanel("None");
		scrollPane.addFocusListener(this);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

		final JPopupMenu popup = new JPopupMenu();
		popup.add(actSelect);
		popup.addSeparator();
		popup.add(actNewState);
		popup.add(actNewTransition);
		popup.add(actNewNote);
		popup.addSeparator();
		popup.add(actDelete);
		pnlShapeViewer.getController().setPopupMenu(popup);

		pnlShapeViewer.getModel().addLayer("Notes", true, 3);
	}
	
	public ShapeViewer getViewer() {
		return pnlShapeViewer;
	}
	
	
	public ProcessMonitorEditorPropertiesPanel getProcessMonitorEditorPropertiesPanel() {
		return this.pnlProperties;
	}

	public void addChangeListener(ChangeListener cl) {
		lstChangeListeners.add(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		lstChangeListeners.remove(cl);
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
	@SuppressWarnings("unused")
	private void addStatePanelListeners() {
		
		pnlProperties.getSubProcessPropertiesPanel().getModel().docName.addDocumentListener(nameDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docDescription.addDocumentListener(descriptionDocumentListener);
		
	}
	
	/**
	 * adds the listeners for the subprocess (properties) panel.
	 */
	private void addSubProcessPanelListeners() {
		
		pnlProperties.getSubProcessPropertiesPanel().getModel().docName.addDocumentListener(nameDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSubProcessUsageCriteria.addDocumentListener(subProcessUsageCriteriaListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docDescription.addDocumentListener(descriptionDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docGuarantor.addDocumentListener(guarantorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSecondGuarantor.addDocumentListener(secondguarantorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSupervisor.addDocumentListener(supervisorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docOriginalSystem.addDocumentListener(originalSystemDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docPlanStartSeries.addDocumentListener(planStartSeriesDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docPlanEndSeries.addDocumentListener(planEndSeriesDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docRuntime.addDocumentListener(runtimeDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docRuntimeFormat.addDocumentListener(runtimeFormatDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().modelSubProcessStateModel.addListDataListener(statemodelDataListener);		
	}

	/**
	 * removes the listeners for the subprocess (properties) panel.
	 */
	private void removeSubProcessPanelListeners() {
		pnlProperties.getSubProcessPropertiesPanel().getModel().docName.removeDocumentListener(nameDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSubProcessUsageCriteria.removeDocumentListener(subProcessUsageCriteriaListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docDescription.removeDocumentListener(descriptionDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docGuarantor.removeDocumentListener(guarantorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSecondGuarantor.removeDocumentListener(secondguarantorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docSupervisor.removeDocumentListener(supervisorDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docOriginalSystem.removeDocumentListener(originalSystemDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docPlanStartSeries.removeDocumentListener(planStartSeriesDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docPlanEndSeries.removeDocumentListener(planEndSeriesDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docRuntime.removeDocumentListener(runtimeDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().docRuntimeFormat.removeDocumentListener(runtimeFormatDocumentListener);
		pnlProperties.getSubProcessPropertiesPanel().getModel().modelSubProcessStateModel.removeListDataListener(statemodelDataListener);
	}

	/**
	 * adds listeners for the note (properties) panel.
	 */
	private void addNotePanelListeners() {
		pnlProperties.getNotePanel().getModel().docText.addDocumentListener(noteDocumentListener);
	}

	/**
	 * removes the listeners for the note (properties) panel.
	 */
	private void removeNotePanelListeners() {
		pnlProperties.getNotePanel().getModel().docText.removeDocumentListener(noteDocumentListener);
	}

	@Override
    public void modelChanged() {
		btnSelection.setSelected(true);

		for (ChangeListener cl : lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
	}
	
	private void setDeleteActionEnabled(boolean enabled) {
			actDelete.setEnabled(enabled);
	}

	/*
	 * when something is selected in editor (shapes) 
	 */
	@Override
    public void selectionChanged(Shape shape) {
		setDeleteActionEnabled(shape != null || pnlShapeViewer.getModel().isMultiSelected());

		this.handlePreviousSelection();

		if (pnlShapeViewer.getModel().isMultiSelected()) {
			/** @todo this seems never to be called */
			log.debug("selectionChanged: multi selection");
			/** @todo show rights editor for multiple states */
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
		else if (shape == null) {
			log.debug("selectionChanged: nothing selected");
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
		else if (shape instanceof NoteShape) {
			log.debug("selectionChanged: single note shape selected");
			pnlProperties.setPanel("Note");
			shapeSelected = shape;

			final NotePropertiesPanelModel model = pnlProperties.getNotePanel().getModel();
			model.setText(((NoteShape) shapeSelected).getText());

			this.addNotePanelListeners();
		}
		else if (shape instanceof AbstractConnector) {
			log.debug("selectionChanged: single transition shape selected");
			pnlProperties.setPanel("Transition");
			shapeSelected = shape;
			
			if(shapeSelected instanceof SubProcessTransition) {
				SubProcessTransition subprocessTransition = (SubProcessTransition)shapeSelected;
				ConnectionPoint pointSrc = subprocessTransition.getSourceConnection();
				if(pointSrc != null) {
					Shape targetShape = pointSrc.getTargetShape();
					if(targetShape instanceof SubProcessShape) {
						SubProcessShape targetSubProcess = (SubProcessShape)targetShape;
						Integer stateModelId = targetSubProcess.getStateVO().getStateModelVO().getId();
						pnlProperties.getSubProcessTransitionPropertiesPanel().getModel().setStateModelId(stateModelId);
					}
					final SubProcessTransitionPanelModel model = pnlProperties.getSubProcessTransitionPropertiesPanel().getModel();
					model.setGeneratorStateId(subprocessTransition.getStateId());
					model.setSubProcessTransitionId(subprocessTransition.getStateTransitionVO().getClientId());
					pnlProperties.getSubProcessTransitionPropertiesPanel().getModel().docGeneratorState.addDocumentListener(transitionGeneratorOnStateDocumentListener);
				}
				else {
					pnlProperties.getSubProcessTransitionPropertiesPanel().getModel().clear();
				}
				
			}
			
			/*
			try {
				pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(((StateTransition) shape).getRules()));
				TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRulePanel().getTblRules(), 10, 10);
				pnlProperties.getTransitionRulePanel().getBtnAutomatic().setSelected(((StateTransition) shapeSelected).getStateTransitionVO().isAutomatic());
				pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shape).getRoles()));
				TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRolePanel().getTblRoles(), 10, 10);
			}
			catch (CreateException ex) {
				Errors.getInstance().showExceptionDialog(this, ex.getMessage(), ex);
			}
			catch (RemoteException ex) {
				Errors.getInstance().showExceptionDialog(this, ex.getMessage(), ex);
			}
			*/
		}
		/*
		 * when subprocessshape is selected
		 */
		else if(shape instanceof SubProcessShape) {
			log.debug("selectionChanged: single state shape selected");
			pnlProperties.setPanel("SubProcess");
			shapeSelected = shape;
			final SubProcessShape subshapeSelected = (SubProcessShape) shape;
			subshapeSelected.checkStatus();
			
			final SubProcessPanelModel model = pnlProperties.getSubProcessPropertiesPanel().getModel();
			model.setSubProcessStateModel(subshapeSelected.getSubProcessStateModel());
			model.setName(subshapeSelected.getName()); 
			model.setDescription(subshapeSelected.getDescription());	
			model.setGuarantor(subshapeSelected.getGuarantor());
			model.setSecondGuarantor(subshapeSelected.getSecondGuarantor());
			model.setSupervisor(subshapeSelected.getSupervisor());
			model.setOriginalSystem(subshapeSelected.getOriginalSystem());
			model.setPlanStartSeries(subshapeSelected.getPlanStartSeries());
			model.setPlanEndSeries(subshapeSelected.getPlanEndSeries());
			model.setRuntime(subshapeSelected.getRuntime());
			model.setRuntimeFormat(subshapeSelected.getRuntimeFormat());
			model.setSubProcessUsageCriteria(subshapeSelected.getSubProcessUsageCriteria());
			addSubProcessPanelListeners();
		}
		else {
			log.debug("selectionChanged: something unknown was selected");
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
	}

	private void handlePreviousSelection() {
		if (shapeSelected instanceof StateShape) {
			log.debug("State shape deselected.");

			// get user rights from properties panel and store them in the selected state's vo:
//			final StateShape stateshape = (StateShape) shapeSelected;
			/** @todo endEditing here? */
//			final StateVO statevo = stateshape.getStateVO();
//			final StateVO.UserRights userrights = getUserRightsFromPropertiesPanel();
//			statevo.setUserRights(userrights);
			this.closeSubForms();
			this.removeSubProcessPanelListeners(); 
		}
		else if (shapeSelected instanceof NoteShape) {
			log.debug("Note shape deselected.");
			this.removeNotePanelListeners();
		}
		else {
			log.debug("Something unknown was deselected.");
			// do nothing
		}
	}

	@SuppressWarnings("unused")
	private void setupSubforms(StateVO statevo) {
		log.debug("setup subforms");
		/** @todo this is just a test - correct this */
	}

	private void closeSubForms() {
		log.debug("close subforms");
	}

	@Override
    public void multiSelectionChanged(Collection<Shape> collShapes) {
		log.debug("multiSelectionChanged");

		/** @todo this is always called when selecting a shape using "rubberbanding", even if a single shape
		 * was selected. In the latter case, selectionChanged() should be called for the selected shape. */

		setDeleteActionEnabled(collShapes.size() > 0);
	}

	@Override
    public void shapeDeleted(Shape shape) {
		for (ChangeListener cl : lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
		setDeleteActionEnabled(false);
	}

	@Override
    public void shapesDeleted(Collection<Shape> collShapes) {
		for (ChangeListener cl : lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
		setDeleteActionEnabled(false);
	}

	/*
	 * change method block 
	 * methods fired by docModel  
	 */
	
	/*
	 * fired in NameDocumentListener methods
	 */
	public void changeStateName() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setName(pnlProperties.getSubProcessPropertiesPanel().getModel().getName());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in StateModelDataListener methods
	 */
	public void changeStateModel() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			//((SubProcessShape) shapeSelected).setNumeral(pnlProperties.getSubProcessPropertiesPanel().getModel().getStateModelForComboBox());
			
			((SubProcessShape) shapeSelected).getStateVO().setStateModelVO(pnlProperties.getSubProcessPropertiesPanel().getModel().getStateModelVO());
						
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in SubProcessUsageCriteriaListener methods
	 */
	public void changeSubProcessUsageCriteria() {
		Integer ucId = pnlProperties.getSubProcessPropertiesPanel().getModel().getSubProcessUsageCriteria();
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape && ucId.intValue() > 0) {
			((SubProcessShape) shapeSelected).setSubProcessUsageCriteria(ucId);
			
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in RuntimeDocumentListener methods
	 */
	public void changeStateRuntime() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setRuntime(pnlProperties.getSubProcessPropertiesPanel().getModel().getRuntime());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in RuntimeDocumentListener methods
	 */
	public void changeStateRuntimeFormat() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setRuntimeFormat(pnlProperties.getSubProcessPropertiesPanel().getModel().getRuntimeFormat());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	/*
	 * fired in DescpriptionDocumentListener methods
	 */
	public void changeStateDescription() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setDescription(pnlProperties.getSubProcessPropertiesPanel().getModel().getDescription());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in GuarantorDocumentListener methods
	 */
	public void changeStateGuarantor() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setGuarantor(pnlProperties.getSubProcessPropertiesPanel().getModel().getGuarantor());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in SecondGuarantorDocumentListener methods
	 */
	public void changeStateSecondGuarantor() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setSecondGuarantor(pnlProperties.getSubProcessPropertiesPanel().getModel().getSecondGuarantor());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	/*
	 * fired in SupervisorDocumentListener methods
	 */
	public void changeStateSupervisor() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setSupervisor(pnlProperties.getSubProcessPropertiesPanel().getModel().getSupervisor());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in OriginalSystemDocumentListener methods
	 */
	public void changeStateOriginalSystem() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessShape) {
			((SubProcessShape) shapeSelected).setOriginalSystem(pnlProperties.getSubProcessPropertiesPanel().getModel().getOriginalSystem());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	/*
	 * fired in RuntimeDocumentListener methods
	 */
	public void changeTransitionState() {
		if (shapeSelected != null && shapeSelected instanceof SubProcessTransition) {
			((SubProcessTransition) shapeSelected).setStateId(pnlProperties.getSubProcessTransitionPropertiesPanel().getModel().getGeneratorStateId());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	/*
	 * fired in NoteDocumentListener methods
	 */
	public void changeNoteText() {
		if (shapeSelected != null && shapeSelected instanceof NoteShape) {
			((NoteShape) shapeSelected).setText(pnlProperties.getNotePanel().getModel().getText());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void removeRule(SortedRuleVO vo) throws CreateException, RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).removeRule(vo.getId());
			pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(((StateTransition) shapeSelected).getRuleIdsWithRunAfterwards()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void addRule(SortedRuleVO vo) throws CreateException, RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).addRule(vo.getId(), vo.isRunAfterwards());
			pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(((StateTransition) shapeSelected).getRuleIdsWithRunAfterwards()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void addRole(MasterDataVO mdvo) throws CreateException, RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).addRole(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shapeSelected).getRoles()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void removeRole(MasterDataVO mdvo) throws CreateException, RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).removeRole(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shapeSelected).getRoles()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	@Override
    public void focusGained(FocusEvent e) {
		pnlShapeViewer.requestFocus();
	}

	@Override
    public void focusLost(FocusEvent e) {
	}

	private static StateModelLayout newLayoutInfo(ProcessMonitorGraphVO stategraphvo) {
		final StateModelLayout result = new StateModelLayout();
		for (SubProcessVO statevo : stategraphvo.getStates()) {
			result.insertStateLayout(statevo.getWorkingId(), new StateLayout(0d, 0d, 120d, 48d));
		}

		// StateModelStartShape
		//result.insertStateLayout(STARTING_STATE_ID, new StateLayout(8d, 8d, 12d, 12d));

		for (ProcessTransitionVO statetransitionvo : stategraphvo.getTransitions()) {
			final int iConnectionStart = (statetransitionvo.getStateSource() != null) ? AbstractShape.CONNECTION_NE : -1;
			final int iConnectionEnd = (statetransitionvo.getStateTarget() != null) ? AbstractShape.CONNECTION_N : -1;
			final TransitionLayout transitionlayout = new TransitionLayout(statetransitionvo.getId(), iConnectionStart, iConnectionEnd);

			result.insertTransitionLayout(statetransitionvo.getId(), transitionlayout);
		}
		return result;
	}

	/*
	 * paints ProcessMonitorModel onto Editor
	 */
	public ProcessMonitorVO setStateGraph(ProcessMonitorGraphVO vo) throws NuclosBusinessException {

		this.stategraphvo = vo;

		final ShapeModel shapemodel = pnlShapeViewer.getModel();
		final Map<Integer, SubProcessEntry> mpShapes = CollectionUtils.newHashMap();

		shapemodel.clear();
		selectionChanged(null);
		try {
			shapemodel.setActiveLayer("Default");

			layoutinfo = stategraphvo.getStateModel().getLayout();
			if (layoutinfo == null) {
				layoutinfo = newLayoutInfo(stategraphvo);
			}


			// add subporcess to shape model:
			for (SubProcessVO statevo : stategraphvo.getStates()) {
				final SubProcessShape subShape = new SubProcessShape(statevo);
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
			for (ProcessTransitionVO processtransitionvo : stategraphvo.getTransitions()) {
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

				if (iSourceStateId == null && iTargetStateId != null) {
					// Initial transition to start state
					processtransition.setSourceConnection(new ConnectionPoint(newStartShape(layoutinfo), AbstractShape.CONNECTION_CENTER));
				}

				if (iSourceStateId != null) {
					final SubProcessEntry entry = mpShapes.get(iSourceStateId);
					if (start < 0) {
						log.error("Startpunkt ist f\u00e4lschlicherweise < 0 (" + start + ", readModel())");
						start = 0;
					}
					processtransition.setSourceConnection(new ConnectionPoint(entry.getShape(), start));
				}
				if (iTargetStateId != null) {
					final SubProcessEntry entry = mpShapes.get(iTargetStateId);
					if (end < 0) {
						log.error("Endpunkt ist f\u00e4lschlicherweise < 0 (" + end + ", readModel())");
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
			clear();
			throw new NuclosBusinessException(ex);
		}
		return stategraphvo.getStateModel();
	}

	private static StateModelStartShape newStartShape(StateModelLayout layoutinfo) {
		final StateLayout statelayoutStartShape = layoutinfo.getStateLayout(STARTING_STATE_ID);
		final double dStartShapeX = statelayoutStartShape.getX() < 8d ? 8d : statelayoutStartShape.getX();
		final double dStartShapeY = statelayoutStartShape.getY() < 8d ? 8d : statelayoutStartShape.getY();
		return new StateModelStartShape(dStartShapeX, dStartShapeY, 12d, 12d);
	}

	
	public void createNewStateModel(ProcessMonitorVO statemodelvo) {
		final ShapeModel model = pnlShapeViewer.getModel();
		model.clear();
		selectionChanged(null);

		stategraphvo = new ProcessMonitorGraphVO(statemodelvo);
		layoutinfo = newLayoutInfo(stategraphvo);
		try {
			model.setActiveLayer("Default");
		}
		catch (ShapeControllerException e) {
			e.printStackTrace();
		}
		pnlShapeViewer.repaint();
	}
	
	// @todo document and/or refactor
	public ProcessMonitorGraphVO prepareForSaving(ProcessMonitorVO statemodelvo) throws CommonBusinessException {
		try {
			if(layoutinfo == null) {
				layoutinfo = new StateModelLayout();
			}
			prepareForSaving(pnlShapeViewer.getModel(), stategraphvo, layoutinfo, statemodelvo);
			return stategraphvo;
		}
		catch (ShapeControllerException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	/*
	 * Collects the non-collectable Components from "DetailsPanel" 
	 */
	private static void prepareForSaving(ShapeModel model, ProcessMonitorGraphVO processgraphvo, StateModelLayout layoutinfo, ProcessMonitorVO statemodelvo) throws ShapeControllerException, CommonValidationException {
		// save subprocess's:
		model.setActiveLayer("Default");
		for (Iterator<Shape> iterShapes = model.getActiveLayer().getShapes().iterator(); iterShapes.hasNext();) {
			final Shape shape = iterShapes.next();
			if (shape instanceof SubProcessShape) {
				final SubProcessShape stateshape = (SubProcessShape) shape;
				if (stateshape.getStateVO().getId() == null) {
					processgraphvo.getStates().add(stateshape.getStateVO());
				}
				if (stateshape.getStateVO().getWorkingId() < 0) {
					layoutinfo.insertStateLayout(stateshape.getStateVO().getWorkingId(),
							new StateLayout(stateshape.getX(), stateshape.getY(), stateshape.getWidth(), stateshape.getHeight()));
				}
				else {
					layoutinfo.updateState(stateshape.getStateVO().getWorkingId(), stateshape.getX(), stateshape.getY(), stateshape.getWidth(),
							stateshape.getHeight());
				}
			}
			else if (shape instanceof StateModelStartShape) {
				layoutinfo.updateState(STARTING_STATE_ID, shape.getX(), shape.getY(), shape.getWidth(), shape.getHeight());
			}
		}

		// save transitions:
		model.setActiveLayer("Connectors");
		for (Iterator<Shape> iterTransitionShapes = model.getActiveLayer().getShapes().iterator(); iterTransitionShapes.hasNext();)
		{
			final SubProcessTransition statetransition = (SubProcessTransition) iterTransitionShapes.next();
			final Integer iTempId = statetransition.getStateTransitionVO().getClientId();

			if (iTempId < 0 || statetransition.getStateTransitionVO().getId() == null) {
				final TransitionLayout layout = new TransitionLayout(iTempId,
						statetransition.getSourceConnection() != null ? statetransition.getSourceConnection().getTargetPoint() : -2,
						statetransition.getDestinationConnection() != null ? statetransition.getDestinationConnection().getTargetPoint() : -2);
				layoutinfo.insertTransitionLayout(iTempId, layout);
				processgraphvo.getTransitions().add(statetransition.getStateTransitionVO());
			}
			else {
				layoutinfo.updateTransition(iTempId,
						statetransition.getSourceConnection() != null ? statetransition.getSourceConnection().getTargetPoint() : -2,
						statetransition.getDestinationConnection() != null ? statetransition.getDestinationConnection().getTargetPoint() : -2);
			}
		}

		// save notes:
		model.setActiveLayer("Notes");
		layoutinfo.getNotes().clear();
		for (Iterator<Shape> iterNoteShapes = model.getActiveLayer().getShapes().iterator(); iterNoteShapes.hasNext();) {
			final NoteShape note = (NoteShape) iterNoteShapes.next();
			layoutinfo.getNotes().add(new NoteLayout(note.getText(), note.getX(), note.getY(),
					note.getWidth(), note.getHeight()));
		}

		statemodelvo.setLayout(layoutinfo);
		
		processgraphvo.getStateModel().setLayout(layoutinfo);
		processgraphvo.getStateModel().setName(statemodelvo.getName());
		processgraphvo.getStateModel().setDescription((statemodelvo.getDescription()));
		//stategraphvo.validate();
	}

	/*
	 * set the select cursor for the editor
	 */
	public void setSelectionTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_SELECTION);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(null);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/*
	 * set the "insert subprocess" cursor for the editor
	 * set the SubProcessShape class as select tool for the controller
	 */
	public void setStateTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_INSERT_SHAPE);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(SubProcessShape.class);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		try {
			pnlShapeViewer.getModel().setActiveLayer("Default");
		}
		catch (ShapeControllerException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * set the "insert transition" cursor for the editor
	 * set the SubProcessTransition class as select tool for the controller
	 */
	public void setTransitionTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_INSERT_CONNECTOR);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(SubProcessTransition.class);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		try {
			pnlShapeViewer.getModel().setActiveLayer("Connectors");
		}
		catch (ShapeControllerException e) {
			e.printStackTrace();
		}
	}

	public void setNoteTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_INSERT_SHAPE);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(NoteShape.class);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		try {
			pnlShapeViewer.getModel().setActiveLayer("Notes");
		}
		catch (ShapeControllerException e) {
			// @todo
			e.printStackTrace();
		}
	}

	/*
	 * delete the selected shape
	 */
	public void deleteSelection() {
		pnlShapeViewer.getModel().removeShapes(pnlShapeViewer.getModel().getSelection());
		pnlShapeViewer.repaint();
	}

	public void zoomIn() {
		if (iCurrentZoom < adZoomSteps.length - 1) {
			iCurrentZoom++;
			actZoomOut.setEnabled(true);
			pnlShapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
			labZoom.setText(new String(new Double(adZoomSteps[iCurrentZoom]).intValue() + "%"));
		}
		else {
			actZoomIn.setEnabled(false);
		}
	}

	public void zoomOut() {
		if (iCurrentZoom > 0) {
			iCurrentZoom--;
			actZoomIn.setEnabled(true);
			pnlShapeViewer.setZoom(adZoomSteps[iCurrentZoom] / 100d);
			labZoom.setText(new String(new Double(adZoomSteps[iCurrentZoom]).intValue() + "%"));
		}
		else {
			actZoomOut.setEnabled(false);
		}
	}

	public void printStateModel() {
		for (ActionListener al : lstPrintEventListeners) {
			al.actionPerformed(new ActionEvent(this, 0, ""));
		}
	}

	public void clear() {
		pnlShapeViewer.getModel().clear();
		pnlShapeViewer.repaint();
	}

	public boolean stopEditing() {
		/** @todo It might be better to programmatically deselect the currently selected object(s) here.
		 * closeSubForms() would then be called if needed. */
		this.closeSubForms();
		return true;
	}

	/**
	 * Rerurns the Shapeviewer used by the editor
	 * @return DefaultShapeViewer
	 */
	public DefaultShapeViewer getPnlShapeViewer() {
		return pnlShapeViewer;
	}

}	// class StateModelEditor
