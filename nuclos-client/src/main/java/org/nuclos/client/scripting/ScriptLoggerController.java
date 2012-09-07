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
package org.nuclos.client.scripting;

import javax.swing.SwingUtilities;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.nuclos.client.main.mainframe.MainFrameTab;
import org.nuclos.client.ui.MainFrameTabAdapter;
import org.nuclos.client.ui.MainFrameTabController;
import org.nuclos.client.ui.ResultListener;

public class ScriptLoggerController extends MainFrameTabController {

	private final static Logger LOG = Logger.getLogger(ScriptEvaluator.class);

	private final ScriptLoggerView view = new ScriptLoggerView();

	private final Appender appender = new AppenderSkeleton(true) {

		PatternLayout layout = new PatternLayout("%d{ISO8601} %-5p [%c] %m%n");

		@Override
		public Layout getLayout() {
			return layout;
		}

		@Override
		public boolean requiresLayout() {
			return true;
		}

		@Override
		public void close() {

		}

		@Override
		protected void append(final LoggingEvent event) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					StringBuilder sb = new StringBuilder();
					sb.append(getLayout().format(event));

					if (getLayout().ignoresThrowable()) {
						String[] s = event.getThrowableStrRep();
						if (s != null) {
							int len = s.length;
							for (int i = 0; i < len; i++) {
								sb.append(s[i]);
								sb.append(Layout.LINE_SEP);
							}
						}
					}

					getView().write(sb.toString());
				}
			});
		}
	};

	public ScriptLoggerController(MainFrameTab parent) {
		super(parent);

		parent.addMainFrameTabListener(new MainFrameTabAdapter() {
			@Override
			public void tabClosing(MainFrameTab tab, ResultListener<Boolean> rl) {
				LOG.removeAppender(appender);
				rl.done(true);
			}
		});

		LOG.addAppender(appender);
	}

	public ScriptLoggerView getView() {
		return view;
	}
}
