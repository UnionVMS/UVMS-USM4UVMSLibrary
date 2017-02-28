package eu.europa.ec.fisheries.uvms.init;

import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.message.AbstractJAXBMarshaller;
import eu.europa.ec.fisheries.uvms.message.MessageException;
import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import eu.europa.ec.fisheries.wsdl.user.module.*;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.jms.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class AbstractModuleInitializerBean extends AbstractJAXBMarshaller {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractModuleInitializerBean.class);

    private static final Long UVMS_USM_TIMEOUT = 10000L;

    private int count = 0;

    @EJB
    protected USMService usmService;

    @Resource
    TimerService timerService;

    @Schedule(minute="*", hour="*", persistent=false, info="AUTO_TIMER_0")
    public void atSchedule() throws InterruptedException, JAXBException, MessageException, JMSException, ServiceException {
        try {
            if(count < 5) {
                // do something on application startup
                InputStream deploymentDescInStream = getDeploymentDescriptorRequest();
                if (deploymentDescInStream != null) {
                    JAXBContext jaxBcontext = JAXBContext.newInstance(DeployApplicationRequest.class);
                    javax.xml.bind.Unmarshaller um = jaxBcontext.createUnmarshaller();

                    DeployApplicationRequest applicationDefinition = (DeployApplicationRequest) um.unmarshal(deploymentDescInStream);

                    if (!isAppDeployed(applicationDefinition.getApplication())) {
                        usmService.deployApplicationDescriptor(applicationDefinition.getApplication());
                    } else if(mustRedeploy()) {
                        usmService.redeployApplicationDescriptor(applicationDefinition.getApplication());
                    }
                } else {
                    LOG.error("USM deployment descriptor is not provided, therefore, the JMS deployment message cannot be sent.");
                }
                stopTimer(); // Stop timer as there is no exception and communication to USM is successful
            }
        } catch (ServiceException e) {
            count ++;
            LOG.info("Failed to connect to USM. Retry count " + count);
            if (count == 5) { // Stop timer after 5 retry
                stopTimer();
                throw new ServiceException("Deployment failed : Could not connect to USM");
            }
        }
    }

    private void stopTimer() {
        Iterator<Timer> timerIterator = timerService.getTimers().iterator();
        Timer timerToCancel = null;
        while(timerIterator.hasNext()) {
            Timer tmpTimer = timerIterator.next();
            if(tmpTimer.getInfo().equals("AUTO_TIMER_0")) {
                timerToCancel = tmpTimer;
                break;
            }
        }
        if(timerToCancel != null) {
            timerToCancel.cancel();
        }
    }

    private boolean isAppDeployed(Application deploymentDescriptor) throws JAXBException, JMSException, ServiceException, MessageException {
        boolean isAppDeployed = false;

        Application application = usmService.getApplicationDefinition(deploymentDescriptor.getName());

        if (application != null) {
            isAppDeployed = true;
        }

        return isAppDeployed;
    }

    /**
     *
     * @return InputStream with the String representation of Application descriptor
     */
    protected abstract InputStream getDeploymentDescriptorRequest();

    /**
     *
     * @return true if the application descriptor must be redeployed
     */
    protected abstract boolean mustRedeploy();
}
