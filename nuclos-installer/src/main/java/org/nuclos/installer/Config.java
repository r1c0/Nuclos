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

package org.nuclos.installer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Properties;

import org.nuclos.installer.database.DbType;
import org.nuclos.installer.database.PostgresService;
import org.nuclos.installer.unpack.Unpacker;
import org.nuclos.installer.util.ConfigFile;
import org.nuclos.installer.util.XmlUtils;
import org.xml.sax.SAXException;

public class Config extends Properties implements Constants {

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException, InvalidPropertiesFormatException {
        this.clear();
        ConfigFile config = new ConfigFile();
        try {
            config.loadXml(XmlUtils.readDocument(in));
            this.putAll(config.getProperties());
            if (containsKey(DATABASE_ADAPTER)) {
                DbType type = DbType.findType(getProperty("database.adapter"));
                if (type != null) {
                    type.parseJdbcConnectionString(config.getProperties().get("database.connection.url"), this);
                }
            }
            migrate();
        }
        catch(SAXException e) {
            throw new InvalidPropertiesFormatException(e);
        }
    }

    public File getFileProperty(String property) {
        return new File(getProperty(property));
    }

    public void setDefaults(Unpacker unpacker) {
        ensurePropertyDefault(this, NUCLOS_HOME, unpacker.getDefaultValue(NUCLOS_HOME));
        ensurePropertyDefault(this, JAVA_HOME, unpacker.getDefaultValue(JAVA_HOME));
        ensurePropertyDefault(this, NUCLOS_INSTANCE, unpacker.getDefaultValue(NUCLOS_INSTANCE));
        ensurePropertyDefault(this, HTTP_ENABLED, unpacker.getDefaultValue(HTTP_ENABLED));
        ensurePropertyDefault(this, HTTP_PORT, unpacker.getDefaultValue(HTTP_PORT));
        ensurePropertyDefault(this, HTTPS_ENABLED, unpacker.getDefaultValue(HTTPS_ENABLED));
        ensurePropertyDefault(this, HTTPS_PORT, unpacker.getDefaultValue(HTTPS_PORT));
        ensurePropertyDefault(this, SHUTDOWN_PORT, unpacker.getDefaultValue(SHUTDOWN_PORT));
        ensurePropertyDefault(this, HEAP_SIZE, unpacker.getDefaultValue(HEAP_SIZE));
        ensurePropertyDefault(this, CLIENT_SINGLEINSTANCE, unpacker.getDefaultValue(CLIENT_SINGLEINSTANCE));
    }

    public void setDbDefaults(Unpacker unpacker, String dboption) {
        ensurePropertyDefault(this, DATABASE_ADAPTER, unpacker.getDefaultValue(DATABASE_ADAPTER));
        ensurePropertyDefault(this, DATABASE_SERVER, unpacker.getDefaultValue(DATABASE_SERVER));
        ensurePropertyDefault(this, DATABASE_NAME, unpacker.getDefaultValue(DATABASE_NAME));
        ensurePropertyDefault(this, DATABASE_USERNAME, unpacker.getDefaultValue(DATABASE_USERNAME));
        ensurePropertyDefault(this, DATABASE_PASSWORD, unpacker.getDefaultValue(DATABASE_PASSWORD));
        ensurePropertyDefault(this, DATABASE_SCHEMA, unpacker.getDefaultValue(DATABASE_SCHEMA));

        if (DBOPTION_INSTALL.equals(dboption)) {
            ensurePropertyDefault(this, POSTGRES_PREFIX, unpacker.getDefaultValue(POSTGRES_PREFIX));
            ensurePropertyDefault(this, POSTGRES_DATADIR, unpacker.getDefaultValue(POSTGRES_DATADIR));
            ensurePropertyDefault(this, POSTGRES_TABLESPACEPATH, unpacker.getDefaultValue(POSTGRES_TABLESPACEPATH));
            ensurePropertyDefault(this, POSTGRES_SUPERUSER, unpacker.getDefaultValue(POSTGRES_SUPERUSER));
            ensurePropertyDefault(this, DATABASE_PORT, unpacker.getDefaultValue(DATABASE_PORT));
        }
        else {
            List<PostgresService> pgservices = unpacker.getPostgresServices();
            if (pgservices != null && pgservices.size() > 0) {
                PostgresService latest = pgservices.get(pgservices.size() - 1);
                ensurePropertyDefault(this, DATABASE_PORT, String.valueOf(latest.port));
                if (DBOPTION_SETUP.equals(dboption)) {
                    ensurePropertyDefault(this, POSTGRES_TABLESPACEPATH, latest.dataDirectory);
                    ensurePropertyDefault(this, POSTGRES_SUPERUSER, latest.superUser);
                }
            }
        }
    }

    public void migrate() {
        if (containsKey("nuclos.home") && !containsKey("server.home")) {
            put("server.home", get("nuclos.home"));
        }
        if (containsKey("java.home") && !containsKey("server.java.home")) {
            put("server.java.home", get("java.home"));
        }
        if (containsKey("tomcat.server.name") && !containsKey("server.name")) {
            put("server.name", get("tomcat.server.name"));
        }
        if (containsKey("tomcat.web.http.port") && !containsKey("server.http.port")) {
            put("server.http.port", get("tomcat.web.http.port"));
        }
        if (containsKey("webstart.singleinstance") && !containsKey("client.singleinstance")) {
            put("client.singleinstance", get("webstart.singleinstance"));
        }
    }

    /**
     * Verifies all parameters and ensures that the set is consistent.
     */
    public void verify() throws InstallException {
        // App-id (may differ for nuclets)
        ensurePropertyDefault(this, "server.name", "nuclos");

        // Database defaults
        DbType type = DbType.findType(getProperty("database.adapter"));
        if (type == null) {
            throw new InstallException("error.illegal.dbadapter", getProperty("database.adapter"));
        }
        ensurePropertyDefault(this, "database.driver", type.getDriverClassName());
        ensurePropertyDefault(this, "database.driverjar", "");
        ensurePropertyDefault(this, "database.connection.url", type.buildJdbcConnectionString(this));
        ensurePropertyDefault(this, "database.tablespace", "");
        ensurePropertyDefault(this, "database.tablespace.index", "");
        ensurePropertyDefault(this, "client.singleinstance", "false");

        // Quartz
        // - Start with the "defaults" (note that the empty values are interpreted as their defaults by Quartz)
        this.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        this.setProperty("org.quartz.jobStore.selectWithLockSQL", "");
        this.setProperty("org.quartz.jobStore.txIsolationLevelSerializable", "");
        if (type == DbType.POSTGRESQL) {
            this.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        } else  if (type == DbType.MSSQL || type == DbType.SYBASE) {
            this.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.MSSQLDelegate");
            this.setProperty("org.quartz.jobStore.selectWithLockSQL", "SELECT * FROM {0}LOCKS UPDLOCK WHERE LOCK_NAME = ?");
            this.setProperty("org.quartz.jobStore.txIsolationLevelSerializable", "true");
        }

        // Heap size
        ensurePropertyDefault(this, "server.heap.size", "1024");
        ensurePropertyDefault(this, "server.launch.on.startup", "false");

        // Protocol settings
        ensurePropertyDefault(this, "server.http.enabled", "true");
        ensurePropertyDefault(this, "server.http.port", "80");
        ensurePropertyDefault(this, "server.https.enabled", "false");
        ensurePropertyDefault(this, "server.https.port", "443");
        ensurePropertyDefault(this, "server.https.keystore.file", "");
        ensurePropertyDefault(this, "server.https.keystore.password", "");

        File nuclosHome = new File(getProperty(NUCLOS_HOME)).getAbsoluteFile();
        File nuclosWeb = new File(nuclosHome, "webapp");
        File nuclosClient = new File(nuclosHome, "client");
        File nuclosData = new File(nuclosHome, "data");
        File nuclosTomcat = new File(nuclosHome, "tomcat");
        File nuclosCatalina = new File(nuclosTomcat, TOMCAT_VERSION);

        // Make some path properties absolute
        put("server.home", nuclosHome.getAbsolutePath());
        put("server.webapp.dir", nuclosWeb.getAbsolutePath());
        put("server.client.dir", nuclosClient.getAbsolutePath());
        put("server.data.dir", nuclosData.getAbsolutePath());
        put("server.tomcat.dir", nuclosCatalina.getAbsolutePath());
    }

    public void setDerivedProperties() throws InstallException {
        DbType type = DbType.findType(getProperty("database.adapter"));
        this.setProperty("database.driver", type.getDriverClassName());
        this.setProperty("database.connection.url", type.buildJdbcConnectionString(this));
    }

    private static void ensurePropertyDefault(Properties props, String key, String def) {
        if (isPropertyUnset(props, key) && def != null) {
            props.setProperty(key, def);
        }
    }

    public static boolean isPropertyUnset(Properties props, String key) {
        return isUnset(props.getProperty(key));
    }

    public static boolean isUnset(String s) {
        return s == null || s.trim().isEmpty();
    }
}
