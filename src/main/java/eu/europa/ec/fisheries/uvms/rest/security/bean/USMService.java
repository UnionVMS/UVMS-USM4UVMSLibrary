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

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import eu.europa.ec.fisheries.wsdl.user.types.Context;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import eu.europa.ec.fisheries.wsdl.user.types.DatasetExtension;
import eu.europa.ec.fisheries.wsdl.user.types.UserContext;

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

    @Transactional
    void createDataset(String applicationName, String datasetName, String discriminator, String category, String description)  throws ServiceException;

    @Transactional
    void deleteDataset(String applicationName, String datasetName) throws ServiceException;


    List<DatasetExtension> findDatasetsByDiscriminator(String applicationName, String discriminator) throws ServiceException;

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