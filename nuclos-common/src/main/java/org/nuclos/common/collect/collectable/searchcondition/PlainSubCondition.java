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
package org.nuclos.common.collect.collectable.searchcondition;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * <code>CollectableSearchCondition</code> that ontaines a sub select condition as a plain SQL text.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Rostislav.Maksymovskyi@novabit.de">Rostislav Maksymovskyi</a>
 * @version	01.00.00
 */
public class PlainSubCondition implements CollectableSearchCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String plainSQL;
	private String conditionName;
	
	public PlainSubCondition(String pPlainSQL){
		this(pPlainSQL, null);
	}

	public PlainSubCondition(String pPlainSQL, String pConditionName){
		this.plainSQL = pPlainSQL;
		this.conditionName = pConditionName;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public int getType() {
		return TYPE_SUB;
	}

	@Override
	public boolean isSyntacticallyCorrect() {
		return (this.plainSQL != null);
	}

	public String getPlainSQL() {
		return plainSQL;
	}

	public void setPlainSQL(String pPlainSQL) {
		this.plainSQL = pPlainSQL;
	}
	
	@Override
    public String getConditionName() {
		return conditionName;
	}

	// --------------------------------------------------------
	
	@Override
	@SuppressWarnings("unchecked")
	public <O, Ex extends Exception> O accept(Visitor<O, Ex> visitor) throws Ex {
		if(visitor instanceof CompositeVisitor<?, ?>){
			return accept((CompositeVisitor<O, Ex>)visitor);
		} 
		return null;
	}

	public <O, Ex extends Exception> O accept(CompositeVisitor<O, Ex> visitor) throws Ex {
		return visitor.visitPlainSubCondition(this);
	}

	// --------------------------------------------------------
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return null;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return false;
	}

	@Override
	public Object getTransferData(DataFlavor arg0)
			throws UnsupportedFlavorException, IOException {
		throw new UnsupportedFlavorException(arg0);
	}

	@Override
    public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
    }
	
	@Override
	public String toString() {
		return getClass().getName() + ":" + getConditionName() + ":" + plainSQL;
	}
	
}
