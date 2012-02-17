//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.spring;

import org.nuclos.common.security.UserFacadeRemote;
import org.nuclos.server.attribute.ejb3.AttributeFacadeBean;
import org.nuclos.server.attribute.ejb3.AttributeFacadeLocal;
import org.nuclos.server.attribute.ejb3.AttributeFacadeRemote;
import org.nuclos.server.attribute.ejb3.LayoutFacadeBean;
import org.nuclos.server.attribute.ejb3.LayoutFacadeLocal;
import org.nuclos.server.attribute.ejb3.LayoutFacadeRemote;
import org.nuclos.server.common.ejb3.LocaleFacadeBean;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.LocaleFacadeRemote;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.ejb3.NuclosFacadeLocal;
import org.nuclos.server.common.ejb3.ParameterFacadeBean;
import org.nuclos.server.common.ejb3.ParameterFacadeRemote;
import org.nuclos.server.common.ejb3.PreferencesFacadeBean;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.ejb3.SecurityFacadeBean;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.nuclos.server.common.ejb3.SecurityFacadeRemote;
import org.nuclos.server.common.ejb3.TaskFacadeBean;
import org.nuclos.server.common.ejb3.TaskFacadeLocal;
import org.nuclos.server.common.ejb3.TaskFacadeRemote;
import org.nuclos.server.common.ejb3.TestFacadeBean;
import org.nuclos.server.common.ejb3.TestFacadeRemote;
import org.nuclos.server.common.ejb3.TimelimitTaskFacadeBean;
import org.nuclos.server.common.ejb3.TimelimitTaskFacadeLocal;
import org.nuclos.server.common.ejb3.TimelimitTaskFacadeRemote;
import org.nuclos.server.console.ejb3.ConsoleFacadeBean;
import org.nuclos.server.console.ejb3.ConsoleFacadeLocal;
import org.nuclos.server.console.ejb3.ConsoleFacadeRemote;
import org.nuclos.server.customcode.ejb3.CodeFacadeBean;
import org.nuclos.server.customcode.ejb3.CodeFacadeRemote;
import org.nuclos.server.customcomp.ejb3.CustomComponentFacadeBean;
import org.nuclos.server.customcomp.ejb3.CustomComponentFacadeRemote;
import org.nuclos.server.dbtransfer.TransferFacadeBean;
import org.nuclos.server.dbtransfer.TransferFacadeLocal;
import org.nuclos.server.dbtransfer.TransferFacadeRemote;
import org.nuclos.server.fileimport.ejb3.ImportFacadeBean;
import org.nuclos.server.fileimport.ejb3.ImportFacadeLocal;
import org.nuclos.server.fileimport.ejb3.ImportFacadeRemote;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeBean;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GeneratorFacadeRemote;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeBean;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeBean;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeLocal;
import org.nuclos.server.genericobject.ejb3.GenericObjectGroupFacadeRemote;
import org.nuclos.server.job.ejb3.JobControlFacadeBean;
import org.nuclos.server.job.ejb3.JobControlFacadeLocal;
import org.nuclos.server.job.ejb3.JobControlFacadeRemote;
import org.nuclos.server.ldap.ejb3.LDAPDataFacadeBean;
import org.nuclos.server.ldap.ejb3.LDAPDataFacadeLocal;
import org.nuclos.server.ldap.ejb3.LDAPDataFacadeRemote;
import org.nuclos.server.livesearch.ejb3.LiveSearchFacadeBean;
import org.nuclos.server.livesearch.ejb3.LiveSearchFacadeRemote;
import org.nuclos.server.masterdata.ejb3.EntityFacadeBean;
import org.nuclos.server.masterdata.ejb3.EntityFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeRemote;
import org.nuclos.server.masterdata.ejb3.MasterDataModuleFacadeBean;
import org.nuclos.server.masterdata.ejb3.MasterDataModuleFacadeLocal;
import org.nuclos.server.masterdata.ejb3.MasterDataModuleFacadeRemote;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeBean;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeLocal;
import org.nuclos.server.navigation.ejb3.TreeNodeFacadeRemote;
import org.nuclos.server.processmonitor.ejb3.InstanceFacadeBean;
import org.nuclos.server.processmonitor.ejb3.InstanceFacadeLocal;
import org.nuclos.server.processmonitor.ejb3.InstanceFacadeRemote;
import org.nuclos.server.processmonitor.ejb3.ProcessMonitorFacadeBean;
import org.nuclos.server.processmonitor.ejb3.ProcessMonitorFacadeLocal;
import org.nuclos.server.processmonitor.ejb3.ProcessMonitorFacadeRemote;
import org.nuclos.server.report.ejb3.DatasourceFacadeBean;
import org.nuclos.server.report.ejb3.DatasourceFacadeLocal;
import org.nuclos.server.report.ejb3.DatasourceFacadeRemote;
import org.nuclos.server.report.ejb3.ReportFacadeBean;
import org.nuclos.server.report.ejb3.ReportFacadeLocal;
import org.nuclos.server.report.ejb3.ReportFacadeRemote;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeBean;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeLocal;
import org.nuclos.server.report.ejb3.SchedulerControlFacadeRemote;
import org.nuclos.server.resource.ejb3.ResourceFacadeBean;
import org.nuclos.server.resource.ejb3.ResourceFacadeRemote;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeBean;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeRemote;
import org.nuclos.server.ruleengine.ejb3.RuleInterfaceFacadeBean;
import org.nuclos.server.ruleengine.ejb3.RuleInterfaceFacadeLocal;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeBean;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeLocal;
import org.nuclos.server.ruleengine.ejb3.TimelimitRuleFacadeRemote;
import org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeBean;
import org.nuclos.server.searchfilter.ejb3.SearchFilterFacadeRemote;
import org.nuclos.server.security.UserFacadeBean;
import org.nuclos.server.security.UserFacadeLocal;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeBean;
import org.nuclos.server.servermeta.ejb3.ServerMetaFacadeRemote;
import org.nuclos.server.statemodel.ejb3.StateFacadeBean;
import org.nuclos.server.statemodel.ejb3.StateFacadeLocal;
import org.nuclos.server.statemodel.ejb3.StateFacadeRemote;
import org.nuclos.server.transfer.ejb3.XmlExportFacadeBean;
import org.nuclos.server.transfer.ejb3.XmlExportFacadeLocal;
import org.nuclos.server.transfer.ejb3.XmlExportFacadeRemote;
import org.nuclos.server.transfer.ejb3.XmlExportImportProtocolFacadeBean;
import org.nuclos.server.transfer.ejb3.XmlExportImportProtocolFacadeLocal;
import org.nuclos.server.transfer.ejb3.XmlExportImportProtocolFacadeRemote;
import org.nuclos.server.transfer.ejb3.XmlImportFacadeBean;
import org.nuclos.server.transfer.ejb3.XmlImportFacadeLocal;
import org.nuclos.server.transfer.ejb3.XmlImportFacadeRemote;
import org.nuclos.server.wiki.ejb3.WikiFacadeBean;
import org.nuclos.server.wiki.ejb3.WikiFacadeRemote;

class FacadeConstraints {
	
	private FacadeConstraints() {
		// Never invoked.
	}
	
	@SuppressWarnings("unused")
	private final class StateFacadeConstraints extends StateFacadeBean implements StateFacadeRemote, StateFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class ImportFacadeConstraints extends ImportFacadeBean implements ImportFacadeRemote, ImportFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class ReportFacadeConstraints extends ReportFacadeBean implements ReportFacadeLocal, ReportFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class DatasourceFacadeConstraints extends DatasourceFacadeBean implements DatasourceFacadeLocal, DatasourceFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class SchedulerControlConstraints extends SchedulerControlFacadeBean implements SchedulerControlFacadeLocal, SchedulerControlFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class GeneratorFacadeConstraints extends GeneratorFacadeBean implements GeneratorFacadeLocal, GeneratorFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class GenericObjectFacadeConstraints extends GenericObjectFacadeBean implements GenericObjectFacadeLocal, GenericObjectFacadeRemote {
	}

	@SuppressWarnings("unused")
	private final class GenericObjectGroupFacadeConstraints extends GenericObjectGroupFacadeBean implements GenericObjectGroupFacadeLocal, GenericObjectGroupFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class LayoutFacadeConstraints extends LayoutFacadeBean implements LayoutFacadeLocal, LayoutFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class AttributeFacadeContraints extends AttributeFacadeBean implements AttributeFacadeLocal, AttributeFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class PreferencesFacadeConstraints extends PreferencesFacadeBean implements PreferencesFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class LocaleFacadeConstraints extends LocaleFacadeBean implements LocaleFacadeLocal, LocaleFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class SecurityFacadeConstraints extends SecurityFacadeBean implements SecurityFacadeLocal, SecurityFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class TestFacadeConstraints extends TestFacadeBean implements TestFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class TimelimitTaskFacadeConstraints extends TimelimitTaskFacadeBean implements TimelimitTaskFacadeLocal, TimelimitTaskFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class NuclosFacadeConstraints extends NuclosFacadeBean implements NuclosFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class TaskFacadeConstraints extends TaskFacadeBean implements TaskFacadeRemote, TaskFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class ParameterFacadeConstraints extends ParameterFacadeBean implements ParameterFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class TreeNodeFacadeConstraints extends TreeNodeFacadeBean implements TreeNodeFacadeLocal, TreeNodeFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class ConsoleFacadeConstraints extends ConsoleFacadeBean implements ConsoleFacadeLocal, ConsoleFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class RuleEngineFacadeConstraints extends RuleEngineFacadeBean implements RuleEngineFacadeLocal, RuleEngineFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class TimelimitRuleFacadeConstraints extends TimelimitRuleFacadeBean implements TimelimitRuleFacadeLocal, TimelimitRuleFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class RuleInterfaceFacadeConstraints extends RuleInterfaceFacadeBean implements RuleInterfaceFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class CodeFacadeConstraints extends CodeFacadeBean implements CodeFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class XmlImportFacadeConstraints extends XmlImportFacadeBean implements XmlImportFacadeLocal, XmlImportFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class XmlExportFacadeConstraints extends XmlExportFacadeBean implements XmlExportFacadeLocal, XmlExportFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class XmlExportImportProtocolFacadeConstraints extends XmlExportImportProtocolFacadeBean 
		implements XmlExportImportProtocolFacadeLocal, XmlExportImportProtocolFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class ResourceFacadeConstraints extends ResourceFacadeBean implements ResourceFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class SearchFilterFacadeConstraints extends SearchFilterFacadeBean implements SearchFilterFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class UserFacadeConstraints extends UserFacadeBean implements UserFacadeRemote, UserFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class ServerMetaFacadeConstraints extends ServerMetaFacadeBean implements ServerMetaFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class InstanceFacadeConstraints extends InstanceFacadeBean implements InstanceFacadeLocal, InstanceFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class ProcessMonitorFacadeConstraints extends ProcessMonitorFacadeBean implements ProcessMonitorFacadeLocal, ProcessMonitorFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class LDAPDataFacadeConstraints extends LDAPDataFacadeBean implements LDAPDataFacadeLocal, LDAPDataFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class WikiFacadeBeanConstraints extends WikiFacadeBean implements WikiFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class TransferFacadeConstraints extends TransferFacadeBean implements TransferFacadeRemote, TransferFacadeLocal {
	}
	
	@SuppressWarnings("unused")
	private final class CustomComponentFacadeConstraints extends CustomComponentFacadeBean implements CustomComponentFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class LiveSearchFacadeConstraints extends LiveSearchFacadeBean implements LiveSearchFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class MasterDataFacadeConstraints extends MasterDataFacadeBean implements MasterDataFacadeLocal, MasterDataFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class MasterDataModuleFacadeConstraints extends MasterDataModuleFacadeBean implements MasterDataModuleFacadeLocal, MasterDataModuleFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class EntityFacadeConstraints extends EntityFacadeBean implements EntityFacadeRemote {
	}
	
	@SuppressWarnings("unused")
	private final class JobControlFacadeConstraints extends JobControlFacadeBean implements JobControlFacadeLocal, JobControlFacadeRemote {
	}
}
