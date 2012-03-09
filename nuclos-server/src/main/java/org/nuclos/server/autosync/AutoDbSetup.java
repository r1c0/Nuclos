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
package org.nuclos.server.autosync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.ApplicationProperties.Version;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common.collection.Pair;
import org.nuclos.common.collection.Transformer;
import org.nuclos.common.dal.vo.SystemFields;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.dal.provider.SystemMetaDataProvider;
import org.nuclos.server.dblayer.DbAccess;
import org.nuclos.server.dblayer.DbException;
import org.nuclos.server.dblayer.DbStatementUtils;
import org.nuclos.server.dblayer.DbTuple;
import org.nuclos.server.dblayer.EntityObjectMetaDbHelper;
import org.nuclos.server.dblayer.expression.DbCurrentDate;
import org.nuclos.server.dblayer.expression.DbCurrentDateTime;
import org.nuclos.server.dblayer.expression.DbId;
import org.nuclos.server.dblayer.expression.DbNull;
import org.nuclos.server.dblayer.impl.SchemaUtils;
import org.nuclos.server.dblayer.query.DbFrom;
import org.nuclos.server.dblayer.query.DbQuery;
import org.nuclos.server.dblayer.query.DbQueryBuilder;
import org.nuclos.server.dblayer.statements.DbStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;
import org.nuclos.server.dblayer.structure.DbTableData;
import org.nuclos.server.dblayer.structure.DbTableType;
import org.nuclos.server.dblayer.util.DbArtifactXmlReader;
import org.nuclos.server.dblayer.util.DbArtifactXmlWriter;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Configurable
public class AutoDbSetup {

	private static final String NUCLOS = "NUCLOS";

	public static class Schema extends Pair<Collection<DbArtifact>, Collection<DbTableData>> {

		public Schema(Collection<DbArtifact> artifacts, Collection<DbTableData> dumps) {
			super(artifacts, dumps);

		}
		public Collection<DbArtifact> getArtifacts() {
			return x;
		}

		public Collection<DbTableData> getDumps() {
			return y;
		}
	}


	public static class ReleaseInfo {

		private String release;
		private Date releaseDate;
		private String schemaversion;
		private Schema schema;

		public ReleaseInfo(String release, Date releaseDate, String schemaversion, Schema schema) {
			this.release = release;
			this.releaseDate = releaseDate;
			this.schemaversion = schemaversion;
			this.schema = schema;
		}

		public String getReleaseString() {
			return release;
		}

		public Date getReleaseDate() {
			return releaseDate;
		}

		public String getSchemaVersion() {
			return schemaversion;
		}

		public Schema getSchema() {
			return schema;
		}
	}


	private final DbAccess dbAccess;
	private Schema schema;

	public AutoDbSetup(DbAccess dbAccess) {
		this.dbAccess = dbAccess;
	}

	/**
	 * Checks whether Nuclos is already set-up on the given database.
	 */
	public boolean checkIsInstalled() {
		// we check for the T_AD_RELEASE (ignore case) table
		TreeSet<String> tableNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		tableNames.addAll(dbAccess.getTableNames(DbTableType.TABLE));
		return tableNames.contains("T_AD_RELEASE");
	}

	public Pair<String, Date> determineLastVersion() {
		return CollectionUtils.getFirst(getInstalledVersions());
	}

	@Transactional(propagation=Propagation.REQUIRES_NEW, noRollbackFor= {Exception.class})
	public List<Pair<String, Date>> getInstalledVersions() {
		DbQueryBuilder builder = dbAccess.getQueryBuilder();
		DbQuery<DbTuple> query = builder.createTupleQuery();
		DbFrom t = query.from("T_AD_RELEASE").alias(SystemFields.BASE_ALIAS);
		query.multiselect(
			t.baseColumn("STRRELEASE", String.class),
			t.baseColumn("DATDELIVERED", Date.class));
		query.where(builder.equal(t.baseColumn("STRAPPLICATION", String.class), NUCLOS));
		query.orderBy(
			builder.desc(t.baseColumn("STRRELEASE", String.class)));
		return dbAccess.executeQuery(query, new Transformer<DbTuple, Pair<String, Date>>() {
			@Override
			public Pair<String, Date> transform(DbTuple tuple) {
				String releaseString = tuple.get(0, String.class);
				Date releaseDate = tuple.get(1, Date.class);
				return Pair.makePair(releaseString, releaseDate);
			}
		});
	}

	public Schema getInstalledSchemaFor(String release) {
		DbQueryBuilder builder = dbAccess.getQueryBuilder();
		DbQuery<byte[]> query = builder.createQuery(byte[].class);
		DbFrom t = query.from("T_AD_RELEASE").alias(SystemFields.BASE_ALIAS);
		query.select(t.baseColumn("BLBDATA", byte[].class));
		query.where(builder.and(
			builder.equal(t.baseColumn("STRAPPLICATION", String.class), NUCLOS),
			builder.equal(t.baseColumn("STRRELEASE", String.class), release)));
		byte[] b = CollectionUtils.getFirst(dbAccess.executeQuery(query));
		if (b != null && b.length > 0) {
			return uncompressSchema(b);
		}
		return null;
	}

	/**
	 * Returns the initial database schema definition and data for Nuclos for the
	 * given database.
	 * The complete schema is assembled from different sources: a general schema
	 * definition, database-specific amendments (e.g. functions and procedures),
	 * schema definitions and data for Quartz (which are also database-specific),
	 * schema definitions derived from current metadata of the system entities,
	 * and the data for the initial user.
	 */
	public Schema getCurrentSchema() {
		if (schema == null) {
			String typeId = StringUtils.toLowerCase(dbAccess.getDbType().name());
			String[] resources = {
				"resources/db/nuclos-base.xml",
				"resources/db/nuclos-user.xml",
				String.format("resources/db/%1$s/nuclos-%1$s.xml", typeId),
				String.format("resources/db/%1$s/quartz-%1$s.xml", typeId)
			};

			DbArtifactXmlReader reader = new DbArtifactXmlReader();
			for (String res : resources) {
				InputStream is = getClass().getClassLoader().getResourceAsStream(res);
				if (is == null)
					throw new NuclosFatalException("Initial schema resource " + res + " not found");
				try {
					reader.read(is);
				} catch (IOException ex) {
					throw new NuclosFatalException("Error loading schema resource " + res, ex);
				}
			}
			List<DbArtifact> artifacts = reader.getArtifacts();
			List<DbTableData> dumps = reader.getDumps();

			//
			SystemMetaDataProvider provider = SystemMetaDataProvider.getInstance();
			EntityObjectMetaDbHelper eoHelper = new EntityObjectMetaDbHelper(dbAccess, provider);
			artifacts.addAll(eoHelper.getSchema().values());

			schema = new Schema(artifacts, dumps);
		}
		return schema;
	}

	public ReleaseInfo getCurrentRelease() {
		Version version = ApplicationProperties.getInstance().getNuclosVersion();
		return new ReleaseInfo(version.getVersionNumber(), version.getVersionDate(), version.getSchemaVersion(), getCurrentSchema());
	}

	public List<DbStatement> getSetupStatements() {
		List<DbStatement> statements = new ArrayList<DbStatement>();
		ReleaseInfo releaseInfo = getCurrentRelease();
		statements.addAll(SchemaUtils.create(releaseInfo.getSchema().getArtifacts()));
		for (DbTableData data : releaseInfo.getSchema().getDumps()) {
			statements.addAll(data.getStatements(false));
		}
		statements.add(getReleaseStatement(releaseInfo));
		return statements;
	}

	public List<DbStatement> getUpdateStatementsSince(String release) {
		ReleaseInfo releaseInfo = getCurrentRelease();
		if (releaseInfo.getSchemaVersion().equals(release))
			return null;

		// Auto-update only considers schema artifacts and _not_ initial data
		Schema installedSchema = getInstalledSchemaFor(release);
		List<DbStatement> statements = new ArrayList<DbStatement>();
		statements.addAll(SchemaUtils.modify(
			installedSchema.getArtifacts(),
			releaseInfo.schema.getArtifacts()));
		statements.add(getReleaseStatement(releaseInfo));
		return statements;
	}

	public static DbStatement getReleaseStatement(ReleaseInfo releaseInfo) {
		String releaseString = releaseInfo.getReleaseString();
		Date releaseDate = releaseInfo.getReleaseDate();
		if (releaseString == null) {
			Version version = ApplicationProperties.getInstance().getNuclosVersion();
			releaseString = version.getVersionNumber();
		}
		return DbStatementUtils.insertInto("T_AD_RELEASE",
			"INTID", new DbId("IDFACTORY"),
			"STRAPPLICATION", NUCLOS,
			"STRRELEASE", releaseInfo.getSchemaVersion(),
			"STRDESCRIPTION", "Nuclos " + releaseString,
			"DATDELIVERED", releaseDate != null ? releaseDate : DbCurrentDate.CURRENT_DATE,
			"DATINSTALLED", DbCurrentDate.CURRENT_DATE,
			"DATCREATED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCREATED", "AUTOSETUP",
			"DATCHANGED", DbCurrentDateTime.CURRENT_DATETIME,
			"STRCHANGED", "AUTOSETUP",
			"INTVERSION", 1,
			"BLBDATA", releaseInfo.getSchema() != null ? compressSchema(releaseInfo.getSchema()) : DbNull.forType(byte[].class));
	}

	public static byte[] compressSchema(Schema schema) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			GZIPOutputStream gzos = new GZIPOutputStream(baos);
			writeSchema(schema, gzos);
			gzos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}

	public static Schema uncompressSchema(byte[] b) {
		DbArtifactXmlReader reader = new DbArtifactXmlReader();
		try {
			GZIPInputStream gzin = new GZIPInputStream(new ByteArrayInputStream(b));
			reader.read(gzin);
			gzin.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new Schema(reader.getArtifacts(), reader.getDumps());
	}

	public static void writeSchema(Schema schema, OutputStream os) throws IOException {
		DbArtifactXmlWriter writer = new DbArtifactXmlWriter(os);
		writer.writeArtifacts(schema.getArtifacts());
		writer.writeDumps(schema.getDumps());
		writer.close();
	}

	public void setup() throws DbException {
		dbAccess.execute(getSetupStatements());
	}
}
