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
package org.nuclos.client.genericobject;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectControllerFactory;
import org.nuclos.client.common.Utils;
import org.nuclos.client.datasource.DatasourceDelegate;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelProvider;
import org.nuclos.client.ui.popupmenu.DefaultJPopupMenuListener;
import org.nuclos.client.ui.popupmenu.JPopupMenuFactory;
import org.nuclos.client.ui.popupmenu.JPopupMenuListener;
import org.nuclos.client.valuelistprovider.cache.CollectableFieldsProviderCache;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.SearchConditionUtils;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.genericobject.searchcondition.CollectableGenericObjectSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;

/**
 * <code>MasterDataSubFormController</code> for dynamic entities.
 * This is a read-only subform, where every line represents a set of attributes from a leased object,
 * which can be opened via context menu.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:uwe.allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */

public class DynamicEntitySubFormController extends MasterDataSubFormController {

	private JMenuItem miDetails = new JMenuItem(CommonLocaleDelegate.getMessage("AbstractCollectableComponent.7","Details anzeigen..."));
	private JMenuItem miDefineAsNewSearchResult = new JMenuItem(CommonLocaleDelegate.getMessage("DynamicEntitySubFormController.1", "In Liste anzeigen"));

	public DynamicEntitySubFormController(Component parent, JComponent parentMdi,
			CollectableComponentModelProvider clctcompmodelprovider, String sParentEntityName, SubForm subform,
			Preferences prefsUserParent, CollectableFieldsProviderCache valueListProviderCache) {
		super(parent, parentMdi, clctcompmodelprovider, sParentEntityName, subform, prefsUserParent, valueListProviderCache);


		setupDetailsStuff();
	}

	private void setupDetailsStuff() {
		final JTable dynamicTable = this.getSubForm().getJTable();
		final JTable fixedTable = this.getSubForm().getSubformRowHeader().getHeaderTable();

		dynamicTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		fixedTable.setEnabled(false);
		fixedTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		this.getSubForm().setEnabled(false);

		// double click:
		MouseAdapter mouseAdapter = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev) && ev.getClickCount() == 2) {
					if (DynamicEntitySubFormController.this.getSelectedCollectable() != null) {
						cmdShowDetails();
					}
				}
			}
		};
		dynamicTable.addMouseListener(mouseAdapter);
		fixedTable.addMouseListener(mouseAdapter);

		// context menu:
		final JPopupMenuFactory factory = new JPopupMenuFactory() {
			@Override
            public JPopupMenu newJPopupMenu() {
				final JPopupMenu result = new JPopupMenu();
				miDetails.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent ev) {
						cmdShowDetails();
					}
				});
				miDefineAsNewSearchResult.addActionListener(new ActionListener() {
					@Override
                    public void actionPerformed(ActionEvent ev) {
						cmdDefineSelectedCollectablesAsNewSearchResult();
					}
				});
				result.add(miDetails);
				result.addSeparator();
				result.add(miDefineAsNewSearchResult);
				return result;
			}
		};
		JPopupMenuListener popupMenuListener = new DefaultJPopupMenuListener(factory, true) {
			@Override
			public void mousePressed(MouseEvent ev) {
				if (ev.getClickCount() == 1) {
					// select current row before opening the menu:
					/** @todo factor out this default selection behavior if possible */
					if (ev.getSource() instanceof JTable) {
						final int iRow = ((JTable)ev.getSource()).rowAtPoint(ev.getPoint());
						if (iRow >= 0) {
//							 Nur, wenn nicht selektiert, selektieren:
							if (!((JTable)ev.getSource()).getSelectionModel().isSelectedIndex(iRow)) {
								if ((ev.getModifiers() & MouseEvent.CTRL_MASK) != 0) {
									// Control gedr\u00fcckt:
									// Zeile zur Selektion hinzuf\u00fcgen:
									((JTable)ev.getSource()).getSelectionModel().addSelectionInterval(iRow, iRow);
								}
								else {
									// Sonst nur diese Zeile selektieren:
									((JTable)ev.getSource()).getSelectionModel().setSelectionInterval(iRow, iRow);
								}
							}  // if


						}
						final int iSelectedRowCount = ((JTable)ev.getSource()).getSelectedRowCount();
						miDetails.setEnabled(iSelectedRowCount == 1
							&& getSelectedCollectable().getId() != null
							&& (Integer)getSelectedCollectable().getId() > 0);
						miDefineAsNewSearchResult.setEnabled(iSelectedRowCount > 1 ? true : false);
					}
				}
				super.mousePressed(ev);
			}
		};
		dynamicTable.addMouseListener(popupMenuListener);
		fixedTable.addMouseListener(popupMenuListener);
	}

	private void cmdShowDetails() {
		UIUtils.runCommand(getParent(), new CommonRunnable() {
			@Override
            public void run() throws CommonBusinessException {
				final Collectable clct = getSelectedCollectable();
				assert clct != null;
				String entityName = null;

				entityName = getEntityNameForDyamicLookup(entityName);
				if (StringUtils.isNullOrEmpty(entityName)) {
					GenericObjectVO govo = GenericObjectDelegate.getInstance().get((Integer) clct.getId());
					entityName = MetaDataClientProvider.getInstance().getEntity(Integer.valueOf(govo.getModuleId()).longValue()).getEntity();
				}
				MainFrameTab tab = UIUtils.getInternalFrameForComponent(getSubForm().getJTable());
				CollectController<?> controller = Main.getMainController().getControllerForInternalFrame(tab);

				Main.getMainController().showDetails(entityName, clct.getId(), false, controller);
			}
		});
	}

	private void cmdDefineSelectedCollectablesAsNewSearchResult() {
		UIUtils.runCommand(getParent(), new CommonRunnable(){
			@Override
            public void run() throws CommonBusinessException {
				final Collection<Collectable> collclct = CollectionUtils.typecheck(getSelectedCollectables(), Collectable.class);

				assert CollectionUtils.isNonEmpty(collclct);
				final CollectableSearchCondition cond = getCollectableSearchCondition(collclct);

				String entityName = null;
				entityName = getEntityNameForDyamicLookup(entityName);

				if(entityName != null) {
					if(MetaDataClientProvider.getInstance().getEntity(entityName).isStateModel()) {
						showGenericobjectInResult(collclct, cond);
					}
					else {
						showMasterDataInResult(cond, entityName);
					}
				}
				else {
					showGenericobjectInResult(collclct, cond);
				}
			}

			private void showMasterDataInResult(
				final CollectableSearchCondition cond, String entityName)
				throws CommonBusinessException {
				MasterDataCollectController ctlMasterdata = NuclosCollectControllerFactory.getInstance().newMasterDataCollectController(getParentMdi(), entityName, null);
				ctlMasterdata.runViewResults(cond);
			}

			private void showGenericobjectInResult(final Collection<Collectable> collclct, final CollectableSearchCondition cond)
				throws CommonPermissionException, CommonBusinessException {
				try {
					final Integer iModuleId = getCommonModuleId(collclct);
					final GenericObjectCollectController ctlGenericObject = NuclosCollectControllerFactory.getInstance().
							newGenericObjectCollectController(getParentMdi(), iModuleId, null);
					ctlGenericObject.setSearchDeleted(CollectableGenericObjectSearchExpression.SEARCH_BOTH);
					ctlGenericObject.runViewResults(cond);
				}
				catch(CommonFatalException ex){
					throw new CommonFatalException(CommonLocaleDelegate.getMessage("DynamicEntitySubFormController.2", "Der Datensatz kann nicht angezeigt werden. Bitte tragen Sie in der Datenquelle für die dynamische Entität, die Entität ein, die angezeigt werden soll!"));
				}
			}
		});
	}

	private String getEntityNameForDyamicLookup(String entityName)
		throws CommonBusinessException {
		CollectableEntity colEntity = DynamicEntitySubFormController.this.getCollectableEntity();
		if(colEntity instanceof CollectableMasterDataEntity) {
			CollectableMasterDataEntity masterDataEntity = (CollectableMasterDataEntity)colEntity;
			MasterDataMetaVO voMeta = masterDataEntity.getMasterDataMetaCVO();
			String entity = voMeta.getEntityName();
			if(entity != null) {
				entity = entity.substring(4, entity.length());
				DynamicEntityVO voDyn = DatasourceDelegate.getInstance().getDynamicEntityByName(entity);
				entityName = voDyn.getEntity();
			}
		}
		return entityName;
	}

	/**
	 * @param collclct
	 * @return
	 * @precondition !CollectionUtils.isNullOrEmpty(collclct)
	 */
	private static CollectableSearchCondition getCollectableSearchCondition(Collection<Collectable> collclct) {
		final Collection<Object> collIds = CollectionUtils.transform(collclct, new Transformer<Collectable, Object>() {
			@Override
            public Object transform(Collectable clct) {
				return clct.getId();
			}
		});

		return SearchConditionUtils.getCollectableSearchConditionForIds(collIds);
	}

	/**
	 @param collclct
	 * @return the module id shared by all collectables, if any.
	 */
	private static Integer getCommonModuleId(Collection<Collectable> collclct) throws CommonPermissionException{
		return Utils.getCommonObject(CollectionUtils.transform(collclct, new Transformer<Collectable, Integer>() {
			@Override
            public Integer transform(Collectable clct) {
				try {
					return GenericObjectDelegate.getInstance().get((Integer)clct.getId()).getModuleId();
				}catch(CommonBusinessException ex){
					return null;
				}

			}
		}));
	}

}	// class DynamicEntitySubFormController
