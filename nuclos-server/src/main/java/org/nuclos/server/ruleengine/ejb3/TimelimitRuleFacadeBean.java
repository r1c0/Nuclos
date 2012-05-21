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
package org.nuclos.server.ruleengine.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.RuleCache;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcode.CustomCodeManager;
import org.nuclos.server.customcode.NuclosTimelimitRule;
import org.nuclos.server.customcode.codegenerator.NuclosJavaCompiler;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator;
import org.nuclos.server.customcode.codegenerator.RuleCodeGenerator.AbstractRuleTemplateType;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.ruleengine.NuclosCompileException;
import org.nuclos.server.ruleengine.RuleInterface;
import org.nuclos.server.ruleengine.valueobject.RuleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

/**
 * Session Bean for TimelimitRule.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
@Transactional(noRollbackFor= {Exception.class})
@RolesAllowed("Login")
public class TimelimitRuleFacadeBean extends NuclosFacadeBean implements TimelimitRuleFacadeRemote {

	private static final Logger LOG = Logger.getLogger(TimelimitRuleFacadeBean.class);
	
	//

	private CustomCodeManager ccm;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public TimelimitRuleFacadeBean() {
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	private final MasterDataFacadeLocal getMasterDataFacade() {
		return masterDataFacade;
	}

	public void setCustomCodeManager(CustomCodeManager ccm) {
		this.ccm = ccm;
	}

	/**
	 * @param result
	 * @return the list, sorted by name
	 */
	private static List<RuleVO> sortedByName(List<RuleVO> result) {
		Collections.sort(result, new Comparator<RuleVO>() {
			@Override
			public int compare(RuleVO ii1, RuleVO ii2) {
				return ii1.getRule().compareTo(ii2.getRule());
			}
		});
		return result;
	}

	/**
	 * get all records
	 * @return Collection<RuleVO> all TimelimitRule definitions
	 * @throws CommonFinderException
	 */
	public Collection<RuleVO> getAllTimelimitRules() {
		List<RuleVO> list = new ArrayList<RuleVO>();
		list = CollectionUtils.transform(getMasterDataFacade().getMasterData(NuclosEntity.TIMELIMITRULE.getEntityName(), null, false), new MakeTimelimitRule());

		return sortedByName(list);
	}

	/**
	 * get all active records
	 * @return Collection<RuleVO> all active TimelimitRule definitions
	 * @throws CommonFinderException
	 */
	public Collection<RuleVO> getActiveTimelimitRules() throws CommonFinderException {
		final List<RuleVO> list = new ArrayList<RuleVO>(getAllTimelimitRules());

		for(Iterator<RuleVO> iter = list.iterator(); iter.hasNext();) {
			if(!iter.next().isActive()) {
				iter.remove();
			}
		}

		return sortedByName(list);
	}

	/**
	 * create a new TimelimitRule definition in the database
	 * @param mdcvo containing the TimelimitRule
	 * @return same layout as value object
	 * @throws NuclosCompileException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 * @throws CommonPermissionException
	 */
	public MasterDataVO create(MasterDataVO mdcvo) 
			throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonValidationException, 
			CommonStaleVersionException, NuclosCompileException, CommonPermissionException, NuclosBusinessRuleException {
		try {
			final MasterDataVO result = getMasterDataFacade().create(NuclosEntity.TIMELIMITRULE.getEntityName(), mdcvo, null);

			RuleVO rule = makeTimelimitRuleVO(mdcvo);
			if (rule.isActive()) {
				check(rule);
			}
			RuleCache.getInstance().invalidate();
			return result;
		}
		catch (NuclosCompileException ex) {
			//getSessionContext().setRollbackOnly();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw ex;
		}
	}

	/**
	 * modify an existing TimelimitRule definition in the database
	 * @param mdcvo
	 * @return new TimelimitRule as value object
	 * @throws NuclosCompileException
	 * @throws CommonCreateException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonStaleVersionException
	 * @throws CommonValidationException
	 * @throws CommonPermissionException
	 */
	public MasterDataVO modify(MasterDataVO mdcvo) throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException,
			CommonValidationException, NuclosCompileException, CommonPermissionException, NuclosBusinessRuleException {
		
		try {
			RuleVO rule = makeTimelimitRuleVO(mdcvo);
			if (rule.isActive()) {
				check(rule);
			}

			final Object oId = getMasterDataFacade().modify(NuclosEntity.TIMELIMITRULE.getEntityName(), mdcvo, null);

			// get the updated mdcvo
			final MasterDataVO result = getMasterDataFacade().get(NuclosEntity.TIMELIMITRULE.getEntityName(), oId);
			RuleCache.getInstance().invalidate();
			return result;
		}
		catch (NuclosCompileException ex) {
			//getSessionContext().setRollbackOnly();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			// @todo we should also delete class files here
			throw ex;
		}
	}

	/**
	 * delete TimelimitRule definition from database
	 * @param mdcvo containing the TimelimitRule
	 * @throws CommonCreateException
	 * @throws CommonPermissionException
	 */
	public void remove(MasterDataVO mdcvo) throws CommonFinderException, CommonRemoveException, CommonStaleVersionException,
		CommonCreateException, CommonPermissionException, NuclosBusinessRuleException, NuclosCompileException {
		
		try {
			getMasterDataFacade().remove(NuclosEntity.TIMELIMITRULE.getEntityName(), mdcvo, false);

			RuleVO rulevo = makeTimelimitRuleVO(mdcvo);
			if (rulevo.isActive()) {
				NuclosJavaCompiler.check(new RuleCodeGenerator<NuclosTimelimitRule>(new TimelimitRuleCodeTemplate(), rulevo), true);
			}
			RuleCache.getInstance().invalidate();
		}
		catch (CommonFinderException ex) {
			//getSessionContext().setRollbackOnly();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw ex;
		}
	}

	/**
	 * Check if compilation would be successful.
	 * @param mdcvo
	 * @throws NuclosCompileException
	 */
	public void check(MasterDataVO rule) throws NuclosCompileException {
		check(makeTimelimitRuleVO(rule));
	}

	private void check(RuleVO rule) throws NuclosCompileException {
		NuclosJavaCompiler.check(new RuleCodeGenerator<NuclosTimelimitRule>(new TimelimitRuleCodeTemplate(), rule), false);
	}

	/**
	 * @return String containing functions the user has to implement
	 */
	public String getClassTemplate() {
		final StringBuffer sb = new StringBuffer();
		sb.append("/** @name        \n");
		sb.append("  * @beschreibung \n");
		sb.append("  * @verwendung   \n");
		sb.append("  * @aenderung    \n");
		sb.append("*/\n\n");
		sb.append("public Collection<Integer> getIntIds(RuleInterface server) {\n\n}\n\n");
		sb.append("public void process(RuleInterface server, Integer iId) throws NuclosBusinessRuleException {\n\n}");
		return sb.toString();
	}

	/**
	 * imports the given timelimit rules, adding new and overwriting existing. The other existing timelimit rules remain untouched.
	 * @param collRuleVO
	 * @throws CreateException
	 */
	public void importTimelimitRules(Collection<RuleVO> collRuleVO) throws CommonBusinessException {
		for (RuleVO ruleVO : collRuleVO) {
			try {
				this.importTimelimitRule(ruleVO);
			}
			catch (CommonBusinessException ex) {
				throw new CommonBusinessException(StringUtils.getParameterizedExceptionMessage(
						"rule.import.error", ruleVO.getRule()), ex);
			}
		}
	}

	/**
	 * imports the given timelimit rule. If one with this name exists already, it will be overwritten, otherwise
	 * the new one will be added.
	 * @param voRule
	 * @throws CommonCreateException
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 */
	private void importTimelimitRule(RuleVO voRule) throws CommonCreateException, CommonFinderException, CommonRemoveException,
			CommonStaleVersionException, CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {
		voRule.validate();

		final String sName = voRule.getRule();

		MasterDataVO mdvo = findByName(sName);
		if (mdvo == null) {
			try {
				getMasterDataFacade().create(NuclosEntity.TIMELIMITRULE.getEntityName(), makeMasterDataVO(voRule), null);
			}
			catch (CommonCreateException e) {
				//getSessionContext().setRollbackOnly();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				throw new CommonCreateException(e);
			}
		}
		else {
			mdvo.setField("source", voRule.getSource());
			getMasterDataFacade().modify(NuclosEntity.TIMELIMITRULE.getEntityName(), mdvo, null);
		}
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
    public Collection<String> getJobRules(Object oId) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<String> query = builder.createQuery(String.class);
		DbFrom t = query.from("V_MD_JOBRULE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("STRVALUE_T_MD_TIMELIMITRULE", String.class));
		query.where(builder.equal(t.baseColumn("INTID_T_MD_JOBCONTROLLER", Integer.class), oId));
		query.orderBy(builder.asc(t.baseColumn("INTORDER", Integer.class)));
		try {
			return dataBaseHelper.getDbAccess().executeQuery(query);
		} catch (DbException ex) {
			LOG.error(ex);
			return Collections.emptySet();
		}
    }

	/**
	 * @param sRuleName
	 * @throws CreateException
	 * @author corina.mandoki
	 */
	public void executeRule(String sRuleName, Integer iSessionId) {
		executeTimelimitRule(sRuleName, iSessionId);
	}

	/**
	 * executes all active timlimit rules.
	 * First we get the list of all relevant generic objects, then we execute the rule for each one in an own transaction.
	 * @return InvoiceInspectorResultRun
	 */
	public void executeTimelimitRule(String sRuleName) {
		executeTimelimitRule(sRuleName, null);
	}

	private void executeTimelimitRule(String ruleName, Integer sessionId) {
		TimelimitRuleFacadeLocal timelimitRuleFacade = ServerServiceLocator.getInstance().getFacade(TimelimitRuleFacadeLocal.class);

		Pair<NuclosTimelimitRule, RuleInterface> instanceAndInterface = timelimitRuleFacade.prepareTimelimitRule(ruleName, sessionId);

		if (instanceAndInterface != null) {
			LOG.info("Start executing timelimit rule \"" + ruleName + "\"");
			Collection<Integer> collIntIds = timelimitRuleFacade.executeTimelimitRule(instanceAndInterface.getX(), instanceAndInterface.getY());

			for(Integer iId : collIntIds) {
				timelimitRuleFacade.executeTimelimitRule(instanceAndInterface.getX(), instanceAndInterface.getY(), iId);
			}
			LOG.info("Finished executing timelimit rule \"" + ruleName + "\"");
		}
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor= {Exception.class})
    public Pair<NuclosTimelimitRule, RuleInterface> prepareTimelimitRule(String ruleName, Integer sessionId) {
		MasterDataVO mdvo = findByName(ruleName);
		RuleVO rulevo = mdvo != null ? makeTimelimitRuleVO(mdvo) : null;

		if (rulevo == null) {
			LOG.error("Eine Fristenregel mit dem Namen \"" + ruleName + "\" existiert nicht.");
			return null;
		}

		final NuclosTimelimitRule ruleInstance;
		try {
			RuleCodeGenerator<NuclosTimelimitRule> generator = getGenerator(rulevo);
			ruleInstance = ccm.getInstance(generator);
		} 
		catch (NuclosCompileException e) {
			LOG.error(e.toString(), e);
			throw new NuclosFatalException(e);
		}
		catch (Exception e) {
			LOG.error(e.toString(), e);
			throw new NuclosFatalException(e);
		}
		final RuleInterface ri = new RuleInterface(rulevo, null, null, null, null);

		ri.setSessionId(sessionId);

		return new Pair<NuclosTimelimitRule, RuleInterface>(ruleInstance, ri);
    }

	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor= {Exception.class})
    public Collection<Integer> executeTimelimitRule(NuclosTimelimitRule ruleInstance, RuleInterface ri) {
	    return ruleInstance.getIntIds(ri);
    }

	/**
	 * executes the given timelimit rule with the genericobject with given id.
	 * @param ruleInstance the rule class
	 * @param ri the rule interface as parameter for executed method
	 * @param iGenericObjectId the leased object id as parameter for executed method
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW, rollbackFor= {Exception.class})
	public void executeTimelimitRule(final NuclosTimelimitRule ruleInstance, final RuleInterface ri, final Integer iGenericObjectId) {
		try {
			ruleInstance.process(ri, iGenericObjectId);
		}
		catch(NuclosBusinessRuleException e) {
			final String sErrorMessage = "Es sind Fehler aufgetreten bei der Abarbeitung der Fristenregel " + ri.getCurrentRule().getRule() +
			" aufgetreten beim Modulobjekt mit der ID " + iGenericObjectId + ":/n" + e;
			Logger.getLogger("TimelimitErrors").error(sErrorMessage);
			LOG.warn(sErrorMessage);

			// Now the rollback is declared in annotation:
			// obsolete: Let the transaction for this generic object id be rolled back and continue eventually with the next...
			// obsolete: getSessionContext().setRollbackOnly();
			// obsolete: TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
	}

	/**
	 * find a timelimit rule by its name
	 * @param sRuleName
	 * @return MasterDataVO or null if no rule with the given name exists
	 */
	private MasterDataVO findByName(String sRuleName) {
		final MasterDataMetaVO mdmetavo = getMasterDataFacade().getMetaData(NuclosEntity.TIMELIMITRULE.getEntityName());
		final CollectableSearchCondition cond = SearchConditionUtils.newMDComparison(mdmetavo, "name", ComparisonOperator.EQUAL, sRuleName);
		final TruncatableCollection<MasterDataVO> collmdvo = getMasterDataFacade().getMasterData(NuclosEntity.TIMELIMITRULE.getEntityName(), cond, false);

		assert collmdvo.isEmpty() || collmdvo.size() == 1;

		if (collmdvo.isEmpty()) {
			return null;
		}
		else {
			return collmdvo.iterator().next();
		}
	}

	/**
	 * transforms a RuleVO to a MasterDataVO
	 * used in the case of importing rules
	 * @param rulevo
	 * @return MasterDataVO
	 */
	private MasterDataVO makeMasterDataVO(RuleVO rulevo) {
		Map<String, Object> mpFields = new HashMap<String, Object>();
		mpFields.put("name", rulevo.getRule());
		mpFields.put("description", rulevo.getDescription());
		mpFields.put("source", rulevo.getSource());
		mpFields.put("active", rulevo.isActive());

		MasterDataVO mdvo = new MasterDataVO(null, new Date(), getCurrentUserName(), new Date(),
				getCurrentUserName(), 1, mpFields);

		return mdvo;
	}

	/**
	 * transforms a MasterDataVO to a RuleVO
	 * @param mdvo
	 * @return RuleVO
	 */
	private RuleVO makeTimelimitRuleVO(MasterDataVO mdvo) {
		NuclosValueObject nvo = new NuclosValueObject(mdvo.getIntId(), mdvo.getCreatedAt(), mdvo.getCreatedBy(),
				mdvo.getChangedAt(), mdvo.getChangedBy(), mdvo.getVersion());
		String sName = (String)mdvo.getField("name");
		String sDescription = (String)mdvo.getField("description");
		String sRuleSource = (String)mdvo.getField("source");
		Integer nucletId = mdvo.getField("nucletId", Integer.class);
		Boolean bActive = mdvo.getField("active") == null ? false : (Boolean)mdvo.getField("active");
		Boolean bDebug = mdvo.getField("debug") == null ? false : (Boolean)mdvo.getField("debug");
		return new RuleVO(nvo, sName, sDescription, sRuleSource, nucletId, bActive, bDebug);
	}

	/**
	 * inner class: Transformer to transform a MasterDataVO to a RuleVO
	 */
	private class MakeTimelimitRule implements Transformer<MasterDataVO, RuleVO> {
		@Override
		public RuleVO transform(MasterDataVO mdvo) {
			return makeTimelimitRuleVO(mdvo);
		}
	}

	private RuleCodeGenerator<NuclosTimelimitRule> getGenerator(RuleVO ruleVO) {
		return new RuleCodeGenerator<NuclosTimelimitRule>(new TimelimitRuleCodeTemplate(), ruleVO);
	}

	private static final String[] IMPORTS = new String[]{
		"org.nuclos.common2.*",
		"org.nuclos.common.*",
		"org.nuclos.common.mail.*",
		"org.nuclos.common.fileimport.*",
		"org.nuclos.common.collect.collectable.searchcondition.*",
		"org.nuclos.common.collect.collectable.CollectableEntity",
		"org.nuclos.common.collect.collectable.CollectableEntityField",
		"org.nuclos.common.collect.collectable.Collectable",
		"org.nuclos.common.collect.collectable.CollectableField",
		"org.nuclos.common.collect.collectable.CollectableValueField",
		"org.nuclos.common.collect.collectable.CollectableValueIdField",
		"org.nuclos.common.RuleNotification",
		"org.nuclos.common.attribute.*",
		"org.nuclos.server.common.calendar.CommonDate",
		"org.nuclos.server.masterdata.valueobject.*",
		"org.nuclos.server.genericobject.valueobject.*",
		"org.nuclos.server.customcode.NuclosTimelimitRule",
		"org.nuclos.server.ruleengine.*",
		"java.util.*",
		"org.apache.log4j.*"
	};

	public static class TimelimitRuleCodeTemplate extends AbstractRuleTemplateType<NuclosTimelimitRule> {

		public TimelimitRuleCodeTemplate() {
			super("TimelimitRule", NuclosTimelimitRule.class);
		}

		@Override
		protected List<String> getImports() {
			List<String> imports = new ArrayList<String>();
			CollectionUtils.addAll(imports, IMPORTS);
			final String additionalImports = ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_ADDITIONAL_IMPORTS_FOR_RULES);
			if (additionalImports != null) {
				CollectionUtils.addAll(imports, additionalImports.split(","));
			}
			imports.addAll(getWebserviceImports());
			return imports;
		}

		@Override
		protected String getHeaderImpl(RuleVO ruleVO) {
			final String ruleName = getClassName(ruleVO);
			final StringBuffer sb = new StringBuffer();
			sb.append("\npublic class ");
			sb.append(ruleName);
			sb.append(" implements NuclosTimelimitRule {\n\t");
			sb.append("public ");
			sb.append(ruleName);
			sb.append("() {\n\t}\n\n");
			return sb.toString();
		}

		@Override
		public String getFooter() {
			return "\n}";
		}

		@Override
		public String getLabel() {
			return "Timelimit rule \"{0}\"";
		}

		@Override
		public String getEntityname() {
			return NuclosEntity.TIMELIMITRULE.getEntityName();
		}
	}
}
