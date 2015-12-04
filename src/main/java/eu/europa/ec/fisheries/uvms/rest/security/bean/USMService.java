package eu.europa.ec.fisheries.uvms.rest.security.bean;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.wsdl.user.types.*;

import javax.transaction.Transactional;
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



    String getUserPreference(String preferenceName,
                             String username,
                             String applicationName,
                             String currentRole,
                             String currentScope) throws ServiceException;

    String getUserPreference(String preferenceName, Context userContext) throws ServiceException;

    Context getUserContext(String username,
                           String applicationName,
                           String currentRole,
                           String currentScope) throws ServiceException;

    Application getApplicationDefinition(String applicationName) throws ServiceException;

    void deployApplicationDescriptor(Application descriptor) throws ServiceException;

    Set<String> getUserFeatures(String username, String applicationName, String currentRole, String currentScope) throws ServiceException;

    Set<String> getUserFeatures(String username, Context userContext) throws ServiceException;


    @Transactional
    void putUserPreference(String keyOption, String userDefinedValue, String applicationName, String scopeName, String roleName, String username) throws ServiceException;

    List<Dataset> getDatasetsPerCategory(String category, String username, String applicationName, String currentRole, String currentScope) throws ServiceException;

    List<Dataset> getDatasetsPerCategory(String category, Context userContext) throws ServiceException;


    void createDataset(String applicationName, DatasetExtension dataset) throws ServiceException;

    void updateDataset(String applicationName, DatasetExtension dataset) throws ServiceException;

    /**
     * This contains all user contexts available in USM for the given user.
     * Be aware that the UserContext object is not cached and each call does a JMS call to USM
     * @param remoteUser
     * @param applicationName
     * @return
     */
    UserContext getFullUserContext(String remoteUser, String applicationName) throws ServiceException;

    void redeployApplicationDescriptor(Application deploymentDescriptor) throws ServiceException;
}
