package org.nuclos.server.dal.processor;

import java.util.ArrayList;

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.dal.vo.IDalVO;
import org.nuclos.common.dal.vo.IDalWithDependantsVO;
import org.nuclos.common2.exception.CommonFatalException;

import com.sun.tools.javac.util.List;

public class JoinEntityFieldVOMapping<T> extends AbstractColumnToVOMapping<T> {
	
	private final String joinEntity;
	
	private final String field;

	JoinEntityFieldVOMapping(String tableAlias, String column, Class<T> dataType, boolean isReadonly,
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
		EntityObjectVO joinEntityVO = CollectionUtils.getSingleIfExist(realDal.getDependants().getData(joinEntity));
		// Create dependant if it does not exist.
		if (joinEntityVO == null) {
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
