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
package org.nuclos.server.resource.ejb3;

import java.io.File;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.Pair;
import org.nuclos.common2.IOUtils;
import org.nuclos.common2.exception.CommonCreateException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonPermissionException;
import org.nuclos.common2.exception.CommonRemoveException;
import org.nuclos.common2.exception.CommonStaleVersionException;
import org.nuclos.common2.exception.CommonValidationException;
import org.nuclos.server.common.NuclosSystemParameters;
import org.nuclos.server.masterdata.ejb3.MasterDataFacadeBean;
import org.nuclos.server.masterdata.valueobject.DependantMasterDataMap;
import org.nuclos.server.masterdata.valueobject.MasterDataVO;
import org.nuclos.server.resource.ResourceCache;
import org.nuclos.server.resource.valueobject.ResourceFile;
import org.nuclos.server.resource.valueobject.ResourceVO;
import org.nuclos.server.ruleengine.NuclosBusinessRuleException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resource facade bean.
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 */
@Stateless
@Remote(ResourceFacadeRemote.class)
@Transactional
public class ResourceFacadeBean extends MasterDataFacadeBean implements ResourceFacadeRemote {

	/**
	 * @param sResourceName
	 */
	@Override
    @RolesAllowed("Login")
	public ResourceVO getResourceByName(String sResourceName) {
		return ResourceCache.getInstance().getResourceByName(sResourceName);
	}
	

	/**
	 * @param sResourceId
	 */
	@Override
    @RolesAllowed("Login")
	public ResourceVO getResourceById(Integer iResourceId) {
		return ResourceCache.getInstance().getResourceById(iResourceId);
	}

	@Override
    @RolesAllowed("Login")
	public Pair<ResourceVO, byte[]> getResource(String resourceName) {
		Pair<ResourceVO, byte[]> res = new Pair<ResourceVO, byte[]>();
		ResourceCache rcinst = ResourceCache.getInstance();
		res.x = rcinst.getResourceByName(resourceName);
		res.y = ResourceCache.getInstance().getResource(resourceName);
		return res;
	}
	

	@Override
	@RolesAllowed("Login")
	public Pair<ResourceVO, byte[]> getResource(Integer resourceId) {
		Pair<ResourceVO, byte[]> res = new Pair<ResourceVO, byte[]>();
		ResourceCache rcinst = ResourceCache.getInstance();
		res.x = rcinst.getResourceById(resourceId);
		res.y = ResourceCache.getInstance().getResource(res.x.getName());
		return res;
	}
	

	@Override
	public MasterDataVO create(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonCreateException, CommonPermissionException, NuclosBusinessRuleException {

		final ResourceFile resourceFile = (ResourceFile)mdvo.getField("file");
		if (resourceFile != null && resourceFile.getContents() != null ) {
			storeResource(resourceFile);
		}
		return super.create(sEntityName, mdvo, mpDependants);
	}

	@Override
	public Object modify(String sEntityName, MasterDataVO mdvo, DependantMasterDataMap mpDependants)
			throws CommonCreateException, CommonFinderException, CommonRemoveException, CommonStaleVersionException,
				CommonValidationException, CommonPermissionException, NuclosBusinessRuleException {

		final ResourceFile resourceFile = (ResourceFile)mdvo.getField("file");
		if (resourceFile != null && resourceFile.getContents() != null ) {
			storeResource(resourceFile);
		}
		return super.modify(sEntityName, mdvo, mpDependants);
	}

	@Override
    public void remove(String sEntityName, MasterDataVO mdvo)throws CommonFinderException, CommonRemoveException,
			CommonStaleVersionException, CommonPermissionException, CommonCreateException, NuclosBusinessRuleException {

		final ResourceFile resourceFile = (ResourceFile)mdvo.getField("file");
		if (resourceFile != null) {
			removeResource(resourceFile.getFilename());
		}
		super.remove(sEntityName, mdvo, true);
	}


	public static void storeResource(ResourceFile resourceFile) {
		removeResource(resourceFile.getFilename());
		try {
			IOUtils.writeToBinaryFile(new java.io.File(getResourcePathName(resourceFile)), resourceFile.getContents());
		}catch (java.io.IOException e) {
			throw new NuclosFatalException("File content cannot be updated for new file (" + e.getMessage() + ").");
		}
	}

	/**
	 * remove the resource with the given id
	 * @param iFileId
	 */
	public static void removeResource(String sFileName) {
		File resourceDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.RESOURCE_PATH);
		final java.io.File file = new java.io.File(resourceDir, sFileName);
		if(file.exists()) {
			file.delete();
		}
	}

	private static String getResourcePathName(ResourceFile resourceFile) {
		try {
			if(resourceFile == null) {
				throw new CommonFatalException("resource.error.invalid.file");//"Der Parameter resourceFile darf nicht null sein.");
			}
			// @todo introduce symbolic constant
			File resourceDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.RESOURCE_PATH);
			if (resourceDir == null) {
				throw new CommonFatalException("resource.error.invalid.path");//"Der Paramater 'Resource Path' darf nicht null sein");
			}

			java.io.File file = new java.io.File(resourceDir, resourceFile.getFilename());
			Logger.getLogger(ResourceFacadeBean.class).debug("Calculated path for resource: " + file.getCanonicalPath());
			return file.getCanonicalPath();
		}
		catch (java.io.IOException e) {
			throw new NuclosFatalException(e);
		}
	}

	/**
	 * get the file content of a resource file
	 * @param iResourceId
	 * @param sFileName
	 * @return resource file content
	 * @throws CommonFinderException
	 */
	@Override
    @RolesAllowed("Login")
	public byte[] loadResource(Integer iResourceId, String sFileName) throws CommonFinderException {
		File resourceDir = NuclosSystemParameters.getDirectory(NuclosSystemParameters.RESOURCE_PATH);

		if (iResourceId == null) {
			throw new NuclosFatalException("resource.error.invalid.id");//"Die Id der Ressource darf nicht null sein");
		}

		java.io.File file = new java.io.File(resourceDir, sFileName);
		try {
			return IOUtils.readFromBinaryFile(file);
		}
		catch(java.io.IOException e) {
			throw new NuclosFatalException(e);
		}
	}
}
