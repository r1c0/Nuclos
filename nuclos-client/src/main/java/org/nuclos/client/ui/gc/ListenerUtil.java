package org.nuclos.client.ui.gc;

import java.awt.event.ActionListener;
import java.lang.ref.Reference;
import java.util.EventListener;

import javax.swing.AbstractButton;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

public class ListenerUtil {
	
	private final static class ButtonRegister implements IRegister {
		
		private final AbstractButton b;
		
		private final ActionAdapter a;
		
		private ButtonRegister(AbstractButton b, ActionAdapter a) {
			this.b = b;
			this.a = a;
		}

		@Override
		public void register() {
			b.addActionListener(a);
			QueueSingleton.getInstance().register(this);
		}

		@Override
		public void unregister() {
			b.removeActionListener(a);
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class JTextFieldRegister implements IRegister {
		
		private final JTextField b;
		
		private final ActionAdapter a;
		
		private JTextFieldRegister(JTextField b, ActionAdapter a) {
			this.b = b;
			this.a = a;
		}

		@Override
		public void register() {
			b.addActionListener(a);
			QueueSingleton.getInstance().register(this);
		}

		@Override
		public void unregister() {
			b.removeActionListener(a);
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class JComboBoxRegister implements IRegister {
		
		private final JComboBox b;
		
		private final ActionAdapter a;
		
		private JComboBoxRegister(JComboBox b, ActionAdapter a) {
			this.b = b;
			this.a = a;
		}

		@Override
		public void register() {
			b.addActionListener(a);
			QueueSingleton.getInstance().register(this);
		}

		@Override
		public void unregister() {
			b.removeActionListener(a);
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private final static class DocumentRegister implements IRegister {
		
		private final Document b;
		
		private final DocumentAdapter a;
		
		private DocumentRegister(Document b, DocumentAdapter a) {
			this.b = b;
			this.a = a;
		}

		@Override
		public void register() {
			b.addDocumentListener(a);
			QueueSingleton.getInstance().register(this);
		}

		@Override
		public void unregister() {
			b.removeDocumentListener(a);
		}

		@Override
		public Reference<EventListener> getReference() {
			return a.getReference();
		}
		
	}
	
	private ListenerUtil() {
		// Never invoked.
	}
	
	public static void registerActionListener(AbstractButton b, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new ButtonRegister(b, a);
		register.register();
	}

	public static void registerActionListener(JTextField b, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new JTextFieldRegister(b, a);
		register.register();
	}

	public static void registerActionListener(JComboBox b, ActionListener l) {
		final ActionAdapter a = new ActionAdapter(l);
		final IRegister register = new JComboBoxRegister(b, a);
		register.register();
	}

	public static void registerDocumentListener(Document b, DocumentListener l) {
		final DocumentAdapter a = new DocumentAdapter(l);
		final IRegister register = new DocumentRegister(b, a);
		register.register();
	}

}
