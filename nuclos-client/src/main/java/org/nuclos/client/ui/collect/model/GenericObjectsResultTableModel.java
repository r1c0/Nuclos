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
package org.nuclos.client.ui.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

public class GenericObjectsResultTableModel<Clct extends Collectable> extends SortableCollectableTableModelImpl<Clct> {
	
	private static final Logger LOG = Logger.getLogger(GenericObjectsResultTableModel.class);

	private final CollectableEntity entity;

	private GenericObjectsResultTableModel(CollectableEntity entity) {
		super(entity.getName());
		this.entity = entity;
	}

	public GenericObjectsResultTableModel(CollectableEntity entity, List<? extends CollectableEntityField> list) {
		this(entity);
		setColumns(list);
	}

	@Override
	public CollectableField getValueAt(int iRow, int iColumn) {
		/* @todo How shall we handle exceptions here?!
		 * (a) pass thru: BAD IDEA! - painting fails, and the GUI looks really weird.
		 * (b) catch and return null: BAD IDEA!!! - We ignore the error and deliberately return a false value - DON'T!!!
		 * (c) catch and return "#ERROR#":  Now this is stupid.
		 * (d) catch, remember exception and let anybody (WHO/WHEN?!) check if an exception was thrown here
		 * Good ideas, anyone???
		 */
		final CollectableGenericObjectWithDependants clct = (CollectableGenericObjectWithDependants) getCollectable(iRow);
		if (clct == null) {
			LOG.warn("getValueAt: CollectableGenericObjectWithDependants is null");
			return null;
		}
		final GenericObjectWithDependantsVO lowdcvo = clct.getGenericObjectWithDependantsCVO();
		final CollectableEntityField clctefwe = getCollectableEntityField(iColumn);
		final PivotInfo pinfo;
		if (clctefwe instanceof CollectableEOEntityField) {
			pinfo = ((CollectableEOEntityField) clctefwe).getMeta().getPivotInfo();
		}
		else {
			pinfo = null;
		}
		final String sFieldName = clctefwe.getName();

		final String sFieldEntityName = clctefwe.getEntityName();
		final String sMainEntityName = getBaseEntityName();

		CollectableField result;
		// field of base entity
		if (sFieldEntityName.equals(sMainEntityName)) {
			result = clct.getField(sFieldName);
		}
		// pivot field
		else if (pinfo != null) {
			final List<Object> values = new ArrayList<Object>(1);
			final Collection<EntityObjectVO> items = lowdcvo.getDependants().getData(sFieldEntityName);

			for (EntityObjectVO k: items) {
				if (sFieldName.equals(k.getRealField(pinfo.getKeyField(), String.class))) {
					values.add(k.getRealField(pinfo.getValueField(), pinfo.getValueType()));
				}
			}
			return new CollectableValueField(values);
		}
		// field of subform entity
		else {
			final Collection<EntityObjectVO> collmdvo = lowdcvo.getDependants().getData(sFieldEntityName);
			List<Object> values = CollectionUtils.transform(collmdvo, new Transformer<EntityObjectVO, Object>() {
				@Override
				public Object transform(EntityObjectVO i) {
					return i.getRealField(sFieldName);
				}
			});
			return new CollectableValueField(values);
		}
		return result;
	}

}
