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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.nuclos.client.genericobject.ui.EnterNameDescriptionPanel;
import org.nuclos.client.genericobject.ui.SaveNameDescriptionPanel;
import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;

/**
 * Lets the user save a filter.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class SaveFilterController {

	public static enum Command {
		None, Overwrite, New
	}

	private final JComponent parent;

	private final SearchFilters searchfilters;

	public SaveFilterController(JComponent parent, SearchFilters searchfilters) {
		this.parent = parent;
		this.searchfilters = searchfilters;
	}

	/**
	 * saves the current filter, possibly overwriting the selected filter.
	 * @param filterSelected
	 * @param filterCurrent contains the written filter when returning overwrite or new.
	 * @return the executed command (None, Overwrite, New)
	 */
	public Command runSave(final SearchFilter filterSelected, SearchFilter filterCurrent) throws NuclosBusinessException {
		Command result = Command.None;

		final boolean bRegularFilterSelected = (filterSelected != null && !filterSelected.isDefaultFilter());

		final String sOldFilterName = filterSelected.getName();
		final String sOwner = filterSelected.getOwner();

		if (!bRegularFilterSelected) {
			// it's not possible to change the default filter.
			result = Command.New;
		}
		else {
			// Ask the user if he wants to overwrite the selected filter or add a new one:
			final SaveNameDescriptionPanel pnl = new SaveNameDescriptionPanel();

			pnl.getRadioButtonOverwrite().setText(CommonLocaleDelegate.getMessage("SaveFilterController.9", "Bestehenden Filter \"{0}\" \u00e4ndern", filterSelected.getName()));
			pnl.getRadioButtonNew().setText(CommonLocaleDelegate.getMessage("SaveFilterController.7","Neuen Filter anlegen"));
			// default is overwrite:
			pnl.getRadioButtonOverwrite().setSelected(true);

			final String sTitle = CommonLocaleDelegate.getMessage("SaveFilterController.1","Aktuelle Sucheinstellung speichern");

			final int iBtn = JOptionPane.showConfirmDialog(parent, pnl, sTitle, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);

			if (iBtn == JOptionPane.OK_OPTION) {
				result = (pnl.getRadioButtonNew().isSelected() ? Command.New : Command.Overwrite);
			}
		}

		if (result != Command.None) {
			if (result == Command.Overwrite && !filterSelected.isEditable()) {
				throw new NuclosBusinessException(CommonLocaleDelegate.getMessage("SaveFilterController.4","Der Suchfilter darf von Ihnen nicht ge\u00e4ndert werden."));
			}

			final EnterNameDescriptionPanel pnlEnterFilter = new EnterNameDescriptionPanel();

			String sTitleEnterFilter;

			switch (result) {
			case New:
				sTitleEnterFilter = CommonLocaleDelegate.getMessage("SaveFilterController.8","Neuen Filter anlegen");
				break;
			case Overwrite:
				sTitleEnterFilter = CommonLocaleDelegate.getMessage("SaveFilterController.2","Bestehenden Filter \u00e4ndern");
				pnlEnterFilter.getTextFieldName().setText(filterSelected.getName());
				pnlEnterFilter.getTextFieldDescription().setText(filterSelected.getDescription());
				break;
			default:
				throw new NuclosFatalException();
			}  // switch


			int iBtnEnterFilter = showDialog(parent, sTitleEnterFilter, pnlEnterFilter, filterSelected.getName(), result);

			if (iBtnEnterFilter == JOptionPane.OK_OPTION) {
				if (bRegularFilterSelected && result == Command.Overwrite) {
					filterSelected.setSearchDeleted(filterCurrent.getSearchDeleted());
					filterCurrent.setSearchFilterVO(new SearchFilterVO(filterSelected.getSearchFilterVO()));
				}

				filterCurrent.setName(pnlEnterFilter.getTextFieldName().getText());
				filterCurrent.setDescription(pnlEnterFilter.getTextFieldDescription().getText());
			}
			else {
				result = Command.None;
			}
		}

		if (result != Command.None) {
			if (result == Command.Overwrite) {
				try {
					SearchFilterDelegate.getInstance().updateSearchFilter(filterCurrent, sOldFilterName, sOwner);
				}
				catch (NuclosBusinessException e) {
					filterCurrent = SearchFilterCache.getInstance().getSearchFilter(sOldFilterName, sOwner);
					throw e;
				}
			}
			else {
				try {
					getSearchFilters().put(filterCurrent);
				}
				catch (PreferencesException ex) {
					final String sMessage = CommonLocaleDelegate.getMessage("SaveFilterController.5","Die Benutzereinstellungen konnten nicht geschrieben werden") + ".";
					throw new NuclosFatalException(sMessage, ex);
				}
			}
		}
		return result;
	}

	private SearchFilters getSearchFilters() {
		return searchfilters;
	}

	public static int showDialog(Component parent, String sTitleEnterFilter, final EnterNameDescriptionPanel pnlEnterFilter, final String oldName, final Command cmd) {
		final ValidatingJOptionPane optpn = new ValidatingJOptionPane(parent, sTitleEnterFilter, pnlEnterFilter) {
			@Override
			protected void validateInput() throws ErrorInfo {
				final String sFilterName = pnlEnterFilter.getTextFieldName().getText();
				if (StringUtils.isNullOrEmpty(sFilterName)) {
					throw new ErrorInfo(CommonLocaleDelegate.getMessage("SaveFilterController.3","Bitte geben Sie einen Namen f\u00fcr den Filter an") + ".", pnlEnterFilter.getTextFieldName());
				}
				try {
					SearchFilter.validate(sFilterName);
				}
				catch(Exception ex) {
					throw new ErrorInfo(ex.getMessage(), pnlEnterFilter.getTextFieldName());
				}
				// Check whether the filter name exists already (in another module)
				/** @todo change the data structure so a filter name must be unique per module only */

				// Uniqueness must be checked for a new filter or if the filter name was changed:
				boolean bMustCheckUniqueness = cmd == Command.New || (cmd == Command.Overwrite && !LangUtils.equals(oldName, pnlEnterFilter.getTextFieldName().getText()));

				if (bMustCheckUniqueness && SearchFilterCache.getInstance().filterExists(sFilterName)) {
					throw new ErrorInfo(CommonLocaleDelegate.getMessage("SaveFilterController.6","Ein Filter mit diesem Namen existiert bereits") + ".", pnlEnterFilter.getTextFieldName());
				}
			}
		};

		return optpn.showDialog();
	}
}  // class SaveFilterControlle
