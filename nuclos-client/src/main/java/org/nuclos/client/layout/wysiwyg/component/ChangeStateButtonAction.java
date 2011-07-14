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
package org.nuclos.client.layout.wysiwyg.component;

import java.util.Collection;
import java.util.Properties;

import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.genericobject.GenericObjectCollectController.StateWrapper;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.CHANGESTATEACTIONLISTENER;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.server.statemodel.valueobject.StateVO;

/**
 * This is a Button Action for changing the State of a GO
 * 
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * NUCLEUSINT-1159
 * @author <a
 *         href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ChangeStateButtonAction<Clct extends Collectable> implements CollectActionAdapter<Clct> {

	@Override
	@SuppressWarnings("unchecked")
	public void run(CollectController controller, Properties probs) {
		if(controller instanceof GenericObjectCollectController) {
			GenericObjectCollectController gController = (GenericObjectCollectController) controller;
			
			if (!controller.getDetailsPanel().isVisible()) {
				// is not in details view, is in search or elsewhere
				return;
			}
			
			String sTargetState = probs.getProperty("targetState");
			Integer targetState = null;
			try {
				 targetState = Integer.parseInt(sTargetState);
			} catch (NumberFormatException e) {
				Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
				return;
			}
			
			String entityName = gController.getEntityName();
			Integer moduleId = Modules.getInstance().getModuleIdByEntityName(entityName);

			Collection<StateVO> possibleStates = StateDelegate.getInstance().getStatesByModule(moduleId);
			String stateName = null;
			Integer stateID = null;
			for(StateVO possibleState : possibleStates) {
				if(possibleState.getNumeral().equals(targetState)) {
					stateName = possibleState.getStatename();
					stateID = possibleState.getId();
					break;
				}
			}
			
			StateWrapper newState = new StateWrapper(stateID, targetState, stateName);
			gController.cmdChangeState(newState);
		}
		else {
			Errors.getInstance().showExceptionDialog(controller.getCollectPanel(),new NuclosBusinessException(CHANGESTATEACTIONLISTENER.NO_GENERICOBJECT));
		}
	}

}