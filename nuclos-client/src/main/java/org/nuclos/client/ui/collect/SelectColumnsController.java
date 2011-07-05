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

import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.SelectObjectsPanel;
import org.nuclos.client.ui.collect.model.ResultObjects;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.CommonLocaleDelegate;
import java.awt.Component;
import java.util.List;

/**
 * Controller for selecting visible columns.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class SelectColumnsController extends SelectObjectsController<CollectableEntityField> {

	private static class SelectColumnsPanel extends DefaultSelectObjectsPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SelectColumnsPanel() {
			super();

			this.labAvailableColumns.setText(CommonLocaleDelegate.getMessage("SelectColumnsController.7","Verf\u00fcgbare Spalten"));
			this.labSelectedColumns.setText(CommonLocaleDelegate.getMessage("SelectColumnsController.2","Ausgew\u00e4hlte Spalten"));

			this.btnLeft.setToolTipText(CommonLocaleDelegate.getMessage("SelectColumnsController.4","Markierte Spalte(n) nicht anzeigen"));
			this.btnRight.setToolTipText(CommonLocaleDelegate.getMessage("SelectColumnsController.3","Markierte Spalte(n) anzeigen"));
			this.btnUp.setToolTipText(CommonLocaleDelegate.getMessage("SelectColumnsController.5","Markierte Spalte nach oben verschieben"));
			this.btnDown.setToolTipText(CommonLocaleDelegate.getMessage("SelectColumnsController.6","Markierte Spalte nach unten verschieben"));

			this.btnUp.setVisible(true);
			this.btnDown.setVisible(true);
		}
	}  // inner class SelectColumnsPanel

	private final SelectColumnsPanel pnl = new SelectColumnsPanel();

	public SelectColumnsController(Component parent) {
		super(parent);
	}

	@Override
	protected SelectObjectsPanel getPanel() {
		return this.pnl;
	}

	/**
	 * runs this Controller, using the given <code>Comparator</code> for the fields.
	 * @param lstAvailableFields List<CollectableEntityField>
	 * @param lstSelectedFields List<CollectableEntityField>
	 * @return Did the user press OK?
	 */
	public boolean run(ResultObjects<CollectableEntityField> ro) {
		return this.run(ro, CommonLocaleDelegate.getMessage("SelectColumnsController.1","Anzuzeigende Spalten ausw\u00e4hlen"));
	}

	/**
	 * @return the selected columns, when the dialog is closed.
	 * @deprecated Use getSelectedObjects()
	 */
	public List<CollectableEntityField> getSelectedColumns() {
		return getSelectedObjects();
	}

	/**
	 * @return the available columns, when the dialog is closed
	 * @deprecated Use getAvailableObjects()
	 */
	public List<CollectableEntityField> getAvailableColumns() {
		return getAvailableObjects();
	}

}  // class SelectColumnsController
