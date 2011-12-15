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
package org.nuclos.common.collect.collectable.searchcondition;

import java.awt.datatransfer.Transferable;
import java.io.Serializable;

import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;

/**
 * A search condition for <code>Collectable</code>s.
 * <strong>Warning:</strong> Serialized objects of this class will not be compatible with future releases.
 * The current serialization support is appropriate for short term storage or RMI.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo The TYPE constants should be used for storing in preferences only. Move to SearchConditionUtils and make them private.
 */
public interface CollectableSearchCondition extends Transferable, Serializable {

	/**
	 * this value is not valid for any instance of this type. It is just used to specify an (illegal)
	 * default value for reading from the preferences.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_UNDEFINED = -1;

	/**
	 * atomic search condition. If these constants are changed, <code>sorted()</code> must be adjusted.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_ATOMIC = 0;

	/**
	 * composite search condition. If these constants are changed, <code>sorted()</code> must be adjusted.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_COMPOSITE = 1;

	/**
	 * subcondition. If these constants are changed, <code>sorted()</code> must be adjusted.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_SUB = 2;

	/**
	 * id condition, that is a condition of kind "id = x".  If these constants are changed, <code>sorted()</code> must be adjusted.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_ID = 3;

	/**
	 * condition on a referenced object.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_REFERENCING = 4;

	/**
	 * "always true" condition.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_TRUE = 5;

	/**
	 * "pivot join" condition.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int TYPE_PIVOTJOIN = 6;
	
	/**
	 * "ref join" condition.
	 * @deprecated Don't use this constant in new applications.
	 */
	@Deprecated
	int REF_JOIN = 7;

	/**
	 * @return the type of this node (TYPE_ATOMIC, TYPE_COMPOSITE, TYPE_SUB, TYPE_ID, TYPE_REFERENCING or TYPE_TRUE)
	 * @see Visitor
	 * 
	 * @deprecated Don't use "switch(getType()) {...} in your code"! Use Visitor pattern instead!
	 */
	int getType();
	
	String getConditionName();
	
	void setConditionName(String conditionName);

	/**
	 * @return Is this condition syntactically correct? (For example, an AND condition node must have
	 * at least two operands, a NOT condition node must have exactly one operand.)
	 * @todo replace with "void validate() throws InvalidCollectableSearchConditionException"
	 */
	boolean isSyntacticallyCorrect();

	/**
	 * @param o
	 * @return Is <code>this</code> syntactically equal to <code>o</code>?
	 */
	// @Override
	boolean equals(Object o);

	// @Override
	int hashCode();

	/**
	 * dispatch method for Visitor pattern.
	 * For a description of the Visitor pattern, see the "GoF" Patterns book.
	 * @param visitor
	 * @return the result of the visitor's respective method.
	 * @precondition visitor != null
	 */
	<O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex;

}	// interface CollectableSearchCondition
