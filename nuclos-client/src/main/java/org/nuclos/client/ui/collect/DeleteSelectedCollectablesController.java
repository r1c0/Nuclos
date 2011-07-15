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

import org.nuclos.client.ui.collect.CollectController.MessageType;
import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;

/**
 * Controller for deleting multiple (selected) <code>Collectable</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public class DeleteSelectedCollectablesController <Clct extends Collectable>
		extends MultiCollectablesActionController<Clct, Object> {

	private static class DeleteAction<Clct extends Collectable> implements Action<Clct, Object> {
		private final CollectController<Clct> ctl;

		DeleteAction(CollectController<Clct> ctl) {
			this.ctl = ctl;
		}

		@Override
		public Object perform(Clct clct) throws CommonBusinessException {
			if (!ctl.isCollectableComplete(clct))
				clct = ctl.readCollectable(clct, false);

			if (!ctl.isDeleteAllowed(clct)) {
				throw new CommonPermissionException("L\u00f6schen ist nicht erlaubt.");
			}
			ctl.deleteCollectable(clct);
			ctl.broadcastCollectableEvent(clct, MessageType.DELETE_DONE);
			return null;
		}

		@Override
		public String getText(Clct clct) {
			return CommonLocaleDelegate.getMessage("DeleteSelectedCollectablesController.1","Datensatz {0} wird gel\u00f6scht...", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getSuccessfulMessage(Clct clct, Object oResult) {
			return CommonLocaleDelegate.getMessage("DeleteSelectedCollectablesController.2","Datensatz {0} erfolgreich gel\u00f6scht.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getConfirmStopMessage() {
			return CommonLocaleDelegate.getMessage("DeleteSelectedCollectablesController.3","Wollen Sie das L\u00f6schen der Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher gel\u00f6schten Datens\u00e4tze bleiben in jedem Fall gel\u00f6scht.)");
		}

		@Override
		public String getExceptionMessage(Clct clct, Exception ex) {
			return CommonLocaleDelegate.getMessage("DeleteSelectedCollectablesController.4","Datensatz {0} konnte nicht gel\u00f6scht werden.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct)) + ex.getMessage();
		}

		@Override
		public void executeFinalAction() throws CommonBusinessException {
			// NUCLOSINT-884 refresh afterwards
			ctl.getResultController().refreshResult();
		}
	}

	public DeleteSelectedCollectablesController(CollectController<Clct> ctl) {
		super(ctl, CommonLocaleDelegate.getMessage("DeleteSelectedCollectablesController.5","Datens\u00e4tze l\u00f6schen"), new DeleteAction<Clct>(ctl), ctl.getSelectedCollectables());
	}

}  // class DeleteSelectedCollectablesController
