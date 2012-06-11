//Copyright (C) 2012  Novabit Informationssysteme GmbH
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
package org.nuclos.client.customcomp.resplan;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.swing.JComponent;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.nuclos.client.image.ImageType;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.resplan.JResPlanComponent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.ClientPreferences;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author Thomas Pasch
 */
@Configurable
public class ResPlanExportDialog extends AbstractResPlanExportDialog {
	
	private static final Logger LOG = Logger.getLogger(ResPlanExportDialog.class);
	
	private static final String SVG_TEMPLATE = "templates/svg/resplan.svg";
	
	//
	
	private final ResPlanPanel panel;
	
	public ResPlanExportDialog(ResPlanPanel panel, JComponent parent) {
		super(ClientPreferences.getUserPreferences().node("resPlan"), panel.getController().getTitle(), parent);
		this.panel = panel;
	}
	
	@Override
	protected void export() {
		final JResPlanComponent<Collectable, Date, Collectable> resPlan = panel.getResPlan();
		final ImageType imageType = ImageType.getFromFileExtension((String) fileTypes.getSelectedItem());
		final ResPlanExporter exporter = new ResPlanExporter(
				panel.getController().getResourceVO(), panel.getTimeGranularity(), 
				panel.getTimeHorizon(), resPlan.getModel(), resPlan.getTimeModel());
		try {
			exporter.run(SVG_TEMPLATE, 0);
			exporter.save(imageType, save);
		}
		catch (IOException ex) {
			LOG.warn("ResPlan export failed: " + ex.toString(), ex);
			Errors.getInstance().showExceptionDialog(this, "Can' save " + save, ex);
		}
		catch (XPathExpressionException ex) {
			LOG.warn("ResPlan export failed: " + ex.toString(), ex);
			Errors.getInstance().showExceptionDialog(this, "Can' save " + save, ex);
		}
	}
	
}
