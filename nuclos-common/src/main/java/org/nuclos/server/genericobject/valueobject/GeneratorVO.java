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
package org.nuclos.server.genericobject.valueobject;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.nuclos.common2.LangUtils;

/**
 * Value object representing a leased object generator.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 01.00.000
 */
public class GeneratorVO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final List<GeneratorActionVO> lstActions;

	/**
	 * constructor to be called by server only
	 * @param lstActions list of generator actions
	 */
	public GeneratorVO(List<GeneratorActionVO> lstActions) {
		this.lstActions = lstActions;
	}

	/**
	 * gets a list of generator actions by module, process and statemnemonic.
	 * If the source process id is null in the spec, any given source process id matches.
	 * If the statemnemonic is null in the spec, any given statemnemonic matches.
	 * @param iModuleId source module id
	 * @param iStateNumeral source state mnemonic
	 * @param iProcessId source process id
	 * @return List<GeneratorActionVO> list of generator actions
	 * @postcondition result != null
	 */
	public List<GeneratorActionVO> getGeneratorActions(Integer iModuleId, Integer iStateNumeral, Integer iProcessId) {
		final List<GeneratorActionVO> result = new LinkedList<GeneratorActionVO>();
		for (GeneratorActionVO generatoractionvo : lstActions) {
			if (iModuleId.equals(generatoractionvo.getSourceModuleId())) {
				final String sStateMnemonic = LangUtils.toString(iStateNumeral);
				for (GeneratorUsageVO guvo : generatoractionvo.getUsages()) {
					if ((getStateMnemonicFromGeneratorusageVO(guvo) == null || getStateMnemonicFromGeneratorusageVO(guvo).equals(sStateMnemonic)) &&
							(guvo.getProcessId() == null || guvo.getProcessId().equals(iProcessId))) {

						result.add(generatoractionvo);
					}
				}
			}
		}
		assert result != null;
		return result;
	}
	
	private String getStateMnemonicFromGeneratorusageVO(GeneratorUsageVO guvo) {
		String state = guvo.getStateMnemonic();
		int index = state.indexOf(" ");
		if (index != -1)
			return state.substring(0, state.indexOf(" "));
		else
			return state;
	}

}	// class GeneratorVO
