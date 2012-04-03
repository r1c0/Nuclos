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
package org.nuclos.client.livesearch;

import java.awt.Image;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.nuclos.client.image.ImageScaler;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.dal.vo.EntityMetaDataVO;
import org.nuclos.common.dal.vo.EntityObjectVO;
import org.nuclos.common2.SpringLocaleDelegate;

/*package*/class LiveSearchResultRow {
	public static final int ICON_SIZE = 24;
	
	public final EntityObjectVO          theObject;
	public final Icon                    icon;
	public final EntityMetaDataVO        entityMeta;
	public final String                  entityName;    // tech. name
	public final String                  entityLabel;   // Translation
	public final String                  titleString;
	public final String                  description;
	public final Map<String, String>     matchDescriptions;
	
	public LiveSearchResultRow(EntityObjectVO theObject, ImageIcon icon, EntityMetaDataVO entityMeta, String titleString, Map<String, String> matchDescriptions) {
		this.theObject = theObject;
		// final Image image = icon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
		final Image image = ImageScaler.scaleImage(icon.getImage(), ICON_SIZE, ICON_SIZE);
		this.icon = new ImageIcon(image);		
	    this.entityMeta = entityMeta;
	    this.titleString = titleString;
	    this.matchDescriptions = matchDescriptions;
	    
	    this.entityName = entityMeta.getEntity();
	    this.entityLabel = SpringLocaleDelegate.getInstance().getLabelFromMetaDataVO(entityMeta);
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("<html><b>")
	    	.append(titleString)
	    	.append("</b><br><small>");
	    String sep = "&nbsp;&nbsp;";
	    for(String key : CollectionUtils.sorted(matchDescriptions.keySet())) {
	    	sb.append(sep)
	    		.append(key)
	    		.append("=")
	    		.append(matchDescriptions.get(key));
	    	sep = ", ";
	    }
	    this.description = sb.toString();
    }
}
