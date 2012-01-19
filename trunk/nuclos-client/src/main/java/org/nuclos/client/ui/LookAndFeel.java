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
package org.nuclos.client.ui;

/**
 * encapsulates look&feel issues.
 * <br>
 * <br>Created by Novabit Informationssysteme GmbH
 * <br>Please visit <a href="http://www.novabit.de">www.novabit.de</a>
 *
 * @author	<a href="mailto:Christoph.Radig@novabit.de">Christoph.Radig</a>
 * @version	01.00.00
 */

public final class LookAndFeel {
	public static final int CROSS_PLATFORM = 0;
	public static final int METAL = 1;
	public static final int WINDOWS = 2;
	public static final int MOTIF = 3;
	public static final int PLATFORM_SPECIFIC = 4;
	public static final int KUNSTSTOFF = 5;
	public static final int N_LOOKANDFEELS = 6;

	private static final String[] aNames = {
		"CrossPlatform", "Metal", "Windows", "Motif", "PlatformSpecific", "Kunststoff"
	};

	private int iLookAndFeel;

	public LookAndFeel() {
		this(CROSS_PLATFORM);
	}

	public LookAndFeel(int iLookAndFeel) {
		this.setLookAndFeel(iLookAndFeel);
	}

	public int getLookAndFeel() {
		return this.iLookAndFeel;
	}

	public void setLookAndFeel(int iLookAndFeel) {
		this.iLookAndFeel = iLookAndFeel;
	}

	public static String getLookAndFeelClassName(int iLookAndFeel) {
		String result = null;

		switch (iLookAndFeel) {
			case CROSS_PLATFORM:
			case METAL:
				result = javax.swing.UIManager.getCrossPlatformLookAndFeelClassName();
				break;

			case WINDOWS:
				result = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
				break;

			case MOTIF:
				result = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
				break;

			case PLATFORM_SPECIFIC:
				result = javax.swing.UIManager.getSystemLookAndFeelClassName();
				break;

			case KUNSTSTOFF:
				result = "com.incors.plaf.kunststoff.KunststoffLookAndFeel";
				break;
		}  // switch

		return result;
	}  // getLookAndFeelClassName

	public String getLookAndFeelClassName() {
		return LookAndFeel.getLookAndFeelClassName(iLookAndFeel);
	}

	public static String getName(int iLookAndFeel) {
		return LookAndFeel.aNames[iLookAndFeel];
	}

	public String getName() {
		return LookAndFeel.getName(iLookAndFeel);
	}

	public static int byName(String sLookAndFeelName) throws IllegalArgumentException {
		for (int i = 0; i < N_LOOKANDFEELS; ++i) {
			if (LookAndFeel.getName(i).equalsIgnoreCase(sLookAndFeelName)) {
				return i;
			}
		}  // for

		throw new IllegalArgumentException("sLookAndFeelName");
	}  // byName

	public String getPlafPackageName() {
		return LookAndFeel.getPlafPackageName(this.iLookAndFeel);
	}

	public static String getPlafPackageName(int iLookAndFeel) {
		String result = null;

		switch (iLookAndFeel) {
			case CROSS_PLATFORM:
			case METAL:
				result = "javax.swing.plaf.metal";
				break;

			case WINDOWS:
				result = "com.sun.java.swing.plaf.windows";
				break;

			case MOTIF:
				result = "com.sun.java.swing.plaf.motif";
				break;

			case PLATFORM_SPECIFIC:
				try {
					String sClassName = javax.swing.UIManager.getSystemLookAndFeelClassName();
					result = Class.forName(sClassName).getPackage().getName();
				}
				catch (ClassNotFoundException ex) {
					result = null;
				}
				break;
		}  // switch

		return result;
	}  // getPlafPackageName

}  // class LookAndFeel
