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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
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
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.eventsupport.EventSupportDelegate;
import org.nuclos.client.eventsupport.EventSupportRepository;
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
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.statemodel.models.NotePropertiesPanelModel;
import org.nuclos.client.statemodel.models.StatePropertiesPanelModel;
import org.nuclos.client.statemodel.panels.StateModelEditorPropertiesPanel;
import org.nuclos.client.statemodel.panels.rights.RightTransfer;
import org.nuclos.client.statemodel.panels.rights.RightTransfer.RoleRights;
import org.nuclos.client.statemodel.shapes.NoteShape;
import org.nuclos.client.statemodel.shapes.StateModelStartShape;
import org.nuclos.client.statemodel.shapes.StateShape;
import org.nuclos.client.statemodel.shapes.StateTransition;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelListener;
import org.nuclos.client.ui.collect.component.model.DetailsComponentModelEvent;
import org.nuclos.client.ui.collect.component.model.SearchComponentModelEvent;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.statemodel.valueobject.NoteLayout;
import org.nuclos.server.statemodel.valueobject.StateGraphVO;
import org.nuclos.server.statemodel.valueobject.StateLayout;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.TransitionLayout;

/**
 * The state model editor.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class StateModelEditor extends JPanel implements ShapeModelListener, FocusListener {

	private static final Logger LOG = Logger.getLogger(StateModelEditor.class);

	/**
	 * id of the starting state
	 */
	private static final Integer STARTING_STATE_ID = -666;

	private class SelectAction extends AbstractAction {

		SelectAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.1","Auswahl"), 
					Icons.getInstance().getIconSelectObject());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnSelection.setSelected(true);
			setSelectionTool();
		}
	}

	private class NewStateAction extends AbstractAction {

		NewStateAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.9","Neuer Status"), 
					Icons.getInstance().getIconState());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertState.setSelected(true);
			setStateTool();
		}
	}

	private class NewTransitionAction extends AbstractAction {

		NewTransitionAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.10","Neue Transition"), 
					Icons.getInstance().getIconStateTransition());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertTransition.setSelected(true);
			setTransitionTool();
		}
	}

	private class NewNoteAction extends AbstractAction {

		NewNoteAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.8","Neue Bemerkung"), 
					Icons.getInstance().getIconStateNewNote());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			btnInsertNote.setSelected(true);
			setNoteTool();
		}
	}

	private class DeleteAction extends AbstractAction {

		DeleteAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.3","Auswahl l\u00f6schen"), 
					Icons.getInstance().getIconDelete16());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
				deleteSelection();
		}
	}

	private class DefaultTransitionAction extends AbstractAction {

		DefaultTransitionAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.19","Als Standardpfad definieren"));
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			if (shapeSelected instanceof AbstractConnector) {
				((StateTransition) shapeSelected).getStateTransitionVO().setDefault(btnDefaultTransition.getSelectedObjects() != null);
				pnlProperties.getTransitionRulePanel().getBtnDefault().setSelected(btnDefaultTransition.getSelectedObjects() != null);
				
				getViewer().getModel().fireModelChanged();
				((Component) getViewer()).repaint();
			}	
		}
	}
	
	private class CopyStateRightsAction extends AbstractAction {

		CopyStateRightsAction() {
			super(SpringLocaleDelegate.getInstance().getMessage(
					"StateModelEditor.17","Rechte & Pflichten kopieren"), 
					Icons.getInstance().getIconCopy16());
		}
		
		@Override
        public void actionPerformed(ActionEvent e) {
				copyStateRights();
		}
	}
	
	private class PasteStateRightsAction extends AbstractAction {

		PasteStateRightsAction() {
			super(SpringLocaleDelegate.getInstance().getMessage(
					"StateModelEditor.18","Rechte & Pflichten einfügen"), 
					Icons.getInstance().getIconPaste16());
		}
		
		@Override
        public void actionPerformed(ActionEvent e) {
				pasteStateRights();
		}
	}

	private class ZoomInAction extends AbstractAction {

		ZoomInAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.15","Zoom +"), 
					Icons.getInstance().getIconZoomIn());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			zoomIn();
		}
	}

	private class ZoomOutAction extends AbstractAction {

		ZoomOutAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.13","Zoom -"), 
					Icons.getInstance().getIconZoomOut());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			zoomOut();
		}
	}

	private class PrintAction extends AbstractAction {

		PrintAction() {
			super(SpringLocaleDelegate.getInstance().getMessage("StateModelEditor.5","Drucken..."), 
					Icons.getInstance().getIconPrint16());
		}

		@Override
        public void actionPerformed(ActionEvent e) {
			printStateModel();
		}
	}

	private class NameDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateName();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateName();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateName();
		}
	}

	private class MnemonicDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateMnemonic();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateMnemonic();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateMnemonic();
		}
	}


	private class IconDocumentListener implements CollectableComponentModelListener {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			StateModelEditor.this.changeStateIcon();
		}

		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			// ...
			
		}

		@Override
		public void valueToBeChanged(DetailsComponentModelEvent ev) {
			// ...			
		}
	}
	
	
	private class ButtonIconChangeListener implements CollectableComponentModelListener {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			StateModelEditor.this.changeButtonIcon();
		}

		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			// ...
			
		}

		@Override
		public void valueToBeChanged(DetailsComponentModelEvent ev) {
			// ...			
		}
	}
	
	
	private class ColorChangeListener implements CollectableComponentModelListener {
		@Override
		public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
			StateModelEditor.this.changeColor();
		}

		@Override
		public void searchConditionChangedInModel(SearchComponentModelEvent ev) {
			// ...
			
		}

		@Override
		public void valueToBeChanged(DetailsComponentModelEvent ev) {
			// ...			
		}
	}
	

	private class DescriptionDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateDescription();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateDescription();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			StateModelEditor.this.changeStateDescription();
		}
	}

	private class NoteDocumentListener implements DocumentListener {
		@Override
        public void changedUpdate(DocumentEvent e) {
			StateModelEditor.this.changeNoteText();
		}

		@Override
        public void insertUpdate(DocumentEvent e) {
			StateModelEditor.this.changeNoteText();
		}

		@Override
        public void removeUpdate(DocumentEvent e) {
			StateModelEditor.this.changeNoteText();
		}
	}
	
	private class TabDataListener implements ListDataListener {
		@Override
        public void contentsChanged(ListDataEvent e) {
			StateModelEditor.this.changeTabData();
		}

		@Override
        public void intervalAdded(ListDataEvent e) {
		}

		@Override
        public void intervalRemoved(ListDataEvent e) {
		}
	}

	private final DefaultShapeViewer pnlShapeViewer;
	private final JScrollPane scrollPane;
	private final StateModelEditorPropertiesPanel pnlProperties;
/**
     * @return the pnlProperties
     */
    public StateModelEditorPropertiesPanel getPropertiesPanel() {
    	return pnlProperties;
    }

	//	private final JSplitPane splitpnMain;
	private final JToolBar toolbar = new JToolBar();
	private final Action actSelect = new SelectAction();
	private final Action actNewState = new NewStateAction();
	private final Action actNewTransition = new NewTransitionAction();
	private final Action actNewNote = new NewNoteAction();
	private final Action actDelete = new DeleteAction();
	private final Action actDefaultTransition = new DefaultTransitionAction();
	private final Action actCopyStateRights = new CopyStateRightsAction();
	private final Action actPasteStateRights = new PasteStateRightsAction();
	private final Action actZoomIn = new ZoomInAction();
	private final Action actZoomOut = new ZoomOutAction();
	private final Action actPrint = new PrintAction();
	private final JLabel labZoom = new JLabel("100%");
	private final JToggleButton btnSelection = new JToggleButton(actSelect);
	private final JToggleButton btnInsertState = new JToggleButton(actNewState);
	private final JToggleButton btnInsertTransition = new JToggleButton(actNewTransition);
	private final JToggleButton btnInsertNote = new JToggleButton(actNewNote);
	private final JCheckBoxMenuItem btnDefaultTransition = new JCheckBoxMenuItem(actDefaultTransition);
	private final double[] adZoomSteps = {30d, 50d, 75d, 100d, 125d, 150d, 200d, 300d};
	private int iCurrentZoom = 3;
	private final List<ChangeListener> lstChangeListeners = new Vector<ChangeListener>();
	private Shape shapeSelected;

	/** @todo eliminate this field - use local variables instead */
	private StateGraphVO stategraphvo;
	
	private List<CollectableEntityObject> usages;

	private StateModelLayout layoutinfo;
	private final List<ActionListener> lstPrintEventListeners = new Vector<ActionListener>();
	private final NameDocumentListener nameDocumentListener = new NameDocumentListener();
	private final MnemonicDocumentListener mnemonicDocumentListener = new MnemonicDocumentListener();
	private final IconDocumentListener iconDocumentListener = new IconDocumentListener();
	private final DescriptionDocumentListener descriptionDocumentListener = new DescriptionDocumentListener();
	private final NoteDocumentListener noteDocumentListener = new NoteDocumentListener();
	private final TabDataListener tabDataListener = new TabDataListener();
	private final ColorChangeListener colorChangeListener = new ColorChangeListener();
	private final ButtonIconChangeListener buttonIconChangeListener = new ButtonIconChangeListener();

//	private StateRoleSubFormController ctlsubformRole;
//	private StateRoleAttributeGroupSubFormController ctlsubformAttributeGroup;
//	private StateRoleSubFormsSubFormController ctlsubformSubForm;

	public StateModelEditor() {
		super(new BorderLayout());

		pnlShapeViewer = new DefaultShapeViewer();
		pnlProperties = new StateModelEditorPropertiesPanel(this);
		scrollPane = new JScrollPane(pnlShapeViewer);
		
//		splitpnMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, pnlProperties);

		pnlShapeViewer.getModel().addShapeModelListener(this);
		pnlShapeViewer.setExtents(new Extents2D(1024, 1024));

		this.init();
	}

	private void init() {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		final ButtonGroup bg = new ButtonGroup();

		btnSelection.setSelected(true);
		btnSelection.setText("");
		btnSelection.setToolTipText(localeDelegate.getMessage("StateModelEditor.2","Auswahl"));
		bg.add(btnSelection);
		toolbar.add(btnSelection);
		btnSelection.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertState.setText("");
		btnInsertState.setToolTipText(localeDelegate.getMessage("StateModelEditor.11","Status einf\u00fcgen"));
		bg.add(btnInsertState);
		toolbar.add(btnInsertState);
		btnInsertState.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertTransition.setText("");
		btnInsertTransition.setToolTipText(localeDelegate.getMessage("StateModelEditor.12","Transition einf\u00fcgen"));
		bg.add(btnInsertTransition);
		toolbar.add(btnInsertTransition);
		btnInsertTransition.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		btnInsertNote.setText("");
		btnInsertNote.setToolTipText(localeDelegate.getMessage("StateModelEditor.7","Kommentar einf\u00fcgen"));
		bg.add(btnInsertNote);
		toolbar.add(btnInsertNote);
		btnInsertNote.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		JButton btn = new JButton(actDelete);
		btn.setText("");
		btn.setToolTipText(localeDelegate.getMessage("StateModelEditor.4","Auswahl l\u00f6schen"));
		toolbar.addSeparator();
		toolbar.add(btn);
		btn.setEnabled(SecurityCache.getInstance().isWriteAllowedForMasterData(NuclosEntity.STATEMODEL));

		toolbar.addSeparator();
		btn = toolbar.add(actPrint);
		btn.setText("");
		btn.setToolTipText(localeDelegate.getMessage("StateModelEditor.6","Drucken"));

		toolbar.addSeparator();
		labZoom.setFont(new Font("Dialog", Font.PLAIN, 8));
		btn = toolbar.add(actZoomIn);
		btn.setText("");
		btn.setToolTipText(localeDelegate.getMessage("StateModelEditor.16","Zoom +"));
		toolbar.add(labZoom);

		btn = toolbar.add(actZoomOut);
		btn.setText("");
		btn.setToolTipText(localeDelegate.getMessage("StateModelEditor.14","Zoom -"));


		toolbar.setOrientation(JToolBar.VERTICAL);

		this.add(toolbar, BorderLayout.WEST);		
		this.add(scrollPane, BorderLayout.CENTER);
		//this.add(pnlProperties, BorderLayout.EAST);
		
//		splitpnMain.setResizeWeight(0.8d);

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
		popup.add(btnDefaultTransition);
		popup.add(actDelete);
		popup.addSeparator();
		popup.add(actCopyStateRights);
		popup.add(actPasteStateRights);
		pnlShapeViewer.getController().setPopupMenu(popup);

		pnlShapeViewer.getModel().addLayer("Notes", true, 3);
		
	}

	public ShapeViewer getViewer() {
		return pnlShapeViewer;
	}

	public StateModelEditorPropertiesPanel getStateModelEditorPropertiesPanel(){
		return this.pnlProperties;
	}

	public void addChangeListener(ChangeListener cl) {
		lstChangeListeners.add(cl);
		getStateModelEditorPropertiesPanel().getStatePropertiesPanel().getStateDependantRightsPanel().addDetailsChangedListener(cl);
	}

	public void removeChangeListener(ChangeListener cl) {
		lstChangeListeners.remove(cl);
		getStateModelEditorPropertiesPanel().getStatePropertiesPanel().getStateDependantRightsPanel().removeDetailsChangedListener(cl);
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
		pnlProperties.getStatePropertiesPanel().getModel().clctImage.getModel().addCollectableComponentModelListener(iconDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docDescription.addDocumentListener(descriptionDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().modelTab.addListDataListener(tabDataListener);
		pnlProperties.getStatePropertiesPanel().getModel().clctColor.getModel().addCollectableComponentModelListener(colorChangeListener);
		pnlProperties.getStatePropertiesPanel().getModel().clctButtonIcon.getModel().addCollectableComponentModelListener(buttonIconChangeListener);
	}

	/**
	 * removes the listeners for the state (properties) panel.
	 */
	private void removeStatePanelListeners() {
		pnlProperties.getStatePropertiesPanel().getModel().docName.removeDocumentListener(nameDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docMnemonic.removeDocumentListener(mnemonicDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().clctImage.getModel().removeCollectableComponentModelListener(iconDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().docDescription.removeDocumentListener(descriptionDocumentListener);
		pnlProperties.getStatePropertiesPanel().getModel().clctColor.getModel().removeCollectableComponentModelListener(colorChangeListener);
		pnlProperties.getStatePropertiesPanel().getModel().clctButtonIcon.getModel().removeCollectableComponentModelListener(buttonIconChangeListener);
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

	private void setDefaultTransitionActionEnabled(boolean enabled) {
		actDefaultTransition.setEnabled(enabled);
	}

	private void setDefaultTransitionActionSelected(boolean selected) {
		btnDefaultTransition.setSelected(selected);
	}
	
	private void setCopyAndPasteStateRightAction(boolean enabled) {
		actCopyStateRights.setEnabled(enabled);
		actPasteStateRights.setEnabled(enabled);
	}

	@Override
    public void selectionChanged(Shape shape) {
		setDeleteActionEnabled(shape != null || pnlShapeViewer.getModel().isMultiSelected());
		setDefaultTransitionActionEnabled(false);
		setDefaultTransitionActionSelected(false);
		setCopyAndPasteStateRightAction(shape != null && !pnlShapeViewer.getModel().isMultiSelected());

		this.handlePreviousSelection();

		if (pnlShapeViewer.getModel().isMultiSelected()) {
			/** @todo this seems never to be called */
			LOG.debug("selectionChanged: multi selection");
			/** @todo show rights editor for multiple states */
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
		else if (shape == null) {
			LOG.debug("selectionChanged: nothing selected");
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
		else if (shape instanceof StateShape) {
			LOG.debug("selectionChanged: single state shape selected");
			pnlProperties.setPanel("State");
			shapeSelected = shape;
			final StateShape stateshapeSelected = (StateShape) shape;
			stateshapeSelected.checkStatus();

			final StatePropertiesPanelModel model = pnlProperties.getStatePropertiesPanel().getModel();
			model.setName(stateshapeSelected.getName());
			model.setNumeral(stateshapeSelected.getNumeral());
			model.setIcon(stateshapeSelected.getIcon());
			model.setDescription(stateshapeSelected.getDescription());
			model.setTab(stateshapeSelected.getStateVO().getTabbedPaneName());
			model.setColor(stateshapeSelected.getStateVO().getColor());
			model.setButtonIcon(stateshapeSelected.getStateVO().getButtonIcon());

			this.addStatePanelListeners();
			this.setupRightsPanel(stateshapeSelected.getStateVO());
//			this.setupSubforms(stateshapeSelected.getStateVO());
		}
		else if (shape instanceof NoteShape) {
			LOG.debug("selectionChanged: single note shape selected");
			pnlProperties.setPanel("Note");
			shapeSelected = shape;

			final NotePropertiesPanelModel model = pnlProperties.getNotePanel().getModel();
			model.setText(((NoteShape) shapeSelected).getText());

			this.addNotePanelListeners();
		}
		else if (shape instanceof AbstractConnector) {
			LOG.debug("selectionChanged: single transition shape selected");
			pnlProperties.setPanel("Transition");
			shapeSelected = shape;
			
			setDefaultTransitionActionEnabled(!((StateTransition) shapeSelected).getStateTransitionVO().isAutomatic());
			setDefaultTransitionActionSelected(((StateTransition) shapeSelected).getStateTransitionVO().isDefault());
			
			try {
				List<SortedRuleVO> selectedRules = 
						RuleRepository.getInstance().selectRulesById(((StateTransition) shape).getRuleIdsWithRunAfterwards());
				
				selectedRules.addAll(0,
						EventSupportDelegate.getInstance().selectEventSupportById(((StateTransition) shape).getEventSupportsWithRunAfterwards()));
				
				pnlProperties.getTransitionRulePanel().getModel().setRules(selectedRules);
				
				TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRulePanel().getTblRules(), 10, 10);
				pnlProperties.getTransitionRulePanel().getBtnAutomatic().setSelected(((StateTransition) shapeSelected).getStateTransitionVO().isAutomatic());
				pnlProperties.getTransitionRulePanel().getBtnDefault().setSelected(((StateTransition) shapeSelected).getStateTransitionVO().isDefault());
				pnlProperties.getTransitionRulePanel().getBtnAutomatic().setEnabled(((StateTransition) shapeSelected).getStateTransitionVO().getStateSource()!=null);
				pnlProperties.getTransitionRulePanel().getBtnDefault().setEnabled(((StateTransition) shapeSelected).getStateTransitionVO().getStateSource()!=null);
				pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shape).getRoles()));
				TableUtils.setPreferredColumnWidth(pnlProperties.getTransitionRolePanel().getTblRoles(), 10, 10);
			}
			catch (RemoteException ex) {
				Errors.getInstance().showExceptionDialog(this, ex.getMessage(), ex);
			}
		}
		else {
			LOG.debug("selectionChanged: something unknown was selected");
			pnlProperties.setPanel("None");
			shapeSelected = null;
		}
	}

	private void handlePreviousSelection() {
		if (shapeSelected instanceof StateShape) {
			LOG.debug("State shape deselected.");

			// get user rights from properties panel and store them in the selected state's vo:
//			final StateShape stateshape = (StateShape) shapeSelected;
			/** @todo endEditing here? */
//			final StateVO statevo = stateshape.getStateVO();
//			final StateVO.UserRights userrights = getUserRightsFromPropertiesPanel();
//			statevo.setUserRights(userrights);
			updateStateProperties();
			
			// this.closeSubForms();
			 
			this.removeStatePanelListeners();
		}
		else if (shapeSelected instanceof NoteShape) {
			LOG.debug("Note shape deselected.");
			this.removeNotePanelListeners();
		}
		else {
			LOG.debug("Something unknown was deselected.");
			// do nothing
		}
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

//	private void setupSubforms(StateVO statevo) {
//		log.debug("setup subforms");
//		/** @todo this is just a test - correct this */
//		if(ctlsubformRole != null)
//			ctlsubformRole.close();
//		
//		// remove listeners in SubForms (from previous selection)
//		pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformRoles().removeAllSubFormToolListeners();
//		pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformAttributeGroups().removeAllSubFormToolListeners();
//		pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getSubformSubForms().removeAllSubFormToolListeners();
//		
//		//(re)init controllers
//		ctlsubformRole = new StateRoleSubFormController(statevo);
//		if(ctlsubformAttributeGroup != null)
//			ctlsubformAttributeGroup.close();
//		ctlsubformAttributeGroup = new StateRoleAttributeGroupSubFormController(statevo);
//		if(ctlsubformSubForm != null)
//			ctlsubformSubForm.close();
//		ctlsubformSubForm = new StateRoleSubFormsSubFormController(statevo);
//	}
//
//	private void closeSubForms() {
//		log.debug("close subforms");
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
//	}

	@Override
    public void multiSelectionChanged(Collection<Shape> collShapes) {
		LOG.debug("multiSelectionChanged");

		/** @todo this is always called when selecting a shape using "rubberbanding", even if a single shape
		 * was selected. In the latter case, selectionChanged() should be called for the selected shape. */

		setDeleteActionEnabled(collShapes.size() > 0);
		setCopyAndPasteStateRightAction(collShapes.size() == 1);
	}

	@Override
    public void shapeDeleted(Shape shape) {
		for (ChangeListener cl : lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
		setDeleteActionEnabled(false);
		setCopyAndPasteStateRightAction(false);
	}

	@Override
    public void shapesDeleted(Collection<Shape> collShapes) {
		for (ChangeListener cl : lstChangeListeners) {
			cl.stateChanged(new ChangeEvent(this));
		}
		setDeleteActionEnabled(false);
		setCopyAndPasteStateRightAction(false);
	}

	public void changeStateName() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setName(pnlProperties.getStatePropertiesPanel().getModel().getName());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void changeStateMnemonic() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setNumeral(pnlProperties.getStatePropertiesPanel().getModel().getNumeral());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void changeStateIcon() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setIcon(pnlProperties.getStatePropertiesPanel().getModel().getIcon());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	public void changeButtonIcon() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).getStateVO().setButtonIcon(pnlProperties.getStatePropertiesPanel().getModel().getButtonIcon());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	public void changeColor() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setStateColor(pnlProperties.getStatePropertiesPanel().getModel().getColor());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void changeStateDescription() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setDescription(pnlProperties.getStatePropertiesPanel().getModel().getDescription());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void changeNoteText() {
		if (shapeSelected != null && shapeSelected instanceof NoteShape) {
			((NoteShape) shapeSelected).setText(pnlProperties.getNotePanel().getModel().getText());
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	public void changeTabData() {
		if (shapeSelected != null && shapeSelected instanceof StateShape) {
			((StateShape) shapeSelected).setTabbedPaneName(pnlProperties.getStatePropertiesPanel().getModel().getTab());
			pnlShapeViewer.getModel().fireModelChanged();
		}
	}

	public void removeRule(SortedRuleVO vo) throws RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			// Rules are available by importing via jar oder creating them in Nuclos in an editor
			// With first case there is no id avaibable beacuse the rule does not exist as an entry in the database
			if (vo.getId() != null)
			{
				((StateTransition) shapeSelected).removeRule(vo.getId());
				pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(((StateTransition) shapeSelected).getRuleIdsWithRunAfterwards()));
			}
			else
			{
				((StateTransition) shapeSelected).removeEventSupport(vo.getClassname());
				pnlProperties.getTransitionRulePanel().getModel().setRules(EventSupportDelegate.getInstance().selectEventSupportById(((StateTransition) shapeSelected).getEventSupportsWithRunAfterwards()));
				
			}
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
 
	public void addRule(SortedRuleVO vo) throws RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) 
		{
			if (vo.getId() != null)
			{
				((StateTransition) shapeSelected).addRule(vo.getId(), vo.isRunAfterwards());
				pnlProperties.getTransitionRulePanel().getModel().setRules(RuleRepository.getInstance().selectRulesById(((StateTransition) shapeSelected).getRuleIdsWithRunAfterwards()));				
			}
			else
			{
				((StateTransition) shapeSelected).addRule(vo.getClassname(), vo.isRunAfterwards());
				pnlProperties.getTransitionRulePanel().getModel().setRules(EventSupportDelegate.getInstance().selectEventSupportById(((StateTransition) shapeSelected).getEventSupportsWithRunAfterwards()));
			}
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}
	
	public void updateRule(SortedRuleVO vo) {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) 
		{
			
			if (vo.getId() != null)
			{
				Pair<Integer, Boolean> rule = null;
				rule = ((StateTransition) shapeSelected).getRule(vo.getId());
				if (rule != null) {
					rule.setY(vo.isRunAfterwards());
					pnlShapeViewer.getModel().fireModelChanged();
					pnlShapeViewer.repaint();
				}
				
			}
			else
			{
				Pair<String, Boolean> rule = null;
					rule = ((StateTransition) shapeSelected).getEventSupport(vo.getClassname());
					if (rule != null) {
						rule.setY(vo.isRunAfterwards());
						pnlShapeViewer.getModel().fireModelChanged();
						pnlShapeViewer.repaint();
					}
			}
		}
	}

	public void addRole(MasterDataVO mdvo) throws RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).addRole(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shapeSelected).getRoles()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	public void removeRole(MasterDataVO mdvo) throws RemoteException {
		if (shapeSelected != null && shapeSelected instanceof StateTransition) {
			((StateTransition) shapeSelected).removeRole(mdvo.getIntId());
			pnlProperties.getTransitionRolePanel().getModel().setRoles(RoleRepository.getInstance().selectRolesById(((StateTransition) shapeSelected).getRoles()));
			pnlShapeViewer.getModel().fireModelChanged();
			pnlShapeViewer.repaint();
		}
	}

	@Override
    public void focusGained(FocusEvent e) {
		pnlShapeViewer.requestFocusInWindow();
	}

	@Override
    public void focusLost(FocusEvent e) {
	}

	private static StateModelLayout newLayoutInfo(StateGraphVO stategraphvo) {
		final StateModelLayout result = new StateModelLayout();
		for (StateVO statevo : stategraphvo.getStates()) {
			result.insertStateLayout(statevo.getClientId(), new StateLayout(0d, 0d, 120d, 48d));
		}

		// StateModelStartShape
		result.insertStateLayout(STARTING_STATE_ID, new StateLayout(8d, 8d, 12d, 12d));

		for (StateTransitionVO statetransitionvo : stategraphvo.getTransitions()) {
			final int iConnectionStart = (statetransitionvo.getStateSource() != null) ? AbstractShape.CONNECTION_NE : -1;
			final int iConnectionEnd = (statetransitionvo.getStateTarget() != null) ? AbstractShape.CONNECTION_N : -1;
			final TransitionLayout transitionlayout = new TransitionLayout(statetransitionvo.getId(), iConnectionStart, iConnectionEnd);

			result.insertTransitionLayout(statetransitionvo.getId(), transitionlayout);
		}
		return result;
	}
	
	/**
	 * 
	 * @param stategraphvo
	 * @param layoutinfo (could be null, would be set inside)
	 * @param pnlShapeViewer
	 * @param log
	 * @throws ShapeControllerException
	 */
	public static StateModelLayout showLayout(StateGraphVO stategraphvo, DefaultShapeViewer pnlShapeViewer, Logger log) throws ShapeControllerException{
		StateModelLayout layoutinfo = stategraphvo.getStateModel().getLayout();
		if (layoutinfo == null) {
			layoutinfo = newLayoutInfo(stategraphvo);
		} 
		
		final ShapeModel shapemodel = pnlShapeViewer.getModel();
		final Map<Integer, StateEntry> mpShapes = CollectionUtils.newHashMap();

		shapemodel.clear();

		shapemodel.setActiveLayer("Default");
		shapemodel.addShape(newStartShape(layoutinfo));

		// add states to shape model:
		for (StateVO statevo : stategraphvo.getStates()) {
			final StateShape stateshape = new StateShape(statevo);
			shapemodel.addShape(stateshape);

			final StateLayout layout = layoutinfo.getStateLayout(statevo.getClientId());
			if (layout != null) {
				stateshape.setDimension(new Rectangle2D.Double(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight()));
			}
			else {
				stateshape.setDimension(new Rectangle2D.Double(0d, 0d, 120d, 48d));
			}

			final StateEntry entry = new StateEntry();
			statevo.setStatename(SpringLocaleDelegate.getInstance().getResource(
					StateDelegate.getInstance().getResourceSIdForName(statevo.getId()), statevo.getStatename()));
			//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForName(statevo.getId())));
			statevo.setDescription(SpringLocaleDelegate.getInstance().getResource(
					StateDelegate.getInstance().getResourceSIdForDescription(statevo.getId()), statevo.getDescription()));
			//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForDescription(statevo.getId())));
			entry.setVo(statevo);
			entry.setShape(stateshape);
			mpShapes.put(statevo.getClientId(), entry);
		}

		// add transitions to shape model:
		for (StateTransitionVO statetransitionvo : stategraphvo.getTransitions()) {
			final StateTransition statetransition = new StateTransition(statetransitionvo);
			statetransition.setView(pnlShapeViewer);
			final Integer iSourceStateId = statetransitionvo.getStateSource();
			final Integer iTargetStateId = statetransitionvo.getStateTarget();

			final TransitionLayout transitionlayout = layoutinfo.getTransitionLayout(statetransitionvo.getId());
			int start = AbstractShape.CONNECTION_NE;
			int end = AbstractShape.CONNECTION_N;
			if (transitionlayout != null) {
				start = transitionlayout.getConnectionStart();
				end = transitionlayout.getConnectionEnd();
			}

			if (iSourceStateId == null && iTargetStateId != null) {
				// Initial transition to start state
				statetransition.setSourceConnection(new ConnectionPoint(newStartShape(layoutinfo), AbstractShape.CONNECTION_CENTER));
			}

			if (iSourceStateId != null) {
				final StateEntry entry = mpShapes.get(iSourceStateId);
				if (start < 0) {
					log.error("Startpunkt ist f\u00e4lschlicherweise < 0 (" + start + ", readModel())");
					start = 0;
				}
				if (entry != null)
					statetransition.setSourceConnection(new ConnectionPoint(entry.getShape(), start));
			}
			if (iTargetStateId != null) {
				final StateEntry entry = mpShapes.get(iTargetStateId);
				if (end < 0) {
					log.error("Endpunkt ist f\u00e4lschlicherweise < 0 (" + end + ", readModel())");
					end = 0;
				}
				if (entry != null)
					statetransition.setDestinationConnection(new ConnectionPoint(entry.getShape(), end));
			}

			shapemodel.setActiveLayer("Connectors");
			shapemodel.addShape(statetransition);
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

		return layoutinfo;
	}

	public StateModelVO setStateGraph(StateGraphVO stategraphvo) throws NuclosBusinessException {
		this.stategraphvo = stategraphvo;

		selectionChanged(null);
		
		try {
			layoutinfo = showLayout(stategraphvo, pnlShapeViewer, LOG);
			
//		final ShapeModel shapemodel = pnlShapeViewer.getModel();
//		final Map<Integer, StateEntry> mpShapes = CollectionUtils.newHashMap();
//
//		shapemodel.clear();
//		selectionChanged(null);
//		try {
//			shapemodel.setActiveLayer("Default");
//
//			layoutinfo = stategraphvo.getStateModel().getLayout();
//			if (layoutinfo == null) {
//				layoutinfo = newLayoutInfo(stategraphvo);
//			}
//
//			shapemodel.addShape(newStartShape(layoutinfo));
//
//			// add states to shape model:
//			for (StateVO statevo : stategraphvo.getStates()) {
//				final StateShape stateshape = new StateShape(statevo);
//				shapemodel.addShape(stateshape);
//
//				final StateLayout layout = layoutinfo.getStateLayout(statevo.getClientId());
//				if (layout != null) {
//					stateshape.setDimension(new Rectangle2D.Double(layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight()));
//				}
//				else {
//					stateshape.setDimension(new Rectangle2D.Double(0d, 0d, 120d, 48d));
//				}
//
//				final StateEntry entry = new StateEntry();
//				statevo.setStatename(SpringLocaleDelegate.getResource(StateDelegate.getInstance().getResourceSIdForName(statevo.getId()), statevo.getStatename()));//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForName(statevo.getId())));
//				statevo.setDescription(SpringLocaleDelegate.getResource(StateDelegate.getInstance().getResourceSIdForDescription(statevo.getId()), statevo.getDescription()));//LocaleDelegate.getInstance().getResourceByIntId(StateDelegate.getInstance().getResourceIdForDescription(statevo.getId())));
//				entry.setVo(statevo);
//				entry.setShape(stateshape);
//				mpShapes.put(statevo.getClientId(), entry);
//			}
//
//			// add transitions to shape model:
//			for (StateTransitionVO statetransitionvo : stategraphvo.getTransitions()) {
//				final StateTransition statetransition = new StateTransition(statetransitionvo);
//				statetransition.setView(pnlShapeViewer);
//				final Integer iSourceStateId = statetransitionvo.getStateSource();
//				final Integer iTargetStateId = statetransitionvo.getStateTarget();
//
//				final TransitionLayout transitionlayout = layoutinfo.getTransitionLayout(statetransitionvo.getId());
//				int start = AbstractShape.CONNECTION_NE;
//				int end = AbstractShape.CONNECTION_N;
//				if (transitionlayout != null) {
//					start = transitionlayout.getConnectionStart();
//					end = transitionlayout.getConnectionEnd();
//				}
//
//				if (iSourceStateId == null && iTargetStateId != null) {
//					// Initial transition to start state
//					statetransition.setSourceConnection(new ConnectionPoint(newStartShape(layoutinfo), AbstractShape.CONNECTION_CENTER));
//				}
//
//				if (iSourceStateId != null) {
//					final StateEntry entry = mpShapes.get(iSourceStateId);
//					if (start < 0) {
//						log.error("Startpunkt ist f\u00e4lschlicherweise < 0 (" + start + ", readModel())");
//						start = 0;
//					}
//					statetransition.setSourceConnection(new ConnectionPoint(entry.getShape(), start));
//				}
//				if (iTargetStateId != null) {
//					final StateEntry entry = mpShapes.get(iTargetStateId);
//					if (end < 0) {
//						log.error("Endpunkt ist f\u00e4lschlicherweise < 0 (" + end + ", readModel())");
//						end = 0;
//					}
//					statetransition.setDestinationConnection(new ConnectionPoint(entry.getShape(), end));
//				}
//
//				shapemodel.setActiveLayer("Connectors");
//				shapemodel.addShape(statetransition);
//			}
//
//			// add notes to shape model:
//			for (NoteLayout notelayout : layoutinfo.getNotes()) {
//				final NoteShape noteshape = new NoteShape();
//				noteshape.setDimension(new Rectangle2D.Double(notelayout.getX(), notelayout.getY(), notelayout.getWidth(), notelayout.getHeight()));
//				noteshape.setText(notelayout.getText());
//				shapemodel.setActiveLayer("Notes");
//				shapemodel.addShape(noteshape);
//			}
//			pnlShapeViewer.repaint();
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

	public void createNewStateModel(StateModelVO statemodelvo) {
		final ShapeModel model = pnlShapeViewer.getModel();
		model.clear();
		selectionChanged(null);

		stategraphvo = new StateGraphVO(statemodelvo);
		layoutinfo = newLayoutInfo(stategraphvo);
		try {
			model.setActiveLayer("Default");
		}
		catch (ShapeControllerException e) {
			LOG.warn("createNewStateModel failed: " + e, e);
		}
		model.addShape(new StateModelStartShape(8d, 8d, 12d, 12d));
		pnlShapeViewer.repaint();
	}

	// @todo document and/or refactor
	public StateGraphVO prepareForSaving(StateModelVO statemodelvo) throws CommonBusinessException {
		try {
			prepareForSaving(pnlShapeViewer.getModel(), stategraphvo, layoutinfo, statemodelvo);
			return stategraphvo;
		}
		catch (ShapeControllerException ex) {
			throw new NuclosBusinessException(ex);
		}
	}

	private static void prepareForSaving(ShapeModel model, StateGraphVO stategraphvo, StateModelLayout layoutinfo, StateModelVO statemodelvo) throws ShapeControllerException, CommonValidationException {
		// save states:
		model.setActiveLayer("Default");
		for (Iterator<Shape> iterShapes = model.getActiveLayer().getShapes().iterator(); iterShapes.hasNext();) {
			final Shape shape = iterShapes.next();
			if (shape instanceof StateShape) {
				final StateShape stateshape = (StateShape) shape;
				if (stateshape.getStateVO().getId() == null) {
					stategraphvo.getStates().add(stateshape.getStateVO());
				}
				if (stateshape.getStateVO().getClientId() < 0 || layoutinfo.getStateLayout(stateshape.getStateVO().getClientId()) == null) {
					StateLayout layout = new StateLayout(stateshape.getX(), stateshape.getY(), stateshape.getWidth(), stateshape.getHeight());

					Integer iStateId = stateshape.getStateVO().getClientId();
					layoutinfo.insertStateLayout(iStateId, layout);
				}
				else {
					Integer iStateId = stateshape.getStateVO().getClientId();
					layoutinfo.updateState(iStateId, stateshape.getX(), stateshape.getY(), stateshape.getWidth(),
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
			final StateTransition statetransition = (StateTransition) iterTransitionShapes.next();
			final Integer iTempId = statetransition.getStateTransitionVO().getClientId();

			if (iTempId < 0 || statetransition.getStateTransitionVO().getId() == null) {
				final TransitionLayout layout = new TransitionLayout(iTempId,
						statetransition.getSourceConnection() != null ? statetransition.getSourceConnection().getTargetPoint() : -2,
						statetransition.getDestinationConnection() != null ? statetransition.getDestinationConnection().getTargetPoint() : -2);
				layoutinfo.insertTransitionLayout(iTempId, layout);
				stategraphvo.getTransitions().add(statetransition.getStateTransitionVO());
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

		stategraphvo.getStateModel().setLayout(layoutinfo);
		stategraphvo.getStateModel().setName(statemodelvo.getName());
		stategraphvo.getStateModel().setDescription((statemodelvo.getDescription()));
		stategraphvo.validate();
	}

	public void setSelectionTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_SELECTION);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(null);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	public void setStateTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_INSERT_SHAPE);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(StateShape.class);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		try {
			pnlShapeViewer.getModel().setActiveLayer("Default");
		}
		catch (ShapeControllerException e) {
			LOG.warn("setStateTool failed: " + e, e);
		}
	}

	public void setTransitionTool() {
		pnlShapeViewer.getController().setMouseMode(AbstractShapeController.MOUSE_INSERT_CONNECTOR);
		pnlShapeViewer.getController().setDragMode(AbstractShapeController.DRAG_NONE);
		pnlShapeViewer.getController().setSelectedTool(StateTransition.class);
		pnlShapeViewer.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		try {
			pnlShapeViewer.getModel().setActiveLayer("Connectors");
		}
		catch (ShapeControllerException e) {
			LOG.warn("setTransitionTool failed: " + e, e);
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
			LOG.warn("setNoteTool failed: " + e, e);
		}
	}

	public void deleteSelection() {
		pnlShapeViewer.getModel().removeShapes(pnlShapeViewer.getModel().getSelection());
		pnlShapeViewer.repaint();
	}
	
	public void copyStateRights() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getRightTransfer(), null);
	}
	
	public void pasteStateRights() {
		Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard(); 
		Transferable transfer = sysClip.getContents(null); 
		if (pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getRightTransfer() != null) {
			try {
				Object transferO = transfer.getTransferData(RightTransfer.OneRoleRightsDataFlavor.flavor);
				if (transferO instanceof RoleRights)
					pnlProperties.getStatePropertiesPanel().getStateDependantRightsPanel().getRightTransfer().setAllRoleRights((RoleRights) transferO);
				for (ChangeListener cl : lstChangeListeners) {
					cl.stateChanged(new ChangeEvent(this));
				}
			}
			catch(UnsupportedFlavorException e) {
				LOG.warn("pasteStateRights failed: " + e);
			}
			catch(IOException e) {
				LOG.warn("pasteStateRights failed: " + e);
			}
		}
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

	/**
	 * Returns the Shapeviewer used by the editor
	 * @return DefaultShapeViewer
	 */
	public DefaultShapeViewer getPnlShapeViewer() {
		return pnlShapeViewer;
	}
	
	/**
	 * Returns the btnDefaultTransition used by the editor
	 * @return btnDefaultTransition
	 */
	public JCheckBoxMenuItem getBtnDefaultTransition() {
		return btnDefaultTransition;
	}

}	// class StateModelEditor
