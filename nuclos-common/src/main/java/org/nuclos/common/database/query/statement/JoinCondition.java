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
package org.nuclos.common.database.query.statement;

/**
 * JoinCondition Interface
 * 
 * This interface is used by the Join Statements contained in
 * @see org.nuclos.common.database.query.statement.databasespecific
 * 
 * Oracle9i Implementation
 * MSSQL2005 Implementation
 * 
 * Used for independend working in 
 * @see org.nuclos.server.report.ejb.DatasourceFacadeBean
 * 
 * @author hartmut.beckschulze
 *
 */
public interface JoinCondition {

    /** returns the complete join condition */
	@Override
	public String toString() ;

}	// class JoinCondition
