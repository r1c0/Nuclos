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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.ServiceLocator;
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
	private static final Logger log = Logger.getLogger(RuleCache.class);

	private final MasterDataFacadeLocal mdFacade = ServiceLocator.getInstance().getFacade(MasterDataFacadeLocal.class);

	private static RuleCache singleton;

	private Map<Integer, RuleVO> mpRulessById;
	private Map<String, RuleVO> mpRulesByName;
	private Map<Pair<String, String>, List<RuleVO>> mpRulesByEventAndEntity;
	private Map<String, List<RuleVO>> mpRulesByEvent;

	private Map<String, RuleVO> timelimitrules;

	private Map<String, CodeVO> commoncode;
	private Map<String, MasterDataVO> webservices;

	private RuleCache() {
		init();
	}

	public static synchronized RuleCache getInstance() {
		if (singleton == null) {
			singleton = new RuleCache();
			// register this cache as MBean
			MBeanAgent.registerCache(singleton, RuleCacheMBean.class);
		}

		return singleton;
	}

	/**
	 * initialize RuleById and RuleByName
	 *
	 */
	private synchronized void init() {
		if (mpRulessById == null) {
			log.debug("Initializing RuleCache for RuleById and RulesByName");
			mpRulessById = Collections.synchronizedMap(new HashMap<Integer, RuleVO>());
			mpRulesByName = Collections.synchronizedMap(new HashMap<String, RuleVO>());
			timelimitrules = Collections.synchronizedMap(new HashMap<String, RuleVO>());
			commoncode = Collections.synchronizedMap(new HashMap<String, CodeVO>());
			webservices = Collections.synchronizedMap(new HashMap<String, MasterDataVO>());

			Collection<MasterDataVO> ruleVOs = mdFacade.getMasterData(NuclosEntity.RULE.getEntityName(), null, true);

			for (MasterDataVO ruleVO : ruleVOs) {
				final RuleVO rulevo = MasterDataWrapper.getRuleVO(ruleVO);
				mpRulessById.put(rulevo.getId(), rulevo);
				mpRulesByName.put(rulevo.getName(), rulevo);
			}

			Collection<MasterDataVO> timelimitruleVOs = mdFacade.getMasterData(NuclosEntity.TIMELIMITRULE.getEntityName(), null, true);

			for (MasterDataVO timelimitruleVO : timelimitruleVOs) {
				final RuleVO rulevo = MasterDataWrapper.getRuleVO(timelimitruleVO);
				timelimitrules.put(rulevo.getName(), rulevo);
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

			log.debug("FINISHED initializing rule cache.");
		}
	}

	/**
	 * get a single rule by id
	 * @param iRuleId
	 * @return the RuleVO of the rule with the specified id
	 */
	public synchronized RuleVO getRule(Integer iRuleId) {
		init();
		return mpRulessById.get(iRuleId);
	}

	/**
	 * get a single rule by name
	 * @param sRuleName
	 * @return the RuleVO of the rule with the specified name
	 */
	public synchronized RuleVO getRule(String sRuleName) {
		if (sRuleName == null) {
			throw new NullArgumentException("sRuleName");
		}
		this.init();
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
	public synchronized List<RuleVO> getByEventAndEntityOrdered(String sEventName, String sEntity) {
		return getByEventAndEntityOrdered(new Pair<String, String>(sEventName, sEntity));
	}

	/**
	 * get a list of rules by Pair of Eventname and EntityId  (ordered by the execution order).
	 * @param pair
	 * @return List<RuleVO> of all rules for the specified entity and event type, ordered by the execution order.
	 */
	private synchronized List<RuleVO> getByEventAndEntityOrdered(Pair<String, String> pair) {

		if (mpRulesByEventAndEntity == null) {
			mpRulesByEventAndEntity = CollectionUtils.newHashMap();
		}
		if (!mpRulesByEventAndEntity.containsKey(pair)) {
			log.debug("Initializing RuleCache for " + pair);
			try {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("V_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID_T_MD_RULE", Integer.class));
				query.where(builder.and(
					builder.equal(t.baseColumn("STREVENT", String.class), pair.getX()),
					builder.equal(t.baseColumn("STRMASTERDATA", String.class), pair.getY())));
				query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
				List<Integer> ruleIds = DataBaseHelper.getDbAccess().executeQuery(query);

				List<RuleVO> rules = new ArrayList<RuleVO>();

				for (Integer id : ruleIds)
					rules.add(MasterDataWrapper.getRuleVO(mdFacade.get(NuclosEntity.RULE.getEntityName(), id)));

				mpRulesByEventAndEntity.put(pair, rules);

				log.debug("FINISHED initializing RuleCache for " + pair);
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
	public synchronized List<RuleVO> getByEventOrdered(String sEventName) {
		if (mpRulesByEvent == null) {
			mpRulesByEvent = CollectionUtils.newHashMap();
		}
		if (!mpRulesByEvent.containsKey(sEventName)) {
			log.debug("Initializing RuleCache for " + sEventName);
			try {
				DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
				DbQuery<Integer> query = builder.createQuery(Integer.class);
				DbFrom t = query.from("V_MD_RULE_EVENT").alias(SystemFields.BASE_ALIAS);
				query.select(t.baseColumn("INTID_T_MD_RULE", Integer.class));
				query.where(builder.equal(t.baseColumn("STREVENT", String.class), sEventName));
				query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
				List<Integer> ruleIds = DataBaseHelper.getDbAccess().executeQuery(query);

				List<RuleVO> rules = new ArrayList<RuleVO>();

				for (Integer id : ruleIds)
					rules.add(MasterDataWrapper.getRuleVO(mdFacade.get(NuclosEntity.RULE.getEntityName(), id)));

				mpRulesByEvent.put(sEventName, rules);
				log.debug("FINISHED initializing RuleCache for " + sEventName);
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
	public synchronized Collection<RuleVO> getAllRules() {
		init();
		return new HashSet<RuleVO>(mpRulessById.values());
	}

	public synchronized Collection<RuleVO> getTimelimitRules() {
		init();
		return new HashSet<RuleVO>(timelimitrules.values());
	}

	public synchronized Collection<MasterDataVO> getWebservices() {
		init();
		return new HashSet<MasterDataVO>(webservices.values());
	}

	public synchronized Collection<CodeVO> getCommonCode() {
		init();
		return new HashSet<CodeVO>(commoncode.values());
	}

	/**
	 * Invalidate the cache
	 */
	@Override
	public synchronized void invalidate() {
		log.debug("Invalidating RuleCache");
		mpRulessById = null;
		mpRulesByName = null;
		mpRulesByEventAndEntity = null;
		mpRulesByEvent = null;
		commoncode = null;
		webservices = null;
	}

	@Override
	public int getRuleCount() {
		return this.getAllRules().size();
	}

	@Override
	public Collection<String> showRuleNames() {
		if(mpRulesByName == null) {
			init();
		}
		return this.mpRulesByName.keySet();
		//return this.mpRulesByName.keySet().toArray(new String[0]);
	}

}	// class RuleCache
