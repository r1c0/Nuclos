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
package org.nuclos.installer.database;

public class PostgresService implements Comparable<PostgresService> {

	public String version;
	public String serviceId;
	public Integer port;
	public String superUser;
	public String baseDirectory;
	public String dataDirectory;

	@Override
	public int compareTo(PostgresService s) {
		return version.compareToIgnoreCase(s.version);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Service [");
		if(version != null) {
			builder.append("version=");
			builder.append(version);
			builder.append(", ");
		}
		if(serviceId != null) {
			builder.append("serviceId=");
			builder.append(serviceId);
			builder.append(", ");
		}
		if(port != null) {
			builder.append("port=");
			builder.append(port);
			builder.append(", ");
		}
		if(superUser != null) {
			builder.append("superUser=");
			builder.append(superUser);
			builder.append(", ");
		}
		if(baseDirectory != null) {
			builder.append("baseDirectory=");
			builder.append(baseDirectory);
			builder.append(", ");
		}
		if(dataDirectory != null) {
			builder.append("dataDirectory=");
			builder.append(dataDirectory);
		}
		builder.append("]");
		return builder.toString();
	}
}
