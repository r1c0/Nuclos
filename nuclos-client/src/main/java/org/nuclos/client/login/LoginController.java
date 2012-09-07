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
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.naming.NamingException;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.NuclosCollectableEntityProvider;
import org.nuclos.client.common.ShutdownActions;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.genericobject.Modules;
import org.nuclos.client.livesearch.LiveSearchController;
import org.nuclos.client.main.ChangePasswordPanel;
import org.nuclos.client.main.StartUp;
import org.nuclos.client.main.SwingLocaleSwitcher;
import org.nuclos.client.security.NuclosRemoteServerSession;
import org.nuclos.client.ui.Controller;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.CryptUtil;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.security.RemoteAuthenticationManager;
import org.nuclos.common2.ContextConditionVariable;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeRemote;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;

/**
 * Performs the login process.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LoginController extends Controller<Component> {

	private static final Logger LOG = Logger.getLogger(LoginController.class);
	
	private static final byte[] CRYPT = new byte[] {
		(byte) 0x1e, (byte) 0x63, (byte) 0xc5, (byte) 0xe6, (byte) 0x41,
		(byte) 0x82, (byte) 0x9e, (byte) 0x16, (byte) 0xff, (byte) 0xce,
		(byte) 0x59, (byte) 0xc7, (byte) 0x9a, (byte) 0x12, (byte) 0x3c,
		(byte) 0xb3
	};

	private static final String ARGUMENT_USERID = "userid";
	private static final String ARGUMENT_USER_ID = "user_id";

	/**
	 * the default username that is taken if none has ever been entered before.
	 */
	private final String	          USERNAME_DEFAULT	= "";
	private boolean	                  passwordSaveAllowed;
	private LoginPanel	              loginPanel;
	private List<LoginListener>	      loginListeners	= new LinkedList<LoginListener>();
	private double                    shakeStepSize	    = 0;
	private final String[] args;
	
	private ContextConditionVariable clientContextCondition;
	
	// former Spring injection
	
	private ServerMetaFacadeRemote serverMetaFacadeRemote;
	
	private NuclosRemoteServerSession nuclosRemoteServerSession;
	
	// end of former Spring injection
	
	private LoginController() {
		this(null, new String[] {}, null, null);
	}

	public LoginController(Component parent, String[] args, ClassPathXmlApplicationContext startupContext, ContextConditionVariable clientContextCondition) {
		super(parent);
		this.args = args;
		this.clientContextCondition = clientContextCondition;
		try {
			setServerMetaFacadeRemote(startupContext.getBean(ServerMetaFacadeRemote.class));
			setNuclosRemoteServerSession(startupContext.getBean(NuclosRemoteServerSession.class));
			
	        passwordSaveAllowed = Boolean.valueOf(
	        	StringUtils.defaultIfNull(
	        		StringUtils.nullIfEmpty(
	        			serverMetaFacadeRemote.getServerProperty("application.settings.client.autologin.allowed")),
	        	"false"));
        }
        catch(CommonFatalException e) {
        	LOG.fatal("LoginController failed: " + e, e);
        	// Ok! (tp)
        	e.printStackTrace();
	        JOptionPane.showMessageDialog(
	        	null,
	        	"The server at " + System.getProperty("url.remoting") + " cannot be reached.\n\n"
	        	+ "Please contact your system administrator.",
	        	"Fatal Error",
	        	JOptionPane.ERROR_MESSAGE);
	        System.exit(1);
        }
		loginPanel = LoginPanel.getInstance();
		loginPanel.enableRememberCheckbox(passwordSaveAllowed);

		// fill language combo with language information
		List<LocaleInfo> localeInfo = LocalUserProperties.getInstance().getLoginLocaleSelection();

		if(localeInfo == null) {
			this.loginPanel.hideLanguageSelection();
		}
		else {
			int preselectId = LocalUserProperties.getInstance().getLoginLocaleId();
			loginPanel.getLanguageComboBox().setModel(new DefaultComboBoxModel(localeInfo.toArray(new LocaleInfo[localeInfo.size()])));
			for(int i = 0; i < localeInfo.size(); i++)
				if(localeInfo.get(i).localeId == preselectId)
					loginPanel.getLanguageComboBox().setSelectedIndex(i);
		}
	}

	public LoginController(Component parent) {
		super(parent);
		this.args = new String[]{};
	}
	
	final void setServerMetaFacadeRemote(ServerMetaFacadeRemote serverMetaFacadeRemote) {
		this.serverMetaFacadeRemote = serverMetaFacadeRemote;
	}
	
	final ServerMetaFacadeRemote getServerMetaFacadeRemote() {
		return serverMetaFacadeRemote;
	}
	
	final void setNuclosRemoteServerSession(NuclosRemoteServerSession nuclosRemoteServerSession) {
		this.nuclosRemoteServerSession = nuclosRemoteServerSession;
	}

	final NuclosRemoteServerSession getNuclosRemoteServerSession() {
		return nuclosRemoteServerSession;
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
		// frame.setIconImage(NuclosIcons.getInstance().getDefaultFrameIcon().getImage());
		frame.getContentPane().add(optpn, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
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
				if (arg != null && (arg.toLowerCase().startsWith(ARGUMENT_USERID.toLowerCase()) || arg.toLowerCase().startsWith(ARGUMENT_USER_ID.toLowerCase())) && arg.indexOf('=') > -1 && arg.indexOf('=') + 1 < arg.length()) {
					userid = arg.substring(arg.indexOf('=') + 1);
				}
			}
		}

		loginPanel.getUsernameField().setText(userid != null ? userid : userName);
		loginPanel.getUsernameField().addFocusListener(new SelectAllFocusAdapter());
		loginPanel.getPasswordField().addFocusListener(new SelectAllFocusAdapter());

		// set focus to password field:
		if(!userName.equals("")) {
			String pass = StringUtils.nullIfEmpty(props.getUserPasswd());

			if (userid != null && !userid.equals(userName)) {
				pass = null;
			}

			if(pass != null && passwordSaveAllowed) {
				try {
					String dec = CryptUtil.decryptAESHex(pass, CRYPT);
					loginPanel.getPasswordField().setText(dec);
					loginPanel.getPasswordField().setSelectionStart(0);
					loginPanel.getPasswordField().setSelectionEnd(dec.length());
					loginPanel.getRememberPwCheckBox().setSelected(true);
					attemptAutoLogin = true;
				}
				catch(Exception e) {
					LOG.error("Error decoding autologin-pass", e);
				}
			}
			loginPanel.getPasswordField().requestFocusInWindow();
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
					try {
						if(cmdPerformLogin(frame, optpn, JOptionPane.OK_OPTION,  props)) {
							postProcessLogin();
							LoginController.this.fireLoginSuccessful();
							frame.dispose();
							return;
						}
					}
					catch (Exception e) {
						LOG.error("attemptAutoLogin failed: " + e, e);
					}
				}
			});
		}
	}

	public boolean run(JFrame frame) {
		final LocalUserProperties props = LocalUserProperties.getInstance();

		loginPanel = LoginPanel.getInstance();
		loginPanel.hideLanguageSelection();

		final JOptionPane optpn = new JOptionPane(loginPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		optpn.setInitialValue(null);
		optpn.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

		String sTitle = MessageFormat.format(
			props.getLoginResource(LocalUserProperties.KEY_LOGIN_TITLE),
			ApplicationProperties.getInstance().getCurrentVersion().getShortName());
		final JDialog dialog = optpn.createDialog(frame, sTitle);
		dialog.setName("dlgLogin");
		dialog.setIconImage(NuclosIcons.getInstance().getDefaultFrameIcon().getImage());
		dialog.pack();
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(frame);

		loginPanel.getUsernameField().setText(props.getUserName());
		loginPanel.getUsernameField().setEnabled(false);
		loginPanel.getUsernameField().addFocusListener(new SelectAllFocusAdapter());
		loginPanel.getPasswordField().addFocusListener(new SelectAllFocusAdapter());

		while (true) {
			dialog.setVisible(true);
			Object result = optpn.getValue();
			if (result instanceof Integer) {
				Integer iValue = (Integer) result;
				switch (iValue) {
					case JOptionPane.OK_OPTION:
						if(cmdPerformLogin(frame, optpn, iValue, props)) {
							dialog.dispose();
							return true;
						}
						else {
							optpn.setValue(optpn.getInitialValue());
						}
						break;
					case JOptionPane.CANCEL_OPTION:
					case JOptionPane.CLOSED_OPTION:
						dialog.dispose();
						return false;
					default:
						throw new NuclosFatalException();
				}
			}
			return false;
		}
	}

	private void postProcessLogin() {
		List<LocaleInfo> clientCachedLocaleInfo
			= LocalUserProperties.getInstance().getLoginLocaleSelection();

		final LocaleDelegate localeDelegate = SpringApplicationContextHolder.getBean(LocaleDelegate.class);
		if (localeDelegate == null) {
			throw new IllegalStateException("Spring injection failed: Most probably cause: You need load-time weaving but started client without -javaagent.");
		}
		Collection<LocaleInfo> localeInfo = localeDelegate.getAllLocales(false);

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
			selLocale = (LocaleInfo) loginPanel.getLanguageComboBox().getSelectedItem();
		}
		localeDelegate.selectLocale(localeInfo, selLocale);
		
		if (clientContextCondition != null) {
			synchronized(clientContextCondition) {
				clientContextCondition.waitFor();
			}
		}
		// LiveSearchController, NuclosCollectableEntityProvider and MainFrame
		// need access to (locale) resources. This is the first place
		// where we know the locale.
		LiveSearchController.getInstance().init();
		NuclosCollectableEntityProvider.getInstance().init();
		Modules.initialize();
	}

	private boolean cmdPerformLogin(final JFrame frame, final JOptionPane optpn, final int selectedOption, final LocalUserProperties props) {
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
				final String sUserName = loginPanel.getUsernameField().getText().trim();
				final char[] acPassword = loginPanel.getPasswordField().getPassword();
				try {
					try {
						performLogin(sUserName, acPassword);
						result = true;
					}
					catch (CredentialsExpiredException ex) {
						ChangePasswordPanel cpp = new ChangePasswordPanel(false, new String(acPassword), true);
						boolean ok = cpp.showInDialog(frame, new ChangePasswordPanel.ChangePasswordDelegate() {
							@Override
							public void changePassword(String oldPw, String newPw) throws CommonBusinessException {
								RemoteAuthenticationManager ram = SpringApplicationContextHolder.getBean(RemoteAuthenticationManager.class);
								ram.changePassword(sUserName, new String(acPassword), newPw);
								loginPanel.getPasswordField().setText(newPw);
							}
						});
						if (!ok) {
							return result;
						}
						else {
							performLogin(sUserName, loginPanel.getPasswordField().getPassword());
							result = true;
						}
					}

					// check for credential expiration date
					Date expirationdate = SecurityDelegate.getInstance().getPasswordExpiration();
					if (expirationdate != null) {
						long difference = expirationdate.getTime() - Calendar.getInstance().getTimeInMillis();
						difference = (difference / (1000 * 60 * 60 * 24)) + 1L;
						if (difference <= 3) {
							String message = MessageFormat.format(LocalUserProperties.getInstance().getLoginResource("login.question.password.change"), difference);
							int i = JOptionPane.showConfirmDialog(frame, message, ApplicationProperties.getInstance().getName(), JOptionPane.YES_NO_OPTION);
							if (i == JOptionPane.YES_OPTION) {
								ChangePasswordPanel cpp = new ChangePasswordPanel(false, new String(acPassword), true);
								result = cpp.showInDialog(frame, new ChangePasswordPanel.ChangePasswordDelegate() {
									@Override
									public void changePassword(String oldPw, String newPw) throws CommonBusinessException {
										RemoteAuthenticationManager ram = SpringApplicationContextHolder.getBean(RemoteAuthenticationManager.class);
										ram.changePassword(sUserName, new String(acPassword), newPw);
										getNuclosRemoteServerSession().relogin(sUserName, newPw);
									}
								});
							}
						}
					}

					if (result) {
						props.setUserName(sUserName);

						props.setUserPasswd(
							loginPanel.getRememberPwCheckBox().isSelected()
							? CryptUtil.encryptAESHex(new String(loginPanel.getPasswordField().getPassword()), CRYPT)
						    : "");

						props.store();
					}
				}
				catch (LockedException ex) {
					loginPanel.shake(shakeStepSize);
					loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_LOCKED));
				}
				catch (AccountExpiredException ex) {
					loginPanel.shake(shakeStepSize);
					loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_ACCOUNT_EXPIRED));
				}
				catch (AuthenticationException ex) {
					loginPanel.shake(shakeStepSize);
					loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_UPASS));
				}
				catch (AccessDeniedException ex) {
					loginPanel.shake(shakeStepSize);
					loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_UPERM));
				}
				catch (RemoteAccessException ex) {
					loginPanel.shake(shakeStepSize);
					loginPanel.setPasswordError(LocalUserProperties.getInstance().getLoginResource(LocalUserProperties.KEY_ERR_SERVER));
				}
				catch (Exception ex) {
					Errors.getInstance().showExceptionDialog(frame, ex);
				}
				finally {
					StringUtils.clear(acPassword);
				}
			}
		}
		catch (CommonFatalException ex) {
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
			Errors.getInstance().showExceptionDialog(frame, ex);
		}
		finally {
			frame.setEnabled(true);
			frame.requestFocusInWindow();
			setSubComponentsEnabled(optpn, acls, true);
			loginPanel.setProgressVisible(result);
			loginPanel.getPasswordField().setText("");
			loginPanel.getPasswordField().requestFocusInWindow();
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

	private String performLogin(String sUserName, char[] acPassword) {
		// wait for context set. @see NUCLOS-1036 
		if (clientContextCondition != null) {
			synchronized(clientContextCondition) {
				clientContextCondition.waitFor();
			}
		}
		final String result = getNuclosRemoteServerSession().login(sUserName, new String(acPassword));
		if (!ShutdownActions.getInstance().isRegistered(ShutdownActions.SHUTDOWNORDER_LOGOUT)) {
			ShutdownActions.getInstance().registerShutdownAction(ShutdownActions.SHUTDOWNORDER_LOGOUT, new Logout());
		}
		return result;
	}

	public synchronized void addLoginListener(LoginListener loginlistener) {
		loginListeners.add(loginlistener);
	}

	public synchronized void removeLoginListener(LoginListener loginlistener) {
		loginListeners.remove(loginlistener);
	}

	public void fireLoginSuccessful() {
		LoginEvent ev = new LoginEvent(this, loginPanel.getUsernameField().getText(), "default");
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

	private class Logout implements Runnable {
		@Override
		public void run() {
			getNuclosRemoteServerSession().logout();
		}
	}

	public void increaseLoginProgressBar(int step) {
		this.loginPanel.increaseProgress(step);
	}
}	// class LoginController
