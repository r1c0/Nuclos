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
package org.nuclos.client.searchfilter;

import javax.swing.JComponent;

/**
 * @deprecated Maybe this will be used sometime in the future.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */

@Deprecated
public class ManageSearchFiltersController {
	public ManageSearchFiltersController(JComponent parent) {
	}

//	public void run() {
//		final ManageSearchFiltersPanel pnl = new ManageSearchFiltersPanel();
//
//		final List lstFilters = SearchFilters.getPersonalSearchFilters();
//
//		final String sTitle = "Suchfilter verwalten";
//
//		final int iBtn = JOptionPane.showConfirmDialog(parent, pnl, sTitle, JOptionPane.OK_CANCEL_OPTION,
//		    JOptionPane.PLAIN_MESSAGE);
//
//		if(iBtn == JOptionPane.OK_OPTION) {
//			SearchFilters.storePersonalSearchFilters(lstFilters);
//			try {
//				Preferences.getUserPreferences().flush();
//			}
//			catch (BackingStoreException ex) {
//				throw new NuclosFatalException(ex);
//			}
//		}
//	}

}	// class ManageSearchFiltersController
