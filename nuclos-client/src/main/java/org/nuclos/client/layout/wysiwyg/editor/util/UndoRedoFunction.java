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
package org.nuclos.client.layout.wysiwyg.editor.util;

import info.clearthought.layout.TableLayoutConstraints;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EmptyStackException;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.client.layout.wysiwyg.WYSIWYGLayoutControllingPanel;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.UNDO_REDO;
import org.nuclos.client.layout.wysiwyg.component.WYSIWYGComponent;
import org.nuclos.client.layout.wysiwyg.component.properties.ComponentProperties;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGLayoutEditorPanel;
import org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.Changes;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.ComponentSettings;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.LayoutCell;
import org.nuclos.client.layout.wysiwyg.editor.util.valueobjects.layoutmlrules.LayoutMLRules;
import org.nuclos.client.ui.Errors;
import org.nuclos.common.NuclosFatalException;

/**
 * This class handles the Undo Redo.
 * 
 * It is connected with the {@link TableLayoutUtil} for logging changes on the
 * Layout and to {@link ComponentProperties} for logging changes to the
 * Component Properties.
 * 
 * It does work on two Stacks, one for undo and one for redo.
 * 
 * If a undo Action is performed the {@link Changes} is put on the redo Stack.
 * 
 * The performed actions are logged into the {@link Changes} Objects. It does
 * contain the Object before the Change and after the Change. If a Action is
 * undone the performed Action is inverted @see UndoRedoFunction#undoChanges()
 * and automaticly performed.
 * 
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * 
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class UndoRedoFunction implements WYSIWYGToolbarAttachable {

	/** the Stack for the Undo Actions */
	private Stack<Changes> undoStack = new Stack<Changes>();
	/** the Stack for the Redo Actions */
	private Stack<Changes> redoStack = new Stack<Changes>();

	/** The Stack for transactioned Undo Actions */
	private Stack<Transaction> undoTransactions = new Stack<Transaction>();
	/** The Stack for transactioned Redo Actions */
	private Stack<Transaction> redoTransactions = new Stack<Transaction>();

	private JButton undo = null;
	private JButton redo = null;

	private int stackcounter = 0;

	public boolean isManualChange = true;
	
	@SuppressWarnings("unused")
	private WYSIWYGEditorsToolbar wysiwygEditorsToolbar;

	private KeyStroke keyStrokeUndo = KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK);
	private KeyStroke keyStrokeRedo = KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK);
	
	private static final Logger log = Logger.getLogger(UndoRedoFunction.class);

	@SuppressWarnings("unused")
	private boolean compositeChange = false;

	/**
	 * 
	 * @param wysiwygEditorsToolbar
	 */
	public UndoRedoFunction(WYSIWYGEditorsToolbar wysiwygEditorsToolbar) {
		this.wysiwygEditorsToolbar = wysiwygEditorsToolbar;
		wysiwygEditorsToolbar.addComponentToToolbar(this);
		enableDisableUndoRedoIfNeeded();
	}

	/**
	 * Clears the Undo and Redo Stack
	 * Is called from {@link WYSIWYGLayoutControllingPanel}
	 * 
	 */
	public void clearUndoRedoStack() {
		stackcounter = 0;
		undoStack = new Stack<Changes>();
		redoStack = new Stack<Changes>();
		enableDisableUndoRedoIfNeeded();
	}

	/**
	 * 
	 * @return the Number ob Undo Steps on the Stack
	 */
	public int getUndoSteps() {
		return undoStack.size();
	}

	/**
	 * 
	 * @return the Number of Redo Steps on the Stack
	 */
	public int getRedoSteps() {
		return redoStack.size();
	}

	/**
	 * This method accesses the current {@link Changes}.
	 * 
	 * If the Changes Object is completly filled, a new one is created and returned.
	 * 
	 * @return
	 */
	private Changes getChangeSet() {
		try {
			Changes recentChange = undoStack.peek();
			if (!recentChange.getFinalSettings().isInitialized())
				return undoStack.pop();
		} catch (EmptyStackException ex) {
		}

		Changes newChangeSet = new Changes(stackcounter++);
		if (!undoTransactions.isEmpty() && undoTransactions.peek().start == null) {
			undoTransactions.peek().setStart(newChangeSet);
		}
		return newChangeSet;
	}

	/**
	 * 
	 * @param changesMade
	 * @param cell
	 * @param automatic
	 * @return
	 */
	private Changes getChangeSetForModifiyingSizes(int changesMade, LayoutCell cell, boolean automatic) {
		try {
			Changes recentChange = undoStack.pop();

			if (!recentChange.getFinalSettings().isInitialized()) {
				return recentChange;
			}

			if (recentChange.getInitialSettings().getLayoutCell().getCellX() == cell.getCellX() && recentChange.getInitialSettings().getLayoutCell().getCellY() == cell.getCellY()) {
				if (recentChange.isAnotherAction() || automatic) {
					undoStack.push(recentChange);
				} else {
					recentChange.setTimeStamp();
					recentChange.resetFinalSettings();
					return recentChange;
				}
			}
		} catch (EmptyStackException ex) {
		}

		Changes newChangeSet = new Changes(stackcounter++);
		if (!undoTransactions.isEmpty() && undoTransactions.peek().start == null) {
			undoTransactions.peek().setStart(newChangeSet);
		}
		return newChangeSet;
	}

	/**
	 * Logs changes made to the Width of a column
	 * @see TableLayoutUtil#modifyTableLayoutSizes(double, boolean, LayoutCell, boolean)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 * @param automatic
	 */
	public synchronized void loggingChangeWidthOfColumn(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil, boolean automatic) {
		if (isManualChange) {
			Changes changes = getChangeSetForModifiyingSizes(Changes.MODIFY_COL, cellEdited, automatic);
			changes.setChanges(cellEdited, Changes.MODIFY_COL, tableLayoutUtil);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**	 
	 * Logs changes made to the Height of a row
	 * @see TableLayoutUtil#modifyTableLayoutSizes(double, boolean, LayoutCell, boolean)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 * @param automatic
	 */
	public synchronized void loggingChangeHeightOfRow(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil, boolean automatic) {
		if (isManualChange) {
			Changes changes = getChangeSetForModifiyingSizes(Changes.MODIFY_ROW, cellEdited, automatic);
			changes.setChanges(cellEdited, Changes.MODIFY_ROW, tableLayoutUtil);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This method logs deleting a Row
	 * @see TableLayoutUtil#delRow(java.awt.Point)
	 * @see TableLayoutUtil#delRow(LayoutCell)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingDeleteRow(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(cellEdited, Changes.DELETEROW, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}
	
	/**
	 * This method logs adding a Row
	 * @see TableLayoutUtil#addRow(LayoutCell)
	 * @see TableLayoutUtil#addRow(java.awt.Point)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingAddRow(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(cellEdited, Changes.ADDROW, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}
	
	/**
	 * This method logs deleting a Column
	 * @see TableLayoutUtil#delCol(LayoutCell)
	 * @see TableLayoutUtil#delCol(java.awt.Point)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingDeleteColumn(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(cellEdited, Changes.DELETECOL, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}
	
	/**
	 * This method logs adding a Column
	 * @see TableLayoutUtil#addCol(LayoutCell)
	 * @see TableLayoutUtil#addCol(java.awt.Point)
	 * 
	 * @param cellEdited
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingAddColumn(LayoutCell cellEdited, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(cellEdited, Changes.ADDCOL, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This Method logs {@link WYSIWYGComponent} movement
	 * 
	 * @see TableLayoutUtil#moveComponentTo(WYSIWYGComponent, TableLayoutConstraints, TableLayoutUtil)
	 * 
	 * @param constraint
	 * @param c
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingMoveComponentToCell(TableLayoutConstraints constraint, WYSIWYGComponent c, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(constraint, c, Changes.MOVE_TO_CELL, tableLayoutUtil);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This Method logs the deleting a {@link WYSIWYGComponent} from a LayoutCell
	 * @see TableLayoutUtil#removeComponentFromLayout(WYSIWYGComponent)
	 * 
	 * @param constraint
	 * @param tableLayoutUtil
	 * @param c
	 */
	public synchronized void loggingDeleteComponentFromCell(TableLayoutConstraints constraint, TableLayoutUtil tableLayoutUtil, WYSIWYGComponent c) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(constraint, c, Changes.DELETE_FROM_CELL, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}
	
	/**
	 * This method logs insert of {@link WYSIWYGComponent} to a LayoutCell
	 * 
	 * @see TableLayoutUtil#insertComponentTo(WYSIWYGComponent, LayoutCell)
	 * 
	 * @param target
	 * @param c
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingInsertComponentToCell(LayoutCell target, WYSIWYGComponent c, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(target, c, Changes.INSERT_TO_CELL, tableLayoutUtil);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This method logs insert of {@link WYSIWYGComponent} to a LayoutCell
	 * 
	 * @see TableLayoutUtil#insertComponentTo(WYSIWYGComponent, TableLayoutConstraints)
	 * 
	 * @param constraint
	 * @param c
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingInsertComponentToCell(TableLayoutConstraints constraint, WYSIWYGComponent c, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(constraint, c, Changes.INSERT_TO_CELL, tableLayoutUtil);
			changes.copySettings();

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This Method logs the Change of the Alignment
	 * @see TableLayoutUtil#moveComponentTo(WYSIWYGComponent, LayoutCell)
	 * @see TableLayoutUtil#moveComponentTo(WYSIWYGComponent, TableLayoutConstraints)
	 * @see TableLayoutUtil#moveComponentTo(WYSIWYGComponent, TableLayoutConstraints, TableLayoutUtil)
	 *
	 * @param constraint
	 * @param c
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingChangeComponentsAlignment(TableLayoutConstraints constraint, WYSIWYGComponent c, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(constraint, c, Changes.CHANGE_ALIGNMENT, tableLayoutUtil);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This method logs changes for {@link ComponentProperties}
	 * 
	 * @param c
	 * @param componentProperties
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingChangeComponentsProperties(WYSIWYGComponent c, ComponentProperties componentProperties, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(c, componentProperties, tableLayoutUtil, Changes.CHANGE_PROPERTIES);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}

	/**
	 * This method logs Changes of {@link LayoutMLRules}
	 * 
	 * @param c
	 * @param layoutMLRules
	 * @param tableLayoutUtil
	 */
	public synchronized void loggingChangeComponentsLayoutMLRules(WYSIWYGComponent c, LayoutMLRules layoutMLRules, TableLayoutUtil tableLayoutUtil) {
		if (isManualChange) {
			Changes changes = getChangeSet();
			changes.setChanges(c, layoutMLRules, tableLayoutUtil, Changes.CHANGE_LAYOUTMLRULES);

			undoStack.push(changes);
			clearRedoIfAnotherActionWasPerformedWhenRedoActive(tableLayoutUtil);
		}
	}
	
	/**
	 * This method starts a Undo Action.
	 * The Stacks are changed and {@link UndoRedoFunction#undoChanges(Stack, Stack, boolean)} is called.
	 * The Changing of the Stacks makes the {@link #undoChanges(Stack, Stack, boolean)} Method reusable.
	 */
	public void undoChanges() {
		undoChanges(undoStack, redoStack, false);
	}

	/**
	 * This method starts a Redo Action.
	 * The Stacks are changed and {@link UndoRedoFunction#undoChanges(Stack, Stack, boolean)} is called.
	 * The Changing of the Stacks makes the {@link #undoChanges(Stack, Stack, boolean)} Method reusable.
	 */
	public void redoChanges() {
		undoChanges(redoStack, undoStack, true);
	}

	/**
	 * This is the core Method for controlling Undo Redo.
	 * It takes the fitting {@link Changes} Object and performs the Action found in the Changes Object using {@link TableLayoutUtil}
	 * 
	 * @param undo
	 * @param redo
	 * @param isRedoAction
	 */
	public synchronized void undoChanges(Stack<Changes> undo, Stack<Changes> redo, boolean isRedoAction) {
		if (!isRedoAction && !undoTransactions.isEmpty() && undoTransactions.peek().finish == undoStack.peek()) {
			undoTransactions.pop().undo();
			return;
		} else if (isRedoAction && !redoTransactions.isEmpty() && redoTransactions.peek().start == redoStack.peek()) {
			redoTransactions.pop().redo();
			return;
		}

		if (!undo.isEmpty()) {
			isManualChange = false;
			Changes change = undo.pop();

			ComponentSettings settings = change.getInitialSettings();

			if (isRedoAction) {
				settings = change.getFinalSettings();
			}

			LayoutCell changesMade = settings.getLayoutCell();
			TableLayoutUtil tableLayoutUtil = settings.getTableLayoutUtil();

			switch (change.getChangesMade()) {
				case Changes.DELETEROW :
					tableLayoutUtil.addRow(changesMade);
					change.setChangesMade(Changes.ADDROW);
					break;

				case Changes.DELETECOL :
					tableLayoutUtil.addCol(changesMade);
					change.setChangesMade(Changes.ADDCOL);
					break;

				case Changes.ADDROW :
					tableLayoutUtil.delRow(changesMade);
					change.setChangesMade(Changes.DELETEROW);
					break;

				case Changes.ADDCOL :
					tableLayoutUtil.delCol(changesMade);
					change.setChangesMade(Changes.DELETECOL);
					break;

				case Changes.MODIFY_ROW :
					tableLayoutUtil.modifyTableLayoutSizes(changesMade.getCellHeight(), false, changesMade, true);
					break;

				case Changes.MODIFY_COL :
					tableLayoutUtil.modifyTableLayoutSizes(changesMade.getCellWidth(), true, changesMade, true);
					break;

				case Changes.MOVE_TO_CELL :
					tableLayoutUtil.moveComponentTo(settings.getWYSIWYGComponent(), settings.getTableLayoutConstraint(), settings.getTableLayoutUtil());
					change.setChangesMade(Changes.MOVE_TO_CELL);
					break;

				case Changes.INSERT_TO_CELL :
					tableLayoutUtil.removeComponentFromLayout(settings.getWYSIWYGComponent());
					change.setChangesMade(Changes.DELETE_FROM_CELL);
					break;

				case Changes.DELETE_FROM_CELL :
					tableLayoutUtil.insertComponentTo(settings.getWYSIWYGComponent(), settings.getTableLayoutConstraint());
					change.setChangesMade(Changes.INSERT_TO_CELL);
					break;

				case Changes.CHANGE_ALIGNMENT :
					tableLayoutUtil.changeComponentsAlignment(settings.getWYSIWYGComponent(), settings.getTableLayoutConstraint());
					break;

				case Changes.CHANGE_PROPERTIES :
					applyProperties(settings.getComponentProperties());
					break;
				case Changes.CHANGE_LAYOUTMLRULES :
					LayoutMLRuleController.attachRulesToComponent(settings.getWYSIWYGComponent(), settings.getLayoutMLRules());
					break;
			}
			redo.push(change);

			deepRefresh(settings.getWYSIWYGComponent());
			tableLayoutUtil.getContainer().updateUI();
			enableDisableUndoRedoIfNeeded();

			isManualChange = true;
		}
	}
	
	/**
	 * This method resets the Redo Stack if a Undo Action was performed and a new Change happend.
	 * This is needed because every change might depend on a concrete Layout. If some Changes are undone this actions are put on the Redo Stack.
	 * If there is another Action performed the sequence of Changes is interrupted and strange Things may happen.
	 * 
	 * @param tableLayoutUtil
	 */
	private void clearRedoIfAnotherActionWasPerformedWhenRedoActive(TableLayoutUtil tableLayoutUtil) {
		if (redoStack.size() > 0) {
			redoStack = new Stack<Changes>();
		}

		enableDisableUndoRedoIfNeeded();
	}

	/**
	 * This Method is used for a "deepRefresh".
	 * 
	 * It does prevent "drawn artifacts" when something changed and the container did not notice the change.
	 * This is done here manually. Example is moving component from one to another panel.
	 * 
	 * @param c WYSIWYGComponent
	 */
	private void deepRefresh(WYSIWYGComponent c) {
		if (c != null) {
			if (c instanceof WYSIWYGLayoutEditorPanel)
				((WYSIWYGLayoutEditorPanel) c).getTableLayoutUtil().getContainer().updateUI();
			try {
				if (c.getParentEditor() != null)
					deepRefresh(c.getParentEditor());
			} catch (Exception e) {
				/** nothing to do */
			}
		}
	}

	/**
	 * This method is used for restoring {@link ComponentProperties}
	 * @param properties
	 */
	private synchronized void applyProperties(ComponentProperties properties) {
		properties.getComponent().setProperties(properties);
		try {
			properties.getComponent().getProperties().setProperties(properties.getProperties());
		} catch (CommonBusinessException e) {
			log.error(e);
			Errors.getInstance().showExceptionDialog(null, e);
		}
	}
		
	/**
	 * This Class makes Undo Redo work transactional.
	 * 
	 * This is needed for collecting multiple Changes that can be undone or redone in one Step.
	 * 
	 * 
	 * 
	 * <br>
	 * Created by Novabit Informationssysteme GmbH <br>
	 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
	 * 
	 * @author <a href="mailto:thomas.schiffmann@novabit.de">thomas.schiffmann</a>
	 * @version 01.00.00
	 */
	private class Transaction {

		private Changes start;
		private Changes finish;

		public void setStart(Changes start) {
			this.start = start;
		}

		public void setFinish(Changes finish) {
			this.finish = finish;
		}

		public synchronized void undo() {
			if (undoStack.peek() == finish) {
				while (undoStack.peek() != start) {
					undoChanges();
				}
				undoChanges();
			} else {
				throw new NuclosFatalException("Transaction.undo() is not allowed in current state of UndoRedoFunction.");
			}
			redoTransactions.push(this);
		}

		public synchronized void redo() {
			if (redoStack.peek() == start) {
				while (redoStack.peek() != finish) {
					redoChanges();
				}
				redoChanges();
			} else {
				throw new NuclosFatalException("Transaction.redo() is not allowed in current state of UndoRedoFunction.");
			}
			undoTransactions.push(this);
		}
	}
	
	/**
	 * This Method marks the Start of a {@link Transaction}
	 */
	public synchronized void beginTransaction() {
		undoTransactions.push(new Transaction());
	}

	/**
	 * This Method marks the End of a {@link Transaction}
	 */
	public synchronized void commitTransaction() {
		if (undoTransactions.peek().start == null) {
			undoTransactions.pop();
		} else {
			undoTransactions.peek().setFinish(undoStack.peek());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.nuclos.client.layout.wysiwyg.editor.ui.panels.WYSIWYGEditorsToolbar.WYSIWYGToolbarAttachable#getToolbarItems()
	 */
	@Override
	public JComponent[] getToolbarItems() {
		String path = "org/nuclos/client/layout/wysiwyg/editor/ui/panels/icons/";

		Icon iconUndo = new ImageIcon(this.getClass().getClassLoader().getResource(path + "edit-undo.png"));
		undo = new JButton(iconUndo);
		undo.setToolTipText(UNDO_REDO.LABEL_TOOLTIP_UNDO);
		InputMap inputMap = undo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(keyStrokeUndo, "undo");
		ActionMap actionMap = undo.getActionMap();
		Action undoAction = new UndoAction();
		actionMap.put("undo", undoAction);
		undo.addActionListener(undoAction);

		Icon iconRedo = new ImageIcon(this.getClass().getClassLoader().getResource(path + "edit-redo.png"));
		redo = new JButton(iconRedo);
		redo.setToolTipText(UNDO_REDO.LABEL_TOOLTIP_REDO);
		inputMap = redo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(keyStrokeRedo, "redo");
		actionMap = redo.getActionMap();
		Action redoAction = new RedoAction();
		actionMap.put("redo", redoAction);
		redo.addActionListener(redoAction);

		JButton[] toolbarItems = {undo, redo};

		return toolbarItems;
	}

	/**
	 * Implementing the Methods for {@link WYSIWYGToolbarAttachable}
	 */
	@SuppressWarnings("serial")
	private class UndoAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			undoChanges();
		}
	}

	/**
	 * Implementing the Methods for {@link WYSIWYGToolbarAttachable}
	 */
	@SuppressWarnings("serial")
	private class RedoAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent e) {
			redoChanges();
		}
	}
	
	/**
	 * This Method enables and disables the Toolbarbuttons if there is no Undo or Redo is possible
	 */
	private void enableDisableUndoRedoIfNeeded() {
		if (getUndoSteps() == 0) {
			this.undo.setEnabled(false);
		} else {
			this.undo.setEnabled(true);
		}

		if (getRedoSteps() == 0) {
			this.redo.setEnabled(false);
		} else {
			this.redo.setEnabled(true);
		}
	}

	@Override
	public String toString() {
		StringBuffer fubber = new StringBuffer();

		fubber.append("Undo Steps: " + undoStack.size() + "\n");
		fubber.append("Redo Steps: " + redoStack.size() + "\n");

		fubber.append("Undostack Content: \n");
		fubber.append(undoStack.toString() + "\n");

		fubber.append("Redostack Content: \n");
		fubber.append(redoStack.toString() + "\n");

		return fubber.toString();
	}
	
}
