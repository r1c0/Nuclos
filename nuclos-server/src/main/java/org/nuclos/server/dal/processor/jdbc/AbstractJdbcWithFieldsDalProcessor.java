package org.nuclos.server.dal.processor.jdbc;

import java.util.List;

import org.nuclos.common.dal.vo.IDalWithFieldsVO;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.dal.processor.IColumnToVOMapping;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public abstract class AbstractJdbcWithFieldsDalProcessor<DalVO extends IDalWithFieldsVO<?>> extends AbstractJdbcDalProcessor<DalVO> {

	private int maxFieldCount = 20;

	private int maxFieldIdCount = 5;

	protected AbstractJdbcWithFieldsDalProcessor(Class<DalVO> type, List<IColumnToVOMapping<? extends Object>> allColumns, int maxFieldCount, int maxFieldIdCount) {
		super(type, allColumns);
		this.maxFieldCount = maxFieldCount;
		this.maxFieldIdCount = maxFieldIdCount;
	}
	
	@Override
	protected DalVO newDalVOInstance() {
		try {
			DalVO newInstance = getDalType().newInstance();
			((IDalWithFieldsVO<?>) newInstance).initFields(maxFieldCount, maxFieldIdCount);
			return newInstance;
		} catch (Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
