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

package org.nuclos.server.livesearch.ejb3;

import java.util.List;
import java.util.Set;

import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.EntityObjectVO;

/**
 * Interface to implement, if additional filtering of live-search results is
 * necessary due to business limitations.
 * 
 * Configured as a comma-separated list in the server-parameter
 * "livesearch.server.addfilter"
 */
public interface LiveSearchAddFilter {
	/**
	 * Apply an additional filter to a live-search result. The input is the
	 * complete list of search results so far, consisting of either an entity
	 * object and a list of hidden fields, or an entity object and null
	 * 
	 * @param sessionContext  the current session context
	 * @param in              the list
	 * 
	 * @return    a filtered list in the same format
	 */
	public List<Pair<EntityObjectVO, Set<String>>> applyFilter(String username, List<Pair<EntityObjectVO, Set<String>>> in);
}
