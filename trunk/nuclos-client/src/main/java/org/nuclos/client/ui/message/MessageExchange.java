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
package org.nuclos.client.ui.message;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.ListIterator;

import org.apache.log4j.Logger;

public final class MessageExchange {
	
	private static final Logger LOG = Logger.getLogger(MessageExchange.class);
	
	private static LinkedList<WeakReference<MessageExchangeListener>> listeners = new LinkedList<WeakReference<MessageExchangeListener>>();

	private MessageExchange() {
	}
	
	public static void send(Object id, MessageExchangeListener.ObjectType type, MessageExchangeListener.MessageType msg) {
		synchronized(listeners) {
			for(ListIterator<WeakReference<MessageExchangeListener>> i = listeners.listIterator(); i.hasNext();) {
				WeakReference<MessageExchangeListener> s = i.next();
				MessageExchangeListener l = s.get();
				if(l == null)
					i.remove();
				else
					try {
						l.receive(id, type, msg);
					} catch(Exception e) {
						LOG.warn("send failed: " + e, e);
					}
			}
		}
	}

	/**
	 * Add a listener
	 * 
	 * @param l the listener
	 */
	public static void addListener(MessageExchangeListener l) {
		synchronized(listeners) {
			listeners.add(new WeakReference<MessageExchangeListener>(l));
		}
	}

	/**
	 * Remove a listener
	 * 
	 * @param l the listener
	 */
	public static void removeListener(MessageExchangeListener l) {
		synchronized(listeners) {
			for(ListIterator<WeakReference<MessageExchangeListener>> i = listeners.listIterator(); i.hasNext();) {
				WeakReference<MessageExchangeListener> s = i.next();
				MessageExchangeListener li = s.get();
				if(li == null || li == l)
					i.remove();
			}
		}
	}

	public interface MessageExchangeListener {
		public enum ObjectType {
			TEXTFIELD,
			TASKBAR,
			EXPLORER,
			TASKPANEL,
			TRANSPARENCY
		};

		public enum MessageType {
			REFRESH
		};

		public abstract void receive(Object id, ObjectType type, MessageType msg);
	}
}
