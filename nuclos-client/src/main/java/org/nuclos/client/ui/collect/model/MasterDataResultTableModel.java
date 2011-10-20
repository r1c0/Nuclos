package org.nuclos.client.ui.collect.model;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.masterdata.CollectableMasterData;
import org.nuclos.client.masterdata.CollectableMasterDataWithDependants;
import org.nuclos.common.CollectableEntityFieldWithEntity;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.genericobject.GenericObjectUtils;
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
			if (sFieldEntityName.equals(Modules.getInstance().getParentEntityName(sMainEntityName))) {

				/** @todo assert govoParent != null */
				if (mdwdvo == null) {
					result = clctefwe.getNullField();
				}
				else {
					result = new CollectableMasterData(((CollectableMasterDataWithDependants) clct).getCollectableEntity(), mdwdvo).getField(sFieldName);
				}
			}
			else {
				final Collection<EntityObjectVO> collmdvo = mdwdvo.getDependants().getData(sFieldEntityName);
				result = new CollectableValueField(GenericObjectUtils.getConcatenatedValue(collmdvo, sFieldName));
			}
		}

		// set output format
		final Class<?> cls = clctefwe.getJavaClass();
		if (Number.class.isAssignableFrom(cls)) {
			String sFormatOutput = clctefwe.getField().getFormatOutput();
			if (result.getValue() != null && sFormatOutput != null && !sFormatOutput.equals("")) {
				final DecimalFormat df =   new DecimalFormat(sFormatOutput);
				result = new CollectableValueField(df.format(result.getValue()));
			}
		}

		return result;
	}
	
}
