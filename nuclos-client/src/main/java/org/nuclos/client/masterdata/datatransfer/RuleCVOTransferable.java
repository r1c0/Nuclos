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
package org.nuclos.client.masterdata.datatransfer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.List;

/**
 * Rule and its usage as a <code>Transferable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rainer.Schneider@novabit.de">Rainer Schneider</a>
 * @version 01.00.00
 */

public class RuleCVOTransferable implements Transferable {

	private final DataFlavor[] aflavors;
	private final List<RuleAndRuleUsageEntity> ruleVo;
	private final String sText;

	/**
	 * creates a <code>Transferable</code> for a <code>MasterDataVO</code>
	 * @param mdvo
	 */
	public RuleCVOTransferable(List<RuleAndRuleUsageEntity> aRuleVo) {
		this.ruleVo = aRuleVo;

		this.sText = ruleVo.get(0).getRuleVo().getName();

		this.aflavors = new DataFlavor[] {
				new RuleAndRuleUsageEntity.RuleUsageDataFlavor(),
				DataFlavor.stringFlavor
		};
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
		Object result;

		if (flavor instanceof RuleAndRuleUsageEntity.RuleUsageDataFlavor) {
			result = this.ruleVo;
		}
		else {
			if (flavor.equals(DataFlavor.stringFlavor)) {
				result = this.sText;
			}
			else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		return result;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return this.aflavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		final DataFlavor[] flavors = getTransferDataFlavors();
		for (int i = 0; i < flavors.length; i++) {
			if (flavors[i].equals(flavor)) {
				return true;
			}
		}
		return false;
	}

}	// class MasterDataVOTransferable
