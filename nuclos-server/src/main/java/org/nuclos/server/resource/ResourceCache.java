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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A (server) cache for resources.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:corina.mandoki@novabit.de">Corina Mandoki</a>
 * @version 00.01.000
 */
@Component
public class ResourceCache {
	
	private Map<String, ResourceVO> mpResourcesByName;
	private Map<Integer, ResourceVO> mpResourcesById;
	
	//private final ClientNotifier clientnotifier = new ClientNotifier(JMSConstants.TOPICNAME_RESOURCECACHE);
	
	private static final File resourceDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.RESOURCE_PATH);
	
	private static ResourceCache INSTANCE;
	
	//
	
	private final Map<ResourceVO, byte[]> mpResources = new ConcurrentHashMap<ResourceVO, byte[]>();
	
	private final Object lock = new Object();
	
	private DataBaseHelper dataBaseHelper;
	
	public static ResourceCache getInstance() {
		return INSTANCE;
	}
	
	ResourceCache() {
		INSTANCE = this;
	}
	
	@Autowired
	void setDataBaseHelper(DataBaseHelper dataBaseHelper) {
		this.dataBaseHelper = dataBaseHelper;
	}
	
	@PostConstruct
	final void init() {
		synchronized (lock) {
			this.mpResourcesByName = buildMap();
			this.mpResourcesById = buildIdMap();
		}
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
		DbQueryBuilder builder = dataBaseHelper.getDbAccess().getQueryBuilder();
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
		for (DbTuple tuple : dataBaseHelper.getDbAccess().executeQuery(query)) {
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
		synchronized (lock) {
			if (mpResourcesByName.isEmpty()) {
				mpResourcesByName = buildMap();
			}
		}
		return mpResourcesByName.get(sResourceName);
	}
	
	public ResourceVO getResourceById(Integer iResourceId) {
		synchronized (lock) {
			if (mpResourcesById.isEmpty()) {
				mpResourcesById = buildIdMap();
			}
		}
		return mpResourcesById.get(iResourceId);
	}
	
	/**
	 * @param sResourceName
	 * @return the resource as <code>byte[]</code>
	 */
	public byte[] getResource(String sResourceName) {
		synchronized (lock) {
			if (mpResourcesByName.isEmpty()) {
				mpResourcesByName = buildMap();
			}
		}
		ResourceVO resourcevo = mpResourcesByName.get(sResourceName);
		if (resourcevo == null) {
			return null;
		}
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
	
	public Set<String> getResourceNames() {
		synchronized (lock) {
			if (mpResourcesByName.isEmpty()) {
				mpResourcesByName = buildMap();
			}
		}
		return new HashSet<String>(mpResourcesByName.keySet());
	}
	
	public void invalidate() {
		synchronized (lock) {
			this.mpResourcesByName.clear();
			this.mpResourcesById.clear();
			this.mpResources.clear();
		}
		
		this.notifyClients();	
	}
	
	private void notifyClients() {
		NuclosJMSUtils.sendMessage(null, JMSConstants.TOPICNAME_RESOURCECACHE);
	}
}
