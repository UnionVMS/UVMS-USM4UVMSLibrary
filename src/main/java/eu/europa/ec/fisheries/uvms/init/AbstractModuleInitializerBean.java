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
package eu.europa.ec.fisheries.uvms.init;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.Iterator;

import eu.europa.ec.fisheries.uvms.commons.service.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.wsdl.user.module.DeployApplicationRequest;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractModuleInitializerBean {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModuleInitializerBean.class);

    private Application application;

    @EJB
    private USMService usmService;

    @Resource
    private TimerService timerService;

    @Schedule(minute = "*", hour = "*", persistent = false, info = "AUTO_TIMER_0")
    public void atSchedule() throws JAXBException {
        try {
            // on application startup do
            application = getApplication();
            if (!isAppDeployed(application)) {
                usmService.deployApplicationDescriptor(application);
            } else if (mustRedeploy()) {
                usmService.redeployApplicationDescriptor(application);
            }
            stopTimer(); // Stop timer as there is no exception and communication to USM is successful
        } catch (ServiceException e) {
            LOG.info("Could not update USM");
        }
    }

    private Application getApplication() throws JAXBException, ServiceException {
        if (application != null) { // app descriptor already loaded
            return application;
        }
        InputStream deploymentDescInStream = getDeploymentDescriptorRequest();
        if (deploymentDescInStream != null) {
            JAXBContext jaxBcontext = JAXBContext.newInstance(DeployApplicationRequest.class);
            javax.xml.bind.Unmarshaller um = jaxBcontext.createUnmarshaller();
            DeployApplicationRequest applicationDefinition = (DeployApplicationRequest) um.unmarshal(deploymentDescInStream);
            return applicationDefinition.getApplication();
        } else {
            stopTimer();
            LOG.error("USM deployment descriptor is not provided, therefore, the JMS deployment message cannot be sent.");
            throw new ServiceException("USM deployment descriptor is not provided, therefore, the JMS deployment message cannot be sent.");
        }
    }

    private void stopTimer() {
        Iterator<Timer> timerIterator = timerService.getTimers().iterator();
        Timer timerToCancel = null;
        while (timerIterator.hasNext()) {
            Timer tmpTimer = timerIterator.next();
            if (tmpTimer.getInfo().equals("AUTO_TIMER_0")) {
                timerToCancel = tmpTimer;
                break;
            }
        }
        if (timerToCancel != null) {
            timerToCancel.cancel();
        }
    }

    private boolean isAppDeployed(Application deploymentDescriptor) throws ServiceException {
        boolean isAppDeployed = false;
        Application application = usmService.getApplicationDefinition(deploymentDescriptor.getName());
        if (application != null) {
            isAppDeployed = true;
        }
        return isAppDeployed;
    }

    /**
     * @return InputStream with the String representation of Application descriptor
     */
    protected abstract InputStream getDeploymentDescriptorRequest();

    /**
     * @return true if the application descriptor must be redeployed
     */
    protected abstract boolean mustRedeploy();
}