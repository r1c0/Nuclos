package org.nuclos.server.dal.processor;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.IDalWithDependantsVO;
import org.nuclos.common2.exception.CommonFatalException;

public class PivotJoinEntityFieldVOMapping<T> extends AbstractColumnToVOMapping<T> {
	
	private final String joinEntity;
	
	private final String field;

	PivotJoinEntityFieldVOMapping(String tableAlias, String column, Class<T> dataType, boolean isReadonly,
			String joinEntity, String field) {
		super(tableAlias, column, dataType, isReadonly, false);
		this.joinEntity = joinEntity;
		this.field = field;
	}

	@Override
	public String toString() {
		final StringBuilder result = new StringBuilder();
		result.append(getClass().getName()).append("[");
		result.append("col=").append(getColumn());
		result.append(", tableAlias=").append(getTableAlias());
		result.append(", joinEntity=").append(joinEntity);
		result.append(", field=").append(field);
		if (getDataType() != null)
			result.append(", type=").append(getDataType().getName());
		result.append("]");
		return result.toString();
	}
	
	@Override
	public Object convertFromDalFieldToDbValue(IDalVO dal) {
		final IDalWithDependantsVO<?> realDal = (IDalWithDependantsVO<?>) dal;
		final EntityObjectVO joinEntityVO = CollectionUtils.getSingleIfExist(realDal.getDependants().getData(joinEntity));
		try {
			if (joinEntityVO == null) {
				return convertToDbValue(getDataType(), null);
			}
			return convertToDbValue(getDataType(), joinEntityVO.getField(field));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public void convertFromDbValueToDalField(IDalVO result, T o) {
		final IDalWithDependantsVO<?> realDal = (IDalWithDependantsVO<?>) result;
		// This is a bit HACKISH. But we know that the the dependents get build 'row by row' from the SQL result.
		EntityObjectVO joinEntityVO = CollectionUtils.getLastOrNull(realDal.getDependants().getData(joinEntity));
		
		// Create a new dependent if there is none. This should be clear.
		// *Or*
		// Create a new dependent if your key is already there:
		//
		// This is the complicated case (and a little HACKISH). It assumes that the two pivot FIELDS from the SQL 
		// SELECT clause (aka the selected pivot fields) of the result row follow each other.
		// 
		// Each of such a pair is mapped (by IColumnToVOMapping) for each pivot column to display to one
		// dependent object.
		// 
		// Example SQL (with 2 pivot columns to display):
		// 
		// SELECT t.INTID, t.INTVALUE_NUCLOSSTATE, _Boskopf.INTquantity, _Boskopf.STRVALUE_Position2Ware, _Williams_Christ.INTquantity, _Williams_Christ.STRVALUE_Position2Ware  
		// 	FROM V_EO_BESTELLUNG t 
		// 	LEFT OUTER JOIN V_EO_BESTELLPOSITION _Boskopf ON (t.INTID = _Boskopf.INTID_STRref) 
		// 	LEFT OUTER JOIN V_EO_BESTELLPOSITION _Williams_Christ ON (t.INTID = _Williams_Christ.INTID_STRref) 
		// 	WHERE (((_Boskopf.STRVALUE_Position2Ware = ? OR _Boskopf.STRVALUE_Position2Ware IS NULL) 
		// 		AND (_Williams_Christ.STRVALUE_Position2Ware = ? OR _Williams_Christ.STRVALUE_Position2Ware IS NULL)) 
		// 		AND t.INTID IN (?, ?, ?, ?, ?))
		//
		// As you could see, the pivot key and value column for each pivot join are grouped together.
		// 
		// (Thomas Pasch)
		if (joinEntityVO == null || joinEntityVO.getFields().containsKey(field)) {
			joinEntityVO = new EntityObjectVO();
			joinEntityVO.initFields(5, 5);
			joinEntityVO.setEntity(joinEntity);
			
			realDal.getDependants().addData(joinEntity, joinEntityVO);
		}
		try {
			joinEntityVO.getFields().put(field,
					convertFromDbValue(o, getColumn(), getDataType(), joinEntityVO.getId()));
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

	/**
	 * @deprecated This is impossible in the general case, thus avoid it.
	 */
	@Override
	public String getField() {
		return joinEntity + "." + field;
	}

}