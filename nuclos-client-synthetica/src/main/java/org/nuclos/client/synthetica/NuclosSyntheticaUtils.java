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
package org.nuclos.client.synthetica;

import java.text.ParseException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

public class NuclosSyntheticaUtils {

	private static final Logger LOG = Logger.getLogger(NuclosSyntheticaUtils.class);

	private static final String WRONG_PASSLEN_MESSAGE = "passphrase must be exactly 16 bytes";
	private static final String AES_CBC_PAD = "AES/CBC/PKCS5Padding";
	private static final String AES = "AES";

	private static final byte[] CRYPT = new byte[] {
		(byte) 0xfa, (byte) 0x79, (byte) 0xa1, (byte) 0x2c, (byte) 0x81,
		(byte) 0x4b, (byte) 0x6f, (byte) 0x03, (byte) 0xae, (byte) 0xd8,
		(byte) 0x70, (byte) 0x04, (byte) 0xd7, (byte) 0xd2, (byte) 0x6a,
		(byte) 0x9c
	};
	
	public static void setLookAndFeel() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, ParseException {
		setLookAndFeel(null);
	}
	
	public static void registerNuclosTheme(String themeName, String pathToXml) {
		if (NuclosSyntheticaThemeableLookAndFeel.registerNuclosTheme(themeName, pathToXml)) {
			LOG.info("Nuclos theme \"" + themeName + "\" found (" + pathToXml + ")");
		}
	}

    public static void setLookAndFeel(String nuclosTheme) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, ParseException {
		UIManager.put(
			dec("74412a97922b28b6368ca2aff3cdfa3dea7b143aaf0b5f0f39c20bc0573c2760"),
			new String[] {
				dec("8733c987c4ea57f6f59ff76caa2642b7d052003acb575aaa27aaf8d305ab8875e5dbf2231d8e16d94f4cf3425042b906"),
				dec("2a7a0993c6898d3d4ccebfdd81138b5050ba49a2ef588b651248b22daa6a14db4d86fb4b25cc13abeb9be3b89c3ccf72"),
				dec("19aa52f8a2e7fa09ae8edce87135a0828908bb3580c3ed376bd4765a346cd140"),
				dec("2e8be6d2c6078b6cca144c7feaba1564d93d65fea6a184f3c3485a6ccb35ad01aa994e34c94e91bd79af1a77812a83bf"),
				dec("fdb3e4f94fdbb87aafcda33d860ebb60e8e91ce0060798e1ccba6f0e18f306c3"),
				dec("fcf594087c3e7d74fb1edc5e6cce5ad38fac275daad1e56f336439d8cbed7d79")
			});
		UIManager.put(
			dec("74412a97922b28b6368ca2aff3cdfa3d431a5f0a9f8a2f27dad966401d6ddc64"),
			dec("ac0cf046be29e85f56f6499b1f22db2a9f529a04835833e3cd0b10ee6339c93725b53d1b61e197bb5bade15ed86c6928"));
		
		NuclosSyntheticaThemeableLookAndFeel laf = new NuclosSyntheticaThemeableLookAndFeel();
		laf.setNuclosTheme(nuclosTheme);
		NuclosSyntheticaThemeableLookAndFeel.setWindowsDecorated(false);
		NuclosSyntheticaThemeableLookAndFeel.setLookAndFeel(NuclosSyntheticaThemeableLookAndFeel.class.getName(), true, true);		
	}

	private static String dec(String d) {
		return decryptAES(fromHex(d), CRYPT);
	}

	private static byte[] fromHex(String s) {
		byte[] bytes = new byte[s.length() / 2];
		for(int i = 0, n = s.length(); i < n; i += 2)
			bytes[i / 2] = (byte) (Integer.parseInt(s.substring(i, i + 2), 16) & 0xff);
		return bytes;
	}

	private static String decryptAES(byte[] bytes, byte[] key) {
		if(key.length != 16)
			throw new IllegalArgumentException(WRONG_PASSLEN_MESSAGE);

		try {
			IvParameterSpec ivSpec = new IvParameterSpec(key);
			SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
			Cipher cipher = Cipher.getInstance(AES_CBC_PAD);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
			return new String(cipher.doFinal(bytes), "UTF-8");
		}
		catch(Exception e) {
			throw new RuntimeException("decyption failure", e);
		}
	}
}
