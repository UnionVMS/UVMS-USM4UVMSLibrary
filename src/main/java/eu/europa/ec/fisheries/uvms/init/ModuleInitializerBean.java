package eu.europa.ec.fisheries.uvms.init;

import eu.europa.ec.fisheries.uvms.rest.security.JwtTokenHandler;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Singleton
@Startup
public class ModuleInitializerBean {

    public static final String PROP_MODULE_NAME = "module.name";
    public static final String PROP_USM_REST_SERVER = "usm.rest.server";
    public static final String PROP_USM_DESCRIPTOR_FORCE_UPDATE = "usm.deployment.descriptor.force-update";
    public static final String PROP_USM_ADMIN_REST_USERNAME = "usm.admin.rest.username";

    public static final String USM_REST_DESCRIPTOR_URI = "/usm-administration/rest/deployments/";
    public static final String USM_REST_AUTHENTICATE_URI = "/usm-administration/rest/authenticate";
    public static final String CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML = "usmDeploymentDescriptor.xml";
    public static final String PROP_FILE_NAME = "config.properties";
    public static final String TRUE = "true";

    public static final String AUTHORIZATION_HEADER = "authorization";

    private static final Logger LOG = LoggerFactory.getLogger(ModuleInitializerBean.class);

    @PostConstruct
    public void onStartup() throws IOException {
        // do something on application startup
        Properties moduleConfigs = retrieveModuleConfigs();

        Client client = ClientBuilder.newClient();

        JwtTokenHandler tokenHandler = new JwtTokenHandler();

        LOG.info("Successfully authenticated into USM.");
        String authToken = tokenHandler.createToken(moduleConfigs.getProperty(PROP_USM_ADMIN_REST_USERNAME));

        WebTarget target = client.target(moduleConfigs.getProperty(PROP_USM_REST_SERVER)).path(USM_REST_DESCRIPTOR_URI);

        LOG.info("Verifying that Reporting module is deployed into USM...");
        Response response = target.path(moduleConfigs.getProperty(PROP_MODULE_NAME)).request(MediaType.APPLICATION_XML_TYPE).header(AUTHORIZATION_HEADER, authToken).get();

        try {
            String descriptor = retrieveDescriptorAsString();

            if (!isDescriptorAlreadyRegistered(response)) {
                response.close();
                LOG.info("USM doesn't recognize the current module. Deploying module deployment descriptor...");
                response = target.request(MediaType.APPLICATION_XML_TYPE).header(AUTHORIZATION_HEADER, authToken).post(Entity.xml(descriptor));
                checkResult(response, "");
            } else {
                LOG.info("Module deployment descriptor has already been deployed at USM.");

                if (isForceUpdate(moduleConfigs)) {
                    LOG.info("Updating the existing module deployment descriptor into USM.");
                    response = target.request(MediaType.APPLICATION_XML_TYPE).header(AUTHORIZATION_HEADER, authToken).put(Entity.xml(descriptor));
                    checkResult(response, "re");
                }
            }
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to unmarshal descriptor", e);
        } finally {
            closeConnection(response);
        }
    }

    @PreDestroy
    public void onShutdown() {
        //TODO undeploy app
    }

    private void closeConnection(Response response) {
        if (response != null) {
            response.close();
        }
    }

    private boolean isForceUpdate(Properties moduleConfigs) {
        return TRUE.equalsIgnoreCase(moduleConfigs.getProperty(PROP_USM_DESCRIPTOR_FORCE_UPDATE));
    }

    private boolean isDescriptorAlreadyRegistered(Response response) {
        return response.getStatus() == HttpServletResponse.SC_OK;
    }

    private Properties retrieveModuleConfigs() throws IOException {
        Properties prop = new Properties();
        InputStream properties = getClass().getClassLoader().getResourceAsStream(PROP_FILE_NAME);
        if (properties != null) {
            prop.load(properties);
            return prop;
        } else {
            throw new FileNotFoundException("Property file '" + PROP_FILE_NAME + "' not found in the classpath");
        }
    }

    private USMDeploymentDescriptor retrieveDescriptor() throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(USMDeploymentDescriptor.class);
        Unmarshaller un = context.createUnmarshaller();

        InputStream descriptorXML = getClass().getClassLoader().getResourceAsStream(CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML);
        if (descriptorXML == null) {
            throw new FileNotFoundException("Descriptor template file '" + CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML + "' not found in the classpath");
        }
        return (USMDeploymentDescriptor) un.unmarshal(descriptorXML);
    }

    private String retrieveDescriptorAsString() throws JAXBException, IOException {
        InputStream descriptorXML = getClass().getClassLoader().getResourceAsStream(CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML);
        if (descriptorXML == null) {
            throw new FileNotFoundException("Descriptor template file '" + CONFIG_USM_DEPLOYMENT_DESCRIPTOR_XML + "' not found in the classpath");
        }
        return IOUtils.toString(descriptorXML, "UTF-8");
    }

    private void checkResult(Response response, String logPrefix) {
        if (response.getStatus() == HttpServletResponse.SC_OK) {
            LOG.info("Application " + logPrefix + "deployment descriptor successfully " + logPrefix + "deployed into USM.");
        } else {
            closeConnection(response);
            throw new RuntimeException("Unable to " + logPrefix + "deploy application descriptor into USM. Response code: " + response.getStatus() + ". Response body: " + response.toString());
        }
    }

}