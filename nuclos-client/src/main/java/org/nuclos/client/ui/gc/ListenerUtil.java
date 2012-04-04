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
//
package org.nuclos.client.ui.gc;

import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.EventListener;

import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.nuclos.client.ui.collect.SubForm;
import org.nuclos.client.ui.collect.SubForm.SubFormToolListener;

/**
 * A utility class for registering (gui) listeners that get garbage collected.
 * <p>
 * Sometimes you want to register a temporary (gui) component as a listener to a permanent
 * (gui) component. However, when you do this, the temporary component will <em>never</em>
 * get garbage collected. This is because the event producer class hold an reference to the 
 * listener class. Hence the listener will be alive as long as the sender class is alive.
 * </p><p>
 * The consequence of this is that you dig a memory hole if you register a temporary component
 * as a listener to a permanent component.
 * </p><p>
 * This is drop-in solution for this problem. Instead of registering the listener class with
 * the event producer class directly, just use the static utility methods of this class. 
 * </p><p>
 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
 * without setting the outer class object</em>. The will be garbage collected directly!
 * </p>
 * @since Nuclos 3.3.0
 * @author Thomas Pasch
 */
public class ListenerUtil {
	
	private final static class ButtonRegister implements IRegister {
		
		private final WeakReference<AbstractButton> b;
		
		private final ActionAdapter a;
		
		private ButtonRegister(AbstractButton b, ActionAdapter a) {
			this.b = new WeakReference<AbstractButton>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final AbstractButton bb = b.get();
			if (bb != null) {
				bb.addActionListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final AbstractButton bb = b.get();
			if (bb != null) {
				bb.removeActionListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class JTextFieldRegister implements IRegister {
		
		private final WeakReference<JTextField> b;
		
		private final ActionAdapter a;
		
		private JTextFieldRegister(JTextField b, ActionAdapter a) {
			this.b = new WeakReference<JTextField>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final JTextField bb = b.get();
			if (bb != null) {
				bb.addActionListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final JTextField bb = b.get();
			if (bb != null) {
				bb.removeActionListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class JComboBoxRegister implements IRegister {
		
		private final WeakReference<JComboBox> b;
		
		private final ActionAdapter a;
		
		private JComboBoxRegister(JComboBox b, ActionAdapter a) {
			this.b = new WeakReference<JComboBox>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final JComboBox bb = b.get();
			if (bb != null) {
				bb.addActionListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final JComboBox bb = b.get();
			if (bb != null) {
				bb.removeActionListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class DocumentRegister implements IRegister {
		
		private final WeakReference<Document> b;
		
		private final DocumentAdapter a;
		
		private DocumentRegister(Document b, DocumentAdapter a) {
			this.b = new WeakReference<Document>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final Document bb = b.get();
			if (bb != null) {
				bb.addDocumentListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final Document bb = b.get();
			if (bb != null) {
				bb.removeDocumentListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class WindowRegister implements IRegister {
		
		private final WeakReference<Window> b;
		
		private final WindowAdapter a;
		
		private WindowRegister(Window b, WindowAdapter a) {
			this.b = new WeakReference<Window>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final Window bb = b.get();
			if (bb != null) {
				bb.addWindowListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final Window bb = b.get();
			if (bb != null) {
				bb.removeWindowListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class SubFormToolRegister implements IRegister {
		
		private final WeakReference<SubForm> b;
		
		private final SubformToolAdapter a;
		
		private SubFormToolRegister(SubForm b, SubformToolAdapter a) {
			this.b = new WeakReference<SubForm>(b);
			this.a = a;
		}

		@Override
		public void register() {
			final SubForm bb = b.get();
			if (bb != null) {
				bb.addSubFormToolListener(a);
				QueueSingleton.getInstance().register(this);
			}
		}

		@Override
		public void unregister() {
			final SubForm bb = b.get();
			if (bb != null) {
				bb.removeSubFormToolListener(a);
			}
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private ListenerUtil() {
		// Never invoked.
	}
	
	private static void dependant(IReferenceHolder outer, EventListener realListener) {
		if (outer != null) {
			outer.addRef(realListener);
		}
	}
	
	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerActionListener(AbstractButton b, IReferenceHolder outer, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new ButtonRegister(b, a);
		register.register();
		dependant(outer, l);
	}

	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerActionListener(JTextField b, IReferenceHolder outer, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new JTextFieldRegister(b, a);
		register.register();
		dependant(outer, l);
	}

	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerActionListener(JComboBox b, IReferenceHolder outer, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new JComboBoxRegister(b, a);
		register.register();
		dependant(outer, l);
	}

	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerDocumentListener(Document b, IReferenceHolder outer, DocumentListener l) {
		final DocumentAdapter a = new DocumentAdapter(l);
		final IRegister register = new DocumentRegister(b, a);
		register.register();
		dependant(outer, l);
	}

	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerWindowListener(Window b, IReferenceHolder outer, WindowListener l) {
		final WindowAdapter a = new WindowAdapter(l);
		final IRegister register = new WindowRegister(b, a);
		register.register();
		dependant(outer, l);
	}
	
	/**
	 * <p>
	 * ATTENTION: Don't register anonymous inner class listeners with this utility class <em>
	 * without setting the outer class object</em>. The will be garbage collected directly!
	 * </p>
	 */
	public static void registerSubFormToolListener(SubForm b, IReferenceHolder outer, SubFormToolListener l) {
		final SubformToolAdapter a = new SubformToolAdapter(l);
		final IRegister register = new SubFormToolRegister(b, a);
		register.register();		
		dependant(outer, l);
	}

}
