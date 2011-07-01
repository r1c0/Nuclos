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

import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuclos.common.JMSConstants;
import org.nuclos.common.NuclosEntity;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.common.ejb3.NuclosFacadeBean;
import org.nuclos.server.common.valueobject.NuclosValueObject;
import org.nuclos.server.customcomp.valueobject.CustomComponentVO;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.transaction.annotation.Transactional;

@Stateless
@Remote(CustomComponentFacadeRemote.class)
//@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Transactional
public final class CustomComponentFacadeBean extends NuclosFacadeBean implements CustomComponentFacadeRemote {

	
	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);

	@Override
	public List<CustomComponentVO> getAll() {
		Collection<MasterDataVO> mdvos = getMasterDataFacade().getMasterData(NuclosEntity.CUSTOMCOMPONENT.getEntityName(), null, true);
//		Collection<MasterDataVO> mdvos = MasterDataDelegate.getInstance().getMasterData(
//			NuclosEntity.CUSTOMCOMPONENT.getEntityName());
		List<CustomComponentVO> vos = new ArrayList<CustomComponentVO>(mdvos.size());
		for (MasterDataVO mdvo : mdvos) {
			vos.add(getCustomComponentVO(mdvo));
		}
		return vos;
	}

	@Override
	public void create(CustomComponentVO vo) throws CommonBusinessException {
		getMasterDataFacade().create(NuclosEntity.CUSTOMCOMPONENT.getEntityName(),
			wrapVO(vo), null);
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);
		//clientnotifier.notifyClients(null);
	}

	@Override
	public void modify(CustomComponentVO vo) throws CommonBusinessException {
		getMasterDataFacade().modify(NuclosEntity.CUSTOMCOMPONENT.getEntityName(),
			wrapVO(vo), null);
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);
		//clientnotifier.notifyClients(null);
	}

	@Override
	public void remove(CustomComponentVO vo) throws CommonBusinessException {
		getMasterDataFacade().remove(NuclosEntity.CUSTOMCOMPONENT.getEntityName(),
			wrapVO(vo), true);
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_CUSTOMCOMPONENTCACHE);
		//clientnotifier.notifyClients(null);
	}

	private static MasterDataVO wrapVO(CustomComponentVO vo) {
		Map<String, Object> mpFields = new HashMap<String,Object>();
		mpFields.put("name", vo.getInternalName());
		mpFields.put("label", vo.getDefaultLabel());
		mpFields.put("menupath", vo.getDefaultMenuPath());
		mpFields.put("componenttype", vo.getComponentType());
		mpFields.put("componentversion", vo.getComponentVersion());
		mpFields.put("data", vo.getData());

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
		vo.setDefaultLabel(mdVO.getField("label", String.class));
		vo.setDefaultMenuPath(mdVO.getField("menupath", String.class));
		vo.setComponentType(mdVO.getField("componenttype", String.class));
		vo.setComponentVersion(mdVO.getField("componentversion", String.class));
		vo.setData(mdVO.getField("data", byte[].class));
		return vo;
	}

}
