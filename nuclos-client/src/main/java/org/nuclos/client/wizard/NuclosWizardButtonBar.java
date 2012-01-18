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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.wizard.steps.NuclosEntitySQLLayoutStep;
import org.nuclos.common2.CommonLocaleDelegate;
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
public class NuclosWizardButtonBar extends ButtonBar {
	
	private static final Logger LOG = Logger.getLogger(NuclosWizardButtonBar.class);

	private Wizard mywizard;
	
	private JButton btFinish;
	private JButton btCancel;

	/**
	 * @param wizard
	 */
	public NuclosWizardButtonBar(Wizard wizard) {
		super(wizard);
		this.mywizard = wizard;
		
		post();
	}
	
	protected void post() {
		btFinish.setAction(new WizardAction("finish", this.mywizard) {
			
			@Override
			protected void updateState() {
		      WizardStep activeStep = getActiveStep();
		      setEnabled(activeStep != null && getModel().isLastStep(activeStep) && activeStep.isComplete() && !activeStep.isBusy());
			}
			
			@Override
			public void doAction(final ActionEvent e) throws InvalidStateException {
				final NuclosEntityWizardStaticModel model = (NuclosEntityWizardStaticModel)getWizard().getModel();
				model.getParentFrame().lockLayer();
				
				Thread t = new Thread(new Runnable() {
					
						@Override
						public void run() {
							try {
								btCancel.setEnabled(false);
								getWizard().getCancelAction().setEnabled(false);
								getWizard().getNextAction().setEnabled(false);
								getWizard().getFinishAction().setEnabled(false);
								getWizard().getPreviousAction().setEnabled(false);

								WizardStep finishStep = getModel().getActiveStep();
								finishStep.applyState();
							} catch(InvalidStateException ex) {
								// do nothing here
								LOG.warn("run: " + ex);
							} catch(Exception ex) {
								Errors.getInstance().showExceptionDialog(NuclosWizardButtonBar.this, ex);
								getWizard().getCloseAction().actionPerformed(e);
								return;
							} finally {	
								getWizard().getCancelAction().setEnabled(true);
								getWizard().getNextAction().setEnabled(true);
								getWizard().getFinishAction().setEnabled(true);
								getWizard().getPreviousAction().setEnabled(true);
								btCancel.setEnabled(true);							
								getWizard().getCloseAction().actionPerformed(e);
								
								model.getParentFrame().unlockLayer();
							}
						}
					});
				
				t.start();
				
				}
		
		});
		
		btFinish.setText(CommonLocaleDelegate.getInstance().getMessage(
				"wizard.buttonbar.finish", "Fertig"));
	}
	
	@Override
	protected void layoutButtons(JButton helpButton,
			final JButton previousButton, JButton nextButton, JButton lastButton,
			JButton finishButton, JButton cancelButton, JButton closeButton) {
		
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		nextButton.setText(cld.getMessage(
				"wizard.buttonbar.next", "weiter"));
		previousButton.setText(cld.getMessage(
				"wizard.buttonbar.previous", "zur\u00fcck"));
		lastButton.setText(">>");			
		finishButton.setText(cld.getMessage(
				"wizard.buttonbar.finish", "Fertig"));
		cancelButton.setText(cld.getMessage(
				"wizard.buttonbar.cancel", "Verwerfen"));
		closeButton.setText(cld.getMessage(
				"wizard.buttonbar.close", "Schliessen"));
		
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
