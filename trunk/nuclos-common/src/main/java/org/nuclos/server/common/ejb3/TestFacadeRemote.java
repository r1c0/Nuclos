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
package org.nuclos.server.common.ejb3;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;

// @Remote
public interface TestFacadeRemote {

	/**
	 * Used for testing serialization of CollectableSearchCondition.
	 * @param clctcond
	 * @return clctcond itself
	 */
	CollectableSearchCondition testSerialization(
		CollectableSearchCondition clctcond);

	/**
	 * Used to test client notification
	 * @param topic    the topic
	 * @param message  the message
	 */
	void testClientNotification(String topic, String message);

}
