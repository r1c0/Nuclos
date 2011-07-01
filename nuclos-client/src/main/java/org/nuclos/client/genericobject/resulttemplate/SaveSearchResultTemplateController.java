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
package org.nuclos.client.genericobject.resulttemplate;

import java.util.prefs.BackingStoreException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.client.genericobject.ui.EnterNameDescriptionPanel;
import org.nuclos.client.genericobject.ui.SaveNameDescriptionPanel;
import org.nuclos.client.ui.ValidatingJOptionPane;
import org.nuclos.common.NuclosFatalException;

/**
 * Lets the user save a search result template.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class SaveSearchResultTemplateController {
	
	public static enum Command {
		None, Overwrite, New
	}

	private final JComponent parent;

	private final ResultTemplates.SearchResultTemplates templates;

	public SaveSearchResultTemplateController(JComponent parent, ResultTemplates.SearchResultTemplates templates) {
		this.parent = parent;
		this.templates = templates;
	}

	/**
	 * saves the current search result template, possibly overwriting the selected search result template.
	 * @param templateSelected
	 * @param templateCurrent contains the written search result template when returning overwrite or new.
	 * @return the executed command (None, Overwrite, New)
	 */
	public Command runSave(final SearchResultTemplate templateSelected, SearchResultTemplate templateCurrent) {
		Command result = Command.None;

		final boolean bRegularTemplateSelected = (templateSelected != null && !templateSelected.isDefaultTemplate());

		if (!bRegularTemplateSelected) {
			// it's not possible to change the default template.
			result = Command.New;
		}
		else {
			// Ask the user if he wants to overwrite the selected template or add a new one:
			final SaveNameDescriptionPanel pnl = new SaveNameDescriptionPanel();

			pnl.getRadioButtonOverwrite().setText(CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.1", "Bestehende Suchergebnisvorlage \"{0}\" \u00e4ndern", templateSelected.getName()));
			pnl.getRadioButtonNew().setText(CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.2", "Neue Suchergebnisvorlage anlegen"));
			// default is overwrite:
			pnl.getRadioButtonOverwrite().setSelected(true);

			final String sTitle = CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.3", "Aktuelles Suchergebnisformat speichern");

			final int iBtn = JOptionPane.showConfirmDialog(parent, pnl, sTitle, JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			if (iBtn == JOptionPane.OK_OPTION) {
				result = (pnl.getRadioButtonNew().isSelected() ? Command.New : Command.Overwrite);
			}
		}

		if (result != Command.None) {
			final EnterNameDescriptionPanel pnlEnterTemplate = new EnterNameDescriptionPanel();

			String sTitleEnterTemplate;

			switch (result) {
				case New:
					sTitleEnterTemplate = CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.2", "Neue Suchergebnisvorlage anlegen");
					break;
				case Overwrite:
					sTitleEnterTemplate = CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.4", "Bestehende Suchergebnisvorlage \u00e4ndern");
					pnlEnterTemplate.getTextFieldName().setText(templateSelected.getName());
					pnlEnterTemplate.getTextFieldDescription().setText(templateSelected.getDescription());
					break;
				default:
					throw new NuclosFatalException();
			}  // switch

			// "final version of result" - needed for inner class:
			final Command resultFinal = result;

			final ValidatingJOptionPane optpn = new ValidatingJOptionPane(parent, sTitleEnterTemplate, pnlEnterTemplate) {
				@Override
				protected void validateInput() throws ErrorInfo {
					final String sTemplateName = pnlEnterTemplate.getTextFieldName().getText();
					if (StringUtils.isNullOrEmpty(sTemplateName)) {
						throw new ErrorInfo(CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.5", "Bitte geben Sie einen Namen f\u00fcr die Suchergebnisvorlage an."), pnlEnterTemplate.getTextFieldName());
					}
					try {
						SearchResultTemplate.validate(sTemplateName);
					}
					catch(Exception ex) {
						throw new ErrorInfo(ex.getMessage(), pnlEnterTemplate.getTextFieldName());
					}
					// Check whether the search result template name exists already (in another module)
					/** @todo change the data structure so a template name must be unique per module only */

				// Uniqueness must be checked for a new template or if the template name was changed:
				final boolean bMustCheckUniqueness = (resultFinal == Command.New) ||
						(resultFinal == Command.Overwrite && !LangUtils.equals(templateSelected.getName(), pnlEnterTemplate.getTextFieldName().getText()));

					if (bMustCheckUniqueness && getSearchResultTemplates().contains(sTemplateName)) {
						throw new ErrorInfo(CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.6", "Eine Suchergebnisvorlage mit diesem Namen existiert bereits."), pnlEnterTemplate.getTextFieldName());
					}
				}
			};

			final int iBtnEnterTemplate = optpn.showDialog();

			if (iBtnEnterTemplate == JOptionPane.OK_OPTION) {
				templateCurrent.setName(pnlEnterTemplate.getTextFieldName().getText());
				templateCurrent.setDescription(pnlEnterTemplate.getTextFieldDescription().getText());
			}
			else {
				result = Command.None;
			}
		}

		if (result != Command.None) {
			if (result == Command.Overwrite) {
				getSearchResultTemplates().remove(templateSelected.getName());
			}
			try {
				getSearchResultTemplates().put(templateCurrent);
				ClientPreferences.getUserPreferences().flush();
			}
			catch (PreferencesException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.7", "Die Benutzereinstellungen konnten nicht geschrieben werden.");
				throw new NuclosFatalException(sMessage, ex);
			}
			catch (BackingStoreException ex) {
				final String sMessage = CommonLocaleDelegate.getMessage("SaveSearchResultTemplateController.7", "Die Benutzereinstellungen konnten nicht geschrieben werden.");
				throw new NuclosFatalException(sMessage, ex);
			}
		}
		return result;
	}

	private ResultTemplates.SearchResultTemplates getSearchResultTemplates() {
		return this.templates;
	}
}
