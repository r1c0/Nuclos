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

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.nuclos.client.common.DetailsSubFormController;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.entityobject.CollectableEntityObject;
import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.GeneratorDelegate;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
import org.nuclos.client.masterdata.ui.GenerationRulesPanel;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectPanel;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelAdapter;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModelEvent;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collect.collectable.CollectableValueIdField;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;


/**
 * <code>MasterDataCollectController</code> for entity "generation".
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenerationCollectController extends MasterDataCollectController {

	private static final Logger LOG = Logger.getLogger(GenerationCollectController.class);

	private GenerationRulesController generationRulesController;

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton}
	 * to get an instance.
	 *
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GenerationCollectController(MainFrameTab tabIfAny) {
		super(NuclosEntity.GENERATION, tabIfAny);
	}

	@Override
	protected void initialize(CollectPanel<CollectableMasterDataWithDependants> pnlCollect) {
		super.initialize(pnlCollect);
		getDetailsComponentModel("sourceModule").addCollectableComponentModelListener(new CollectableComponentModelAdapter() {
			@Override
			public void collectableFieldChangedInModel(CollectableComponentModelEvent ev) {
				Integer newValue = (Integer) ev.getNewValue().getValueId();
				boolean enabled = false;
				if (newValue != null && MetaDataClientProvider.getInstance().getEntity(IdUtils.toLongId(newValue)).isStateModel()) {
					enabled = true;
				}
				DetailsSubFormController<CollectableEntityObject> sfctl = getDetailsSubforms().get(NuclosEntity.GENERATIONUSAGE.getEntityName());
				if (!enabled) {
					for (CollectableEntityObject clct : new ArrayList<CollectableEntityObject>(sfctl.getCollectables())) {
						sfctl.getCollectableTableModel().remove(clct);
					}
				}
				sfctl.getSubForm().setEnabled(enabled);
			}
		});
		// todo: Add listener to target module combo box to enable/disable details check box
		//getSelectedCollectable().getField("targetModule").   //?!
	}

	@Override
	public void setupEditPanelForDetailsTab() {
		super.setupEditPanelForDetailsTab();

		GenerationRulesPanel grp = new GenerationRulesPanel();
		generationRulesController = new GenerationRulesController(grp, getTab(), this);

		final JPanel pnlRules = (JPanel) UIUtils.findJComponent(this.getDetailsPanel(), "pnlRules");
		if (pnlRules != null) {
			pnlRules.removeAll();
			pnlRules.setLayout(new BorderLayout());
			pnlRules.add(grp, BorderLayout.CENTER);
		}
	}

	@Override
	protected void unsafeFillDetailsPanel(CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		if (clct.getField("showobject").getValue() == null)
			clct.setField("showobject", new CollectableValueField(Boolean.TRUE));
		if (clct.getField("refreshsrcobject").getValue() == null)
			clct.setField("refreshsrcobject", new CollectableValueField(Boolean.FALSE));
		
		super.unsafeFillDetailsPanel(clct);
		
		try {
			generationRulesController.setRuleUsages(GeneratorDelegate.getInstance().getRuleUsages((Integer) clct.getId()));
		} catch (CommonPermissionException e) {
			throw new NuclosBusinessException(e.getMessage(), e);
		}
	}

	/**
	 * invalidates the generator actions cache after successful insert.
	 * @param clctNew
	 * @return
	 * @throws NuclosBusinessException
	 */
	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		final DependantMasterDataMap mpmdvoDependants = org.nuclos.common.Utils.clearIds(this.getAllSubFormData(null).toDependantMasterDataMap());
		Boolean grouping = LangUtils.defaultIfNull((Boolean)clctNew.getField("groupattributes").getValue(), Boolean.FALSE);
		validateAttributes(grouping, mpmdvoDependants.getValues(NuclosEntity.GENERATIONATTRIBUTE.getEntityName()));
		validateSubformAttributes(mpmdvoDependants.getValues(NuclosEntity.GENERATIONSUBENTITY.getEntityName()));

		final CollectableMasterDataWithDependants result = super.insertCollectable(clctNew);
		GeneratorDelegate.getInstance().updateRuleUsages((Integer) result.getId(), generationRulesController.getRuleUsages());

		GeneratorActions.invalidateCache();

		return result;
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oDependantData) throws CommonBusinessException {
		final DependantMasterDataMap mpDependantMasterData = getAllSubFormData(clct.getId()).toDependantMasterDataMap();
		Boolean grouping = LangUtils.defaultIfNull((Boolean)clct.getField("groupattributes").getValue(), Boolean.FALSE);
		validateAttributes(grouping, mpDependantMasterData.getValues(NuclosEntity.GENERATIONATTRIBUTE.getEntityName()));
		validateSubformAttributes(mpDependantMasterData.getValues(NuclosEntity.GENERATIONSUBENTITY.getEntityName()));

		final CollectableMasterDataWithDependants result = super.updateCollectable(clct, oDependantData);
		GeneratorDelegate.getInstance().updateRuleUsages((Integer) clct.getId(), generationRulesController.getRuleUsages());
		GeneratorActions.invalidateCache();
		return result;
	}

	private void validateSubformAttributes(Collection<MasterDataVO> coll) throws CommonBusinessException {
		for(MasterDataVO vo : coll) {
			if(vo.getField("groupAttributes") != null && (Boolean)vo.getField("groupAttributes") == false)
				continue;

			DependantMasterDataMap mp = vo.getDependants();
			for(MasterDataVO voAttribute : mp.getAllValues()) {
				if(voAttribute.getField("subentityAttributeGrouping") == null){
					throw new CommonBusinessException(getSpringLocaleDelegate().getMessage("GenerationCollectController.2",
					"Sie müssen Gruppierungsfunktionen für die Unterformular Attribute angeben!"));
				}
				String sGroup = (String)voAttribute.getField("subentityAttributeGrouping");
				if(sGroup == null || sGroup.length() < 1){
					throw new CommonBusinessException(getSpringLocaleDelegate().getMessage("GenerationCollectController.2",
						"Sie müssen Gruppierungsfunktionen für die Unterformular Attribute angeben!"));
				}
			}
		}
	}

	private void validateAttributes(boolean grouping, Collection<MasterDataVO> coll) throws CommonBusinessException {
		HashSet<String> stAllTargetAttributes = new HashSet<String>(coll.size());

		for(MasterDataVO mdVOTarget : coll){
			if (mdVOTarget.isRemoved())
				continue;

			String sTargetAttribute = (String) mdVOTarget.getField("attributeTarget");
			if( !(stAllTargetAttributes.add(sTargetAttribute)) ){
				throw new CommonBusinessException(getSpringLocaleDelegate().getMessage("GenerationCollectController.1",
					"Das Zielattribut '{0}' darf nicht mehrfach vorkommen. W\u00e4hlen Sie bitte ein anderes Zielattribut aus.", sTargetAttribute));
			}

			boolean hasSourceType = mdVOTarget.getField("sourceType") != null;
			boolean hasFunction = mdVOTarget.getField("groupfunction") != null;
			if (grouping && !hasSourceType && !hasFunction) {
				throw new CommonBusinessException("GenerationCollectController.groupfunction.mandatory");
			}
			else if (hasFunction && !grouping) {
				throw new CommonBusinessException("GenerationCollectController.groupfunction.notallowed");
			}
			else if (hasFunction && hasSourceType) {
				throw new CommonBusinessException("GenerationCollectController.groupfunction.notallowed.sourcetype");
			}
		}
	}

	public void runWithNewCollectableWithSomeFields(MasterDataVO vo) {
		try {
			this.runNew();
			for(CollectableComponent cc : this.getDetailCollectableComponentsFor("sourceModule")) {
				CollectableValueIdField valueId = new CollectableValueIdField(vo.getField("sourceModuleId"),vo.getField("sourceModule"));
				cc.setField(valueId);
			}
			for(CollectableComponent cc : this.getDetailCollectableComponentsFor("targetModule")) {
				CollectableValueIdField valueId = new CollectableValueIdField(vo.getField("targetModuleId"),vo.getField("targetModule"));
				cc.setField(valueId);
			}
		}
		catch(CommonBusinessException e) {
			LOG.warn("runWithNewCollectableWithSomeFields: " + e);
		}
	}

	/**
	 * invalidates the generator actions cache after successful delete.
	 * @throws org.nuclos.common.NuclosBusinessException
	 */
	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		super.deleteCollectable(clct);

		GeneratorActions.invalidateCache();
	}

	public void detailsChanged(Component cSource) {
		super.detailsChanged(cSource);
	}

}	// class GenerationCollectController
