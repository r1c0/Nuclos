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
package org.nuclos.client.common;

import java.awt.Component;
import java.util.List;

import org.nuclos.client.masterdata.ui.ColorCellRenderer;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

/**
 * Controller for the LDAP user synchronization dialog panel.
 */
public class SelectUserController<T extends MasterDataVO> extends SelectObjectsController<T> {

	/**
	 * LDAP user synchronization dialog panel.
	 * <p>
	 * <dl>
	 *   <dt>blue</dt><dd>user known to both systems, can be synchronized</dd>
	 *   <dt>yellow</dt><dd>user known to LDAP only, can be added to nuclos</dd>
	 *   <dt>red</dt><dd>user known to nuclos only, can be deleted in nuclos</dd>
	 * </dl>
	 * </p>
	 */
	private static class SelectUserPanel<T> extends DefaultSelectObjectsPanel<T> {

		SelectUserPanel(String text1, String text2, String text3, String text4) {
			this.labAvailableColumns.setText(text1);
			this.labSelectedColumns.setText(text2);

			this.btnLeft.setToolTipText(text3);
			this.btnRight.setToolTipText(text4);
			this.btnUp.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectUserController.1", "Markierte Benutzer nach oben verschieben"));
			this.btnDown.setToolTipText(SpringLocaleDelegate.getInstance().getMessage(
					"SelectUserController.2", "Markierte Benutzer nach unten verschieben"));

			this.btnUp.setVisible(false);
			this.btnDown.setVisible(false);

			this.btnLeft.setVisible(true);
			this.btnRight.setVisible(true);
		}
	}  // inner class SelectColumnsPanel

	public SelectUserController(Component parent, String text1, String text2, String text3, String text4) {
		super(parent, new SelectUserPanel(text1, text2, text3, text4));
		final SelectUserPanel pnl = (SelectUserPanel) getPanel();
		this.getPanel().getJListAvailableObjects().setCellRenderer(new ColorCellRenderer());
		this.getPanel().getJListSelectedObjects().setCellRenderer(new ColorCellRenderer());

		String tooltipTxt = "<html>";
		tooltipTxt += getSpringLocaleDelegate().getMessage("SelectUserController.3", "Beim Synchronisieren:") + "<br>";
		tooltipTxt += getSpringLocaleDelegate().getMessage("SelectUserController.4", "gelb - Benutzer wird in Nuclos angelegt.") + "<br>";
		tooltipTxt += getSpringLocaleDelegate().getMessage("SelectUserController.5", "blau - Benutzerdaten werden in Nuclos aktualisiert.") + "<br>";
		tooltipTxt += getSpringLocaleDelegate().getMessage("SelectUserController.6", "rot  - Benutzer wird in Nuclos gel\u00f6scht.") + "<br>";
		tooltipTxt += "</html>";
		this.getPanel().getJListAvailableObjects().setToolTipText(tooltipTxt);
		this.getPanel().getJListSelectedObjects().setToolTipText(tooltipTxt);
	}

	/**
	 * @deprecated Use getSelectedObjects(). 
	 */
	public List<MasterDataWithDependantsVOWrapper> getSelectedColumns() {
		return (List<MasterDataWithDependantsVOWrapper>) this.getSelectedObjects();
	}

	/**
	 * @deprecated Use getAvailableObjects(). 
	 */
	public List<MasterDataVO> getAvailableColumns() {
		return (List<MasterDataVO>) this.getAvailableObjects();
	}
}
