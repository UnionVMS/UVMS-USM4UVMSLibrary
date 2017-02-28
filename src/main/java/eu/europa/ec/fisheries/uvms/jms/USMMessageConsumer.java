package eu.europa.ec.fisheries.uvms.jms;

import eu.europa.ec.fisheries.uvms.message.AbstractConsumer;
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
public class USMMessageConsumer extends AbstractConsumer {

    @Resource(mappedName = MessageConstants.QUEUE_USM4UVMS)
    private Destination destination;

    @Override
    public Destination getDestination() {
        return destination;
    }
}