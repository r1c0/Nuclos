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
package org.nuclos.server.resource;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.nuclos.common.JMSConstants;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.database.DataBaseHelper;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.jms.NuclosJMSUtils;
import org.nuclos.server.resource.valueobject.ResourceVO;

/**
 * A (server) cache for resources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 00.01.000
 */
public class ResourceCache {
	
	private Map<String, ResourceVO> mpResourcesByName;
	private Map<Integer, ResourceVO> mpResourcesById;
	
	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_RESOURCECACHE);
	
	private Map<ResourceVO, byte[]> mpResources = CollectionUtils.newHashMap();
	
	private static final File resourceDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.RESOURCE_PATH);
	
	private static ResourceCache singleton;
	
	public static synchronized ResourceCache getInstance() {
		if (singleton == null) {
			singleton = new ResourceCache();
		}
		return singleton;
	}
	
	private ResourceCache() {
		this.mpResourcesByName = buildMap();
		this.mpResourcesById = buildIdMap();
	}
	
	/**
	 * init the map
	 */
	private Map<String, ResourceVO> buildMap() {
		return CollectionUtils.transformIntoMap(buildIdMap().values(), new Transformer<ResourceVO, String>() {
			@Override
			public String transform(ResourceVO resourcevo) {
				return resourcevo.getName();
			}
		});
	}
	
	/**
	 * init the ids map
	 */
	private Map<Integer, ResourceVO> buildIdMap() {
		DbQueryBuilder builder = DataBaseHelper.getDbAccess().getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_MD_RESOURCE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("INTID", Integer.class),
			t.baseColumn("DATCREATED", Date.class),
			t.baseColumn("STRCREATED", String.class),
			t.baseColumn("DATCHANGED", Date.class),
			t.baseColumn("STRCHANGED", String.class),
			t.baseColumn("INTVERSION", Integer.class),
			t.baseColumn("STRNAME", String.class),
			t.baseColumn("STRDESCRIPTION", String.class),
			t.baseColumn("STRFILENAME", String.class),
			t.baseColumn("BLNSYSTEMRESOURCE", Boolean.class));

		final Map<Integer, ResourceVO> result = CollectionUtils.newHashMap();
		for (DbTuple tuple : DataBaseHelper.getDbAccess().executeQuery(query)) {
			ResourceVO resourcevo = new ResourceVO(
				tuple.get(0, Integer.class),
				tuple.get(1, Date.class),
				tuple.get(2, String.class),
				tuple.get(3, Date.class),
				tuple.get(4, String.class),
				tuple.get(5, Integer.class),
				tuple.get(6, String.class),
				tuple.get(7, String.class),
				tuple.get(8, String.class),
				tuple.get(9, Boolean.class));
			result.put(resourcevo.getId(), resourcevo);
		}
		return result;
	}

	/**
	 * @param sResourceName
	 */
	public ResourceVO getResourceByName(String sResourceName) {
		if (mpResourcesByName.isEmpty()) {
			mpResourcesByName = buildMap();
		}
		return mpResourcesByName.get(sResourceName);
	}
	
	public ResourceVO getResourceById(Integer iResourceId) {
		if (mpResourcesById.isEmpty()) {
			mpResourcesById = buildIdMap();
		}
		return mpResourcesById.get(iResourceId);
	}
	
	/**
	 * @param sResourceName
	 * @return the resource as <code>byte[]</code>
	 */
	public byte[] getResource(String sResourceName) {
		if (mpResourcesByName.isEmpty()) {
			mpResourcesByName = buildMap();
		}
		ResourceVO resourcevo = mpResourcesByName.get(sResourceName);
		if (!mpResources.containsKey(resourcevo)) {
			if(resourcevo != null && resourcevo.getFileName() != null) {
				final File file = new File(resourceDir, resourcevo.getFileName());			
				try {
					byte[] resource = IOUtils.readFromBinaryFile(file);
					mpResources.put(resourcevo, resource);
				}
				catch(IOException ex) {
					throw new CommonFatalException(ex);
				}		
			}				
		}
		return mpResources.get(resourcevo);		
	}
	
	public void invalidate() {
		this.mpResourcesByName.clear();
		this.mpResourcesById.clear();
		this.mpResources.clear();
		
		this.notifyClients();	
	}
	
	private void notifyClients() {
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_RESOURCECACHE);
	}
}
