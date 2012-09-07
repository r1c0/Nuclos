package org.nuclos.client.customcomp.resplan;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.nuclos.client.image.ImageType;
import org.nuclos.client.image.SVGDOMDocumentSupport;

public interface IResPlanExporter<R, E, L> {

	SVGDOMDocumentSupport getSVGDOMDocumentSupport();

	void run(String template, int startCategory) throws IOException, XPathExpressionException;

	void save(ImageType imageType, File save) throws IOException;

}
