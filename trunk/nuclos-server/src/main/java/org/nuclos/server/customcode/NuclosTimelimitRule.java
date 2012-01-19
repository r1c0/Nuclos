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
package org.nuclos.server.customcode;

import java.util.Collection;

import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.RuleInterface;

/**
 * Interface all timelimit rules have to implement.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:martin.weber@novabit.de">Martin Weber</a>
 * @version	01.00.00
 */

public interface NuclosTimelimitRule {
	
	/**
	 * Retrieve all the ids of all records to process.
	 * @param server
	 * @return
	 */
	public Collection<Integer> getIntIds(RuleInterface server);
	
	/**
	 * Process one of the retrieved ids in an own transaction. This is called once for all ids in the collection retrieved  by getIntIds.
	 * @param server
	 * @param iId
	 * @throws NuclosBusinessRuleException
	 */
	public void process(RuleInterface server, Integer iId) throws NuclosBusinessRuleException;
}
