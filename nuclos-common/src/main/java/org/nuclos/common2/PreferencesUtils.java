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
package org.nuclos.common2;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import javax.swing.JInternalFrame;
import javax.swing.JSplitPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.nuclos.common.NuclosBusinessException;
import org.nuclos.common.preferences.PreferencesConverter;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.PreferencesException;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Utility methods for <code>java.util.prefs.Preferences</code>.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @todo refactor some of the implementations. This might also affect declared exceptions!
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */
public class PreferencesUtils {

	private static final Logger log = Logger.getLogger(PreferencesUtils.class);

	/**
	 * the preferences key to store the size of the array (number of elements)
	 */
	private static final String PREFS_KEY_LIST_SIZE = "size";
	//   private static final String PREFS_KEY_NAME = "name";
	//   private static final String PREFS_KEY_MODE = "mode";

	private static final String PREFS_KEY_RECTANGLE_X = "x";
	private static final String PREFS_KEY_RECTANGLE_Y = "y";
	private static final String PREFS_KEY_RECTANGLE_WIDTH = "width";
	private static final String PREFS_KEY_RECTANGLE_HEIGHT = "height";

	private static final String PREFS_KEY_WINDOW_EXTENDEDSTATE = "extendedState";
	private static final String PREFS_KEY_NORMAL_BOUNDS = "normalBounds";

	private static final String PREFS_KEY_ISMAXIMIZED = "isMaximized";
	private static final String PREFS_KEY_ISICONIFIED = "isIconified";

	private static final String PREFS_KEY_ORIENTATION = "orientation";
	private static final String PREFS_KEY_DIVIDER_LOCATION = "dividerLocation";
	private static final String PREFS_KEY_LAST_DIVIDER_LOCATION = "lastDividerLocation";

	/**
	 * preferences key for storing the window state
	 */
	private final static String PREFS_NODE_WINDOWSTATE = "windowState";

	/**
	 * specifies how to read and write an Object from/to the preferences.
	 */
	public interface PreferencesIO<T> {
		/**
		 * reads a <code>T</code> from the given preferences.
		 * @param prefs
		 * @return the read <code>T</code>.
		 */
		T get(Preferences prefs) throws PreferencesException;

		/**
		 * writes the given <code>T</code> to the given preferences.
		 * @param prefs
		 * @param t
		 */
		void put(Preferences prefs, T t) throws PreferencesException;

	}  // inner interface PreferencesIO

	/**
	 * PreferencesIO for reading/writing a serializable object from/to the preferences.
	 * @deprecated Don't serialize objects to the preferences!
	 */
	@Deprecated
	public static class SerializablePreferencesIO implements PreferencesIO<Object> {
		private final String sKey;

		/**
		 * This constructor can be used when the key doesn't matter.
		 */
		public SerializablePreferencesIO() {
			this("serializedObject");
		}

		/**
		 * @param sKey the key to store the serialized object.
		 */
		public SerializablePreferencesIO(String sKey) {
			this.sKey = sKey;
		}

		@Override
		public Object get(Preferences prefs) throws PreferencesException {
			return PreferencesUtils.getSerializable(prefs, this.sKey);
		}

		@Override
		public void put(Preferences prefs, Object o) throws PreferencesException {
			PreferencesUtils.putSerializable(prefs, this.sKey, o);
		}
	}  // inner class SerializablePreferencesIO

	private static class XMLSerializationIO implements PreferencesIO<Object> {
		private String key;

		public XMLSerializationIO(String key) {
			this.key = key;
		}

		@Override
		public Object get(Preferences prefs) throws PreferencesException {
			//         String s = prefs.get(key, null);
			//         if(s == null)
			//            return null;
			//
			//         try {
			//            ByteArrayInputStream is = new ByteArrayInputStream(s.getBytes("ISO-8859-1"));
			//            XMLDecoder x = new XMLDecoder(is);
			//            Object res = x.readObject();
			//            x.close();
			//            return res;
			//         }
			//         catch(UnsupportedEncodingException e) {
			//            throw new PreferencesException("XMLSerializationIO.get Error", e);
			//         }

			String s = prefs.get(key, null);
			if(s == null)
				return null;
			XStream xstream = new XStream(new DomDriver());
			return xstream.fromXML(s);
		}

		@Override
		public void put(Preferences prefs, Object t) throws PreferencesException {
			//         try {
			//            ByteArrayOutputStream os = new ByteArrayOutputStream();
			//            XMLEncoder x = new XMLEncoder(os);
			//            x.writeObject(t);
			//            x.close();
			//            String s = os.toString("ISO-8859-1");
			//            prefs.put(key, s);
			//         }
			//         catch(UnsupportedEncodingException e) {
			//            throw new PreferencesException("XMLSerializationIO.put Error", e);
			//         }
			XStream xstream = new XStream(new DomDriver());
			String s = xstream.toXML(t);
			prefs.put(key, s);
		}
	}

	protected PreferencesUtils() {
	}

	public static void resetToTemplateUser(String nodeToReset) throws NuclosBusinessException {
		try {
			PreferencesFacadeRemote fascade = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class);

			PreferencesVO userPreferences = fascade.getUserPreferences();
			PreferencesVO templateUserPreferences = fascade.getTemplateUserPreferences();

			if (templateUserPreferences == null)
				throw new NuclosBusinessException("Wiederherstellen vom Template User nicht m\u00f6glich, keiner gesetzt (eigentlich kann man das so nicht anklicken)");

			ByteArrayInputStream userPrefs = new ByteArrayInputStream(userPreferences.getPreferencesBytes());
			ByteArrayInputStream templatePrefs = new ByteArrayInputStream(templateUserPreferences.getPreferencesBytes());

			DocumentBuilderFactory factoryTemplateUser = DocumentBuilderFactory.newInstance();

			DocumentBuilder builderTemplateUser = factoryTemplateUser.newDocumentBuilder();
			builderTemplateUser.setEntityResolver(PreferencesConverter.getPreferencesDTDResolver());
			Document documentTemplateUser = builderTemplateUser.parse(templatePrefs);

			DocumentBuilderFactory factoryTargetUser = DocumentBuilderFactory.newInstance();
			DocumentBuilder builderTargetUser = factoryTargetUser.newDocumentBuilder();
			builderTargetUser.setEntityResolver(PreferencesConverter.getPreferencesDTDResolver());
			Document documentTargetUser = builderTargetUser.parse(userPrefs);

			javax.xml.xpath.XPath xpath = javax.xml.xpath.XPathFactory.newInstance().newXPath();

			XPathExpression expression = xpath.compile("//*[@name='" + nodeToReset + "']");
			Node templateChilds = null;
			Node targetChilds = null;
			Object returnValueTemplate = expression.evaluate(documentTemplateUser, XPathConstants.NODE);
			if (returnValueTemplate != null) {
				Element elementSearchedFor = (Element) returnValueTemplate;
				templateChilds = elementSearchedFor.getFirstChild();
			}
			Object returnValueTarget = expression.evaluate(documentTargetUser, XPathConstants.NODE);
			if (returnValueTarget != null) {
				Element elementSearchedFor = (Element) returnValueTarget;
				targetChilds = elementSearchedFor.getFirstChild();
			}
			if (targetChilds != null) {
				Node parent = targetChilds.getParentNode();

				if (parent.hasChildNodes()) {
					Node nextChild = parent.getFirstChild();
					while (nextChild != null) {
						parent.removeChild(nextChild);
						nextChild = parent.getFirstChild();
					}
				}

				Node templateParent = templateChilds.getParentNode();
				if (templateParent.hasChildNodes()) {
					Node nextChild = templateParent.getFirstChild();
					while(nextChild != null) {
						parent.appendChild(documentTargetUser.importNode(nextChild, true));
						templateParent.removeChild(nextChild);
						nextChild = templateParent.getFirstChild();
					}
				}
			} else {
				/*
				   <node name="collect">
							<map />
							<node name="entity">
								<map />
								<node name="bosProduct">
									<map />
									<node name="fields">
				 */

				expression = xpath.compile("//*[@name='entity']");

				Object entityTree = expression.evaluate(documentTargetUser, XPathConstants.NODE); //<node name="entity">
				Node entityTreeMainNode = null;
				if (entityTree != null) {
					Element elementSearchedFor = (Element) entityTree;
					entityTreeMainNode = elementSearchedFor.getFirstChild(); // <map/>
					entityTreeMainNode = entityTreeMainNode.getParentNode(); //<node name="entity">
				}
				entityTreeMainNode.appendChild(documentTargetUser.importNode(templateChilds, true));
				//TODO \u00fcbertragen von preferences wenn der node noch nicht existiert
			}

			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "http://java.sun.com/dtd/preferences.dtd");
			DOMSource source = new DOMSource(documentTargetUser);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			StreamResult result = new StreamResult(os);
			transformer.transform(source, result);

			String resultPreferences = os.toString();
			Preferences.importPreferences(new ByteArrayInputStream(resultPreferences.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {

		}
		catch(TransformerException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(XPathExpressionException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(SAXException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(ParserConfigurationException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(CommonFinderException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
		catch(InvalidPreferencesFormatException e) {
			e.printStackTrace();
			throw new NuclosBusinessException(e);
		}
	}

	/**
	 * reads a <code>List</code> of generic objects from the preferences node
	 * specified by <code>prefs.node(sNode)</code>.
	 * @param prefs
	 * @param sNode
	 * @param pio specify how to read a single element of the list.
	 * @return possibly empty list
	 * @postcondition result != null
	 */
	public static <T> List<T> getGenericList(Preferences prefs, String sNode, PreferencesIO<T> pio) throws PreferencesException {
		final ArrayList<T> result = new ArrayList<T>();

		if (nodeExists(prefs, sNode)) {
			prefs = prefs.node(sNode);
			final int iSize = prefs.getInt(PREFS_KEY_LIST_SIZE, -1);
			if (iSize >= 0) {
				result.ensureCapacity(iSize);
				for (int i = 0; i < iSize; ++i) {
					// use a separate node for each element:
					final Preferences prefsElement = prefs.node(String.valueOf(i));
					result.add(pio.get(prefsElement));
				}
			}
		}

		return result;
	}

	/**
	 * writes a generic <code>List</code> to the preferences node
	 * specified by <code>prefs.node(sNode)</code>.
	 * That preferences node is completely removed before writing.
	 * @param prefs
	 * @param sNode
	 * @param lst
	 * @param pio
	 */
	public static <T> void putGenericList(Preferences prefs, String sNode, List<? extends T> lst, PreferencesIO<T> pio)
	throws PreferencesException {
		if (lst == null) {
			throw new IllegalArgumentException("lst");
		}
		int length = lst.size();
		prefs = getEmptyNode(prefs, sNode);
		prefs.putInt(PREFS_KEY_LIST_SIZE, length);
		for (int i = 0; i < length; ++i) {
			// use a separate node for each element:
			final Preferences prefsElement = prefs.node(String.valueOf(i));
			pio.put(prefsElement, lst.get(i));
		}
	}

	/**
	 * reads a String array from the preferences node specified by <code>prefs.node(sNode)</code>. If the given node
	 * doesn't exist, <code>null</code> is returned.
	 * @param prefs
	 * @param sNode
	 * @return the <code>String[]</code> stored under <code>prefs.node(sNode)</code>, if any.
	 */
	public static String[] getStringArrayOrNull(Preferences prefs, String sNode) throws PreferencesException {
		String[] result = null;

		if (nodeExists(prefs, sNode)) {
			prefs = prefs.node(sNode);
			final int iSize = prefs.getInt(PREFS_KEY_LIST_SIZE, -1);
			if (iSize >= 0) {
				result = new String[iSize];
				for (int i = 0; i < iSize; ++i) {
					result[i] = prefs.get(String.valueOf(i), null);
				}
			}
		}
		return result;
	}

	/**
	 * reads a String array from the preferences node specified by <code>prefs.node(sNode)</code>. If the given node
	 * doesn't exist, an empty array is returned.
	 * @param prefs
	 * @param sNode
	 * @return the <code>String[]</code> stored under <code>prefs.node(sNode)</code> or an empty array.
	 * @postcondition result != null
	 */
	public static String[] getStringArray(Preferences prefs, String sNode) throws PreferencesException {
		final String[] as = PreferencesUtils.getStringArrayOrNull(prefs, sNode);
		final String[] result = (as == null) ? new String[0] : as;

		assert result != null;
		return result;
	}

	/**
	 * writes a String array to the preferences node specified by <code>prefs.node(sNode)</code>.
	 * That preferences node is completely removed before writing.
	 * @param prefs
	 * @param sNode
	 * @param as
	 */
	public static void putStringArray(Preferences prefs, String sNode, String[] as) throws PreferencesException {
		prefs = getEmptyNode(prefs, sNode);
		prefs.putInt(PREFS_KEY_LIST_SIZE, as.length);
		for (int i = 0; i < as.length; ++i) {
			prefs.put(String.valueOf(i), as[i]);
		}
	}

	/**
	 * reads a String list from the preferences node specified by <code>prefs.node(sNode)</code>.
	 * If the given node doesn't exist, an empty list is returned.
	 * @param prefs
	 * @param sNode
	 * @return List<String>
	 * @postcondition result != null
	 */
	public static ArrayList<String> getStringList(Preferences prefs, String sNode) throws PreferencesException {
		final String[] as = PreferencesUtils.getStringArray(prefs, sNode);
		// Note that we create an extra ArrayList around Arrays.asList, so the result allows for
		// removal of single elements.
		final ArrayList<String> result = (as == null) ? new ArrayList<String>() : new ArrayList<String>(Arrays.asList(as));
		assert result != null;
		return result;
	}

	/**
	 * writes a String list to the preferences node specified by <code>prefs.node(sNode)</code>.
	 * That preferences node is completely removed before writing.
	 * @param prefs
	 * @param sNode
	 * @param list
	 */
	public static void putStringList(Preferences prefs, String sNode, List<String> list) throws PreferencesException {
		prefs = getEmptyNode(prefs, sNode);
		prefs.putInt(PREFS_KEY_LIST_SIZE, list.size());
		final Iterator<String> iter = list.listIterator();
		int i = 0;
		while (iter.hasNext()) {
			prefs.put(String.valueOf(i++), iter.next());
		}
	}

	/**
	 * reads an Integer array from the preferences node specified by <code>prefs.node(sNode)</code>.
	 * If the given node does not exist, <code>null</code> is returned.
	 * If a value is not in the backing store, the corresponding element is Integer(-1).
	 * There is no way to tell if <code>-1</code> was read or if there was no entry.
	 * @param prefs
	 * @param sNode
	 * @return the <code>Integer[]</code> stored under <code>prefs.node(sNode)</code>, if any.
	 */
	public static Integer[] getIntegerArrayOrNull(Preferences prefs, String sNode) throws PreferencesException {
		Integer[] result = null;

		if (nodeExists(prefs, sNode)) {
			prefs = prefs.node(sNode);
			final int iSize = prefs.getInt(PREFS_KEY_LIST_SIZE, -1);
			if (iSize >= 0) {
				result = new Integer[iSize];
				for (int i = 0; i < iSize; ++i) {
					result[i] = prefs.getInt(String.valueOf(i), -1);
				}
			}
		}
		return result;
	}

	/**
	 * reads an Integer array from the preferences node specified by <code>prefs.node(sNode)</code>.
	 * If a value is not in the backing store, the corresponding element is Integer(-1).
	 * There is no way to tell if <code>-1</code> was read or if there was no entry.
	 * @param prefs
	 * @param sNode
	 * @return the <code>Integer[]</code> stored under <code>prefs.node(sNode)</code> or an empty array.
	 * @postcondition result != null
	 */
	public static Integer[] getIntegerArray(Preferences prefs, String sNode) throws PreferencesException {
		final Integer[] ai = PreferencesUtils.getIntegerArrayOrNull(prefs, sNode);
		final Integer[] result = (ai == null) ? new Integer[0] : ai;

		assert result != null;
		return result;
	}

	/**
	 * writes an Integer array to the preferences node specified by <code>prefs.node(sNode)</code>.
	 * That preferences node is completely removed before writing.
	 * @param prefs
	 * @param sNode
	 * @param ai
	 */
	public static void putIntegerArray(Preferences prefs, String sNode, Integer[] ai) throws PreferencesException {
		prefs = getEmptyNode(prefs, sNode);
		prefs.putInt(PREFS_KEY_LIST_SIZE, ai.length);
		for (int i = 0; i < ai.length; ++i) {
			prefs.putInt(String.valueOf(i), ai[i]);
		}
	}

	/**
	 * reads an Integer list from the preferences node specified by <code>prefs.node(sNode)</code>.
	 * If a value is not in the backing store, the corresponding element is Integer(-1).
	 * There is no way to tell if <code>-1</code> was read or if there was no entry.
	 * @param prefs
	 * @param sNode
	 * @return
	 * @postcondition result != null
	 */
	public static List<Integer> getIntegerList(Preferences prefs, String sNode) throws PreferencesException {
		final Integer[] ai = PreferencesUtils.getIntegerArray(prefs, sNode);
		// Note that we create an extra ArrayList around Arrays.asList, so the result allows for
		// removal of single elements.
		final List<Integer> result = (ai == null) ? new ArrayList<Integer>() : new ArrayList<Integer>(Arrays.asList(ai));
		assert result != null;
		return result;
	}

	/**
	 * writes an Integer list to the preferences node specified by <code>prefs.node(sNode)</code>.
	 * That preferences node is completely removed before writing.
	 * @param prefs
	 * @param sNode
	 * @param list
	 */
	public static void putIntegerList(Preferences prefs, String sNode, List<Integer> list) throws PreferencesException {
		prefs = getEmptyNode(prefs, sNode);
		prefs.putInt(PREFS_KEY_LIST_SIZE, list.size());
		final Iterator<Integer> iter = list.listIterator();
		int i = 0;
		while (iter.hasNext()) {
			prefs.putInt(String.valueOf(i++), iter.next());
		}
	}

	/**
	 * reads a serializable object from the given preferences.
	 * @param prefs
	 * @param sKey
	 * @return null if the object can't be found in the preferences.
	 */
	public static Object getSerializable(Preferences prefs, String sKey) throws PreferencesException {
		Object result = null;
		final byte[] ab = prefs.getByteArray(sKey, null);
		if (ab != null) {
			try {
				final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ab));
				result = ois.readObject();
				ois.close();
			}
			catch (IOException ex) {
				throw new PreferencesException(ex);
			}
			catch (ClassNotFoundException ex) {
				throw new PreferencesException(ex);
			}
		}
		return result;
	}

	/**
	 * writes a serializable object to the given preferences.
	 * @param prefs
	 * @param sKey
	 * @param o must be serializable
	 * @todo change type of o to Serializable
	 */
	public static void putSerializable(Preferences prefs, String sKey, Object o) throws PreferencesException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			prefs.putByteArray(sKey, baos.toByteArray());
			oos.close();
			baos.close();
		}
		catch (IOException ex) {
			throw new PreferencesException(ex);
		}
	}

	/**
	 * Note: this is the matching reader for the deprecated method putSerializableList
	 * above. Removed the deprecation tag as it may be needed as a fallback method
	 * for old prefs settings even in non-deprecated code.
	 *
	 * @param prefs
	 * @param sNode
	 * @return
	 * @throws PreferencesException
	 * @postcondition result != null
	 * deprecated Don't serialize objects to the preferences!
	 */
	public static List<?> getSerializableList(Preferences prefs, String sNode) throws PreferencesException {
		return getGenericList(prefs, sNode, new SerializablePreferencesIO());
	}


	/**
	 * New version of putSerializableList using xml-encoding underneath the hood.
	 *
	 * @param prefs  the prefs object
	 * @param key    the preferences-key
	 * @param list   the list to put
	 * @throws PreferencesException
	 */
	public static void putSerializableListXML(Preferences prefs, String key, List<?> list) throws PreferencesException {
		new XMLSerializationIO(key).put(prefs, list);
	}

	public static Object getSerializableObject(Preferences prefs, String key) throws PreferencesException {
		return new XMLSerializationIO(key).get(prefs);
	}

	public static void putSerializableObjectXML(Preferences prefs, String key, Object o) throws PreferencesException {
		new XMLSerializationIO(key).put(prefs, o);
	}

	/**
	 * Matching reader method for putSerializableListXML above.
	 * @param prefs   the prefs object
	 * @param key     the preferences-key
	 * @return a list, or null
	 *
	 * @throws PreferencesException
	 */
	public static List<?> getSerializableListXML(Preferences prefs, String key) throws PreferencesException {
		Object o = new XMLSerializationIO(key).get(prefs);
		if(o != null && o instanceof List<?>)
			return (List<?>) o;
		return null;
	}



	/**
	 * reads a rectangle from the preferences
	 * @param prefs the preferences base path
	 * @param sName the node relative to the base path
	 * @param iDefaultWidth the default width for the rectangle, if no entry was found
	 * @param iDefaultHeight the default height for the rectangle, if no entry was found
	 */
	public static Rectangle getRectangle(Preferences prefs, String sName, int iDefaultWidth, int iDefaultHeight) {
		/** @todo this should not build a node for a non-existing node! */
		final Preferences node = prefs.node(sName);

		final int x = node.getInt(PREFS_KEY_RECTANGLE_X, 0);
		final int y = node.getInt(PREFS_KEY_RECTANGLE_Y, 0);
		final int width = node.getInt(PREFS_KEY_RECTANGLE_WIDTH, iDefaultWidth);
		final int height = node.getInt(PREFS_KEY_RECTANGLE_HEIGHT, iDefaultHeight);

		return new Rectangle(x, y, width, height);
	}

	/**
	 * writes the given rectangle to the preferences
	 * @param prefs the preferences base path
	 * @param sName the node relative to the base path
	 * @param rect the rectangle
	 */
	public static void putRectangle(Preferences prefs, String sName, Rectangle rect) {
		final Preferences node = prefs.node(sName);

		node.putInt(PREFS_KEY_RECTANGLE_X, rect.x);
		node.putInt(PREFS_KEY_RECTANGLE_Y, rect.y);
		node.putInt(PREFS_KEY_RECTANGLE_WIDTH, rect.width);
		node.putInt(PREFS_KEY_RECTANGLE_HEIGHT, rect.height);
	}

	/**
	 * reads the bounds of the given component from the preferences
	 * @param prefs the preferences base path
	 * @param sName the node relative to the base path
	 * @param comp the component
	 * @param iDefaultWidth the default width for the component, if no entry was found
	 * @param iDefaultHeight the default height for the component, if no entry was found
	 * @precondition comp != null
	 */
	public static void readBounds(Preferences prefs, String sName, Component comp, int iDefaultWidth, int iDefaultHeight) {
		final Rectangle rect = getRectangle(prefs, sName, iDefaultWidth, iDefaultHeight);

		comp.setBounds(rect);
	}

	/**
	 * writes the bounds of the given component to the preferences
	 * @param prefs the preferences base path
	 * @param sName the node relative to the base path
	 * @param comp the component
	 * @precondition comp != null
	 */
	public static void writeBounds(Preferences prefs, String sName, Component comp) {
		putRectangle(prefs, sName, comp.getBounds());
	}

//	unused
//	/**
//	 * reads the window state of a <code>frame</code> from the preferences.
//	 * The state consists of the extendedState and bounds properties of the frame.
//	 * @param prefs the preferences base path
//	 * @param frame the frame whose state is to be restored
//	 * @param iDefaultState the default state for the frame, if no entry was found
//	 * @param iDefaultWidth the default width for the component, if no entry was found
//	 * @param iDefaultHeight the default height for the component, if no entry was found
//	 */
//	public static void readWindowState(Preferences prefs, CommonJFrame frame, int iDefaultState, int iDefaultWidth, int iDefaultHeight) {
//		/** @todo this should not build a node for a non-existing node! */
//		try {
//			final Preferences node = prefs.node(PREFS_NODE_WINDOWSTATE);
//			final Rectangle rectNormalBounds = getRectangle(node, PREFS_KEY_NORMAL_BOUNDS, iDefaultWidth, iDefaultHeight);
//
//			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
//			if(rectNormalBounds.x > (screenSize.width - 100) || rectNormalBounds.y > (screenSize.height - 100)) {
//				rectNormalBounds.x = 0;
//				rectNormalBounds.y = 0;
//			}
//
//			frame.setBounds(rectNormalBounds);
//			frame.setNormalBounds(rectNormalBounds);
//
//			frame.setExtendedState(node.getInt(PREFS_KEY_WINDOW_EXTENDEDSTATE, iDefaultState));
//		}
//		catch(IllegalStateException e) {
//			e.printStackTrace();
//		}
//	}
//
//	/**
//	 * writes the window state of a <code>frame</code> to the preferences.
//	 * The state consists of the extendedState and bounds properties of the frame.
//	 * @param prefs the preferences base path
//	 * @param frame the frame whose state is to be stored
//	 */
//	public static void writeWindowState(Preferences prefs, CommonJFrame frame) {
//		final Preferences node = prefs.node(PREFS_NODE_WINDOWSTATE);
//		node.putInt(PREFS_KEY_WINDOW_EXTENDEDSTATE, frame.getExtendedState());
//
//		/*
//		 * LINDA-1912
//		 * check abnormal values for width and height
//		 */
//		Rectangle rect = frame.getNormalBounds();
//
//		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
//		if(rect.height > dim.height) {
//			rect.height = dim.height;
//		}
//
//		if(rect.width > dim.width) {
//			rect.width = dim.width;
//		}
//
//		putRectangle(node, PREFS_KEY_NORMAL_BOUNDS, rect);
//	}

	/**
	 * reads the window state of a <code>frame</code> from the preferences.
	 * The state consists of the "maximum", "icon" and "bounds" properties of the frame.
	 * @param prefs the preferences base path
	 * @param iframe the frame whose state is to be restored
	 * @param iDefaultWidth the default width for the component, if no entry was found
	 * @param iDefaultHeight the default height for the component, if no entry was found
	 */
	public static void readWindowState(Preferences prefs, JInternalFrame iframe, int iDefaultWidth,
		int iDefaultHeight) {
		/** @todo this should not build a node for a non-existing node! */
		final Preferences node = prefs.node(PREFS_NODE_WINDOWSTATE);

		try {
			//			iframe.setIcon(false);
			iframe.setMaximum(false);
		}
		catch (PropertyVetoException ex) {
			// ignore
			log.debug(ex);
		}

		final Rectangle rectNormalBounds = getRectangle(node, PREFS_KEY_NORMAL_BOUNDS, iDefaultWidth, iDefaultHeight);
		iframe.setBounds(rectNormalBounds);
		//		iframe.setNormalBounds(rectNormalBounds);

		try {
			//			iframe.setIcon(node.getBoolean(PREFS_KEY_ISICONIFIED, false));
			iframe.setMaximum(node.getBoolean(PREFS_KEY_ISMAXIMIZED, false));
		}
		catch (PropertyVetoException ex) {
			// ignore
			log.debug(ex);
		}
	}

	/**
	 * writes the window state of a <code>frame</code> to the preferences.
	 * The state consists of the "maximum", "icon" and "bounds" properties of the frame.
	 * @param prefs the preferences base path
	 * @param iframe the frame whose state is to be stored
	 */
	public static void writeWindowState(Preferences prefs, JInternalFrame iframe) {
		final Preferences node = prefs.node(PREFS_NODE_WINDOWSTATE);
		node.putBoolean(PREFS_KEY_ISMAXIMIZED, iframe.isMaximum());
		node.putBoolean(PREFS_KEY_ISICONIFIED, iframe.isIcon());

		/*
		 * LINDA-1912
		 * check abnormal values for width and height
		 */
		Rectangle rect = iframe.getNormalBounds();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		if(rect.height > dim.height) {
			rect.height = dim.height;
		}

		if(rect.width > dim.width) {
			rect.width = dim.width;
		}

		putRectangle(node, PREFS_KEY_NORMAL_BOUNDS, rect);
	}

	/**
	 * convenience method encapsulating <code>BackingStoreException</code>.
	 * @param prefs
	 * @param sNode
	 * @return <code>prefs.nodeExists(sNode)</code>
	 * @throws PreferencesException
	 */
	public static boolean nodeExists(Preferences prefs, String sNode) throws PreferencesException {
		try {
			return prefs.nodeExists(sNode);
		}
		catch (BackingStoreException ex) {
			throw new PreferencesException(ex);
		}
	}

	/**
	 * gets the node specified by <code>prefs</code> and <code>sNode</code>, creating it if needed.
	 * It is guaranteed that the result does not contain any key/value pairs or subnodes.
	 * @param prefs
	 * @param sNode
	 * @return <code>prefs.node(sNode)</code>
	 * @throws org.nuclos.common2.exception.PreferencesException
	 * @postcondition prefs.nodeExists(sNode)
	 * @postcondition result.childrenNames().length == 0;
	 */
	public static Preferences getEmptyNode(Preferences prefs, String sNode) throws PreferencesException {
		try {
			if (prefs.nodeExists(sNode)) {
				// remove the node completely (including all of its subnodes)
				prefs.node(sNode).removeNode();
			}
			final Preferences result = prefs.node(sNode);

			assert prefs.nodeExists(sNode);
			assert result.childrenNames().length == 0;
			return result;
		}
		catch (BackingStoreException ex) {
			throw new PreferencesException(ex);
		}
	}


	/**
	 * Remove a child node or key of a given key name from a preferences node.
	 * Designed to catch and ignore underlying exceptions, thus safely callable
	 * in any attempt to clean up.
	 *
	 * @param prefs          the preferences node
	 * @param keyOrSubnode   the key to remove
	 */
	public static void removeChild(Preferences prefs, String keyOrSubnode) {
		try {
			if(prefs.nodeExists(keyOrSubnode))
				prefs.node(keyOrSubnode).removeNode();
		}
		catch(BackingStoreException e) {/*ign*/}
		prefs.remove(keyOrSubnode);
	}

	public static void writeSplitPaneStateToPrefs(Preferences prefsParent, String sKey, JSplitPane splitpn) {
		final Preferences prefs = prefsParent.node(sKey);
		prefs.putInt(PREFS_KEY_DIVIDER_LOCATION, splitpn.getDividerLocation());
		prefs.putInt(PREFS_KEY_LAST_DIVIDER_LOCATION, splitpn.getLastDividerLocation());
		prefs.putInt(PREFS_KEY_ORIENTATION, splitpn.getOrientation());
	}

	public static void readSplitPaneStateFromPrefs(Preferences prefsParent, String sKey, JSplitPane splitpn) {
		final Preferences prefs = prefsParent.node(sKey);
		splitpn.setDividerLocation(prefs.getInt(PREFS_KEY_DIVIDER_LOCATION, splitpn.getDividerLocation()));
		splitpn.setLastDividerLocation(prefs.getInt(PREFS_KEY_LAST_DIVIDER_LOCATION, splitpn.getLastDividerLocation()));
	}

}  // class PreferencesUtils