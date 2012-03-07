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

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.installer.L10n;
import org.pietschy.wizard.AbstractWizardModel;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

/**
 * Show and accept Nuclos license.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class UnpackWizardStep extends AbstractWizardStep {

	private static final Logger LOG = Logger.getLogger(UnpackWizardStep.class);

	private JTextArea text = new JTextArea();
	private Thread unpackThread;

	private static double layout[][] = {
        { TableLayout.FILL }, // Columns
        { TableLayout.FILL } // Rows
    };

	public UnpackWizardStep() {
		super(L10n.getMessage("gui.wizard.unpack.title"), L10n.getMessage("gui.wizard.unpack.description"));
	}

	@Override
	public void init(WizardModel model) {
		super.init(model);
		TableLayout layout = new TableLayout(this.layout);
        layout.setVGap(5);
        layout.setHGap(5);
        this.setLayout(layout);

        text.setEditable(false);

		final JScrollPane scrlpane = new JScrollPane();
		scrlpane.getViewport().add(text, null);
		this.add(scrlpane, "0,0");
	}

	@Override
	public void prepare() {
		setBusy(true);
		unpackThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					getUnpacker().unpack(getModel().getCallback());
					info("installation.finished");
					getUnpacker().startup(getModel().getCallback());
				}
				catch(/* Install */ Exception e) {
					// Ok! (tp)
					e.printStackTrace();
					LOG.error("prepare failed: " + e, e);
					getModel().getCallback().error("error.installation.failed", e.getMessage());
				}
				finally {
					setBusy(false);
					setComplete(true);
					if (getModel() instanceof AbstractWizardModel) {
						((AbstractWizardModel)getModel()).nextStep();
					}
				}
			}
		}, "UnpackWizardStep.prepare.unpack");
		unpackThread.start();
	}

	@Override
	protected void updateState() { }

	@Override
	public void applyState() throws InvalidStateException { }

	public void info(final String message, final Object ... args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					text.append(L10n.getMessage(message, args) + System.getProperty("line.separator"));
				}
				catch (Exception e) {
					LOG.error("info failed: " + e, e);
				}
			}
		});
	}
}
