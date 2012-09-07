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
package org.nuclos.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.nuclos.client.attribute.AttributeCache;
import org.nuclos.client.common.ClientParameterProvider;
import org.nuclos.client.common.LocaleDelegate;
import org.nuclos.client.common.MetaDataClientProvider;
import org.nuclos.client.common.security.SecurityDelegate;
import org.nuclos.client.customcomp.CustomComponentCache;
import org.nuclos.client.genericobject.GenericObjectLayoutCache;
import org.nuclos.client.genericobject.GenericObjectMetaDataCache;
import org.nuclos.client.masterdata.MasterDataCache;
import org.nuclos.client.masterdata.MetaDataCache;
import org.nuclos.client.resource.ResourceCache;
import org.nuclos.client.rule.RuleCache;
import org.nuclos.client.searchfilter.SearchFilterCache;
import org.nuclos.client.statemodel.StateDelegate;
import org.nuclos.client.tasklist.TasklistCache;
import org.nuclos.common.ApplicationProperties;
import org.nuclos.common.NuclosFatalException;
import org.nuclos.common2.LangUtils;
import org.nuclos.server.common.ejb3.LocalUserCachesFacadeRemote;
import org.springframework.beans.factory.InitializingBean;

/**
 * Local user properties that cannot be stored on the server, because they are needed before
 * the client is connected to the server, such as the user name and the look & feel.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version 01.00.00
 */
public class LocalUserCaches extends java.util.Properties {
	
	private static final Logger LOG = Logger.getLogger(LocalUserCaches.class);

	private static final boolean bUseHashing = false;
	private static final boolean bUseEncryption = true;
	private static final String LOCALUSERCACHES_HASH = "localusercaches.hash";
	private static final String LOCALUSERCACHES_APP_VERSION = "localusercaches.app.version";

	private transient LocalUserCachesFacadeRemote remoteInterface;
	
	public interface LocalUserCache extends Serializable, InitializingBean {
		String getCachingTopic();
		boolean wasDeserialized();
		void setDeserialized(boolean blnDeserialized);
		boolean isValid();
	}
	
	public static abstract class AbstractLocalUserCache implements LocalUserCache {
		boolean blnDeserialized = false;
		
		@Override
		public final void setDeserialized(boolean blnDeserialized) {
			this.blnDeserialized = blnDeserialized;
		}
		
		@Override
		public final boolean wasDeserialized() {
			return blnDeserialized;
		}
		
		@Override
		public final boolean isValid() {
			return wasDeserialized() && LocalUserCaches.getInstance().checkValid(this);
		}
	}
	
	private static LocalUserCaches INSTANCE;

    public static LocalUserCaches getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("too eary");
        }
        return INSTANCE;
    }

    LocalUserCaches() {
    	try {
	        InputStream in = null;
	        try {
	        	if (bUseHashing && !LangUtils.equals(LocalUserProperties.getInstance().get(LOCALUSERCACHES_HASH), getHash())) {
	        		LOG.info("hash missmatch. skipping.");
	        	} else {
		        	if (bUseEncryption)
			        	in = new BufferedInputStream(new CipherInputStream(
			        			new FileInputStream(this.getCachesFile()), createCipher(
			        					Cipher.DECRYPT_MODE, SecurityDelegate.getInstance().getCurrentApplicationInfoOnServer())));
		        	else
			        	in = new BufferedInputStream(new FileInputStream(this.getCachesFile()));
		            load(in);
		            
		            // compare current version
		            if (!LangUtils.equals(ApplicationProperties.getInstance().getCurrentVersion().getSchemaVersion(), get(LOCALUSERCACHES_APP_VERSION))) {
		        		LOG.info("version missmatch. skipping.");
		            	clear();
		            }
	        	}
	        }
	        catch (FileNotFoundException ex) { 
	            // The properties file doesn't exist. or other exception...
	            // So we start with empty or default values.
	        }
	        catch (GeneralSecurityException ex) { 
	            // other exception... just log and start with empty or default values.
	        	LOG.warn("Lokale Caches konnten nicht geladen werden: "  + ex.getMessage());
	        }
	        catch (IllegalArgumentException ex) { 
	            // other exception... just log and start with empty or default values.
	        	LOG.warn("Lokale Caches konnten nicht geladen werden: "  + ex.getMessage());
	        }
	        catch (Exception ex) { 
	            // other exception... just log and start with empty or default values.
	        	LOG.error("Lokale Caches konnten nicht geladen werden: "  + ex.getMessage(), ex);
	        }
	        finally {
	        	if (in != null) {
	        		in.close();
	        	}
	        }
    	}
    	catch (IOException e) {
            final String sMessage = "Lokale Caches konnten nicht geladen werden: " + e.getMessage();
            throw new NuclosFatalException(sMessage, e);
    	}
    	INSTANCE = this;
    }

    /**
     * Spring injection setter.
     */
	public final void setLocalUserCachesService(LocalUserCachesFacadeRemote service) {
		this.remoteInterface = service;
	}

    private File getCachesFile() {
        File fileHomeDir = new File(System.getProperty("user.home"));
        String fileName = ApplicationProperties.getInstance().getAppId() + ".caches";
        fileName = System.getProperty("local.caches.filename", fileName);
        return new File(fileHomeDir, fileName);
    }

	private String serialize(Object object) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
	 
	        oos.writeObject(object);
	        oos.flush();
	        oos.close();
	        
	        return new String(new Base64().encode(bos.toByteArray()));
		} catch (IOException e) {
			LOG.warn("Serializing cache '" + object.getClass().getSimpleName() + "' failed.", e);
		}
		return null;
	}
	
	private Object deserialize(Class<?> clazz) {
		try {
			String in = LocalUserCaches.getInstance().getProperty(clazz.getName());
			ByteArrayInputStream bis = new ByteArrayInputStream(new Base64().decode(in.getBytes()));
			ObjectInputStream ois = new ObjectInputStream(bis);
			
			Object obj = ois.readObject();
			ois.close();
			
			return obj;
		} 
		catch (ClassNotFoundException e) {
			LOG.warn("Deserializing cache '" + clazz.getSimpleName() + "' failed. Maybe file information is missing: " + e);
		}
		catch (IOException e) {
			LOG.warn("Deserializing cache '" + clazz.getSimpleName() + "' failed. Maybe file information is missing: " + e);
		}
		catch (Exception e) {
			LOG.warn("Deserializing cache '" + clazz.getSimpleName() + "' failed. Maybe file information is missing.");
		}
		return null;
	}

	public Object getObject(Class<?> clazz) {
		Object object = deserialize(clazz);
		if (object instanceof LocalUserCache)
			((LocalUserCache)object).setDeserialized(true);
		return object;
	}
	
	public void storeObject(LocalUserCache object) {
		put(object.getClass().getName(), serialize(object));
		// store date of last revalidation.
		Date revalidationDate = remoteInterface.queryLocalCacheRevalidation(object.getCachingTopic());
		put(object.getCachingTopic(), revalidationDate == null ? "" + System.currentTimeMillis() : "" + revalidationDate.getTime());
	}
	
    public void store() {
        try {
        	final BufferedOutputStream out;

        	if (bUseEncryption) {
        		out = new BufferedOutputStream(
            		new CipherOutputStream(new FileOutputStream(this.getCachesFile()),
            				createCipher(Cipher.ENCRYPT_MODE, SecurityDelegate.getInstance().getCurrentApplicationInfoOnServer())));
        	}
        	else {
        		out = new BufferedOutputStream(new FileOutputStream(this.getCachesFile()));
        	}
            	
        	try {
            	storeObject(MetaDataClientProvider.getInstance());
            	storeObject(AttributeCache.getInstance());
            	storeObject(ResourceCache.getInstance());
            	storeObject(GenericObjectMetaDataCache.getInstance());
            	storeObject(MasterDataCache.getInstance());
            	storeObject(MetaDataCache.getInstance());
            	storeObject(GenericObjectLayoutCache.getInstance());
            	//storeObject(SearchFilterCache.getInstance());
            	storeObject(CustomComponentCache.getInstance());
            	storeObject(ClientParameterProvider.getInstance());
            	storeObject(LocaleDelegate.getInstance());
            	storeObject(TasklistCache.getInstance());
            	storeObject(StateDelegate.getInstance());
            	storeObject(RuleCache.getInstance());
            	
            	put(LOCALUSERCACHES_APP_VERSION, ApplicationProperties.getInstance().getCurrentVersion().getSchemaVersion());
            	store(out, ApplicationProperties.getInstance().getAppId() + " Local User Caches");
            }
            finally {
            	out.close();
            }
            
        	if (bUseHashing) {
	            // get sha hash.
            	LocalUserProperties.getInstance().put(LOCALUSERCACHES_HASH, getHash());
            	LocalUserProperties.getInstance().store();
        	}
        }
        catch (GeneralSecurityException e) {
            final String sMessage = "Lokale Caches konnten nicht gespeichert werden: " + e;
            LOG.error(sMessage, e);
        }
        catch (IOException e) {
            final String sMessage = "Lokale Caches konnten nicht gespeichert werden: " + e;
            LOG.error(sMessage, e);
        }
        catch (Exception ex) { 
        	 final String sMessage = "Lokale Caches konnten nicht gespeichert werden: " + ex;
             LOG.error(sMessage, ex);
        }
    }	// store
    
    public boolean checkValid(LocalUserCache cache) {
    	Date revalidationTime = remoteInterface.queryLocalCacheRevalidation(cache.getCachingTopic());
    	if (revalidationTime == null)
    		return true; // no date from server... must be valid - no changes.
    	else {
    		String cachingTime = (String)get(cache.getCachingTopic());
    		if (cachingTime == null)
    			return false; // we do not have a date... continue.
    		
    		Date cachingDate = new Date(new Long(cachingTime));
    		Date revalidationDate = new Date(revalidationTime.getTime());
    		
    		return !cachingDate.before(revalidationDate);
    	}
    }
    
    private static Cipher createCipher(int mode, String password) throws GeneralSecurityException {
        String alg = "PBEWithSHA1AndDESede"; //BouncyCastle has better algorithms
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(alg);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
        // TODO: A fixed salt doesn't help anything.
        cipher.init(mode, secretKey, new PBEParameterSpec("saltsalt".getBytes(), 2000));

        return cipher;
    }
    
    private String getHash() throws GeneralSecurityException, IOException {
    	MessageDigest md = MessageDigest.getInstance("SHA-256");
        FileInputStream fis = new FileInputStream(this.getCachesFile());
 
        byte[] dataBytes = new byte[1024];
 
        int nread = 0; 
        while ((nread = fis.read(dataBytes)) != -1) {
          md.update(dataBytes, 0, nread);
        }
        byte[] mdbytes = md.digest();
 
        //convert the byte to hex format method 1
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mdbytes.length; i++) {
          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        //convert the byte to hex format method 2
        StringBuilder hexString = new StringBuilder();
    	for (int i=0;i<mdbytes.length;i++) {
    	  hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
    	}
    	
    	return hexString.toString();
    }

}	// class LocalUserCaches
