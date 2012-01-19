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
package org.nuclos.common.collect.collectable.searchcondition.visit;

import org.nuclos.common.collect.collectable.searchcondition.CollectableSelfSubCondition;
import org.nuclos.common.collect.collectable.searchcondition.PlainSubCondition;

/**
 * CompositeVisitor for <code>CollectableSearchCondition</code>s.
 * For a description of the Visitor pattern, see the "GoF" Patterns book.
 * 
 * @author	<a href="mailto:Rostislav.Maksmovskyi@novabit.de">Rostislav Maksmovskyi</a>
 * @version 01.00.00
 *
 * @todo refactor: merge this interface to Visitor and extract an abstract super class 
 * with default implementations for some methods for all search conditions 
 * that implements Visitor interface. The goal: provide simple possibility to extend this Visitor 
 * interface without cganging all implementors.
 */
public interface CompositeVisitor<O, Ex extends Exception> {
	/**
	 * @precondition subcond != null
	 */
	O visitSelfSubCondition(CollectableSelfSubCondition subcond) throws Ex;

	/**
	 * @precondition subcond != null
	 */
	O visitPlainSubCondition(PlainSubCondition subcond) throws Ex;

} // interface CompositeVisitor

