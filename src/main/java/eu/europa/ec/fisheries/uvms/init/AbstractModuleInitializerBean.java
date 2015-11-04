package eu.europa.ec.fisheries.uvms.init;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.jms.USMMessageConsumer;
import eu.europa.ec.fisheries.uvms.jms.USMMessageProducer;
import eu.europa.ec.fisheries.uvms.message.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageConsumer;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.wsdl.user.module.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public abstract class AbstractModuleInitializerBean extends AbstractJAXBMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModuleInitializerBean.class);

    private static final Long UVMS_USM_TIMEOUT = 10000L;

    @EJB
    protected USMMessageProducer messageProducer;

    @EJB
    protected USMMessageConsumer messageConsumer;

    @PostConstruct
    public void onStartup() throws IOException, JAXBException, ModelMarshallException, JMSException, ServiceException, MessageException {
        // do something on application startup
        InputStream deploymentDescInStream = getDeploymentDescriptorRequest();

        if (deploymentDescInStream != null) {
            String deploymentDescriptor = IOUtils.toString(deploymentDescInStream, "UTF-8");
            if (!isAppDeployed(deploymentDescriptor)) {
                deployApp(deploymentDescriptor);
            }
        } else {
            LOG.error("USM deployment descriptor is not provided, therefore, the JMS deployment message cannot be sent.");
        }
    }

    private boolean isAppDeployed(String deploymentDescriptor) throws JAXBException, JMSException, ServiceException, MessageException {
        JAXBContext jaxBcontext = JAXBContext.newInstance(DeployApplicationRequest.class);
        javax.xml.bind.Unmarshaller um = jaxBcontext.createUnmarshaller();

        DeployApplicationRequest deploymentRequest = (DeployApplicationRequest) um.unmarshal(new StringReader(deploymentDescriptor));

        GetDeploymentDescriptorRequest getDeploymentDescriptorRequest = new GetDeploymentDescriptorRequest();
        getDeploymentDescriptorRequest.setMethod(UserModuleMethod.GET_DEPLOYMENT_DESCRIPTOR);
        getDeploymentDescriptorRequest.setApplicationName(deploymentRequest.getApplication().getName());
        try {
            String msgId = messageProducer.sendModuleMessage(marshallJaxBObjectToString(getDeploymentDescriptorRequest), messageConsumer.getDestination());
            Message response = messageConsumer.getMessage(msgId, GetDeploymentDescriptorResponse.class, UVMS_USM_TIMEOUT);

            if (!(response instanceof TextMessage)) {
                throw new ServiceException("Unable to receive a response from USM.");
            } else {
                GetDeploymentDescriptorResponse getDeploymentDescriptorResponse
                        = unmarshallTextMessage(((TextMessage) response), GetDeploymentDescriptorResponse.class);
                if (getDeploymentDescriptorResponse.getApplication() != null && getDeploymentDescriptorResponse.getApplication().getName().equals(deploymentRequest.getApplication().getName())) {
                    return true;
                }
            }
        } catch (MessageException e) {
            LOG.error("Unable to open JMS producerConnection producerSession.");
            throw e;
        }

        return false;
    }

    private void deployApp(String deploymentDescriptor) throws ServiceException, JMSException, MessageException, JAXBException {
        try {
            String msgId = messageProducer.sendModuleMessage(deploymentDescriptor, messageConsumer.getDestination());
            Message response = messageConsumer.getMessage(msgId, DeployApplicationResponse.class, UVMS_USM_TIMEOUT);

            if (response instanceof TextMessage) {
                DeployApplicationResponse deployApplicationResponse = unmarshallTextMessage(((TextMessage) response), DeployApplicationResponse.class);

                if ("OK".equalsIgnoreCase(deployApplicationResponse.getResponse())) {
                    LOG.info("Application successfully registered into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            } else {
                throw new ServiceException("Unrecognized response during USM registration: " + response.toString());
            }

        } catch (MessageException e) {
            LOG.error("Unable to open JMS producerConnection producerSession.", e);
            throw e;
        }
    }

    @PreDestroy
    public void onShutdown() {
        //TODO undeploy app
    }

    protected abstract InputStream getDeploymentDescriptorRequest();
}
