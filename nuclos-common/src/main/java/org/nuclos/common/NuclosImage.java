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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import org.nuclos.common2.IOUtils;
import org.nuclos.common2.LangUtils;

public class NuclosImage implements Serializable, NuclosAttributeExternalValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String sFileName;
	byte[] content;
	byte[] thumbnail;
	boolean bProduce;

	public static int thumbsize = 20;

	public NuclosImage() {
		sFileName = "";
		bProduce = false;
	}

	public NuclosImage(String fileName, byte[] content, byte[] thumbnail, boolean produceThumbnail) {
		this.sFileName = fileName;
		this.content = content;
		this.thumbnail = thumbnail;
		if(produceThumbnail)
			produceThumbnail();
	}

	public String getFilename() {
		return sFileName;
	}

	public byte[] getContent() {
		return content;
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThmubnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof NuclosImage) {
			NuclosImage that = (NuclosImage)obj;
			return LangUtils.equals(this.sFileName, that.sFileName);
		}
		return false;
	}

	@Override
	public String toString() {
		return sFileName;
	}

	public void produceThumbnail() {
		try {
			Object o = SpringApplicationContextHolder.getBean("parameterProvider");
			if (o != null && o instanceof ParameterProvider) {
				ParameterProvider pp = (ParameterProvider) o;
				String sThumbnailsize = pp.getValue(ParameterProvider.KEY_THUMBAIL_SIZE);

				int iHeight = thumbsize;
				int iWidth = thumbsize;

				try {
					String s[] = sThumbnailsize.split("\\*");
					iWidth = Integer.parseInt(s[0]);
					iHeight = Integer.parseInt(s[1]);
				}
				catch(Exception e) {
					// do nothing here
				}

				BufferedImage buff = ImageIO.read(new ByteArrayInputStream(this.content));
				BufferedImage bdest = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bdest.createGraphics();
			    AffineTransform at = AffineTransform.getScaleInstance((double)iWidth/buff.getWidth(), (double)iHeight/buff.getHeight());
			    g.drawRenderedImage(buff,at);
			    File scaled = new File(IOUtils.getDefaultTempDir() + "tmp.jpg");
			    scaled.deleteOnExit();
			    ImageIO.write(bdest,"JPG", scaled);

				BufferedInputStream bisscaled = new BufferedInputStream(new FileInputStream(scaled));
				byte bscaled[] = new byte[bisscaled.available()];
				int cscaled = 0;
				int counterscaled = 0;
				while((cscaled = bisscaled.read()) != -1) {
					bscaled[counterscaled++] = (byte)cscaled;
				}
				bisscaled.close();

				setThmubnail(bscaled);
			}
		}
		catch(Exception ex) {
			// thumbnail can't be set
		}
	}


}
