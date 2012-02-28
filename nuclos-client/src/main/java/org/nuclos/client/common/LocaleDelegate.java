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
package org.nuclos.client.common;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.nuclos.client.LocalUserProperties;
import org.nuclos.client.main.SwingLocaleSwitcher;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.JMSConstants;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.common.ejb3.LocaleFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class LocaleDelegate implements SpringLocaleDelegate.LookupService, MessageListener, InitializingBean, DisposableBean {

	private static final Logger LOG = Logger.getLogger(LocaleDelegate.class);
	
	private static Object RB_LOCk = new Object();
	private static Locale LOCALE;
	private static LocaleInfo LOCALE_INFO;
	private static List<LocaleInfo> PARENTCHAIN;
	private static ResourceBundle RESOURCE_BUNDLE;
	private static Date DATE;

	// SimpleDateFormat is not Thread-safe, therefore: clone the format!
	private static NumberFormat NUMBER_FORMAT;
	private static DateFormat DATE_FORMAT;
	private static DateFormat TIME_FORMAT;
	private static DateFormat DATETIME_FORMAT;
	
	private static LocaleDelegate INSTANCE;
	
	// 

	private LocaleFacadeRemote remoteInterface;
	
	private TopicNotificationReceiver tnr;
	
	public LocaleDelegate() {
		INSTANCE = this;
	}
	
	@Autowired
	void setTopicNotificationReceiver(TopicNotificationReceiver tnr) {
		this.tnr = tnr;
	}

	@Autowired
	void setLocaleService(LocaleFacadeRemote service) {
		this.remoteInterface = service;
	}

	@Override
	public void afterPropertiesSet() {
		tnr.subscribe(JMSConstants.TOPICNAME_LOCALE, this);
	}

	public static LocaleDelegate getInstance() {
		// return (LocaleDelegate) SpringApplicationContextHolder.getBean("lookupService");
		return INSTANCE;
	}

	private ResourceBundle getResourceBundle() {
		// too early
		if (LOCALE_INFO == null) {
			LOG.info("early call to getResourceBundle()");
			return null;
		}
		if (RESOURCE_BUNDLE == null) {
			synchronized (RB_LOCk) {
				if (RESOURCE_BUNDLE == null) {
					try {
						RESOURCE_BUNDLE = remoteInterface.getResourceBundle(LOCALE_INFO);
						// TODO respect timezone client vs server
						DATE = Calendar.getInstance().getTime();
					} catch (RuntimeException e) {
						throw new CommonFatalException(e);
					}
				}
			}
			LOG.info("Updated ResourceBundle for cache " + this);
		}
		return RESOURCE_BUNDLE;
	}

	public void flush() {
		synchronized (RB_LOCk) {
			RESOURCE_BUNDLE = null;
		}
	}

	@Override
	public NumberFormat getNumberFormat() {
		if (NUMBER_FORMAT == null) {
			return NumberFormat.getNumberInstance();
		}
		return (NumberFormat) NUMBER_FORMAT.clone();
	}

	@Override
	public DateFormat getDateFormat() {
		if (DATE_FORMAT == null)
			return SimpleDateFormat.getDateInstance();
		return (DateFormat) DATE_FORMAT.clone();
	}

	@Override
	public DateFormat getTimeFormat() {
		if (TIME_FORMAT == null)
			return SimpleDateFormat.getTimeInstance();
		return (DateFormat) TIME_FORMAT.clone();
	}

	@Override
	public DateFormat getDateTimeFormat() {
		if (DATETIME_FORMAT == null)
			return SimpleDateFormat.getDateTimeInstance();
		return (DateFormat) DATETIME_FORMAT.clone();
	}

	public LocaleInfo getDefaultLocale() {
		try {
			return remoteInterface.getDefaultLocale();
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Collection<LocaleInfo> getAllLocales(boolean includeNull) {
		try {
			return remoteInterface.getAllLocales(includeNull);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String setResourceForLocale(String sResourceId, LocaleInfo localeInfo, String sText) {
		try {
			return remoteInterface.setResourceForLocale(sResourceId, localeInfo, sText);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public void updateResource(String resourceId, String sText) {
		try {
			remoteInterface.updateResource(resourceId, sText);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String createResource(String sText) {
		try {
			return remoteInterface.createResource(sText);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getDefaultResource(String sResourceId) {
		try {
			return remoteInterface.getResourceById(LocaleInfo.I_DEFAULT, sResourceId);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getResourceByStringId(LocaleInfo localeInfo, String sResourceId) {
		try {
			return remoteInterface.getResourceById(localeInfo, sResourceId);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Map<String, String> getAllResourcesByStringId(String sResourceId) {
		try {
			return remoteInterface.getAllResourcesById(sResourceId);
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public String getDefaultResource(String resId, String text) {
		try {
			if (resId != null) {
				return remoteInterface.getResource(resId);
			} else {
				return text;
			}
		} catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Collection<MasterDataVO> getDefaultResourcesAsVO(Collection<String> coll) {
		try {
			return remoteInterface.getResourcesAsVO(coll, getDefaultLocale());
		}
		catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Collection<MasterDataVO> getLocaleResourcesForParent(LocaleInfo localeInfo) {
		try {
			return remoteInterface.getLocaleResourcesForParent(localeInfo);
		}
		catch (RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public Object update(MasterDataVO mdvo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		try {
			return remoteInterface.modify(mdvo, mpDependants);
		} catch (RuntimeException ex) {
			if(ex.getCause() != null && (ex.getCause() instanceof CommonFatalException)){
                throw new NuclosUpdateException(ex.getCause().getMessage());
        } else {
                throw new NuclosUpdateException(ex);
        }
		}
	}

	public LocaleInfo getBestLocale(LocaleInfo localeInfo) {
		try {
			return remoteInterface.getBestLocale(localeInfo);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public List<LocaleInfo> getParentChain() {
		return PARENTCHAIN;
	}

	public MasterDataVO getLocaleVO(LocaleInfo localeInfo) {
		try {
			return remoteInterface.getLocaleVO(localeInfo);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public void selectLocale(Collection<LocaleInfo> allLocales, LocaleInfo sel) {
		// find best existing match
		LOCALE_INFO = getBestLocale(sel);
		// switch client locale
		LOCALE = LOCALE_INFO.toLocale();

		SwingLocaleSwitcher.setLocale(LOCALE);
		LocaleContextHolder.setLocale(LOCALE, true);

		// write to prefs
		Preferences prefs = ClientPreferences.getUserPreferences();
		Preferences localeNode = prefs.node("locale");
		localeNode.put("locale", LOCALE_INFO.getTag());
		// Copy login-relevant texts to local settings
		LocalUserProperties.getInstance().copyLoginResourcesFromResourceBundle(
					allLocales, LOCALE_INFO);
		// Get additional info from the locale's master data entry, and also set
		// it in the client
		MasterDataVO lmd = getLocaleVO(LOCALE_INFO);
		final String numberformat = lmd.getField("numberformat", String.class);
		final String dateformat = lmd.getField("dateformat", String.class);
		final String timeformat = lmd.getField("timeformat", String.class);

		PARENTCHAIN = remoteInterface.getParentChain(LOCALE_INFO);
		NUMBER_FORMAT = new DecimalFormat(numberformat, new DecimalFormatSymbols(LOCALE));
		DATE_FORMAT = new SimpleDateFormat(dateformat, LOCALE);
		TIME_FORMAT = new SimpleDateFormat(timeformat, LOCALE);
		DATETIME_FORMAT = new SimpleDateFormat(dateformat + " " + timeformat, LOCALE);
	}

	public void removeResource(String resourceId) {
		try {
			remoteInterface.deleteResource(resourceId);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	public void removeResourceFromLocale(String resouceId, LocaleInfo localeInfo) {
		try {
			remoteInterface.deleteResourceFromLocale(resouceId, localeInfo);
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public Locale getLocale() {
		return LOCALE;
	}

	@Override
	public LocaleInfo getLocaleInfo() {
		return LOCALE_INFO;
	}

	@Override
	public Date getLastChange() {
		try {
			return remoteInterface.getLastChange();
		}
		catch(RuntimeException e) {
			throw new CommonFatalException(e);
		}
	}

	@Override
	public boolean isResource(String key) {
		return getResourceBundle().containsKey(key);
	}

	@Override
	public String getResource(String key) {
		final ResourceBundle rb = getResourceBundle();
		// too early 
		if (rb == null) {
			LOG.info("early call to getResource(" + key + ")");
			return key;
		}
		if (!rb.containsKey(key)) {
			if (DATE != null && DATE.before(getLastChange())) {
				flush();
			}
		}
		try {
			return rb.getString(key);
		}
		catch (MissingResourceException ex) {
			if (ApplicationProperties.getInstance().isFunctionBlockDev()) {
				// find all unknown resource id or all code paths were translated texts are treated as resource ids
				return "Resource[" + key + "]";
			}
			else {
				// for backwards compatibility (the key is already translated)
				return key;
			}
		}
	}

	@Override
	public void onMessage(Message arg0) {
		synchronized(RB_LOCk) {
			LOG.info("onMessage " + this + " RESOURCE_BUNDLE=null");
			RESOURCE_BUNDLE = null;
		}
		LOG.info("onMessage: cleared cache " + this);
	}

	@Override
	public String getResourceById(LocaleInfo li, String key) {
		return remoteInterface.getResourceById(li, key);
	}
	
	@Override
	public synchronized void destroy() {
		tnr.unsubscribe(this);
		NUMBER_FORMAT = null;
		DATE_FORMAT = null;
		TIME_FORMAT = null;
		DATETIME_FORMAT = null;
	}
}
