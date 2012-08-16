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
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.nuclos.client.genericobject.GeneratorActions;
import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.masterdata.MasterDataCollectController;
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
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;


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
public class GeneratorButtonAction<Clct extends Collectable> implements CollectActionAdapter<Clct> {

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

					String sactionId = probs.getProperty("generatortoexecute");
					
					GeneratorActionVO generatorToExecute = null;
					try {
						generatorToExecute = GeneratorActions.getGeneratorAction(sactionId);
					} catch (Exception e) {
						// do nothing.
					}	
					if (generatorToExecute == null) {
						try {
							generatorToExecute = GeneratorActions.getGeneratorAction(Integer.parseInt(sactionId));
						} catch (Exception e) {
							// do nothing.
						}	
					}	
			
					if (generatorToExecute != null) {
						if (controller instanceof MasterDataCollectController) {
							if (((MasterDataCollectController)controller).getGeneratorActions().contains(generatorToExecute))
								((MasterDataCollectController)controller).cmdGenerateObject(generatorToExecute);
						}
						else if (controller instanceof GenericObjectCollectController) {
							if (((GenericObjectCollectController)controller).getGeneratorActions().contains(generatorToExecute))
								((GenericObjectCollectController)controller).cmdGenerateObject(generatorToExecute);
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
				}
				catch (UserCancelledException e) {
					return;
				}
				catch (CommonFinderException e) {
					Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
				}
				catch (CommonBusinessException e) {
					Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
				}
			}
		});
	}

	@Override
	public boolean isRunnable(CollectController<Clct> controller, Properties probs) {
		if (!controller.getDetailsPanel().isVisible()) {
			return false;
		}
		if (controller.getCollectStateModel().getCollectState().getInnerState() != CollectState.DETAILSMODE_VIEW) {
			return false;
		}
		
		String sactionId = probs.getProperty("generatortoexecute");

		GeneratorActionVO generatorToExecute = null;
		try {
			generatorToExecute = GeneratorActions.getGeneratorAction(sactionId);
		} catch (Exception e) {
			// do nothing.
		}	
		if (generatorToExecute == null) {
			try {
				generatorToExecute = GeneratorActions.getGeneratorAction(Integer.parseInt(sactionId));
			} catch (Exception e) {
				// do nothing.
			}	
		}
		
		if (generatorToExecute != null) {
			if (controller instanceof MasterDataCollectController) {
				if (((MasterDataCollectController)controller).getGeneratorActions().contains(generatorToExecute))
					return true;
			}
			else if (controller instanceof GenericObjectCollectController) {
				if (((GenericObjectCollectController)controller).getGeneratorActions().contains(generatorToExecute))
					return true;
			}
		}
		return false;
	}
}
