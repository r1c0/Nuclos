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

import javax.annotation.security.RolesAllowed;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class NuclosRestService {
	
	NuclosRestWorkerFactory workerFactory;	
	
	private static final Logger LOG = Logger.getLogger(NuclosRestService.class);
	
	@RequestMapping(value = "/nuclosrestservice/{workerclass}/{parameter}", method = RequestMethod.GET)
	public ModelAndView execute(@PathVariable String workerclass, @PathVariable String parameter, Model model) {
		return doExecute(workerclass, parameter);
	}
	
	@RequestMapping(value = "/nuclosrestservicewithauth/{workerclass}/{parameter}", method = RequestMethod.GET)
	@RolesAllowed("Login")
	public ModelAndView executeWithAuth(@PathVariable String workerclass, @PathVariable String parameter, Model model) {
		return doExecute(workerclass, parameter);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private ModelAndView doExecute(String workerclass, Object parameter) {
		LOG.info(workerclass + " " + parameter);
		
		try {
			NuclosRestWorker worker = workerFactory.getRestfulWorker(workerclass);
			worker.setParameter(parameter);
			worker.doWork();		
			
			return worker.getModelAndView();
		}
		catch(NuclosRestServiceException ex) {
			// class not found or similar
			NuclosFallbackView view = new NuclosFallbackView(ex);			
			return new ModelAndView(view);
		}
		catch(Exception ex) {
			// Fallback
			NuclosFallbackView view = new NuclosFallbackView(ex);
			return new ModelAndView(view);
		}
	}
	
	@Autowired
	public void setWorkerFactory(NuclosRestWorkerFactory worker) {
		this.workerFactory = worker;
	}
		
}
