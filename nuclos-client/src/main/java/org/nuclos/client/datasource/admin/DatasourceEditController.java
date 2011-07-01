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
package org.nuclos.client.datasource.admin;

import java.awt.Component;
import java.util.Set;

import org.nuclos.client.ui.CommonClientWorker;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.report.valueobject.ResultVO;

public interface DatasourceEditController {

	public String generateSql() throws CommonBusinessException;

	public Component getFrame();

	public void detailsChanged(Component c);

	public ResultVO cmdExecuteCurrentStatement(Integer iMaxRowCount);
	
	public void execute(final CommonClientWorker worker);
	
	public Set<String> getQueryTypes();
	
	public boolean isWithParameterEditor();
	
	public boolean isParameterEditorWithValuelistProviderColumn();
	
	public boolean isParameterEditorWithLabelColumn();

}
