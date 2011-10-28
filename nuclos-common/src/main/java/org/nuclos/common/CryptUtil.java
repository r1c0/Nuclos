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
package org.nuclos.common;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

public class CryptUtil {
	
	private static final Logger LOG = Logger.getLogger(CryptUtil.class);
	
	private CryptUtil() {}

	private static final String WRONG_PASSLEN_MESSAGE = "passphrase must be exactly 16 bytes";
	private static final String AES_CBC_PAD = "AES/CBC/PKCS5Padding";
	private static final String AES = "AES";

	/**
	 * Encrypt a String with a 16-bit passphrase via AES. Encodes the result
	 * as a hex-string with 4 bytes per char
	 *
	 * @param message  the string to encrypt
	 * @param pass     passphrase
	 * @return encrypted string
	 */
	public static String encryptAESHex(String message, byte[] pass) {
		return toHex(encryptAES(message, pass));
	}

	/**
	 * Decrypt a String as returned by encryptAESHex above
	 *
	 * @param hexMessage  the encrypted string
	 * @param pass        passphrase
	 * @return decrypted string
	 */
	public static String decryptAESHex(String hexMessage, byte[] pass) {
		return decryptAES(fromHex(hexMessage), pass);
	}

	public static byte[] encryptAES(String string, byte[] key) {
		if(key.length != 16)
			throw new IllegalArgumentException(WRONG_PASSLEN_MESSAGE);

		try {
			Cipher cipher = Cipher.getInstance(AES_CBC_PAD);
			IvParameterSpec ivSpec = new IvParameterSpec(key);
			SecretKeySpec skeySpec = new SecretKeySpec(key, AES);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec);
			return cipher.doFinal(string.getBytes("UTF-8"));
		}
		catch(Exception e) {
			throw new RuntimeException("encyption failure", e);
		}
	}

	public static String decryptAES(byte[] bytes, byte[] key) {
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

	public static String toHex(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for(byte b : bytes)
			sb.append(String.format("%02x", b & 0xff));
		return sb.toString();
	}

	public static byte[] fromHex(String s) {
		byte[] bytes = new byte[s.length() / 2];
		for(int i = 0, n = s.length(); i < n; i += 2)
			bytes[i / 2] = (byte) (Integer.parseInt(s.substring(i, i + 2), 16) & 0xff);
		return bytes;
	}

	/**
	 * Determine the size needed to store an input string of inputLength when
	 * encrypted with encryptAESHex.
	 * @param inputLength
	 * @return size needed
	 */
	public static int calcSizeForAESHexInputLength(int inputLength) {
		return ((inputLength * 2 + 32) / 32) * 32; //note: integer arithmetics!
	}

	public static void main(String[] args) {
		byte[] CRYPT = new byte[16];
		new Random().nextBytes(CRYPT);
		for(byte b : CRYPT)
			LOG.debug("(byte) 0x" + String.format("%02x", b) + ", ");
	}
}
