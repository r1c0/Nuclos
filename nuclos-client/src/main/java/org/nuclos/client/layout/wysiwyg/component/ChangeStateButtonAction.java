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

import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.layout.wysiwyg.WYSIWYGStringsAndLabels.CHANGESTATEACTIONLISTENER;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.statemodel.StateWrapper;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.resource.valueobject.ResourceVO;
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
	public void run(final JButton btn, final CollectController<Clct> controller, Properties probs) {
		try {
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
					return;
				}
				
				String entityName = gController.getEntityName();
				Integer moduleId = Modules.getInstance().getModuleIdByEntityName(entityName);
	
				Collection<StateVO> possibleStates = StateDelegate.getInstance().getStatesByModule(moduleId);
				String stateName = null;
				Integer stateID = null;
				Integer stateNumeral = null;
				String stateDescription = null;
				NuclosImage stateIcon = null;
				String stateColor = null;
				ResourceVO stateButtonIcon = null;
				for(StateVO possibleState : possibleStates) {
					if(possibleState.getId().equals(targetState)) {
						stateName = possibleState.getStatename();
						stateID = possibleState.getId();
						stateNumeral = possibleState.getNumeral();
						stateIcon = possibleState.getIcon();
						stateDescription = possibleState.getDescription();
						stateColor = possibleState.getColor();
						stateButtonIcon = possibleState.getButtonIcon();
						break;
					}
					// for compatibility to old fashioned way to set target state via other properties.
					if(possibleState.getNumeral().equals(targetState)) {
						stateName = possibleState.getStatename();
						stateID = possibleState.getId();
						stateNumeral = possibleState.getNumeral();
						stateIcon = possibleState.getIcon();
						stateDescription = possibleState.getDescription();
						stateColor = possibleState.getColor();
						stateButtonIcon = possibleState.getButtonIcon();
						break;
					}
				}
				
				StateWrapper newState = new StateWrapper(stateID, stateNumeral, stateName, stateIcon, stateDescription, stateColor, stateButtonIcon);
				gController.cmdChangeState(newState);
				
				//@todo refactor to LayoutMLButton.
				controller.addCollectableEventListener(new CollectableEventListener() {
					@Override
					public void handleCollectableEvent(
							Collectable collectable,
							MessageType messageType) {
						if (messageType.equals(MessageType.REFRESH_DONE)
								|| messageType.equals(MessageType.REFRESH_DONE_DIRECTLY)) {
							SwingUtilities.invokeLater(new Runnable() {
								
								@Override
								public void run() {
									// Must be invoked later, else focus is not set with compound components like LOVs
									EventQueue.invokeLater(new Runnable() {
										@Override
							            public void run() {
											if (btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION) != null 
													&& btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION).equals(Boolean.TRUE)) {
												KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(btn);
											}
										}
									});
								}
							});
						}
						controller.removeCollectableEventListener(this);
					}
				});
				controller.refreshCurrentCollectable(false);
			}
			else {
				Errors.getInstance().showExceptionDialog(controller.getCollectPanel(),new NuclosBusinessException(CHANGESTATEACTIONLISTENER.NO_GENERICOBJECT));
			}
		}
		catch (CommonBusinessException e) {
			Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
		}
	}

	@Override
	public boolean isRunnable(CollectController<Clct> controller, Properties probs) {
		if(controller instanceof GenericObjectCollectController) {
			GenericObjectCollectController gController = (GenericObjectCollectController) controller;
			
			if (!controller.getDetailsPanel().isVisible()) {
				// is not in details view, is in search or elsewhere
				return false;
			}
			
			if (controller.getCollectState().isDetailsModeNew()) {
				// only possible state is initial state
				return false;
			}
			
			String sTargetState = probs.getProperty("targetState");
			Integer targetState = null;
			try {
				 targetState = Integer.parseInt(sTargetState);
			} catch (NumberFormatException e) {
				return false;
			}
			
			List<StateVO> lstSubsequentStates = gController.getPossibleSubsequentStates();
			for (Iterator it = lstSubsequentStates.iterator(); it.hasNext();) {
				StateVO stateVO = (StateVO) it.next();
				if (stateVO.getId().equals(targetState))
					return true;
				// for compatibility to old fashioned way to set target state via other properties.
				if (stateVO.getNumeral().equals(targetState))
					return true;
			}
		}
		return false;
	}
}
