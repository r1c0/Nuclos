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

import org.nuclos.common.collect.collectable.searchcondition.visit.Visitor;

/**
 * <code>CollectableSearchCondition</code> that is always true.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 * @todo TrueCondition extends AtomicCollectableSearchCondition?
 */
public final class TrueCondition extends AbstractCollectableSearchCondition {

	public static final TrueCondition TRUE = new TrueCondition();

	private TrueCondition() {
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_TRUE;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		return true;
	}


	@Override
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		return visitor.visitTrueCondition(this);
	}

}	// class TrueCondition
