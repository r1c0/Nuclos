//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.common;

import java.util.Collection;
import java.util.List;

import org.nuclos.common.dal.vo.EntityFieldMetaDataVO;
import org.nuclos.common.dal.vo.PivotInfo;

/**
 * Common <em>client</em> interface implemented by MetaData Delegates, Facades, and Providers. 
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.1.06
 */
public interface CommonMetaDataClientProvider extends CommonMetaDataProvider {

	/**
	 * Return all (pseudo) fields/columns that can be accessed in a subform used as pivot table. 
	 * 
	 * @param  info The subform and key of the pivot table. <p><b>Attention:</b> The value part of PivotInfo is
	 * 		<em>ignored</em> (together with the value type). Use the parameter valueColumns (see below).</p>
	 * @param  valueColumns The valueColumns/fields to use. This parameter is <em>required</em> due to 
	 * 		the pivot multi-value support.
	 * @return All (pseudo) fields.
	 */
    Collection<EntityFieldMetaDataVO> getAllPivotEntityFields(PivotInfo info, List<String> valueColumns);

}
