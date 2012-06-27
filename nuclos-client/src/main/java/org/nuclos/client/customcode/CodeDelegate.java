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
package org.nuclos.client.customcode;

import java.util.Collections;
import java.util.List;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.customcode.ejb3.CodeFacadeRemote;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.InitializingBean;

/**
 * Business delegate for code administration.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class CodeDelegate {

	private static CodeDelegate INSTANCE;

	private CodeFacadeRemote codeFacadeRemote;

	CodeDelegate() {
		INSTANCE = this;
	}
	
	public final void setCodeFacadeRemote(CodeFacadeRemote codeFacadeRemote) {
		this.codeFacadeRemote = codeFacadeRemote;
	}
	
	public static CodeDelegate getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException("too early");
		}
		return INSTANCE;
	}

	public MasterDataVO create(MasterDataVO vo) throws CommonBusinessException {
		return codeFacadeRemote.create(vo);
	}

	public MasterDataVO modify(MasterDataVO vo) throws CommonBusinessException {
		return codeFacadeRemote.modify(vo);
	}

	public void remove(MasterDataVO vo) throws CommonBusinessException {
		codeFacadeRemote.remove(vo);
	}

	public void compile(MasterDataVO vo) throws CommonBusinessException {
		codeFacadeRemote.check(vo);
	}

	public List<CodeVO> getAll() {
		try {
			return codeFacadeRemote.getAll();
		}
		catch (CommonPermissionException ex) {
			return Collections.emptyList();
		}
	}

	public Object invokeFunction(String functionname, Object[] args) {
		return codeFacadeRemote.invokeFunction(functionname, args);
	}

}
