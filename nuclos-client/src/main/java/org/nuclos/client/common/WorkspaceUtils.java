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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;

import org.apache.commons.httpclient.util.LangUtils;
import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.security.SecurityCache;
import org.nuclos.client.entityobject.CollectableEOEntityClientProvider;
import org.nuclos.client.genericobject.CollectableGenericObjectEntity;
import org.nuclos.client.genericobject.GenericObjectClientUtils;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.masterdata.MasterDataDelegate;
import org.nuclos.common.Actions;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.CollectableEntityFieldWithEntityForExternal;
import org.nuclos.common.NuclosEOField;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.WorkspaceDescription;
import org.nuclos.common.WorkspaceDescription.ColumnPreferences;
import org.nuclos.common.WorkspaceDescription.ColumnSorting;
import org.nuclos.common.WorkspaceDescription.EntityPreferences;
import org.nuclos.common.WorkspaceDescription.SubFormPreferences;
import org.nuclos.common.WorkspaceDescription.TablePreferences;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Predicate;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common.entityobject.CollectableEOEntityProvider;
import org.nuclos.common.genericobject.CollectableGenericObjectEntityField;
import org.nuclos.common.masterdata.CollectableMasterDataEntity;
import org.nuclos.common.masterdata.CollectableMasterDataForeignKeyEntityField;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;

public class WorkspaceUtils {
	
	private static final Logger LOG = Logger.getLogger(WorkspaceUtils.class);
	
	
	/**
	 * New Fields are added
	 * 
	 * @param ep
	 * @return selected columns with fixed
	 */
	public static List<String> getSelectedColumns(EntityPreferences ep) {
		return addNewColumns(getSelectedColumns(ep.getResultPreferences()), ep);
	}
	/**
	 * New Fields are added
	 * 
	 * @param sfp
	 * @return selected columns with fixed
	 */
	public static List<String> getSelectedColumns(SubFormPreferences sfp) {
		return addNewColumns(getSelectedColumns(sfp.getTablePreferences()), sfp);
	}
	/**
	 * @param tp
	 * @return selected columns with fixed
	 */
	private static List<String> getSelectedColumns(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, String>() {
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static List<String> getSelectedEntities(EntityPreferences ep) {
		return getSelectedEntities(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public static List<String> getSelectedEntities(SubFormPreferences sfp) {
		return getSelectedEntities(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private static List<String> getSelectedEntities(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, String>() {
					@Override
					public String transform(ColumnPreferences i) {
						return i.getEntity();
					}
				});
	}

	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static List<Integer> getFixedWidths(EntityPreferences ep) {
		return getFixedWidths(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public static List<Integer> getFixedWidths(SubFormPreferences sfp) {
		return getFixedWidths(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private static List<Integer> getFixedWidths(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, Integer>(){
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
					;
				});
	}
	
	
	/**
	 * New Fields are added
	 * 
	 * @param ep
	 * @return selected columns WITHOUT fixed
	 */
	public static List<String> getSelectedWithoutFixedColumns(EntityPreferences ep) {
		return addNewColumns(getSelectedWithoutFixedColumns(ep.getResultPreferences()), ep);
	}
	/**
	 * New Fields are added
	 * 
	 * @param sfp
	 * @return selected columns WITHOUT fixed
	 */
	public static List<String> getSelectedWithoutFixedColumns(SubFormPreferences sfp) {
		return addNewColumns(getSelectedWithoutFixedColumns(sfp.getTablePreferences()), sfp);
	}
	/**
	 * @param tp
	 * @return selected columns WITHOUT fixed
	 */
	private static List<String> getSelectedWithoutFixedColumns(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return !t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, String>(){
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
					;
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static List<String> getFixedColumns(EntityPreferences ep) {
		return getFixedColumns(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public static List<String> getFixedColumns(SubFormPreferences sfp) {
		return getFixedColumns(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private static List<String> getFixedColumns(TablePreferences tp) {
		return CollectionUtils.transform(
				CollectionUtils.select(tp.getSelectedColumnPreferences(),
						new Predicate<ColumnPreferences>() {
							@Override
							public boolean evaluate(ColumnPreferences t) {
								return t.isFixed();
							}
				}), 
				new Transformer<ColumnPreferences, String>(){
					@Override
					public String transform(ColumnPreferences i) {
						return i.getColumn();
					}
					;
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return selected columns widths (incl. fixed)
	 */
	public static List<Integer> getColumnWidths(EntityPreferences ep) {
		return getColumnWidths(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return selected column widths (incl. fixed)
	 */
	public static List<Integer> getColumnWidths(SubFormPreferences sfp) {
		return getColumnWidths(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return selected column widths (incl. fixed)
	 */
	private static List<Integer> getColumnWidths(TablePreferences tp) {
		return CollectionUtils.transform(tp.getSelectedColumnPreferences(), 
				new Transformer<ColumnPreferences, Integer>() {
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static List<Integer> getColumnWidthsWithoutFixed(EntityPreferences ep) {
		return getColumnWidthsWithoutFixed(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public static List<Integer> getColumnWidthsWithoutFixed(SubFormPreferences sfp) {
		return getColumnWidthsWithoutFixed(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private static List<Integer> getColumnWidthsWithoutFixed(TablePreferences tp) {
		return CollectionUtils.transform(CollectionUtils.select(tp.getSelectedColumnPreferences(), 
				new Predicate<ColumnPreferences>() {
					@Override
					public boolean evaluate(ColumnPreferences t) {
						return !t.isFixed();
					}
				}), 
				new Transformer<ColumnPreferences, Integer>() {
					@Override
					public Integer transform(ColumnPreferences i) {
						return i.getWidth();
					}
				});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static Map<String, Integer> getColumnWidthsMap(EntityPreferences ep) {
		return getColumnWidthsMap(ep.getResultPreferences());
	}
	/**
	 * 
	 * @param sfp
	 * @return
	 */
	public static Map<String, Integer> getColumnWidthsMap(SubFormPreferences sfp) {
		return getColumnWidthsMap(sfp.getTablePreferences());
	}
	/**
	 * 
	 * @param tp
	 * @return
	 */
	private static Map<String, Integer> getColumnWidthsMap(TablePreferences tp) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			result.put(cp.getColumn(), cp.getWidth());
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param ciResolver
	 * @return
	 */
	public static List<SortKey> getSortKeys(EntityPreferences ep, final IColumnIndexRecolver ciResolver) {
		return getSortKeys(ep.getResultPreferences(), ciResolver);
	}
	/**
	 * 
	 * @param sfp
	 * @param ciResolver
	 * @return
	 */
	public static List<SortKey> getSortKeys(SubFormPreferences sfp, final IColumnIndexRecolver ciResolver) {
		return getSortKeys(sfp.getTablePreferences(), ciResolver);
	}
	/**
	 * 
	 * @param tp
	 * @param ciResolver
	 * @return
	 */
	private static List<SortKey> getSortKeys(TablePreferences tp, final IColumnIndexRecolver ciResolver) {
		return CollectionUtils.transform(tp.getColumnSortings(), 
				new Transformer<ColumnSorting, SortKey>(){
					@Override
					public SortKey transform(ColumnSorting i) {
						return new SortKey(ciResolver.getColumnIndex(i.getColumn()),
								i.isAsc()?SortOrder.ASCENDING:SortOrder.DESCENDING);
					}
		});
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @return
	 */
	public static List<CollectableEntityField> getSelectedFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		return getSelectedFields(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @return
	 */
	public static List<CollectableEntityField> getSelectedFields(SubFormPreferences sfp, List<CollectableEntityField> fields) {
		return getSelectedFields(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 * @return
	 */
	private static List<CollectableEntityField> getSelectedFields(TablePreferences tp, List<CollectableEntityField> fields) {
		List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			for (CollectableEntityField clctef : fields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName())) {
					result.add(clctef);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @return
	 */
	public static List<CollectableEntityField> getSelectedWithoutFixedFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		return getSelectedWithoutFixedFields(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @return
	 */
	public static List<CollectableEntityField> getSelectedWithoutFixedFields(SubFormPreferences sfp, List<CollectableEntityField> fields) {
		return getSelectedWithoutFixedFields(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 * @return
	 */
	private static List<CollectableEntityField> getSelectedWithoutFixedFields(TablePreferences tp, List<CollectableEntityField> fields) {
		List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		for (ColumnPreferences cp : CollectionUtils.select(tp.getSelectedColumnPreferences(),
				new Predicate<ColumnPreferences>() {
					@Override
					public boolean evaluate(ColumnPreferences t) {
						return !t.isFixed();
					}
				})) {
			for (CollectableEntityField clctef : fields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName())) {
					result.add(clctef);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param selectedFields
	 * @return
	 */
	public static Set<CollectableEntityField> getFixedFields(EntityPreferences ep, List<CollectableEntityField> selectedFields) {
		return getFixedFields(ep.getResultPreferences(), selectedFields);
	}
	/**
	 * 
	 * @param sfp
	 * @param selectedFields
	 * @return
	 */
	public static Set<CollectableEntityField> getFixedFields(SubFormPreferences sfp, List<CollectableEntityField> selectedFields) {
		return getFixedFields(sfp.getTablePreferences(), selectedFields);
	}
	/**
	 * 
	 * @param tp
	 * @param selectedFields
	 * @return
	 */
	private static Set<CollectableEntityField> getFixedFields(TablePreferences tp, List<CollectableEntityField> selectedFields) {
		Set<CollectableEntityField> result = new HashSet<CollectableEntityField>();
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			for (CollectableEntityField clctef : selectedFields) {
				if (LangUtils.equals(cp.getColumn(), clctef.getName()) 
						&& cp.isFixed()) {
					result.add(clctef);
				}
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @return
	 */
	public static List<? extends CollectableEntityField> getCollectableEntityFieldsForGenericObject(EntityPreferences ep) {
		final List<CollectableEntityField> result = new ArrayList<CollectableEntityField>();
		
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		final CollectableEOEntityProvider ceeoProv = CollectableEOEntityClientProvider.getInstance();
		
		for (ColumnPreferences cp : ep.getResultPreferences().getSelectedColumnPreferences()) {
			try {
				switch (cp.getType()) {
				
				case ColumnPreferences.TYPE_EOEntityField:

					if (cp.getPivotSubForm() == null) {
						final EntityFieldMetaDataVO efMeta = mdProv.getEntityField(cp.getEntity(), cp.getColumn());
						result.add(new CollectableEOEntityField(efMeta, cp.getEntity()));
					} else {
						result.add(getPivotField(cp));
					}
					
					break;
					
				case ColumnPreferences.TYPE_GenericObjectEntityField:
					result.add(CollectableGenericObjectEntity.getByModuleId(
							mdProv.getEntity(cp.getEntity()).getId().intValue()).getEntityField(cp.getColumn()));
					break;
					
				case ColumnPreferences.TYPE_MasterDataForeignKeyEntityField:
					CollectableMasterDataForeignKeyEntityField clctef = 
						new CollectableMasterDataForeignKeyEntityField(
							MasterDataDelegate.getInstance().getMetaData(cp.getEntity()).getField(cp.getColumn()), cp.getEntity());
					result.add(clctef);
					clctef.setCollectableEntity(new CollectableMasterDataEntity(MasterDataDelegate.getInstance().getMetaData(cp.getEntity())));
					break;
					
				case ColumnPreferences.TYPE_EntityFieldWithEntityForExternal:
					result.add(GenericObjectClientUtils.getCollectableEntityFieldForResult(
							ceeoProv.getCollectableEntity(cp.getEntity()), 
							cp.getColumn(), 
							ceeoProv.getCollectableEntity(ep.getEntity())));
					break;
					
				case ColumnPreferences.TYPE_EntityFieldWithEntity:
					result.add(new CollectableEntityFieldWithEntity(
							ceeoProv.getCollectableEntity(cp.getEntity()), 
							cp.getColumn()));
					break;
					
				default:
					result.add(GenericObjectClientUtils.getCollectableEntityFieldForResult(
							ceeoProv.getCollectableEntity(cp.getEntity()==null?ep.getEntity():cp.getEntity()), 
							cp.getColumn(), 
							ceeoProv.getCollectableEntity(ep.getEntity())));
				}
			} catch (Exception ex) {
				LOG.error("Column could not be restored " + cp, ex); 
			}
		}
		
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
		
			// do not add columns first time...
			if (result.isEmpty() && ep.getResultPreferences().getHiddenColumns().isEmpty()) {
				return result;
			}
			// add new columns
			try {
				for (EntityFieldMetaDataVO efMeta : CollectionUtils.sorted( // order by intid
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(ep.getEntity()).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								return o1.getId().compareTo(o2.getId());
							}
						})) {
					if (NuclosEOField.getByField(efMeta.getField()) != null) {
						// do not add system fields
						continue;
					}
					for (CollectableEntityField clctef : result) {
						if (LangUtils.equals(clctef.getName(), efMeta.getField())) {
							// field already selected
							continue;
						}
					}
					if (ep.getResultPreferences().getHiddenColumns().contains(efMeta.getField())) {
						// field is hidden
						continue;
					}
					
					// field is new
					result.add(new CollectableGenericObjectEntityField(
							AttributeCache.getInstance().getAttribute(ep.getEntity(), efMeta.getField()),
							efMeta,
							ep.getEntity()));
				}
			} catch (Exception ex) {
				LOG.error("New columns not added", ex);
			} 
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param cp
	 * @return
	 * @throws Exception
	 */
	private static CollectableEOEntityField getPivotField(ColumnPreferences cp) throws Exception {
		final MetaDataClientProvider mdProv = MetaDataClientProvider.getInstance();
		final PivotInfo pi = new PivotInfo(cp.getPivotSubForm(), cp.getPivotKeyField(), cp.getPivotValueField(), Class.forName(cp.getPivotValueType()));
		EntityFieldMetaDataVO rightField = null;
		for (EntityFieldMetaDataVO f : mdProv.getAllPivotEntityFields(pi, Collections.singletonList(pi.getValueField()))) {
			if (f.getField().equals(cp.getColumn()) && f.getPivotInfo().equals(pi)) {
				rightField = f;
				break;
			}
		}
		if (rightField == null) {
			throw new Exception("No pivot field found for " + pi);
		} 
		return new CollectableEOEntityField(rightField, cp.getEntity());
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param selectedFields
	 * @param fieldWidths 
	 */
	public static void setCollectableEntityFieldsForGenericObject(EntityPreferences ep, List<? extends CollectableEntityField> selectedFields, List<Integer> fieldWidths, List<String> fixedFields) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
		
			ep.getResultPreferences().removeAllSelectedColumnPreferences();
			
			final List<CollectableEntityField> fixedEfs = new ArrayList<CollectableEntityField>();
			final List<CollectableEntityField> normalEfs = new ArrayList<CollectableEntityField>();
			// fixed before normal columns...
			for (CollectableEntityField clctef : selectedFields) {
				if (fixedFields.contains(clctef.getName())) {
					fixedEfs.add(clctef);
				} else {
					normalEfs.add(clctef);
				}
			}
			
			final List<CollectableEntityField> efs = new ArrayList<CollectableEntityField>();
			efs.addAll(fixedEfs);
			efs.addAll(normalEfs);
			
			for (int i = 0; i < efs.size(); i++) {
				final CollectableEntityField clctef = efs.get(i); 
				final ColumnPreferences cp = new ColumnPreferences();
				cp.setColumn(clctef.getName());
				cp.setEntity(clctef.getEntityName());
				cp.setFixed(i < fixedEfs.size());
				
				if (clctef instanceof CollectableEOEntityField) {
					cp.setType(ColumnPreferences.TYPE_EOEntityField);
					PivotInfo pi = ((CollectableEOEntityField) clctef).getMeta().getPivotInfo();
					if (pi != null) {
						cp.setPivotSubForm(pi.getSubform());
						cp.setPivotKeyField(pi.getKeyField());
						cp.setPivotValueField(pi.getValueField());
						cp.setPivotValueType(pi.getValueType().getName());
					}
				} else if (clctef instanceof CollectableGenericObjectEntityField) {
					cp.setType(ColumnPreferences.TYPE_GenericObjectEntityField);
				} else if (clctef instanceof CollectableMasterDataForeignKeyEntityField) {
					cp.setType(ColumnPreferences.TYPE_MasterDataForeignKeyEntityField);
				} else if (clctef instanceof CollectableEntityFieldWithEntityForExternal) {
					cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntityForExternal);
				} else if (clctef instanceof CollectableEntityFieldWithEntity) {
					cp.setType(ColumnPreferences.TYPE_EntityFieldWithEntity);
				}
				
				if (fieldWidths.size() > i) {
					cp.setWidth(fieldWidths.get(i));
				}
				
				ep.getResultPreferences().addSelectedColumnPreferences(cp);
			}
		}
	}
	
	
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @param fieldWidths
	 */
	public static void addFixedColumns(SubFormPreferences sfp, List<String> fields, List<Integer> fieldWidths) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
				for (int i = 0; i < fields.size(); i++) {
				if (fieldWidths.size() > i) {
					ColumnPreferences cp = new ColumnPreferences();
					cp.setFixed(true);
					cp.setColumn(fields.get(i));
					cp.setWidth(fieldWidths.get(i));
					sfp.getTablePreferences().addSelectedColumnPreferencesInFront(cp);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 * @param fieldWidths
	 */
	public static void setColumnPreferences(EntityPreferences ep, List<String> fields, List<Integer> fieldWidths) {
		setColumnPreferences(ep.getResultPreferences(), fields, fieldWidths);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 * @param fieldWidths
	 */
	public static void setColumnPreferences(SubFormPreferences sfp, List<String> fields, List<Integer> fieldWidths) {
		setColumnPreferences(sfp.getTablePreferences(), fields, fieldWidths);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 */
	private static void setColumnPreferences(TablePreferences tp, List<String> fields, List<Integer> fieldWidths) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
			tp.removeAllSelectedColumnPreferences();
			for (int i = 0; i < fields.size(); i++) {
				ColumnPreferences cp = new ColumnPreferences();
				cp.setColumn(fields.get(i));
				if (fieldWidths.size()>i) {
					cp.setWidth(fieldWidths.get(i));
				} else {
					cp.setWidth(75);
				}
				
				tp.addSelectedColumnPreferences(cp);
				
				// remove from hidden
				if (tp.getHiddenColumns().contains(cp.getColumn())) {
					tp.removeHiddenColumn(cp.getColumn());
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 */
	public static void updateFixedColumns(EntityPreferences ep, List<String> fields) {
		updateFixedColumns(ep.getResultPreferences(), fields);
	}
	/**
	 * 
	 * @param sfp
	 * @param fields
	 */
	public static void updateFixedColumns(SubFormPreferences sfp, List<String> fields) {
		updateFixedColumns(sfp.getTablePreferences(), fields);
	}
	/**
	 * 
	 * @param tp
	 * @param fields
	 */
	private static void updateFixedColumns(TablePreferences tp, List<String> fields) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
			for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
				if (fields.contains(cp.getColumn())) {
					cp.setFixed(true);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param sortKeys
	 * @param cnResolver
	 */
	public static void setSortKeys(EntityPreferences ep, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		setSortKeys(ep.getResultPreferences(), sortKeys, cnResolver);
	}
	/**
	 * 
	 * @param sfp
	 * @param sortKeys
	 * @param cnResolver
	 */
	public static void setSortKeys(SubFormPreferences sfp, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		setSortKeys(sfp.getTablePreferences(), sortKeys, cnResolver);
	}
	/**
	 * 
	 * @param tp
	 * @param sortKeys
	 * @param cnResolver
	 */
	private static void setSortKeys(TablePreferences tp, List<? extends SortKey> sortKeys, IColumnNameResolver cnResolver) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
			tp.removeAllColumnSortings();
			for (SortKey sortKey : sortKeys) {
				if (sortKey.getSortOrder() == SortOrder.UNSORTED)
					continue;
				if (sortKey.getColumn() == -1)
					continue;
				ColumnSorting cs = new ColumnSorting();
				cs.setColumn(cnResolver.getColumnName(sortKey.getColumn()));
				cs.setAsc(sortKey.getSortOrder() == SortOrder.ASCENDING);
				tp.addColumnSorting(cs);
			}
		}
	}
	
	
	public static void removeColumnSorting(TablePreferences tp, String column) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
			for (ColumnSorting cs : tp.getColumnSortings()) {
				if (LangUtils.equals(cs.getColumn(), column)) {
					tp.removeColumnSorting(cs);
				}
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param entity
	 */
	public static void validatePreferences(EntityPreferences ep) {
		validatePreferences(ep.getResultPreferences(), ep.getEntity());
	}
	/**
	 * 
	 * @param sfp
	 * @param entity
	 */
	public static void validatePreferences(SubFormPreferences sfp) {
		validatePreferences(sfp.getTablePreferences(), sfp.getEntity());
	}
	/**
	 * 
	 * @param tp
	 * @param entity
	 */
	private static void validatePreferences(TablePreferences tp, String entity) {
		// special entities with custom collect controller
		if (NuclosEntity.getByName(entity) != null) {
			switch (NuclosEntity.getByName(entity)) {
			case RULE :
			case TIMELIMITRULE : 
			case CODE :
				return;
			}
		}
		
		for (ColumnPreferences cp : tp.getSelectedColumnPreferences()) {
			try {	
				if (cp.getPivotSubForm() == null) {
					MetaDataClientProvider.getInstance().getEntityField(
						cp.getEntity()!=null?cp.getEntity():entity, 
								cp.getColumn());
				} else {
					MetaDataClientProvider.getInstance().getEntityField(
							cp.getPivotSubForm(), cp.getPivotKeyField());
					MetaDataClientProvider.getInstance().getEntityField(
							cp.getPivotSubForm(), cp.getPivotValueField());
				}
			} catch (Exception e) {
				tp.removeSelectedColumnPreferences(cp);
				removeColumnSorting(tp, cp.getColumn());
			}
		}
		for (String hidden : tp.getHiddenColumns()) {
			try {
				MetaDataClientProvider.getInstance().getEntityField(entity, hidden);
			} catch (Exception e) {
				tp.removeHiddenColumn(hidden);
			}
		}
	}
	
	
	/**
	 * 
	 * @param selectedFields
	 * @param ep
	 * @return
	 */
	private static List<String> addNewColumns(final List<String> selectedFields, final EntityPreferences ep) {
		return addNewColumns(selectedFields, ep.getResultPreferences(), ep.getEntity());
	}
	/**
	 * 
	 * @param selectedFields
	 * @param sfp
	 * @return
	 */
	private static List<String> addNewColumns(final List<String> selectedFields, final SubFormPreferences sfp) {
		return addNewColumns(selectedFields, sfp.getTablePreferences(), sfp.getEntity());
	}
	/**
	 * 
	 * @param selectedFields
	 * @param tp
	 * @param entity
	 * @return
	 */
	private static List<String> addNewColumns(final List<String> selectedFields, final TablePreferences tp, final String entity) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
		
			// do not add columns first time...
			if (selectedFields.isEmpty() && tp.getHiddenColumns().isEmpty()) {
				return selectedFields;
			}
			
			try {
				for (EntityFieldMetaDataVO efMeta : CollectionUtils.sorted( // order by intid
						MetaDataClientProvider.getInstance().getAllEntityFieldsByEntity(entity).values(),
						new Comparator<EntityFieldMetaDataVO>() {
							@Override
							public int compare(EntityFieldMetaDataVO o1, EntityFieldMetaDataVO o2) {
								return o1.getId().compareTo(o2.getId());
							}
						})) {
					if (NuclosEOField.getByField(efMeta.getField()) != null) {
						// do not add system fields
						continue;
					}
					if (selectedFields.contains(efMeta.getField())) {
						// field already selected
						continue;
					}
					if (tp.getHiddenColumns().contains(efMeta.getField())) {
						// field is hidden
						continue;
					}
					
					// field is new
					selectedFields.add(efMeta.getField());
				}
			} catch (Exception ex) {
				// not a meta data entity
			} 
		}
		return selectedFields;
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param column
	 */
	public static void addHiddenColumn(EntityPreferences ep, String column) {
		addHiddenColumn(ep.getResultPreferences(), column);
	}
	/**
	 * 
	 * @param sfp
	 * @param column
	 */
	public static void addHiddenColumn(SubFormPreferences sfp, String column) {
		addHiddenColumn(sfp.getTablePreferences(), column);
	}
	/**
	 * 
	 * @param tp
	 * @param column
	 */
	private static void addHiddenColumn(TablePreferences tp, String column) {
		if (SecurityCache.getInstance().isActionAllowed(Actions.ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS)
				|| !MainFrame.getWorkspace().isAssigned()) {
			
			tp.addHiddenColumn(column);
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @param fields
	 */
	public static void addMissingPivotFields(EntityPreferences ep, List<CollectableEntityField> fields) {
		for (ColumnPreferences cp : ep.getResultPreferences().getSelectedColumnPreferences()) {
			try {
				CollectableEOEntityField pivotField = getPivotField(cp);
				if (!fields.contains(pivotField))
					fields.add(pivotField);
			} catch (Exception e) {
				LOG.error("Column could not be restored " + cp, e); 
			}
		}
	}
	
	
	/**
	 * 
	 * @param ep
	 * @throws CommonBusinessException 
	 */
	public static void restoreEntityPreferences(EntityPreferences ep) throws CommonBusinessException {
		final Long assignedWorkspaceId = MainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = getPrefsFacade().getWorkspace(assignedWorkspaceId).getWoDesc();
			if (assignedWd.containsEntityPreferences(ep.getEntity())) {
				final TablePreferences assignedTp = assignedWd.getEntityPreferences(ep.getEntity()).getResultPreferences();
				if (assignedTp.getSelectedColumnPreferences().isEmpty()) {
					restoreToSystemDefault = true;
				} else {
					ep.getResultPreferences().clearAndImport(assignedTp);
				}
			} else {
				restoreToSystemDefault = true;
			}
		}
		
		if (restoreToSystemDefault) {
			ep.clearResultPreferences();
		}
	}
	
	
	/**
	 * 
	 * @param sfp
	 * @param mainEntity
	 * @throws CommonBusinessException 
	 */
	public static void restoreSubFormPreferences(SubFormPreferences sfp, String mainEntity) throws CommonBusinessException {
		final Long assignedWorkspaceId = MainFrame.getWorkspace().getAssignedWorkspace();
		boolean restoreToSystemDefault = false;
		
		if (assignedWorkspaceId == null) {
			// restore to first time
			restoreToSystemDefault = true;
		} else {
			final WorkspaceDescription assignedWd = getPrefsFacade().getWorkspace(assignedWorkspaceId).getWoDesc();
			if (assignedWd.containsEntityPreferences(mainEntity)) {
				final TablePreferences assignedTp = assignedWd.getEntityPreferences(mainEntity).getSubFormPreferences(sfp.getEntity()).getTablePreferences();
				if (assignedTp.getSelectedColumnPreferences().isEmpty()) {
					restoreToSystemDefault = true;
				} else {
					sfp.getTablePreferences().clearAndImport(assignedTp);
				}
			} else {
				restoreToSystemDefault = true;
			}
		}
		
		if (restoreToSystemDefault) {
			sfp.clearTablePreferences();
		}
	}
	
	private static PreferencesFacadeRemote getPrefsFacade() {
		return ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);
	}
	
	public interface IColumnNameResolver {
		public String getColumnName(int iColumn);
	}
	
	public interface IColumnIndexRecolver {
		public int getColumnIndex(String columnIdentifier);
	}
}
