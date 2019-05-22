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
package eu.europa.ec.fisheries.uvms.rest.security;

/**
 * Defines all known features
 */
public enum UnionVMSFeature {


    /**
     * Viewing both vessels and mobile terminals
     */
    viewVesselsAndMobileTerminals(292),
    /**
     * Managing mobile terminals
     */
    manageMobileTerminals(294),
    /**
     * Managing vessels
     */
    manageVessels(295),

    /**
     * Viewing mobile terminal polls
     */
    viewMobileTerminalPolls(297),
    /**
     * Managing mobile terminal polls
     */
    managePolls(201),

    mobileTerminalPlugins(203),  // <<<<< 242

    /**
     * Viewing movements
     */
    viewMovements(267),
    /**
     * Viewing manual movements
     */
    viewManualMovements(276),
    /**
     * Managing movements
     */
    manageManualMovements(279),

    /**
     * Viewing exchange
     */
    viewExchange(249),
    /**
     * Managing sending queue
     */
    manageExchangeSendingQueue(204),
    /**
     * Managing transmission statuses
     */
    manageExchangeTransmissionStatuses(205),

    /**
     * Rules
     */
    viewAlarmRules(206),
    manageAlarmRules(207),
    viewAlarmsHoldingTable(208),
    manageAlarmsHoldingTable(209),
    viewAlarmsOpenTickets(210),
    manageAlarmsOpenTickets(211),
    manageGlobalAlarmsRules(215),

    /**
     * Viewing audit
     */
    viewAudit(286),

    /**
     * Configuration page
     */
    viewConfiguration(214),

    /**
     * Sales
     */
    viewSalesReports(-1),              // not in db
    manageSalesReports(-1);            // not in db


    private int featureId;

    UnionVMSFeature(int featureId) {
        this.featureId = featureId;
    }

    public int getFeatureId(){
        return this.featureId;
    }

}