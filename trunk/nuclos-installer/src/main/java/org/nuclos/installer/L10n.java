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

package org.nuclos.installer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import org.nuclos.installer.util.PropUtils;


public class L10n {

	private static final String BUNDLE_NAME = "org/nuclos/installer/Messages";

	private static Locale locale;
	private static ResourceBundle bundle;
	private static ResourceBundle fallbackbundle;

	static {
		setLocale(Locale.getDefault());
		fallbackbundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH);
	}

	private L10n() {
	}

	public static ResourceBundle getBundle() {
		return bundle;
	}

	public static void setLocale(Locale l) {
		if (l != null) {
			locale = l;
			bundle = ResourceBundle.getBundle(BUNDLE_NAME, l);
		} else {
			locale = null;
			bundle = ResourceBundle.getBundle(BUNDLE_NAME);
		}
	}

	public static String getMessage(String text, Object...args) {
		text = get(text);

		List<String> paramList = new ArrayList<String>();

		if (args != null) {
			for (Object o : args) {
				String arg = "null";
				if (o instanceof String) {
					arg = (String) o;
				}
				else if (o != null) {
					arg = o.toString();
				}
				arg = get(arg);
				paramList.add(arg);
			}
		}

		if (text != null) {
			return MessageFormat.format(text, paramList.toArray());
		}
		else {
			return "null";
		}
	}

	public static String getLocalizedHtml(String prefix, Properties params) throws IOException {
		URL url = null;
		if (locale != null) {
			url = L10n.class.getClassLoader().getResource(prefix + "_" + locale.getLanguage() + ".html");
		}
		if (url == null) {
			url = L10n.class.getClassLoader().getResource(prefix + ".html");
		}
		if (url == null) {
			throw new IOException(L10n.getMessage("error.resource.notfound", prefix + ".html"));
		}

		Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        InputStream in = url.openStream();
        try {
            Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            in.close();
        }
        return PropUtils.resolveParameters(writer.toString(), params);
	}

	private static String get(String resId) {
		try {
			return bundle.getString(resId);
		}
		catch (Exception e) {
			try {
				return fallbackbundle.getString(resId);
			}
			catch (Exception e2) {
				return resId;
			}
		}
	}
}
