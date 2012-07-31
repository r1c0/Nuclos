package org.nuclos.client.customcomp.resplan;

import org.nuclos.common.dblayer.INameProducer;

public interface IResPlanExporterWithNameProducer<R, E, L> extends IResPlanExporter<R, E, L> {

	INameProducer<R> getResourceNameProducer();

	INameProducer<E> getEntryNameProducer();

	void setResourceNameProducer(INameProducer<R> rnp);

	void setEntryNameProducer(INameProducer<E> enp);

}
