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
package org.nuclos.installer.mode.wizard;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.unpack.Unpacker;
import org.pietschy.wizard.InvalidStateException;

/**
 * Choose database setup option.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class ChooseDatabaseWizardStep extends AbstractWizardStep implements ActionListener, Constants {
	
	private static final Logger LOG = Logger.getLogger(ChooseDatabaseWizardStep.class);

	private JLabel label = new JLabel();

	private JRadioButton optInstall = new JRadioButton();
	private JRadioButton optSetup = new JRadioButton();
	private JRadioButton optUse = new JRadioButton();

	private ButtonGroup group = new ButtonGroup();


	private static double layout[][] = {
        { 20.0, TableLayout.FILL, 20.0 }, // Columns
        { 20.0, 20.0, 20.0, 20.0, TableLayout.FILL } };// Rows

	public ChooseDatabaseWizardStep() {
		super(L10n.getMessage("gui.wizard.choosedb.title"), L10n.getMessage("gui.wizard.choosedb.description"));

		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        label = new JLabel();
        label.setText(L10n.getMessage("gui.wizard.choosedb.label"));
        this.add(label, "0,0, 1,0");

        optInstall.setText(L10n.getMessage("gui.wizard.choosedb.install.label"));
        optInstall.addActionListener(this);
		this.add(optInstall, "1,1");

		optSetup.setText(L10n.getMessage("gui.wizard.choosedb.setup.label"));
		optSetup.addActionListener(this);
		this.add(optSetup, "1,2");

		optUse.setText(L10n.getMessage("gui.wizard.choosedb.use.label"));
		optUse.addActionListener(this);
		this.add(optUse, "1,3");

		group.add(optInstall);
		group.add(optSetup);
		group.add(optUse);
	}

	@Override
	public void prepare() {
		Unpacker u = getModel().getUnpacker();
		List<PostgresService> pgs = u.getPostgresServices();
		final boolean canInstallPostgresql = u.isPostgresBundled() && (pgs == null || pgs.size() == 0);
		LOG.info("checking if is it is possible to install postgresql... " + canInstallPostgresql);
		optInstall.setEnabled(canInstallPostgresql);

		if (!ConfigContext.containsKey(DATABASE_SETUP)) {
			if (ConfigContext.isUpdate()) {
				ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_USE);
			}
			else if (optInstall.isEnabled()) {
				ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_INSTALL);
			}
			else
			{
				ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_SETUP);
			}
		}

		if (ConfigContext.containsKey(DATABASE_SETUP)) {
			if (DBOPTION_INSTALL.equals(ConfigContext.getProperty(DATABASE_SETUP))) {
				optInstall.setSelected(true);
			}
			else if (DBOPTION_SETUP.equals(ConfigContext.getProperty(DATABASE_SETUP))) {
				optSetup.setSelected(true);
			}
			else if (DBOPTION_USE.equals(ConfigContext.getProperty(DATABASE_SETUP))) {
				optUse.setSelected(true);
			}
			setComplete(true);
		}
	}

	@Override
	protected void updateState() {
		this.setComplete(true);
		if (optInstall.isSelected()) {
			ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_INSTALL);
		}
		else if (optSetup.isSelected()) {
			ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_SETUP);
		}
		else if (optUse.isSelected()) {
			ConfigContext.setProperty(DATABASE_SETUP, DBOPTION_USE);
		}
		else {
			setComplete(false);
		}
	}

	@Override
	public void applyState() throws InvalidStateException {
		ConfigContext.getCurrentConfig().setDbDefaults(getUnpacker(), ConfigContext.getProperty(DATABASE_SETUP));
	}
}
