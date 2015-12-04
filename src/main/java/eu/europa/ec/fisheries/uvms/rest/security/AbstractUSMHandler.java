package eu.europa.ec.fisheries.uvms.rest.security;

import javax.ejb.EJB;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.JAXBException;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.jms.USMMessageConsumer;
import eu.europa.ec.fisheries.uvms.jms.USMMessageProducer;
import eu.europa.ec.fisheries.uvms.message.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
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
public abstract class AbstractUSMHandler  {

    private static String UNION_VMS_APPLICATION = "Union-VMS";

    @EJB
    protected USMService usmService;


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


}
