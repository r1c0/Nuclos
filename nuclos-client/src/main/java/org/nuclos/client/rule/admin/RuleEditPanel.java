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
package org.nuclos.client.rule.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Element;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.combobox.ListComboBoxModel;
import org.jdesktop.swingx.renderer.DefaultListRenderer;
import org.jdesktop.swingx.renderer.IconValue;
import org.jdesktop.swingx.renderer.LabelProvider;
import org.jdesktop.swingx.renderer.StringValue;
import org.nuclos.client.main.Main;
import org.nuclos.client.ui.Errors;
import org.nuclos.client.ui.Icons;
import org.nuclos.client.ui.SimpleDocumentListener;
import org.nuclos.common2.CommonLocaleDelegate;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.server.ruleengine.NuclosCompileException.ErrorMessage;

/**
 * Panel for editing of rules.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Boris.Sander@novabit.de">Boris Sander</a>
 * @version 01.00.00
 */
public class RuleEditPanel extends JPanel {

	private static final Logger LOG = Logger.getLogger(RuleEditPanel.class);

	private final List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

	private final RuleHeaderPanel pnlHeader;
	private final JEditorPane pnlJavaEditor;
	private final JList errorMessagesList;

	private String entityname;
	private Long id;

	public RuleEditPanel(JPanel pnlUsage) {
		super(new BorderLayout());

		pnlHeader = new RuleHeaderPanel();
		pnlJavaEditor = new JEditorPane();
		errorMessagesList = new JList();
		errorMessagesList.setBackground(pnlJavaEditor.getBackground());
		errorMessagesList.setForeground(Color.RED);
		errorMessagesList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2) {
					int index = errorMessagesList.locationToIndex(evt.getPoint());
					Rectangle cellBounds = errorMessagesList.getCellBounds(index, index);
					if (cellBounds != null && cellBounds.contains(evt.getPoint())) {
						ErrorMessage error = (ErrorMessage) errorMessagesList.getModel().getElementAt(index);
						if (error != null) {
							if (entityname == null || id == null || (entityname.equals(error.getEntityname()) && id.equals(error.getId()))) {
								if (error.getLineNumber() != ErrorMessage.NOPOS) {
									int p1 = -1, p2 = -1;
									if (error.getPosition() != ErrorMessage.NOPOS) {
										p1 = (int) error.getStartPosition();
										p2 = (int) error.getEndPosition();
									} else if (error.getLineNumber() != ErrorMessage.NOPOS) {
										Element e = pnlJavaEditor.getDocument().getDefaultRootElement().getElement((int) error.getLineNumber());
										p1 = e.getStartOffset();
										if (error.getColumnNumber() != -1)
											p1 += (int) error.getColumnNumber();
										p2 = p1;
									}
									if (p1 != -1)
										pnlJavaEditor.select(p1, p2);
									pnlJavaEditor.requestFocusInWindow();
								}
							}
							else {
								try {
									Main.getMainController().showDetails(error.getEntityname(), error.getId());
								}
								catch(CommonBusinessException e) {
									Errors.getInstance().showExceptionDialog(RuleEditPanel.this, e);
								}
							}
						}
					}
				}
			}
		});
		errorMessagesList.setCellRenderer(new DefaultListRenderer(new LabelProvider(new ErrorMessageConverter())));

		final JSplitPane splitpn = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitpn.setTopComponent(new JScrollPane(pnlJavaEditor));
		splitpn.setBottomComponent(new JScrollPane(errorMessagesList));
		splitpn.setResizeWeight(1.0);

		jsyntaxpane.DefaultSyntaxKit.initKit();
		pnlJavaEditor.setContentType("text/java");
		pnlJavaEditor.getDocument().addDocumentListener(new SimpleDocumentListener() {
			@Override
			public void documentChanged(DocumentEvent ev) {
				fireChange();
			}
		});

		final JPanel pnlRulesEditor = new JPanel(new BorderLayout());
		pnlRulesEditor.add(splitpn, BorderLayout.CENTER);

		final JTabbedPane tabpn = new JTabbedPane();
		tabpn.add(CommonLocaleDelegate.getMessage("RuleEditPanel.1","Regel"), pnlRulesEditor);

		if(pnlUsage != null) {
			add(pnlHeader, BorderLayout.NORTH);
			tabpn.add(CommonLocaleDelegate.getMessage("RuleEditPanel.2","Verwendung"), pnlUsage);
		}
		this.add(tabpn, BorderLayout.CENTER);
	}

	RuleHeaderPanel getHeaderPanel() {
		return pnlHeader;
	}

	public JEditorPane getJavaEditorPanel() {
		return pnlJavaEditor;
	}

	public void setMessages(List<ErrorMessage> messages) {
		if (messages != null) {
			errorMessagesList.setModel(new ListComboBoxModel<ErrorMessage>(messages));
		} else {
			errorMessagesList.setModel(new DefaultListModel());
		}
	}

	public void clearMessages() {
		setMessages(null);
	}

	public void addChangeListener(ChangeListener changelistener) {
		changeListeners.add(changelistener);
	}

	public void removeChangeListener(ChangeListener changelistener) {
		changeListeners.remove(changelistener);
	}

	private void fireChange() {
		for(ChangeListener changelistener : changeListeners) {
			changelistener.stateChanged(new ChangeEvent(this));
		}
	}

	public String getEntityname() {
		return entityname;
	}

	public void setEntityname(String entityname) {
		this.entityname = entityname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	static class ErrorMessageConverter implements StringValue, IconValue {

		@Override
		public String getString(Object value) {
			ErrorMessage error = (ErrorMessage) value;
			if (error.getPosition() != ErrorMessage.NOPOS) {
				return String.format("%s - %d:%d: %s", error.getSource(), error.getLineNumber(), error.getColumnNumber(),
					error.getMessage(null).replace("\n", ", "));
			} else {
				return error.getMessage(null);
			}
		}
		
		@Override
		public Icon getIcon(Object value) {
			switch (((ErrorMessage) value).getKind()) {
			case ERROR:
				return Icons.getInstance().getIconJobError();
			case WARNING:
			case MANDATORY_WARNING:
				return Icons.getInstance().getIconJobWarning();
			default:
				return Icons.getInstance().getIconJobUnknown();
			}
		}
	}
}
