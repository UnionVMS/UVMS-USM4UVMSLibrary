package eu.europa.ec.fisheries.uvms.init;

import javax.ejb.Singleton;
import javax.ejb.Startup;
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
    public static final String PROP_FILE_NAME = "config.properties";

    @Override
    protected Properties retrieveModuleConfigs() throws IOException {
        Properties prop = new Properties();
        InputStream properties = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME);
        if (properties != null) {
            prop.load(properties);
            return prop;
        } else {
            throw new FileNotFoundException("Property file '" + PROP_FILE_NAME + "' not found in the classpath");
        }
    }

    @Override
    public InputStream getDeploymentDescriptor() {
        return  getClass().getClassLoader().getResourceAsStream(CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML);
    }
}
