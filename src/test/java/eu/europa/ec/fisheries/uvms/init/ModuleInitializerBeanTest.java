package eu.europa.ec.fisheries.uvms.init;


import eu.europa.ec.fisheries.uvms.exception.ServiceException;
import eu.europa.ec.fisheries.uvms.user.model.exception.ModelMarshallException;
import eu.europa.ec.fisheries.wsdl.user.module.DeployApplicationRequest;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.Application;
import eu.europa.ec.fisheries.wsdl.user.types.Dataset;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jms.*;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import static java.lang.Integer.valueOf;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
public class ModuleInitializerBeanTest {

    @Spy
    private ModuleInitializerBean initializerBean;

    @Mock
    protected ConnectionFactory connectionFactory;

    @Mock
    protected Queue usmRequestQueue;

    @Mock
    private MessageProducer jmsProducer;

    @Mock
    private Session session;

    @Mock
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initializerBean = new ModuleInitializerBean();
        Whitebox.setInternalState(initializerBean, "connectionFactory", connectionFactory);
        Whitebox.setInternalState(initializerBean, "usmRequestQueue", usmRequestQueue);
        Whitebox.setInternalState(initializerBean, "session", session);
        Whitebox.setInternalState(initializerBean, "connection", connection);

    }



    @Test
    @Ignore
    public void okDeployDescriptor() throws Exception {

        when(session.createProducer(usmRequestQueue)).thenReturn(jmsProducer);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTextMessage(anyString())).thenReturn(mock(TextMessage.class));

        initializerBean.onStartup();

        verify(jmsProducer, times(1)).send(Matchers.any(TextMessage.class));
        verify(session, times(1)).createProducer(Matchers.eq(usmRequestQueue));
        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);
    }

    @Test (expected = JAXBException.class)
    @Ignore
    public void badDescriptor() throws Exception {
        initializerBean.onStartup();

        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);
    }

    @Test
    @Ignore
    public void noDescriptor() throws Exception{
        initializerBean.onStartup();
        verify(session, times(0)).createProducer(Matchers.eq(usmRequestQueue));

        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);
    }
}