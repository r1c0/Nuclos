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
package org.nuclos.client.ui.collect;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common.collect.collectable.CollectableField;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Controller for updating multiple (selected) <code>Collectable</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class UpdateSelectedCollectablesController <Clct extends Collectable> extends MultiCollectablesActionController<Clct, Object> {

	public static class UpdateAction<Clct extends Collectable> implements Action<Clct, Object> {
		private final Logger log = Logger.getLogger(this.getClass());

		private final CollectController<Clct> ctl;

		private final Map<String, CollectableField>	changedFields;

		protected UpdateAction(CollectController<Clct> ctl) throws CommonBusinessException {
			this.ctl = ctl;
			this.changedFields = new HashMap<String, CollectableField>();
			for (CollectableComponent clctcomp : ctl.getEditView(false).getCollectableComponents()) {
				if (clctcomp.getDetailsModel().isValueToBeChanged()) {
					changedFields.put(clctcomp.getFieldName(), clctcomp.getField());
				}
			}
		}

		@Override
		public Object perform(Clct clct) throws CommonBusinessException {
			if (!changedFields.isEmpty()) {
				for (Map.Entry<String, CollectableField> e : changedFields.entrySet()) {
					log.debug("Field to be changed: " + e.getKey());
					clct.setField(e.getKey(), e.getValue());
				}
			}

			ctl.replaceCollectableInTableModel(ctl.updateCollectable(clct, ctl.getAdditionalDataForMultiUpdate(clct)));
			return null;
		}

		@Override
		public String getText(Clct clct) {
			return CommonLocaleDelegate.getMessage("UpdateSelectedCollectablesController.1", "Datensatz {0} wird ge\u00e4ndert...", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));//"Datensatz " + clct.getIdentifierLabel() + " wird ge\u00e4ndert...";
		}

		@Override
		public String getSuccessfulMessage(Clct clct, Object oResult) {
			return CommonLocaleDelegate.getMessage("UpdateSelectedCollectablesController.2", "Datensatz {0} erfolgreich ge\u00e4ndert.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));//"Datensatz " + clct.getIdentifierLabel() + " erfolgreich ge\u00e4ndert.";
		}

		@Override
		public String getConfirmStopMessage() {
			return CommonLocaleDelegate.getMessage("UpdateSelectedCollectablesController.3", "Wollen Sie das \u00c4ndern der Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher ge\u00e4nderten Datens\u00e4tze bleiben in jedem Fall ge\u00e4ndert.)");
			//"Wollen Sie das \u00c4ndern der Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher ge\u00e4nderten Datens\u00e4tze bleiben in jedem Fall ge\u00e4ndert.)";
		}

		@Override
		public String getExceptionMessage(Clct clct, Exception ex) {
			return CommonLocaleDelegate.getMessage("UpdateSelectedCollectablesController.4", "Datensatz {0} konnte nicht ge\u00e4ndert werden.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct)) + ex.getMessage();
			//"Datensatz " + clct.getIdentifierLabel() + " konnte nicht ge\u00e4ndert werden. " + ex.getMessage();
		}

		@Override
		public void executeFinalAction() throws CommonBusinessException {
			// jump to multi view mode:
			ctl.setCollectState(CollectState.OUTERSTATE_DETAILS, CollectState.DETAILSMODE_MULTIVIEW);
		}
	}

	public UpdateSelectedCollectablesController(CollectController<Clct> ctl) throws CommonBusinessException {
		super(ctl, CommonLocaleDelegate.getMessage("UpdateSelectedCollectablesController.5", "Datens\u00e4tze \u00e4ndern"), new UpdateAction<Clct>(ctl), ctl.getCompleteSelectedCollectables());
	}

}  // class UpdateSelectedCollectablesController
