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
/*
 * Created on 24.09.2009
 */
package org.nuclos.client.common.prefs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.nuclos.common2.ServiceLocator;
import org.nuclos.common2.exception.CommonBusinessException;
import org.nuclos.common2.exception.CommonFatalException;
import org.nuclos.common2.exception.CommonFinderException;
import org.nuclos.common2.exception.CommonRemoteException;
import org.nuclos.common.DynamicCollectionIterator;
import org.nuclos.server.common.ejb3.PreferencesFacadeRemote;
import org.nuclos.server.common.valueobject.PreferencesVO;

/** WebAccessPref
 * Object of the Java Userpreferencesobject for the webclient
 * @author oliver.helbig
 *
 */
public class WebAccessPrefs {
   private static final String	PREFS_BASE	= "org/nuclos/client";

   private Document _d;
   private List<String> idList = makeList();
   private Document getDoc(){
      if(_d == null) {
      	SAXBuilder sxbuild = new SAXBuilder(false);
      	sxbuild.setFeature("http://xml.org/sax/features/validation", false);
      	sxbuild.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar",	false);
      	sxbuild.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
         try {
            PreferencesVO vo = ServiceLocator.getInstance().getFacade(PreferencesFacadeRemote.class).getUserPreferences();
            byte[] xml = vo.getPreferencesBytes();
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            // Variante 1 (keinerlei Validierung):
            // http://groups.google.com/group/de.comp.lang.java/browse_thread/thread/249392b022b1f99a
            class EmptyEntityResolver implements EntityResolver {
               /* (non-Javadoc)
                * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
                */
               @Override
			public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                  return new InputSource(new StringReader(""));
               }
            }
            db.setEntityResolver(new EmptyEntityResolver());
            _d = sxbuild.build(new ByteArrayInputStream(xml));
         }
         catch(CommonRemoteException e) {
            throw new CommonFatalException("Could not read user preferences", e);
         }
         catch(CommonFinderException e) {

   			Preferences prefs = new DummyPreferences();
   			prefs = prefs.userRoot().node("org/nuclos/client/collect/entity");

   			final ByteArrayOutputStream os = new ByteArrayOutputStream();
   			try {
					prefs.exportSubtree(os);
					_d = sxbuild.build(new ByteArrayInputStream(os.toString("UTF-8").getBytes()));
				}
				catch(IOException e1) {
					throw new CommonFatalException("Could not read user preferences", e1);
				}
				catch(BackingStoreException e1) {
					throw new CommonFatalException("Could not read user preferences", e1);
				}
				catch(JDOMException e1) {
					throw new CommonFatalException("Could not read user preferences", e1);
				}

         }
         catch(ParserConfigurationException e) {
            throw new CommonFatalException("Could not read user preferences", e);
         }
         catch(RuntimeException e) {
            throw new CommonFatalException("Could not read user preferences", e);
         }
         catch(UnsupportedEncodingException e) {
         	 throw new CommonFatalException("Could not read user preferences", e);
         }
         catch(JDOMException e) {
         	 throw new CommonFatalException("Could not read user preferences", e);
         }
         catch(IOException e) {
         	 throw new CommonFatalException("Could not read user preferences", e);
         }
      }
      return _d;
   }

	   /** return the document as string
	    * @return String
	    * @throws TransformerException
	    */
	   public String getAsXML() throws TransformerException
	   {
	   	return new XMLOutputter(Format.getPrettyFormat()).outputString(_d);
	   }

	   /**Generates the id list (synchronized)
	    * @return
	    */
	   private static List<String> makeList() {
	      List<String> list = new ArrayList<String>();
	      return Collections.synchronizedList(list);
	   }

   	public List<String> getList() {
         return idList;
      }


   /** Adds an entry to the node
    * @param id
    * @param map key value pair
    * @throws CommonBusinessException
    */
   @SuppressWarnings("unchecked")
	public void addEntry(String id, HashMap<String, String> map)throws CommonBusinessException{

      if(!id.startsWith(PREFS_BASE))
         id = PREFS_BASE + "/" + id;

   	getList().clear();

      for (String key : id.split("\\/")) {
			getList().add(key);
		}

      Element root = _d.getRootElement();
      Element element = root.getChild("root");
      Element elem = getChildren(element);

		List children = elem.getChild("map").getChildren("entry");

		children.clear();
		Element newElement;
		for(String key : map.keySet()) {
			newElement = new Element("entry");
			newElement.setAttribute("key", key);
			newElement.setAttribute("value", map.get(key));

			children.add(newElement);
		}
   }

   @SuppressWarnings("unchecked")
	public Map<String, String> getPrefsMap(String id) throws CommonBusinessException {

		getDoc();

   	if(!id.startsWith(PREFS_BASE))
         id = PREFS_BASE + "/" + id;

   	getList().clear();

      for (String key : id.split("\\/")) {
			getList().add(key);
		}

      Element root = _d.getRootElement();
      Element element = root.getChild("root");
      Element elem = getChildren(element);

		HashMap<String, String> res = new HashMap<String, String>();
		List children = elem.getChild("map").getChildren("entry");
		for(Object child : children) {
			Element entry = (Element) child;
			res.put(entry.getAttributeValue("key"), entry.getAttributeValue("value"));
		}

      return res;
   }

   /** Return the child: child will be created if no child was found
    * @postcondition: the idList must be set with the path of the element
    * @param element
    * @return
    */
   @SuppressWarnings("unchecked")
	private Element getChildren(Element element){
      List childern = element.getChildren("node");

      Iterator<String> it = new DynamicCollectionIterator<String>(idList);
      boolean found = true;
      while (it.hasNext()) {
			String path = it.next();

			if(childern.isEmpty())
				found =false;

			for(Object child : childern) {
				Element element2 = (Element) child;
	      	if(element2.getAttributeValue("name").equals(path)){
	      		getList().remove(path);
	      		found = true;
	      		return getChildren(element2);
	      	}else{
	      		found = false;
	      	}
			}
			if(!found)
			{
				String nodeName = getList().get(0);
				System.out.println("create node " + nodeName);
				Element newNode = new Element("node");
				newNode.setAttribute("name", nodeName);

				Element newMap = new Element("map");
				newNode.getChildren().add(newMap);
				element.getChildren().add(newNode);
				getList().remove(nodeName);

				return getChildren(newNode);

			}
      }
	return element;
   }
}
