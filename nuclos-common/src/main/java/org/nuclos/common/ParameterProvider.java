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
package org.nuclos.common;



/**
 * Provides system parameters.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public interface ParameterProvider {

	public static final String KEY_ATTRIBUTE_AS_HEADLINE_IN_LOGBOOK = "Attribute as headline in logbook";

//	public static final String KEY_NUCLOS_SCHEMA = "Nucleus Schema";
//	public static final String KEY_NUCLOS_TABLESPACE = "Nucleus Tablespace";
	public static final String KEY_COMPOSITE_PROCESSES = "Composite Processes";
	public static final String KEY_SERVER_VALIDATES_ATTRIBUTEVALUES = "Server validates attribute values";
	public static final String KEY_CLIENT_VALIDATES_ATTRIBUTEVALUES = "Client validates attribute values";
	public static final String KEY_SERVER_VALIDATES_MASTERDATAVALUES = "Server validates masterdata values";
	public static final String KEY_CLIENT_VALIDATES_MASTERDATAVALUES = "Client validates masterdata values";
	public static final String KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TREE = "Max row count for search result in tree";
	public static final String KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TASKLIST = "Max row count for search result in tasklist";

	public static final String KEY_EXCEL_SHEET_NAME = "Excel Sheet Name 4pm Report";
	public static final String KEY_DEFAULT_GENERATION_COUNT_ATTRIBUTE = "Default generation count attribute";

	public static final String KEY_INTERNAL_INFORMATION_FILE_PATH = "Internal information file path";

	public static final String KEY_REPORT_MAXROWCOUNT = "Report Max Row Count";

	public static final String KEY_FOCUSSED_ITEM_BACKGROUND_COLOR = "Focussed item background color";
	public static final String KEY_MANDATORY_ITEM_BACKGROUND_COLOR = "Mandatory item background color";
	public static final String KEY_MANDATORY_ADDED_ITEM_BACKGROUND_COLOR = "Mandatory added item background color";

	public static final String KEY_NOTIFICATION_SENDER = "Notification Sender";
	public static final String KEY_HISTORICAL_STATE_CHANGED_COLOR = "Historical state changed color";
	public static final String KEY_HISTORICAL_STATE_NOT_TRACKED_COLOR = "Historical state not tracked color";
	public static final String KEY_ADDITIONAL_IMPORTS_FOR_RULES = "Additional Imports for Rules";
	public static final String KEY_USE_LDAP = "USE_LDAP";
	public static final String USE_LDAP_SERVER_NAME = "USE_LDAP_SERVER_NAME";
	public static final String KEY_LDAP_P_URL = "LDAP_P_URL";
	public static final String KEY_LDAP_P_USERNAME = "USE_LDAP_SERVER_USERNAME";
	public static final String KEY_LDAP_P_PASSWORD = "USE_LDAP_SERVER_PASSWORD";
	public static final String KEY_LDAP_P_SEARCH_CONTEXT = "LDAP_P_SEARCH_CONTEXT";
	public static final String KEY_LDAP_P_SEARCH_FILTER = "LDAP_P_SEARCH_FILTER";
	public static final String KEY_LDAP_P_SEARCH_SCOPE = "LDAP_P_SEARCH_SCOPE";

	public static final String KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_UPDATE = "Generic Object Import Excluded Attributes For Update";
	public static final String KEY_GENERICOBJECT_IMPORT_EXCLUDED_ATTRIBUTES_FOR_CREATE = "Generic Object Import Excluded Attributes For Create";

	public static final String KEY_GENERICOBJECT_IMPORT_PATH = "Generic Object Import Path";
	public static final String KEY_GENERICOBJECT_ARCHIVE_PATH = "Generic Object Archive Path";

	public static final String KEY_CLIENT_VALIDATION_LAYER_PAINTER_NAME = "Painter name for client validation layer";
	public static final String KEY_BLUR_FILTER = "Blur Filter";

	public static final String KEY_WIKI_BASE_URL = "Wiki base url";
	public static final String KEY_WIKI_INVALID_CHARACTERS = "Wiki invalid Characters";
	public static final String KEY_WIKI_MAPPING_ENABLED = "Wiki Mapping";
	public static final String KEY_WIKI_DEFAULT_PAGE = "Wiki defaultpage";
	public static final String KEY_WIKI_STARTPAGE = "Wiki startpage";

	public static final String KEY_DEFAULT_LOCALE = "Default Locale";

	public static final String JMS_MESSAGE_ALL_PARAMETERS_ARE_REVALIDATED = "All parameters are revalidated.";
	public static final String	KEY_TEMPLATE_USER	= "PREFERENCES_TEMPLATE_USER";

	public static final String KEY_DATASOURCE_TABLE_FILTER = "Datasource Table Filter";

	public static final String KEY_SMTP_AUTHENTICATION = "SMTP Authentication";
	public static final String KEY_SMTP_USERNAME = "SMTP Username";
	public static final String KEY_SMTP_PASSWORD = "SMTP Password";
	public static final String KEY_SMTP_SENDER = "SMTP Sender";
	public static final String KEY_SMTP_SERVER = "SMTP Server";
	public static final String KEY_SMTP_PORT = "SMTP Port";

	public static final String KEY_POP3_USERNAME = "POP3 Username";
	public static final String KEY_POP3_PASSWORD = "POP3 Password";
	public static final String KEY_POP3_SERVER = "POP3 Server";
	public static final String KEY_POP3_PORT = "POP3 Port";

	public static final String KEY_WSDL_RULECOMPILE_PATH = "WSDL_RULECOMPILE_PATH";
	public static final String KEY_WSDL_GENERATOR_COMPILED_CLASSES_PATH = "WSDL_GENERATOR_COMPILED_CLASSES_PATH";
	public static final String KEY_WSDL_IMPORT_PATH = "WSDL_IMPORT_PATH";

	public static final String KEY_CIPHER = "server.cryptfield.cipher";

	public static final String KEY_RESPLAN_RESOURCE_LIMIT = "ResPlan Resource Limit";

	public static final String KEY_THUMBAIL_SIZE = "THUMBNAIL_SIZE";

	public static final String KEY_TIMELIMIT_RULE_USER = "Timelimit Rule User";

	public static final String KEY_DRAG_CURSOR_HOLDING_TIME = "DRAG_CURSOR_HOLDING_TIME";

	public static final String KEY_SECURITY_LOCK_DAYS = "SECURITY_LOCK_USER_PERIOD";
	public static final String KEY_SECURITY_LOCK_ATTEMPTS = "SECURITY_LOCK_ATTEMPTS";
	public static final String KEY_SECURITY_PASSWORD_INTERVAL = "SECURITY_PASSWORD_INTERVAL";
	public static final String KEY_SECURITY_PASSWORD_HISTORY_NUMBER = "SECURITY_PASSWORD_HISTORY_NUMBER";
	public static final String KEY_SECURITY_PASSWORD_HISTORY_DAYS = "SECURITY_PASSWORD_HISTORY_DAYS";
	public static final String KEY_SECURITY_PASSWORD_STRENGTH_LENGTH = "SECURITY_PASSWORD_STRENGTH_LENGTH";
	public static final String KEY_SECURITY_PASSWORD_STRENGTH_REGEXP = "SECURITY_PASSWORD_STRENGTH_REGEXP";
	public static final String KEY_SECURITY_PASSWORD_NOTIFICATION_SUBJECT = "SECURITY_PASSWORD_NOTIFICATION_SUBJECT";
	public static final String KEY_SECURITY_PASSWORD_NOTIFICATION_BODY = "SECURITY_PASSWORD_NOTIFICATION_BODY";

	/**
	 * @param sParameterName
	 * @return the value for the parameter with the given name, if any.
	 */
	String getValue(String sParameterName);

	/**
	 * gets an int value
	 * @param sParameterName
	 * @param iDefaultValue
	 * @return the int value from the parameters or the default value.
	 */
	int getIntValue(String sParameterName, int iDefaultValue);

}	// interface ParameterProvider
