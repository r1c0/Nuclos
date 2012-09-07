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

import java.util.Date;

import javax.swing.JComponent;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.nuclos.client.image.ImageType;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.UIUtils;
import org.nuclos.client.ui.resplan.JResPlanComponent;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.ClientPreferences;

/**
 * A SVG (and more) export dialog for {@link org.nuclos.client.ui.resplan.JResPlanComponent}s.
 * 
 * @author Thomas Pasch
 * @since Nuclos 3.6
 */
public class ResPlanExportDialog2 extends AbstractResPlanExportDialog {
	
	private static final Logger LOG = Logger.getLogger(ResPlanExportDialog2.class);
	
	private static final String SVG_TEMPLATE = "templates/svg/resplan.svg";
	
	//
	
	private final ResPlanPanel panel;
	
	public ResPlanExportDialog2(ResPlanPanel panel, JComponent parent) {
		super(ClientPreferences.getUserPreferences().node("resPlan"), panel.getController().getTitle(), parent);
		this.panel = panel;
	}
	
	@Override
	protected void export() {
		UIUtils.showWaitCursorForFrame(getParent(), true);
		final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				final JResPlanComponent<Collectable, Date, Collectable, Collectable> resPlan = panel.getResPlan();
				final ImageType imageType = ImageType.getFromFileExtension((String) fileTypes.getSelectedItem());
				final ResPlanExporter2 exporter = new ResPlanExporter2(resPlan);
				try {
					exporter.run(SVG_TEMPLATE, 0);
					exporter.save(imageType, save);
				}
				catch (Exception ex) {
					LOG.warn("ResPlan export failed: " + ex.toString(), ex);
					Errors.getInstance().showExceptionDialog(ResPlanExportDialog2.this, "Can' save " + save, ex);
				}
				return null;
			}
			
			@Override
			protected void done() {
				UIUtils.showWaitCursorForFrame(getParent(), false);
			}
			
		};
		worker.execute();
	}
	
}
