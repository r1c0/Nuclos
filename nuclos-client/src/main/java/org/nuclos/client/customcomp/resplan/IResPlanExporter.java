package org.nuclos.client.customcomp.resplan;

import java.io.File;
import java.io.IOException;

import javax.xml.xpath.XPathExpressionException;

import org.nuclos.client.image.ImageType;
import org.nuclos.client.image.SVGDOMDocumentSupport;
import org.nuclos.common.dblayer.INameProducer;

public interface IResPlanExporter<R, E> {

	void setResourceNameProducer(INameProducer<R> rnp);

	void setEntryNameProducer(INameProducer<E> enp);

	INameProducer<R> getResourceNameProducer();

	INameProducer<E> getEntryNameProducer();

	SVGDOMDocumentSupport getSVGDOMDocumentSupport();

	void run(String template, int startCategory) throws IOException, XPathExpressionException;

	void save(ImageType imageType, File save) throws IOException;

}
