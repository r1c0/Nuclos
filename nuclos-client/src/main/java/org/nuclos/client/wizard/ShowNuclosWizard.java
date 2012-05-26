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


import org.apache.log4j.Logger;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.main.mainframe.MainFrameTabbedPane;
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
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.SpringLocaleDelegate;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;

public class ShowNuclosWizard  {
	
	private static final Logger LOG = Logger.getLogger(ShowNuclosWizard.class);

	public static class NuclosWizardRoRunnable implements Runnable {
		
		private final MainFrameTabbedPane desktopPane;
		
		public NuclosWizardRoRunnable(MainFrameTabbedPane desktopPane) {
			this.desktopPane = desktopPane;
		}
		
		@Override
		public void run() {
			try {
				ShowNuclosWizard w = new ShowNuclosWizard(false);
				w.showWizard(desktopPane);
			}
			catch (Exception e) {
				LOG.error("showWizard failed: " + e, e);
			}
		}
		
	}

	public static class NuclosWizardEditRunnable implements CommonRunnable {
		
		private final boolean editMode;
		
		private final MainFrameTabbedPane desktopPane;
		
		private final EntityMetaDataVO entity;
		
		public NuclosWizardEditRunnable(boolean editMode, MainFrameTabbedPane desktopPane, EntityMetaDataVO entity) {
			this.editMode = editMode;
			this.desktopPane = desktopPane;
			this.entity = entity;
		}
		
		@Override
		public void run() {
			ShowNuclosWizard w = new ShowNuclosWizard(editMode);
			w.setEntityToEdit(entity);
			w.showWizard(desktopPane);
		}
		
	}

	private NuclosEntityWizardStaticModel model;
	private boolean blnEditMode;
	private EntityMetaDataVO toEdit;
	
	//

	public static void main(String[] args) {
		ShowNuclosWizard show = new ShowNuclosWizard(false);
		show.showWizard(null);
	}

	private ShowNuclosWizard(boolean editMode) {
		model = new NuclosEntityWizardStaticModel();
		this.blnEditMode = editMode;
	}

	public void setEntityToEdit(EntityMetaDataVO vo) {
		this.toEdit = vo;
	}

	public void showWizard(final MainFrameTabbedPane desktopPane) {
		final SpringLocaleDelegate localeDelegate = SpringLocaleDelegate.getInstance();
		
		final MainFrameTab ifrm = Main.getInstance().getMainController().newMainFrameTab(null, 
				localeDelegate.getMessage("wizard.show.17", "Nucleus Entit\u00e4tenwizard <Neue Entit\u00e4t>"));

		NuclosEntityNameStep step1 = new NuclosEntityNameStep(localeDelegate.getMessage(
				"wizard.show.1", "Entit\u00e4ten erstellen"), 
				localeDelegate.getMessage("wizard.show.2", "Bitte geben Sie den Namen der Entit\u00e4t ein"));
		if(toEdit != null)
			step1.setEntityToEdit(toEdit);
		NuclosEntityCommonPropertiesStep step2 = new NuclosEntityCommonPropertiesStep(localeDelegate.getMessage(
				"wizard.show.3", "Definition allgemeiner Eigenschaften"), 
				localeDelegate.getMessage("wizard.show.4", "Bitte tragen Sie hier die allgemeinen Eigenschaften ein"));
		step2.setParentComponent(ifrm);
		NuclosEntityOptionStep step3 = new NuclosEntityOptionStep(localeDelegate.getMessage(
				"wizard.show.5", "Definition von Attributen"), 
				localeDelegate.getMessage("wizard.show.6", "Wie m\u00f6chten Sie die Attribute der Entit\u00e4t erstellen?"));
		NuclosEntityAttributeInputStep step4 = new NuclosEntityAttributeInputStep(localeDelegate.getMessage(
				"wizard.show.7", "Attribute bearbeiten"), 
				localeDelegate.getMessage("wizard.show.8", "F\u00fcgen Sie Attribute hinzu in dem Sie auf den Button Attribute hinzuf\u00fcgen dr\u00fccken"));
		step4.setParentComponent(ifrm);
		step4.setComplete(true);
		NuclosEntityProcessStep step41 = new NuclosEntityProcessStep(localeDelegate.getMessage(
				"wizard.show.processes", "Aktionen definieren"), 
				localeDelegate.getMessage("wizard.show.processes.summary", "Definition von Aktionen für die Verwendung unterschiedlicher Layouts und Statusmodelle."));
		NuclosEntityMenuStep step42 = new NuclosEntityMenuStep(localeDelegate.getMessage(
				"wizard.show.menu", "Menue konfigurieren"), 
				localeDelegate.getMessage("wizard.show.menu.summary", "Konfigurieren Sie die Menueeintraege für diese Entitaet. Wenn Sie keine Eintraege definieren, wird ein Standard-Menueeintrag generiert."));
		NuclosEntityTreeValueStep step5 = new NuclosEntityTreeValueStep(localeDelegate.getMessage(
				"wizard.show.9", "Baumdarstellung definieren"), "<html><body>"
				+ localeDelegate.getMessage("wizard.show.10", "Definition von Fenstertitel und Baumdarstellung")
				+ "</body></html>");
		NuclosUserGroupRightsStep step6 = new NuclosUserGroupRightsStep(localeDelegate.getMessage(
				"wizard.show.11", "Rechte verwalten"), 
				localeDelegate.getMessage("wizard.show.11", "Rechte verwalten"));
		step6.setComplete(true);
		NuclosEntityTranslationStep step7 = new NuclosEntityTranslationStep(localeDelegate.getMessage(
				"wizard.show.12", "\u00dcbersetzen"), 
				localeDelegate.getMessage("wizard.show.12", "\u00dcbersetzen"));
		step7.setComplete(true);
		NuclosEntitySQLLayoutStep step8 = new NuclosEntitySQLLayoutStep(
				localeDelegate.getMessage("wizard.show.13", "SQL und Layout erzeugen"), 
				localeDelegate.getMessage("wizard.show.14", "M\u00f6chten Sie SQL-Skripte und eine Standardmaske von Nucleus erstellen lassen"));
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

		ifrm.setTabIconFromNuclosResource("org.nuclos.client.resource.icon.glyphish-blue.81-dashboard.png");

		model.setParentFrame(ifrm);


		wizard.addWizardListener(new WizardListener() {

			@Override
			public void wizardClosed(WizardEvent e) {
				ifrm.dispose();
				ifrm.removeAll();
				desktopPane.removeTab(ifrm);
			}

			@Override
			public void wizardCancelled(WizardEvent e) {
				ifrm.dispose();
				ifrm.removeAll();
				desktopPane.removeTab(ifrm);
			}
		});

		ifrm.setLayeredComponent(WizardFrame.createFrameInScrollPane(wizard));
		desktopPane.add(ifrm);
		ifrm.setVisible(true);
	}

}
