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

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.text.MessageFormat;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.login.LoginController;
import org.nuclos.client.main.Main;
import org.nuclos.client.main.mainframe.MainFrame;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Displays error messages (especially for <code>Exception</code>s) to the user.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class Errors {

	private static final Logger LOG = Logger.getLogger(Errors.class);

	private static Errors singleton;

	private String sAppName;

	private static final String NEW_LINE = "\n";

	public static final int BUBBLE_ERROR_LINE_LENGTH = 200;

	/**
	 * @invariant criticalerrorhandler != null
	 */
	private CriticalErrorHandler criticalerrorhandler = new DefaultCriticalErrorHandler();

	public static synchronized Errors getInstance() {
		if (singleton == null) {
			singleton = new Errors();
		}
		return singleton;
	}

	/**
	 * @return the application name.
	 */
	public String getAppName() {
		return this.sAppName;
	}

	/**
	 * sets the application name. Should be set as soon as possible by each application that uses <code>Errors</code>.
	 * @param sAppName
	 */
	public void setAppName(String sAppName) {
		this.sAppName = sAppName;
	}

	/**
	 * sets the critical error handler that is to be called when a <code>java.lang.Error</code> occurs.
	 * @param criticalerrorhandler If <code>null</code>, the default critical error handler is (re)installed.
	 */
	public void setCriticalErrorHandler(CriticalErrorHandler criticalerrorhandler) {
		this.criticalerrorhandler = (criticalerrorhandler == null) ? new DefaultCriticalErrorHandler() : criticalerrorhandler;
		assert this.criticalerrorhandler != null;
	}

	/**
	 * @return the critical error handler. If no special handler is set, the default handler is returned.
	 * @postcondition result != null
	 */
	public CriticalErrorHandler getCriticalErrorHandler() {
		final CriticalErrorHandler result = this.criticalerrorhandler;
		assert result != null;
		return result;
	}

	public void showExceptionDialog(Component parent, Throwable t) {
		this.showExceptionDialog(parent, null, t);
	}

	public void showDetailedExceptionDialog(Component parent, Throwable t) {
		this.showExceptionDialog(parent, null, t, true);
	}

	public void showExceptionDialog(final Component parent, final String sErrorMsg, final Throwable t) {
		this.showExceptionDialog(parent, sErrorMsg, t, false);
	}

	public void showExceptionDialog(final Component parent, final String sErrorMsg, final Throwable t, final boolean forceDetailDialog) {
		LOG.error("showExceptionDialog: " + t.toString(), t);
		UIUtils.invokeOnDispatchThread(new Runnable() {
			@Override
			public void run() {
				// LOG.warn("showExceptionDialog: " + t, t);
				try {
					// if the reason is a RemoteAccessException, check if user needs to re-authenticate
					Throwable authexception = getCause(t, RemoteAccessException.class);
					if (authexception != null) {
						synchronized (singleton) {
							try {
								NuclosRemoteServerSession.authenticate();
							}
							catch (AuthenticationException ex2) {
								final MainFrame mf = Main.getInstance().getMainFrame();

								LoginController lc = new LoginController(mf);

								if (!lc.run(mf)) {
									String message = LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_EXIT);
									JOptionPane.showMessageDialog(mf, message, ApplicationProperties.getInstance().getName(), JOptionPane.ERROR_MESSAGE);
									System.exit(1);
								}
								else {
									return;
								}
							}
						}
					}

					// if the reason is a AccessDeniedException, exit client
					Throwable accessexception = getCause(t, AccessDeniedException.class);
					if (accessexception != null) {
						String message = LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_ACCESS_DENIED);
						JOptionPane.showMessageDialog(Main.getInstance().getMainFrame(), message, ApplicationProperties.getInstance().getName(), JOptionPane.ERROR_MESSAGE);
						System.exit(1);
					}

					if (t instanceof RuntimeException || t instanceof Error) {
						Errors.this.showDetailedExceptionDialog(parent, sErrorMsg, t, Errors.this.getAppName(), JOptionPane.ERROR_MESSAGE);
					}
					else {
						if (forceDetailDialog) {
							Errors.this.showDetailedExceptionDialog(parent, sErrorMsg, t, Errors.this.getAppName(), JOptionPane.WARNING_MESSAGE);
						} else {
							if (t instanceof CommonBusinessException && !Boolean.TRUE.equals(ApplicationProperties.getInstance().isFunctionBlockDev())) {
								Errors.this.showNiceExceptionDialog(parent, sErrorMsg, Errors.this.getAppName(), (Exception) t, JOptionPane.WARNING_MESSAGE);
							}
							else {
								Errors.this.showDetailedExceptionDialog(parent, sErrorMsg, t, Errors.this.getAppName(), JOptionPane.WARNING_MESSAGE);
							}
						}
					}
				}
				catch (Exception ex2) {
					LOG.fatal("Exception occured in showExceptionDialog:", ex2);
					LOG.fatal("Original Throwable was:", t);
					// We don't rethrow the message as we don't want to make things worse...
				}
			}
		});
	}

	protected void showNiceExceptionDialog(Component parent, String sErrorMsg, String sTitle, Exception ex,
			int iMessageType) {
		String sErrorText;
		if (sErrorMsg == null) {
			String localizedMessage = SpringLocaleDelegate.getInstance().getMessageFromResource(ex.getLocalizedMessage());
			sErrorText = localizedMessage != null ? localizedMessage : ex.getLocalizedMessage();
		}
		else {
			String resMessage = SpringLocaleDelegate.getInstance().getMessageFromResource(sErrorMsg);
			String localizedMessage = SpringLocaleDelegate.getInstance().getMessageFromResource(ex.getLocalizedMessage());

			sErrorText = (resMessage != null ? resMessage : sErrorMsg) + "\n" + (localizedMessage != null ? localizedMessage : ex.getLocalizedMessage());
		}
		LOG.debug("Checked exception occured: ", ex);

		sErrorText = formatErrorMessage(sErrorText);
		JOptionPane.showMessageDialog(parent, sErrorText, sTitle, iMessageType);
	}

	protected void showDetailedExceptionDialog(Component parent, String sErrorMsg, final Throwable t, String sTitle,
			int iMessageType) {
		final JDialog dlg;
		final Window window = UIUtils.getWindowForComponent(parent);
		if (window == null || window instanceof Frame) {
			dlg = new JDialog((Frame) window, sTitle, true);
		}
		else if (window instanceof Dialog) {
			dlg = new JDialog((Dialog) window, sTitle, true);
		}
		else {
			throw new IllegalArgumentException("parent must be null or a Frame or a Dialog.");
		}
		final ExceptionMessagePanel pnl = new ExceptionMessagePanel(dlg, iMessageType);

		// Main message:
		String resMessage = sErrorMsg != null ? SpringLocaleDelegate.getInstance().getMessageFromResource(sErrorMsg) : null;
		String sErrorText = (resMessage != null ? resMessage : failsafeGetMessage("Errors.3","An error occured"));
		final String sReasonableMessage = getReasonableMessage(t);
		if (sReasonableMessage != null) {
			sErrorText += "\n" + sReasonableMessage;
		}
		// Also give the root cause
		sErrorText += "\n\n" + getRootCause(t);

		sErrorText = formatErrorMessage(sErrorText);
		pnl.taMessage.setText(sErrorText);
		pnl.taMessage.setFont(pnl.taMessage.getFont().deriveFont(Font.PLAIN));

		// Details message:
		pnl.epDetails.setText(getDetails(t));

		// Action: copy stack trace to clipboard:
		pnl.btnCopy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ev) {
				final StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				final Transferable transferable = new StringSelection(sw.toString());
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferable, null);
			}
		});

		dlg.getRootPane().setDefaultButton(pnl.btnOK);

		dlg.getContentPane().add(pnl);
		pnl.adjustDialog(false);
		// initialize layout
		dlg.pack();

		dlg.setLocationRelativeTo(parent);

		LOG.error("Runtime exception occured: ", t);

		dlg.setVisible(true);
	}

	private String getRootCause(Throwable t) {
		Throwable result = t;
		while (result.getCause() != null) {
			result = result.getCause();
		}
		return result.toString();
	}

	/**
	 * Method for formatting the Errortext.
	 * Inserts linebreaks after MAX_LINELENGTH.
	 *
	 * The linebreak is added after a "," or replaces a found " "
	 *
	 * FIX ELISA-6484
	 * @param sErrorMessage
	 * @return
	 */
	public static String formatErrorMessage(String sErrorMessage) {
		final int MAX_LINELENGTH = 150;
		return formatErrorMessage(sErrorMessage, MAX_LINELENGTH, NEW_LINE);
	}

	public static String formatErrorMessage(String sErrorMessage, int lineLength, String sLineBreakInResult) {
		return formatErrorMessage(sErrorMessage, lineLength, NEW_LINE, sLineBreakInResult);
	}

	public static String formatErrorForBubble(String sErrorMessage) {
		return formatErrorMessage(sErrorMessage, BUBBLE_ERROR_LINE_LENGTH, NEW_LINE, "<br/>");
	}

	public static String formatErrorMessage(String sErrorMessage, int lineLength, String sLineBreakIncoming, String sLineBreakInResult) {

		StringBuffer buffer = new StringBuffer();

		String[] lines = sErrorMessage.split(sLineBreakIncoming);
		for (int i = 0; i < lines.length; i++) {
			buffer.append(lineBreak(lines[i], lineLength, sLineBreakInResult));
			if (i + 1 < lines.length)
				buffer.append(sLineBreakInResult);
		}

		return buffer.toString();
	}

	private static String lineBreak(String sMessage, int lineLength, String sLineBreak) {

		StringBuffer buffer = new StringBuffer(sMessage);

		String restString = "";
		int nextSpace = -1;
		for (int i = lineLength; i < buffer.length(); i += lineLength){
			restString = buffer.substring(i);

			nextSpace = restString.indexOf(' ');
			if (nextSpace != -1)
				buffer.replace((i+nextSpace + 1), (i+nextSpace+ 1), sLineBreak);
			else if ((nextSpace = restString.indexOf(',')) != -1) {
				buffer.insert((i+nextSpace+1), sLineBreak);
			}
		}

		return buffer.toString();
	}

	private static boolean isReasonableException(Throwable t) {
		return (t instanceof CommonBusinessException) || ((t instanceof CommonFatalException) && (!(t instanceof CommonRemoteException)));
	}

	public static String getReasonableMessage(Throwable t) {
		String result = null;

		if (t != null) {
			String resMessage = SpringLocaleDelegate.getInstance().getMessageFromResource(getRealDetailMessage(t));
			if (isReasonableException(t)) {
				result = resMessage != null ? resMessage : t.getLocalizedMessage();
			}
			else if (t.getCause() != null && t.getCause() instanceof Exception) {
				result = resMessage != null ? resMessage : getReasonableMessage(t.getCause());
			}
		}
		return result;
	}

	protected String getDetails(Throwable t) {
		final StringBuffer sb = new StringBuffer("<html>");
		sb.append("<p><b>" + failsafeGetMessage("Errors.5","Error class") + ":</b>");
		sb.append("<br>");
		sb.append(t.getClass().getName());
		sb.append("</p>");

		sb.append("<p><b>" + failsafeGetMessage("Errors.6","Error message") + ":</b>");
		sb.append("<br>");
		sb.append(t.getLocalizedMessage());
		sb.append("</p>");

		if (t instanceof SQLException) {
			final SQLException ex = (SQLException) t;
			sb.append("<p><b>" + failsafeGetMessage("Errors.8","SQL-state") + ":</b> ");
			sb.append(ex.getSQLState());
			sb.append("<br>");
			sb.append("<b>" + failsafeGetMessage("Errors.7","Native error code") + ":</b> ");
			sb.append(ex.getErrorCode());
			sb.append("</p>");
		}

		// StackTrace:
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		// Ok! (tp)
		t.printStackTrace(pw);
		pw.flush();
		sb.append("<p><b>Stack Trace:</b>");
		sb.append("<pre>");
		// Workaround for buggy EditorPane.getPreferredSize():
		// replace tab characters by spaces:
		/** @todo enter this bug in Sun's bug database */
		sb.append(sw.toString().replaceAll("\t", "    "));
		sb.append("</pre>");
		sb.append("</p>");

		return sb.toString();
	}

	/**
	 * Handles critical errors (<code>java.lang.Error</code>).
	 */
	public static interface CriticalErrorHandler {
		/**
		 * A typical action is to display a message box and exit the application afterwards.
		 * @param parent
		 * @param error
		 */
		void handleCriticalError(Component parent, Error error);
	}

	/**
	 * the default critical error handler that is installed when no special critical handler is installed.
	 * Shows an exception dialog for the given error and exits the application afterwards.
	 */
	private static class DefaultCriticalErrorHandler implements CriticalErrorHandler {
		@Override
		public void handleCriticalError(Component parent, Error error) {
			final String sMessage = failsafeGetMessage("Errors.4","A critical system error occured.\n");
			Errors.getInstance().showExceptionDialog(parent, sMessage, error);
			System.exit(1);
		}
	}	// inner class DefaultCriticalErrorHandler


	private static String failsafeGetMessage(String resid, String def, Object ... args) {
		try {
			return SpringLocaleDelegate.getInstance().getMessage(resid, null, args);
		}
		catch(Exception e) {
			LOG.info("failsafeGetMessage: " + e);
			try {
				return MessageFormat.format(def, args);
			}
			catch(Exception e2) {
				LOG.info("failsafeGetMessage: " + e2);
				return def;
			}
		}
	}

	private static String getRealDetailMessage(Throwable t) {
		Throwable cause = t;
		while (cause.getCause() != null && !isReasonableException(cause))
			cause = cause.getCause();
		return cause.getMessage();
	}

	public static <T> T getCause(Throwable t, Class<T> clazz) {
		if (t == null) {
			return null;
		}
		else if (clazz.isAssignableFrom(t.getClass())) {
			return (T) t;
		}
		else {
			return getCause(t.getCause(), clazz);
		}
	}

	public static Throwable getCause(Throwable t) {
		if (t.getCause() == null) {
			return t;
		}
		else {
			return getCause(t.getCause());
		}
	}
}	// class Errors
