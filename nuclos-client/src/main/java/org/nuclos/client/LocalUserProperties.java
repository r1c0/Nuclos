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
package org.nuclos.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.nuclos.client.ui.LookAndFeel;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LocaleInfo;

/**
 * Local user properties that cannot be stored on the server, because they are needed before
 * the client is connected to the server, such as the user name and the look & feel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LocalUserProperties extends java.util.Properties {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static LocalUserProperties singleton;

    private static final String KEY_LOOKANDFEEL = "look.and.feel";
    private static final String KEY_USERNAME = "user.name";
    private static final String KEY_PASSWD = "user.passwd";
    private static final String KEY_SERVERNAME = "nucleus.server.name";

    public static final String KEY_PLOGIN_LOCALE_SELECTION = "login.locale.selection";
    public static final String KEY_LOGIN_TITLE = "login.lab.title";
    public static final String KEY_LAB_USERNAME = "login.lab.username";
    public static final String KEY_LAB_PASSWORD = "login.lab.password";
    public static final String KEY_LAB_SERVER = "login.lab.server";
    public static final String KEY_LANG_AUTOLOGIN = "login.lab.autologin";
    public static final String KEY_LOCALE = "login.def.locale";
    public static final String KEY_ERR_UPASS = "login.err.upass";
    public static final String KEY_ERR_UPERM = "login.err.uperm";
    public static final String KEY_ERR_ADMIN = "login.err.admin";
    public static final String KEY_ERR_SERVER = "login.err.server";
    public static final String KEY_LANG_SELECT = "login.lang.select";
    public static final String KEY_LANG_REGION = "login.lang.region";
    public static final String KEY_PLOGIN_LOCALE_ID = "login.locale.id";

    private static final Map<String, String> defaults;
    static {
    	HashMap<String, String> t = new HashMap<String, String>();
    	t.put(KEY_PLOGIN_LOCALE_ID, "1");
    	t.put(KEY_LOGIN_TITLE, "{0} Login");
    	t.put(KEY_LAB_USERNAME, "Username");
    	t.put(KEY_LAB_PASSWORD, "Password");
    	t.put(KEY_LAB_SERVER, "Server");
    	t.put(KEY_LANG_AUTOLOGIN, "Autologin");
    	t.put(KEY_LOCALE, "en_EN");
    	t.put(KEY_LANG_REGION, "Region/Language");
    	t.put(KEY_ERR_UPASS, "Wrong username/password");
    	t.put(KEY_ERR_UPERM, "You have no permission to execute this action.");
    	t.put(KEY_ERR_ADMIN, "At the moment, only administrators can log in");
    	t.put(KEY_ERR_SERVER, "Could not connect to server. \nPlease contact your system administrator.");
    	t.put(KEY_LANG_SELECT, "Your {0} client does not have a valid \ndefault region and language setting at the moment.\n\n Please select one below.");
    	defaults = Collections.unmodifiableMap(t);
    };

    public static synchronized LocalUserProperties getInstance() {
        if (singleton == null) {
            singleton = new LocalUserProperties();
        }
        return singleton;
    }

    private LocalUserProperties() {
        try {
            final InputStream in = new FileInputStream(this.getPropertiesFile());
            load(in);
            in.close();
        }
        catch (FileNotFoundException ex) {
            // The properties file doesn't exist.
            // So we start with empty or default values.
        }
        catch (IOException ex) {
            final String sMessage = "Lokale Benutzereinstellungen konnten nicht geladen werden.";
            throw new NuclosFatalException(sMessage, ex);
        }
    }

    private File getPropertiesFile() {
        File fileHomeDir = new File(System.getProperty("user.home"));
        String fileName = ApplicationProperties.getInstance().getAppId() + ".properties";
        fileName = System.getProperty("local.properties.filename", fileName);
        return new File(fileHomeDir, fileName);
    }

    public void store() {
        try {
            final OutputStream out = new FileOutputStream(this.getPropertiesFile());
            store(out, ApplicationProperties.getInstance().getAppId() + " Local User Properties");//ApplicationProperties.getInstance().getName() + " Local User Properties"
            out.close();
        }
        catch (IOException ex) {
            final String sMessage = "Lokale Benutzereinstellungen konnten nicht geladen werden.";
            throw new NuclosFatalException(sMessage, ex);
        }
    }	// store

    /**
     * @return the stored user name, if any.
     */
    public String getUserName() {
        return getProperty(KEY_USERNAME);
    }

    public void setUserName(String sUserName) {
        setProperty(KEY_USERNAME, sUserName);
    }

    /**
     * @return the stored password, if any
     */
    public String getUserPasswd() {
   	 return getProperty(KEY_PASSWD);
    }

    public void setUserPasswd(String pass) {
   	 setProperty(KEY_PASSWD, pass);
    }

    /**
     * @return the stored look&feel (default: WINDOWS)
     */
    public LookAndFeel getLookAndFeel() {
        LookAndFeel result = null;
        final String sLookAndFeel = getProperty(KEY_LOOKANDFEEL);
        if (sLookAndFeel != null) {
            result = new LookAndFeel(LookAndFeel.byName(sLookAndFeel));
        }
        else {
            result = new LookAndFeel(LookAndFeel.WINDOWS);
        }
        return result;
    }	// getLookAndFeel

    public void setLookAndFeel(LookAndFeel lf) {
        setProperty(KEY_LOOKANDFEEL, lf.getName());
    }

    public String getServerName() {
        return getProperty(KEY_SERVERNAME);
    }

    public void setServerName(String sServerName) {
        setProperty(KEY_SERVERNAME, sServerName);
    }

    public String getLoginResource(String key) {
        assert(defaults.containsKey(key));
        String s = getProperty(key);
        if(s == null)
            s = defaults.get(key);
        return s;
    }

    public Locale getLoginLocale() {
        String match = getLoginResource(KEY_LOCALE);
        for(Locale l : Locale.getAvailableLocales())
            if(match.equals(l.getLanguage() + "_" + l.getCountry()))
                return l;
        return Locale.ENGLISH;
    }

    public int getLoginLocaleId() {
        String s = getLoginResource(KEY_PLOGIN_LOCALE_ID);
        if(s != null)
        	return Integer.parseInt(s);
        return 0;
    }

    public void copyLoginResourcesFromResourceBundle(
    			Collection<LocaleInfo> allLocales,
    			LocaleInfo selectedLocale) {
        boolean changes = false;
        for(String key : defaults.keySet())
            if(CommonLocaleDelegate.isResourceId(key)) {
                String fromBundle = CommonLocaleDelegate.getText(key);
                String previous   = getProperty(key);
                changes |= previous == null || !fromBundle.equals(previous);
                setProperty(key, CommonLocaleDelegate.getText(key));
            }

        StringBuilder allLocalesCoded = new StringBuilder();
        for(LocaleInfo info : allLocales) {
            allLocalesCoded.append(info.localeId).append("\n")
            	.append(info.title).append("\n")
                .append(info.language).append("\n")
                .append(info.country).append("\n");
        }
        String newCoded = allLocalesCoded.toString();
        String previousCoded = getProperty(KEY_PLOGIN_LOCALE_SELECTION);
        changes |= previousCoded != null || !newCoded.equals(previousCoded);
        setProperty(KEY_PLOGIN_LOCALE_SELECTION, newCoded);

        String selLoc = Integer.toString(selectedLocale.localeId);
        String prevLoc = getProperty(KEY_PLOGIN_LOCALE_ID);
        changes |= prevLoc != null || !selLoc.equals(prevLoc);
        setProperty(KEY_PLOGIN_LOCALE_ID, selLoc);

        if(changes)
            store();
    }

    public List<LocaleInfo> getLoginLocaleSelection() {
    	String pre = getProperty(KEY_PLOGIN_LOCALE_SELECTION);
    	if(pre == null)
    		return null;
    	String[] valArr = pre.split("\n");
    	ArrayList<LocaleInfo> res = new ArrayList<LocaleInfo>();
    	for(int i = 0; i < valArr.length; i += 4) {
    		if(i + 3 < valArr.length) {
    			int id = Integer.parseInt(valArr[i]);
    			String title = valArr[i + 1];
    			String language = valArr[i + 2];
    			String country = valArr[i + 3];
    			res.add(new LocaleInfo(title, id, language, country));
    		}
    	}
    	return res;
    }

    public boolean hasLoginLocaleUserSetting() {
        return getProperty(KEY_LOCALE) != null || getLoginLocaleId() != 0;
    }

}	// class LocalUserProperties
