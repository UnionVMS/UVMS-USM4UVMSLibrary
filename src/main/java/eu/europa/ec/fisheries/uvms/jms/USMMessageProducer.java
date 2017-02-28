package eu.europa.ec.fisheries.uvms.jms;

import eu.europa.ec.fisheries.uvms.message.AbstractProducer;
import eu.europa.ec.fisheries.uvms.message.MessageConstants;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.naming.InitialContext;
import eu.europa.ec.fisheries.uvms.message.JMSUtils;

/**
 * Created by georgige on 10/23/2015.
 */
@Stateless
@Local
public class USMMessageProducer extends AbstractProducer {

    private Destination destination;

	@PostConstruct
    public void init() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
        } catch (Exception e) {
            LOG.error("Failed to get InitialContext",e);
            throw new RuntimeException(e);
        }
        destination = JMSUtils.lookupQueue(ctx, MessageConstants.QUEUE_USM);
    }
	
	
    @Override
    protected Destination getDestination() {
        return destination;
    }
}