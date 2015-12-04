package eu.europa.ec.fisheries.uvms.rest.security.bean;

import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.jms.USMMessageConsumer;
import eu.europa.ec.fisheries.uvms.jms.USMMessageProducer;
import eu.europa.ec.fisheries.uvms.message.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.user.module.*;
import eu.europa.ec.fisheries.wsdl.user.types.*;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.*;

/**
 * Created by georgige on 10/30/2015.
 */
@Stateless
@Local(USMService.class)
public class USMServiceBean extends AbstractJAXBMarshaller implements USMService {

    private static final Logger LOG = LoggerFactory.getLogger(USMServiceBean.class);
    private static final Long UVMS_USM_TIMEOUT = 30000L;
    public static Cache userSessionCache = CacheManager.newInstance().getCache(AuthConstants.CACHE_NAME_USER_SESSION);
    private static Cache appCache = CacheManager.getInstance().getCache(AuthConstants.CACHE_NAME_APP_MODULE);
    @EJB
    protected USMMessageProducer messageProducer;

    @EJB
    protected USMMessageConsumer messageConsumer;


    @Override
    public String getOptionDefaultValue(String optionName, String applicationName) throws ServiceException {
        LOG.debug("START getOptionDefaultValue({}, {})", optionName, applicationName);
        Element cachedApp = appCache.get(applicationName);
        Application application;
        String defaultOptionValue = null;

        if (cachedApp == null || cachedApp.isExpired()) {
            application = getApplicationDefinition(applicationName);
            cachedApp = new Element(applicationName, application);
            appCache.put(cachedApp);
        } else {
            application = (Application) cachedApp.getObjectValue();
        }

        List<Option> allOptions = application.getOption();
        for (Option opt : allOptions) {
            if (opt.getName().equalsIgnoreCase(optionName)) {
                defaultOptionValue = opt.getDefaultValue();
                break;
            }
        }

        LOG.debug("END getOptionDefaultValue(), returning: {}", defaultOptionValue);
        return defaultOptionValue;
    }

    private String getCacheKey(String userName, String currentRole, String currentScope) {
        return new StringBuilder(userName).append('_').append(currentRole).append('_').append(currentScope).toString();
    }

    @Override
    public Context getUserContext(String username, String applicationName, String currentRole, String currentScope) throws ServiceException {
        LOG.debug("START getUserContext({}, {}, {}, {})", username, applicationName, currentRole, currentScope);
        Context context = null;
        String cacheKey = getCacheKey(username, currentRole, currentScope);

        Element cachedContext = userSessionCache.get(cacheKey);

        if (cachedContext == null || cachedContext.isExpired()) {

            UserContext fullContext = getFullUserContext(username, applicationName);

            if (fullContext != null) {

                for (Context usmCtx : fullContext.getContextSet().getContexts()) {

                    if (isContextMatch(usmCtx, currentRole, currentScope)) {
                        context = usmCtx;
                        cachedContext = new Element(cacheKey, usmCtx);
                        userSessionCache.put(cachedContext);
                        LOG.debug("the received userContext is cached with a key: {}", cacheKey);

                        break;
                    }
                }
            }

        } else {
            context = (Context) cachedContext.getObjectValue();
            LOG.debug("userContext is retrieved from the cache.");
        }

        if (context == null) {
            throw new ServiceException("Context with the provided username, role and scope is not found.");
        }

        LOG.debug("END getUserContext(..)");
        return context;
    }

    @Override
    public String getUserPreference(String preferenceName, String username, String applicationName, String currentRole, String currentScope) throws ServiceException {
        LOG.debug("START getUserPreference({}, {}, {}, {}, {})", preferenceName, username, applicationName, currentRole, currentScope);
        String userPrefValue = null;

//        try {
//            String msgId = messageProducer.sendModuleMessage(marshallJaxBObjectToString(getDeploymentDescriptorRequest), messageConsumer.getDestination());
//            LOG.debug("JMS message with ID: {} is sent to USM.", msgId);
//
//            Message response = messageConsumer.getMessage(msgId, GetDeploymentDescriptorResponse.class, UVMS_USM_TIMEOUT);
//
//            if (response != null && !isUserFault((TextMessage) response)) {
//                GetDeploymentDescriptorResponse getDeploymentDescriptorResponse = unmarshallTextMessage((TextMessage) response, GetDeploymentDescriptorResponse.class);
//                LOG.debug("Response concerning message with ID: {} is received.", msgId);
//                application = getDeploymentDescriptorResponse.getApplication();
//            } else {
//                LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);
//
//                if (response != null) {
//                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
//                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
//                    throw new ServiceException("Unable to receive a response from USM.");
//                }
//            }
//        } catch (MessageException | JMSException | JAXBException e) {
//            throw new ServiceException("Unable to get Application Definition", e);
//        }

        Context userContext = getUserContext(username, applicationName, currentRole, currentScope);

   //     Map<String, String> userPreferences = new HashMap<>();

        userPrefValue = getUserPreference(preferenceName, userContext);

        LOG.debug("END getUserPreference(), returning: {}", userPrefValue);
        return userPrefValue;
    }

    @Override
    public String getUserPreference(String preferenceName, Context userContext) throws ServiceException {
        LOG.debug("START getUserPreference({}, {})", preferenceName, userContext);
        String userPrefValue = null;

        if (userContext != null) {
           // Map<String, String> userPreferences = new HashMap<>();

            if (userContext.getPreferences() != null) {
                List<Preference> listPrefs = userContext.getPreferences().getPreference();
                //lets filter out all other preferences which comes from the other apps
               /* for (Preference pref : listPrefs) {
                    userPreferences.put(pref.getOptionName(), pref.getOptionValue());
                }*/
                for (Preference pref : listPrefs) {
                    if (pref.getOptionName().equals(preferenceName)) {
                        userPrefValue = pref.getOptionValue();
                        break;
                    }
                }
            }
        }

        LOG.debug("END getUserPreference(), returning: {}", userPrefValue);
        return userPrefValue;
    }

    @Override
    public Application getApplicationDefinition(String applicationName) throws ServiceException {
        LOG.debug("START getApplicationDefinition({})", applicationName);

        Application application = null;
        Element cachedAppDefinition = appCache.get(applicationName);

        if (cachedAppDefinition == null || cachedAppDefinition.isExpired()) {
            GetDeploymentDescriptorRequest getDeploymentDescriptorRequest = new GetDeploymentDescriptorRequest();
            getDeploymentDescriptorRequest.setMethod(UserModuleMethod.GET_DEPLOYMENT_DESCRIPTOR);
            getDeploymentDescriptorRequest.setApplicationName(applicationName);

            try {
                String msgId = messageProducer.sendModuleMessage(marshallJaxBObjectToString(getDeploymentDescriptorRequest), messageConsumer.getDestination());
                LOG.debug("JMS message with ID: {} is sent to USM.", msgId);

                Message response = messageConsumer.getMessage(msgId, GetDeploymentDescriptorResponse.class, UVMS_USM_TIMEOUT);

                if (response != null && !isUserFault((TextMessage) response)) {
                    GetDeploymentDescriptorResponse getDeploymentDescriptorResponse = unmarshallTextMessage((TextMessage) response, GetDeploymentDescriptorResponse.class);
                    LOG.debug("Response concerning message with ID: {} is received.", msgId);
                    application = getDeploymentDescriptorResponse.getApplication();
                } else {
                    LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                    if (response != null) {
                        UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                        LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                        throw new ServiceException("Unable to receive a response from USM.");
                    }
                }
            } catch (MessageException | JMSException | JAXBException e) {
                throw new ServiceException("Unable to get Application Definition", e);
            }

            cachedAppDefinition = new Element(applicationName, application);
            appCache.put(cachedAppDefinition);
            LOG.debug("application definition is put in cache.");
        } else {
            application = (Application) cachedAppDefinition.getObjectValue();
            LOG.debug("application definition is retrieved from the cache.");
        }

        LOG.debug("END getApplicationDefinition()");
        return application;
    }

    @Override
    @Transactional
    public void deployApplicationDescriptor(Application descriptor) throws ServiceException {
        LOG.debug("START deployApplicationDescriptor({})", descriptor);

        try {
            String descriptorString = UserModuleRequestMapper.mapToDeployApplicationRequest(descriptor);
            String msgId = messageProducer.sendModuleMessage(descriptorString, messageConsumer.getDestination());
            Message response = messageConsumer.getMessage(msgId, DeployApplicationResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                DeployApplicationResponse deployApplicationResponse = unmarshallTextMessage((TextMessage) response, DeployApplicationResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", msgId);
                if ("OK".equalsIgnoreCase(deployApplicationResponse.getResponse())) {
                    LOG.info("Application successfully registered into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to register into USM.");
                }
            }

        } catch (MessageException | JMSException | JAXBException | ModelMarshallException e) {
            throw new ServiceException("Unable to deploy Application descriptor", e);
        }

        LOG.debug("END deployApplicationDescriptor()");
    }

    @Override
    public void redeployApplicationDescriptor(Application deploymentDescriptor) throws ServiceException {
        LOG.debug("START redeployApplicationDescriptor({})", deploymentDescriptor);

        try {
            String descriptorString = UserModuleRequestMapper.mapToRedeployApplicationRequest(deploymentDescriptor);
            String msgId = messageProducer.sendModuleMessage(descriptorString, messageConsumer.getDestination());
            Message response = messageConsumer.getMessage(msgId, RedeployApplicationResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                RedeployApplicationResponse redeployApplicationResponse = unmarshallTextMessage((TextMessage) response, RedeployApplicationResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", msgId);
                if ("OK".equalsIgnoreCase(redeployApplicationResponse.getResponse())) {
                    LOG.info("Application successfully registered into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to register into USM.");
                }
            }

        } catch (MessageException | JMSException | JAXBException | ModelMarshallException e) {
            throw new ServiceException("Unable to deploy Application descriptor", e);
        }

        LOG.debug("END redeployApplicationDescriptor()");
    }

    @Override
    @Transactional
    public void setOptionDefaultValue(String keyOption, String defaultValue, String applicationName) throws ServiceException {

        LOG.debug("START setOptionDefaultValue({}, {}, {})", keyOption, defaultValue, applicationName);

        Application application = getApplicationDefinition(applicationName);
        List<Option> optionList = application.getOption();
        boolean isOptionAdd = true;

        for (Option option : optionList) {
            if (option.getName().equals(keyOption)) {
                isOptionAdd = false;
                option.setDefaultValue(defaultValue);
                break;
            }
        }

        if (isOptionAdd) {
            Option option = new Option();
            option.setName(keyOption);
            option.setDefaultValue(defaultValue);
            application.getOption().add(option);
        }

        appCache.remove(applicationName);

        redeployApplicationDescriptor(application);

        LOG.debug("END setOptionDefaultValue()");
    }

    @Override
    @Transactional
    public void putUserPreference(String keyOption, String userDefinedValue, String applicationName, String scopeName, String roleName, String username) throws ServiceException {
        LOG.debug("START putUserPreference({} , {}, {}, {}, {}, {})", keyOption, userDefinedValue, applicationName, scopeName, roleName, username);

        String cacheKey = getCacheKey(username, roleName, scopeName);

        if (userSessionCache.remove(cacheKey)) {
            LOG.debug("clearing {} application definition from the cache.", applicationName);
        }

        UserPreference userPreference = new UserPreference();
        userPreference.setApplicationName(applicationName);
        userPreference.setOptionName(keyOption);
        userPreference.setOptionValue(userDefinedValue.getBytes());
        userPreference.setScopeName(scopeName);
        userPreference.setUserName(username);
        userPreference.setRoleName(roleName);


        //FIXME once we have better APIs from USM
        //update
        try {
            updateUserPreference(userPreference);
        } catch (Exception anyExc) {//this is ugly, dirty workaround until USM implements better API
            createUserPreference(userPreference);
        }


        LOG.debug("END putUserPreference()");
    }

    private void updateUserPreference(UserPreference userPreference) throws ServiceException {
        LOG.debug("START updateUserPreference");

        String payload = null;
        try {
            payload = UserModuleRequestMapper.mapToUpdateUserPreferenceRequest(userPreference);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is successfully sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, UpdatePreferenceResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                UpdatePreferenceResponse updatePreferenceResponse = unmarshallTextMessage((TextMessage) response, UpdatePreferenceResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, updatePreferenceResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to set user preference into USM.", e);
        }

        LOG.debug("END updateUserPreference");
    }

    private void createUserPreference(UserPreference userPreference) throws ServiceException {
        LOG.debug("START createUserPreference");



        String payload = null;
        try {
            payload = UserModuleRequestMapper.mapToCreateUserPreferenceRequest(userPreference);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is successfully sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, CreatePreferenceRequest.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                CreatePreferenceRequest createPreferenceRequest = unmarshallTextMessage((TextMessage) response, CreatePreferenceRequest.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, createPreferenceRequest.getUserPreference());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to set user preference into USM.", e);
        }

        LOG.debug("END createUserPreference");
    }


    @Override
    public List<Dataset> getDatasetsPerCategory(String category, String username, String applicationName, String currentRole, String currentScope) throws ServiceException {

        Context ctxt = getUserContext(username, applicationName, currentRole, currentScope);

        return getDatasetsPerCategory(category, ctxt);
    }

    @Override
    public List<Dataset> getDatasetsPerCategory(String category, Context userContext) throws ServiceException {
        LOG.debug("START getDatasetsPerCategory({}, {})", category, userContext);

        List<Dataset> filteredDatasets = new LinkedList<>();

        if (userContext != null) {
            List<Dataset> datasetList = userContext.getScope().getDataset();

            for (Dataset dataset : datasetList) {
                if (dataset.getCategory().equalsIgnoreCase(category)) {
                    filteredDatasets.add(dataset);
                }
            }
        }

        LOG.debug("END getDatasetsPerCategory(...), return {} datasets.", filteredDatasets.size());
        return filteredDatasets;
    }

    @Override
    public void createDataset(String applicationName, DatasetExtension dataset) throws ServiceException {

        LOG.debug("START createDataset({}, {}", applicationName, dataset);

        if (appCache.remove(applicationName)) {
            LOG.debug("clearing {} application definition from the cache.", applicationName);
        }

        try {
            String payload = UserModuleRequestMapper.mapToCreateDatasetRequest(dataset);


            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, CreateDatasetResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                CreateDatasetResponse createDatasetResponse = unmarshallTextMessage((TextMessage) response, CreateDatasetResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, createDatasetResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to update Dataset.", e);
        }

        LOG.debug("END createDataset(...)");
    }

    @Override
    public void updateDataset(String applicationName, DatasetExtension dataset) throws ServiceException {

        LOG.debug("START updateDataset({}, {}, {}", applicationName, dataset);

        if (appCache.remove(applicationName)) {
            LOG.debug("clearing {} application definition from the cache.", applicationName);
        }

        try {
            String payload = UserModuleRequestMapper.mapToUpdateDatasetRequest(dataset);


            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, UpdateDatasetResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                UpdateDatasetResponse updateDatasetResponse = unmarshallTextMessage((TextMessage) response, UpdateDatasetResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, updateDatasetResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to update Dataset.", e);
        }

        LOG.debug("END updateDataset(...)");
    }

    @Override
    public UserContext getFullUserContext(String remoteUser, String applicationName) throws ServiceException {
        LOG.debug("START getFullUserContext({}, {})", remoteUser, applicationName);
        UserContext userContext = null;
        UserContextId contextId = new UserContextId();
        contextId.setApplicationName(applicationName);
        contextId.setUserName(remoteUser);

        String payload;
        try {
            payload = UserModuleRequestMapper.mapToGetUserContextRequest(contextId);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, GetUserContextResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                GetUserContextResponse userContextResponse = unmarshallTextMessage((TextMessage) response, GetUserContextResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", messageID);
                userContext = userContextResponse.getContext();
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to receive a response from USM.");
                }
            }
        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unexpected exception while trying to get user context.", e);
        }
        LOG.debug("END getFullUserContext(...)");
        return userContext;
    }

    @Override
    public Set<String> getUserFeatures(String username, Context userContext) throws ServiceException {
        LOG.debug("START getUserFeatures({} ,{})", username, userContext);

        if (userContext == null) {
            return null;
        }

        List<Feature> features = userContext.getRole().getFeature();
        Set<String> featuresStr = new HashSet<>(features.size());

        //extract only the features that the particular application is interested in
        for (Feature feature : features) {
            featuresStr.add(feature.getName());
        }

        LOG.debug("END getUserFeatures(...), returns {} ", featuresStr);
        return featuresStr;
    }

    @Override
    public Set<String> getUserFeatures(String username, String applicationName, String currentRole, String currentScope) throws ServiceException {

        Context ctxt = getUserContext(username, applicationName, currentRole, currentScope);
        return getUserFeatures(username, ctxt);
    }

    private boolean isUserFault(TextMessage message) {
        boolean isErrorResponse = false;

        try {
            UserFault userFault = unmarshallTextMessage(message, UserFault.class);
            isErrorResponse = true;
        } catch (JAXBException | JMSException e) {
            //do nothing  since it's not a UserFault
        }

        return isErrorResponse;
    }


    private boolean isContextMatch(Context usmCtx, String currentRole, String currentScope) {
        boolean isContextMatch = false;
        if (usmCtx.getRole().getRoleName().equalsIgnoreCase(currentRole)) {
            isContextMatch = true;
        }

        //check if our user has a scope (it is possible to have a context without a scope)
        if (StringUtils.isNotBlank(currentScope)) {
            if (usmCtx.getScope() == null || !usmCtx.getScope().getScopeName().equalsIgnoreCase(currentScope)) {
                isContextMatch = false;
            }
        }
        return isContextMatch;
    }

    private void redeployApplication(Application application) throws ServiceException {
        //we must clear the app cache for the given application definition
        // since it contains all options default values
        if (appCache.remove(application.getName())) {
            LOG.debug("clearing {} application definition from the cache.", application.getName());
        }

        String payload = null;
        try {
            payload = UserModuleRequestMapper.mapToRedeployApplicationRequest(application);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, GetUserContextResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                RedeployApplicationResponse redeployApplicationResponse = unmarshallTextMessage((TextMessage) response, RedeployApplicationResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, redeployApplicationResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }
        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to set option default value into USM.", e);
        }
    }
}
