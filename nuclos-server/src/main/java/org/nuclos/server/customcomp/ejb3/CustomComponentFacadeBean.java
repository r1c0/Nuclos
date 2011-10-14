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

package org.nuclos.server.customcomp.ejb3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common.TranslationVO;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common2.LocaleInfo;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.LocaleFacadeLocal;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Transactional
public final class CustomComponentFacadeBean extends NuclosFacadeBean implements CustomComponentFacadeRemote {

	private static final Logger LOG = Logger.getLogger(CustomComponentFacadeBean.class);

	private static final TransactionSynchronization ts = new TransactionSynchronizationAdapter() {
		@Override
		public void afterCommit() {
			NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);
		}
	};

	private void notifyClients() {
		try {
			List<TransactionSynchronization> list = TransactionSynchronizationManager.getSynchronizations();
			if (!list.contains(ts)) {
				TransactionSynchronizationManager.registerSynchronization(ts);
			}
		}
		catch (IllegalStateException ex) {
			LOG.warn("Error on transaction synchronization registration.", ex);
		}
	}

	@Override
	public List<CustomComponentVO> getAll() {
		Collection<MasterDataVO> mdvos = getMasterDataFacade().getMasterData(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), null, true);
		List<CustomComponentVO> vos = new ArrayList<CustomComponentVO>(mdvos.size());
		for (MasterDataVO mdvo : mdvos) {
			vos.add(getCustomComponentVO(mdvo));
		}
		return vos;
	}

	@Override
	public void create(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.CUSTOMCOMPONENT);
		MasterDataVO result = getMasterDataFacade().create(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), wrapVO(vo), null);

		setResources(getCustomComponentVO(result), translations);

		notifyClients();
	}

	@Override
	public void modify(CustomComponentVO vo, List<TranslationVO> translations) throws CommonBusinessException {
		checkWriteAllowed(NuclosEntity.CUSTOMCOMPONENT);
		getMasterDataFacade().modify(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), wrapVO(vo), null);

		setResources(vo, translations);

		notifyClients();
	}

	@Override
	public void remove(CustomComponentVO vo) throws CommonBusinessException {
		getMasterDataFacade().remove(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), wrapVO(vo), true);

		notifyClients();
	}

	private static MasterDataVO wrapVO(CustomComponentVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getInternalName());
		mpFields.put("label", vo.getLabelResourceId());
		mpFields.put("menupath", vo.getMenupathResourceId());
		mpFields.put("componenttype", vo.getComponentType());
		mpFields.put("componentversion", vo.getComponentVersion());
		mpFields.put("data", vo.getData());
		mpFields.put("nucletId", vo.getNucletId()==null?null:vo.getNucletId().intValue());

		return new MasterDataVO(vo.getId(), vo.getChangedAt(), vo.getCreatedBy(), vo.getChangedAt(), vo.getChangedBy(), vo.getVersion(), mpFields);
	}

	private static CustomComponentVO getCustomComponentVO(MasterDataVO mdVO) {
		NuclosValueObject nvo = new NuclosValueObject(
			(Integer) mdVO.getId(),
			mdVO.getCreatedAt(),
			mdVO.getChangedBy(),
			mdVO.getChangedAt(),
			mdVO.getChangedBy(),
			mdVO.getVersion());
		CustomComponentVO vo = new CustomComponentVO(nvo);
		vo.setInternalName(mdVO.getField("name", String.class));
		vo.setLabelResourceId(mdVO.getField("label", String.class));
		vo.setMenupathResourceId(mdVO.getField("menupath", String.class));
		vo.setComponentType(mdVO.getField("componenttype", String.class));
		vo.setComponentVersion(mdVO.getField("componentversion", String.class));
		vo.setData(mdVO.getField("data", byte[].class));
		vo.setNucletId(mdVO.getField("nucletId", Integer.class)==null?null:mdVO.getField("nucletId", Integer.class).longValue());
		return vo;
	}

	private void setResources(CustomComponentVO cc, List<TranslationVO>  translations) {
		String labelResourceId = cc.getLabelResourceId();
		String menupathResourceId = cc.getMenupathResourceId();

		if (!isResourceId(labelResourceId)) {
			labelResourceId = null;
		}

		if (!isResourceId(menupathResourceId)) {
			menupathResourceId = null;
		}

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

			labelResourceId = getLocaleFacade().setResourceForLocale(labelResourceId, li, vo.getLabels().get(TranslationVO.labelsCustomComponent[0]));
			menupathResourceId = getLocaleFacade().setResourceForLocale(menupathResourceId, li, vo.getLabels().get(TranslationVO.labelsCustomComponent[1]));

		}
		setResourceIdForField(cc.getId(), "STRLABEL", labelResourceId);
		setResourceIdForField(cc.getId(), "STRMENUPATH", menupathResourceId);
	}

	private static boolean isResourceId(String s) {
		return ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class).isResourceId(s);
	}

	private void setResourceIdForField(Integer iId, String column, String sResourceId) {
		DataBaseHelper.execute(DbStatementUtils.updateValues("T_MD_CUSTOMCOMPONENT", column, DbNull.escapeNull(sResourceId, String.class)).where("INTID", iId));
	}

	@Override
	public List<TranslationVO> getTranslations(Integer ccid) throws CommonBusinessException {
		ArrayList<TranslationVO> result = new ArrayList<TranslationVO>();

		CustomComponentVO cc = getCustomComponentVO(getMasterDataFacade().get(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), ccid));

		LocaleFacadeLocal service = ServiceLocator.getInstance().getFacade(LocaleFacadeLocal.class);

		// If the resource-ids do not exist, setup the translation list with these values (backwards compatibility)
		boolean isResourceIdLabel = service.isResourceId(cc.getLabelResourceId());
		boolean isResourceIdMenupath = service.isResourceId(cc.getMenupathResourceId());

		for (LocaleInfo li : service.getAllLocales(false)) {
			Map<String, String> labels = new HashMap<String, String>();
			labels.put(TranslationVO.labelsCustomComponent[0], isResourceIdLabel ? service.getResourceById(li, cc.getLabelResourceId()) : cc.getLabelResourceId());
			labels.put(TranslationVO.labelsCustomComponent[1], isResourceIdMenupath ? service.getResourceById(li, cc.getMenupathResourceId()) : cc.getMenupathResourceId());

			TranslationVO vo = new TranslationVO(li.localeId, li.title, li.language, labels);
			result.add(vo);
		}
		return result;
	}
}
