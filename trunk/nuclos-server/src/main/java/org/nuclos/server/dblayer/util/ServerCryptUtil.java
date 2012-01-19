//Copyright (C) 2011  Novabit Informationssysteme GmbH
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
package org.nuclos.server.dblayer.util;

import java.sql.SQLException;

import org.nuclos.common.CryptUtil;
import org.nuclos.common.ParameterProvider;
import org.nuclos.common2.StringUtils;
import org.nuclos.server.common.ServerParameterProvider;

/**
 * @author Thomas Pasch
 * @since Nuclos 3.2.0
 */
public class ServerCryptUtil {
	
	private ServerCryptUtil() {
		// Never invoked.
	}

    public static String encrypt(String s) throws SQLException {
    	if(s == null)
    		return null;

    	byte[] b = getCipher();
    	if(b != null)
    		s = CryptUtil.encryptAESHex(s, b);
        return s;
    }

    public static String decrypt(String s) throws SQLException {
    	if(s == null)
    		return null;

    	byte[] b = getCipher();
    	if(b != null)
    		s = CryptUtil.decryptAESHex(s, b);
        return s;
    }

    private static byte[] getCipher() throws SQLException {
        String cipher = StringUtils.nullIfEmpty(ServerParameterProvider.getInstance().getValue(ParameterProvider.KEY_CIPHER));
        if(cipher == null)
            return null;

        if(cipher.length() != 32)
        	throw new SQLException("Server parameter " + ParameterProvider.KEY_CIPHER + " illegal, length != 32 - unable to en-/decrypt fields");

        return CryptUtil.fromHex(cipher);
    }

}
