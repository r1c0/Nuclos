//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.client.ui.collect.result;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JTable;

import org.apache.log4j.Logger;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.ui.collect.PivotController;
import org.nuclos.client.ui.collect.PivotPanel;
import org.nuclos.client.ui.collect.SelectFixedColumnsController;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableUtils;
import org.nuclos.common.collect.collectable.DefaultCollectableEntityProvider;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.IdUtils;

/**
 * A specialization of ResultController for use with an {@link GenericObjectCollectController}.
 * <p>
 * At present the feature to include rows from a subform in the base entity result list 
 * is only available for GenericObjects. The support for finding the support fields is 
 * implemented in {@link #getFieldsAvailableForResult}. 
 * </p>
 * @author Thomas Pasch
 * @since Nuclos 3.1.01
 */
public class GenericObjectResultController<Clct extends CollectableGenericObjectWithDependants> extends NuclosResultController<Clct> {
	
	private static final Logger LOG = Logger.getLogger(GenericObjectResultController.class);
	
	/**
	 * subform name -> pivot information.
	 * <p>
	 * If a subform is included in this list, {@link #getFieldsAvailableForResult} will provide a
	 * pivot representation instead of a subform field representation.
	 * </p> 
	 */
	private final Map<String,List<PivotInfo>> pivots;
	
	public GenericObjectResultController(CollectableEntity clcte, ISearchResultStrategy<Clct> srs) {
		super(clcte, srs);
		pivots = new HashMap<String, List<PivotInfo>>();
	}
	
	public void putPivotInfo(String subform, List<PivotInfo> info) {
		pivots.put(subform, CollectionUtils.copyWithoutDublicates(info));
	}
	
	public List<PivotInfo> getPivotInfo(String subformName) {
		return pivots.get(subformName);
	}
	
	public void removePivotInfo(String subformName) {
		pivots.remove(subformName);
	}
	
	/**
	 * Adds the ability to select subforms as pivot columns in the result list view.
	 */
	@Override
	public SelectFixedColumnsController newSelectColumnsController(Component parent) {
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		
		// retrieve sub form fields
		final Map<String, Map<String, EntityFieldMetaDataVO>> subFormFields = new HashMap<String, Map<String,EntityFieldMetaDataVO>>();
		final String entityName = getEntity().getName();
		final EntityMetaDataVO entityMd = mdProv.getEntity(entityName);
		final Set<String> subforms = GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(
				IdUtils.unsafeToId(entityMd.getId()));
		
		for (String subform: subforms) {
			final EntityFieldMetaDataVO key = mdProv.getPivotKeyField(entityName, subform);
			if (key != null) {
				final Map<String, EntityFieldMetaDataVO> map = mdProv.getAllEntityFieldsByEntity(subform);
				subFormFields.put(subform, map);
			}
			else {
				// remove subforms with unsuited key fields from state
				pivots.remove(subform);
			}
		}
		
		return new PivotController(parent, new PivotPanel(getEntity().getName(), subFormFields, pivots), this);
		// Old (pre-pivot) columns controller.
		// return super.newSelectColumnsController(parent);
	}
	
	private final class GetCollectableEntityFieldForResult implements Transformer<String, CollectableEntityField> {
		private final CollectableEntity clcte;

		public GetCollectableEntityFieldForResult(CollectableEntity clcte) {
			this.clcte = clcte;
		}

		@Override
		public CollectableEntityField transform(String sFieldName) {
			return getCollectableEntityFieldForResult(clcte, sFieldName);
		}
	}

	/**
	 * This methods adds (pivot, subform, and parent key) field inclusion to result display.
	 * 
	 * @param clcte
	 * @return the fields of the given entity, plus the fields of all subentities for that entity.
	 */
	@Override
	public SortedSet<CollectableEntityField> getFieldsAvailableForResult(Comparator<CollectableEntityField> comp) {
		final SortedSet<CollectableEntityField> result = super.getFieldsAvailableForResult(comp);
		final GenericObjectCollectController controller = getGenericObjectCollectController();

		// add subentities' fields, if any:
		final Set<String> stSubEntityNames = GenericObjectMetaDataCache.getInstance().getSubFormEntityNamesByModuleId(controller.getModuleId());
		final Set<String> stSubEntityLabels = new HashSet<String>();
		
		for (String sSubEntityName : stSubEntityNames) {
			if (pivots.containsKey(sSubEntityName)) {
				getFieldsAvaibleInPivotSubform(result, sSubEntityName);
			}
			else {
				getFieldsAvaibleInSubform(result, stSubEntityLabels, sSubEntityName);
			}
		}
		return result;
	}
	
	private void getFieldsAvaibleInSubform(SortedSet<CollectableEntityField> result, Set<String> stSubEntityLabels, String sSubEntityName) {
		final CollectableEntity clcteSub = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(sSubEntityName);
		// WORKAROUND for general search: We don't want duplicate entities (assetcomment, ordercomment etc.), so we
		// ignore entities with duplicate labels:
		// TODO: eliminate this workaround 
		final String sSubEntityLabel = clcteSub.getLabel();
		if (!stSubEntityLabels.contains(sSubEntityLabel)) {
			stSubEntityLabels.add(sSubEntityLabel);
			result.addAll(CollectionUtils.transform(clcteSub.getFieldNames(), new GetCollectableEntityFieldForResult(clcteSub)));
		}
	}

	private void getFieldsAvaibleInPivotSubform(SortedSet<CollectableEntityField> result, String sSubEntityName) {
		final List<PivotInfo> plist = getPivotInfo(sSubEntityName);
		final List<String> valueColumns = new ArrayList<String>(plist.size());
		for (PivotInfo p: plist) {
			valueColumns.add(p.getValueField());
		}
		
		final Collection<EntityFieldMetaDataVO> fields = MetaDataClientProvider.getInstance().getAllPivotEntityFields(plist.get(0), valueColumns);
		for (EntityFieldMetaDataVO md: fields) {
			result.add(new CollectableEOEntityField(md, sSubEntityName));
		}
	}
	
	/**
	 * reads the selected fields and their entities from the user preferences.
	 * @param clcte
	 * @return the list of previously selected fields
	 */
	@Override
	protected List<? extends CollectableEntityField> readSelectedFieldsFromPreferences() {
		final List<? extends CollectableEntityField> result = getWorkspaceUtils().getCollectableEntityFieldsForGenericObject(
				getGenericObjectCollectController().getEntityPreferences());
		CollectionUtils.removeDublicates(result);
		
		// recover pivots state
		for (CollectableEntityField f: result) {
			if (f instanceof CollectableEOEntityField) {
				final CollectableEOEntityField field = (CollectableEOEntityField) f;
				final PivotInfo pinfo = field.getMeta().getPivotInfo();
				if (pinfo != null) {
					List<PivotInfo> plist = pivots.get(pinfo.getSubform());
					if (plist == null) {
						plist = new ArrayList<PivotInfo>();
						pivots.put(pinfo.getSubform(), plist);
					}
					if (!plist.contains(pinfo)) {
						plist.add(pinfo);
					}
				}
			}
		}
			
		return result;
	}
	
	@Override
	protected void writeSelectedFieldsAndWidthsToPreferences(
			EntityPreferences entityPreferences, 
			List<? extends CollectableEntityField> lstclctefSelected, Map<String, Integer> mpWidths) {
		getWorkspaceUtils().setCollectableEntityFieldsForGenericObject(entityPreferences, lstclctefSelected, getFieldWidthsForPreferences(),
				CollectableUtils.getFieldNamesFromCollectableEntityFields(getNuclosResultPanel().getFixedColumns()));
	}

	/**
	 * @deprecated We *must* get rid of this!
	 */
	@Override
	public CollectableEntityField getCollectableEntityFieldForResult(CollectableEntity sClcte, String sFieldName) {
		// TODO: Find out why the following condition does *not* hold:
		// (Perhaps because sClcte is a subform and sFieldName is a field of the subform???)
		// assert getEntity().equals(sClcte);
		final GenericObjectCollectController controller = getGenericObjectCollectController();
		final CollectableEntity ce = controller.getCollectableEntity();
		CollectableEntityFieldWithEntity.QualifiedEntityFieldName qFieldName = new CollectableEntityFieldWithEntity.QualifiedEntityFieldName(sFieldName);
		if(qFieldName.isQualifiedEntityFieldName()){
			String clcteName = qFieldName.getEntityName();
			if(clcteName != null && !clcteName.equals(ce.getName()))
				sClcte = DefaultCollectableEntityProvider.getInstance().getCollectableEntity(clcteName);
		}
		return GenericObjectClientUtils.getCollectableEntityFieldForResult(sClcte, qFieldName.getFieldName(), ce);
	}
	
	/**
	 * sets all column widths to user preferences; set optimal width if no preferences yet saved
	 * Unfinalized in the CollectController
	 * @param tbl
	 * @deprecated Remove this.
	 */
	@Override
	public void setColumnWidths(final JTable tbl) {
		final GenericObjectCollectController controller = getGenericObjectCollectController();
		if(controller.getSearchResultTemplateController() == null || controller.getSearchResultTemplateController().isSelectedDefaultSearchResultTemplate()) {
			super.setColumnWidths(tbl);
		}
	}

	public GenericObjectCollectController getGenericObjectCollectController() {
		return (GenericObjectCollectController) getCollectController();
	}

}
