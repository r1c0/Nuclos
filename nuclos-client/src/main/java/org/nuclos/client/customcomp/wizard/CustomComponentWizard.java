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

package org.nuclos.client.customcomp.wizard;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.awt.Dimension;

import javax.swing.JButton;

import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.wizard.WizardFrame;
import org.nuclos.common2.CommonLocaleDelegate;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.WizardModel;


public class CustomComponentWizard extends Wizard {

	protected CustomComponentWizard(WizardModel model) {
		super(model);
		
	}
	
	@Override
	protected ButtonBar createButtonBar() {
		return new ButtonBar(this){

			@Override
			protected void layoutButtons(JButton helpButton,
				JButton previousButton, JButton nextButton, JButton lastButton,
				JButton finishButton, JButton cancelButton, JButton closeButton) {
				
				previousButton.setText(getMessage("wizard.buttonbar.previous", "zur\u00fcck"));
				nextButton.setText(getMessage("wizard.buttonbar.next", "weiter"));
				lastButton.setText(">>");
				finishButton.setText(getMessage("wizard.buttonbar.finish", "Fertig"));
				cancelButton.setText(getMessage("wizard.buttonbar.cancel", "Verwerfen"));
				cancelButton.setPreferredSize(new Dimension(80, cancelButton.getPreferredSize().height));
				closeButton.setText(getMessage("wizard.buttonbar.close", "Schliessen"));
				
				super.layoutButtons(helpButton, previousButton, nextButton, lastButton,
					finishButton, cancelButton, closeButton);
			}
			
		};
	}	
	
	public static void run() {
		final MainFrameTab tab = new MainFrameTab(CommonLocaleDelegate.getText("nuclos.resplan.wizard.title", "Resourcenplanungskomponente Wizard"));
		
		CustomComponentWizardModel model = new CustomComponentWizardModel();
		Wizard wizard = new CustomComponentWizard(model);
		model.setWizard(wizard);
		wizard.addWizardListener(new WizardListener() {
			@Override
			public void wizardClosed(WizardEvent e) {
				tab.dispose();
			}
			@Override
			public void wizardCancelled(WizardEvent e) {
				tab.dispose();
			}
		});

		tab.setLayeredComponent(WizardFrame.createFrameInScrollPane(wizard));
		tab.setTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.83-calendar.png"));
		
		MainFrame.addTab(tab);
		tab.setVisible(true);
	}
}
