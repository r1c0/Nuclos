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

/**
 * Controller for restoring multiple (selected) <code>Collectable</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 01.00.00
 */
public class RestoreSelectedCollectablesController
		extends MultiCollectablesActionController<CollectableGenericObjectWithDependants, Object> {

	private static class RestoreAction implements Action<CollectableGenericObjectWithDependants, Object> {
		private final GenericObjectCollectController ctl;

		RestoreAction(GenericObjectCollectController ctl) {
			this.ctl = ctl;
		}

		@Override
		public Object perform(CollectableGenericObjectWithDependants clct) throws CommonBusinessException {
			ctl.checkedRestoreCollectable(clct);
			ctl.getSearchStrategy().search();
			return null;
		}

		@Override
		public String getText(CollectableGenericObjectWithDependants clct) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"RestoreSelectedCollectablesController.1", "Datensatz {0} wird wiederhergestellt...", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getSuccessfulMessage(CollectableGenericObjectWithDependants clct, Object oResult) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"RestoreSelectedCollectablesController.2", "Datensatz {0} erfolgreich wiederhergestellt.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct));
		}

		@Override
		public String getConfirmStopMessage() {
			return SpringLocaleDelegate.getInstance().getMessage(
					"RestoreSelectedCollectablesController.3", "Wollen Sie die Wiederherstellung der Datens\u00e4tze an dieser Stelle beenden?\n(Die bisher wiederhergestellten Datens\u00e4tze bleiben in jedem Fall erhalten.)");
		}

		@Override
		public String getExceptionMessage(CollectableGenericObjectWithDependants clct, Exception ex) {
			return SpringLocaleDelegate.getInstance().getMessage(
					"RestoreSelectedCollectablesController.4", "Datensatz {0} konnte nicht wiederhergestellt werden.", MultiCollectablesActionController.getCollectableLabel(ctl.getEntityName(), clct)) + " " + ex.getMessage();
		}

		@Override
		public void executeFinalAction() throws CommonBusinessException {
			// do nothing
		}
	}

	public RestoreSelectedCollectablesController(GenericObjectCollectController ctl) {
		super(ctl, SpringLocaleDelegate.getInstance().getMessage("RestoreSelectedCollectablesController.5", "Datens\u00e4tze wiederherstellen"), new RestoreAction(ctl), ctl.getSelectedCollectables());
	}

}	// class RestoreSelectedCollectablesController
