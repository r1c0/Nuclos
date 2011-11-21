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
 * Action value object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:sekip.topcu@novabit.de">M. Sekip Top\u00e7u</a>
 * @version 01.00
 */
public class Actions {

	public static final String ACTION_SYSTEMSTART 								= "Login";//"Systemstart";
	public static final String ACTION_TIMELIMIT_LIST 							= "EditTimelimits";//"Fristenliste bearbeiten";
	public static final String ACTION_MANAGEMENT_CONSOLE 						= "UseManagementConsole";//"Management Console ausf\u00fchren";
	public static final String ACTION_DELETE_RECORD 							= "PhysicallyDeleteGenericObjects";//"Modulobjekte physikalisch l\u00f6schen";
	public static final String ACTION_READ_DELETED_RECORD 					= "ViewDeletedGenericObjects";//"Gel\u00f6schte Modulobjekte sehen";
	public static final String ACTION_USE_INVALID_MASTERDATA 				= "ViewInvalidMasterData";//"Ung\u00fcltige Stammdaten in Modulobjekten verwenden";
	public static final String ACTION_EXECUTE_RULE_BY_USER 					= "ExecuteRulesManually";//"Regelausf\u00fchrung manuell starten";
	public static final String ACTION_PRINT_SEARCHRESULT 						= "PrintSearchResultList";//"Suchergebnisliste drucken";
	public static final String ACTION_XML_EXPORT_IMPORT						= "XMLTransfer";//"XML Ex/Import";
	public static final String ACTION_TRANSLATION								= "EditTranslationsForLocale";//"\u00dcbersetzungen bearbeiten";
	public static final String ACTION_TASKLIST 								= "TaskList";//"Aufgabenliste";
	public static final String ACTION_PRINT_TASKLIST 						= "PrintTaskList";//"Aufgabenliste drucken";
	public static final String ACTION_WORKSPACE_ASSIGN						= "WorkspaceAssignment";//"Arbeitsumgebung: Zuweisen";
	public static final String ACTION_WORKSPACE_CREATE_NEW					= "WorkspaceCreateNew";//"Arbeitsumgebung: Anlegen";
	public static final String ACTION_WORKSPACE_CUSTOMIZE_STARTTAB			= "WorkspaceCustomizeStarttab";//"Arbeitsumgebung zugewiesen: Starttabs individualisieren";
	public static final String ACTION_WORKSPACE_CUSTOMIZE_ENTITY_AND_SUBFORM_COLUMNS = "WorkspaceCustomizeEntityAndSubFormColumn";//"Arbeitsumgebung zugewiesen: Entitäten- und Unterformularspalten individualisieren";
	public static final String ACTION_EXECUTE_REPORTS = "ExecuteReports";
}	// interface Actions
