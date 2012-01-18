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

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.resource.NuclosResourceCache;
import org.nuclos.client.wizard.steps.NuclosEntityAttributeInputStep;
import org.nuclos.client.wizard.steps.NuclosEntityCommonPropertiesStep;
import org.nuclos.client.wizard.steps.NuclosEntityMenuStep;
import org.nuclos.client.wizard.steps.NuclosEntityNameStep;
import org.nuclos.client.wizard.steps.NuclosEntityOptionStep;
import org.nuclos.client.wizard.steps.NuclosEntityProcessStep;
import org.nuclos.client.wizard.steps.NuclosEntitySQLLayoutStep;
import org.nuclos.client.wizard.steps.NuclosEntityTranslationStep;
import org.nuclos.client.wizard.steps.NuclosEntityTreeValueStep;
import org.nuclos.client.wizard.steps.NuclosUserGroupRightsStep;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common2.CommonLocaleDelegate;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

public class ShowNuclosWizard  {

	NuclosEntityWizardStaticModel model;
	boolean blnEditMode;
	EntityMetaDataVO toEdit;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ShowNuclosWizard show = new ShowNuclosWizard(false);
		show.showWizard(null, null);
	}

	public ShowNuclosWizard(boolean editMode) {
		model = new NuclosEntityWizardStaticModel();
		this.blnEditMode = editMode;
	}

	public void setEntityToEdit(EntityMetaDataVO vo) {
		this.toEdit = vo;
	}

	public void showWizard(JTabbedPane desktopPane, JFrame mainFrame) {
		final CommonLocaleDelegate cld = CommonLocaleDelegate.getInstance();
		
		final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				cld.getMessage("wizard.show.17", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));

		NuclosEntityNameStep step1 = new NuclosEntityNameStep(cld.getMessage(
				"wizard.show.1", "Entit\u00e4ten erstellen"), 
				cld.getMessage("wizard.show.2", "Bitte geben Sie den Namen der Entit\u00e4t ein"));
		if(toEdit != null)
			step1.setEntityToEdit(toEdit);
		NuclosEntityCommonPropertiesStep step2 = new NuclosEntityCommonPropertiesStep(cld.getMessage(
				"wizard.show.3", "Definition allgemeiner Eigenschaften"), 
				cld.getMessage("wizard.show.4", "Bitte tragen Sie hier die allgemeinen Eigenschaften ein"));
		step2.setParentComponent(ifrm);
		NuclosEntityOptionStep step3 = new NuclosEntityOptionStep(cld.getMessage(
				"wizard.show.5", "Definition von Attributen"), 
				cld.getMessage("wizard.show.6", "Wie m\u00f6chten Sie die Attribute der Entit\u00e4t erstellen?"));
		NuclosEntityAttributeInputStep step4 = new NuclosEntityAttributeInputStep(cld.getMessage(
				"wizard.show.7", "Attribute bearbeiten"), 
				cld.getMessage("wizard.show.8", "F\u00fcgen Sie Attribute hinzu in dem Sie auf den Button Attribute hinzuf\u00fcgen dr\u00fccken"));
		step4.setParentComponent(ifrm);
		step4.setComplete(true);
		NuclosEntityProcessStep step41 = new NuclosEntityProcessStep(cld.getMessage(
				"wizard.show.processes", "Aktionen definieren"), 
				cld.getMessage("wizard.show.processes.summary", "Definition von Aktionen für die Verwendung unterschiedlicher Layouts und Statusmodelle."));
		NuclosEntityMenuStep step42 = new NuclosEntityMenuStep(cld.getMessage(
				"wizard.show.menu", "Menue konfigurieren"), 
				cld.getMessage("wizard.show.menu.summary", "Konfigurieren Sie die Menueeintraege für diese Entitaet. Wenn Sie keine Eintraege definieren, wird ein Standard-Menueeintrag generiert."));
		NuclosEntityTreeValueStep step5 = new NuclosEntityTreeValueStep(cld.getMessage(
				"wizard.show.9", "Baumdarstellung definieren"), "<html><body>"
				+ cld.getMessage("wizard.show.10", "Definition von Fenstertitel und Baumdarstellung")
				+ "</body></html>");
		NuclosUserGroupRightsStep step6 = new NuclosUserGroupRightsStep(cld.getMessage(
				"wizard.show.11", "Rechte verwalten"), 
				cld.getMessage("wizard.show.11", "Rechte verwalten"));
		step6.setComplete(true);
		NuclosEntityTranslationStep step7 = new NuclosEntityTranslationStep(cld.getMessage(
				"wizard.show.12", "\u00dcbersetzen"), 
				cld.getMessage("wizard.show.12", "\u00dcbersetzen"));
		step7.setComplete(true);
		NuclosEntitySQLLayoutStep step8 = new NuclosEntitySQLLayoutStep(
				cld.getMessage("wizard.show.13", "SQL und Layout erzeugen"), 
				cld.getMessage("wizard.show.14", "M\u00f6chten Sie SQL-Skripte und eine Standardmaske von Nucleus erstellen lassen"));
		step8.setComplete(true);
		model.setLastVisible(false);
		model.add(step1);
		model.add(step2);
		model.add(step3);
		model.add(step4);
		model.add(step41);
		model.add(step42);
		model.add(step5);
		model.add(step6);
		model.add(step7);
		model.add(step8);


		NuclosEntityWizard wizard = new NuclosEntityWizard(model);

		model.setWizard(wizard);

		ifrm.setTabIcon(MainFrame.resizeAndCacheTabIcon(NuclosResourceCache.getNuclosResourceIcon("org.nuclos.client.resource.icon.glyphish-blue.81-dashboard.png")));

		model.setParentFrame(ifrm);


		wizard.addWizardListener(new WizardListener() {

			@Override
			public void wizardClosed(WizardEvent e) {
				ifrm.dispose();
			}

			@Override
			public void wizardCancelled(WizardEvent e) {
				ifrm.dispose();
			}
		});

		ifrm.setLayeredComponent(WizardFrame.createFrameInScrollPane(wizard));
//		ifrm.setLayeredComponent(new WizardFrame(wizard));

//		int x = desktopPane.getWidth()/2-wizard.getPreferredSize().width/2;
//      int y = desktopPane.getHeight()/2-wizard.getPreferredSize().height/2;
//      x = x<0?0:x;
//      y = y<0?0:y;
//      ifrm.setBounds(x, y, wizard.getWidth(), wizard.getHeight());


//		ifrm.pack();

		desktopPane.add(ifrm);

		ifrm.setVisible(true);

	}

}
