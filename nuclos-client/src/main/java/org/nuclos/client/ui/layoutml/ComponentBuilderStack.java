// Copyright (C) 2011 Novabit Informationssysteme GmbH
//
// This file is part of Nuclos.
//
// Nuclos is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Nuclos is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Nuclos. If not, see <http://www.gnu.org/licenses/>.
package org.nuclos.client.ui.layoutml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;
import org.nuclos.client.ui.JInfoTabbedPane;
import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.component.CollectableComponent;
import org.nuclos.client.ui.layoutml.LayoutMLParser.BuildFormHandler.CollectableComponentBuilder;
import org.nuclos.client.ui.layoutml.LayoutMLParser.BuildFormHandler.DefaultComponentBuilder;
import org.nuclos.client.ui.layoutml.LayoutMLParser.BuildFormHandler.ScrollPaneBuilder;
import org.nuclos.client.ui.layoutml.LayoutMLParser.BuildFormHandler.TabbedPaneBuilder;
import org.nuclos.common2.StringUtils;
import org.xml.sax.SAXException;

/**
 * Stack of ComponentBuilder used in LayoutMLParser.
 * 
 * @see ComponentBuilder
 * @see LayoutMLParser
 */
class ComponentBuilderStack {

	private static final Logger				LOG			= Logger.getLogger(ComponentBuilderStack.class);

	/**
	 * the stack used to build the structure from the (hierarchical) XML
	 * document
	 */
	private final Stack<ComponentBuilder>	stack		= new Stack<ComponentBuilder>();

	/**
	 * maps child entities to subforms
	 */
	private final Map<String, SubForm>		mpSubForms	= new HashMap<String, SubForm>();

	/**
	 * Extra TabbedPaneBuilder stack.
	 */
	private Stack<TabbedPaneBuilder>		tabbedStack	= new Stack<TabbedPaneBuilder>();

	ComponentBuilderStack() {
	}

	/**
	 * constructs a <code>ComponentBuilder</code> out of <code>comp</code> and
	 * pushes it on the stack.
	 * 
	 * @param comp
	 */
	public void addComponent(JComponent comp) {
		stack.push(new DefaultComponentBuilder(comp));
	}

	/**
	 * constructs an <code>CollectableComponentBuilder</code> out of
	 * <code>clctcomp</code> and pushes it on the stack.
	 * 
	 * @param clctcomp
	 */
	public void addCollectableComponent(CollectableComponent clctcomp) {
		stack.push(new CollectableComponentBuilder(clctcomp));
	}

	/**
	 * constructs a <code>TabbedPaneBuilder</code> out of <code>tbdpn</code> and
	 * pushes it on the stack.
	 * 
	 * @param tbdpn
	 */
	public void addTabbedPane(JInfoTabbedPane tbdpn) {
		final TabbedPaneBuilder builder = new TabbedPaneBuilder(tbdpn);
		stack.push(builder);
		tabbedStack.push(builder);
	}

	/**
	 * constructs a <code>ScrollPaneBuilder</code> out of <code>scrlpn</code>
	 * and pushes it on the stack.
	 * 
	 * @param scrlpn
	 */
	public void addScrollPane(JScrollPane scrlpn) {
		stack.push(new ScrollPaneBuilder(scrlpn));
	}

	/**
	 * @return the component builder that lies on top of the stack
	 */
	public ComponentBuilder peekComponentBuilder() {
		return stack.peek();
	}

	/**
	 * pops the component builder that lies on top of the stack
	 * 
	 * @return the component builder that lies on top of the stack
	 */
	public ComponentBuilder popComponentBuilder() {
		final ComponentBuilder result = stack.pop();
		// TODO: This instanceof is extremly ugly - but how to avoid it? (Thomas
		// Pasch)
		if(result instanceof TabbedPaneBuilder) {
			tabbedStack.pop();
		}
		return result;
	}

	/**
	 * shortcut for peekComponentBuilder().getComponent()
	 * 
	 * @return the component in the component builder that lies on top of the
	 *         stack
	 */
	public JComponent peekComponent() {
		return peekComponentBuilder().getComponent();
	}

	private boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * finishes the current component. That is, pops the corresponding component
	 * builder that lies top on the stack and adds it to the component builder
	 * that lies next on the stack. If there is no parent (that is: the stack is
	 * empty), sets the popped component as the root panel.
	 */
	public JComponent finishComponent() throws SAXException {
		final ComponentBuilder cb = popComponentBuilder();
		final JComponent compRoot;
		if(isEmpty()) {
			if(!(cb.getComponent() instanceof JPanel)) {
				LOG.warn("Wurzelkomponente ist kein JPanel, sondern "
					+ cb.getComponent().getClass().getName() + ".");
			}
			compRoot = cb.getComponent();
		}
		else {
			final ComponentBuilder parent = peekComponentBuilder();
			cb.finish(parent);
			compRoot = null;
		}
		return compRoot;
	}

	// subform stuff

	Map<String, SubForm> getMapOfSubForms() {
		return mpSubForms;
	}

	public void addSubForm(String sEntityName, SubForm subform)
		throws SAXException {
		if(mpSubForms.containsKey(sEntityName)) {
			throw new SAXException(
				StringUtils.getParameterizedExceptionMessage(
					"LayoutMLParser.16", sEntityName));
		}
		mpSubForms.put(sEntityName, subform);
		addComponent(subform);

		if(!tabbedStack.isEmpty()) {
			final TabbedPaneBuilder builder = tabbedStack.peek();
			builder.addSubForm(subform);
		}
	}

	public SubForm getSubFormForEntity(String sEntityName) {
		return mpSubForms.get(sEntityName);
	}

}
