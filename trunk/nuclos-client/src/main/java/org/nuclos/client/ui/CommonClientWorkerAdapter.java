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
package org.nuclos.client.ui;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.collect.CollectController;
import org.nuclos.common.collect.collectable.Collectable;
import org.nuclos.common2.exception.CommonBusinessException;

/**
 * Convenience class for using the CommonClientWorker.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 * @author	<a href="mailto:uwe.allner@novabit.de">uwe.allner</a>
 * @version 01.00.00
 */
public abstract class CommonClientWorkerAdapter<T extends Collectable> implements CommonClientWorkerSelfExecutable {
	
	private static final Logger LOG = Logger.getLogger(CommonClientWorkerAdapter.class);
	
	private CollectController<T> ctl;

	public CommonClientWorkerAdapter(CollectController<T> ctl) {
		this.ctl = ctl;
	}

	@Override
	public void init() throws CommonBusinessException {
		ctl.lockFrame(true);
	}

	@Override
	public abstract void work() throws CommonBusinessException;

	@Override
	public void paint() throws CommonBusinessException {
		ctl.lockFrame(false);
	}

	@Override
	public JComponent getResultsComponent() {
		return ctl.getFrame();
	}

	@Override
	public void handleError(Exception ex) {
		ctl.forceUnlockFrame();
		if(ctl.getCollectPanel() != null && ctl.getCollectPanel().isShowing()) {
			Errors.getInstance().showExceptionDialog(getResultsComponent(), ex);
		}
		else {
			LOG.error("handleError: " + ex, ex);			
		}
	}
	
	@Override
	public void runInCallerThread() throws CommonBusinessException {
		
		init();
		
		work();
		
		final PaintRunner pr = new PaintRunner();
		SwingUtilities.invokeLater(pr);
			
		if (pr.occurredBusinessException != null)
			throw pr.occurredBusinessException;
		
	}
	
	
	class PaintRunner implements Runnable {
		
		CommonBusinessException occurredBusinessException;

		@Override
		public void run() {
			try {
				paint();
			}
			catch(CommonBusinessException e) {
				LOG.warn("PaintRunner.run failed: " + e, e);
				occurredBusinessException = e;
			}
			catch(Exception e) {
				LOG.error("PaintRunner.run failed: " + e, e);
			}
		}
		
	}
}
