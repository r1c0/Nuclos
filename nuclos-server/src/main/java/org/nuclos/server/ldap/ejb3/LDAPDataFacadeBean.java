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
package org.nuclos.server.ldap.ejb3;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.SearchConditionUtils;
import org.nuclos.common.collect.collectable.searchcondition.CollectableSearchCondition;
import org.nuclos.common.collect.collectable.searchcondition.ComparisonOperator;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.MasterDataMetaCache;
import org.nuclos.server.common.MetaDataServerProvider;
import org.nuclos.server.common.ServerServiceLocator;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.dal.provider.NucletDalProvider;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeLocal;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMapImpl;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.masterdata.valueobject.MasterDataWithDependantsVOWrapper;
import org.nuclos.server.security.NuclosLdapBindAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.control.PagedResultsCookie;
import org.springframework.ldap.control.PagedResultsDirContextProcessor;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.core.support.SingleContextSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade bean for all LDAP data management functions.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class LDAPDataFacadeBean extends NuclosFacadeBean implements LDAPDataFacadeRemote {

	private static final Logger log = Logger.getLogger(LDAPDataFacadeBean.class);

	private final static int PAGESIZE = 1000;
	
	private MasterDataFacadeLocal masterDataFacade;
	
	public LDAPDataFacadeBean() {
	}

	@Autowired
	final void setMasterDataFacade(MasterDataFacadeLocal masterDataFacade) {
		this.masterDataFacade = masterDataFacade;
	}
	
	public MasterDataVO create(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.LDAPSERVER);
		validate(vo, mpDependants);

		return masterDataFacade.create(NuclosEntity.LDAPSERVER.getEntityName(), vo, mpDependants, null);
	}

	public MasterDataVO modify(MasterDataVO vo, DependantMasterDataMap mpDependants) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.LDAPSERVER);
		validate(vo, mpDependants);

		Integer id = (Integer) masterDataFacade.modify(NuclosEntity.LDAPSERVER.getEntityName(), vo, mpDependants, null);
		return masterDataFacade.get(NuclosEntity.LDAPSERVER.getEntityName(), id);
	}

	/**
	 * Find all LDAP users.
	 * @param filterExpr ldap filter expression, i.e. (sAMAccountName={0})
	 * @param filterArgs filter parameters, i.e. username
	 * @return a collection containing the search result for the given search expression.
	 * TODO restrict permissions
	 */
	@SuppressWarnings("deprecation")
	@RolesAllowed("Login")
	public Collection<MasterDataWithDependantsVOWrapper> getUsers(String filterExpr, Object[] filterArgs) throws CommonBusinessException {
		List<EntityObjectVO> servers = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.LDAPSERVER).getAll();

		final Collection<MasterDataWithDependantsVOWrapper> searchResult = new ArrayList<MasterDataWithDependantsVOWrapper>();

		for (EntityObjectVO server : servers) {
			if (server.getField("active", Boolean.class) == null
				|| !server.getField("active", Boolean.class)
				|| StringUtils.isNullOrEmpty(server.getField("serversearchfilter", String.class))) {
				continue;
			}

			String url = server.getField("serverurl", String.class);
	    	String context = server.getField("serversearchcontext", String.class);
	    	Integer searchscope = server.getField("serversearchscope", Integer.class);
	    	String bindDN = server.getField("binddn", String.class);
			String bindCredential = server.getField("bindcredential", String.class);
			String filter = server.getField("serversearchfilter", String.class);

			LdapContextSource contextSource = new LdapContextSource();
			contextSource.setUrl(url);
			contextSource.setBase(context);
			if (!StringUtils.isNullOrEmpty(bindDN)) {
				contextSource.setUserDn(bindDN);
				contextSource.setPassword(bindCredential);
			}
			else {
				contextSource.setAnonymousReadOnly(true);
			}
			contextSource.setReferral("ignore");
			try {
				contextSource.afterPropertiesSet();
			}
			catch(Exception e) {
				throw new NuclosFatalException(e);
			}

			final Map<String, String> attributeMapping = new HashMap<String, String>();

			CollectableSearchCondition attrcond = SearchConditionUtils.newEOComparison(NuclosEntity.LDAPMAPPING.getEntityName(), "ldapserver", ComparisonOperator.EQUAL, server.getId(), MetaDataServerProvider.getInstance());
			List<EntityObjectVO> mappings = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.LDAPMAPPING).getBySearchExpression(new CollectableSearchExpression(attrcond));

			if (mappings != null && mappings.size() > 0) {
				for (EntityObjectVO mapping : mappings) {
					attributeMapping.put(mapping.getField("fieldNucleus", String.class), mapping.getField("fieldLDAP", String.class));
				}

				if (!StringUtils.isNullOrEmpty(filterExpr)) {
					filter = "(&" + filterExpr + filter + ")";
				}

				SearchControls constraints = new SearchControls();
				constraints.setSearchScope(searchscope);
				constraints.setReturningAttributes(attributeMapping.values().toArray(new String[attributeMapping.values().size()]));

				SingleContextSource singleContextSource = new SingleContextSource(contextSource.getReadOnlyContext());

				try {
					LdapTemplate template = new LdapTemplate(singleContextSource);
					template.setIgnorePartialResultException(true);

					PagedResultsCookie cookie;
					PagedResultsDirContextProcessor pagingProcessor = new PagedResultsDirContextProcessor(PAGESIZE);

					do {
						template.search("", MessageFormat.format(filter, filterArgs), constraints, new ContextMapper() {
							@Override
							public Object mapFromContext(Object ctx) {
								DirContextOperations dirctx = (DirContextOperations) ctx;
								final MasterDataVO result = new MasterDataVO(NuclosEntity.USER.getEntityName(), null, 
										null, "INITIAL", null, "INITIAL", new Integer(1), null);
								HashMap<String, Object> wrapperFields = new HashMap<String, Object>();
								wrapperFields.put("superuser", false);
								Iterator<String> keysIt = attributeMapping.keySet().iterator();
								Attributes attrs =  dirctx.getAttributes();
								String reqKey = null;
								while (keysIt.hasNext()) {
									reqKey = keysIt.next();
									Object value = null;
									try {
										if (attrs.get(attributeMapping.get(reqKey)) != null && attrs.get(attributeMapping.get(reqKey)).get(0) != null) {
											value = attrs.get(attributeMapping.get(reqKey)).get(0);
										}
									}
									catch (NamingException ex) {
										throw new NuclosFatalException(ex);
									}
									wrapperFields.put(reqKey, value);
									result.setField(reqKey, value);
								}
								MasterDataWithDependantsVOWrapper wrapper = new MasterDataWithDependantsVOWrapper(result, null, new ArrayList<String>(attributeMapping.keySet()), wrapperFields);
								wrapper.setIsWrapped();
								searchResult.add(wrapper);
								return wrapper;
							}
						}, pagingProcessor);

						cookie = pagingProcessor.getCookie();
						pagingProcessor = new PagedResultsDirContextProcessor(PAGESIZE, cookie);
					} while (cookie != null && cookie.getCookie() != null);
				}
				finally {
					singleContextSource.destroy();
				}
			}
		}

		final Collection<MasterDataVO> nucleususers = ServerServiceLocator.getInstance().getFacade(
				MasterDataFacadeLocal.class).getMasterData(NuclosEntity.USER.getEntityName(), null, true);

		final Map<String, MasterDataWithDependantsVOWrapper> map = new HashMap<String, MasterDataWithDependantsVOWrapper>();
		for (MasterDataWithDependantsVOWrapper wrapper : searchResult) {
			map.put(wrapper.getField(MasterDataVO.FIELDNAME_NAME).toString().toLowerCase(), wrapper);
		}

		for (MasterDataVO nucleususer : nucleususers) {
			MasterDataWithDependantsVOWrapper wrapper;
			String key = nucleususer.getField(MasterDataVO.FIELDNAME_NAME).toString().toLowerCase();
			if (map.containsKey(key)) {
				wrapper = new MasterDataWithDependantsVOWrapper(nucleususer, null, new ArrayList<String>(map.get(key).getMappedFields()));
				wrapper.setWrapperFields(map.get(key).getWrapperFields());
				wrapper.setIsMapped();
				map.remove(key);
			}
			else {
				wrapper = new MasterDataWithDependantsVOWrapper(nucleususer, null, new ArrayList<String>());
				wrapper.setIsNative();
			}
			map.put(key, wrapper);
		}

		return new ArrayList<MasterDataWithDependantsVOWrapper>(map.values());
    }

	@RolesAllowed("Login")
	public boolean tryAuthentication(String ldapserver, String username, String password) throws CommonPermissionException, CommonBusinessException {
		checkWriteAllowed(NuclosEntity.LDAPSERVER);

		CollectableSearchCondition cond = SearchConditionUtils.newEOComparison(NuclosEntity.LDAPSERVER.getEntityName(), "servername", ComparisonOperator.EQUAL, ldapserver, MetaDataServerProvider.getInstance());
		List<EntityObjectVO> servers = NucletDalProvider.getInstance().getEntityObjectProcessor(NuclosEntity.LDAPSERVER).getBySearchExpression(new CollectableSearchExpression(cond));

		if (servers != null && servers.size() > 0) {
			EntityObjectVO server = servers.get(0);

			String url = server.getField("serverurl", String.class);
			String baseDN = server.getField("serversearchcontext", String.class);
			String bindDN = server.getField("binddn", String.class);
			String bindCredential = server.getField("bindcredential", String.class);
			String userSearchFilter = server.getField("userfilter", String.class);
			Integer scope = server.getField("serversearchscope", Integer.class);

			try {
				NuclosLdapBindAuthenticator authenticator = new NuclosLdapBindAuthenticator(url, baseDN, bindDN, bindCredential, userSearchFilter, scope);
				return authenticator.authenticate(new UsernamePasswordAuthenticationToken(username, password));
			}
			catch (Exception ex) {
				log.info("LDAP error", ex);
				throw new CommonBusinessException(ex.getMessage());
			}
		}
		else {
			throw new NuclosFatalException("ldap.exception.servernotfound");
		}
	}

	private void validate(MasterDataVO vo, DependantMasterDataMap dependants) throws CommonValidationException {
		vo.validate(MasterDataMetaCache.getInstance().getMetaData(NuclosEntity.LDAPSERVER));

		if (!StringUtils.isNullOrEmpty(vo.getField("binddn", String.class))
			&& StringUtils.isNullOrEmpty(vo.getField("bindcredential", String.class))) {
			throw new CommonValidationException("ldap.validation.password");
		}

		Boolean active = vo.getField("active", Boolean.class);
		String sync = vo.getField("serversearchfilter", String.class);
		
		if (active != null && active && !StringUtils.isNullOrEmpty(sync)) {
			List<String> nuclosfields = new ArrayList<String>(Arrays.asList("name", "firstname", "lastname"));
			for (EntityObjectVO mdvo : dependants.getData(NuclosEntity.LDAPMAPPING.getEntityName())) {
				String na = mdvo.getField("fieldNucleus", String.class);
				if (!StringUtils.isNullOrEmpty(na) && !mdvo.isFlagRemoved()) {
					nuclosfields.remove(na);
				}
			}
			if (nuclosfields.size() > 0) {
				throw new CommonValidationException("ldap.validation.mapping");
			}
		}
	}
}
