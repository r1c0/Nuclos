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

package org.nuclos.server.customcomp.ejb3;

import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.TranslationVO;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;

// @Remote
public interface CustomComponentFacadeRemote {

	@RolesAllowed("Login")
	List<CustomComponentVO> getAll();

	@RolesAllowed("Login")
	void create(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException;

	@RolesAllowed("Login")
	void modify(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException;

	@RolesAllowed("Login")
	void remove(CustomComponentVO vo) throws CommonBusinessException;

	@RolesAllowed("Login")
	List<TranslationVO> getTranslations(Integer ccid) throws CommonBusinessException;
}
