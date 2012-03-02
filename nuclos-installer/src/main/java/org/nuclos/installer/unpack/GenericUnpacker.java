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
package org.nuclos.installer.unpack;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.mode.Installer;

/**
 * <code>GenericUnpacker</code> performs unpacking actions that are common to
 * all operating systems. Use this unpacker for unknown operating systems or installations without administration privilegs.
 * No OS-specific installation actions (product and service registration, auto-startup, postgres installation) be performed.<br>
 * <br>
 * Created by Novabit Informationssysteme GmbH <br>
 * Please visit <a href="http://www.nuclos.de">www.nuclos.de</a>
 */
public class GenericUnpacker extends AbstractUnpacker {

	private static final Logger log = Logger.getLogger(GenericUnpacker.class);

	// The delegate unpacker should be set if GenericUnpacker is just a fallback (param -d or no admin privileges)
	private final Unpacker delegate;

	public GenericUnpacker() {
		this(null);
	}

	public GenericUnpacker(Unpacker delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isPrivileged() {
		return false;
	}

	@Override
	public boolean isProductRegistered() {
		if (delegate != null) {
			return delegate.isProductRegistered();
		}
		return false;
	}

	@Override
	public boolean isPostgresBundled() {
		return false;
	}

	@Override
	public boolean canInstall() {
		return isPrivileged();
	}
	
	@Override
	public List<PostgresService> getPostgresServices() {
		return new ArrayList<PostgresService>();
	}

	@Override
	public void shutdown(Installer cb) throws InstallException {
		log.info("Skipping shutdown.");
	}

	@Override
	public void startup(Installer cb) throws InstallException {
		log.info("Skipping startup.");
	}

	@Override
	public void installPostgres(Installer cb) throws InstallException {
		throw new UnsupportedOperationException("error.generic.installdb");
	}

	@Override
	public void register(Installer cb, boolean systemlaunch) throws InstallException {
		log.info("Skipping product registration.");
	}

	@Override
	public void unregister(Installer cb) throws InstallException {
		log.info("Skipping product unregistration.");
	}
}
