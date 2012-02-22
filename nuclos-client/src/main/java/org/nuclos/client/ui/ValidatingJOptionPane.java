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
package org.nuclos.client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Validating <code>JOptionPane</code>.
 * @todo refactor
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public abstract class ValidatingJOptionPane extends JOptionPane {
	
	private final Component parent;
	private final String sTitle;

	public static class ErrorInfo extends CommonBusinessException {

		/**
		 * error msg (with default value) [null]
		 **/
		private String sMessage;

		/**
		 * Component to set focus to [null]
		 **/
		private Component compInvalid;

		public ErrorInfo() {
			this(SpringLocaleDelegate.getInstance().getMessage("ValidatingJOptionPane.1", "Ung\u00fcltige Eingabe."), null);
		}

		public ErrorInfo(String sMessage, Component compInvalid) {
			super();
			this.sMessage = sMessage;
			this.compInvalid = compInvalid;
		}

		@Override
		public final String getMessage() {
			return getErrorMessage();
		}

		/**
		 * @return the error message to display to the user.
		 * null means: Don't display an error message.
		 **/
		public String getErrorMessage() {
			return sMessage;
		}

		/**
		 * @param sErrorMsg the error message to display to the user.
		 * null means: Don't display an error message.
		 **/
		public void setErrorMessage(String sErrorMsg) {
			this.sMessage = sErrorMsg;
		}

		/**
		 * @return Component to set focus to [null]
		 **/
		public Component getComponent() {
			return compInvalid;
		}

		/**
		 * @param comp Component to set focus to [null]
		 **/
		public void setComponent(Component comp) {
			this.compInvalid = comp;
		}

	}  // class ErrorInfo

	/**
	 * @see JOptionPane
	 **/
	public ValidatingJOptionPane(Component parent, String sTitle, Object oMessage) {
		this(parent, sTitle, oMessage, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
	}  // ctor

	/**
	 * @see JOptionPane
	 **/
	public ValidatingJOptionPane(Component parent, String sTitle, Object oMessage, int iMessageType, int iOptionType) {
		super(oMessage, iMessageType, iOptionType);

		this.parent = parent;
		this.sTitle = sTitle;
	}  // ctor

	/**
	 * allows subclasses to perform validation
	 * @see ErrorInfo
	 * @todo throw ValidationException
	 **/
	protected abstract void validateInput() throws ErrorInfo;

	/**
	 * shows the modal dialog.
	 * @return the selected option or button, as in the JOptionPane.showXxx() methods.
	 **/
	public int showDialog() {
		final JDialog dlg = createDialogForValidatingJOptionPane(parent, sTitle, this);

		return showOptionPaneDialog(dlg, this);
	}

	/**
	 * @return the selected button (e.g. JOptionPane.OK_OPTION)
	 **/
	private static int showOptionPaneDialog(JDialog dlg, final JOptionPane pane) {
		pane.selectInitialValue();
		dlg.setVisible(true);

		return getSelectedValue(pane);
	}

	private static int getSelectedValue(final JOptionPane pane) {
		final Object oSelectedValue = pane.getValue();

		int result = JOptionPane.CLOSED_OPTION;

		if (oSelectedValue != null) {
			if (oSelectedValue instanceof Integer) {
				result = (Integer) oSelectedValue;
			}
			else {
				// If there is an array of option buttons:
				final Object[] aoOptions = pane.getOptions();
				if (aoOptions != null) {
					for (int iOption = 0; iOption < aoOptions.length; ++iOption) {
						if (aoOptions[iOption].equals(oSelectedValue)) {
							result = iOption;
							break;
						}
					}
				}
			}
		}
		return result;
	}

	/**
	 * Creates and returns a new <code>JDialog</code> wrapping
	 * <code>pane</code> centered on the <code>parentComponent</code>
	 * in the <code>parentComponent</code>'s frame.
	 * <code>title</code> is the title of the returned dialog.
	 *
	 * No!!! The returned <code>JDialog</code> will be set up such that
	 * once it is closed, or the user clicks on the OK button,
	 * the dialog will be disposed and closed.
	 *
	 * @param parent determines the frame in which the dialog
	 *		is displayed; if the <code>parentComponent</code> has
	 *		no <code>Frame</code>, a default <code>Frame</code> is used
	 * @param sTitle the title string for the dialog
	 * @return a new <code>JDialog</code> containing this instance
	 */
	public static JDialog createDialogForValidatingJOptionPane(final Component parent, String sTitle,
			final ValidatingJOptionPane pane) {
		final JDialog result;

		final Window window = UIUtils.getWindowForComponent(parent);
		if (window instanceof Frame) {
			result = new JDialog((Frame) window, sTitle, true);
		}
		else {
			result = new JDialog((Dialog) window, sTitle, true);
		}

		final Container contentPane = result.getContentPane();

		contentPane.setLayout(new BorderLayout());
		contentPane.add(pane, BorderLayout.CENTER);
		result.pack();
		result.setLocationRelativeTo(parent);

		result.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		result.addWindowListener(new WindowAdapter() {
			boolean gotFocus = false;

			@Override
			public void windowActivated(WindowEvent ev) {
				// Once window gets focus, set initial focus
				if (!gotFocus) {
					pane.selectInitialValue();
					gotFocus = true;
				}
			}

			@Override
			public void windowClosing(WindowEvent ev) {
				pane.setValue(JOptionPane.CLOSED_OPTION);
				// !!! pane.setValue(null); ?
			}

			@Override
			public void windowClosed(WindowEvent ev) {
			}
		});  // WindowListener

		addPropertyChangeListener(pane, result, parent);

		return result;
	}

	private static void addPropertyChangeListener(final ValidatingJOptionPane pane, final Component result, final Component parent) {
		pane.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent ev) {
				final Object oValue = pane.getValue();

				if (oValue == null) {
					// ignore closing of window (via close button) - window will close automatically
				}
				else if (oValue == JOptionPane.UNINITIALIZED_VALUE) {
					// ignore reset
				}
				else if (oValue.equals(JOptionPane.OK_OPTION)) {
					// validate input:

					try {
						try {
							pane.validateInput();

							// input was ok:
							result.setVisible(false);
						}
						catch (ErrorInfo ex) {
							pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
							// Ensure that the next time the same button is pressed,
							// another PropertyChangeEvent will be fired.

							final Component compInvalid = ex.getComponent();
							if (compInvalid != null && compInvalid instanceof JTextField) {
								// Textfelder werden zus\u00e4tzlich selektiert
								final JTextField tf = (JTextField) compInvalid;
								tf.selectAll();
							}

							final String sErrorMsg = ex.getErrorMessage();
							if (sErrorMsg != null) {
								JOptionPane.showMessageDialog(result, sErrorMsg, 
										SpringLocaleDelegate.getInstance().getMessage(
												"ValidatingJOptionPane.2", "Fehler"), JOptionPane.ERROR_MESSAGE);
							}

							if (compInvalid != null) {
								compInvalid.requestFocus();
							}
						}
					}
					catch (Exception ex) {
						Errors.getInstance().showExceptionDialog(parent, ex);
						pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
						// Ensure that the next time the same button is pressed,
						// another PropertyChangeEvent will be fired.
					}
				}
				else {
					// Cancel pressed:
					result.setVisible(false);
				}
			}
		});
	}

}  // class ValidatingJOptionPane
