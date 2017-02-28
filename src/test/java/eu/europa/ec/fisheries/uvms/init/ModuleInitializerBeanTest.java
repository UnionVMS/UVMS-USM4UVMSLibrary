package eu.europa.ec.fisheries.uvms.init;


import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.JAXBException;

import static java.lang.Integer.valueOf;

@RunWith(MockitoJUnitRunner.class)
public class ModuleInitializerBeanTest {

    @Spy
    private ModuleInitializerBean initializerBean;

  /*  @Mock
    protected ConnectionFactory connectionFactory;

    @Mock
    protected Queue usmRequestQueue;

    @Mock
    private MessageProducer jmsProducer;

    @Mock
    private Session session;

    @Mock
    private Connection connection;*/

    @Mock
    private USMService usmService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        initializerBean = new ModuleInitializerBean();
        Whitebox.setInternalState(initializerBean, "usmService", usmService);
//        Whitebox.setInternalState(initializerBean, "connectionFactory", connectionFactory);
//        Whitebox.setInternalState(initializerBean, "usmRequestQueue", usmRequestQueue);
//        Whitebox.setInternalState(initializerBean, "session", session);
//        Whitebox.setInternalState(initializerBean, "connection", connection);

    }



    @Test
    public void okDeployDescriptor() throws Exception {

      /*  when(session.createProducer(usmRequestQueue)).thenReturn(jmsProducer);
        when(connectionFactory.createConnection()).thenReturn(connection);
        when(connection.createSession(false, Session.AUTO_ACKNOWLEDGE)).thenReturn(session);
        when(session.createTextMessage(anyString())).thenReturn(mock(TextMessage.class));

        initializerBean.onStartup();

        verify(jmsProducer, times(1)).send(Matchers.any(TextMessage.class));
        verify(session, times(1)).createProducer(Matchers.eq(usmRequestQueue));
        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);*/
    }

    @Ignore
    @Test (expected = JAXBException.class)
    public void badDescriptor() throws Exception {
//        initializerBean.onStartup();

//        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);
    }

    @Test
    public void noDescriptor() throws Exception{
//        initializerBean.onStartup();
//        verify(session, times(0)).createProducer(Matchers.eq(usmRequestQueue));
//
//        reset(session, connection, connectionFactory, jmsProducer, usmRequestQueue);
    }
}