// Copyright (C) 2010 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.installer.mode;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.client.synthetica.NuclosSyntheticaUtils;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.L10n;
import org.nuclos.installer.VersionInformation;
import org.nuclos.installer.icons.InstallerIcons;
import org.nuclos.installer.mode.wizard.ChooseDatabaseWizardStep;
import org.nuclos.installer.mode.wizard.ClientConfigurationWizardStep;
import org.nuclos.installer.mode.wizard.FinishWizardStep;
import org.nuclos.installer.mode.wizard.InformationWizardStep;
import org.nuclos.installer.mode.wizard.InstallDatabaseWizardStep;
import org.nuclos.installer.mode.wizard.InstallWizard;
import org.nuclos.installer.mode.wizard.InstallWizardModel;
import org.nuclos.installer.mode.wizard.LicenseWizardStep;
import org.nuclos.installer.mode.wizard.ServerConfigurationWizardStep;
import org.nuclos.installer.mode.wizard.SetupDatabaseWizardStep;
import org.nuclos.installer.mode.wizard.SummaryWizardStep;
import org.nuclos.installer.mode.wizard.TargetPathWizardStep;
import org.nuclos.installer.mode.wizard.UninstallWizard;
import org.nuclos.installer.mode.wizard.UninstallWizardModel;
import org.nuclos.installer.mode.wizard.UnpackWizardStep;
import org.nuclos.installer.mode.wizard.UseDatabaseWizardStep;
import org.nuclos.installer.unpack.Unpacker;
import org.pietschy.wizard.I18n;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.models.BranchingPath;
import org.pietschy.wizard.models.Condition;
import org.pietschy.wizard.models.SimplePath;

/**
 * GUI (Swing) based interactive installation. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class GuiInstaller implements Installer, Constants {

	private static final Logger log = Logger.getLogger(GuiInstaller.class);

	private JFrame frame = new JFrame();

	private BranchingPath startpath;
	private WizardModel model;
	private Wizard wizard;
	private UnpackWizardStep unpackstep;

	private Unpacker unpacker;

	public GuiInstaller() {
		init();

		startpath = new BranchingPath();
		SimplePath installDb = new SimplePath();
		SimplePath setupDb = new SimplePath();
		SimplePath useDb = new SimplePath();
		SimplePath finish = new SimplePath();

		startpath.addStep(new InformationWizardStep());
		startpath.addStep(new LicenseWizardStep());
		startpath.addStep(new TargetPathWizardStep());
		startpath.addStep(new ChooseDatabaseWizardStep());

		installDb.addStep(new InstallDatabaseWizardStep());
		setupDb.addStep(new SetupDatabaseWizardStep());
		useDb.addStep(new UseDatabaseWizardStep());

		finish.addStep(new ServerConfigurationWizardStep());
		finish.addStep(new ClientConfigurationWizardStep());
		finish.addStep(new SummaryWizardStep());

		unpackstep = new UnpackWizardStep();
		finish.addStep(unpackstep);
		finish.addStep(new FinishWizardStep());

		startpath.addBranch(installDb, new Condition() {
			@Override
			public boolean evaluate(WizardModel arg0) {
				return "install".equals(ConfigContext.getProperty(DATABASE_SETUP));
			}
		});

		startpath.addBranch(setupDb, new Condition() {
			@Override
			public boolean evaluate(WizardModel model) {
				return "setup".equals(ConfigContext.getProperty(DATABASE_SETUP));
			}
		});

		startpath.addBranch(useDb, new Condition() {
			@Override
			public boolean evaluate(WizardModel model) {
				return "use".equals(ConfigContext.getProperty(DATABASE_SETUP));
			}
		});

		installDb.setNextPath(finish);
		setupDb.setNextPath(finish);
		useDb.setNextPath(finish);
	}

	private void init() {
		try {
			NuclosSyntheticaUtils.setLookAndFeel();
			I18n.setBundle(L10n.getBundle());
		} catch (Exception e) {
			log.warn(e);
		}
	}

	@Override
	public void info(String message, Object... args) {
		log.info(L10n.getMessage(message, args));
		if (model.getActiveStep() == unpackstep) {
			unpackstep.info(message, args);
		}
		else if (model instanceof UninstallWizardModel && model.getActiveStep() == ((UninstallWizardModel)model).getRemoveWizardStep()) {
			((UninstallWizardModel)model).getRemoveWizardStep().info(message, args);
		}
		else {
			JOptionPane.showMessageDialog(this.frame, L10n.getMessage(message, args), L10n.getMessage("dialog.info.title"), JOptionPane.INFORMATION_MESSAGE);
		}
	}

	@Override
	public void warn(String message, Object... args) {
		log.warn(L10n.getMessage(message, args));
		if (askQuestion(L10n.getMessage(message, args) + System.getProperty("line.separator") + L10n.getMessage("question.continue"), QUESTION_YESNO, ANSWER_NO) == ANSWER_NO) {
			close();
		}
	}

	@Override
	public void error(String message, Object... args) {
		log.error(L10n.getMessage(message, args));
		JOptionPane.showMessageDialog(this.frame, L10n.getMessage(message, args), L10n.getMessage("dialog.error.title"), JOptionPane.ERROR_MESSAGE);
		close();
	}

	@Override
	public int askQuestion(String text, int questiontype, int automatedAnswer, Object...args) {
		if (questiontype == QUESTION_YESNO) {
			if (JOptionPane.showConfirmDialog(this.frame, L10n.getMessage(text, args), L10n.getMessage("dialog.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				return ANSWER_YES;
			} else {
				return ANSWER_NO;
			}
		} else if (questiontype == QUESTION_OKCANCEL) {
			if (JOptionPane.showConfirmDialog(this.frame, L10n.getMessage(text, args), L10n.getMessage("dialog.title"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
				return ANSWER_OK;
			} else {
				return ANSWER_CANCEL;
			}
		}
		return automatedAnswer;
	}

	@Override
	public void install(Unpacker os) throws InstallException {
		this.unpacker = os;
		this.frame = new JFrame(VersionInformation.getInstance().getName() + " " + VersionInformation.getInstance().getVersion() + " Installation");

		this.model = new InstallWizardModel(startpath, unpacker, this);
		this.wizard = new InstallWizard(model);

		this.frame.setIconImage(InstallerIcons.getFrameIcon().getImage());
		this.frame.setSize(640, 480);
		this.frame.setResizable(false);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().add(wizard);
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	@Override
	public void uninstall(Unpacker os) throws InstallException {
		this.unpacker = os;

		this.frame = new JFrame(VersionInformation.getInstance().getName() + " " + VersionInformation.getInstance().getVersion() + " Deinstallation");

		this.model = new UninstallWizardModel(unpacker, this);
		this.wizard = new UninstallWizard(model, frame);

		this.frame.setIconImage(InstallerIcons.getFrameIcon().getImage());
		this.frame.setSize(640, 480);
		this.frame.setResizable(false);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.getContentPane().add(wizard);
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	@Override
	public void close() {
		this.frame.setVisible(false);
		System.exit(0);
	}

	@Override
	public void cancel() {
		if (JOptionPane.showConfirmDialog(this.frame, L10n.getMessage("question.cancel"), L10n.getMessage("dialog.title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			this.frame.setVisible(false);
			System.exit(0);
		}
	}
}
