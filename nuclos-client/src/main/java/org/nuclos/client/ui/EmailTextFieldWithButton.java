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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.nuclos.api.ui.MailToEventHandler;
import org.nuclos.client.nuclet.MailToEventHandlerRepository;
import org.nuclos.client.ui.labeled.ILabeledComponentSupport;
import org.nuclos.common.collection.CollectionUtils;
import org.nuclos.common2.SpringLocaleDelegate;
import org.nuclos.common2.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

@Configurable
public class EmailTextFieldWithButton extends HyperlinkTextFieldWithButton {

	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(EmailTextFieldWithButton.class);
	
	private MailToEventHandlerRepository mtehRepository;

	public EmailTextFieldWithButton(ILabeledComponentSupport support, boolean bSearchable) {
		super(support, bSearchable);
	}
	
	@Autowired
	void setMailToEventHandlerRepository(MailToEventHandlerRepository mtehRepository) {
		this.mtehRepository = mtehRepository;
	}

	/**
	 * Click or Ctrl+Enter
	 */
	@Override
	public void openLink() {
		if (!StringUtils.looksEmpty(getText())) {
			List<Action> mailToActions = getMailToEventHandler();
			if (mailToActions.isEmpty()) {
				newMailWithDefaultSystemMailclient();
			} else {
				mailToActions.get(0).actionPerformed(new ActionEvent(EmailTextFieldWithButton.this, 0, "MailTo"));
			}
		}
	}

	public void newMailWithDefaultSystemMailclient() {
		try {	
			String url = "mailto:" + getText();
			Desktop.getDesktop().mail(URI.create(url));	
		} catch (IllegalArgumentException e) {
			// ignore.
		} catch (IOException e) {
			log.info(e.getMessage());
		}
	}
	
	@Override
	protected List<JComponent> getContextMenuItems() {
		List<JComponent> result = new ArrayList<JComponent>();
		result.add(getEditMenuItem());
		
		boolean enableNewMailActions = !StringUtils.looksEmpty(getText());
		JMenuItem miCtrlEnter = null;
		List<Action> mailToActions = getMailToEventHandler();
		if (!mailToActions.isEmpty()) {
			result.add(new JSeparator());
		}
		for (Action mailToAction : mailToActions) {
			JMenuItem mi = new JMenuItem(mailToAction); 
			mi.setEnabled(enableNewMailActions);
			if (miCtrlEnter == null) {
				miCtrlEnter = mi;
			}
			result.add(mi);
		}
		
		JMenuItem miDefaultSystemClient = getNewMailMenuItem();
		miDefaultSystemClient.setEnabled(enableNewMailActions);
		if (miCtrlEnter == null) {
			miCtrlEnter = miDefaultSystemClient;
		}
		result.add(miDefaultSystemClient);
		
		miCtrlEnter.setAccelerator(KeyStroke.getKeyStroke(ENTER, CTRL));
		
		return result;
	}
	
	private List<Action> getMailToEventHandler() {
		List<Action> result = new ArrayList<Action>();
		for (MailToEventHandler mteh : mtehRepository.getMailToEventHandler()) {
			Action mailToAction = mteh.getMailToAction(getText(), null);
			if (mailToAction != null) {
				result.add(mailToAction);
			}
		}
		return CollectionUtils.sorted(result, new Comparator<Action>() {
			@Override
			public int compare(Action o1, Action o2) {
				return StringUtils.compareIgnoreCase((String) o1.getValue(Action.NAME), (String) o2.getValue(Action.NAME));
			}
		});
	}
	
	protected JMenuItem getNewMailMenuItem() {
		return new JMenuItem(new AbstractAction(
				SpringLocaleDelegate.getInstance().getMessage("Email.defaultSystemMailClient.new", "Neue Email mit Standard-Mailprogramm")) {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				newMailWithDefaultSystemMailclient();
			}
		});
	}
}
