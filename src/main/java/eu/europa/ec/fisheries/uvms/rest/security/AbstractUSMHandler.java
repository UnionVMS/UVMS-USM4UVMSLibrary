package eu.europa.ec.fisheries.uvms.rest.security;

import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.servlet.ServletContext;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.jms.USMMessageConsumer;
import eu.europa.ec.fisheries.uvms.jms.USMMessageProducer;
import eu.europa.ec.fisheries.uvms.message.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.wsdl.user.module.GetUserContextRequest;
import eu.europa.ec.fisheries.wsdl.user.module.GetUserContextResponse;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.UserContext;
import eu.europa.ec.fisheries.wsdl.user.types.UserContextId;
/*
 import eu.europa.ec.mare.usm.information.domain.UserContext;
 import eu.europa.ec.mare.usm.information.domain.UserContextQuery;
 import eu.europa.ec.mare.usm.information.service.InformationService;
 */

/**
 * Created by georgige on 9/22/2015.
 */
public abstract class AbstractUSMHandler extends AbstractJAXBMarshaller {

    private static String UNION_VMS_APPLICATION = "Union-VMS";
    private static final Long UVMS_USM_TIMEOUT = 10000L;

    @EJB
    protected USMMessageProducer messageProducer;

    @EJB
    protected USMMessageConsumer messageConsumer;

    protected String getApplicationName(ServletContext servletContext) {
        String cfgName = servletContext.getInitParameter("usmApplication");
        if (cfgName == null) {
            cfgName = UNION_VMS_APPLICATION;
        }

        /*   see my comments in UnionVMSModule Enum
         UnionVMSModule application = UnionVMSModule.valueOf(cfgName);
         if (application == null) {
         return UNION_VMS_APPLICATION;
         }*/
        return cfgName;
    }

    protected UserContext getUserContext(String username, String applicationName) throws JAXBException, MessageException, ServiceException, JMSException {
        UserContext userContext;

        UserContextId contextId = new UserContextId();
        contextId.setApplicationName(applicationName);
        contextId.setUserName(username);

        GetUserContextRequest userContextRequest = new GetUserContextRequest();
        userContextRequest.setMethod(UserModuleMethod.GET_USER_CONTEXT);
        userContextRequest.setContextId(contextId);
        String messageID = messageProducer.sendModuleMessage(marshallJaxBObjectToString(userContextRequest), messageConsumer.getDestination());
        Message response = messageConsumer.getMessage(messageID, GetUserContextResponse.class, UVMS_USM_TIMEOUT);

        if (!(response instanceof TextMessage)) {
            throw new ServiceException("Unable to receive a response from USM.");
        } else {
            GetUserContextResponse getUserContextResponse
                    = unmarshallTextMessage(((TextMessage) response), GetUserContextResponse.class);
            userContext = getUserContextResponse.getContext();
        }
        return userContext;
    }

    /*

     public InformationService getInfoService() {
     return infoService;
     }

     public void setInfoService(InformationService infoService) {
     this.infoService = infoService;
     }

     */
}
