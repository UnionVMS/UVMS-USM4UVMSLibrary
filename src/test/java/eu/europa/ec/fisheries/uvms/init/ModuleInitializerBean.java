package eu.europa.ec.fisheries.uvms.init;

import eu.europa.ec.fisheries.uvms.message.MessageConsumer;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.JMSException;
import javax.jms.Queue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by georgige on 9/30/2015.
 */
@Singleton
@Startup
public class ModuleInitializerBean extends AbstractModuleInitializerBean {

    public static final String CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML = "usmDeploymentDescriptor.xml";

    protected Queue reportingResponseQueue;

    @Override
    public InputStream getDeploymentDescriptorRequest() {
        return  getClass().getClassLoader().getResourceAsStream(CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML);
    }

    @Override
    protected boolean mustRedeploy() {
        return true;
    }


}
