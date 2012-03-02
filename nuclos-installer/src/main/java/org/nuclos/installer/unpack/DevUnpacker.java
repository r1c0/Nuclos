package org.nuclos.installer.unpack;

import java.io.File;
import java.util.List;

import org.nuclos.installer.ConfigContext;
import org.nuclos.installer.Constants;
import org.nuclos.installer.InstallException;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.mode.Installer;

public class DevUnpacker implements Unpacker, Constants {

	@Override
	public String getDefaultValue(String key) {
		return null;
	}

	@Override
	public void validate(String key, String value) throws InstallException {
		if (NUCLOS_HOME.equals(key)) {
			File nucloshome = new File(value);
			File nuclosXml = new File(nucloshome, NUCLOS_XML);
			if (nuclosXml.isFile()) {
				ConfigContext.update(nuclosXml);
			}
		}
	}

	@Override
	public boolean isPrivileged() {
		return false;
	}

	@Override
	public boolean canInstall() {
		return isPrivileged();
	}
	
	@Override
	public boolean isProductRegistered() {
		return false;
	}

	@Override
	public boolean isPostgresBundled() {
		return false;
	}

	@Override
	public List<PostgresService> getPostgresServices() {
		return null;
	}

	@Override
	public boolean isServerRunning() {
		return false;
	}

	@Override
	public void shutdown(Installer cb) throws InstallException { }

	@Override
	public void unpack(Installer cb) throws InstallException {
		try {
			String dbsetup = ConfigContext.getProperty(DATABASE_SETUP);

			if (DBOPTION_INSTALL.equals(dbsetup) || DBOPTION_SETUP.equals(dbsetup)) {
				AbstractUnpacker.setupDatabase(cb);
			}
			AbstractUnpacker.createConfDirectory(cb);
			AbstractUnpacker.createDataAndLogsDirectory();
		}
		catch (Exception ex) {
			throw new InstallException(ex);
		}
	}

	@Override
	public void rollback(Installer cb) throws InstallException { }

	@Override
	public void remove(Installer cb) throws InstallException { }

	@Override
	public void startup(Installer cb) throws InstallException { }
}
