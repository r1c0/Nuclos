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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pietschy.wizard.WizardStep;
import org.pietschy.wizard.models.StaticModel;

import org.nuclos.client.wizard.steps.NuclosEntityAbstractStep;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeAbstractStep;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeRelationShipStep;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeValueListShipStep;

/**
* <br>
* Created by Novabit Informationssysteme GmbH <br>
* Please visit <a href="http://www.novabit.de">www.novabit.de</a>
* 
* @author <a href="mailto:marc.finke@novabit.de">Marc Finke</a>
* @version 01.00.00
*/
public class NuclosEntityWizardStaticModelOverview extends JPanel implements PropertyChangeListener {
	
   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
private StaticModel model;
   private HashMap<WizardStep, JLabel> labels = new HashMap<WizardStep, JLabel>();

   @SuppressWarnings("unchecked")
	public NuclosEntityWizardStaticModelOverview(StaticModel model) {
      this.model = model;
      this.model.addPropertyChangeListener(this);
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      JLabel title = new JLabel(getMessage("wizard.statemodel.overview", "\u00dcbersicht"));
      title.setBorder(BorderFactory.createEmptyBorder(0,4,4,4));
      title.setAlignmentX(0);
      title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title.getMaximumSize().height));
      add(title);
      int i = 1;
      for (Iterator<WizardStep> iter = model.stepIterator(); iter.hasNext();) {
         WizardStep step = iter.next();
         JLabel label = new JLabel(""+ i++ + ". " + step.getName());
         label.setBackground(Color.GRAY.brighter());
         label.setBorder(BorderFactory.createEmptyBorder(2,4,2,4));
         label.setAlignmentX(0);
         label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label.getMaximumSize().height));
         add(label);
         labels.put(step, label);
         label.addMouseListener(new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					
					
					JLabel lbClicked = (JLabel)e.getSource();
					for(WizardStep step : labels.keySet()) {
						JLabel lb = labels.get(step);
						if(lb.equals(lbClicked)) {
							if(step.isComplete()) {
								if(step instanceof NuclosEntityAbstractStep) {
									NuclosEntityAbstractStep wizardStep = (NuclosEntityAbstractStep)step;								
									wizardStep.getModel().reset();
									boolean blnSearching = true;
									while(blnSearching) {									
										WizardStep activeStep = wizardStep.getModel().getActiveStep();
										if(!activeStep.isComplete()) {
											blnSearching = false;
											break;
										}
										if(activeStep == wizardStep) {
											blnSearching = false;									
										}
										else {
											wizardStep.getModel().nextStep();
										}
									}
									break;
								}
								else if(step instanceof NuclosEntityAttributeAbstractStep){
									NuclosEntityAttributeAbstractStep wizardStep = (NuclosEntityAttributeAbstractStep)step;
									wizardStep.getModel().reset();
									boolean blnSearching = true;
									while(blnSearching) {									
										WizardStep activeStep = wizardStep.getModel().getActiveStep();
										if(!activeStep.isComplete()) {
											if(!(activeStep instanceof NuclosEntityAttributeRelationShipStep)) {
												blnSearching = false;
												break;
											}
										}
										if(activeStep == wizardStep) {
											if(!(activeStep instanceof NuclosEntityAttributeValueListShipStep)) 
												blnSearching = false;									
											else {
												blnSearching = false;
												wizardStep.getModel().nextStep();
												wizardStep.getModel().nextStep();
											}
										}
										else {
											wizardStep.getModel().nextStep();
										}
									}
									break;
								}
							}
						}
					}
				}
         	
         });
      }

      add(Box.createGlue());
   }

   @Override
public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals("activeStep"))
      {
         JLabel old = labels.get(evt.getOldValue());
         if (old != null)
            formatInactive(old);

         JLabel label = labels.get(evt.getNewValue());
         formatActive(label);
         repaint();
      }
   }

   protected void formatActive(JLabel label) {
      label.setOpaque(true);
   }

   protected void formatInactive(JLabel label) {
      label.setOpaque(false);
   }
}
