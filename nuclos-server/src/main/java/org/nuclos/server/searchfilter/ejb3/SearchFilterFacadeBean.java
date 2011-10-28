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
package org.nuclos.server.searchfilter.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.DalSupportForMD;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.LocaleUtils;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.nuclos.server.searchfilter.valueobject.SearchFilterUserVO;
import org.nuclos.server.searchfilter.valueobject.SearchFilterVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for searchfilter. <br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
*/
// @Stateless
// @Remote(SearchFilterFacadeRemote.class)
@Transactional
public class SearchFilterFacadeBean extends MasterDataFacadeBean implements SearchFilterFacadeRemote {

	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_SEARCHFILTERCACHE);

	private static final String SEARCHFILTER_TABLE = "T_UD_SEARCHFILTER";

	@Override
	public Object modify(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants, List<TranslationVO> resources)
			throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException,
			CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {

		Object id = super.modify(sEntityName, mdvo, mpDependants);
		setResources(mdvo, resources);

		Collection<MasterDataVO> colldep = this.getDependantMasterData(NuclosEntity.SEARCHFILTERUSER.getEntityName(), "searchfilter", id);
		String asUsers[] = new String[colldep.size()];
		int i = 0;
		for (MasterDataVO mdvodep : colldep) {
			asUsers[i] = (String)mdvodep.getField("user");
			i++;
		}
		notifyClients(asUsers);
		// Invalidate security cache (compulsory filters are security-related)
		SecurityCache.getInstance().invalidate();

		return id;
	}

	/**
	 * @return all searchfilters for the given user
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonFinderException
	 */
	@Override
    @RolesAllowed("Login")
	public Collection<SearchFilterVO> getAllSearchFilterByUser(String sUser) throws CommonFinderException, CommonPermissionException {
		Collection<SearchFilterVO> collSearchFilter = new ArrayList<SearchFilterVO>();

		// 1. get all searchfilteruser objects for given user
		final CollectableSearchCondition condSearchFilterUser = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTERUSER), "user", ComparisonOperator.EQUAL, sUser);

		for (MasterDataVO mdVO_searchFilteruser : getMasterDataFacade().getMasterData(NuclosEntity.SEARCHFILTERUSER.getEntityName(), condSearchFilterUser, true)) {
			// 2. get corresponding searchfilter
			MasterDataVO mdVO_searchfilter = getMasterDataFacade().get(NuclosEntity.SEARCHFILTER.getEntityName(), mdVO_searchFilteruser.getField("searchfilterId"));

			// 3. transform MasterdataVOs to SearchFilterVO
			collSearchFilter.add(SearchFilterVO.transformToSearchFilter(mdVO_searchfilter, mdVO_searchFilteruser));
		}

		for (Integer iRoleId : SecurityCache.getInstance().getUserRoles(sUser)) {
			// 4. get all searchfilterrole objects for given user
			final CollectableSearchCondition condSearchFilterRole = SearchConditionUtils.newMDReferenceComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.SEARCHFILTERROLE), "role", iRoleId);

			for (MasterDataVO mdVO_searchFilterrole : getMasterDataFacade().getMasterData(NuclosEntity.SEARCHFILTERROLE.getEntityName(), condSearchFilterRole, true)) {
				// 5. get corresponding searchfilter
				MasterDataVO mdVO_searchfilter = getMasterDataFacade().get(NuclosEntity.SEARCHFILTER.getEntityName(), mdVO_searchFilterrole.getField("searchfilterId"));

				// 6. set user id and editable cause of transformToSearchFilter in 7. | Now this entry looks like an SEARCHFILTERUSER entry
				mdVO_searchFilterrole.setField("userId", SecurityCache.getInstance().getUserId(sUser));
				mdVO_searchFilterrole.setField("editable", Boolean.FALSE);

				// 7. transform MasterdataVOs to SearchFilterVO and check if exists already in list
				SearchFilterVO sfRoleVO = SearchFilterVO.transformToSearchFilter(mdVO_searchfilter, mdVO_searchFilterrole);
				boolean sfUserVOFound = false;
				for (SearchFilterVO sfUserVO : collSearchFilter) {
					if (sfUserVO.getId().equals(sfRoleVO.getId()))
						sfUserVOFound = true;
				}
				if (!sfUserVOFound)
					collSearchFilter.add(sfRoleVO);
			}
		}

		return collSearchFilter;
	}

	/**
	 * creates the given search filter for the current user as owner
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
    @RolesAllowed("Login")
	public SearchFilterVO createSearchFilter(SearchFilterVO filterVO) throws NuclosBusinessRuleException, CommonCreateException, CommonPermissionException {
		MasterDataVO mdVO_searchfilter = SearchFilterVO.transformToMasterData(filterVO);

		filterVO.getSearchFilterUser().setEditable(true);
		filterVO.getSearchFilterUser().setForced(false);
		filterVO.getSearchFilterUser().setUser(getIdByUser(getCurrentUserName()));

		MasterDataVO mdVO_searchfilteruser = SearchFilterUserVO.transformToMasterData(filterVO.getSearchFilterUser());

		DependantMasterDataMap dmdp = new DependantMasterDataMap();
		dmdp.addData(NuclosEntity.SEARCHFILTERUSER.getEntityName(), DalSupportForMD.getEntityObjectVO(mdVO_searchfilteruser));

		MasterDataVO mdVO_searchfilter_new = getMasterDataFacade().create(NuclosEntity.SEARCHFILTER.getEntityName(), mdVO_searchfilter, dmdp);
		Collection<MasterDataVO> coll_searchfilteruser_new = getMasterDataFacade().getDependantMasterData(NuclosEntity.SEARCHFILTERUSER.getEntityName(), "searchfilter", mdVO_searchfilter_new.getId());

		assert coll_searchfilteruser_new.size() == 1;

		return SearchFilterVO.transformToSearchFilter(mdVO_searchfilter_new, coll_searchfilteruser_new.iterator().next());
	}

	/**
	 * modifies the given searchfilter
	 * ATTENTION: this will not modify the searchfilteruser, only the searchfilter will be modified
	 * @throws CreateException
	 * @throws CommonPermissionException
	 * @throws CommonValidationException
	 * @throws CommonStaleVersionException
	 * @throws CommonRemoveException
	 * @throws CommonFinderException
	 * @throws CommonCreateException
	 * @throws NuclosBusinessRuleException
	 */
	@Override
    @RolesAllowed("Login")
	public SearchFilterVO modifySearchFilter(SearchFilterVO filterVO) throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {
		Object oId = getMasterDataFacade().modify(NuclosEntity.SEARCHFILTER.getEntityName(), SearchFilterVO.transformToMasterData(filterVO), null);
		return SearchFilterVO.transformToSearchFilter(getMasterDataFacade().get(NuclosEntity.SEARCHFILTER.getEntityName(), oId), SearchFilterUserVO.transformToMasterData(filterVO.getSearchFilterUser()));
	}

	/**
	 * deletes the given searchfilter
	 */
	@Override
    @RolesAllowed("Login")
	public void removeSearchFilter(SearchFilterVO filterVO) throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {
		// if the user is not the owner of the searchfilter, remove only the searchfilteruser record
		if (!filterVO.getOwner().equals(getCurrentUserName())) {
			getMasterDataFacade().remove(NuclosEntity.SEARCHFILTERUSER.getEntityName(), SearchFilterUserVO.transformToMasterData(filterVO.getSearchFilterUser()), false);
		}
		// if the user is the owner of the searchfilter, remove the searchfilter and the assigned searchfilteruser records
		else {
			getMasterDataFacade().remove(NuclosEntity.SEARCHFILTER.getEntityName(), SearchFilterVO.transformToMasterData(filterVO), true);
		}
	}

	/**
	 * updates the createdBy field of the given searchfilter
	 * ATTENTION: this is only used by the migration process
	 */
	@Override
    @RolesAllowed("Login")
	public void changeCreatedUser(Integer iId, String sUserName) throws NuclosBusinessRuleException, CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException, CommonValidationException, CommonPermissionException {
		DataBaseHelper.execute(DbStatementUtils.updateValues("T_UD_SEARCHFILTER",
			"STRCREATED", sUserName).where("INTID", iId));
	}

	/**
	 * get the user-id for the given user
	 * @param sUser
	 * @return Integer
	 * @throws CreateException
	 */
	private Integer getIdByUser(String sUser) {
		final CollectableSearchCondition cond = SearchConditionUtils.newMDComparison(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.USER), "name", ComparisonOperator.EQUAL, sUser);
		TruncatableCollection<MasterDataVO> collmdvo = getMasterDataFacade().getMasterData(NuclosEntity.USER.getEntityName(), cond, true);

		assert collmdvo.size() <= 1;

		if (collmdvo.isEmpty()) {
			return null;
		}

		return collmdvo.iterator().next().getIntId();
	}

	private void notifyClients(String[] asUsers) {
		NuclosJMSUtils.sendObjectMessage(asUsers, JMSConstants.TOPICNAME_SEARCHFILTERCACHE, null);
		//clientnotifier.notifyClientsByUsers(asUsers);
	}

	@Override
	public List<TranslationVO> getResources(Integer id) throws CommonBusinessException {
		ArrayList<TranslationVO> result = new ArrayList<TranslationVO>();

		MasterDataVO sf = getMasterDataFacade().get(NuclosEntity.SEARCHFILTER.getEntityName(), id);

		LocaleFacadeLocal service = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		String labelResourceId = sf.getField("labelres", String.class);
		String descriptionResourceId = sf.getField("descriptionres", String.class);

		for (LocaleInfo li : service.getAllLocales(false)) {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put(TranslationVO.labelsField[0], service.getResourceById(li, labelResourceId));
			labels.put(TranslationVO.labelsField[1], service.getResourceById(li, descriptionResourceId));

			TranslationVO vo = new TranslationVO(li.localeId, li.title, li.language, labels);
			result.add(vo);
		}
		return result;
	}

	private void setResources(MasterDataVO sf, List<TranslationVO>  translations) {
		String labelResourceId = sf.getField("labelres", String.class);
		String descriptionResourceId = sf.getField("descriptionres", String.class);

		Map<Integer, LocaleInfo> lis = CollectionUtils.transformIntoMap(getLocaleFacade().getAllLocales(false), new Transformer<LocaleInfo, Integer>() {
				@Override
				public Integer transform(LocaleInfo i) {
					return i.localeId;
				}
			}, new Transformer<LocaleInfo, LocaleInfo>() {
				@Override
				public LocaleInfo transform(LocaleInfo i) {
					return i;
				}
			});

		for(TranslationVO vo : translations) {
			LocaleInfo li = lis.get(vo.getLocaleId());

			labelResourceId = getLocaleFacade().setResourceForLocale(labelResourceId, li, vo.getLabels().get(TranslationVO.labelsField[0]));
			descriptionResourceId = getLocaleFacade().setResourceForLocale(descriptionResourceId, li, vo.getLabels().get(TranslationVO.labelsField[1]));
		}
		LocaleUtils.setResourceIdForField(SEARCHFILTER_TABLE, sf.getIntId(), LocaleUtils.FIELD_LABEL, labelResourceId);
		LocaleUtils.setResourceIdForField(SEARCHFILTER_TABLE, sf.getIntId(), LocaleUtils.FIELD_DESCRIPTION, descriptionResourceId);
	}
}
