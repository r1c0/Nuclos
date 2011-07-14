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
package org.nuclos.client.wizard;

import static org.nuclos.common2.CommonLocaleDelegate.getMessage;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.nuclos.client.wizard.steps.NuclosEntitySQLLayoutStep;
import org.pietschy.wizard.ButtonBar;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardAction;
import org.pietschy.wizard.WizardStep;


/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
public class NuclosAttributeWizardButtonBar extends ButtonBar {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Wizard mywizard;
	
	JButton btFinish;
	JButton btCancel;

	/**
	 * @param wizard
	 */
	public NuclosAttributeWizardButtonBar(Wizard wizard) {
		super(wizard);
		this.mywizard = wizard;
		
		post();
		
	}
	
	protected void post() {
		btFinish.setAction(new WizardAction("finish", this.mywizard) {
			
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void updateState() {
		      WizardStep activeStep = getActiveStep();
		      setEnabled(activeStep != null && getModel().isLastStep(activeStep) && activeStep.isComplete() && !activeStep.isBusy());
			}
			
			@Override
			public void doAction(final ActionEvent e) throws InvalidStateException {
				WizardStep finishStep = getModel().getActiveStep();
		      try {
		      	finishStep.applyState();
		      }
		      catch(InvalidStateException ex) {
		      	updateState();
		      	return;
		      }
		      int defaultCloseOperation = getWizard().getDefaultExitMode();

		      // todo (ap): should really consider making this more OO.
		      if (defaultCloseOperation == Wizard.EXIT_ON_FINISH)
		         getWizard().getCloseAction().actionPerformed(e);
		      else if (defaultCloseOperation == Wizard.EXIT_ON_CLOSE)
		         getWizard().getCloseAction().setEnabled(true);
		      else
		         throw new InvalidStateException("Invalid finish operaion: " + defaultCloseOperation);
			}
		
		});
		btFinish.setText(getMessage("wizard.buttonbar.finish", "Fertig"));
				
	}

	
	@Override
	protected void layoutButtons(JButton helpButton,
			final JButton previousButton, JButton nextButton, JButton lastButton,
			JButton finishButton, JButton cancelButton, JButton closeButton) {
		
		nextButton.setText(getMessage("wizard.buttonbar.next", "weiter"));
		previousButton.setText(getMessage("wizard.buttonbar.previous", "zur\u00fcck"));
		lastButton.setText(">>");			
		finishButton.setText(getMessage("wizard.buttonbar.finish", "Fertig"));
		cancelButton.setText(getMessage("wizard.buttonbar.cancel", "Verwerfen"));
		closeButton.setText(getMessage("wizard.buttonbar.close", "Schliessen"));
		
		super.layoutButtons(helpButton, previousButton, nextButton, lastButton,
				finishButton, cancelButton, closeButton);

		this.equalizeButtonWidths(helpButton, previousButton, nextButton, lastButton, finishButton, cancelButton, closeButton);
		cancelButton.setPreferredSize(new Dimension(90, 25));
		
		nextButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(mywizard instanceof NuclosEntityWizard) {
					NuclosEntityWizard wi = (NuclosEntityWizard)mywizard;
					WizardStep step = wi.getModel().getActiveStep();
					if(step instanceof NuclosEntitySQLLayoutStep) {
						previousButton.setEnabled(false);
					}
				}				
			}
		});
		
		this.btFinish = finishButton;
		
		this.btCancel = cancelButton;
		
	}		

}