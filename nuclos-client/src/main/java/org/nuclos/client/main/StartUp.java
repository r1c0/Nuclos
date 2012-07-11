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
package org.nuclos.client.main;

import java.awt.EventQueue;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
import org.nuclos.api.ui.annotation.NucletComponent;
import org.nuclos.client.NuclosIcons;
import org.nuclos.client.common.NuclosCollectableComponentFactory;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.jms.TopicNotificationReceiver;
import org.nuclos.client.login.LoginController;
import org.nuclos.client.login.LoginEvent;
import org.nuclos.client.login.LoginListener;
import org.nuclos.client.login.LoginPanel;
import org.nuclos.client.synthetica.NuclosSyntheticaUtils;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.ResultListener;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.collect.component.CollectableComponentFactory;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SpringApplicationSubContextsHolder;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.startup.Startup;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeRemote;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.SystemPropertyUtils;

/**
 * Controller responsible for starting up the Nucleus client.
 * This is a temporary object which will be discarded after the startup is done.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class StartUp  {

	private static final Logger LOG = Logger.getLogger(StartUp.class);

	// These progress constants sum up to to 100:
	static final int PROGRESS_COMPARE_VERSIONS = 5;
	static final int PROGRESS_INIT_SECURITYCACHE = 5;
	static final int PROGRESS_INIT_NOTIFICATION = 5;
	static final int PROGRESS_READ_ATTRIBUTES = 8;
	static final int PROGRESS_READ_LOMETA = 5;
	static final int PROGRESS_READ_SEARCHFILTER = 5;
	static final int PROGRESS_CREATE_MAINFRAME = 28;
	static final int PROGRESS_INIT_ONLINEHELP = 4;
	static final int PROGRESS_CREATE_MAINMENU = 6;
	static final int PROGRESS_RESTORE_WORKSPACE = 29;

	/**
	 * log4j category
	 * @todo this shouldn't be a member probably
	 */
	private static Logger log;
	
	public static class ClientContextCondition {
		
		private boolean refreshed = false;
		
		private ClientContextCondition() {
		}
		
		public boolean isRefreshed() {
			return refreshed;
		}
		
		public void waitFor() {
			try {
				for(int i = 0; !refreshed && i < 1000; ++i) {
					wait(100);
				}
			}
			catch (InterruptedException e) {
				// ignore
			}
			if (!refreshed) {
				throw new IllegalStateException("Can't create MainController: Spring context not initialized!");
			}			
		}
		
		private void refreshed() {
			refreshed = true;
		}
		
	}
	
	private static final String[] CLIENT_SPRING_BEANS = new String[] {
		"META-INF/nuclos/client-beans.xml"
	};
	
	private static final String EXTENSION_SPRING_BEANS = "classpath*:META-INF/nuclos/nuclos-extension-client-beans.xml";
	
	//

	private final String[] args;
	
	private ClassPathXmlApplicationContext startupContext;
	
	private ClassPathXmlApplicationContext clientContext;
	
	private ClientContextCondition clientContextCondition = new ClientContextCondition();
	
	private final ApplicationListener<ContextRefreshedEvent> refreshListener = new ApplicationListener<ContextRefreshedEvent>() {
		
		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			synchronized (clientContextCondition) {
				clientContextCondition.refreshed();
				clientContextCondition.notify();
			}
		}
		
	};
	
	//
	
	public StartUp(String[] asArgs) {
		this.args = asArgs;
		// init();
	}
	
	public final void init() {
		// setup client side logging:
		setupClientLogging();
		final ClassLoader cl = this.getClass().getClassLoader();

		startupContext = new ClassPathXmlApplicationContext(
				new String[] { "META-INF/nuclos/client-beans-startup.xml" }, false);
		// see http://fitw.wordpress.com/2009/03/14/web-start-and-spring/ why this is needed (tp)
		startupContext.setClassLoader(cl);
		startupContext.refresh();
		startupContext.registerShutdownHook();
		
		final Runnable run1 = new Runnable() {

			@Override
			public void run() {
				try {
					// Time zone stuff
			        final ServerMetaFacadeRemote sm = startupContext.getBean(ServerMetaFacadeRemote.class);
			        final TimeZone serverDefaultTimeZone = sm.getServerDefaultTimeZone();
			        final StringBuilder msg = new StringBuilder(); 
			        msg.append("Default local  time zone is: ").append(TimeZone.getDefault().getID()).append("\n");
			        msg.append("Default server time zone is: ").append(serverDefaultTimeZone.getID()).append("\n");
					if (!LangUtils.equals(TimeZone.getDefault(), serverDefaultTimeZone)) {
						TimeZone.setDefault(serverDefaultTimeZone);
						msg.append("Local default time zone is set to server default!\n");
					}
					msg.append("Initial local  time zone is: " + Main.getInitialTimeZone().getID());
					LOG.info(msg);
					
					// Scanning context
					clientContext = new ClassPathXmlApplicationContext(CLIENT_SPRING_BEANS, false, startupContext);
					// see http://fitw.wordpress.com/2009/03/14/web-start-and-spring/ why this is needed (tp)
					clientContext.addApplicationListener(refreshListener);
					clientContext.setClassLoader(cl);
					Thread.yield();					
					clientContext.refresh();
					clientContext.registerShutdownHook();
					
					Thread.yield();
					log.info("@NucletComponents within spring context: " + clientContext.getBeansWithAnnotation(NucletComponent.class));

					Thread.yield();
					final Resource[] themes = clientContext.getResources("classpath*:META-INF/nuclos/nuclos-theme.properties");
					log.info("loading themes properties from the following files: " + Arrays.asList(themes));

					for (Resource r : themes) {
						if (!r.exists()) {
							continue;
						}
						Properties p = new Properties();
						p.load(r.getInputStream());

						for (Object key : p.keySet()) {
							if (key instanceof String && p.get(key) != null && p.get(key) instanceof String) {

								String sKey = (String) key;
								if (sKey.startsWith("name")) {
									String xmlKey = "xml";
									if (sKey.length() > 4) {
										String sNumber = sKey.substring(4);
										xmlKey = xmlKey + sNumber;
									}
									Object xml = p.get(xmlKey);
									if (xml != null && xml instanceof String) {
										NuclosSyntheticaUtils.registerNuclosTheme((String) p.get(key), (String) xml);
									}
								}
							}
						}
					}
					
					boolean hasSubContext = false;
					final SpringApplicationSubContextsHolder holder = SpringApplicationSubContextsHolder.getInstance();
					final Resource[] extensions = clientContext.getResources(EXTENSION_SPRING_BEANS);
					log.info("loading extensions spring sub contexts from the following xml files: " + Arrays.asList(extensions));

					for (Resource r : extensions) {
						if (!r.exists()) {
							log.info("spring sub context xml not found: " + r);
							continue;
						}
						final AbstractXmlApplicationContext ctx;
						if (r instanceof ClassPathResource) {
							ctx = new ClassPathXmlApplicationContext(new String[] { ((ClassPathResource)r).getPath() }, false, clientContext);
						} 
						else if (r instanceof UrlResource) {
							ctx = new ResourceXmlApplicationContext(r, Collections.EMPTY_LIST, clientContext, Collections.EMPTY_LIST, false);
						}
						else {
							ctx = new FileSystemXmlApplicationContext(new String[] { r.getFile().getPath() }, false, clientContext);
						}
						// see http://fitw.wordpress.com/2009/03/14/web-start-and-spring/ why this is needed (tp)
						ctx.setClassLoader(cl);
						// clientContext.addApplicationListener(refreshListener);
						log.info("before refreshing spring context " + r);
						ctx.refresh();
						holder.registerSubContext(ctx);
						hasSubContext = true;
						log.info("after refreshing spring context " + r);
					}
					if (!hasSubContext) {
						holder.registerSubContext(clientContext);
					}
					// MetaDataClientProvider.initialize();
				}
				catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
			}
		};
		final Thread thread1 = new Thread(run1, "Startup.init.run1");
		thread1.setPriority(Thread.NORM_PRIORITY - 1);
		thread1.start();
		
		// set the default locale:
		// this makes sure the client is independent of the host's locale.
		/** @todo i18n */
		Locale.setDefault(Locale.GERMANY);

		log.info("Java-Version " + System.getProperty("java.version"));

		// initialize the Errors instance:
		Errors.getInstance().setAppName(ApplicationProperties.getInstance().getName());
		// first, set the non-strict version of the critical error handler to avoid locking-out on initialization errors.
		// on successful initialization, the strict version of the critical error handler will be set.
		Errors.getInstance().setCriticalErrorHandler(new NuclosCriticalErrorHandler(false));

		// from here, we can issue error messages using the Errors class.

		// Install the CollectableComponentFactory for the application:
		CollectableComponentFactory.setInstance(newCollectableComponentFactory());

		// we create the gui completely in the event dispatch thread, as recommended by Sun:

		EventQueue.invokeLater(new Runnable() {
			@Override
            public void run() {
				try {
					createGUI();
					if (Main.getInstance().isMacOSX()) {
						Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
						Object macAppObject = macAppClass.getConstructor().newInstance();
						// set Nuclos dock icon
						macAppClass.getMethod("setDockIconImage", java.awt.Image.class).invoke(macAppObject, NuclosIcons.getInstance().getBigTransparentApplicationIcon512().getImage());
					}
					// really subscribe to the topics collected at startup time
					TopicNotificationReceiver.getInstance().realSubscribe();
				}
				catch (Exception e) {
					LOG.fatal("Startup failed: " + e, e);
				}
			}
		});
	}	// ctor

	private static CollectableComponentFactory newCollectableComponentFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getCollectableComponentFactoryClassName(),
					NuclosCollectableComponentFactory.class.getName());

			return (CollectableComponentFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("CollectableComponentFactory cannot be created.", ex);
		}
	}

	private void setupClientLogging() {
		LogLog.setInternalDebugging(true);
		String sLog4jUrl = System.getProperty("log4j.url");
		if (!StringUtils.isNullOrEmpty(sLog4jUrl)) {
			try {
				LogLog.debug("Try to configure loggging from " + sLog4jUrl + ".");
				Log4jConfigurer.initLogging(sLog4jUrl);
				log = Logger.getLogger(StartUp.class);
				log.info("Logging configured from " + sLog4jUrl);
				LogLog.setInternalDebugging(false);
				return;
			}
			catch(Throwable e) {
		        // Ok! (tp)
				System.out.println("Failed to configure logging from " + sLog4jUrl + ": " + e.getMessage());
			}
		}
	
		final URL configurationfile = getLog4jConfigurationFile();
		try {
			LogLog.debug("Try to configure loggging from configuration file: " + configurationfile);
			Log4jConfigurer.initLogging(configurationfile.toExternalForm());
			log = Logger.getLogger(StartUp.class);
			log.info("Logging configured from log4j configuration: " + configurationfile);
		}
		catch (Throwable t) {
			throw new NuclosFatalException("The client-side logging could not be initialized, because the configuration file " 
					+ configurationfile  + " was not found in the Classpath.", t);
		}
		finally {
			LogLog.setInternalDebugging(false);
		}
	}
	
	private static URL getLog4jConfigurationFile() {
		String conf = System.getProperty("log4j.configuration");
		if (conf == null) {
			conf = Boolean.getBoolean("functionblock.dev")
					? "log4j-dev.properties" : "log4j.properties";
		}
		URL url;
		try {
			url = new URL(conf);
		}
		catch (MalformedURLException e) {
			url = null;
		}
		if (url == null) {
			url = Startup.class.getClassLoader().getResource(conf);
		}
		return url;
	}
	
	/**
	 * 
	 * @return 
	 * 		x = logfile with path
	 * 		y = date pattern
	 */
	public static Pair<String, String> getLogFile() {
		Pair<String, String> result = new Pair("<not avaiable>", "");
		try {
			java.util.Properties clientLog4jProperties = new Properties();
			clientLog4jProperties.load(StartUp.getLog4jConfigurationFile().openStream());
			result.x = SystemPropertyUtils.resolvePlaceholders(clientLog4jProperties.getProperty("log4j.appender.logfile.File"));
			result.y = clientLog4jProperties.getProperty("log4j.appender.logfile.DatePattern");
		} catch (Exception ex) {
			// do nothing
		}
		return result;
	}

	private void createGUI() {
		this.setupLookAndFeel();
		// show LoginPanal as soon as possible
		LoginPanel.getInstance();
		// LOG.info("SHOW LOGIN PANEL");
		
		try {
			// perform login:
			final LoginController ctlLogin = new LoginController(null, this.args, clientContextCondition);
			// ctlLogin.setLocaleDelegate(startupContext.getBean(LocaleDelegate.class));
			final Main main = new Main();

			ctlLogin.addLoginListener(new LoginListener() {
				@Override
                public void loginSuccessful(final LoginEvent ev) {
					log.info("login of " + ev.getAuthenticatedUserName() + " to server " 
							+ ev.getConnectedServerName() + " triggered");
					try {
						registerOSXHandler();
					}
					catch (Exception ex) {
						Errors.getInstance().showExceptionDialog(null, ex);
						Main.exit(Main.ExitResult.ABNORMAL);
					}

					UIUtils.runCommandForTabbedPane(null, new Runnable() {
						@Override
                        public void run() {
							try {
								Main.getInstance().getMainFrame().postConstruct();
								// ???
								Main.getInstance().getMainFrame().init("", "");
								
								compareClientAndServerVersions();
								ctlLogin.increaseLoginProgressBar(PROGRESS_COMPARE_VERSIONS);
								createMainController(ev.getAuthenticatedUserName(), ev.getConnectedServerName(), ctlLogin);

								try {
									// notify LaunchListeners
									ServiceManager.lookup("javax.jnlp.SingleInstanceService");
									Main.getInstance().notifyListeners(StartUp.this.args);
							    }
								catch (UnavailableServiceException ex) {
							    	// no webstart context
									Main.getInstance().notifyListeners(StartUp.this.args);
							    }

								// After successful initialization, set the strict version of the critical error handler:
								SwingUtilities.invokeLater(new Runnable() {
									@Override
                                    public void run() {
										Errors.getInstance().setCriticalErrorHandler(new NuclosCriticalErrorHandler(true));
										log.info("login done");
									}
								});
							}
							catch (Exception ex) {
								Errors.getInstance().showExceptionDialog(null, ex);
								Main.exit(Main.ExitResult.ABNORMAL);
							}
						}

						private void compareClientAndServerVersions() {
							final ApplicationProperties.Version versionClient = ApplicationProperties.getInstance().getNuclosVersion();
							final ApplicationProperties.Version versionServer = SecurityDelegate.getInstance().getCurrentApplicationVersionOnServer();
							if (!versionClient.equals(versionServer)) {
								final String sMessage = "The version of this client is not compatible with the version of the connected server." +
										"\nClient-version: " + versionClient.getVersionNumber() + "\nServer-version: " + versionServer.getVersionNumber() +
										"\n\nPlease contact the system administrator.";
									//"Die Version dieses Clients ist nicht kompatibel mit der Version des verbundenen Servers.\n" + "Client-Version: " + versionClient + "\n" + "Server-Version: " + versionServer + "\n" + "\nBitte wenden Sie sich an den Systemadministrator.";
								throw new NuclosFatalException(sMessage);
							}
						}

					});
				}

				@Override
                public void loginCanceled(LoginEvent ev) {
					/** @todo adjust semantics (failed vs. canceled) */
					Main.exit(Main.ExitResult.LOGIN_FAILED);
				}
			});

			ctlLogin.run();
			/** @todo move exception handling to LoginController.run() */
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
				final String sMessage = "No connection could be establish to the server.\nPlease contact the system administrator.";
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			catch (Exception exCause) {	
				// everything else
				final String sMessage = "A fatal error occurred.";
				Errors.getInstance().showExceptionDialog(null, sMessage, ex);
			}
			finally {
				Main.exit(Main.ExitResult.ABNORMAL);
			}
		}
		catch (Exception ex) {
			Errors.getInstance().showExceptionDialog(null, null, ex);
			Main.exit(Main.ExitResult.ABNORMAL);
		}
	}
	
	private static void registerOSXHandler() 
			throws ClassNotFoundException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, 
			InvocationTargetException, NoSuchMethodException 
	{
		if (Main.getInstance().isMacOSX()) {
			Class<?> macAppClass = Class.forName("com.apple.eawt.Application");
			Object macAppObject = macAppClass.getConstructor().newInstance();

			// register about handler
			Class<?> macAboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
			Method macAppSetAboutHandlerMethod = macAppClass.getDeclaredMethod("setAboutHandler", new Class[] { macAboutHandlerClass });

			Object macAboutHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macAboutHandlerClass }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
					if (method != null && "handleAbout".equals(method.getName()) && args.length == 1) {
						Main.getInstance().getMainController().cmdShowAboutDialog();
					}

					return null;
				}
			});
			macAppSetAboutHandlerMethod.invoke(macAppObject, new Object[] { macAboutHandler });

			// register preferences handler
			Class<?> macPreferencesHandlerClass = Class.forName("com.apple.eawt.PreferencesHandler");
			Method macAppSetPreferencesHandlerMethod = macAppClass.getDeclaredMethod("setPreferencesHandler", new Class[] { macPreferencesHandlerClass });

			Object macPreferencesHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macPreferencesHandlerClass }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)	throws Throwable {
					if (method != null && "handlePreferences".equals(method.getName()) && args.length == 1) {
						Main.getInstance().getMainController().cmdOpenSettings();
					}

					return null;
				}
			});
			macAppSetPreferencesHandlerMethod.invoke(macAppObject, new Object[] { macPreferencesHandler });

			// register quit handler
            Class<?> macQuitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
            Method macAppSetQuitHandlerMethod = macAppClass.getDeclaredMethod("setQuitHandler", new Class[] { macQuitHandlerClass });

            Object macQuitHandler = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[] { macQuitHandlerClass }, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, final Object[] args)	throws Throwable {
					if (method != null && "handleQuitRequestWith".equals(method.getName()) && args.length == 2) {
						Class<?> macQuitResponseClass = Class.forName("com.apple.eawt.QuitResponse");
						final Method macQuitResponsePerformQuitMethod = macQuitResponseClass.getDeclaredMethod("performQuit");
						final Method macQuitResponseCancelQuitMethod = macQuitResponseClass.getDeclaredMethod("cancelQuit");

						Main.getInstance().getMainController().cmdWindowClosing(new ResultListener<Boolean>() {
							@Override
							public void done(Boolean result) {
								try {
									if (Boolean.TRUE.equals(result)) {
										macQuitResponsePerformQuitMethod.invoke(args[1]);
									} else {
										macQuitResponseCancelQuitMethod.invoke(args[1]);
									}
								} catch (Exception ex) {
									LOG.error(ex.getMessage(), ex);
								}
							}
						});
					}
					return null;
				}
			});
            macAppSetQuitHandlerMethod.invoke(macAppObject, new Object[] { macQuitHandler });

            //register dock menu
            PopupMenu macDockMenu = new PopupMenu();
            MenuItem miDockLogoutExit = new MenuItem(SpringLocaleDelegate.getInstance().getResource("miLogoutExit", "Abmelden und Beenden"));
            miDockLogoutExit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					Main.getInstance().getMainController().cmdLogoutExit();
				}
			});
            macDockMenu.add(miDockLogoutExit);

            Method macAppSetDockMenuMethod = macAppClass.getDeclaredMethod("setDockMenu", PopupMenu.class);
            macAppSetDockMenuMethod.invoke(macAppObject, macDockMenu);
		}
		
		if (Main.getInstance().isMacOSXLionOrBetter()) {
			//register Mac OS X Lion Fullscreen Support
            try {
                Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
                Class params[] = new Class[2];
                params[0] = Window.class;
                params[1] = Boolean.TYPE;
                Method method = util.getMethod("setWindowCanFullScreen", params);
                method.invoke(util, Main.getInstance().getMainFrame(), true);
            } catch (ClassNotFoundException e) {
            	LOG.warn("Mac OS X Lion Fullscreen Support not activ. Java Update necesary...", e);
            } catch (NoSuchMethodException e) {
                LOG.error(e.getMessage(), e);
            } catch (InvocationTargetException e) {
            	LOG.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
            	LOG.error(e.getMessage(), e);
            }
		}
	}

	private void createMainController(String sUserName, String sNucleusServerName, LoginController lc)
			throws CommonPermissionException {
		synchronized (clientContextCondition) {
			clientContextCondition.waitFor();
		}
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getMainControllerClassName(),
					MainController.class.getName());

			final Class<? extends MainController> clsMainController = (Class<? extends MainController>) Class.forName(sClassName);
			final Constructor<? extends MainController> ctor = clsMainController.getConstructor(String.class, String.class, LoginController.class);
			ctor.newInstance(sUserName, sNucleusServerName, lc);
		}
		catch (InvocationTargetException ex) {
			final Throwable tTarget = ex.getTargetException();
			if (tTarget instanceof CommonPermissionException) {
				throw ((CommonPermissionException) tTarget);
			}
			else {
				throw new CommonFatalException(tTarget);
			}
		}
		catch (Exception ex) {
			throw new CommonFatalException("MainController cannot be created.", ex);
		}
	}

	private void setupLookAndFeel() {
		try {
			ServerMetaFacadeRemote sm = (ServerMetaFacadeRemote) startupContext.getBean("serverMetaService");
			String defaultNuclosTheme = sm.getDefaultNuclosTheme();
			NuclosSyntheticaUtils.setLookAndFeel(defaultNuclosTheme);

			UIManager.put("TabbedPane.contentOpaque", Boolean.FALSE);
			UIManager.put("DesktopIconUI", "org.nuclos.client.ui.NuclosDesktopIconUI");
			UIManager.put("TextArea.font", UIManager.get("TextField.font"));

			Toolkit.getDefaultToolkit().setDynamicLayout(false);
			System.setProperty("awt.dynamicLayoutSupported", "false");
		}
		catch (Exception ex) {
			// If the look&feel can't be set, don't worry. Just print stack trace and continue:
			log.warn("Look&Feel cannot be set", ex);
		}
	}

}	// class StartUp
