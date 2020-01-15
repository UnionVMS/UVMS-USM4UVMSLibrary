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

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import eu.europa.ec.fisheries.uvms.rest.security.bean.USMService;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

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