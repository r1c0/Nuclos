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

import org.nuclos.common2.exception.CommonFatalException;
import java.text.*;

/**
 * The current JDK (1.4) does not provide support for traditional German ("phonebook") collation.
 * This class does. Note that the "modern" collation provided by <code>Collator.getInstance(Locale.GERMANY)</code>
 * is compatible with DIN 5007 (which is very similar to the collation proposed by the "Duden").
 * The "traditional" or "phonebook" collation provided by this method may according to DIN 5007
 * only be used if names are to be collated, as in phonebooks or name directories.
 * @see <a href="http://faql.de/eszett.html">Sortierung von Umlauten</a>
 * @see LangUtils#getTraditionalGermanCollator()
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

class TraditionalGermanCollator extends RuleBasedCollator {
	private static final String sAccents = "=\u0301;\u0300;\u0302;\u0308;\u0327;\u0303;\u0304;\u0305;\u0306;\u0307;\u0309;\u030A" + ";\u030B;\u030C;\u030D;\u030E;\u030F;\u0310;\u0311;\u0312";

	private static final String sTraditionalGermanRules = sAccents + "< a,A < b,B < c,C < d,D < e,E < f,F < g,G < h,H < i,I < j,J < k,K < l,L < m,M < n,N < o,O " + "< p,P < q,Q < r,R < s,S < t,T < u,U < v,V < w,W < x,X < y,Y < z,Z " + "& ae;\u00e4 & AE;\u00c4 & oe;\u00f6 & OE;\u00d6 & ue;\u00fc & UE;\u00dc & ss;\u00df ";

	private static TraditionalGermanCollator singleton;

	private TraditionalGermanCollator() throws ParseException {
		super(sTraditionalGermanRules);

		// accents are sorting relevant ("ss" < "\u00df"):
		this.setStrength(Collator.SECONDARY);
		// no decomposition necessary ("a\u00b4" != "\u00e1")
		this.setDecomposition(Collator.NO_DECOMPOSITION);
	}

	public static synchronized Collator getInstance() {
		if (singleton == null) {
			try {
				singleton = new TraditionalGermanCollator();
			}
			catch (ParseException ex) {
				throw new CommonFatalException(ex);
			}
		}
		assert singleton != null;
		return singleton;
	}

}  // class TraditionalGermanCollator
