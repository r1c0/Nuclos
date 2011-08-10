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
package org.nuclos.server.dal.processor.nuclet;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.server.dal.specification.IDalCountSpecification;
import org.nuclos.server.dal.specification.IDalReadSpecification;
import org.nuclos.server.dal.specification.IDalSearchExpressionSpecification;
import org.nuclos.server.dal.specification.IDalVersionSpecification;
import org.nuclos.server.dal.specification.IDalWriteSpecification;

public interface JdbcEntityObjectProcessor extends 
	IDalReadSpecification<EntityObjectVO>,
	IDalWriteSpecification<EntityObjectVO>,
	IDalSearchExpressionSpecification<EntityObjectVO>,
	IDalVersionSpecification,
	IDalCountSpecification,
	Cloneable {
	
	Object clone();
	
	void addToColumns(EntityFieldMetaDataVO field);

}
