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

	public static void registerWindowListener(Window b, WindowListener l) {
		final WindowAdapter a = new WindowAdapter(l);
		final IRegister register = new WindowRegister(b, a);
		register.register();
	}
	
	public static void registerSubFormToolListener(SubForm b, SubFormToolListener l) {
		final SubformToolAdapter a = new SubformToolAdapter(l);
		final IRegister register = new SubFormToolRegister(b, a);
		register.register();		
	}

}
