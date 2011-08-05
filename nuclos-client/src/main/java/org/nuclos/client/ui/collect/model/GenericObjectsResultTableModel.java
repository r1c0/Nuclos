package org.nuclos.client.ui.collect.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.nuclos.client.genericobject.CollectableGenericObject;
import org.nuclos.client.genericobject.CollectableGenericObjectWithDependants;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableEntity;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common.collect.collectable.CollectableValueField;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.PivotInfo;
import org.nuclos.common.entityobject.CollectableEOEntityField;
import org.nuclos.common2.FormatOutputUtils;
import org.nuclos.server.genericobject.valueobject.GenericObjectVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;

public class GenericObjectsResultTableModel<Clct extends Collectable> extends SortableCollectableTableModelImpl<Clct> {
	
	private final CollectableEntity entity;
	
	public GenericObjectsResultTableModel(CollectableEntity entity) {
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
		final String sMainEntityName = getEntityName();

		CollectableField result;
		// field of base entity
		if (sFieldEntityName.equals(sMainEntityName)) {
			result = clct.getField(sFieldName);
			// result = FormatOutputUtils.format(clctefwe, clct);
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
			// If there is more than one value (not supported in the pivot case), we loose the type... 
			if (values.size() == 1) {
				result = new CollectableValueField(values.iterator().next());
			}
			else {
				result = new CollectableValueField(FormatOutputUtils.formatOutput(clctefwe.getFormatOutput(), values, " "));
			}
		}
		// field of subform entity
		else {
			if (sFieldEntityName.equals(Modules.getInstance().getParentEntityName(sMainEntityName))) {
				// dead code here
				assert false;
				final GenericObjectVO govoParent = lowdcvo.getParent();
				if (govoParent == null) {
					result = clctefwe.getNullField();
				}
				else {
					result = new CollectableGenericObject(govoParent).getField(sFieldName);
				}
			}
			else {
				final Collection<EntityObjectVO> collmdvo = lowdcvo.getDependants().getData(sFieldEntityName);
				// result = new CollectableValueField(GenericObjectUtils.getConcatenatedValue(collmdvo, sFieldName));
				// If there is more than one value, we loose the type... 
				if (collmdvo.size() == 1) {
					result = new CollectableValueField(collmdvo.iterator().next());
				}
				else {
					result = FormatOutputUtils.format(clctefwe, collmdvo, " ");
				}
			}
		}
		
		/*
		Since Nuclos 3.1.01 we do _not_ format number in the model. 
		If needed it has to be done 'later'. (Thomas Pasch + Maik Stüker)
		
		// set output format
		final Class<?> cls = clctefwe.getJavaClass();
		if (Number.class.isAssignableFrom(cls)) {
			String sFormatOutput = clctefwe.getFormatOutput();
			if (result.getValue() != null && sFormatOutput != null && !sFormatOutput.equals("")) {
				final DecimalFormat df =   new DecimalFormat(sFormatOutput);
				result = new CollectableValueField(df.format(result.getValue()));
			}			
		}
		 */
		
		return result;
	}

}
