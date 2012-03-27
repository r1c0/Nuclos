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
package org.nuclos.client.common;

import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.synthetica.NuclosThemeSettings;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.collect.FixedColumnRowHeader;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.Actions;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.PointerException;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProviderFactory;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.exception.CollectableFieldFormatException;
import org.nuclos.common.collect.exception.CollectableFieldValidationException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.PredicateUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.collection.ValueObjectList;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.DocumentFileBase;
import org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile;

/**
 * Controller for collecting dependant data (in a one-to-many relationship) in a subform.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public abstract class DetailsSubFormController<Clct extends Collectable>
		extends AbstractDetailsSubFormController<Clct> implements NuclosDropTargetVisitor {

	private static final Logger LOG = Logger.getLogger(DetailsSubFormController.class);
	
	/**
	 * the id of the (current) parent object.
	 */
	private Object oParentId;

	private FixedColumnRowHeader fixedcolumnheader;

	private EntityCollectController<?> cltctl;

	private boolean multiEdit = false;

	/**
	 * required for determination of editable rows (and in the future probably for editing sub-subforms)
	 */
	private MultiUpdateOfDependants multiUpdateOfDependants;

	/**
	 * @param parent
	 * @param parentMdi
	 * @param clctcompmodelproviderParent provides the enclosing <code>CollectController</code>'s <code>CollectableComponentModel</code>s.
	 * This avoids handing the whole <code>CollectController</code> to the <code>SubFormController</code>.
	 * May be <code>null</code> if there are no dependencies from subform columns to fields of the main form.
	 * @param sParentEntityName
	 * @param subform
	 * @param prefsUserParent the preferences of the parent controller
	 */
	public DetailsSubFormController(CollectableEntity clcte, Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderFactory clctfproviderfactory) {
		super(clcte, parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform,
				prefsUserParent, entityPrefs, clctfproviderfactory);

		if (this.isColumnSelectionAllowed(sParentEntityName)) {
			this.fixedcolumnheader = new FixedColumnRowHeader(this.getSubFormPrefs());
			this.fixedcolumnheader.initializeFieldsFromPreferences(this.getSubFormPrefs());
			subform.setTableRowHeader(this.fixedcolumnheader);
		}

		this.postCreate();
		this.setupDragDrop();
	}

	@Override
	protected void postCreate() {
		this.setupHeaderToolTips(this.getJTable());

		if (this.getSubForm().getSubFormFilter() == null) {
			this.getSubForm().setupTableFilter(this.getCollectableFieldsProviderFactory());
		}

		this.getSubForm().loadTableFilter(getParentEntityName());

		TableRowIndicator row2 = new TableRowIndicator(this.fixedcolumnheader.getHeaderTable(), TableRowIndicator.RESIZE_ALL_ROWS, this.getPrefs(), this);
		row2.addJTableToSynch(getJTable());

		super.postCreate();
		this.getSubForm().getJTable().setRowHeight(this.getPrefs().getInt(TableRowIndicator.SUBFORM_ROW_HEIGHT, 20));

		this.getSubForm().getJTable().getTableHeader().addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu pop = new JPopupMenu();

					int iColumn = getSubForm().getJTable().getTableHeader().columnAtPoint(e.getPoint());
					iColumn = getSubForm().getJTable().getTableHeader().getColumnModel().getColumn(iColumn).getModelIndex();
					List<JComponent> lstColumnActions = getColumnIndicatorActions(iColumn);
					if (!lstColumnActions.isEmpty())
					{
						for (JComponent action : lstColumnActions) {
							pop.add(action);
						}
					}
					pop.setLocation(e.getLocationOnScreen());
					pop.show(DetailsSubFormController.this.getJTable(), e.getX(), e.getY());
				}
			}

		});
		
		this.fixedcolumnheader.getHeaderTable().getColumnModel().addColumnModelListener(newSubFormTablePreferencesUpdateListener());
	}

	/**
	 * @return an list of jmenuitems. if no boolean entityfield available returns an empty list.
	 */
	List<JComponent> getRowIndicatorActions() {
		List<JComponent> result = new LinkedList<JComponent>();
		
		getSubForm().addToolbarMenuItems(result);
		result.add(new JSeparator());

		Collectable clct = this.getSelectedCollectable();
		if (clct == null) {
			return result;
		}

		final List<String> lstFieldNames = new LinkedList<String>();
		CollectableEntity clcte = this.getCollectableEntity();
		for (Iterator<String> iterator = clcte.getFieldNames().iterator(); iterator.hasNext();) {
			String sFieldName = iterator.next();
			CollectableEntityField clctef = clcte.getEntityField(sFieldName);
			if (clctef.getJavaClass() == Boolean.class && getSubForm().isColumnVisible(sFieldName)) {
				lstFieldNames.add(sFieldName);
			}
		}

		if (!lstFieldNames.isEmpty()) { // if empty, return an empty list.
			JMenuItem mi1 = new JMenuItem(getSpringLocaleDelegate().getMessage("DetailsSubFormController.1", "Alle setzen"));
			mi1.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean blnChanged = false;
					for (Collectable clct : DetailsSubFormController.this.getSelectedCollectables()) {
						if (clct != null) {
							for (Iterator<String> iterator = lstFieldNames.iterator(); iterator.hasNext();) {
								String sFieldName = iterator.next();
								if (DetailsSubFormController.this.getSubForm().isColumnEnabled(sFieldName)) {
									clct.setField(sFieldName, new CollectableValueField(Boolean.TRUE));
									blnChanged = true;
								}
							}
						}
					}
					if (blnChanged) {
						fireDataUpdatedForSelectedRows();
					}
				}
			});
			JMenuItem mi2 = new JMenuItem(getSpringLocaleDelegate().getMessage("DetailsSubFormController.2", "Alle zurücksetzen"));
			mi2.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					boolean blnChanged = false;
					for (Collectable clct : DetailsSubFormController.this.getSelectedCollectables()) {
						if (clct != null) {
							for (Iterator<String> iterator = lstFieldNames.iterator(); iterator.hasNext();) {
								String sFieldName = iterator.next();
								if (DetailsSubFormController.this.getSubForm().isColumnEnabled(sFieldName)) {
									clct.setField(sFieldName, new CollectableValueField(Boolean.FALSE));
									blnChanged = true;
								}
							}
						}
					}
					if (blnChanged) {
						fireDataUpdatedForSelectedRows();
					}
				}
			});
			result.add(mi1);
			result.add(mi2);
			result.add(new JPopupMenu.Separator());
		}
		return result;
	}

	/**
	 * @return an list of jmenuitems. if no boolean entityfield available returns an empty list.
	 */
	List<JComponent> getColumnIndicatorActions(final int iColumn) {
		List<JComponent> result = new LinkedList<JComponent>();

		if (iColumn == -1)
		{
			return result;
		}
		
		final CollectableEntityField clctef = getCollectableTableModel().getCollectableEntityField(iColumn);
		if (clctef.getJavaClass() == Boolean.class && getSubForm().isColumnVisible(clctef.getName())) {

			JMenuItem mi1 = new JMenuItem(getSpringLocaleDelegate().getMessage("DetailsSubFormController.1", "Alle setzen"));
			mi1.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					List<Clct> clcts = DetailsSubFormController.this.getCollectables();
					boolean blnChanged = false;
					for (Clct clct : clcts) {
						clct.setField(clctef.getName(), new CollectableValueField(Boolean.TRUE));
						blnChanged = true;
					}
					if (blnChanged) {
						int last = DetailsSubFormController.this.getSubForm().getJTable().getRowCount()-1;
						((AbstractTableModel)DetailsSubFormController.this.getSubForm().getJTable().getModel()).fireTableRowsUpdated(0, last);
					}
				}
			});
			mi1.setEnabled(!getSubForm().getSubFormFilter().isFilteringActive());
			JMenuItem mi2 = new JMenuItem(getSpringLocaleDelegate().getMessage("DetailsSubFormController.2", "Alle zurücksetzen"));
			mi2.addActionListener(new ActionListener() {
	
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean blnChanged = false;
					List<Clct> clcts = DetailsSubFormController.this.getCollectables();
					for (Clct clct : clcts) {
						clct.setField(clctef.getName(), new CollectableValueField(Boolean.FALSE));
						blnChanged = true;
					}
					if (blnChanged) {
						int last = DetailsSubFormController.this.getSubForm().getJTable().getRowCount()-1;;
						((AbstractTableModel)DetailsSubFormController.this.getSubForm().getJTable().getModel()).fireTableRowsUpdated(0, last);
					}
				}
			});
			mi2.setEnabled(!getSubForm().getSubFormFilter().isFilteringActive());
	
			result.add(mi1);
			result.add(mi2);
			result.add(new JSeparator());
	
			JMenuItem mi3 = new JMenuItem(getSpringLocaleDelegate().getMessage(
					"DetailsSubFormController.setselected", "Selektierte Zeilen setzen"));
			mi3.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<Clct> clcts = DetailsSubFormController.this.getSelectedCollectables();
					boolean blnChanged = false;
					for (Clct clct : clcts) {
						clct.setField(clctef.getName(), new CollectableValueField(Boolean.TRUE));
						blnChanged = true;
					}
					if (blnChanged) {
						fireDataUpdatedForSelectedRows();
					}
				}
			});
			mi3.setEnabled(!getSubForm().getSubFormFilter().isFilteringActive() && getSelectedCollectables().size() > 0);
			JMenuItem mi4 = new JMenuItem(getSpringLocaleDelegate().getMessage(
					"DetailsSubFormController.resetselected", "Selektierte Zeilen zurücksetzen"));
			mi4.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					boolean blnChanged = false;
					List<Clct> clcts = DetailsSubFormController.this.getSelectedCollectables();
					for (Clct clct : clcts) {
						clct.setField(clctef.getName(), new CollectableValueField(Boolean.FALSE));
						blnChanged = true;
					}
					if (blnChanged) {
						fireDataUpdatedForSelectedRows();
					}
				}
			});
			mi4.setEnabled(!getSubForm().getSubFormFilter().isFilteringActive() && getSelectedCollectables().size() > 0);
	
			result.add(mi3);
			result.add(mi4);
		}
		
		if (!result.isEmpty())
			result.add(new JSeparator());
		
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS) ||
					!MainFrame.getWorkspace().isAssigned()) {
				final JMenuItem miPopupHideThisColumn = new JMenuItem(
						SpringLocaleDelegate.getInstance().getMessage("DetailsSubFormController.5","Diese Spalte ausblenden"));
				miPopupHideThisColumn.setIcon(Icons.getInstance().getIconRemoveColumn16());
				miPopupHideThisColumn.addActionListener(new ActionListener() {
					@Override
	                public void actionPerformed(ActionEvent ev) {
						fixedcolumnheader.hideCollectableEntityFieldColumn(clctef, 
								new AbstractAction() {
							@Override
							public void actionPerformed(ActionEvent e) {
								List<? extends SortKey> sortKeys = new ArrayList<SortKey>(getCollectableTableModel().getSortKeys());
								for (SortKey sortKey : getCollectableTableModel().getSortKeys()) {
									if (sortKey.getColumn() == iColumn)
										sortKeys.remove(sortKey);
								}
								getCollectableTableModel().setSortKeys(sortKeys, true);
							}
						});
					}
				});
				result.add(miPopupHideThisColumn);
				result.add(new JSeparator());
			}
		
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_ASSIGN) &&
				MainFrame.getWorkspace().isAssigned()) {
			final JMenuItem miPublishColumns = new JMenuItem(new AbstractAction(getSpringLocaleDelegate().getMessage(
					"DetailsSubFormController.4", "Spalten in Vorlage publizieren"), 
					Icons.getInstance().getIconRedo16()) {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).publishSubFormPreferences(
								MainFrame.getWorkspace(), 
								getCollectController().getEntity(), 
								getSubFormPrefs());
					} catch (CommonBusinessException e1) {
						Errors.getInstance().showExceptionDialog(getParent(), e1);
					}
				}
			});
			result.add(miPublishColumns);
		}
		
		JMenuItem miRestoreColumns = new JMenuItem(new AbstractAction(
				getSpringLocaleDelegate().getMessage("DetailsSubFormController.3", "Alle Spalten auf Vorlage zurücksetzen"), 
				Icons.getInstance().getIconUndo16()) {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					WorkspaceUtils.restoreSubFormPreferences(getSubFormPrefs(), getCollectController().getEntity());
					
					final List<Integer> widths = WorkspaceUtils.getColumnWidths(getSubFormPrefs());
					final List<CollectableEntityField> allFields = fixedcolumnheader.getAllAvailableFields();
					final List<CollectableEntityField> selected = WorkspaceUtils.getSelectedFields(getSubFormPrefs(), allFields);
					final Set<CollectableEntityField> fixed = WorkspaceUtils.getFixedFields(getSubFormPrefs(), selected);
					
					makeSureSelectedFieldsAreNonEmpty(getCollectableEntity(), selected);
					
					fixedcolumnheader.changeSelectedColumns(
							selected, 
							fixed, 
							widths,
							null,
							new AbstractAction() {
								@Override
								public void actionPerformed(ActionEvent e) {
									getSubForm().resetDefaultColumnWidths();
								}
							},
							new AbstractAction() {
								@Override
								public void actionPerformed(ActionEvent e) {
									setColumnOrder();
								}
							});
				} catch (CommonBusinessException e1) {
					Errors.getInstance().showExceptionDialog(getParent(), e1);
				}
			}
		});
		result.add(miRestoreColumns);

		return result;
	}

	private void fireDataUpdatedForSelectedRows() {
		int first = DetailsSubFormController.this.getSubForm().getJTable().getSelectionModel().getMinSelectionIndex();
		int last = DetailsSubFormController.this.getSubForm().getJTable().getSelectionModel().getMinSelectionIndex();
		((AbstractTableModel)DetailsSubFormController.this.getSubForm().getJTable().getModel()).fireTableRowsUpdated(first, last);
	}

	/**
	 * sets the responsible CollectController for this SubForm
	 * @param CollectController cltctl
	 */
	public void setCollectController(EntityCollectController<?> masterDataCollectController) {
		this.cltctl = masterDataCollectController;
	}

	/**
	 * @return the responsible CollectController of this SubForm
	 */

	public EntityCollectController<?> getCollectController() {
		return this.cltctl;
	}

	/**
	 * releases resources, removes listeners.
	 */
	@Override
	public void close() {
		if (getSubForm() != null) {
			final JTable table = getJTable();
			if (table != null) {
				TableUtils.removeMouseListenersForSortingFromTableHeader(table);
			}
		}

		this.removeColumnModelListener();
		this.removeTableModelListener();

		getSubForm().storeTableFilter(getParentEntityName());

		super.close();
	}

 	/**
	 * @param lstclct List<Collectable>
	 * @return a new <code>ValueObjectList</code> containing the given <code>Collectable</code>s.
	 */
	protected abstract ValueObjectList<Clct> newValueObjectList(List<Clct> lstclct);

	@Override
	protected List<Clct> newCollectableList(List<Clct> lstclct) {
		return this.newValueObjectList(lstclct);
	}

	protected Object getParentId() {
		return oParentId;
	}

	public void setParentId(Object oParentId) {
		this.oParentId = oParentId;
	}

	/**
	 * @return ValueObjectList<Collectable>
	 * BEWARE: This list is used for reading only. Don't write to this list!
	 * @todo make private
	 */
	final ValueObjectList<Clct> getValueObjectList() {
		return (ValueObjectList<Clct>) this.getModifiableListOfCollectables();
	}

	/**
	 * validates the Collectables in this subform and the childsubforms if any, 
	 * sets the given parent id in the foreign key field of each Collectable
	 * and returns them.
	 * 
	 * @return All collectables, even the removed ones.
	 * @postcondition result != null
	 */
	public List<Clct> getAllCollectables(Object oParentId, Collection<DetailsSubFormController<Clct>> collSubForms, 
			boolean bSetParent, Clct clct) throws CommonValidationException {
		final String entity = getEntityAndForeignKeyFieldName().getEntityName();
		final List<Clct> result;
		
		if (bSetParent) {
			result= getCollectables(oParentId, true, true, true);
		}
		else {
			if (clct != null && clct instanceof CollectableMasterData) {
				// don't get collectables from table model for dependant data of subform childs
				final DependantCollectableMasterDataMap dcmdm = 
						((CollectableMasterData)clct).getDependantCollectableMasterDataMap();
				result = prepareAndValidateCollectables((List<Clct>)dcmdm.getValues(entity), 
						true, false, true);
			}
			else {
				result= getCollectables(true, true, true);
			}
		}

		for (DetailsSubFormController<Clct> subFormController : CollectionUtils.emptyIfNull(collSubForms)) {
			final String pentity = subFormController.getParentEntityName();
			if (entity.equals(pentity)) {
				// dependant subform case
				for (Clct clct1 : result) {
					subFormController.setCollectables(
							subFormController.getAllCollectables(clct1.getId(), collSubForms, false, clct1));
				}
			}
		}
		return result;
	}

	/**
	 * @return the ids of all <code>Collectable</code>s in this subform, including incomplete ones,
	 * excluding removed ones.
	 */
	public List<?> getCollectableIds() {
		return CollectionUtils.transform(this.getCollectables(), new Collectable.GetId());
	}

	/**
	 * sets the parent id on the Collectables in this subform and returns them.
	 * @return the <code>Collectable</code>s in this subform
	 * @param bIncludeIncompleteOnes Include incompletes <code>Collectable</code>s in the <code>result</code>?
	 * @param bIncludeRemovedOnes Include removed <code>Collectable</code>s in the <code>result</code>?
	 * @param bPrepareForSavingAndValidate prepare for saving and validate?
	 * In this case, empty rows are removed, booleans that are <code>null</code> are mapped to <code>false</code>.
	 * @postcondition result != null
	 */
	protected List<Clct> getCollectables(Object oParentId, boolean bIncludeIncompleteOnes, boolean bIncludeRemovedOnes, boolean bPrepareForSavingAndValidate) throws CommonValidationException {
		final List<Clct> result = this.getCollectables(bIncludeIncompleteOnes, bIncludeRemovedOnes, bPrepareForSavingAndValidate);
		// set parent id on those Collectables:
		/** @todo this doesn't belong here */
		for (Collectable clct : result) {
			this.setParentId(clct, oParentId);
		}
		assert result != null;
		return result;
	}

	/**
	 * @return the <code>Collectable</code>s in this subform
	 * @param bIncludeIncompleteOnes Include incomplete <code>Collectable</code>s in the <code>result</code>?
	 * @param bIncludeRemovedOnes Include removed <code>Collectable</code>s in the <code>result</code>?
	 * @param bPrepareForSavingAndValidate prepare for saving and validate?
	 * In this case, empty rows are removed, booleans that are <code>null</code> are mapped to <code>false</code>.
	 * @postcondition result != null
	 */
	public List<Clct> getCollectables(boolean bIncludeIncompleteOnes, boolean bIncludeRemovedOnes, boolean bPrepareForSavingAndValidate) throws CommonValidationException {
		if (!this.stopEditing()) {
			throw new CommonValidationException(getSpringLocaleDelegate().getMessage(
					"details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", getCollectableEntity().getLabel()));
				//"Ung\u00fcltige Eingabe im Unterformular \"" + getCollectableEntity().getLabel() + "\"");
		}

		final List<Clct> result = bIncludeIncompleteOnes ?
				new ArrayList<Clct>(this.getCollectables()) :
				CollectionUtils.select(this.getCollectables(), new Collectable.IsComplete());

		if (bPrepareForSavingAndValidate) {
			this.prepareForSaving(result);
			this.validate(result);
		}

		if (bIncludeRemovedOnes) {
			result.addAll(this.getValueObjectList().getRemovedObjects());
		}
		assert result != null;
		return result;
	}

	public List<Clct> prepareAndValidateCollectables(List<Clct> result, boolean bIncludeIncompleteOnes, boolean bIncludeRemovedOnes, boolean bPrepareForSavingAndValidate) throws CommonValidationException {
		if (!this.stopEditing()) {
			throw new CommonValidationException(getSpringLocaleDelegate().getMessage(
					"details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", getCollectableEntity().getLabel()));
		}

		result = bIncludeIncompleteOnes ?
				result :
				CollectionUtils.select(result, new Collectable.IsComplete());

		if (bPrepareForSavingAndValidate) {
			this.prepareForSaving(result);
			this.validate(result);
		}

		if (bIncludeRemovedOnes) {
			result.addAll(this.getValueObjectList().getRemovedObjects());
		}
		assert result != null;
		return result;
	}

	/**
	 * prepares the <code>Collectable</code>s in this subform for saving:
	 * Empty rows are removed, when they do not contain Booleans.
	 * Else, Booleans that are <code>null</code> are mapped to <code>false</code>.
	 */
	private void prepareForSaving(Collection<Clct> collclct) {
		final CollectableEntity clcte = this.getCollectableEntity();
		final Collection<String> collFieldNames = getNonForeignKeyFieldNames(clcte, this.getForeignKeyFieldName());

		for (Clct clct : new ArrayList<Clct>(collclct)) {
			if (containsOnlyNullFields(clct, collFieldNames)) {
				collclct.remove(clct);
			}
			else {
				// Note that the foreign key field needn't be treated specially, as it is never a Boolean.
				Utils.prepareCollectableForSaving(clct, clcte);
			}
		}
	}

	/**
	 * validates the given <code>Collectable</code>s (excluding the foreign key field):
	 * @throws CommonValidationException
	 * @param collclct
	 */
	private void validate(Collection<? extends Collectable> collclct) throws CommonValidationException {
		if(ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_CLIENT_VALIDATES_MASTERDATAVALUES).equals("1")) {
			final CollectableEntity clcte = this.getCollectableEntity();
			final Collection<String> collFieldNames = getNonForeignKeyFieldNames(clcte, this.getForeignKeyFieldName());

			for (Collectable clct : collclct) {
				if (!containsOnlyNullFields(clct, collFieldNames)) {
					// We cannot just validate the whole Collectable, as the foreign key field (referring to
					// the parent entity) might be null here (in case the Collectable in the parent entity is a new one).
					for (String sFieldName : collFieldNames) {
						try {
							final CollectableEntityField clctef = clcte.getEntityField(sFieldName);
							clct.getField(sFieldName).validate(clctef);
						}
						catch (CollectableFieldValidationException ex) {
							final String sMessage = getSpringLocaleDelegate().getMessage(
									"details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", clcte.getLabel()) + ex.getMessage();
							throw new CommonValidationException(sMessage, ex);
						}
					}
				}
			}
		}
	}

	private static Collection<String> getNonForeignKeyFieldNames(CollectableEntity clcte, String sForeignKeyFieldName) {
		return CollectionUtils.select(clcte.getFieldNames(), PredicateUtils.not(PredicateUtils.<String>isEqual(sForeignKeyFieldName)));
	}

	protected final void setupHeaderToolTips(JTable tbl) {
		tbl.setTableHeader(new ToolTipsTableHeader(this.getCollectableTableModel(), tbl.getColumnModel()));
	}

	/**
	 * sets the parent id of the given <code>Collectable</code>.
	 * The corresponding field in <code>clct</code> is not changed if the parent id is already equal to the given parent id.
	 * @param clct
	 * @param oParentId
	 */
	protected void setParentId(Collectable clct, Object oParentId) {
		final String sForeignKeyFieldName = this.getForeignKeyFieldName();
		if (!LangUtils.equals(IdUtils.toLongId(clct.getField(sForeignKeyFieldName).getValueId()), IdUtils.toLongId(oParentId))) {
			clct.setField(sForeignKeyFieldName, new CollectableValueIdField(oParentId, null));
		}
	}

	/**
	 * inserts a new row. The foreign key field to the parent entity will be set accordingly.
	 */
	@Override
	public Clct insertNewRow() throws NuclosBusinessException {
		final Clct clctNew = this.newCollectable();
		this.setParentId(clctNew, this.getParentId());
		CollectableUtils.setDefaultValues(clctNew, this.getCollectableEntity());

		// if subform filter is used, transfer the filter criteria to the inserted collectable
		if (getSubForm().getSubFormFilter().isFilteringActive()) {
			Map<String, CollectableComponent> comps = getSubForm().getSubFormFilter().getAllFilterComponents();
			for (String key : comps.keySet()) {
				CollectableComponent clctComp = comps.get(key);
				if (clctComp != null) {
					try {
						clctNew.setField(clctComp.getEntityField().getName(), clctComp.getField());
					}
					catch(CollectableFieldFormatException e) {
						throw new CommonFatalException(e);
					}
				}
			}
		}

		if (this.isMultiEdit()) {
			this.getSubForm().getJTable().setBackground(null);
		}

		this.getCollectableTableModel().add(clctNew);
		return clctNew;

	}

	/**
	 * @return the selected collectable, if any.
	 */
	public final Clct getSelectedCollectable() {
		final int iSelectedRow = this.getJTable().getSelectedRow();
		// Note that iSelectedRow >= this.getTableModel().getRowCount() may occur during deletion of the last element.
		// We take care of such a temporary inconsistency here.
		if (iSelectedRow == -1 || iSelectedRow >= this.getCollectableTableModel().getRowCount())
			return null;

		int modelIndex = this.getJTable().convertRowIndexToModel(iSelectedRow);
		return (modelIndex == -1) ? null : this.getCollectableTableModel().getCollectable(modelIndex);
	}

	public final List<Clct> getSelectedCollectables() {
		final List<Integer> lstSelectedRowNumbers = CollectionUtils.asList(this.getJTable().getSelectedRows());
		final List<Clct> result = CollectionUtils.transform(lstSelectedRowNumbers, new Transformer<Integer, Clct>() {
			@Override
			public Clct transform(Integer iRowNo) {
				int modelIndex = getJTable().convertRowIndexToModel(iRowNo);
				return getCollectableTableModel().getCollectable(modelIndex);
			}
		});
		assert result != null;
		return result;
	}

	/**
	 * removes all rows from this subform.
	 */
	public void clear() {
		this.setMultiUpdateOfDependants(null);
		this.updateTableModel(new ArrayList<Clct>());
	}

	/**
	 * @param clct
	 * @param collFieldNames
	 * @return Does the given <code>Collectable</code> contain only null values in the fields specified by
	 * <code>collFieldNames</code>?
	 */
	protected static boolean containsOnlyNullFields(Collectable clct, Collection<String> collFieldNames) {
		boolean result = true;
		for (String sFieldName : collFieldNames) {
			final CollectableField clctf = clct.getField(sFieldName);
			if (!clctf.isNull()) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * copies the model from <code>subformctlSource</code> to <code>subformctlDest</code>.
	 * @param subformctlSource
	 * @param subformctlDest
	 */
	public static <Clct extends Collectable> void copyModel(DetailsSubFormController<Clct> subformctlSource, DetailsSubFormController<Clct> subformctlDest) {
		subformctlDest.setParentId(subformctlSource.getParentId());
		subformctlDest.setCollectables(subformctlSource.getValueObjectList().clone());
	}

	/**
	 * stores the order of the columns in the table
	 */
	@Override
	protected void storeColumnOrderAndWidths(JTable tbl) {
		super.storeColumnOrderAndWidths(tbl);
		try {
			if (fixedcolumnheader != null) {
				this.fixedcolumnheader.writeFieldToPreferences(getSubFormPrefs());
			}
		}
		catch (PreferencesException ex) {
			throw new CommonFatalException(ex);
		}
	}

	public void selectFirstRow() {
	}

	public void setMultiEdit(boolean multiEdit) {
		this.multiEdit = multiEdit;
	}

	public boolean isMultiEdit() {
		return this.multiEdit;
	}
	public MultiUpdateOfDependants getMultiUpdateOfDependants() {
		return multiUpdateOfDependants;
	}

	public void setMultiUpdateOfDependants(MultiUpdateOfDependants multiUpdateOfDependants) {
		this.multiUpdateOfDependants = multiUpdateOfDependants;
	}

	@Override
	public boolean isRowEditable(int row) {
		if (getMultiUpdateOfDependants() != null) {
			Collectable clct = getCollectables().get(row);
			if (clct.getId() != null && !getMultiUpdateOfDependants().isCollectableEditable(getEntityAndForeignKeyFieldName().getEntityName(), clct)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isRowRemovable(int row) {
		if (getMultiUpdateOfDependants() != null) {
			Collectable clct = getCollectables().get(row);
			if (clct.getId() != null && !getMultiUpdateOfDependants().isCollectableEditable(getEntityAndForeignKeyFieldName().getEntityName(), clct)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public TableCellRenderer getTableCellRenderer(CollectableEntityField clctefTarget) {
		final TableCellRenderer result = super.getTableCellRenderer(clctefTarget);
		if (isMultiEdit()) {
			return new TableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					Component c = result.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					if (!isRowEditable(row) && !isSelected) {
						c.setBackground(NuclosThemeSettings.BACKGROUND_INACTIVEROW);
					}
					return c;
				}
			};
		}
		else {
			return result;
		}
	}

	protected void setupDragDrop() {
		DropTarget dropTargetSubform = new DropTarget(this.getSubForm().getJTable(), new NuclosDropTargetListener(this));
		dropTargetSubform.setActive(true);

		DropTarget dropTargetButton = new DropTarget(this.getSubForm().getToolbarButton("NEW"), new NuclosDropTargetListener(this));
		dropTargetButton.setActive(true);

		DropTarget dropTargetScroll = new DropTarget(this.getSubForm().getSubformScrollPane().getViewport(), new NuclosDropTargetListener(this));
		dropTargetScroll.setActive(true);

	}

	protected void insertNewRowFromDrop(File file) throws IOException {
		final Clct clctNew = this.newCollectable();
		this.setParentId(clctNew, this.getParentId());
		FileInputStream fis = new FileInputStream(file);
		byte[] b = new byte[(int)file.length()];
		fis.read(b);
		fis.close();
		GenericObjectDocumentFile docfile = new GenericObjectDocumentFile(file.getName(), b);

		final String sFieldDocument = getDocumentField();
		clctNew.setField(sFieldDocument, new CollectableValueField(docfile));
		this.getCollectableTableModel().add(clctNew);
	}

	private String getDocumentField() {
		String sField = null;
		for(String sf : this.getCollectableEntity().getFieldNames()) {
			if(this.getCollectableEntity().getEntityField(sf).getJavaClass().isAssignableFrom(org.nuclos.server.genericobject.valueobject.GenericObjectDocumentFile.class)) {
				sField = sf;
				break;
			}
		}
		return sField;
	}

	protected void updateRowFromDrop(int hereRow, List<File> files) throws FileNotFoundException, IOException {
		for(Iterator<File> it = files.iterator(); it.hasNext(); ) {
			File file = it.next();
			CollectableMasterData clma = (CollectableMasterData) DetailsSubFormController.this.getCollectables().get(hereRow);
			FileInputStream fis = new FileInputStream(file);
			byte[] b = new byte[(int)file.length()];
			fis.read(b);
			fis.close();
			GenericObjectDocumentFile docfile = new GenericObjectDocumentFile(file.getName(), b);
			final String sFieldDocument = getDocumentField();
			CollectableValueField valueField = new CollectableValueField(docfile);
			clma.setField(sFieldDocument, valueField);
			SortableCollectableTableModel<CollectableMasterData> model = (SortableCollectableTableModel<CollectableMasterData>) DetailsSubFormController.this.getCollectableTableModel();
			model.setCollectable(hereRow, clma);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					DetailsSubFormController.this.getJTable().repaint();

				}
			});

		}
	}

	@Override
	public void visitDragEnter(DropTargetDragEvent dtde) {}

	@Override
	public void visitDragExit(DropTargetEvent dte) {}

	@Override
	public void visitDragOver(DropTargetDragEvent dtde) {
		// check for TransferableGenericObjects or MasterDataVO support
		if (dtde.isDataFlavorSupported(TransferableGenericObjects.dataFlavor) || dtde.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)) {
        	dtde.acceptDrag(dtde.getDropAction());
        	return;
        }

		// check for File support
		boolean blnAcceptFileChosser = false;
		boolean blnAcceptFileList = false;
		boolean blnAcceptEmail = false;
		// check if there is an DocumentFileBase in the subform, otherwise don't let the user drop files
		CollectableEntity entity = DetailsSubFormController.this.getCollectableEntity();
		Set<String> setEntities = entity.getFieldNames();
		for(String sEntity : setEntities) {
			CollectableEntityField field = entity.getEntityField(sEntity);
			Class<?> clazz = field.getJavaClass();
			if(DocumentFileBase.class.isAssignableFrom(clazz)) {
				blnAcceptFileChosser = true;
				break;
			}
		}

		// check if one or more file want to be dropped
		Transferable trans = dtde.getTransferable();
		boolean b = trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		DataFlavor flavors[] = trans.getTransferDataFlavors();

		flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

        // Select best data flavor
        DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

        // Flavor will be null on Windows
        // In which case use the 1st available flavor
        flavor = (flavor == null) ? flavors[0] : flavor;

        // Flavors to check
        DataFlavor linux = null;
        try {
        	linux = new DataFlavor("text/uri-list;class=java.io.Reader");
        }
        catch(Exception e) {
        	LOG.warn("visitDragOver fails on linux: " + e);
        }

        if(flavor.equals(linux)) {
        	blnAcceptFileList = true;
        }
        else {
			if(flavors != null && flavors.length > 0) {
				try {
					int index = DragAndDropUtils.getIndexOfFileList(flavors, trans);
					if(trans.getTransferData(flavors[index]) instanceof List) {
						List<?> files = (List<?>) trans.getTransferData(flavors[index]);
						if(files.size() > 0) {
							if(files.get(0) instanceof File) {
								blnAcceptFileList = true;
							}
						}
					}
				}
				catch (Exception e) {
					// do nothing here
		        	LOG.warn("visitDragOver fails on flavours: " + e);
				}
			}

			// check if one or more outlook email want to be dropped
			if(flavors != null && flavors.length > 0) {
				int count = flavors.length;
				for(int i = 0; i < count; i++) {
					try {
						Object obj = trans.getTransferData(flavors[i]);
						if(obj instanceof String) {
							String strRow = (String)obj;
							if(strRow.indexOf("Betreff") != -1) {
								blnAcceptEmail = true;
							}
						}
					}
					catch(Exception e) {
						// do nothing here
			        	LOG.warn("visitDragOver fails on Betreff: " + e);
					}
				}
			}
        }

		if(blnAcceptFileChosser && (blnAcceptFileList || blnAcceptEmail)) {
			dtde.acceptDrag(dtde.getDropAction());
		}
		else {
			dtde.rejectDrag();
		}
	}

	@Override
	public void visitDrop(DropTargetDropEvent dtde) {
		try {
			boolean blnViewPort = false;
			if(dtde.getSource() instanceof DropTarget) {
				DropTarget target = (DropTarget)dtde.getSource();
				if(target.getComponent() instanceof JViewport) {
					blnViewPort = true;
				}
			}
			dtde.acceptDrop(dtde.getDropAction());
			Transferable trans = dtde.getTransferable();

			// check for TransferableGenericObjects or MasterDataVO support
			boolean reference = false;

			List<?> lstloim = null;
			if (dtde.isDataFlavorSupported(TransferableGenericObjects.dataFlavor)) {
				reference = true;
				lstloim = (List<?>) trans.getTransferData(TransferableGenericObjects.dataFlavor);
			}
			else if (dtde.isDataFlavorSupported(MasterDataIdAndEntity.dataFlavor)){
				reference = true;
				lstloim = (List<?>) trans.getTransferData(MasterDataIdAndEntity.dataFlavor);
			}

			if (reference) {
				int countNotImported = 0;
	    		int countImported = 0;
	    		boolean noReferenceFound = false;
				String entityname = null;
	    		String entityLabel = null;

	            for (Object o : lstloim) {
	            	Collectable clct = null;
	            	if (o instanceof GenericObjectIdModuleProcess) {
	            		GenericObjectIdModuleProcess goimp = (GenericObjectIdModuleProcess) o;
	            		Integer entityId = goimp.getModuleId();
	            		entityname = MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(entityId)).getEntity();

	            		try {
	            			clct = new CollectableGenericObjectWithDependants(GenericObjectDelegate.getInstance().getWithDependants(goimp.getGenericObjectId()));
	                    }
	                    catch(Exception e) {
	                        LOG.error("visitDrop failed: " + e, e);
	                    }
	            	}
	            	else if (o instanceof MasterDataIdAndEntity) {
	            		MasterDataIdAndEntity mdiae = (MasterDataIdAndEntity) o;
	            		entityname = mdiae.getEntity();
	            		try {
	            			clct = new CollectableMasterData(new CollectableMasterDataEntity(MasterDataDelegate.getInstance().getMetaData(mdiae.getEntity())), MasterDataDelegate.getInstance().get(mdiae.getEntity(), mdiae.getId()));
	            		}
	                    catch(CommonBusinessException e) {
	                        LOG.error("visitDrop failed: " + e, e);
	                    }
	            	}

	            	entityLabel = SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(
	            			MetaDataClientProvider.getInstance().getEntity(entityname));

	            	if (clct != null) {
	            		try {
	            			if (!insertNewRowWithReference(entityname, clct, true)) {
	                			countNotImported++;
	                		} else {
	                			countImported++;
	                		}
	                    }
	                    catch (NuclosBusinessException e) {
	                    	noReferenceFound = true;
	    		        	LOG.debug("visitDrop: No reference found: " + e);
	                    }
	            	}
	            }

	            if (noReferenceFound) {
	            	String bubbleInfo = getSpringLocaleDelegate().getMessage(
	            			"MasterDataSubFormController.4", "Dieses Unterformular enthält keine Referenzspalte zur Entität ${entity}.", entityLabel);
	            	new Bubble(getSubForm().getJTable(), bubbleInfo, 10, Bubble.Position.NO_ARROW_CENTER).setVisible(true);
	            } else {
	            	String sNotImported = getSpringLocaleDelegate().getMessage(
	            			"MasterDataSubFormController.5", "Der Valuelist Provider verhindert das Anlegen von ${count} Unterformular Datensätzen.", countNotImported);

	                getCollectController().getDetailsPanel().setStatusBarText(getSpringLocaleDelegate().getMessage(
	                		"MasterDataSubFormController.6", "${count} Unterformular Datensätze angelegt.", countImported) + (countNotImported == 0 ? "": " " + sNotImported));
	                if (countNotImported != 0) {
	                	new Bubble(getCollectController().getDetailsPanel().tfStatusBar, sNotImported, 10, Bubble.Position.UPPER) .setVisible(true);
	                }
	            }
	        	return;
	        }

			DataFlavor flavors[] = trans.getTransferDataFlavors();

			flavors = (flavors.length == 0) ? dtde.getCurrentDataFlavors() : flavors;

	        // Select best data flavor
	        DataFlavor flavor = DataFlavor.selectBestTextFlavor(flavors);

	        // Flavor will be null on Windows
	        // In which case use the 1st available flavor
	        flavor = (flavor == null) ? flavors[0] : flavor;

	        // Flavors to check
	        DataFlavor Linux = new DataFlavor("text/uri-list;class=java.io.Reader");

			Point here = dtde.getLocation();
			int hereRow = DetailsSubFormController.this.getSubForm().getJTable().rowAtPoint(here);

			if(flavor.equals(Linux)) {

                BufferedReader read = new BufferedReader(flavor.getReaderForText(trans));
                // Remove 'file://' from file name
                String fileName = read.readLine().substring(7).replace("%20"," ");
                // Remove 'localhost' from OS X file names
                if(fileName.substring(0,9).equals("localhost")) {
                        fileName = fileName.substring(9);
                }
                read.close();

                if(fileName != null && fileName.length() > 0 && hereRow > 0 && !blnViewPort) {
                	updateRowFromDrop(hereRow, Collections.singletonList(new File(fileName)));
                }
                else if(fileName != null && fileName.length() > 0) {
                	insertNewRowFromDrop(new File(fileName));
                }

			}
			else {
				for(int i = 0; i < flavors.length; i++) {
					Object obj = trans.getTransferData(flavors[i]);
					if(obj instanceof List) {
						List<File> files = (List<File>) trans.getTransferData(flavors[i]);
						if(files.size() == 1 && hereRow > 0 && !blnViewPort) {
							updateRowFromDrop(hereRow, files);
						}
						else {
							for(Iterator<File> it = files.iterator(); it.hasNext(); ) {
								File file = it.next();
								insertNewRowFromDrop(file);
							}
						}
					}
					else {
						List<File> lstFile = DragAndDropUtils.mailHandling();
						if(lstFile.size() == 1 && hereRow > 0 && !blnViewPort) {
							updateRowFromDrop(hereRow, lstFile);
						}
						else {
							for(File file : lstFile) {
								insertNewRowFromDrop(file);
							}
						}
						break;
					}
				}
			}
		}
		catch (PointerException e) {
        	LOG.warn("visitDrop fails with PointerException: " + e);
			Bubble bubble = new Bubble(DetailsSubFormController.this.getJTable(), getSpringLocaleDelegate().getMessage(
					"details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"),5, Bubble.Position.NW);
			bubble.setVisible(true);
		}
		catch (Exception e) {
        	LOG.warn("visitDrop fails: " + e);
		}
	}
	
	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	public abstract boolean insertNewRowWithReference(String entity, Collectable collectable, boolean b) throws NuclosBusinessException;

}	// class DetailsSubFormController
