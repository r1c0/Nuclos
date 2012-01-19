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
package org.nuclos.server.masterdata;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.nuclos.common.MarshalledValue;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.NuclosImage;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common.valueobject.EntityRelationshipModelVO;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.IdUtils;
import org.nuclos.common2.KeyEnum;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.common.valueobject.TaskObjectVO;
import org.nuclos.server.common.valueobject.TaskVO;
import org.nuclos.server.common.valueobject.TimelimitTaskVO;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.genericobject.valueobject.GeneratorActionVO;
import org.nuclos.server.genericobject.valueobject.GeneratorUsageVO;
import org.nuclos.server.genericobject.valueobject.GenericObjectRelationVO;
import org.nuclos.server.genericobject.valueobject.LogbookVO;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVO;
import org.nuclos.server.masterdata.valueobject.RoleTransitionVO;
import org.nuclos.server.processmonitor.valueobject.ProcessMonitorVO;
import org.nuclos.server.processmonitor.valueobject.ProcessTransitionVO;
import org.nuclos.server.processmonitor.valueobject.SubProcessVO;
import org.nuclos.server.report.ByteArrayCarrier;
import org.nuclos.server.report.valueobject.DatasourceVO;
import org.nuclos.server.report.valueobject.DynamicEntityVO;
import org.nuclos.server.report.valueobject.RecordGrantVO;
import org.nuclos.server.report.valueobject.ReportOutputVO;
import org.nuclos.server.report.valueobject.ReportVO;
import org.nuclos.server.report.valueobject.ReportVO.ReportType;
import org.nuclos.server.report.valueobject.SubreportVO;
import org.nuclos.server.report.valueobject.ValuelistProviderVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineGenerationVO;
import org.nuclos.server.ruleengine.valueobject.RuleEngineTransitionVO;
import org.nuclos.server.ruleengine.valueobject.RuleEventUsageVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.statemodel.valueobject.AttributegroupPermissionVO;
import org.nuclos.server.statemodel.valueobject.EntityFieldPermissionVO;
import org.nuclos.server.statemodel.valueobject.MandatoryColumnVO;
import org.nuclos.server.statemodel.valueobject.MandatoryFieldVO;
import org.nuclos.server.statemodel.valueobject.StateHistoryVO;
import org.nuclos.server.statemodel.valueobject.StateModelLayout;
import org.nuclos.server.statemodel.valueobject.StateModelVO;
import org.nuclos.server.statemodel.valueobject.StateTransitionVO;
import org.nuclos.server.statemodel.valueobject.StateVO;
import org.nuclos.server.statemodel.valueobject.SubformColumnPermissionVO;
import org.nuclos.server.statemodel.valueobject.SubformPermissionVO;

public class MasterDataWrapper {

	public static MasterDataVO wrapREUsageVO(RuleEventUsageVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("event", vo.getEvent());
		mpFields.put("entity", vo.getEntity());
		mpFields.put("ruleId", vo.getRuleId());
		mpFields.put("rule", vo.getRule());
		mpFields.put("order", vo.getOrder());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static RuleEventUsageVO getREUsageVO(MasterDataVO mdVO) {
		RuleEventUsageVO vo = new RuleEventUsageVO(
			mdVO.getIntId(),
			(String)mdVO.getField("event"),
			(String)mdVO.getField("entity"),
			(Integer)mdVO.getField("ruleId"),
			(String)mdVO.getField("rule"),
			(Integer)mdVO.getField("order"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapRuleVO(RuleVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("rule", vo.getName());
		mpFields.put("description", vo.getDescription());
		mpFields.put("source", vo.getRuleSource());
		mpFields.put("nucletId", vo.getNucletId());
		mpFields.put("active", vo.isActive());
		mpFields.put("debug", vo.isDebug());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static MasterDataVO wrapCodeVO(CodeVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("description", vo.getDescription());
		mpFields.put("source", vo.getSource());
		mpFields.put("active", vo.isActive());
		mpFields.put("debug", vo.isDebug());
		mpFields.put("nucletId", vo.getNucletId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static RuleVO getRuleVO(MasterDataVO mdVO) {
		String name = (String)mdVO.getField("rule");
		if(name == null && mdVO.getField("name") != null)
			name = (String)mdVO.getField("name");
		RuleVO vo = new RuleVO(
			new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			name,
			(String)mdVO.getField("description"),
			(String)mdVO.getField("source"),
			mdVO.getField("nucletId", Integer.class),
			mdVO.getField("active") == null ? false : (Boolean)mdVO.getField("active"),
			mdVO.getField("debug") == null ? false : (Boolean)mdVO.getField("debug"));


		return vo;
	}

	public static CodeVO getCodeVO(MasterDataVO mdVO) {
		String name = (String)mdVO.getField("name");
		CodeVO vo = new CodeVO(
			new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			name,
			(String)mdVO.getField("description"),
			(String)mdVO.getField("source"),
			mdVO.getField("active") == null ? false : (Boolean)mdVO.getField("active"),
			mdVO.getField("debug") == null ? false : (Boolean)mdVO.getField("debug"),
			(Integer)mdVO.getField("nucletId"));

		return vo;
	}

	public static GeneratorActionVO getGeneratorActionVO(MasterDataVO mdVO, Collection<GeneratorUsageVO> usages) {
		GeneratorActionVO vo = new GeneratorActionVO(
			mdVO.getIntId(),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("label"),
			(Integer)mdVO.getField("sourceModuleId"),
			(Integer)mdVO.getField("targetModuleId"),
			(Integer)mdVO.getField("targetProcessId"),
			(Integer)mdVO.getField("parameterEntityId"),
			(Integer)mdVO.getField("subProcessTransition"),
			usages);
		if(mdVO.getField("groupattributes") != null)
			vo.setGroupAttributes((Boolean)mdVO.getField("groupattributes"));
		if(mdVO.getField("createRelation") != null)
			vo.setCreateRelationBetweenObjects((Boolean)mdVO.getField("createRelation"));
		if(mdVO.getField("createParameterRelation") != null)
			vo.setCreateRelationToParameterObject((Boolean)mdVO.getField("createParameterRelation"));
		if(mdVO.getField("parameterValuelistId") != null)
			vo.setValuelistProviderId((Integer)mdVO.getField("parameterValuelistId"));
		return vo;
	}

	public static MasterDataVO wrapGeneratorActionVO(GeneratorActionVO gaVO) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", gaVO.getName());
		mpFields.put("label", gaVO.getLabel());
		mpFields.put("sourceModuleId", gaVO.getSourceModuleId());
		mpFields.put("targetModuleId", gaVO.getTargetModuleId());
		mpFields.put("targetProcessId", gaVO.getTargetProcessId());
		mpFields.put("parameterEntityId", gaVO.getParameterEntityId());
		mpFields.put("subProcessTransition", gaVO.getCaseTransitionId());
		mpFields.put("createRelation", gaVO.isCreateRelationBetweenObjects());
		mpFields.put("createParameterRelation", gaVO.isCreateRelationToParameterObject());

		return new MasterDataVO(gaVO.getId(), null, null, null, null, null, mpFields);
	}

	public static GeneratorUsageVO getGeneratorUsageVO(MasterDataVO mdVO) {
		return new GeneratorUsageVO(
			(String)mdVO.getField("state"),
			(Integer)mdVO.getField("processId"));
	}

	public static RuleEngineGenerationVO getRuleEngineGenerationVO(MasterDataVO mdVO) {
		RuleEngineGenerationVO vo = new RuleEngineGenerationVO(
			mdVO.getIntId(),
			(Integer)mdVO.getField("generationId"),
			(Integer)mdVO.getField("ruleId"),
			(Integer)mdVO.getField("order"),
			(Boolean)mdVO.getField("runafterwards"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapRuleEngineGenerationVO(RuleEngineGenerationVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("generationId", vo.getGenerationId());
		mpFields.put("ruleId", vo.getRuleId());
		mpFields.put("order", vo.getOrder());
		mpFields.put("runafterwards" , vo.isRunAfterwards());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static RuleEngineTransitionVO getRuleEngineTransitionVO(MasterDataVO mdVO) {
		RuleEngineTransitionVO vo = new RuleEngineTransitionVO(
			mdVO.getIntId(),
			(Integer)mdVO.getField("transitionId"),
			(Integer)mdVO.getField("ruleId"),
			(Integer)mdVO.getField("order"),
			(Boolean)mdVO.getField("runafterwards"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapRuleEngineTransitionVO(RuleEngineTransitionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("transitionId", vo.getTransitionId());
		mpFields.put("ruleId", vo.getRuleId());
		mpFields.put("order", vo.getOrder());
		mpFields.put("runafterwards", vo.isRunAfterwards());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static StateTransitionVO getStateTransitionVOWithoutDependants(MasterDataVO mdVO) {
		StateTransitionVO vo = new StateTransitionVO(
			mdVO.getIntId(),
			(Integer)mdVO.getField("state1Id"),
			(Integer)mdVO.getField("state2Id"),
			(String)mdVO.getField("description"),
			(Boolean)mdVO.getField("automatic"),
			mdVO.getField("default") == null ? false : (Boolean)mdVO.getField("default"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static StateTransitionVO getStateTransitionVO(MasterDataWithDependantsVO mdVO) {
		StateTransitionVO vo = getStateTransitionVOWithoutDependants(mdVO);

		Collection<EntityObjectVO> mdRules = mdVO.getDependants().getData(NuclosEntity.RULETRANSITION.getEntityName());
		List<Pair<Integer, Boolean>> rules = new ArrayList<Pair<Integer, Boolean>>();
		for (EntityObjectVO md : mdRules) {
			rules.add(new Pair<Integer, Boolean>(md.getField("ruleId", Integer.class), md.getField("runafterwards", Boolean.class)));
		}

		Collection<EntityObjectVO> mdRoles = mdVO.getDependants().getData(NuclosEntity.ROLETRANSITION.getEntityName());
		List<Integer> roleIds = new ArrayList<Integer>();
		for (EntityObjectVO md : mdRoles)
			roleIds.add(md.getField("roleId", Integer.class));

		vo.setRuleIdsWithRunAfterwards(rules);
		vo.setRoleIds(roleIds);

		return vo;
	}

	public static MasterDataVO wrapStateTransitionVO(StateTransitionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("state1Id", vo.getStateSource());
		mpFields.put("state2Id", vo.getStateTarget());
		mpFields.put("automatic", vo.isAutomatic());
		mpFields.put("default", vo.isDefault());
		mpFields.put("description", vo.getDescription());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static RoleTransitionVO getRoleTransitionVO(MasterDataVO mdVO) {
		RoleTransitionVO vo = new RoleTransitionVO(
			mdVO.getIntId(),
			(Integer)mdVO.getField("transitionId"),
			(Integer)mdVO.getField("roleId"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapRoleTransitionVO(RoleTransitionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("transitionId", vo.getTransitionId());
		mpFields.put("roleId", vo.getRoleId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static StateModelVO getStateModelVO(MasterDataVO mdVO) {
		StateModelLayout layout = null;
		String xmlLayout = null;

		try {

			byte b[] = (byte[])mdVO.getField("layout");
			Object o = null;

			String xml = new String(b);
			if(xml != null && xml.startsWith("<?xml")) {
				o = xml;
			}
			else {
				o = IOUtils.fromByteArray(b);
			}

			if (o == null) {
				throw new NuclosFatalException("StateModelLayout == null");
			}
			else if (o instanceof MarshalledValue) {
				// for backwards compatibility:
				layout = (StateModelLayout) ((MarshalledValue) o).get();
			}
			else if (o instanceof StateModelLayout) {
				layout = (StateModelLayout) o;
			}
			else if(o instanceof String) {
				xmlLayout = (String)o;
			}
			else {
				throw new NuclosFatalException("Unexpected class: " + o.getClass().getName());
			}
		}
		catch(IOException e) {
			throw new NuclosFatalException("can't read StateModelLayout");
		}
		catch(ClassNotFoundException e) {
			throw new NuclosFatalException("can't read StateModelLayout");
		}

		StateModelVO vo = new StateModelVO(getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			layout, xmlLayout, (Integer)mdVO.getField("nucletId"));

		return vo;
	}

	public static EntityRelationshipModelVO getEntityRelationshipModelVO(MasterDataVO mdVO) {

		EntityRelationshipModelVO vo = new EntityRelationshipModelVO(getBaseVO(mdVO), (String)mdVO.getField("name"),
			(String)mdVO.getField("description"));

		return vo;
	}


	public static MasterDataVO wrapStateModelVO(StateModelVO vo) {

		byte[] layoutData = null;

		if (vo.getLayout() == null && vo.getXMLLayout() == null) {
			throw new NullArgumentException("layoutinfo");
		}
		try {
			layoutData = IOUtils.toByteArray(vo.getLayout());
			assert layoutData != null;
			assert layoutData.length > 0;
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}

		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("description", vo.getDescription());
//		if(vo.getXMLLayout() != null && vo.getXMLLayout().length() > 0) {
//			layoutData = vo.getXMLLayout().getBytes();
//		}
		mpFields.put("layout", layoutData);
		mpFields.put("nucletId", vo.getNucletId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static StateHistoryVO getStateHistoryVO(MasterDataVO mdVO) {
		StateHistoryVO vo = new StateHistoryVO(
			mdVO.getIntId(),
			(Integer)mdVO.getField("genericObjectId"),
			(Integer)mdVO.getField("stateId"),
			(String)mdVO.getField("state"),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapStateHistoryVO(StateHistoryVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("genericObjectId", vo.getGenericObjectId());
		mpFields.put("stateId", vo.getStateId());
		mpFields.put("state", vo.getStateName());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static StateVO getStateVO(MasterDataVO mdVO) {
		StateVO vo = new StateVO(
			getBaseVO(mdVO),
			(Integer)mdVO.getField("numeral"),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			(NuclosImage)mdVO.getField("icon"),
			(Integer)mdVO.getField("modelId"));
		vo.setTabbedPaneName((String)mdVO.getField("tab"));

		return vo;
	}

	public static MasterDataVO wrapStateVO(StateVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("numeral", vo.getNumeral());
		mpFields.put("name", vo.getStatename());
		mpFields.put("description", vo.getDescription());
		mpFields.put("icon", vo.getIcon());
		mpFields.put("modelId", vo.getModelId());
		mpFields.put("tab", vo.getTabbedPaneName());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static MandatoryFieldVO getMandatoryFieldVO(MasterDataVO mdVO) {
		MandatoryFieldVO vo = new MandatoryFieldVO(
			getBaseVO(mdVO),
			(Integer)mdVO.getField("entityfieldId"),
			(Integer)mdVO.getField("stateId"));

		return vo;
	}

	public static MasterDataVO wrapMandatoryFieldVO(MandatoryFieldVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("entityfieldId", vo.getFieldId());
		mpFields.put("stateId", vo.getStateId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static MandatoryColumnVO getMandatoryColumnVO(MasterDataVO mdVO) {
		MandatoryColumnVO vo = new MandatoryColumnVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("entity"),
			(String)mdVO.getField("column"),
			(Integer)mdVO.getField("stateId"));

		return vo;
	}

	public static MasterDataVO wrapMandatoryColumnVO(MandatoryColumnVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("entity", vo.getEntity());
		mpFields.put("column", vo.getColumn());
		mpFields.put("stateId", vo.getStateId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static AttributegroupPermissionVO getAttributegroupPermissionVO(MasterDataVO mdVO) {
		AttributegroupPermissionVO vo = new AttributegroupPermissionVO(
			getBaseVO(mdVO),
			(Integer)mdVO.getField("attributegroupId"),
			(String)mdVO.getField("attributegroup"),
			(Integer)mdVO.getField("roleId"),
			(String)mdVO.getField("role"),
			(Integer)mdVO.getField("stateId"),
			(String)mdVO.getField("state"),
			(Boolean)mdVO.getField("readwrite"));

		return vo;
	}

	public static MasterDataVO wrapAttributegroupPermissionVO(AttributegroupPermissionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("attributegroupId", vo.getAttributegroupId());
		mpFields.put("attributegroup", vo.getAttributegroup());
		mpFields.put("roleId", vo.getRoleId());
		mpFields.put("role", vo.getRoleId());
		mpFields.put("stateId", vo.getStateId());
		mpFields.put("state", vo.getState());
		mpFields.put("readwrite", vo.isWritable());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static EntityFieldPermissionVO getEntityFieldPermissionVO(MasterDataVO mdVO) {
		EntityFieldPermissionVO vo = new EntityFieldPermissionVO(
			getBaseVO(mdVO),
			(Integer)mdVO.getField("entityfieldId"),
			(Integer)mdVO.getField("roleId"),
			(Integer)mdVO.getField("stateId"),
			(Boolean)mdVO.getField("read"),
			(Boolean)mdVO.getField("readwrite"));

		return vo;
	}

	public static MasterDataVO wrapEntityFieldPermissionVO(EntityFieldPermissionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("entityfieldId", vo.getFieldId());
		mpFields.put("roleId", vo.getRoleId());
		mpFields.put("stateId", vo.getStateId());
		mpFields.put("read", vo.isReadable());
		mpFields.put("readwrite", vo.isWriteable());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static SubformPermissionVO getSubformPermissionVO(MasterDataVO mdVO) {
		SubformPermissionVO vo = new SubformPermissionVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("entity"),
			(Integer)mdVO.getField("roleId"),
			(String)mdVO.getField("role"),
			(Integer)mdVO.getField("stateId"),
			(String)mdVO.getField("state"),
			(Boolean)mdVO.getField("readwrite"),
			null);

		return vo;
	}

	public static MasterDataVO wrapSubformPermissionVO(SubformPermissionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("entity", vo.getSubform());
		mpFields.put("roleId", vo.getRoleId());
		mpFields.put("role", vo.getRole());
		mpFields.put("stateId", vo.getStateId());
		mpFields.put("state", vo.getState());
		mpFields.put("readwrite", vo.isWriteable());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static SubformColumnPermissionVO getSubformColumnPermissionVO(MasterDataVO mdVO) {
		SubformColumnPermissionVO vo = new SubformColumnPermissionVO(
			getBaseVO(mdVO),
			(Integer)mdVO.getField("rolesubformId"),
			(String)mdVO.getField("column"),
			(Boolean)mdVO.getField("readwrite"));

		return vo;
	}

	public static MasterDataVO wrapSubformColumnPermissionVO(SubformColumnPermissionVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("rolesubformId", vo.getRoleSubformId());
		mpFields.put("column", vo.getColumn());
		mpFields.put("readwrite", vo.isWriteable());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static DatasourceVO getDatasourceVO(MasterDataVO mdVO, String currentUserName) {

		int permission = DatasourceVO.PERMISSION_NONE;

		if (SecurityCache.getInstance().getWritableDataSourceIds(currentUserName).contains(mdVO.getIntId()))
			permission = DatasourceVO.PERMISSION_READWRITE;
		else if (SecurityCache.getInstance().getReadableDataSourceIds(currentUserName).contains(mdVO.getIntId()))
			permission = DatasourceVO.PERMISSION_READONLY;

		DatasourceVO vo = new DatasourceVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			(Boolean)mdVO.getField("valid"),
			(String)mdVO.getField("source"),
			mdVO.getField("nucletId", Integer.class),
			permission);

		return vo;
	}

	public static MasterDataVO wrapDatasourceVO(DatasourceVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("description", vo.getDescription());
		mpFields.put("valid", vo.getValid());
		mpFields.put("source", vo.getSource());
		mpFields.put("nucletId", vo.getNucletId());
		if (vo instanceof RecordGrantVO)
			mpFields.put("entity", ((RecordGrantVO) vo).getEntity());
		else if(vo instanceof DynamicEntityVO)
			mpFields.put("entity", ((DynamicEntityVO) vo).getEntity());
		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static ValuelistProviderVO getValuelistProviderVO(MasterDataVO mdVO) {
		ValuelistProviderVO vo = new ValuelistProviderVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			(Boolean)mdVO.getField("valid"),
			(String)mdVO.getField("source"),
			mdVO.getField("nucletId", Integer.class));

		return vo;
	}

	public static RecordGrantVO getRecordGrantVO(MasterDataVO mdVO) {
		RecordGrantVO vo = new RecordGrantVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			mdVO.getField("entity", String.class),
			(Boolean)mdVO.getField("valid"),
			(String)mdVO.getField("source"),
			mdVO.getField("nucletId", Integer.class));

		return vo;
	}

	public static MasterDataVO wrapValuelistProviderVO(ValuelistProviderVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("description", vo.getDescription());
		mpFields.put("valid", vo.getValid());
		mpFields.put("source", vo.getSource());
		mpFields.put("nucletId" , vo.getNucletId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static DynamicEntityVO getDynamicEntityVO(MasterDataVO mdVO) {
		DynamicEntityVO vo = new DynamicEntityVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			(String)mdVO.getField("entity"),
			(Boolean)mdVO.getField("valid"),
			(String)mdVO.getField("source"),
			mdVO.getField("nucletId", Integer.class));

		return vo;
	}

	public static MasterDataVO wrapValuelistProviderVO(DynamicEntityVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("description", vo.getDescription());
		mpFields.put("valid", vo.getValid());
		mpFields.put("source", vo.getSource());
		mpFields.put("nucletId", vo.getNucletId());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static TaskVO getTaskVO(MasterDataWithDependantsVO mdVO, Map<Long, String> mapObjectIdentifier) {
		Collection<EntityObjectVO> mdTaskObjects = mdVO.getDependants().getData(NuclosEntity.TASKOBJECT.getEntityName());

		Collection<TaskObjectVO> taskObjects = new ArrayList<TaskObjectVO>();
		for (EntityObjectVO md : mdTaskObjects) {
			taskObjects.add(getTaskObjectVO(DalSupportForMD.wrapEntityObjectVO(md), mapObjectIdentifier != null ? mapObjectIdentifier.get(IdUtils.toLongId(md.getField("entityId"))) : null));
		}

		TaskVO vo = new TaskVO(
			mdVO.getIntId(),
			(String)mdVO.getField("name"),
			(Integer)mdVO.getField("visibility"),
			(Integer)mdVO.getField("priority"),
			mdVO.getField("scheduled") != null ? new java.util.Date(((java.sql.Date)mdVO.getField("scheduled")).getTime()) : null,
			mdVO.getField("completed") != null ? new java.util.Date(((java.sql.Date)mdVO.getField("completed")).getTime()) : null,
			(Integer)mdVO.getField("taskdelegatorId"),
			(String)mdVO.getField("taskdelegator"),
			(Integer)mdVO.getField("taskstatusId"),
			(String)mdVO.getField("taskstatus"),
			(String)mdVO.getField("description"),
			(String)mdVO.getField("comment"),
			taskObjects,
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());
		return vo;
	}

	public static MasterDataWithDependantsVO wrapTaskVO(TaskVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getName());
		mpFields.put("taskdelegator", vo.getDelegator());
		mpFields.put("taskdelegatorId", vo.getDelegatorId());
		mpFields.put("priority", vo.getPriority());
		mpFields.put("comment", vo.getComment());
		mpFields.put("visibility", vo.getVisibility());

		if (vo.getScheduled() != null)
			mpFields.put("scheduled", new java.sql.Date(vo.getScheduled().getTime()));
		if (vo.getCompleted() != null)
			mpFields.put("completed", new java.sql.Date(vo.getCompleted().getTime()));

		DependantMasterDataMap dependants = new DependantMasterDataMap();
		for (TaskObjectVO toVO : vo.getRelatedObjects())
			dependants.addData(NuclosEntity.TASKOBJECT.getEntityName(), DalSupportForMD.getEntityObjectVO(wrapTaskObjectVO(toVO)));

		MasterDataVO mdVO = new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);

		return new MasterDataWithDependantsVO(mdVO,dependants);
	}

	public static TaskObjectVO getTaskObjectVO(MasterDataVO mdVO, String sIdentifier) {
		TaskObjectVO vo = new TaskObjectVO(
			mdVO.getIntId(),
			IdUtils.toLongId(mdVO.getField("entityId")),
			IdUtils.toLongId(mdVO.getField("tasklistId")),
			(String)mdVO.getField("entity"),
			sIdentifier,
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapTaskObjectVO(TaskObjectVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("entityId", vo.getObjectId().intValue());
		mpFields.put("tasklistId", vo.getTaskId().intValue());
		mpFields.put("entity", vo.getEntityName());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static TimelimitTaskVO getTimelimitTaskVO(MasterDataVO mdVO, String sIdentifier, String sStatus, String sProcess) {
		TimelimitTaskVO vo = new TimelimitTaskVO(
			mdVO.getIntId(),
			(String)mdVO.getField("description"),
			(Date)mdVO.getField("expired"),
			(Date)mdVO.getField("completed"),
			(Integer)mdVO.getField("genericobjectId"),
			(Integer)mdVO.getField("module"),
			sIdentifier,
			sStatus,
			sProcess,
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());

		return vo;
	}

	public static MasterDataVO wrapTimelimitTaskVO(TimelimitTaskVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("genericobjectId", vo.getGenericObjectId());
		mpFields.put("expired", vo.getExpired());
		mpFields.put("completed", vo.getCompleted());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static ReportOutputVO getReportOutputVO(MasterDataVO mdVO) {
		ReportOutputVO vo = new ReportOutputVO(
			new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			(Integer)mdVO.getField("parentId"),
			KeyEnum.Utils.findEnum(ReportOutputVO.Format.class, (String)mdVO.getField("format")),
			KeyEnum.Utils.findEnum(ReportOutputVO.Destination.class, (String)mdVO.getField("destination")),
			(String)mdVO.getField("parameter"),
			(String)mdVO.getField("sourceFile"),
			(ByteArrayCarrier)mdVO.getField("reportCLS"),
			(ByteArrayCarrier)mdVO.getField("sourceFileContent"),
			(Integer)mdVO.getField("datasourceId"),
			(String)mdVO.getField("datasource"),
			(String)mdVO.getField("sheetname"),
			(String)mdVO.getField("description"),
			(String)mdVO.getField("locale"));

		return vo;
	}

	public static MasterDataVO wrapReportOutputVO(ReportOutputVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("parentId", vo.getReportId());
		mpFields.put("format", KeyEnum.Utils.unwrap(vo.getFormat()));
		mpFields.put("destination", KeyEnum.Utils.unwrap(vo.getDestination()));
		mpFields.put("parameter", vo.getParameter());
		mpFields.put("sourceFile", vo.getSourceFile());
		mpFields.put("reportCLS", vo.getReportCLS());
		mpFields.put("sourceFileContent", vo.getSourceFileContent());
		mpFields.put("datasourceId", vo.getDatasourceId());
		mpFields.put("datasource", vo.getDatasource());
		mpFields.put("sheetname", vo.getSheetname());
		mpFields.put("description", vo.getDescription());
		mpFields.put("locale", vo.getLocale());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static SubreportVO getSubreportVO(MasterDataVO mdVO) {
		SubreportVO vo = new SubreportVO(new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			(Integer) mdVO.getField("reportoutputId"),
			(String) mdVO.getField("parametername"),
			(String) mdVO.getField("sourcefilename"),
			(ByteArrayCarrier) mdVO.getField("sourcefileContent"),
			(ByteArrayCarrier) mdVO.getField("reportCLS"));

		return vo;
	}

	public static MasterDataVO wrapSubreportVO(SubreportVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("reportoutputId", vo.getReportOutputId());
		mpFields.put("parametername", vo.getParameter());
		mpFields.put("sourcefilename", vo.getSourcefileName());
		mpFields.put("sourcefileContent", vo.getSourcefileContent());
		mpFields.put("reportCLS", vo.getReportCLS());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static ReportVO getReportVO(MasterDataVO mdVO, String currentUserName) {
		ReportVO vo = new ReportVO(
			new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			KeyEnum.Utils.findEnum(ReportType.class, (Integer)mdVO.getField("type")),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			(Integer)mdVO.getField("datasourceId"),
			(String)mdVO.getField("outputtype"),
			getReportPermission(mdVO.getIntId(), currentUserName));

		return vo;
	}

	private static Permission getReportPermission(Integer iReportId, String currentUserName) {
		final Permission result;

		// Note that we don't use Permission.getPermission() here for performance reasons.
		// @todo use Permission.getPermission() after refactoring getWritableReportIds()/getReadableReportIds()
		if (SecurityCache.getInstance().getWritableReportIds(currentUserName).contains(iReportId)) {
			result = Permission.READWRITE;
		}
		else if (CollectionUtils.concatAll(SecurityCache.getInstance().getReadableReports(currentUserName).values()).contains(iReportId)) {
			result = Permission.READONLY;
		}
		else {
			result = Permission.NONE;
		}
		return result;
	}

	public static MasterDataVO wrapGenericObjectRelationVO(GenericObjectRelationVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("sourceId", vo.getSourceGOId());
		mpFields.put("destinationId", vo.getDestinationGOId());
		mpFields.put("relationType", vo.getRelationType());
		mpFields.put("validFrom", vo.getValidFrom());
		mpFields.put("validUntil", vo.getValidUntil());
		mpFields.put("description", vo.getDescription());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	public static GenericObjectRelationVO getGenericObjectRelationVO(MasterDataVO mdVO) {
		GenericObjectRelationVO vo = new GenericObjectRelationVO(
			new NuclosValueObject(mdVO.getIntId(), mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion()),
			(Integer)mdVO.getField("sourceId"),
			(Integer)mdVO.getField("destinationId"),
			(String)mdVO.getField("relationType"),
			(Date)mdVO.getField("validFrom"),
			(Date)mdVO.getField("validUntil"),
			(String)mdVO.getField("description"));

		return vo;
	}

	public static MasterDataVO wrapMasterDataMetaVO(MasterDataMetaVO vo) {
		Map<String, Object> mpFields = new HashMap<String, Object>();
		String sFieldsForEquality = "";
		boolean isFirstField = true;
		for (String field : vo.getFieldsForEquality()) {
			if (isFirstField) {
				isFirstField = false;
			} else {
				sFieldsForEquality = sFieldsForEquality + ";";
			}
			sFieldsForEquality = sFieldsForEquality + "field";
		}
		mpFields.put("entity", vo.getEntityName());
		mpFields.put("dbentity", vo.getDBEntity());
		mpFields.put("menupath", vo.getMenuPath());
		mpFields.put("searchable", vo.isSearchable());
		mpFields.put("editable", vo.isEditable());
		mpFields.put("name", vo.getLabel());
		mpFields.put("lstFieldsForEquality", sFieldsForEquality);
		mpFields.put("cacheable", vo.isCacheable());
		mpFields.put("mp", vo);
		mpFields.put("treeview", vo.getTreeView());
		mpFields.put("treeviewdescription", vo.getDescription());
		mpFields.put("resource", vo.getResourceName());
		mpFields.put("importexport", vo.getIsImportExport());
		mpFields.put("labelplural", vo.getLabelPlural());
		mpFields.put("acceleratormodifier", vo.getAcceleratorModifier());
		mpFields.put("accelerator", vo.getAccelerator());
		return new MasterDataVO(vo.getId(), vo.getCreatedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	private static NuclosValueObject getBaseVO(MasterDataVO mdVO) {
		return new NuclosValueObject(
			mdVO.getIntId(),
			mdVO.getCreatedAt(),
			mdVO.getCreatedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());
	}

	public static ProcessTransitionVO getProcessTransitionVO(MasterDataVO mdVO) {
		ProcessTransitionVO result = new ProcessTransitionVO(mdVO.getIntId(),
			mdVO.getField("sourceCaseId", Integer.class),
			mdVO.getField("targetCaseId", Integer.class),
			mdVO.getField("description", String.class),
			mdVO.getField("automatic", Boolean.class),
			mdVO.getField("stateId", Integer.class),
			mdVO.getField("generationId", Integer.class),
			mdVO.getField("caseId", Integer.class),
			mdVO.getCreatedAt(), mdVO.getCreatedBy(), mdVO.getChangedAt(), mdVO.getChangedBy(), mdVO.getVersion());
		return result;
	}

	public static MasterDataVO wrapProcessTransitionVO(ProcessTransitionVO ptVO) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("sourceCaseId", ptVO.getStateSource());
		mpFields.put("targetCaseId", ptVO.getStateTarget());
		mpFields.put("description", ptVO.getDescription());
		mpFields.put("automatic", ptVO.isAutomatic());
		mpFields.put("stateId", ptVO.getState());
		mpFields.put("generationId", ptVO.getGenerationId());
		mpFields.put("caseId", ptVO.getProcessMonitorId());

		return new MasterDataVO(ptVO.getId(), ptVO.getChangedAt(), ptVO.getCreatedBy(), ptVO.getChangedAt(), ptVO.getChangedBy(), ptVO.getVersion(), mpFields);
	}

	public static SubProcessVO getSubProcessVO(MasterDataVO mdVO, StateModelVO stateModelVO) {
		SubProcessVO result = new SubProcessVO(
			mdVO.getNuclosValueObject(),
			stateModelVO,
			mdVO.getField("stateModelUsageId", Integer.class),
			"", "",
			mdVO.getField("guarantor", String.class),
			mdVO.getField("secondGuarantor", String.class),
			mdVO.getField("supervisor", String.class),
			mdVO.getField("originalSystem", String.class),
			mdVO.getField("planStartSeries", String.class),
			mdVO.getField("planEndSeries", String.class),
			mdVO.getField("runtime", Integer.class),
			mdVO.getField("runtimeFormat", Integer.class),
			mdVO.getField("caseId", Integer.class));

		return result;
	}

	public static MasterDataVO wrapSubProcessVO(SubProcessVO spVO) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("stateModelId", spVO.getStateModelVO().getId());
		mpFields.put("stateModelUsageId", spVO.getStateModelUsageId());
		mpFields.put("guarantor", spVO.getGuarantor());
		mpFields.put("secondGuarantor", spVO.getSecondGuarator());
		mpFields.put("supervisor", spVO.getSupervisor());
		mpFields.put("originalSystem", spVO.getOriginalSystem());
		mpFields.put("planStartSeries", spVO.getPlanStartSeries());
		mpFields.put("planEndSeries", spVO.getPlanEndSeries());
		mpFields.put("runtime", spVO.getRuntime());
		mpFields.put("runtimeFormat", spVO.getRuntimeFormat());
		mpFields.put("caseId", spVO.getProcessMonitorId());

		return new MasterDataVO(spVO.getId(), spVO.getChangedAt(), spVO.getCreatedBy(), spVO.getChangedAt(), spVO.getChangedBy(), spVO.getVersion(), mpFields);
	}

	public static ProcessMonitorVO getProcessMonitorVO(MasterDataVO mdVO) {
		StateModelLayout layout = null;

		try {
			Object o = IOUtils.fromByteArray((byte[])mdVO.getField("layout"));

			if (o == null) {
				throw new NuclosFatalException("StateModelLayout == null");
			}
			else if (o instanceof MarshalledValue) {
				// for backwards compatibility:
				layout = (StateModelLayout) ((MarshalledValue) o).get();
			}
			else if (o instanceof StateModelLayout) {
				layout = (StateModelLayout) o;
			}
			else {
				throw new NuclosFatalException("Unexpected class: " + o.getClass().getName());
			}
		}
		catch(IOException e) {
			throw new NuclosFatalException("can't read StateModelLayout");
		}
		catch(ClassNotFoundException e) {
			throw new NuclosFatalException("can't read StateModelLayout");
		}

		ProcessMonitorVO pmVO = new ProcessMonitorVO(
			getBaseVO(mdVO),
			(String)mdVO.getField("name"),
			(String)mdVO.getField("description"),
			layout);

		return pmVO;
	}

	public static MasterDataVO wrapProcessMonitorVO(ProcessMonitorVO pmVO) {
		byte[] layoutData = null;

		if (pmVO.getLayout() == null) {
			throw new NullArgumentException("layoutinfo");
		}
		try {
			layoutData = IOUtils.toByteArray(pmVO.getLayout());
			assert layoutData != null;
			assert layoutData.length > 0;
		}
		catch (IOException ex) {
			throw new NuclosFatalException(ex);
		}

		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", pmVO.getName());
		mpFields.put("description", pmVO.getDescription());
		mpFields.put("layout", layoutData);

		return new MasterDataVO(pmVO.getId(), pmVO.getChangedAt(), pmVO.getCreatedBy(), pmVO.getChangedAt(), pmVO.getChangedBy(), pmVO.getVersion(), mpFields);
	}

	public static LogbookVO getLogbookVO(MasterDataVO mdVO) {
		return new LogbookVO(
			new NuclosValueObject(
				(Integer) mdVO.getId(),
				mdVO.getCreatedAt(),
				mdVO.getCreatedBy(),
				mdVO.getChangedAt(),
				mdVO.getChangedBy(),
				mdVO.getVersion()),
			(Integer) mdVO.getField("genericObjectId"),
			(Integer) mdVO.getField("attributeId"),
			(Integer) mdVO.getField("masterdataId"),
			(Integer) mdVO.getField("entityfieldId"),
			(Integer) mdVO.getField("externalid"),
			(String) mdVO.getField("action"),
			(Integer) mdVO.getField("oldattributevalueId"),
			(Integer) mdVO.getField("oldexternalvalue"),
			(String) mdVO.getField("oldvalue"),
			(Integer) mdVO.getField("newattributevalueId"),
			(Integer) mdVO.getField("newexternalvalue"),
			(String) mdVO.getField("newvalue"));
	}

	public static MasterDataVO wrapLogbookVO(LogbookVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("genericObjectId", vo.getGenericObject());
		mpFields.put("attributeId", vo.getAttribute());
		mpFields.put("masterdataId", vo.getMasterDataMetaId());
		mpFields.put("entityfieldId", vo.getMasterDataMetaFieldId());
		mpFields.put("externalid", vo.getMasterDataRecordId());
		mpFields.put("action", vo.getMasterDataAction());
		mpFields.put("oldattributevalueId", vo.getOldValueId());
		mpFields.put("oldexternalvalue", vo.getOldValueExternalId());
		mpFields.put("oldvalue", vo.getOldValue());
		mpFields.put("newattributevalueId", vo.getNewValueId());
		mpFields.put("newexternalvalue", vo.getNewValueExternalId());
		mpFields.put("newvalue", vo.getNewValue());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}
}
