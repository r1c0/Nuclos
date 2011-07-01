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

import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;



/**
 * Enum with all system Nuclos entities.  Use this enum if you want to refer to a system
 * entity (name) from code.
 */
public enum NuclosEntity {

	ACTION("action"),
	DATATYP("datatype"),
	CODE("code"),
	CUSTOMCOMPONENT("customcomponent"),
	DBOBJECTTYPE("dbobjecttype"),
	DBOBJECT("dbobject"),
	DBTYPE("dbtype"),
	DBSOURCE("dbsource"),
	DATASOURCE("datasource"),
	DATASOURCEUSAGE("datasourceUsage"),
	DYNAMICENTITY("dynamicEntity"),
	DYNAMICENTITYUSAGE("dynamicEntityUsage"),
	ENTITY("entity"),
	ENTITYFIELD("entityfield"),
	ENTITYFIELDGROUP("entityfieldgroup"),
	ENTITYSUBNODES("entitysubnodes"),
	ENTITYRELATION("entityrelation"),
	EVENT("event"),
	FORMUSAGE("formUsage"),
	GENERALSEARCHCOMMENT("generalsearchcomment"),
	GENERALSEARCHDOCUMENT("generalsearchdocument"),
	GENERATION("generation"),
	GENERATIONATTRIBUTE("generationAttribute"),
	GENERATIONSUBENTITY("generationSubentity"),
	GENERATIONSUBENTITYATTRIBUTE("generationSubentityAttribute"),
	GENERATIONUSAGE("generationUsage"),
	GENERICOBJECT("genericobject"),
	GENERICOBJECTGROUP("genericobjectgroup"),
	GENERICOBJECTLOGBOOK("genericobjectlogbook"),
	GENERICOBJECTRELATION("genericobjectrelation"),
	GROUP("group"),
	GROUPTYPE("grouptype"),
	IMAGE("image"),
	IMPORT("import"),
	IMPORTATTRIBUTE("importattribute"),
	IMPORTEXPORT("importexport"),
	IMPORTEXPORTMESSAGES("importexportmessages"),
	IMPORTFEIDENTIFIER("importfeidentifier"),
	IMPORTFILE("importfile"),
	IMPORTIDENTIFIER("importidentifier"),
	IMPORTUSAGE("importusage"),
	INSTANCE("instance"),
	INSTANCEOBJECTGENERATION("instanceObjectGeneration"),
	JOBCONTROLLER("jobcontroller"),
	JOBDBOBJECT("jobdbobject"),
	JOBRULE("jobrule"),
	JOBRUN("jobrun"),
	JOBRUNMESSAGES("jobrunmessages"),
	LAYOUT("layout"),
	LAYOUTUSAGE("layoutUsage"),
	LDAPMAPPING("ldapmapping"),
	LDAPSERVER("ldapserver"),
	LOCALE("locale"),
	LOCALERESOURCE("localeresource"),
	MASTERDATA("masterdata"),
	MODULE("module"),
	NUCLET("nuclet"),
	NUCLETDEPENDENCE("nucletDependence"),
	NUCLETCONTENTUID("nucletContentUID"),
	PARAMETER("parameter"),
	PROCESS("process"),
	PROCESSSTATEMODEL("processStateModel"),
	PROCESSTRANSITION("processTransition"),
	PROCESSMONITOR("processmonitor"),
	RECORDGRANT("recordGrant"),
	RECORDGRANTUSAGE("recordGrantUsage"),
	RELATIONTYPE("relationtype"),
	RELEASEHISTORY("releasehistory"),
	REPORT("report"),
	REPORTEXECUTION("reportExecution"),
	REPORTOUTPUT("reportoutput"),
	RESOURCE("resource"),
	ROLE("role"),
	ROLEACTION("roleaction"),
	ROLEATTRIBUTEGROUP("roleattributegroup"),
	ROLEENTITYFIELD("roleentityfield"),
	ROLEMASTERDATA("rolemasterdata"),
	ROLEMODULE("rolemodule"),
	ROLEREPORT("rolereport"),
	ROLESUBFORM("rolesubform"),
	ROLESUBFORMCOLUMN("rolesubformcolumn"),
	ROLETRANSITION("roletransition"),
	ROLEUSER("roleuser"),
	RULE("rule"),
	RULEUSAGE("ruleUsage"),
	RULEGENERATION("rulegeneration"),
	RULETRANSITION("ruletransition"),
	SEARCHFILTER("searchfilter"),
	SEARCHFILTERUSER("searchfilteruser"),
	SEARCHFILTERROLE("searchfilterrole"),
	STATE("state"),
	STATEHISTORY("statehistory"),
	STATEMANDATORYFIELD("statemandatoryfield"),
	STATEMANDATORYCOLUMN("statemandatorycolumn"),
	STATEMODEL("statemodel"),
	STATEMODELUSAGE("statemodelUsage"),
	STATETRANSITION("statetransition"),
	SUBREPORT("subreport"),
	TASKLIST("tasklist"),
	TASKSTATUS("taskstatus"),
	TASKOBJECT("taskobject"),
	TASKOWNER("taskowner"),
	TIMELIMITRULE("timelimitrule"),
	TIMELIMITTASK("timelimittask"),
	TASKFILES("taskfiles"),
	TRADE("trade"),
	USER("user"),
	VALUELISTPROVIDER("valuelistProvider"),
	VALUELISTPROVIDERUSAGE("valuelistProviderUsage"),
	WIKI("wiki"),
	WIKIMAPPING("wikimapping"),
	WIKIMAPPINGGENERAL("wikimappinggeneral"),
	WEBSERVICE("webservice"),
	WORKSPACE("workspace");

	private final String entityName;

	private NuclosEntity(String entityName) {
		this.entityName = "nuclos_" + entityName;
	}

	public String getEntityName() {
		return entityName;
	}

	public boolean checkEntityName(String entityName) {
		return (entityName != null) && entityName.equals(getEntityName());
	}
	
	public static boolean isNuclosEntity(String entityName) {
		return getByName(entityName)!=null;
	}

	public static NuclosEntity getByName(String entityName) {
		for (NuclosEntity entity : NuclosEntity.values()) {
			if (entity.checkEntityName(entityName))
				return entity;
		}
		return null;
	}

	public static String[] getEntityNames(NuclosEntity...entities) {
		return CollectionUtils.transformArray(entities, String.class, new Transformer<NuclosEntity, String>() {
			@Override
			public String transform(NuclosEntity e) { return e.getEntityName(); }
		});
	}
}
