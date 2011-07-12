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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.common.Utils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.ui.CommonAbstractAction;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.ResultPanel.TableHeaderColumnPopupListener;
import org.nuclos.client.ui.collect.component.model.ChoiceEntityFieldList;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableSorting;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.PreferencesUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.PreferencesException;

/**
 * Controller for the Result panel.
 * <p>
 * This implementation is to use with {@link CollectController}s for which
 * no subtype is defined.
 * </p><p>
 * The ResultController is used for displaying a search result on
 * ResultPanel. The result shown are (list) representations of 
 * (DB) persisted entities.
 * </p><p>
 * Since Nuclos 3.1.01 ResultController is a Controller hierarchy of its own.
 * The main intention of this is to remove as many methods away from
 * CollectController as possible. In principle, the ResultController hierarchy 
 * should mimic the hierarchy of CollectControllers. In practice, subtypes of 
 * ResultController are only implemented in case they change the behaviour 
 * defined here.
 * </p>
 * @see ResultPanel
 * @since Nuclos 3.1.01 this is a top-level class.
 * @author Thomas Pasch
 */
public class ResultController<Clct extends Collectable> {
	
	private static final Logger LOG = Logger.getLogger(ResultController.class);

	/**
	 * The entity for which the results are displayed.
	 * 
	 * @since Nuclos 3.1.01
	 * @author Thomas Pasch
	 */
	private final CollectableEntity clcte;
	
	/** 
	 * TODO: Try to avoid cyclic dependency: The ResultController shouldn't depend on the CollectController. 
	 * 		While this would be desirable, it is - in real - completely unrealistic at present. Even
	 * 		more, a Result is always the result (pun intended!) of some other (controller) operation!
	 * 		(Thomas Pasch)
	 */
	private CollectController<Clct> clctctl;

	/**
	 * the lists of available and selected fields, resp.
	 */
	private final ChoiceEntityFieldList fields = new ChoiceEntityFieldList(null);

	/**
	 * Use custom column widths? This will always be true as soon as the user changed one or more column width
	 * the first time.
	 * TODO move to ResultController or ResultPanel
	 */
	private boolean bUseCustomColumnWidths;

	/**
	 * action: Edit selected Collectable (in Result panel)
	 * 
	 * TODO: make private
	 */
	Action actEditSelectedCollectables;

	/**
	 * action: Delete selected Collectable (in Result panel)
	 * 
	 * TODO: make private
	 */
	final Action actDeleteSelectedCollectables;

	/**
	 * TODO: make private
	 */
	MouseListener mouselistenerTableDblClick;

	/**
	 * action: Define as new Search result
	 */
	private final Action actDefineAsNewSearchResult = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ResultController.3","Als neues Suchergebnis"),
			Icons.getInstance().getIconEmpty16(), CommonLocaleDelegate.getMessage("ResultController.4","Ausgew\u00e4hlte Datens\u00e4tze als neues Suchergebnis anzeigen")) {

		private static final long serialVersionUID = 1L;

		@Override
        public void actionPerformed(ActionEvent ev) {
			cmdDefineSelectedCollectablesAsNewSearchResult();
		}
	};

	/**
	 * Don't make this public!
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	ResultController(CollectController<Clct> clctctl, CollectableEntity clcte) {
		this(clcte);
		setCollectController(clctctl);
	}
	
	public ResultController(CollectableEntity clcte) {
		actDeleteSelectedCollectables = new CommonAbstractAction(CommonLocaleDelegate.getMessage("ResultController.10","L\u00f6schen..."),
				(clctctl instanceof GenericObjectCollectController)? // quick and dirty... I know
				Icons.getInstance().getIconDelete16() : Icons.getInstance().getIconRealDelete16(), 
				CommonLocaleDelegate.getMessage("ResultController.5","Ausgew\u00e4hlte Datens\u00e4tze l\u00f6schen")) {
			
			private static final long serialVersionUID = 1L;

			@Override
	        public void actionPerformed(ActionEvent ev) {
				cmdDeleteSelectedCollectables();
			}
		};
		this.clcte = clcte;
	}
	
	/**
	 * @deprecated You should really provide a CollectableEntity here.
	 */
	public ResultController(String entityName) {
		this(NuclosCollectableEntityProvider.getInstance().getCollectableEntity(entityName));
	}
	
	/**
	 * Don't make this public!
	 */
	void setCollectController(CollectController<Clct> controller) {
		this.clctctl = controller;
	}
	
	protected CollectController<Clct> getCollectController() {
		return clctctl;
	}
	
	protected CollectableEntity getEntity() {
		return clcte;
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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

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
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent ev) {
				clctctl.cmdViewSelectedCollectables();
			}
		};
		pnlResult.btnEdit.setAction(this.actEditSelectedCollectables);
		actEditSelectedCollectables.setEnabled(false);

		// action: Select Columns
		pnlResult.btnSelectColumns.setAction(new CommonAbstractAction(pnlResult.btnSelectColumns) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public void actionPerformed(ActionEvent ev) {
				cmdSelectColumns(clctctl.getFields(), clctctl);
			}
		});

		if (!clctctl.isTransferable()) {
			pnlResult.btnExport.setVisible(false);
			pnlResult.btnImport.setVisible(false);
		}
		else {
			// action: Export
			pnlResult.btnExport.setAction(new CommonAbstractAction(pnlResult.btnExport) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
                public void actionPerformed(ActionEvent ev) {
					pnlResult.cmdExport(clctctl);
				}
			});
			
			// action: Import
			pnlResult.btnImport.setAction(new CommonAbstractAction(pnlResult.btnImport) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

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

	/**
	 * Initializes the <code>fields</code> field as follows: 
	 * <ol>
	 *   <li>(re-)set the list of avaiable fields to {@link #getFieldsAvailableForResult}</li>
	 *   <li>(re-)set the list of selected fields to {@link #getSelectedFieldsFromPreferences}</li>
	 *   <li>(re)-set the comparator to {@link #getCollectableEntityFieldComparator()}</li>
	 * </ul>
	 * @param clcte
	 * @param preferences
	 */
	protected void initializeFields(CollectableEntity clcte, CollectController<Clct> clctctl, Preferences preferences) {
		assert clctctl == this.clctctl && clctctl.getFields() == fields;
		assert this.clcte.equals(clcte);
		final Comparator<CollectableEntityField> comp = getCollectableEntityFieldComparator();
		fields.set(
				getFieldsAvailableForResult(clcte, comp), 
				new ArrayList<CollectableEntityField>(), 
				comp);

		// select the previously selected fields according to user preferences:
		fields.moveToSelectedFields(getSelectedFieldsFromPreferences(clcte, clctctl));
	}
	
	/**
	 * initializes the <code>fields</code> field, while setting selected columns.
	 * @param clcte
	 * @param clctctl
	 * @param lstSelectedNew
	 * @param lstFixedNew
	 * @param lstColumnWiths
	 */
	public final void initializeFields(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl, final List<CollectableEntityField> lstSelectedNew) 
	{		
		assert clctctl == this.clctctl && clctctl.getFields() == fields;
		fields.setSelectedFields(lstSelectedNew);
	}

	/**
	 * tries to read the selected fields from the preferences, making sure they contain at least one field.
	 * @param clcte
	 * @return List<CollectableEntityField> the selected fields from the user preferences.
	 * @postcondition !result.isEmpty()
	 */
	@SuppressWarnings("unchecked")
	private List<CollectableEntityField> getSelectedFieldsFromPreferences(CollectableEntity clcte, CollectController<Clct> clctctl) {
		assert clctctl == this.clctctl && clctctl.getFields() == fields;
		assert this.clcte.equals(clcte);
		final List<CollectableEntityField> result = (List<CollectableEntityField>) readSelectedFieldsFromPreferences(clcte);

		clctctl.makeSureSelectedFieldsAreNonEmpty(clcte, result);

		// Here we have at least one field as selected column:
		assert !result.isEmpty();
		return result;
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
					getResultPanel().columnMovedInHeader(clctctl.getFields());

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
				bUseCustomColumnWidths = true;
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
		writeSelectedFieldsAndWidthsToPreferences();
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
	 * @return the fields displayed in the result panel
	 */
	public ChoiceEntityFieldList getFields() {
		return fields;
	}

	/**
	 * reads the previously selected fields from the user preferences, ignoring unknown fields that might occur when
	 * the database schema has changed from one software release to another. This method tries to avoid throwing exceptions.
	 * @param clcte
	 * @return List<CollectableEntityField> the previously selected fields from the user preferences.
	 * @see #writeSelectedFieldsToPreferences(List)
	 * TODO: make private?
	 */
	protected List<? extends CollectableEntityField> readSelectedFieldsFromPreferences(CollectableEntity clcte) {
		assert this.clcte.equals(clcte);
		List<String> lstSelectedFieldNames;
		try {
			lstSelectedFieldNames = PreferencesUtils.getStringList(clctctl.getPreferences(), CollectController.PREFS_NODE_SELECTEDFIELDS);
		}
		catch (PreferencesException ex) {
			LOG.error("Die selektierten Felder konnten nicht aus den Preferences geladen werden.", ex);
			lstSelectedFieldNames = new ArrayList<String>();
			// no exception is thrown here.
		}
		final List<CollectableEntityField> result = Utils.createCollectableEntityFieldListFromFieldNames(this, clcte, lstSelectedFieldNames);
		return result;
	}

	/**
	 * writes the given list of selected fields to the preferences, so they can be restored later by calling <code>readSelectedFieldsFromPreferences</code>.
	 * @param lstclctefSelected List<CollectableEntityField>
	 * @throws PreferencesException
	 * @see #readSelectedFieldsFromPreferences(CollectableEntity)
	 * TODO make this private
	 */
	public void writeSelectedFieldsToPreferences(List<? extends CollectableEntityField> lstclctefSelected) throws PreferencesException {
		PreferencesUtils.putStringList(clctctl.getPreferences(), CollectController.PREFS_NODE_SELECTEDFIELDS, CollectableUtils.getFieldNamesFromCollectableEntityFields(lstclctefSelected));
	}

	/**
	 * writes the selected columns (fields) and their widths to the user preferences.
	 * TODO make private again or refactor!
	 */
	public final void writeSelectedFieldsAndWidthsToPreferences() {
		try {
			writeSelectedFieldsToPreferences(getFields().getSelectedFields());
			writeFieldWidthsToPreferences(clctctl.getPreferences());
		}
		catch (PreferencesException ex) {
			LOG.error("Failed to write selected field names and widths (search result columns) to preferences.", ex);
			// No exception is thrown here.
		}
	}

	/**
	 * @param clcte
	 * @return List<CollectableEntityField> the fields that are available for the result. This default implementation
	 * returns all fields of the given entity that are to be display in the table.
	 * Successors may want to do weird things like appending fields from subentities here...
	 * TODO Make this private.
	 */
	public SortedSet<CollectableEntityField> getFieldsAvailableForResult(CollectableEntity clcte, Comparator<CollectableEntityField> comp) {
		assert this.clcte.equals(clcte);
		final SortedSet<CollectableEntityField> result = new TreeSet<CollectableEntityField>(comp);
		for (String sFieldName : clcte.getFieldNames()) {
			if (this.isFieldToBeDisplayedInTable(sFieldName)) {
				result.add(getCollectableEntityFieldForResult(clcte, sFieldName));
			}
		}
		return result;
	}

	/**
	 * can be used to hide columns in the table.
	 * @param sFieldName
	 * @return Is the field with the given name to be displayed in a table?
	 * TODO move to ResultPanel.isFieldToBeShown
	 */
	protected boolean isFieldToBeDisplayedInTable(String sFieldName) {
		return true;
	}

	/**
	 * @param clcte
	 * @param sFieldName
	 * @return a <code>CollectableEntityField</code> of the given entity with the given field name, to be used in the Result metadata.
	 * Some successors may want to do weird things here...
	 * TODO: Make this private.
	 */
	public CollectableEntityField getCollectableEntityFieldForResult(CollectableEntity clcte, String sFieldName) {
		assert this.clcte.equals(clcte);
		return clcte.getEntityField(sFieldName);
	}

	/**
	 * @return the <code>Comparator</code> used for <code>CollectableEntityField</code>s (columns in the Result).
	 * The default is to compare the column labels.
	 * @postcondition result != null
	 * TODO move to ResultController or ResultPanel
	 */
	public Comparator<CollectableEntityField> getCollectableEntityFieldComparator() {
		return new CollectableEntityField.LabelComparator();
	}

	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 * Copied from the SubFormController
	 * @param tbl
	 * TODO move to ResultController or ResultPanel
	 */
	public void setColumnWidths(final JTable tbl) {
		this.getResultPanel().setColumnWidths(tbl, this.bUseCustomColumnWidths, clctctl.getPreferences());
	}

	/**
	 * @return the list of sorting columns.
	 * TODO make this private
	 */
	public List<CollectableSorting> getCollectableSortingSequence() {
		final List<CollectableSorting> result = new LinkedList<CollectableSorting>();
		for (SortKey sortKey : clctctl.getResultTableModel().getSortKeys()) {
			final String fieldName = clctctl.getResultTableModel().getCollectableEntityField(sortKey.getColumn()).getName();
			result.add(new CollectableSorting(fieldName, sortKey.getSortOrder() == SortOrder.ASCENDING));
		}
		return result;
	}

	/**
	 * @return the selected <code>Collectable</code>, if any, from the table model.
	 * Note that there is no selected <code>Collectable</code> in New mode.
	 * Note that the result might be incomplete, that means, some fields might be missing.
	 * TODO make this private
	 */
	public Clct getSelectedCollectableFromTableModel() {
		final int iSelectedRow = clctctl.getResultTable().getSelectedRow();
		return (iSelectedRow == -1) ? null : clctctl.getResultTableModel().getCollectable(iSelectedRow);
	}

	/**
	 * replaces the selected <code>Collectable</code> in the table model with <code>clct</code>.
	 * @param clct
	 * @precondition clct != null
	 * TODO make this private
	 */
	public final void replaceSelectedCollectableInTableModel(Clct clct) {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		this.replaceCollectableInTableModel(clctctl.getResultTable().getSelectedRow(), clct);
	}

	/**
	 * replaces the <code>Collectable</code> in the table model that has the same id as <code>clct</code>
	 * with <code>clct</code>.
	 * @param clct
	 * @precondition clct != null
	 * TODO make this private
	 */
	public final void replaceCollectableInTableModel(Clct clct) {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		final int iRow = clctctl.getResultTableModel().findRowById(clct.getId());
		if (iRow == -1) {
			throw new CommonFatalException("Der Datensatz mit der Id " + clct.getId() + " ist nicht im Suchergebnis vorhanden.");
		}
		this.replaceCollectableInTableModel(iRow, clct);
	}

	/**
	 * replaces the <code>Collectable</code> in the given row of the table model with <code>clct</code>.
	 * @param iRow
	 * @param clct
	 * @precondition clct != null
	 * TODO move to ResultController
	 */
	private void replaceCollectableInTableModel(int iRow, Clct clct) {
		if (clct == null) {
			throw new NullArgumentException("clct");
		}
		clctctl.getResultTableModel().setCollectable(iRow, clct);
	}

	/**
	 * replaces the <code>Collectable</code>s the table model that have the same ids
	 * as the given <code>Collectable</code>s, with those <code>Collectable</code>s.
	 * @param collclct
	 * @precondition collclct != null
	 * TODO move to ResultController
	 */
	protected final void replaceCollectablesInTableModel(Collection<Clct> collclct) {
		if (collclct == null) {
			throw new NullArgumentException("collclct");
		}
		for (Clct clct : collclct) {
			this.replaceCollectableInTableModel(clct);
		}
	}

	/**
	 * command: select columns
	 * Lets the user select the columns to show in the result list.
	 */
	@SuppressWarnings("unchecked")
	public void cmdSelectColumns(final ChoiceEntityFieldList fields, final CollectController<Clct> clctctl) {
		assert clctctl == this.clctctl && clctctl.getFields() == fields;

		final SelectColumnsController ctl = new SelectColumnsController(clctctl.getFrame());
		// final List<CollectableEntityField> lstAvailable = (List<CollectableEntityField>) fields.getAvailableFields();
		// final List<CollectableEntityField> lstSelected = (List<CollectableEntityField>) fields.getSelectedFields();
		final ResultPanel<Clct> panel = getResultPanel();
		final JTable tbl = panel.getResultTable();

		final Map<String, Integer> mpWidths = panel.getVisibleColumnWidth(fields.getSelectedFields());
		ctl.setModel(fields);
		final boolean bOK = ctl.run(CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));

		if (bOK) {
			UIUtils.runCommand(clctctl.getFrame(), new CommonRunnable() {
				@Override
                public void run() throws CommonBusinessException {
					final int iSelectedRow = tbl.getSelectedRow();
					fields.set(ctl.getAvailableObjects(), ctl.getSelectedObjects(), clctctl.getResultController().getCollectableEntityFieldComparator());
					final List<? extends CollectableEntityField> lstSelectedNew = fields.getSelectedFields();
					((CollectableTableModel<?>) tbl.getModel()).setColumns(lstSelectedNew);
					panel.setupTableCellRenderers(tbl);
					Collection<CollectableEntityField> collNewlySelected = new ArrayList<CollectableEntityField>(lstSelectedNew);
					collNewlySelected.removeAll(fields.getSelectedFields());
					if (!collNewlySelected.isEmpty()) {
						if (!clctctl.getCollectablesInResultAreAlwaysComplete()) {
							// refresh the result:
							clctctl.refreshResult();
						}
					}

					// reselect the previously selected row (which gets lost be refreshing the model)
					if (iSelectedRow != -1) {
						tbl.setRowSelectionInterval(iSelectedRow, iSelectedRow);
					}

					panel.restoreColumnWidths(ctl.getSelectedObjects(), mpWidths);

					// Set the newly added columns to optimal width
					for (CollectableEntityField clctef : collNewlySelected) {
						TableUtils.setOptimalColumnWidth(tbl, tbl.getColumnModel().getColumnIndex(clctef.getLabel()));
					}
				}
			});
		}
	}

	/**
	 * adds the column with the given field name before column columnBefore to the table.
	 * @param fields available and selected fields of the result table
	 * @param columnBefore the column of the column model (as opposed to the column of the table model)
	 * @param sFieldNameToAdd name of the column to add
	 */
	protected void cmdAddColumn(final ChoiceEntityFieldList fields, TableColumn columnBefore, final String sFieldNameToAdd) throws CommonBusinessException {
		// find the field with the given name in available fields:
		final CollectableEntityField clctef = CollectionUtils.findFirst(fields.getAvailableFields(),
				PredicateUtils.transformedInputEquals(new CollectableEntityField.GetName(), sFieldNameToAdd));

		assert clctef != null;

		final CollectableTableModel<?> tblmodel = (CollectableTableModel<?>) getResultPanel().getResultTable().getModel();
		final CollectableEntityField clctefBefore = tblmodel.getCollectableEntityField(columnBefore.getModelIndex());
		int insertPos = fields.getSelectedFields().indexOf(clctefBefore);
		insertPos = (insertPos >= 0) ? insertPos : 0;
		fields.moveToSelectedFields(insertPos, clctef);

		// Note that it is not enough to add the column to the result table model.
		// We must rebuild the table tblmodel's columns in order to sync it with the table column model:
		tblmodel.setColumns(fields.getSelectedFields());
	}

	/**
	 * removes the given column from the table
	 * @param entityField the column of the column model (as opposed to the column of the table model)
	 */
	protected void cmdRemoveColumn(final ChoiceEntityFieldList fields, CollectableEntityField entityField, CollectController<Clct> ctl) {
		assert ctl == this.clctctl && ctl.getFields() == fields;
		fields.moveToAvailableFields(entityField);

		// Note that it is not enough to remove the column from the result table model.
		// We must rebuild the table model's columns in order to sync it with the table column model:
		// this.getResultTableModel().removeColumn(iColumn);
		((SortableCollectableTableModel<?>) getResultPanel().getResultTable().getModel()).setColumns(fields.getSelectedFields());
	}

	protected void setModel(CollectableTableModel<Clct> tblmodel, final CollectableEntity clcte, final CollectController<Clct> ctl) {
		assert ctl == this.clctctl && ctl.getFields() == fields;
		assert this.clcte.equals(clcte);
		final ResultPanel<Clct> panel = getResultPanel();
		final JTable resultTable = panel.getResultTable();
		resultTable.setModel(tblmodel);
		((ToolTipsTableHeader) resultTable.getTableHeader()).setExternalModel(tblmodel);
		
		panel.tableHeaderColumnListener = new TableHeaderColumnPopupListener(resultTable.getTableHeader()) {

			@Override
			protected void removeColumnVisibility(TableColumn column) {
				final Map<String, Integer> mpWidths = panel.getVisibleColumnWidth(ctl.getFields().getSelectedFields());
				final CollectableEntityField clctef = ((CollectableEntityFieldBasedTableModel) resultTable.getModel()).getCollectableEntityField(column.getModelIndex());
				cmdRemoveColumn(ctl.getFields(), clctef, ctl);
				panel.restoreColumnWidths(ctl.getFields().getSelectedFields(), mpWidths);
			}

			@Override
			public List<? extends CollectableEntityField> getSelectedFields() {
				return ctl.getFields().getSelectedFields();
			}
		};
		resultTable.getTableHeader().addMouseListener(panel.tableHeaderColumnListener);
	}

	protected void toggleColumnVisibility(TableColumn columnBefore, final String sFieldName, final CollectController<Clct> ctl,  final CollectableEntity clcte)  {
		assert ctl == this.clctctl && ctl.getFields() == fields;
		assert this.clcte.equals(clcte);
		final ResultPanel<Clct> panel = getResultPanel();
		try {
			final ChoiceEntityFieldList fields = ctl.getFields();
			final Map<String, Integer> mpWidths = panel.getVisibleColumnWidth(fields.getSelectedFields());
			final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
			final int iIndex = fields.getSelectedFields().indexOf(clctef);
			if (iIndex == -1) {
				cmdAddColumn(fields, columnBefore, sFieldName);
				if (!ctl.getCollectablesInResultAreAlwaysComplete()) {
					ctl.refreshResult();
				}
			}
			else {
				cmdRemoveColumn(fields, clctef, ctl);
			}
			panel.restoreColumnWidths(fields.getSelectedFields(), mpWidths);
		} 
		catch (CommonBusinessException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	/**
	 * writes the widths of the selected columns (fields) to the user preferences.
	 * @param prefs
	 */
	protected void writeFieldWidthsToPreferences(Preferences prefs) throws PreferencesException {
		final ResultPanel<Clct> panel = getResultPanel();
		final List<Integer> lstFieldWidths = CollectableTableHelper.getColumnWidths(panel.getResultTable());
		PreferencesUtils.putIntegerList(prefs, CollectController.PREFS_NODE_SELECTEDFIELDWIDTHS, lstFieldWidths);
	}
	
	/**
	 * Command: Define selected <code>Collectable</code>s as a new search result.
	 * 
	 * @deprecated Does nothing.
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

}	// class ResultController
