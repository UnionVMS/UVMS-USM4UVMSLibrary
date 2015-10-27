package eu.europa.ec.fisheries.uvms.jms;

import eu.europa.ec.fisheries.uvms.message.AbstractProducer;
import eu.europa.ec.fisheries.uvms.message.MessageConstants;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Destination;

/**
 * Created by georgige on 10/23/2015.
 */
@Stateless
@Local
public class USMMessageProducer extends AbstractProducer {

    @Resource(mappedName = MessageConstants.QUEUE_USM)
    private Destination destination;

    @Override
    protected Destination getDestination() {
        return destination;
    }
}