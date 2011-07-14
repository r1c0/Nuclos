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
package org.nuclos.client.ui.multiaction;

import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common2.CommonLocaleDelegate;
import static org.nuclos.common2.CommonLocaleDelegate.getMessage;


import java.util.Collection;


public abstract class MultiActionProgressResultHandler implements
		IMultiActionProgressResultHandler {

	protected final CollectController<?> controller;
	
	public MultiActionProgressResultHandler(CollectController<?> pCollectController){
		this.controller = pCollectController;
	}
	
	@Override
	public String getSuccessLabel() {
		return CommonLocaleDelegate.getMessage("MultiActionProgressResultHandler.3","erfolgreich");
	}

	@Override
	public String getExceptionLabel() {
		return CommonLocaleDelegate.getMessage("MultiActionProgressResultHandler.4","nicht erfolgreich");
	}

	@Override
	public String getStateHeaderLabel() {
		return CommonLocaleDelegate.getMessage("MultiActionProgressResultHandler.2","Aktionstatus");
	}

	@Override
	public String getSingleSelectionMenuLabel() {
		return getMessage("RuleExplorerNode.1","Details anzeigen");
	}

	@Override
	public String getMultiSelectionMenuLabel() {
		return getMessage("RuleExplorerNode.1","Details anzeigen");
	}

	@Override
	public abstract void handleMultiSelection(Collection<Integer> collLeasedObjectIds);

}