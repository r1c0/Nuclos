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

package org.nuclos.server.webservice.ejb3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Factories;
import org.nuclos.common.collection.LazyInitMapWrapper;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.util.DalTransformations;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.common.MasterDataPermission;
import org.nuclos.server.common.MasterDataPermissions;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ModulePermission;
import org.nuclos.server.common.ModulePermissions;
import org.nuclos.server.common.ejb3.SecurityFacadeLocal;
import org.nuclos.server.dal.processor.nuclet.JdbcEntityObjectProcessor;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeLocal;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.ejb3.RuleEngineFacadeLocal;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.nuclos.server.ruleengine.valueobject.RuleObjectContainerCVO.Event;

//Note2self: @EndpointConfig(configName="Standard WSSecurity Endpoint")
//config in jboss-4.2.2.GA/server/nuclos/deploy/jbossws.sar/META-INF
//Must include JBOSS_HOME/server/default/deploy/jbossws.sar/jbossws-core.jar in classpath for build

// @Stateless
// @Local(WebAccessWS.class)
public class WebAccessBean implements WebAccessWS {
	
	private SecurityFacadeLocal securityFacade;
	private RuleEngineFacadeLocal ruleEngineFacade;
	private GenericObjectFacadeLocal genericObjectFacade;

	@RolesAllowed("Login")
	@Override
	public ArrayList<String> listEntities() {
		MasterDataPermissions masterDataPermissions = securityFacade.getMasterDataPermissions();
		ModulePermissions modulePermissions = securityFacade.getModulePermissions();

		ArrayList<String> res = new ArrayList<String>();
		for(EntityMetaDataVO e : MetaDataServerProvider.getInstance().getAllEntities())
			if(canReadEntity(e.getEntity(), masterDataPermissions, modulePermissions))
				res.add(e.getEntity());
		
		Collections.sort(res);
		return res;
	}
	
	private boolean canReadEntity(String entity, MasterDataPermissions masterDataPermissions, ModulePermissions modulePermissions) {
		MasterDataPermission m = masterDataPermissions.get(entity);
		if(m != null && MasterDataPermission.includesReading(m))
			return true;
		
		ModulePermission g = modulePermissions.getPermissionsByEntityName().get(new Pair<String, Integer>(entity, null));
		if(g != null && ModulePermission.includesReading(g))
			return true;
		
		return false;
	}
	
	
	
	@Override
    public ArrayList<Long> list(String entityName) {
		JdbcEntityObjectProcessor proc = NucletDalProvider.getInstance().getEntityObjectProcessor(entityName);
		List<EntityObjectVO> dbRes = proc.getBySearchExpression(new CollectableSearchExpression(null), true);
		ArrayList<Long> res
		= new ArrayList<Long>(
			CollectionUtils.transform(dbRes, DalTransformations.getId()));
		return res;
    }

	@Override
    public ArrayList<String> read(String entityName, Long id) {
		JdbcEntityObjectProcessor proc = NucletDalProvider.getInstance().getEntityObjectProcessor(entityName);
		List<EntityObjectVO> dbRes =
			proc.getBySearchExpression(new CollectableSearchExpression(new CollectableIdCondition(id)), true);
		if(dbRes.isEmpty())
			return null;
		
		return entityObjectToString(dbRes.get(0));
    }
		
	private ArrayList<String> entityObjectToString(EntityObjectVO obj) {
		Map<String, Permission> attributePermissions;

		Long stateId = obj.getFieldId("nuclosState");
		if(stateId != null)
			attributePermissions = new LazyInitMapWrapper<String, Permission>(
				securityFacade.getAttributePermissionsByEntity(obj.getEntity(), stateId.intValue()),
				Factories.constFactory(Permission.NONE));
		else
			attributePermissions = new LazyInitMapWrapper<String, Permission>(
				new HashMap<String, Permission>(),
				Factories.constFactory(Permission.READONLY));
		
		ArrayList<String> res = new ArrayList<String>();

		for(Map.Entry<String, Object> e : obj.getFields().entrySet())
			if(attributePermissions.get(e.getKey()).includesReading())
				res.add(e.getKey() + "=" + (e.getValue() != null ? e.getValue().toString() : ""));
		
		return res;
	}
	
	
	@Override
    public void executeBusinessRule(String entityName, Long id, String rulename) {
		try {
	        RuleVO rule = ruleEngineFacade.get(rulename);
	        
	        RuleObjectContainerCVO ruleContainer;
	        EntityMetaDataVO meta = MetaDataServerProvider.getInstance().getEntity(entityName);
	        if(meta.isStateModel())
	        	ruleContainer = genericObjectFacade.getRuleObjectContainerCVO(Event.INTERFACE, id.intValue());
	        else
	        	ruleContainer = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class).getRuleObjectContainerCVO(Event.INTERFACE, entityName, id.intValue());
	        
	        ruleEngineFacade.executeBusinessRules(
	        	CollectionUtils.asList(rule),
	        	ruleContainer,
	        	false);
        }
        catch(NuclosBusinessRuleException e) {
        	throw new CommonFatalException(e);
        }
        catch(CommonFinderException e) {
        	throw new CommonFatalException(e);
        }
        catch(CommonPermissionException e) {
        	throw new CommonFatalException(e);
        }
        catch(NuclosBusinessException e) {
        	throw new CommonFatalException(e);
        }
	}
}
