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
package org.nuclos.client.ui.collect;

import java.io.Serializable;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.nuclos.api.context.InputDelegate;
import org.nuclos.api.context.InputRequiredException;
import org.nuclos.api.context.InputSpecification;
import org.nuclos.client.NuclosHttpInvokerAttributeContext;
import org.nuclos.client.main.Main;
import org.nuclos.common2.CommonRunnable;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// @Component
public class InvokeWithInputRequiredSupport {

	private NuclosHttpInvokerAttributeContext ctx;

	public InvokeWithInputRequiredSupport() {
	}

	// @Autowired
	public final void setNuclosHttpInvokerAttributeContext(NuclosHttpInvokerAttributeContext ctx) {
		this.ctx = ctx;
	}

	public void invoke(CommonRunnable runnable, Map<String, Serializable> context, JComponent parent) throws CommonBusinessException {
		try {
			ctx.setSupported(true);
			ctx.putAll(context);
			try {
				runnable.run();
				context.clear();
			}
			finally {
				ctx.clear();
				ctx.setSupported(false);
			}
		}
		catch (CommonBusinessException cbex) {
			InputRequiredException ex = getInputRequiredException(cbex);
			if (ex != null) {
				handleInputRequiredException(ex, runnable, context, parent);
			}
			else {
				context.clear();
				throw cbex;
			}
		}
		catch (CommonFatalException cfex) {
			InputRequiredException ex = getInputRequiredException(cfex);
			if (ex != null) {
				handleInputRequiredException(ex, runnable, context, parent);
			}
			else {
				context.clear();
				throw cfex;
			}
		}
	}

	private void handleInputRequiredException(InputRequiredException ex, CommonRunnable r, Map<String, Serializable> context, JComponent parent) throws CommonBusinessException {
		String title = Main.getInstance().getMainFrame().getTitle();
		
		if (ex.getInputSpecification() != null) {
			String message = ex.getInputSpecification().getMessage();
			switch (ex.getInputSpecification().getType()) {
			case InputSpecification.CONFIRM_YES_NO:
				int i = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
				if (i != JOptionPane.CLOSED_OPTION && i != JOptionPane.CANCEL_OPTION) {
					context.put(ex.getInputSpecification().getKey(), (i == JOptionPane.YES_OPTION) ? InputSpecification.YES : InputSpecification.NO);
					invoke(r, context, parent);
				}
				else {
					throw new UserCancelledException();
				}
				break;
			case InputSpecification.CONFIRM_OK_CANCEL:
				i = JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.OK_CANCEL_OPTION);
				if (i != JOptionPane.CLOSED_OPTION && i != JOptionPane.CANCEL_OPTION) {
					context.put(ex.getInputSpecification().getKey(), InputSpecification.OK);
					invoke(r, context, parent);
				}
				else {
					throw new UserCancelledException();
				}
				break;
			case InputSpecification.INPUT_VALUE:
				String s = JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE);
				if (s != null) {
					context.put(ex.getInputSpecification().getKey(), s);
					invoke(r, context, parent);
				}
				else {
					throw new UserCancelledException();
				}
				break;
			case InputSpecification.INPUT_OPTION:
				Object o = JOptionPane.showInputDialog(parent, message, title, JOptionPane.PLAIN_MESSAGE, null, ex.getInputSpecification().getOptions(), ex.getInputSpecification().getDefaultOption());
				if (o != null) {
					context.put(ex.getInputSpecification().getKey(), (Serializable) o);
					invoke(r, context, parent);
				}
				else {
					throw new UserCancelledException();
				}
				break;
			default:
				break;
			}
		}
		else if (ex.getInputDelegateSpecification() != null) {
			String classname = ex.getInputDelegateSpecification().getDelegateClass();

			try {
				Class<?> clazz = Class.forName(classname);
				if (InputDelegate.class.isAssignableFrom(clazz)) {
					InputDelegate ics = (InputDelegate) clazz.newInstance();
					InputDelegatePane pane = new InputDelegatePane(ics);
					JDialog dialog = new JDialog(Main.getInstance().getMainFrame(), title, true);
					Map<String, Serializable> result = pane.show(dialog, ex.getInputDelegateSpecification().getData(), parent);
					if (result != null) {
						context.putAll(result);
						invoke(r, context, parent);
					}
					else {
						throw new UserCancelledException();
					}
				}
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		else {
			throw new UnsupportedOperationException("Unable to process exception " +  ex.toString());
		}
	}

	private InputRequiredException getInputRequiredException(Throwable ex) {
		if (ex instanceof InputRequiredException) {
			return (InputRequiredException) ex;
		}
		else if (ex.getCause() != null) {
			return getInputRequiredException(ex.getCause());
		}
		else {
			return null;
		}
	}
}
