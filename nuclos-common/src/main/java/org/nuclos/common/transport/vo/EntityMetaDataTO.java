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
package org.nuclos.common.transport.vo;

import java.io.Serializable;
import java.util.List;

import org.nuclos.common.EntityTreeViewVO;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.dal.vo.EntityMetaDataVO;


/**
 * Entity meta data vo
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:maik.stueker@novabit.de">Maik.Stueker</a>
 * @version 01.00.00
 */
public class EntityMetaDataTO implements Serializable {
	
	EntityMetaDataVO voEntityMetaVO;
	
	List<TranslationVO> lstTranslation;
	List<EntityTreeViewVO> lstTreeView;

	public EntityMetaDataVO getEntityMetaVO() {
		return voEntityMetaVO;
	}

	public void setEntityMetaVO(EntityMetaDataVO voEntityMetaVO) {
		this.voEntityMetaVO = voEntityMetaVO;
	}

	public List<TranslationVO> getTranslation() {
		return lstTranslation;
	}

	public void setTranslation(List<TranslationVO> lstTranslation) {
		this.lstTranslation = lstTranslation;
	}
	
	public List<EntityTreeViewVO> getTreeView() {
		return lstTreeView;
	}

	public void setTreeView(List<EntityTreeViewVO> lstTreeView) {
		this.lstTreeView = lstTreeView;
	}
	
	
}
