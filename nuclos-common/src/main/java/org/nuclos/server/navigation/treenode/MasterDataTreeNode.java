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
package org.nuclos.server.navigation.treenode;

import java.util.StringTokenizer;

import org.nuclos.common.MasterDataMetaProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.masterdata.valueobject.MasterDataMetaVO;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;

/**
 * Tree node implementation representing a master data object.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:ramin.goettlich@novabit.de">ramin.goettlich</a>
 * @version 00.01.000
 */
public abstract class MasterDataTreeNode<Id> extends AbstractTreeNode<Id> implements Comparable<MasterDataTreeNode<Id>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String sEntityName;

	/**
	 * @param id id of tree node
	 */
	public MasterDataTreeNode(String sEntityName, Id id) {
		super(id);

		this.sEntityName = sEntityName;
	}

	public String getEntityName() {
		return this.sEntityName;
	}

	protected String getIdentifier(MasterDataVO mdvo) {
		MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
		if (cache != null) {
			final MasterDataMetaVO mdMeta = cache.getMetaData(this.getEntityName());
			String treeview = mdMeta.getTreeView();
			final String text = mdMeta.getResourceSIdForTreeView() != null ?
				CommonLocaleDelegate.getResourceById(CommonLocaleDelegate.getUserLocaleInfo(), mdMeta.getResourceSIdForTreeView()) : treeview;
			if (text != null) {
				return replaceTreeView(text, mdvo);
			}

			// if no treeview is set use field "name"; if "name" does not exist use the first field we find or "<undefiniert>" if this one is empty
			return mdvo.getField("name") != null ? mdvo.getField("name").toString() : mdvo.getField(mdMeta.getFieldNames().iterator().next()) != null ? mdvo.getField(mdMeta.getFieldNames().iterator().next()).toString() : "<undefiniert>";
		}
		else {
			return "<undefiniert>";
		}
	}

	protected String getDescription(MasterDataVO mdvo){
		MasterDataMetaProvider cache = SpringApplicationContextHolder.getBean(MasterDataMetaProvider.class);
		if (cache != null) {
			final MasterDataMetaVO mdMeta = cache.getMetaData(this.getEntityName());
			String treeviewdescription = mdMeta.getTreeView();
			final String text = mdMeta.getResourceSIdForTreeViewDescription() != null ?
				CommonLocaleDelegate.getResourceById(CommonLocaleDelegate.getUserLocaleInfo(), mdMeta.getResourceSIdForTreeViewDescription()) : treeviewdescription;
			if (text != null) {
				return replaceTreeView(text, mdvo);
			}
		}

		// if no treeview description
		return "Last change: " + mdvo.getChangedAt() + " by " + mdvo.getChangedBy();
	}

	/**
	 * replace the user defined pattern with the attribute values for this object
	 * @param sTreeView
	 * @param gowdvo
	 * @param attrprovider
	 * @return
	 */
    private String replaceTreeView(String sTreeView, MasterDataVO mdvo) {
       int sidx = 0;
       while ((sidx = sTreeView.indexOf("${", sidx)) >= 0) {
           int eidx = sTreeView.indexOf("}", sidx);
           String key = sTreeView.substring(sidx + 2, eidx);
           String flags = null;
           int ci = key.indexOf(':');
           if(ci >= 0) {
              flags = key.substring(ci + 1);
              key = key.substring(0, ci);
           }
           String rep = findReplacement(key, flags, mdvo);
           sTreeView = sTreeView.substring(0, sidx) + rep + sTreeView.substring(eidx + 1);
           sidx = sidx + rep.length();
      }
      return sTreeView;
  }

	/**
	 * replace a single attribute pattern with the value for this object
	 * @param sKey
	 * @param sFlag
	 * @param gowdvo
	 * @param attrprovider
	 * @return attribute value or "" if attribute has no value
	 */
    private String findReplacement(String sKey, String sFlag, MasterDataVO mdvo) {
       String sResIfNull = "";
       if(sFlag != null) {
          for(StringTokenizer st = new StringTokenizer(sFlag, ":"); st.hasMoreElements(); ) {
             String flag = st.nextToken();
             if(flag.startsWith("ifnull="))
                sResIfNull = flag.substring(7);
          }
       }
       final Object oValue = mdvo.getField(sKey);
       return oValue != null ? oValue.toString() : sResIfNull;
   }

   @Override
public int compareTo(MasterDataTreeNode<Id> that) {
   	return LangUtils.compareComparables(this.getLabel(), that.getLabel());
   }
}	// class MasterDataTreeNode
