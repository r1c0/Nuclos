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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.log4j.Logger;
import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.L10n;
import org.nuclos.installer.icons.InstallerIcons;
import org.nuclos.installer.unpack.Unpacker;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

public abstract class AbstractWizardStep extends PanelWizardStep implements DocumentListener, ActionListener, Constants {

	private static final Logger log = Logger.getLogger(AbstractWizardStep.class);

	private InstallerWizardModel model;

	public AbstractWizardStep(String title) {
		this(title, null);
	}

	public AbstractWizardStep(String title, String description) {
		super(title, description);
	}

	protected InstallerWizardModel getModel() {
		return model;
	}

	protected Unpacker getUnpacker() {
		return this.model.getUnpacker();
	}

	protected void validate(String property, String value) throws InvalidStateException {
		try {
			getUnpacker().validate(property, value);
		}
		catch (Exception ex) {
			log.error("Validation failed.", ex);
			log.info(MessageFormat.format("Validation failed for {0}={1}: {2}.", property, value, ex.getLocalizedMessage()));
			throw new InvalidStateException(ex.getLocalizedMessage());
		}
	}

	protected void validatePasswordEquality(JPasswordField field1, JPasswordField field2, String labelresid) throws InvalidStateException {
		char[] pw1 = field1.getPassword();
		char[] pw2 = field2.getPassword();
		if (pw1 == null || pw1.length == 0 || pw2 == null || pw2.length == 0) {
			throw new InvalidStateException(L10n.getMessage("error.password.empty", labelresid));
		}
		else if (!Arrays.equals(pw1, pw2)) {
			throw new InvalidStateException(L10n.getMessage("error.password.match", labelresid));
		}
	}

	@Override
	public void init(WizardModel model) {
		if (model instanceof InstallerWizardModel) {
			this.model = (InstallerWizardModel)model;
		}
		else {
			throw new RuntimeException("execption.invalid.model");
		}
	}

	protected abstract void updateState();

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateState();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateState();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateState();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateState();
	}

	@Override
	public Icon getIcon() {
		return InstallerIcons.getLogo();
	}

	protected void modelToView(final String property, final JComponent component) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				String value = null;
				if (ConfigContext.containsKey(property)) {
					value = ConfigContext.getProperty(property);
				}

				if (component instanceof JTextField) {
					((JTextField)component).setText(value);
				}
				else if (component instanceof JComboBox) {
					JComboBox cb = (JComboBox)component;
					for (int i = 0; i < cb.getItemCount(); i++) {
						if (cb.getItemAt(i) instanceof ComboBoxItem && ((ComboBoxItem)cb.getItemAt(i)).value.equals(value)) {
							cb.setSelectedIndex(i);
							break;
						}
					}
				}
				else if (component instanceof JCheckBox) {
					JCheckBox cb = (JCheckBox)component;
					cb.setSelected("true".equals(value));
				}
				else {
					throw new RuntimeException("Not implemented");
				}
			}
		});

	}

	protected void viewToModel(String property, JComponent component) throws InvalidStateException {
		String value = null;

		if (component instanceof JTextField) {
			value = ((JTextField)component).getText();
		}
		else if (component instanceof JComboBox) {
			JComboBox cb = (JComboBox)component;
			if (cb.getSelectedItem() != null && cb.getSelectedItem() instanceof ComboBoxItem) {
				value = ((ComboBoxItem)cb.getSelectedItem()).value;
			}
		}
		else if (component instanceof JCheckBox) {
			JCheckBox cb = (JCheckBox)component;
			value = cb.isSelected() ? "true" : "false";
		}
		else {
			throw new RuntimeException("Not implemented");
		}
		validate(property, value);
		ConfigContext.setProperty(property, value);
	}

	protected class ComboBoxItem {
		final String value;
		final String label;

		ComboBoxItem(String value, String label) {
			super();
			this.value = value;
			this.label = label;
		}

		@Override
		public String toString() {
			return label.toString();
		}
	}
}
