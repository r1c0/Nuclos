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

package org.nuclos.server.dblayer.util;

import org.nuclos.server.dblayer.statements.DbBatchStatement;
import org.nuclos.server.dblayer.statements.DbDeleteStatement;
import org.nuclos.server.dblayer.statements.DbInsertStatement;
import org.nuclos.server.dblayer.statements.DbPlainStatement;
import org.nuclos.server.dblayer.statements.DbStatementVisitor;
import org.nuclos.server.dblayer.statements.DbStructureChange;
import org.nuclos.server.dblayer.statements.DbUpdateStatement;
import org.nuclos.server.dblayer.structure.DbArtifact;

public class StatementToStringVisitor implements DbStatementVisitor<String> {

	private final ArtifactToStringVisitor artifactToStringVisitor;
	
	public StatementToStringVisitor() {
		artifactToStringVisitor = new ArtifactToStringVisitor();
	}

	@Override
	public String visitDelete(DbDeleteStatement delete) {
		return "Delete from " + delete.getTableName();
	}

	@Override
	public String visitInsert(DbInsertStatement insert) {
		return "Insert into " + insert.getTableName();
	}

	@Override
	public String visitUpdate(DbUpdateStatement update) {
		return "Update " + update.getTableName();
	}

	@Override
	public String visitStructureChange(DbStructureChange structureChange) {
		String type = new String[] {"Drop", "Create", "Alter"}[structureChange.getType().ordinal()];

		DbArtifact artifact = structureChange.getArtifact2();
		if (artifact == null)
			artifact = structureChange.getArtifact1();

		return type + " " + artifact.accept(artifactToStringVisitor);
	}

	@Override
	public String visitPlain(DbPlainStatement command) {
		return String.format("Plain SQL + '%s'", command.getSql());
	}

	@Override
	public String visitBatch(DbBatchStatement batch) {
		return String.format("Batch of %d statements ", batch.getStatements().size());
	}
}
