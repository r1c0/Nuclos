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
import java.util.Comparator;
import java.util.List;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.client.masterdata.ui.ColorCellRenderer;
import org.nuclos.client.ui.DefaultSelectObjectsPanel;
import org.nuclos.client.ui.SelectObjectsController;
import org.nuclos.client.ui.SelectObjectsPanel;
import org.nuclos.client.ui.collect.model.ResultObjects;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;

public class SelectUserController<T extends MasterDataVO> extends SelectObjectsController<T> {

	private static class SelectUserPanel extends DefaultSelectObjectsPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		SelectUserPanel(String text1, String text2, String text3, String text4) {
			super();

			this.labAvailableColumns.setText(text1);
			this.labSelectedColumns.setText(text2);

			this.btnLeft.setToolTipText(text3);
			this.btnRight.setToolTipText(text4);
			this.btnUp.setToolTipText(CommonLocaleDelegate.getMessage("SelectUserController.1", "Markierte Benutzer nach oben verschieben"));
			this.btnDown.setToolTipText(CommonLocaleDelegate.getMessage("SelectUserController.2", "Markierte Benutzer nach unten verschieben"));

			this.btnUp.setVisible(false);
			this.btnDown.setVisible(false);

			this.btnLeft.setVisible(true);
			this.btnRight.setVisible(true);
		}
	}  // inner class SelectColumnsPanel

	private SelectUserPanel pnl = null;

	public SelectUserController(Component parent, String text1, String text2, String text3, String text4) {
		super(parent);
		pnl = new SelectUserPanel(text1, text2, text3, text4);
		this.getPanel().getJListAvailableObjects().setCellRenderer(new ColorCellRenderer());
		this.getPanel().getJListSelectedObjects().setCellRenderer(new ColorCellRenderer());

		String tooltipTxt = "<html>";
		tooltipTxt += CommonLocaleDelegate.getMessage("SelectUserController.3", "Beim Synchronisieren:") + "<br>";
		tooltipTxt += CommonLocaleDelegate.getMessage("SelectUserController.4", "gelb - Benutzer wird in Nuclos angelegt.") + "<br>";
		tooltipTxt += CommonLocaleDelegate.getMessage("SelectUserController.5", "blau - Benutzerdaten werden in Nuclos aktualisiert.") + "<br>";
		tooltipTxt += CommonLocaleDelegate.getMessage("SelectUserController.6", "rot  - Benutzer wird in Nuclos gel\u00f6scht.") + "<br>";
		tooltipTxt += "</html>";
		this.getPanel().getJListAvailableObjects().setToolTipText(tooltipTxt);
		this.getPanel().getJListSelectedObjects().setToolTipText(tooltipTxt);
	}

	@Override
	protected SelectObjectsPanel getPanel() {
		return this.pnl;
	}

	public boolean run(ResultObjects<T> ro, Comparator<T> comparator) {
		return this.run(ro, new MasterDataVO.NameComparator(), CommonLocaleDelegate.getMessage("SelectUserController.7", "Mit LDAP Synchronisieren"));
	}

	@SuppressWarnings("unchecked")
	public List<MasterDataWithDependantsVOWrapper> getSelectedColumns() {
		return (List<MasterDataWithDependantsVOWrapper>) this.getSelectedObjects();
	}

	@SuppressWarnings("unchecked")
	public List<MasterDataVO> getAvailableColumns() {
		return (List<MasterDataVO>) this.getAvailableObjects();
	}
}
