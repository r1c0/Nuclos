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

import org.nuclos.common.database.query.definition.Column;
import org.nuclos.common.database.query.definition.Schema;
import org.nuclos.common.database.query.definition.Table;

/**
 * ansi sql92 specific implementation of the join condition.
 * 
 * 
 * @author hartmut.beckschulze
 *
 */
public class OuterJoinCondition extends Condition implements JoinCondition {
    
    private final Operand left;
    private final ComparisonOperator op;
    private final Operand right;

    public OuterJoinCondition(Operand left, ComparisonOperator op, Operand right) {
	super(null);
	this.left = left;
	this.right = right;
	this.op = op;
    }
    
    /**
     * method for creating a fake table with the join statement as table name
     * 
     * in standard sql the join statement is made in the FROM block
     * 
     * therefore it must be a table object that is passed, otherwise it would not be accepted.
     * 
     * @return
     */
    public Table getOuterJoinTable(){
	
	/** preparing parts of the statement, step by step for better readability */
	Column leftpart = (Column) left.getObject();
	Column rightpart = (Column) right.getObject();
	
	String leftColumnName = leftpart.getTable().getName();
	String rightColumnName = rightpart.getTable().getName();

	String lefttable = leftColumnName + " " + leftpart.getTable().getAlias();

	String righttable = rightColumnName  + " " + rightpart.getTable().getAlias();

	String leftcolumn = leftpart.getTable().getAlias() +".\"" + leftpart.getName() +"\"";
	String rightcolumn = rightpart.getTable().getAlias() +".\"" + rightpart.getName() +"\"";
	
	Table table = new Table(new Schema(), "");
	
	/**
	 * finding out what join statement (left or right outer join)
	 */
	String whatJoin = "";
	
	if (op.equals(ComparisonOperator.LEFT_OUTER_JOIN_OPERATOR))
	    whatJoin = " LEFT OUTER JOIN ";
	if (op.equals(ComparisonOperator.RIGHT_OUTER_JOIN_OPERATOR))
	    whatJoin = " RIGHT OUTER JOIN ";
	
	/** concating the statement */
	// Join statement using JDBC's escape syntax for outer joins
	String statement = "{oj " + lefttable + whatJoin + righttable + " ON " + leftcolumn + " = " + rightcolumn + "}";
	// (similar to the ANSI SQL 92-compliant join but with the "{oj...}" escape braces)

	table.setName(statement);
	table.setAlias("");
	table.setJoin(true);
		
	return table;
    }

    @Override
	public String toString() {
	/** here is no need for join etc... this case is calling getMSSQL2005JoinTable */
	return left.toString() + op.toString() + right.toString();

    }
    
    

} // class OuterJoinCondition
