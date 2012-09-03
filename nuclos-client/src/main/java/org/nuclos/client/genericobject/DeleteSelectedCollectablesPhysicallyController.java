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
package org.nuclos.client.genericobject;

import org.nuclos.client.ui.multiaction.MultiCollectablesActionController;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;

/**
 * Controller for deleting multiple (selected) <code>Collectable</code>s physically.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Uwe.Allner@novabit.de">Uwe.Allner</a>
 * @version 01.00.00
 */
public class DeleteSelectedCollectablesPhysicallyController
		extends MultiCollectablesActionController<CollectableGenericObjectWithDependants, Object> {

	private static class DeleteAction implements Action<CollectableGenericObjectWithDependants, Object> {
		private final GenericObjectCollectController ctl;

		DeleteAction(GenericObjectCollectController ctl) {
			this.ctl = ctl;
		}

		@Override
		public Object perform(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
			if (!this.ctl.isPhysicallyDeleteAllowed(clct))
				throw new CommonPermissionException(SpringLocaleDelegate.getInstance().getMessage(
						"GenericObjectCollectController.41","Endg\u00fcltiges L\u00f6schen ist nicht erlaubt."));

			GenericObjectDelegate.getInstance().remove(clct.getGenericObjectWithDependantsCVO(), true, ctl.getCustomUsage());
			return null;
		}

		@Override
		public String getText(CollectableGenericObjectWithDependants clct) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"DeleteSelectedCollectablesPhysicallyController.1", "Datensatz {0} wird endg\u00fcltig gel\u00f6scht...", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getSuccessfulMessage(CollectableGenericObjectWithDependants clct, Object oResult) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"DeleteSelectedCollectablesPhysicallyController.2", "Datensatz {0} erfolgreich endg\u00fcltig gel\u00f6scht.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getConfirmStopMessage() {
			return SpringLocaleDelegate.getInstance().getMessage(
					"DeleteSelectedCollectablesPhysicallyController.3", "Wollen Sie das endg\u00fcltige L\u00f6schen der Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher gel\u00f6schten Datens\u00e4tze bleiben in jedem Fall gel\u00f6scht.)");
		}

		@Override
		public String getExceptionMessage(CollectableGenericObjectWithDependants clct, Exception ex) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"DeleteSelectedCollectablesPhysicallyController.4", "Datensatz {0} konnte nicht endg\u00fcltig gel\u00f6scht werden.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct)) + " " + ex.getMessage();
		}

		@Override
		public void executeFinalAction() throws CommonBusinessException {
			// NUCLOSINT-884 refresh afterwards
			ctl.getSearchStrategy().search(true);
		}
	}

	public DeleteSelectedCollectablesPhysicallyController(GenericObjectCollectController ctl) {
		super(ctl, SpringLocaleDelegate.getInstance().getMessage(
				"DeleteSelectedCollectablesPhysicallyController.5", "Datens\u00e4tze endg\u00fcltig l\u00f6schen"), new DeleteAction(ctl), ctl.getListOfSelectedCollectables());
	}

}	// class DeleteSelectedCollectablesController
