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

import java.awt.KeyboardFocusManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.nuclos.client.common.EntityCollectController;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectController.CollectableEventListener;
import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.UserCancelledException;
import org.nuclos.client.ui.layoutml.LayoutMLParser;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.server.ruleengine.valueobject.RuleVO;


/**
 * Button Action executing a Business Rule on click.
 * <br>
 * NUCLOSINT-743 Rule Button Action
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author <a href="mailto:hartmut.beckschulze@novabit.de">hartmut.beckschulze</a>
 * @version 01.00.00
 */
public class ExecuteRuleButtonAction<Clct extends Collectable> implements CollectActionAdapter<Clct> {

	/**
	 */
	@Override
	public void run(final JButton btn, final CollectController<Clct> controller, final Properties probs) {
		if (!controller.getDetailsPanel().isVisible()) {
			return;
		}

		UIUtils.runCommandLater(controller.getTab(), new Runnable() {
			@Override
			public void run() {
				try {
					CollectState cs = controller.getCollectState();
					
					// Don't save: As the rule is collecting the GUI state from the model,
					// saving the thing before this is plain wrong.
					/*
					if (cs.getOuterState() == CollectState.OUTERSTATE_DETAILS && CollectState.isDetailsModeChangesPending(cs.getInnerState())) {
						controller.save();
					}
					 */

					String sruleId = probs.getProperty("ruletoexecute");
					

					RuleVO ruleToExecute = null;
					try {
						ruleToExecute = RuleCache.getInstance().get(sruleId);
					} catch (Exception e) {
						// do nothing.
					}	
					if (ruleToExecute == null) {
						try {
							ruleToExecute = RuleCache.getInstance().get(Integer.parseInt(sruleId));
						} catch (Exception e) {
							// do nothing.
						}	
					}
					
					if (ruleToExecute != null) {
						List<RuleVO> rule = new ArrayList<RuleVO>();
						rule.add(ruleToExecute);
						if (controller instanceof MasterDataCollectController) {
							((MasterDataCollectController)controller).executeBusinessRules(rule, true);
						}
						else if (controller instanceof GenericObjectCollectController) {
							((GenericObjectCollectController)controller).executeBusinessRules(rule, true);
						}
						
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
											if (btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION) != null 
													&& btn.getClientProperty(LayoutMLParser.ATTRIBUTE_NEXTFOCUSONACTION).equals(Boolean.TRUE)) {
												KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(btn);
											}
										}
									});
								}
								controller.removeCollectableEventListener(this);
							}
						});
						controller.refreshCurrentCollectable();
					}
				}
				catch (UserCancelledException e) {
					return;
				}
				catch (CommonFinderException e) {
					Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
				}
				catch (CommonBusinessException e) {
					if (controller instanceof EntityCollectController<?>) {
						EntityCollectController<?> eController = (EntityCollectController<?>) controller;
						if (!eController.handlePointerException(e)) {
							Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
						}
					} else {
						Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
					}
				}
			}
		});
	}

	@Override
	public boolean isRunnable(CollectController<Clct> controller, Properties probs) {
		if (!controller.getDetailsPanel().isVisible()) {
			return false;
		}
		
		String sruleId = probs.getProperty("ruletoexecute");

		RuleVO ruleToExecute = null;
		try {
			ruleToExecute = RuleCache.getInstance().get(sruleId);
		} catch (Exception e) {
			// do nothing.
		}	
		if (ruleToExecute == null) {
			try {
				ruleToExecute = RuleCache.getInstance().get(Integer.parseInt(sruleId));
			} catch (Exception e) {
				// do nothing.
			}	
		}
		
		if (ruleToExecute != null) {
			if (controller instanceof MasterDataCollectController) {
				if (((MasterDataCollectController)controller).getUserRules().contains(ruleToExecute))
					return true;
			}
			else if (controller instanceof GenericObjectCollectController) {
				if (((GenericObjectCollectController)controller).getUserRules().contains(ruleToExecute))
					return true;
			}
		}
		return false;
	}
}
