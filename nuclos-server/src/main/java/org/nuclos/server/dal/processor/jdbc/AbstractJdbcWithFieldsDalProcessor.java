package org.nuclos.server.dal.processor.jdbc;

import org.nuclos.common.dal.vo.IDalWithFieldsVO;
import org.nuclos.common2.exception.CommonFatalException;

public abstract class AbstractJdbcWithFieldsDalProcessor<T, DalVO extends IDalWithFieldsVO<T>> extends AbstractJdbcDalProcessor<T, DalVO> {

	private int maxFieldCount = 20;

	private int maxFieldIdCount = 5;

	/**
	 *
	 * @param maxFieldCount
	 * @param maxFieldIdCount
	 */
	protected AbstractJdbcWithFieldsDalProcessor(int maxFieldCount, int maxFieldIdCount) {
		this();
		this.maxFieldCount = maxFieldCount;
		this.maxFieldIdCount = maxFieldIdCount;
	}
	
	protected AbstractJdbcWithFieldsDalProcessor() {
	}

	@Override
	protected DalVO newDalVOInstance(){
		try {
			DalVO newInstance = (DalVO) getDalVOClass().newInstance();
			((IDalWithFieldsVO<T>) newInstance).initFields(maxFieldCount, maxFieldIdCount);
			return newInstance;
		}
		catch(Exception e) {
			throw new CommonFatalException(e);
		}
	}

}
