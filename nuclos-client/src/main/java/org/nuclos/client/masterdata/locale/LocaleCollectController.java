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
package org.nuclos.client.masterdata.locale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.masterdata.MasterDataSubFormController;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.collect.component.model.CollectableComponentModel;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosEntity;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class LocaleCollectController extends MasterDataCollectController {

	private static final String F_PARENT = "parent";
	private static final String F_RESOURCEID = "resourceID";
	private static final String F_TEXT = "text";
	private static final String F_LOCALETEXT = "localetext";

	Collection<String> collresid;

	LocaleDelegate delegate = LocaleDelegate.getInstance();


	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public LocaleCollectController(JComponent parent, MainFrameTab tabIfAny) {
		super(parent, NuclosEntity.LOCALE, tabIfAny);
	}

	/**
	 * You should use {@link org.nuclos.client.ui.collect.CollectControllerFactorySingleton} 
	 * to get an instance.
	 * 
	 * @deprecated You should normally do sth. like this:<code><pre>
	 * ResultController<~> rc = new ResultController<~>();
	 * *CollectController<~> cc = new *CollectController<~>(.., rc);
	 * </code></pre>
	 */
	public LocaleCollectController(JComponent parent, Collection<String> collresids, MainFrameTab tabIfAny) {
		this(parent, tabIfAny);
		this.collresid = collresids;
	}

	
	public void runViewSingleCollectableWithLocale(LocaleInfo localeInfo) throws CommonBusinessException{
		this.runViewSingleCollectableWithId(localeInfo.localeId);
	}

	
	public Integer getCurrentCollectableId() {
		if (this.getCollectState().isDetailsModeNew()) {
			return null;
		}
		else {
			return (Integer)getSelectedCollectableId();
		}
	}

	@Override
	protected CollectableMasterDataWithDependants insertCollectable(CollectableMasterDataWithDependants clctNew) throws CommonBusinessException {
		if (clctNew.getId() != null) {
			throw new IllegalArgumentException("clctNew");
		}

		final MasterDataVO mdvoInserted = this.mddelegate.create(this.getEntityName(), clctNew.getMasterDataCVO(), new DependantMasterDataMap());

		return new CollectableMasterDataWithDependants(clctNew.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoInserted, this.readDependants(mdvoInserted.getId())));
	}

	@Override
	protected CollectableMasterDataWithDependants updateCurrentCollectable(CollectableMasterDataWithDependants clctCurrent) throws CommonBusinessException {
		return this.updateCollectable(clctCurrent, null);
	}

	@Override
	protected CollectableMasterDataWithDependants updateCollectable(CollectableMasterDataWithDependants clct, Object oAdditionalData) throws CommonBusinessException {
	   final Object oId = delegate.update(clct.getMasterDataCVO(), this.getAllSubFormData(clct.getId()).toDependantMasterDataMap());

	   final MasterDataVO mdvoUpdated = this.mddelegate.get(this.getEntityName(), oId);

	   return new CollectableMasterDataWithDependants(clct.getCollectableEntity(), new MasterDataWithDependantsVO(mdvoUpdated, this.readDependants(mdvoUpdated.getId())));
	}


	/**
	 * @param clct
	 * @throws CommonBusinessException
	 */
	@Override
	protected void unsafeFillDetailsPanel(CollectableMasterDataWithDependants clct) throws NuclosBusinessException {
		for (String sFieldName : this.getDetailsPanel().getLayoutRoot().getOrderedFieldNames()) {
			log.debug("sFieldName = " + sFieldName);

			// iterate over the models rather than over the components:
			final CollectableComponentModel clctcompmodel = this.getDetailsPanel().getLayoutRoot().getCollectableComponentModelFor(sFieldName);
			clctcompmodel.setField(clct.getField(sFieldName));
		}

		CollectableComponent parentComp = CollectionUtils.getFirst(getDetailCollectableComponentsFor(F_PARENT));
		try {
			String parent = (String) parentComp.getField().getValue();
			loadLocaleResources(parent, (Integer)clct.getId());
		}
		catch (CommonBusinessException ex) {
			throw new NuclosBusinessException(ex.getMessage(), ex);
		}
	}


	private void loadLocaleResources(String sParentLocale, Integer iLocale) throws CommonFinderException, CommonPermissionException, NuclosBusinessException {
		if (!(iLocale == null && sParentLocale == null)) {
			if (this.collresid != null && !this.collresid.isEmpty()) {
				this.fillLocaleResourceSubForm(this.getResourcesForDefaultLocaleByIntId(collresid));
			}
			else {
				this.fillLocaleResourceSubForm(this.getLocaleResourcesFor(sParentLocale, iLocale));
			}
		}
	}

	public void fillLocaleResourceSubForm(Collection<LocaleResource> collres)  throws NuclosBusinessException {
		((MasterDataSubFormController)this.getSubFormController(NuclosEntity.LOCALERESOURCE.getEntityName())).fillSubForm(getLocaleResourceAsVO(collres));
	}

	/**
	 * make <code>LocaleResource</code> as <code>MasterDataVO</code>
	 * @param collres collection of <code>LocaleResource</code> objects
	 * @return collection of <code>MasterDataVO</code>
	 */
	private Collection<MasterDataVO> getLocaleResourceAsVO(Collection<LocaleResource> collres){
		List<MasterDataVO> result = new ArrayList<MasterDataVO>();
		for (LocaleResource lr : collres) {
			MasterDataVO mdvo = lr.getMasterDataVO();
			mdvo.setField(F_RESOURCEID, lr.getResourceId());
			mdvo.setField(F_TEXT, lr.getResource());
			mdvo.setField(F_LOCALETEXT, lr.getTranslatedResource());
			result.add(mdvo);
		}
		return result;
	}


	/**
	 *
	 * @param iParentLocale
	 * @param iLocale
	 * @return
	 */
	private Collection<LocaleResource> getLocaleResourcesFor(String sParentLocale, Integer iLocale) {
		Collection<LocaleResource> result = new ArrayList<LocaleResource>();
		/*
		for (MasterDataVO mdvo_parent : LocaleDelegate.getInstance().getLocaleVO(LocaleInfo)) {
			String translationText = delegate.getResourceByStringId(iLocale, (String)mdvo_parent.getField(F_RESOURCEID));
			result.add(new LocaleResource(mdvo_parent, (String)mdvo_parent.getField(F_RESOURCEID), (String)mdvo_parent.getField(F_TEXT), translationText));
		}
		for (MasterDataVO mdvo : delegate.getLocaleResourcesFor(iLocale)) {
			if (!this.exist(result, (String)mdvo.getField(F_RESOURCEID))) {
				result.add(new LocaleResource(mdvo, (String)mdvo.getField(F_RESOURCEID), null, (String)mdvo.getField(F_TEXT)));
			}
		}
		*/
		return result;//Collections.sort(result);
	}

	/**
	 * this method is used only for default locale,
	 * if locale CollectController has been invoked from another CollectController (e.g. AttributeCollectController)
	 * @param collIds
	 * @return
	 */
	private Collection<LocaleResource> getResourcesForDefaultLocaleByIntId(Collection<String> collIds) {
		Collection<LocaleResource> result = new ArrayList<LocaleResource>();
		for (MasterDataVO mdvo : delegate.getDefaultResourcesAsVO(collIds)) {
			String parentText = delegate.getDefaultResource((String)mdvo.getField(F_RESOURCEID));
			result.add(new LocaleResource(mdvo, (String)mdvo.getField(F_RESOURCEID), parentText, (String)mdvo.getField(F_TEXT)));
		}
		return result;
	}

	public class LocaleResource {
		private MasterDataVO mdvo;
		private String sResourceId;
		private String sResource;
		private String sTranslatedResource;

		LocaleResource(MasterDataVO mdvo, String sResourceId, String sResource, String sTranslatedResource) {
			this.mdvo = mdvo;
			this.sResourceId = sResourceId;
			this.sResource = sResource;
			this.sTranslatedResource = sTranslatedResource;

		}

		public MasterDataVO getMasterDataVO() {
			return this.mdvo;
		}

		public void setMasterDataVO(MasterDataVO mdvo) {
			this.mdvo = mdvo;
		}

		public String getResourceId() {
			return this.sResourceId;
		}

		public String getResource() {
			return this.sResource;
		}

		public String getTranslatedResource() {
			return this.sTranslatedResource;
		}

		public void setResourceId(String sReosurceId) {
			this.sResourceId = sReosurceId;
		}

		public void setResourceText(String sResource) {
			this.sResource = sResource;
		}

		public void setTranslatedResource(String sTranslatedResource) {
			this.sTranslatedResource = sTranslatedResource;
		}
	}
}
