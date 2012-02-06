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

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.nuclos.client.genericobject.GenericObjectCollectController;
import org.nuclos.client.masterdata.MasterDataCollectController;
import org.nuclos.client.rule.RuleDelegate;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.CollectActionAdapter;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.client.ui.collect.CollectState;
import org.nuclos.client.ui.collect.UserCancelledException;
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
	public void run(final CollectController<Clct> controller, final Properties probs) {
		if (!controller.getDetailsPanel().isVisible()) {
			return;
		}

		UIUtils.runCommandLater(controller.getFrame(), new Runnable() {
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
					Integer ruleId = null;
					try {
						ruleId = Integer.parseInt(sruleId);
					}
					catch (NumberFormatException e) {
						Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
						return;
					}
					RuleVO ruleToExecute = RuleDelegate.getInstance().get(ruleId);
					if (ruleToExecute != null) {
						List<RuleVO> rule = new ArrayList<RuleVO>();
						rule.add(ruleToExecute);
						if (controller instanceof MasterDataCollectController) {
							((MasterDataCollectController)controller).executeBusinessRules(rule, true);
						}
						else if (controller instanceof GenericObjectCollectController) {
							((GenericObjectCollectController)controller).executeBusinessRules(rule, true);
						}
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
					Errors.getInstance().showExceptionDialog(controller.getCollectPanel(), e);
				}
			}
		});
	}
}
