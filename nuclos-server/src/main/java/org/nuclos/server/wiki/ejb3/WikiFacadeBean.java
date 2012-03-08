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
package org.nuclos.server.wiki.ejb3;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.nuclos.common.NuclosEntity;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.server.autosync.XMLEntities;
import org.nuclos.server.common.ServerParameterProvider;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wiki facade bean.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Transactional(noRollbackFor= {Exception.class})
public class WikiFacadeBean extends MasterDataFacadeBean implements WikiFacadeRemote {
	
	public WikiFacadeBean() {
	}

	public String getWikiPageFor(String sEntityName, String sAttributeName) {
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<Integer> query = builder.createQuery(Integer.class);
		DbFrom t = query.from("T_MD_WIKI").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("INTID", Integer.class));
		query.where(builder.equal(t.baseColumn("STRMASTERDATA", String.class), sEntityName));
		
		Collection<Integer> stWikiIds = dataBaseHelper.getDbAccess().executeQuery(query); 
		
		stWikiIds.addAll(CollectionUtils.typecheck(
			XMLEntities.getSystemObjectIdsWith(NuclosEntity.WIKI.getEntityName(), "entity", getMetaData(sEntityName).getEntityName()),
			Integer.class));

		Set<MasterDataVO> coll_wikimapping = new HashSet<MasterDataVO>();
		for (Integer iWikiId : stWikiIds) {
			coll_wikimapping.addAll(getDependantMasterData(NuclosEntity.WIKIMAPPING.getEntityName(), "wiki", iWikiId));
		}

		Object sWikiPage = null;
		for (MasterDataVO mdvo : coll_wikimapping) {
			// wikipage for an attribute
			if (mdvo.getField("attribute") != null && mdvo.getField("attribute").equals(sAttributeName)) {
				sWikiPage = mdvo.getField("wikipage");
			}
			//wikipage for a masterdata field
			else if (mdvo.getField("field") != null && mdvo.getField("field").equals(sAttributeName)) {
				sWikiPage = mdvo.getField("wikipage");
			}
			//wikipage for an entity
			else if (mdvo.getField("attribute") == null && mdvo.getField("field") == null && sAttributeName == null) {
				sWikiPage = mdvo.getField("wikipage");
			}
			//wikipage for a component within the layout: btnNew, btnClone ...
			else if (mdvo.getField("attribute") == null && mdvo.getField("field") == null && sAttributeName != null) {
				if (getMetaData(sEntityName).getField(sAttributeName) != null) {
					sWikiPage = mdvo.getField("wikipage");
				}
				else {
					sWikiPage = getWikiPageFor(sAttributeName);
				}
			}
		}
		//if no wikipages for this entity try to find a page for this component within the layout in "general" components
		if (sWikiPage == null && sAttributeName != null) {
			sWikiPage = getWikiPageFor(sAttributeName);
		}

		return sWikiPage != null ? (String)sWikiPage : ServerParameterProvider.KEY_WIKI_DEFAULT_PAGE;
	}

	/**
	 * wikipage for a "general" component: explorer, menubar, statusbar ...
	 */
	public String getWikiPageFor(String sComponentName) {
		Collection<MasterDataVO> coll_wikimappinggeneral = getMasterData(NuclosEntity.WIKIMAPPINGGENERAL.getEntityName(), null, true);

		for (MasterDataVO mdvo : coll_wikimappinggeneral) {
			if (mdvo.getField("name") != null && mdvo.getField("name").equals(sComponentName)) {
				return mdvo.getField("wikipage") != null ? (String)mdvo.getField("wikipage") : ServerParameterProvider.KEY_WIKI_DEFAULT_PAGE;
			}
		}
		return ServerParameterProvider.KEY_WIKI_DEFAULT_PAGE;
	}
}
