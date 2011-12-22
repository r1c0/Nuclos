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
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.ClientPreferences;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.NuclosUpdateException;
import org.nuclos.server.common.ejb3.LocaleFacadeRemote;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.i18n.LocaleContextHolder;

public class LocaleDelegate implements CommonLocaleDelegate.LookupService, MessageListener, InitializingBean, DisposableBean {

	private static final Logger LOG = Logger.getLogger(LocaleDelegate.class);
	
	private static Object rbLock = new Object();
	private static Locale locale;
	private static LocaleInfo localeInfo;
	private static List<LocaleInfo> parentchain;
	private static ResourceBundle resourceBundle;
	private static Date date;

	// SimpleDateFormat is not Thread-safe, therefore: one per thread
	private static ThreadLocal<NumberFormat> numberFormat;
	private static ThreadLocal<DateFormat> dateFormat;
	private static ThreadLocal<DateFormat> timeFormat;
	private static ThreadLocal<DateFormat> dateTimeFormat;

	private LocaleFacadeRemote remoteInterface;
	
	public LocaleDelegate() {
	}

	public void setService(LocaleFacadeRemote service) {
		this.remoteInterface = service;
	}

	@Override
	public void afterPropertiesSet() {
		TopicNotificationReceiver.subscribe(JMSConstants.TOPICNAME_LOCALE, this);
	}

	public static synchronized LocaleDelegate getInstance() {
		return (LocaleDelegate) SpringApplicationContextHolder.getBean("lookupService");
	}

	private ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			synchronized (rbLock) {
				if (resourceBundle == null) {
					try {
						resourceBundle = remoteInterface.getResourceBundle(localeInfo);
						// TODO respect timezone client vs server
						date = Calendar.getInstance().getTime();
					} catch (RuntimeException e) {
						throw new CommonFatalException(e);
					}
				}
			}
		}
		return resourceBundle;
	}

	public void flush() {
		synchronized (rbLock) {
			resourceBundle = null;
		}
	}

	@Override
	public NumberFormat getNumberFormat() {
		if (numberFormat == null) {
			return NumberFormat.getNumberInstance();
		}
		return numberFormat.get();
	}

	@Override
	public DateFormat getDateFormat() {
		if(dateFormat == null)
			return SimpleDateFormat.getDateInstance();
		return dateFormat.get();
	}

	@Override
	public DateFormat getTimeFormat() {
		if(timeFormat == null)
			return SimpleDateFormat.getTimeInstance();
		return timeFormat.get();
	}

	@Override
	public DateFormat getDateTimeFormat() {
		if(dateTimeFormat == null)
			return SimpleDateFormat.getDateTimeInstance();
		return dateTimeFormat.get();
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
		return parentchain;
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
		this.localeInfo = getBestLocale(sel);
		// switch client locale
		this.locale = localeInfo.toLocale();

		SwingLocaleSwitcher.setLocale(this.locale);
		LocaleContextHolder.setLocale(this.locale, true);

		// write to prefs
		Preferences prefs = ClientPreferences.getUserPreferences();
		Preferences localeNode = prefs.node("locale");
		localeNode.put("locale", this.localeInfo.getTag());
		// Copy login-relevant texts to local settings
		LocalUserProperties.getInstance().copyLoginResourcesFromResourceBundle(
					allLocales,
					this.localeInfo);
		// Get additional info from the locale's master data entry, and also set
		// it in the client
		MasterDataVO lmd = getLocaleVO(this.localeInfo);
		final String numberformat = lmd.getField("numberformat", String.class);
		final String dateformat = lmd.getField("dateformat", String.class);
		final String timeformat = lmd.getField("timeformat", String.class);

		parentchain = remoteInterface.getParentChain(localeInfo);

		numberFormat = new ThreadLocal<NumberFormat>() {
			@Override
			protected NumberFormat initialValue() {
				return new DecimalFormat(numberformat, new DecimalFormatSymbols(locale));
			}
		};

		dateFormat = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(dateformat, locale);
			}
		};

		timeFormat = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(timeformat, locale);
			}
		};

		dateTimeFormat = new ThreadLocal<DateFormat>() {
			@Override
			protected DateFormat initialValue() {
				return new SimpleDateFormat(dateformat + " " + timeformat, locale);
			}
		};
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
		return locale;
	}

	@Override
	public LocaleInfo getLocaleInfo() {
		return localeInfo;
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
		if (!getResourceBundle().containsKey(key)) {
			if (date != null && date.before(getLastChange())) {
				flush();
			}
		}
		try {
			return getResourceBundle().getString(key);
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
		synchronized(rbLock) {
			resourceBundle = null;
		}
	}

	@Override
	public String getResourceById(LocaleInfo li, String key) {
		return remoteInterface.getResourceById(li, key);
	}
	
	@Override
	public void destroy() {
		TopicNotificationReceiver.unsubscribe(this);
		numberFormat.remove();
		dateFormat.remove();
		timeFormat.remove();
		dateTimeFormat.remove();
	}
}
