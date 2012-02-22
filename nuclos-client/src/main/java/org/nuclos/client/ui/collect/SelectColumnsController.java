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

import java.awt.Component;

import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.common.collect.collectable.CollectableEntityField;
import org.nuclos.common2.SpringLocaleDelegate;

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

	private static class SelectColumnsPanel<T> extends DefaultSelectObjectsPanel<T> {

		SelectColumnsPanel() {
			this.labAvailableColumns.setText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.7","Verf\u00fcgbare Spalten"));
			this.labSelectedColumns.setText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.2","Ausgew\u00e4hlte Spalten"));

			this.btnLeft.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.4","Markierte Spalte(n) nicht anzeigen"));
			this.btnRight.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.3","Markierte Spalte(n) anzeigen"));
			this.btnUp.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.5","Markierte Spalte nach oben verschieben"));
			this.btnDown.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectColumnsController.6","Markierte Spalte nach unten verschieben"));

			this.btnUp.setVisible(true);
			this.btnDown.setVisible(true);
		}
	}  // inner class SelectColumnsPanel

	public SelectColumnsController(Component parent) {
		super(parent, new SelectColumnsPanel());
	}

}  // class SelectColumnsController
