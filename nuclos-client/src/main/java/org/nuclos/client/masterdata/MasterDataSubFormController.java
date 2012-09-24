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
package org.nuclos.client.masterdata;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.nuclos.client.common.AbstractDetailsSubFormController;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.layout.LayoutUtils;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.scripting.ScriptEvaluator;
import org.nuclos.client.scripting.context.SubformControllerScriptContext;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SizeKnownEvent;
import org.nuclos.client.ui.SizeKnownListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormTable;
import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;
import org.nuclos.client.ui.collect.SubForm.TransferLookedUpValueAction;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.event.PopupMenuMouseAdapter;
import org.nuclos.client.ui.gc.ListenerUtil;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.AbstractCollectableField;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.DefaultValueObjectList;
import org.nuclos.common.collection.ValueObjectList;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;

/**
 * Controller for collecting dependant masterdata (in a one-to-many relationship) in a subform.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph Radig</a>
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @todo rename to MasterDataDetailsSubFormController
 * @version 01.00.00
 */
public class MasterDataSubFormController extends DetailsSubFormController<CollectableEntityObject> {

	private static final Logger LOG = Logger.getLogger(MasterDataSubFormController.class);

	protected static final String TB_CLONE = "Toolbar.CLONE";

	private RowSelectionListener rowselectionlistener;
	private List<MasterDataSubFormController> lstChildSubController = new ArrayList<MasterDataSubFormController>();
	private CollectableMasterData clctParent;
	private TransferHandler subFormTransferHandler;
	private EntityCollectController<CollectableEntityObject> parentController;

	int selectedColumn;

	/**
	 * @param tab
	 * @param clctcompmodelproviderParent provides <code>CollectableComponentModel</code>s. This avoids handing
	 * the whole <code>CollectController</code> to the DetailsSubFormController.
	 * @param sParentEntityName
	 * @param subform contains the subform (which in return containsPersonalSearchFilter the entity name for the subform).
	 * @param prefsUserParent the preferences of the parent (controller)

	 */
	public MasterDataSubFormController(MainFrameTab tab,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
		this(DefaultCollectableEntityProvider.getInstance().getCollectableEntity(subform.getEntityName()), tab, clctcompmodelproviderParent,
				sParentEntityName, subform, prefsUserParent, entityPrefs, valueListProviderCache);
	}

	/**
	 * Ctor for creating a new MasterDataSubFormController with custom CollectableEntity.
	 *
	 * @param clcte
	 * @param tab
	 * @param parentMdi
	 * @param clctcompmodelproviderParent
	 * @param sParentEntityName
	 * @param subform
	 * @param prefsUserParent
	 * @param valueListProviderCache
	 */
	public MasterDataSubFormController(CollectableEntity clcte, MainFrameTab tab,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, EntityPreferences entityPrefs, CollectableFieldsProviderCache valueListProviderCache) {
		super(clcte, tab, clctcompmodelproviderParent, sParentEntityName, subform,
				prefsUserParent, entityPrefs, MasterDataCollectableFieldsProviderFactory.newFactory(null, valueListProviderCache));

		getSubForm().getJTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent e) {
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				boolean enabled = !lsm.isSelectionEmpty() && lsm.getMaxSelectionIndex() == lsm.getMinSelectionIndex() && MasterDataSubFormController.this.isEnabled();
				if (enabled && getSubForm().getCloneEnabledScript() != null) {
					for (int i : getSubForm().getJTable().getSelectedRows()) {
						Collectable c = getCollectables().get(getSubForm().getSubformTable().convertRowIndexToModel(i));

						Object o = ScriptEvaluator.getInstance().eval(getSubForm().getCloneEnabledScript(), new SubformControllerScriptContext(getCollectController(), MasterDataSubFormController.this, c), enabled);
						if (o instanceof Boolean) {
							enabled = (Boolean) o;
						}
					}
				}
				final boolean bEnabled = enabled;

				UIUtils.invokeOnDispatchThread(new Runnable() {
					@Override
					public void run() {
						getSubForm().setToolbarFunctionState(TB_CLONE,
							bEnabled ? SubForm.ToolbarFunctionState.ACTIVE
								: SubForm.ToolbarFunctionState.DISABLED);
					}
				});
			}
		});

		getSubForm().setToolbarFunctionState(SubForm.ToolbarFunction.FILTER.name(), SubForm.ToolbarFunctionState.ACTIVE);

		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				JButton clone = new JButton(Icons.getInstance().getIconClone16());
				clone.setSize(16,16);
				clone.setToolTipText(getSpringLocaleDelegate().getMessage(
						"MasterDataSubFormController.1", "Datensatz klonen"));
				JMenuItem miClone = new JMenuItem(getSpringLocaleDelegate().getMessage(
						"MasterDataSubFormController.1", "Datensatz klonen"), Icons.getInstance().getIconClone16());
				getSubForm().addToolbarFunction(TB_CLONE, clone, miClone, 1);
				getSubForm().setToolbarFunctionState(TB_CLONE, SubForm.ToolbarFunctionState.DISABLED);
			}
		});

		ListenerUtil.registerSubFormToolListener(getSubForm(), null, cloneListener);
		setupSubFormTableContextMenue();
	}


	private void setupSubFormTableContextMenue() {
		PopupMenuMouseAdapter popupMenuMouseAdapter = new PopupMenuMouseAdapter() {
			@Override
			public void doPopup(MouseEvent e) {
				final JPopupMenu result = new JPopupMenu();

				JMenuItem miDetails = new JMenuItem(getSpringLocaleDelegate().getMessage(
						"AbstractCollectableComponent.7","Details anzeigen..."));
				JMenuItem miEdit = new JMenuItem(getSpringLocaleDelegate().getMessage(
						"AbstractCollectableComponent.21","Zelle bearbeiten"));

				miDetails.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent ev) {
						try {
							cmdShowDetails();
						}
						catch(CommonFinderException e) {
							throw new NuclosFatalException(e);
						}
						catch(CommonPermissionException e) {
							throw new NuclosFatalException(e);
						}
						catch(CommonBusinessException e) {
							throw new NuclosFatalException(e);
						}
					}
				});
				result.add(miDetails);

				final int iRow = getSubForm().getJTable().rowAtPoint(e.getPoint());
				if (iRow >= 0) {
					final int iSelectedRowCount = getSubForm().getJTable().getSelectedRowCount();
					String entityName = MasterDataSubFormController.this.getSelectedCollectable().getCollectableEntity().getName();

					try {
						miDetails.setEnabled(iSelectedRowCount == 1
							&& getSelectedCollectable().getId() != null
							&& (Integer)getSelectedCollectable().getId() > 0 && !LayoutUtils.isSubformEntity(entityName));
					}
					catch(Exception e1) {
						LOG.warn("setupSubFormTableContextMenue: " + e1);
						miDetails.setEnabled(false);
					}

					try {
						final int row = getSubForm().getJTable().rowAtPoint(e.getPoint());
						final int col = getSubForm().getJTable().columnAtPoint(e.getPoint());
						Object obj = getSubForm().getJTable().getValueAt(iRow, col);
						if(obj instanceof AbstractCollectableField) {
							AbstractCollectableField field = (AbstractCollectableField)obj;
							miEdit.setVisible(LangUtils.isValidURI(field.toString()));
							miEdit.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent e) {
									cmdEditCell(row, col);
								}
							});
							result.add(miEdit);
						}
						else {
							miEdit.setVisible(false);
						}
					}
					catch(Exception e1) {
						LOG.warn("setupSubFormTableContextMenue: " + e1);
						miEdit.setVisible(false);
					}
				}
				result.show(getSubForm().getJTable(), e.getX(), e.getY());
			}
		};

		this.getSubForm().setPopupMenuAdapter(popupMenuMouseAdapter);
	}

	private void cmdEditCell(int row, int col) {
		this.getSubForm().getJTable().editCellAt(row, col);
		Component comp = this.getSubForm().getJTable().getEditorComponent();
		if(comp != null) {
			comp.requestFocusInWindow();
		}
	}

	private void cmdShowDetails() throws CommonBusinessException {
		UIUtils.runCommandForTabbedPane(getMainFrameTabbedPane(), new CommonRunnable() {

			@Override
			public void run() throws CommonBusinessException {
				String sEntity = MasterDataSubFormController.this.getSelectedCollectable().getCollectableEntity().getName();

				if(sEntity != null) {
					if(MetaDataClientProvider.getInstance().getEntity(sEntity).isStateModel()) {
						showGenericobject(MasterDataSubFormController.this.getSelectedCollectable().getId());
					}
					else {
						showMasterData(MasterDataSubFormController.this.getSelectedCollectable().getId(), sEntity);
					}
				}
				else {
					showGenericobject(MasterDataSubFormController.this.getSelectedCollectable().getId());
				}
			}

		});

	}

	private void showMasterData(final Object iMasterdataId,	String entityName) throws CommonBusinessException {
		if(LayoutUtils.isSubformEntity(entityName))
			return;
		MasterDataCollectController ctlMasterdata = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(entityName, null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));
		ctlMasterdata.runViewSingleCollectableWithId(iMasterdataId);
	}

	private void showGenericobject(final Object iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		//final Object iGenericObjectId = clct.getId();
		try {
			final GenericObjectVO govo = GenericObjectDelegate.getInstance().get((Integer) iGenericObjectId);
			final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
					newGenericObjectCollectController(govo.getModuleId(), null, ClientParameterProvider.getInstance().getValue(ParameterProvider.KEY_LAYOUT_CUSTOM_KEY));

			ctlGenericObject.runViewSingleCollectable(CollectableGenericObjectWithDependants.newCollectableGenericObject(govo));
		}
		catch(CommonFatalException ex){
			throw new CommonFatalException(getSpringLocaleDelegate().getMessage(
					"DynamicEntitySubFormController.2", "Der Datensatz kann nicht angezeigt werden. Bitte tragen Sie in der Datenquelle für die dynamische Entität, die Entität ein, die angezeigt werden soll!"));
		}
	}

	private static class LookupValuesListener implements LookupListener {

		private final SubFormTable subformtbl;

		private final boolean searchable;

		private final int row;

		private final Collection<TransferLookedUpValueAction> valueActions;

		private LookupValuesListener(SubFormTable subformtbl, boolean searchable, int row,
				Collection<TransferLookedUpValueAction> valueActions) {

			this.subformtbl = subformtbl;
			this.searchable = searchable;
			this.row = row;
			this.valueActions = valueActions;
		}

		@Override
		public void lookupSuccessful(LookupEvent ev) {
			SubForm.transferLookedUpValues(ev.getSelectedCollectable(), subformtbl, searchable,
					row, valueActions, true);
		}

		@Override
        public int getPriority() {
            return 1;
        }
	}

	/**
	 *
	 * @param referenceEntity
	 * @param referenceId
	 * @return true if new clct is inserted
	 * @throws NuclosBusinessException is thrown if reference entity could not be found in columns
	 */
	@Override
	public boolean insertNewRowWithReference(String referenceEntity, Collectable referenceClct, boolean validateWithVLP) throws NuclosBusinessException {
		if (getSubForm().getSubformTable().getCellEditor() != null)
			getSubForm().getSubformTable().getCellEditor().stopCellEditing();

		boolean clctChanged = false;

		CollectableEntityObject clct = insertNewRow();
		final int row = getCollectableTableModel().getRowCount()-1;

		boolean noReferenceFound = true;

		// find fields for copied entity objects
		for (final String field : clct.getCollectableEntity().getFieldNames()) {
			CollectableEntityField clctef = clct.getCollectableEntity().getEntityField(field);
			if (referenceEntity.equals(clctef.getReferencedEntityName())) {

				// for getting readable presentation and fire lookup use ListOfValues
                CollectableListOfValues clctlov = new CollectableListOfValues(clct.getCollectableEntity().getEntityField(field));
                try {
                	boolean insert = false;
                	//check provider if value exists in result

                	CollectableComponentTableCellEditor cellEditor = ((CollectableComponentTableCellEditor)getTableCellEditor(getJTable(), row, clctef));
                	if (cellEditor == null) {
                		// could happen, column is foreign column.
                		continue;
                	}
                	CollectableComponent clctcomp = cellEditor.getCollectableComponent();

                	noReferenceFound = false;

                	if (clctcomp instanceof LabeledCollectableComponentWithVLP) {
                		CollectableFieldsProvider provider = ((LabeledCollectableComponentWithVLP)clctcomp).getValueListProvider();
                		if (provider != null) {
                			if (validateWithVLP) {
	                            for (CollectableField clctField : provider.getCollectableFields()) {
	                            	if (clctField instanceof CollectableValueIdField) {
	                            		CollectableValueIdField clctValueIdField = (CollectableValueIdField) clctField;
	                            		if (referenceClct.getId().equals(clctValueIdField.getValueId())) {
	                            			insert = true;
	                            			break;
	                            		}
	                            	}
	                            }
                			} else {
                				insert = true;
                			}
                    	} else {
                    		insert = true;
                    	}
                	} else {
                		insert = true;
                	}

                	// set field
                    if (insert) {
                		final Collection<TransferLookedUpValueAction> valueActions =
                				getSubForm().getTransferLookedUpValueActions(field);
                        clctlov.addLookupListener(new LookupValuesListener(
                        		(SubFormTable)getJTable(), isSearchable(), row, valueActions));
                        try {
                            clctlov.acceptLookedUpCollectable(referenceClct);
                            clct.setField(clctlov.getFieldName(), clctlov.getField());
                            clctChanged = true;
                        }
                        catch(Exception e1) {
                            LOG.error("insertNewRowWithReference failed: " + e1, e1);
                        }
                    }
                }
                catch(CommonBusinessException e) {
                    LOG.error("insertNewRowWithReference failed: " + e, e);
                }
			}
		}

		if (!clctChanged) {
			getCollectableTableModel().remove(clct);
		}

		if (noReferenceFound)
			throw new NuclosBusinessException("Reference entity column " + referenceEntity + " not found.");

		return clctChanged;
	}

	private SubFormToolListener cloneListener = new SubFormToolListener() {
		@Override
		public void toolbarAction(String actionCommand) {
			if(actionCommand.equals(TB_CLONE)) {
				int selRow = getJTable().getSelectedRow();
				if (selRow != -1) {
					stopEditing();

					// clone selected record
					CollectableEntityObject original = getCollectableTableModel().getCollectable(getJTable().getSelectedRow());
					CollectableEntityObject clone = new CollectableEntityObject(original.getCollectableEntity(), original.getEntityObjectVO().copy());

					try {
						cloneRow(original, clone);
					} catch (NuclosBusinessException e) {
						throw new CommonFatalException(e);
					}
					
					getSubForm().getJTable().setRowSelectionInterval(selRow, selRow);
				}
			}
		}
	};
	
	protected void cloneRow(CollectableEntityObject original, CollectableEntityObject clone) throws NuclosBusinessException {
		// check whether the selected record has some dependant data
		boolean hasDependantData = false;
		if (hasChildSubForm()) {
			for (MasterDataSubFormController mdsfctl : getChildSubFormController()) {
				if (!mdsfctl.getCollectables().isEmpty()) {
					hasDependantData = true;
					break;
				}
			}
		}

		int result = JOptionPane.NO_OPTION;
		if (hasDependantData) {
			String sMessage = getSpringLocaleDelegate().getMessage(
					"MasterDataSubFormController.2", "Der zu klonende Datensatz besitzt abh\u00e4ngige Unterformulardaten. Sollen diese auch geklont werden?");
			result = JOptionPane.showConfirmDialog(getTab(), sMessage,
					getSpringLocaleDelegate().getMessage("MasterDataSubFormController.1", "Datensatz klonen"), JOptionPane.YES_NO_OPTION);
		}
		
		// add cloned data
		CollectableEntityObject clctNew = insertNewRow(clone);
		setParentId(clctNew, getParentId());
		getCollectableTableModel().add(clctNew);

		// clone and add dependant data
		if (result == JOptionPane.YES_OPTION) {
			cloneDependantData(original, clone);
		}
	}

	/**
	 * clones the dependant data of the given original collectable recursively
	 * @param clctmd_original original collectable masterdata
	 * @param clctmd_clone a clone of the original collectable masterdata
	 * @throws NuclosBusinessException
	 */
	protected void cloneDependantData(CollectableEntityObject clctmd_original, CollectableEntityObject clctmd_clone) throws NuclosBusinessException {
		for (MasterDataSubFormController mdsfctl : getChildSubFormController()) {
			List<CollectableEntityObject> lsclctmd = mdsfctl.readDependants(clctmd_original);
			mdsfctl.setCollectableParent(clctmd_clone);

			for (CollectableEntityObject clctmd : lsclctmd) {
				if (!clctmd.isMarkedRemoved()) {
					CollectableEntityObject clone = new CollectableEntityObject(clctmd.getCollectableEntity(), clctmd.getEntityObjectVO().copy());
					clone.setField(mdsfctl.getForeignKeyFieldName(), CollectableValueIdField.NULL); // reset referencing field.- @see NUCLOS-736
					mdsfctl.insertNewRow(clone);
					mdsfctl.cloneDependantData(clctmd, clone);
				}
			}
		}
	}

	@Override
	protected void postCreate() {
		this.setupListSelectionListener();

		getSubForm().addLookupListener(new LookupListener() {
			@Override
			public void lookupSuccessful(LookupEvent ev) {
				if (ev.getAdditionalCollectables() != null)
					for (Collectable clct : ev.getAdditionalCollectables()) {
						CollectableEntityField clctef = ev.getCollectableComponent().getEntityField();
						clctef.getReferencedEntityName();
						try {
		                    insertNewRowWithReference(clctef.getReferencedEntityName(), clct, false);
	                    }
	                    catch(NuclosBusinessException e) {
                            LOG.error("lookupSuccessful failed: " + e, e);
	                    }
					}
			}

			@Override
            public int getPriority() {
	            return 2;
            }
		});

		super.postCreate();
	}

	/**
	 * releases resources, removes listeners.
	 */
	@Override
	public void close() {
		this.removeListSelectionListener();
		super.close();
	}

	private void setCollectableParent(CollectableMasterData clctParent) {
		this.clctParent = clctParent;
		if (clctParent != null) {
			this.setParentId(clctParent.getId());
		}
	}

	protected CollectableMasterData getCollectableParent() {
		return this.clctParent;
	}

	/**
	 * @return a new collectable adapter object containing a MasterDataVO
	 */
	@Override
	public CollectableEntityObject newCollectable() {
		// final CollectableEntity clctmde = this.getCollectableEntity();
		final CollectableEntity clctmde = getCollectableEntity();
		return new CollectableEntityObject(clctmde, EntityObjectVO.newObject(clctmde.getName()));
	}

	@Override
	protected ValueObjectList<CollectableEntityObject> newValueObjectList(List<CollectableEntityObject> lstclct) {
		return new DefaultValueObjectList<CollectableEntityObject>(lstclct);
	}

	/**
	 * fills this subform by loading the dependant data.
	 * @todo generalize/refactor
	 * @param oParentId
	 * @throws NuclosBusinessException
	 */
	public void fillSubForm(Integer iParentId) throws NuclosBusinessException {
		this.setParentId(iParentId);

		final Collection<EntityObjectVO> collmdvo = (iParentId == null) ?
				new ArrayList<EntityObjectVO>() :
				MasterDataDelegate.getInstance().getDependantMasterData(this.getCollectableEntity().getName(), this.getForeignKeyFieldName(), iParentId, getSubForm().getMapParams());

		this.fillSubForm(iParentId, collmdvo);
	}

	/**
	 * fills this subform with data in collection.
	 * @param collmdvo
	 * @throws NuclosBusinessException
	 */
	public void fillSubForm(Integer iParentId, final Collection<EntityObjectVO> collmdvo) throws NuclosBusinessException {
		this.setParentId(iParentId);

		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				final SizeKnownListener listener = getSubForm().getSizeKnownListener();
				// Trigger the 'loading' display...
				if (listener != null) {
					listener.actionPerformed(new SizeKnownEvent(getSubForm(), null));
				}
				final List<CollectableEntityObject> lst = CollectionUtils.transform(
					collmdvo,
					new CollectableEntityObject.MakeCollectable(getCollectableEntity()));
				MasterDataSubFormController.this.updateTableModel(lst);
				// Trigger the 'size' display...
				if (listener != null) {
					listener.actionPerformed(new SizeKnownEvent(getSubForm(), lst.size()));
				}
			}
		});
	}

	protected class RowSelectionListener implements ListSelectionListener {

		public RowSelectionListener() {
		}

		@Override
        public void valueChanged(ListSelectionEvent event) {
			if (!isMultiEdit()) {
				if (!event.getValueIsAdjusting()) {
					for (MasterDataSubFormController controller : getChildSubFormController()) {
						if (getJTable().getSelectedRows() != null && getJTable().getSelectedRows().length == 1) {
							// single selection
							controller.getSubForm().setEnabled(true); // enabled or disabled from Layout is respected via setEnabledByLayout
							CollectableEntityObject clct = getSelectedCollectable();
							controller.fillAsSubFormChild(MasterDataSubFormController.this, clct);
							controller.selectFirstRow();
						}
						else {
							// multi-selection
							controller.fillAsSubFormChild(MasterDataSubFormController.this, null);
							controller.getSubForm().setEnabled(false);  // enabled or disabled from Layout is respected via setEnabledByLayout
						}
					}
				}
			}
		}
	}

	private void setupListSelectionListener() {
		JTable tbl = getJTable();
		// initialize listener for row selection in the table:
		this.rowselectionlistener = new RowSelectionListener();
		tbl.getSelectionModel().addListSelectionListener(this.rowselectionlistener);
	}

	private void removeListSelectionListener() {
		if (getSubForm() != null) {
			getJTable().getSelectionModel().removeListSelectionListener(this.rowselectionlistener);
		}
		this.rowselectionlistener = null;
	}

	// add child subform controller to this subform
	public void addChildSubFormController(MasterDataSubFormController childSubFormController) {
		this.lstChildSubController.add(childSubFormController);
	}

	// get all child subform controller of this subform
	public List<MasterDataSubFormController> getChildSubFormController() {
		return this.lstChildSubController;
	}

	/**
	 * fills the subform (child subform) depending on the selected parent data
	 * @param clct
	 */
	private void fillAsSubFormChild(final AbstractDetailsSubFormController<?> sfc, final CollectableEntityObject clct) {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				MasterDataSubFormController.this.setCollectableParent(clct);

				List<CollectableEntityObject> lstclctmd = readDependants(clct);
				final boolean bWasDetailsChangedIgnored = getCollectController().isDetailsChangedIgnored();
				getCollectController().setDetailsChangedIgnored(true);
				try {
					final SizeKnownListener listener = getSubForm().getSizeKnownListener();
					// Trigger the 'loading' display...
					if (listener != null) {
						listener.actionPerformed(new SizeKnownEvent(getSubForm(), null));
					}
					MasterDataSubFormController.this.updateTableModel(lstclctmd);
					getSubForm().setNewEnabled(new SubformControllerScriptContext(getCollectController(), sfc, clct));
					// Trigger the 'size' display...
					if (listener != null) {
						listener.actionPerformed(new SizeKnownEvent(getSubForm(), lstclctmd.size()));
					}
				}
				finally {
					getCollectController().setDetailsChangedIgnored(bWasDetailsChangedIgnored);
				}
				//}
				// if subform contains no data, clear all dependant subforms as well
				if (lstclctmd == null) {
					for (MasterDataSubFormController subformcontroller : getChildSubFormController()) {
						subformcontroller.fillAsSubFormChild(MasterDataSubFormController.this, null);
					}
				}
			}
		});
	}

	/**
	 * fills all dependant child subforms of the current controller recursively
	 * @throws NuclosBusinessException
	 */
	public void fillAllChildSubForms() throws NuclosBusinessException {
		for (MasterDataSubFormController child : getChildSubFormController()) {
			for (CollectableEntityObject data : getCollectables()) {
				child.fillAsSubFormChild(this, data);
				child.fillAllChildSubForms();
			}
		}
	}

	/**
	 * read all dependant data for the given clct
	 * @param clct
	 * @return List<CollectableMasterData>
	 */
	private List<CollectableEntityObject> readDependants(CollectableMasterData clct) {
		if (clct == null) {
			return Collections.<CollectableEntityObject>emptyList();
		}

		List<CollectableEntityObject> lstclctmd = (List<CollectableEntityObject>)clct.getDependantCollectableMasterDataMap().getValues(this.getEntityAndForeignKeyFieldName().getEntityName());

		if (lstclctmd.isEmpty() && clct.getId() != null) {
			final Collection<EntityObjectVO> collmdvo = (clct == null) ?
					new ArrayList<EntityObjectVO>() :
						MasterDataDelegate.getInstance().getDependantMasterData(this.getCollectableEntity().getName(), this.getForeignKeyFieldName(), clct.getId(), getSubForm().getMapParams());

					lstclctmd = CollectionUtils.transform(collmdvo, new CollectableEntityObject.MakeCollectable(this.getCollectableEntity()));
					clct.getDependantCollectableMasterDataMap().addValues(this.getEntityAndForeignKeyFieldName().getEntityName(), lstclctmd);
		}
		return lstclctmd;
	}

	/**
	 * inserts a new row. The foreign key field to the parent entity will be set accordingly.
	 */
	@Override
	public CollectableEntityObject insertNewRow() throws NuclosBusinessException {
		CollectableEntityObject clct = super.insertNewRow();
		return insertNewRow(clct);
	}

	private CollectableEntityObject insertNewRow(CollectableEntityObject clct) throws NuclosBusinessException {
		if (this.isChildSubForm() && this.getCollectableParent() == null) {
			String sMessage = getSpringLocaleDelegate().getMessage(
					"MasterDataSubFormController.3", "Es kann kein Bezug zu einem \u00dcbergeordneten Datensatz hergestellt werden. "+
					"Bitte w\u00e4hlen Sie zuerst einen Datensatz aus dem \u00fcbergeordneten Unterformular aus.");
			throw new NuclosBusinessException(sMessage);
		}

		if (this.isChildSubForm()) {
			this.getCollectableParent().getDependantCollectableMasterDataMap().addValue(this.getEntityAndForeignKeyFieldName().getEntityName(), clct);
		}
		return clct;
	}

	@Override
	protected CollectableField getFieldFromParentSubform(String sFieldName) {
		if(isChildSubForm()) {
			DetailsSubFormController<CollectableEntityObject> subDetailsParent = parentController.getDetailsSubforms().get(getSubForm().getParentSubForm());
			subDetailsParent.stopEditing();
			CollectableMasterData md = subDetailsParent.getSelectedCollectable();
			return md.getField(sFieldName);
		}
		return null;
	}


	@Override
	protected void removeSelectedRows() {
		// this is necessary for rows that have been added and again removed before saving
		for (CollectableEntityObject clct : this.getSelectedCollectables()) {
			clct.markRemoved();
		}
		super.removeSelectedRows();
	}

	/**
	 * checks whether this subform is a child subform
	 * @return true, if this subform data depends on parent subform data, otherwise false
	 */
	public boolean isChildSubForm() {
		return (this.getSubForm().getParentSubForm() == null) ? false : true;
	}

	/**
	 * checks whether this subform has assigned one or more child subforms
	 * @return true, if child(ren) is/are assigned, otherwise false
	 */
	public boolean hasChildSubForm() {
		return !getChildSubFormController().isEmpty();
	}

	@Override
	public void selectFirstRow() {
		if (hasChildSubForm() && getJTable().getRowCount() > 0) {
			getJTable().setRowSelectionInterval(0,0);
		}
	}

	// clears all child subformcontroller of this controller recursively
	public void clearChildSubFormController() {
		for (MasterDataSubFormController subformcontroller : lstChildSubController) {
			subformcontroller.clearChildSubFormController();
			subformcontroller.fillAsSubFormChild(MasterDataSubFormController.this, null);
			if (subformcontroller.getCollectableParent() != null) {
				subformcontroller.getCollectableParent().getDependantCollectableMasterDataMap().clear();
				subformcontroller.setCollectableParent(null);
			}
		}
	}

	/**
	 * removes all rows from this subform.
	 */
	@Override
	public void clear() {
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				//@see NUCLOS-1139
				final SizeKnownListener listener = getSubForm().getSizeKnownListener();
				// Trigger the 'loading' display...
				if (listener != null) {
					listener.actionPerformed(new SizeKnownEvent(getSubForm(), null));
				}
				MasterDataSubFormController.super.clear();
				clearChildSubFormController();
			}
		});
	}

	public void setParentController(EntityCollectController<CollectableEntityObject> parentController) {
		this.parentController = parentController;
	}


}	// class MasterDataSubFormController
