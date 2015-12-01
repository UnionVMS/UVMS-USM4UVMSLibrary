package eu.europa.ec.fisheries.uvms.rest.security.bean;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import eu.europa.ec.fisheries.wsdl.user.types.Context;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import eu.europa.ec.fisheries.wsdl.user.types.UserContext;

import java.util.List;
import java.util.Set;

/**
 * Created by georgige on 11/25/2015.
 */
public interface USMService {

    /**
     * Gets the default value of an option for a given application.
     *
     * @param optionName
     * @param applicationName
     * @return
     * @throws ServiceException
     */
    String getOptionDefaultValue(String optionName, String applicationName) throws ServiceException;


    /**
     * updates an option default value.
     *
     * @param keyOption
     * @param defaultValue
     * @param applicationName
     * @throws ServiceException
     */
    void setOptionDefaultValue(String keyOption, String defaultValue, String applicationName) throws ServiceException;


    /**
     *
     * @param keyOption
     * @param userDefinedValue
     * @param applicationName
     * @param scopeName
     * @param username
     * @param cacheKey needed to clear the users session cache in order to refresh the user preferences
     * @throws ServiceException
     */
    void updateUserPreference(String keyOption,
                              String userDefinedValue,
                              String applicationName,
                              String scopeName,
                              String username,
                              String cacheKey) throws ServiceException;

    String getUserPreference(String preferenceName,
                             String username,
                             String applicationName,
                             String currentRole,
                             String currentScope,
                             String cacheKey) throws ServiceException;

    String getUserPreference(String preferenceName, Context userContext) throws ServiceException;

    Context getUserContext(String username,
                           String applicationName,
                           String currentRole,
                           String currentScope,
                           String cacheKey) throws ServiceException;

    Application getApplicationDefinition(String applicationName) throws ServiceException;

    void deployApplicationDescriptor(String descriptor) throws ServiceException;

    Set<String> getUserFeatures(String username, String applicationName, String currentRole, String currentScope, String cacheKey) throws ServiceException;

    Set<String> getUserFeatures(String username, Context userContext) throws ServiceException;

    List<Dataset> getDatasetsPerCategory(String category, String username, String applicationName, String currentRole, String currentScope, String cacheKey) throws ServiceException;

    List<Dataset> getDatasetsPerCategory(String category, Context userContext) throws ServiceException;


    void setDataset(String applicationName, Dataset dataset, String cacheKey) throws ServiceException;

    /**
     * This contains all user contexts available in USM for the given user.
     * Be aware that the UserContext object is not cached and each call does a JMS call to USM
     * @param remoteUser
     * @param applicationName
     * @return
     */
    UserContext getFullUserContext(String remoteUser, String applicationName) throws ServiceException;
}
