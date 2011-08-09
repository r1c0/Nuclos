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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.datatransfer.MasterDataIdAndEntity;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.collect.FixedColumnRowHeader;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.ToolTipsTableHeader;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.collect.model.SortableCollectableTableModel;
import org.nuclos.client.ui.table.TableUtils;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.PointerException;
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
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.common2.exception.PreferencesException;
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

	/**
	 * the id of the (current) parent object.
	 */
	private Object oParentId;

	private FixedColumnRowHeader fixedcolumnheader;

	private EntityCollectController<?> cltctl;

	private boolean multiEdit = false;

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
	public DetailsSubFormController(Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, CollectableFieldsProviderFactory clctfproviderfactory) {
		super(parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform,
				prefsUserParent, clctfproviderfactory);

		if (this.isColumnSelectionAllowed(sParentEntityName)) {
			this.fixedcolumnheader = new FixedColumnRowHeader();
			this.fixedcolumnheader.initializeFieldsFromPreferences(this.getPrefs());
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

		TableRowResizer row2 = new TableRowResizer(this.fixedcolumnheader.getHeaderTable(), TableRowResizer.RESIZE_ALL_ROWS, this.getPrefs());
		row2.addJTableToSynch(getJTable());

		super.postCreate();
		this.getSubForm().getJTable().setRowHeight(this.getPrefs().getInt(TableRowResizer.SUBFORM_ROW_HEIGHT, 20));
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
		TableUtils.removeMouseListenersForSortingFromTableHeader(this.getJTable());

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
	 * validates the Collectables in this subform and the childsubforms if any, sets the given parent id in the foreign key field of each Collectable
	 * and returns them.
	 * @return All collectables, even the removed ones.
	 * @postcondition result != null
	 */
	@SuppressWarnings("unchecked")
	public List<Clct> getAllCollectables(Object oParentId, Collection<DetailsSubFormController<Clct>> collSubForms, boolean bSetParent, Clct clct) throws CommonValidationException {
		List<Clct> lsclct;
		if (bSetParent) {
			lsclct= this.getCollectables(oParentId, true, true, true);
		}
		else {
			if (clct != null && clct instanceof CollectableMasterData) {
				// don't get collectables from table model for dependant data of subform childs
				DependantCollectableMasterDataMap dcmdm = ((CollectableMasterData)clct).getDependantCollectableMasterDataMap();
				lsclct = prepareAndValidateCollectables((List<Clct>)dcmdm.getValues(this.getEntityAndForeignKeyFieldName().getEntityName()), true, false, true);
			}
			else {
				lsclct= this.getCollectables(true, true, true);
			}
		}

		for (DetailsSubFormController<Clct> subFormController : CollectionUtils.emptyIfNull(collSubForms)) {
			if (this.getEntityAndForeignKeyFieldName().getEntityName().equals(subFormController.getParentEntityName())) {
				for (Clct clct1 : lsclct) {
					subFormController.setCollectables(subFormController.getAllCollectables(clct1.getId(), collSubForms, false, clct1));
				}
			}
		}

		return lsclct;
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
			throw new CommonValidationException(CommonLocaleDelegate.getMessage("details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", getCollectableEntity().getLabel()));
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
			throw new CommonValidationException(CommonLocaleDelegate.getMessage("details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", getCollectableEntity().getLabel()));
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
							final String sMessage = CommonLocaleDelegate.getMessage("details.subform.controller", "Ung\u00fcltige Eingabe im Unterformular ''{0}''", clcte.getLabel()) + ex.getMessage();
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
				this.fixedcolumnheader.writeFieldToPreferences(getPrefs());
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

	protected void updateRowFromDrop(int hereRow, List files) throws FileNotFoundException, IOException {
		for(Iterator it = files.iterator(); it.hasNext(); ) {
			File file = (File)it.next();
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
			Class clazz = field.getJavaClass();
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
        DataFlavor Linux = null;
        try {
        	Linux = new DataFlavor("text/uri-list;class=java.io.Reader");
        }
        catch(Exception e) { }

        if(flavor.equals(Linux)) {
        	blnAcceptFileList = true;
        }
        else {
			if(flavors != null && flavors.length > 0) {
				try {
					int index = DragAndDropUtils.getIndexOfFileList(flavors, trans);
					if(trans.getTransferData(flavors[index]) instanceof List) {
						List files = (List) trans.getTransferData(flavors[index]);
						if(files.size() > 0) {
							if(files.get(0) instanceof File) {
								blnAcceptFileList = true;
							}
						}
					}
				}
				catch(Exception ex) {
					// do nothing here
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
					catch(Exception ex) {
						// do nothing here
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
	            		entityname = MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)).getEntity();
	            		entityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)));

	            		try {
	            			clct = new CollectableGenericObjectWithDependants(GenericObjectDelegate.getInstance().getWithDependants(goimp.getGenericObjectId()));
	                    }
	                    catch(Exception e) {
	                        log.error(e.getMessage(), e);
	                    }
	            	}
	            	else if (o instanceof MasterDataIdAndEntity) {
	            		MasterDataIdAndEntity mdiae = (MasterDataIdAndEntity) o;
	            		entityname = mdiae.getEntity();
	            		try {
	            			clct = new CollectableMasterData(new CollectableMasterDataEntity(MasterDataDelegate.getInstance().getMetaData(mdiae.getEntity())), MasterDataDelegate.getInstance().get(mdiae.getEntity(), mdiae.getId()));
	            		}
	                    catch(CommonBusinessException e) {
	                        log.error(e.getMessage(), e);
	                    }
	            	}
	            	if (clct != null) {
	            		try {
	            			if (!insertNewRowWithReference(entityname, clct, true)) {
	                			countNotImported++;
	                		} else {
	                			countImported++;
	                		}
	                    }
	                    catch(NuclosBusinessException e2) {
	                    	noReferenceFound = true;
	                    }
	            	}
	            }

	            if (noReferenceFound) {
	            	String bubbleInfo = CommonLocaleDelegate.getMessage("MasterDataSubFormController.4", "Dieses Unterformular enthält keine Referenzspalte zur Entität ${entity}.", entityLabel);
	            	new Bubble(getSubForm().getJTable(), bubbleInfo, 10, Bubble.Position.NO_ARROW_CENTER).setVisible(true);
	            } else {
	            	String sNotImported = CommonLocaleDelegate.getMessage("MasterDataSubFormController.5", "Der Valuelist Provider verhindert das Anlegen von ${count} Unterformular Datensätzen.", countNotImported);

	                getCollectController().getDetailsPanel().setStatusBarText(CommonLocaleDelegate.getMessage("MasterDataSubFormController.6", "${count} Unterformular Datensätze angelegt.", countImported) + (countNotImported == 0 ? "": " " + sNotImported));
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
						List files = (List) trans.getTransferData(flavors[i]);
						if(files.size() == 1 && hereRow > 0 && !blnViewPort) {
							updateRowFromDrop(hereRow, files);
						}
						else {
							for(Iterator it = files.iterator(); it.hasNext(); ) {
								File file = (File)it.next();
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
		catch(PointerException e) {
			Bubble bubble = new Bubble(DetailsSubFormController.this.getJTable(), CommonLocaleDelegate.getMessage("details.subform.controller.2", "Diese Funktion wird nur unter Microsoft Windows unterstützt!"),5, Bubble.Position.NW);
			bubble.setVisible(true);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void visitDropActionChanged(DropTargetDragEvent dtde) {}

	public abstract boolean insertNewRowWithReference(String entity, Collectable collectable, boolean b) throws NuclosBusinessException;

}	// class DetailsSubFormController
