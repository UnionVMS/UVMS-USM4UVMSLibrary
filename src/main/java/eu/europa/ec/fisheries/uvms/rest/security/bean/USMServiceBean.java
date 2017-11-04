/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.rest.security.bean;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.constants.AuthConstants;
import eu.europa.ec.fisheries.uvms.jms.USMMessageConsumer;
import eu.europa.ec.fisheries.uvms.jms.USMMessageProducer;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.user.module.CreateDatasetResponse;
import eu.europa.ec.fisheries.wsdl.user.module.DeleteDatasetResponse;
import eu.europa.ec.fisheries.wsdl.user.module.DeployApplicationResponse;
import eu.europa.ec.fisheries.wsdl.user.module.FilterDatasetResponse;
import eu.europa.ec.fisheries.wsdl.user.module.GetDeploymentDescriptorRequest;
import eu.europa.ec.fisheries.wsdl.user.module.GetDeploymentDescriptorResponse;
import eu.europa.ec.fisheries.wsdl.user.module.GetUserContextResponse;
import eu.europa.ec.fisheries.wsdl.user.module.PutPreferenceResponse;
import eu.europa.ec.fisheries.wsdl.user.module.RedeployApplicationResponse;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import eu.europa.ec.fisheries.wsdl.user.types.Context;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import eu.europa.ec.fisheries.wsdl.user.types.DatasetExtension;
import eu.europa.ec.fisheries.wsdl.user.types.DatasetFilter;
import eu.europa.ec.fisheries.wsdl.user.types.Feature;
import eu.europa.ec.fisheries.wsdl.user.types.Option;
import eu.europa.ec.fisheries.wsdl.user.types.Preference;
import eu.europa.ec.fisheries.wsdl.user.types.UserContext;
import eu.europa.ec.fisheries.wsdl.user.types.UserContextId;
import eu.europa.ec.fisheries.wsdl.user.types.UserFault;
import eu.europa.ec.fisheries.wsdl.user.types.UserPreference;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@Local(USMService.class)
public class USMServiceBean implements USMService {

    private static final Logger LOG = LoggerFactory.getLogger(USMServiceBean.class);
    private static final Long UVMS_USM_TIMEOUT = 30000L;
    public static Cache userSessionCache = CacheManager.newInstance().getCache(AuthConstants.CACHE_NAME_USER_SESSION);
    private static Cache appCache = CacheManager.getInstance().getCache(AuthConstants.CACHE_NAME_APP_MODULE);

    @EJB
    private USMMessageProducer messageProducer;

    @EJB
    private USMMessageConsumer messageConsumer;

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
                       break;
                    }
                }
            }

        } else {
            context = (Context) cachedContext.getObjectValue();
        }

        if (context == null) {
            throw new ServiceException("Context with the provided username, role and scope is not found.");
        }

        return context;
    }

    @Override
    public String getUserPreference(String preferenceName, String username, String applicationName, String currentRole, String currentScope) throws ServiceException {
        LOG.debug("START getUserPreference({}, {}, {}, {}, {})", preferenceName, username, applicationName, currentRole, currentScope);
        String userPrefValue = null;


        Context userContext = getUserContext(username, applicationName, currentRole, currentScope);

        userPrefValue = getUserPreference(preferenceName, userContext);

        return userPrefValue;
    }

    @Override
    public String getUserPreference(String preferenceName, Context userContext) throws ServiceException {
        String userPrefValue = null;

        if (userContext != null) {

            if (userContext.getPreferences() != null) {
                List<Preference> listPrefs = userContext.getPreferences().getPreference();
                for (Preference pref : listPrefs) {
                    if (pref.getOptionName().equals(preferenceName)) {
                        userPrefValue = pref.getOptionValue();
                        break;
                    }
                }
            }
        }

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
                String msgId = messageProducer.sendModuleMessage(JAXBUtils.marshallJaxBObjectToString(getDeploymentDescriptorRequest), messageConsumer.getDestination());
                LOG.debug("JMS message with ID: {} is sent to USM.", msgId);

                Message response = messageConsumer.getMessage(msgId, GetDeploymentDescriptorResponse.class, UVMS_USM_TIMEOUT);

                if (response != null && !isUserFault((TextMessage) response)) {
                    GetDeploymentDescriptorResponse getDeploymentDescriptorResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, GetDeploymentDescriptorResponse.class);
                    LOG.debug("Response concerning message with ID: {} is received.", msgId);
                    application = getDeploymentDescriptorResponse.getApplication();
                } else {
                    LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                    if (response != null) {
                        UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                        LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                        throw new ServiceException("Unable to receive a response from USM.");
                    }
                }
            } catch (MessageException | JMSException | JAXBException e) {
                throw new ServiceException("Unable to get Application Definition", e);
            }

            cachedAppDefinition = new Element(applicationName, application);
            appCache.put(cachedAppDefinition);
        } else {
            application = (Application) cachedAppDefinition.getObjectValue();
        }

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
                DeployApplicationResponse deployApplicationResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, DeployApplicationResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", msgId);
                if ("OK".equalsIgnoreCase(deployApplicationResponse.getResponse())) {
                    LOG.info("Application successfully registered into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to register into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            }

        } catch (MessageException | JMSException | JAXBException | ModelMarshallException e) {
            throw new ServiceException("Unable to deploy Application descriptor", e);
        }
    }

    @Override
    public void redeployApplicationDescriptor(Application deploymentDescriptor) throws ServiceException {
        LOG.debug("START redeployApplicationDescriptor({})", deploymentDescriptor);

        //we must clear the app cache for the given application definition
        // since it contains all options default values
        if (appCache.remove(deploymentDescriptor.getName())) {
            LOG.debug("clearing {} application definition from the cache.", deploymentDescriptor.getName());
        }

        try {
            String descriptorString = UserModuleRequestMapper.mapToRedeployApplicationRequest(deploymentDescriptor);
            String msgId = messageProducer.sendModuleMessage(descriptorString, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", msgId);

            Message response = messageConsumer.getMessage(msgId, RedeployApplicationResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                RedeployApplicationResponse redeployApplicationResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, RedeployApplicationResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", msgId);
                if ("OK".equalsIgnoreCase(redeployApplicationResponse.getResponse())) {
                    LOG.info("Application successfully registered into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", msgId);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to register into USM.");
                } else {
                    throw new ServiceException("Unable to register into USM.");
                }
            }

        } catch (MessageException | JMSException | JAXBException | ModelMarshallException e) {
            throw new ServiceException("Unable to deploy Application descriptor", e);
        }
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

        putUserPreference(userPreference);

    }

    private void putUserPreference(UserPreference userPreference) throws ServiceException {
        LOG.debug("START putUserPreference param: {}", userPreference);

        String payload = null;
        try {
            payload = UserModuleRequestMapper.mapToPutUserPreferenceRequest(userPreference);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is successfully sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, PutPreferenceResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                PutPreferenceResponse putPreferenceResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, PutPreferenceResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, putPreferenceResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to set user preference into USM.", e);
        }

        LOG.debug("END putUserPreference");
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
    @Transactional
    public void createDataset(String applicationName, String datasetName, String discriminator, String category, String description) throws ServiceException {

        LOG.debug("START createDataset({}, {}, {}, {}, {})", applicationName, datasetName, discriminator, category, description);

        if (StringUtils.isEmpty(applicationName) || StringUtils.isEmpty(datasetName)) {
            throw new IllegalArgumentException("Application name, nor dataset name cannot be null");
        }

        if (appCache.remove(applicationName)) {
            LOG.debug("clearing {} application definition from the cache.", applicationName);
        }

        try {
            DatasetExtension dataset = new DatasetExtension();
            dataset.setApplicationName(applicationName);
            dataset.setDiscriminator(discriminator);
            dataset.setName(datasetName);
            dataset.setCategory(category);
            dataset.setDescription(description);

            String payload = UserModuleRequestMapper.mapToCreateDatasetRequest(dataset);


            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, CreateDatasetResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                CreateDatasetResponse createDatasetResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, CreateDatasetResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, createDatasetResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to update Dataset.", e);
        }

    }


    @Override
    public void deleteDataset(String applicationName, String datasetName) throws ServiceException {
        LOG.debug("START deleteDataset({}, {}", applicationName, datasetName);

        if (appCache.remove(applicationName)) {
            LOG.debug("clearing {} application definition from the cache.", applicationName);
        }

        try {
            DatasetExtension dataset = new DatasetExtension();
            dataset.setApplicationName(applicationName);
            dataset.setName(datasetName);

            String payload = UserModuleRequestMapper.mapToDeleteDatasetRequest(dataset);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, DeleteDatasetResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                DeleteDatasetResponse deleteDatasetResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, DeleteDatasetResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, deleteDatasetResponse.getResponse());
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }

        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to update Dataset.", e);
        }
    }


    @Override
    public List<DatasetExtension> findDatasetsByDiscriminator(String applicationName, String discriminator) throws ServiceException {
        LOG.debug("START findDatasetByDiscriminator({}, {}", applicationName, discriminator);
        List<DatasetExtension> listToReturn = null;

        try {
            DatasetFilter datasetFilter = new DatasetFilter();
            datasetFilter.setApplicationName(applicationName);
            datasetFilter.setDiscriminator(discriminator);

            String payload = UserModuleRequestMapper.mapToFindDatasetRequest(datasetFilter);

            String messageID = messageProducer.sendModuleMessage(payload, messageConsumer.getDestination());
            LOG.debug("JMS message with ID: {} is sent to USM.", messageID);

            Message response = messageConsumer.getMessage(messageID, DeleteDatasetResponse.class, UVMS_USM_TIMEOUT);

            if (response != null && !isUserFault((TextMessage) response)) {
                FilterDatasetResponse filterDatasetResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, FilterDatasetResponse.class);
                LOG.debug("Response concerning message with ID: {} is received. The response is: {}", messageID, filterDatasetResponse.getDatasetList().getList());
                listToReturn = filterDatasetResponse.getDatasetList().getList();
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                }
            }


        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unable to update Dataset.", e);
        }

        LOG.debug("END findDatasetByDiscriminator(...), returning {}", listToReturn);
        return listToReturn;
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
                GetUserContextResponse userContextResponse = JAXBUtils.unmarshallTextMessage((TextMessage) response, GetUserContextResponse.class);
                LOG.debug("Response concerning message with ID: {} is received.", messageID);
                userContext = userContextResponse.getContext();
            } else {
                LOG.error("Error occurred while receiving JMS response for message ID: {}", messageID);

                if (response != null) {
                    UserFault error = JAXBUtils.unmarshallTextMessage((TextMessage) response, UserFault.class);
                    LOG.error("Error Code: {}, Message: {}", error.getCode(), error.getFault());
                    throw new ServiceException("Unable to receive a response from USM.");
                }
            }
        } catch (ModelMarshallException | MessageException | JMSException | JAXBException e) {
            throw new ServiceException("Unexpected exception while trying to get user context.", e);
        }
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
            UserFault userFault = JAXBUtils.unmarshallTextMessage(message, UserFault.class);
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

}