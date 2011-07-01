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
package org.nuclos.client.ui.collect;

import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;


import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;

import org.apache.commons.lang.NullArgumentException;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;

/**
 * Controller for the Result panel.
 */
public class ResultController <Clct extends Collectable> {

	/** @todo Try to avoid cyclic dependency: The ResultController shouldn't depend on the CollectController. */
	private final CollectController<Clct> clctctl;

	/**
	 * action: Edit selected Collectable (in Result panel)
	 */
	Action actEditSelectedCollectables;

	/**
	 * action: Delete selected Collectable (in Result panel)
	 */
	final Action actDeleteSelectedCollectables;

	/**
	 * action: Define as new Search result
	 */
	private final Action actDefineAsNewSearchResult = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ResultController.3","Als neues Suchergebnis"),
			Icons.getInstance().getIconEmpty16(), CommonLocaleDelegate.getMessage("ResultController.4","Ausgew\u00e4hlte Datens\u00e4tze als neues Suchergebnis anzeigen")) {
		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdDefineSelectedCollectablesAsNewSearchResult();
		}
	};

	MouseListener mouselistenerTableDblClick;

	public ResultController(CollectController<Clct> clctctl) {
		this.clctctl = clctctl;
		
		actDeleteSelectedCollectables = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ResultController.10","L\u00f6schen..."),
				(clctctl instanceof GenericObjectCollectController)? // quick and dirty... I know
					Icons.getInstance().getIconDelete16():
						Icons.getInstance().getIconRealDelete16(), 
						CommonLocaleDelegate.getMessage("ResultController.5","Ausgew\u00e4hlte Datens\u00e4tze l\u00f6schen")) {
			@Override
	        public void actionPerformed(ActionEvent ev) {
				cmdDeleteSelectedCollectables();
			}
		};
	}

	private ResultPanel<Clct> getResultPanel() {
		return this.clctctl.getResultPanel();
	}
	
	void setupResultPanel() {
		setupActions();

		// add selection listener for Result table:
		final JTable tblResult = this.getResultPanel().getResultTable();

		tblResult.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		tblResult.getSelectionModel().addListSelectionListener(newListSelectionListener(tblResult));

		// add mouse listener for double click in table:
		this.mouselistenerTableDblClick = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev) && ev.getClickCount() == 2) {
					if (clctctl.getSelectedCollectable() != null) {
						clctctl.cmdViewSelectedCollectables();
					}
				}
			}
		};
		this.getResultPanel().addDoubleClickMouseListener(this.mouselistenerTableDblClick);

		// change column ordering in table model when table columns are reordered by dragging a column with the mouse:
		this.getResultPanel().addColumnModelListener(newColumnModelListener());
		this.getResultPanel().addPopupMenuListener();
	}

	private void setupActions() {
		final ResultPanel<Clct> pnlResult = this.getResultPanel();

		// action: Refresh (search again)
		final Action actRefresh = new CommonAbstractAction(pnlResult.btnRefresh) {
			@Override
            public void actionPerformed(ActionEvent ev) {
				clctctl.cmdRefreshResult();
			}
		};
		pnlResult.btnRefresh.setAction(actRefresh);

		// action: New
		pnlResult.btnNew.setAction(this.clctctl.getNewAction());
		
		// action: Bookmark
		pnlResult.btnBookmark.setAction(this.clctctl.getBookmarkAction());

		// action: Clone
		pnlResult.btnClone.setAction(this.clctctl.getCloneAction());

		// action: Delete
		pnlResult.btnDelete.setAction(this.actDeleteSelectedCollectables);
		this.actDeleteSelectedCollectables.setEnabled(false);

		// action: View
		this.actEditSelectedCollectables = new CommonAbstractAction(pnlResult.btnEdit) {
			@Override
            public void actionPerformed(ActionEvent ev) {
				clctctl.cmdViewSelectedCollectables();
			}
		};
		pnlResult.btnEdit.setAction(this.actEditSelectedCollectables);
		actEditSelectedCollectables.setEnabled(false);

		// action: Select Columns
		pnlResult.btnSelectColumns.setAction(new CommonAbstractAction(pnlResult.btnSelectColumns) {
			@Override
            public void actionPerformed(ActionEvent ev) {
				pnlResult.cmdSelectColumns(clctctl.fields, clctctl);
			}
		});

		if (!clctctl.isTransferable()) {
			pnlResult.btnExport.setVisible(false);
			pnlResult.btnImport.setVisible(false);
		}
		else {
			// action: Export
			pnlResult.btnExport.setAction(new CommonAbstractAction(pnlResult.btnExport) {
				@Override
                public void actionPerformed(ActionEvent ev) {
					pnlResult.cmdExport(clctctl);
				}
			});
			
			// action: Import
			pnlResult.btnImport.setAction(new CommonAbstractAction(pnlResult.btnImport) {
				@Override
                public void actionPerformed(ActionEvent ev) {
					pnlResult.cmdImport(clctctl);
				}
			});
		}
		
		pnlResult.miPopupEdit.setAction(actEditSelectedCollectables);
		pnlResult.miPopupClone.setAction(clctctl.getCloneAction());
		pnlResult.miPopupDelete.setAction(this.actDeleteSelectedCollectables);
		pnlResult.miPopupOpenInNewTab.setAction(clctctl.getOpenInNewTabAction());
		pnlResult.miPopupBookmark.setAction(clctctl.getBookmarkAction());
		pnlResult.miPopupDefineAsNewSearchResult.setAction(actDefineAsNewSearchResult);
	}

	private ListSelectionListener newListSelectionListener(final JTable tblResult) {
		return new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent ev) {
				try {
					final ListSelectionModel lsm = (ListSelectionModel) ev.getSource();

					final CollectStateModel<?> clctstatemodel = clctctl.getCollectStateModel();
					if (clctstatemodel.getOuterState() == CollectState.OUTERSTATE_RESULT) {
						final int iResultMode = CollectStateModel.getResultModeFromSelectionModel(lsm);
						if (iResultMode != clctctl.getCollectStateModel().getResultMode()) {
							clctctl.setCollectState(CollectState.OUTERSTATE_RESULT, iResultMode);
						}
					}

					if (!ev.getValueIsAdjusting()) {
						// Autoscroll selection. It's okay to do that here rather than in the CollectStateListener.
						if (!lsm.isSelectionEmpty() && tblResult.getAutoscrolls()) {
							// ensure that the last selected row is visible:
							final int iRowIndex = lsm.getLeadSelectionIndex();
							final int iColIndex = tblResult.getSelectedColumn();
							final Rectangle rectCell = tblResult.getCellRect(iRowIndex, iColIndex != -1 ? iColIndex : 0, true);
							if (rectCell != null) {
								tblResult.scrollRectToVisible(rectCell);
							}
						}
					}
				}
				catch (CommonBusinessException ex) {
					Errors.getInstance().showExceptionDialog(clctctl.getFrame(), ex);
				}
			}	// valueChanged
		};
	}

	/**
	 * change column ordering in table model when table columns
	 * are reordered by dragging a column with the mouse
	 */
	private TableColumnModelListener newColumnModelListener() {
		return new TableColumnModelListener() {
			@Override
            public void columnMoved(TableColumnModelEvent ev) {
				final int iFromColumn = ev.getFromIndex();
				final int iToColumn = ev.getToIndex();
				if (iFromColumn != iToColumn) {
					//log.debug("column moved from " + iFromColumn + " to " + iToColumn);
					// Sync the "selected fields" with the table column model:
					getResultPanel().columnMovedInHeader(clctctl.fields);

					// Note that the columns of the result table model are not adjusted here.
					// That means, the table column model and the table model are not 1:1 -
					// use JTable.convertColumnIndexToModel to convert between the two.
				}
			}

			@Override
            public void columnAdded(TableColumnModelEvent ev) {
			}

			@Override
            public void columnRemoved(TableColumnModelEvent ev) {
			}

			@Override
            public void columnMarginChanged(ChangeEvent ev) {
				clctctl.bUseCustomColumnWidths = true;
			}

			@Override
            public void columnSelectionChanged(ListSelectionEvent ev) {
			}
		};
	}

	/**
	 * releases the resources (esp. listeners) for this controller.
	 */
	void close() {
		final ResultPanel<Clct> pnlResult = getResultPanel();
		pnlResult.btnRefresh.setAction(null);
		pnlResult.btnNew.setAction(null);
		pnlResult.btnBookmark.setAction(null);
		pnlResult.btnClone.setAction(null);
		pnlResult.btnDelete.setAction(null);
		pnlResult.btnEdit.setAction(null);
		pnlResult.btnSelectColumns.setAction(null);
		pnlResult.btnExport.setAction(null);
		pnlResult.btnImport.setAction(null);
		pnlResult.miPopupEdit.setAction(null);
		pnlResult.miPopupClone.setAction(null);
		pnlResult.miPopupDelete.setAction(null);
		pnlResult.miPopupOpenInNewTab.setAction(null);
		pnlResult.miPopupBookmark.setAction(null);
		pnlResult.miPopupDefineAsNewSearchResult.setAction(null);
		UIUtils.removeAllMouseListeners(this.getResultPanel().getResultTable().getTableHeader());

		/** @todo this doesn't really belong here */
		clctctl.writeSelectedFieldsAndWidthsToPreferences();
		clctctl.writeColumnOrderToPreferences();
	}

	/**
	 * @param tblResult
	 * @param bResultTruncated
	 * @param iTotalNumberOfRecords
	 */
	void setStatusBar(JTable tblResult, boolean bResultTruncated, int iTotalNumberOfRecords) {
		String sStatus;
		if (iTotalNumberOfRecords == 0) {
			sStatus = CommonLocaleDelegate.getMessage("ResultController.9","Keinen zur Suchanfrage passenden Datensatz gefunden.");
		}
		else if (iTotalNumberOfRecords == 1) {
			sStatus = CommonLocaleDelegate.getMessage("ResultController.2","1 Datensatz gefunden.");
		}
		else {
			sStatus = CommonLocaleDelegate.getMessage("ResultController.1","{0} Datens\u00e4tze gefunden.", Integer.toString(iTotalNumberOfRecords));
			if (bResultTruncated) {
				sStatus += CommonLocaleDelegate.getMessage("ResultController.6"," Das Ergebnis wurde nach {0} Zeilen abgeschnitten.", tblResult.getRowCount());
			}
		}
		this.getResultPanel().tfStatusBar.setText(sStatus);
	}

	/**
	 * Command: Delete selected <code>Collectable</code>s in the Result panel.
	 */
	private void cmdDeleteSelectedCollectables() {
		assert clctctl.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;
		assert CollectState.isResultModeSelected(clctctl.getCollectStateModel().getResultMode());

		if (clctctl.multipleCollectablesSelected()) {
			final int iCount = clctctl.getResultTable().getSelectedRowCount();
			final String sMessagePattern = CommonLocaleDelegate.getMessage("ResultController.13","Sollen die ausgew\u00e4hlten {0} Datens\u00e4tze wirklich gel\u00f6scht werden?");
			final String sMessage = MessageFormat.format(sMessagePattern, iCount);
			final int btn = JOptionPane.showConfirmDialog(clctctl.getFrame(), sMessage, CommonLocaleDelegate.getMessage("ResultController.7","Datens\u00e4tze l\u00f6schen"), JOptionPane.YES_NO_OPTION);
			if (btn == JOptionPane.YES_OPTION) {
				new DeleteSelectedCollectablesController<Clct>(clctctl).run(clctctl.getMultiActionProgressPanel(iCount));
			}
		}
		else {
			final String sMessagePattern = CommonLocaleDelegate.getMessage("ResultController.12","Soll der ausgew\u00e4hlte Datensatz ({0}) wirklich gel\u00f6scht werden?");
			final String sMessage = MessageFormat.format(sMessagePattern,
					clctctl.getSelectedCollectable().getIdentifierLabel());
			final int btn = JOptionPane.showConfirmDialog(clctctl.getFrame(), sMessage, CommonLocaleDelegate.getMessage("ResultController.8","Datensatz l\u00f6schen"), JOptionPane.YES_NO_OPTION);

			if (btn == JOptionPane.YES_OPTION) {
				UIUtils.runCommand(clctctl.getFrame(), new Runnable() {
					@Override
                    public void run() {
						try {
							clctctl.checkedDeleteSelectedCollectable();
						}
						catch (CommonPermissionException ex) {
							final String sErrorMsg = CommonLocaleDelegate.getMessage("ResultController.11","Sie verf\u00fcgen nicht \u00fcber die ausreichenden Rechte, um diesen Datensatz zu l\u00f6schen.");
							Errors.getInstance().showExceptionDialog(clctctl.getFrame(), sErrorMsg, ex);
						}
						catch (CommonBusinessException ex) {
							Errors.getInstance().showExceptionDialog(clctctl.getFrame(), "Der Datensatz konnte nicht gel\u00f6scht werden.", ex);
						}
					}
				});
			}
		}
	}

	/**
	 * Command: Define selected <code>Collectable</code>s as a new search result.
	 */
	private void cmdDefineSelectedCollectablesAsNewSearchResult() {
		assert clctctl.getCollectStateModel().getOuterState() == CollectState.OUTERSTATE_RESULT;
		assert CollectState.isResultModeSelected(clctctl.getCollectStateModel().getResultMode());

		/** @todo this doesn't work yet - we need to compare the Collectable's id, as in Collectable.getId() */
//		UIUtils.runCommand(this.getFrame(), new Runnable() {
//			public void run() {
//				final Collection collclct = CollectController.this.getSelectedCollectables();
//
//				assert CollectionUtils.isNonEmpty(collclct);
//				final CompositeCollectableSearchCondition cond = new CompositeCollectableSearchCondition(LogicalOperator.OR);
//
//				final String sIdentifier = SystemParameters.getValue(SystemParameters.KEY_SYSTEMATTRIBUTE_IDENTIFIER);
//				final CollectableEntityField clctef = getCollectableEntity().getEntityField(sIdentifier);
//				for (Iterator iter = collclct.iterator(); iter.hasNext();) {
//					final Collectable clct = (Collectable) iter.next();
//					final CollectableField clctfComparand = clct.getField(sIdentifier);
//					cond.addOperand(new CollectableComparison(clctef, ComparisonOperator.EQUAL, clctfComparand));
//				}
//				try {
//					CollectController.this.setCollectableSearchCondition(cond);
//					CollectController.this.search();
//				}
//				catch (CommonBusinessException ex) {
//					Errors.getInstance().showExceptionDialog(getFrame(), ex);
//				}
//			}
//		});
	}

	/**
	 * inner class <code>Fields</code>.
	 * Encapsulates the lists of available and selected fields, resp.
	 * The selected fields are shown as columns in the result table.
	 * The selected fields are always in sync with the table column model, but not necessarily
	 * with the table model's columns.
	 */
	public static class Fields {

		/**
		 * the list of available (currently not selected) fields
		 */
		private List<CollectableEntityField> lstclctefAvailable = new ArrayList<CollectableEntityField>();

		/**
		 * the list of selected fields
		 */
		private List<CollectableEntityField> lstclctefSelected = new ArrayList<CollectableEntityField>();

		/**
		 * sets the available and selected fields, respectively.
		 * @param lstclctefAvailable available (currently not selected) fields
		 * @param lstclctefSelected selected fields
		 * @precondition lstclctefAvailable != null;
		 * @precondition lstclctefSelected != null;
		 */
		public void set(List<CollectableEntityField> lstclctefAvailable, List<CollectableEntityField> lstclctefSelected) {
			if (lstclctefAvailable == null) {
				throw new NullArgumentException("lstclctefAvailable");
			}
			if (lstclctefSelected == null) {
				throw new NullArgumentException("lstclctefSelected");
			}
			this.lstclctefAvailable = lstclctefAvailable;
			this.lstclctefSelected = lstclctefSelected;

		}

		/**
		 * @return the available (currently not selected) fields
		 * @postcondition result != null
		 */
		public List<? extends CollectableEntityField> getAvailableFields() {
			return Collections.unmodifiableList(this.lstclctefAvailable);
		}

		/**
		 * @return List<CollectableEntityField> the selected fields that are shown as columns in the result table
		 * @postcondition result != null
		 */
		public List<? extends CollectableEntityField> getSelectedFields() {
			return Collections.unmodifiableList(this.lstclctefSelected);
		}

		/**
		 * sets the selected fields. The available fields are adjusted accordingly.
		 * @param lstclctefSelected
		 */
		public void setSelectedFields(List<? extends CollectableEntityField> lstclctefSelected) {
			this.lstclctefAvailable.addAll(this.lstclctefSelected);
			this.lstclctefSelected.clear();
			this.moveToSelectedFields(lstclctefSelected);
		}

		/**
		 * moves the given fields from the available to the selected fields.
		 * @param lstclctef
		 */
		void moveToSelectedFields(List<? extends CollectableEntityField> lstclctef) {
			for (CollectableEntityField clctef : lstclctef) {
				this.moveToSelectedFields(clctef);
			}
		}

		/**
		 * moves the given field from the available to the selected fields.
		 * @param clctef
		 */
		private void moveToSelectedFields(CollectableEntityField clctef) {
			this.lstclctefAvailable.remove(clctef);
			this.lstclctefSelected.add(clctef);
		}

		/**
		 * moves the given field from the available to the selected fields, inserting it at the given position.
		 * @param clctef
		 */
		public void moveToSelectedFields(int iColumn, CollectableEntityField clctef) {
			this.lstclctefSelected.add(iColumn, clctef);
			this.lstclctefAvailable.remove(clctef);
		}

		/**
		 * moves the given field from the selected to the available fields.
		 * @param clctef
		 */
		public void moveToAvailableFields(CollectableEntityField clctef) {
			this.lstclctefSelected.remove(clctef);
			this.lstclctefAvailable.add(clctef);
		}

	}	// inner class Fields

}	// class ResultController
