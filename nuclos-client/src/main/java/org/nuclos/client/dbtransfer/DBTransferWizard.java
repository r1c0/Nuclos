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
package org.nuclos.client.dbtransfer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JButton;

import org.nuclos.common2.CommonLocaleDelegate;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardAction;
import org.pietschy.wizard.WizardModel;

public class DBTransferWizard extends Wizard {
	
	private boolean blnCancelEnabled = true;

	/**
	 * @param model
	 * @param blnImportRunning 
	 */
	public DBTransferWizard(WizardModel model) {
		super(model);
		this.setDefaultExitMode(EXIT_ON_FINISH);
	}
	
	@Override
	public Action getCancelAction() {
		return new WizardAction("cancel", this){

			@Override
			public void doAction(ActionEvent e) throws InvalidStateException {
				 getWizard().cancel();
			}

			@Override
			protected void updateState() {
				setEnabled(blnCancelEnabled);
			}

		};
	}
	

	public void setCancelEnabled(boolean blnCancelEnabled) {
		this.blnCancelEnabled = blnCancelEnabled;
	}

	@Override
	protected ButtonBar createButtonBar() {
		return new ButtonBar(this){

			@Override
			protected void layoutButtons(JButton helpButton,
				JButton previousButton, JButton nextButton, JButton lastButton,
				JButton finishButton, JButton cancelButton, JButton closeButton) {
				
				final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
				previousButton.setText(cld.getMessage("wizard.buttonbar.previous", "zur\u00fcck"));
				nextButton.setText(cld.getMessage("wizard.buttonbar.next", "weiter"));
				lastButton.setText(">>");
				finishButton.setText(cld.getMessage("wizard.buttonbar.finish", "Fertig"));
				cancelButton.setText(cld.getMessage("wizard.buttonbar.cancel", "Verwerfen"));
				cancelButton.setPreferredSize(new Dimension(80, cancelButton.getPreferredSize().height));
				closeButton.setText(cld.getMessage("wizard.buttonbar.close", "Schliessen"));
				
				super.layoutButtons(helpButton, previousButton, nextButton, lastButton,
					finishButton, cancelButton, closeButton);
			}
			
		};
	}
		
	public static ResourceBundle getResourceBundle(){
		return new WizardResourceBundle();
	}
	
	private static class WizardResourceBundle extends ResourceBundle {

		private ResourceBundle mainBundle = ResourceBundle.getBundle("org-pietschy-wizard");
		
		@Override
		public boolean containsKey(String key) {
			return mainBundle.containsKey(key);
		}

		@Override
		public Locale getLocale() {
			return mainBundle.getLocale();
		}

		@Override
		protected Set<String> handleKeySet() {
			return super.handleKeySet();
		}

		@Override
		public Set<String> keySet() {
			return mainBundle.keySet();
		}

		@Override
		public Enumeration<String> getKeys() {
			return mainBundle.getKeys();
		}

		@Override
		protected Object handleGetObject(String key) {
			if ("StaticModelOverview.title".equals(key)){
				return CommonLocaleDelegate.getInstance().getMessage(
						"wizard.statemodel.overview", "\u00dcbersicht");
			}
			return mainBundle.getString(key);
		}

	}

}
