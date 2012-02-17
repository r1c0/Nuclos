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
package org.nuclos.server.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.server.customcode.valueobject.CodeVO;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.MasterDataWrapper;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.mbean.MBeanAgent;
import org.nuclos.server.mbean.RuleCacheMBean;
import org.nuclos.server.ruleengine.valueobject.RuleVO;

/**
 * A cache for rules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:lars.rueckemann@novabit.de">Lars R\u00fcckemann</a>
 * @version 00.01.000
 */
public class RuleCache implements RuleCacheMBean {
	
	private static final Logger LOG = Logger.getLogger(RuleCache.class);
	
	private static RuleCache INSTANCE;

	//

	private final Map<Integer, RuleVO> mpRulessById
		= new ConcurrentHashMap<Integer, RuleVO>();
	
	private final Map<String, RuleVO> mpRulesByName
		= new ConcurrentHashMap<String, RuleVO>();
	
	private final Map<Pair<String, String>, List<RuleVO>> mpRulesByEventAndEntity
		= new ConcurrentHashMap<Pair<String,String>, List<RuleVO>>();
	
	private final Map<String, List<RuleVO>> mpRulesByEvent
		= new ConcurrentHashMap<String, List<RuleVO>>();

	private final Map<String, RuleVO> timelimitrules
		= new ConcurrentHashMap<String, RuleVO>();

	private final Map<String, CodeVO> commoncode
		= new ConcurrentHashMap<String, CodeVO>();
	
	private final Map<String, MasterDataVO> webservices
		= new ConcurrentHashMap<String, MasterDataVO>();
	
	private DataBaseHelper dataBaseHelper;
	
	private MasterDataFacadeLocal mdFacade;

	RuleCache() {
		INSTANCE = this;
		// register this cache as MBean
		MBeanAgent.registerCache(INSTANCE, RuleCacheMBean.class);
	}

	public static RuleCache getInstance() {
		return INSTANCE;
	}
	
	public void setDataBaseHelper(DataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	public void setMasterDataFacadeLocal(MasterDataFacadeLocal masterDataFacadeLocal) {
		this.mdFacade = masterDataFacadeLocal;
	}

	/**
	 * initialize RuleById and RuleByName
	 *
	 */
	@PostConstruct
	private void init() {
		if (mpRulessById.isEmpty()) {
			LOG.debug("Initializing RuleCache for RuleById and RulesByName");

			Collection<MasterDataVO> ruleVOs = mdFacade.getMasterData(NuclosEntity.RULE.getEntityName(), null, true);

			for (MasterDataVO ruleVO : ruleVOs) {
				final RuleVO rulevo = MasterDataWrapper.getRuleVO(ruleVO);
				mpRulessById.put(rulevo.getId(), rulevo);
				mpRulesByName.put(rulevo.getRule(), rulevo);
			}

			Collection<MasterDataVO> timelimitruleVOs = mdFacade.getMasterData(NuclosEntity.TIMELIMITRULE.getEntityName(), null, true);

			for (MasterDataVO timelimitruleVO : timelimitruleVOs) {
				final RuleVO rulevo = MasterDataWrapper.getRuleVO(timelimitruleVO);
				timelimitrules.put(rulevo.getRule(), rulevo);
			}

			Collection<MasterDataVO> webserviceVOs = mdFacade.getMasterData(NuclosEntity.WEBSERVICE.getEntityName(), null, true);

			for (MasterDataVO webserviceVO : webserviceVOs) {
				webservices.put(webserviceVO.getField("name", String.class), webserviceVO);
			}

			Collection<MasterDataVO> commoncodeVOs = mdFacade.getMasterData(NuclosEntity.CODE.getEntityName(), null, true);

			for (MasterDataVO commoncodeVO : commoncodeVOs) {
				CodeVO code = MasterDataWrapper.getCodeVO(commoncodeVO);
				if (code.isActive()) {
					commoncode.put(code.getName(), code);
				}
			}

			LOG.debug("FINISHED initializing rule cache.");
		}
	}

	/**
	 * get a single rule by id
	 * @param iRuleId
	 * @return the RuleVO of the rule with the specified id
	 */
	public RuleVO getRule(Integer iRuleId) {
		init();
		return mpRulessById.get(iRuleId);
	}

	/**
	 * get a single rule by name
	 * @param sRuleName
	 * @return the RuleVO of the rule with the specified name
	 */
	public RuleVO getRule(String sRuleName) {
		if (sRuleName == null) {
			throw new NullArgumentException("sRuleName");
		}
		init();
		final RuleVO result = mpRulesByName.get(sRuleName);
		if (result == null) {
			throw new NuclosFatalException(StringUtils.getParameterizedExceptionMessage("rulecache.exception", sRuleName));//"Regel nicht gefunden: " + sRuleName);
		}
		assert result != null;
		return result;
	}

	/**
	 * get a list of rules by Eventname and EntityId (ordered by the execution order).
	 * @param sEventName
	 * @param iModuleId
	 * @return List<RuleVO> of all rules for the specified entity and event type, ordered by the execution order.
	 */
	public List<RuleVO> getByEventAndEntityOrdered(String sEventName, String sEntity) {
		return getByEventAndEntityOrdered(new Pair<String, String>(sEventName, sEntity));
	}

	/**
	 * get a list of rules by Pair of Eventname and EntityId  (ordered by the execution order).
	 * @param pair
	 * @return List<RuleVO> of all rules for the specified entity and event type, ordered by the execution order.
	 */
	private List<RuleVO> getByEventAndEntityOrdered(Pair<String, String> pair) {
		if (!mpRulesByEventAndEntity.containsKey(pair)) {
			LOG.debug("Initializing RuleCache for " + pair);
			try {
				DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("V_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID_T_MD_RULE", Integer.class));
				query.where(builder.and(
					builder.equal(t.baseColumn("STREVENT", String.class), pair.getX()),
					builder.equal(t.baseColumn("STRMASTERDATA", String.class), pair.getY())));
				query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
				List<Integer> ruleIds = dataBaseHelper.getDbAccess().executeQuery(query);

				List<RuleVO> rules = new ArrayList<RuleVO>();

				for (Integer id : ruleIds)
					rules.add(MasterDataWrapper.getRuleVO(mdFacade.get(NuclosEntity.RULE.getEntityName(), id)));

				mpRulesByEventAndEntity.put(pair, rules);

				LOG.debug("FINISHED initializing RuleCache for " + pair);
			}
			catch (CommonFinderException ex) {
				throw new NuclosFatalException(ex);
			}
			catch (CommonPermissionException ex) {
				throw new NuclosFatalException(ex);
			}
		}
		return mpRulesByEventAndEntity.get(pair);
	}

	/**
	 * get a List of rules by Pair of Eventname and ModuleId  (ordered by the execution order).
	 * Used for timelimit rules.
	 * @param sEventName
	 * @return List<RuleVO> of all rules for the specified event type, ordered by the execution order.
	 */
	public List<RuleVO> getByEventOrdered(String sEventName) {
		if (!mpRulesByEvent.containsKey(sEventName)) {
			LOG.debug("Initializing RuleCache for " + sEventName);
			try {
				DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("V_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID_T_MD_RULE", Integer.class));
				query.where(builder.equal(t.baseColumn("STREVENT", String.class), sEventName));
				query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
				List<Integer> ruleIds = dataBaseHelper.getDbAccess().executeQuery(query);

				List<RuleVO> rules = new ArrayList<RuleVO>();

				for (Integer id : ruleIds)
					rules.add(MasterDataWrapper.getRuleVO(mdFacade.get(NuclosEntity.RULE.getEntityName(), id)));

				mpRulesByEvent.put(sEventName, rules);
				LOG.debug("FINISHED initializing RuleCache for " + sEventName);
			}
			catch (CommonFinderException e) {
				throw new NuclosFatalException(e);
			}
			catch (CommonPermissionException e) {
				throw new NuclosFatalException(e);
			}
		}
		return mpRulesByEvent.get(sEventName);
	}

	/**
	 * get a collection of all rules
	 * @return Collection<RuleVO> of all rules
	 */
	public Collection<RuleVO> getAllRules() {
		init();
		return new HashSet<RuleVO>(mpRulessById.values());
	}

	public Collection<RuleVO> getTimelimitRules() {
		init();
		return new HashSet<RuleVO>(timelimitrules.values());
	}

	public Collection<MasterDataVO> getWebservices() {
		init();
		return new HashSet<MasterDataVO>(webservices.values());
	}

	public Collection<CodeVO> getCommonCode() {
		init();
		return new HashSet<CodeVO>(commoncode.values());
	}

	/**
	 * Invalidate the cache
	 */
	@Override
	public void invalidate() {
		LOG.debug("Invalidating RuleCache");
		mpRulessById.clear();
		mpRulesByName.clear();
		mpRulesByEventAndEntity.clear();
		mpRulesByEvent.clear();
		commoncode.clear();
		webservices.clear();
	}

	@Override
	public int getRuleCount() {
		return getAllRules().size();
	}

	@Override
	public Collection<String> showRuleNames() {
		if (mpRulesByName.isEmpty()) {
			init();
		}
		return this.mpRulesByName.keySet();
		//return this.mpRulesByName.keySet().toArray(new String[0]);
	}

}	// class RuleCache
