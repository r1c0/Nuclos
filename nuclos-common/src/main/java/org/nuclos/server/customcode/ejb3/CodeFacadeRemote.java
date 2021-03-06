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
package org.nuclos.server.customcode.ejb3;

import java.util.List;

import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

public interface CodeFacadeRemote {

	MasterDataVO create(MasterDataVO vo) throws CommonBusinessException;

	MasterDataVO modify(MasterDataVO vo) throws CommonBusinessException;

	void remove(MasterDataVO vo) throws CommonBusinessException;

	void check(MasterDataVO vo) throws CommonBusinessException;

	List<CodeVO> getAll() throws CommonPermissionException;

	Object invokeFunction(String functionname, Object[] args);
}
