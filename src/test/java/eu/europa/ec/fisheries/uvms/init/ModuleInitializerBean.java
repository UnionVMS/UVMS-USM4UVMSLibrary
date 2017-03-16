package eu.europa.ec.fisheries.uvms.init;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Queue;
import java.io.InputStream;

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
