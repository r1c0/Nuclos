package org.nuclos.client.ui.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;

public class MasterDataResultTableModel<Clct extends Collectable> extends SortableCollectableTableModelImpl<Clct> {

	private final CollectableEntity entity;

	private MasterDataResultTableModel(CollectableEntity entity) {
		super(entity.getName());
		this.entity = entity;
	}

	public MasterDataResultTableModel(CollectableEntity entity, List<? extends CollectableEntityField> list) {
		this(entity);
		setColumns(list);
	}

	@Override
	public CollectableField getValueAt(int iRow, int iColumn) {
		final Collectable clct = this.getCollectable(iRow);
		final CollectableEntityFieldWithEntity clctefwe = (CollectableEntityFieldWithEntity) this.getCollectableEntityField(iColumn);
		final String sFieldName = clctefwe.getName();

		final String sFieldEntityName = clctefwe.getCollectableEntityName();
		final String sMainEntityName = getBaseEntityName();

		CollectableField result;
		if (sFieldEntityName.equals(sMainEntityName)) {
			result = clct.getField(sFieldName);
		}
		else {
			final MasterDataWithDependantsVO mdwdvo = ((CollectableMasterDataWithDependants) clct).getMasterDataWithDependantsCVO();

			final Collection<EntityObjectVO> collmdvo = mdwdvo.getDependants().getData(sFieldEntityName);
			result = new CollectableValueField(getValues(collmdvo, sFieldName));
		}

		return result;
	}

	public static List<Object> getValues(Collection<EntityObjectVO> collmdvo, String sFieldName) {
		final List<Object> result = new ArrayList<Object>();
		if (collmdvo != null) {
			for (Iterator<EntityObjectVO> iter = collmdvo.iterator(); iter.hasNext();) {
				final EntityObjectVO mdvo = iter.next();
				result.add(mdvo.getField(sFieldName, Object.class));
			}
		}
		return result;
	}
}
