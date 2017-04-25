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
package eu.europa.ec.fisheries.uvms.jms;

import eu.europa.ec.fisheries.uvms.message.AbstractConsumer;
import eu.europa.ec.fisheries.uvms.message.JMSUtils;
import eu.europa.ec.fisheries.uvms.message.MessageConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Destination;
import javax.naming.InitialContext;

/**
 * Created by georgige on 10/23/2015.
 */
@Stateless
@LocalBean
public class USMMessageConsumer extends AbstractConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(USMMessageConsumer.class);

    private Destination destination;

	@PostConstruct
    public void init() {
        InitialContext ctx;
        try {
            ctx = new InitialContext();
        } catch (Exception e) {
            LOG.error("Failed to get InitialContext",e);
            throw new RuntimeException(e);
        }
        destination = JMSUtils.lookupQueue(ctx, MessageConstants.QUEUE_USM4UVMS);
    }


    @Override
    public Destination getDestination() {
        return destination;
    }

    public String getDestinationName() {
        return MessageConstants.QUEUE_USM4UVMS;
    }
}