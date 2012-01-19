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

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.customcode.ejb3.CodeFacadeRemote;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Business delegate for code administration.
 *
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
public class CodeDelegate {

	private static final CodeDelegate singleton = new CodeDelegate();

	private CodeFacadeRemote facade;

	private CodeDelegate() {
		facade = ServiceLocator.getInstance().getFacade(CodeFacadeRemote.class);
	}

	public static CodeDelegate getInstance() {
		return singleton;
	}

	public MasterDataVO create(MasterDataVO vo) throws CommonBusinessException {
		return facade.create(vo);
	}

	public MasterDataVO modify(MasterDataVO vo) throws CommonBusinessException {
		return facade.modify(vo);
	}

	public void remove(MasterDataVO vo) throws CommonBusinessException {
		facade.remove(vo);
	}

	public void compile(MasterDataVO vo) throws CommonBusinessException {
		facade.check(vo);
	}

	public List<CodeVO> getAll() {
		try {
			return facade.getAll();
		}
		catch (CommonPermissionException ex) {
			return Collections.emptyList();
		}
	}
}
