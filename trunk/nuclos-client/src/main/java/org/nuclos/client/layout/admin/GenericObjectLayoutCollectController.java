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
package org.nuclos.client.layout.admin;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.swing.JComponent;

import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.valuelistprovider.GenericObjectCollectableFieldsProviderFactory;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels;
import org.nuclos.client.layout.wysiwyg.CollectableWYSIWYGLayoutEditor.WYSIWYGDetailsComponentModel;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.client.masterdata.valuelistprovider.MasterDataCollectableFieldsProviderFactory;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.client.ui.layoutml.LayoutRoot;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.xml.sax.InputSource;
/**
 * Controller for collecting the layouts for generic object dialogs.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class GenericObjectLayoutCollectController extends LayoutCollectController {

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public GenericObjectLayoutCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.LAYOUT, tabIfAny);
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		clctNew.setField("layoutML", new CollectableValueField(this.getLayoutMLFromEditor()));
		
		final CollectableMasterDataWithDependants result = super.insertCollectable(clctNew);

		GenericObjectLayoutCache.getInstance().invalidate();
		MasterDataDelegate.getInstance().invalidateLayoutCache();

		return result;
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oDependantData) throws CommonBusinessException {
		clct.setField("layoutML", new CollectableValueField(this.getLayoutMLFromEditor()));
		
		final CollectableMasterDataWithDependants result = super.updateCollectable(clct, oDependantData);

		GenericObjectLayoutCache.getInstance().invalidate();
		MasterDataDelegate.getInstance().invalidateLayoutCache();

		return result;
	}

	@Override
	protected void deleteCollectable(CollectableMasterDataWithDependants clct) throws CommonBusinessException {
		super.deleteCollectable(clct);	

		GenericObjectLayoutCache.getInstance().invalidate();
		MasterDataDelegate.getInstance().invalidateLayoutCache();
	}

	@Override
	protected LayoutRoot parseLayoutML() throws CommonBusinessException, IOException {
		final String sLayoutML = this.getLayoutMLFromEditor();
		if (sLayoutML == null) {
			throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("GenericObjectLayoutCollectController.1","Die LayoutML-Definition ist leer."));
		}
		final LayoutMLParser parser = new LayoutMLParser();
		
//		final CollectableGenericObjectEntityForAllAttributes clcte = CollectableGenericObjectEntityForAllAttributes.getInstance(Modules.ENTITYNAME_GENERALSEARCH);
		final EntityMetaDataVO eMeta = MetaDataClientProvider.getInstance().getEntity(getUsedEntityName());
		final String label = CommonLocaleDelegate.getText(eMeta.getLocaleResourceIdForLabel(), null);
		final Map<String, EntityFieldMetaDataVO> mapEfMeta = MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(eMeta.getEntity());		
		
		if (eMeta.isStateModel()) {
			final CollectableGenericObjectEntity clcte = new CollectableGenericObjectEntity(eMeta.getEntity(), label, mapEfMeta.keySet());
			return parser.getResult(new InputSource(new StringReader(sLayoutML)),
				clcte, false, null, GenericObjectCollectableFieldsProviderFactory.newFactory(clcte.getName()), CollectableComponentFactory.getInstance());
		} else {
			final CollectableEntity clcte = NuclosCollectableEntityProvider.getInstance().getCollectableEntity(this.getUsedEntityName());
			return parser.getResult(new InputSource(new StringReader(sLayoutML)),
				clcte, false, null, MasterDataCollectableFieldsProviderFactory.newFactory(clcte.getName()), CollectableComponentFactory.getInstance());
		}
	}

	/**
	 * @return the entity name the layout is used for, as specified in the layouts's usages. If there are several
	 * usages, the first one is taken.
	 * @throws CommonBusinessException if there are no usages defined for this layout.
	 * @postcondition result != null
	 */
	@Override
	protected String getUsedEntityName() throws CommonBusinessException {
		String entityName = null;
		
		if (this.getSelectedCollectableId() != null) {
			for (EntityObjectVO mdvo : MasterDataDelegate.getInstance().getDependantMasterData(getLayoutUsageEntity().getEntityName(), "layout", this.getSelectedCollectableId())) {
				entityName = mdvo.getField("entity", String.class);
				break;
			}
		}
		if(entityName == null) {
			for (Collectable clct : this.getSubFormController(getLayoutUsageEntity().getEntityName()).getCollectables()) {
				entityName = (String) clct.getValue("entity");
			}
		}
		
		if (entityName == null) {
			//NUCLEUSINT-261
			throw new CommonBusinessException(WYSIWYGStringsAndLabels.ERROR_MESSAGES.MISSING_ASSIGNMENT_MD);
		}

		assert entityName != null;
		return entityName;
	}

	@Override
	protected NuclosEntity getLayoutUsageEntity() {
		return NuclosEntity.LAYOUTUSAGE;
	}

	@Override
	public void setMetaInformation(WYSIWYGDetailsComponentModel wysiwygModel) {
		try {
			CollectableEntity entity = NuclosCollectableEntityProvider.getInstance().getCollectableEntity(getUsedEntityName());
			wysiwygModel.setCollectableEntity(entity);
		} catch (CommonBusinessException e) {
			// do nothing
		}
	}

}	// class GenericObjectLayoutCollectController
