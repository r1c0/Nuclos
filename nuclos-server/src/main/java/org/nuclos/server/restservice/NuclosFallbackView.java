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
package org.nuclos.server.restservice;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.SmartView;
import org.springframework.web.servlet.view.AbstractView;

public class NuclosFallbackView extends AbstractView implements SmartView {

	String sMessage;
	
	public NuclosFallbackView() {
		this.sMessage = "Es ist ein interner Fehler aufgetreten!";
	}
	
	public NuclosFallbackView(String sMessage) {
		this.sMessage = sMessage;
	}
	
	public NuclosFallbackView(Exception ex) {
		this.sMessage = ex.getMessage();
	}

	@Override
	public boolean isRedirectView() {		
		return true;
	}
	
	@Override
	protected boolean isContextRequired() {
		return false;
	}

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		StringBuffer sHTML = new StringBuffer();
		sHTML.append("<html>\n");
		sHTML.append("<body>\n");
		sHTML.append(this.sMessage);
		sHTML.append("\n");
		sHTML.append("</body>\n");
		sHTML.append("</html>\n");
		response.getOutputStream().write(sHTML.toString().getBytes());
		
	}

}
