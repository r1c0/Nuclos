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
package org.nuclos.client.login;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.ShutdownActions;
import org.nuclos.client.main.SwingLocaleSwitcher;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.CryptUtil;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.security.NuclosLoginException;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.security.FatalLoginException;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeRemote;

/**
 * Performs the login process.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LoginController extends Controller {
	private static final byte[] CRYPT = new byte[] {
		(byte) 0x1e, (byte) 0x63, (byte) 0xc5, (byte) 0xe6, (byte) 0x41,
		(byte) 0x82, (byte) 0x9e, (byte) 0x16, (byte) 0xff, (byte) 0xce,
		(byte) 0x59, (byte) 0xc7, (byte) 0x9a, (byte) 0x12, (byte) 0x3c,
		(byte) 0xb3
	};

	private static final String ARGUMENT_USERID = "userid";

	/**
	 * the default username that is taken if none has ever been entered before.
	 */
	private final String	          USERNAME_DEFAULT	= "";
	private final ServerConfiguration serverConfig;
	private boolean	                  passwordSaveAllowed;
	private LoginPanel	              loginPanel;
	private List<LoginListener>	      loginListeners	= new LinkedList<LoginListener>();
	private double                    shakeStepSize	    = 0;
	private final String[] args;

	public LoginController(Component parent, ServerConfiguration serverConfig, String[] args) {
		super(parent);

		this.serverConfig = serverConfig;
		this.args = args;

		try {
	        ServerMetaFacadeRemote sm = ServiceLocator.getInstance().getFacade(ServerMetaFacadeRemote.class);
	        passwordSaveAllowed = Boolean.valueOf(
	        	StringUtils.defaultIfNull(
	        		StringUtils.nullIfEmpty(
	        			sm.getServerProperty("application.settings.client.autologin.allowed")),
	        	"false"));
        }
        catch(CommonFatalException e) {
        	e.printStackTrace();
	        JOptionPane.showMessageDialog(
	        	null,
	        	"The server at " + serverConfig.getUrl() + " cannot be reached.\n\n"
	        	+ "Please contact your system administrator.",
	        	"Fatal Error",
	        	JOptionPane.ERROR_MESSAGE);
	        System.exit(1);
        }
		loginPanel = new LoginPanel(passwordSaveAllowed);

		// fill language combo with language information
		List<LocaleInfo> localeInfo = LocalUserProperties.getInstance().getLoginLocaleSelection();

		if(localeInfo == null) {
			this.loginPanel.hideLanguageSelection();
		}
		else {
			int preselectId = LocalUserProperties.getInstance().getLoginLocaleId();
			this.loginPanel.cmbbxLanguage.setModel(new DefaultComboBoxModel(localeInfo.toArray(new LocaleInfo[localeInfo.size()])));
			for(int i = 0; i < localeInfo.size(); i++)
				if(localeInfo.get(i).localeId == preselectId)
					this.loginPanel.cmbbxLanguage.setSelectedIndex(i);
		}
	}

	public void run() {
		final LocalUserProperties props = LocalUserProperties.getInstance();
		SwingLocaleSwitcher.setLocale(props.getLoginLocale());

		final JOptionPane optpn = new JOptionPane(loginPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optpn.setInitialValue(null);
		optpn.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		String sTitle = MessageFormat.format(
			props.getLoginResource(LocalUserProperties.KEY_LOGIN_TITLE),
			ApplicationProperties.getInstance().getCurrentVersion().getShortName());
		final JFrame frame = new JFrame(sTitle);
		frame.setName("frmLogin");
		frame.setIconImage(NuclosIcons.getInstance().getDefaultFrameIcon().getImage());
		frame.getContentPane().add(optpn, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(this.getParent());
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		boolean attemptAutoLogin = false;

		// fill in last entered username:
		String userName = props.getUserName();
		if(userName == null) {
			userName = USERNAME_DEFAULT;
		}

		String userid = System.getProperty("loginUsername");

		if (StringUtils.isNullOrEmpty(userid) && this.args != null) {
			for (String arg : this.args) {
				if (arg != null && arg.toLowerCase().startsWith(ARGUMENT_USERID.toLowerCase()) && arg.indexOf('=') > -1 && arg.indexOf('=') + 1 < arg.length()) {
					userid = arg.substring(arg.indexOf('=') + 1);
				}
			}
		}

		loginPanel.tfUserName.setText(userid != null ? userid : userName);
		loginPanel.tfUserName.addFocusListener(new SelectAllFocusAdapter());
		loginPanel.tfPassword.addFocusListener(new SelectAllFocusAdapter());

		// set focus to password field:
		if(!userName.equals("")) {
			String pass = StringUtils.nullIfEmpty(props.getUserPasswd());

			if (userid != null && !userid.equals(userName)) {
				pass = null;
			}

			if(pass != null && passwordSaveAllowed) {
				try {
					String dec = CryptUtil.decryptAESHex(pass, CRYPT);
					loginPanel.tfPassword.setText(dec);
					loginPanel.tfPassword.setSelectionStart(0);
					loginPanel.tfPassword.setSelectionEnd(dec.length());
					loginPanel.rememberPass.setSelected(true);
					attemptAutoLogin = true;
				}
				catch(Exception e) {
					System.err.println("Error decoding autologin-pass");
					e.printStackTrace(System.err);
				}
			}
			loginPanel.tfPassword.requestFocusInWindow();
		}
		final WindowAdapter windowlistener = new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				super.windowClosing(ev);
				optpn.setValue(JOptionPane.CANCEL_OPTION);
			}
		};
		frame.addWindowListener(windowlistener);

		PropertyChangeListener optPaneListener = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent ev) {
				final Integer iValue = (Integer) ev.getNewValue();
				if (iValue != null) {
					switch (iValue) {
					case JOptionPane.OK_OPTION:
						if(cmdPerformLogin(frame, optpn, iValue, props)) {
							frame.removeWindowListener(windowlistener);
							postProcessLogin();
							LoginController.this.fireLoginSuccessful();
							frame.dispose();
						}
						else {
							optpn.setValue(optpn.getInitialValue());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
						frame.removeWindowListener(windowlistener);
						frame.dispose();
						LoginController.this.fireLoginCanceled();
						break;
					default:
						throw new NuclosFatalException();
					}
				}
			}
		};
		optpn.addPropertyChangeListener("value", optPaneListener);

		frame.setVisible(true);

		if(attemptAutoLogin) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					if(cmdPerformLogin(frame, optpn, JOptionPane.OK_OPTION,  props)) {
						postProcessLogin();
						LoginController.this.fireLoginSuccessful();
						frame.dispose();
						return;
					}
				}
			});
		}
	}

	private void postProcessLogin() {
		List<LocaleInfo> clientCachedLocaleInfo
		= LocalUserProperties.getInstance().getLoginLocaleSelection();

		Collection<LocaleInfo> localeInfo = LocaleDelegate.getInstance().getAllLocales(false);

		LocalUserProperties props = LocalUserProperties.getInstance();
		LocaleInfo selLocale;

		if(clientCachedLocaleInfo == null || !props.hasLoginLocaleUserSetting()) {
			// Must select new locale
			JPanel pnlSelLang = new JPanel(new BorderLayout());
			pnlSelLang.setBackground(Color.WHITE);
			pnlSelLang.setBorder(BorderFactory.createEtchedBorder());
			JTextArea ta = new JTextArea(
				MessageFormat.format(
					props.getLoginResource(LocalUserProperties.KEY_LANG_SELECT),
					ApplicationProperties.getInstance().getCurrentVersion().getShortName()));
			ta.setFont(loginPanel.getFont());
			ta.setEditable(false);
			pnlSelLang.add(ta, BorderLayout.CENTER);

			JComboBox cmb = new JComboBox(localeInfo.toArray(new LocaleInfo[localeInfo.size()]));
			JPanel pnlCmb = new JPanel(new FlowLayout(FlowLayout.CENTER));
			pnlCmb.setBackground(Color.WHITE);
			pnlCmb.add(cmb);
			pnlSelLang.add(pnlCmb, BorderLayout.SOUTH);

			JOptionPane.showMessageDialog(this.loginPanel, pnlSelLang);

			selLocale = (LocaleInfo) cmb.getSelectedItem();
		}
		else {
			selLocale = (LocaleInfo) this.loginPanel.cmbbxLanguage.getSelectedItem();
		}
		LocaleDelegate.getInstance().selectLocale(localeInfo, selLocale);
	}

	private boolean cmdPerformLogin(JFrame frame, JOptionPane optpn, int selectedOption, LocalUserProperties props) {
		shakeStepSize += 6;
		loginPanel.setProgressVisible(true);

		// frame.setEnabled(false);
		// disable all buttons:
		final Class<?>[] acls = {JButton.class, JTextField.class, JPasswordField.class, JComboBox.class, JCheckBox.class};
		setSubComponentsEnabled(optpn, acls, false);
		UIUtils.paintImmediately(optpn);

		boolean result = false;
		try {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

			if (selectedOption == JOptionPane.OK_OPTION) {
				// make sure that username is in lowercase characters
				if(loginPanel.tfUserName.getText() != null) {
					loginPanel.tfUserName.setText(loginPanel.tfUserName.getText().toLowerCase());
				}
				final String sUserName = loginPanel.tfUserName.getText().trim();
				final char[] acPassword = loginPanel.tfPassword.getPassword();
				try {
					performLogin(sUserName, acPassword);
					props.setUserName(sUserName);
					props.setServerName(serverConfig.getName());

					props.setUserPasswd(
						loginPanel.rememberPass.isSelected()
						? CryptUtil.encryptAESHex(new String(acPassword), CRYPT)
					    : "");

					props.store();
					result = true;
				}
				catch (LoginException ex) {
					loginPanel.setProgressVisible(result);
					if(ex instanceof FatalLoginException){
						Errors.getInstance().showExceptionDialog(frame, ex);
					} else {
						if(ex instanceof NuclosLoginException){
							switch(((NuclosLoginException)ex).getErrorCode()){
								case 3: {
									Errors.getInstance().showExceptionDialog(frame, new LoginException(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_SERVER)));
									optpn.firePropertyChange("value", 0, JOptionPane.CLOSED_OPTION);
									break;
								}
								case 2: {
									Errors.getInstance().showExceptionDialog(frame, new LoginException(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_UPERM)));
									optpn.firePropertyChange("value", 0, JOptionPane.CLOSED_OPTION);
									break;
								}
								case 1:
								default: {
									loginPanel.shake(shakeStepSize);
									loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_UPASS));
									break;
								}
							}
						} else {
							loginPanel.shake(shakeStepSize);
							loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_UPASS));
						}
					}
				}
//				catch (Exception ex) {
//					loginPanel.setProgressVisible(result);
//					Errors.getInstance().showExceptionDialog(frame, ex);
//				}
				finally {
					StringUtils.clear(acPassword);
				}
			}
		}
		catch (CommonFatalException ex) {
			loginPanel.setProgressVisible(result);
			try {
				final Throwable tCause = ex.getCause();
				if (tCause instanceof Exception) {
					throw (Exception) tCause;
				}
				else if (tCause instanceof Error) {
					throw (Error) tCause;
				}
				else {
					throw new CommonFatalException("Unknown Throwable", ex);
				}
			}
			catch (NamingException exCause) {
				final String sMessage = StringUtils.looksEmpty(ex.getMessage()) ? LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_SERVER) : "";
				Errors.getInstance().showExceptionDialog(frame, sMessage, ex);
			}
			catch (Exception exCause) {	// everything else
				final String sMessage = StringUtils.looksEmpty(ex.getMessage()) ? LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_SERVER) : "";
				Errors.getInstance().showExceptionDialog(frame, sMessage, ex);
			}
		}
		catch (Exception ex) {
			loginPanel.setProgressVisible(result);
			Errors.getInstance().showExceptionDialog(frame, ex);
		}
		finally {
			frame.setEnabled(true);
			frame.requestFocus();
			setSubComponentsEnabled(optpn, acls, true);
			loginPanel.tfPassword.setText("");
			loginPanel.tfPassword.requestFocusInWindow();
			frame.setCursor(null);
		}
		return result;
	}

	/**
	 * @todo factor out (this is generic)
	 * <br>
	 * @param comp
	 * @param acls
	 * @param bEnabled
	 */
	private static void setSubComponentsEnabled(Component comp, final Class<?>[] acls, final boolean bEnabled) {
		if (Arrays.asList(acls).contains(comp.getClass())) {
			comp.setEnabled(bEnabled);
		}

		if (comp instanceof Container) {
			Container container = (Container) comp;

			for (Component compChild : container.getComponents()) {
				if (compChild instanceof Container) {
					setSubComponentsEnabled(compChild, acls, bEnabled);
				}
			}
		}
	}

	private void performLogin(String sUserName, char[] acPassword) throws LoginException {
		NuclosRemoteServerSession.login(sUserName, new String(acPassword));
		ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_LOGOUT, new Logout());
	}

	public synchronized void addLoginListener(LoginListener loginlistener) {
		loginListeners.add(loginlistener);
	}

	public synchronized void removeLoginListener(LoginListener loginlistener) {
		loginListeners.remove(loginlistener);
	}

	public void fireLoginSuccessful() {
		LoginEvent ev = new LoginEvent(this, this.loginPanel.tfUserName.getText(), serverConfig.getName());
		for (LoginListener loginlistener : loginListeners) {
			loginlistener.loginSuccessful(ev);
		}
	}

	public void fireLoginCanceled() {
		LoginEvent ev = new LoginEvent(this);
		for (LoginListener loginlistener : loginListeners) {
			loginlistener.loginCanceled(ev);
		}
	}

	private static class SelectAllFocusAdapter extends FocusAdapter {
		@Override
		public void focusGained(FocusEvent ev) {
			JTextComponent tc = (JTextComponent) ev.getComponent();
			tc.selectAll();
		}
	}

	private static class Logout implements Runnable {
		@Override
		public void run() {
			NuclosRemoteServerSession.logout();
		}
	}

	public void increaseLoginProgressBar(int step) {
		this.loginPanel.increaseProgress(step);
	}
}	// class LoginController
