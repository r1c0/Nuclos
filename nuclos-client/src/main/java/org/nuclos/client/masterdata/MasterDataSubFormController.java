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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectDelegate;
import org.nuclos.client.genericobject.datatransfer.GenericObjectIdModuleProcess;
import org.nuclos.client.genericobject.datatransfer.TransferableGenericObjects;
import org.nuclos.client.layout.LayoutUtils;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.ui.Bubble;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SizeKnownEvent;
import org.nuclos.client.ui.SizeKnownListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.CollectableComponentTableCellEditor;
import org.nuclos.client.ui.collect.component.CollectableListOfValues;
import org.nuclos.client.ui.collect.component.LabeledCollectableComponentWithVLP;
import org.nuclos.client.ui.collect.component.LookupEvent;
import org.nuclos.client.ui.collect.component.LookupListener;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableFieldsProvider;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.DefaultValueObjectList;
import org.nuclos.common.collection.ValueObjectList;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.entityobject.CollectableEOEntity;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
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
	
	private JMenuItem miDetails = new JMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.7","Details anzeigen..."));
	
	protected static final String TB_CLONE = "Toolbar.CLONE";
	
	private RowSelectionListener rowselectionlistener;
	private List<MasterDataSubFormController> lstChildSubController = new ArrayList<MasterDataSubFormController>();
	private CollectableMasterData clctParent;
	private TransferHandler subFormTransferHandler;
	private EntityCollectController<CollectableEntityObject> parentController;
	
	/**
	 * @param parent
	 * @param clctcompmodelproviderParent provides <code>CollectableComponentModel</code>s. This avoids handing
	 * the whole <code>CollectController</code> to the DetailsSubFormController.
	 * @param sParentEntityName
	 * @param subform contains the subform (which in return containsPersonalSearchFilter the entity name for the subform).
	 * @param prefsUserParent the preferences of the parent (controller)

	 */
	public MasterDataSubFormController(Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelproviderParent, String sParentEntityName, final SubForm subform,
			Preferences prefsUserParent, CollectableFieldsProviderCache valueListProviderCache) {
		super(parent, parentMdi, clctcompmodelproviderParent, sParentEntityName, subform,
				prefsUserParent, MasterDataCollectableFieldsProviderFactory.newFactory(null, valueListProviderCache));

		getSubForm().getJTable().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
            public void valueChanged(ListSelectionEvent e) {
				final ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				final boolean bEnabled = !lsm.isSelectionEmpty() && MasterDataSubFormController.this.isEnabled();
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
				clone.setToolTipText(CommonLocaleDelegate.getMessage("MasterDataSubFormController.1", "Datensatz klonen"));
				getSubForm().addToolbarFunction(TB_CLONE, clone, 1);
				getSubForm().setToolbarFunctionState(TB_CLONE, SubForm.ToolbarFunctionState.DISABLED);
			}
		});
		getSubForm().addSubFormToolListener(cloneListener);
		
		// add TransferHandler
		subform.getJTable().setTransferHandler(getSubFormTransferHandler());
		if (subform.getJTable().getParent() instanceof JViewport) {
			((JViewport)subform.getJTable().getParent()).setTransferHandler(getSubFormTransferHandler());
		}
		
		setupSubFormTableContextMenue();
		
	}
	
		
	private void setupSubFormTableContextMenue() {
		
		// context menu:
		final JPopupMenuFactory factory = new JPopupMenuFactory() {
			@Override
            public JPopupMenu newJPopupMenu() {
				final JPopupMenu result = new JPopupMenu();
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

				return result;
			}
		};
		JPopupMenuListener popupMenuListener = new DefaultJPopupMenuListener(factory, true) {
			@Override
			public void mousePressed(MouseEvent ev) {
				if (ev.getClickCount() == 1 && SwingUtilities.isRightMouseButton(ev)) {					
					if (ev.getSource() instanceof JTable) {
						final int iRow = ((JTable)ev.getSource()).rowAtPoint(ev.getPoint());
						if (iRow >= 0) {
							if (!((JTable)ev.getSource()).getSelectionModel().isSelectedIndex(iRow)) {
								if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
									((JTable)ev.getSource()).getSelectionModel().addSelectionInterval(iRow, iRow);
								}
								else {
									((JTable)ev.getSource()).getSelectionModel().setSelectionInterval(iRow, iRow);
								}
							}  // if

						
							final int iSelectedRowCount = ((JTable)ev.getSource()).getSelectedRowCount();
							String entityName = MasterDataSubFormController.this.getSelectedCollectable().getCollectableEOEntity().getName();
							
							try {
								miDetails.setEnabled(iSelectedRowCount == 1
									&& getSelectedCollectable().getId() != null
									&& (Integer)getSelectedCollectable().getId() > 0 && !LayoutUtils.isSubformEntity(entityName));
							}
							catch(Exception e) {
								miDetails.setEnabled(false);
							}
						}
						
					}
				}
				super.mousePressed(ev);
			}
		};
		
		this.getSubForm().getJTable().addMouseListener(popupMenuListener);
	}
	
	private void cmdShowDetails() throws CommonBusinessException {
		UIUtils.runCommand(getParent(), new CommonRunnable() {

			@Override
			public void run() throws CommonBusinessException {
				String sEntity = MasterDataSubFormController.this.getSelectedCollectable().getCollectableEOEntity().getName();
				
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
		MasterDataCollectController ctlMasterdata = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(MainFrame.getPredefinedEntityOpenLocation(entityName), entityName, null);
		ctlMasterdata.runViewSingleCollectableWithId(iMasterdataId);
	}
	
	private void showGenericobject(final Object iGenericObjectId) throws CommonFinderException, CommonPermissionException {
		//final Object iGenericObjectId = clct.getId();
		try {
			final GenericObjectVO govo = GenericObjectDelegate.getInstance().get((Integer) iGenericObjectId);
			final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
					newGenericObjectCollectController(MainFrame.getPredefinedEntityOpenLocation(MetaDataClientProvider.getInstance().getEntity(Integer.valueOf(govo.getModuleId()).longValue()).getEntity()), govo.getModuleId(), null);

			ctlGenericObject.runViewSingleCollectable(CollectableGenericObjectWithDependants.newCollectableGenericObject(govo));
		}
		catch(CommonFatalException ex){
			throw new CommonFatalException(CommonLocaleDelegate.getMessage("DynamicEntitySubFormController.2", "Der Datensatz kann nicht angezeigt werden. Bitte tragen Sie in der Datenquelle für die dynamische Entität, die Entität ein, die angezeigt werden soll!"));
		}
	}
	
	/**
	 * 
	 * @param referenceEntity
	 * @param referenceId
	 * @return true if new clct is inserted
	 * @throws NuclosBusinessException is thrown if reference entity could not be found in columns
	 */
	public boolean insertNewWithReference(String referenceEntity, Collectable referenceClct, boolean validateWithVLP) throws NuclosBusinessException {
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
                        clctlov.addLookupListener(new LookupListener() {
        					@Override
        					public void lookupSuccessful(LookupEvent ev) {
        						SubForm.transferLookedUpValues(ev.getSelectedCollectable(), getSubFormTableModel(), isSearchable(), row, getSubForm().getTransferLookedUpValueActions(field));
        					}

							@Override
                            public int getPriority() {
	                            return 1;
                            }
        				});
                        try {
                            clctlov.acceptLookedUpCollectable(referenceClct);
                            clct.setField(clctlov.getFieldName(), clctlov.getField());
                            clctChanged = true;
                        }
                        catch(Exception e1) {
                            log.error(e1.getMessage(), e1);
                        }
                    } 
                }
                catch(CommonBusinessException e) {
                	log.error(e.getMessage(), e);
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
	
	/**
	 * 
	 * @return TransferHandler for handling drop's and paste events
	 */
	protected TransferHandler getSubFormTransferHandler() {
		if (subFormTransferHandler == null)
			subFormTransferHandler = new TransferHandler(){

			@Override
            public boolean importData(JComponent comp, Transferable t) {
				try {
					if (!canImport(comp, t.getTransferDataFlavors())) 
						return false;
					
					int countNotImported = 0;
            		int countImported = 0;
            		boolean noReferenceFound = false;
            		String entityLabel = null;
            		
	                final List<?> lstloim = (List<?>) t.getTransferData(TransferableGenericObjects.dataFlavor);
	                for (Object o : lstloim) {
	                	if (o instanceof GenericObjectIdModuleProcess) {
	                		GenericObjectIdModuleProcess goimp = (GenericObjectIdModuleProcess) o;
	                		Integer entityId = goimp.getModuleId();
	                		String entity = MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)).getEntity();
	                		entityLabel = CommonLocaleDelegate.getLabelFromMetaDataVO(MetaDataClientProvider.getInstance().getEntity(LangUtils.convertId(entityId)));
	                		
                            try {
                            	if (!insertNewWithReference(entity, new CollectableGenericObjectWithDependants(GenericObjectDelegate.getInstance().getWithDependants(goimp.getGenericObjectId())), true)) {
    	                			countNotImported++;
    	                		} else {
    	                			countImported++;
    	                		}
                            }
                            catch(NuclosBusinessException e2) {
                            	noReferenceFound = true;
                            }
                            catch(CommonBusinessException e) {
	                            log.error(e.getMessage(), e);
                            }
	                	}
	                }
	                
	                if (noReferenceFound) {
	                	new Bubble(
	                		getSubForm().getJTable(),
	                		CommonLocaleDelegate.getMessage("MasterDataSubFormController.4", "Dieses Unterformular enthält keine Referenzspalte zur Entität ${entity}.", entityLabel),
							10,
							Bubble.Position.NO_ARROW_CENTER)
						.setVisible(true);
	                } else {
	                	String sNotImported = CommonLocaleDelegate.getMessage("MasterDataSubFormController.5", "Der Valuelist Provider verhindert das Anlegen von ${count} Unterformular Datensätzen.", countNotImported);
		                
		                getCollectController().getDetailsPanel().setStatusBarText(
		                	CommonLocaleDelegate.getMessage("MasterDataSubFormController.6", "${count} Unterformular Datensätze angelegt.", countImported)
		                	+ (countNotImported==0?"": " " + sNotImported));
		                if (countNotImported!=0)
		                	new Bubble(
		                		getCollectController().getDetailsPanel().tfStatusBar,
		                		sNotImported,
								10,
								Bubble.Position.UPPER)
							.setVisible(true);
	                }
	                return true;
                }
                catch(UnsupportedFlavorException e) {
                    log.error(e.getMessage(), e);
	                return false;
                }
                catch(IOException e) {
                    log.error(e.getMessage(), e);
	                return false;
                }
            }

			@Override
            public boolean canImport(JComponent comp,
                DataFlavor[] transferFlavors) {
				return isEnabled() && transferFlavors.length > 0 && TransferableGenericObjects.dataFlavor.equals(transferFlavors[0]);
            }
			
		};
		
		return subFormTransferHandler;
	}
	
	private SubFormToolListener cloneListener = new SubFormToolListener() {
		@Override
		public void toolbarAction(String actionCommand) {
			if(actionCommand.equals(TB_CLONE)) {
				if (getJTable().getSelectedRow() != -1) {
					stopEditing();
					
					// clone selected record
					CollectableEntityObject original = getCollectableTableModel().getCollectable(getJTable().getSelectedRow());
					CollectableEntityObject clone = new CollectableEntityObject(original.getCollectableEOEntity(), original.getEntityObjectVO().copy());
					
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
						String sMessage = CommonLocaleDelegate.getMessage("MasterDataSubFormController.2", "Der zu klonende Datensatz besitzt abh\u00e4ngige Unterformulardaten. Sollen diese auch geklont werden?");
						result = JOptionPane.showConfirmDialog(getParent(), sMessage, CommonLocaleDelegate.getMessage("MasterDataSubFormController.1", "Datensatz klonen"), JOptionPane.YES_NO_OPTION);
					}
		
					try {
						// add cloned data
						CollectableEntityObject clctNew = insertNewRow(clone);
						setParentId(clctNew, getParentId());
						getCollectableTableModel().add(clctNew);
						
						// clone and add dependant data
						if (result == JOptionPane.YES_OPTION) {
							cloneDependantData(original, clone);
						}
					}
					catch (NuclosBusinessException e) {
						throw new CommonFatalException(e);
					}
				}
			}
		}
	};
	
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
					CollectableEntityObject clone = new CollectableEntityObject(clctmd.getCollectableEOEntity(), clctmd.getEntityObjectVO().copy());
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
		                    insertNewWithReference(clctef.getReferencedEntityName(), clct, false);
	                    }
	                    catch(NuclosBusinessException e) {
		                    log.error(e.getMessage(), e);
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
	
	protected CollectableEOEntity getCollectableMasterDataEntity() {
		if(this.getCollectableEntity() instanceof CollectableGenericObjectEntity) {
			CollectableGenericObjectEntity cgoe = (CollectableGenericObjectEntity)this.getCollectableEntity();
			CollectableEOEntityProvider provider = new CollectableEOEntityProvider(MetaDataClientProvider.getInstance());
			return (CollectableEOEntity)provider.getCollectableEntity(cgoe.getName());
		}
		else if(this.getCollectableEntity() instanceof CollectableMasterDataEntity) {
			CollectableMasterDataEntity cmde = (CollectableMasterDataEntity)this.getCollectableEntity();
			CollectableEOEntityProvider provider = new CollectableEOEntityProvider(MetaDataClientProvider.getInstance());
			return (CollectableEOEntity)provider.getCollectableEntity(cmde.getName());
		}
		return (CollectableEOEntity) this.getCollectableEntity();
	}

	/**
	 * @return a new collectable adapter object containing a MasterDataVO
	 */
	@Override
	public CollectableEntityObject newCollectable() {
		final CollectableEOEntity clctmde = this.getCollectableMasterDataEntity();		
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
	public void fillSubForm(Object oParentId) throws NuclosBusinessException {
		this.setParentId(oParentId);

		final Collection<EntityObjectVO> collmdvo = (oParentId == null) ?
				new ArrayList<EntityObjectVO>() :
				MasterDataDelegate.getInstance().getDependantMasterData(this.getCollectableEntity().getName(), this.getForeignKeyFieldName(), oParentId);

		this.fillSubForm(collmdvo);
	}

	/**
	 * fills this subform with data in collection.
	 * @param collmdvo
	 * @throws NuclosBusinessException
	 */
	public void fillSubForm(final Collection<EntityObjectVO> collmdvo) throws NuclosBusinessException {
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
					new CollectableEntityObject.MakeCollectable(MasterDataSubFormController.this.getCollectableMasterDataEntity()));
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
				for (MasterDataSubFormController controller : getChildSubFormController()) {
					CollectableEntityObject clct = getSelectedCollectable();
					controller.fillAsSubFormChild(clct);
					controller.selectFirstRow();
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
		getJTable().getSelectionModel().removeListSelectionListener(this.rowselectionlistener);
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
	private void fillAsSubFormChild(final CollectableEntityObject clct) {		
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				MasterDataSubFormController.this.setCollectableParent(clct);
				
				List<CollectableEntityObject> lstclctmd = readDependants(clct);
				final boolean bWasDetailsChangedIgnored = getCollectController().isDetailsChangedIgnored();
				getCollectController().setDetailsChangedIgnored(true);
				try {
					MasterDataSubFormController.this.updateTableModel(lstclctmd);
				}
				finally {
					getCollectController().setDetailsChangedIgnored(bWasDetailsChangedIgnored);
				}
				//}
				// if subform contains no data, clear all dependant subforms as well
				if (lstclctmd == null) {
					for (MasterDataSubFormController subformcontroller : getChildSubFormController()) {
						subformcontroller.fillAsSubFormChild(null);
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
				child.fillAsSubFormChild(data);
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
						MasterDataDelegate.getInstance().getDependantMasterData(this.getCollectableEntity().getName(), this.getForeignKeyFieldName(), clct.getId());
					
					lstclctmd = CollectionUtils.transform(collmdvo, new CollectableEntityObject.MakeCollectable(this.getCollectableMasterDataEntity()));
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
			String sMessage = CommonLocaleDelegate.getMessage("MasterDataSubFormController.3", "Es kann kein Bezug zu einem \u00dcbergeordneten Datensatz hergestellt werden. "+
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
	protected void removeSelectedRow() {
		// this is necessary for rows that have been added and again removed before saving
		this.getSelectedCollectable().markRemoved();
		super.removeSelectedRow();
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
				MasterDataSubFormController.super.clear();
				clearChildSubFormController();
			}
		});
	}

	public void setParentController(EntityCollectController<CollectableEntityObject> parentController) {
		this.parentController = parentController;
	}
	
	
}	// class MasterDataSubFormController
