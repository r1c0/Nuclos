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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;

/**
 * Abstract implementation of a collectable search condition. Implements Transferable.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public abstract class AbstractCollectableSearchCondition implements CollectableSearchCondition {
	
	private String conditionName;

	public static final DataFlavor dataflavorSearchCondition = new DataFlavor(CollectableSearchCondition.class,
			"Suchbedingung");

	@Override
	public final Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		final Object result;

		if (flavor.equals(dataflavorSearchCondition)) {
			result = this;
		}
		else if (flavor.equals(DataFlavor.stringFlavor)) {
			result = this.toString();
		}
		else {
			throw new UnsupportedFlavorException(flavor);
		}

		return result;
	}

	@Override
	public final DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{dataflavorSearchCondition, DataFlavor.stringFlavor};
	}

	@Override
	public final boolean isDataFlavorSupported(DataFlavor flavor) {
		final DataFlavor[] flavors = this.getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

	@Override
    public String getConditionName() {
    	return conditionName;
    }

	@Override
    public void setConditionName(String conditionName) {
    	this.conditionName = conditionName;
    }
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + hashCode() + ":" + conditionName;
	}
	
}  // class AbstractCollectableSearchCondition
