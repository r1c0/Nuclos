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
package org.nuclos.client.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.security.auth.login.LoginException;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Simple GUI for the managment console.
 *
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:florian.speidel@novabit.de">florian.speidel</a>
 */
public class NuclosConsoleGui extends JPanel {
	
	private static final Logger LOG = Logger.getLogger(NuclosConsoleGui.class);

	private PipedInputStream piOut;

	private PipedOutputStream poOut;

	private PipedInputStream piErr;

	private PipedOutputStream poErr;

	private final JTextArea textArea = new JTextArea();

	private final JComboBox cmbxCommands;

	private final JTextField txfArgument;

	private final JButton btnStart = new JButton(new AbstractAction(CommonLocaleDelegate.getInstance().getMessage(
			"NuclosConsoleGui.3","Aktion starten...")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			startCommand();

		}
	});

	Thread thread = null;

	private final JButton btnCancel = new JButton(new AbstractAction(CommonLocaleDelegate.getInstance().getMessage(
			"NuclosConsoleGui.2","Aktion abbrechen...")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (thread != null) {
				thread.stop();
			}
		}
	});

	private static final List<String> shortCuts = NuclosConsole.getInstance().LSTCOMMANDS;

	private JButton btnClearOutput = new JButton(new AbstractAction(CommonLocaleDelegate.getInstance().getMessage(
			"NuclosConsoleGui.5","Ausgabe l\u00f6schen")) {
		@Override
		public void actionPerformed(ActionEvent e) {
			textArea.setText("");
		}
	});

	/**
	 * @param sShortCut a command short cut to use in combobox
	 */
	public static void addShortCut(String sShortCut) {
		shortCuts.add(sShortCut);
		Collections.sort(shortCuts);
	}

	public NuclosConsoleGui() {
		super(new BorderLayout());

		cmbxCommands = new JComboBox();

		txfArgument = new JTextField();
		textArea.setEditable(false);
		this.add(createCmdPanel(), BorderLayout.NORTH);
		this.add(new JScrollPane(textArea), BorderLayout.CENTER);
		this.add(createButtonPnl(), BorderLayout.SOUTH);
	}

	private JPanel createButtonPnl() {
		final JPanel pnlButtons = new JPanel();
		pnlButtons.setLayout(new BoxLayout(pnlButtons, BoxLayout.PAGE_AXIS));

		pnlButtons.add(Box.createRigidArea(new Dimension(0, 5)));

		pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		final Box btnBox = Box.createHorizontalBox();
		btnBox.add(btnStart);
		btnBox.add(btnCancel);
		btnBox.add(btnClearOutput);
		btnCancel.setEnabled(false);
		pnlButtons.add(btnBox);

		return pnlButtons;
	}

	private void showShortCuts() {
		cmbxCommands.addItem(CommonLocaleDelegate.getInstance().getMessage(
				"NuclosConsoleGui.8","Manuelle Eingabe (Argument)"));
		if (shortCuts.size() == 0) {
			cmbxCommands.setEnabled(false);
		}
		else {
			for (String sShotCut : shortCuts) {
				cmbxCommands.addItem(sShotCut);
			}
		}
	}

	private JPanel createCmdPanel() {
		JPanel cmdPnl = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		cmdPnl.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(5, 5, 5, 5);
		JLabel actLabel = new JLabel(CommonLocaleDelegate.getInstance().getMessage("NuclosConsoleGui.1","Aktion"));
		actLabel.setPreferredSize(new Dimension(150, 20));
		constraints.gridx = 0;
		constraints.gridy = 0;
		cmdPnl.add(actLabel, constraints);
		cmbxCommands.setPreferredSize(new Dimension(600, 20));
		constraints.gridx = 1;
		constraints.gridy = 0;
		cmdPnl.add(cmbxCommands, constraints);
		JLabel argumentsLabel = new JLabel(CommonLocaleDelegate.getInstance().getMessage("NuclosConsoleGui.4","Argumente"));
		argumentsLabel.setPreferredSize(new Dimension(150, 20));
		constraints.gridx = 0;
		constraints.gridy = 1;
		cmdPnl.add(argumentsLabel, constraints);
		txfArgument.setPreferredSize(new Dimension(600, 20));
		constraints.gridx = 1;
		constraints.gridy = 1;
		cmdPnl.add(txfArgument, constraints);

		return cmdPnl;
	}

	private void startCommand() {

		final ArrayList<String> commands = new ArrayList<String>();

		if (cmbxCommands.getSelectedIndex() != 0) {
			commands.add(cmbxCommands.getSelectedItem().toString());
		}
		// Added handling of blanks in arguments, i.g. for path and file names
		StringTokenizer stArgument = new StringTokenizer(txfArgument.getText(), "\"", true);
		boolean bHasSpaceCharacters = false;
		String sArgumentContent;

		while (stArgument.hasMoreTokens()) {
			sArgumentContent = stArgument.nextToken();
			if (sArgumentContent.equals("\"")) {
				bHasSpaceCharacters = !bHasSpaceCharacters;
			}
			else {
				if (bHasSpaceCharacters) {
					commands.add(sArgumentContent);
				}
				else {
					StringTokenizer stToken = new StringTokenizer(sArgumentContent, " ");
					while (stToken.hasMoreTokens()) {
						commands.add(stToken.nextToken());
					}
				}
			}
		}

		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Set up System.out
					piOut = new PipedInputStream();
					poOut = new PipedOutputStream(piOut);

					System.setOut(new PrintStream(poOut, true));

					// Set up System.err
					piErr = new PipedInputStream();
					poErr = new PipedOutputStream(piErr);

					System.setErr(new PrintStream(poErr, true));
				}
				catch (IOException e) {
					LOG.error("System.setErr failed", e);
				}
				catch (Exception e) {
					LOG.error("System.setErr failed", e);
				}

				// Create reader threads
				new ReaderThread(piOut).start();
				new ReaderThread(piErr).start();

				try {
					NuclosConsole.getInstance().parseAndInvoke(commands.toArray(new String[0]), false);
				}
				catch (CommonBusinessException ex) {
					LOG.error("parseAndInvoke failed", ex);
				}
				catch (Exception ex) {
					LOG.error("parseAndInvoke failed", ex);
				}

				/** @todo what about resetting System.out/err? */
			}
		});
		thread.start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				UIUtils.invokeOnDispatchThread(new Runnable() {
					@Override
					public void run() {
						try {
							btnStart.setEnabled(false);
							btnCancel.setEnabled(true);
						}
						catch (Exception e) {
							LOG.error("startCommand failed: " + e, e);
						}
					}
				});
				try {
					thread.join();
				}
				catch (InterruptedException e) {
					/** @todo !!! */
					LOG.error(e);
				}
				// OK! (tp)
				System.out.println();
				UIUtils.invokeOnDispatchThread(new Runnable() {
					@Override
					public void run() {
						try {
							btnStart.setEnabled(true);
							btnCancel.setEnabled(false);
						}
						catch (Exception e) {
							LOG.error("startCommand failed: " + e, e);
						}
					}
				});
			}
		}).start();
	}

	public static JFrame showInFrame(JComponent parent) {
		NuclosConsoleGui panel = new NuclosConsoleGui();
		Collections.sort(shortCuts);

		panel.showShortCuts();
		final JFrame frame = new JFrame();
		frame.setTitle(CommonLocaleDelegate.getInstance().getMessage("NuclosConsoleGui.7","Management Console"));
		frame.setSize(new Dimension(800, 500));
		frame.setResizable(false);
		frame.setLocationRelativeTo(parent);
		frame.setIconImage(NuclosIcons.getInstance().getFrameIcon().getImage());
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.setVisible(true);
		return frame;
	}

	class ReaderThread extends Thread {
		PipedInputStream pi;

		ReaderThread(PipedInputStream pi) {
			this.pi = pi;
		}

		@Override
		public void run() {
			final byte[] buf = new byte[1024];
			try {
				while (true) {
					final int len = pi.read(buf);
					if (len == -1) {
						break;
					}
					textArea.append(new String(buf, 0, len));

					// Make sure the last line is always visible
					textArea.setCaretPosition(textArea.getDocument()
							.getLength());
				}
			}
			catch (Exception e) {
				LOG.error("ReaderThread failed: " + e, e);
			}
		}
	}

	private static void login(String sUsername, String sPassword) throws LoginException {
		NuclosRemoteServerSession.login(sUsername, sPassword);
	}

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					NuclosRemoteServerSession.logout();
				}
				catch (Exception e) {
					LOG.error("main failed: " + e, e);
				}
			}
		}));
		final LoginPanel pnlLogin = new LoginPanel();
		if (JOptionPane.showConfirmDialog(pnlLogin, pnlLogin, "NucleusConsoleGUI Login", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.YES_OPTION)
		{
			try {
				login(pnlLogin.loginField.getText(), new String(pnlLogin.passwordField.getPassword()));
				final JFrame frame = showInFrame(null);
				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			}
			catch (LoginException e) {
				JOptionPane.showMessageDialog(pnlLogin, "Login fehlgeschlagen");
				System.exit(-1);
			}
		}
	}

	static class LoginPanel extends JPanel {
		private JLabel lbUser = new JLabel(CommonLocaleDelegate.getInstance().getMessage(
				"NuclosConsoleGui.6","Benutzername"));
		private JLabel lbPassword = new JLabel(CommonLocaleDelegate.getInstance().getMessage(
				"NuclosConsoleGui.9","Passwort"));
		JTextField loginField = new JTextField();
		JPasswordField passwordField = new JPasswordField();

		LoginPanel() {
			this.setLayout(new GridLayout(2, 2));
			this.add(lbUser);
			this.add(loginField);
			this.add(lbPassword);
			this.add(passwordField);
		}
	} // class LoginPanel

}	// class NuclosConsoleGui
