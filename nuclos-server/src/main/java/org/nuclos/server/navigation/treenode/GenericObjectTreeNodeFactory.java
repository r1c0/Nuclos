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

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.AttributeProvider;
import org.nuclos.common.ModuleProvider;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common.SpringApplicationContextHolder;
import org.nuclos.common.attribute.DynamicAttributeVO;
import org.nuclos.common.collect.collectable.searchcondition.CollectableIdCondition;
import org.nuclos.common.security.Permission;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.LangUtils;
import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.StringUtils;
import org.nuclos.common2.TruncatableCollection;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.SecurityCache;
import org.nuclos.server.genericobject.ejb3.GenericObjectFacadeRemote;
import org.nuclos.server.genericobject.searchcondition.CollectableSearchExpression;
import org.nuclos.server.genericobject.valueobject.GenericObjectWithDependantsVO;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.RelationDirection;
import org.nuclos.server.navigation.treenode.GenericObjectTreeNode.SystemRelationType;

/**
 * Factory that creates <code>GenericObjectTreeNode</code>s.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:christoph.radig@novabit.de">christoph.radig</a>
 * @version 01.00.00
 */
public class GenericObjectTreeNodeFactory {

	private static GenericObjectTreeNodeFactory singleton;

	public static synchronized GenericObjectTreeNodeFactory getInstance() {
		if (singleton == null) {
			singleton = newFactory();
		}
		return singleton;
	}

	private static GenericObjectTreeNodeFactory newFactory() {
		try {
			final String sClassName = LangUtils.defaultIfNull(
					ApplicationProperties.getInstance().getGenericObjectTreeNodeFactoryClassName(),
					GenericObjectTreeNodeFactory.class.getName());

			return (GenericObjectTreeNodeFactory) Class.forName(sClassName).newInstance();
		}
		catch (Exception ex) {
			throw new CommonFatalException("GenericObjectTreeNodeFactory cannot be created.", ex);
		}
	}

	/**
	 * creates a GenericObjectTreeNode.
	 * @param gowdvo
	 * @param attrprovider
	 * @param paramprovider
	 * @return a new GenericObjectTreeNode
	 * @postcondition result != null
	 */
	public GenericObjectTreeNode newTreeNode(GenericObjectWithDependantsVO gowdvo,
			AttributeProvider attrprovider, ParameterProvider paramprovider,
			Integer iRelationId, SystemRelationType relationtype, RelationDirection direction,
			String sUserName) {
		String label = getIdentifier(gowdvo, attrprovider, sUserName);
		String description = getDescription(gowdvo, attrprovider, gowdvo.getChangedAt(), sUserName);
		return new GenericObjectTreeNode(gowdvo, attrprovider, iRelationId, relationtype, direction, sUserName, label, description);
	}

	/**
	 * get the representation of this node in the tree
	 * @param gowdvo
	 * @param attrprovider
	 * @return
	 */
	protected String getIdentifier(GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, String username) {
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		String sTreeView = (String)modules.getModuleById(gowdvo.getModuleId()).getField("treeview");
		if(sTreeView != null) {
			sTreeView = CommonLocaleDelegate.getResourceById(CommonLocaleDelegate.getUserLocaleInfo(), sTreeView);
			this.addAttribute(gowdvo, attrprovider, StringUtils.getFieldsFromTreeViewPattern(sTreeView));
			return replaceTreeView(sTreeView, gowdvo, attrprovider, username);
		}
		return LangUtils.defaultIfNull(gowdvo.getSystemIdentifier(), "#FEHLER#");
	}

	/**
	 * get the description of the representation of this node in the tree
	 * @param gowdvo
	 * @param attrprovider
	 * @param dateChangedAt
	 * @return
	 */
	public String getDescription(GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, Date dateChangedAt, String username){
		ModuleProvider modules = SpringApplicationContextHolder.getBean(ModuleProvider.class);
		String sTreeViewDescription = (String)modules.getModuleById(gowdvo.getModuleId()).getField("treeviewdescription");
		if(sTreeViewDescription != null) {
			sTreeViewDescription = CommonLocaleDelegate.getResourceById(CommonLocaleDelegate.getUserLocaleInfo(), sTreeViewDescription);
			this.addAttribute(gowdvo, attrprovider, StringUtils.getFieldsFromTreeViewPattern(sTreeViewDescription));
			return replaceTreeView(sTreeViewDescription, gowdvo, attrprovider, username);
		}

		//if no treeview description
		return MessageFormat.format(CommonLocaleDelegate.getResourceById(CommonLocaleDelegate.getUserLocaleInfo(), "gotreenode.tooltip"), DateFormat.getDateTimeInstance().format(dateChangedAt));
		//"Last change: " + DateFormat.getDateTimeInstance().format(dateChangedAt);
	}

	/**
	 * replace the user defined pattern with the attribute values for this object
	 * @param sTreeView
	 * @param gowdvo
	 * @param attrprovider
	 * @return
	 */
    private String replaceTreeView(String sTreeView, GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, String username) {
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
           String rep = findReplacement(key, flags, gowdvo, attrprovider, username);
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
    private String findReplacement(String sKey, String sFlag, GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, String username) {
    	String sResIfNull = "";
    	String sResIfNoPermission = StringUtils.defaultIfNull(SpringApplicationContextHolder.getBean(ParameterProvider.class).getValue(ParameterProvider.KEY_BLUR_FILTER),"***");

    	if(sFlag != null) {
    		for(StringTokenizer st = new StringTokenizer(sFlag, ":"); st.hasMoreElements(); ) {
    			String flag = st.nextToken();
    			if(flag.startsWith("ifnull="))
    				sResIfNull = flag.substring(7);
    		}
    	}
    	DynamicAttributeVO attrVO = gowdvo.getAttributeForTreeView(attrprovider.getAttribute(gowdvo.getModuleId(), sKey).getId());

    	boolean blnReadPermission = isReadAllowedForAttribute(username, sKey, gowdvo, attrprovider);

    	String oValue = null;
    	if (attrVO != null && attrVO.getValue() != null) {
     		if (attrVO.getValue() instanceof java.util.Date) {
    		oValue = CommonLocaleDelegate.getDateFormat().format(attrVO.getValue());
     		}
     		else {
     			oValue = attrVO.getValue().toString();
     		}
     	 }
     	 else {
     		oValue = sResIfNull;
     	 }
    	return (blnReadPermission) ? oValue : sResIfNoPermission;
   }

    /**
	 * add and set additional attributes to gowdvo
	 * @param gowdvo
	 * @param attrprovider
	 * @param stSAttribute
	 */
	protected void addAttribute(GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider, Set<String> stSAttribute) {
		// 1. create search condition/expression with id of original gowdvo
		final CollectableIdCondition collIdCond = new CollectableIdCondition(gowdvo.getId());
		final CollectableSearchExpression collSearchExpr = new CollectableSearchExpression(collIdCond);

		// 2. define set of additional attributes
		final Set<Integer> stIAttribute = new HashSet<Integer>();
		for (String sAttribute : stSAttribute) {
			Integer id = attrprovider.getAttribute(gowdvo.getModuleId(), sAttribute).getId();
			if(!gowdvo.wasAttributeIdLoaded(id))
				stIAttribute.add(id);
		}

		if(stIAttribute.isEmpty())
			return;

		final int DEFAULT_ROWCOUNT_FOR_SEARCHRESULT = 500;
		final ParameterProvider paramprovider = SpringApplicationContextHolder.getBean(ParameterProvider.class);
		final int iMaxRowCount = paramprovider.getIntValue(ParameterProvider.KEY_MAX_ROWCOUNT_FOR_SEARCHRESULT_IN_TREE, DEFAULT_ROWCOUNT_FOR_SEARCHRESULT);

		// 3. create temporary gowdvo incuding the new attributes
		TruncatableCollection<GenericObjectWithDependantsVO> collgowdvo = null;
		try {
			// TODO switch from remote to local interface
			collgowdvo = ServiceLocator.getInstance().getFacade(GenericObjectFacadeRemote.class).getRestrictedNumberOfGenericObjects(gowdvo.getModuleId(), collSearchExpr,
						stIAttribute, getSubEntityNamesRequiredForGenericObjectTreeNode(), iMaxRowCount);
		}
		catch (RuntimeException ex) {
			throw new CommonFatalException(ex);
		}

		// 4. add new attributes to original gowdvo
		for (String sAttribute : stSAttribute) {
			gowdvo.addAttribute(attrprovider.getAttribute(gowdvo.getModuleId(), sAttribute).getId());
		}

		assert collgowdvo != null && collgowdvo.size() == 1;

		// 5. set values of new attributes in original gowdvo using temporary gowdvo
		for (GenericObjectWithDependantsVO vo: collgowdvo) {
			for (String sAttribute : stSAttribute) {
				DynamicAttributeVO daVO = vo.getAttribute(attrprovider.getAttribute(gowdvo.getModuleId(), sAttribute).getId());
				if (daVO != null) {
					gowdvo.setAttribute(daVO);
				}
			}
		}
	}

	private Set<String> getSubEntityNamesRequiredForGenericObjectTreeNode() {
		return Collections.emptySet();
	}

	/**
	    * check whether the data of the attribute is readable for current user
	    * @param sUserName
	    * @param sKey
	    * @param gowdvo
	    * @param attrprovider
	    * @return true, if attribute data is readable, otherwise false
	    */
	   public boolean isReadAllowedForAttribute(String sUserName, String sKey, GenericObjectWithDependantsVO gowdvo, AttributeProvider attrprovider) {
		   Integer iAttributeGroupId = SpringApplicationContextHolder.getBean(AttributeProvider.class).getAttribute(gowdvo.getModuleId(), sKey).getAttributegroupId();
		   Permission permission = SecurityCache.getInstance().getAttributeGroup(sUserName, iAttributeGroupId).get(gowdvo.getStatusId());

		   return (permission == null) ? false : permission.includesReading();
	   }
}	// class GenericObjectTreeNodeFactory
